/*
 * InitJ2KProcessGroundwater.java
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
        author="Peter Krause",
        description="Initialises the J2KGroundwater module by multiplying the "
        + "maximum storage capacity of the two groundwater storages RG1 and RG2"
        + "by the area of the respective modelling unit to provide them with"
        + "absolute storage capacity values in litres. Secondly the actual"
        + "content of the two storages can be set to a relative amount."
        + "Modified by Ivan horner to add 'adaptation factors' to distributed parameters.",
        version="1.0_0",
        date="2010-10-29"
        )
        public class InitJ2KProcessGroundwater_sa extends JAMSComponent {
    
    /*
     *  Component variables
     */
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "The collection of model entities"
            )
            public Attribute.EntityCollection st_entities;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "maximum RG1 storage",
            lowerBound = 0,
            //upperBound = infinity,
            unit="L"
            )
            public Attribute.Double st_max_rg1;
            
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "maximum RG1 storage 'multiplicative' adaptation factor",
            defaultValue = "1.0",
            unit="-"
            )
            public Attribute.Double par_rg1_max_maf;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "maximum RG1 storage 'additive' adaptation factor",
            defaultValue = "0.0",
            unit="-"
            )
            public Attribute.Double par_rg1_max_aaf;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "maximum RG2 storage",
            lowerBound = 0,
            //upperBound = infinity,
            unit="L"
            )
            public Attribute.Double st_max_rg2;
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "actual RG1 storage",
            lowerBound = 0,
            //upperBound = infinity,
            unit="L"
            )
            public Attribute.Double out_act_rg1;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "actual RG2 storage",
            lowerBound = 0,
            //upperBound = infinity,
            unit="L"
            )
            public Attribute.Double out_act_rg2;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "relative initial RG1 storage",
            lowerBound = 0,
            upperBound = 1.0,
            unit="n/a",
            defaultValue = "0.0"
            )
            public Attribute.Double par_init_rg1;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "relative initial RG2 storage",
            lowerBound = 0,
            upperBound = 1.0,
            unit="n/a",
            defaultValue = "0.0"
            )
            public Attribute.Double par_init_rg2;
    
    
    /*
     *  Component run stages
     */
    
    public void init() {
        
    }
    
    public void run() {
        
        Attribute.Entity run_entity = st_entities.getCurrent();
        double run_area = run_entity.getDouble("area");
        double run_rg1_max = run_entity.getDouble("RG1_max");
        run_rg1_max = run_rg1_max * this.par_rg1_max_maf.getValue()+ this.par_rg1_max_aaf.getValue();
        if (run_rg1_max < 1) {
            run_rg1_max = 1;
        }
        st_max_rg1.setValue(run_rg1_max * run_area);
        st_max_rg2.setValue(run_entity.getDouble("RG2_max") * run_area);
        
        out_act_rg1.setValue(st_max_rg1.getValue() * par_init_rg1.getValue());
        out_act_rg2.setValue(st_max_rg2.getValue() * par_init_rg2.getValue());       
    }
    
    public void cleanup() {
        
    }
    
   
}
