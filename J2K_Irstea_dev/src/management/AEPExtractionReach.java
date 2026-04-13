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
@JAMSComponentDescription(title = "AEP device to extract water from reach",
        author = "Francois Tilmant & Flora Branger / L Crochemore & AL Borgna",
        description = "Component used for the simulation of drinking water extraction in the reach.",
        version = "1.0_0",
        date = "2026-01-20")
public class AEPExtractionReach extends JAMSComponent {

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
            description = "Ratio of water available for AEP / water present in the reach. - parameter"
        )
        public Attribute.Double actPrel;
        
        @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Factor to quantify expected losses through the pipe network. - parameter"
        )
        public Attribute.Double aepLossesFactor;
        
        @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "Water volume of expected losses through the pipe network. - output",
            unit = "L"
        )
        public Attribute.Double aepLosses;
        
        @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Multiplicative factor for adjusting the consumption values in AEP.dat. - parameter"
        )
        public Attribute.Double aepConsumptionFactor;

        @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "Water volume actually extracted from source (HRU or reach) for drinking water, "
                    +"before network losses. - output",
            unit = "L"
        )
        public Attribute.Double aepGrossExtractedVolume;
        
        @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "Water volume actually extracted from source (HRU or reach) for drinking water, "
                    +"after network losses. Will be read by pointer aepNetExtractedVolumeName. - output",
            unit = "L"
        )
        public Attribute.Double aepNetExtractedVolume;       
          
        
        
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
                double run_actPrel = actPrel.getValue();
                double run_aepLossesFactor = aepLossesFactor.getValue();
                            
                // check
//                 getModel().getRuntime().println("Extraction in reach (AEPExtractionReach):"+reaches.getCurrent().getId());
                
                // Total inflow
                double run_totalIn = run_inRD1 + run_inRD2 + run_inRG1 + run_inRG2;

                // Water already in the reach available for extraction 
                // -- see if actPrel relevant in management scenarios
                // -- set to 1 at first because AEP is generally prioritized over other uses
                double run_totalAct = run_actPrel * (run_actRD1 + run_actRD2 + run_actRG1 + run_actRG2); // water available for drinking water

                if(run_totalIn+run_totalAct > 1E-10) {
                
                    // Water consumed (correction with aepConsumptionFactor)
                    double FO_act_corr = FO.getValue() * aepConsumptionFactor.getValue();

                    // Account for losses in the network: case aepLossesFactor > 1
                    if (run_aepLossesFactor > 1) { // this case is not possible: a warning is printed + aepLossesFactor is reset to 0.2
                        getModel().getRuntime().println("Warning: aepLossesFactor > 1: error. aepLossesFactor is reset to 0.2");
                        aepLossesFactor.setValue(0.2);
                        run_aepLossesFactor = aepLossesFactor.getValue();
                    }
                                           
                    // Account for losses in the network: case aepLossesFactor != 1
                    double FO_act = FO_act_corr / (1 - run_aepLossesFactor);

                    // looking if we can cover the demand with in
                    if (Math.abs(FO_act)<= run_totalIn) {

                        if (run_totalIn > 1E-10) { // to avoid division by zero

                            double run_demandFractionOverInflow = FO_act/run_totalIn;

                            // we can cover all only with in to the reach, reduce the components accordingly
                            inRD1.setValue(run_inRD1 * (1 - run_demandFractionOverInflow));
                            inRD2.setValue(run_inRD2 * (1 - run_demandFractionOverInflow));
                            inRG1.setValue(run_inRG1 * (1 - run_demandFractionOverInflow));
                            inRG2.setValue(run_inRG2 * (1 - run_demandFractionOverInflow));
                            aepGrossExtractedVolume.setValue(FO_act); // extracted volume = FO_act : FO_act demand fully satisfied

                            run_inRD2 = inRD2.getValue(); // update run_inRD2 for losses, thereafter
                        }

                    } else {
                        // looking if we can cover the demand by including part of act...
                        double run_demandFractionOverTotalWater = FO_act / (run_totalIn + run_totalAct);

                        // we can cover only part of the demand with in, reduce the components to 0
                        inRD1.setValue(0);
                        inRD2.setValue(0);
                        inRG1.setValue(0);
                        inRG2.setValue(0);

                        run_inRD2 = inRD2.getValue(); // update run_inRD2 for losses, thereafter

                        if (run_demandFractionOverTotalWater <= 1) { // i.e. if FO_act <= totalIn+totalAct  

                            if (run_totalAct > 1E-10) { // to avoid division by zero

                                double actDemand = FO_act - run_totalIn;
                                double run_actDemandFraction = actDemand/run_totalAct;

                                // we can cover all of the demand but not only with in..., reduce the components accordingly
                                actRD1.setValue(run_actRD1 * (1 - run_actDemandFraction));
                                actRD2.setValue(run_actRD2 * (1 - run_actDemandFraction));
                                actRG1.setValue(run_actRG1 * (1 - run_actDemandFraction));
                                actRG2.setValue(run_actRG2 * (1 - run_actDemandFraction));
                                aepGrossExtractedVolume.setValue(FO_act); // extracted volume = FO_act : FO_act demand fully satisfied
                            }

                        } else {
                            // we can cover part of the demand ; reduce the act... to (1 - actPrel)*act...
                            actRD1.setValue(run_actRD1 * (1 - run_actPrel));
                            actRD2.setValue(run_actRD2 * (1 - run_actPrel));
                            actRG1.setValue(run_actRG1 * (1 - run_actPrel));
                            actRG2.setValue(run_actRG2 * (1 - run_actPrel));
                            aepGrossExtractedVolume.setValue(-(run_totalIn + run_totalAct)); // FO_act demand partially satisfied
                        }
                    }

                    // restitute lost water to inRD2 (when efficiency of the network aepLossesFactor <1) :
                    double run_aepLosses = run_aepLossesFactor * aepGrossExtractedVolume.getValue();
                    inRD2.setValue(run_inRD2 + run_aepLosses);
                    aepLosses.setValue(run_aepLosses);

                    // calculate volume that can be released into release reach (in component AEPReleaseReach) = extracted volume after losses
                    double run_aepNetExtractedVolume = aepGrossExtractedVolume.getValue() - aepLosses.getValue();
                    aepNetExtractedVolume.setValue(run_aepNetExtractedVolume);
                    
                } else { // no extraction (not enough water in reach)
                    aepGrossExtractedVolume.setValue(0.0);
                    aepNetExtractedVolume.setValue(0.0);
                }   
        }
}
