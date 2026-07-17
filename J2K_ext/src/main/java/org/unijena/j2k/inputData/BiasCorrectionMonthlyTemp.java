/*
 * LapseRateAdaptation.java
 * Created on 09.10.2019, 13:45:14
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
package org.unijena.j2k.inputData;

import jams.JAMS;
import jams.data.*;
import jams.model.*;
import java.util.Calendar;

/**
 *
 * @author Sven Kralisch <sven.kralisch@uni-jena.de>
 */
@JAMSComponentDescription(
        title = "LapseRateAdaptation",
        author = "Sven Kralisch",
        description = "Adaptation of local climate values through "
        + "elevation difference based on yearly/monthly lapse rates.\n"
        + "1. Cacluate the elevation difference between modelling unit"
        + "and climate data source\n"
        + "2. Adjust the climate value according to the "
        + "lapse rate and elevation difference",
        date = "2019-10-15",
        version = "1.0_1"
)
@VersionComments(entries = {
    @VersionComments.Entry(version = "1.0_0", comment = "Initial version"),
    @VersionComments.Entry(version = "1.0_1", comment = "Fixed bug in fixedMinimum/fixedMaximum evaluation"),
    @VersionComments.Entry(version = "1.1_0", date = "2020-04-27", comment = "Extended for use of input/output arrays for Precip BIAS correction")

})
public class BiasCorrectionMonthlyTemp extends JAMSComponent {

    /*
     *  Component attributes
     */
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Climate input value")
    public Attribute.DoubleArray inputValues;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Lapse rates for 100m elevation difference, given "
            + "either as a single values or as 12 (monthly) values")
    public Attribute.Double[] correctionFactors;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "The current model time")
    public Attribute.Calendar time;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "Calculated output for the modelling entity")
    public Attribute.DoubleArray outputValues;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
            description = "Absolute possible minimum value for data set",
            defaultValue = "-Infinity")
    public Attribute.Double fixedMinimum;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
            description = "Absolute possible maximum value for data set",
            defaultValue = "Infinity")
    public Attribute.Double fixedMaximum;

 /*
     *  Component run stages
     */
    @Override
    public void init() {
        if (correctionFactors.length != 1 && correctionFactors.length != 12) {
            getModel().getRuntime().sendHalt("Number of lapse rate values must be either 1 or 12!");
        }
    }

    @Override
    public void run() {

        double[] inputs = inputValues.getValue();
        double[] outputs = new double[inputs.length];

        for (int i = 0; i < inputs.length; i++) {

            double input = inputs[i];

            if (input != JAMS.getMissingDataValue()) {

                double monthlyCorrectionFactor;
                if (correctionFactors.length > 1) {
                    int nowmonth = time.get(Calendar.MONTH);
                    monthlyCorrectionFactor = correctionFactors[nowmonth].getValue();
                } else {
                    monthlyCorrectionFactor = correctionFactors[0].getValue();
                }

                //  elevation difference
                //  double elevationdiff = (sourceElev.getValue() - entityElev.getValue());
                //result calculation
                double result = monthlyCorrectionFactor + input;
                result = Math.min(fixedMaximum.getValue(), result);
                result = Math.max(fixedMinimum.getValue(), result);

                outputs[i] = result;

            } else {

                outputs[i] = JAMS.getMissingDataValue();

            }
        }

        outputValues.setValue(outputs);

    }
}
