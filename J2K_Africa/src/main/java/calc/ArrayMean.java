package calc;

/*
 * ArrayMean.java
 * Created on 31.08.2026
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
import jams.JAMS;
import jams.data.*;
import jams.model.*;

/**
 * Reduces a data array to its mean, ignoring missing entries.
 *
 * Written for comparing a spatially distributed observation network against a
 * single catchment-scale simulated value: a TSDataStoreReader hands the whole
 * station array to the time loop each step, but only some stations report on
 * any given day, and the rest arrive as the JAMS missing value.
 *
 * Missing handling is the entire point of the component. JAMS represents a
 * missing value as POSITIVE_INFINITY, so a plain average over the array does
 * not merely include the gaps - it returns infinity. Entries equal to the JAMS
 * missing value, to the configurable nodataValue, or that are NaN or infinite
 * are skipped here.
 *
 * When no entry is valid the result is set to the JAMS missing value, which is
 * exactly what optas.efficiencies.UniversalEfficiencyCalculator tests for in
 * considerData() before adding a pair. That makes days with no observations
 * drop out of the efficiency rather than being scored as zeros.
 *
 * @author Andrew Watson <awatson@sun.ac.za>
 */
@JAMSComponentDescription(
        title = "ArrayMean",
        author = "Andrew Watson",
        description = "Mean of a data array, ignoring missing entries. Entries "
        + "equal to the JAMS missing value (positive infinity), equal to the "
        + "configurable nodataValue, or NaN/infinite are excluded. If nothing is "
        + "valid the result is set to the JAMS missing value, so that a downstream "
        + "efficiency calculator skips that time step instead of scoring it. "
        + "Intended for reducing a station network to one catchment value when the "
        + "model produces a single aggregate to compare against.",
        date = "2026-08-31",
        version = "1.0_0"
)
@VersionComments(entries = {
    @VersionComments.Entry(version = "1.0_0", comment = "Initial version")
})
public class ArrayMean extends JAMSComponent {

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "The data array to reduce, e.g. the array a "
            + "TSDataStoreReader produces for an observation network."
    )
    public Attribute.DoubleArray values;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "Mean of the valid entries, or the JAMS missing value "
            + "when none are valid."
    )
    public Attribute.Double result;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "How many entries were valid this time step. Worth "
            + "tracing: it tells you how many stations each comparison rests on."
    )
    public Attribute.Double count = null;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "An additional sentinel to treat as missing, for data "
            + "that carries its own nodata marker.",
            defaultValue = "-9999.0"
    )
    public Attribute.Double nodataValue;

    @Override
    public void run() {

        double[] v = (values == null) ? null : values.getValue();
        double nodata = nodataValue.getValue();

        double sum = 0.0;
        int n = 0;

        if (v != null) {
            for (int i = 0; i < v.length; i++) {
                double x = v[i];
                if (Double.isNaN(x) || Double.isInfinite(x)) {
                    continue;
                }
                if (x == JAMS.getMissingDataValue() || x == nodata) {
                    continue;
                }
                sum += x;
                n++;
            }
        }

        if (count != null) {
            count.setValue(n);
        }

        // The missing value rather than 0 or NaN: UniversalEfficiencyCalculator
        // drops a time step only when the measurement equals it exactly.
        result.setValue((n == 0) ? JAMS.getMissingDataValue() : sum / n);
    }
}
