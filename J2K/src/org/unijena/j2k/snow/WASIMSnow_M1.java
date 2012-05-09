/*
 * WASIMProcessSnow.java
 * Created on 17. August 2006, 17:33
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

package org.unijena.j2k.snow;

import jams.data.*;
import jams.model.*;

/**
 *
 * @author Peter Krause
 */
@JAMSComponentDescription(
title="WASIMProcessSnow",
        author="Joerg Schulla, implementation and minor adaptations Peter Krause",
        description="Adaptation of the WASIM module for snow calculation"
        )
        public class WASIMSnow_M1 extends JAMSComponent {
    
    /*
     *  Component variables
     */
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "time"
            )
            public JAMSCalendar time;
    
//Entity attributes
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "Entity area",
            unit = "m²"
            )
            public JAMSDouble area;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "Entity slope aspect correction factor"
            )
            public JAMSDouble sloAspCorrFactor;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "Entity latitude"
            )
            public JAMSDouble latitude;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "Entity landuse albedo"
            )
            public JAMSDouble landAlbedo;
    
    //state variables
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "Snow water equivalent",
            unit = "mm"
            )
            public JAMSDouble snowWaterEquivalent;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "liquid water in snowpack",
            unit = "mm"
            )
            public JAMSDouble freeWater;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "frozen water in snowpack",
            unit = "mm"
            )
            public JAMSDouble iceContent;
    
    /*@JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "radiation melt factor",
            unit = "mm / C*day"
            )
            public JAMSDouble radiationMeltFactor;*/
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "the snow melt"
            )
            public JAMSDouble snowMelt;
    
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READWRITE,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "the albedo of the entity"
            )
            public JAMSDouble snowAlbedo;
    
    /*@JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "actual evapotranspiration"
            )
            public JAMSDouble aET;*/
    
    //calibration parameters
       
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.INIT,
            description = "melt temperature: temperature at" +
            "which snow melt will start",
            unit = "C"
            )
            public JAMSDouble meltTemp;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.INIT,
            description = "factor c0 for potential melt rate"
            )
            public JAMSDouble c0Factor;
    
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.INIT,
            description = "Free-water holding capacity (decimal percent) of " +
            "snowpack expressed as decimal fraction of total " +
            "snowpack water equivalent"
            )
            public JAMSDouble freeWaterCapacity;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.INIT,
            description = "constant albedo of snow"
            )
            public JAMSDouble snowConstAlbedo;
    
    //driving data
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READWRITE,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "rain amount"
            )
            public JAMSDouble rain;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "snow amount"
            )
            public JAMSDouble snow;
    
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "precip amount"
            )
            public JAMSDouble precip;
    
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "minum temperature",
            unit = "C"
            )
            public JAMSDouble tmin;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "mean temperature",
            unit = "C"
            )
            public JAMSDouble tmean;
    
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "max temperature",
            unit = "C"
            )
            public JAMSDouble tmax;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.INIT,
            description = "module active"
            )
            public JAMSBoolean active;
    
    
    //run variables
    double runRain, runSnow, runIceContent, runFreeWater, runSnowWaterEquivalent, runMeltTemperature;
    double runRadiationMeltFactor, runWind, runPrecip, runSnowMelt, runSnowAlbedo, runAET;
    
    /*
     *  Component run stages
     */
    public void init() {
        
    }
    
    
    public void run() throws JAMSEntity.NoSuchAttributeException {
        if(this.active == null || this.active.getValue()){
            // map run variables
            // driving vars
            this.runRain = this.rain.getValue();
            this.runSnow = this.snow.getValue();
            this.runPrecip = this.precip.getValue();
            
            // state vars
            this.runIceContent = this.iceContent.getValue();
            this.runFreeWater = this.freeWater.getValue();
            this.runSnowWaterEquivalent = this.snowWaterEquivalent.getValue();
            this.runSnowMelt = 0;
            this.runSnowAlbedo = this.snowAlbedo.getValue();
            
            // calibration pars
            this.runMeltTemperature = this.meltTemp.getValue();
            
            
            // day and night temperatures
            double tempDay = (this.tmax.getValue() + this.tmean.getValue()) / 2;
            double tempNight = (this.tmin.getValue() + this.tmean.getValue()) / 2;
            
            if (this.runSnow > 0 || this.runSnowWaterEquivalent > 0) {
                
                //very super simple snowET
                //double deltaET = this.pET.getValue() - this.runAET;
                //if(this.runIceContent <= deltaET){
                //    this.runAET = this.runAET + this.runIceContent;
                //    this.runIceContent = 0;
                //}else{
                //    this.runIceContent = this.runIceContent - deltaET;
                //    this.runAET = this.pET.getValue();
                //}
                
                int julDay = this.time.get(time.DAY_OF_YEAR);
                double dayFrac = org.unijena.j2k.physicalCalculations.DailySolarRadiationCalculationMethods.calcDayFraction(this.latitude.getValue(), julDay);
                
                //day part
                double potMeltRate = this.calcAccumulationAndMelt(tempDay, dayFrac);
                //night part
                potMeltRate = potMeltRate + this.calcAccumulationAndMelt(tempNight, (1-dayFrac));
                
                potMeltRate = potMeltRate * this.area.getValue();
                
                //snow and rain has been taken care of now!!
                this.runRain = 0;
                this.runSnow = 0;
                
                // balancing potMeltRate against iceContent
                if (potMeltRate > this.runIceContent) {
                    potMeltRate = this.runIceContent;
                }
                
                // transforming ice to liquid
                this.runIceContent = this.runIceContent - potMeltRate;
                this.runFreeWater = this.runFreeWater + potMeltRate;
                
                // computing maximum amount of free water
                double maxFreeWater = this.runIceContent
                        * this.freeWaterCapacity.getValue();
                
                // balancing free water against max free water
                if (maxFreeWater < this.runFreeWater) {
                    // snowmelt occurs
                    this.runSnowMelt = this.runFreeWater - maxFreeWater;
                    this.runFreeWater = maxFreeWater;
                }
                
                // if snow water equivalent < 5 mm the albedo is set to landcover
                // albedo
                // otherwise to a constant(!) snow albedo
                if ((this.runIceContent + this.runFreeWater) > (5.0 * this.area.getValue())) {
                    this.runSnowAlbedo = this.snowConstAlbedo.getValue();
                } else {
                    this.runSnowAlbedo = this.landAlbedo.getValue();
                }
            }
            //write vars back
            // state vars
            this.iceContent.setValue(this.runIceContent);
            this.freeWater.setValue(this.runFreeWater);
            this.snowWaterEquivalent.setValue(this.runIceContent + this.runFreeWater);
            this.snowMelt.setValue(this.runSnowMelt);
            this.snowAlbedo.setValue(this.runSnowAlbedo);
            this.rain.setValue(this.runRain);
            this.snow.setValue(this.runSnow);
        }
    }
    
    
    public void cleanup() {
        
    }
    
    private double calcAccumulationAndMelt(double temperature, double dayFraction){
        this.runIceContent = this.runIceContent + this.runSnow * dayFraction;
        this.runFreeWater = this.runFreeWater + this.runRain * dayFraction;
        //this.runSnow = this.runSnow - (this.runSnow * dayFraction);
        //this.runRain = this.runRain - (this.runRain * dayFraction);
        
        //refreezing of free water
        double potMeltRate = 0.;
        
        //compute snow melt
        if(this.runIceContent > 0){
            //temperature index approach
            potMeltRate = this.c0Factor.getValue() * (temperature - this.runMeltTemperature);
        }
        else
            potMeltRate = 0;
        
        return potMeltRate * dayFraction;
    }
}
