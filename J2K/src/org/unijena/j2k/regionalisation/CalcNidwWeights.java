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

import jams.data.*;
import jams.model.*;

/**
 *
 * @author Peter Krause
 */
@JAMSComponentDescription(
        title="CalcNidwWeights",
        author="Peter Krause",
        description="Calculates weights for the regionalisation procedure"
        )
        public class CalcNidwWeights extends JAMSComponent {
    
    /*
     *  Component variables
     */
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "entity x-coordinate"
            )
            public Attribute.Double entityX;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "entity y-coordinate"
            )
            public Attribute.Double entityY;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "Array of station's x coordinates"
            )
            public Attribute.DoubleArray statX;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "Array of station's y coordinates"
            )
            public Attribute.DoubleArray statY;
    
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
            public Attribute.Double pidw;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "weights for IDW part of regionalisation"
            )
            public Attribute.DoubleArray statWeights;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.INIT,
            description = "Doug Boyle's famous function"
            )
            public JAMSBoolean equalWeights;  
    
    /*
     *  Component run stages
     */
    
    public void init() throws JAMSEntity.NoSuchAttributeException{
        
    }
    
    public void run() throws JAMSEntity.NoSuchAttributeException{
        Attribute.DoubleArray idwWeights = getModel().getRuntime().getDataFactory().createDoubleArray();
        if(equalWeights == null || !equalWeights.getValue()){
        	idwWeights.setValue(org.unijena.j2k.statistics.IDW.calcNidwWeights(entityX.getValue(), entityY.getValue(), statX.getValue(), statY.getValue(), pidw.getValue(), nidw.getValue()));
        }
        else if(equalWeights.getValue()){
        	idwWeights.setValue(org.unijena.j2k.statistics.IDW.equalWeights(nidw.getValue()));
        }
        	
        statWeights.setValue(idwWeights.getValue());
        
    }
    
    public void cleanup() throws JAMSEntity.NoSuchAttributeException{
        int nstat = statWeights.getValue().length;
        double[] sw = new double[nstat];
        for(int i = 0; i < nstat; i++)
            sw[i] = 0;
        
        statWeights.setValue(sw);
        
    }
}
