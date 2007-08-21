/*
 * CalcRelativeHumidity.java
 * Created on 24. November 2005, 09:48
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

package org.unijena.j2k.inputData;

import org.unijena.jams.data.*;
import org.unijena.jams.model.*;

/**
 *
 * @author Peter Krause
 */
@JAMSComponentDescription(
        title="CalcAbsoluteHumidity",
        author="Peter Krause",
        description="Calculates absolute humidity of relative humidity and temperature" +
        "If temperature is not existent at the site it is regionalized"
        )
        public class CalcAbsoluteHumidity extends JAMSComponent {
    
    /*
     *  Component variables
     */
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "time"
            )
            public JAMSCalendar time;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "the relative humidity values"
            )
            public JAMSDoubleArray rhum;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "temperature for the computation"
            )
            public JAMSDoubleArray temperature;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "aboslute humidity values"
            )
            public JAMSDoubleArray ahum;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "Array of temperature station elevations"
            )
            public JAMSDoubleArray tempElevation = new JAMSDoubleArray();
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "Array of temperature station's x coordinate"
            )
            public JAMSDoubleArray tempXCoord = new JAMSDoubleArray();
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "Array of temperature station's y coordinate"
            )
            public JAMSDoubleArray tempYCoord = new JAMSDoubleArray();
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "Regression coefficients for temperature"
            )
            public JAMSDoubleArray tempRegCoeff = new JAMSDoubleArray();
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "Array of rhum station elevations"
            )
            public JAMSDoubleArray rhumElevation = new JAMSDoubleArray();
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "Array of rhum station's x coordinate"
            )
            public JAMSDoubleArray rhumXCoord = new JAMSDoubleArray();
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "Array of rhum station's y coordinate"
            )
            public JAMSDoubleArray rhumYCoord = new JAMSDoubleArray();
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "number of temperature station for IDW"
            )
            public JAMSInteger tempNIDW;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "power for IDW function"
            )
            public JAMSDouble pIDW;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "regression threshold"
            )
            public JAMSDouble regThres;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "Use caching of regionalised data?"
            )
            public JAMSBoolean dataCaching;
    
    
    /*
     *  Component run stages
     */
    
    public void init() throws JAMSEntity.NoSuchAttributeException {
        
    }
    
    public void run() throws JAMSEntity.NoSuchAttributeException {
        if(!dataCaching.getValue()){
            double[] rhum = this.rhum.getValue();
            double[] temperature = this.temperature.getValue();
            double[] ahum = new double[rhum.length];
            
            double[] rhumElev = new double[rhum.length];
            double[] rhumX = new double[rhum.length];
            double[] rhumY = new double[rhum.length];
            
            //we need arrays for the temperature stations dist weight and id
            double[][] statWeights = new double[rhum.length][temperature.length];
            
            //parameterization of rhum stations
            for(int i = 0; i < rhum.length; i++){
                rhumElev[i] = this.rhumElevation.getValue()[i];
                rhumX[i] = this.rhumXCoord.getValue()[i];
                rhumY[i] = this.rhumYCoord.getValue()[i];
                
                
                for(int n = 0; n < this.tempNIDW.getValue(); n++){
                    statWeights[i][n] = 0;
                    //statDists[i][n]   = 0;
                }
            }
            
            for(int i = 0; i < ahum.length; i++){
                //Calculating weights for nidw stations
                statWeights[i] = org.unijena.j2k.statistics.IDW.calcNidwWeights(rhumX[i], rhumY[i], this.tempXCoord.getValue(), this.tempYCoord.getValue(), this.pIDW.getValue(), this.tempNIDW.getValue());
            }
            double rsq = this.tempRegCoeff.getValue()[2];
            double grad = this.tempRegCoeff.getValue()[1];
            
            //temperature for each rain station
            double rhumTemp;
            for (int r = 0; r < ahum.length; r++) {
                rhumTemp = 0;
                for(int t = 0; t < temperature.length; t++){
                    if(rsq >= this.regThres.getValue()) {  //Elevation correction is applied
                        double deltaElev = this.rhumElevation.getValue()[r] - this.tempElevation.getValue()[t];  //Elevation difference between unit and Station
                        rhumTemp += ((deltaElev * grad + temperature[t]) * statWeights[r][t]);
                    } else{ //No elevation correction
                        rhumTemp  += (temperature[t] * statWeights[r][t]);
                    }
                }
                //calculate saturation vapour pressure
                double est = 6.11 * Math.exp((17.62*rhumTemp)/(243.12+rhumTemp));
                
                //compute maximum humidity
                double maxHum = est * 216.7 /(rhumTemp + 273.15);
              
                //compute absolute humidity
                ahum[r] = maxHum * (rhum[r] / 100.);
            }
            
            this.ahum.setValue(ahum);
        }
    }
    
    public void cleanup() throws JAMSEntity.NoSuchAttributeException {
        
    }
}
