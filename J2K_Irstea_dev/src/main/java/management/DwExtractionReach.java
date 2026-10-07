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


/**
 *
 * @author Sven Kralisch & Mériem Labbas & Christian Fischer
 */
@JAMSComponentDescription(title = "Drinking water device to extract water from reach",
        author = "Francois Tilmant & Flora Branger / L Crochemore & AL Borgna",
        description = "Component used for the simulation of drinking water extraction in the reach.",
        version = "1.0_0",
        date = "2026-01-20")
public class DwExtractionReach extends JAMSComponent {

    /*
     * Component variables
     */  
        @JAMSVarDescription(
                access = JAMSVarDescription.AccessType.READ,
                description = "Reach list"
        )
        public Attribute.EntityCollection reaches;
        
        @JAMSVarDescription (
            access = JAMSVarDescription.AccessType.READ,
            description = "Regionalised data value (objective function) of water extraction for drinking water. - input",
            unit = "L"
        )
        public Attribute.Double FO;
                
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
            description = "Ratio of water available (allowed to be extracted) for drinking water over water present "+
                    "in the hru GW (actR.. + inR..). - parameter"
        )
        public Attribute.Double allowedExtractionFraction;
        
        @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Factor to quantify expected losses through the pipe network. - parameter"
        )
        public Attribute.Double dwLossesFactor;
        
        @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "Water volume of expected losses through the pipe network. - output",
            unit = "L"
        )
        public Attribute.Double dwLosses;
        
        @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Multiplicative factor for adjusting the consumption values in drinking_water.dat. - parameter"
        )
        public Attribute.Double dwConsumptionFactor;

        @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "Water volume actually extracted from source (HRU or reach) for drinking water, "
                    +"before network losses. - output",
            unit = "L"
        )
        public Attribute.Double dwGrossExtractedVolume;
        
        @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "Water volume actually extracted from source (HRU or reach) for drinking water, "
                    +"after network losses. Will be read by pointer dwNetExtractedVolumeName. - output",
            unit = "L"
        )
        public Attribute.Double dwNetExtractedVolume;       
          
        
        
        @Override
        public void run() {
                            
                double run_inRD1 = inRD1.getValue();
                double run_inRD2 = inRD2.getValue();
                double run_inRG1 = inRG1.getValue();
                double run_inRG2 = inRG2.getValue();
                double run_actRD1 = actRD1.getValue();
                double run_actRD2 = actRD2.getValue();
                double run_actRG1 = actRG1.getValue();
                double run_actRG2 = actRG2.getValue();
                double run_allowedExtractionFraction = allowedExtractionFraction.getValue();
                double run_dwLossesFactor = dwLossesFactor.getValue();
                
                double run_totalIn =  run_inRD1 + run_inRD2 + run_inRG1 + run_inRG2; // all water in inflow (for proportional extraction)
                double run_totalAct = run_actRD1 + run_actRD2 + run_actRG1 + run_actRG2; // all water in act (for proportional extraction)
                double run_totalStorage = run_totalIn + run_totalAct; // all water in inflow and act
                double run_inAvailable = run_allowedExtractionFraction * run_totalIn;
                double run_actAvailable = run_allowedExtractionFraction * run_totalAct; // hru GW water available for drinking water
                double run_totalAvailable = run_inAvailable + run_actAvailable; // all available water
                            
                // check
//                 getModel().getRuntime().println("Extraction in reach (DwExtractionReach):"+reaches.getCurrent().getId());

                if(run_totalIn+run_totalAct > 1E-10) {
                
                    // Water consumed (correction with dwConsumptionFactor)
                    double FO_act_corr = FO.getValue() * dwConsumptionFactor.getValue();

                    // Account for losses in the network: case dwLossesFactor > 1
                    if (run_dwLossesFactor > 1) { // this case is not possible: a warning is printed + dwLossesFactor is reset to 0.2
                        getModel().getRuntime().println("Warning: dwLossesFactor > 1: error. dwLossesFactor is reset to 0.2");
                        dwLossesFactor.setValue(0.2);
                        run_dwLossesFactor = dwLossesFactor.getValue();
                    }
                                           
                    // Account for losses in the network: case dwLossesFactor != 1
                    double FO_act = FO_act_corr / (1 - run_dwLossesFactor);

                    if (FO_act <= run_totalAvailable) { // demand can be satisfied with available water from inflow and act
                        
                        double run_storageDemandFraction = FO_act / run_totalStorage;// fraction of all stored water that is demanded for drinking water

                        // extract proportionally from inflow (ratio demand over all water)
                        inRD1.setValue(run_inRD1 * (1 - run_storageDemandFraction));
                        inRD2.setValue(run_inRD2 * (1 - run_storageDemandFraction));
                        inRG1.setValue(run_inRG1 * (1 - run_storageDemandFraction));
                        inRG2.setValue(run_inRG2 * (1 - run_storageDemandFraction));
                        // extract proportionally from act (ratio demand over all water)
                        actRD1.setValue(run_actRD1 * (1 - run_storageDemandFraction));
                        actRD2.setValue(run_actRD2 * (1 - run_storageDemandFraction));
                        actRG1.setValue(run_actRG1 * (1 - run_storageDemandFraction));
                        actRG2.setValue(run_actRG2 * (1 - run_storageDemandFraction));
                        
                        // we can satisfy the demand (extract everything that is needed)
                        dwGrossExtractedVolume.setValue(FO_act);
                        
                        // update run_inRD2 for losses, thereafter
                        run_inRD2 = inRD2.getValue();
                        
                    } else { // not all of the demand can be satisfied from available water. Only available water will be extracted
                        
                        // extract proportionally from inflow (ratio demand over all water)
                        inRD1.setValue(run_inRD1 * (1 - run_allowedExtractionFraction));
                        inRD2.setValue(run_inRD2 * (1 - run_allowedExtractionFraction));
                        inRG1.setValue(run_inRG1 * (1 - run_allowedExtractionFraction));
                        inRG2.setValue(run_inRG2 * (1 - run_allowedExtractionFraction));
                        // extract proportionally from act (ratio demand over all water)
                        actRD1.setValue(run_actRD1 * (1 - run_allowedExtractionFraction));
                        actRD2.setValue(run_actRD2 * (1 - run_allowedExtractionFraction));
                        actRG1.setValue(run_actRG1 * (1 - run_allowedExtractionFraction));
                        actRG2.setValue(run_actRG2 * (1 - run_allowedExtractionFraction));
                        
                         // we extract all available water
                        dwGrossExtractedVolume.setValue(run_totalAvailable);
                        
                        // update run_inRD2 for losses, thereafter
                        run_inRD2 = inRD2.getValue();
                    }

                    // restitute lost water to inRD2 (when efficiency of the network dwLossesFactor <1) :
                    double run_dwLosses = run_dwLossesFactor * dwGrossExtractedVolume.getValue();
                    inRD2.setValue(run_inRD2 + run_dwLosses);
                    dwLosses.setValue(run_dwLosses);
                    
                    // calculate volume that can be released into release reach (in component DwReleaseReach) = extracted volume after losses
                    double run_dwNetExtractedVolume = dwGrossExtractedVolume.getValue() - dwLosses.getValue();
                    dwNetExtractedVolume.setValue(run_dwNetExtractedVolume);
                    
                } else { // no extraction (not enough water in reach)
                    dwGrossExtractedVolume.setValue(0.0);
                    dwNetExtractedVolume.setValue(0.0);
                }   
        }
}
