
/*
 * WasserfallEntityReader.java
 *
 * Created on 5. Juni 2008, 14:22
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

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import org.unijena.j2k.*;
import jams.data.*;
import jams.model.*;
import java.util.*;
import jams.JAMS;
import java.lang.Math.*;
import jams.tools.FileTools;
import javax.swing.JOptionPane;

/**
 *
 * @author S. Kralisch, f?r mehrdimensionale Topologie modifiziert von D.Varga und B.Pfennig
 * 09.10.2008
 */
public class MultiEntityReaderTS_2 extends JAMSComponent {

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    description = "HRU parameter file name")
    public Attribute.String hruFileName;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    description = "Reach parameter file name")
    public Attribute.String reachFileName;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    description = "Parameter file name for topological linkage with receiver entities")
    public Attribute.String to_hru_FileName;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    description = "Parameter file name for weighting of receiver entity")
    public Attribute.String bfl_FileName;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.WRITE,
    description = "Collection of hru objects")
    public Attribute.EntityCollection hrus;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.WRITE,
    description = "Collection of reach objects")
    public Attribute.EntityCollection reaches;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.WRITE,
    description = "Collection of hru objects with their topology")
    public Attribute.EntityCollection topology;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    description = "Name of the attribute containing the HRU identifiers",
    defaultValue = "ID")
    public Attribute.String hruIDAttribute;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    description = "Name of the attribute containing the reach identifiers",
    defaultValue = "ID")
    public Attribute.String reachIDAttribute;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    description = "Name of the attribute describing the HRU to HRU relation in the input file",
    defaultValue = "to_poly")
    public Attribute.String hru2hruAttribute;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    description = "Name of the attribute describing the HRU to reach relation in the input file",
    defaultValue = "to_reach")
    public Attribute.String hru2reachAttribute;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    description = "Name of the attribute describing the reach to reach relation in the input file",
    defaultValue = "to_reach")
    public Attribute.String reach2reachAttribute;

    @Override
    public void init() {
 

        //read hru parameter
        hrus.setEntities(J2KFunctions.readParas(FileTools.createAbsoluteFileName(getModel().getWorkspaceDirectory().getPath(), hruFileName.getValue()), getModel()));

        //assign IDs to all hru entities
        for (Attribute.Entity e : hrus.getEntityArray()) {
            try {
                e.setId((long) e.getDouble(hruIDAttribute.getValue()));
            } catch (Attribute.Entity.NoSuchAttributeException nsae) {
                getModel().getRuntime().sendErrorMsg("Couldn't find attribute \"ID\" while reading J2K HRUu parameter file (" + hruFileName.getValue() + ")!");
            }
        }

        //read reach parameter
        reaches.setEntities(J2KFunctions.readParas(FileTools.createAbsoluteFileName(getModel().getWorkspaceDirectory().getPath(), reachFileName.getValue()), getModel()));

        //assign IDs to all reach entities
        for (Attribute.Entity e : reaches.getEntityArray()) {
            try {
                e.setId((long) e.getDouble(reachIDAttribute.getValue()));
            } catch (Attribute.Entity.NoSuchAttributeException nsae) {
                getModel().getRuntime().sendErrorMsg("Couldn't find attribute \"ID\" while reading J2K Reach parameter file (" + reachFileName.getValue() + ")!");
            }
        }

        //create object associations from id attributes for hrus and reaches
        createTopology();

        //create total order on hrus and reaches that allows processing them subsequently
        getModel().getRuntime().println("Create ordered hru-list", JAMS.VERBOSE);
        createOrderedList(hrus, hru2hruAttribute.getValue());
        getModel().getRuntime().println("HRU entities read successfully", JAMS.STANDARD);
        getModel().getRuntime().println("Create ordered reach-list", JAMS.VERBOSE);
        createOrderedList(reaches, reach2reachAttribute.getValue());
        getModel().getRuntime().println("Reach entities read successfully", JAMS.STANDARD);

    }

    private void createTopology() {

        BufferedReader reader1;
        BufferedReader reader2;

        HashMap<Double, Attribute.Entity> hruMap = new HashMap<Double, Attribute.Entity>();
        HashMap<Double, Attribute.Entity> reachMap = new HashMap<Double, Attribute.Entity>();
        Iterator<Attribute.Entity> hruIterator;
        Iterator<Attribute.Entity> reachIterator;

        ArrayList<Attribute.Entity> toHRUsArrayList, toReachesArrayList;
        ArrayList<Double> toHRUsWeightsArrayList, toReachesWeightsArrayList, toHRUsBFlArrayList;

        Attribute.Entity e, toPoly, zielHRU, zielReach, toReach;


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


        Attribute.Entity nullEntity = getModel().getRuntime().getDataFactory().createEntity();
        nullEntity.setValue((HashMap<String, Object>) null);
        hruMap.put(new Double(0), nullEntity);
        reachMap.put(new Double(0), nullEntity);

         //associate the hru entities with their downstream entity
        hruIterator = hrus.getEntities().iterator();
        while (hruIterator.hasNext()) {
            e = hruIterator.next();
            toPoly = hruMap.get(e.getDouble(hru2hruAttribute.getValue()));
            toReach = reachMap.get(e.getDouble(hru2reachAttribute.getValue()));

            if ((toPoly == null) || (toReach == null)) {
                getModel().getRuntime().sendErrorMsg("Topological neighbour for HRU with ID "
                        + e.getId() + " could not be found. This may cause errors!");
            }

            e.setObject(hru2hruAttribute.getValue(), toPoly);
            e.setObject(hru2reachAttribute.getValue(), toReach);

        }


        //create empty entities, i.e. those that are linked to in case there is no linkage ;-)
        nullEntity.setValue((HashMap<String, Object>) null);
        reachMap.put(new Double(0), nullEntity);

        try {

            reader1 = new BufferedReader(new FileReader(getModel().getWorkspaceDirectory().getPath() + "/" + to_hru_FileName.getValue()));
            reader2 = new BufferedReader(new FileReader(getModel().getWorkspaceDirectory().getPath() + "/" + bfl_FileName.getValue()));

            String toHRUsLine = "#";
            while (toHRUsLine.startsWith("#")) {
                toHRUsLine = reader1.readLine();
            }

            String toHRUsWeightsLine = "#";
            while (toHRUsWeightsLine.startsWith("#")) {
                toHRUsWeightsLine = reader2.readLine();
            }

            while ((toHRUsLine != null) && !toHRUsLine.startsWith("#")) {
                //String zeichenkette = "\t";
                String zeichenkette = ",-8888.000,";

                String[] toHRUsSplitArray = toHRUsLine.split(zeichenkette);
                String[] toHRUsWeightsSplitArray = toHRUsWeightsLine.split(zeichenkette);

                double HRUsID = Double.parseDouble(toHRUsSplitArray[0]);
                double HRUsBFl = Double.parseDouble(toHRUsSplitArray[1]);
                String toHRUsString = toHRUsSplitArray[3];

                e = hruMap.get(HRUsID);
                e.setDouble("BFLDouble", HRUsBFl);

                double HRUsID2 = Double.parseDouble(toHRUsWeightsSplitArray[0]);
                String toHRUsWeightsString = toHRUsWeightsSplitArray[2];

                //Checks if e1 and e2 are identical
                //Die Tabellen topologie_to_hru und topologie_bfl muessen identisch sortiert sein
                if (HRUsID != HRUsID2) {
                    getModel().getRuntime().sendHalt("One of tables topologie_to_hru or topologie_bfl is missorted");
                }

                toHRUsArrayList = new ArrayList<Attribute.Entity>();
                toReachesArrayList = new ArrayList<Attribute.Entity>();

                toHRUsWeightsArrayList = new ArrayList<Double>();
                toReachesWeightsArrayList = new ArrayList<Double>();

                toHRUsBFlArrayList = new ArrayList<Double>();

                //Tokenizer zum Unterteilen bei ","
                StringTokenizer toHRUsToken = new StringTokenizer(toHRUsString, ","); //Tokenizer fuer Empfaenger-HRUs
                StringTokenizer toHRUsWeightsToken = new StringTokenizer(toHRUsWeightsString, ","); //Tokenizer fuer die Wichtungen der Empfaenger-HRUs

                boolean tschuessnull = false;
                double sumWeights = 1;

                while (toHRUsToken.hasMoreTokens() && tschuessnull == false) {
                    double toHRUsID = Double.parseDouble(toHRUsToken.nextToken());
                    double toHRUsWeight = Double.parseDouble(toHRUsWeightsToken.nextToken());

                    sumWeights = 0.0001 * Math.round((sumWeights - toHRUsWeight) * 10000);

                    if ((toHRUsID == 0 && toHRUsWeight != 0) || (toHRUsID != 0 && toHRUsWeight == 0)) {
                        System.out.println("Fehler bei HRU " + HRUsID + ". Anzahl der Empfaenger-HRUs und der Wichtungen stimmen nicht ueberein.");
                        JOptionPane.showMessageDialog(null, "Fehler bei HRU " + HRUsID + ". Anzahl der Empfaenger-HRUs und der Wichtungen stimmen nicht ueberein.");
                        System.exit(-1);
                    }

                    //for receiver HRUs
                    if (toHRUsID > 0) {
                        zielHRU = hruMap.get(toHRUsID);
                        toHRUsArrayList.add(zielHRU);
                        toHRUsWeightsArrayList.add(toHRUsWeight);
                    }

                    //for receiver Reaches
                    if (toHRUsID < 0) {
                        double toReachesID = toHRUsID * (-1);
                        zielReach = reachMap.get(toReachesID);
                        toReachesArrayList.add(zielReach);
                        toReachesWeightsArrayList.add(toHRUsWeight);
                    }

                    if (toHRUsID == 0) {
                        tschuessnull = true;
                    }
                }

                sumWeights = Math.abs(sumWeights);
                if (sumWeights >= 0.001) {
                    System.out.println("Fehler bei HRU " + HRUsID + ". Summe der einzelnen Gewichte ungleich 1");
                    JOptionPane.showMessageDialog(null, "Fehler bei HRU " + HRUsID + ". Summe der einzelnen Gewichte ungleich 1");
                    System.exit(-1);
                }

                //converting the ArrayLists into Arrays

                //Attribute.Entity nullEntity = getModel().getRuntime().getDataFactory().createEntity();

                Attribute.Entity[] toHRUsArray = toHRUsArrayList.toArray(new Attribute.Entity[toHRUsArrayList.size()]);
                Attribute.Entity[] toReachesArray = toReachesArrayList.toArray(new Attribute.Entity[toReachesArrayList.size()]);

                Double[] toHRUsWeightsArray = toHRUsWeightsArrayList.toArray(new Double[toHRUsWeightsArrayList.size()]);
                Double[] toReachesWeightsArray = toReachesWeightsArrayList.toArray(new Double[toReachesWeightsArrayList.size()]);

                Double[] toHRUsBflArray = toHRUsBFlArrayList.toArray(new Double[toHRUsWeightsArrayList.size()]);

                //creating new Objects for each entity
                e.setObject("to_poly", toHRUsArray);
                e.setObject("to_reach", toReachesArray);

                e.setObject("to_poly_weights", toHRUsWeightsArray);
                e.setObject("to_reach_weights", toReachesWeightsArray);

                e.setObject("bfl", toHRUsBflArray);

                sumWeights = 1;

                //Auslesen der jeweils naechsten Zeile
                toHRUsLine = reader1.readLine();
                toHRUsWeightsLine = reader2.readLine();
            }

        } catch (IOException ioe) {
            getModel().getRuntime().handle(ioe);
        }


        //associate the reach entities with their downstream entity
        reachIterator = reaches.getEntities().iterator();
        while (reachIterator.hasNext()) {
            e = reachIterator.next();

            toReach = reachMap.get(e.getDouble(reach2reachAttribute.getValue()));

            if (toReach == null) {
                getModel().getRuntime().sendErrorMsg("Topological neighbour for reach with ID "
                        + e.getId() + " could not be found. This may cause errors!");
            }

            e.setObject(reach2reachAttribute.getValue(), toReach);
        }
    }

    protected void createOrderedList(Attribute.EntityCollection col, String asso) {

        Iterator<Attribute.Entity> entityIterator;
        Attribute.Entity e = null ;
        ArrayList<Attribute.Entity> newList = new ArrayList<Attribute.Entity>();
        HashMap<Attribute.Entity, Integer> depthMap = new HashMap<Attribute.Entity, Integer>();
        Integer eDepth, fDepth;
        boolean mapChanged;

        //Identifikation und Aufloesung von Zirkeln


        // Aufbau der Topologie
        mapChanged = true;
        entityIterator = col.getEntities().iterator();
        while (entityIterator.hasNext()) {
            depthMap.put(entityIterator.next(), new Integer(0));
        }

        //put all collection elements (keys) and their maximum depth (values) into a HashMap
        while (mapChanged) {
            mapChanged = false;
            entityIterator = col.getEntities().iterator();
            while (entityIterator.hasNext()) {
                e = entityIterator.next();
                eDepth = depthMap.get(e);

                if ((asso.toString()).equals("to_poly")) {

                    Attribute.Entity[] e_ziel_to_hru;
                    e_ziel_to_hru = (Attribute.Entity[]) e.getObject(asso);

                    if (e_ziel_to_hru.length > 0) {

                        for (int i = 0; i < e_ziel_to_hru.length; i++) {

                            if (e_ziel_to_hru[i] != null) {
                                fDepth = depthMap.get(e_ziel_to_hru[i]);

                                if (fDepth.intValue() <= eDepth.intValue()) {
                                    depthMap.put(e_ziel_to_hru[i], new Integer(eDepth.intValue() + 1));
                                    mapChanged = true;
                                }
                            }
                        }
                    }
                }
                if ((asso.toString()).equals("to_reach")) {

                    Attribute.Entity eff;
                    eff = (Attribute.Entity) e.getObject(asso);
                    if (eff.getValue() == null) {
                        eff = null;
                    }

                    if (eff != null) {
                        fDepth = depthMap.get(eff);

                        if (fDepth.intValue() <= eDepth.intValue()) {
                            depthMap.put(eff, new Integer(eDepth.intValue() + 1));
                            mapChanged = true;
                        }
                    }
                }
            }
        }

        //find out which is the max depth of all entities
        int maxDepth = 0;
        entityIterator = col.getEntities().iterator();
        while (entityIterator.hasNext()) {
            e = entityIterator.next();
            maxDepth = Math.max(maxDepth, depthMap.get(e).intValue());
        }

        //create ArrayList of ArrayList objects, each element keeping the entities of one level
        ArrayList<ArrayList<Attribute.Entity>> alList = new ArrayList<ArrayList<Attribute.Entity>>();

        for (int i = 0; i <= maxDepth; i++) {
            alList.add(new ArrayList<Attribute.Entity>());
        }

        //fill the ArrayList objects within the ArrayList with entity objects
        entityIterator = col.getEntities().iterator();

        while (entityIterator.hasNext()) {
            e = entityIterator.next();
            int depth = depthMap.get(e).intValue();
            alList.get(depth).add(e);
        }

        //put the entities
        for (int i = 0; i <= maxDepth; i++) {
            entityIterator = alList.get(i).iterator();
            while (entityIterator.hasNext()) {
                e = entityIterator.next();
                newList.add(e);
            }
        }
        col.setEntities(newList);
    }
}
