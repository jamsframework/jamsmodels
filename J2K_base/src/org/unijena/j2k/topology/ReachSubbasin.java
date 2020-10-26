/*
 * ReachSubbasin.java
 * Created on 26.10.2020, 09:49:59
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Sven Kralisch <sven.kralisch@uni-jena.de>
 */
@JAMSComponentDescription(
        title = "ReachSubbasin",
        author = "Sven Kralisch",
        description = "Retrieve the subbasin of a given reach",
        date = "2020-10-25",
        version = "1.0_0"
)
@VersionComments(entries = {
    @VersionComments.Entry(version = "1.0_0", comment = "Initial version")
})
public class ReachSubbasin extends JAMSComponent {

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

    
    private Map<Attribute.Entity, List<Attribute.Entity>> reach2hruMap = new HashMap();
    
    /*
     *  Component run stages
     */
    @Override
    public void init() {

        Map<Long, Attribute.Entity> reachMap = new HashMap();
        for (Attribute.Entity reach : reaches.getEntities()) {
            reachMap.put(reach.getId(), reach);
        }

        for (Attribute.Entity hru : hrus.getEntities()) {
            double subbasinID = hru.getDouble(subbasinAttributeName.getValue());
            Attribute.Entity reach = reachMap.get((long) subbasinID);

            List<Attribute.Entity> hruList = reach2hruMap.get(reach);
            if (hruList == null) {
                hruList = new ArrayList();
                reach2hruMap.put(reach, hruList);
            }

            hruList.add(hru);
        }
        
        for (Attribute.Entity reach : reaches.getEntities()) {            
            List<Attribute.Entity> hruList = reach2hruMap.get(reach);
            Attribute.EntityCollection hrus = getModel().getRuntime().getDataFactory().createEntityCollection();
            hrus.setEntities(hruList);
            reach.setObject(subbasinEntitiesAttributeName.getValue(), hrus);
        }        
    }

}
