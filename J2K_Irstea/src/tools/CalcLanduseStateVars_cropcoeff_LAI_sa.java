/*
 * CalcLanduseStateVars_cropcoeff.java
 * Created on 17. April 2012 after CalcLanduseStateVars.java by P. Krause
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
package tools;

import jams.data.*;
import jams.model.*;

/**
 *
 * @author c0krpe
 */
@JAMSComponentDescription(
        title="CalcLanduseStateVars_cropcoeff",
        author="Peter Krause + Francois Tilmant",
        description="Calculates landuse state variables for a modelling unit"
        + "For evapotranspiration calculation using crop coeff."
        + "The calculation is done for a standard year (i.e. 366 days or 8784 hours)."
        + "The module can be used in hourly, daily and monthly resolution."
        + "FT : change the approach of LAI computation. We take the same as Kc."
        + "Modified by Ivan horner to add 'adaptation factors' to distributed parameters.",
        version="1.0_1",
        date="2011-05-30 + 2013-08-07"
        )
        public class CalcLanduseStateVars_cropcoeff_LAI_sa extends JAMSComponent {
    
    /*
     *  Component variables
     */
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "The current spatial entity"
            )
            public Attribute.EntityCollection st_entities;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "Array with LAI values for a standard year"
            )
            public Attribute.DoubleArray in_lai_array;
    
/*    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "Array with eff. Height values for a standard year"
            )
            public JAMSDoubleArray effHArray;
            * 
            */
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "Monthly crop coefficient values",
            lowerBound = 0,
            upperBound = 2,
            unit = "-"
            )
            public Attribute.DoubleArray in_crop_coeff_array;
      
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "temporal resolution [d | h | m]"
            )
            public Attribute.String par_temp_res;
            
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Crop coefficient 'multiplicative' adaptation factor",
            defaultValue = "1"
            )
            public Attribute.Double par_crop_coef_maf;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Crop coefficient 'additive' adaptation factor",
            defaultValue = "0.0"
            )
            public Attribute.Double par_crop_coef_aaf;
            
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Leaf Area Index 'multiplicative' adaptation factor",
            defaultValue = "1"
            )
            public Attribute.Double par_lai_maf;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Leaf Area Index 'additive' adaptation factor",
            defaultValue = "0.0"
            )
            public Attribute.Double par_lai_aaf;
    
    /* Following variables are not used ?
    * Michael Rabotin mars 2025
    */
    int[] run_month_mean = {15,45,74,105,135,166,196,227,258,288,319,349};
    int run_old_month = 0;
    double run_crop = 0;
    
    /*
     *  Component run stages
     */
    
    public void init() throws Attribute.Entity.NoSuchAttributeException {
      
    }
    
    public void run() throws Attribute.Entity.NoSuchAttributeException {
        
        Attribute.Entity entity = st_entities.getCurrent();
        
        double[] run_lai_vals = null; 
       // double[] effH_vals = null;
        if(this.par_temp_res == null || this.par_temp_res.getValue().equals("d") || this.par_temp_res.getValue().equals("h") || this.par_temp_res.getValue().equals("m")){
            run_lai_vals = new double[12];
            //effH_vals = new double[366];
        }
         
        String run_lai_name = "LAI_";
        for(int j = 0; j < 12; j++){
            int run_count = j+1;
            String run_lai_loop_name = run_lai_name + run_count;
            run_lai_vals[j] = entity.getDouble(run_lai_loop_name);
            run_lai_vals[j] = run_lai_vals[j] * this.par_lai_maf.getValue() + this.par_lai_aaf.getValue();
            if (run_lai_vals[j] < 0)
                run_lai_vals[j] = 0;
        }
        
        
        in_lai_array.setValue(run_lai_vals);
        //effHArray.setValue(effH_vals);
        
        double[] run_crop_coeff = new double[12];
        String run_crop_coeff_name = "Kc_";
        for(int i = 0; i < 12; i++){
            int run_count = i+1;
            String run_loop_name = run_crop_coeff_name + run_count;
            run_crop_coeff[i] = entity.getDouble(run_loop_name);
            run_crop_coeff[i] = run_crop_coeff[i] * this.par_crop_coef_maf.getValue() + this.par_crop_coef_aaf.getValue();
            if (run_crop_coeff[i] < 0)
                run_crop_coeff[i] = 0;
            entity.setDouble(run_loop_name+"_test", run_crop_coeff[i]); // what's that?
        }
        in_crop_coeff_array.setValue(run_crop_coeff);

    }
    
    public void cleanup() {
        
    }
    
     
}

 
