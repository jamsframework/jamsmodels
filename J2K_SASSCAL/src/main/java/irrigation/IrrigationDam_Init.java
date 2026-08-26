package irrigation;

/*
 * IrrigationDam_Init.java
 * Created on 07.09.2020, 11:23:03
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
 * @author Sven Kralisch <sven.kralisch@uni-jena.de>
 */
@JAMSComponentDescription(
        title = "IrrigationDam",
        author = "Sven Kralisch",
        description = "TBD",
        date = "2020-09-07",
        version = "1.0_0"
)
@VersionComments(entries = {
    @VersionComments.Entry(version = "1.0_0", comment = "Initial version")
})
public class IrrigationDam_Init extends JAMSComponent {

    /*
     *  Component attributes
     */
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Dam capacity",
            defaultValue = "0",
            unit = "m3",
            lowerBound = 0,
            upperBound = Double.POSITIVE_INFINITY
    )
    public Attribute.Double damCapacity;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Initial dam storage as proportion of dam capacity",
            defaultValue = "0",
            unit = "",
            lowerBound = 0,
            upperBound = 1
    )
    public Attribute.Double damStorageInit;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
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
    public void initAll() {
        damStorage.setValue(damCapacity.getValue() * damStorageInit.getValue());
    }
}
