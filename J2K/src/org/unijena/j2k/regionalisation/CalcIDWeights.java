/*
 * CalcNidwWeights.java
 * Created on 27. Januar 2006, 09:50
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

package org.unijena.j2k.regionalisation;

import org.unijena.jams.data.*;
import org.unijena.jams.model.*;

/**
 *
 * @author Peter Krause
 */
@JAMSComponentDescription(
        title="CalcIDWeights",
        author="Peter Krause",
        description="Calculates inverse distance weights for the regionalisation procedure"
        )
        public class CalcIDWeights extends JAMSComponent {
    
    /*
     *  Component variables
     */
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "entity x-coordinate"
            )
            public JAMSDouble entityX;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "entity y-coordinate"
            )
            public JAMSDouble entityY;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "Array of station's x coordinates"
            )
            public JAMSDoubleArray statX;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "Array of station's y coordinates"
            )
            public JAMSDoubleArray statY;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.INIT,
            description = "Number of IDW stations"
            )
            public JAMSInteger nidw;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.INIT,
            description = "Power of IDW function"
            )
            public JAMSDouble pidw;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "weights for IDW part of regionalisation"
            )
            public JAMSDoubleArray statIDWeights = new JAMSDoubleArray();
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "position array to determine best weights"
            )
            public JAMSIntegerArray wArray = new JAMSIntegerArray();
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.INIT,
            description = "Doug Boyle's famous function"
            )
            public JAMSBoolean equalWeights; 
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.INIT,
            description = "Calculation with geographical coordinates LL"
            )
            public JAMSBoolean latLong; 
    
    /*
     *  Component run stages
     */
    
    public void init() throws JAMSEntity.NoSuchAttributeException{
        
    }
    
    public void run() throws JAMSEntity.NoSuchAttributeException{
        JAMSDoubleArray idwWeights = new JAMSDoubleArray();
        JAMSIntegerArray wA = new JAMSIntegerArray();
        double[] dist = null;
        if(equalWeights == null || !equalWeights.getValue()){
            if(latLong == null || !latLong.getValue()){
                dist = org.unijena.j2k.statistics.IDW.calcDistances(entityX.getValue(), entityY.getValue(), statX.getValue(), statY.getValue(), pidw.getValue());
            }
            else{
                dist = org.unijena.j2k.statistics.IDW.calcLatLongDistances(entityX.getValue(), entityY.getValue(), statX.getValue(), statY.getValue(), pidw.getValue());
            }
            idwWeights.setValue(org.unijena.j2k.statistics.IDW.calcWeights(dist));
            wA.setValue(org.unijena.j2k.statistics.IDW.computeWeightArray(idwWeights.getValue()));
            
        }
        else if(equalWeights.getValue()){
            int nstat = this.statX.getValue().length;
            idwWeights.setValue(org.unijena.j2k.statistics.IDW.equalWeights(nstat));
            int[] tmp = new int[nstat];
            for(int i = 0; i < nstat; i++)
                tmp[i] = i;
            wA.setValue(tmp);
        }
        	
        statIDWeights.setValue(idwWeights.getValue());
        wArray.setValue(wA.getValue());
    }
    
    public void cleanup() throws JAMSEntity.NoSuchAttributeException{
        int nstat = statIDWeights.getValue().length;
        double[] sw = new double[nstat];
        for(int i = 0; i < nstat; i++)
            sw[i] = 0;
        
        statIDWeights.setValue(sw);
        
    }
}
