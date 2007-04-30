/*
 * GaussianLearner.java
 *
 * Created on 16. April 2007, 18:08
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.unijena.predictionnet;


import org.unijena.j2k.*;
import org.unijena.jams.data.*;
import org.unijena.jams.model.*;
import java.util.*;
import java.io.*;
import org.unijena.jams.JAMS;
import java.util.Random;
import Jama.*;
import Jama.Matrix;
import Jama.LUDecomposition;
import Jama.util.Maths;

public class GaussianLearner extends Learner  {
    Matrix CovarianzMatrix;
    CholeskyDecomposition Solver;
    Matrix Observations;
    Matrix alpha;
    Matrix invCovarianzMatrix;
        
    double average = 0.0;
    double sigma = 0.2;      
    int kernelmethod = 2;    
    int optimizeMethod = 1;
    
    double theta[];    
    double marginal_likelihood;    
    
    public double SqrDistance2(double x[],double y[]) {
	double sum = 0;
	double tmp;
	for (int i=0;i<x.length;i++) {
	    tmp = (x[i]-y[i])/theta[i];
	    sum += tmp*tmp;//Math.pow((x[i]-y[i])/(theta[i]),2);
	}	
	return sum;
    }
    public double Distance2(double x[],double y[]) {
	return Math.sqrt(SqrDistance2(x,y));
    }
    
    public double dotProd(double x[],double y[]) {
	double sum = 0;
	for (int i=0;i<x.length;i++) {
	    sum += x[i]*y[i];
	}
	return sum;
    }
    
    /*public double dkernel(double x[],double y[]) {
	double r = SqrDistance2(x,y);
	//r /= theta[0]*theta[0];
	//return r*Math.exp(-0.5*r/(theta[0]*theta[0]))/Math.pow(theta[0],3);
	return r*Math.exp(-0.5*r);
    }*/
    
    public double dkernel(int i,int j,int d) {
	double x[] = this.normed_data[i];
	double y[] = this.normed_data[j];
	
	switch (kernelmethod)  {
	    case 2: {
		double r = SqrDistance2(x,y);
		double dr = (x[d]-y[d])/theta[d];
		
		return dr*dr*Math.exp(-0.5*r)/theta[d];
		//return r*Math.exp(-0.5*r)/theta[0];
	    }
	    case 5: {
		double alpha = theta[DataLength];
		double r = SqrDistance2(x,y);	
		double base = 1.0 + r / (2.0*alpha);
		double expterm = Math.pow(base,-alpha);
		
		if (d < DataLength) {		    		    
		    double dr = (x[d]-y[d])/theta[d];		
		    return expterm*dr*dr/(base*theta[d]);
		}
		if (d == DataLength) {
		    return expterm*(-Math.log(base)+r/(2.0*alpha*base));
		}		
	    }
	}
	return 0;
    }
	    
    public double kernel(double x[],double y[]) {
	switch (kernelmethod) {
	    //WEKA covariance function
	    case 1:
		double val1 = dotProd(x, y);
		double vali = dotProd(x, x);
		double valj = dotProd(y, y);
	
		return Math.exp(theta[0] * (2.0 * val1 - vali - valj)) ;
	    //squared exponential function
	    case 2:
		return Math.exp(-0.5*SqrDistance2(x,y));	   
	    //matern class for v = 3/2
	    case 3: {	    
		double r = Distance2(x,y);
		return (1.0 + 1.732*r/theta[0])*Math.exp(-1.732*r/theta[0]); }
	    //matern class for v = 5/2
	    case 4: {
		double r = Distance2(x,y);
		return (1.0 + 1.732*r/theta[0] + 2.236*r*r/(3*theta[0]*theta[0]))*Math.exp(-2.236*r/theta[0]); }
	    //rational quadratic covariance function
	    case 5: {
		double r = SqrDistance2(x,y);
		return Math.pow(1.0 + r / (2*theta[DataLength]),-theta[DataLength]);
	    }
	    default:
		return 0;
	}	
    }
    
    Matrix BuildDerivedMatrix(int d) {
	Matrix diff = new Matrix(TrainLength,TrainLength);
	for (int i=0;i<this.TrainLength;i++) {
	    for (int j=i;j<this.TrainLength;j++) {
		double value = dkernel(i,j,d);
		diff.set(i,j,value);
		diff.set(j,i,value);
	    }
	}
	return diff;
    }
    
    double [] getGradient() {
	if (optimizeMethod == 1)
	    return getlogMargGradient();
	else
	    return getLOOGradient();	
    }
    double [] getLOOGradient() {			
	invCovarianzMatrix = CovarianzMatrix.inverse();
	double gradient[] = new double[theta.length];
		
	for (int j=0;j<this.theta.length;j++) {
	    Matrix diff = BuildDerivedMatrix(j);	    
	    Matrix Z = invCovarianzMatrix.times(diff);
	    Matrix Zalpha = Z.times(alpha);
	    double sum = 0;
	    
	    for (int i=0;i<this.TrainLength;i++) {
		double alpha_i = this.alpha.get(i,0);
		double term1 = alpha_i*Zalpha.get(i,0);
		double term2 = -0.5*(1.0 + (alpha_i* alpha_i / invCovarianzMatrix.get(i,i)));
		double term3 = 0;
		for (int k=0;k<this.TrainLength;k++) {
		    term3 += Z.get(i,k)*invCovarianzMatrix.get(k,i);
		}
		sum += (term1 + term2*term3) / invCovarianzMatrix.get(i,i);
	    }
	    gradient[j] = sum;
	}
	return gradient;
    }
    
    double [] getlogMargGradient() {
	double gradient[] = new double[theta.length];
	
	invCovarianzMatrix = CovarianzMatrix.inverse();
	Matrix yK = Observations.transpose().times(invCovarianzMatrix);
	Matrix Ky = invCovarianzMatrix.times(Observations);
	
	for (int i=0;i<this.theta.length;i++) {	    
	    //build derived matrix
	    Matrix diff = BuildDerivedMatrix(i);		    			    	    
	    Matrix tmp = yK.times(diff);
	    tmp = tmp.times(Ky);
	    	    
	    int n = this.TrainLength;
	    double sum = 0.0;
	    for (int k=0;k<n;k++) {
		for (int l=0;l<n;l++) {
		    sum += invCovarianzMatrix.get(k,l)*diff.get(l,k);
		}
	    }
	    gradient[i] = 0.5*tmp.get(0,0) - 0.5*sum;
	}		
	return gradient;
    }
    
    double[] norm(double x[]) {
	double sum = 0;
	double result[] = new double[x.length];
	
	for (int i=0;i<x.length;i++) {
	    sum += x[i]*x[i];
	}
	sum = Math.sqrt(sum);
	for (int i=0;i<x.length;i++) {
	    result[i] = x[i] / sum;
	}
	return result;
    }
    
    void optimizeLength() {
	//gradient based!!
	double change = 1.0;
	double epsilon = 0.01;
	double step = 0.1;
	double avgchange = 1.0;		
	double beta = 0.5;
	double oldgradient[] = new double[theta.length];
	int iteration = 1;
	
	for (int i=0;i<theta.length;i++) {
	    oldgradient[i] = 0;
	}
	
	double value = this.Train();	
	while (avgchange > epsilon) {
	    System.out.println("step: " + iteration);
	    iteration++;
	    	    
	    double gradient[] = getGradient();	    	    	    	    
	    gradient = norm(gradient);
	    	    	    
	    System.out.println("actual value:" + value);	    
	    for (int i=0;i<gradient.length;i++) {
		System.out.print("theta_" + i + " " + theta[i] + " ");		 
	    }
	    	    
	    System.out.println("");
	    for (int i=0;i<gradient.length;i++) {
		System.out.print("gradient_" + i + " " + gradient[i] + " ");		 
	    }
	    System.out.println("");
	    
	    double newvalue = value;	    	    
	    	    	    	    	    	    	    
	    for (int i=0;i<theta.length;i++) {
	        theta[i] = theta[i] + step*gradient[i] + beta*oldgradient[i];
		if (theta[i] < 0.0) {
		    theta[i] = 0.01;
		}
	        oldgradient[i] = step*gradient[i] + beta*oldgradient[i];
	    }
	    newvalue = Train();				
							 
	    change = Math.abs(newvalue - value);
	    this.average = average + change / 10.0;
	    value = newvalue;	
	}
    }
    
    void Testoptimizer() {
	//gradient based!!
	double change = 1.0;
	double epsilon = 0.01;
	double step = 0.1;
	double avgchange = 1.0;		
	double beta = 0.5;
	double oldgradient[] = new double[theta.length];
	int iteration = 1;
	
	for (int i=0;i<theta.length;i++) {
	    oldgradient[i] = 0;
	}
	
	double value = this.Train();	
	while (avgchange > epsilon) {
	    System.out.println("step: " + iteration);
	    iteration++;
	    	    
	    double gradient[] = getGradient();	    	    	    	    
	    gradient = norm(gradient);
	    	    	    
	    System.out.println("actual value:" + value);	    
	    for (int i=0;i<gradient.length;i++) {
		System.out.print("theta_" + i + " " + theta[i] + " ");		 
	    }
	    	    
	    System.out.println("");
	    for (int i=0;i<gradient.length;i++) {
		System.out.print("gradient_" + i + " " + gradient[i] + " ");		 
	    }
	    System.out.println("");
	    
	    double newvalue = value;	    	    
	    double oldtheta = theta[0];
	    
	    step *= 4.0;
	    
	    while (newvalue <= value) {
		step /= 2.0;
		
		theta[0] = oldtheta + step*gradient[0];
		for (int i=0;i<theta.length;i++)
		    theta[i] = theta[0];
		newvalue = Train();
		
		System.out.println("Try with step:" + -step + "get:" + newvalue);
		
		theta[0] = oldtheta - step*gradient[0];
		for (int i=0;i<theta.length;i++)
		    theta[i] = theta[0];
		newvalue = Train();
		
		System.out.println("Try with step:" + step + "get:" + newvalue);				
	    }
	    
	    change = Math.abs(newvalue - value);
	    this.average = average + change / 10.0;
	    value = newvalue;	
	}
    }
    
    public double Train() {
	CovarianzMatrix = new Matrix(TrainLength,TrainLength);
	Observations = new Matrix(TrainLength,1);
	
	average = 0.0;
		
	for (int i=0;i<TrainLength;i++) {
	    average += result[i];
	    for (int j=0;j<i;j++) {
		//calculate covariance for xi,xj
		double varianz = kernel(normalize(data[i]),normalize(data[j]));
		CovarianzMatrix.set(i,j,varianz);
		CovarianzMatrix.set(j,i,varianz);
	    }
	    double varianz = kernel(normalize(data[i]),normalize(data[i]));
	    CovarianzMatrix.set(i,i,varianz + sigma*sigma);	    
	}	
	
	average /= TrainLength;
	
	for (int i=0;i<TrainLength;i++) {
	    Observations.set(i,0,this.result[i] - average);
	}
	Solver = CovarianzMatrix.chol();
	//error!!
	if (!Solver.isSPD()) {
	    System.out.println("NOT a SPD Matrix");
	    return -1000000000000.0;
	}
	alpha = Solver.solve(Observations);
	
	/*Matrix test = CovarianzMatrix.times(alpha).minus(Observations);
	System.out.println("norm:" + test.norm1());*/
	
	//performance
	if (this.optimizeMethod == 1)
	    return getMarginalLikelihood();
	return this.getLOOlogPredictiveProbability();
    }
    
    public double getMarginalLikelihood() {
	double n = this.TrainLength;
	double term1 = -0.5*Observations.transpose().times(this.alpha).get(0,0);		
	double term2 = 0;
	
	Matrix L = this.Solver.getL();
	
	for (int i=0;i<L.getColumnDimension();i++) {
	    term2 += 2.0 * Math.log(L.get(i,i));
	}
	
	term2 = -0.5*term2;
	double term3 = -n/2.0 * Math.log(2*Math.PI);
	return term1 + term2 + term3;
    }
    
    public double getLOOlogPredictiveProbability() {	
	invCovarianzMatrix = this.CovarianzMatrix.inverse();	
	double logp = 0;
	
	for (int i=0;i<TrainLength;i++) {
	    //leave i out
	    double mu_i = result[i] - alpha.get(i,0)/ invCovarianzMatrix.get(i,i);
	    double sigma_i = 1.0 / invCovarianzMatrix.get(i,i);
	    
	    logp += - Math.log(sigma_i) - Math.pow(result[i] - mu_i,2.0) - 0.5*Math.log(2.0*Math.PI);
	}
	return logp;
    }
    
    
    
    public double getVariance(double x[]) {
	double k = kernel(normalize(x),normalize(x));
	
	Matrix kstar = new Matrix(1,TrainLength);
	
	for (int i=0;i<TrainLength;i++) {	    
	    //calculate covariance for xi,x
	    double covariance = kernel(normalize(data[i]),normalize(x));
	    kstar.set(0,i,covariance);
	}
	
	Matrix tmp = Solver.solve(kstar.transpose());
	return k - kstar.times(tmp).get(0,0);
    }
    
    public double Predict(double x[]) {
	Matrix kstar = new Matrix(1,TrainLength);
	
	for (int i=0;i<TrainLength;i++) {	    
	    //calculate covariance for xi,x
	    double varianz = kernel(normalize(data[i]),normalize(x));
	    kstar.set(0,i,varianz);
	}
		
	Matrix prediction = (kstar.times(alpha));
	
	return prediction.get(0,0) + average;
    }    
}
