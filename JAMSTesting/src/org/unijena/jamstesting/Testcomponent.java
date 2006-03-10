/*
 * Testcomponent.java
 *
 * Created on 8. September 2005, 16:32
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

package org.unijena.jamstesting;
import org.unijena.jams.model.*;
import org.unijena.jams.data.*;

/**
 *
 * @author S. Kralisch
 */

public class Testcomponent extends JAMSComponent {
    
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            update = JAMSVarDescription.UpdateType.RUN,
            lowerBound = 0,
            upperBound = 1000,
            unit = "km",
            description = "This is a short description"
            )
            public JAMSDoubleArray lengthVal = new JAMSDoubleArray();
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.INIT
            )
            public JAMSDouble initLength;    

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            update = JAMSVarDescription.UpdateType.RUN
            )
            public JAMSString s = new JAMSString();        
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.INIT
            )
            public JAMSEntityCollection entities;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.RUN
            )
            public JAMSEntity entity;    
    
    public void init() {
        //System.out.println(initLength);
        //lengthVal.setValue(initLength.getValue());
        double[] x = {0};
        lengthVal.setValue(x);
        s.setValue("TEST");
    }
    
    public void run(){
        lengthVal.getValue()[0] = lengthVal.getValue()[0] + 1;
        //entity.setDouble("x", length.getValue());
    }
    
    public void cleanup() {
    }
    
}
