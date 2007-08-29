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
    int KernelParameterCount;
    double theta[] = null;    
    double meanTheta[] = null;
    public MeanModell MM = null;
    
    public void SetMeanModell(MeanModell MM) {
	this.MM = MM;
	
	parameterCount = KernelParameterCount + MM.GetParameterCount();
    }
    
    public boolean SetParameter(double []theta) {
	if (theta.length < parameterCount) {
	    return false;
	}
	this.theta = new double[KernelParameterCount];
	for (int i=0;i<KernelParameterCount;i++) {
	    this.theta[i] = theta[i];
	}
	if (MM != null) {
	    if (MM.GetParameterCount() != 0) {
		meanTheta = new double[MM.GetParameterCount()];	
		for (int i=KernelParameterCount;i<theta.length;i++) {
		    meanTheta[i-KernelParameterCount] = Math.log(theta[i]);
		}
		MM.SetParameters(meanTheta);
	    }
	}	
	//this.theta = theta;
	return true;
    }         
     
    abstract public double kernel(double x[],double y[],int index1,int index2);
    abstract public double dkernel(double x[],double y[],int d);
    
    public int getParameterCount() {
	return parameterCount;
    }    
    
}
