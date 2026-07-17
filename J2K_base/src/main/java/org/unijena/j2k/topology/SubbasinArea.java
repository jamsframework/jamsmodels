/*
 * SubbasinArea.java
 * Created on 26.10.2020, 11:08:12
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

package org.unijena.j2k.topology;

import jams.data.*;
import jams.model.*;
import java.util.List;

/**
 *
 * @author Sven Kralisch <sven.kralisch@uni-jena.de>
 */
@JAMSComponentDescription(
        title = "SubbasinArea",
        author = "Sven Kralisch",
        description = "Calc the area of the subbasin of a given reach",
        date = "2020-10-25",
        version = "1.0_0"
)
@VersionComments(entries = {
    @VersionComments.Entry(version = "1.0_0", comment = "Initial version")
})
public class SubbasinArea extends JAMSComponent {

    /*
     *  Component attributes
     */
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Subbasin HRUs"
    )
    public Attribute.EntityCollection subbasinHRUs;     
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Name of area attribute in subbasin HRUs",
            defaultValue = "area"
    )
    public Attribute.String areaAttributeName; 
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "The area of the sub-basin"
    )
    public Attribute.Double area;    
                
    /*
     *  Component run stages
     */
    
    @Override
    public void run() {
        
        double a = 0;
        for (Attribute.Entity hru : subbasinHRUs.getEntities()) {
            
            double hruArea = hru.getDouble(areaAttributeName.getValue());
            a += hruArea;
            
        }
        
        area.setValue(a);
        
    }

}