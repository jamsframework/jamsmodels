/*
 * CalcDailySolarRadiation.java
 * Created on 24. November 2005, 12:50
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

/*
<component class="org.unijena.j2k.CalcDailySolarRadiation" name="CalcDailySolarRadiation">
    <jamsvar name="dirName" globvar="workspaceDir"/>
    <jamsvar name="dataCaching" globvar="data_caching"/>
    <jamsvar name="time" provider="TemporalContext" providervar="current"/>
    <jamsvar name="actSlAsCf" provider="HRUContext" providervar="currentEntity.actSlAsCf"/>
    <jamsvar name="latitude" provider="HRUContext" providervar="currentEntity.latitude"/>
    <jamsvar name="actExtRad" provider="HRUContext" providervar="currentEntity.actExtRad"/>
    <jamsvar name="sunh" provider="HRUContext" providervar="currentEntity.sunh"/>
    <jamsvar name="angstrom_a" value="0.25"/>
    <jamsvar name="angstrom_b" value="0.5"/>
    <jamsvar name="solRad" provider="HRUContext" providervar="currentEntity.solRad"/>
</component>
 */
package org.unijena.j2k.radiation;

import java.io.*;
import org.unijena.jams.JAMS;
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
        public class CalcDailySolarRadiation extends JAMSComponent {
    
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
            update = JAMSVarDescription.UpdateType.RUN,
            description = "time"
            )
            public JAMSCalendar time;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "state variable sunshine hours [h/d]"
            )
            public JAMSDouble sunh;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "state variable slope aspect correction factor"
            )
            public JAMSDouble actSlAsCf;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "attribute latitude [deg]"
            )
            public JAMSDouble latitude;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "daily extraterrestic radiation [MJ/m²d]"
            )
            public JAMSDouble actExtRad;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "daily solar radiation [MJ/m²d]"
            )
            public JAMSDouble solRad;
    
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
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.INIT,
            description = "Use caching of regionalised data?"
            )
            public JAMSBoolean dataCaching; 
    
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
            getModel().sendHalt(this.getInstanceName() + ": dataCaching is true but no cache file available!");
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
            int julDay = time.get(time.DAY_OF_YEAR);
        
            double SAC = actSlAsCf.getValue();
            double lati = latitude.getValue();
            double sunsh = sunh.getValue();
            double extraterrRadiation = this.actExtRad.getValue();
            double declination = org.unijena.j2k.physicalCalculations.SolarRadiationCalculationMethods.calc_SunDeclination(julDay);
          
            double latRad = org.unijena.j2k.mathematicalCalculations.MathematicalCalculations.deg2rad(lati);
            double sunsetHourAngle = org.unijena.j2k.physicalCalculations.DailySolarRadiationCalculationMethods.calc_SunsetHourAngle(latRad, declination);
            double maximumSunshine = org.unijena.j2k.physicalCalculations.DailySolarRadiationCalculationMethods.calc_maximumSunshineHours(sunsetHourAngle);
            
            double solarRadiation = org.unijena.j2k.physicalCalculations.SolarRadiationCalculationMethods.calc_SolarRadiation(sunsh, maximumSunshine, extraterrRadiation, angstrom_a.getValue(), angstrom_b.getValue());
            //considering slope and aspect
            solarRadiation = solarRadiation * SAC;
            
            solRad.setValue(solarRadiation);
            writer.writeDouble(solarRadiation);
        } 
        else {
            solRad.setValue(reader.readDouble());
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
