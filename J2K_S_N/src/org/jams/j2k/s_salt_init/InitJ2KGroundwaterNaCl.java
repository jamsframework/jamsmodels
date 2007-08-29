/*
 * InitJ2KNSoil.java
 * Created on 13. February 2006, 09:03
 *
 * This file is part of JAMS
 * Copyright (C) 2006 FSU Jena, Manfred Fink
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

package org.jams.j2k.s_salt_init;

import org.unijena.jams.data.*;
import org.unijena.jams.model.*;

/**
 *
 * @author Manfred Fink
 */
@JAMSComponentDescription(
        title="InitJ2KGroundwaterNaCl",
        author="Manfred Fink",
        description="intitiallizing groundwater NaCl module with two different NaCl-Pools"
        )
        public class InitJ2KGroundwaterNaCl extends JAMSComponent  {
    
    /*
     *  Component variables
     */

    
    
    
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "maximum RG1 storage"
            )
            public JAMSDouble maxRG1;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "maximum RG2 storage"
            )
            public JAMSDouble maxRG2;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "actual RG1 NaCl storage in kgNaCl"
            )
            public JAMSDouble NaClActRG1;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "actual RG2 N storage in kgNaCl"
            )
            public JAMSDouble NaClActRG2;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.INIT,
            description = "HRU Concentration in mgNaCl/l for RG1"
            )
            public JAMSDouble NaCl_concRG1;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.INIT,
            description = "HRU Concentration in mgNaCl/l for RG2"
            )
            public JAMSDouble NaCl_concRG2;
   
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.INIT,
            description = "Relativ size of the groundwaterNaCl damping tank RG1 0 - 10 to calibrate in -"
            )
            public JAMSDouble NaCl_delay_RG1;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.INIT,
            description = "Relativ size of the groundwaterNaCl damping tank RG2 0 - 10 to calibrate in -"
            )
            public JAMSDouble NaCl_delay_RG2;
    
    
    
    // constants and calibration parameter

    
    
    /*
     *  Component run stages
     */
    
    
    
 
    
    public void init() throws JAMSEntity.NoSuchAttributeException{
        
        
    }
    
    public void run() throws JAMSEntity.NoSuchAttributeException{
        
            
              
            double iNaClActRG1 = (maxRG1.getValue() * NaCl_concRG1.getValue() / 1000000) * NaCl_delay_RG1.getValue();
            double iNaClActRG2 = (maxRG2.getValue() * NaCl_concRG2.getValue() / 1000000) * NaCl_delay_RG2.getValue();
            
            NaClActRG1.setValue(iNaClActRG1);
            NaClActRG2.setValue(iNaClActRG2);
    }
    
 
    public void cleanup() throws JAMSEntity.NoSuchAttributeException{
        
    }
}
