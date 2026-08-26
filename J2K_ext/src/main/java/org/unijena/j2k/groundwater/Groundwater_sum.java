/*
 * J2KProcessGroundwater.java
 * Created on 25. November 2005, 16:54
 *
 * This file is part of JAMS
 * Copyright (C) 2005 FSU Jena, c0krpe
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA
 *
 */

package org.unijena.j2k.groundwater;


import jams.data.*;
import jams.model.*;

/**
 *
 * @author c0krpe
 */
@JAMSComponentDescription(
        title="Groundwater_sum",
        author="Manfred Fink",
        description="Calculation of the Sum and the reative filling of storages"
        )
        public class Groundwater_sum extends JAMSComponent {
    
    /*
     *  Component variables
     */
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Storage filling of HRU",
            unit = "L"
            )
            public Attribute.Double HRU_storage;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Maximum storage filling of HRU",
            unit = "L"
            )
            public Attribute.Double HRU_storage_max;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "Storage filling of HRU",
            unit = "L"
            )
            public Attribute.Double Sum_storage;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "Maximum storage filling of HRU",
            unit = "L"
            )
            public Attribute.Double Sum_storage_max;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "average filling proportion"
           
            )
            public Attribute.Double GW_Filling;
    
   
    
    /*
     *  Component run stages
     */
    
    public void initAll() {
        
        
        
    }
    
    public void run() {
        
        Sum_storage_max.setValue(Sum_storage_max.getValue()+HRU_storage_max.getValue());
        
        Sum_storage.setValue(Sum_storage.getValue()+HRU_storage.getValue());
        
        GW_Filling.setValue(Sum_storage.getValue()/Sum_storage_max.getValue());
        
        
       
    }
    
    public void cleanup() {
        
    }
    
    
    
    
}
