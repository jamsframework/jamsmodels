/*
 * IrrigationInit.java
 * Created on 25.09.2015, 25.09.2015 17:04:04
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
import java.util.Calendar;

/**
 *
 * @author Sven Kralisch <sven.kralisch at uni-jena.de>
 */
@JAMSComponentDescription(
    title="IrrigationInit",
    author="Sven Kralisch",
    description="Decide if entity should be irrigated at current time step",
    date = "2015-09-25",
    version = "1.0_0"
)
@VersionComments(entries = {
    @VersionComments.Entry(version = "1.0_0", comment = "Initial version")
})
public class IrrigationInit extends JAMSComponent {

    /*
     *  Component attributes
     */
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Current time"
            )
            public Attribute.Calendar time;
                
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Julian day of irrigation start"
            )
            public Attribute.Double start;
                
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Julian day of irrigation end"
            )
            public Attribute.Double end;
                                
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "Irrigation indicator"
            )
            public Attribute.Double irrigated;
                
    /*
     *  Component run stages
     */

    @Override
    public void run() {
        int jDay = time.get(Calendar.DAY_OF_YEAR);
        if (jDay >= start.getValue() && jDay <= end.getValue()) {
            irrigated.setValue(1);
        } else {
            irrigated.setValue(0);
        }
        
    }
}