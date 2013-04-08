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

 */
package org.unijena.j2k.radiation;

import java.io.*;
import jams.tools.JAMSTools;
import jams.data.*;
import jams.model.*;

/**
 *
 * @author c0krpe
 */
@JAMSComponentDescription(title = "CalcDailySolarRadiation",
author = "Peter Krause",
description = "Calculates solar radiation for daily or monthly timesteps",
version="1.0_0",
date="2011-05-30")
public class CalcDailySolarRadiation extends JAMSComponent {

    /*
     *  Component variables
     */
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    description = "time")
    public Attribute.Calendar time;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    description = "state variable sunshine hours",
    unit = "h/d")
    public Attribute.Double sunh;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.WRITE,
    description = "Maximum sunshine duration in h",
    unit = "h/d",
    defaultValue = "0")
    public Attribute.Double sunhmax;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    description = "state variable slope aspect correction factor")
    public Attribute.Double actSlAsCf;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    description = "attribute latitude",
    unit = "deg")
    public Attribute.Double latitude;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    description = "daily extraterrestic radiation",
    unit = "MJ / m² d")
    public Attribute.Double actExtRad;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.WRITE,
    description = "daily solar radiation",
    unit = "MJ / m² d")
    public Attribute.Double solRad;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    description = "Angstrom factor a",
    lowerBound = 0,
    upperBound = 1,
    defaultValue = "0.25")
    public Attribute.Double angstrom_a;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    description = "Angstrom factor b",
    lowerBound = 0,
    upperBound = 1,
    defaultValue = "0.5")
    public Attribute.Double angstrom_b;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    description = "temporal resolution [d | m]")
    public Attribute.String tempRes;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    description = "Caching configuration: 0 - write cache, 1 - use cache, 2 - caching off",
    defaultValue = "2")
    public Attribute.Integer dataCaching;

    private File cacheFile;

    transient private ObjectOutputStream writer;

    transient private ObjectInputStream reader;

    int[] monthMean = {15, 45, 74, 105, 135, 166, 196, 227, 258, 288, 319, 349};
    /*
     *  Component run stages
     */

    public void init() throws Attribute.Entity.NoSuchAttributeException, IOException {
        //first, check if cached data are available
        //cacheFile = new File(dirName.getValue() + "/$" + this.getInstanceName() + ".cache");
        cacheFile = new File(getModel().getWorkspace().getTempDirectory(), this.getInstanceName() + ".cache");
        if (!cacheFile.exists() && (dataCaching.getValue() == 1)) {
            //           getModel().getRuntime().sendHalt(this.getInstanceName() + ": dataCaching is true but no cache file available!");
        }
        if (dataCaching.getValue() == 1) {
            reader = new ObjectInputStream(new BufferedInputStream(new FileInputStream(cacheFile)));
        } else if (dataCaching.getValue() == 0) {
            writer = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(cacheFile)));
        }
    }

    public void run() throws Attribute.Entity.NoSuchAttributeException, IOException {

        if (dataCaching.getValue() == 1) {
            solRad.setValue(reader.readDouble());
        } else {
            int julDay = time.get(Attribute.Calendar.DAY_OF_YEAR);
            int month = time.get(Attribute.Calendar.MONTH);
            double SAC = actSlAsCf.getValue();
            double lati = latitude.getValue();
            double sunsh = sunh.getValue();
            double extraterrRadiation = this.actExtRad.getValue();
            double declination = 0;
            if (this.tempRes == null) {
                declination = org.unijena.j2k.physicalCalculations.SolarRadiationCalculationMethods.calc_SunDeclination(julDay);
            } else if (this.tempRes.getValue().equals("d")) {
                declination = org.unijena.j2k.physicalCalculations.SolarRadiationCalculationMethods.calc_SunDeclination(julDay);
            } else if (this.tempRes.getValue().equals("m")) {
                declination = org.unijena.j2k.physicalCalculations.SolarRadiationCalculationMethods.calc_SunDeclination(this.monthMean[month]);
            }
            double latRad = org.unijena.j2k.mathematicalCalculations.MathematicalCalculations.deg2rad(lati);
            double sunsetHourAngle = org.unijena.j2k.physicalCalculations.DailySolarRadiationCalculationMethods.calc_SunsetHourAngle(latRad, declination);
            double maximumSunshine = org.unijena.j2k.physicalCalculations.DailySolarRadiationCalculationMethods.calc_maximumSunshineHours(sunsetHourAngle);
            sunhmax.setValue(maximumSunshine);
            double solarRadiation = org.unijena.j2k.physicalCalculations.SolarRadiationCalculationMethods.calc_SolarRadiation(sunsh, maximumSunshine, extraterrRadiation, angstrom_a.getValue(), angstrom_b.getValue());
            //considering slope and aspect
            solarRadiation = solarRadiation * SAC;

            solRad.setValue(solarRadiation);

            if (dataCaching.getValue() == 0) {
                writer.writeDouble(solarRadiation);
            }
        }
    }

    public void cleanup() throws IOException {
        if (dataCaching.getValue() == 0) {
            writer.flush();
            writer.close();
        } else if (dataCaching.getValue() == 1) {
            reader.close();
        }
    }
}
