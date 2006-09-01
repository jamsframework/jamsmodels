/*
 * StandardManagementParaReader.java
 *
 * Created on 6. März 2006, 13:35
 *
 * * This file is part of JAMS
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

package org.jams.j2k.s_n.io;

import org.unijena.jams.model.*;
import java.util.*;
import java.io.*;
import org.jams.j2k.s_n.crop.*;
import org.unijena.jams.JAMS;
import org.unijena.jams.data.*;

/**
 *
 * @author c5ulbe
 */
public class StandardManagementParaReader extends JAMSComponent {
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.INIT,
            description = "Data file directory name"
            )
            public JAMSString dirName;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.INIT,
            description = "Crop parameter file name"
            )
            public JAMSString cropFileName;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.INIT,
            description = "Fertilization parameter file name"
            )
            public JAMSString fertFileName;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.INIT,
            description = "Tillage parameter file name"
            )
            public JAMSString tillFileName;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.INIT,
            description = "Management parameter file name"
            )
            public JAMSString managementFileName;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.INIT,
            description = "Rotation parameter file name"
            )
            public JAMSString rotationFileName;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.INIT,
            description = "HRU rotation mapping file name"
            )
            public JAMSString hruRotationFileName;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "Collection of hru objects"
            )
            public JAMSEntityCollection hrus;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.INIT,
            description = "Attribute name of HRU ID"
            )
            public JAMSString aNameHRUID;
    
    // HashMap für Fertilizer anlegen
    private HashMap<Integer, J2KSNFertilizer> readFertPara(String fileName) {
        
        BufferedReader reader;
        StringTokenizer tokenizer;
        String[] vals = new String[11];
        HashMap<Integer, J2KSNFertilizer> map = new HashMap<Integer, J2KSNFertilizer>();
        
        try {
            
            reader = new BufferedReader(new FileReader(fileName));
            
            String s = "#";
            
            // get rid of comments
            while (s.startsWith("#")) {
                s = reader.readLine();
            }
            s = reader.readLine();
            
            while ((s != null) && !s.startsWith("#"))  {
                
                tokenizer = new StringTokenizer(s, "\t");
                
                for (int i = 0; i < 11; i++) {
                    vals[i] = tokenizer.nextToken();
                }
                
                map.put(Integer.parseInt(vals[0]), new J2KSNFertilizer(vals));
                
                s = reader.readLine();
            }
            
        } catch (IOException ioe) {
            getModel().getRuntime().handle(ioe);
        }
        
        return map;
    }
    
    private HashMap<Integer, J2KSNTillage> readTillPara(String fileName) {
        
        BufferedReader reader;
        StringTokenizer tokenizer;
        String[] vals = new String[5];
        // neu seit Netbeans 5.0 ist die nähere Definition der HashMaps gewünscht
        // HashMap für Tillage anlegen
        HashMap<Integer, J2KSNTillage> map = new HashMap<Integer, J2KSNTillage>();
        
        try {
            
            reader = new BufferedReader(new FileReader(fileName));
            
            String s = "#";
            
            // get rid of comments
            while (s.startsWith("#")) {
                s = reader.readLine();
            }
            s = reader.readLine();
            
            while ((s != null) && !s.startsWith("#"))  {
                
                tokenizer = new StringTokenizer(s, "\t");
                
                for (int i = 0; i < 5; i++) {
                    vals[i] = tokenizer.nextToken();
                }
                // Schreibe vals in die J2KSNTillage Hashmap
                map.put(Integer.parseInt(vals[0]), new J2KSNTillage(vals));
                
                
                
                s = reader.readLine();
            }
            
        } catch (IOException ioe) {
            getModel().getRuntime().handle(ioe);
        }
        
        return map;
    }
    
    // HashMap für Management anlegen; mit Verweis auf Tillage und Fertilizer
    private HashMap<Integer, ArrayList<J2KSNLMArable>> readManagementPara(String fileName, HashMap<Integer, J2KSNTillage> tills, HashMap<Integer, J2KSNFertilizer> ferts) {
        
        BufferedReader reader;
        StringTokenizer tokenizer;
        String[] vals = new String[8];
        int oldCID = -1, cid = -1;
        ArrayList<J2KSNLMArable> managements = null;
        // neu seit Java 5 ist die nähere Definition der Collections gewünscht
        // HashMap anlegen
        HashMap<Integer, ArrayList<J2KSNLMArable>> map = new HashMap<Integer, ArrayList<J2KSNLMArable>>();
        
        try {
            
            reader = new BufferedReader(new FileReader(fileName));
            
            String s = "#";
            
            // get rid of comments
            while (s.startsWith("#")) {
                s = reader.readLine();
            }
            s = reader.readLine();
            
            while ((s != null) && !s.startsWith("#"))  {
                
                tokenizer = new StringTokenizer(s, "\t");
                
                for (int i = 0; i < 8; i++) {
                    vals[i] = tokenizer.nextToken();
                }
                
                cid = Integer.parseInt(vals[0]);
                if (cid != oldCID) {
                    map.put(oldCID, managements);
                    managements = new ArrayList<J2KSNLMArable>();
                    oldCID = cid;
                }
                
                managements.add(new J2KSNLMArable(vals, tills, ferts));
                
                s = reader.readLine();
            }
            map.put(cid, managements);
            
        } catch (IOException ioe) {
            getModel().getRuntime().handle(ioe);
        }
        
        return map;
    }
    
    // HashMap für Crop anlegen; mit Verweis auf Management, respective tillage und fertilizer
    private HashMap<Integer, J2KSNCrop> readCrops(String fileName, HashMap<Integer, ArrayList<J2KSNLMArable>> management) {
        
        BufferedReader reader;
        StringTokenizer tokenizer;
        String[] vals = new String[44];
        Integer id;
        
        // HashMap anlegen
        HashMap<Integer, J2KSNCrop> map = new HashMap<Integer, J2KSNCrop>();
        
        try {
            
            reader = new BufferedReader(new FileReader(fileName));
            
            String s = "#";
            
            // get rid of comments
            while (s.startsWith("#")) {
                s = reader.readLine();
            }
            s = reader.readLine();
            
            while ((s != null) && !s.startsWith("#"))  {
                
                tokenizer = new StringTokenizer(s, "\t");
                
                for (int i = 0; i < 44; i++) {
                    vals[i] = tokenizer.nextToken();
                }
                
                id = Integer.parseInt(vals[0]);
                J2KSNCrop crop = new J2KSNCrop(vals, management.get(id));
                map.put(crop.cid, crop);
                
                //map.put(id, new J2KSNCrop(vals, management.get(id)));
                
                s = reader.readLine();
            }
            
            
        } catch (IOException ioe) {
            getModel().getRuntime().handle(ioe);
        }
        
        return map;
    }
    // haspMap für crop rotations, respective vorherige Hashmaps
    private HashMap<Integer, ArrayList<J2KSNCrop>> readRotations(String fileName, HashMap<Integer, J2KSNCrop> crops) {
        
        BufferedReader reader;
        StringTokenizer tokenizer;
        Integer rid, cid;
        ArrayList <J2KSNCrop> cropList;
        // neu seit Java 5 ist die nähere Definition der Collections gewünscht
        // HashMap anlegen
        HashMap<Integer, ArrayList<J2KSNCrop>> map = new HashMap<Integer, ArrayList<J2KSNCrop>>();
        
        try {
            
            reader = new BufferedReader(new FileReader(fileName));
            
            String s = "#";
            
            // get rid of comments
            while (s.startsWith("#")) {
                s = reader.readLine();
            }
            s = reader.readLine();
            
            while ((s != null) && !s.startsWith("#"))  {
                
                tokenizer = new StringTokenizer(s, "\t");
                
                cropList = new ArrayList <J2KSNCrop>();
                rid = Integer.parseInt(tokenizer.nextToken());
                
                while (tokenizer.hasMoreElements()) {
                    cid = Integer.parseInt(tokenizer.nextToken());
                    /*
                    J2KSNCrop crop = crops.get(cid);
                    cropList.add(crop);
                     */
                    cropList.add(crops.get(cid));
                }
                
                map.put(rid, cropList);
                
                //map.put(id, new J2KSNCrop(vals, management.get(id)));
                
                s = reader.readLine();
            }
            
            
        } catch (IOException ioe) {
            getModel().getRuntime().handle(ioe);
        }
        
        return map;
    }
    
    private void linkCrops(String fileName, HashMap<Integer, ArrayList<J2KSNCrop>> rotations) {
        
        BufferedReader reader;
        StringTokenizer tokenizer;
        Double hid;
        Integer rid;
        ArrayList<J2KSNCrop> rotation;
        JAMSEntity hru;
        HashMap<Double, JAMSEntity> hruMap = new HashMap<Double, JAMSEntity>();
        
        //put all entities into a HashMap with their ID as key
        Iterator<JAMSEntity> hruIterator = hrus.getEntities().iterator();
        while (hruIterator.hasNext()) {
            hru = hruIterator.next();
            try {
                hruMap.put(hru.getDouble(aNameHRUID.getValue()),  hru);
            } catch (JAMSEntity.NoSuchAttributeException nsae) {
//                getModel().sendHalt("Attribute " + aNameHRUID + " not found!");
            }
        }
        
        try {
            
            reader = new BufferedReader(new FileReader(fileName));
            
            String s = "#";
            
            // get rid of comments
            while (s.startsWith("#")) {
                s = reader.readLine();
            }
            s = reader.readLine();
            
            while ((s != null) && !s.startsWith("#"))  {
                
                tokenizer = new StringTokenizer(s, "\t");
                
                hid = Double.parseDouble(tokenizer.nextToken());
                rid = Integer.parseInt(tokenizer.nextToken());
                
                rotation = rotations.get(rid);
                hru = hruMap.get(hid);
                
                hru.setObject("landuseRotation",  rotation);
                hru.setInt("rotPos", 0);
                hru.setInt("managementPos", 0);
                
                
                s = reader.readLine();
            }
            
            
        } catch (IOException ioe) {
            getModel().getRuntime().handle(ioe);
        }
        
    }
    
    
    public void run() {
        /*
        HashMap<Integer, J2KSNFertilizer> ferts = readFertPara("C:\\jamsmodels\\JAMS-Application\\JAMS-Arnstadt\\parameter\\fert.par");
        HashMap<Integer, J2KSNTillage> tills = readTillPara("C:\\jamsmodels\\JAMS-Application\\JAMS-Arnstadt\\parameter\\till.par");
        HashMap<Integer, ArrayList<J2KSNLMArable>> managements = readManagementPara("C:\\jamsmodels\\JAMS-Application\\JAMS-Arnstadt\\parameter\\lmArable.par", tills, ferts);
        HashMap<Integer, J2KSNCrop> crops = readCrops("C:\\jamsmodels\\JAMS-Application\\JAMS-Arnstadt\\parameter\\crop.par", managements);
        HashMap<Integer, ArrayList<J2KSNCrop>> rotations = readRotations("C:\\jamsmodels\\JAMS-Application\\JAMS-Arnstadt\\parameter\\croprotation.par", crops);
        linkCrops("C:\\jamsmodels\\JAMS-Application\\JAMS-Arnstadt\\parameter\\hrus_rot.par", rotations);
         */
        
        HashMap<Integer, J2KSNFertilizer> ferts = readFertPara(dirName.getValue() + "/" + fertFileName.getValue());
        HashMap<Integer, J2KSNTillage> tills = readTillPara(dirName.getValue() + "/" + tillFileName.getValue());
        HashMap<Integer, ArrayList<J2KSNLMArable>> managements = readManagementPara(dirName.getValue() + "/" + managementFileName.getValue(), tills, ferts);
        HashMap<Integer, J2KSNCrop> crops = readCrops(dirName.getValue() + "/" + cropFileName.getValue(), managements);
        HashMap<Integer, ArrayList<J2KSNCrop>> rotations = readRotations(dirName.getValue() + "/" + rotationFileName.getValue(), crops);
        linkCrops(dirName.getValue() + "/" + hruRotationFileName.getValue(), rotations);
    }
    
    public static void main(String[] x) {
        StandardManagementParaReader r = new StandardManagementParaReader();
        r.run();
        
    }
}