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
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


/**
 *
 * @author Sven Kralisch & Mériem Labbas & Christian Fischer
 */
@JAMSComponentDescription(title = "AEP device to release water into reach",
        author = "AL Borgna",
        description = "Component used for the simulation of drinking water release (wastewater release).",
        version = "1.0_0",
        date = "2026-01-13")
public class AEPReleaseReach extends JAMSComponent {

    /*
     * Component variables
     */
        @JAMSVarDescription(
                access = JAMSVarDescription.AccessType.READ,
                description = "Reach list"
        )
        public Attribute.EntityCollection reaches;
                        
        @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "RD1 inflow to reach. - state variable",
            unit = "L"
        )
        public Attribute.Double inRD1;
    
        @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "RD2 inflow to reach. - state variable",
            unit = "L"
        )
        public Attribute.Double inRD2;
    
        @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "RG1 inflow to reach. - state variable",
            unit = "L"
        )
        public Attribute.Double inRG1;
    
        @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "RG2 inflow to reach. - state variable",
            unit = "L"
        )
        public Attribute.Double inRG2;
        
                @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "actRD1 component in reach. - state variable"
        )
        public Attribute.Double actRD1;
        
        @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "actRD2 component in reach. - state variable"
        )
        public Attribute.Double actRD2;
        
        @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "actRG1 component in reach. - state variable"
        )
        public Attribute.Double actRG1;
            
        @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "actRG2 component in reach. - state variable"
        )
        public Attribute.Double actRG2;
                
        @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Name of list of HRUs in which extraction occurs. Will be written for each release reach."
                    + "List will be read by this component. - parameter / pointer",
            defaultValue = "aepHRUEntities"
        )
        public Attribute.String aepHRUEntitiesListName;
        
        @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Name of list of reaches in which extraction occurs. Will be written for each release reach."
                    + "List will be read by this component. - parameter / pointer",
        defaultValue = "aepReachEntities"
        )
        public Attribute.String aepReachEntitiesListName;
        
        @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Name of attribute that stores water extracted from an entity (HRU or reach) for drinking water."
                    + "Extracted volume will be read by this component. - parameter / pointer",
            defaultValue = "aepExtractedVolume"
        )
        public Attribute.String aepExtractedVolumeName;
        
        @JAMSVarDescription(
        access = JAMSVarDescription.AccessType.WRITE,
        description = "Total volume extracted from HRUs and reaches. - output",
        unit = "L"
        )
        public Attribute.Double aepTotalExtractedVolume;
        
        @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Fraction of extracted volume that is actually released. - parameter"
        )
        public Attribute.Double aepReleaseFactor;
        
        @JAMSVarDescription(
        access = JAMSVarDescription.AccessType.WRITE,
        description = "Total volume released into release reach (wastewater release). - output",
        unit = "L"
        )
        public Attribute.Double aepTotalReleasedVolume;
           
        
        
        private Map<Long, Attribute.Entity> run_reachMap = new HashMap();

        @Override
        public void init() {
            
            //put all reaches to a map for easier access -> used for the release reaches
            for (Attribute.Entity run_reach : reaches.getEntities()) {
                run_reachMap.put(run_reach.getId(), run_reach);
            }
    }
        
        
        @Override
        public void run() {

            // check
//            getModel().getRuntime().println("Reach de rejet (AEPReleaseReach):"+reaches.getCurrent().getId());
            
            
            Attribute.Entity run_currentReach = reaches.getCurrent(); 
            double run_totalExtractedVolumeHRU = 0;
            double run_totalExtractedVolumeReach = 0;
            double run_inRD1 = inRD1.getValue();
            double run_inRD2 = inRD2.getValue();
            double run_inRG1 = inRG1.getValue();
            double run_inRG2 = inRG2.getValue();
            double run_actRD1 = actRD1.getValue();
            double run_actRD2 = actRD2.getValue();
            double run_actRG1 = actRG1.getValue();
            double run_actRG2 = actRG2.getValue();
            

            // loop on HRUs: recover data of extracted volumes from HRUs in which extraction occurs
            long release_reach = reaches.getCurrent().getId();
             if (run_reachMap.get(release_reach).existsAttribute(aepHRUEntitiesListName.getValue())) { // check if aepHRUEntities exists -- otherwise there will be error "Attribute aepHRUEntities not found!"      
            
            List<Attribute.Entity> run_h = (List) run_currentReach.getObject(aepHRUEntitiesListName.getValue());
            for (Attribute.Entity run_hru : run_h) {
                double run_ExtractedVolumeHRU = -run_hru.getDouble(aepExtractedVolumeName.getValue()); // NPO DE CHANGER LES SIGNES PLUS TARD
                run_totalExtractedVolumeHRU += run_ExtractedVolumeHRU; // sum of all the extracted volumes from HRUs
                }
            }
            
//            getModel().getRuntime().println("run_currentReach.getObject(aepHRUEntitiesListName.getValue() (AEPReleaseReach):"+run_currentReach.getObject(aepHRUEntitiesListName.getValue())); // check
            
            
            // check : permet de print tous les ID des entités dans aepHRUEntities
//                List<Attribute.Entity> liste = (List) run_currentReach.getObject(aepHRUEntitiesListName.getValue());
//
//                getModel().getRuntime().println("reach rejet (478600): $IDs = [");
//                for (Attribute.Entity entite : liste) {
//                    getModel().getRuntime().println(entite.getId() + ",");
//                }
//                getModel().getRuntime().println("]");                    
//                getModel().getRuntime().println("- AEPReleaseReach - reach rejet (478600): $IDs dans aepEntities = " + ((List<Attribute.Entity>) run_currentReach.getObject(aepHRUEntitiesListName.getValue())).stream().map(e -> String.valueOf(e.getId())).collect(Collectors.joining(", ", "[", "]")));
//                getModel().getRuntime().println("run_totalExtractedVolumeHRU:"+run_totalExtractedVolumeHRU); // check
                
             
            // loop on reaches: recover data of extracted volumes from reaches in which extraction occurs
             if (run_reachMap.get(release_reach).existsAttribute(aepReachEntitiesListName.getValue())) { // check if aepReachEntities exists -- otherwise there will be error "Attribute aepReachEntities not found!"      
            
                List<Attribute.Entity> run_r = (List) run_currentReach.getObject(aepReachEntitiesListName.getValue());
            for (Attribute.Entity run_reach : run_r) {
                double run_ExtractedVolumeReach = -run_reach.getDouble(aepExtractedVolumeName.getValue()); // NPO DE CHANGER LES SIGNES PLUS TARD
                run_totalExtractedVolumeReach += run_ExtractedVolumeReach; // sum of all the extracted volumes from reaches
                }
            }
            
//            getModel().getRuntime().println("run_totalExtractedVolumeReach:"+run_totalExtractedVolumeReach); // check

            aepTotalExtractedVolume.setValue(run_totalExtractedVolumeHRU + run_totalExtractedVolumeReach); // total extracted volume for the release reach
            

            aepTotalReleasedVolume.setValue(aepReleaseFactor.getValue() * aepTotalExtractedVolume.getValue()); // volume rejete = aepReleaseFactor * volume preleve
            double run_aepTotalReleasedVolume = aepTotalReleasedVolume.getValue();
            
            // release into the reach
            double run_totalIn = run_inRD1 + run_inRD2 + run_inRG1 + run_inRG2; // total inflow
            double run_totalAct = run_actRD1 + run_actRD2 + run_actRG1 + run_actRG2; // total actual stock
            double run_totalWater = run_totalIn + run_totalAct;
            
            if (run_totalWater == 0) {
//                getModel().getRuntime().println("Infinite fraction due to totalIn+totalAct=0. Reach "+reaches.getCurrent().getId());
                // if no water, release 33% of TotalReleasedVolume in each component
                double run_releasedVolumeForEach = run_aepTotalReleasedVolume/3;
                inRD1.setValue(run_inRD1 + run_releasedVolumeForEach);
                inRD2.setValue(run_inRD2 + run_releasedVolumeForEach);
                inRG1.setValue(run_inRG1 + run_releasedVolumeForEach);
//                inRG2.setValue(run_inRG2 + releasedVolumeForEach); // no RG2 for Rhone model
//                getModel().getRuntime().println("run_aepTotalReleasedVolume:"+run_aepTotalReleasedVolume); // check
//                getModel().getRuntime().println("inRD1 qd total=0 : "+inRD1.getValue()); // check
//                getModel().getRuntime().println("inRD2 qd total=0 : "+inRD2.getValue()); // check
//                getModel().getRuntime().println("inRG1 qd total=0 : "+inRG1.getValue()); // check
            }
            else {
            // aepTotalReleasedVolume is proportionally added to inRD1, inRD2, inRG1, inRG2
                double run_fractionOverTotalWater = run_aepTotalReleasedVolume/run_totalWater;
            
                inRD1.setValue(run_inRD1 + run_inRD1 * run_fractionOverTotalWater);
                inRD2.setValue(run_inRD2 + run_inRD2 * run_fractionOverTotalWater);
                inRG1.setValue(run_inRG1 + run_inRG1 * run_fractionOverTotalWater);
                inRG2.setValue(run_inRG2 + run_inRG2 * run_fractionOverTotalWater);
//                getModel().getRuntime().println("run_aepTotalReleasedVolume:"+run_aepTotalReleasedVolume); // check
//                getModel().getRuntime().println("inRD1 qd rejet prop : "+inRD1.getValue()); // check
//                getModel().getRuntime().println("run_inRD1 qd rejet prop : "+run_inRD1); // check
//                getModel().getRuntime().println("run_inRD1 * run_fractionOverTotalWater qd rejet prop : "+run_inRD1 * run_fractionOverTotalWater); // check
//                getModel().getRuntime().println("inRD2 qd rejet prop : "+inRD2.getValue()); // check
//                getModel().getRuntime().println("run_inRD2 * run_fractionOverTotalWater qd rejet prop : "+run_inRD2 * run_fractionOverTotalWater); // check
//                getModel().getRuntime().println("inRG1 qd rejet prop : "+inRG1.getValue()); // check
//                getModel().getRuntime().println("run_inRG1 * run_fractionOverTotalWater qd rejet prop : "+run_inRG1 * run_fractionOverTotalWater); // check
                
            
            }
            
            
        }
    
}
