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
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Auto-detect subbasin ID if not existing? This is a "
                    + "workaround for old J2K parameter files without "
                    + "\"subbasin\" attribute.",
            defaultValue = "true"
    )
    public Attribute.Boolean autoSubbasin;    
    
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
            description = "Name of the attribute describing the HRU to HRU relation in the input file",
            defaultValue = "to_poly")
    public Attribute.String hru2hruAttributeName;
    
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
            description = "Name of the attribute describing the HRU to reach relation in the input file",
            defaultValue = "to_reach")
    public Attribute.String hru2reachAttributeName;         

    
    protected Map<Attribute.Entity, List<Attribute.Entity>> reach2hruMap = new HashMap();
    
    /*
     *  Component run stages
     */
    @Override
    public void init() {
        
        // check if we should auto-detect subbasin IDs
        if (autoSubbasin.getValue() && !hrus.getEntities().get(0).existsAttribute(subbasinAttributeName.getValue())) {

            List<Attribute.Entity> hruList = hrus.getEntities();
            
            for (int i = hruList.size()-1; i >= 0; i--) {
                Attribute.Entity hru = hruList.get(i);

                Attribute.Entity toReach = (Attribute.Entity) hru.getObject(hru2reachAttributeName.getValue());
                Attribute.Entity toHRU = (Attribute.Entity) hru.getObject(hru2hruAttributeName.getValue());

                if (toReach.getId() != -1) {
                    hru.setDouble(subbasinAttributeName.getValue(), toReach.getId());
                } else if (toHRU.getId() != -1) {
                    if (toHRU.existsAttribute(subbasinAttributeName.getValue())) {
                        hru.setDouble(subbasinAttributeName.getValue(), toHRU.getDouble(subbasinAttributeName.getValue()));
                    } else {
                        getModel().getRuntime().println("Problem: No subbbasin found for HRU " + hru.getId());
                    }
                }
            }
        }           

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
