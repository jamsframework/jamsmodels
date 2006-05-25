/*
 * VolumeError.java
 *
 * Created on 23. Mai 2006, 09:13
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.unijena.j2k.efficiencies;

/**
 *
 * @author c0krpe
 */
public class VolumeError {
    
    /** Creates a new instance of VolumeError */
    public VolumeError() {
    }
    
    public static double absVolumeError(double[] prediction, double[] validation){
        double volError = 0;
        for(int i = 0; i < prediction.length; i++){
            volError += (prediction[i] - validation[i]);
        }
        return volError; 
    }
    
}
