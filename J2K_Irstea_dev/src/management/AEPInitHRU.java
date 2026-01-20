/*
 * SewerOverflowDevice.java
 * Created on 05. October 2012, 17:02
 *
 * This file is part of JAMS
 * Copyright (C) FSU Jena
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA
 *
 */
package management;

import jams.data.*;
import jams.model.*;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.util.stream.Collectors;


/**
 *
 * @author Sven Kralisch & Mériem Labbas & Christian Fischer
 */
@JAMSComponentDescription(title = "AEP device to link release reaches and extraction HRUs/reaches",
        author = "ALB",
        description = "Component used to create a new attribute for release reaches,"
        + "which contains the list of HRUs in which extraction occurs.",
        version = "1.0_0",
        date = "2025-12-19")
public class AEPInitHRU extends JAMSComponent {

    /*
     * Component variables
     */ 
        @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Reaches list"
        )
        public Attribute.EntityCollection reaches;
                
        @JAMSVarDescription(
                access = JAMSVarDescription.AccessType.READ,
                description = "HRUs list"
        )
        public Attribute.EntityCollection hrus;
        
        @JAMSVarDescription(
                access = JAMSVarDescription.AccessType.READ,
                description = "Name of list of HRUs in which extraction occurs. Will be written for each release reach."
                        + "List will be read by this component. - parameter / pointer",
                defaultValue = "aepHRUEntities"
        )
        public Attribute.String aepHRUEntitiesListName;

        @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Reach ID where release occurs (attribute release_reach_ID from hru.par). - parameter"
        )
        public Attribute.Double releaseReach;
    
 
        private Map<Long, Attribute.Entity> run_reachMap = new HashMap();

    /*
     *  Component run stages
     */
        @Override
        public void init() {
            
            // put all reaches to a map for easier access -> used for the release reaches
            for (Attribute.Entity run_reach : reaches.getEntities()) {
                run_reachMap.put(run_reach.getId(), run_reach);
            }
    }
        
        @Override
        public void run() {
       
            // check
            getModel().getRuntime().println("HRU used for AEP (extraction):"+hrus.getCurrent().getId());
            
            // get the matching reach (release reach) for the current entity
            Attribute.Entity run_entity = hrus.getCurrent(); // current entity, in which extraction occurs
            Attribute.Entity release_reach = run_reachMap.get((long) releaseReach.getValue()); // corresponding reach (in which release occurs)

            // add the current entity to the list of HRUs in which extraction occurs
            if (!release_reach.existsAttribute(aepHRUEntitiesListName.getValue())) {
                release_reach.setObject(aepHRUEntitiesListName.getValue(), new ArrayList<Attribute.Entity>()); // if list aepHRUEntitiesListName doesn't exist for the release reach, create an empty list
            }
            List<Attribute.Entity> run_l = (List) release_reach.getObject(aepHRUEntitiesListName.getValue()); // put aepHRUEntities into list run_l

            run_l.add(run_entity); // add extraction HRU to the list of extraction HRUs, list that belongs to the corresponding release reach


            // check
             long ID_reach_rejet_1 = 478600;
             if (run_reachMap.get(ID_reach_rejet_1).existsAttribute(aepHRUEntitiesListName.getValue())) {
                getModel().getRuntime().println("- AEPInitHRU - reach rejet (" + ID_reach_rejet_1 + "): $IDs dans aepHRUEntities = " + ((List<Attribute.Entity>) run_reachMap.get(ID_reach_rejet_1).getObject(aepHRUEntitiesListName.getValue())).stream().map(e -> String.valueOf(e.getId())).collect(Collectors.joining(", ", "[", "]")));
             }
             
             long ID_reach_rejet_2 = 478400;
             if (run_reachMap.get(ID_reach_rejet_2).existsAttribute(aepHRUEntitiesListName.getValue())) {
                getModel().getRuntime().println("- AEPInitHRU - reach rejet (" + ID_reach_rejet_2 + "): $IDs dans aepHRUEntities = " + ((List<Attribute.Entity>) run_reachMap.get(ID_reach_rejet_2).getObject(aepHRUEntitiesListName.getValue())).stream().map(e -> String.valueOf(e.getId())).collect(Collectors.joining(", ", "[", "]")));
             }

    }
}

