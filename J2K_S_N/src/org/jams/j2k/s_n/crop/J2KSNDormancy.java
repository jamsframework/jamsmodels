/*
 * J2KSNDormancy.java
 * Created on 24. Oktober 2006, 13:15
 *
 * This file is part of JAMS
 * Copyright (C) 2005 FSU Jena, c8fima
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

package org.jams.j2k.s_n.crop;

import jams.data.*;
import jams.model.*;

/**
 *
 * @author c8fima
 */
@JAMSComponentDescription(
        title="J2KSNDormancy",
        author="Manfred Fink",
        description="Calculates dormancy of plants under use of day length (after SWAT). Dormancy variable is also used to simulate maturity"
        )
        public class J2KSNDormancy extends JAMSComponent {
    
    /*
     *  Component variables
     */
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "Maximum sunshine duration in h"
            )
            public JAMSDouble sunhmax;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "Minimum yearly sunshine duration in h"
            )
            public JAMSDouble sunhmin;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "entity latidute"
            )
            public JAMSDouble latitude;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "indicates dormancy of plants"
            )
            public JAMSBoolean dormancy;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "Plants base growth temperature [Â°C]"
            )
            public JAMSDouble tbase;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "HRU daily mean temperature [Â°C]"
            )
            public JAMSDouble Tmean;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "Fraction of actual potential heat units sum [-]"
            )
            public JAMSDouble FPHUact;  
    
    /*
     *  Component run stages
     */
    
    public void init() {
        
    }
    
    public void run() {
        double sunhminrun = 0;
        double tdorm = 0;
        boolean rundormancy = false; 
        
        
        if (sunhmin.getValue() > 0){
         sunhminrun = sunhmin.getValue();
        } else {
         sunhminrun =  sunhmax.getValue(); 
        }
        
        sunhminrun = Math.min(sunhminrun, sunhmax.getValue());
        
        if (latitude.getValue() < 20){
            tdorm = 0;
        }else if (latitude.getValue() < 40){
            tdorm = (latitude.getValue() - 20) / 20 ;
        }else{
            tdorm = 1;
        }
        
        if (sunhmax.getValue() < (sunhminrun + tdorm)){
           rundormancy = true; 
        } else {
            
            if (Tmean.getValue() <  tbase.getValue()) {
            rundormancy = true;     
            }else{
            rundormancy = false;     
            }

            
        }
        
        if (FPHUact.getValue() > 1){
            rundormancy = true;
        }
        
        sunhmin.setValue(sunhminrun);
        
        dormancy.setValue(rundormancy);
        
        
        
    
    
    }
    
    public void cleanup() {
        
    }
}
