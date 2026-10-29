package calc;

/*
 * RangeConstraintPenalty.java
 * Created on 30.08.2026
 *
 * This file is part of JAMS
 * Copyright (C) FSU Jena
 *
 * JAMS is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * JAMS is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with JAMS. If not, see <http://www.gnu.org/licenses/>.
 *
 */
import jams.data.*;
import jams.model.*;

import java.util.List;

/**
 * Turns expert knowledge about an expected flux ratio - "TMG recharge should be
 * 20-28% of MAP" - into an objective the OPTAS optimiser can minimise, WITHOUT
 * collapsing that expectation onto a single number.
 *
 * The problem this solves
 * -----------------------
 * A normal efficiency calculator compares against a point target. Aiming one at
 * a recharge percentage would make recharge a fitted quantity and force the
 * calibration onto exactly one value, which is not what a range like 20-28%
 * means - inside that band we have no preference at all, and want the streamflow
 * and isotope objectives to decide.
 *
 * So the penalty here is FLAT-BOTTOMED (a dead zone):
 *
 *     d       = max(0, lower - r, r - upper)      // 0 anywhere inside the band
 *     penalty = weight * (d / scale)^exponent
 *
 * Every parameter set landing anywhere in [lower, upper] scores exactly 0, so
 * the optimiser ranks those purely on the other objectives. Outside the band the
 * penalty grows with DISTANCE, which matters: a flat "death penalty" outside
 * would give the optimiser no direction back towards feasibility, whereas a
 * graded one gives it a slope to follow.
 *
 * Because 0 is perfect and larger is worse, the output matches the OPTAS
 * "normalized" convention (compare optas.efficiencies.KGE.calcNormative, which
 * returns 1 - KGE). OptimizerWrapper.effValue is a plain Attribute.Double[], so
 * this attribute can be listed as an objective directly, or - usually better -
 * added onto an existing normalised efficiency. Adding it costs no extra Pareto
 * dimension, and since the penalty is 0 inside the band it does not distort the
 * ranking between acceptable solutions at all; it only pushes away unacceptable
 * ones.
 *
 * Generic by class
 * ----------------
 * Bands are declared per class of a categorical entity attribute - here the
 * hydrogeology ID - so a different expected recharge range can be given for each
 * geology unit, and each contributes its own penalty term. This is only useful
 * because the controlling parameter has been distributed the same way (the
 * SwitchContext on hgeoID that scales soilLatVertLPS by soilLatVertLPS_TMG vs
 * soilLatVertLPS_basin): the optimiser needs a per-class handle before a
 * per-class constraint can be satisfied. Constraining a class whose parameters
 * are shared with everything else just makes the objective harder to satisfy
 * without giving the optimiser any way to satisfy it.
 *
 * The class token "*" matches every entity, giving a catchment-wide band
 * alongside (or instead of) the per-class ones.
 *
 * Nothing here is recharge-specific: it constrains
 * sum(numerator*area) / sum(denominator*area) for any two accumulated entity
 * attributes. Point at percolation_s_sum / precip_sum for recharge as a
 * percentage of rainfall, or at anything else with the same shape.
 *
 * @author Andrew Watson <awatson@sun.ac.za>
 */
@JAMSComponentDescription(
        title = "RangeConstraintPenalty",
        author = "Andrew Watson",
        description = "Computes an area-weighted flux ratio (e.g. recharge as a "
        + "percentage of rainfall) per class of a categorical entity attribute "
        + "(e.g. hydrogeology ID), and converts each against an expected RANGE "
        + "into a flat-bottomed penalty: exactly zero anywhere inside the range, "
        + "growing with distance outside it. Intended as an optimiser objective "
        + "(0 = perfect, matching the OPTAS normalised convention) so that "
        + "process behaviour can be constrained to a plausible band without "
        + "forcing it onto a single value. Bands are given per class, so each "
        + "geology unit can carry its own expected range.",
        date = "2026-08-30",
        version = "1.0_0"
)
@VersionComments(entries = {
    @VersionComments.Entry(version = "1.0_0", comment = "Initial version")
})
public class RangeConstraintPenalty extends JAMSComponent {

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "The entity collection to aggregate over, normally the "
            + "HRU collection (hrus)."
    )
    public Attribute.EntityCollection entities;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Name of the entity attribute forming the NUMERATOR of the "
            + "ratio, already accumulated over the whole run [mm]. For recharge this "
            + "is percolation_s_sum, the post-episodic-recharge percolation summed by "
            + "the rechargeAggregator.",
            defaultValue = "percolation_s_sum"
    )
    public Attribute.String numeratorAttribute;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Name of the entity attribute forming the DENOMINATOR [mm]. "
            + "Use precip_sum for a percentage of rainfall (MAP). NOTE that this is "
            + "NOT the same as the totalIn used by the existing percolationPercentage "
            + "calculation: totalIn adds inRD1_sum + inRD2_sum, i.e. run-on received "
            + "from upslope HRUs, so a ratio against it is systematically lower than a "
            + "ratio against precipitation and should not be compared against a band "
            + "expressed as a percentage of MAP.",
            defaultValue = "precip_sum"
    )
    public Attribute.String denominatorAttribute;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Name of the entity attribute used to area-weight the "
            + "aggregation. Both numerator and denominator are depths [mm], so they "
            + "must be weighted by area before summing across entities or large and "
            + "small HRUs count equally. Leave empty for an unweighted mean.",
            defaultValue = "area"
    )
    public Attribute.String areaAttribute;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Name of the categorical entity attribute the bands are "
            + "declared against, e.g. hgeoID.",
            defaultValue = "hgeoID"
    )
    public Attribute.String classAttribute;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Semicolon-separated list of class values to constrain, e.g. "
            + "\"1;3;5;6\". The special token \"*\" matches every entity and gives a "
            + "catchment-wide band. lowerBounds and upperBounds must have the same "
            + "number of entries as this list.",
            defaultValue = "*"
    )
    public Attribute.String classIDs;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Semicolon-separated LOWER bounds of the expected range, one "
            + "per class, as a PERCENTAGE (e.g. \"20\" for 20% of MAP).",
            defaultValue = "0"
    )
    public Attribute.String lowerBounds;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Semicolon-separated UPPER bounds of the expected range, one "
            + "per class, as a PERCENTAGE. Setting upper equal to lower degenerates to "
            + "a point target with a distance-based penalty, which is occasionally "
            + "useful but gives up the whole benefit of a range.",
            defaultValue = "100"
    )
    public Attribute.String upperBounds;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Optional semicolon-separated relative weight per class. "
            + "Leave empty to weight every class equally at 1. Use this when one class "
            + "is far better constrained by field evidence than the others.",
            defaultValue = ""
    )
    public Attribute.String classWeights;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "How many PERCENTAGE POINTS outside the band cost one unit of "
            + "penalty (before the class weight). Sets how hard the constraint bites "
            + "relative to the efficiency it is added to: with scale 4 and exponent 2, "
            + "being 4 points outside costs 1.0, and 2 points outside costs 0.25. Scale "
            + "it against a meaningful loss in the objective it joins - if adding to a "
            + "normalised KGE, a class weight around 0.1 makes 4 points outside worth "
            + "about 0.1 KGE. Too heavy and this becomes a hard constraint again; too "
            + "light and the optimiser ignores it.",
            defaultValue = "4.0"
    )
    public Attribute.Double scale;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Exponent applied to the normalised distance outside the "
            + "band. 2 (default) is gentle just outside and bites harder further out; "
            + "1 is a constant-slope penalty.",
            defaultValue = "2.0"
    )
    public Attribute.Double exponent;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "The summed penalty across all classes. ZERO IS PERFECT and "
            + "larger is worse, matching the OPTAS normalised-efficiency convention, so "
            + "this can be listed directly in the optimiser's effValue list or added "
            + "onto an existing normalised efficiency."
    )
    public Attribute.Double penalty;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "Optional per-class ratio [percent], in the order the classes "
            + "were declared. Wire one attribute name per class, semicolon-separated. "
            + "Worth tracing during optimisation even if no penalty is applied: it tells "
            + "you where the unconstrained optimum actually sits, and lets the Pareto "
            + "set be filtered for behavioural solutions afterwards."
    )
    public Attribute.Double[] fractions = null;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "Optional per-class penalty contribution, same order as "
            + "classIDs. Useful for seeing which class is actually driving the "
            + "constraint."
    )
    public Attribute.Double[] classPenalties = null;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Print the per-class summary table to the JAMS console at the "
            + "end of the run. Turn this OFF for optimisation runs - it prints once per "
            + "model evaluation.",
            defaultValue = "true"
    )
    public Attribute.Boolean verbose;

    // Parsed configuration, resolved once in init().
    private String[] classTokens;
    private double[] classValues;
    private boolean[] wildcard;
    private double[] lower;
    private double[] upper;
    private double[] weight;

    private String numName, denName, areaName, className;
    private boolean useArea;

    @Override
    public void init() {

        classTokens = split(classIDs);
        if (classTokens.length == 0) {
            getModel().getRuntime().sendHalt(getInstanceName()
                    + ": classIDs is empty - nothing to constrain.");
            return;
        }

        double[] lo = parseNumbers(lowerBounds, "lowerBounds");
        double[] hi = parseNumbers(upperBounds, "upperBounds");

        if (lo == null || hi == null) {
            return;
        }
        if (lo.length != classTokens.length || hi.length != classTokens.length) {
            getModel().getRuntime().sendHalt(getInstanceName() + ": classIDs has "
                    + classTokens.length + " entries but lowerBounds has " + lo.length
                    + " and upperBounds has " + hi.length
                    + " - these three lists must be the same length.");
            return;
        }

        // Weights are optional; absent means every class counts the same.
        double[] wt = new double[classTokens.length];
        String rawWeights = (classWeights == null) ? "" : classWeights.getValue();
        if (rawWeights == null || rawWeights.trim().isEmpty()) {
            for (int i = 0; i < wt.length; i++) {
                wt[i] = 1.0;
            }
        } else {
            double[] parsed = parseNumbers(classWeights, "classWeights");
            if (parsed == null) {
                return;
            }
            if (parsed.length != classTokens.length) {
                getModel().getRuntime().sendHalt(getInstanceName() + ": classWeights has "
                        + parsed.length + " entries but there are " + classTokens.length
                        + " classes. Leave classWeights empty to weight them equally.");
                return;
            }
            wt = parsed;
        }

        classValues = new double[classTokens.length];
        wildcard = new boolean[classTokens.length];
        for (int i = 0; i < classTokens.length; i++) {
            if ("*".equals(classTokens[i])) {
                wildcard[i] = true;
            } else {
                try {
                    classValues[i] = Double.parseDouble(classTokens[i]);
                } catch (NumberFormatException e) {
                    getModel().getRuntime().sendHalt(getInstanceName()
                            + ": class token \"" + classTokens[i] + "\" is neither a "
                            + "number nor the wildcard \"*\".");
                    return;
                }
            }
            if (lo[i] > hi[i]) {
                getModel().getRuntime().sendHalt(getInstanceName() + ": class "
                        + classTokens[i] + " has lower bound " + lo[i]
                        + " above upper bound " + hi[i] + ".");
                return;
            }
        }

        lower = lo;
        upper = hi;
        weight = wt;

        numName = trimmed(numeratorAttribute);
        denName = trimmed(denominatorAttribute);
        className = trimmed(classAttribute);
        areaName = trimmed(areaAttribute);
        useArea = !areaName.isEmpty();

        if (scale.getValue() <= 0.0) {
            getModel().getRuntime().sendHalt(getInstanceName()
                    + ": scale must be greater than zero.");
        }

        // Sized here rather than in run() so a mismatch is reported before the
        // model spends an hour producing a number nobody can read.
        if (fractions != null && fractions.length != classTokens.length) {
            getModel().getRuntime().sendHalt(getInstanceName() + ": fractions is wired "
                    + "to " + fractions.length + " attributes but there are "
                    + classTokens.length + " classes - wire one per class, or none.");
        }
        if (classPenalties != null && classPenalties.length != classTokens.length) {
            getModel().getRuntime().sendHalt(getInstanceName() + ": classPenalties is "
                    + "wired to " + classPenalties.length + " attributes but there are "
                    + classTokens.length + " classes - wire one per class, or none.");
        }
    }

    @Override
    public void run() {

        List<Attribute.Entity> hrus = entities.getEntities();
        if (hrus == null || hrus.isEmpty()) {
            getModel().getRuntime().sendHalt(getInstanceName()
                    + ": the entity collection is empty.");
            return;
        }

        int n = classTokens.length;

        // Zeroed on every run() rather than in init(). During optimisation the
        // model is evaluated hundreds of times; accumulators surviving between
        // evaluations would silently blend one parameter set into the next.
        double[] numSum = new double[n];
        double[] denSum = new double[n];
        double[] areaSum = new double[n];
        int[] count = new int[n];
        int unmatched = 0;

        boolean checked = false;

        for (Attribute.Entity e : hrus) {

            if (!checked) {
                requireAttribute(e, numName, "numeratorAttribute");
                requireAttribute(e, denName, "denominatorAttribute");
                requireAttribute(e, className, "classAttribute");
                if (useArea) {
                    requireAttribute(e, areaName, "areaAttribute");
                }
                checked = true;
            }

            double num = e.getDouble(numName);
            double den = e.getDouble(denName);
            double cls = e.getDouble(className);
            double a = useArea ? e.getDouble(areaName) : 1.0;

            boolean matchedAny = false;
            for (int i = 0; i < n; i++) {
                if (!wildcard[i] && Math.abs(cls - classValues[i]) > 1e-6) {
                    continue;
                }
                if (!wildcard[i]) {
                    matchedAny = true;
                }
                numSum[i] += num * a;
                denSum[i] += den * a;
                areaSum[i] += a;
                count[i]++;
            }
            if (!matchedAny) {
                unmatched++;
            }
        }

        double total = 0.0;
        StringBuilder report = new StringBuilder();
        report.append(String.format("%n%s%n", getInstanceName()));
        report.append(String.format("  %-8s %6s %10s %10s %14s %10s%n",
                "class", "n", "area", "ratio %", "band %", "penalty"));

        for (int i = 0; i < n; i++) {

            double ratio = Double.NaN;
            double p = 0.0;

            if (count[i] == 0) {
                // A configuration error, not something the optimiser can exploit:
                // class membership does not change with the parameters. Warn and
                // contribute nothing rather than halting a long run.
                getModel().getRuntime().println(getInstanceName() + ": WARNING - class "
                        + classTokens[i] + " matched no entities of " + classAttribute.getValue()
                        + "; its band is being ignored.");
            } else if (denSum[i] <= 0.0) {
                getModel().getRuntime().println(getInstanceName() + ": WARNING - class "
                        + classTokens[i] + " has a zero " + denName
                        + " total; its band is being ignored.");
            } else {
                ratio = 100.0 * numSum[i] / denSum[i];

                double d = 0.0;
                if (ratio < lower[i]) {
                    d = lower[i] - ratio;
                } else if (ratio > upper[i]) {
                    d = ratio - upper[i];
                }
                // d == 0 anywhere inside the band, so p == 0 there: the flat
                // bottom that stops this behaving like a point target.
                p = weight[i] * Math.pow(d / scale.getValue(), exponent.getValue());
                total += p;
            }

            if (fractions != null && fractions[i] != null) {
                fractions[i].setValue(ratio);
            }
            if (classPenalties != null && classPenalties[i] != null) {
                classPenalties[i].setValue(p);
            }

            report.append(String.format("  %-8s %6d %10.2f %10s %14s %10.4f%n",
                    classTokens[i],
                    count[i],
                    areaSum[i],
                    Double.isNaN(ratio) ? "-" : String.format("%.2f", ratio),
                    String.format("%.1f - %.1f", lower[i], upper[i]),
                    p));
        }

        penalty.setValue(total);

        report.append(String.format("  %-8s %6s %10s %10s %14s %10.4f%n",
                "", "", "", "", "TOTAL", total));
        if (unmatched > 0) {
            report.append(String.format("  %d entities matched no declared class "
                    + "(they still count towards any \"*\" band).%n", unmatched));
        }

        if (verbose.getValue()) {
            getModel().getRuntime().println(report.toString());
        }
    }

    private void requireAttribute(Attribute.Entity e, String name, String varName) {
        if (name.isEmpty()) {
            getModel().getRuntime().sendHalt(getInstanceName() + ": " + varName
                    + " must be set.");
        } else if (!e.existsAttribute(name)) {
            getModel().getRuntime().sendHalt(getInstanceName() + ": " + varName
                    + " names \"" + name + "\", which does not exist on the entities. "
                    + "Check that the component producing it runs before this one.");
        }
    }

    private String trimmed(Attribute.String s) {
        return (s == null || s.getValue() == null) ? "" : s.getValue().trim();
    }

    private static String[] split(Attribute.String s) {
        if (s == null || s.getValue() == null) {
            return new String[0];
        }
        String[] raw = s.getValue().split(";");
        int keep = 0;
        for (String r : raw) {
            if (!r.trim().isEmpty()) {
                keep++;
            }
        }
        String[] out = new String[keep];
        int j = 0;
        for (String r : raw) {
            if (!r.trim().isEmpty()) {
                out[j++] = r.trim();
            }
        }
        return out;
    }

    private double[] parseNumbers(Attribute.String s, String varName) {
        String[] tokens = split(s);
        double[] out = new double[tokens.length];
        for (int i = 0; i < tokens.length; i++) {
            try {
                out[i] = Double.parseDouble(tokens[i]);
            } catch (NumberFormatException e) {
                getModel().getRuntime().sendHalt(getInstanceName() + ": " + varName
                        + " entry \"" + tokens[i] + "\" is not a number.");
                return null;
            }
        }
        return out;
    }
}
