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
package org.unijena.scn;
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
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.INIT,
            unit = "s",
            description = "duration of precip event"
            )
            public JAMSInteger precipDuration;
    
    /*@JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.INIT,
            unit = "mm",
            description = "effective rainfall"
            )
            public JAMSDouble effectivePrecip;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.INIT,
            description = "retention factor beta"
            )
            public JAMSDouble beta;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.INIT,
            description = "retention factor k1"
            )
            public JAMSDouble k1;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.INIT,
            description = "retention factor k2"
            )
            public JAMSDouble k2;*/
    
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
    
    
    double hNe1, hNe2, hNe;
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
        int arrayLength = (int)(this.precipDuration.getValue() / this.timeInterval.getTimeUnitCount());
       
        if(this.timeStepCounter < arrayLength){
            start = 0;
        } else{
            start++;
        }
        for(int j = start; j < this.timeStepCounter; j++){
            runoff = runoff + u1_arr[j];//((Double)this.u1.get(j)).doubleValue();
            runoff = runoff + u2_arr[j];//((Double)this.u2.get(j)).doubleValue();
        }
        double volume = (runoff * this.timeInterval.getTimeUnitCount() * 1000) / (this.catchmentArea.getValue() * 1000000);
        
        this.runoff.setValue(runoff);
        this.volume.setValue(volume);

        try{
        
        } catch (Exception e) {
            System.out.println("");
        }
        runoff = 0;
        this.timeStepCounter++;
        
    }
    
    
    
}
