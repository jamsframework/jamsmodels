/*
 * CalcDailyNetRadiation.java
 * Created on 24. November 2005, 13:32
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

package org.unijena.j2k.radiation;

import java.io.*;
//import jams.JAMS;
import jams.data.*;
import jams.model.*;

/**
 *
 * @author c0krpe
 */
@JAMSComponentDescription(
        title="Title",
        author="Author",
        description="Description"
        )
        public class CalcDailyNetRadiation_1 extends JAMSComponent {
    
    /*
     *  Component variables
     */

    /*@JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "state variable sunshine hours"
            )
            public JAMSDouble sunh;
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
            description = "state variable solar radiation"
            )
            public JAMSDouble extRad;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "state variable solar radiation"
            )
            public JAMSDouble solRad;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "state variable albedo"
            )
            public JAMSDouble albedo;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "attribute elevation"
            )
            public JAMSDouble elevation;
    
    /*@JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "attribute latitude"
            )
            public JAMSDouble latitude;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "Current time"
            )
            public JAMSCalendar time;
    */
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "daily net radiation [MJ/m²]"
            )
            public JAMSDouble netRad;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "daily shortwave radiation [MJ/m²]",
            defaultValue="0"            
            )
            public JAMSDouble swRad;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "daily longwave radiation [MJ/m²]",
            defaultValue="0"
            )
            public JAMSDouble lwRad;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "daily net radiation for refET [MJ/m²]",
            defaultValue="0"
            )
            public JAMSDouble refETNetRad;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.INIT,
            description = "Use caching of regionalised data?"
            )
            public JAMSBoolean dataCaching; 
    
 
    
    
    /*
     *  Component run stages
     */
    
    public void init() throws JAMSEntity.NoSuchAttributeException, IOException {
     
    }
    
    public void run() throws JAMSEntity.NoSuchAttributeException, IOException{
        
            double elev = elevation.getValue();
            double temp = tmean.getValue();
            double rh   = rhum.getValue();
            double sR   = solRad.getValue();
            double alb  = albedo.getValue();
            double extraTerrestialRad = extRad.getValue();
                    
            double sat_vapour_pressure = org.unijena.j2k.physicalCalculations.ClimatologicalVariables.calc_saturationVapourPressure(temp);
            double act_vapour_pressure = org.unijena.j2k.physicalCalculations.ClimatologicalVariables.calc_vapourPressure(rh, sat_vapour_pressure);
            
            double clearSkyRad = org.unijena.j2k.physicalCalculations.SolarRadiationCalculationMethods.calc_ClearSkySolarRadiation(elev, extraTerrestialRad);
            double netSWRadiation = org.unijena.j2k.physicalCalculations.SolarRadiationCalculationMethods.calc_NetShortwaveRadiation(alb, sR);
            double netRefETSWRadiation = org.unijena.j2k.physicalCalculations.SolarRadiationCalculationMethods.calc_NetShortwaveRadiation(0.23, sR);
            double netLWRadiation = org.unijena.j2k.physicalCalculations.DailySolarRadiationCalculationMethods.calc_DailyNetLongwaveRadiation(temp, act_vapour_pressure, sR, clearSkyRad, false);
            
            double nR_norm = org.unijena.j2k.physicalCalculations.SolarRadiationCalculationMethods.calc_NetRadiation(netSWRadiation, netLWRadiation);
            double nR_refET = org.unijena.j2k.physicalCalculations.SolarRadiationCalculationMethods.calc_NetRadiation(netRefETSWRadiation, netLWRadiation);
            netRad.setValue(nR_norm);
            refETNetRad.setValue(nR_refET);
            this.swRad.setValue(netSWRadiation);
            this.lwRad.setValue(netLWRadiation);
            
       
        
    }
    
    public void cleanup() throws IOException {

    }
}
