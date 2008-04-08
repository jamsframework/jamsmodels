/*
 * CalcDailyETP_PenmanMonteith.java
 * Created on 24. November 2005, 13:57
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

package org.unijena.j2k.potET;

import java.io.*;
import org.unijena.jams.data.*;
import org.unijena.jams.model.*;

/**
 *
 * @author Peter Krause
 */

    
    
@JAMSComponentDescription(
        title="CalcDailyETP_Haude",
        author="Peter Krause",
        description="Calculates daily potential ETP after Penman-Monteith"
        )
    
    public class CalcDailyETP_Haude extends JAMSComponent {
    
        
    /*
     *  Component variables
     */

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "state variable mean temperature"
            )
            public JAMSDouble tmean;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "state variable relative humidity"
            )
            public JAMSDouble rhum;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "state variable haude factor"
            )
            public JAMSDouble haudeFactor;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "attribute area"
            )
            public JAMSDouble area;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "daily potential ETP [mm/d]"
            )
            public JAMSDouble pET;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "daily actual ETP [mm/d]"
            )
            public JAMSDouble aET;
    
    
    
    /*
     *  Component run stages
     */
    
    public void init() throws JAMSEntity.NoSuchAttributeException, IOException {
        
    }
    public void run() throws JAMSEntity.NoSuchAttributeException, IOException {
        
            double tmeanVal = tmean.getValue();
            double rhumVal = rhum.getValue();
            double areaVal = area.getValue();
            double h_factor = haudeFactor.getValue();
            double est = org.unijena.j2k.physicalCalculations.ClimatologicalVariables.calc_saturationVapourPressure(tmeanVal);
            //kPa -> hPa
            est = 10 * est;
                        
            double pETP = est * (1 - (rhumVal/100.)) * h_factor; 
            
            double aETP = 0;
            
            //converting mm to litres
            pETP = pETP * areaVal;
            
            //avoiding negative potETPs
            if(pETP < 0){
                pETP = 0;
            }
            
            //conversion from daily to hourly values
            //pETP = pETP / 24;
            
            pET.setValue(pETP);
            aET.setValue(aETP);
        
        
    }
    
    public void cleanup()  throws IOException {
        
    }
}
