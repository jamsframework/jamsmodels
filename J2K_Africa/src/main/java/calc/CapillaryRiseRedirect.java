package calc;

/*
 * CapillaryRiseRedirect.java
 * Created on 08.09.2026
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
 * Makes capillary rise draw from the SHALLOW groundwater store instead of the
 * deep one, without modifying J2KProcessGroundwater.
 *
 * The problem
 * -----------
 * J2KProcessGroundwater satisfies the soil-moisture deficit from RG2 and only
 * from RG2:
 *
 *     inSoilStor = deltaSoilStor * (1 - exp(-gwCapRise / sat_SoilStor));
 *     actSoilStorage += inSoilStor;
 *     actRG2         -= inSoilStor;
 *
 * RG1 is never touched. Capillary rise is a process of centimetres to a metre or
 * so above a water table, so sourcing it from the store that represents deep,
 * slowly-circulating groundwater is not physically defensible - and it is
 * quantitatively severe here. Measured on this catchment, a slow store given a
 * 20-year residence time realises 12.2 years with gwCapRise at zero but only 4.2
 * years at the calibrated 0.157, because most of what recharges the deep store is
 * pulled back into the root zone before it can age. Any groundwater age the model
 * reports is then governed by capillary rise rather than by residence time, which
 * makes it useless for comparison against a tritium age.
 *
 * How this fixes it without touching J2K_base
 * -------------------------------------------
 * J2KProcessGroundwater alters the soil storage attribute in exactly one place -
 * the capillary-rise block above - so the CHANGE in soil storage across that
 * component is precisely the volume it removed from RG2. Snapshot the soil store
 * immediately before it runs, take the difference immediately after, and the
 * transfer can be undone and re-sourced:
 *
 *     taken = soilStorage_after - soilStorage_before
 *     deep    += taken            // give the deep store its water back
 *     shallow -= taken            // and charge the shallow store instead
 *
 * The soil is left exactly as J2KProcessGroundwater set it, so evapotranspiration
 * and every downstream soil process are unaffected; only the SOURCE of the water
 * changes. Because the amount is measured rather than recomputed, this cannot
 * drift out of step if the capillary-rise formula in J2K_base is ever changed.
 *
 * Wiring - the component is used TWICE
 * ------------------------------------
 * Instance 1, mode "snapshot", placed immediately BEFORE J2KProcessGroundwater.
 * Instance 2, mode "redirect", placed immediately AFTER it, sharing the same
 * snapshot attribute. Both must sit inside the HRU loop, and the snapshot
 * attribute must belong to the enclosing spatial context so that each entity
 * keeps its own value.
 *
 * When the shallow store cannot cover it
 * --------------------------------------
 * Only what the shallow store actually holds is taken from it; any remainder is
 * left with the deep store, as J2KProcessGroundwater originally had it. That is
 * the physically sensible fallback - if the shallow store is dry, a deeper source
 * is what remains - and it means the correction can never drive a store negative
 * or create water. shortfall reports how much had to be left behind, which is
 * worth tracing: if it is routinely large, the shallow store is too small for the
 * evaporative demand being placed on it and that is a calibration problem in its
 * own right.
 *
 * @author Andrew Watson <awatson@sun.ac.za>
 */
@JAMSComponentDescription(
        title = "CapillaryRiseRedirect",
        author = "Andrew Watson",
        description = "Re-sources capillary rise from the shallow groundwater store "
        + "instead of the deep one, without modifying J2KProcessGroundwater. That "
        + "component meets the soil deficit from RG2 alone, which is not physical for "
        + "a deep store and which dominates the deep store's apparent age - a 20-year "
        + "residence time realises 4.2 years at the calibrated capillary rise against "
        + "12.2 years at zero. Used as a pair of instances, one snapshotting the soil "
        + "store before J2KProcessGroundwater and one redirecting after it, measuring "
        + "the transferred volume rather than recomputing it so the two cannot drift "
        + "apart. Soil moisture is left untouched; only the source changes.",
        date = "2026-09-08",
        version = "1.0_0"
)
@VersionComments(entries = {
    @VersionComments.Entry(version = "1.0_0", comment = "Initial version")
})
public class CapillaryRiseRedirect extends JAMSComponent {

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "\"snapshot\" for the instance placed BEFORE "
            + "J2KProcessGroundwater, \"redirect\" for the one placed AFTER it. Any "
            + "other value halts the model rather than silently doing nothing.",
            defaultValue = "snapshot"
    )
    public Attribute.String mode;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "The soil storage attribute J2KProcessGroundwater fills by "
            + "capillary rise - actMPS in the standard wiring. Read only; this "
            + "component never changes it."
    )
    public Attribute.Double soilStorage;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "Per-entity store for the pre-capillary-rise soil volume. "
            + "Must be an attribute of the enclosing spatial context, and the SAME "
            + "attribute for both instances."
    )
    public Attribute.Double soilSnapshot;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "The DEEP store that capillary rise wrongly drew from and "
            + "that is being repaid - actRG2. Only used in \"redirect\" mode."
    )
    public Attribute.Double deepStore = null;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "The SHALLOW store that should have supplied the water - "
            + "actRG1. Only used in \"redirect\" mode."
    )
    public Attribute.Double shallowStore = null;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "Volume actually moved from the shallow store to the deep one "
            + "this step. Trace it to confirm the redirect is doing anything at all - a "
            + "zero here with a zero shortfall means no capillary rise was detected, "
            + "which is a different problem from the shallow store being too small."
    )
    public Attribute.Double transferred = null;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "Volume the shallow store could not cover, left with the deep "
            + "store this step. Trace it: persistently large values mean the shallow "
            + "store is too small for the evaporative demand, which is a separate "
            + "problem this component should not be hiding."
    )
    public Attribute.Double shortfall = null;

    private boolean redirecting;

    @Override
    public void init() {
        String m = (mode == null || mode.getValue() == null) ? "" : mode.getValue().trim();
        if ("snapshot".equalsIgnoreCase(m)) {
            redirecting = false;
        } else if ("redirect".equalsIgnoreCase(m)) {
            redirecting = true;
            if (deepStore == null || shallowStore == null) {
                getModel().getRuntime().sendHalt(getInstanceName()
                        + ": \"redirect\" mode needs both deepStore and shallowStore wired.");
            }
        } else {
            getModel().getRuntime().sendHalt(getInstanceName()
                    + ": mode must be \"snapshot\" or \"redirect\", got \"" + m + "\".");
        }
    }

    @Override
    public void run() {

        if (!redirecting) {
            soilSnapshot.setValue(soilStorage.getValue());
            return;
        }

        // Whatever the soil gained across J2KProcessGroundwater is exactly what it
        // removed from the deep store - that block is the only place it touches
        // this attribute.
        double taken = soilStorage.getValue() - soilSnapshot.getValue();
        if (taken <= 0.0) {
            if (shortfall != null) {
                shortfall.setValue(0.0);
            }
            if (transferred != null) {
                transferred.setValue(0.0);
            }
            return;
        }

        double shallow = shallowStore.getValue();
        double moved = Math.min(taken, Math.max(shallow, 0.0));

        shallowStore.setValue(shallow - moved);
        deepStore.setValue(deepStore.getValue() + moved);

        if (transferred != null) {
            transferred.setValue(moved);
        }
        if (shortfall != null) {
            shortfall.setValue(taken - moved);
        }
    }
}
