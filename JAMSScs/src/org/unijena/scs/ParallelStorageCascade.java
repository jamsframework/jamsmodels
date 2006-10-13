/*
 * ParallelStorageCascade.java
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
        title="Parallel storage cascade",
        author="Peter Krause",
        description="simulates runoff in a parallel linear storage cascade"
        )
public class ParallelStorageCascade extends JAMSComponent {
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "time"
            )
            public JAMSCalendar time;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.INIT,
            description = "timeInterval"
            )
            public JAMSTimeInterval timeInterval;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.RUN,
            unit = "km^2",
            description = "the entire area of the catchment"
            )
            public JAMSDouble catchmentArea;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            update = JAMSVarDescription.UpdateType.RUN,
            unit = "m³/s",
            description = "runoff"
            )
            public JAMSDouble runoff;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            update = JAMSVarDescription.UpdateType.RUN,
            unit = "mm",
            description = "volume"
            )
            public JAMSDouble volume;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            update = JAMSVarDescription.UpdateType.RUN,
            unit = "m³/s",
            description = "runoff_arr"
            )
            public JAMSDoubleArray runoff_arr;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            update = JAMSVarDescription.UpdateType.RUN,
            unit = "mm",
            description = "volume_arr"
            )
            public JAMSDoubleArray volume_arr;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "unit hydrograph 1"
            )
            public JAMSDoubleArray uh1;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "unit hydrograph 2"
            )
            public JAMSDoubleArray uh2;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "precip for uh1"
            )
            public JAMSDoubleArray hNe1;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "precip for uh2"
            )
            public JAMSDoubleArray hNe2;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "array length"
            )
            public JAMSInteger arrayLength;
    
    
    double[] u1_arr, u2_arr; 
    double[] vol_arr, run_arr;
    int timeStepCounter = 0;
    int start = 0;
    
    public void init() throws JAMSEntity.NoSuchAttributeException {
        this.timeStepCounter = 0;
        int timeSteps = (int)timeInterval.getNumberOfTimesteps()+1;
        vol_arr = new double[timeSteps]; 
        run_arr = new double[timeSteps];
    } 
    
    public void run() throws JAMSEntity.NoSuchAttributeException {
        //let it run until no relevant outflow occurs
    	this.u1_arr = this.uh1.getValue();
        this.u2_arr = this.uh2.getValue();
        
        //let it run until no relevant outflow occurs
        double runoff = 0;
        int arrayLength = this.arrayLength.getValue();
       
        if(this.timeStepCounter < arrayLength){
            start = 0;
        } else{
            start++;
        }
        int pIdx = this.timeStepCounter;
        
        for(int j = 0; j <= this.timeStepCounter; j++){
            runoff = runoff + u1_arr[j] * hNe1.getValue()[pIdx];
            runoff = runoff + u2_arr[j] * hNe2.getValue()[pIdx];
            
            pIdx = pIdx - 1;
        }
        double volume = (runoff * this.timeInterval.getTimeUnitCount());
        
        run_arr[this.timeStepCounter] = runoff;
        vol_arr[this.timeStepCounter] = volume;
        
        this.runoff.setValue(runoff);
        this.volume.setValue(volume);
        
        this.runoff_arr.setValue(run_arr);
        
        try{
        
        } catch (Exception e) {
            System.out.println("Error somewhere ...");
        }
        runoff = 0;
        this.timeStepCounter++;
        
    }
}
