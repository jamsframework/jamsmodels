/*
 * NashSutcliffe.java
 *
 * Created on 30. November 2005, 12:15
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package org.unijena.j2k.efficiencies;

/**
 *
 * @author Peter Krause
 */
public class NashSutcliffe {
    
    /** Creates a new instance of NashSutcliffe */
    public NashSutcliffe() {
    }
    
    /** Calculates the efficiency between a test data set and a verification data set
     * after Nash & Sutcliffe (1970). The efficiency is described as the proportion of
     * the cumulated cubic deviation between both data sets and the cumulated cubic
     * deviation between the verification data set and its mean value.
     * @param predicition the simulation data set
     * @param validation the validation (observed) data set
     * @param pow the power for the deviation terms
     * @return the calculated efficiency or -9999 if an error occurs
     */    
    public static double efficiency(double[] prediction, double[] validation, double pow){
        int pre_size = prediction.length;
        int val_size = validation.length;
                
        int steps = 0;
        
        double sum_td = 0;
        double sum_vd = 0;
        
        /** checking if both data arrays have the same number of elements*/
        if(pre_size != val_size){
            System.err.println("Prediction data and validation data are not consistent!");
            return -9999;
        }
        else{
            steps = pre_size;
        }
        
        /**summing up both data sets */
        for(int i = 0; i < steps; i++){
            sum_td = sum_td + prediction[i];
            sum_vd = sum_vd + validation[i];
        }
        
        /** calculating mean values for both data sets */
        double mean_td = sum_td / steps;
        double mean_vd = sum_vd / steps;
        
        /** calculating mean pow deviations */
        double td_vd = 0;
        double vd_mean = 0;
        for(int i = 0; i < steps; i++){
            td_vd = td_vd + (Math.pow((Math.abs(validation[i] - prediction[i])),pow));
            vd_mean = vd_mean + (Math.pow((Math.abs(validation[i] - mean_vd)),pow));
        }
        
        /** calculating efficiency after Nash & Sutcliffe (1970) */
        double efficiency = 1 - (td_vd / vd_mean);
        
        return efficiency;
         
    }
    
    /** Calculates the efficiency between the log values of a test data set and a verification data set
     * after Nash & Sutcliffe (1970). The efficiency is described as the proportion of
     * the cumulated cubic deviation between both data sets and the cumulated cubic
     * deviation between the verification data set and its mean value.
     * @param predicition the simulation data set
     * @param validation the validation (observed) data set
     * @param pow the power for the deviation terms
     * @return the calculated log_efficiency or -9999 if an error occurs
     */    
    public static double logEfficiency(double[] prediction, double[] validation, double pow){
        int pre_size = prediction.length;
        int val_size = validation.length;
        
        int steps = 0;
        
        double sum_log_pd = 0;
        double sum_log_vd = 0;
        
        /** checking if both data arrays have the same number of elements*/
        if(pre_size != val_size){
            System.err.println("Prediction data and validation data are not consistent!");
            return -9999;
        }
        else{
            steps = pre_size;
        }
        
        /** calculating logarithmic values of both data sets. Sets 0 if data is 0 */
        double[] log_preData = new double[pre_size];
        double[] log_valData = new double[val_size];
        
        for(int i = 0; i < steps; i++){
            if(prediction[i] < 0){
                System.err.println("Logarithmic efficiency can only be calculated for positive values!");
                return -9999;
            }
            if(validation[i] < 0){
                System.err.println("Logarithmic efficiency can only be calculated for positive values!");
                return -9999;
            }
            
            if(prediction[i] == 0){
                log_preData[i] = 0;
            }
            else{
                log_preData[i] = Math.log(prediction[i]);
            }
            
            if(validation[i] == 0){
                log_valData[i] = 0;
            }
            else{
                log_valData[i] = Math.log(validation[i]);
            }   
        }
        
        /**summing up both data sets */
        for(int i = 0; i < steps; i++){
            sum_log_pd = sum_log_pd + log_preData[i];
            sum_log_vd = sum_log_vd + log_valData[i];
        }
        
        /** calculating mean values for both data sets */
        double mean_log_pd = sum_log_pd / steps;
        double mean_log_vd = sum_log_vd / steps;
        
        /** calculating mean pow deviations */
        double pd_log_vd = 0;
        double vd_log_mean = 0;
        for(int i = 0; i < steps; i++){
            pd_log_vd = pd_log_vd + (Math.pow(Math.abs(log_valData[i] - log_preData[i]),pow));
            vd_log_mean = vd_log_mean + (Math.pow(Math.abs(log_valData[i] - mean_log_vd),pow));
        }
        
        /** calculating efficiency after Nash & Sutcliffe (1970) */
        double log_efficiency = 1 - (pd_log_vd / vd_log_mean);
        
        return log_efficiency;
         
    }
    
}
