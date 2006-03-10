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

package org.jams.j2k.s_n;

import org.unijena.jams.data.*;
import org.unijena.jams.model.*;

/**
 *
 * @author c0krpe
 */
@JAMSComponentDescription(
        title="J2KGroundwater",
        author="Peter Krause modifications Manfred Fink",
        description="Description"
        )
        public class J2KProcessGroundwater extends JAMSComponent {
    
    /*
     *  Component variables
     */
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "attribute area"
            )
            public JAMSDouble area;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "attribute slope"
            )
            public JAMSDouble slope;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "maximum RG1 storage"
            )
            public JAMSDouble maxRG1;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "maximum RG2 storage"
            )
            public JAMSDouble maxRG2;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "recision coefficient k RG1"
            )
            public JAMSDouble kRG1;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "recision coefficient k RG2"
            )
            public JAMSDouble kRG2;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "actual RG1 storage"
            )
            public JAMSDouble actRG1;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "actual RG2 storage"
            )
            public JAMSDouble actRG2;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "RG1 inflow"
            )
            public JAMSDouble inRG1;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "RG2 inflow"
            )
            public JAMSDouble inRG2;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "RG1 outflow"
            )
            public JAMSDouble outRG1;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "RG2 outflow"
            )
            public JAMSDouble outRG2;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "RG1 generation"
            )
            public JAMSDouble genRG1;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "RG2 generation"
            )
            public JAMSDouble genRG2;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "percolation"
            )
            public JAMSDouble percolation;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "interflow"
            )
            public JAMSDouble interflow;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "portion of percolation to RG1 in l"
            )
            public JAMSDouble pot_RG1;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "portion of percolation to RG2 in l"
            )
            public JAMSDouble pot_RG2;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "portion of percolation to interflow in l"
            )
            public JAMSDouble partint;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "maximum soil storage"
            )
            public JAMSDouble maxSoilStorage;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "actual soil storage"
            )
            public JAMSDouble actSoilStorage;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.INIT,
            description = "RG1 correction factor"
            )
            public JAMSDouble gwRG1Fact;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.INIT,
            description = "RG2 correction factor"
            )
            public JAMSDouble gwRG2Fact;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.INIT,
            description = "RG1 RG2 distribution factor"
            )
            public JAMSDouble gwRG1RG2dist;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.INIT,
            description = "capilary rise factor"
            )
            public JAMSDouble gwCapRise;
    
    double run_maxRG1, run_maxRG2, run_actRG1, run_actRG2, run_inRG1, run_inRG2, run_outRG1, run_outRG2, run_genRG1, run_genRG2,
            run_k_RG1, run_k_RG2, run_RG1_rec, run_RG2_rec, run_maxSoilStor, run_actSoilStor, run_slope,
            run_percolation, run_interflow, run_pot_RG1, run_pot_RG2, run_partint;
    /*
     *  Component run stages
     */
    
    public void init() throws JAMSEntity.NoSuchAttributeException {
        
    }
    
    public void run() throws JAMSEntity.NoSuchAttributeException {
        this.run_maxRG1 = maxRG1.getValue();
        this.run_maxRG2 = maxRG2.getValue();
        this.run_actRG1 = actRG1.getValue();
        this.run_actRG2 = actRG2.getValue();
        this.run_inRG1 = inRG1.getValue();
        this.run_inRG2 = inRG2.getValue();
        
        this.run_maxSoilStor = maxSoilStorage.getValue();
        this.run_actSoilStor = actSoilStorage.getValue();
        this.run_percolation = percolation.getValue();
        this.run_interflow = interflow.getValue();
        
        this.run_outRG1 = 0;
        this.run_outRG2 = 0;
        this.run_genRG1 = 0;
        this.run_genRG2 = 0;
        
        this.run_k_RG1 = kRG1.getValue();
        this.run_k_RG2 = kRG2.getValue();
        
        this.run_RG1_rec = this.run_k_RG1 * this.gwRG1Fact.getValue();
        this.run_RG2_rec = this.run_k_RG2 * this.gwRG2Fact.getValue();
        
        this.run_slope = slope.getValue();
        
        this.replenishSoilStor();
        this.redistRG1_RG2_in();
        this.distRG1_RG2();
        //this.calcDeepSink();
        //this.calcExpGWout();
        this.calcLinGWout();
        
        actRG1.setValue(this.run_actRG1);
        actRG2.setValue(this.run_actRG2);
        outRG1.setValue(this.run_outRG1);
        outRG2.setValue(this.run_outRG2);
        genRG1.setValue(this.run_genRG1);
        genRG2.setValue(this.run_genRG2);
        inRG1.setValue(this.run_inRG1);
        inRG2.setValue(this.run_inRG2);
        interflow.setValue(this.run_interflow);
        actSoilStorage.setValue(this.run_actSoilStor);
        pot_RG1.setValue(this.run_pot_RG1);
        pot_RG2.setValue(this.run_pot_RG2);
        partint.setValue(this.run_partint);
    }
    
    public void cleanup() {
        
    }
    
    public boolean replenishSoilStor(){
        double deltaSoilStor = this.run_maxSoilStor - this.run_actSoilStor;
        double sat_SoilStor = 0;
        double inSoilStor = 0;
        if((this.run_actSoilStor > 0) && (this.run_maxSoilStor > 0)){
            sat_SoilStor = this.run_actSoilStor / this.run_maxSoilStor;
        } else
            sat_SoilStor = 0.000001;
        if(this.run_actRG2 > deltaSoilStor){
            double alpha = this.gwCapRise.getValue();
            inSoilStor = (deltaSoilStor) * (1. - Math.exp(-1*alpha / sat_SoilStor));
        }
        
        this.run_actSoilStor = this.run_actSoilStor + inSoilStor;
        this.run_actRG2 = this.run_actRG2 - inSoilStor;
        
        return true;
    }
    
    private boolean redistRG1_RG2_in(){
        if(this.run_inRG1 > 0){
            double deltaRG1 = this.run_maxRG1 - this.run_actRG1;
            if(this.run_inRG1 <= deltaRG1){
                this.run_actRG1 = this.run_actRG1 + this.run_inRG1;
                this.run_inRG1 = 0;
            } else{
                this.run_actRG1 = this.run_maxRG1;
                this.run_outRG1 = this.run_outRG1 + this.run_inRG1 - deltaRG1;
                this.run_inRG1 = 0;
            }
        }
        
        if(this.run_inRG2 > 0){
            double deltaRG2 = this.run_maxRG2 - this.run_actRG2;
            if(this.run_inRG2 <= deltaRG2){
                this.run_actRG2 = this.run_actRG2 + this.run_inRG2;
                this.run_inRG2 = 0;
            } else{
                this.run_actRG2 = this.run_maxRG2;
                this.run_outRG2 = this.run_outRG2 + this.run_inRG2 - deltaRG2;
                this.run_inRG2 = 0;
            }
        }
        
        return true;
    }
    
    private boolean distRG1_RG2(){
        double slope_weight = Math.tan(this.run_slope * (Math.PI / 180.));
        double gradh = ((1 - slope_weight) * this.gwRG1RG2dist.getValue());
        
        if(gradh < 0)
            gradh = 0;
        else if(gradh > 1)
            gradh = 1;
        
        this.run_pot_RG1 = ((1 - gradh) * this.run_percolation);
        this.run_pot_RG2 = (gradh * this.run_percolation);
        
        this.run_actRG1 = this.run_actRG1 + this.run_pot_RG1;
        this.run_actRG2 = this.run_actRG2 + this.run_pot_RG2;
        
        /** testing if inflows can be stored in groundwater storages */
        double delta_RG2 = this.run_actRG2 - this.run_maxRG2;
        if(delta_RG2 > 0){
            this.run_actRG1 = this.run_actRG1 + delta_RG2;
            this.run_actRG2 = this.run_maxRG2;
            this.run_pot_RG1 = run_pot_RG1 + delta_RG2;
            this.run_pot_RG2 = run_pot_RG2 - delta_RG2;
        }
        double delta_RG1 = this.run_actRG1 - this.run_maxRG1;
        if(delta_RG1 > 0){
            this.run_interflow = this.run_interflow + delta_RG1;
            this.run_actRG1 = this.run_maxRG1;
            this.run_pot_RG1 = run_pot_RG1 - delta_RG1;
            this.run_partint = delta_RG1;
        }
        
        return true;
    }
    
    private boolean calcLinGWout(){
        //double k_rg1 = this.conc_index / this.RG1_k;
        double k_rg1 = 1 / this.run_RG1_rec;
        if(k_rg1 > 1)
            k_rg1 = 1;
        double rg1_out = k_rg1 * this.run_actRG1;
        this.run_actRG1 = this.run_actRG1 - rg1_out;
        this.run_outRG1 = this.run_outRG1 + rg1_out;
        
        //double k_rg2 = this.conc_index / this.RG2_k;
        double k_rg2 = 1 / this.run_RG2_rec;
        if(k_rg2 > 1)
            k_rg2 = 1;
        double rg2_out = k_rg2 * this.run_actRG2;
        this.run_actRG2 = this.run_actRG2 - rg2_out;
        this.run_outRG2 = this.run_outRG2 + rg2_out;
        
        this.run_genRG1 = rg1_out;
        this.run_genRG2 = rg2_out;
        
        return true;
    }
}

/*
 			<component class="org.jams.j2k.s_n.J2KProcessGroundwater" name="J2KProcessGroundwater">
				<jamsvar name="area" provider="HRUContext" providervar="currentEntity.area"/>
				<jamsvar name="slope" provider="HRUContext" providervar="currentEntity.slope"/>
				<jamsvar name="maxRG1" provider="HRUContext" providervar="currentEntity.maxRG1"/>
				<jamsvar name="maxRG2" provider="HRUContext" providervar="currentEntity.maxRG2"/>
				<jamsvar name="kRG1" provider="HRUContext" providervar="currentEntity.RG1_k"/>
				<jamsvar name="kRG2" provider="HRUContext" providervar="currentEntity.RG2_k"/>
				<jamsvar name="actRG1" provider="HRUContext" providervar="currentEntity.actRG1"/>
				<jamsvar name="actRG2" provider="HRUContext" providervar="currentEntity.actRG2"/>
				<jamsvar name="inRG1" provider="HRUContext" providervar="currentEntity.inRG1"/>
				<jamsvar name="inRG2" provider="HRUContext" providervar="currentEntity.inRG2"/>
				<jamsvar name="outRG1" provider="HRUContext" providervar="currentEntity.outRG1"/>
				<jamsvar name="outRG2" provider="HRUContext" providervar="currentEntity.outRG2"/>
				<jamsvar name="genRG1" provider="HRUContext" providervar="currentEntity.genRG1"/>
				<jamsvar name="genRG2" provider="HRUContext" providervar="currentEntity.genRG2"/>
				<jamsvar name="percolation" provider="HRUContext" providervar="currentEntity.percolation"/>
				<jamsvar name="interflow" provider="HRUContext" providervar="currentEntity.outRD2"/>
				<jamsvar name="maxSoilStorage" provider="HRUContext" providervar="currentEntity.maxMPS"/>
				<jamsvar name="actSoilStorage" provider="HRUContext" providervar="currentEntity.actMPS"/>
				<jamsvar name="pot_RG1" provider="HRUContext" providervar="currentEntity.pot_RG1"/>
				<jamsvar name="pot_RG2" provider="HRUContext" providervar="currentEntity.pot_RG2"/>
				<jamsvar name="partint" provider="HRUContext" providervar="currentEntity.partint"/>
				<jamsvar name="gwRG1RG2dist" value="0.8"/>
				<jamsvar name="gwRG1Fact" value="1.0"/>
				<jamsvar name="gwRG2Fact" value="1.0"/>
				<jamsvar name="gwCapRise" value="0.0"/>
			</component>
 */