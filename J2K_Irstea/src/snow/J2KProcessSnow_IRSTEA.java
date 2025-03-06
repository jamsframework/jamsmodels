/*
 * J2KProcessSnow_IRSTEA.java
 * This is a slightly modified version of the original J2KProcessSnow component by P. Krause
 * Changes w/r to original J2KProcessSnow :
 * - desactivation of aspect correction for snow melt by F. Branger, Oct 26, 2012
 * - new snow density is forced at 300 kg/m3 (0.3 g/cm3 in the code) by  F. Tilmant, Irstea, March 29, 2013
 * - the mean daily temperature is used for snow accumulation (instead of Taccu=0.5*(Tmin+Tean)) by I. Gouttevin, 2016
 * - the mean daily temperature is also used for snow melt (instead of meltTemp=0.5*(Tmean+Tmax)) by I. Gouttevin, 2016
 * - modification F. Branger 2024: make component work also when area = 0 (compatibility with glacier modules)
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
package snow;

import jams.JAMS;
import jams.data.*;
import jams.model.*;

/**
 *
 * @author Francois Tilmant
 */
@JAMSComponentDescription(
        title="J2KProcessSnow_IRSTEA",
        author="Francois Tilmant",
        description="Calculates snow accumulation, metamorphosis and melt",
        version="1.0_0",
        date="2013-03-29")
        public class J2KProcessSnow_IRSTEA extends JAMSComponent {
    
    /*
     *  Component variables
     */
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "time"
            )
            public JAMSCalendar par_time;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "The current spatial modelling entity"
            )
            public JAMSEntity st_entity;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "attribute area",
            unit = "m²"
            )
            public JAMSDouble par_area;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "state var slope-aspect-correction-factor"
            )
            public JAMSDouble st_act_sl_as_cf;
    
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "mean temperature",
            unit = "°C"
            )
            public JAMSDouble par_mean_temp;

    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "state variable net rain",
            unit = "L"
            )
            public JAMSDouble st_net_rain;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "state variable net snow",
            unit = "L"
            )
            public JAMSDouble st_net_snow;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "total snow water equivalent",
            unit = "L"
            )
            public JAMSDouble out_snow_tot_swe;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "dry snow water equivalent",
            unit = "L"
            )
            public JAMSDouble out_dry_swe;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "total snow density",
            unit = "g cm^-3"
            )
            public JAMSDouble out_tot_dens;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "dry snow density",
            unit = "g cm^-3"
            )
            public JAMSDouble out_dry_dens;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "snow depth",
            unit = "mm"
            )
            public JAMSDouble out_snow_depth;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "snow age",
            unit = "d"
            )
            public JAMSDouble out_snow_age;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "snow cold content"
            )
            public JAMSDouble out_snow_cold_content;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "daily snow melt",
            unit = "L"
            )
            public JAMSDouble out_snow_melt;
    
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.INIT,
            description = "base temperature - trigger the snow melt",
            lowerBound = -10.0,
            upperBound = 10.0,
            defaultValue="0",
            unit = "°C"
            )
            public JAMSDouble in_base_temp;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.INIT,
            description = "temperature factor for snowmelt",
            lowerBound = 0.0,
            upperBound = 20.0,
            defaultValue="1",
            unit = "mm °C^-1"
            )
            public JAMSDouble par_t_factor;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.INIT,
            description = "rain factor for snowmelt",
            lowerBound = 0.0,
            upperBound = 20.0,
            defaultValue="1",
            unit = "°C^-1"
            )
            public JAMSDouble par_r_factor;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.INIT,
            description = "ground factor for snowmelt",
            lowerBound = 0.0,
            upperBound = 20.0,
            defaultValue="1",
            unit = "mm"
            )
            public JAMSDouble par_g_factor;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.INIT,
            description = "critical density",
            lowerBound = 0.0,
            upperBound = 1.0,
            defaultValue="0.45",
            unit = "g cm^-3"
            )
            public JAMSDouble in_snow_crit_dens;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.INIT,
            description = "cold content factor",
            lowerBound = 0.0,
            upperBound = 5.0,
            defaultValue="0.01"
            )
            public JAMSDouble in_ccf_factor;
    
        @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            update = JAMSVarDescription.UpdateType.INIT,
            description = "1 if the HRU is covered with snow, else 0",
            unit = "-"
            )
            public JAMSDouble out_snow_cover;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.INIT,
            description = "module active"
            )
            public JAMSBoolean par_active;
    

        
    
    double run_area;
    double run_in_snow;
    double run_in_rain;
    double run_snow_depth;
    double run_tot_swe;
    double run_dry_swe;
    double run_init_dens = 0;
    double run_tot_dens;
    double run_dry_dens;
    double run_snow_age;
    double run_cold_content;
    double run_snow_melt = 0;
    double run_snow_cover = 0;

   
    /*
     *  Component run stages
     */
    
    public void init() {
    	if(this.par_active == null || this.par_active.getValue()){
	        this.out_snow_depth.setValue(0.0);
	        this.out_snow_tot_swe.setValue(0.0);
	        this.out_dry_swe.setValue(0.0);
	        this.out_tot_dens.setValue(0.0);
	        this.out_dry_dens.setValue(0.0);
	        this.out_snow_age.setValue(0);
	        this.out_snow_cold_content.setValue(0.0);
                this.out_snow_cover.setValue(0.0);

    	}
    }
    
    public void run() throws JAMSEntity.NoSuchAttributeException {
                
    	if(this.par_active == null || this.par_active.getValue()){
            
	        this.run_area = this.par_area.getValue();
                
                // do calculations only if run_area > 0!!
                if(this.run_area > 0.0){
   
                    this.run_in_snow = this.st_net_snow.getValue();
                    this.run_in_rain = this.st_net_rain.getValue();
                    double run_bal_in = this.run_in_snow + this.run_in_rain;

                    double run_in_mean_temp = this.par_mean_temp.getValue();
                    this.run_snow_depth = this.out_snow_depth.getValue();

                    this.run_tot_swe = out_snow_tot_swe.getValue();
                    double run_bal_stor_start = this.run_tot_swe;
                    this.run_dry_swe = out_dry_swe.getValue();
                    this.run_tot_dens = out_tot_dens.getValue();
                    this.run_dry_dens = out_dry_dens.getValue();
                    this.run_snow_age = out_snow_age.getValue();
                    this.run_cold_content = out_snow_cold_content.getValue();

                    double run_crit_dens = in_snow_crit_dens.getValue();
                    double run_cold_content_factor = in_ccf_factor.getValue();
                    double run_trs = in_base_temp.getValue();
                    double run_temp_fac = par_t_factor.getValue();
                    double run_rain_fac = par_r_factor.getValue();
                    double run_ground_fac = par_g_factor.getValue();

                    this.run_snow_melt = 0;
                    this.run_snow_cover = 0;

                    run_cold_content = run_cold_content + this.calcColdContent(run_in_mean_temp, run_cold_content_factor);
                    if (run_cold_content > 0) {
                        run_cold_content = 0;
                    }

                    if (run_snow_depth > 0) {
                        //increasing snow age by one day
                        run_snow_age += 1;
                    }

                    if (run_in_snow > 0) {
// we want to have the snow accumulation at each timestep
                        this.calcSnowAccumulation(run_in_mean_temp, run_area, run_crit_dens);
                    }

                    if ((run_in_mean_temp >= run_trs) && (this.run_snow_depth > 0)) {
                        this.calcMetamorphosis(run_in_mean_temp, run_temp_fac, run_rain_fac, run_ground_fac, run_area, run_crit_dens); // RQ IG : s'il a plu ET neigé, la pluie a été mise à 0 dans SnowAccumulation donc le melt lié à la pluie n'est pas calculé.
                    }

                    if (run_snow_depth != 0) {
                        this.run_snow_cover = 1;
                    } else {
                        this.run_snow_cover = 0;

                    }

                    this.calcSnowDensities(run_area);

                    this.st_net_rain.setValue(this.run_in_rain);
                    this.st_net_snow.setValue(this.run_in_snow);
                    this.out_snow_tot_swe.setValue(this.run_tot_swe);
                    this.out_dry_swe.setValue(this.run_dry_swe);
                    this.out_tot_dens.setValue(this.run_tot_dens);
                    this.out_dry_dens.setValue(this.run_dry_dens);
                    this.out_snow_depth.setValue(this.run_snow_depth);
                    this.out_snow_cover.setValue(this.run_snow_cover);

                    this.out_snow_age.setValue(this.run_snow_age);
                    this.out_snow_cold_content.setValue(this.run_cold_content);

                    this.out_snow_melt.setValue(this.run_snow_melt);
                    double run_bal_stor_end = this.run_tot_swe;
                    double run_bal_out = this.run_snow_melt + this.run_in_rain + this.run_in_snow;
                    double run_balance = run_bal_in + (run_bal_stor_start - run_bal_stor_end) - run_bal_out;
                    if (Math.abs(run_balance) > 0.0001) {
                        getModel().getRuntime().println("balance error in snow module: " + run_balance);
                        getModel().getRuntime().println("balIn: " + run_bal_in);
                        getModel().getRuntime().println("balStorStart: " + run_bal_stor_start);
                        getModel().getRuntime().println("balStorEnd: " + run_bal_stor_end);
                        getModel().getRuntime().println("balOut: " + run_bal_out);
                        getModel().getRuntime().println("shit!");
                    }
                    //if(this.run_drySWE > this.run_totSWE)
                    //    System.out.getRuntime().println("dry is larger than tot at end at time: " + time.toString() + " in entity: " + entity.getDouble("ID"));
                    if (this.run_snow_melt < 0) {
                        getModel().getRuntime().println("negative snowmelt!!");
                    }
                }
                //else: set everything to 0
                else{
                    this.st_net_rain.setValue(0.0);
                    this.st_net_snow.setValue(0.0);
                    this.out_snow_tot_swe.setValue(0.0);
                    this.out_dry_swe.setValue(0.0);
                    this.out_tot_dens.setValue(0.0);
                    this.out_dry_dens.setValue(0.0);
                    this.out_snow_depth.setValue(0.0);
                    this.out_snow_cover.setValue(0.0);
                    this.out_snow_age.setValue(0.0);
                    this.out_snow_cold_content.setValue(0.0);
                    this.out_snow_melt.setValue(0.0);
                    
                }
                
	        
    	}
    }
    
    public void cleanup() {
    	if(this.par_active == null || this.par_active.getValue()){
	        this.out_snow_depth.setValue(0.0);
	        this.out_snow_tot_swe.setValue(0.0);
	        this.out_dry_swe.setValue(0.0);
	        this.out_tot_dens.setValue(0.0);
	        this.out_dry_dens.setValue(0.0);
	        this.out_snow_age.setValue(0);
	        this.out_snow_cold_content.setValue(0.0);
   
    	}
    }
    
    private double calcColdContent(double run_temperature, double run_cold_content_factor){
        double run_cc_factor = run_cold_content_factor * 24;
        return (run_cc_factor * run_temperature);
    }
    
    /** calculates snow accumulation for a spatial unit and one daily
     * time step. Snow accumulation is positive if snow falls on snow
     * pack and can be negative if rain on snow occurs. Snow pack settlement
     * because of rain on snow is also covered here following the approach
     * of BERTLE 1966 as presented by KRAUSE 2001; local vars rain and snow
     * are set to zero after accumulation
     * @return true if successfull, false otherwise
     */
    private boolean calcSnowAccumulation(double run_temp, double run_area, double run_crit_dens){
        double run_delta_height = 0;
        //increase of snow pack because of snow fall
        if(this.run_in_snow > 0){
            
            double run_new_snow_density = this.calcNewSnowDensity(run_temp);
            run_delta_height = this.run_in_snow / (run_new_snow_density * run_area);
            this.run_snow_depth = this.run_snow_depth + run_delta_height; // mm; unit checked.
            
            
            //increase of dry and total snow water equivalent by snow precip amount
            //double old_SWE = this.tot_SWE;
            this.run_dry_swe = this.run_dry_swe + this.run_in_snow;
            this.run_tot_swe = this.run_tot_swe + this.run_in_snow;
            this.run_in_snow = 0;
            
            //recalculation of snow Densities
            this.calcSnowDensities(run_area);
            
            //resetting snow age
            this.run_snow_age = 0;
            
            //saving the initial density for snow pack settlement
            this.run_init_dens = this.run_dry_dens;
        }
        
        //calculation of snow pack settlement by free water
        if(this.run_in_rain > 0){
            this.calcRainSnowSettlement(this.run_in_rain);
            this.run_in_rain = 0;
        }
        //if snow pack has vanished, nothing more to do
        if(this.run_snow_depth == 0)
            return true;
        
        //Calculation of new snow densities
        this.calcSnowDensities(run_area);
        
        /** water from snow pack */
        if(Math.round(this.run_tot_dens * 100000d) / 100000d > run_crit_dens){
            this.run_snow_melt = this.run_snow_melt + calcSnowMeltRunoff(run_crit_dens, run_area);
            //if(this.run_snowMelt < 0)
            //System.out.getRuntime().println("negative SM a");
        } else{
            double run_p_ro = this.calcPotRunoff(run_crit_dens, this.run_tot_dens, this.run_tot_swe - this.run_dry_swe);
            this.run_snow_melt = this.run_snow_melt + run_p_ro;
            this.run_tot_swe = this.run_tot_swe - run_p_ro;
            //if(this.run_snowMelt < 0)
            //System.out.getRuntime().println("negative SM b because of: " + pRO);
        //Calculation of new snow densities
        this.calcSnowDensities(run_area); // IG 5-07-2016: displacement because calcSnowDensities(area) is included in calcSnowMeltRunoff
        }

        

        return true;
    }
    
    /** calculates density of new fallen snow depending
     * on the mean temperature. Follows the approach
     * of KUCHMENT 1983 and VEHVILÄINEN 1992 as presented
     * by HERPERTZ 2002
     * @param run_tmean the current mean temperature of the spatial unit
     * @return density of new fallen snow
     */
    private double calcNewSnowDensity(double run_temp){
        double run_new_snow_density = 0;      
 // Francois Tilmant : we force new snow density = 0.3 to avoid enormous snowDepth 
        run_new_snow_density = 0.3;
        return run_new_snow_density;
    }
    
    private void calcSnowDensities(double run_area){
        //Calculation of new snow densities
        if(this.run_snow_depth > 0){
            this.run_tot_dens = this.run_tot_swe / (run_area * this.run_snow_depth);
            this.run_dry_dens = this.run_dry_swe / (run_area * this.run_snow_depth);
        } else{
            this.run_tot_dens = 0;
            this.run_dry_dens = 0;
        }
    }
    
    private void calcRainSnowSettlement(double run_input_water){
        /**************************************************************
         * /*Change of snow depth due to setting caused by rain on snow or meltwater
         ***************************************************************/
        double run_pw = 100;
        if(run_input_water > 0){
            this.run_tot_swe = this.run_tot_swe + run_input_water;
            this.run_in_rain = 0;
            run_pw = (this.run_tot_swe / this.run_dry_swe) * 100.0;
        }
        
        //determination of settle rate after BERTLE 1966 due to rain on snow
        double run_ph = 147.4 - 0.474 * run_pw;
        
        if(run_ph > 0){
            this.run_snow_depth = this.run_snow_depth * (run_ph / 100.);
            this.calcSnowDensities(this.run_area);
            if(Math.round(this.run_dry_dens * 100000d) / 100000d > this.in_snow_crit_dens.getValue()){
              double run_max_swe = this.in_snow_crit_dens.getValue() * run_area * this.run_snow_depth;
              this.run_dry_swe = run_max_swe;
            }
        }
        else{ //loss of whole snow pack because of heavy rain on few snow or complete melting
            this.run_snow_melt = this.run_snow_melt + this.run_tot_swe;
            this.run_snow_depth = 0;
            this.run_tot_swe = 0;
            this.run_dry_swe = 0;
            this.run_tot_dens = 0;
            this.run_dry_dens = 0;
            this.run_snow_age = 0;
            //if(this.run_snowMelt < 0)
            //    System.out.getRuntime().println("negative SM 0");
        }
        
    }
    
    private double calcSnowMeltRunoff(double run_crit_dens, double run_area){
        /** maximum water capacity of snow pack */
        double run_w_smax = run_crit_dens * this.run_snow_depth * run_area;
        double run_snowmelt = this.run_tot_swe - run_w_smax;
        this.run_tot_swe = run_w_smax;
        
        this.calcSnowDensities(run_area);
        return run_snowmelt;
    }
    
    private double calcPotRunoff(double run_crit_dens, double run_tot_dens, double run_liq_water){
        if(Math.abs(run_liq_water) > 0.00001 && run_liq_water < 0)
            getModel().getRuntime().println("liq_water is negative: "+run_liq_water);
        double run_pot_runoff = (1 - Math.exp(-1 * Math.pow((run_crit_dens/run_tot_dens), 4))) * run_liq_water;
        if(run_pot_runoff < 0)
            run_pot_runoff = 0;
        return run_pot_runoff;
    }
    
    private boolean calcMetamorphosis(double run_temp, double run_temp_fac, double run_rain_fac, double run_ground_fac, double run_area, double run_crit_dens){
        /**calculation of snowmelt - complex formula*/
        //@todo integration of canopy shadow by LAI
        double run_pot_meltrate = 0;
        run_pot_meltrate = this.calcPotMRSemiComp(run_temp, run_temp_fac, run_rain_fac, run_ground_fac, run_area);// kg d'eau fondue/jour/m2.
        
        if(Math.abs(this.run_cold_content) >= run_pot_meltrate){
            this.run_cold_content = this.run_cold_content + run_pot_meltrate;
            run_pot_meltrate = 0;
        } else{
            run_pot_meltrate = run_pot_meltrate + this.run_cold_content;
            this.run_cold_content = 0;
        }
        
        run_pot_meltrate = run_pot_meltrate * run_area;
        
        /** decrease of dry snow depth caused by snow melt */
        double run_delta_snow_depth = run_pot_meltrate / (this.run_dry_dens * run_area);
        
        //if(this.run_snowMelt < 0)
        //    System.out.getRuntime().println("negative SM 1");
        /** depletion of whole snow pack */
        if(run_delta_snow_depth >= this.run_snow_depth){
            run_delta_snow_depth = this.run_snow_depth;
            this.run_snow_depth = 0;
            this.run_tot_dens = 0;
            this.run_dry_dens = 0;
            this.run_snow_melt = this.run_snow_melt + this.run_tot_swe;
            this.run_tot_swe = 0;
            this.run_dry_swe = 0;
            this.run_snow_age = 0;
            //if(this.run_snowMelt < 0)
            //System.out.getRuntime().println("negative SM 1.5");
            //nothing more to do -- no snow left
            return true; // est-ce que cela signifie qu'on sort de la routine ?
        }
        //if(this.run_snowMelt < 0)
        //    System.out.getRuntime().println("negative SM 2");
        
        /** decrease of snow pack due to snow melt */
        this.run_snow_depth = this.run_snow_depth - run_delta_snow_depth;
        
        /** decrease of dry SWE due to snow melt */
        this.run_dry_swe = this.run_dry_swe - run_pot_meltrate;
        run_pot_meltrate = 0;
        
        //Calculation of new snow densities
        this.calcSnowDensities(run_area);
        
        //if(this.run_snowMelt < 0)
        //    System.out.getRuntime().println("negative SM 3");
        /** potential water from snow pack */
        if(Math.round(this.run_tot_dens * 100000d) / 100000d >= run_crit_dens){
            this.run_snow_melt = this.run_snow_melt + calcSnowMeltRunoff(run_crit_dens, run_area);
            //if(this.run_snowMelt < 0)
            //System.out.getRuntime().println("negative SM 4");
        } else{
            double run_p_ro = this.calcPotRunoff(run_crit_dens, this.run_tot_dens, this.run_tot_swe - this.run_dry_swe);
            this.run_snow_melt = this.run_snow_melt + run_p_ro;
            this.run_tot_swe = this.run_tot_swe - run_p_ro;
            //if(this.run_snowMelt < 0)
            //System.out.getRuntime().println("negative SM 5");
        }
        //Calculation of new snow densities
        this.calcSnowDensities(run_area);
        
        /** settlement of snow-pack by rain and/or snowmelt */
        this.calcRainSnowSettlement(this.run_in_rain + run_pot_meltrate); 
//IG : if snowfall + rainfall, rainfall has already been accounted for in Accumulation; in this case only melt is dealt with here. However, necessary if only rainfall and / or melt.
//IG - to be checked. potMeltrate has been set to 0 before, so only rain compaction
        this.run_in_rain = 0;
        
        //if snow pack has vanished, nothing more to do
        if(this.run_snow_depth == 0)
            return true;
        
        //Calculation of new snow densities
        this.calcSnowDensities(run_area);
        
        /** water from snow pack */
        if(Math.round(this.run_tot_dens * 100000d) / 100000d >= run_crit_dens){
            this.run_snow_melt = this.run_snow_melt + calcSnowMeltRunoff(run_crit_dens, run_area);
            //if(this.run_snowMelt < 0)
            //System.out.getRuntime().println("negative SM 6");
        } else{
            double run_p_ro = this.calcPotRunoff(run_crit_dens, this.run_tot_dens, this.run_tot_swe - this.run_dry_swe);
            this.run_snow_melt = this.run_snow_melt + run_p_ro;
            this.run_tot_swe = this.run_tot_swe - run_p_ro;
            //if(this.run_snowMelt < 0)
            //System.out.getRuntime().println("negative SM 7");
        }
        
        this.calcSnowDensities(run_area);
        return true;
    }
    
    private double calcPotMRSemiComp(double run_temp, double run_temp_fac, double run_rain_fac, double run_ground_fac, double run_area){
        double run_melt_temp = run_temp;
        double run_pot_mr = (run_temp_fac * run_melt_temp + run_ground_fac + run_rain_fac * (this.run_in_rain / run_area) * run_melt_temp);
        //avoid negative melt rates
        if(run_pot_mr < 0)
            run_pot_mr = 0;
        return run_pot_mr;
    }
}
