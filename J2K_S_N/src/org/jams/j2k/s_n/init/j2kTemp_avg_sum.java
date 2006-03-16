/*
 * j2kTemp_avg_sum.java
 * Created on 30. January 2005, 14:40
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

package org.jams.j2k.s_n.init;

import org.unijena.jams.data.*;
import org.unijena.jams.model.*;

/**
 *
 * @author Manfred Fink
 */
@JAMSComponentDescription(
        title="j2kTemp_avg_sum",
        author="Manfred Fink",
        description="Module for the calculation of long yearly average temperature and temperature sum"
        )
        public class j2kTemp_avg_sum extends JAMSComponent {
    
    
    
    
    /*
     *  Component variables
     */
    
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "Daily mean temperature in °C"
            )
            public JAMSDouble tmeanpre;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "mean temperature of the simulation period in °C"
            )
            public JAMSDouble tmeanavg;
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "average yearly temperature sum of the simulation period in °C"
            )
            public JAMSDouble tmeansum;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "number of current days"
            )
            public JAMSDouble I;
    
     @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "soil temperature in layerdepth in °C"
            )
            public JAMSDouble Soil_Temp_Layer;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "in °C *  Output soil surface temperature"
            )
            public JAMSDouble Surfacetemp;
    
    
    /*
     *  Component run stages
     */
    
    public void init() {
            tmeanavg.setValue(0);
            tmeansum.setValue(0);
            I.setValue(0);
            
        
        
    }
    
    public void run() throws JAMSEntity.NoSuchAttributeException{
        double tempmean = tmeanpre.getValue();
        double tempmeanavg = tmeanavg.getValue();
        double tempmeansum = tmeansum.getValue();
        double ii = I.getValue();
        
        ii++;
        tempmeanavg = ((tempmeanavg * (ii - 1)) + tempmean) / ii;
        tempmeansum = ((tempmeansum * ((ii - 1) / 365.25)) + tempmean )/ (ii / 365.25);
/*        
        if (i == 1000) {
            
          
            System.out.println("tmeanavg = " + tmeanavg +" i =  "+ i);
            System.out.println("tmeansum = " + tmeansum +" i =  "+ i);
            
            
            
        }
 */       
        tmeanavg.setValue(tempmeanavg);
        tmeansum.setValue(tempmeansum);
        Soil_Temp_Layer.setValue(tempmeanavg);
        Surfacetemp.setValue(tempmeanavg);
        I.setValue(ii);
    }
    
    public void cleanup() {
        
    }
}
