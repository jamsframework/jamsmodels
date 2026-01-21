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
        author = "Francois Tilmant & Flora Branger / LC & ALB",
        description = "Component used for the simulation of drinking water extraction. It takes the different components outflows"
        + "coming from a sewer reach (threshold test).",
        version = "3.0_0",
        date = "2026-01-20")
public class AEPExtractionReach extends JAMSComponent {

    /*
     * Component variables
     */
        @JAMSVarDescription(
                access = JAMSVarDescription.AccessType.READ,
                description = "HRUs list"
        )
        public Attribute.EntityCollection hrus;    
    
        @JAMSVarDescription(
                access = JAMSVarDescription.AccessType.READ,
                description = "Reach list"
        )
        public Attribute.EntityCollection reaches;
        
        @JAMSVarDescription (
            access = JAMSVarDescription.AccessType.READ,
            description = "regionalised data value (objective function)"
        )
        public Attribute.Double FO;
                
        @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "RD1 inflow to reach",
            unit = "L"
        )
        public Attribute.Double inRD1;
    
        @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "RD2 inflow to reach",
            unit = "L"
        )
        public Attribute.Double inRD2;
    
        @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "RG1 inflow to reach",
            unit = "L"
        )
        public Attribute.Double inRG1;
    
        @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "RG2 inflow to reach",
            unit = "L"
        )
        public Attribute.Double inRG2;
   
        @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "actRD1 component in reach"
        )
        public Attribute.Double actRD1;
        
        @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "actRD2 component in reach"
        )
        public Attribute.Double actRD2;
        
        @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "actRG1 component in reach"
        )
        public Attribute.Double actRG1;
            
        @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "actRG2 component in reach"
        )
        public Attribute.Double actRG2;
                 
        @JAMSVarDescription(
                access = JAMSVarDescription.AccessType.READ,
                description = "Ratio of water available for AEP / water present in the Reach"
        )
        public Attribute.Double actPrel;
        
        @JAMSVarDescription(
                access = JAMSVarDescription.AccessType.READ,
                description = "Expected losses through the pipe network"
        )
        public Attribute.Double netLoss;
        
        @JAMSVarDescription(
                access = JAMSVarDescription.AccessType.READ,
                description = "Multiplicative factor for adjusting the consumption values in AEP.dat"
        )
        public Attribute.Double aepFactor;

        @JAMSVarDescription(
                access = JAMSVarDescription.AccessType.WRITE,
            description = "Water volume effectively extracted from source (HRU or reach) for AEP."
                +"Will be read by pointer aepExtractedVolumeName. - output",
            unit = "L"
        )
        public Attribute.Double aepExtractedVolume;
        
        @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Reach ID where release occurs (attribute release_reach_ID from reach.par). - parameter"
        )
        public Attribute.Double releaseReach;
          
        
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
                double run_netLoss = netLoss.getValue();
                            
                // check
//                 getModel().getRuntime().println("Extraction in reach (AEPExtractionReach):"+reaches.getCurrent().getId());
                
                // Total inflow
                double totalIn = run_inRD1 + run_inRD2 + run_inRG1 + run_inRG2;

                // Water already in the reach available for extraction 
                // -- see if actPrel relevant in management scenarios
                // -- set to 1 at first because AEP is generally prioritized over other uses
                double totalAct = run_actPrel * (run_actRD1 + run_actRD2 + run_actRG1 + run_actRG2); // water available for drinking water

                if(totalIn > 1E-20) {

                    // Water consumed
                    double FO_act = FO.getValue() * aepFactor.getValue();

                    // Account for losses in the network
                    FO_act = FO_act + run_netLoss * FO_act;

                    // looking if we can cover the demand with in
                    double frac_in = FO_act/totalIn;
                    if(Double.isInfinite(frac_in)){
                        getModel().getRuntime().println("Infinite fraction:"+reaches.getCurrent().getId());
                    }

                    if (frac_in >= -1) {

                        // we can cover all only with in to the reach, reduce the components accordingly
                        inRD1.setValue(run_inRD1 * (1 - frac_in));
                        inRD2.setValue(run_inRD2 * (1 - frac_in));
                        inRG1.setValue(run_inRG1 * (1 - frac_in));
                        inRG2.setValue(run_inRG2 * (1 - frac_in));
                        aepExtractedVolume.setValue(FO_act); // extracted volume = FO_act : FO_act demand fully satisfied. /!\ FO_act < 0

                    } else {
                        // looking if we can cover the demand by including part of act...
                        double frac_in_act = FO_act / (totalIn+totalAct);
                        if(Double.isInfinite(frac_in_act)){
                            getModel().getRuntime().println("Infinite fraction:"+reaches.getCurrent().getId());
                        }

                        // we can cover only part of the demand with in, reduce the components to 0
                        inRD1.setValue(0);
                        inRD2.setValue(0);
                        inRG1.setValue(0);
                        inRG2.setValue(0);

                        if (frac_in_act >= -1) {
                            // we can cover all of the demand but not only with in..., reduce the components accordingly
                            double actDemand = 0;
                            actDemand = FO_act + totalIn;
                            double frac2 = -actDemand/totalAct;
                            if(Double.isInfinite(frac2)){
                                getModel().getRuntime().println("Infinite fraction:"+reaches.getCurrent().getId());
                            }
                            if(frac2<0) {
                                getModel().getRuntime().println("Warning: error in sign when extracting drinking water in reach");
                            }
                            actRD1.setValue(run_actRD1 * (1 - frac2));
                            actRD2.setValue(run_actRD2 * (1 - frac2));
                            actRG1.setValue(run_actRG1 * (1 - frac2));
                            actRG2.setValue(run_actRG2 * (1 - frac2));
                            aepExtractedVolume.setValue(FO_act); // extracted volume = FO_act : FO_act demand fully satisfied. /!\ FO_act < 0

                        } else {
                            // we can cover part of the demand ; reduce the act... to (1 - actPrel)*act...
                            actRD1.setValue(run_actRD1 * (1 - run_actPrel));
                            actRD2.setValue(run_actRD2 * (1 - run_actPrel));
                            actRG1.setValue(run_actRG1 * (1 - run_actPrel));
                            actRG2.setValue(run_actRG2 * (1 - run_actPrel));
                            aepExtractedVolume.setValue(-(totalIn+totalAct)); // FO_act demand partially satisfied. /!\ < 0
                        }
                    }

                    // restitute lost water to RD2 (when efficiency of the network netLoss <1) :
                    inRD2.setValue(run_inRD2 + Math.max(0.,-run_netLoss * aepExtractedVolume.getValue()));
                    
                
                } else { // no extraction (not enough water in reach)
                    aepExtractedVolume.setValue(0.0);

        }   
    }
}
