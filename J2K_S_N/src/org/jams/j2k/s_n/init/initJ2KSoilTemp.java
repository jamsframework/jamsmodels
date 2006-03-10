/*
 * initJ2KSoilTemp.java
 * Created on 23. November 2005, 16:40
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
        title="initJ2KSoilTemp",
        author="Manfred Fink",
        description="Asinghs soil temperature initial states in diffrent depths"
        )
        public class initJ2KSoilTemp extends JAMSComponent {
    
    /*
     *  Component variables
     */
    
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "mean temperature of the simulation period in °C"
            )
            public JAMSDouble tmeanavg;
    
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
    
    public void init() throws JAMSEntity.NoSuchAttributeException{
        
        
    }
    
    public void run() throws JAMSEntity.NoSuchAttributeException {
        
    
        Soil_Temp_Layer.setValue(tmeanavg.getValue());
        Surfacetemp.setValue(tmeanavg.getValue());
      
            
    }
}


