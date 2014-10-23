/*
 * Reach2ReachTransfer.java
 * Created on 23.10.2014, 12:01:26
 *
 * This file is part of JAMS
 * Copyright (C) FSU Jena
 *
 * JAMS is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * JAMS is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with JAMS. If not, see <http://www.gnu.org/licenses/>.
 *
 */

package org.unijena.j2k.routing;

import jams.data.*;
import jams.model.*;

/**
 *
 * @author Sven Kralisch
 */
@JAMSComponentDescription(
    title="Reach2ReachTransfer",
    author="Sven Kralisch",
    description="Simulation of artificial transfer of water and substances "
            + "between reaches.",
    date = "2014-10-23",
    version = "1.0_0"
)
@VersionComments(entries = {
    @VersionComments.Entry(version = "1.0_0", comment = "Initial version")
})
public class Reach2ReachTransfer extends JAMSComponent {

    /*
     *  Component attributes
     */
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Target reach"
            )
            public Attribute.Entity target;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "Components (water, substances) to be transferred"
            )
            public Attribute.Double[] values;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,    
            description = "Target reach's receiving attributes"
            )
            public Attribute.String[] inNames;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,    
            description = "Fractions of components to be transferred"
            )
            public Attribute.Double[] fractions;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,    
            description = "Remove transferred volumes from source values?",
            defaultValue= "false"
            )
            public Attribute.Boolean removeFromSource;    
                
    /*
     *  Component run stages
     */
    
    @Override
    public void init() {
    }

    @Override
    public void run() {
        
        if(!target.getValue() .isEmpty()){
            for (int i = 0; i < values.length; i++) {            
                double x = values[i].getValue() * fraction[i].getValue();
                target.setDouble(inNames[i].getValue(),target.getDouble(inNames[i].getValue()) + x);
                if (removeFromSource.getValue()) {
                    values[i].setValue(values[i].getValue() - x);
                }
            }   
        }        
        
    }

    @Override
    public void cleanup() {
    }
}