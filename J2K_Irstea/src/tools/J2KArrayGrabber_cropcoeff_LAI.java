/*
 * J2KArrayGrabber_cropcoeff.java
 * Created on 17 April 2012
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

/*
 <component class="org.unijena.j2k.J2KArrayGrabber" name="j2kArrayGrabber">
    <jamsvar name="time" provider="TemporalContext" providervar="current"/>
    <jamsvar name="LAIArray" provider="HRUContext" providervar="currentEntity.LAIArray"/>
    <jamsvar name="effHArray" provider="HRUContext" providervar="currentEntity.effHArray"/>
    <jamsvar name="slAsCfArray" provider="HRUContext" providervar="currentEntity.slAsCfArray"/>
    <jamsvar name="rsc0Array" provider="HRUContext" providervar="currentEntity.rsc0Array"/>
    <jamsvar name="extRadArray" provider="HRUContext" providervar="currentEntity.extRadArray"/>
    <jamsvar name="actLAI" provider="HRUContext" providervar="currentEntity.actLAI"/>
    <jamsvar name="actEffH" provider="HRUContext" providervar="currentEntity.actEffH"/>
    <jamsvar name="actSlAsCf" provider="HRUContext" providervar="currentEntity.actSlAsCf"/>
    <jamsvar name="actRsc0" provider="HRUContext" providervar="currentEntity.actRsc0"/>
    <jamsvar name="actExtRad" provider="HRUContext" providervar="currentEntity.actExtRad"/>
    <jamsvar name="tempRes" value="d"/>
</component>
 */

package tools;

import jams.data.*;
import jams.model.*;

/**
 *
 * @author Peter Krause
 */
@JAMSComponentDescription(
        title="J2KArrayGrabber_cropcoeff",
        author="Peter Krause",
        description="This component selects data values from arrays representing"
        + "a standard year.",
        version="1.0_0",
        date="2012-04-17"
        )
        public class J2KArrayGrabber_cropcoeff_LAI extends JAMSComponent {
    
    /*
     *  Component variables
     */
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "temporal resolution [m | d | h]"
            )
            public Attribute.String par_temp_res;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "time"
            )
            public Attribute.Calendar par_time;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "extraTerrRadiationArray"
            )
            public Attribute.DoubleArray in_ext_rad_array;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "LeafAreaIndexArray"
            )
            public Attribute.DoubleArray in_lai_array;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "EffectiveHeightArray"
            )
            public Attribute.DoubleArray in_eff_h_array;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "rsc0 Array"
            )
            public Attribute.DoubleArray in_rsc0_array;
    
        @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "CropCoeff Array"
            )
            public Attribute.DoubleArray in_crop_coeff_array;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "slopeAscpectCorrectionFactorArray"
            )
            public Attribute.DoubleArray par_sl_as_c_f_array;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "actExtraTerrRadiation"
            )
            public Attribute.Double out_act_ext_rad;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "actLAI"
            )
            public Attribute.Double out_act_lai;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "actEffH"
            )
            public Attribute.Double out_act_effh;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "actRsc0"
            )
            public Attribute.Double out_act_rsc0;
    
        @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "actCropCoeff"
            )
            public Attribute.Double out_act_crop_coeff;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Haude factor array"
            )
            public Attribute.DoubleArray par_haude_factor_array;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "actHaudeFactor",
            defaultValue="0"
            )
            public Attribute.Double par_act_haude_factor;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "actSlopeAspectCorrectionFactor"
            )
            public Attribute.Double par_act_sl_as_c_f;
    /*
     *  Component run stages
     */
    
    public void init() throws Attribute.Entity.NoSuchAttributeException{
        
    }
    
    public void run() throws Attribute.Entity.NoSuchAttributeException{
    	
        int run_month_count = par_time.get(Attribute.Calendar.MONTH);
        int run_day_count = par_time.get(Attribute.Calendar.DAY_OF_YEAR) - 1;
        int run_hour_count = par_time.get(Attribute.Calendar.HOUR_OF_DAY) + (24 * run_day_count);
        
        double run_in_lai = -9999;
        double run_in_effh = -9999;
        double run_in_ext_rad = -9999;
        double run_in_scf = -9999;
        double run_in_rsc0 = -9999;
        double run_in_cropcoeff = -9999;
        double run_in_haudef = -9999;
        
        if(this.in_rsc0_array != null)
            run_in_rsc0 = this.in_rsc0_array.getValue()[run_month_count];
        
        if(this.in_crop_coeff_array != null)
            run_in_cropcoeff = this.in_crop_coeff_array.getValue()[run_month_count];
        
        if(this.par_haude_factor_array != null)
            run_in_haudef = this.par_haude_factor_array.getValue()[run_month_count];
        
        if(this.in_lai_array != null)
            run_in_lai = this.in_lai_array.getValue()[run_month_count];
        
        this.out_act_lai.setValue(run_in_lai);
        this.out_act_effh.setValue(run_in_effh);
        this.out_act_rsc0.setValue(run_in_rsc0);
        this.out_act_crop_coeff.setValue(run_in_cropcoeff);
        this.par_act_sl_as_c_f.setValue(run_in_scf);
        this.out_act_ext_rad.setValue(run_in_ext_rad);
        this.par_act_haude_factor.setValue(run_in_haudef);
        
    }
    
    public void cleanup() {
        
    }
    
}
