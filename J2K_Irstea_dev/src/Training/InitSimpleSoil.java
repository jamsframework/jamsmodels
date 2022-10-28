/*
 * InitJ2KProcessLumpedSoilWaterStates.java
 * Created on 25. November 2005, 13:21
 *
 * This file is part of JAMS
 * Copyright (C) 2005 FSU Jena, c0krpe
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

package Training;

import jams.data.*;
import jams.model.*;

/**
 *
 * @author Flora Branger
 */
@JAMSComponentDescription(
        title="InitSimpleSoil",
        author="Peter Krause",
        description="Initalises the states of the SimpleSoil module.",
        version="1.0_0",
        date="2022-05-16"
        )
        public class InitSimpleSoil extends JAMSComponent {
    
    /*
     *  Component variables
     */
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "The model entity set"
            )
            public Attribute.EntityCollection entities;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "attribute area",
            unit="m^2"
            )
            public Attribute.Double area;
    
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "HRU simple soil initial level",
            unit="mm",
            lowerBound = 0
            //upperBound = 1000000
            )
            public Attribute.Double startS;  
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "state var actual reservoir level",
            unit = "L"
    )
    public Attribute.Double actS;
    
    
    /*
     *  Component run stages
     */
    
    public void init() {
        
        
    }
    
    public void run() {
        
        Attribute.Entity entity = entities.getCurrent();        
        
        this.actS.setValue(this.area.getValue() * this.startS.getValue());
        
    }
    
    public void cleanup() {
        
    }
    
    
}
