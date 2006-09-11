/*
 * SCNInput.java
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
        title="SCN-Input",
        author="Peter Krause",
        description="Preliminary class for gathering all inputs need for SCN Method"
        )
public class SCNInput extends JAMSComponent {
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.INIT,
            unit = "mm",
            description = "the input precip"
            )
            public JAMSDouble inputPrecip;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            update = JAMSVarDescription.UpdateType.RUN,
            unit = "mm",
            description = "the effective precip"
            )
            public JAMSDouble effectivePrecip; 
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.INIT,
            unit = "s",
            description = "duration of precip event"
            )
            public JAMSInteger precipDuration;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.INIT,
            unit = "km",
            description = "stream length"
            )
            public JAMSDouble streamLength;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.INIT,
            unit = "m",
            description = "maximum elevation"
            )
            public JAMSDouble maxElevation;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.INIT,
            unit = "m",
            description = "minimum elevation"
            )
            public JAMSDouble minElevation;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            update = JAMSVarDescription.UpdateType.RUN,
            unit = "%",
            description = "slope of stream"
            )
            public JAMSDouble streamSlope;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "catchment CN value"
            )
            public JAMSDouble cnValue;
    
    public void init() throws JAMSEntity.NoSuchAttributeException {
                
        
    } 
    public void run() throws JAMSEntity.NoSuchAttributeException {
        double slope =(this.maxElevation.getValue() - this.minElevation.getValue()) / (this.streamLength.getValue()*1000);
        this.streamSlope.setValue(slope);
        
        //calc effective precip
        double precipBoundary = ((200 - 2 * this.cnValue.getValue()) * 25.4)/ this.cnValue.getValue();
        
        double termA = Math.pow((inputPrecip.getValue()/25.4) - (200.0/cnValue.getValue())+2.0, 2);
        double termB = (inputPrecip.getValue() / 25.4) + (800 / cnValue.getValue()) - 8.0;
        
        double effPrec = termA / termB * 25.4;
        
        if(this.inputPrecip.getValue() <= precipBoundary){
            effPrec = 0;
        }
        
        this.effectivePrecip.setValue(effPrec);
        System.out.println("input precip: " + this.inputPrecip.getValue());
        System.out.println("eff. precip: " + effPrec);
    }
    
}
