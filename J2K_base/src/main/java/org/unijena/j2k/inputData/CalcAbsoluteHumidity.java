/*
 * CalcRelativeHumidity.java
 * Created on 24. November 2005, 09:48
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

package org.unijena.j2k.inputData;

import jams.JAMS;
import jams.data.*;
import jams.model.*;

/**
 *
 * @author Peter Krause
 */
@JAMSComponentDescription(
title="CalcAbsoluteHumidity",
        author="Peter Krause",
        description="Calculates absolute humidity of relative humidity and temperature" +
        "at climate station location. If either rhum or temp is missing ahum will no be calculated.",
        version = "1.1_0"
        )
@VersionComments(entries = {
    @VersionComments.Entry(version = "1.0_0", comment = "Initial version"),
    @VersionComments.Entry(version = "1.1_0", comment = "Changed selection procedure for temperature station. "
            + "Now, the closest station will be used. If its distance to the rhum station is > 0, an info "
            + "message will be issued.")
})
public class CalcAbsoluteHumidity extends JAMSComponent {
    
    /*
     *  Component variables
     */
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READ,
            description = "the relative humidity values"
            )
            public Attribute.DoubleArray rhum;
    
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READ,
            description = "temperature for the computation"
            )
            public Attribute.DoubleArray temperature;
    
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.WRITE,
            description = "absolute humidity values"
            )
            public Attribute.DoubleArray ahum;
    
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.WRITE,
            description = "Array of temperature station elevations"
            )
            public Attribute.DoubleArray tempElevation;
    
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.WRITE,
            description = "Array of temperature station's x coordinate"
            )
            public Attribute.DoubleArray tempXCoord;
    
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.WRITE,
            description = "Array of temperature station's y coordinate"
            )
            public Attribute.DoubleArray tempYCoord;
    
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.WRITE,
            description = "Array of rhum station elevations"
            )
            public Attribute.DoubleArray rhumElevation;
    
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.WRITE,
            description = "Array of rhum station's x coordinate"
            )
            public Attribute.DoubleArray rhumXCoord;
    
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.WRITE,
            description = "Array of rhum station's y coordinate"
            )
            public Attribute.DoubleArray rhumYCoord;
    
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.WRITE,
            description = "rsqr for ahum stations"
            )
            public Attribute.DoubleArray regCoeffAhum;
    
    /*
     *  Component run stages
     */
    
    public void init() {
        
    }
    
    public void run() {

        double[] rhum = this.rhum.getValue();
        double[] temperature = this.temperature.getValue();
        double[] ahum = new double[rhum.length];

        double[] rhumElev = new double[rhum.length];
        double[] rhumX = new double[rhum.length];
        double[] rhumY = new double[rhum.length];

        double[] tempElev = new double[temperature.length];
        double[] tempX = new double[temperature.length];
        double[] tempY = new double[temperature.length];

        //parameterization of rhum stations
        for (int i = 0; i < rhum.length; i++) {
            rhumElev[i] = this.rhumElevation.getValue()[i];
            rhumX[i] = this.rhumXCoord.getValue()[i];
            rhumY[i] = this.rhumYCoord.getValue()[i];
        }

        //parameterization of temp stations
        for (int i = 0; i < temperature.length; i++) {
            tempElev[i] = this.tempElevation.getValue()[i];
            tempX[i] = this.tempXCoord.getValue()[i];
            tempY[i] = this.tempYCoord.getValue()[i];
        }

        //temperature for each rhum station
        double rhumTemp;
        for (int r = 0; r < ahum.length; r++) {
            if (rhum[r] > 0) {
                rhumTemp = 0;
                double absDist = -1;
                int t = 0;
                
                int minT = 0;
                double minDist = Double.MAX_VALUE;
                
                while (absDist != 0 && t < temperature.length) {
                    absDist = Math.sqrt(Math.pow(tempX[t] - rhumX[r], 2) + Math.pow(tempY[t] - rhumY[r], 2));
                    if (absDist < minDist) {
                        minDist = absDist;
                        minT = t;
                    }
                    t++;
                }
                
                if (absDist != 0) {
                    t = minT + 1;
                    getModel().getRuntime().println("Attention: using remote temperature station for ahum calculation! Distance: " + minDist + "m", JAMS.VERBOSE);
                }
                
                rhumTemp = temperature[t - 1];
                if (rhumTemp != JAMS.getMissingDataValue()) {
                    //calculate saturation vapour pressure
                    double est = 6.11 * Math.exp((17.62 * rhumTemp) / (243.12 + rhumTemp));

                    //compute maximum humidity
                    double maxHum = est * 216.7 / (rhumTemp + 273.15);

                    //compute absolute humidity
                    ahum[r] = maxHum * (rhum[r] / 100.);
                } else {
                    ahum[r] = JAMS.getMissingDataValue();
                }
            } else {
                ahum[r] = JAMS.getMissingDataValue();
            }
        }

        this.ahum.setValue(ahum);
        regCoeffAhum.setValue(org.unijena.j2k.statistics.Regression.calcLinReg(rhumElevation.getValue(), this.ahum.getValue()));
    }
    
    public void cleanup() {
        
    }
}
