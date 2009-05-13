/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.unijena.j2k.regionWK.Statistik;

import org.apache.commons.math.stat.regression.SimpleRegression;
import jams.data.*;
import jams.model.*;


@JAMSComponentDescription(
title="Extremwerte",
        author="Corina Manusch",
        description="Calculates minimum and maxium of timeseries."
        )
        public class EinfacheRegression extends JAMSComponent {

    /*
     *  Component variables
     */
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "the relative humidity values"
            )
            public JAMSDoubleArray rhum;

    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "temperature for the computation"
            )
            public JAMSDoubleArray temperature;


public void init() throws JAMSEntity.NoSuchAttributeException {

    }

    public void run() throws JAMSEntity.NoSuchAttributeException {

        //zu vergleichende Zeitreihen
        double[] rHum = this.rhum.getValue();
        double[] tMean = this.temperature.getValue();

        SimpleRegression regression = new SimpleRegression();

        //addDate(double x, double y)
        for(int i = 0; i < rHum.length; i++){
               regression.addData(rHum[i], tMean[i]);
        }


        /*System.out.println(regression.getIntercept());
        // displays intercept of regression line

        System.out.println(regression.getSlope());
        // displays slope of regression line

        System.out.println(regression.getSlopeStdErr());
        // displays slope standard error

        System.out.println(regression.predict(rHum[6]));*/
        // displays predicted y value for x = 1.5

        double rsq = regression.getRSquare();
        double mse = regression.getMeanSquareError();
     
    }


    public void cleanup() throws JAMSEntity.NoSuchAttributeException {

    }


}
