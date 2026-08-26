/*
 * InitSoilWaterStates.java
 * Created on 25. November 2005, 13:21
 *
 * This file is part of JAMS
 * Copyright (C) 2005 FSU Jena, c0krpe
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA
 *
 */
package org.unijena.j2000g;

import jams.JAMS;
import jams.data.*;
import jams.model.*;

/**
 *
 * @author Peter Krause
 */
@JAMSComponentDescription(
        title = "TemperatureLapseRate",
        author = "Santosh Nepal, Peter Krause",
        description = "Regionalisation of Temp through general adiabatic rate"
        + "depends upon given adaiabatic rate"
)
@VersionComments(entries = {
    @VersionComments.Entry(version = "1.1", comment = "Added functionality to "
            + "handle the case that the closest station does not provide a "
            + "valid value", date = "2016-08-30")
})
public class TemperatureLapseRate extends JAMSComponent {

    /*
     *  Component variables
     */
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "station elevation"
    )
    public Attribute.DoubleArray statElev;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "entity elevation"
    )
    public Attribute.Double entityElev;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "the measured input from a base station"
    )
    public Attribute.DoubleArray inputValue;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "calculated output for the modelling entity"
    )
    public Attribute.Double outputValue;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "lapse rate per 100 m elevation difference"
    )
    public Attribute.Double lapseRate;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "position array to determine best weights"
    )
    public Attribute.IntegerArray statOrder;

    /*
         *  Component run stages
     */
    public void init() {

    }

    public void run() {

        for (int i = 0; i < inputValue.getValue().length; i++) {

            int closestStation = statOrder.getValue()[i];
            double input = inputValue.getValue()[closestStation];

            if (input != JAMS.getMissingDataValue()) {
                //elevation difference
                double elevationdiff = (statElev.getValue()[closestStation] - entityElev.getValue());
                //temp calculation
                outputValue.setValue(elevationdiff * (lapseRate.getValue() / 100.) + input);
                return;
            }

        }
        
        getModel().getRuntime().sendHalt("No station with valid value found. Please check your inputs!");
    }

    public void cleanup() {

    }

}
