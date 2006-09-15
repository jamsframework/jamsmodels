/*
 * CalcAdditionalHRUAttribs.java
 * Created on 24. November 2005, 11:46
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
<component class="org.unijena.j2k.CalcExtraterrRadiation" name="ExtRad">
    <jamsvar name="latitude" provider="InitHRUContext" providervar="currentEntity.latitude"/>
    <jamsvar name="longitude" provider="InitHRUContext" providervar="currentEntity.longitude"/>
    <jamsvar name="longTZ" value="15"/>
    <jamsvar name="locGrw" value="e"/>
    <jamsvar name="tempRes" value="h"/>
    <jamsvar name="extRadArray" provider="InitHRUContext" providervar="currentEntity.extRadArray"/>
 </component>
 */

package org.unijena.j2k.radiation;

import org.unijena.jams.data.*;
import org.unijena.jams.data.JAMSEntity.NoSuchAttributeException;
import org.unijena.jams.model.*;

/**
 *
 * @author Peter Krause
 */
@JAMSComponentDescription(
        title="CalcAdditionalHRUAttribs",
        author="Peter Krause",
        description="Calculates additional attributes from existent ones"
        )
        public class CalcDailyExtraterrRadiation extends JAMSComponent {
    
    /*
     *  Component variables
     */
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.RUN
            )
            public JAMSCalendar time;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "entity latidute [deg]"
            )
            public JAMSDouble latitude;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "extraterrestric radiation [MJ/m²d]"
            )
            public JAMSDouble extRad;
    
    
    /*
     *  Component run stages
     */
    
    public void init() throws JAMSEntity.NoSuchAttributeException {
        
    }
    
    public void run() throws JAMSEntity.NoSuchAttributeException{
    	System.out.println("run in extra rad");
        double extRadiation = 0;
        double lati = this.latitude.getValue();
        double latRad = org.unijena.j2k.mathematicalCalculations.MathematicalCalculations.deg2rad(lati);
        int julDay = time.get(time.DAY_OF_YEAR);
        double declination = org.unijena.j2k.physicalCalculations.SolarRadiationCalculationMethods.calc_SunDeclination(julDay);
        double solarConstant = org.unijena.j2k.physicalCalculations.SolarRadiationCalculationMethods.calc_SolarConstant(julDay);
        double invRelDistEarthSun = org.unijena.j2k.physicalCalculations.SolarRadiationCalculationMethods.calc_InverseRelativeDistanceEarthSun(julDay);
        double sunsetHourAngle = org.unijena.j2k.physicalCalculations.DailySolarRadiationCalculationMethods.calc_SunsetHourAngle(latRad, declination);
        extRadiation = org.unijena.j2k.physicalCalculations.DailySolarRadiationCalculationMethods.calc_DailyExtraterrestrialRadiation(solarConstant,
                        invRelDistEarthSun, sunsetHourAngle, latRad, declination);
        this.extRad.setValue(extRadiation);
    }
    
    public void cleanup() {
        
    }
}
