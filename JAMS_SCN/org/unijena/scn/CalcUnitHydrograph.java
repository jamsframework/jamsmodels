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
package org.unijena.scn;
import org.unijena.jams.model.*;
import org.unijena.jams.data.*;

/**
 *
 * @author P. Krause
 */
@JAMSComponentDescription(
        title="CN-SoilParameters",
        author="Peter Krause",
        description="Preliminary class for estimation of soil CN values"
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
            update = JAMSVarDescription.UpdateType.RUN,
            unit = "s",
            description = "duration of precip event"
            )
            public JAMSInteger precipDuration;
    
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
            description = "retention factor beta"
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
    
    public void init() throws JAMSEntity.NoSuchAttributeException {
        
    } 
    
    public void run() throws JAMSEntity.NoSuchAttributeException {
        int nStor = 2;
                
        int timeSteps = (int)timeInterval.getNumberOfTimesteps()+1;
        
        double[] u1_arr = new double[timeSteps]; 
        double[] u2_arr = new double[timeSteps];
        
        System.out.println("timeSteps: " + timeSteps);
        System.out.println("timeSize: " + this.timeInterval.getTimeUnitCount());
        
        int arrayLength = (int)(this.precipDuration.getValue() / this.timeInterval.getTimeUnitCount());
        double hNe = effectivePrecip.getValue() / arrayLength;
        double hNe1 = hNe * beta.getValue();
        double hNe2 = hNe * (1 - beta.getValue());
        
        int faculty = 1;
        double tInterval = (double)this.timeInterval.getTimeUnitCount() / (double)this.precipDuration.getValue();
        
        double counter = 0;
        //calculate the unit hydrograph for both cascades
        for(int i = 0; i < timeSteps; i++){
            u1_arr[i] = hNe1 * (this.catchmentArea.getValue() / 3.6) * (1/(this.k1.getValue() * faculty) * Math.pow(counter/this.k1.getValue(), nStor-1) * Math.exp(-counter/this.k1.getValue()));
            u2_arr[i] = hNe2 * (this.catchmentArea.getValue() / 3.6) * (1/(this.k2.getValue() * faculty) * Math.pow(counter/this.k2.getValue(), nStor-1) * Math.exp(-counter/this.k2.getValue()));
            //System.out.println(u1_arr[i]);
            counter = counter + tInterval;
        }
        
        this.uh1.setValue(u1_arr);
        this.uh2.setValue(u2_arr);
    }
    
}
