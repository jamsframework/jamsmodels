/*
 * CalcAdditionalHRUAttribs.java
 * Created on 24. November 2005, 11:46
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

package org.unijena.j2k.geographicalCalculations;

import org.unijena.jams.data.*;
import org.unijena.jams.data.JAMSEntity.NoSuchAttributeException;
import org.unijena.jams.model.*;

/**
 *
 * @author Peter Krause
 */
@JAMSComponentDescription(
        title="CalcAdditionalHRUAttribs",
        author="Peter Krause",
        description="Calculates additional attributes from existent ones"
        )
        public class CalcLatLong extends JAMSComponent {
    
    /*
     *  Component variables
     */
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "The current hru entity"
            )
            public JAMSEntityCollection entities;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "entity x-coordinate"
            )
            public JAMSDouble x;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "entity y-coordinate"
            )
            public JAMSDouble y;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "entity slope"
            )
            public JAMSDouble slope;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "entity aspect"
            )
            public JAMSDouble aspect;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "entity latidute"
            )
            public JAMSDouble latitude;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "entity longitude"
            )
            public JAMSDouble longitude;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "entity slopeAspectCorrectionFactor"
            )
            public JAMSDoubleArray slAsCfArray = new JAMSDoubleArray();
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.INIT,
            description = "Projection [GK, UTMZZL]"
            )
            public JAMSString projection;
       
    /*
     *  Component run stages
     */
    
    public void init() throws JAMSEntity.NoSuchAttributeException {
        
    }
    
    public void run() throws JAMSEntity.NoSuchAttributeException{
        double[] latLong = new double[2];
        String proj;
        if(this.projection.toString() == null)
            proj = "GK";
        else
            proj = this.projection.toString();
        if(proj.equals("GK")){
            latLong = org.unijena.j2k.geographicalCalculations.GKConversion.GK2LatLon(x.getValue(), y.getValue());
        }
        else if(proj.substring(0,3).equals("UTM")){
            int len = proj.length();
            String zoneStr = proj.substring(3, len);
            latLong = org.unijena.j2k.geographicalCalculations.UTMConversion.utm2LatLong(x.getValue(), y.getValue(), zoneStr);
            
        }
        latitude.setValue(latLong[0]);
        longitude.setValue(latLong[1]);
        
        double[] sloAspCorr = new double[366];
        for(int i = 0; i < 366; i++){
            int julDay = i+1;
            sloAspCorr[i] = org.unijena.j2k.geographicalCalculations.CalcSlopeAspectCorrectionFactor.calc_slopeAspectCorrectionFactor(julDay, latitude.getValue(), slope.getValue(), aspect.getValue());
        }
        slAsCfArray.setValue(sloAspCorr);
    }
    
    public void cleanup() {
        
    }
}
