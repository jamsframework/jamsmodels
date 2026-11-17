package calc;

/*
 * LinearGroundwaterStore.java
 * Created on 07.09.2026
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
 * A groundwater store parameterised by its RESIDENCE TIME, not by a recession
 * multiplier.
 *
 * Why this exists
 * ---------------
 * J2KProcessGroundwater sets the RG2 release fraction as
 *
 *     k_rg2 = 1 / (RG2_k * gwRG2Fact),   clamped to <= 1
 *
 * where RG2_k is a J2K-level MULTIPLIER on a per-HRU value from hgeo.par (300 d
 * for the TMG here). The product therefore spans roughly 1e2 to 1e5 across the
 * calibration bounds, and the store's behaviour collapses into two useless
 * regimes rather than varying smoothly: with the multiplier above about 1 the
 * release fraction is so small that outflow rounds to nothing and the store only
 * fills, while below it the store empties every timestep and holds nothing at
 * all. Neither is an aquifer. A calibration searching that parameter spends most
 * of its evaluations in a region where the deep store cannot reach the stream.
 *
 * Here the free parameter is the residence time in DAYS:
 *
 *     outflow = storage * (1 - exp(-dt / residenceTimeDays))
 *
 * which is the analytical solution of the linear reservoir over one step, rather
 * than the Euler form storage/tau. The two agree for tau >> dt but the
 * exponential cannot overshoot: it releases at most the whole store however
 * short the residence time, so no clamp is needed and there is no dead zone
 * anywhere in the range.
 *
 * Why residence time is the right parameter to expose
 * ---------------------------------------------------
 * For a well-mixed linear reservoir at steady state the mean transit time IS the
 * residence time, so this parameter is the same quantity a tritium or CFC age
 * estimates. It can be bounded by field evidence directly - a 13-25 year
 * tritium MTT is a bound of 4700-9100 days - instead of being inferred after the
 * fact from a storage/flux ratio. That also makes the calibrated value falsifiable
 * against the tracer rather than merely consistent with it.
 *
 * Two wiring modes
 * ----------------
 * FULL STORE - wire inflow to the recharge (rechargeRG2), storage and outflow to
 * their own new attributes, and re-point whatever consumes the groundwater
 * (routing, the isotope mixer) at those. This component then owns the store
 * outright and nothing else touches it. Cleanest, but it is several rewirings.
 *
 * RELEASE ONLY - leave inflow UNWIRED, point storage at the existing actRG2 and
 * outflow at outRG2, and place this after J2KProcessGroundwater in the HRU loop.
 * That component keeps doing the recharge, capillary rise and spill; this one
 * replaces only its release calculation. Fewer changes, but it relies on
 * J2KProcessGroundwater's own release being negligible - set gwRG2Fact high
 * enough that its 1/(RG2_k*gwRG2Fact) rounds to nothing - otherwise the store is
 * drained twice per step.
 *
 * A caution about what this can and cannot deliver
 * ------------------------------------------------
 * A single linear reservoir has an exponential transit-time distribution, so a
 * long mean is bought with a long tail and a strongly damped outflow signature.
 * If the observed baseflow isotope signal is more variable than that allows, no
 * residence time will fit both the age and the dynamics; that is a statement
 * about the one-box structure, not about the calibration. Two stores in parallel
 * (a fast one and a slow one) are the usual remedy, and this component is written
 * to be instantiated more than once so that remains open.
 *
 * @author Andrew Watson <awatson@sun.ac.za>
 */
@JAMSComponentDescription(
        title = "LinearGroundwaterStore",
        author = "Andrew Watson",
        description = "Linear groundwater reservoir parameterised by residence "
        + "time in days rather than by a recession multiplier. Outflow is the "
        + "analytical linear-reservoir solution over one time step, "
        + "storage*(1-exp(-dt/tau)), so it is well behaved for every residence "
        + "time and has no dead zone. Because the mean transit time of a "
        + "well-mixed linear reservoir equals its residence time, the calibration "
        + "parameter is directly comparable with a tritium or CFC age, and can be "
        + "bounded by that evidence. Optionally caps the store and reports the "
        + "excess as spill, and reports turnover in years for tracing.",
        date = "2026-09-07",
        version = "1.0_0"
)
@VersionComments(entries = {
    @VersionComments.Entry(version = "1.0_0", comment = "Initial version")
})
public class LinearGroundwaterStore extends JAMSComponent {

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Inflow to the store this time step - the recharge reaching "
            + "it, e.g. rechargeRG2 [same volume unit as the storage]. OPTIONAL: leave "
            + "unwired when the store is already filled by another component and this "
            + "one is only replacing its release (see the two wiring modes in the "
            + "class comment), otherwise the recharge is counted twice."
    )
    public Attribute.Double inflow = null;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "The store itself, updated in place [volume]. Wire this to "
            + "the attribute the rest of the model reads as the groundwater storage "
            + "(e.g. actRG2) so that the mixing chain and any turnover diagnostic see "
            + "the same water this component is routing."
    )
    public Attribute.Double storage;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "Outflow generated this time step [volume]. Wire to the "
            + "attribute that is routed to the reaches (e.g. outRG2)."
    )
    public Attribute.Double outflow;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Mean residence time of the store [days]. THE calibration "
            + "parameter. For a well-mixed linear reservoir this equals the mean "
            + "transit time, so bound it with the tracer evidence: a 13-25 year "
            + "tritium MTT is 4700-9100 days. Values below one time step are "
            + "physically meaningless for a groundwater store but are handled "
            + "gracefully (the store simply empties each step).",
            defaultValue = "1000.0"
    )
    public Attribute.Double residenceTimeDays;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Length of one time step [days]. 1 for a daily model.",
            defaultValue = "1.0"
    )
    public Attribute.Double timeStepDays;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Optional maximum storage [volume]. Inflow beyond this is "
            + "reported as spill rather than being silently discarded. Leave at or "
            + "below zero for an uncapped store, which is the honest default: a "
            + "capacity that binds turns the residence time into a fiction, because "
            + "the store then empties by spilling rather than by draining.",
            defaultValue = "0.0"
    )
    public Attribute.Double maxStorage;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "Inflow that exceeded maxStorage this time step [volume]. "
            + "Wire it to whatever should receive the overflow (e.g. the shallow "
            + "store) so the water is conserved rather than lost."
    )
    public Attribute.Double spill = null;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "Residence time expressed in years, for tracing next to a "
            + "tritium-derived age. This is the PARAMETER, not an emergent "
            + "storage/flux ratio - if the store is capped and spilling, the actual "
            + "turnover will be shorter than this."
    )
    public Attribute.Double residenceYears = null;

    private double releaseFraction;

    @Override
    public void init() {

        double dt = timeStepDays.getValue();
        if (dt <= 0.0) {
            getModel().getRuntime().sendHalt(getInstanceName()
                    + ": timeStepDays must be greater than zero.");
            return;
        }

        double tau = residenceTimeDays.getValue();
        if (tau <= 0.0) {
            // Not an error: a zero residence time is the degenerate pass-through
            // case, and the optimiser may well probe the edge of its range.
            releaseFraction = 1.0;
        } else {
            // Analytical linear-reservoir depletion over one step. Bounded in
            // (0,1] by construction, so - unlike 1/(k*factor) - it can neither
            // overshoot nor round to zero and strand the store.
            releaseFraction = 1.0 - Math.exp(-dt / tau);
        }

        if (residenceYears != null) {
            residenceYears.setValue(tau / 365.0);
        }
    }

    @Override
    public void run() {

        double s = storage.getValue();
        if (inflow != null) {
            s += inflow.getValue();
        }

        double over = 0.0;
        double cap = maxStorage.getValue();
        if (cap > 0.0 && s > cap) {
            over = s - cap;
            s = cap;
        }

        double out = releaseFraction * s;
        s -= out;

        storage.setValue(s);
        outflow.setValue(out);
        if (spill != null) {
            spill.setValue(over);
        }
    }
}
