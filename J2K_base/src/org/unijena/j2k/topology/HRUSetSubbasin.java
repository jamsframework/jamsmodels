/*
 * HRUSetSubbasin.java
 * Created on 01.07.2025, 22:08:36
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
 * @author Sven Kralisch <sven at kralisch.com>
 */
@JAMSComponentDescription(
    title="Title",
    author="Author",
    description="Description",
    date = "YYYY-MM-DD",
    version = "1.0_0"
)
@VersionComments(entries = {
    @VersionComments.Entry(version = "1.0_0", comment = "Initial version")
})
public class HRUSetSubbasin extends JAMSComponent {

    /*
     *  Component attributes
     */
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "List of hrus"
    )
    public Attribute.EntityCollection hrus;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "List of reaches"
    )
    public Attribute.EntityCollection reaches;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Name of sub-basin attribute in HRUs",
            defaultValue = "subbasin"
    )
    public Attribute.String subbasinAttributeName;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Name of sub-basin entity collection in reaches",
            defaultValue = "subbasinhrus"
    )
    public Attribute.String subbasinEntitiesAttributeName;       
                
    /*
     *  Component run stages
     */
    
    @Override
    public void init() {

        for (Attribute.Entity reach : reaches.getEntities()) {
        
            Attribute.EntityCollection hrus = (Attribute.EntityCollection ) reach.getObject(subbasinEntitiesAttributeName.getValue());
            
            for (Attribute.Entity hru : hrus.getEntities()) {

                hru.setDouble(subbasinAttributeName.getValue(), reach.getId());
                
            }
        }
    }

}