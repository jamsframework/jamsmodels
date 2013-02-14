/*
 * TimeNumbers.java
 *
 * Created on 17. Juli 2006, 17:21
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.unijena.j2k;

import jams.data.Attribute;
import jams.data.Attribute.Calendar;
import jams.data.JAMSCalendar;
import java.util.Locale;
import jams.data.JAMSDataFactory;


/**
 *
 * @author c0krpe
 */
public class TimeNumbers {
    static double a = 12;
    /** Creates a new instance of TimeNumbers */
    public TimeNumbers() {
    }
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        Attribute.Calendar time = JAMSDataFactory.getDataFactory().createCalendar();
        System.out.println("seconds: " + JAMSCalendar.SECOND);
        System.out.println("Minutes: " + JAMSCalendar.MINUTE);
        System.out.println("Hours: " + JAMSCalendar.HOUR_OF_DAY);
        System.out.println("Day: " + JAMSCalendar.DAY_OF_YEAR);
        System.out.println("Months: " + JAMSCalendar.MONTH);
        System.out.println("Years: " + JAMSCalendar.YEAR);
        
        double x1 = 4;
        double x2 = 2;
        double y = (1 - (8 * x1) + (7 * Math.pow(x1,2)) - 7./3. * Math.pow(x1,3) + 1./4. * Math.pow(x1,4)) * Math.pow(x2,2) * Math.exp(-1 * x2);
        System.out.println("y: " + y);
        
        double[] p = {3,2,1,0};
        double[] v = {1,0,0,0};
       
        double e2 = org.unijena.j2k.efficiencies.NashSutcliffe.efficiency(p, v, 2);
        double le2 = org.unijena.j2k.efficiencies.NashSutcliffe.logEfficiency(p, v, 2);
        
        System.out.println("e2: " + e2);
        System.out.println("le2: " + le2);
        
        double z1 = 3.24;
        double z2 = 3.27;
        
        System.out.println("3.24 with %f.0 " + String.format(Locale.US, "%.0f ", z1));
        System.out.println("3.24 with %f.1 " + String.format(Locale.US, "%.1f ", z1));
        System.out.println("3.24 with %f.2 " + String.format(Locale.US, "%.2f ", z1));
        
        System.out.println("3.27 with %f.0 " + String.format(Locale.US, "%.0f ", z2));
        System.out.println("3.27 with %f.1 " + String.format(Locale.US, "%.1f ", z2));
        System.out.println("3.27 with %f.2 " + String.format(Locale.US, "%.2f ", z2));
        
        
        
    }
    
    
    public static void test(){
        double NODATA = -9999;
        double value = 0;
        double[] dist = {1,2,3,4,5,6};
        //double[] data2 = {-9999,-9999,-9999,-8,-9999,-9999};
        double[] data2 = {-99,42,37,-8,22,56};
        //double[] data2 = {-9999,-9999,-9999,-9999,-9999,-9999};
        
        double[] weights2 = org.unijena.j2k.statistics.IDW.calcWeights(dist);
        int[] wArray = org.unijena.j2k.statistics.IDW.computeWeightArray(weights2);
        for(int i = 0; i < dist.length; i++){
            System.out.println("data["+i+"]: " + data2[i] + " dist["+i+"]: " + dist[i] + " w["+i+"]: " + weights2[i] + " wA["+i+"]: " + wArray[i]);
        }
        
        int nIDW = 3;
        
        double[] data = new double[nIDW];
        double[] weights = new double[nIDW];
        
        //make sure that the arrays are intialized with 0s
        for(int i = 0; i < nIDW;i++){
            data[i] = 0;
            weights[i] = 0;
        }
        
//@TODO: Recheck this for correct calculation, the Doug Boyle Problem!!
        
        //Retreiving data, elevations and weights
        int[] wA = wArray;
        int counter = 0;
        int element = counter;
        boolean cont = true;
        boolean valid = false;
        
        while(counter < nIDW && cont){
            int t = wA[element];
            //check if data is valid or no data
            if(data2[t] == NODATA){
                element++;
                if(element >= wA.length){
                    System.out.println("BREAK1: too less data NIDW had been reduced!");
                    cont = false;
                    //value = NODATA;
                }
                else{
                    t = wA[element];
                }
            } else{
                valid = true;
                data[counter] = data2[t];
                weights[counter] = weights2[t];
                counter++;
                element++;
                /*if(element >= wA.length){
                    if(element <= nIDW)
                        System.out.println("NIDW has been reduced, because of too less valid data!");
                    cont = false;
                }*/
                
            }
            
        }
        //normalising weights
        double weightsum = 0;
        for(int i = 0; i < counter; i++)
            weightsum += weights[i];
        for(int i = 0; i < counter; i++)
            weights[i] = weights[i] / weightsum;
        
        for (int i = 0; i < counter; i++) {  
                System.out.println("data["+i+"] = " + data[i] + " * weights["+i+"] = " + weights[i]);
                value = value + (data[i] * weights[i]);
        }
        
        
        if(!valid){
            value = -9999;
            System.out.println("No valid data sets therefore Value: " + value);
        }
        else{
            System.out.println("Value: " + value);
        }
    }
    
}
