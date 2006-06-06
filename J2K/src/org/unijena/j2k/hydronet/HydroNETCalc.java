/*
 * HydroNETCalc.java
 * Created on 24. Mai 2006, 16:03
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

package org.unijena.j2k.hydronet;

import org.unijena.j2k.*;
import org.unijena.jams.data.*;
import org.unijena.jams.model.*;
import java.util.*;
import org.unijena.jams.JAMS;

/**
 *
 * @author Christian Fischer
 */
public class HydroNETCalc extends JAMSComponent {
    @JAMSComponentDescription(
        title="HydroNETCalc",
        author="Christian Fischer",
        description=""
        )    
   @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            update = JAMSVarDescription.UpdateType.INIT,
            description = "Collection of hru objects"
            )
            public JAMSEntity NitrogenOutEntity;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            update = JAMSVarDescription.UpdateType.INIT,
            description = "Collection of hru objects"
            )
            public JAMSEntity CostOutEntity;    
        
    public void run() throws JAMSEntity.NoSuchAttributeException {            
	DistNeuron NitrogenOutNeuron = (DistNeuron)NitrogenOutEntity.getObject("NEURON");
	DistNeuron CostOutNeuron = (DistNeuron)CostOutEntity.getObject("NEURON");
	
        NitrogenOutNeuron.calc();
        CostOutNeuron.calc();        	
		
	NitrogenOutNeuron.reset();
	CostOutNeuron.reset();	        
        
        //second step backpropagate        
        NitrogenOutNeuron.setDelta(-NitrogenOutNeuron.getActivation());
        CostOutNeuron.setDelta(-CostOutNeuron.getActivation());       
    }
}


