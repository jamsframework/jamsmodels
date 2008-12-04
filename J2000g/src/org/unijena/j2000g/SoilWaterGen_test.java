/*
 * SoilWaterBalance.java
 * Created on 25. October 2006, 13:21
 *
 * This file is part of JAMS
 * Copyright (C) 2005 FSU Jena, c0krpe
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

package org.unijena.j2000g;

import jams.data.*;
import jams.model.*;

/**
 *
 * @author Peter Krause
 */
@JAMSComponentDescription(
        title="SoilWaterBalance",
        author="Peter Krause",
        description="Calculates a simplified soil water balance for each HRU"
        )
        public class SoilWaterGen_test extends JAMSComponent {
    
    /*
     *  Component variables
     */
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "attribute slope"
            )
            public JAMSDouble slope;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "attribute area"
            )
            public JAMSDouble area;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "HRU attribute maximum MPS"
            )
            public JAMSDouble maxMPS;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "HRU state var actual MPS"
            )
            public JAMSDouble actMPS;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "HRU state var relative saturation of MPS"
            )
            public JAMSDouble satMPS;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "surface runoff"
            )
            public JAMSDouble surfaceQ;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "subsurface runoff"
            )
            public JAMSDouble subsurfaceQ;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "groundwater recharge"
            )
            public JAMSDouble gwRecharge;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "total runoff"
            )
            public JAMSDouble totQ;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "potential ET"
            )
            public JAMSDouble potET;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "actual ET"
            )
            public JAMSDouble actET;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "precipitation"
            )
            public JAMSDouble precip;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "snow melt"
            )
            public JAMSDouble snowMelt;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.INIT,
            description = "lateral-vertical distribution coefficient"
            )
            public JAMSDouble latVertDist;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.INIT,
            description = "ET reduction factor"
            )
            public JAMSDouble linETRed;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "HRU attribute maximum percolation"
            )
            public JAMSDouble maxPerc;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.INIT,
            description = "direct runoff coefficient beta"
            )
            public JAMSDouble df_beta;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.INIT,
            description = "direct runoff coefficient a"
            )
            public JAMSDouble df_a;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.INIT,
            description = "interflow coefficient beta"
            )
            public JAMSDouble if_beta;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.INIT,
            description = "interflow coefficient a"
            )
            public JAMSDouble if_a;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.INIT,
            description = "pet multiplier"
            )
            public JAMSDouble mPET;
    

    /*
     *  Component run stages
     */
    
    public void init() throws JAMSEntity.NoSuchAttributeException {
        
        
    }
    
    public void run() throws JAMSEntity.NoSuchAttributeException {
        
        double actMPS = this.actMPS.getValue();
        double inflow = this.precip.getValue() + this.snowMelt.getValue();
        double maxMPS = this.maxMPS.getValue();
        
        //****Balance check****
        double balIn = inflow;
        double balMPSstart = actMPS;
        
        //System.out.println("***IN***");
        //System.out.println("P: " + this.precip.getValue());
        //System.out.println("SM: " + this.snowMelt.getValue());
          
        
        //et out of the soil
        double actET = this.actET.getValue();
        double potET = this.potET.getValue() * this.mPET.getValue();
        this.potET.setValue(potET);
        
        double deltaET = potET - actET;
        
        //excess water is used first
        if(inflow >= deltaET){
            actET = potET;
            inflow = inflow - deltaET;
            deltaET = 0;
        }
        else{
            actET = actET + inflow;
            inflow = 0;
            deltaET = potET - actET;
        }
        //soilwater storage is used next
        
        //evapotranspiration
        //reduction function here
        double linRed = this.linETRed.getValue();
        double reduceET = 1.0;
        if(actMPS < (linRed * maxMPS)){
            if(maxMPS == 0)
                reduceET = 0;
            else
                reduceET = actMPS / (linRed * maxMPS);
        }
        deltaET = deltaET * reduceET;
        
        if(actMPS >= deltaET){
            actET = actET + deltaET;
            actMPS = actMPS - deltaET;
            deltaET = 0;
        }
        else{
            actET = actET + actMPS;
            actMPS = 0;
        }
        
        //***Balance check***
        double balET = actET;
        
        //subtraction of surface runoff; same approach as in HBV
        double directFlow = 0;
        double ifbf = 0;
        double fact_df = 0;
        double fact_if = 0;
        if(this.df_beta.getValue() != 0){
            double sat = 0;
            if(maxMPS > 0)
                sat = actMPS / maxMPS;
            //double fact = Math.pow(sat, this.df_beta.getValue());
            //double fact = df_beta.getValue() * Math.log(sat) + 0.1;
            double b = df_beta.getValue();
            double a = df_a.getValue();
            fact_df = a * Math.pow(sat, b);
            if(fact_df < 0)
                fact_df = 0;
            
            fact_if = if_a.getValue() * Math.pow(sat, if_beta.getValue());
            if(fact_if < 0)
                fact_if = 0;
            //System.out.println("fact: " + fact + "sat: " + sat);
            directFlow = fact_df * inflow;
            inflow = inflow - directFlow;
            
            ifbf = fact_if * inflow;
            inflow = inflow - ifbf;
            
        }
        //System.out.println("DF: " + directFlow);
        
        
        double interflow = 0;
        double gwRecharge = 0;
         
        
        //double interflow = 0;
        //excess water is distributed to Qdir and GWrecharge
        double slope_weight = (Math.tan(this.slope.getValue() * (Math.PI / 180.))) * this.latVertDist.getValue();
        if(slope_weight > 1)
            slope_weight = 1;
        
        interflow = (ifbf * slope_weight);
        gwRecharge = ifbf * (1 - slope_weight);
        
        //System.out.println("INF: " + interflow);
        //System.out.println("GWR: " + gwRecharge);
        
        //cross checking against maximum percolation additional perc is interflow
       if(gwRecharge > maxPerc.getValue()){
            double delta = gwRecharge - maxPerc.getValue();
            interflow = interflow + delta;
            gwRecharge = maxPerc.getValue();
        }
        
        //inflow goes into the soil
        double deltaMPS = maxMPS - actMPS;
        
        //remaining water is put into soil
        deltaMPS = maxMPS - actMPS;
        if(inflow <= deltaMPS){
            actMPS = actMPS + inflow;
            inflow = 0;
        }
        else{
            actMPS = maxMPS;
            inflow = inflow - deltaMPS;
        }
        
        //any excess is added to interflow
        interflow = interflow + inflow;
        
        double balFlows = directFlow + interflow + gwRecharge;
        double balMPSend = actMPS;
        
        double totBal = balIn - balET - balFlows + (balMPSstart - balMPSend);
        
        if(Math.abs(totBal) > 0.001){
            System.out.println("Balance error: "+ totBal);
        }
        double sat = 0;
        if(maxMPS > 0)
            sat = (actMPS / maxMPS);
        
        //writing values back
        this.actET.setValue(actET);
        this.actMPS.setValue(actMPS);
        this.satMPS.setValue(sat);
        this.surfaceQ.setValue(directFlow);
        this.subsurfaceQ.setValue(interflow);
        this.gwRecharge.setValue(gwRecharge);
        this.totQ.setValue(directFlow + interflow + gwRecharge);
    }
    
    public void cleanup() {
        
    }
    
    
}
