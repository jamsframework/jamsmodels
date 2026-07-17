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
        description = "This generates rainfall distribution from given durations and amount (KOSTRA)" +
                "only for use in the time loop. This results ina uniform precip input ")
public class TSKostra_generator extends JAMSComponent {

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
    
    
    double[] dataarray = null;
    

    @Override
    public void init() {

        long il = timeInterval.getNumberOfTimesteps();
        int unit = timeInterval.getTimeUnit();
        int cunit = timeInterval.getTimeUnitCount();
        double run_amount = amount.getValue();

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

        Long count_rain = Math.round(raindura / time_mult);

        /* DVWK-Distribution
        time/propotion
        0.3/0.2
        0.2/0.5
        0.5/0.3
         */
        double count03 = 0;
        double count05 = 0;
        double count10 = 0;
        long count03i = 0;
        long count05i = 0;
        long count10i = 0;
        double amount03 = 0;
        double amount05 = 0;
        double amount10 = 0;

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
        }
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
