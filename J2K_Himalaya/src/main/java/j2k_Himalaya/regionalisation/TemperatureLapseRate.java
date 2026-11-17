/*
 * InitSoilWaterStates.java
 * Created on 25. November 2005, 13:21
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
package j2k_Himalaya.regionalisation;

import jams.JAMS;
import jams.data.*;
import jams.model.*;
import java.util.Calendar;

/**
 *
 * @author Peter Krause
 */
@JAMSComponentDescription(title = "TemperatureLapseRate",
        author = "Santosh Nepal, Peter Krause",
        description = "Regionalisation of Temp through general adiabatic rate"
        + "depends upon given adaiabatic rate +++ included seasonal lapse rate. Two seasonal different Lapse rate for Summer and Winter season is proposed"
        + "now accept if station has data gaps, another nearest station is considered for lapse rate")

public class TemperatureLapseRate extends JAMSComponent {

    /*
     *  Component variables
     */
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
            description = "station elevation")
    public Attribute.DoubleArray statElev;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
            description = "entity elevation")
    public Attribute.Double entityElev;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
            description = "the measured input from a base station")
    public Attribute.DoubleArray inputValue;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.WRITE,
            description = "calculated output for the modelling entity")
    public Attribute.Double outputValue;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
            description = "lapse rate per 100 m elevation difference")
    public Attribute.Double lapseRateSummer;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
            description = "lapse rate per 100 m elevation difference")
    public Attribute.Double lapseRateWinter;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "The current model time")
    public Attribute.Calendar time;

      @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
            description = "Start month for summer lapse rate")
    public Attribute.Double SummerMonthStart;
    
         @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
            description = "End month for summer lapse rate")
    public Attribute.Double SummerMonthEnd;
    
    

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
            description = "position array to determine best weights")
    public Attribute.IntegerArray statOrder;

    /*
     *  Component run stages
     */
    public void init() {
    }

    public void run() {

        for (int i = 0; i < inputValue.getValue().length; i++) {

            int closestStation = statOrder.getValue()[i];
            double input = inputValue.getValue()[closestStation];

            if (input != JAMS.getMissingDataValue()) {

                //elevation difference
                double elevationdiff = (statElev.getValue()[closestStation] - entityElev.getValue());
                //temp calculation

                // int nowmonth = (time.get(time.MONTH) + 1 );
                int nowmonth = (time.get(Calendar.MONTH)+1);

                  if ((nowmonth >= this.SummerMonthStart.getValue()) && (nowmonth <= this.SummerMonthEnd.getValue())) {
                    outputValue.setValue(elevationdiff * (lapseRateSummer.getValue() / 100.) + input);
                    return;
                } else {
                    outputValue.setValue(elevationdiff * (lapseRateWinter.getValue() / 100.) + input);
                    return;
                }
            }
        }

        getModel().getRuntime().sendHalt("No station with valid value found. Please check your inputs!");
    }

    public void cleanup() {
    }
}
