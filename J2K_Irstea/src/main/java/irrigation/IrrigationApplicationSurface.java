/*
 * IrrigationApplication.java
 * Created on 13.08.2015, 17:42:55
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
import jams.model.*;

/**
 *
 * @author Sven Kralisch <sven.kralisch at uni-jena.de>
 */
@JAMSComponentDescription(
        title = "",
        author = "Sven Kralisch",
        description = "Apply irrigation on an HRU based on available water",
        date = "2015-08-13",
        version = "1.0_0"
)
@VersionComments(entries = {
    @VersionComments.Entry(version = "1.0_0", comment = "Initial version")
})
public class IrrigationApplicationSurface extends JAMSComponent {

    /*
     *  Component attributes
     */
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "Water available for irrigation",
            unit = "l"
    )
    public Attribute.Double irrigationWater;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "Added water for irrigation",
            unit = "l"
    )
    public Attribute.Double irrigationTotal;



    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "HRU net Rain",
            unit = "L"
    )
    public Attribute.Double netRain;

    /*
     *  Component run stages
     */

    @Override
    public void run() {
        
        irrigationTotal.setValue(0);
        double irrigationInMM = irrigationWater.getValue();
        netRain.setValue(netRain.getValue() + irrigationInMM);
        irrigationTotal.setValue(irrigationWater.getValue());
        irrigationWater.setValue(0);
        
    }

}
