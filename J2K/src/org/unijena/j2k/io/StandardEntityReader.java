/*
 * StandardEntityReader.java
 * Created on 2. November 2005, 15:49
 *
 * This file is part of JAMS
 * Copyright (C) 2005 S. Kralisch and P. Krause
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

package org.unijena.j2k.io;

import org.unijena.j2k.*;
import org.unijena.jams.data.*;
import org.unijena.jams.model.*;
import java.util.*;
import org.unijena.jams.JAMS;

/**
 *
 * @author S. Kralisch
 */
public class StandardEntityReader extends JAMSComponent {
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.INIT,
            description = "Workspace directory name"
            )
            public JAMSString dirName;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.INIT,
            description = "HRU parameter file name"
            )
            public JAMSString hruFileName;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.INIT,
            description = "Reach parameter file name"
            )
            public JAMSString reachFileName;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            update = JAMSVarDescription.UpdateType.INIT,
            description = "Collection of hru objects"
            )
            public JAMSEntityCollection hrus;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            update = JAMSVarDescription.UpdateType.INIT,
            description = "Collection of reach objects"
            )
            public JAMSEntityCollection reaches;
    
    boolean firstRun = true;
    
    public void init() throws JAMSEntity.NoSuchAttributeException {
        
        if (firstRun) {
            //read hru parameter
            hrus = new JAMSEntityCollection();
            hrus.setEntities(J2KFunctions.readParas(dirName.getValue() + "/" + hruFileName.getValue(), getModel()));
            
            //read reach parameter
            reaches = new JAMSEntityCollection();
            reaches.setEntities(J2KFunctions.readParas(dirName.getValue() + "/" + reachFileName.getValue(), getModel()));
            
            //create object associations from id attributes for hrus and reaches
            createTopology();
            
            //create total order on hrus and reaches that allows processing them subsequently
            getModel().getRuntime().println("Create ordered hru-list", JAMS.STANDARD);
            createOrderedList(hrus, "to_poly");
            getModel().getRuntime().println("Create ordered reach-list", JAMS.STANDARD);
            createOrderedList(reaches, "to_reach");
            getModel().getRuntime().println("Entities read successfull!", JAMS.STANDARD);
            
            firstRun = false;
        }
    }
    
    private void createTopology() throws JAMSEntity.NoSuchAttributeException {
        
        HashMap<Double, JAMSEntity> hruMap = new HashMap<Double, JAMSEntity>();
        HashMap<Double, JAMSEntity> reachMap = new HashMap<Double, JAMSEntity>();
        Iterator<JAMSEntity> hruIterator;
        Iterator<JAMSEntity> reachIterator;
        JAMSEntity e;
        
        //put all entities into a HashMap with their ID as key
        hruIterator = hrus.getEntities().iterator();
        while (hruIterator.hasNext()) {
            e = hruIterator.next();
            hruMap.put(e.getDouble("ID"),  e);
        }
        reachIterator = reaches.getEntities().iterator();
        while (reachIterator.hasNext()) {
            e = reachIterator.next();
            reachMap.put(e.getDouble("ID"),  e);
        }
        
        //associate the hru entities with their downstream entity
        hruIterator = hrus.getEntities().iterator();
        while (hruIterator.hasNext()) {
            e = hruIterator.next();
            e.setObject("to_poly", hruMap.get(e.getDouble("to_poly")));
            e.setObject("to_reach", reachMap.get(e.getDouble("to_reach")));
        }
        
        //associate the reach entities with their downstream entity
        reachIterator = reaches.getEntities().iterator();
        while (reachIterator.hasNext()) {
            e = reachIterator.next();
            e.setObject("to_reach", reachMap.get(e.getDouble("to-reach")));
        }
        
    }
    
    private void createOrderedList(JAMSEntityCollection col, String asso) throws JAMSEntity.NoSuchAttributeException {
        
        Iterator<JAMSEntity> hruIterator;
        JAMSEntity e, f;
        ArrayList<JAMSEntity> newList = new ArrayList<JAMSEntity>();
        HashMap<JAMSEntity, Integer> depthMap = new HashMap<JAMSEntity, Integer>();
        Integer eDepth, fDepth;
        boolean mapChanged = true;
        
        hruIterator = col.getEntities().iterator();
        while (hruIterator.hasNext()) {
            depthMap.put(hruIterator.next(), new Integer(0));
        }
        
        int numHRUs = col.getEntities().size();
        
        //put all collection elements (keys) and their depth (values) into a HashMap
        int maxDepth = 0;
        while (mapChanged) {
            mapChanged = false;
            hruIterator = col.getEntities().iterator();
            while (hruIterator.hasNext()) {
                
                e = hruIterator.next();
                f = (JAMSEntity) e.getObject(asso);
                if (f != null) {
                    eDepth = depthMap.get(e);
                    fDepth = depthMap.get(f);
                    if (fDepth.intValue() <= eDepth.intValue()) {
                        /*
                        if (eDepth.intValue() >= numHRUs) {
                            getModel().getRuntime().sendHalt("Found circle in entity parameter file!");
                            return;
                            //System.out.println(e.getDouble("ID") + " -> " + f.getDouble("ID"));
                        }*/
                        depthMap.put(f, new Integer(fDepth.intValue()+1));
                        mapChanged = true;
                        
                    }
                }
            }
        }
        
        
        //find out which is the max depth of all entities
        maxDepth = 0;
        hruIterator = col.getEntities().iterator();
        while (hruIterator.hasNext()) {
            e = hruIterator.next();
            maxDepth = Math.max(maxDepth, depthMap.get(e).intValue());
        }
        
        //create ArrayList of ArrayList objects, each element keeping the entities of one level
        ArrayList<ArrayList<JAMSEntity>> alList = new ArrayList<ArrayList<JAMSEntity>>();
        for (int i=0; i<=maxDepth; i++) {
            alList.add(new ArrayList<JAMSEntity>());
        }
        
        //fill the ArrayList objects within the ArrayList with entity objects
        hruIterator = col.getEntities().iterator();
        while (hruIterator.hasNext()) {
            e = hruIterator.next();
            int depth = depthMap.get(e).intValue();
            alList.get(depth).add(e);
        }
        
        //put the entities
        for (int i=0; i<=maxDepth; i++) {
            hruIterator = alList.get(i).iterator();
            while (hruIterator.hasNext()) {
                e = hruIterator.next();
                newList.add(e);
            }
        }
        col.setEntities(newList);
    }
}
