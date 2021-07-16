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
//import java.jamsui.juice;


/**
 *
 * @author Sven Kralisch & Mériem Labbas & Christian Fischer
 */
@JAMSComponentDescription(title = "AEP Device to extract water from reach",
        author = "Francois Tilmant & Flora Branger / LC",
        description = "Component used for the simulation of drinking water extraction. It takes the different components outflows"
        + "coming from a sewer reach(threshold test) and adds it to the receiving reach river.",
        version = "3.0_0",
        date = "2021-06-14")
public class AEP_Device_Reach extends JAMSComponent {

    /*
     * Component variables
     */
        @JAMSVarDescription(
                access = JAMSVarDescription.AccessType.READ,
                description = "Reach list"
        )
        public Attribute.EntityCollection reach;
        
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
                description = "Multiplicative factor for adjusting the consumption values in AEP.dat"
        )
        public Attribute.Double aepFactor;
        
        @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "FO corrected if there isn't enough water in the river",
            unit = "L"
        )
        public Attribute.Double FO_fin;
   
        
        public void run() {

            // Check if reach related to AEP transfers
            //if (reach.getCurrent().existsAttribute("AEP")) {
            if (reach.getCurrent().getDouble("AEP")==1.0) {
                            
                // check
                // getModel().getRuntime().println("Drinking water in Reach:"+reach.getCurrent().getId());
                
                // Total inflow
                double totalIn = this.inRD1.getValue() + this.inRD2.getValue() + this.inRG1.getValue() + this.inRG2.getValue();

                // Water already in the reach available for extraction 
                // -- see if actPrel relevant in management scenarios
                // -- set to 1 at first because AEP is generally prioritized over other uses
                double totalAct = this.actPrel.getValue() * (actRD1.getValue() + actRD2.getValue() + actRG1.getValue() + actRG2.getValue()); // eau du reach dispo pour l'irrigation.

                if(totalIn > 1E-20) {

                    // Water consumed
                    double FO_act = this.FO.getValue() * this.aepFactor.getValue();

                    //Case of extraction
                    if (FO_act < 0) {

                        // Account for losses in the network - not possible with reaches
                        //FO_act = FO_act + netLoss.getValue()*FO_act;
                        
                        //looking if we can cover the demand with in
                        double frac = FO_act/totalIn;
                        if(Double.isInfinite(frac)){
                            getModel().getRuntime().println("Infinite frac:"+reach.getCurrent().getId());
                        }

                        if (frac >= -1) {

                            //we can cover all only with in to the reach, reduce the components accordingly
                            inRD1.setValue(inRD1.getValue() * (1 - frac));
                            inRD2.setValue(inRD2.getValue() * (1 - frac));
                            inRG1.setValue(inRG1.getValue() * (1 - frac));
                            inRG2.setValue(inRG2.getValue() * (1 - frac));
                            this.FO_fin.setValue(FO_act) ;

                        } else {
                            //looking if we can cover the demand by including part of act...
                            frac = FO_act / (totalIn+totalAct);
                            if(Double.isInfinite(frac)){
                                getModel().getRuntime().println("Infinite frac:"+reach.getCurrent().getId());
                            }

                            //we can cover only part of the demand with in, reduce the components to 0
                            inRD1.setValue(0);
                            inRD2.setValue(0);
                            inRG1.setValue(0);
                            inRG2.setValue(0);

                            if (frac >= -1) {
                                //we can cover all of the demand but not only with in..., reduce the components accordingly
                                double actDemand = 0;
                                actDemand = FO_act + totalIn;
                                double frac2 = -actDemand/totalAct;
                                if(Double.isInfinite(frac2)){
                                    getModel().getRuntime().println("Infinite frac:"+reach.getCurrent().getId());
                                }
                                if(frac2<0) {
                                    getModel().getRuntime().println("Warning: error in sign when extracting drinking water in reach");
                                }
                                actRD1.setValue(actRD1.getValue() * (1 - frac2));
                                actRD2.setValue(actRD2.getValue() * (1 - frac2));
                                actRG1.setValue(actRG1.getValue() * (1 - frac2));
                                actRG2.setValue(actRG2.getValue() * (1 - frac2));
                                this.FO_fin.setValue(FO_act) ;

                            } else {
                                // we can cover part of the demand ; reduce the act... to (1 - actPrel)*act...
                                actRD1.setValue(actRD1.getValue() * (1 - actPrel.getValue()));
                                actRD2.setValue(actRD2.getValue() * (1 - actPrel.getValue()));
                                actRG1.setValue(actRG1.getValue() * (1 - actPrel.getValue()));
                                actRG2.setValue(actRG2.getValue() * (1 - actPrel.getValue()));
                                this.FO_fin.setValue(-(totalIn+totalAct));
                            }
                        }

                        // restitute lost water to RD2 (when efficiency of the network netLoss <1) :
                        //inRD2.setValue(inRD2.getValue()+Math.max(0.,-netLoss.getValue()*this.FO_fin.getValue()));

                    //Case of release
                    } else {

                        //looking if we can cover the demand with in
                        double frac = FO_act/totalIn;
                        if(Double.isInfinite(frac)){
                            getModel().getRuntime().println("Infinite frac:"+reach.getCurrent().getId());
                        }

                        //we can cover all only with in to the reach, reduce the components accordingly
                        inRD1.setValue(inRD1.getValue() * (1 + frac));
                        inRD2.setValue(inRD2.getValue() * (1 + frac));
                        inRG1.setValue(inRG1.getValue() * (1 + frac));
                        inRG2.setValue(inRG2.getValue() * (1 + frac));
                        this.FO_fin.setValue(FO_act) ;

                    }
                    
                } else {
                    
                    this.FO_fin.setValue(0.0);
            
                }
                
            } else {
                
                this.FO_fin.setValue(0.0);
            
            }
            
        }
    
}
