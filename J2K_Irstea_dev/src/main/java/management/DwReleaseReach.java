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
@JAMSComponentDescription(title = "Drinking water device to release water into reach",
        author = "AL Borgna",
        description = "Component used for the simulation of drinking water release (wastewater release).",
        version = "1.0_0",
        date = "2026-01-13")
public class DwReleaseReach extends JAMSComponent {

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
            defaultValue = "dwHRUEntities"
        )
        public Attribute.String dwHRUEntitiesListName;
        
        @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Name of list of reaches in which extraction occurs. Will be written for each release reach."
                    + "List will be read by this component. - parameter / pointer",
        defaultValue = "dwReachEntities"
        )
        public Attribute.String dwReachEntitiesListName;
        
        @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Name of attribute that stores water extracted from an entity (HRU or reach) for drinking water."
                    + "Extracted volume will be read by this component. - parameter / pointer",
            defaultValue = "dwNetExtractedVolume"
        )
        public Attribute.String dwNetExtractedVolumeName;
        
        @JAMSVarDescription(
        access = JAMSVarDescription.AccessType.WRITE,
        description = "Total volume extracted from HRUs and reaches. - output",
        unit = "L"
        )
        public Attribute.Double dwTotalExtractedVolume;
        
        @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Fraction of extracted volume that is actually released. - parameter"
        )
        public Attribute.Double dwReleaseFactor;
        
        @JAMSVarDescription(
        access = JAMSVarDescription.AccessType.WRITE,
        description = "Total volume released into release reach (wastewater release). - output",
        unit = "L"
        )
        public Attribute.Double dwTotalReleasedVolume;
           
        
        
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
//            getModel().getRuntime().println("Reach de rejet (DwReleaseReach):"+reaches.getCurrent().getId());
                
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
            double run_totalIn =  run_inRD1 + run_inRD2 + run_inRG1 + run_inRG2; // all water in inflow (for proportional extraction)
            double run_totalAct = run_actRD1 + run_actRD2 + run_actRG1 + run_actRG2; // all water in act (for proportional extraction)
            double run_totalStorage = run_totalIn + run_totalAct; // all water in inflow and act
            
            // loop on HRUs: recover data of extracted volumes from HRUs in which extraction occurs
            long release_reach = reaches.getCurrent().getId();
             if (run_reachMap.get(release_reach).existsAttribute(dwHRUEntitiesListName.getValue())) { // check if dwHRUEntities exists -- otherwise there will be error "Attribute dwHRUEntities not found!"      
            
            List<Attribute.Entity> run_h = (List) run_currentReach.getObject(dwHRUEntitiesListName.getValue());
            for (Attribute.Entity run_hru : run_h) {
                double run_ExtractedVolumeHRU = run_hru.getDouble(dwNetExtractedVolumeName.getValue());
                run_totalExtractedVolumeHRU += run_ExtractedVolumeHRU; // sum of all the extracted volumes from HRUs
                }
            }  
             
            // loop on reaches: recover data of extracted volumes from reaches in which extraction occurs
             if (run_reachMap.get(release_reach).existsAttribute(dwReachEntitiesListName.getValue())) { // check if dwReachEntities exists -- otherwise there will be error "Attribute dwReachEntities not found!"      
            
                List<Attribute.Entity> run_r = (List) run_currentReach.getObject(dwReachEntitiesListName.getValue());
            for (Attribute.Entity run_reach : run_r) {
                double run_ExtractedVolumeReach = run_reach.getDouble(dwNetExtractedVolumeName.getValue());
                run_totalExtractedVolumeReach += run_ExtractedVolumeReach; // sum of all the extracted volumes from reaches
                }
            }
             
            // total extracted volume for the release reach
            dwTotalExtractedVolume.setValue(run_totalExtractedVolumeHRU + run_totalExtractedVolumeReach);
            
            dwTotalReleasedVolume.setValue(dwReleaseFactor.getValue() * dwTotalExtractedVolume.getValue()); // released volume = dwReleaseFactor * extracted volume
            double run_dwTotalReleasedVolume = dwTotalReleasedVolume.getValue();
            
            // release into the reach
            
            if (run_totalIn + run_totalAct > 1E-10) {
                getModel().getRuntime().println("- release : run_totalIn+Act > 1E-10");
                
                double run_storageReleasedFraction = run_dwTotalReleasedVolume / run_totalStorage;// fraction of all stored water that is released

                // release proportionally into inflow (ratio release over all water)
                inRD1.setValue(run_inRD1 * (1 + run_storageReleasedFraction));
                inRD2.setValue(run_inRD2 * (1 + run_storageReleasedFraction));
                inRG1.setValue(run_inRG1 * (1 + run_storageReleasedFraction));
                inRG2.setValue(run_inRG2 * (1 + run_storageReleasedFraction));
                // release proportionally into act (ratio release over all water)
                actRD1.setValue(run_actRD1 * (1 + run_storageReleasedFraction));
                actRD2.setValue(run_actRD2 * (1 + run_storageReleasedFraction));
                actRG1.setValue(run_actRG1 * (1 + run_storageReleasedFraction));
                actRG2.setValue(run_actRG2 * (1 + run_storageReleasedFraction));              
                
            }
            
            else { // if no water in inflow and act, everything is released into inRD1 and actRD1
                getModel().getRuntime().println("- release : else : run_totalIn+Act = 0");
                
                inRD1.setValue(run_dwTotalReleasedVolume / 2);
                actRD1.setValue(run_dwTotalReleasedVolume / 2);
                
            }
            
        }
    
}
