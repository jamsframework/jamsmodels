/*
 * InitSoilWaterStates.java
 * Created on 25. November 2005, 13:21
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
package j2k_Himalaya.regionalisation;


import jams.data.*;
import jams.model.*;
import java.util.Calendar;

/**
 *
 * @author Peter Krause
 */
@JAMSComponentDescription(title = "TemperatureLapseRate Monthly",
author = "Santosh Nepal, Peter Krause, Manfred Fink",
description = "Regionalisation of Temp through general adiabatic rate"
+ "depends upon given adaiabatic rate +++ included monthly lapse rate. Twelve different Lapse for each month are proposed" )

public class TemperatureLapseRateMonthly extends JAMSComponent {

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
    public Attribute.Double lapseRateJan;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    description = "lapse rate per 100 m elevation difference")
    public Attribute.Double lapseRateFeb;
    
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    description = "lapse rate per 100 m elevation difference")
    public Attribute.Double lapseRateMar;
        
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    description = "lapse rate per 100 m elevation difference")
    public Attribute.Double lapseRateApr;
            
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    description = "lapse rate per 100 m elevation difference")
    public Attribute.Double lapseRateMay;
    
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    description = "lapse rate per 100 m elevation difference")
    public Attribute.Double lapseRateJun;
    
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    description = "lapse rate per 100 m elevation difference")
    public Attribute.Double lapseRateJul;
    
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    description = "lapse rate per 100 m elevation difference")
    public Attribute.Double lapseRateAug;
    
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    description = "lapse rate per 100 m elevation difference")
    public Attribute.Double lapseRateSep;
    
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    description = "lapse rate per 100 m elevation difference")
    public Attribute.Double lapseRateOct;
    
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    description = "lapse rate per 100 m elevation difference")
    public Attribute.Double lapseRateNov;
    
    
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    description = "lapse rate per 100 m elevation difference")
    public Attribute.Double lapseRateDec;
    
    
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
      


     if (nowmonth == 0) {
          outputValue.setValue(elevationdiff * (lapseRateJan.getValue() / 100.) + inputValue.getValue()[closestStation]);
     } else if (nowmonth == 1) {
         outputValue.setValue(elevationdiff * (lapseRateFeb.getValue() / 100.) + inputValue.getValue()[closestStation]);
     } else if (nowmonth == 2) {
         outputValue.setValue(elevationdiff * (lapseRateMar.getValue() / 100.) + inputValue.getValue()[closestStation]);
     } else if (nowmonth == 3) {
         outputValue.setValue(elevationdiff * (lapseRateApr.getValue() / 100.) + inputValue.getValue()[closestStation]);
     } else if (nowmonth == 4) {
         outputValue.setValue(elevationdiff * (lapseRateMay.getValue() / 100.) + inputValue.getValue()[closestStation]);
     } else if (nowmonth == 5) {
         outputValue.setValue(elevationdiff * (lapseRateJun.getValue() / 100.) + inputValue.getValue()[closestStation]);
     } else if (nowmonth == 6) {
         outputValue.setValue(elevationdiff * (lapseRateJul.getValue() / 100.) + inputValue.getValue()[closestStation]);
     } else if (nowmonth == 7) {
         outputValue.setValue(elevationdiff * (lapseRateAug.getValue() / 100.) + inputValue.getValue()[closestStation]);
     } else if (nowmonth == 8) {
         outputValue.setValue(elevationdiff * (lapseRateSep.getValue() / 100.) + inputValue.getValue()[closestStation]);
     } else if (nowmonth == 9) {
         outputValue.setValue(elevationdiff * (lapseRateOct.getValue() / 100.) + inputValue.getValue()[closestStation]);
     } else if (nowmonth == 10) {
         outputValue.setValue(elevationdiff * (lapseRateNov.getValue() / 100.) + inputValue.getValue()[closestStation]);
     } else if (nowmonth == 11) {
         outputValue.setValue(elevationdiff * (lapseRateDec.getValue() / 100.) + inputValue.getValue()[closestStation]);
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
