/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */


package org.unijena.j2k.regionWK.AP5;

import jams.data.*;
import jams.model.*;
import java.util.*;

/**
 *
 * @author Corina Manusch
 */
@JAMSComponentDescription(
title="Extremwerte",
        author="Corina Manusch",
        description="Calculates minimum and maxium of timeseries."
        )
        public class Extremwerte extends JAMSComponent {
    
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
   
    
    /*
     *  Component run stages
     */
    
    public void init() throws JAMSEntity.NoSuchAttributeException {
        
    }
    
    public void run() throws JAMSEntity.NoSuchAttributeException {
            
            
            double[] rhum = this.rhum.getValue();
           
            double min = minimum(rhum);
            double max = maximum(rhum);
   
           
            }
           
   
    public void cleanup() throws JAMSEntity.NoSuchAttributeException {
        
    }
    
    public static double minimum(double [] timeseries) {
        
         double min = timeseries[0];
      
        for(int j = 1; j < timeseries.length; j++){
    
            if(timeseries[j] < min){
                min = timeseries[j];
            }
        }
         return min;
    }
    
     public static double maximum(double [] timeseries) {
      
         double max = timeseries[0];
      
            for(int j = 1; j < timeseries.length; j++){
    
                if(timeseries[j] > max){
                    max = timeseries[j];
                }
            }
         return max;
     }
}

