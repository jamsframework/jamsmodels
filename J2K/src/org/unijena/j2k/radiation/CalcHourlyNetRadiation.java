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
import org.unijena.jams.JAMSTools;
import org.unijena.jams.data.*;
import org.unijena.jams.model.*;

/**
 *
 * @author c0krpe
 */
@JAMSComponentDescription(
        title="Title",
        author="Author",
        description="Description"
        )
        public class CalcHourlyNetRadiation extends JAMSComponent {
    
    /*
     *  Component variables
     */
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.INIT,
            description = "Workspace directory name"
            )
            public JAMSString dirName;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.INIT,
            description = "Use caching of regionalised data?"
            )
            public JAMSBoolean dataCaching;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "fraction of sunshine in one hour"
            )
            public JAMSDouble sunFrac;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "state variable temperature"
            )
            public JAMSDouble temp;
    
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
            public JAMSDouble solRad;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "state variable solar radiation"
            )
            public JAMSDouble extRad;
    
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
    
    @JAMSVarDescription(
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
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.INIT,
            description = "calc refET with fixed values"
            )
            public JAMSBoolean refET;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "daily net radiation [MJ/m²]"
            )
            public JAMSDouble netRad;
    
    private File cacheFile;
    private boolean useCache = false;
    transient private ObjectOutputStream writer;
    transient private ObjectInputStream reader;
    
    
    /*
     *  Component run stages
     */
    
    public void init() throws JAMSEntity.NoSuchAttributeException, IOException {
        //first, check if cached data are available
        //cacheFile = new File(dirName.getValue() + "/$" + this.getInstanceName() + ".cache");
        cacheFile = new File(JAMSTools.CreateAbsoluteFileName(dirName.getValue(),"/$" + this.getInstanceName() + ".cache"));
        if (!cacheFile.exists() && dataCaching.getValue()) {
 //           getModel().getRuntime().sendHalt(this.getInstanceName() + ": dataCaching is true but no cache file available!");
        }
        if(dataCaching.getValue()){
            useCache = true;
            reader = new ObjectInputStream(new BufferedInputStream(new FileInputStream(cacheFile)));
        } else {
            useCache = false;
            writer = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(cacheFile)));
        }
        
    }
    
    public void run() throws JAMSEntity.NoSuchAttributeException, IOException{
        if (!useCache) {
            int julDay = time.get(time.DAY_OF_YEAR);
            int idx = (julDay - 1) * 24 + time.get(time.HOUR_OF_DAY);
            double extraterrRadiation = extRad.getValue();
            double elev = elevation.getValue();
            double alb;
            if(refET.getValue())
                alb = 0.23;
            else
                alb = albedo.getValue();
            
            double temperature = temp.getValue();
            double rh = rhum.getValue();
            double sRad = solRad.getValue();
            //double sunh = sunFrac.getValue();
            
            double sat_vapour_pressure = org.unijena.j2k.physicalCalculations.ClimatologicalVariables.calc_saturationVapourPressure(temperature);
            double act_vapour_pressure = org.unijena.j2k.physicalCalculations.ClimatologicalVariables.calc_vapourPressure(rh, sat_vapour_pressure);
            
            //double extraTerrestialRad = sRad /(0.25 + 0.5 * sunh);
            double clearSkyRad = org.unijena.j2k.physicalCalculations.SolarRadiationCalculationMethods.calc_ClearSkySolarRadiation(elev, extraterrRadiation);
            double netSWRadiation = org.unijena.j2k.physicalCalculations.SolarRadiationCalculationMethods.calc_NetShortwaveRadiation(alb, sRad);
            double netLWRadiation = org.unijena.j2k.physicalCalculations.HourlySolarRadiationCalculationMethods.calc_HourlyNetLongwaveRadiation(temperature, act_vapour_pressure, sRad, clearSkyRad, 0.3, false);
            
            double nRad = org.unijena.j2k.physicalCalculations.SolarRadiationCalculationMethods.calc_NetRadiation(netSWRadiation, netLWRadiation);
            
            netRad.setValue(nRad);
        } else {
            netRad.setValue(reader.readDouble());
        }
        
    }
    
    public void cleanup() throws IOException {
        if (!useCache) {
            writer.flush();
            writer.close();
        } else {
            reader.close();
        }
    }
}
