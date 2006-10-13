/*
 * CalcUnitHydrograph.java
 * Created on 17. July 2006, 17:15
 *
 * This file is part of JAMS
 * Copyright (C) 2005 S. Kralisch and P. Krause
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
package org.unijena.scs;
import org.unijena.jams.model.*;
import org.unijena.jams.data.*;

/**
 *
 * @author P. Krause
 */
@JAMSComponentDescription(
        title="SCS-UnitHydrograph",
        author="Peter Krause",
        description="This components calculates the unit-hydrographs" +
                    "for the two storages of the SCS-method, based on the " +
                    "recession coefficients k1 and k2. The first" +
                    "unit hydrographs represent the quick runoff component" +
                    "the second one the delayed runoff component" +
                    "In addition, the effective precipitation is divided into two components" +
                    "to serve as input for the two unit hydrographs. This is done by the coefficient beta," +
                    "which has to be provided as input for this component"
        )
public class CalcUnitHydrograph extends JAMSComponent {
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.RUN,
            unit = "km^2",
            description = "the entire area of the catchment"
            )
            public JAMSDouble catchmentArea;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "timeInterval"
            )
            public JAMSTimeInterval timeInterval;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.INIT,
            unit = "s",
            description = "duration of precip event"
            )
            public JAMSDouble precipDuration;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.RUN,
            unit = "mm",
            description = "effective rainfall"
            )
            public JAMSDouble effectivePrecip;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "distribution factor beta"
            )
            public JAMSDouble beta;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "retention factor k1"
            )
            public JAMSDouble k1;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "retention factor k2"
            )
            public JAMSDouble k2;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "unit hydrograph 1"
            )
            public JAMSDoubleArray uh1;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "unit hydrograph 2"
            )
            public JAMSDoubleArray uh2;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "precip for uh1"
            )
            public JAMSDoubleArray hNe1;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "precip for uh2"
            )
            public JAMSDoubleArray hNe2;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "array length"
            )
            public JAMSInteger arrayLength;
    
    public void init() throws JAMSEntity.NoSuchAttributeException {
        
    } 
    
    public void run() throws JAMSEntity.NoSuchAttributeException {
        int nStor = 2;
                
        int timeSteps = (int)timeInterval.getNumberOfTimesteps()+1;
        
        double[] u1_arr = new double[timeSteps]; 
        double[] u2_arr = new double[timeSteps];
        double[] p1_arr = new double[timeSteps]; 
        double[] p2_arr = new double[timeSteps];
        
        int arrayLength = (int)(this.precipDuration.getValue() / this.timeInterval.getTimeUnitCount());
        double hNe = 0;
        double hNe1 = 0;
        double hNe2 = 0;
        
        int faculty = 1;
        double tInterval = (double)this.timeInterval.getTimeUnitCount() / 3600.0;
        
        double counter = 0;
        //calculate the unit hydrograph for both cascades
        int pD = (int)this.precipDuration.getValue();
        int tC = this.timeInterval.getTimeUnitCount();
        
        double restPrecip = effectivePrecip.getValue();
        
        for(int i = 0; i < timeSteps; i++){
            int ts = i+1;
            if((ts * tC) <= pD){
                hNe = effectivePrecip.getValue() / ((double)pD / (double)tC);
                hNe1 = hNe * beta.getValue();
                hNe2 = hNe * (1 - beta.getValue());
                restPrecip = restPrecip - hNe;
            }
            else{
                hNe = restPrecip;
                hNe1 = hNe * beta.getValue();
                hNe2 = hNe * (1 - beta.getValue());
                restPrecip = restPrecip - hNe;
            }
            
            u1_arr[i] = (this.catchmentArea.getValue() / 3.6) * (1/(this.k1.getValue() * faculty) * Math.pow(counter/this.k1.getValue(), nStor-1) * Math.exp(-counter/this.k1.getValue()));
            u2_arr[i] = (this.catchmentArea.getValue() / 3.6) * (1/(this.k2.getValue() * faculty) * Math.pow(counter/this.k2.getValue(), nStor-1) * Math.exp(-counter/this.k2.getValue()));
            p1_arr[i] = hNe1;
            p2_arr[i] = hNe2;
            counter = counter + tInterval;
        }
        
        this.uh1.setValue(u1_arr);
        this.uh2.setValue(u2_arr);
        this.hNe1.setValue(p1_arr);
        this.hNe2.setValue(p2_arr);
        
        this.arrayLength.setValue(arrayLength);
        
    }
    
}
