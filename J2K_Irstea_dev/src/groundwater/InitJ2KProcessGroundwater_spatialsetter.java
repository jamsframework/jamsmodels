/*
 * InitJ2KProcessGroundwater.java
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

package groundwater;

import jams.data.*;
import jams.model.*;

/**
 *
 * @author c0krpe
 */
@JAMSComponentDescription(
        title="J2KGroundwater",
        author="Peter Krause, Nico Hachgenei",
        description="Initialises the J2KGroundwater module by multiplying the "
        + "maximum storage capacity of the two groundwater storages RG1 and RG2"
        + "by the area of the respective modelling unit to provide them with"
        + "absolute storage capacity values in litres. Secondly the actual"
        + "content of the two storages can be set to a relative amount."
        + "Modified to explicitly read RG1_max and RG2_max from HRUInit context"
        + "as they are modified by the spatial setters (for calibration of"
        + "distributed parameters). This version is only to be used for distributed"
        + "calibration of maxRG1 and max RG2 and should be placed after spatial setters",
        version="1.0_0",
        date="2024-07-18"
        )
        public class InitJ2KProcessGroundwater_spatialsetter extends JAMSComponent {
    
    /*
     *  Component variables
     */
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "The collection of model HRUs."
            )
            public Attribute.EntityCollection entities;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Maximum RG1 storage in water height [mm] to read explicitely from HRUInit context. - parameter to read",
            lowerBound = 0,
            //upperBound = infinity,
            unit="mm"
            )
            public Attribute.Double RG1_max;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Maximum RG2 storage in water height [mm] to read explicitely from HRUInit context. - parameter to read",
            lowerBound = 0,
            //upperBound = infinity,
            unit="mm"
            )
            public Attribute.Double RG2_max;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "Maximum RG1 storage (volume in L), to be set by this component. - parameter to set",
            lowerBound = 0,
            //upperBound = infinity,
            unit="L"
            )
            public Attribute.Double maxRG1;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "Maximum RG2 storage (volume in L), to be set by this component. - parameter to set",
            lowerBound = 0,
            //upperBound = infinity,
            unit="L"
            )
            public Attribute.Double maxRG2;
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "Initial RG1 storage to be set by this component (should be actRG1 attribute of HRUInit). - output(?)",
            lowerBound = 0,
            //upperBound = infinity,
            unit="L"
            )
            public Attribute.Double actRG1;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "Initial RG2 storage to be set by this component (should be actRG1 attribute of HRUInit). - output(?)",
            lowerBound = 0,
            //upperBound = infinity,
            unit="L"
            )
            public Attribute.Double actRG2;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Initial RG1 storage as fraction of max capacity. Used by this module to set inital actRG1. - parameter",
            lowerBound = 0,
            upperBound = 1.0,
            unit="n/a",
            defaultValue = "0.0"
            )
            public Attribute.Double initRG1;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Initial RG2 storage as fraction of max capacity. Used by this module to set inital actRG2. - parameter",
            lowerBound = 0,
            upperBound = 1.0,
            unit="n/a",
            defaultValue = "0.0"
            )
            public Attribute.Double initRG2;
    
    
    /*
     *  Component run stages
     */
    
    @Override
    public void init() {
        
    }
    
    @Override
    public void run() {
        
        Attribute.Entity run_entity = entities.getCurrent();
        double run_area = run_entity.getDouble("area");
//        maxRG1.setValue(entity.getDouble("RG1_max") * run_area);
//        maxRG2.setValue(entity.getDouble("RG2_max") * run_area);
        
        maxRG1.setValue(RG1_max.getValue() * run_area);
        maxRG2.setValue(RG2_max.getValue() * run_area);
        
//        getModel().getRuntime().println("HRU " + entity.getObject("ID") + ". RG1_max (from entity) : " + entity.getDouble("RG1_max") + ". RG1_max (from loop) : " + RG1_max.getValue() + ". maxRG1 : " + maxRG1.getValue() + ".");
        
        actRG1.setValue(maxRG1.getValue() * initRG1.getValue());
        actRG2.setValue(maxRG2.getValue() * initRG2.getValue());       
    }
    
    @Override
    public void cleanup() {
        
    }
    
   
}
