package Isotopes;

/*
 * IsoPrecipCorrection.java
 * Created on 28.08.2026
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
 * Deliberately small, physically-parameterised correction to the regionalised
 * precipitation isotope field, intended to be tuned by the catchment optimiser
 * against downstream (stream / groundwater) isotope performance.
 *
 * Why so few parameters
 * ---------------------
 * The available isotope observations over the 2015-2023 run are sparse:
 * roughly 328 precipitation values (~10 stations), 57 stream values on 15
 * distinct days, and 52 RG2 groundwater values on 19 days. A flexible
 * corrector calibrated against that would fit the sampled days and generalise
 * nowhere - which is exactly what the Gaussian process attempt demonstrated
 * (R2 1.000 in-sample, -0.03 on held-out stations). Two free parameters is
 * about what this many observations can identify.
 *
 * The correction is
 *
 *     corrected = raw + offset + lapse * (elevation - elevRef) / 100
 *
 * offset absorbs a uniform level error in the isoRSM/QMAP input; lapse is an
 * altitude gradient, expressed in permil per 100 m so it can be compared
 * directly against the published d2H lapse of about -1 to -3 permil/100 m and
 * bounded accordingly. Elevation is centred on elevRef so that the two
 * parameters stay close to independent - without centring, changing lapse also
 * shifts the mean and the optimiser sees a strongly correlated pair.
 *
 * Defaults are the identity (offset 0, lapse 0), so enabling this component
 * changes nothing until the optimiser moves the parameters. It corrects the
 * isotope attribute IN PLACE so that everything downstream - the mixing chain,
 * routing, and ultimately the stream and groundwater signatures - sees the
 * corrected value. Writing to a separate attribute would leave the correction
 * invisible to those, and the optimiser would then see a completely flat
 * objective.
 *
 * @author Andrew Watson <awatson@sun.ac.za>
 */
@JAMSComponentDescription(
        title = "IsoPrecipCorrection",
        author = "Andrew Watson",
        description = "Applies a two-parameter (offset + altitude lapse) correction to "
        + "the regionalised precipitation isotope value, in place, so the corrected "
        + "signal propagates through mixing and routing to the stream and groundwater "
        + "isotope output. Both parameters default to the identity, and both are "
        + "intended to be exposed to the catchment optimiser alongside the physical "
        + "mixing/storage parameters, so the precipitation input is tuned against the "
        + "downstream isotope signatures that are actually of interest. Kept "
        + "deliberately low-dimensional: the isotope observation record is far too "
        + "sparse to identify a flexible corrector without overfitting.",
        date = "2026-08-28",
        version = "1.0_0"
)
@VersionComments(entries = {
    @VersionComments.Entry(version = "1.0_0", comment = "Initial version")
})
public class IsoPrecipCorrection extends JAMSComponent {

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "The precipitation isotope value to correct, corrected IN "
            + "PLACE [permil]. Wire this to the same attribute the mixing chain reads "
            + "(e.g. HRULoop 2h) - if the correction is written somewhere else, nothing "
            + "downstream sees it and the optimiser has no gradient to work with."
    )
    public Attribute.Double value;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "HRU elevation [m]"
    )
    public Attribute.Double elevation;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Uniform level correction [permil]. CALIBRATION PARAMETER - "
            + "suggested range -25 to +25.",
            defaultValue = "0.0"
    )
    public Attribute.Double offset;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Altitude gradient [permil per 100 m]. CALIBRATION PARAMETER - "
            + "suggested range -6 to +2. The published d2H altitude effect is about -1 "
            + "to -3 permil/100 m, so a calibrated value far outside that is a signal "
            + "that the optimiser is absorbing an error which does not actually live in "
            + "the precipitation input.",
            defaultValue = "0.0"
    )
    public Attribute.Double lapse;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Reference elevation the lapse term is centred on [m]. Fixed, "
            + "NOT a calibration parameter - set it near the mean elevation of the "
            + "isotope stations (about 200 m here) so offset and lapse stay close to "
            + "independent.",
            defaultValue = "200.0"
    )
    public Attribute.Double elevRef;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Lower physical bound for the corrected value [permil]",
            defaultValue = "-200.0"
    )
    public Attribute.Double minValue;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Upper physical bound for the corrected value [permil]",
            defaultValue = "50.0"
    )
    public Attribute.Double maxValue;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "Copy of the value before correction [permil], for diagnostics "
            + "and for comparing corrected against raw in the output datastore"
    )
    public Attribute.Double uncorrectedValue = null;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "Copy of the value immediately AFTER correction [permil]. Needed "
            + "for any downstream comparison, because the attribute corrected in place is "
            + "subsequently overwritten by the mixing and fractionation chain (IsotopeMixer "
            + "binds it as a READWRITE concentration) - so reading it later gives a "
            + "post-mixing soil-water value, not the corrected precipitation input. Compare "
            + "this against uncorrectedValue for a like-for-like pair."
    )
    public Attribute.Double correctedValueCopy = null;

    @Override
    public void run() {
        double raw = value.getValue();

        if (uncorrectedValue != null) {
            uncorrectedValue.setValue(raw);
        }

        double corrected = raw
                + offset.getValue()
                + lapse.getValue() * (elevation.getValue() - elevRef.getValue()) / 100.0;

        // Clamp to physically possible values: the optimiser explores the full
        // parameter range, and an extreme lapse applied to an outlying HRU
        // elevation could otherwise push the tracer somewhere impossible and
        // destabilise the mixing chain downstream.
        if (corrected < minValue.getValue()) {
            corrected = minValue.getValue();
        } else if (corrected > maxValue.getValue()) {
            corrected = maxValue.getValue();
        }

        value.setValue(corrected);

        if (correctedValueCopy != null) {
            correctedValueCopy.setValue(corrected);
        }
    }
}
