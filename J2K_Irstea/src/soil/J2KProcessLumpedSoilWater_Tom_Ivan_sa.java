/*
 * J2KProcessLumpedSoilWater.java
 * Created on 25. November 2005, 13:21
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

package soil;

import jams.JAMS;
import jams.data.*;
import jams.model.*;

/**
 *
 * @author Peter Krause
 */
@JAMSComponentDescription(
        title="J2KProcessLumpedSoilWater",
        author="Peter Krause + Francois Tilmant",
        description="Calculates soil water balance for each spatial modelling unit."
        + "Changes in the module to add many variables as output"
        + "Modified by Ivan horner to add 'adaptation factors' to distributed parameters.",
        version="1.1_0",
        date="2011-05-30")
        public class J2KProcessLumpedSoilWater_Tom_Ivan_sa extends JAMSComponent {
    
    /*
     *  Component variables
     */
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "time"
            )
            public Attribute.Calendar par_time;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "The current spatial modelling entity"
            )
            public Attribute.Entity st_entity;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "attribute area",
            unit="m²"
            )
            public Attribute.Double par_area;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "attribute slope",
            unit="deg"
            )
            public Attribute.Double par_slope;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "sealed grade"
            )
            public Attribute.Double par_sealed_grade;
        
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "sealed grade 'multiplicative' adaptation Factor",
            defaultValue = "1"
            )
            public Attribute.Double par_sealed_grade_maf;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "sealed grade 'additive' adaptation Factor",
            defaultValue = "0"
            )
            public Attribute.Double par_sealed_grade_aaf;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "state variable net rain",
            unit="L"
            )
            public Attribute.Double st_net_rain;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "state variable net snow",
            unit="L"
            )
            public Attribute.Double st_net_snow;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "state variable potET",
            unit="L"
            )
            public Attribute.Double st_pot_et;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "state variable actET",
            unit="L"
            )
            public Attribute.Double st_act_et;
    
   @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "state variable actET before MPS Evaporation",
            unit="L"
            )
            public Attribute.Double st_act_et_intc;
   
   @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "delta ETP (= potET - actET) in MPS Evaporation",
            unit="L"
            )
            public Attribute.Double out_delta_etp;
    
   
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "snow depth",
            unit="mm"
            )
            public Attribute.Double in_snow_depth;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "daily snow melt",
            unit="L"
            )
            public Attribute.Double in_snow_melt;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "maximum MPS",
            unit="L"
            )
            public Attribute.Double in_max_mps;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "maximum LPS",
            unit="L"
            )
            public Attribute.Double in_max_lps;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "state var actual MPS",
            unit="L"
            )
            public Attribute.Double st_act_mps;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "state var actual LPS",
            unit="L"
            )
            public Attribute.Double st_act_lps;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "state var actual depression storage",
            unit="L"
            )
            public Attribute.Double st_act_dps;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "state var saturation of MPS"
            )
            public Attribute.Double st_sat_mps;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "state var saturation of LPS"
            )
            public Attribute.Double st_sat_lps;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "state var saturation of whole soil"
            )
            public Attribute.Double st_sat_soil;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "statevar infiltration",
            unit="L"
            )
            public Attribute.Double st_infiltration;
    
        @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "infiltration after reduction due to sealed grade",
            unit="L"
            )
            public Attribute.Double out_inf_after_sealed_grade;
 
        @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "infiltration after MPS",
            unit="L"
            )
            public Attribute.Double out_inf_after_mps;        
        
        @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "max infiltration",
            unit="L"
            )
            public Attribute.Double out_max_inf2;
        
        @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "delta MPS (max - act) before comparison with infiltration",
            unit="L"
            )
            public Attribute.Double out_delta_mps2;
        
                @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "act MPS after MPSInflow",
            unit="L"
            )
            public Attribute.Double out_act_mps2;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "statevar interflow",
            unit="L"
            )
            public Attribute.Double st_interflow;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "statevar percolation",
            unit="L"
            )
            public Attribute.Double st_percolation;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "statevar RD1 inflow",
            unit="L"
            )
            public Attribute.Double st_in_rd1;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "statevar RD1 outflow",
            unit="L"
            )
            public Attribute.Double st_out_rd1;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "statevar RD1 generation",
            unit="L"
            )
            public Attribute.Double st_gen_rd1;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "statevar RD2 inflow",
            unit="L"
            )
            public Attribute.Double st_in_rd2;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "statevar RD2 outflow",
            unit="L"
            )
            public Attribute.Double st_out_rd2;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "statevar RD2 generation",
            unit="L"
            )
            public Attribute.Double st_gen_rd2;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "maximum depression storage",
            unit="L"
            )
            public Attribute.Double in_soil_max_dps;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "poly reduction of ETP",
            lowerBound = 0.0,
            upperBound = 10.0,
            defaultValue = "3.0"
            )
            public Attribute.Double in_soil_pol_red;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "linear reduction of ETP",
            lowerBound = 0.0,
            upperBound = 1.0,
            defaultValue = "0.9",
            unit="?"
            )
            public Attribute.Double in_soil_lin_red;
    
        @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "reduction factor calculated for MPS transpiration",
            unit="-"
            )
            public Attribute.Double out_reduction_factor;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "maximum infiltration rate in summer for one time step",
            lowerBound = 0.0,
            upperBound = 100.0,
            defaultValue = "50.0",
            unit="mm"
            )
            public Attribute.Double par_soil_max_inf_summer;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "maximum infiltration rate in winter for one time step",
            lowerBound = 0.0,
            upperBound = 100.0,
            defaultValue = "50.0",
            unit="mm"
            )
            public Attribute.Double par_soil_max_inf_winter;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "maximum infiltration rate on snow for one time step",
            lowerBound = 0.0,
            upperBound = 100.0,
            defaultValue = "50.0",
            unit="mm"
            )
            public Attribute.Double par_soil_max_inf_snow;
   
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "maximum infiltration rate in summer for one time step 'multiplicative' ADAPTATION FACTOR",
            defaultValue = "1",
            unit="-"
            )
            public Attribute.Double par_soil_max_inf_summer_maf;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "maximum infiltration rate in winter for one time step 'multiplicative' ADAPTATION FACTOR",
            defaultValue = "1",
            unit="-"
            )
            public Attribute.Double par_soil_max_inf_winter_maf;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "maximum infiltration rate on snow for one time step 'multiplicative' ADAPTATION FACTOR",
            defaultValue = "1",
            unit="-"
            )
            public Attribute.Double par_soil_max_inf_snow_maf;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "maximum infiltration rate in summer for one time step 'additive' ADAPTATION FACTOR",
            defaultValue = "0",
            unit="-"
            )
            public Attribute.Double par_soil_max_inf_summer_aaf;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "maximum infiltration rate in winter for one time step 'additive' ADAPTATION FACTOR",
            defaultValue = "0",
            unit="-"
            )
            public Attribute.Double par_soil_max_inf_winter_aaf;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "maximum infiltration rate on snow for one time step 'additive' ADAPTATION FACTOR",
            defaultValue = "0",
            unit="-"
            )
            public Attribute.Double par_soil_max_inf_snow_aaf;
     
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "MPS/LPS distribution coefficient for inflow",
            lowerBound = 0.0,
            upperBound = 10.0,
            defaultValue = "1.0"
            )
            public Attribute.Double par_soil_dist_mps_lps;
    
 //   @JAMSVarDescription(
 //           access = JAMSVarDescription.AccessType.WRITE,
 //           description = "MPS/LPS distribution coefficient for inflow",
 //           lowerBound = 0.0,
 //           upperBound = 10.0,
 //           defaultValue = "1.0"
 //           )
 //           public Attribute.Double soilDistMPSLPS2;
    
        @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "LPS outflow",
            unit="L"
            )
            public Attribute.Double out_mobile_water2;
        
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "MPS/LPS diffusion coefficient",
            lowerBound = 0.0,
            upperBound = 10.0,
            defaultValue = "1.0"
            )
            public Attribute.Double par_soil_diff_mps_lps;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "LPS outflow coefficient",
            lowerBound = 0.0,
            upperBound = 10.0,
            defaultValue = "1.0"
            )
            public Attribute.Double par_soil_out_lps;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "LPS lateral-vertical distribution coefficient",
            lowerBound = 0.0,
            upperBound = 10.0,
            defaultValue = "1.0"
            )
            public Attribute.Double par_soil_lat_vert_lps;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "maximum percolation rate [mm/d]",
            lowerBound = 0.0,
            upperBound = 20.0,
            defaultValue = "5.0",
            unit = "mm d^-1"
            )
            public Attribute.Double par_soil_max_perc;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "concentration coefficient for RD1",
            lowerBound = 0.0,
            upperBound = 10.0,
            defaultValue = "2.0"
            )
            public Attribute.Double par_soil_conc_rd1;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "concentration coefficient for RD2",
            lowerBound = 1.0,
            upperBound = 20.0,
            defaultValue = "8.0"
            )
            public Attribute.Double par_soil_conc_rd2;
    
    //internal state variables
    double run_max_mps, run_max_lps, run_act_mps, run_act_mps2,run_act_lps, run_sat_mps, run_act_dps, run_sat_lps, run_sat_soil, run_in_rd1, run_in_rd2, run_in_rain, run_in_snow,
            run_snow_melt, run_infiltration,run_infiltration2 ,run_infiltration3, run_interflow, run_percolation, run_overlandflow, run_pot_etp, run_act_etp, run_snow_depth, run_area, run_slope,
            run_out_rd1, run_out_rd2, run_gen_rd1, run_gen_rd2,run_delta_mps, run_mobile_water,run_act_et2,run_reduction_factor,run_delta_etp; //run_soilDistMPSLPS2
    
    /*
     *  Component run stages
     */
    
    public void init() {
       

        
    }
    
    public void run() {
        this.run_area = par_area.getValue();
        this.run_slope = par_slope.getValue();
        
        this.run_max_mps = in_max_mps.getValue();
        this.run_max_lps = in_max_lps.getValue();
        this.run_act_mps = st_act_mps.getValue();
        this.run_act_lps = st_act_lps.getValue();
        this.run_sat_mps = st_sat_mps.getValue();
        this.run_sat_lps = st_sat_lps.getValue();
        this.run_act_dps = st_act_dps.getValue();
        
        this.run_in_rd1 = st_in_rd1.getValue();
        this.run_in_rd2 = st_in_rd2.getValue();
        
        this.run_in_rain = st_net_rain.getValue();
        this.run_in_snow = st_net_snow.getValue();
        
        this.run_pot_etp = st_pot_et.getValue();
        this.run_act_etp = st_act_et.getValue();
        this.run_snow_depth = in_snow_depth.getValue();
        this.run_snow_melt = in_snow_melt.getValue();
        
        this.run_gen_rd1 = 0;
        this.run_gen_rd2 = 0;
        this.run_out_rd1 = 0;
        this.run_out_rd2 = 0;
        this.run_interflow = 0;
        this.run_percolation = 0;
        
        //calculation of saturations first
        this.calcSoilSaturations(false);
        
        /** redistributing RD1 and RD2 inflow of antecedent unit */
        this.redistRD1RD2In();
        
        /** calculation of ETP from depression Storage and open water bodies */
        this.calcPreInfEvaporation();
        
        /** determining available water for infiltration */
        this.run_infiltration = this.run_in_rain + this.run_in_snow
                + this.run_snow_melt
                + this.run_act_dps;
        
        this.run_act_dps = 0;
        this.run_in_rain = 0;
        this.run_in_snow = 0;
        this.run_snow_melt = 0;
        
        /** infiltration on impervious areas and water bodies
         *  is directly routed as DirectRunoff to the next polygon
         *  a better implementation would be the next river reach */
        double run_sg = par_sealed_grade.getValue() * par_sealed_grade_maf.getValue() + par_sealed_grade_aaf.getValue();
        if (run_sg < 0) {
            run_sg = 0;
        } else if (run_sg >1 ) {
            run_sg = 1;
        }
        this.calcInfImperv(run_sg);
        
        run_infiltration2 = this.run_infiltration;
        /** determining maximal infiltration rate */
        double run_max_inf = this.calcMaxInfiltration(par_time.get(Attribute.Calendar.MONTH)+1);
        if(run_max_inf < this.run_infiltration){
            double run_delta_inf = this.run_infiltration - run_max_inf;
            this.run_act_dps = this.run_act_dps + run_delta_inf;
            this.run_infiltration = run_max_inf;
        }
        
        
        /** determining inflow of infiltration to MPS */
        this.run_infiltration = this.calcMPSInflow(this.run_infiltration);
        this.run_act_mps2 = this.run_act_mps;
        this.run_infiltration3 = this.run_infiltration;
        
        /** determining transpiration from MPS */
        
        this.run_act_et2 = this.run_act_etp;  
        this.calcMPSTranspiration(false);
        
        /** inflow to LPS */
        this.run_infiltration = this.calcLPSInflow(this.run_infiltration);
        
        /** updating saturations */
        this.calcSoilSaturations(false);
        
        
        /** determining outflow from LPS */
        double run_mobile_water2 = 0;
        if(this.run_act_lps > 0){
            run_mobile_water2 = this.calcLPSOutflow();
        } else
            run_mobile_water2 = 0;
        this.run_mobile_water = run_mobile_water2;
        /** Distribution of MobileWater to the lateral (interflow) and
         * vertical (percolation) flowpaths  */
        this.calcIntfPercRates(run_mobile_water2);
        
        /** determining direct runoff from depression storage */
        this.run_overlandflow = this.run_overlandflow + this.calcDirectRunoff();
        
        /** determining internal area routing **/
        this.calcRD1RD2Out();
        
        /** determining diffusion from LPS to MPS */
        this.calcDiffusion();
        
        /** updating saturations */
        this.calcSoilSaturations(false);
        
        st_sat_soil.setValue(this.run_sat_soil);
        st_sat_mps.setValue(this.run_sat_mps);
        st_sat_lps.setValue(this.run_sat_lps);
        st_act_mps.setValue(this.run_act_mps);
        st_act_lps.setValue(this.run_act_lps);
        st_act_dps.setValue(this.run_act_dps);
        st_act_et.setValue(this.run_act_etp);
        st_in_rd1.setValue(this.run_in_rd1);
        st_in_rd2.setValue(this.run_in_rd2);
        st_out_rd1.setValue(this.run_out_rd1);
        st_out_rd2.setValue(this.run_out_rd2);
        st_gen_rd1.setValue(this.run_gen_rd1);
        st_gen_rd2.setValue(this.run_gen_rd2);
        st_percolation.setValue(this.run_percolation);
        st_interflow.setValue(this.run_interflow);
        out_inf_after_sealed_grade.setValue(this.run_infiltration2);
        out_max_inf2.setValue(run_max_inf);
        out_delta_mps2.setValue(run_delta_mps);
        out_act_mps2.setValue(run_act_mps2);
        out_inf_after_mps.setValue(this.run_infiltration3);
        out_mobile_water2.setValue(this.run_mobile_water);
        st_act_et_intc.setValue(this.run_act_et2);
        out_reduction_factor.setValue(this.run_reduction_factor);
        out_delta_etp.setValue(this.run_delta_etp);
    }
    
    public void cleanup() {
        
    }
    
    private boolean calcSoilSaturations(boolean debug){
       
        if((this.run_act_lps > 0) && (this.run_max_lps > 0)){
            this.run_sat_lps = this.run_act_lps / this.run_max_lps;
        } else
            this.run_sat_lps = 0;
        
        if((this.run_act_mps > 0) && (this.run_max_mps > 0)){
            this.run_sat_mps = this.run_act_mps / this.run_max_mps;
        } else
            this.run_sat_mps = 0;
        
        if(((this.run_max_lps > 0) | (this.run_max_mps > 0)) & ((this.run_act_lps > 0) | (this.run_act_mps > 0))){
            this.run_sat_soil = ((this.run_act_lps + this.run_act_mps) / (this.run_max_lps + this.run_max_mps));
        } else{
            this.run_sat_soil = 0;
        }
       
        return true;
    }
    
    private boolean redistRD1RD2In(){
        if(this.run_in_rd1 > 0){
            this.run_act_dps = this.run_act_dps + this.run_in_rd1;
            this.run_in_rd1 = 0;
        }
        if(this.run_in_rd2 > 0){
            this.run_in_rd2 = this.calcMPSInflow(this.run_in_rd2);
            this.run_in_rd2 = this.calcLPSInflow(this.run_in_rd2);
            if(this.run_in_rd2 > 0){
                getModel().getRuntime().println("RD2 is not null");
            }
        }
        return true;
    }
    
    private boolean calcPreInfEvaporation(){
        double run_delta_etp = this.run_pot_etp - this.run_act_etp;
        if(this.run_act_dps > 0){
            if(this.run_act_dps >= run_delta_etp){
                this.run_act_dps = this.run_act_dps - run_delta_etp;
                run_delta_etp = 0;
                this.run_act_etp = this.run_pot_etp;
            } else{
                run_delta_etp = run_delta_etp - this.run_act_dps;
                this.run_act_dps = 0;
                this.run_act_etp = this.run_pot_etp - run_delta_etp;
            }
        }
        /** @todo implementation for open water bodies has to be implemented here */
        return true;
    }
    
    private boolean calcInfImperv(double run_sealed_grade){
        this.run_overlandflow = this.run_overlandflow + run_sealed_grade * this.run_infiltration;
        this.run_infiltration = this.run_infiltration * (1 - run_sealed_grade);
        return true;
    }
    
    private double calcMaxInfiltration(int run_now_month){
        double run_max_inf = 0;
        this.calcSoilSaturations(false);
        if(this.run_snow_depth > 0){
            run_max_inf = this.par_soil_max_inf_snow.getValue() * this.par_soil_max_inf_snow_maf.getValue() + this.par_soil_max_inf_snow_aaf.getValue();
            run_max_inf = run_max_inf * this.run_area;
        }else if((run_now_month >= 5) & (run_now_month <=10)){
            run_max_inf = this.par_soil_max_inf_summer.getValue() * this.par_soil_max_inf_summer_maf.getValue() + this.par_soil_max_inf_summer_aaf.getValue();
            run_max_inf = (1 - this.run_sat_soil) * run_max_inf * this.run_area;
        }else{
            run_max_inf = this.par_soil_max_inf_winter.getValue() * this.par_soil_max_inf_winter_maf.getValue() + this.par_soil_max_inf_winter_aaf.getValue();
            run_max_inf = (1 - this.run_sat_soil) * run_max_inf * this.run_area;
        }
        if (run_max_inf < 0) {
            run_max_inf = 0;
        }
        return run_max_inf;
    }

    private boolean calcMPSTranspiration(boolean run_debug){
        double run_max_trans = 0;
        /** updating saturations */
        this.calcSoilSaturations(run_debug);
        
        /** delta ETP */
        double run_delta_etp_int = this.run_pot_etp - this.run_act_etp;
        run_delta_etp = run_delta_etp_int;
        /**linear reduction after MENZEL 1997 was chosen*/
        //if(this.etp_reduction == 0){
        if(this.in_soil_lin_red.getValue() > 0){
            /** reduction if actual saturation is smaller than linear factor */
            if(this.run_sat_mps < in_soil_lin_red.getValue()){
                //if(this.sat_MPS < this.etp_linRed){
                double run_reduction_factor_int = this.run_sat_mps / in_soil_lin_red.getValue();
                
                //double reductionFactor = this.sat_MPS / etp_linRed;
                run_max_trans = run_delta_etp_int * run_reduction_factor_int;
                run_reduction_factor = run_reduction_factor_int;
            } else{
                run_max_trans = run_delta_etp_int;
                run_reduction_factor = 1;
            }
        }
        /** polynomial reduction after KRAUSE 2001 was chosen */
        else if(in_soil_pol_red.getValue() > 0){
            //else if(this.etp_reduction == 1){
            double run_sat_factor = -10. * Math.pow((1 - this.run_sat_mps), in_soil_pol_red.getValue());
            //double sat_factor = Math.pow((1 - this.sat_MPS), etp_polRed);
            double run_reduction_factor_int = Math.pow(10, run_sat_factor);
            run_max_trans = run_delta_etp_int * run_reduction_factor_int;
            if(run_max_trans > run_delta_etp_int)
                run_max_trans = run_delta_etp_int;
        }
        
        /** Transpiration from MPS */
        if(run_delta_etp_int > 0){
            
            /** if enough water is available */
            if(this.run_act_mps > run_max_trans){
                this.run_act_mps = this.run_act_mps - run_max_trans;
                run_delta_etp_int = run_delta_etp_int - run_max_trans;
            }
            /** storage is limiting ETP */
            else{
                run_delta_etp_int = run_delta_etp_int - this.run_act_mps;
                this.run_act_mps = 0;
            }
        }
        
        /** recalculation actual ETP */
        this.run_act_etp = this.run_pot_etp - run_delta_etp_int;
        this.calcSoilSaturations(run_debug);
        
        /* @todo: ETP from water bodies has to be implemented here */
        return true;
    }
    
    private double calcMPSInflow(double run_infiltration){
        this.calcSoilSaturations(false);
        this.run_delta_mps = this.run_max_mps - this.run_act_mps;
        if(this.run_sat_mps == 0) 
            this.run_sat_mps = 0.0000001;
        double run_in_mps = (run_infiltration) * (1. - Math.exp(-1 * this.par_soil_dist_mps_lps.getValue() / this.run_sat_mps));
        if (run_in_mps > this.run_delta_mps)
            run_in_mps = this.run_delta_mps;
        this.run_act_mps = this.run_act_mps + run_in_mps;
        run_infiltration = run_infiltration - run_in_mps;
        return run_infiltration;
    }
  
    private double calcLPSInflow(double run_infiltration){
        this.run_act_lps = this.run_act_lps + run_infiltration;
        run_infiltration = 0;
        /** if LPS is saturated depression Storage occurs */
        if(this.run_act_lps > this.run_max_lps){
            this.run_act_dps = this.run_act_dps + (this.run_act_lps - this.run_max_lps);
            this.run_act_lps = this.run_max_lps;
        }
        return run_infiltration;
    }
    
    private double calcLPSOutflow(){
        double run_alpha = this.par_soil_out_lps.getValue();
        //if soilSat is 1 the outflow equation would produce an error,
        //for this (unlikely) case soilSat is set to 0.999999
        
        //testing if LPSsat might give a better behaviour
        //if(this.run_satLPS == 1.0)
        //    this.run_satLPS = 0.999999;
        //original function
        //double potLPSoutflow = this.act_LPS * (1. - Math.exp(-1*alpha/(1-this.sat_LPS)));
        double run_pot_lps_outflow = Math.pow(this.run_sat_soil, run_alpha) * this.run_act_lps;
        
        //testing a simple function function out = 1/k * sto
        //double potLPSoutflow = 1 / alpha * this.act_LPS;//Math.pow(this.act_LPS, alpha);
        if(run_pot_lps_outflow > this.run_act_lps)
            run_pot_lps_outflow = this.run_act_lps;
        
        double run_lps_outflow = run_pot_lps_outflow;// * ( 1 / this.parameter.getDouble("lps_kfForm"));
        if(run_lps_outflow > this.run_act_lps)
            run_lps_outflow = this.run_act_lps;
        
        this.run_act_lps = this.run_act_lps - run_lps_outflow;
        
        return run_lps_outflow;
    }
    
    private boolean calcIntfPercRates(double run_mobile_water){
        if(run_mobile_water > 0){
            double run_slope_weight = (Math.tan(this.run_slope * (Math.PI / 180.))) * this.par_soil_lat_vert_lps.getValue();
            
            /** potential part of percolation */
            double run_part_perc = (1 - run_slope_weight);
            if(run_part_perc > 1)
                run_part_perc = 1;
            else if(run_part_perc < 0)
                run_part_perc = 0;
            
            /** potential part of interflow */
            double run_part_intf = (1 - run_part_perc);
            
            this.run_interflow = run_mobile_water * run_part_intf;
            this.run_percolation = run_mobile_water * run_part_perc;
            
            /** checking if percolation rate is limited by parameter */
            double run_max_perc = this.par_soil_max_perc.getValue() * this.run_area;
            if(this.run_percolation > run_max_perc){
                double run_rest = this.run_percolation - run_max_perc;
                this.run_percolation = run_max_perc;
                this.run_interflow = this.run_interflow + run_rest;
            }
        }
        /** no MobileWater available */
        else{
            this.run_interflow = 0;
            this.run_percolation = 0;
        }
        return true;
    }
    
    private double calcDirectRunoff(){
        double run_direct_runoff = 0;
        if(this.run_act_dps > 0){
            double run_max_dep = 0;
            
            /** depression storage on slopes is half the normal dep. storage */
            if(this.run_slope > 5.0){
                run_max_dep = (this.in_soil_max_dps.getValue() * this.run_area) / 2;
            } else
                run_max_dep = this.in_soil_max_dps.getValue() * this.run_area;
            
            if(this.run_act_dps > run_max_dep){
                run_direct_runoff = this.run_act_dps - run_max_dep;
                this.run_act_dps = run_max_dep;
            }
        }
        return run_direct_runoff;
    }
    
    private boolean calcRD1RD2Out(){
        /** DIRECT OVERLANDFLOW */
        //switched off 15-03-2004
        //double RD1_output_factor = this.conc_index / this.parameter.getDouble("conc_recRD1");
        double run_rd1_output_factor = 1. / this.par_soil_conc_rd1.getValue();
        if(run_rd1_output_factor > 1)
            run_rd1_output_factor = 1;
        else if(run_rd1_output_factor < 0)
            run_rd1_output_factor = 0;
        
        /** real RD1 output */
        double run_rd1_output = this.run_overlandflow * run_rd1_output_factor;
        /** rest is put back to dep. Storage */
        this.run_act_dps = this.run_act_dps + (this.run_overlandflow - run_rd1_output);
        this.run_out_rd1 = this.run_out_rd1 + run_rd1_output;
        this.run_gen_rd1 = this.run_out_rd1;// - this.in_RD1;
        //this.in_RD1 = 0;
        /** lateral interflow */
        //switched of 15-03-2004
        //double RD2_output_factor = this.conc_index / this.parameter.getDouble("conc_recRD2");
        double run_rd2_output_factor = 1. / this.par_soil_conc_rd2.getValue();
        if(run_rd2_output_factor > 1)
            run_rd2_output_factor = 1;
        else if(run_rd2_output_factor < 0)
            run_rd2_output_factor = 0;
        
        /** real RD2 output */
        double run_rd2_output = this.run_interflow * run_rd2_output_factor;
        /** rest is put back to LPS Storage */
        this.run_act_lps = this.run_act_lps + (this.run_interflow - run_rd2_output);
        this.run_out_rd2 = this.run_out_rd2 + run_rd2_output;
        this.run_gen_rd2 = this.run_out_rd2;// - this.in_RD2;
        if(this.run_gen_rd2 < 0)
            this.run_gen_rd2 = 0;
        //this.in_RD2 = 0;
        
        this.run_overlandflow = 0;
        this.run_interflow = 0;
        return true;
    }
    
    private boolean calcDiffusion(){
        double run_diffusion;
        this.calcSoilSaturations(false);
        double run_delta_mps = this.run_max_mps - this.run_act_mps;
        if(this.run_sat_mps == 0.0)
            this.run_sat_mps = 0.000001;
        run_diffusion = this.run_act_lps * (1. - Math.exp((-1. * this.par_soil_diff_mps_lps.getValue()) / this.run_sat_mps)); 
        if(run_diffusion > this.run_act_lps)
            run_diffusion = this.run_act_lps;
        if(run_diffusion > run_delta_mps)
            run_diffusion = run_delta_mps;
        this.run_act_mps = this.run_act_mps + run_diffusion;
        this.run_act_lps = this.run_act_lps - run_diffusion;
        return true;
    }
    
    private boolean calcDiffusion_OBSOLETE(){
        double run_diffusion = 0;
        /** updating saturations */
        this.calcSoilSaturations(false);
        double run_delta_mps = this.run_max_mps - this.run_act_mps;
        //if sat_MPS is 0 the diffusion equation would produce an error,
        //for this (unlikely) case diffusion is set to zero
        if(this.run_sat_mps == 0.0){
            run_diffusion = 0;
        } else{
            double run_diff = this.par_soil_dist_mps_lps.getValue();
            
            //new equation like all other exps 04.03.04
            run_diffusion = this.run_act_lps * (1. - Math.exp((-1. * run_diff) / this.run_sat_mps)); 
        }
        
        if(run_diffusion > this.run_act_lps)
            run_diffusion = this.run_act_lps;
        
        
        /** MPS can take all the water from diffusion */
        if(run_diffusion < run_delta_mps){
            this.run_act_mps = this.run_act_mps + run_diffusion;
            this.run_act_lps = this.run_act_mps - run_diffusion;
        }
        /** MPS can take only part of the water */
        else{
            double run_rest = this.run_max_mps - this.run_act_mps;
            this.run_act_mps = this.run_max_mps;
            this.run_act_lps = this.run_act_lps - run_rest;
        }
        return true;
    }
}
