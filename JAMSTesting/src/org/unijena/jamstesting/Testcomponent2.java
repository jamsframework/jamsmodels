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
import org.unijena.jams.dataaccess.entity.JAMSDoubleAccessor;
import org.unijena.jams.model.*;
import org.unijena.jams.data.*;

/**
 *
 * @author S. Kralisch
 */

public class Testcomponent2 extends JAMSComponent {
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.RUN
            )
            public JAMSDoubleArray test = new JAMSDoubleArray();
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.RUN,
            lowerBound = 0,
            upperBound = 1000,
            unit = "L/min",
            description = "This is a short description"
            )
            public JAMSDouble dValue1;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.RUN
            )
            public JAMSString str = new JAMSString();  
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.INIT
            )
            public JAMSDouble dValue2;
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.RUN
            )
            public JAMSDouble dValue3;
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.RUN
            )
            public JAMSDouble dValue4;
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.RUN
            )
            public JAMSDouble dValue5;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.RUN
            )
            public JAMSEntity entity;
    
    public void init(){
        //test.setValue(42);
        System.out.println("calibration parameter: " + dValue2);
    }
    public void run() throws Exception {
        //test.setValue(entity.getDouble("x"));
        //System.out.println("Testcomponent2: " + test);
        dValue1.setValue(dValue1.getValue()+1);
    }
    
    public void cleanup() {
        System.out.println("Testcomponent2: " + test.getValue()[0]);
        System.out.println("Testcomponent2: " + dValue1);
        System.out.println(str.getValue());
    }
    
    public static void main(String[] args) throws Exception {
        JAMSDouble d;
        JAMSEntity[] ea = new JAMSEntity[10];
        for (int i=0; i<10; i++) {
            ea[i] = JAMSDataFactory.newEntity();
            d = new JAMSDouble();
            d.setValue(i);
            ea[i].setObject("x", d);
        }
        
        JAMSComponent c = new Testcomponent2();
        JAMSDoubleAccessor da = new JAMSDoubleAccessor(ea, c, "x", "test");
        
        c.run();
    }
    
}
