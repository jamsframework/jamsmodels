/*
 * RainCorrectionRichter2.java
 * Created on 8. May 2008, 09:48
 *
 * This file is part of JAMS
 * Copyright (C) 2005 FSU Jena, Peter Krause
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

import jams.data.*;
import jams.model.*;

/**
 *
 * @author Peter Krause
 */
@JAMSComponentDescription(
        title="RainCorrectionRichter2",
        author="Peter Krause",
        description="Applies correction according to RICHTER 1985 for measured daily precip sums," +
        "this module allows the consideration of the station location and shelter"
        )
        public class RainCorrectionRichter2 extends JAMSComponent {
    
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
            description = "the precip values"
            )
            public JAMSDoubleArray precip;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "temperature for the correction function"
            )
            public JAMSDoubleArray temperature;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "corrected precip values"
            )
            public JAMSDoubleArray rcorr;
    
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
            description = "Array of precip station elevations"
            )
            public JAMSDoubleArray rainElevation = new JAMSDoubleArray();
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "Array of precip station's x coordinate"
            )
            public JAMSDoubleArray rainXCoord = new JAMSDoubleArray();
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "Array of precip station's y coordinate"
            )
            public JAMSDoubleArray rainYCoord = new JAMSDoubleArray();
    
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
            access = JAMSVarDescription.AccessType.WRITE,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "snow_trs"
            )
            public JAMSDouble snow_trs;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "snow_trans"
            )
            public JAMSDouble snow_trans;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "Use caching of regionalised data?"
            )
            public JAMSBoolean dataCaching;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "Station shelter [1 - no protection; 2 - gentle protection; " +
            "3 - moderate protection; 4 - strong protection]"
            )
            public JAMSIntegerArray protection = new JAMSIntegerArray();
    
    
    /*
     *  Component run stages
     */
    
    public void init() throws JAMSEntity.NoSuchAttributeException {
        
    }
    
    public void run() throws JAMSEntity.NoSuchAttributeException {
        if(!dataCaching.getValue()){
            double[] precip = this.precip.getValue();
            double[] temperature = this.temperature.getValue();
            double[] rcorr = new double[precip.length];
            
            double[] rainElev = new double[precip.length];
            double[] rainX = new double[precip.length];
            double[] rainY = new double[precip.length];
            
            int[] protect = new int[precip.length];
            
            //we need arrays for the temperature stations dist weight and id
            double[][] statWeights = new double[precip.length][temperature.length];
            //double[][] statDists   = new double[precip.length][temperature.length];
            
            //parameterization of rain stations
            for(int i = 0; i < precip.length; i++){
                rainElev[i] = this.rainElevation.getValue()[i];
                rainX[i] = this.rainXCoord.getValue()[i];
                rainY[i] = this.rainYCoord.getValue()[i];
                protect[i] = this.protection.getValue()[i];
                
                
                for(int n = 0; n < this.tempNIDW.getValue(); n++){
                    statWeights[i][n] = 0;
                    //statDists[i][n]   = 0;
                }
            }
            
            for(int i = 0; i < rcorr.length; i++){
                //Calculating weights for nidw stations
                statWeights[i] = org.unijena.j2k.statistics.IDW.calcNidwWeights(rainX[i], rainY[i], this.tempXCoord.getValue(), this.tempYCoord.getValue(), this.pIDW.getValue(), this.tempNIDW.getValue());
            }
            double rsq = this.tempRegCoeff.getValue()[2];
            double grad = this.tempRegCoeff.getValue()[1];
            
            //temperature for each rain station
            double rainTemp;
            for (int r = 0; r < rcorr.length; r++) {
                rainTemp = 0;
                for(int t = 0; t < temperature.length; t++){
                    if(rsq >= this.regThres.getValue()) {  //Elevation correction is applied
                        double deltaElev = this.rainElevation.getValue()[r] - this.tempElevation.getValue()[t];  //Elevation difference between unit and Station
                        rainTemp += ((deltaElev * grad + temperature[t]) * statWeights[r][t]);
                    } else{ //No elevation correction
                        rainTemp  += (temperature[t] * statWeights[r][t]);
                    }
                }
                //determine rain and snow amount of precip
                double pSnow = (snow_trs.getValue() + snow_trans.getValue() - rainTemp) /
                        (2 * snow_trans.getValue());
                
                //fixing upper and lower bound for pSnow (has to be between 0 and 1
                if(pSnow > 1.0)
                    pSnow = 1.0;
                else if(pSnow < 0)
                    pSnow = 0;
                
                //dividing input precip into rain and snow
                double rain = (1 - pSnow) * precip[r];
                double snow = pSnow * precip[r];
                
                
                
                
                //Calculating relative Winderror acc to RICHTER 1995
                double as = 0,bs = 0,ls = 0;
                double ar = 0,br = 0,lr = 0;
                //coefficients according to protection
                switch(protect[r]){
                    case 1: 
                        lr = 0.642;
                        ar = 0.1801;
                        br = -0.608;
                        ls = 1.102;
                        as = 0.6774;
                        bs = -0.204;
                        break;
                        
                    case 2:
                        lr = 0.492;
                        ar = 0.1412;
                        br = -0.505;
                        ls = 0.938;
                        as = 0.5424;
                        bs = -0.211;
                        break;
                        
                    case 3:
                        lr = 0.304;
                        ar = 0.1029;
                        br = -0.553;
                        ls = 0.516;
                        as = 0.3194;
                        bs = -0.431;
                        break;
                    
                    case 4:
                        lr = 0.270;
                        ar = 0.0584;
                        br = -0.693;
                        ls = 0.326;
                        as = 0.1008;
                        bs = -0.022;
                        break;
                        
                    default:
                        System.out.println("Wrong protection type for rain station");
                        break;
                }
                double windErr = 0;
                if(snow > 0){//if(pSnow >= 1.0){      //set to all snow (5/11/01), rechanged 1.03.02
                    if(snow <= 0.1)
                        snow = snow + (snow * ls);
                    else{
                        double relSnow = as * Math.pow(snow, bs);
                        snow = snow + (snow * relSnow);
                    }
                }
                if(rain > 0){ //if(pSnow < 1.0){//
                    if(rain < 0.1)
                        rain += (rain * lr);
                    else{
                        double relRain = ar * Math.pow(rain,br);
                        rain = rain + (rain * relRain);
                    }
                }
                
                //Calculating error from evaporation and wetting acc. to Richter
                double wetErr = 0;
                if(precip[r] < 0.1)
                    wetErr = 0;
                else{
                    if(time.get(time.MONTH) >= 4 & time.get(time.MONTH) < 10){ //Summer half of the year
                        if(precip[r] >= 9.0)
                            wetErr = 0.47;
                        else
                            wetErr = 0.08 * Math.log(precip[r]) + 0.225;
                    } else{   //Winter half of the year
                        if(precip[r] >= 9.0)
                            wetErr = 0.3;
                        else
                            wetErr = 0.05 * Math.log(precip[r]) + 0.13;
                    }
                }
                
                //Calculating corrected rain_value
                rcorr[r] = rain + snow + wetErr;
            }
            
            this.rcorr.setValue(rcorr);
        }
    }
    
    public void cleanup() throws JAMSEntity.NoSuchAttributeException {
        
    }
}
