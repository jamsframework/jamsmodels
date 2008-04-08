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

import org.unijena.jams.data.*;
import org.unijena.jams.model.*;

/**
 *
 * @author Peter Krause
 */
@JAMSComponentDescription(
        title="SoilWaterBalance",
        author="Peter Krause",
        description="Calculates a simplified soil water balance for each HRU"
        )
        public class SoilWaterBalance_2 extends JAMSComponent {
    
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
            description = "attribute id"
            )
            public JAMSDouble id;
    
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
            access = JAMSVarDescription.AccessType.READWRITE,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "HRU excess storage"
            )
            public JAMSDouble excStor;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "direct runoff"
            )
            public JAMSDouble dirQ;
    
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
            description = "lateral recession constant"
            )
            public JAMSDouble recConst;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.INIT,
            description = "ET reduction factor"
            )
            public JAMSDouble linETRed;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.INIT,
            description = "maximum excStor"
            )
            public JAMSDouble maxExcStor;

    /*
     *  Component run stages
     */
    
    public void init() throws JAMSEntity.NoSuchAttributeException {
        
        
    }
    
    public void run() throws JAMSEntity.NoSuchAttributeException {
        double k_factor = 1;
        double maxExcStor = 100000.0;
        if(this.recConst.getValue() != 0){
            k_factor = this.recConst.getValue();
        }
        if(this.maxExcStor.getValue() != 0){
            maxExcStor = this.maxExcStor.getValue() * this.area.getValue();
        }
        double excStor = this.excStor.getValue();
        double actMPS = this.actMPS.getValue();
        double inflow = this.precip.getValue() + this.snowMelt.getValue();
        double maxMPS = this.maxMPS.getValue();
        
        //inflow goes into the soil
        double deltaMPS = maxMPS - actMPS;
        
        if(inflow <= deltaMPS){
            actMPS = actMPS + inflow;
            inflow = 0;
        }
        else{
            actMPS = maxMPS;
            inflow = inflow - deltaMPS;
        }
        
        //et out of the soil
        double actET = this.actET.getValue();
        double potET = this.potET.getValue();
        
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
        
        //reduction function here
        double linRed = this.linETRed.getValue();
        double reduceET = 1.0;
        if(actMPS < (linRed * maxMPS)){
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
        
        //available water is put into soil
        deltaMPS = maxMPS - actMPS;
        if(inflow <= deltaMPS){
            actMPS = actMPS + inflow;
            inflow = 0;
        }
        else{
            actMPS = maxMPS;
            inflow = inflow - deltaMPS;
        }
        
        double dirQ = 0;
        double gwRecharge = 0;
        //excess water is distributed to Qdir and GWrecharge
        double slope_weight = (Math.tan(this.slope.getValue() * (Math.PI / 180.))) * this.latVertDist.getValue();
        if(slope_weight > 1)
            slope_weight = 1;
        
        excStor = excStor + (inflow * slope_weight);
        if(excStor > maxExcStor){
            dirQ = (excStor - maxExcStor);
            excStor = maxExcStor;
        }
        double interflow = excStor * (1.0 / k_factor);
        excStor = excStor - interflow;
        dirQ = dirQ + interflow;
        gwRecharge = inflow * (1 - slope_weight);
        
        //writing values back
        this.actET.setValue(actET);
        this.actMPS.setValue(actMPS);
        this.satMPS.setValue(actMPS / maxMPS);
        this.excStor.setValue(excStor);
        this.gwRecharge.setValue(gwRecharge);
        this.dirQ.setValue(dirQ);
        this.totQ.setValue(dirQ + gwRecharge);
    }
    
    public void cleanup() {
        
    }
    
    
}
