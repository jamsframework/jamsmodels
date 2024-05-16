/*
 * AnimalWaterExcretion.java
 * Created on 13.08.2015, 16:17:09
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
package AnimalWater;

import jams.data.*;
import jams.model.*;
import java.util.Calendar;
import java.util.List;

/**
 *
 * @author Sven Kralisch <sven.kralisch at uni-jena.de>
 */
@JAMSComponentDescription(
        title = "",
        author = "Nico Hachgenei",
        description = "Excretion of water by animals into HRUs",
        date = "2024-04-16",
        version = "1.0_0"
)
@VersionComments(entries = {
    @VersionComments.Entry(version = "1.0_0", comment = "Initial version")
})
public class AnimalWaterExcretion extends JAMSComponent {

    /*
     *  Component attributes
     */
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "HRUs list"
    )
    public Attribute.EntityCollection hrus;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Current time"
            )
            public Attribute.Calendar time;
                
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Julian day of beginning of summer (hot, dry conditions)"
            )
            public Attribute.Double summerStart;
                
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Julian day of end of summer (hot, dry conditions)"
            )
            public Attribute.Double summerEnd;;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "HRU throughfall or precipitation -> adding animal excretion to this value",
            unit = "L"
    )
    public Attribute.Double throughfall;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "HRU area",
            unit = "m²"
    )
    public Attribute.Double area;

    /*
     *  Component run stages
     */

    @Override
    public void run() {
        Attribute.Entity currentHRU = hrus.getCurrent();
        
        double HRUExcretion = 0;
        // check season in order to decide how much animals excrete
        int jDay = time.get(Calendar.DAY_OF_YEAR);
        if (jDay >= summerStart.getValue() && jDay <= summerEnd.getValue()) {
            HRUExcretion = currentHRU.getDouble("excr_su");
        } else {
            HRUExcretion = currentHRU.getDouble("excr_wi");
        }
        
        //double HRUExcretionInMM = HRUExcretion / area.getValue(); // only needed if adding to precip in mm, not for throughfall in L
        throughfall.setValue(throughfall.getValue() + HRUExcretion);

    }
}
