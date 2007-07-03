/*
 * RationalQuadratic.java
 *
 * Created on 1. Juni 2007, 16:52
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.unijena.predictionnet.kernels;

/**
 *
 * @author Christian(web)
 */
public class TestKernel extends Kernel {
    
    /** Creates a new instance of RationalQuadratic */
    public TestKernel(int inputDim) {              
	this.inputDim = inputDim;	
	this.parameterCount = 2;
    }
       
    public double SqrDistance2(double x[],double y[]) {
	double sum = 0;
	double tmp;
	for (int i=0;i<x.length;i++) {
	    tmp = (x[i]-y[i])/*theta[i]*/;
	    sum += tmp*tmp;
	}	
	return sum;
    }
    
    public double kernel(double x[],double y[],int index1,int index2) {
	double r = SqrDistance2(x,y);
	double noise = 0.0;
	
	if (index1 == index2) {
	    noise = this.theta[1]*this.theta[1];
	}
	return Math.pow(1.0 + r / (2*theta[0]),-theta[0]) + noise;			
    }
    
    public double dkernel(double x[],double y[],int d) {
	double alpha = theta[inputDim];
	double r = SqrDistance2(x,y);	
	double base = 1.0 + r / (2.0*alpha);
	double expterm = Math.pow(base,-alpha);
		
	if (d < inputDim) {		    		    
	    double dr = (x[d]-y[d])/theta[d];		
	    return expterm*dr*dr/(base*theta[d]);
	}
	if (d == inputDim) {
	    return expterm*(-Math.log(base)+r/(2.0*alpha*base));
	}		
	//do not adjust sigma
	if (d > inputDim)
	    return 0.0;
	return 0.0;
    }       
}
