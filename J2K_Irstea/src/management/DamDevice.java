/*
 * SewerOverflowDevice.java
 * Created on 05. October 2012, 17:02
 *
 * This file is part of JAMS
 * Copyright (C) FSU Jena
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
package management;

import jams.data.*;
import jams.model.*;
import jams.workspace.DataSetDefinition;
import jams.workspace.stores.InputDataStore;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.Calendar;
//import java.jamsui.juice;


/**
 *
 * @author Sven Kralisch & Mériem Labbas & Christian Fischer
 */
@JAMSComponentDescription(title = "DamDevice",
        author = "Francois Tilmant & Flora Branger",
        description = "Component used for the simulation of an overflow device. It takes the different components outflows"
        + "coming from a sewer reach(threshold test) and adds it to the receiving reach river.",
        version = "3.0_0",
        date = "2014-04-17")
public class DamDevice extends JAMSComponent {

    /*
     * Component variables
     */
        @JAMSVarDescription (
            access = JAMSVarDescription.AccessType.READ,
            description = "regionalised data value (objective function)"
        )
        public Attribute.Double par_fo;
        
        @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Initial volume in the reservoir",
            unit = "Mm3"
        )
        public Attribute.Double par_v0;
        
        @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Maximum storage of the reservoir",
            unit = "Mm3"
        )
        public Attribute.Double par_smax;
                
        @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "RD1 inflow to reach",
            unit = "L"
        )
        public Attribute.Double out_in_rd1;
    
        @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "RD2 inflow to reach",
            unit = "L"
        )
        public Attribute.Double out_in_rd2;
    
        @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "RG1 inflow to reach",
            unit = "L"
        )
        public Attribute.Double out_in_rg1;
    
        @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "RG2 inflow to reach",
            unit = "L"
        )
        public Attribute.Double out_in_rg2;
    
        @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "FO corrected if there isn't enough water in the river",
            unit = "L"
        )
        public Attribute.Double out_fo_fin;
        
        @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "state variable - Storage in the reservoir",
            unit = "L"
        )
        public Attribute.Double st_storage;
        
        @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Current time"
        )
        public Attribute.Calendar par_time;
            
        @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "The time interval"
        )
        public Attribute.TimeInterval par_time_interval;
    

    
    int run_n_comp = 4;
    double[] run_rel_comp;
    double[] run_comp;
    double[] run_out_comp;
    double run_curr_volume = 0;
    
    public void init() {
        run_rel_comp = new double[run_n_comp];
        run_comp = new double[run_n_comp];
        run_out_comp = new double[run_n_comp];    
    }
    
   

    public void run() {
       
        if (this.par_v0.getValue() > 0) {

            String run_date = par_time.toString();
            String run_start = par_time_interval.getStart().toString();
            if ( (run_date.equals(run_start) )) {
                this.st_storage.setValue(Math.pow(10,9)*this.par_v0.getValue());
            }

            double run_test = this.out_in_rd1.getValue();
            run_test = run_test + this.out_in_rd2.getValue();
            run_test = run_test + this.out_in_rg1.getValue();
            run_test = run_test + this.out_in_rg2.getValue();
            double run_outflow = 0;
            double run_new_s = 0;
            double run_fo_act = 0;

            calcRelComponents(); 

            this.run_comp[0] = this.out_in_rd1.getValue();
            this.run_comp[1] = this.out_in_rd2.getValue();
            this.run_comp[2] = this.out_in_rg1.getValue();
            this.run_comp[3] = this.out_in_rg2.getValue();

            calcRelComponents();
            
            run_fo_act = this.par_fo.getValue();
            if(run_fo_act >= 0){
                //Cas de restitution
                if (this.st_storage.getValue() < run_fo_act) {run_fo_act = this.st_storage.getValue();}
                run_new_s = Math.max(this.st_storage.getValue() - run_fo_act,0);
            }    else  {
                //Cas de stockage
                // in case test < FO, we put FO = test 
                // because we can't keep more water than there is in the river
                if( (run_test+ run_fo_act) <0) { run_fo_act = -run_test;} 
                run_new_s = Math.min(this.st_storage.getValue() - run_fo_act,Math.pow(10,9)*this.par_smax.getValue());        
            }

            // Calcul de la restitution réelle
            run_outflow = Math.max(0,run_test -(run_new_s- this.st_storage.getValue()));
            this.st_storage.setValue(run_new_s);   
            this.out_fo_fin.setValue(run_fo_act) ; 
            for(int i = 0; i < run_comp.length; i++){
                run_out_comp[i] = run_outflow * run_rel_comp[i];
                run_comp[i] = run_comp[i] - run_out_comp[i];
            }
            this.out_in_rd1.setValue(run_out_comp[0]);  
            this.out_in_rd2.setValue(run_out_comp[1]);  
            this.out_in_rg1.setValue(run_out_comp[2]);  
            this.out_in_rg2.setValue(run_out_comp[3]);  

        }    else {
            this.st_storage.setValue(0.0);
            this.out_fo_fin.setValue(0.0) ; 
        }
    }
    
    private void calcRelComponents(){
        run_curr_volume = 0;
        for(int i = 0; i < run_n_comp; i++){
            run_curr_volume = run_curr_volume + run_comp[i];
        }
        for(int i = 0; i < run_n_comp; i++){
            if(run_curr_volume > 0)
                run_rel_comp[i] = run_comp[i] / run_curr_volume;
            else
                run_rel_comp[i] = 0;
        }
    }
}
