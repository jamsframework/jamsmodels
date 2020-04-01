/*
 * StandardEntityReaderGI4sewers.java
 * Updated on on March 2020 from StandardEntityReader_2sewers.java
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
package io;

//import org.unijena.j2k.*;
import jams.JAMS;
import jams.data.Attribute;
import jams.data.DefaultDataFactory;
import jams.model.JAMSComponent;
import jams.model.JAMSComponentDescription;
import jams.model.JAMSVarDescription;
import jams.tools.FileTools;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Stack;

/**
 *
 * @author S. Kralisch
 */
@JAMSComponentDescription(title = "StandardEntitiesReader",
author = "Meriem Labbas",
description = "This component reads four ASCII files containing data of hru, "
+ "reach and 2 sewers entities and creates four collections of entities accordingly. "
+ "1:n topologies between different entities are created based on provided "
+ "attribute names. Sewer overflow devices are linked to the receiving reach. "
+ "Additionally, the topologies are checked for cycles."
+ "Updated in February/March/April 2020 by Jeremie Bonneau for the Ratier catchment"
+ "to include 4 sewer networks",
date = "2013-10-27",
version = "1.0")
public class StandardEntityReaderGI4sewers extends JAMSComponent {

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    description = "HRU parameter file name")
    public Attribute.String hruFileName;
    
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    description = "Reach parameter file name")
    public Attribute.String reachFileName;
    
	// read sewer1 parameter file 
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    description = "Sewer1 parameter file name")
    public Attribute.String sewer1FileName;

	// read sewer2 parameter file    
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    description = "Sewer2 parameter file name")
    public Attribute.String sewer2FileName;
        
	// read sewer3 parameter file    
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    description = "Sewer3 parameter file name")
    public Attribute.String sewer3FileName;
	
	// read sewer4 parameter file    
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    description = "Sewer4 parameter file name")
    public Attribute.String sewer4FileName;

	// read GI parameter file - optional as of March 2020 JB
	//@JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,	
    //description = "GI parameter file name")
    //public Attribute.String GIFileName; 	
       
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.WRITE,
    description = "Collection of hru objects")
    public Attribute.EntityCollection hrus;
    
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.WRITE,
    description = "Collection of reach objects")
    public Attribute.EntityCollection reaches;
    
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.WRITE,
    description = "Collection of sewer n°1 objects")
    public Attribute.EntityCollection sewers1;
    
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.WRITE,
    description = "Collection of sewer n°2 objects")
    public Attribute.EntityCollection sewers2;

	// addition of entity collection sewer 3  
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.WRITE,
    description = "Collection of sewer n°3 objects")
    public Attribute.EntityCollection sewers3;
	
	// addition of entity collection sewer 4 	
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.WRITE,
    description = "Collection of sewer n°4 objects")
    public Attribute.EntityCollection sewers4;
	
	// addition of entity collection  
	@JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    description = "Name of the attribute containing the HRU identifiers",
    defaultValue = "ID")
    public Attribute.String hruIDAttribute;
    
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    description = "Name of the attribute containing the reach identifiers",
    defaultValue = "ID")
    public Attribute.String reachIDAttribute;
    
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    description = "Name of the attribute containing the sewer 1 identifiers",
    defaultValue = "ID")
    public Attribute.String sewer1IDAttribute;
    
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    description = "Name of the attribute containing the sewer 2 identifiers",
    defaultValue = "ID")
    public Attribute.String sewer2IDAttribute;

	// addition of sewer3attribute
	@JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    description = "Name of the attribute containing the sewer 3 identifiers",
    defaultValue = "ID")
    public Attribute.String sewer3IDAttribute;

	// addition of sewer4attribute
	@JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    description = "Name of the attribute containing the sewer 4 identifiers",
    defaultValue = "ID")
    public Attribute.String sewer4IDAttribute;

	// addition of GI ID attribute
 	//@JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    //description = "Name of the attribute containing the sewer 4 identifiers",
    //defaultValue = "ID")
    //public Attribute.String GIIDAttribute;
	
    //to poly/reach/sewer1.2.3.4 attribute
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    description = "Name of the attribute describing the HRU to HRU relation in the input file",
    defaultValue = "to_poly")
    public Attribute.String hruTohruAttribute;
    
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    description = "Name of the attribute describing the HRU to reach relation in the input file",
    defaultValue = "to_reach")
    public Attribute.String hruToreachAttribute;
    
	//to sewer 1 attribute
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    description = "Name of the attribute describing the HRU to sewer 1 relation in the input file",
    defaultValue = "to_sewer1")
    public Attribute.String hruTosewer1Attribute;
    
	//to sewer 2 attribute
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    description = "Name of the attribute describing the HRU to sewer 2 relation in the input file",
    defaultValue = "to_sewer2")
    public Attribute.String hruTosewer2Attribute;
	
	//to sewer 3 attribute
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    description = "Name of the attribute describing the HRU to sewer 2 relation in the input file",
    defaultValue = "to_sewer3")
    public Attribute.String hruTosewer3Attribute;	
	
	//to sewer 4 attribute
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    description = "Name of the attribute describing the HRU to sewer 2 relation in the input file",
    defaultValue = "to_sewer4")
    public Attribute.String hruTosewer4Attribute;
	
	// to GI
    //@JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    //description = "Name of the attribute describing the HRU to sewer 2 relation in the input file",
    //defaultValue = "to_GI")
    //public Attribute.String hruToGIAttribute;

	// connection reach -> reach, sewer -> sewer 	
	@JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    description = "Name of the attribute describing the reach to reach relation in the input file",
    defaultValue = "to_reach")
    public Attribute.String reachToreachAttribute;
    
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    description = "Name of the attribute describing the sewer 1 to sewer 1 relation in the input file",
    defaultValue = "to_reach")
    public Attribute.String sewer1Tosewer1Attribute;
    
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    description = "Name of the attribute describing the sewer 2 to sewer 2 relation in the input file",
    defaultValue = "to_reach")
    public Attribute.String sewer2Tosewer2Attribute;
	
	//sewer 3 to sewer 3 
	@JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    description = "Name of the attribute describing the sewer 3 to sewer 3 relation in the input file",
    defaultValue = "to_reach")
    public Attribute.String sewer3Tosewer3Attribute;
	
	//sewer 4 to sewer 4 
	@JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    description = "Name of the attribute describing the sewer 4 to sewer 4 relation in the input file",
    defaultValue = "to_reach")
    public Attribute.String sewer4Tosewer4Attribute;
    
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    description = "Name of the attribute describing the sewer 1 to reach relation in the input file",
    defaultValue = "to_river")
    public Attribute.String sewer1ToreachAttribute; 
	
	// Addition of GI
	
	// Addition of connection from GI to sewer 1 
    //@JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    //description = "Name of the attribute describing the sewer 1 to reach relation in the input file",
    //defaultValue = "to_sewer1")
    //public Attribute.String GITosewer1Attribute;

	//Addition of connection from GI to sewer 2 
    //@JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    //description = "Name of the attribute describing the sewer 1 to reach relation in the input file",
    //defaultValue = "to_sewer2")
    //public Attribute.String GITosewer2Attribute;
	
	//Addition of connection from GI to sewer 3 
    //@JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    //description = "Name of the attribute describing the sewer 3 to reach relation in the input file",
    //defaultValue = "to_sewer3")
    //public Attribute.String GITosewer3Attribute;
	
	//Addition of connection from GI to sewer 4
    //@JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    //description = "Name of the attribute describing the sewer 4 to reach relation in the input file",
    //defaultValue = "to_sewer4")
    //public Attribute.String GITosewer4Attribute;
    
    @Override
    public void init() throws Attribute.Entity.NoSuchAttributeException {

        //read hru parameter
        hrus.setEntities(J2KFunctions.readParas(FileTools.createAbsoluteFileName(getModel().getWorkspaceDirectory().getPath(), hruFileName.getValue()), getModel()));

        //assign IDs to all hru entities
        for (Attribute.Entity e : hrus.getEntityArray()) {
            try {
                e.setId((long) e.getDouble(hruIDAttribute.getValue()));
            } catch (Attribute.Entity.NoSuchAttributeException nsae) {
                getModel().getRuntime().sendErrorMsg("Couldn't find attribute \"ID\" while reading J2K HRU parameter file (" + hruFileName.getValue() + ")!");
            }
        }

        //read reach parameter
        reaches.setEntities(J2KFunctions.readParas(FileTools.createAbsoluteFileName(getModel().getWorkspaceDirectory().getPath(), reachFileName.getValue()), getModel()));

        //assign IDs to all reach entities
        for (Attribute.Entity e : reaches.getEntityArray()) {
            try {
                e.setId((long) e.getDouble(reachIDAttribute.getValue()));
            } catch (Attribute.Entity.NoSuchAttributeException nsae) {
                getModel().getRuntime().sendErrorMsg("Couldn't find attribute \"ID\" while reading J2K HRU parameter file (" + reachFileName.getValue() + ")!");
            }
        }

        //read sewer 1 parameter
        sewers1.setEntities(J2KFunctions.readParas(FileTools.createAbsoluteFileName(getModel().getWorkspaceDirectory().getPath(), sewer1FileName.getValue()), getModel()));

        //assign IDs to all sewer entities
        for (Attribute.Entity e : sewers1.getEntityArray()) {
            try {
                e.setId((long) e.getDouble(sewer1IDAttribute.getValue()));
            } catch (Attribute.Entity.NoSuchAttributeException nsae) {
                getModel().getRuntime().sendErrorMsg("Couldn't find attribute \"ID\" while reading J2K sewer 1 parameter file (" + sewer1FileName.getValue() + ")!");
            }
        }  
        
        //read sewer 2 parameter
        sewers2.setEntities(J2KFunctions.readParas(FileTools.createAbsoluteFileName(getModel().getWorkspaceDirectory().getPath(), sewer2FileName.getValue()), getModel()));

        //assign IDs to all sewer entities
        for (Attribute.Entity e : sewers2.getEntityArray()) {
            try {
                e.setId((long) e.getDouble(sewer2IDAttribute.getValue()));
            } catch (Attribute.Entity.NoSuchAttributeException nsae) {
                getModel().getRuntime().sendErrorMsg("Couldn't find attribute \"ID\" while reading J2K sewer 2 parameter file (" + sewer2FileName.getValue() + ")!");
            }
        }
        

        //read sewer 3 parameter
        sewers3.setEntities(J2KFunctions.readParas(FileTools.createAbsoluteFileName(getModel().getWorkspaceDirectory().getPath(), sewer3FileName.getValue()), getModel()));

        //assign IDs to all sewer entities
        for (Attribute.Entity e : sewers3.getEntityArray()) {
            try {
                e.setId((long) e.getDouble(sewer3IDAttribute.getValue()));
            } catch (Attribute.Entity.NoSuchAttributeException nsae) {
                getModel().getRuntime().sendErrorMsg("Couldn't find attribute \"ID\" while reading J2K sewer 3 parameter file (" + sewer3FileName.getValue() + ")!");
            }
        }
        

        //read sewer 4 parameter
        sewers4.setEntities(J2KFunctions.readParas(FileTools.createAbsoluteFileName(getModel().getWorkspaceDirectory().getPath(), sewer4FileName.getValue()), getModel()));

        //assign IDs to all sewer entities
        for (Attribute.Entity e : sewers4.getEntityArray()) {
            try {
                e.setId((long) e.getDouble(sewer4IDAttribute.getValue()));
            } catch (Attribute.Entity.NoSuchAttributeException nsae) {
                getModel().getRuntime().sendErrorMsg("Couldn't find attribute \"ID\" while reading J2K sewer 4 parameter file (" + sewer4FileName.getValue() + ")!");
            }
        }
        

        //read GI parameter
        //GI.setEntities(J2KFunctions.readParas(FileTools.createAbsoluteFileName(getModel().getWorkspaceDirectory().getPath(), sewer4FileName.getValue()), getModel()));

        //assign IDs to all sewer entities
        //for (Attribute.Entity e : GI.getEntityArray()) {
        //    try {
        //        e.setId((long) e.getDouble(sewer4IDAttribute.getValue()));
        //    } catch (Attribute.Entity.NoSuchAttributeException nsae) {
        //        getModel().getRuntime().sendErrorMsg("Couldn't find attribute \"ID\" while reading J2K sewer 4 parameter file (" + sewer4FileName.getValue() + ")!");
        //    }
        //}
        



        //create object associations from id attributes for hrus, reaches, sewers1 and sewers2
        createTopology();

        //create total order on hrus and reaches that allows processing them subsequently
        getModel().getRuntime().println("Create ordered hru-list", JAMS.VERBOSE);
        createOrderedList(hrus, hruTohruAttribute.getValue());
        getModel().getRuntime().println("HRU entities read successfully", JAMS.STANDARD);
        getModel().getRuntime().println("Create ordered reach-list", JAMS.VERBOSE);
        createOrderedList(reaches, reachToreachAttribute.getValue());
        getModel().getRuntime().println("Reach entities read successfully", JAMS.STANDARD);
        getModel().getRuntime().println("Create ordered reach-list", JAMS.VERBOSE);
        createOrderedList(sewers1, sewer1Tosewer1Attribute.getValue());
        getModel().getRuntime().println("Sewer 1 entities read successfully", JAMS.STANDARD);
        getModel().getRuntime().println("Create ordered sewer1-list", JAMS.VERBOSE);
        createOrderedList(sewers2, sewer2Tosewer2Attribute.getValue());
        getModel().getRuntime().println("Sewer 2 entities read successfully", JAMS.STANDARD);
        getModel().getRuntime().println("Create ordered sewer2-list", JAMS.VERBOSE);
		getModel().getRuntime().println("Sewer 3 entities read successfully", JAMS.STANDARD);
        getModel().getRuntime().println("Create ordered sewer3-list", JAMS.VERBOSE);
		getModel().getRuntime().println("Sewer 4 entities read successfully", JAMS.STANDARD);
        getModel().getRuntime().println("Create ordered sewer4-list", JAMS.VERBOSE);
    }

    //do depth first search to find cycles
    protected boolean cycleCheck(Attribute.Entity node, Stack<Attribute.Entity> searchStack, HashSet<Long> closedList, HashSet<Long> visitedList) throws Attribute.Entity.NoSuchAttributeException {
        Attribute.Entity child_node;

        //current node allready in search stack -> circle found
        if (searchStack.indexOf(node) != -1) {
            int index = searchStack.indexOf(node);

            String cyc_output = new String();
            for (int i = index; i < searchStack.size(); i++) {
                cyc_output += ((Attribute.Entity) searchStack.get(i)).getId() + " ";
            }
            getModel().getRuntime().println("Found circle with ids:" + cyc_output);

            return true;
        }
        //node in closed list? -> then skip it
        if (closedList.contains(node.getId()) == true) {
            return false;
        }
        //now this node is visited
        visitedList.add(node.getId());

        child_node = (Attribute.Entity) node.getObject(hruTohruAttribute.getValue());
        if ((child_node != null) && (child_node.isEmpty())) {
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

        HashSet<Long> closedList = new HashSet<Long>();
        HashSet<Long> visitedList = new HashSet<Long>();

        Attribute.Entity start_node;

        getModel().getRuntime().println("Cycle checking...");

        hruIterator = hrus.getEntities().iterator();

        boolean result = false;

        while (hruIterator.hasNext()) {
            start_node = hruIterator.next();
            //connected component of start_node allready processed?
            if (closedList.contains(start_node.getId()) == false) {
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
        HashMap<Double, Attribute.Entity> sewer1Map = new HashMap<Double, Attribute.Entity>();
        HashMap<Double, Attribute.Entity> sewer2Map = new HashMap<Double, Attribute.Entity>();
		// addition swer 3 and 4
		HashMap<Double, Attribute.Entity> sewer3Map = new HashMap<Double, Attribute.Entity>();
        HashMap<Double, Attribute.Entity> sewer4Map = new HashMap<Double, Attribute.Entity>();

        Iterator<Attribute.Entity> hruIterator;
        Iterator<Attribute.Entity> reachIterator;
        Iterator<Attribute.Entity> sewer1Iterator;
        Iterator<Attribute.Entity> sewer2Iterator;
		// addition sewer 3 and 4
		Iterator<Attribute.Entity> sewer3Iterator;
		Iterator<Attribute.Entity> sewer4Iterator;
        Attribute.Entity e, toPoly, toReach, toSewer1, toSewer2, toSewer3, toSewer4;

        //put all entities into a HashMap with their ID as key
        hruIterator = hrus.getEntities().iterator();
        while (hruIterator.hasNext()) {
            e = hruIterator.next();
            hruMap.put(e.getDouble(hruIDAttribute.getValue()), e);
        }
        reachIterator = reaches.getEntities().iterator();
        while (reachIterator.hasNext()) {
            e = reachIterator.next();
            reachMap.put(e.getDouble(reachIDAttribute.getValue()), e);
        }
        sewer1Iterator = sewers1.getEntities().iterator();
        while (sewer1Iterator.hasNext()) {
            e = sewer1Iterator.next();
            sewer1Map.put(e.getDouble(sewer1IDAttribute.getValue()), e);
        }
        sewer2Iterator = sewers2.getEntities().iterator();
        while (sewer2Iterator.hasNext()) {
            e = sewer2Iterator.next();
            sewer2Map.put(e.getDouble(sewer2IDAttribute.getValue()), e);
        }
		// addition sewer 3 and 4 
		sewer3Iterator = sewers3.getEntities().iterator();
        while (sewer3Iterator.hasNext()) {
            e = sewer3Iterator.next();
            sewer3Map.put(e.getDouble(sewer3IDAttribute.getValue()), e);
        }
		sewer4Iterator = sewers4.getEntities().iterator();
        while (sewer4Iterator.hasNext()) {
            e = sewer4Iterator.next();
            sewer4Map.put(e.getDouble(sewer4IDAttribute.getValue()), e);
        }
		

        //create empty entities, i.e. those that are linked to in case there is no linkage ;-)
        Attribute.Entity nullEntity = getModel().getRuntime().getDataFactory().createEntity();
        hruMap.put(new Double(0), nullEntity);
        reachMap.put(new Double(0), nullEntity);
        sewer1Map.put(new Double(0), nullEntity);
        sewer2Map.put(new Double(0), nullEntity);
		// addition sewer 3 and 4 
		sewer3Map.put(new Double(0), nullEntity);
		sewer4Map.put(new Double(0), nullEntity);

        //associate the hru entities with their downstream entity
        hruIterator = hrus.getEntities().iterator();
        while (hruIterator.hasNext()) {
            e = hruIterator.next();
            toPoly = hruMap.get(e.getDouble(hruTohruAttribute.getValue()));
            toReach = reachMap.get(e.getDouble(hruToreachAttribute.getValue()));
            toSewer1 = sewer1Map.get(e.getDouble(hruTosewer1Attribute.getValue()));
            toSewer2 = sewer2Map.get(e.getDouble(hruTosewer2Attribute.getValue()));
			toSewer3 = sewer3Map.get(e.getDouble(hruTosewer3Attribute.getValue()));
			toSewer4 = sewer4Map.get(e.getDouble(hruTosewer4Attribute.getValue()));

            if ((toPoly == null) || (toReach == null) || (toSewer1 == null) || (toSewer2 == null) || (toSewer3 == null) || (toSewer4 == null)) {
                getModel().getRuntime().sendErrorMsg("Topological neighbour for HRU with ID "
                        + e.getId() + " could not be found. This may cause errors!");
            }

            e.setObject(hruTohruAttribute.getValue(), toPoly);
            e.setObject(hruToreachAttribute.getValue(), toReach);
            e.setObject(hruTosewer1Attribute.getValue(), toSewer1);
            e.setObject(hruTosewer2Attribute.getValue(), toSewer2);
			e.setObject(hruTosewer3Attribute.getValue(), toSewer3);
			e.setObject(hruTosewer4Attribute.getValue(), toSewer4);

        }

        //associate the reach entities with their downstream entity
        reachIterator = reaches.getEntities().iterator();
        while (reachIterator.hasNext()) {
            e = reachIterator.next();

            toReach = reachMap.get(e.getDouble(reachToreachAttribute.getValue()));

            if (toReach == null) {
                getModel().getRuntime().sendErrorMsg("Topological neighbour for reach with ID "
                        + e.getId() + " could not be found. This may cause errors!");
            }

            e.setObject(reachToreachAttribute.getValue(), toReach);
        }
        
        //associate the sewer 1 entities with their downstream entity
        sewer1Iterator = sewers1.getEntities().iterator();
        while (sewer1Iterator.hasNext()) {
            e = sewer1Iterator.next();

            toSewer1 = sewer1Map.get(e.getDouble(sewer1Tosewer1Attribute.getValue()));

            if (toSewer1 == null) {
                getModel().getRuntime().sendErrorMsg("Topological neighbour for sewer 1 with ID "
                        + e.getId() + " could not be found. This may cause errors!");
            }

            e.setObject(sewer1Tosewer1Attribute.getValue(), toSewer1);

            toReach = reachMap.get(e.getDouble(sewer1ToreachAttribute.getValue()));

            if (toReach == null) {
                getModel().getRuntime().sendErrorMsg("Topological neighbour for reach with ID "
                        + e.getId() + " could not be found. This may cause errors!");
            }

            e.setObject(sewer1ToreachAttribute.getValue(), toReach);            
            
        }
        
        //associate the sewer 2 entities with their downstream entity
        sewer2Iterator = sewers2.getEntities().iterator();
        while (sewer2Iterator.hasNext()) {
            e = sewer2Iterator.next();

            toSewer2 = sewer2Map.get(e.getDouble(sewer2Tosewer2Attribute.getValue()));

            if (toSewer2 == null) {
                getModel().getRuntime().sendErrorMsg("Topological neighbour for sewer 2 with ID "
                        + e.getId() + " could not be found. This may cause errors!");
            }

            e.setObject(sewer2Tosewer2Attribute.getValue(), toSewer2);
		}	
			
			
		//associate the sewer 3 entities with their downstream entity
        sewer3Iterator = sewers3.getEntities().iterator();
        while (sewer3Iterator.hasNext()) {
            e = sewer3Iterator.next();

            toSewer3 = sewer3Map.get(e.getDouble(sewer3Tosewer3Attribute.getValue()));

            if (toSewer3 == null) {
                getModel().getRuntime().sendErrorMsg("Topological neighbour for sewer 3 with ID "
                        + e.getId() + " could not be found. This may cause errors!");
            }

            e.setObject(sewer3Tosewer3Attribute.getValue(), toSewer3);
		}
		
			
		//associate the sewer 4 entities with their downstream entity
        sewer4Iterator = sewers4.getEntities().iterator();
        while (sewer4Iterator.hasNext()) {
            e = sewer4Iterator.next();

            toSewer4 = sewer4Map.get(e.getDouble(sewer4Tosewer4Attribute.getValue()));

            if (toSewer4 == null) {
                getModel().getRuntime().sendErrorMsg("Topological neighbour for sewer 4 with ID "
                        + e.getId() + " could not be found. This may cause errors!");
            }

            e.setObject(sewer4Tosewer4Attribute.getValue(), toSewer4);
        }

        //check for cycles
        if (this.getModel().getRuntime().getDebugLevel() >= JAMS.VVERBOSE) {
            if (cycleCheck() == true) {
                getModel().getRuntime().sendHalt("HRUs --> cycle found ... :( ");
            } else {
                getModel().getRuntime().println("HRUs --> no cycle found");
            }
        }

    }

    protected void createOrderedList(Attribute.EntityCollection col, String asso) throws Attribute.Entity.NoSuchAttributeException {

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

        //put all collection elements (keys) and their depth (values) into a HashMap
        int maxDepth = 0;
        while (mapChanged) {
            mapChanged = false;
            hruIterator = col.getEntities().iterator();
            while (hruIterator.hasNext()) {
                e = hruIterator.next();

                f = (Attribute.Entity) e.getObject(asso);
                if (f==null){
                    this.getModel().getRuntime().println("warning hru with id:" + e.getId() + " has no receiver");
                }
                if ((f != null) && (f.isEmpty())) {
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
