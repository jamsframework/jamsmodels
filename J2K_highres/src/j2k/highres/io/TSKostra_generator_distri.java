/*
 * TSDataStoreReader.java
 * Created on 16. Oktober 2008, 17:34
 *
 * This file is a JAMS component
 * Copyright (C) FSU Jena
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
 * along with this program; if not, see <http://www.gnu.org/licenses/>.
 *
 */
package j2k.highres.io;

import jams.JAMS;
//import jams.components.efficiencies.Regression;
import jams.data.*;
import jams.model.*;
import jams.workspace.DataSetDefinition;
import jams.workspace.DataValue;
import jams.workspace.DefaultDataSet;
import jams.workspace.stores.InputDataStore;
import jams.workspace.stores.TSDataStore;
import java.util.ArrayList;

/**
 *
 * @author Manfred Fink
 */
@JAMSComponentDescription(title = "TSDataStoreReader",
        author = "Manfred Fink",
        date = "2020-05-04",
        version = "1.0",
        description = "This generates rainfall distribution from given durations and amount (KOSTRA), with difrent distrubution types"
        + "only for use in the time loop. This results in a uniform precip input ")
public class TSKostra_generator_distri extends JAMSComponent {

    /*
     *  Component variables
     */
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
            description = "Amount of rain",
            unit = "mm")
    public Attribute.Double amount;

    /* @JAMSVarDescription(access = JAMSVarDescription.AccessType.READWRITE,
            description = "Array of double values splited up according to the distribution ")
    public Attribute.DoubleArray dataArray;*/
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
            description = "The time interval within which the component shall read "
            + "data from the datastore")
    public Attribute.TimeInterval timeInterval;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
            description = "Duration of rain",
            unit = "h",
            defaultValue = "1.0")
    public Attribute.Double duration;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READWRITE,
            description = "Numer irrterator for the timestep")
    public Attribute.Integer timestep;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.WRITE,
            description = "Amount of rain for each timestep",
            unit = "mm")
    public Attribute.Double precip;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "precip distribution B = uniform, M = middle accented, A = start accented, E = end accented, D = DVWK dirtibution, A_SCS = JAMS_SCS dirtibution - start accented, E_SCS = JAMS_SCS dirtibution - end accented",
            defaultValue = "D"
    )
    public Attribute.String precipDistribution;

    double[] dataarray = null;

    @Override
    public void init() {

        long il = timeInterval.getNumberOfTimesteps();
        int unit = timeInterval.getTimeUnit();
        int cunit = timeInterval.getTimeUnitCount();
        double run_amount = amount.getValue();

        //proportions IWG-HW
        double[] rain_end = new double[]{0.018, 0.021, 0.023, 0.025, 0.027, 0.03, 0.033, 0.037, 0.04, 0.045, 0.051, 0.057, 0.064, 0.072, 0.08, 0.084, 0.085, 0.079, 0.07, 0.059};
        double[] rain_middle = new double[]{0.015, 0.022, 0.029, 0.037, 0.045, 0.054, 0.062, 0.072, 0.081, 0.083, 0.083, 0.081, 0.072, 0.062, 0.054, 0.045, 0.037, 0.029, 0.022, 0.015};

        int i = Integer.parseInt(Long.toString(il));

        int j = 0;

        dataarray = new double[i];

        double time_mult = 0;

        switch (unit) {
            case 6: // day
                time_mult = 24;
                break;
            case 11:  // hour
                time_mult = 1;
                break;
            case 12:  // minute
                time_mult = 1 / 60;
                break;

        }

        time_mult = time_mult * cunit;

        double dura_time_h = time_mult * i;

        double raindura = duration.getValue();

        int count_rain = (int) Math.round(raindura / time_mult);

        double[] rain_steps = new double[count_rain];


        /* DVWK-Distribution
        time/propotion
        0.3/0.2
        0.2/0.5
        0.5/0.3
         */
        double count03 = 0;
        double count05 = 0;
        double count10 = 0;
        double count025 = 0;
        double count01 = 0;
        double count0075 = 0;
        double count005 = 0;
        double count0025 = 0;;
        long count03i = 0;
        long count05i = 0;
        long count10i = 0;
        long count025i = 0;
        long count01i = 0;
        long count0075i = 0;
        long count005i = 0;
        long count0025i = 0;
        double amount03 = 0;
        double amount05 = 0;
        double amount10 = 0;
        double amount025 = 0;
        double amount01 = 0;
        double amount0075 = 0;
        double amount005 = 0;
        double amount0025 = 0;
        double fac_rest = 0;
        double tempsum = 0;

        //int avg_step20 = Math.max(1, ((int) (Math.round(avg_steps20))));
        if (this.precipDistribution.getValue().equalsIgnoreCase("D")) {
            if (count_rain < 2) {
                dataarray[0] = 0;
                dataarray[1] = run_amount;
                j = 2;
            } else if (count_rain < 3) {
                dataarray[0] = 0;
                dataarray[1] = run_amount * 0.7;
                dataarray[2] = run_amount * 0.3;
                j = 3;
            } else {
                count03 = 0.3 * count_rain;
                count03i = Math.round(count03);

                count05 = (0.2 * count_rain) + count03;
                count05i = Math.round(count05) - count03i;

                count10 = (0.5 * count_rain) + count05;
                count10i = Math.round(count10) - count03i - count05i;

                amount03 = 0.2 * run_amount / count03i;
                amount05 = 0.5 * run_amount / count05i;
                amount10 = 0.3 * run_amount / count10i;
                dataarray[0] = 0;
                j = 1;
                while (j <= count_rain) {
                    if (j > count05i + count03i) {
                        dataarray[j] = amount10;
                    } else if (j > count03i) {
                        dataarray[j] = amount05;
                    } else {
                        dataarray[j] = amount03;
                    }
                    j++;
                }
            }//Blockregen)
        } else if (this.precipDistribution.getValue().equalsIgnoreCase("B")) {
            dataarray[0] = 0;
            j = 1;
            while (j <= count_rain) {
                dataarray[j] = run_amount / count_rain;
                j++;
            }

        } //left and right accented rainfall 
        else if (this.precipDistribution.getValue().equalsIgnoreCase("A_SCS") || this.precipDistribution.getValue().equalsIgnoreCase("E_SCS")) {
            /*            if (count_rain < 0) {
                dataarray[0] = 0;
                dataarray[1] = run_amount;
                j = 2;
            } else if (count_rain < 0) {
                dataarray[0] = 0;
                dataarray[1] = run_amount * 0.85;
                dataarray[2] = run_amount * 0.15;
                j = 3;
            } else if (count_rain < 0) {
                dataarray[0] = 0;
                dataarray[1] = run_amount * 0.75;
                dataarray[2] = run_amount * 0.175;
                dataarray[3] = run_amount * 0.075;
                j = 4;
            } else if (count_rain < 0) {
                dataarray[0] = 0;
                dataarray[1] = run_amount * 0.625;
                dataarray[2] = run_amount * 0.225;
                dataarray[3] = run_amount * 0.1;
                dataarray[4] = run_amount * 0.05;
                j = 5;
            } else if (count_rain < 0) {
                dataarray[0] = 0;
                dataarray[1] = run_amount * 0.55;
                dataarray[2] = run_amount * 0.24;
                dataarray[3] = run_amount * 0.105;
                dataarray[4] = run_amount * 0.07;
                dataarray[5] = run_amount * 0.035;
                j = 6;
            } else {*/

            int avg_step = Math.max(1, Math.round(count_rain / 6));
            count05 = 0.166667 * count_rain;
            count05i = Math.round(count05);

            count025 = (0.166667 * count_rain) + count05;
            count025i = Math.round(count025) - count05i;

            count01 = (0.166667 * count_rain) + count025;
            count01i = Math.round(count01) - count025i - count05i;

            count0075 = (0.166667 * count_rain) + count01;
            count0075i = Math.round(count0075) - count01i - count025i - count05i;

            count005 = (0.166667 * count_rain) + count0075;
            count005i = Math.round(count005) - count0075i - count01i - count025i - count05i;

            count0025 = (0.166667 * count_rain) + count005;
            count0025i = Math.round(count0025) - count005i - count0075i - count01i - count025i - count05i;

            amount05 = 0.5 * run_amount / avg_step;
            amount025 = 0.25 * run_amount / avg_step;
            amount01 = 0.1 * run_amount / avg_step;
            amount0075 = 0.075 * run_amount / avg_step;
            amount005 = 0.05 * run_amount / avg_step;
            amount0025 = 0.025 * run_amount / avg_step;

            tempsum = (amount05 * count05i) + (amount025 * count025i) + (amount01 * count01i) + (amount0075 * count0075i) + (amount005 * count005i) + (amount0025 * count0025i);

            fac_rest = run_amount / tempsum;

            amount05 = amount05 * fac_rest;
            amount025 = amount025 * fac_rest;
            amount01 = amount01 * fac_rest;
            amount0075 = amount0075 * fac_rest;
            amount005 = amount005 * fac_rest;
            amount0025 = amount0025 * fac_rest;

            //amount05 = ((0.5 * run_amount) + rest) / count05i;
            dataarray[0] = 0;

            if (this.precipDistribution.getValue().equalsIgnoreCase("A_SCS")) {
                j = 1;
                while (j <= count_rain) {
                    if (j > (count05i + count025i + count01i + count0075i + count005i)) {
                        dataarray[j] = amount0025;
                    } else if (j > (count05i + count025i + count01i + count0075i)) {
                        dataarray[j] = amount005;
                    } else if (j > (count05i + count025i + count01i)) {
                        dataarray[j] = amount0075;
                    } else if (j > (count05i + count025i)) {
                        dataarray[j] = amount01;
                    } else if (j > (count05i)) {
                        dataarray[j] = amount025;
                    } else {
                        dataarray[j] = amount05;
                    }
                    j++;
                }
            } else {
                j = 1;
                while (j <= count_rain) {
                    if (j > (count025i + count01i + count0075i + count005i + count0025i)) {
                        dataarray[j] = amount05;
                    } else if (j > (count01i + count0075i + count005i + count0025i)) {
                        dataarray[j] = amount025;
                    } else if (j > (count0075i + count005i + count0025i)) {
                        dataarray[j] = amount01;
                    } else if (j > (count005i + count0025i)) {
                        dataarray[j] = amount0075;
                    } else if (j > (count0025i)) {
                        dataarray[j] = amount005;
                    } else {
                        dataarray[j] = amount0025;
                    }
                    j++;
                }

                // }
                //fill the rest of the timeintervall with 0
                while (j < i) {
                    dataarray[j] = 0.0;
                    j++;
                }

                int ii = 0;
                double testsum = 0;

                //check amount 
                while (ii < dataarray.length) {
                    testsum = testsum + dataarray[ii];
                    ii++;
                }

                double testdiff = run_amount - testsum;

                if (testdiff != 0.0) {
                    System.out.println("Error in precip distribution"
                            + " calulation = " + testdiff);
                }

                //dataArray.setValue(dataarray);
                timestep.setValue(0);

            }
        } else if (this.precipDistribution.getValue().equalsIgnoreCase("A") || this.precipDistribution.getValue().equalsIgnoreCase("E") || this.precipDistribution.getValue().equalsIgnoreCase("M")) {
            //IWG-HW Verteilungen

            double[] rain_dist = new double[]{0.018, 0.021, 0.023, 0.025, 0.027, 0.03, 0.033, 0.037, 0.04, 0.045, 0.051, 0.057, 0.064, 0.072, 0.08, 0.084, 0.085, 0.079, 0.07, 0.059};

            if (this.precipDistribution.getValue().equalsIgnoreCase("A")) {
                rain_dist = new double[]{0.059, 0.07, 0.079, 0.085, 0.084, 0.08, 0.072, 0.064, 0.057, 0.051, 0.045, 0.04, 0.037, 0.033, 0.03, 0.027, 0.025, 0.023, 0.021, 0.018};
            }
            if (this.precipDistribution.getValue().equalsIgnoreCase("M")) {
                rain_dist = new double[]{0.015, 0.022, 0.029, 0.037, 0.045, 0.054, 0.062, 0.072, 0.081, 0.083, 0.083, 0.081, 0.072, 0.062, 0.054, 0.045, 0.037, 0.029, 0.022, 0.015};
            }
            double avg_steps20 = (double) count_rain / rain_dist.length;
            int ii = 0;
            int iii = 0;
            double ratiosum = 0;

            double sum_avg_steps20 = avg_steps20;
            
            int ratio = (int) (1 / avg_steps20);

            double dratio = (1 / avg_steps20);
            
            double reminder = dratio - ratio;
            
            double remindersum = reminder;
            
            int reminderstep = 0;
            
            double step_amount = 0;

            double part_step = 0;

            //double part_step_sum = 0;
            double part_step_rest = 0;

            double rest_step_amount = 0;

            double raininter = 0;

            ratiosum = ratio;
            //Fall Zeitschritte < Verteilungschritte
            if (avg_steps20 < 1) {

                while (ii < count_rain) {
                    //schritte der gewichte (20)
                    while (iii < Math.min(ratio + raininter, rain_dist.length)) {

                        if (sum_avg_steps20 <= 1) {
                            step_amount = Math.min(rain_dist[iii] + step_amount + rest_step_amount, rain_dist[iii] * dratio);
                            rest_step_amount = 0;
                            sum_avg_steps20 = sum_avg_steps20 + avg_steps20;
                            iii++;
                        } else {
                            part_step = reminder;
                            //part_step = ((sum_avg_steps20 - 1) / avg_steps20);
                            //part_step_sum = part_step_sum + part_step;
                            part_step_rest = 1 - part_step;
                            step_amount = rain_dist[iii] * part_step + step_amount;
                            rest_step_amount = rain_dist[iii] * part_step_rest;
                            sum_avg_steps20 = sum_avg_steps20 + avg_steps20;
                            iii++;
                        }
                    }
                    dataarray[ii + 1] = step_amount * run_amount;
                    step_amount = 0;
                    sum_avg_steps20 = avg_steps20;
                    
                    if (remindersum > 0){
                        reminderstep = 1;
                        remindersum = remindersum - 1;
                    }
                    
                    raininter = raininter + ratio + reminderstep;
                    reminderstep = 0;                    
                    remindersum = reminder + remindersum;
                    if (iii == rain_dist.length){
                        int k = 0;
                        double arraysum = 0;
                        while (k < dataarray.length){
                            arraysum = arraysum + dataarray[k];
                            k++;
                        }                      
                        k = 0;
                        double rest = run_amount / arraysum;
                        
                        
                        
                        while (k < dataarray.length){
                            dataarray[k] = dataarray[k] * rest;
                            k++;
                        }
                        
                        
                    }
                    ii++;
                }

                //Fall Verteilungschritte <= Zeitschritte  
            } else {
                
                int iavg_steps20 = (int)(avg_steps20);
                
                
                while (ii < rain_dist.length) {
                    
                    
                    int jj = 0;
                    
                    while (jj <= iavg_steps20) {

                        if (sum_avg_steps20 > 1) {
                            step_amount = ((rain_dist[ii] / avg_steps20));
                            //part_step = 0;
                            sum_avg_steps20 = sum_avg_steps20 - 1;
                            jj++;
                            iii++;
                            dataarray[iii] = step_amount * run_amount;
                        } else {
                            //part_step = rain_dist[ii] * sum_avg_steps20; ;
                            jj++;
                        }
                    }
                   
                    

                    step_amount = 0;
                    
                    remindersum = remindersum + sum_avg_steps20;
                    
                    sum_avg_steps20 = avg_steps20;
                    
                    if (remindersum > 0.5){
                        reminderstep = 1;
                        sum_avg_steps20 = avg_steps20 + reminderstep;
                        remindersum = remindersum - 1;
                    }
                    

                    
                    if (iii == count_rain){
                        int k = 0;
                        double arraysum = 0;
                        while (k < dataarray.length){
                            arraysum = arraysum + dataarray[k];
                            k++;
                        }                      
                        k = 0;
                        double rest = run_amount / arraysum;
                        
                        
                        
                        while (k < dataarray.length){
                            dataarray[k] = dataarray[k] * rest;
                            k++;
                        }
                        
                        
                    }
                    ii++;
                }

            }

        }
    }

    @Override
    public void run() {
        int i = timestep.getValue();
        precip.setValue(dataarray[i]);
        i++;
        timestep.setValue(i);

    }

    @Override
    public void cleanup() {

    }
}
