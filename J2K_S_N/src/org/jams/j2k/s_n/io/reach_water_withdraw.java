/*
 * reach_water_withdraw.java
 * Created on 28. Juni 2007, 17:39
 *
 * This file is part of JAMS
 * Copyright (C) 2007 FSU Jena, c8fima
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

package org.jams.j2k.s_n.io;

import org.unijena.jams.data.*;
import org.unijena.jams.model.*;

/**
 *
 * @author c8fima
 */
@JAMSComponentDescription(
title="Reach water  withdrawal",
        author="Manfred Fink",
        description="Takes water and nitrogen out of a reach segment"
        )
        public class reach_water_withdraw extends JAMSComponent {
    
    /*
     *  Component variables
     */
    
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "The reach collection"
            )
            public JAMSEntityCollection entities;
    
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READWRITE,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "Reach statevar RD1 inflow in l"
            )
            public JAMSDouble inRD1;
    
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READWRITE,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "Reach statevar RD2 inflow in l"
            )
            public JAMSDouble inRD2;
    
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READWRITE,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "Reach statevar RG1 inflow in l"
            )
            public JAMSDouble inRG1;
    
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READWRITE,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "Reach statevar RG2 inflow in l"
            )
            public JAMSDouble inRG2;
    
    
    
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "Water that leaves the reach out of the system in m³/s determinated by the file"
            )
            public JAMSDouble measured_waterloss;
    
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READWRITE,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "SurfaceN inflow in kgN"
            )
            public JAMSDouble SurfaceN_in;
    
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READWRITE,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "(fast) InterflowN inflow in kgN"
            )
            public JAMSDouble InterflowN_sum;
    
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READWRITE,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "(slow) InterflowN inflow in kgN"
            )
            public JAMSDouble N_RG1_in;
    
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READWRITE,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "GoundwaterN inflow in kgN"
            )
            public JAMSDouble N_RG2_in;
    
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.WRITE,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "Water that leaves the reach out of the system in m³/s determinated by the model"
            )
            public JAMSDouble model_waterloss;
    
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.WRITE,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "Nitrogen that leaves the reach out of the system in kgN determinated by the model"
            )
            public JAMSDouble Nitrogenloss;
    
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "ID of the reach which losses water and nitrogen"
            )
            public JAMSInteger reach_id;
    
    /*
     *  Component run stages
     */
    
    public void init() {
        
    }
    
    public void run() throws JAMSEntity.NoSuchAttributeException {
        
        JAMSEntity entity = entities.getCurrent();
        
        int runid =  (int)entity.getDouble("ID");
        
        if (runid == reach_id.getValue()){
        
        double sumwater = inRD1.getValue() + inRD2.getValue() + inRG1.getValue() + inRG2.getValue();
        
        double sumN = SurfaceN_in.getValue() + InterflowN_sum.getValue() + N_RG1_in.getValue() + N_RG2_in.getValue();
        
        double waterloss_run = measured_waterloss.getValue() * 86400000; //transformation from m³/s in l/d
        
        double losspart = 0;
        
        
        if (waterloss_run < sumwater){
            losspart =  waterloss_run / sumwater;
        } else{
            losspart = 1;
        }
        double keep_part = 1 - losspart;
        
        inRD1.setValue(keep_part * inRD1.getValue());
        inRD2.setValue(keep_part * inRD2.getValue());
        inRG1.setValue(keep_part * inRG1.getValue());
        inRG2.setValue(keep_part * inRG2.getValue());
        
        SurfaceN_in.setValue(keep_part * SurfaceN_in.getValue());
        InterflowN_sum.setValue(keep_part * InterflowN_sum.getValue());
        N_RG1_in.setValue(keep_part * N_RG1_in.getValue());
        N_RG2_in.setValue(keep_part * N_RG2_in.getValue());
        
        double run_model_waterloss = sumwater * losspart / 86400000;
        double run_N_loss = sumN * losspart;
        
        model_waterloss.setValue(run_model_waterloss);
        
        Nitrogenloss.setValue(run_N_loss);
    }
    }
    
    public void cleanup() {
        
    }
}
