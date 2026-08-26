/*
 * TemperatureLapseRateMonthly.java
 * Created on 25.06.2018, 14:05:28
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

package j2k_Himalaya.regionalisation;

import jams.JAMS;
import jams.data.*;
import jams.model.*;
import java.util.Calendar;

/**
 *
 * @author Peter Krause
 */
@JAMSComponentDescription(title = "TemperatureLapseRateMonthly",
        author = "Santosh Nepal, Peter Krause, Manfred Fink",
        version = "1.0_1",
        description = "Regionalisation of Temp through general adiabatic rate"
        + "depends upon given adaiabatic rate +++ included monthly lapse rate. Twelve different Lapse for each month are proposed"
        + "now accept if station has data gaps, another nearest station is considered for lapse rate")
@VersionComments(entries = {
    @VersionComments.Entry(version = "1.0_0", comment = "Initial version"),
    @VersionComments.Entry(version = "1.0_1", comment = "Fixed station selection, taking missing values into account")
})
public class TemperatureLapseRateMonthly extends JAMSComponent {

    /*
     *  Component variables
     */
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
            description = "entity elevation")
    public Attribute.Double entityElev;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
            description = "the measured input from base station(s)")
    public Attribute.DoubleArray statValue;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
            description = "station elevation(s)")
    public Attribute.DoubleArray statElev;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
            description = "stations ordered by distance to the HRU")
    public Attribute.IntegerArray statOrder;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
            description = "lapse rate per 100 m elevation difference. This should be 12 values, one for each month")
    public Attribute.Double[] lapseRates;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
            description = "the current model time")
    public Attribute.Calendar time;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.WRITE,
            description = "calculated output for the modelling entity")
    public Attribute.Double outputValue;

    /*
     *  Component run stages
     */
    public void init() throws Attribute.Entity.NoSuchAttributeException {
    }

    public void run() throws Attribute.Entity.NoSuchAttributeException {

        // find the closest station that has valid data
        int stationIndex = -1;
        for (int i = 0; i < statValue.getValue().length; i++) {
            if (statValue.getValue()[i] != JAMS.getMissingDataValue()) {
                stationIndex = i;
                break;
            }
        }

        // check if there is valid data
        if (stationIndex < 0) {

            getModel().getRuntime().sendHalt("No station with valid value found. Please check your inputs!");

        } else {

            double input = statValue.getValue()[stationIndex];
            double elevationdiff = (statElev.getValue()[stationIndex] - entityElev.getValue());
            int nowmonth = time.get(Calendar.MONTH);

            outputValue.setValue(elevationdiff * (lapseRates[nowmonth].getValue() / 100.) + input);
        }
    }

    public void cleanup() {
    }
}
