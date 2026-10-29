package calc;

/*
 * SuperflexParallelGroundwater.java
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
 * The SUPERFLEX parallel fast/slow groundwater configuration, expressed as a
 * single J2000 component.
 *
 * Provenance
 * ----------
 * SUPERFLEX (Fenicia, Kavetski and Savenije, WRR 2011, with the companion
 * application paper by Kavetski and Fenicia) builds model structures from
 * generic elements: reservoirs with a constitutive storage-discharge function,
 * lag functions, splitters and junctions. The structure implemented here is its
 * standard parallel-response configuration - a splitter divides the recharge
 * between a FAST reservoir and a SLOW reservoir, which rejoin at a junction:
 *
 *              recharge
 *                 |
 *            splitter (D)
 *            /          \
 *      (1-D) |            | D
 *        FAST RES      SLOW RES
 *      Qf = Sf/Kf * (Sf/Sref)^(a-1)     Qs = Ss/Ks
 *            \          /
 *             junction (Qf + Qs)
 *
 * The slow reservoir is linear, as is conventional. The fast reservoir carries
 * an optional power-law exponent: with alphaFast = 1 it is linear and Kf is
 * exactly its residence time; above 1 the recession steepens at high storage and
 * flattens at low storage, which is the behaviour reported for nonlinear
 * baseflow recessions (Wittenberg; Kirchner's storage-discharge sensitivity).
 * Setting alphaFast = 1 recovers the plain two-linear-reservoir structure, so the
 * nonlinearity can be switched off entirely and is not imposed.
 *
 * NOTE ON FIDELITY: the architecture and the parallel structure are taken from
 * SUPERFLEX, but the exact normalisation convention for the power-law reservoir
 * differs between papers. Here the exponent acts on storage normalised by
 * storageRefFast, so Kf keeps its units of days at S = storageRefFast and the
 * parameter stays interpretable. Check the form against the source before
 * reporting it as SUPERFLEX's own.
 *
 * Why this structure, for this catchment
 * --------------------------------------
 * A single groundwater reservoir must be either old or variable: its transit-time
 * distribution is exponential, so a long mean transit time necessarily damps the
 * outflow signature. That is the bind this model has been in - the tritium ages
 * want an old store, the stream d2H wants a variable one, and one reservoir
 * cannot be both. Two reservoirs in parallel produce a COMPOSITE transit-time
 * distribution, young plus old, which is what lets a catchment show a responsive
 * tracer signal in the stream while still discharging decades-old water. That is
 * the standard remedy in the field and the reason the parallel configuration is
 * worth importing rather than tuning the single store further.
 *
 * Spin-up
 * -------
 * A reservoir reaches steady state only after several times its residence time.
 * With a nine-year run a slow store of Ks = 7300 d (20 yr) is still filling
 * throughout, so it appears to contribute far less baseflow than it would at
 * equilibrium - and a calibration will then reject old groundwater for a reason
 * that is an artefact of the record length, not evidence. initialiseAtSteadyState
 * sets each store to meanRecharge * K on the first step, which removes that bias.
 * Leave it off only if a genuine spin-up period precedes the analysis window.
 *
 * @author Andrew Watson <awatson@sun.ac.za>
 */
@JAMSComponentDescription(
        title = "SuperflexParallelGroundwater",
        author = "Andrew Watson",
        description = "SUPERFLEX-style parallel fast/slow groundwater: a splitter "
        + "divides recharge between a fast reservoir and a slow linear reservoir "
        + "which rejoin at a junction, giving a composite (young + old) transit-time "
        + "distribution that a single reservoir cannot produce. Time constants are "
        + "residence times in DAYS, so the slow store's constant is directly the "
        + "quantity a tritium age estimates. The fast reservoir takes an optional "
        + "power-law exponent (1 = linear). Optionally initialises both stores at "
        + "their steady-state volumes, without which a long residence time cannot be "
        + "evaluated fairly on a short record.",
        date = "2026-09-07",
        version = "1.0_0"
)
@VersionComments(entries = {
    @VersionComments.Entry(version = "1.0_0", comment = "Initial version")
})
public class SuperflexParallelGroundwater extends JAMSComponent {

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Recharge arriving this time step, to be split between the "
            + "two stores [volume]. OPTIONAL. Leave UNWIRED in release-only mode, where "
            + "J2KProcessGroundwater has already distributed the recharge into the two "
            + "stores on its own slope-based split and this component supplies only the "
            + "release. Wiring it there would add the recharge a second time. When it is "
            + "unwired, splitToSlow has no effect and gwRG1RG2dist governs the mixture."
    )
    public Attribute.Double inflow = null;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "SUPERFLEX splitter D: the fraction of recharge sent to the "
            + "SLOW store, in [0,1]. The remainder goes to the fast store. This is the "
            + "single most informative parameter for a tracer study - it sets the "
            + "young/old mixture reaching the stream.",
            defaultValue = "0.5"
    )
    public Attribute.Double splitToSlow;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Fast reservoir time constant Kf [days]. With alphaFast = 1 "
            + "this is exactly the residence time.",
            defaultValue = "30.0"
    )
    public Attribute.Double kFastDays;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Slow reservoir time constant Ks [days] - a linear reservoir, "
            + "so this IS its mean transit time and is directly comparable with a "
            + "tritium age. A 13-25 year MTT is 4700-9100 days.",
            defaultValue = "3650.0"
    )
    public Attribute.Double kSlowDays;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Power-law exponent of the FAST reservoir. 1.0 makes it "
            + "linear and is the safe default; values above 1 steepen the recession at "
            + "high storage and flatten it at low storage. The slow reservoir is always "
            + "linear, as is conventional.",
            defaultValue = "1.0"
    )
    public Attribute.Double alphaFast;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Reference storage at which the fast reservoir's Kf applies "
            + "[volume]. Only used when alphaFast != 1; it keeps Kf in days rather than "
            + "letting the exponent absorb the units. Set it near the fast store's "
            + "typical volume.",
            defaultValue = "1.0"
    )
    public Attribute.Double storageRefFast;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Length of one time step [days]. 1 for a daily model.",
            defaultValue = "1.0"
    )
    public Attribute.Double timeStepDays;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Set both stores to their steady-state volumes on the first "
            + "step, using meanRechargeEstimate. Without this a store whose residence "
            + "time approaches the length of the record spends the whole run filling, "
            + "and is penalised for it - which biases a calibration against old "
            + "groundwater for reasons that have nothing to do with the data.",
            defaultValue = "true"
    )
    public Attribute.Boolean initialiseAtSteadyState;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Mean recharge per time step [volume], used only to size the "
            + "steady-state initialisation. A rough catchment estimate is sufficient - "
            + "it sets the starting point, not the dynamics.",
            defaultValue = "0.0"
    )
    public Attribute.Double meanRechargeEstimate;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "SELF-INITIALISATION. Number of time steps over which to "
            + "measure the actual recharge reaching each store, after which both stores "
            + "are jumped to their steady-state volumes (mean recharge * K) and the run "
            + "continues normally. Set 0 to disable. "
            + "This exists because a store cannot be spun up here: the isotope forcing "
            + "begins at the start of the run, so there is no earlier period to run "
            + "through. Without it a store whose residence time approaches the record "
            + "length spends the whole run filling - a 20-year store reaches about a "
            + "third of its equilibrium volume in nine years - and a calibration then "
            + "rejects old groundwater because it looks like a negligible baseflow "
            + "source, which is an artefact of record length rather than evidence. "
            + "One year is usually enough to size the stores; the cost is that the "
            + "warm-up period itself is not meaningful and should sit before the window "
            + "the efficiencies are computed over.",
            defaultValue = "365.0"
    )
    public Attribute.Double warmupSteps;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "Per-entity accumulator: recharge observed entering the FAST "
            + "store during the warm-up. Wire to an attribute of the enclosing spatial "
            + "context."
    )
    public Attribute.Double sumInflowFast = null;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "Per-entity accumulator for the SLOW store."
    )
    public Attribute.Double sumInflowSlow = null;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "Per-entity warm-up step counter."
    )
    public Attribute.Double stepCounter = null;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "Per-entity memory of the FAST store's volume after the last "
            + "release. The difference between that and the volume seen at the start of "
            + "this step is the recharge another component added in between - which is "
            + "how the inflow is measured in release-only mode, where this component "
            + "does not receive it directly."
    )
    public Attribute.Double lastStorageFast = null;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "Per-entity memory of the SLOW store's volume after the last "
            + "release."
    )
    public Attribute.Double lastStorageSlow = null;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "Per-entity flag marking that this entity's stores have been "
            + "initialised (0 = not yet, 1 = done). MUST be wired to an attribute of "
            + "the enclosing spatial context when the component runs per HRU, because "
            + "a plain Java field would be set by the first entity and leave every "
            + "other entity uninitialised. Leave unwired only for a single-entity use."
    )
    public Attribute.Double initialisedFlag = null;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "Fast store volume [volume]. In J2000 wire to actRG1."
    )
    public Attribute.Double storageFast;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "Slow store volume [volume]. In J2000 wire to actRG2."
    )
    public Attribute.Double storageSlow;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "Fast store outflow this step [volume]. In J2000 wire to outRG1."
    )
    public Attribute.Double outflowFast;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "Slow store outflow this step [volume]. In J2000 wire to outRG2."
    )
    public Attribute.Double outflowSlow;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "The junction: total groundwater outflow, fast + slow [volume]."
    )
    public Attribute.Double outflowTotal = null;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "Fraction of this step's groundwater outflow coming from the "
            + "SLOW store - the old-water fraction of baseflow. The diagnostic worth "
            + "tracing: it is what the parallel structure exists to control, and it can "
            + "be compared against a tracer-derived young-water fraction."
    )
    public Attribute.Double slowFraction = null;

    private boolean first = true;

    @Override
    public void init() {

        if (timeStepDays.getValue() <= 0.0) {
            getModel().getRuntime().sendHalt(getInstanceName()
                    + ": timeStepDays must be greater than zero.");
        }
        double d = splitToSlow.getValue();
        if (d < 0.0 || d > 1.0) {
            getModel().getRuntime().sendHalt(getInstanceName()
                    + ": splitToSlow is a fraction and must lie in [0,1], got " + d);
        }
        if (kFastDays.getValue() <= 0.0 || kSlowDays.getValue() <= 0.0) {
            getModel().getRuntime().sendHalt(getInstanceName()
                    + ": the time constants must be greater than zero.");
        }
        first = true;
    }

    @Override
    public void run() {

        double dt = timeStepDays.getValue();
        double d = splitToSlow.getValue();
        double kf = kFastDays.getValue();
        double ks = kSlowDays.getValue();

        double sf = storageFast.getValue();
        double ss = storageSlow.getValue();

        // Per-ENTITY, not per-component: inside a spatial context run() is called
        // once per entity, so a Java field would be cleared by the first HRU and
        // leave every other one uninitialised and slowly filling.
        boolean isFirst;
        if (initialisedFlag != null) {
            isFirst = (initialisedFlag.getValue() == 0.0);
            if (isFirst) {
                initialisedFlag.setValue(1.0);
            }
        } else {
            isFirst = first;
            first = false;
        }

        if (isFirst) {
            if (initialiseAtSteadyState.getValue()) {
                // At steady state a linear reservoir holds inflow-rate * K. Sizing
                // the stores this way is what makes a long residence time testable
                // on a record shorter than its own filling time.
                double r = meanRechargeEstimate.getValue() / dt;
                if (r > 0.0) {
                    sf = r * kf * (1.0 - d);
                    ss = r * ks * d;
                }
            }
        }

        if (inflow != null) {
            double in = inflow.getValue();
            sf += in * (1.0 - d);
            ss += in * d;
        }

        // Self-initialisation. In release-only mode the recharge is added to the
        // stores by another component, so it is measured here as the growth since
        // this component last left them. After warmupSteps the stores are set to
        // the steady state that measured rate implies, mean recharge * K.
        if (warmupSteps != null && warmupSteps.getValue() > 0.0
                && stepCounter != null && sumInflowFast != null && sumInflowSlow != null
                && lastStorageFast != null && lastStorageSlow != null) {

            double n = stepCounter.getValue();
            if (n < warmupSteps.getValue()) {

                // skip the very first step: lastStorage is still zero then, so the
                // difference would count the initial volume as if it were recharge
                if (n > 0.0) {
                    double gainF = sf - lastStorageFast.getValue();
                    double gainS = ss - lastStorageSlow.getValue();
                    if (gainF > 0.0) {
                        sumInflowFast.setValue(sumInflowFast.getValue() + gainF);
                    }
                    if (gainS > 0.0) {
                        sumInflowSlow.setValue(sumInflowSlow.getValue() + gainS);
                    }
                }
                stepCounter.setValue(n + 1.0);

                if (n + 1.0 >= warmupSteps.getValue()) {
                    double steps = warmupSteps.getValue() - 1.0;   // the skipped step
                    if (steps > 0.0) {
                        double rF = sumInflowFast.getValue() / steps / dt;
                        double rS = sumInflowSlow.getValue() / steps / dt;
                        if (rF > 0.0) {
                            sf = rF * kf;
                        }
                        if (rS > 0.0) {
                            ss = rS * ks;
                        }
                    }
                }
            }
        }

        // Fast store. The exponential form is the analytical linear-reservoir
        // solution over one step and cannot release more than the store holds,
        // so no clamp is needed; the power law rescales the effective rate.
        double a = alphaFast.getValue();
        double kfEff = kf;
        if (a != 1.0) {
            double ref = storageRefFast.getValue();
            if (ref > 0.0 && sf > 0.0) {
                // Q = S/Kf * (S/Sref)^(a-1)  =>  an effective time constant
                kfEff = kf / Math.pow(sf / ref, a - 1.0);
            }
        }
        double qf = (kfEff > 0.0) ? sf * (1.0 - Math.exp(-dt / kfEff)) : sf;
        if (qf > sf) {
            qf = sf;
        }
        sf -= qf;

        // Slow store: linear, always.
        double qs = ss * (1.0 - Math.exp(-dt / ks));
        ss -= qs;

        storageFast.setValue(sf);
        storageSlow.setValue(ss);
        if (lastStorageFast != null) {
            lastStorageFast.setValue(sf);
        }
        if (lastStorageSlow != null) {
            lastStorageSlow.setValue(ss);
        }
        outflowFast.setValue(qf);
        outflowSlow.setValue(qs);

        double tot = qf + qs;
        if (outflowTotal != null) {
            outflowTotal.setValue(tot);
        }
        if (slowFraction != null) {
            slowFraction.setValue(tot > 0.0 ? qs / tot : 0.0);
        }
    }
}
