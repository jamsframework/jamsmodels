/*
 * J2KProcessEpisodicRecharge.java
 * Created on Jul 3, 2026, 1:54:19 PM
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
package org.unijena.j2k.groundwater;

import jams.data.*;
import jams.model.*;

/**
 *
 * @author watso
 */
@JAMSComponentDescription(
        title = "Episodic recharge component",
        author = "Andrew Watson",
        description = "Recharge component for semi-arid environments",
        date = "2026-07-03",
        version = "1.0_0"
)
@VersionComments(entries = {
    @VersionComments.Entry(version = "1.0_0", comment = "Initial version"),
    @VersionComments.Entry(version = "1.0_1", comment = "Some improvements")
})
public class J2KProcessEpisodicRecharge extends JAMSComponent {

    /*
     *  Component attributes
     */
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "episodicRechargeThreshold",
            unit = "mm",
            lowerBound = 0.0,
            upperBound = 200.0,
            defaultValue = "0.0"
    )
    public Attribute.Double rechargeThreshold;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "Percolation leaving the soil profile",
            unit = "L"
    )
    public Attribute.Double percolation;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "Water returned to DPS",
            unit = "L"
    )
    public Attribute.Double rechargeReturnedToDPS;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "DPS",
            unit = "L"
    )
    public Attribute.Double DPS;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "HRU area",
            unit = "m²"
    )
    public Attribute.Double area;

    @Override
    public void init() {
    }

    @Override
    public void run() {
        double recharge_L = rechargeThreshold.getValue() * area.getValue();
        if (percolation.getValue() >= recharge_L) {

            // Entire event becomes recharge
        } else {

            DPS.setValue(DPS.getValue() + percolation.getValue());

            rechargeReturnedToDPS.setValue(percolation.getValue());

            percolation.setValue(0);

        }
    }

    @Override
    public void cleanup() {
    }
}
