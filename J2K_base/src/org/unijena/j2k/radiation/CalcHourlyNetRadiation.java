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
import jams.data.*;
import jams.model.*;

/**
 *
 * @author c0krpe
 */
@JAMSComponentDescription(
        title = "CalcHourlyNetRadiation",
        author = "Peter Krause",
        description = "Calculates the hourly net radiation"
)
public class CalcHourlyNetRadiation extends JAMSComponent {

    /*
     *  Component variables
     */
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "state variable temperature",
            unit = "°C"
    )
    public Attribute.Double temp;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "state variable relative humidity",
            unit = "%"
    )
    public Attribute.Double rhum;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "state variable solar radiation",
            unit = "MJ m^-2 d^-1"
    )
    public Attribute.Double solRad;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "state variable solar radiation",
            unit = "MJ m^-2 d^-1"
    )
    public Attribute.Double extRad;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "state variable albedo"
    )
    public Attribute.Double albedo;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "attribute elevation",
            unit = "m"
    )
    public Attribute.Double elevation;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "calc refET with fixed albedo values",
            defaultValue = "false"
    )
    public Attribute.Boolean refET;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "daily net radiation",
            unit = "MJ m^-2 d^-1"
    )
    public Attribute.Double netRad;

    /*
     *  Component run stages
     */
    public void run() {
        double extraterrRadiation = extRad.getValue();
        double elev = elevation.getValue();
        double alb;
        if (refET.getValue()) {
            alb = 0.23;
        } else {
            alb = albedo.getValue();
        }

        double temperature = temp.getValue();
        double rh = rhum.getValue();
        double sRad = solRad.getValue();

        double sat_vapour_pressure = org.unijena.j2k.physicalCalculations.ClimatologicalVariables.calc_saturationVapourPressure(temperature);
        double act_vapour_pressure = org.unijena.j2k.physicalCalculations.ClimatologicalVariables.calc_vapourPressure(rh, sat_vapour_pressure);

        //double extraTerrestialRad = sRad /(0.25 + 0.5 * sunh);
        double clearSkyRad = org.unijena.j2k.physicalCalculations.SolarRadiationCalculationMethods.calc_ClearSkySolarRadiation(elev, extraterrRadiation);
        double netSWRadiation = org.unijena.j2k.physicalCalculations.SolarRadiationCalculationMethods.calc_NetShortwaveRadiation(alb, sRad);
        double netLWRadiation = org.unijena.j2k.physicalCalculations.HourlySolarRadiationCalculationMethods.calc_HourlyNetLongwaveRadiation(temperature, act_vapour_pressure, sRad, clearSkyRad, 0.3, false);

        double nRad = org.unijena.j2k.physicalCalculations.SolarRadiationCalculationMethods.calc_NetRadiation(netSWRadiation, netLWRadiation);

        netRad.setValue(nRad);

    }

}
