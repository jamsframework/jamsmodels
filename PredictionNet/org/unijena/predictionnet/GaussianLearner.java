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
    
    double gamma = 1.0;
    double average = 0.0;
    double sigma = 0.05;
    double length = 0.4;
    
    int kernelmethod = 2;
    
    public double SqrDistance2(double x[],double y[]) {
	if (x.length != y.length) {
	    System.out.println("Distance -> x length != y length");
	    return Double.NaN;
	}
	double sum = 0;
	for (int i=0;i<x.length;i++) {
	    sum += (x[i]-y[i])*(x[i]-y[i]);
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
    
    public double kernel(double x[],double y[]) {
	switch (kernelmethod) {
	    case 1:
		double val1 = dotProd(x, y);
		double vali = dotProd(x, x);
		double valj = dotProd(y, y);
	
		return Math.exp(gamma * (2.0 * val1 - vali - valj)) ;
	    case 2:
		return Math.exp(-0.5*SqrDistance2(x,y)/(length*length));
	    case 3:
		return 0.05+dotProd(x,y);
	    case 4:
		double r = Distance2(x,y);
		return (1.0 + 1.732*r/length)*Math.exp(-1.732*r/length);
	    default:
		return 0;
	}	
    }
    
    public void Train() {
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
	alpha = Solver.solve(Observations);
	
	Matrix test = CovarianzMatrix.times(alpha).minus(Observations);
	System.out.println("norm:" + test.norm1());
	
	
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
