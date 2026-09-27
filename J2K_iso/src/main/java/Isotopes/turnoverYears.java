package Isotopes;

/*
 * turnoverYears.java
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

/**
 * Mean turnover time of a store, in years, as a running diagnostic.
 *
 *     turnover = mean(storage) / mean(flux per year)
 *
 * Storage and flux must be in the SAME unit - both volumes, or both depths.
 * The ratio then carries no unit of its own, which is what makes this usable
 * against J2K's internal volumes without knowing whether they are litres or
 * millimetres.
 *
 * Why it is worth tracing
 * ----------------------
 * A linear-reservoir groundwater store is sized to reproduce a baseflow
 * recession, not to represent how much water is in the rock, and the two can
 * differ by more than an order of magnitude. Turnover time is the number that
 * exposes it: an RG2 store that empties and refills in weeks is not behaving
 * like an aquifer no matter how well it reproduces the hydrograph, and its
 * isotope signature will track recent rainfall instead of integrating it.
 *
 * Storage versus flow paths
 * -------------------------
 * The calculation is meaningful for any store, so it applies equally to a reach
 * as to groundwater - but the two answer different questions. For a groundwater
 * store the result is the residence time that governs isotopic damping, in
 * years. For a channel it is hydraulic travel time, typically hours, which
 * describes routing and says almost nothing about isotope mixing. Neither is
 * the catchment mean transit time of water arriving at the outlet: that is a
 * flow-weighted mixture over every upstream store and is not obtainable from a
 * single storage/flux pair.
 *
 * isotopeMixingProportion
 * -----------------------
 * Where the store is mixed by Isotopes.IsotopeMixer with a mixingProportion
 * below 1, the volume seen by the isotope mixing is storage/mixingProportion,
 * so the ISOTOPIC turnover is correspondingly longer than the hydraulic one.
 * Supplying the same value here reports both, which is what lets the calibrated
 * mixing proportion be checked against a residence time rather than taken on
 * trust.
 *
 * Accumulators are held in bound attributes rather than in Java fields, so the
 * component works unchanged inside a spatial context (per HRU or per reach) as
 * well as on catchment aggregates.
 *
 * @author Andrew Watson <awatson@sun.ac.za>
 */
@JAMSComponentDescription(
        title = "turnoverYears",
        author = "Andrew Watson",
        description = "Running mean turnover time of a store in years, computed as "
        + "mean storage divided by mean annual flux. Storage and flux must share a "
        + "unit; the ratio is unitless so it works directly on J2K's internal "
        + "volumes. Optionally also reports the isotopic turnover, which is longer "
        + "by 1/mixingProportion wherever an IsotopeMixer damps that store. "
        + "Meaningful for any store: for groundwater it is the residence time that "
        + "controls isotopic damping, for a channel it is hydraulic travel time. It "
        + "is NOT catchment mean transit time, which is a flow-weighted mixture over "
        + "all upstream stores.",
        date = "2026-09-03",
        version = "1.0_0"
)
@VersionComments(entries = {
    @VersionComments.Entry(version = "1.0_0", comment = "Initial version")
})
public class turnoverYears extends JAMSComponent {

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Water held in the store this time step, e.g. actRG2. Must "
            + "be in the same unit as flux."
    )
    public Attribute.Double storage;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Throughflow this time step - the inflow to the store, or "
            + "equivalently its outflow at steady state, e.g. percolation_s for RG2. "
            + "Must be in the same unit as storage."
    )
    public Attribute.Double flux;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Time steps per year, for converting the accumulated flux "
            + "to an annual rate. 365 for a daily model.",
            defaultValue = "365.0"
    )
    public Attribute.Double stepsPerYear;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "The mixingProportion applied to this store by its "
            + "IsotopeMixer, if any. Leave at 1 for the hydraulic turnover alone.",
            defaultValue = "1.0"
    )
    public Attribute.Double isotopeMixingProportion;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "Running total of storage. Wire to an attribute of the "
            + "enclosing context so that, inside a spatial context, each entity keeps "
            + "its own accumulator."
    )
    public Attribute.Double sumStorage;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "Running total of flux."
    )
    public Attribute.Double sumFlux;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "Running count of time steps."
    )
    public Attribute.Double stepCount;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "Hydraulic turnover time of the store [years]. Read it at "
            + "the end of the run, when the means have stabilised."
    )
    public Attribute.Double turnover;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "Turnover time seen by the isotopes [years] - the hydraulic "
            + "value divided by isotopeMixingProportion. Equal to it when that is 1."
    )
    public Attribute.Double isotopicTurnover = null;

    @Override
    public void init() {
        sumStorage.setValue(0.0);
        sumFlux.setValue(0.0);
        stepCount.setValue(0.0);
        turnover.setValue(0.0);
        if (isotopicTurnover != null) {
            isotopicTurnover.setValue(0.0);
        }
    }

    @Override
    public void run() {

        sumStorage.setValue(sumStorage.getValue() + storage.getValue());
        sumFlux.setValue(sumFlux.getValue() + flux.getValue());
        stepCount.setValue(stepCount.getValue() + 1.0);

        double n = stepCount.getValue();
        if (n <= 0.0) {
            return;
        }

        double meanStorage = sumStorage.getValue() / n;
        // accumulated flux spans n/stepsPerYear years, so the annual rate is
        double years = n / stepsPerYear.getValue();
        double fluxPerYear = (years > 0.0) ? sumFlux.getValue() / years : 0.0;

        // A store with no throughflow has an undefined turnover, not an infinite
        // one - report 0 rather than emitting an infinity into a datastore.
        double t = (fluxPerYear > 0.0) ? meanStorage / fluxPerYear : 0.0;
        if (Double.isNaN(t) || Double.isInfinite(t)) {
            t = 0.0;
        }
        turnover.setValue(t);

        if (isotopicTurnover != null) {
            double mp = isotopeMixingProportion.getValue();
            isotopicTurnover.setValue((mp > 0.0) ? t / mp : t);
        }
    }
}
