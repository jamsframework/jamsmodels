/*
 * CalcRetentionFactors.java
 * Created on 17. July 2006, 17:15
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
package org.unijena.scn;
import org.unijena.jams.model.*;
import org.unijena.jams.data.*;

/**
 *
 * @author P. Krause
 */
@JAMSComponentDescription(
        title="Calc retention factors",
        author="Peter Krause",
        description="Calculates the retention factors"
        )
public class CalcRetentionFactors extends JAMSComponent {
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "retention factor beta"
            )
            public JAMSDouble beta;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "retention factor k1"
            )
            public JAMSDouble k1;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "retention factor k2"
            )
            public JAMSDouble k2;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "stream slope"
            )
            public JAMSDouble streamSlope;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "stream length"
            )
            public JAMSDouble streamLength;
    
    public void init() throws JAMSEntity.NoSuchAttributeException {
        
    } 
    
    public void run() throws JAMSEntity.NoSuchAttributeException {
        double lengthSlope = this.streamLength.getValue() / Math.sqrt(this.streamSlope.getValue());
        double beta = 0;
        if(lengthSlope < 10.0){
            beta = 1 - 0.02425 * Math.pow(Math.log(lengthSlope),3.2444);
        }else{
            beta = 0.1 + 3.91 / (Math.pow(lengthSlope, 0.86));
        }
        
        double k1 = 0.555 / Math.pow(lengthSlope, 0.61) + 0.511 * Math.log(lengthSlope) - 0.355;
        double k2 = 3 * Math.pow(k1, 1.3);
        
        this.beta.setValue(beta);
        this.k1.setValue(k1);
        this.k2.setValue(k2);
    }
    
}
