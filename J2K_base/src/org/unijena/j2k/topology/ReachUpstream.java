/*
 * ReachUpstream.java
 * Created on 18.05.2022, 23:33:28
 *
 * This file is part of JAMS
 * Copyright (C) Sven Kralisch <sven at kralisch.com>
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
 * @author Sven Kralisch <sven at kralisch.com>
 */
@JAMSComponentDescription(
        title = "Title",
        author = "Author",
        description = "Description",
        date = "YYYY-MM-DD",
        version = "1.0_0"
)
@VersionComments(entries = {
    @VersionComments.Entry(version = "1.0_0", comment = "Initial version")
})
public class ReachUpstream extends JAMSComponent {

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
            description = "Name of sub-basin entity collection in reaches",
            defaultValue = "subbasinhrus"
    )
    public Attribute.String upstreamEntitiesAttributeName;    

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Name of downstream linkage",
            defaultValue = "to_reach"
    )
    public Attribute.String reach2reachAttributeName;    
    
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
            description = "Name of the attribute describing the HRU to HRU relation in the input file",
            defaultValue = "to_poly")
    public Attribute.String hru2hruAttributeName;
    
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
            description = "Name of the attribute describing the HRU to reach relation in the input file",
            defaultValue = "to_reach")
    public Attribute.String hru2reachAttributeName;        
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Auto-detect subbasin ID if not existing? This is a "
                    + "workaround for old J2K parameter files without "
                    + "\"subbasin\" attribute.",
            defaultValue = "true"
    )
    public Attribute.Boolean autoSubbasin;    

    private Map<Attribute.Entity, List<Attribute.Entity>> reach2hruMap = new HashMap();

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

        for (Attribute.Entity reach : reaches.getEntities()) {
            reach.setObject("__upstreamHRUs", new ArrayList<Attribute.Entity>());
        }

        for (Attribute.Entity reach : reaches.getEntities()) {

            List<Attribute.Entity> upstreamList = (List<Attribute.Entity>) reach.getObject("__upstreamHRUs");
            List<Attribute.Entity> subbasinList = reach2hruMap.get(reach);
            upstreamList.addAll(subbasinList);
            
            Attribute.EntityCollection hrus = getModel().getRuntime().getDataFactory().createEntityCollection();
            hrus.setEntities(upstreamList);
            reach.setObject(upstreamEntitiesAttributeName.getValue(), hrus);            

            Attribute.Entity destReach = (Attribute.Entity) reach.getObject(reach2reachAttributeName.getValue());
            if (destReach != null) {
                List<Attribute.Entity> destReachList = (List<Attribute.Entity>) destReach.getObject("__upstreamHRUs");
                destReachList.addAll(upstreamList);
            }

        }

    }
}
