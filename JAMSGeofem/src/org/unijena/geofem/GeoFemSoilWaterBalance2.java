/*
 * GeoFemSoilWaterBalance.java
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

package org.unijena.geofem;

import org.unijena.jams.data.*;
import org.unijena.jams.model.*;

/**
 *
 * @author Peter Krause
 */
@JAMSComponentDescription(
        title="GeoFemSoilWaterBalance",
        author="Peter Krause",
        description="Calculates a simplified soil water balance for each HRU"
        )
        public class GeoFemSoilWaterBalance2 extends JAMSComponent {
    
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
            description = "HRU attribute maximum MPS"
            )
            public JAMSDouble maxMPS;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "HRU attribute maximum LPS"
            )
            public JAMSDouble maxLPS;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "HRU state var actual MPS"
            )
            public JAMSDouble actMPS;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "HRU state var actual LPS"
            )
            public JAMSDouble actLPS;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "subsurface runoff storage"
            )
            public JAMSDouble ssroStorage;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "HRU state var relative saturation of MPS"
            )
            public JAMSDouble satMPS;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "HRU state var relative saturation of LPS"
            )
            public JAMSDouble satLPS;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "surface runoff"
            )
            public JAMSDouble sro;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "sub-surface runoff"
            )
            public JAMSDouble ssro;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "groundwater recharge"
            )
            public JAMSDouble gwRecharge;
    
    /*@JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "total runoff"
            )
            public JAMSDouble totQ;*/
    
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
            update = JAMSVarDescription.UpdateType.INIT,
            description = "ssro recession factor"
            )
            public JAMSDouble k_factor;

    /*
     *  Component run stages
     */
    
    public void init() throws JAMSEntity.NoSuchAttributeException {
        
        
    }
    
    public void run() throws JAMSEntity.NoSuchAttributeException {
        //double k_factor = 1;
        //double maxExcStor = 100000.0;
    
        
        double actMPS = this.actMPS.getValue();
        double actLPS = 0;//this.actLPS.getValue();
        double inflow = this.precip.getValue() + this.snowMelt.getValue();
        double maxMPS = this.maxMPS.getValue();
        double maxLPS = this.maxLPS.getValue();
        double ssroStor = this.ssroStorage.getValue();
        
        //inflow goes into the soil
        double deltaMPS = maxMPS - actMPS;
        
        //inflow can go into fc
        if(inflow <= deltaMPS){
            actMPS = actMPS + inflow;
            inflow = 0;
        }
        else{
            actMPS = maxMPS;
            inflow = inflow - deltaMPS;
        }
        //remaining inflow goes into ac
        
        double deltaLPS = maxLPS - actLPS;
        if(inflow <= deltaLPS){
            actLPS = actLPS + inflow;
            inflow = 0;
        }
        else{
            actLPS = maxLPS;
            inflow = inflow - deltaLPS;
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
        
        double sro = inflow;
        inflow = 0;
        double gwRecharge = 0;
        //excess water is distributed to Qdir and GWrecharge
        double slope_weight = (Math.tan(this.slope.getValue() * (Math.PI / 180.))) * this.latVertDist.getValue();
        if(slope_weight > 1)
            slope_weight = 1;
        
        this.actLPS.setValue(actLPS);
        
        //latVert distribution
        double latPart = slope_weight * actLPS;
        double vertPart = (1 - slope_weight) * actLPS;
        
        ssroStor = ssroStor + latPart;
        
        double ssro = ssroStor * (1.0 / k_factor.getValue());
        ssroStor = ssroStor - ssro;
        
        gwRecharge = vertPart;
        
        //writing values back
        this.actET.setValue(actET);
        this.actMPS.setValue(actMPS);
        
        this.satMPS.setValue(actMPS / maxMPS);
        this.satLPS.setValue(actLPS/maxLPS);
        this.ssroStorage.setValue(ssroStor);
        this.gwRecharge.setValue(gwRecharge);
        this.sro.setValue(sro);
        this.ssro.setValue(ssro);
    }
    
    public void cleanup() {
        
    }
    
    
}
