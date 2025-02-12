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
public class SubbasinStats extends JAMSComponent {

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
            description = "Name of attribute in subbasin HRUs"
    )
    public Attribute.String valueAttributeName;    
                
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "The area attribute of the hrus",
            defaultValue = "area"
    )
    public Attribute.String areaAttributeName;    

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Do area weighted means?",
            defaultValue = "False"
    )
    public Attribute.Boolean areaWeighting;    

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "The value of the sub-basin"
    )
    public Attribute.Double value;    
    
    /*
     *  Component run stages
     */
    
    @Override
    public void run() {
        
        double v = 0;
        double areaSum = 0, area;
        for (Attribute.Entity hru : subbasinHRUs.getEntities()) {
            
            if (areaWeighting.getValue()) {
                area = hru.getDouble(areaAttributeName.getValue());
                v += (hru.getDouble(valueAttributeName.getValue()) * area);
                areaSum += area;
            } else {
                v += hru.getDouble(valueAttributeName.getValue());            
            }
            
        }
        
        if (areaWeighting.getValue()) {
            v = v / areaSum;
        }
        
        value.setValue(v);
    }

}