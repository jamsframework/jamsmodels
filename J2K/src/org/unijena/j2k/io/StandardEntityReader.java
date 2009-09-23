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

//import org.unijena.j2k.*;
import jams.JAMS;
import jams.JAMSTools;
import jams.data.Attribute;
import jams.data.JAMSDataFactory;
import jams.data.JAMSDouble;
import jams.data.JAMSEntity;
import jams.data.JAMSEntityCollection;
import jams.data.JAMSString;
import jams.model.JAMSComponent;
import jams.model.JAMSVarDescription;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Stack;
import org.unijena.j2k.J2KFunctions;

/**
 *
 * @author S. Kralisch
 */
public class StandardEntityReader extends JAMSComponent {

    @JAMSVarDescription (access = JAMSVarDescription.AccessType.READ,
                         description = "HRU parameter file name")
    public JAMSString hruFileName;

    @JAMSVarDescription (access = JAMSVarDescription.AccessType.READ,
                         description = "Reach parameter file name")
    public JAMSString reachFileName;

    @JAMSVarDescription (access = JAMSVarDescription.AccessType.WRITE,
                         description = "Collection of hru objects")
    public JAMSEntityCollection hrus;

    @JAMSVarDescription (access = JAMSVarDescription.AccessType.WRITE,
                         description = "Collection of reach objects")
    public JAMSEntityCollection reaches;

    @Override
    public void init() throws Attribute.Entity.NoSuchAttributeException {
        
        //read hru parameter
        hrus.setEntities(J2KFunctions.readParas(JAMSTools.CreateAbsoluteFileName(getModel().getWorkspaceDirectory().getPath(), hruFileName.getValue()), getModel()));

        //assign IDs to all hru entities
        for (Attribute.Entity e : hrus.getEntityArray()) {
            try {
                e.setId((long) e.getDouble("ID"));
            } catch (Attribute.Entity.NoSuchAttributeException nsae) {
                getModel().getRuntime().sendErrorMsg("Couldn't find attribute \"ID\" while reading J2K HRU parameter file (" + hruFileName.getValue() + ")!");
            }
        }

        //read reach parameter
        reaches.setEntities(J2KFunctions.readParas(JAMSTools.CreateAbsoluteFileName(getModel().getWorkspaceDirectory().getPath(), reachFileName.getValue()), getModel()));

        //assign IDs to all reach entities
        for (Attribute.Entity e : reaches.getEntityArray()) {
            try {
                e.setId((long) e.getDouble("ID"));
            } catch (Attribute.Entity.NoSuchAttributeException nsae) {
                getModel().getRuntime().sendErrorMsg("Couldn't find attribute \"ID\" while reading J2K HRU parameter file (" + hruFileName.getValue() + ")!");
            }
        }

        //create object associations from id attributes for hrus and reaches
        createTopology();

        //create total order on hrus and reaches that allows processing them subsequently
        getModel().getRuntime().println("Create ordered hru-list", JAMS.VERBOSE);
        createOrderedList(hrus, "to_poly");
        getModel().getRuntime().println("HRU entities read successfully", JAMS.STANDARD);
        getModel().getRuntime().println("Create ordered reach-list", JAMS.VERBOSE);
        createOrderedList(reaches, "to_reach");
        getModel().getRuntime().println("Reach entities read successfully", JAMS.STANDARD);
    }

    //do depth first search to find cycles
    protected boolean cycleCheck(Attribute.Entity node, Stack<Attribute.Entity> searchStack, HashSet<Attribute.Double> closedList, HashSet<Attribute.Double> visitedList) throws JAMSEntity.NoSuchAttributeException {
        Attribute.Entity child_node;

        //current node allready in search stack -> circle found
        if (searchStack.indexOf(node) != -1) {
            int index = searchStack.indexOf(node);

            String cyc_output = new String();
            for (int i = index; i < searchStack.size(); i++) {
                cyc_output += ((Attribute.Entity) searchStack.get(i)).getDouble("ID") + " ";
            }
            getModel().getRuntime().println("Found circle with ids:" + cyc_output);

            return true;
        }
        //node in closed list? -> then skip it
        if (closedList.contains(node.getObject("ID")) == true) {
            return false;
        }
        //now this node is visited
        visitedList.add((JAMSDouble) node.getObject("ID"));

        child_node = (JAMSEntity) node.getObject("to_poly");
        if (child_node.getValue() == null) {
            child_node = null;
        }

        if (child_node != null) {
            //push current node to search stack
            searchStack.push(node);

            boolean result = cycleCheck(child_node, searchStack, closedList, visitedList);

            searchStack.pop();

            return result;
        }
        return false;
    }

    protected boolean cycleCheck() throws Attribute.Entity.NoSuchAttributeException {
        Iterator<Attribute.Entity> hruIterator;

        HashSet<Attribute.Double> closedList = new HashSet<Attribute.Double>();
        HashSet<Attribute.Double> visitedList = new HashSet<Attribute.Double>();

        Attribute.Entity start_node;

        getModel().getRuntime().println("Cycle checking...");

        hruIterator = hrus.getEntities().iterator();

        boolean result = false;

        while (hruIterator.hasNext()) {
            start_node = hruIterator.next();
            //connected component of start_node allready processed?
            if (closedList.contains(start_node.getObject("ID")) == false) {
                if (cycleCheck(start_node, new Stack<Attribute.Entity>(), closedList, visitedList) == true) {
                    result = true;
                }
                closedList.addAll(visitedList);
                visitedList.clear();
            }

        }
        return result;
    }

    protected void createTopology() throws Attribute.Entity.NoSuchAttributeException {

        HashMap<Double, Attribute.Entity> hruMap = new HashMap<Double, Attribute.Entity>();
        HashMap<Double, Attribute.Entity> reachMap = new HashMap<Double, Attribute.Entity>();
        Iterator<Attribute.Entity> hruIterator;
        Iterator<Attribute.Entity> reachIterator;
        Attribute.Entity e;

        //put all entities into a HashMap with their ID as key
        hruIterator = hrus.getEntities().iterator();
        while (hruIterator.hasNext()) {
            e = hruIterator.next();
            hruMap.put(e.getDouble("ID"), e);
        }
        reachIterator = reaches.getEntities().iterator();
        while (reachIterator.hasNext()) {
            e = reachIterator.next();
            reachMap.put(e.getDouble("ID"), e);
        }

        //create empty entities, i.e. those that are linked to in case there is no linkage ;-)
        Attribute.Entity nullEntity = JAMSDataFactory.createEntity();
        nullEntity.setValue((HashMap<String, Object>) null);
        hruMap.put(new Double(0), nullEntity);
        reachMap.put(new Double(0), nullEntity);

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

        //check for cycles
        if (this.getModel().getRuntime().getDebugLevel() >= JAMS.VVERBOSE) {
            if (cycleCheck() == true) {
                getModel().getRuntime().println("HRUs --> cycle found ... :( ");
            } else {
                getModel().getRuntime().println("HRUs --> no cycle found");
            }
        }

    }

    protected void createOrderedList(JAMSEntityCollection col, String asso) throws Attribute.Entity.NoSuchAttributeException {

        Iterator<Attribute.Entity> hruIterator;
        Attribute.Entity e, f;
        ArrayList<Attribute.Entity> newList = new ArrayList<Attribute.Entity>();
        HashMap<Attribute.Entity, Integer> depthMap = new HashMap<Attribute.Entity, Integer>();
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

                f = (Attribute.Entity) e.getObject(asso);
                if (f.getValue() == null) {
                    f = null;
                }

                if (f != null) {
                    eDepth = depthMap.get(e);
                    fDepth = depthMap.get(f);
                    if (fDepth.intValue() <= eDepth.intValue()) {
                        depthMap.put(f, new Integer(eDepth.intValue() + 1));
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
        ArrayList<ArrayList<Attribute.Entity>> alList = new ArrayList<ArrayList<Attribute.Entity>>();
        for (int i = 0; i <= maxDepth; i++) {
            alList.add(new ArrayList<Attribute.Entity>());
        }

        //fill the ArrayList objects within the ArrayList with entity objects
        hruIterator = col.getEntities().iterator();
        while (hruIterator.hasNext()) {
            e = hruIterator.next();
            int depth = depthMap.get(e).intValue();
            alList.get(depth).add(e);
        }

        //put the entities
        for (int i = 0; i <= maxDepth; i++) {
            hruIterator = alList.get(i).iterator();
            while (hruIterator.hasNext()) {
                e = hruIterator.next();
                newList.add(e);
            }
        }
        col.setEntities(newList);
    }
}
