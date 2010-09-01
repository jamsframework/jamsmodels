/*
 * NashCascade.java
 * Created on 20. June 2007, 16:54
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

package org.unijena.j2000g;

import jams.data.*;
import jams.model.*;

/**
 *
 * @author c0krpe
 */
@JAMSComponentDescription(
title="NashCascade",
        author="Peter Krause",
        description="Description"
        )
        public class NashCascade extends JAMSComponent {
    
    /*
     *  Component variables
     */
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.INIT,
            description = "number of tanks"
            )
            public JAMSInteger nTanks;
    
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.INIT,
            description = "recision coefficient k for all tanks"
            )
            public JAMSDouble k;
    
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "input to the first tank in the cascade"
            )
            public JAMSDouble input;
    
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READWRITE,
            update = JAMSVarDescription.UpdateType.INIT,
            description = "the tanks itself"
            )
            public JAMSDoubleArray storages;
    
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.WRITE,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "outflow from the last tank in the cascade"
            )
            public JAMSDouble output;
    
    
    /*
     *  Component run stages
     */
    
    public void init() throws JAMSEntity.NoSuchAttributeException {
        //build the cascade and initalize all tanks with a value of zero
        double[] tanks = new double[this.nTanks.getValue()];
        for(int i = 0; i < this.nTanks.getValue(); i++)
            tanks[i] = 0;
        //this.storages = JAMSDataFactory.createDoubleArray();
        this.storages.setValue(tanks);
    }
    
    public void run() throws JAMSEntity.NoSuchAttributeException {
        double[] tanks = this.storages.getValue();
        double[] outflow = new double[tanks.length];
        double rec = this.k.getValue();
        //add input to first tank
        tanks[0] = tanks[0] + this.input.getValue();
        for(int i = 0; i < this.nTanks.getValue(); i++){
            //outflow of each tank
            outflow[i] = 1 / rec * tanks[i];
            tanks[i] = tanks[i] - outflow[i];
            //adding outflow to next tank in cascade
            if(i < this.nTanks.getValue() - 1){
                tanks[i+1] = tanks[i+1] + outflow[i];
            }
        }
        //outflow of last tank in cascade
        this.output.setValue(outflow[this.nTanks.getValue()-1]);
        //saving storages
        this.storages.setValue(tanks);
        //System.out.println("in: " + this.input.getValue() + ", out: " + this.output.getValue());
        
    }
    
    public void cleanup() {
        
    }
    
    
    
    
}
