/*
 * LapseRateRegionalization.java
 * Created on 08.10.2019, 10:27:19
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
package org.unijena.j2k.regionalisation;

import jams.JAMS;
import jams.data.*;
import jams.model.*;
import java.util.Calendar;

/**
 *
 * @author Sven Kralisch <sven.kralisch@uni-jena.de>
 */
@JAMSComponentDescription(
        title = "LapseRateRegionalization",
        author = "Santosh Nepal, Peter Krause, Sven Kralisch",
        description = "Regionalisation through station proximity and elevation"
        + "difference based on yearly/monthly lapse rates.\n"
        + "1. Find the closest station with valid measurement\n"
        + "2. Cacluate the elevation difference between modelling unit"
        + "and station\n"
        + "3. Adjust the station measurement according to the "
        + "lapse rate and elevation difference",
        date = "2019-10-08",
        version = "1.0_0"
)
@VersionComments(entries = {
    @VersionComments.Entry(version = "1.0_0", comment = "Initial version")
})
public class LapseRateRegionalization extends JAMSComponent {

    /*
     *  Component attributes
     */
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Measured station inputs")
    public Attribute.DoubleArray inputValues;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Station elevations")
    public Attribute.DoubleArray statElev;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Station IDs ordered by their distance")
    public Attribute.IntegerArray statOrder;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Entity elevation")
    public Attribute.Double entityElev;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Lapse rates for 100m elevation difference, given "
            + "either as a single values or as 12 (monthly) values")
    public Attribute.Double[] lapseRates;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "The current model time")
    public Attribute.Calendar time;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "Calculated output for the modelling entity")
    public Attribute.Double outputValue;

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
        if (lapseRates.length != 1 && lapseRates.length != 12) {
            getModel().getRuntime().sendHalt("Number of laps rate values must be either 1 or 12!");
        }
    }

    @Override
    public void run() {

        for (int i = 0; i < statOrder.getValue().length; i++) {

            int closestStation = statOrder.getValue()[i];
            double input = inputValues.getValue()[closestStation];

            if (input != JAMS.getMissingDataValue()) {

                double lapseRate;
                if (lapseRates.length > 1) {
                    int nowmonth = time.get(Calendar.MONTH);
                    lapseRate = lapseRates[nowmonth].getValue();
                } else {
                    lapseRate = lapseRates[0].getValue();
                }

                //elevation difference
                double elevationdiff = (statElev.getValue()[closestStation] - entityElev.getValue());
                //result calculation
                double result = elevationdiff * (lapseRate / 100.) + input;
                result = Math.min(fixedMaximum.getValue(), input);
                result = Math.max(fixedMinimum.getValue(), input);
                outputValue.setValue(result);
                return;
            }

        }

        getModel().getRuntime().sendHalt("No station with valid value found. Please check your inputs!");

    }
}
