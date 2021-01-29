/*
 * ManagedDam.java
 * Created on 28.01.2021, 16:32:07
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
package org.unijena.j2k.reservoir;

import jams.data.*;
import jams.model.*;
import org.unijena.j2k.routing.J2KProcessReachRouting;

/**
 *
 * @author sven.kralisch
 */
@JAMSComponentDescription(
        title = "ManagedDam",
        author = "Sven Kralisch",
        description = "Component for simulation of artificial releases from a dam",
        date = "2021-01-28",
        version = "1.0_0"
)
@VersionComments(entries = {
    @VersionComments.Entry(version = "1.0_0", comment = "Initial version")
})
public class ManagedDam extends J2KProcessReachRouting {

    /*
     *  Component attributes
     */
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Dam release volume at current time step",
            defaultValue = "0",
            unit = "m³/s",
            lowerBound = 0,
            upperBound = Double.POSITIVE_INFINITY
    )
    public Attribute.Double releaseVol;

    /*
     *  Component run stages
     */

    @Override
    public void run() {

        super.run();

    }

}
