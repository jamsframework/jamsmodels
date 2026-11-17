package Isotopes;

/*
 * turnoverYearsByClass.java
 * Created on 03.09.2026
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
 * Mean turnover time of a store in years, resolved PER CLASS of a categorical
 * entity attribute - normally per hydrogeology unit.
 *
 *     turnover[c] = mean(storage over class c) / mean(annual flux into class c)
 *
 * Why per class rather than catchment-wide
 * ----------------------------------------
 * Isotopes.turnoverYears reports one number for the whole catchment, which is
 * the right diagnostic for a single lumped store but cannot be checked against
 * the evidence that actually constrains groundwater age. Tritium is measured in
 * boreholes, and those boreholes sit in different geology: a TMG borehole and an
 * alluvial one carry different ages, and a catchment mean sits somewhere between
 * them, matching neither. Splitting the turnover by hgeoID puts the model
 * diagnostic on the same footing as the measurement, so a modelled TMG residence
 * time can be compared against TMG tritium instead of against an average that no
 * sample represents.
 *
 * This is only informative because the controlling parameters are distributed
 * the same way - the SwitchContext on hgeoID that scales soilLatVertLPS per
 * unit, and the per-HRU RG1_k/RG2_k from hgeo.par. A per-class diagnostic over
 * spatially uniform parameters would just report the same number several times.
 * The same argument is made at greater length in calc.RangeConstraintPenalty,
 * whose class-matching semantics (including the "*" wildcard) this component
 * deliberately mirrors so that the two can be configured from the same class
 * list.
 *
 * Units
 * -----
 * Storage and flux must share a unit; the ratio is then unitless and works
 * directly on J2K's internal volumes without needing to know whether they are
 * litres or millimetres. Supply areaAttribute ONLY if the two are depths [mm],
 * in which case both are area-weighted before summing across entities. For the
 * groundwater stores (actRG1/actRG2 against rechargeRG1/rechargeRG2) both are
 * volumes and areaAttribute should be left empty.
 *
 * What this is NOT
 * ----------------
 * It is a storage/flux residence time for the store in each class, not a
 * catchment mean transit time, and not a tritium age. Comparing it against a
 * tritium-derived MTT is comparing a single well-mixed reservoir against a real
 * transit-time distribution, which is informative about order of magnitude and
 * about relative differences between units, but should not be read as a like-for-
 * like age. See the isotopeMixingProportion note below.
 *
 * @author Andrew Watson <awatson@sun.ac.za>
 */
@JAMSComponentDescription(
        title = "turnoverYearsByClass",
        author = "Andrew Watson",
        description = "Running mean turnover time of a store in years, computed "
        + "separately for each class of a categorical entity attribute such as "
        + "hgeoID. Reports mean storage divided by mean annual flux per class, so "
        + "a modelled residence time can be compared against tritium measured in "
        + "boreholes of that geology rather than against a catchment average. "
        + "Storage and flux must share a unit. Optionally also reports the "
        + "isotopic turnover, longer by 1/mixingProportion wherever an "
        + "IsotopeMixer damps the store. Class matching, including the \"*\" "
        + "wildcard, follows calc.RangeConstraintPenalty.",
        date = "2026-09-03",
        version = "1.0_0"
)
@VersionComments(entries = {
    @VersionComments.Entry(version = "1.0_0", comment = "Initial version")
})
public class turnoverYearsByClass extends JAMSComponent {

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "The entity collection to aggregate over, normally the HRU "
            + "collection (hrus)."
    )
    public Attribute.EntityCollection entities;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Name of the per-entity attribute holding the water in the "
            + "store this time step, e.g. actRG2. Must share a unit with fluxAttribute.",
            defaultValue = "actRG2"
    )
    public Attribute.String storageAttribute;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Name of the per-entity attribute holding the throughflow "
            + "this time step - the inflow to the store, e.g. rechargeRG2. Must share a "
            + "unit with storageAttribute. Use the true recharge flux, NOT a difference "
            + "of a flux and a storage.",
            defaultValue = "rechargeRG2"
    )
    public Attribute.String fluxAttribute;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Name of the categorical entity attribute the classes are "
            + "declared against, e.g. hgeoID.",
            defaultValue = "hgeoID"
    )
    public Attribute.String classAttribute;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Semicolon-separated list of class values to report, e.g. "
            + "\"*;1;3;5;6\". The token \"*\" matches every entity and gives the "
            + "catchment-wide turnover alongside the per-class ones. turnovers must be "
            + "wired to the same number of attributes.",
            defaultValue = "*"
    )
    public Attribute.String classIDs;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Name of the entity attribute used to area-weight the "
            + "aggregation. Leave EMPTY when storage and flux are volumes, which is the "
            + "case for J2K's actRG1/actRG2 and rechargeRG1/rechargeRG2. Set it to area "
            + "only if both are depths [mm].",
            defaultValue = ""
    )
    public Attribute.String areaAttribute;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Time steps per year, for converting the accumulated flux to "
            + "an annual rate. 365 for a daily model.",
            defaultValue = "365.0"
    )
    public Attribute.Double stepsPerYear;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "The mixingProportion applied to this store by its "
            + "IsotopeMixer, if any. Leave at 1 for the hydraulic turnover alone. Where "
            + "it is below 1 the mixer sees a volume of storage/mixingProportion, so the "
            + "isotopic turnover is correspondingly longer - which is the number to "
            + "compare against tritium, since it is the volume the tracer actually "
            + "samples.",
            defaultValue = "1.0"
    )
    public Attribute.Double isotopeMixingProportion;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "Hydraulic turnover time per class [years], in the order the "
            + "classes were declared. Wire one attribute per class. Read at the end of "
            + "the run, when the running means have stabilised."
    )
    public Attribute.Double[] turnovers = null;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "Optional isotopic turnover per class [years] - the hydraulic "
            + "value divided by isotopeMixingProportion. Same order as classIDs."
    )
    public Attribute.Double[] isotopicTurnovers = null;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Print the per-class summary table once at cleanup. Safe to "
            + "leave on for a single run; turn it OFF for optimisation, where it would "
            + "print once per model evaluation.",
            defaultValue = "true"
    )
    public Attribute.Boolean verbose;

    // Parsed configuration, resolved once in init().
    private String[] classTokens;
    private double[] classValues;
    private boolean[] wildcard;
    private String storName, fluxName, className, areaName;
    private boolean useArea;

    // Accumulators. Held in Java fields rather than bound attributes because this
    // component aggregates ACROSS entities and so is placed in the time loop, not
    // inside a spatial context - there is no per-entity state to keep.
    private double[] sumStorage;
    private double[] sumFlux;
    private int[] count;
    private double steps;
    private boolean checked;

    @Override
    public void init() {

        classTokens = split(classIDs);
        if (classTokens.length == 0) {
            getModel().getRuntime().sendHalt(getInstanceName()
                    + ": classIDs is empty - nothing to report.");
            return;
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
        }

        storName = trimmed(storageAttribute);
        fluxName = trimmed(fluxAttribute);
        className = trimmed(classAttribute);
        areaName = trimmed(areaAttribute);
        useArea = !areaName.isEmpty();

        if (stepsPerYear.getValue() <= 0.0) {
            getModel().getRuntime().sendHalt(getInstanceName()
                    + ": stepsPerYear must be greater than zero.");
            return;
        }

        // Checked here rather than at the end of a long run.
        if (turnovers != null && turnovers.length != classTokens.length) {
            getModel().getRuntime().sendHalt(getInstanceName() + ": turnovers is wired "
                    + "to " + turnovers.length + " attributes but there are "
                    + classTokens.length + " classes - wire one per class.");
            return;
        }
        if (isotopicTurnovers != null && isotopicTurnovers.length != classTokens.length) {
            getModel().getRuntime().sendHalt(getInstanceName() + ": isotopicTurnovers is "
                    + "wired to " + isotopicTurnovers.length + " attributes but there "
                    + "are " + classTokens.length + " classes - wire one per class, or "
                    + "none.");
            return;
        }

        // Zeroed on every init(). During optimisation the model is re-initialised
        // per evaluation, so this is what stops one parameter set blending into
        // the next - the same hazard calc.RangeConstraintPenalty guards against by
        // zeroing in run().
        sumStorage = new double[classTokens.length];
        sumFlux = new double[classTokens.length];
        count = new int[classTokens.length];
        steps = 0.0;
        checked = false;
    }

    @Override
    public void run() {

        List<Attribute.Entity> list = entities.getEntities();
        if (list == null || list.isEmpty()) {
            getModel().getRuntime().sendHalt(getInstanceName()
                    + ": the entity collection is empty.");
            return;
        }

        int n = classTokens.length;
        double[] stepStorage = new double[n];
        double[] stepFlux = new double[n];

        for (Attribute.Entity e : list) {

            if (!checked) {
                requireAttribute(e, storName, "storageAttribute");
                requireAttribute(e, fluxName, "fluxAttribute");
                requireAttribute(e, className, "classAttribute");
                if (useArea) {
                    requireAttribute(e, areaName, "areaAttribute");
                }
                checked = true;
            }

            double stor = e.getDouble(storName);
            double flux = e.getDouble(fluxName);
            double cls = e.getDouble(className);
            double a = useArea ? e.getDouble(areaName) : 1.0;

            for (int i = 0; i < n; i++) {
                if (!wildcard[i] && Math.abs(cls - classValues[i]) > 1e-6) {
                    continue;
                }
                stepStorage[i] += stor * a;
                stepFlux[i] += flux * a;
                if (steps == 0.0) {
                    count[i]++;
                }
            }
        }

        steps += 1.0;
        for (int i = 0; i < n; i++) {
            sumStorage[i] += stepStorage[i];
            sumFlux[i] += stepFlux[i];
        }

        // Written every step so the value is available to a datastore traced at any
        // point, and is final once the last step has run.
        double years = steps / stepsPerYear.getValue();
        double mp = isotopeMixingProportion.getValue();

        for (int i = 0; i < n; i++) {

            double t = 0.0;
            if (count[i] > 0 && years > 0.0) {
                double meanStorage = sumStorage[i] / steps;
                double fluxPerYear = sumFlux[i] / years;
                // A store with no throughflow has an undefined turnover, not an
                // infinite one - report 0 rather than emitting an infinity.
                if (fluxPerYear > 0.0) {
                    t = meanStorage / fluxPerYear;
                }
                if (Double.isNaN(t) || Double.isInfinite(t)) {
                    t = 0.0;
                }
            }

            if (turnovers != null && turnovers[i] != null) {
                turnovers[i].setValue(t);
            }
            if (isotopicTurnovers != null && isotopicTurnovers[i] != null) {
                isotopicTurnovers[i].setValue((mp > 0.0) ? t / mp : t);
            }
        }
    }

    @Override
    public void cleanup() {

        if (!verbose.getValue() || classTokens == null) {
            return;
        }

        StringBuilder r = new StringBuilder();
        r.append(String.format("%n%s  (%s / %s, %.0f steps)%n", getInstanceName(),
                storName, fluxName, steps));
        r.append(String.format("  %-8s %6s %14s %16s%n",
                "class", "nHRU", "turnover yr", "isotopic yr"));

        double mp = isotopeMixingProportion.getValue();
        for (int i = 0; i < classTokens.length; i++) {
            double t = (turnovers != null && turnovers[i] != null)
                    ? turnovers[i].getValue() : Double.NaN;
            r.append(String.format("  %-8s %6d %14s %16s%n",
                    classTokens[i], count[i],
                    Double.isNaN(t) ? "-" : String.format("%.3f", t),
                    Double.isNaN(t) ? "-" : String.format("%.3f",
                            (mp > 0.0) ? t / mp : t)));
        }
        if (mp > 0.0 && mp < 1.0) {
            r.append(String.format("  isotopic = hydraulic / mixingProportion "
                    + "(%.4f); it is the isotopic column that is comparable with "
                    + "tritium.%n", mp));
        }
        getModel().getRuntime().println(r.toString());
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
        for (String x : raw) {
            if (!x.trim().isEmpty()) {
                keep++;
            }
        }
        String[] out = new String[keep];
        int j = 0;
        for (String x : raw) {
            if (!x.trim().isEmpty()) {
                out[j++] = x.trim();
            }
        }
        return out;
    }
}
