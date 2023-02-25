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
public class Dam_Inflow_Reach extends JAMSComponent {

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

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "Volume of water release program based on rules and storage condition",
            defaultValue = "0",
            unit = "L",
            lowerBound = 0,
            upperBound = Double.POSITIVE_INFINITY
    )
    public Attribute.Double releaseProgram;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Volume of water release program based on rules and storage condition",
            defaultValue = "1"
    )
    public Attribute.Double maxTransferFrac;

    /*
     *  Component run stages
     */
    @Override
    public void run() {

        //check if there is a dam
        double headroom = damCapacity.getValue() - damStorage.getValue();

        double potInflow = riverInflowRD1.getValue() + riverInflowRD2.getValue() + riverInflowRG1.getValue() + riverInflowRG2.getValue();

        double maxInflow = Math.min(headroom, potInflow);

        double frac = maxInflow / potInflow;

        frac = Math.min(frac, maxTransferFrac.getValue());

        riverInflowRD1.setValue(riverInflowRD1.getValue() * (1 - frac));

        riverInflowRD2.setValue(riverInflowRD2.getValue() * (1 - frac));

        riverInflowRG1.setValue(riverInflowRG1.getValue() * (1 - frac));

        riverInflowRG2.setValue(riverInflowRG2.getValue() * (1 - frac));

        damStorage.setValue(damStorage.getValue() + maxInflow);

    }

    @Override
    public void cleanup() {
    }
}
