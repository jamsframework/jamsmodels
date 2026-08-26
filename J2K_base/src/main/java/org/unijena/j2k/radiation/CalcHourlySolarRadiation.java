/*
 * CalcHourlySolarRadiation.java
 * Created on 13. Januar 2006, 11:57
 *
 * This file is part of JAMS
 * Copyright (C) 2005 FSU Jena, c0krpe
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA
 *
 */
package org.unijena.j2k.radiation;

import jams.data.*;
import jams.model.*;

/**
 *
 * @author Peter Krause
 */
@JAMSComponentDescription(
        title = "HourlySolarRadiation",
        author = "Peter Krause",
        description = "Calculates hourly solar radiation from standard climatological measurements"
)
public class CalcHourlySolarRadiation extends JAMSComponent {

    /*
     *  Component variables
     */
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "slope aspect correction factor"
    )
    public Attribute.Double actSlAsCf;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "fraction of sunshine in one hour",
            unit = "h/h"
    )
    public Attribute.Double sunFrac;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Angstrom factor a",
            lowerBound = 0,
            upperBound = 1,
            defaultValue = "0.25"
    )
    public Attribute.Double angstrom_a;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Angstrom factor b",
            lowerBound = 0,
            upperBound = 1,
            defaultValue = "0.5"
    )
    public Attribute.Double angstrom_b;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "hourly solar radiation [MJ/m²]",
            unit = "MJ / m² d"
    )
    public Attribute.Double solRad;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "extraterrestic radiation [MJ/m²]",
            unit = "MJ / m² d"
    )
    public Attribute.Double actExtRad;

    /*
     *  Component run stages
     */
    public void run() {

        double maximumSunshine = org.unijena.j2k.physicalCalculations.HourlySolarRadiationCalculationMethods.calc_HourlyMaxSunshine(this.actExtRad.getValue());
        double solRadiation = org.unijena.j2k.physicalCalculations.SolarRadiationCalculationMethods.calc_SolarRadiation(sunFrac.getValue(), maximumSunshine, this.actExtRad.getValue(), angstrom_a.getValue(), angstrom_b.getValue());

        solRadiation = solRadiation * actSlAsCf.getValue();

        solRad.setValue(solRadiation);
    }
}
