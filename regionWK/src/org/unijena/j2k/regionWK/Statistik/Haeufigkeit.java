/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.unijena.j2k.regionWK.Statistik;

import org.apache.commons.math.stat.Frequency;
import jams.data.*;
import jams.model.*;


@JAMSComponentDescription(
title="Extremwerte",
        author="Corina Manusch",
        description="Calculates minimum and maxium of timeseries."
        )
        public class Haeufigkeit extends JAMSComponent {

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

        double[] rHum = this.rhum.getValue();

        Frequency f = new Frequency();

           for(int i = 0; i < rHum.length; i++){
                  f.addValue(rHum[i]);
           }
          //Haeufigkeit wieder rausholen, wie wozu???
    }


    public void cleanup() throws JAMSEntity.NoSuchAttributeException {

    }


}
