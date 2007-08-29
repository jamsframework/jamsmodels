/*
 * CalcHourlySolarRadiation.java
 * Created on 13. Januar 2006, 11:57
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
import org.unijena.jams.JAMS;
import org.unijena.jams.data.*;
import org.unijena.jams.model.*;

/**
 *
 * @author Peter Krause
 */
@JAMSComponentDescription(
        title="HourlySolarRadiation",
        author="Peter Krause",
        description="Calculates hourly solar radiation from standard climatological measurements"
        )
        public class CalcHourlySolarRadiation extends JAMSComponent {
    
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
            description = "latitude of entity"
            )
            public JAMSDouble latitude;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "state variable slope aspect correction factor"
            )
            public JAMSDouble actSlAsCf;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "state variable fraction of sunshine in one hour"
            )
            public JAMSDouble sunFrac;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "Current time"
            )
            public JAMSCalendar time;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.INIT,
            description = "Angstrom factor a"
            )
            public JAMSDouble angstrom_a;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.INIT,
            description = "Angstrom factor b"
            )
            public JAMSDouble angstrom_b;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "hourly solar radiation [MJ/m²]"
            )
            public JAMSDouble solRad;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "extraterrestic radiation [MJ/m²]"
            )
            public JAMSDouble actExtRad;
    
    
    
    private File cacheFile;
    private boolean useCache = false;
    private ObjectOutputStream writer;
    private ObjectInputStream reader;
    
    /*
     *  Component run stages
     */
    
    public void init() throws JAMSEntity.NoSuchAttributeException, IOException {
        //first, check if cached data are available
        cacheFile = new File(dirName.getValue() + "/$" + this.getInstanceName() + ".cache");
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
    
    public void run() throws JAMSEntity.NoSuchAttributeException, IOException {
        if (!useCache) {
            int oldjulDay = 0;
            int julDay = time.get(time.DAY_OF_YEAR);
            int idx = (julDay - 1) * 24 + time.get(time.HOUR_OF_DAY);
            
            double lat = latitude.getValue();
            double radLat = org.unijena.j2k.mathematicalCalculations.MathematicalCalculations.deg2rad(lat);
            //double longi = longitude.getValue();
            //double radLon =  org.unijena.j2k.mathematicalCalculations.MathematicalCalculations.deg2rad(longi);
            //double elev = elevation.getValue();
            //double slo = slope.getValue();
            //double asp = aspect.getValue();
            double SAC = actSlAsCf.getValue();
            double declination = 0;
            double invRelDistEarthSun = 0;
            double solarConstant = 0;
            
            double sun_frac = sunFrac.getValue();
            
            if(julDay != oldjulDay){
                //sun's declination in rad
                declination = org.unijena.j2k.physicalCalculations.SolarRadiationCalculationMethods.calc_SunDeclination(julDay);
                //inverse relative dist. Earth Sun
                invRelDistEarthSun = org.unijena.j2k.physicalCalculations.SolarRadiationCalculationMethods.calc_InverseRelativeDistanceEarthSun(julDay);
                //the solar constant MJ /m² min
                solarConstant = org.unijena.j2k.physicalCalculations.SolarRadiationCalculationMethods.calc_SolarConstant(julDay);
                oldjulDay = julDay;
            }
            double longTZ = 15;
            double maximumSunshine = org.unijena.j2k.physicalCalculations.HourlySolarRadiationCalculationMethods.calc_HourlyMaxSunshine(this.actExtRad.getValue());
            double solRadiation = org.unijena.j2k.physicalCalculations.SolarRadiationCalculationMethods.calc_SolarRadiation(sun_frac, maximumSunshine, this.actExtRad.getValue(), angstrom_a.getValue(), angstrom_b.getValue());
            
            solRadiation = solRadiation * SAC;
            
            solRad.setValue(solRadiation);
            
        } else {
            solRad.setValue(reader.readDouble());//entity.setDouble(aNameSolRad.getValue(), reader.readDouble());
        }
    }
    
    public void cleanup() throws JAMSEntity.NoSuchAttributeException, IOException {
        if (!useCache) {
            writer.flush();
            writer.close();
        } else {
            reader.close();
        }
        
    }
}
