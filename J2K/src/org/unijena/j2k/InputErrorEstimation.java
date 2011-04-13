/*
 * CalcAreaWeight.java
 * Created on 23. Februar 2006, 17:15
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

package org.unijena.j2k;

import java.util.Random;
import jams.model.*;
import jams.data.*;

/**
 *
 * @author Peter Krause
 */
@JAMSComponentDescription(
        title="InputErrorEstimation",
        author="Peter Krause",
        description="Adds a stochastic error to input data"
        )
        
public class InputErrorEstimation extends JAMSComponent {
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "original data"
            )
            public JAMSDoubleArray inData;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "the biased data"
            )
            public JAMSDoubleArray outData;    
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.INIT,
            description = "maximum relative error"
            )
            public JAMSDouble maxRelError;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "sign (0 = +/-; 1 = +; -1 = -)"
            )
            public JAMSInteger sign;

    
    Random generator = new Random();
    double missingDataVal = -9999;
    
    public void run() {
        boolean pos = true;
        if(sign.getValue() == 0)
            pos = generator.nextBoolean();
        else if(sign.getValue() == 1)
            pos = true;
        else if(sign.getValue() == -1)
            pos = false;

        double[] inDat = this.inData.getValue();
        double[] outDat = new double[inDat.length];
        
        for(int i = 0; i < inDat.length; i++){
            double error = generator.nextDouble();
            error = (error * this.maxRelError.getValue());
            
            if(!pos)
                error = error * -1;
            if(inDat[i] != missingDataVal)
                outDat[i] = inDat[i] + (Math.abs(inDat[i]) * error);
            else
                outDat[i] = inDat[i];
        }
        this.outData.setValue(outDat);
    }
}
