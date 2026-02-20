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
@JAMSComponentDescription(title = "AEP device to extract water from HRU",
        author = "Francois Tilmant & Flora Branger / L Crochemore & AL Borgna",
        description = "Component used for the simulation of drinking water extraction in the HRU groundwater.",
        version = "1.0_0",
        date = "2026-01-20")
public class AEPExtractionHRU extends JAMSComponent {

    /*
     * Component variables
     */ 
        @JAMSVarDescription(
                access = JAMSVarDescription.AccessType.READ,
                description = "HRUs list"
        )
        public Attribute.EntityCollection hrus;
    
        @JAMSVarDescription (
            access = JAMSVarDescription.AccessType.READ,
            description = "Regionalised data value (objective function) of water extraction for drinking water. - input",
            unit = "L"
        )
        public Attribute.Double FO;
    
        @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "RD2 inflow to HRU. - state variable",
            unit = "L"
        )
        public Attribute.Double inRD2;
        
        @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "RG1 inflow to HRU. - state variable",
            unit = "L"
        )
        public Attribute.Double inRG1;
    
        @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "RG2 inflow to HRU. - state variable",
            unit = "L"
        )
        public Attribute.Double inRG2;
    
        @JAMSVarDescription(
                access = JAMSVarDescription.AccessType.READWRITE,
                description = "actRG1 component in HRU"
        )
        public Attribute.Double actRG1;

        @JAMSVarDescription(
                access = JAMSVarDescription.AccessType.READWRITE,
                description = "actRG2 component in HRU"
        )
        public Attribute.Double actRG2;
    
        @JAMSVarDescription(
                access = JAMSVarDescription.AccessType.READ,
                description = "Ratio of water available for AEP / water present in the HRU. - parameter"
        )
        public Attribute.Double actPrel;
        
        @JAMSVarDescription(
                access = JAMSVarDescription.AccessType.READ,
                description = "Expected losses through the pipe network. - parameter"
        )
        public Attribute.Double netLoss;
         
        @JAMSVarDescription(
                access = JAMSVarDescription.AccessType.READ,
                description = "Multiplicative factor for adjusting the consumption values in AEP.dat. - parameter"
        )
        public Attribute.Double aepFactor;
        
        @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "Water volume actually extracted from source (HRU or reach) for AEP."
                    +"Will be read by pointer aepExtractedVolumeName. - output",
            unit = "L"
        )
        public Attribute.Double aepExtractedVolume;

        @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Reach ID where release occurs (attribute release_reach_ID from hru.par). - parameter"
        )
        public Attribute.Double releaseReach;
    
        

    /*
     *  Component run stages
     */       
        @Override
        public void run() {
                                  
                double run_inRD2 = inRD2.getValue();
                double run_inRG1 = inRG1.getValue();
                double run_inRG2 = inRG2.getValue();
                double run_actRG1 = actRG1.getValue();
                double run_actRG2 = actRG2.getValue();
                double run_actPrel = actPrel.getValue();
                double run_netLoss = netLoss.getValue();
                                
                // check
                //getModel().getRuntime().println("Extraction in HRU (AEPExtractionHRU):"+hrus.getCurrent().getId());
                               
                // Total inflow
                double run_totalIn = run_inRG1 + run_inRG2;
//                getModel().getRuntime().println("run_totalIn:"+run_totalIn); // check

                // Water already in the reach available for extraction 
                // -- see if actPrel relevant in management scenarios
                // -- set to 1 at first because AEP is generally prioritized over other uses
                double run_totalAct = run_actPrel * (run_actRG1 + run_actRG2); // water available for drinking water
//                getModel().getRuntime().println("run_totalAct:"+run_totalAct); // check

                if(run_totalIn+run_totalAct > 1E-10) {
                    
                    // Water consumed
                    double FO_act = FO.getValue() * aepFactor.getValue();

                    // Account for losses in the network
                    FO_act = FO_act + run_netLoss * FO_act;
                    
                    // looking if we can cover the demand with in
                    if (run_totalIn > 1E-10) { // to avoid division by zero
                    double run_demandFractionOverInflow = FO_act/run_totalIn;
//                    getModel().getRuntime().println("run_demandFractionOverInflow = FO_act/run_totalIn:"+run_demandFractionOverInflow); // check

                    if (run_demandFractionOverInflow >= -1) {

                        // we can cover all only with in to the HRU, reduce the components accordingly
                        inRG1.setValue(run_inRG1 * (1 + run_demandFractionOverInflow));
//                        getModel().getRuntime().println("inRG1 apres avoir setValue - cas 1:"+inRG1.getValue()); // check
                        inRG2.setValue(run_inRG2 * (1 + run_demandFractionOverInflow));
                        aepExtractedVolume.setValue(FO_act); // extracted volume = FO_act : FO_act demand fully satisfied. /!\ FO_act < 0
//                        getModel().getRuntime().println("aepExtractedVolume apres avoir setValue - cas 1:"+aepExtractedVolume.getValue()); // check
                    }

                    } else {
                        // looking if we can cover the demand by including part of act...
                        double run_demandFractionOverTotalWater = FO_act / (run_totalIn + run_totalAct);
//                        getModel().getRuntime().println("run_demandFractionOverTotalWater = FO_act / (run_totalIn + run_totalAct):"+run_demandFractionOverTotalWater); // check

                        // we can cover only part of the demand with in, reduce the components to 0
                        inRG1.setValue(0);
//                        getModel().getRuntime().println("inRG1 apres avoir setValue - cas 2: vaut 0 car on vide tt:"+inRG1.getValue()); // check
                        inRG2.setValue(0);

                        if (run_demandFractionOverTotalWater >= -1) {
                            // we can cover all of the demand but not only with in..., reduce the components accordingly
                            double actDemand = FO_act + run_totalIn;
//                            getModel().getRuntime().println("actDemand:"+actDemand); // check
                            if (run_totalAct > 1E-10) { // to avoid division by zero
                            double run_actDemandFraction = -actDemand/run_totalAct;
//                            getModel().getRuntime().println("run_actDemandFraction = -actDemand/run_totalAct:"+run_actDemandFraction); // check
                            if(Double.isInfinite(run_actDemandFraction)){
                                getModel().getRuntime().println("Infinite fraction:"+hrus.getCurrent().getId());
                            }
                            if(run_actDemandFraction < 0) {
                                getModel().getRuntime().println("Warning: error in sign when extracting drinking water in HRU");
                            }
                            actRG1.setValue(run_actRG1 * (1 - run_actDemandFraction));
//                            getModel().getRuntime().println("actRG1 apres avoir setValue - cas 2:"+actRG1.getValue()); // check
                            actRG2.setValue(run_actRG2 * (1 - run_actDemandFraction));
                            aepExtractedVolume.setValue(FO_act); // extracted volume = FO_act : FO_act demand fully satisfied. /!\ FO_act < 0
//                            getModel().getRuntime().println("aepExtractedVolume apres avoir setValue - cas 2:"+aepExtractedVolume.getValue()); // check
                            }

                        } else {
                            // we can cover part of the demand ; reduce the act... to (1 - actPrel)*act...
                            actRG1.setValue(run_actRG1 * (1 - run_actPrel));
//                            getModel().getRuntime().println("actRG1 apres avoir setValue - cas 3:"+actRG1.getValue()); // check
                            actRG2.setValue(run_actRG2 * (1 - run_actPrel));
                            aepExtractedVolume.setValue(-(run_totalIn + run_totalAct)); // FO_act demand partially satisfied. /!\ < 0
//                            getModel().getRuntime().println("aepExtractedVolume apres avoir setValue - cas 3:"+aepExtractedVolume.getValue()); // check
                        }
                    }

                    // restitute lost water to RD2 (when efficiency of the network netLoss <1) :
                    inRD2.setValue(run_inRD2 + Math.max(0.,-run_netLoss * aepExtractedVolume.getValue()));
//                    getModel().getRuntime().println("AEPExtractionHRU - run_inRD2:"+run_inRD2); // check
//                    getModel().getRuntime().println("AEPExtractionHRU - fuites:"+run_netLoss * -aepExtractedVolume.getValue()); // check

                    
                } else { // no extraction (not enough water in HRU)
                    aepExtractedVolume.setValue(0.0);
//                    getModel().getRuntime().println("aepExtractedVolume apres avoir setValue - cas no extraction:"+aepExtractedVolume.getValue()); // check

        }
    }
}
