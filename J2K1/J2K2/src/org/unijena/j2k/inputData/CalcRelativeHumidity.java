/*
 * CalcRelativeHumidity.java
 * Created on 24. November 2005, 09:48
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

package org.unijena.j2k.inputData;

import org.unijena.jams.data.*;
import org.unijena.jams.model.*;

/**
 *
 * @author Peter Krause
 */
@JAMSComponentDescription(
        title="CalcRelativeHumidity",
        author="Peter Krause",
        description="Calculates relative humidity from temperature and absolute humidity"
        )
        public class CalcRelativeHumidity extends JAMSComponent {
    
    /*
     *  Component variables
     */
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "state variable mean tempeature"
            )
            public JAMSDouble tmean;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "state variable absolute humidity"
            )
            public JAMSDouble ahum;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "state variable relative humidity"
            )
            public JAMSDouble rhum;
    
    
    /*
     *  Component run stages
     */
    
    public void init() throws JAMSEntity.NoSuchAttributeException {
        
    }
    
    public void run() throws JAMSEntity.NoSuchAttributeException {
        
        double maxHum = org.unijena.j2k.physicalCalculations.ClimatologicalVariables.calc_maxHum(tmean.getValue());
        double rh = (ahum.getValue() / maxHum) * 100;
        
        //rhum should not be larger than 100%
        if(rh > 100)
            rh = 100;
        
        rhum.setValue(rh);
    }
    
    public void cleanup() throws JAMSEntity.NoSuchAttributeException {
        
    }
}
