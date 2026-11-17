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


import jams.data.*;
import jams.model.*;
import java.util.Calendar;

/**
 *
 * @author Peter Krause
 */
@JAMSComponentDescription(title = "Meteo_para_LapseRate",
author = "Manfred Fink, Santosh Nepal, Peter Krause",
description = "Regionalisation of Weather parameter on the ratio level, using a multiplier" )
public class Meteo_para_LapseRate extends JAMSComponent {

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
    description = "lapse rate multiplier per 100 m elevation difference")
    public Attribute.Double lapseRateSummer;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    description = "lapse rate multiplier per 100 m elevation difference")
    public Attribute.Double lapseRateWinter;
//    @JAMSVarDescription(
//   access = JAMSVarDescription.AccessType.READ,
//            update = JAMSVarDescription.UpdateType.INIT,
//            description = "lapse rate per 100 m elevation difference"
//            )
//            public Attribute.Double lapseRate;
    @JAMSVarDescription(
            access=JAMSVarDescription.AccessType.READ,
            description="The current model time")
    public Attribute.Calendar time;

    
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    description = "position array to determine best weights")
    public Attribute.IntegerArray statOrder;
    /*
     *  Component run stages
     */

    public void init() throws Attribute.Entity.NoSuchAttributeException {
    }

    public void run() throws Attribute.Entity.NoSuchAttributeException {

     
        

        int closestStation = statOrder.getValue()[0];
        //elevation difference
        double elevationdiff = (statElev.getValue()[closestStation] - entityElev.getValue());
        //temp calculation

   // int nowmonth = (time.get(time.MONTH) + 1 );
     int nowmonth = time.get(Calendar.MONTH);



        if ((nowmonth >= 5) && (nowmonth <= 8)) {
            double multisummer = (elevationdiff * (lapseRateSummer.getValue() / 100.) + 1.);
            if  (multisummer < 0){
                multisummer = 0;
            }
            outputValue.setValue(multisummer * inputValue.getValue()[closestStation]);
        
        } else {
            double multiwinter = (elevationdiff * (lapseRateWinter.getValue() / 100.) + 1.);
            if  (multiwinter < 0){
                multiwinter = 0;
            }
            outputValue.setValue(multiwinter * inputValue.getValue()[closestStation]);
        
        }

    }



    public void cleanup() {
    }
}




//
//
//    public void init() throws Attribute.Entity.NoSuchAttributeException {
//    }
//
//    public void run() throws Attribute.Entity.NoSuchAttributeException {
//
//
//
//
//        int closestStation = statOrder.getValue()[0];
//        //elevation difference
//        double elevationdiff = (statElev.getValue()[closestStation] - entityElev.getValue());
//        //temp calculation
//
//   // int nowmonth = (time.get(time.MONTH) + 1 );
//     int nowmonth = time.get(Calendar.MONTH + 1);
//
//        if ((nowmonth >= 6) & (nowmonth <= 9)) {
//            outputValue.setValue(elevationdiff * (lapseRateSummer.getValue() / 100.) + inputValue.getValue()[closestStation]);
//        } else {
//            outputValue.setValue(elevationdiff * (lapseRateWinter.getValue() / 100.) + inputValue.getValue()[closestStation]);
//        }
//
//    }
//
//    public void cleanup() {
//    }
//}



//
//          double newTemp;
//
//        if ((nowmonth >= 6) & (nowmonth <= 9)) {
//           newTemp = (elevationdiff * (lapseRateSummer.getValue() / 100.) + inputValue.getValue()[closestStation]);
//        } else {
//           newTemp = (elevationdiff * (lapseRateWinter.getValue() / 100.) + inputValue.getValue()[closestStation]);
//        }
//return newTemp;
//    }
//
//this.outputValue.setValue(newTemp);
//
//    public void cleanup() {
//    }
//}
