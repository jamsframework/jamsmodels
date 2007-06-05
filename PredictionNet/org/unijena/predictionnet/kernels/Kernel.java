/*
 * Kernel.java
 *
 * Created on 1. Juni 2007, 15:57
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.unijena.predictionnet.kernels;

/**
 *
 * @author Christian(web)
 */
public abstract class Kernel{
    int inputDim;
    int parameterCount;
    double theta[] = null;    
            
     public boolean SetParameter(double []theta) {
	if (theta.length < parameterCount) {
	    return false;
	}
	this.theta = theta;
	return true;
    }         
     
    abstract public double kernel(double x[],double y[]);
    abstract public double dkernel(double x[],double y[],int d);
    
    public int getParameterCount() {
	return parameterCount;
    }    
    
}
