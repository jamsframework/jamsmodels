package Reservoirs;

/*
 * Dam_InflowFromReach_RuleRelease.java
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
import jams.data.*;

import jams.model.*;

/**
 *
 * @author Sven Kralisch <awatson@sun.ac.za>
 */
@JAMSComponentDescription(
        title = "Dam_InflowFromReach",
        author = "Andrew Watson",
        description = "A reservoir component used to store reach inflows",
        date = "2020-09-22",
        version = "1.0_0"
)
@VersionComments(entries = {
    @VersionComments.Entry(version = "1.0_0", comment = "Initial version")
})
public class Dam_InflowFromReach extends JAMSComponent {

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
            unit = "L",
            lowerBound = 0,
            upperBound = Double.POSITIVE_INFINITY
    )
    public Attribute.Double damCapacity;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Inflow from the river RD1",
            defaultValue = "0",
            unit = "L",
            lowerBound = 0,
            upperBound = Double.POSITIVE_INFINITY
    )
    public Attribute.Double riverInflowRD1;
    
        @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Inflow from the river RD2",
            defaultValue = "0",
            unit = "L",
            lowerBound = 0,
            upperBound = Double.POSITIVE_INFINITY
    )
    public Attribute.Double riverInflowRD2;
        
            @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Inflow from the river RG1",
            defaultValue = "0",
            unit = "L",
            lowerBound = 0,
            upperBound = Double.POSITIVE_INFINITY
    )
    public Attribute.Double riverInflowRG1;
            
                @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Inflow from the river RG2",
            defaultValue = "0",
            unit = "L",
            lowerBound = 0,
            upperBound = Double.POSITIVE_INFINITY
    )
    public Attribute.Double riverInflowRG2;
    
    

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "Current dam storage",
            defaultValue = "0",
            unit = "L",
            lowerBound = 0,
            upperBound = Double.POSITIVE_INFINITY
    )
    public Attribute.Double damStorage;

    /*
     *  Component run stages
     */
    @Override
    public void run() {

        //check if there is a dam
        if (damStorage.getValue() > 0) {

            damStorage.setValue(damStorage.getValue() + riverInflowRD1.getValue()+riverInflowRD2.getValue()+riverInflowRG1.getValue()+riverInflowRG2.getValue());

        } else {
            damStorage.setValue(0);
        }
    }

    @Override
    public void cleanup() {
    }
}
