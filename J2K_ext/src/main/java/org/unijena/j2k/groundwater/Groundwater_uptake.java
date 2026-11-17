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
        title="Groundwater_uptake",
        author="Manfred Fink",
        description="Calculation of a withdraw of water in a storage variable according to a reduction factor"
        )
        public class Groundwater_uptake extends JAMSComponent {
    
    /*
     *  Component variables
     */
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "Storage filling of HRU",
            unit = "L"
            )
            public Attribute.Double HRU_storage;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "Factor reducing storage (smaller then one)"
            )
            public Attribute.Double Reduction_factor;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "Sum of the water taken from storage",
            unit = "L"
            )
            public Attribute.Double control_amount_sum;
    
    
    
    
   
    
    /*
     *  Component run stages
     */
    
    public void init() {
        
        control_amount_sum.setValue(0.0);
        Reduction_factor.setValue(1.0);
        
    }
    
    public void run() {
        
        double run_storage = HRU_storage.getValue();
        
        if (Reduction_factor.getValue() >= 0 &&  Reduction_factor.getValue() <= 1){
        
        run_storage = run_storage * Reduction_factor.getValue();
        
        }else{
            System.out.println("reduction Factor out of range (0-1) " +  Reduction_factor.getValue());
        }
        
        control_amount_sum.setValue(control_amount_sum.getValue() + (HRU_storage.getValue() - run_storage));
        
        HRU_storage.setValue(run_storage);
        
       
    }
    
    public void cleanup() {
        
    }
    
    
    
    
}
