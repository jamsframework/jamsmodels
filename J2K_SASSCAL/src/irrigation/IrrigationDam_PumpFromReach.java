/*
 * IrrigationDam_PumpFromReach.java
 * Created on 08.09.2020, 23:11:44
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
package irrigation;

import jams.data.*;
import jams.data.Attribute.Calendar;
import jams.model.*;

/**
 *
 * @author Sven Kralisch <sven.kralisch@uni-jena.de>
 */
@JAMSComponentDescription(
        title = "IrrigationDam_PumpFromReach",
        author = "Sven Kralisch",
        description = "TBD",
        date = "2020-09-07",
        version = "1.0_0"
)
@VersionComments(entries = {
    @VersionComments.Entry(version = "1.0_0", comment = "Initial version")
})
public class IrrigationDam_PumpFromReach extends JAMSComponent {

    /*
     *  Component attributes
     */
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Current date"
    )
    public Attribute.Calendar date;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Dam capacity",
            defaultValue = "0",
            unit = "m³",
            lowerBound = 0,
            upperBound = Double.POSITIVE_INFINITY
    )
    public Attribute.Double damCapacity;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Pumping capactiy from the river",
            defaultValue = "0",
            unit = "m³",
            lowerBound = 0,
            upperBound = Double.POSITIVE_INFINITY
    )
    public Attribute.Double riverPump;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Julian day at which pumping from river starts",
            defaultValue = "1",
            lowerBound = 0,
            upperBound = 366
    )
    public Attribute.Double riverStart;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Julian day at which pumping from river ends",
            defaultValue = "1",
            lowerBound = 0,
            upperBound = 366
    )
    public Attribute.Double riverEnd;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "RD1 component in reach",
            unit = "L"
    )
    public Attribute.Double inRD1;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "RD2 component in reach",
            unit = "L"
    )
    public Attribute.Double inRD2;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "RG1 component in reach",
            unit = "L"
    )
    public Attribute.Double inRG1;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "RG2 component in reach",
            unit = "L"
    )
    public Attribute.Double inRG2;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "Adaptation factor for reducing the water that is allowed to be pumped",
            defaultValue = "1",
            lowerBound = 0,
            upperBound = 1
    )
    public Attribute.Double adaptationFactor;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "Current dam storage",
            defaultValue = "0",
            unit = "L",
            lowerBound = 0,
            upperBound = Double.POSITIVE_INFINITY
    )
    public Attribute.Double damStorage;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "Water volume pumped at current time step",
            defaultValue = "0",
            unit = "L",
            lowerBound = 0,
            upperBound = Double.POSITIVE_INFINITY
    )
    public Attribute.Double pumpVolume;

    /*
     *  Component run stages
     */
    @Override
    public void run() {

        double availableCapacity = damCapacity.getValue() * 1000 - damStorage.getValue();
        double pumpedWater = 0;

        //check dam full
        if (availableCapacity <= 0) {
            pumpVolume.setValue(0);
            return;
        }

        int julDay = date.getValue().get(Calendar.DAY_OF_YEAR);

        double rStart = this.riverStart.getValue();
        double rEnd = this.riverEnd.getValue();
        double rJD = julDay;

        if (rStart > rEnd) {
            rEnd = 366 + rEnd;
            rJD = 366 + rJD;
        }

        //pump from river?
        if (rJD >= rStart && rJD <= rEnd) {

            // sum up incoming water
            double totalIn = inRD1.getValue() + inRD2.getValue() + inRG1.getValue() + inRG2.getValue();
            
            // reduce the water that we consider to be available?
            totalIn *= adaptationFactor.getValue();

            // get amount of water that is available for pumping
            double totalAvail = Math.min(totalIn, riverPump.getValue() * 1000);

            // calc fraction of available water that can be stored
            double frac = Math.min(availableCapacity / totalAvail, 1);

            // remove that fraction from single components
            inRD1.setValue(inRD1.getValue() * (1 - frac));
            inRD2.setValue(inRD2.getValue() * (1 - frac));
            inRG1.setValue(inRG1.getValue() * (1 - frac));
            inRG2.setValue(inRG2.getValue() * (1 - frac));

            // add the water to the dam
            pumpedWater = totalAvail * frac;
            damStorage.setValue(damStorage.getValue() + pumpedWater);
        }
        
        pumpVolume.setValue(pumpedWater);
    }

    @Override
    public void cleanup() {
    }
}
