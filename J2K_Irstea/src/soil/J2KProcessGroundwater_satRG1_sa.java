/*
 * J2KProcessGroundwater.java
 * Created on 25. November 2005, 16:54
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

import jams.data.*;
import jams.model.*;

/**
 *
 * @author c0krpe
 */
@JAMSComponentDescription(
        title="J2KGroundwater",
        author="Peter Krause + Francois Tilmant",
        description="A two-component groundwater module + filling rate RG1 and RG2."
        + "Modified by Ivan horner to add 'adaptation factors' to distributed parameters.",
        version="1.0_2",
        date="2013-04-26 + 2013-08-06"
        )
        public class J2KProcessGroundwater_satRG1_sa extends JAMSComponent {
    
    /*
     *  Component variables
     */
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "attribute slope",
            unit = "deg"
            )
            public Attribute.Double par_slope;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "maximum RG1 storage",
            unit = "L"
            )
            public Attribute.Double par_max_rg1;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "maximum RG2 storage",
            unit = "L"
            )
            public Attribute.Double par_max_rg2;
    
        @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "filling rate of RG1 storage",
            unit = "L"
            )
            public Attribute.Double st_sat_rg1;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "filling rate of RG2 storage",
            unit = "L"
            )
            public Attribute.Double st_sat_rg2;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "recision coefficient k RG1 for one time step",
            lowerBound = 1.0,
            upperBound = 500.0,
            defaultValue = "10.0"
            )
            public Attribute.Double par_k_rg1;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "recision coefficient k RG2 for one time step",
            lowerBound = 1.0,
            upperBound = 700.0,
            defaultValue = "10.0"
            )
            public Attribute.Double par_k_rg2;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "actual RG1 storage",
            unit = "L"
            )
            public Attribute.Double out_act_rg1;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "actual RG2 storage",
            unit = "L"
            )
            public Attribute.Double out_act_rg2;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "RG1 inflow",
            unit = "L"
            )
            public Attribute.Double out_in_rg1;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "RG2 inflow",
            unit = "L"
            )
            public Attribute.Double out_in_rg2;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "RG1 outflow",
            unit = "L"
            )
            public Attribute.Double out_rg1;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "RG2 outflow",
            unit = "L"
            )
            public Attribute.Double out_rg2;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "RG1 generation",
            unit = "L"
            )
            public Attribute.Double out_gen_rg1;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "RG2 generation",
            unit = "L"
            )
            public Attribute.Double out_gen_rg2;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "percolation for one time step",
            unit = "L"
            )
            public Attribute.Double in_percolation;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "gwExcess",
            unit = "L"
            )
            public Attribute.Double par_gw_excess;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "maximum soil storage",
            unit = "L"
            )
            public Attribute.Double st_max_soil_storage;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "actual soil storage",
            unit = "L"
            )
            public Attribute.Double in_act_soil_storage;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "RG1 correction factor",
            lowerBound = 0.0,
            upperBound = 10.0,
            defaultValue = "1.0"
            )
            public Attribute.Double par_gw_rg1_fact;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "RG1 recession rate 'additive' adaptation factor",
            lowerBound = 0.0,
            upperBound = 10.0,
            defaultValue = "0.0"
            )
            public Attribute.Double par_k_rg1_aaf;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "RG2 correction factor",
            lowerBound = 0.0,
            upperBound = 10.0,
            defaultValue = "1.0"
            )
            public Attribute.Double par_gw_rg2_fact;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "RG1 RG2 distribution factor",
            lowerBound = 0.0,
            upperBound = 10.0,
            defaultValue = "1.0"
            )
            public Attribute.Double par_gw_rg1_rg2_dist;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "capilary rise factor",
            lowerBound = 0.0,
            upperBound = 10.0,
            defaultValue = "0.0"
            )
            public Attribute.Double par_gw_cap_rise;
    
    double run_max_rg1, run_max_rg2, run_act_rg1, run_act_rg2, run_in_rg1, run_in_rg2, run_out_rg1, run_out_rg2, run_gen_rg1, run_gen_rg2,
           run_k_rg1, run_k_rg2, run_rg1_rec, run_rg2_rec, run_max_soil_stor, run_act_soil_stor, run_slope,
           run_percolation, run_gw_excess, run_sat_rg1, run_sat_rg2;
    /*
     *  Component run stages
     */
    
    public void init() {
        
    }
    
    public void run() {
        this.run_max_rg1 = par_max_rg1.getValue();
        this.run_max_rg2 = par_max_rg2.getValue();
        this.run_act_rg1 = out_act_rg1.getValue();
        this.run_act_rg2 = out_act_rg2.getValue();
        this.run_in_rg1 = out_in_rg1.getValue();
        this.run_in_rg2 = out_in_rg2.getValue();
        
        this.run_max_soil_stor = st_max_soil_storage.getValue();
        this.run_act_soil_stor = in_act_soil_storage.getValue();
        this.run_percolation = in_percolation.getValue();
        this.run_gw_excess    = par_gw_excess.getValue();
        
        this.run_out_rg1 = 0;
        this.run_out_rg2 = 0;
        this.run_gen_rg1 = 0;
        this.run_gen_rg2 = 0;
        this.run_sat_rg1 = 0;
        this.run_sat_rg2 = 0;
        
        this.run_k_rg1 = par_k_rg1.getValue();
        this.run_k_rg2 = par_k_rg2.getValue();
        
        this.run_k_rg1 = this.run_k_rg1 * this.par_gw_rg1_fact.getValue() + this.par_k_rg1_aaf.getValue();
        if (this.run_k_rg1 < 1) {
            this.run_k_rg1 = 1; // minimum recession coefficient is set to 1 day
        }
        this.run_rg1_rec = this.run_k_rg1;
        this.run_rg2_rec = this.run_k_rg2 * this.par_gw_rg2_fact.getValue();
        
        this.run_slope = par_slope.getValue();
        
        this.replenishSoilStor();
        this.redistRG1RG2In();
        this.distRG1RG2();
        //this.calcDeepSink();
        //this.calcExpGWout();
        this.calcLinGWout();
        
        this.run_sat_rg1 = this.run_act_rg1/this.run_max_rg1;
        this.run_sat_rg2 = this.run_act_rg2/this.run_max_rg2;
        
        out_act_rg1.setValue(this.run_act_rg1);
        out_act_rg2.setValue(this.run_act_rg2);
        out_rg1.setValue(this.run_out_rg1);
        out_rg2.setValue(this.run_out_rg2);
        out_gen_rg1.setValue(this.run_gen_rg1);
        out_gen_rg2.setValue(this.run_gen_rg2);
        out_in_rg1.setValue(this.run_in_rg1);
        out_in_rg2.setValue(this.run_in_rg2);
        par_gw_excess.setValue(this.run_gw_excess);
        in_act_soil_storage.setValue(this.run_act_soil_stor);
        st_sat_rg1.setValue(this.run_sat_rg1);
        st_sat_rg2.setValue(this.run_sat_rg2);
    }
    
    public void cleanup() {
        
    }
    
    public boolean replenishSoilStor(){
        double run_delta_soil_stor = this.run_max_soil_stor - this.run_act_soil_stor;
        double run_sat_soil_stor = 0;
        double run_in_soil_stor = 0;
        if((this.run_act_soil_stor > 0) && (this.run_max_soil_stor > 0)){
            run_sat_soil_stor = this.run_act_soil_stor / this.run_max_soil_stor;
        }
        else
            run_sat_soil_stor = 0.000001;
        if(this.run_act_rg2 > run_delta_soil_stor){
            double run_alpha = this.par_gw_cap_rise.getValue();
            if (run_alpha < 0.0)
                run_alpha = 0.0;
            run_in_soil_stor = (run_delta_soil_stor) * (1. - Math.exp(-1*run_alpha / run_sat_soil_stor));
        }
        if(run_act_rg2 >= run_in_soil_stor){
            this.run_act_soil_stor = this.run_act_soil_stor + run_in_soil_stor;
            this.run_act_rg2 = this.run_act_rg2 - run_in_soil_stor;
        }
        else{
            this.run_act_soil_stor = this.run_act_soil_stor + this.run_act_rg2;
            this.run_act_rg2 = 0;
        }
        
        return true;
    }
    
    private boolean redistRG1RG2In(){
        if(this.run_in_rg1 > 0){
            double run_delta_rg1 = this.run_max_rg1 - this.run_act_rg1;
            if(this.run_in_rg1 <= run_delta_rg1){
                this.run_act_rg1 = this.run_act_rg1 + this.run_in_rg1;
                this.run_in_rg1 = 0;
            }
            else{
                this.run_act_rg1 = this.run_max_rg1;
                this.run_out_rg1 = this.run_out_rg1 + this.run_in_rg1 - run_delta_rg1;
                this.run_in_rg1 = 0;
            }
        }
        
        if(this.run_in_rg2 > 0){
            double run_delta_rg2 = this.run_max_rg2 - this.run_act_rg2;
            if(this.run_in_rg2 <= run_delta_rg2){
                this.run_act_rg2 = this.run_act_rg2 + this.run_in_rg2;
                this.run_in_rg2 = 0;
            }
            else{
                this.run_act_rg2 = this.run_max_rg2;
                this.run_out_rg2 = this.run_out_rg2 + this.run_in_rg2 - run_delta_rg2;
                this.run_in_rg2 = 0;
            }
        }
        
        return true;
    }
    
    private boolean distRG1RG2(){
        double run_slope_weight = Math.tan(this.run_slope * (Math.PI / 180.));
        double run_gradh = ((1 - run_slope_weight) * this.par_gw_rg1_rg2_dist.getValue());
        
        if(run_gradh < 0)
            run_gradh = 0;
        else if(run_gradh > 1)
            run_gradh = 1;
        
        double run_pot_rg1 = ((1 - run_gradh) * this.run_percolation);
        double run_pot_rg2 = (run_gradh * this.run_percolation);
        
        this.run_act_rg1 = this.run_act_rg1 + run_pot_rg1;
        this.run_act_rg2 = this.run_act_rg2 + run_pot_rg2;
        
        /** testing if inflows can be stored in groundwater storages */
        double run_delta_rg2 = this.run_act_rg2 - this.run_max_rg2;
        if(run_delta_rg2 > 0){
            this.run_act_rg1 = this.run_act_rg1 + run_delta_rg2;
            this.run_act_rg2 = this.run_max_rg2;
        }
        double run_delta_rg1 = this.run_act_rg1 - this.run_max_rg1;
        if(run_delta_rg1 > 0){
            this.run_gw_excess = this.run_gw_excess + run_delta_rg1;
            this.run_act_rg1 = this.run_max_rg1;
        }
        if(run_delta_rg1 > 0){
            //getModel().getRuntime().println("interflow surplus in gw: "+delta_RG1);
        }
        return true;
    }
    
    private boolean calcLinGWout(){
        //double k_rg1 = this.conc_index / this.RG1_k;
        double run_k_rg1 = 1 / this.run_rg1_rec;
        if(run_k_rg1 > 1)
            run_k_rg1 = 1;
        double run_rg1_out = run_k_rg1 * this.run_act_rg1;
        this.run_act_rg1 = this.run_act_rg1 - run_rg1_out;
        this.run_out_rg1 = this.run_out_rg1 + run_rg1_out;
        
        //double k_rg2 = this.conc_index / this.RG2_k;
        double run_k_rg2 = 1 / this.run_rg2_rec;
        if(run_k_rg2 > 1)
            run_k_rg2 = 1;
        double run_rg2_out = run_k_rg2 * this.run_act_rg2;
        this.run_act_rg2 = this.run_act_rg2 - run_rg2_out;
        this.run_out_rg2 = this.run_out_rg2 + run_rg2_out;
        
        this.run_gen_rg1 = run_rg1_out;
        this.run_gen_rg2 = run_rg2_out;
        
        return true;
    }
}
