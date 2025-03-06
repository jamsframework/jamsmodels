/*
 * InitJ2KProcessLumpedSoilWaterStates.java
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

import jams.data.*;
import jams.model.*;

/**
 *
 * @author Peter Krause
 */
@JAMSComponentDescription(
        title="InitJ2KProcessLumpedSoilWaterStates",
        author="Peter Krause",
        description="Initalises the states of the lumpedSoilWater module."
        + "Modified by Ivan horner to add 'adaptation factors' to distributed parameters.",
        version="1.0_0",
        date="2010-10-29"
        )
        public class InitJ2KProcessLumpedSoilWaterStates_sa extends JAMSComponent {
    
    /*
     *  Component variables
     */
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "The model entity set"
            )
            public Attribute.EntityCollection st_entities;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "attribute area",
            unit="m^2"
            )
            public Attribute.Double par_area;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "field capacity adaptation factor",
            unit="n/a",
            lowerBound = 0,
            //upperBound = 1000000,
            defaultValue = "1.0"
            )
            public Attribute.Double par_fc_adaptation;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "air capacity adaptation factor",
            unit="n/a",
            lowerBound = 0,
            //upperBound = 1000000,
            defaultValue = "1.0"
            )
            public Attribute.Double par_ac_adaptation;
    
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "HRU statevar rooting depth",
            unit="dm",
            lowerBound = 0
            //upperBound = 1000000
            )
            public Attribute.Double st_root_depth;    
            
    
            

    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "HRU attribute maximum MPS",
            unit="L",
            lowerBound = 0
            //upperBound = 1000000
            )
            public Attribute.Double out_max_mps;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "HRU attribute maximum MPS 'additive' adaptation factor",
            unit="L",
            lowerBound = 0,
            defaultValue = "0.0"
            //upperBound = 1000000
            )
            public Attribute.Double par_max_mps_aaf;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "HRU attribute maximum LPS",
            unit="L",
            lowerBound = 0
            //upperBound = 1000000
            )
            public Attribute.Double out_max_lps;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "HRU attribute maximum LPS 'additive' adaptation factor",
            unit="L",
            lowerBound = 0,
            defaultValue = "0.0"
            //upperBound = 1000000
            )
            public Attribute.Double par_max_lps_aaf;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "HRU state var actual MPS",
            unit="L",
            lowerBound = 0
            //upperBound = 1000000
            )
            public Attribute.Double st_act_mps;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "HRU state var actual LPS",
            unit="L",
            lowerBound = 0
            //upperBound = 1000000
            )
            public Attribute.Double st_act_lps;
   
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "HRU state var saturation of MPS",
            unit="n/a",
            lowerBound = 0,
            upperBound = 1
            )
            public Attribute.Double st_sat_mps;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "HRU state var saturation of LPS",
            unit="n/a",
            lowerBound = 0,
            upperBound = 1
            )
            public Attribute.Double st_sat_lps;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "HRU state var saturation of whole soil",
            unit="n/a",
            lowerBound = 0,
            upperBound = 1
            )
            public Attribute.Double st_sat_soil;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "start saturation of LPS",
            unit="n/a",
            lowerBound = 0,
            upperBound = 1,
            defaultValue = "0.0"
            )
            public Attribute.Double par_sat_start_lps;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "start saturation of MPS",
            unit="n/a",
            lowerBound = 0,
            upperBound = 1,
            defaultValue = "0.0"
            )
            public Attribute.Double par_sat_start_mps;
    
    
    
    
    /*
     *  Component run stages
     */
    
    public void init() {
        
        
    }
    
    public void run() {
        
        Attribute.Entity run_entity = st_entities.getCurrent();
        
        double run_root_depth = this.st_root_depth.getValue();
        double run_mx_mps = 0;
        String run_a_name_fc = "fc_";
        for(int d = 0; d < run_root_depth; d++){
            int run_count = d + 1;
            String run_mps_desc = run_a_name_fc + run_count;
            try {
                double run_mps_val = run_entity.getDouble(run_mps_desc);
                run_mx_mps = run_mx_mps + run_mps_val;
            } catch (Attribute.Entity.NoSuchAttributeException nsae) {
                getModel().getRuntime().sendErrorMsg(nsae.getMessage() + "\n"
                        + "This problem typically occurs if the root depht of "
                        + "current land-use is higer than the soil depht.\n"
                        + "Please provide additional field capacity (fc_*) "
                        + "columns in your soil parameter file or reduce the "
                        + "root depht!");
            }
        }
        run_mx_mps = run_mx_mps + this.par_max_mps_aaf.getValue();
        run_mx_mps = run_mx_mps * this.par_area.getValue();
        run_mx_mps = run_mx_mps * this.par_fc_adaptation.getValue();
        if (run_mx_mps < 1){
            run_mx_mps = 1;
        }
        
        double run_mx_lps = run_entity.getDouble("aircap") + this.par_max_lps_aaf.getValue();
        run_mx_lps = run_mx_lps * this.par_ac_adaptation.getValue() * par_area.getValue();
        if (run_mx_lps < 1){
            run_mx_lps = 1;
        }


        if(par_sat_start_lps != null){
        	this.st_act_lps.setValue(run_mx_lps * this.par_sat_start_lps.getValue());
        }
        
        if(par_sat_start_mps != null){
        	this.st_act_mps.setValue(run_mx_mps * this.par_sat_start_mps.getValue());
        }
        
        this.out_max_mps.setValue(run_mx_mps);
        this.out_max_lps.setValue(run_mx_lps);
        this.st_sat_mps.setValue(this.st_sat_mps.getValue()/run_mx_mps);
        this.st_sat_lps.setValue(this.st_sat_lps.getValue()/run_mx_lps);
        this.st_sat_soil.setValue((this.st_sat_mps.getValue()+this.st_sat_lps.getValue()) / (run_mx_mps+run_mx_lps));
    }
    
    public void cleanup() {
        
    }
    
    
}
