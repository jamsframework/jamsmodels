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
        date = "2014-04-17")
public class AEP_Device_Reach extends JAMSComponent {

    /*
     * Component variables
     */
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
                access = JAMSVarDescription.AccessType.READ,
                description = "Ratio of water available for AEP / water present in the Reach"
        )
        public Attribute.Double actPrel;
        
        @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "FO corrected if there isn't enough water in the river",
            unit = "L"
        )
        public Attribute.Double FO_fin;
   
        int nComp = 4;
        double[] relComp;
        double[] runComp;
        double[] outComp;
        double currVolume = 0;

        public void init() {
            relComp = new double[nComp];
            runComp = new double[nComp];
            outComp = new double[nComp];    
        }

        public void run() {

            double runOutflow;
            double FO_act;

            // Total inflow
            double totalIn = this.inRD1.getValue();
            totalIn = totalIn + this.inRD2.getValue();
            totalIn = totalIn + this.inRG1.getValue();
            totalIn = totalIn + this.inRG2.getValue();

            // Water in the reach available for extraction 
            // -- see if relevant in management scenarios
            // -- set to 1 at first because AEP is generally prioritized over other uses
            double totalAct = totalIn * this.actPrel.getValue();

            calcRelComponents(); 

            this.runComp[0] = this.inRD1.getValue();
            this.runComp[1] = this.inRD2.getValue();
            this.runComp[2] = this.inRG1.getValue();
            this.runComp[3] = this.inRG2.getValue();

            calcRelComponents();

            FO_act = this.FO.getValue();
            if(FO_act < 0){
                //Cas de prelevement
                if( (totalAct + FO_act) <0) { FO_act = -totalAct;} 
            }

            runOutflow = Math.max(0,totalIn + FO_act);       

            this.FO_fin.setValue(FO_act) ;
            for(int i = 0; i < runComp.length; i++){
                outComp[i] = runOutflow * relComp[i];
            }
            this.inRD1.setValue(outComp[0]);
            this.inRD2.setValue(outComp[1]);
            this.inRG1.setValue(outComp[2]);
            this.inRG2.setValue(outComp[3]);

        }
    
        private void calcRelComponents(){
            currVolume = 0;
            for(int i = 0; i < nComp; i++){
                currVolume = currVolume + runComp[i];
            }
            for(int i = 0; i < nComp; i++){
                if(currVolume > 0)
                    relComp[i] = runComp[i] / currVolume;
                else
                    relComp[i] = 0;
            }
        }
}
