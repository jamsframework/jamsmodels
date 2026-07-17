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
            defaultValue = "aepNetExtractedVolume"
        )
        public Attribute.String aepNetExtractedVolumeName;
        
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
            
            // loop on HRUs: recover data of extracted volumes from HRUs in which extraction occurs
            long release_reach = reaches.getCurrent().getId();
             if (run_reachMap.get(release_reach).existsAttribute(aepHRUEntitiesListName.getValue())) { // check if aepHRUEntities exists -- otherwise there will be error "Attribute aepHRUEntities not found!"      
            
            List<Attribute.Entity> run_h = (List) run_currentReach.getObject(aepHRUEntitiesListName.getValue());
            for (Attribute.Entity run_hru : run_h) {
                double run_ExtractedVolumeHRU = run_hru.getDouble(aepNetExtractedVolumeName.getValue());
                run_totalExtractedVolumeHRU += run_ExtractedVolumeHRU; // sum of all the extracted volumes from HRUs
                }
            }  
             
            // loop on reaches: recover data of extracted volumes from reaches in which extraction occurs
             if (run_reachMap.get(release_reach).existsAttribute(aepReachEntitiesListName.getValue())) { // check if aepReachEntities exists -- otherwise there will be error "Attribute aepReachEntities not found!"      
            
                List<Attribute.Entity> run_r = (List) run_currentReach.getObject(aepReachEntitiesListName.getValue());
            for (Attribute.Entity run_reach : run_r) {
                double run_ExtractedVolumeReach = run_reach.getDouble(aepNetExtractedVolumeName.getValue());
                run_totalExtractedVolumeReach += run_ExtractedVolumeReach; // sum of all the extracted volumes from reaches
                }
            }
             
            // total extracted volume for the release reach
            aepTotalExtractedVolume.setValue(run_totalExtractedVolumeHRU + run_totalExtractedVolumeReach);
            
            aepTotalReleasedVolume.setValue(aepReleaseFactor.getValue() * aepTotalExtractedVolume.getValue()); // released volume = aepReleaseFactor * extracted volume
            double run_aepTotalReleasedVolume = aepTotalReleasedVolume.getValue();
            
            // release into the reach
            double run_totalIn = run_inRD1 + run_inRD2 + run_inRG1 + run_inRG2; // total inflow
                    
            if (run_totalIn == 0) {
                // if no water in totalIn, aepTotalReleasedVolume is added to inRD1 only (ratio run_fractionOverTotalWater = run_aepTotalReleasedVolume/run_totalIn can't be calculated)
                inRD1.setValue(run_inRD1 + run_aepTotalReleasedVolume);

            } else {
            // aepTotalReleasedVolume is proportionally added to inRD1, inRD2, inRG1, inRG2
                double run_releasedFractionOverInflow = run_aepTotalReleasedVolume/run_totalIn;
            
                inRD1.setValue(run_inRD1 + run_inRD1 * run_releasedFractionOverInflow);
                inRD2.setValue(run_inRD2 + run_inRD2 * run_releasedFractionOverInflow);
                inRG1.setValue(run_inRG1 + run_inRG1 * run_releasedFractionOverInflow);
                inRG2.setValue(run_inRG2 + run_inRG2 * run_releasedFractionOverInflow);
            
            }
            
            
        }
    
}
