/*
 * HamonET.java
 * Created on 30. September 2005, 11:37
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

package gov.usgs.thornthwaite;

import org.unijena.jams.model.*;
import org.unijena.jams.data.*;
import java.io.*;
import java.util.*;
import org.jscience.physics.units.*;


/**
 *
 * @author S. Kralisch
 */
public class HamonET extends JAMSComponent {
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.RUN
            )
            public JAMSCalendar time;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.RUN
            )
            public JAMSDouble temp;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.RUN,
            unit = "ft³ / s"
            )
            public JAMSDouble daylength;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            update = JAMSVarDescription.UpdateType.RUN,
            unit = "L/s"
            )
            public JAMSDouble potET;
    
    
    // the number of days per months
    final static int[] DAYS = {
        31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31
    };

/*    
    public void init() {
        System.out.println(potET.getUnit());
        System.out.println(daylength.getUnit());
        System.out.println(potET.getUnit().isCompatible(daylength.getUnit()));
        Converter conv = daylength.getUnit().getConverterTo(potET.getUnit());
        System.out.println(conv.convert(1));
    }
*/
    
    public void run(){

        double temp          = this.temp.getValue();
        double daylength     = this.daylength.getValue();
        int month         = this.time.get(JAMSCalendar.MONTH);
        
        double Wt = 4.95 * Math.exp(0.062 * temp) / 100.;
        double D2  = (daylength / 12.0) * (daylength / 12.0);
//        double potET = 0.55 * DAYS[month] * D2 * Wt;
        double potET = 0.55 * time.getActualMaximum(time.DAY_OF_MONTH) * D2 * Wt;
        if (potET <= 0.0) 
            potET = 0.0;
        if (temp <= -1.0) 
            potET = 0.0;
        
        potET *= 25.4;
        
        this.potET.setValue(potET);
    }    
    
    public static void main(String[] args) {
        
        int a = 9;
        int b = 8;
        
        int r=a/b;
        while (r!=0) {
            
            a=b;
            b=r;
            r=a/b;
            
        }
        
        int ggt;
        while (a!=b);
        if(a>b)
            ggt = a-b;
        else
            ggt = b-a;
                    
        
        System.out.println("b: " + ggt);
        
    }
}
