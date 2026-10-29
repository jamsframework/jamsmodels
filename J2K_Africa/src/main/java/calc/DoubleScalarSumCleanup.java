package calc;

/*
 * DoubleScalarSumCleanup.java
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

/**
 * Weighted sum of scalars, evaluated in the CLEANUP stage.
 *
 * Why this exists rather than jams.components.calc.DoubleScalarSum
 * ----------------------------------------------------------------
 * The OPTAS efficiency calculators (UniversalEfficiencyCalculator and
 * ...Serialized) collect their paired series during run() and only compute the
 * actual efficiency values in cleanup(). The stock DoubleScalarSum works in
 * run(), so wiring it to something like e_kge_normalized_RG2 reads the
 * attribute BEFORE it has been written - which does not fail, it just quietly
 * sums a zero. Every combined objective built that way is wrong in a way that
 * is very hard to see from the outside.
 *
 * This component does the same arithmetic in cleanup(). JAMSContext hands its
 * children to the cleanup stage in declaration order, so placing this AFTER the
 * efficiency calculators in the model file is what guarantees it sees finished
 * values.
 *
 * The intended use is assembling one optimiser objective out of an efficiency
 * plus one or more constraint penalties - for example a normalised RG2 isotope
 * KGE plus the recharge-range penalty from calc.RangeConstraintPenalty, so that
 * "the groundwater store behaves plausibly" and "the groundwater isotopes fit"
 * are treated as a single goal. Both terms must already follow the OPTAS
 * normalised convention, where zero is perfect and larger is worse.
 *
 * @author Andrew Watson <awatson@sun.ac.za>
 */
@JAMSComponentDescription(
        title = "DoubleScalarSumCleanup",
        author = "Andrew Watson",
        description = "Computes a weighted sum of double attributes during the "
        + "CLEANUP stage instead of the run stage. Needed when the terms are "
        + "produced by OPTAS efficiency calculators, which only write their "
        + "values in cleanup() - a run-stage adder would read them before they "
        + "exist and silently sum zeros. Place it after the efficiency "
        + "calculators in the model file, since children are cleaned up in "
        + "declaration order. Intended for assembling a single optimiser "
        + "objective from an efficiency plus constraint penalties, all on the "
        + "normalised convention where zero is perfect.",
        date = "2026-08-30",
        version = "1.0_0"
)
@VersionComments(entries = {
    @VersionComments.Entry(version = "1.0_0", comment = "Initial version")
})
public class DoubleScalarSumCleanup extends JAMSComponent {

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "The double attributes to add together, semicolon-separated."
    )
    public Attribute.Double[] values;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Optional semicolon-separated multiplier per term, same "
            + "count as values. Leave empty to weight every term at 1. Use this to "
            + "set how hard a penalty term bites relative to the efficiency it is "
            + "being added to.",
            defaultValue = ""
    )
    public Attribute.String weights;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "The resulting weighted sum."
    )
    public Attribute.Double result;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Ceiling for the result. An OPTAS efficiency comes back as "
            + "Double.MAX_VALUE (1.8e308) whenever the underlying comparison produced "
            + "NaN - typically a degenerate parameter set where the simulated series is "
            + "undefined. Left uncapped that value does not merely mark the solution as "
            + "bad, it destroys the scale of the objective and, in NSGA-II, the crowding "
            + "distance along that dimension. Capping turns it into a large but finite "
            + "penalty, so the offending solution is ranked last on its own merits.",
            defaultValue = "1.7976931348623157E308"
    )
    public Attribute.Double maxValue;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Print the term-by-term breakdown to the JAMS console. Turn "
            + "off for optimisation runs - it prints once per model evaluation.",
            defaultValue = "true"
    )
    public Attribute.Boolean verbose;

    private double[] w;

    @Override
    public void init() {
        if (values == null || values.length == 0) {
            getModel().getRuntime().sendHalt(getInstanceName()
                    + ": no values wired - nothing to sum.");
            return;
        }

        w = new double[values.length];
        String raw = (weights == null || weights.getValue() == null)
                ? "" : weights.getValue().trim();

        if (raw.isEmpty()) {
            for (int i = 0; i < w.length; i++) {
                w[i] = 1.0;
            }
            return;
        }

        String[] tokens = raw.split(";");
        int keep = 0;
        for (String t : tokens) {
            if (!t.trim().isEmpty()) {
                keep++;
            }
        }
        if (keep != values.length) {
            getModel().getRuntime().sendHalt(getInstanceName() + ": weights has "
                    + keep + " entries but values has " + values.length
                    + " - they must match, or leave weights empty.");
            return;
        }

        int j = 0;
        for (String t : tokens) {
            if (t.trim().isEmpty()) {
                continue;
            }
            try {
                w[j++] = Double.parseDouble(t.trim());
            } catch (NumberFormatException e) {
                getModel().getRuntime().sendHalt(getInstanceName()
                        + ": weight \"" + t.trim() + "\" is not a number.");
                return;
            }
        }
    }

    @Override
    public void cleanup() {
        double sum = 0.0;
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("%n%s%n", getInstanceName()));

        boolean sawNaN = false;
        for (int i = 0; i < values.length; i++) {
            double v = values[i].getValue();
            if (Double.isNaN(v)) {
                sawNaN = true;
            }
            double term = w[i] * v;
            sum += term;
            sb.append(String.format("  term %d: %14.6f  x weight %8.4f  = %14.6f%n",
                    i + 1, v, w[i], term));
        }

        // Clamp before writing: see maxValue. A non-finite sum is also pinned to the
        // ceiling rather than propagated, for the same reason.
        double capped = sum;
        if (Double.isNaN(capped) || capped > maxValue.getValue()) {
            capped = maxValue.getValue();
            sb.append(String.format("  %-46s   capped at %.4g%n", "(non-finite or over ceiling)",
                    maxValue.getValue()));
        }
        result.setValue(capped);
        sb.append(String.format("  %-46s = %14.6f%n", "TOTAL", capped));

        if (verbose.getValue()) {
            getModel().getRuntime().println(sb.toString());
        }

        // A NaN term is almost always an efficiency computed from an empty
        // overlap between observed and simulated series. Propagating it would
        // give the optimiser an objective it cannot rank, so say so loudly
        // rather than letting it disappear into a comparison.
        if (sawNaN) {
            getModel().getRuntime().println(getInstanceName() + ": WARNING - at least "
                    + "one term is NaN, so the result is NaN. This usually means an "
                    + "efficiency calculator found no overlapping observed/simulated "
                    + "values.");
        }
    }
}
