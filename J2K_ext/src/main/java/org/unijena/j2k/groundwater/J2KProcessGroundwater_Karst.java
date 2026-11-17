/*
 * J2KProcessGroundwater.java
 * Created on 25. November 2005, 16:54
 *
 * This file is part of JAMS
 * Copyright (C) 2005 FSU Jena, c0krpe
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA
 *
 */
package org.unijena.j2k.groundwater;

import jams.data.*;
import jams.model.*;

/**
 *
 * @author c0krpe
 */
@JAMSComponentDescription(
        title = "J2KGroundwater",
        author = "Peter Krause & Manfred Fink",
        description = "A two-component groundwater module with deepsink",
        version = "1.0_1",
        date = "2018-06-29"
)

@VersionComments(entries = {
    @VersionComments.Entry(version = "1.0_0", comment = "Initial version"),
    @VersionComments.Entry(version = "1.0_1", comment = "Karst version")
})

public class J2KProcessGroundwater_Karst extends JAMSComponent {

    /*
     *  Component variables
     */
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "attribute area",
            unit = "m²"
    )
    public Attribute.Double area;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "attribute slope",
            unit = "deg"
    )
    public Attribute.Double slope;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "maximum RG1 storage",
            unit = "L"
    )
    public Attribute.Double maxRG1;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "maximum RG2 storage",
            unit = "L"
    )
    public Attribute.Double maxRG2;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "hru attribute",
            unit = "mm"
    )
    public Attribute.Double RG1_max;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "hru attribute",
            unit = "mm"
    )
    public Attribute.Double RG2_max;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "recision coefficient k RG1 for one time step",
            lowerBound = 1.0,
            upperBound = 500.0,
            defaultValue = "10.0"
    )
    public Attribute.Double kRG1;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "recision coefficient k RG2 for one time step",
            lowerBound = 1.0,
            upperBound = 700.0,
            defaultValue = "10.0"
    )
    public Attribute.Double kRG2;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "actual RG1 storage",
            unit = "L"
    )
    public Attribute.Double actRG1;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "actual RG2 storage",
            unit = "L"
    )
    public Attribute.Double actRG2;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "RG1 inflow",
            unit = "L"
    )
    public Attribute.Double inRG1;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "RG2 inflow",
            unit = "L"
    )
    public Attribute.Double inRG2;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "RG1 outflow",
            unit = "L"
    )
    public Attribute.Double outRG1;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "RG2 outflow",
            unit = "L"
    )
    public Attribute.Double outRG2;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "deep sink RG1 outflow",
            unit = "L"
    )
    public Attribute.Double deep_outRG1;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "deep sink RG2 outflow",
            unit = "L"
    )
    public Attribute.Double deep_outRG2;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "RG1 generation",
            unit = "L"
    )
    public Attribute.Double genRG1;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "RG2 generation",
            unit = "L"
    )
    public Attribute.Double genRG2;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "RG1 saturation",
            unit = "-"
    )
    public Attribute.Double satRG1;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "RG2 saturation",
            unit = "-"
    )
    public Attribute.Double satRG2;
    
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "percolation for one time step",
            unit = "L"
    )
    public Attribute.Double percolation;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "gwExcess",
            unit = "L"
    )
    public Attribute.Double gwExcess;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "maximum soil storage",
            unit = "L"
    )
    public Attribute.Double maxSoilStorage;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "actual soil storage",
            unit = "L"
    )
    public Attribute.Double actSoilStorage;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "RG1 correction factor",
            lowerBound = 0.0,
            upperBound = 10.0,
            defaultValue = "1.0"
    )
    public Attribute.Double gwRG1Fact;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "RG2 correction factor",
            lowerBound = 0.0,
            upperBound = 10.0,
            defaultValue = "1.0"
    )
    public Attribute.Double gwRG2Fact;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "RG1 RG2 distribution factor",
            lowerBound = 0.0,
            upperBound = 10.0,
            defaultValue = "1.0"
    )
    public Attribute.Double gwRG1RG2dist;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "capilary rise factor",
            lowerBound = 0.0,
            upperBound = 10.0,
            defaultValue = "0.0"
    )
    public Attribute.Double gwCapRise;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "relative initial RG1 storage",
            lowerBound = 0,
            upperBound = 1.0,
            unit = "n/a",
            defaultValue = "0.0"
    )
    public Attribute.Double initRG1;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "relative initial RG2 storage",
            lowerBound = 0,
            upperBound = 1.0,
            unit = "n/a",
            defaultValue = "0.0"
    )
    public Attribute.Double initRG2;
    
        @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Shape parameter for the Karst dynamics for RG1,  To activate the karst dynamics it must be larger than 1. Maximum useful value is 2.75",
            lowerBound = 1.00,
            upperBound = 2.75,
            unit = "n/a",
            defaultValue = "2.0"
    )
    public Attribute.Double RG1_shape_karst;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Shape parameter for the Karst dynamics for RG2,  To activate the karst dynamics it must be larger than 1. Maximum useful value is 2.75",
            lowerBound = 1.00,
            upperBound = 2.75,
            unit = "n/a",
            defaultValue = "2.0"
    )
    public Attribute.Double RG2_shape_karst;
    
        @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "RG1 saturation treshhold for activating groundwater contribution to run off; below the treshold all the water will be deep sink - above it is distibuted between deep sink and RG1_out",
            lowerBound = 0,
            upperBound = 1.0,
            unit = "n/a",
            defaultValue = "0.0"
    )
    public Attribute.Double RG1sat_tres;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "RG1 saturation treshhold for activating groundwater contribution to run off; below the treshold all the water will be deep sink - above it is distibuted between deep sink and RG1_out",
            lowerBound = 0,
            upperBound = 1.0,
            unit = "n/a",
            defaultValue = "0.0"
    )
    public Attribute.Double RG2sat_tres;
    
        @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Proportion of RG1_opt affected by karst dynamics",
            lowerBound = 0,
            upperBound = 1.0,
            unit = "n/a",
            defaultValue = "0.0"
    )
    public Attribute.Double karst_prop_RG1;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Proportion of RG1_opt affected by karst dynamics",
            lowerBound = 0,
            upperBound = 1.0,
            unit = "n/a",
            defaultValue = "0.0"
    )
    public Attribute.Double karst_prop_RG2;

    double run_maxRG1, run_maxRG2, run_actRG1, run_actRG2, run_inRG1, run_inRG2, run_outRG1, run_outRG2, run_genRG1, run_genRG2,
            run_k_RG1, run_k_RG2, run_RG1_rec, run_RG2_rec, run_maxSoilStor, run_actSoilStor, run_slope,
            run_percolation, run_gwExcess, rg1_deep, rg2_deep;
    /*
     *  Component run stages
     */

    public void initAll() {
        maxRG1.setValue(RG1_max.getValue() * area.getValue());
        maxRG2.setValue(RG2_max.getValue() * area.getValue());

        actRG1.setValue(maxRG1.getValue() * initRG1.getValue());
        actRG2.setValue(maxRG2.getValue() * initRG2.getValue());
    }

    public void run() {
        this.run_maxRG1 = maxRG1.getValue();
        this.run_maxRG2 = maxRG2.getValue();
        this.run_actRG1 = actRG1.getValue();
        this.run_actRG2 = actRG2.getValue();
        this.run_inRG1 = inRG1.getValue();
        this.run_inRG2 = inRG2.getValue();

        this.run_maxSoilStor = maxSoilStorage.getValue();
        this.run_actSoilStor = actSoilStorage.getValue();
        this.run_percolation = percolation.getValue();
        this.run_gwExcess = gwExcess.getValue();

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
        this.calcLinGWout_deep_sink();

        actRG1.setValue(this.run_actRG1);
        actRG2.setValue(this.run_actRG2);
        outRG1.setValue(this.run_outRG1);
        outRG2.setValue(this.run_outRG2);
        genRG1.setValue(this.run_genRG1);
        genRG2.setValue(this.run_genRG2);
        inRG1.setValue(this.run_inRG1);
        inRG2.setValue(this.run_inRG2);
        gwExcess.setValue(this.run_gwExcess);
        actSoilStorage.setValue(this.run_actSoilStor);
    }

    public void cleanup() {

    }

    public boolean replenishSoilStor() {
        double deltaSoilStor = this.run_maxSoilStor - this.run_actSoilStor;
        double sat_SoilStor = 0;
        double inSoilStor = 0;
        if ((this.run_actSoilStor > 0) && (this.run_maxSoilStor > 0)) {
            sat_SoilStor = this.run_actSoilStor / this.run_maxSoilStor;
        } else {
            sat_SoilStor = 0.000001;
        }
        if (this.run_actRG2 > deltaSoilStor) {
            double alpha = this.gwCapRise.getValue();
            if (alpha < 0.0) {
                alpha = 0.0;
            }
            inSoilStor = (deltaSoilStor) * (1. - Math.exp(-1 * alpha / sat_SoilStor));
        }
        if (run_actRG2 >= inSoilStor) {
            this.run_actSoilStor = this.run_actSoilStor + inSoilStor;
            this.run_actRG2 = this.run_actRG2 - inSoilStor;
        } else {
            this.run_actSoilStor = this.run_actSoilStor + this.run_actRG2;
            this.run_actRG2 = 0;
        }

        return true;
    }

    private boolean redistRG1_RG2_in() {
        if (this.run_inRG1 > 0) {
            double deltaRG1 = this.run_maxRG1 - this.run_actRG1;
            if (this.run_inRG1 <= deltaRG1) {
                this.run_actRG1 = this.run_actRG1 + this.run_inRG1;
                this.run_inRG1 = 0;
            } else {
                this.run_actRG1 = this.run_maxRG1;
                this.run_outRG1 = this.run_outRG1 + this.run_inRG1 - deltaRG1;
                this.run_inRG1 = 0;
            }
        }

        if (this.run_inRG2 > 0) {
            double deltaRG2 = this.run_maxRG2 - this.run_actRG2;
            if (this.run_inRG2 <= deltaRG2) {
                this.run_actRG2 = this.run_actRG2 + this.run_inRG2;
                this.run_inRG2 = 0;
            } else {
                this.run_actRG2 = this.run_maxRG2;
                this.run_outRG2 = this.run_outRG2 + this.run_inRG2 - deltaRG2;
                this.run_inRG2 = 0;
            }
        }

        return true;
    }

    private boolean distRG1_RG2() {
        double slope_weight = Math.tan(this.run_slope * (Math.PI / 180.));
        double gradh = ((1 - slope_weight) * this.gwRG1RG2dist.getValue());

        if (gradh < 0) {
            gradh = 0;
        } else if (gradh > 1) {
            gradh = 1;
        }

        double pot_RG1 = ((1 - gradh) * this.run_percolation);
        double pot_RG2 = (gradh * this.run_percolation);

        this.run_actRG1 = this.run_actRG1 + pot_RG1;
        this.run_actRG2 = this.run_actRG2 + pot_RG2;

        /**
         * testing if inflows can be stored in groundwater storages
         */
        double delta_RG2 = this.run_actRG2 - this.run_maxRG2;
        if (delta_RG2 > 0) {
            this.run_actRG1 = this.run_actRG1 + delta_RG2;
            this.run_actRG2 = this.run_maxRG2;
        }
        double delta_RG1 = this.run_actRG1 - this.run_maxRG1;
        if (delta_RG1 > 0) {
            this.run_gwExcess = this.run_gwExcess + delta_RG1;
            this.run_actRG1 = this.run_maxRG1;
        }
        if (delta_RG1 > 0) {
            //getModel().getRuntime().println("interflow surplus in gw: "+delta_RG1);
        }
        return true;
    }

    private boolean calcLinGWout_deep_sink() {
        //double k_rg1 = this.conc_index / this.RG1_k;
        double satRG1 = this.run_actRG1 / this.run_maxRG1;
        double satRG2 = this.run_actRG2 / this.run_maxRG2;
        
        this.satRG1.setValue(satRG1);
        this.satRG2.setValue(satRG2);
        

        double k_rg1 = 1 / this.run_RG1_rec;
        if (k_rg1 > 1) {
            k_rg1 = 1;
        }
        double rg1_out = k_rg1 * this.run_actRG1;
        this.run_actRG1 = this.run_actRG1 - rg1_out;
        
        //deepsink RG1 calc
        double shape_karst_RG1 = Math.min(RG1_shape_karst.getValue(),2.75);        
        double satRG1_tres = RG1sat_tres.getValue();
         
        
        
        if (shape_karst_RG1 >= 1) {
             
            double run_karst_prop_RG1 =  Math.min(karst_prop_RG1.getValue(),1.0); 
            
            double rg1_out_karst =   run_karst_prop_RG1 * rg1_out;
            double rg1_out_rest = rg1_out - rg1_out_karst;
            
            
            if (satRG1 <= satRG1_tres) {
                rg1_deep = rg1_out_karst;
                rg1_out_karst = 0.0;
            } else {

                double tres_actRG1 = satRG1_tres * this.run_maxRG1;
                this.rg1_deep = k_rg1 * tres_actRG1;
                rg1_out_karst = rg1_out_karst - rg1_deep;
                double sat_maxdiv = 1 - satRG1_tres;
                double sat_actdiv = satRG1 - satRG1_tres;
                double quo_sat = sat_actdiv / sat_maxdiv;
                double rg1_out_karst_temp = rg1_out_karst - (rg1_out_karst * ((shape_karst_RG1 - Math.pow(shape_karst_RG1, quo_sat)) / shape_karst_RG1));
                rg1_deep = rg1_out_karst - rg1_out_karst_temp;
                rg1_out_karst = rg1_out_karst_temp;

            }
            rg1_out = rg1_out_karst + rg1_out_rest;
        }
        
        
        
        

        this.run_outRG1 = this.run_outRG1 + rg1_out;

        //double k_rg2 = this.conc_index / this.RG2_k;
        double k_rg2 = 1 / this.run_RG2_rec;
        if (k_rg2 > 1) {
            k_rg2 = 1;
        }
        double rg2_out = k_rg2 * this.run_actRG2;
        
        this.run_actRG2 = this.run_actRG2 - rg2_out;

        //deepsink RGrg2 calc
        double shape_karst_RG2 = Math.min(RG2_shape_karst.getValue(),2.75); 
        double satRG2_tres =  RG2sat_tres.getValue(); 
        
        
        if (shape_karst_RG2 >= 1) {
             
            double run_karst_prop_RG2 =  Math.min(karst_prop_RG2.getValue(),1.0); 
            
            double rg2_out_karst =   run_karst_prop_RG2 * rg2_out;
            double rg2_out_rest = rg2_out - rg2_out_karst;
            
            
            
            if (satRG2 <= satRG2_tres) {
                rg2_deep = rg2_out_karst;
                rg2_out_karst = 0.0;
            } else {

                double tres_actRG2 = satRG2_tres * this.run_maxRG2;
                this.rg2_deep = k_rg2 * tres_actRG2;
                rg2_out_karst = rg2_out_karst - rg2_deep;
                double sat_maxdiv = 1 - satRG2_tres;
                double sat_actdiv = satRG2 - satRG2_tres;
                double quo_sat = sat_actdiv / sat_maxdiv;
                double rg2_out_karst_temp = rg2_out_karst - (rg2_out_karst * ((shape_karst_RG2 - Math.pow(shape_karst_RG2, quo_sat)) / shape_karst_RG2));
                rg2_deep = rg2_out_karst - rg2_out_karst_temp;
                rg2_out_karst = rg2_out_karst_temp;

            }
            rg2_out = rg2_out_karst + rg2_out_rest;
        }
        
        
        
        this.run_outRG2 = this.run_outRG2 + rg2_out;

        this.run_genRG1 = rg1_out;

        this.run_genRG2 = rg2_out;

        this.deep_outRG1.setValue(rg1_deep);

        this.deep_outRG2.setValue(rg2_deep);

        return true;
    }

    
}
