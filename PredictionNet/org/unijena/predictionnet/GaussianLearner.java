/*
 * GaussianLearner.java
 *
 * Created on 16. April 2007, 18:08
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.unijena.predictionnet;


import org.unijena.jams.data.*;
import org.unijena.jams.model.*;
import java.io.*;
import Jama.*;
import Jama.Matrix;
import jams.components.optimizer.SCE_Comparator;
import java.util.Arrays;
import java.util.Random;
import java.util.StringTokenizer;
import org.unijena.jams.io.GenericDataWriter;
import org.unijena.predictionnet.kernels.*;
import org.unijena.predictionnet.kernels.LinearMeanModell;

public class GaussianLearner extends Learner  {
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "TimeSerie of Temp Data"
            )
            public JAMSEntity trainData;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "TimeSerie of Temp Data"
            )
            public JAMSEntity optimizationData;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "TimeSerie of Temp Data"
            )
            public JAMSEntity validationData;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "TimeSerie of Temp Data"
            )
            public JAMSInteger kernelMethod;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "TimeSerie of Temp Data"
            )
            public JAMSInteger MeanMethod;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "TimeSerie of Temp Data"
            )
            public JAMSInteger PerformanceMeasure;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "TimeSerie of Temp Data"
            )
            public JAMSDoubleArray param_theta;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "TimeSerie of Temp Data"
            )
            public JAMSString parameterFile = null;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "TimeSerie of Temp Data"
            )
            public JAMSString resultFile = null;
                   
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "TimeSerie of Temp Data"
            )
            public JAMSBoolean doOptimization;
    
    Matrix CovarianzMatrix;
    CholeskyDecomposition Solver;
    Matrix Observations;
    Matrix alpha;
    Matrix invCovarianzMatrix;
               
    double logtheta[];    
    double theta[];
                        
    static final int MAXIMIZATION = 1;
    static final int MINIMIZATION = 2;
    static final int ABSMAXIMIZATION = 3;  
    static final int ABSMINIMIZATION = 4;
     
    int MaximizeEff = 1;
    
    Kernel kernel;
//table for gauss dist. from 0 to 4
    static double gaussianDistribution[] = {
        0.5000,	0.5039,	0.5079,	0.5119,	0.5159,	0.5199,	0.5239,	0.5279,	0.5318,	0.53586,
 	0.5398,	0.5438,	0.5477,	0.5517,	0.5556,	0.5596,	0.5635,	0.5674,	0.5714,	0.57535,
 	0.5792,	0.5831,	0.5870,	0.5909,	0.5948,	0.5987,	0.6025,	0.6064,	0.6102,	0.61409,
 	0.6179,	0.6217,	0.6255,	0.6293,	0.6330,	0.6368,	0.6405,	0.6443,	0.6480,	0.65173,
 	0.6554,	0.6591,	0.6627,	0.6664,	0.6700,	0.6736,	0.6772,	0.6808,	0.6843,	0.68793,
 	0.6914,	0.6949,	0.6984,	0.7019,	0.7054,	0.7088,	0.7122,	0.7156,	0.7190,	0.72240,
 	0.7257,	0.7290,	0.7323,	0.7356,	0.7389,	0.7421,	0.7453,	0.7485,	0.7517,	0.75490,
 	0.7580,	0.7611,	0.7642,	0.7673,	0.7703,	0.7733,	0.7763,	0.7793,	0.7823,	0.78524,
 	0.7881,	0.7910,	0.7938,	0.7967,	0.7995,	0.8023,	0.8051,	0.8078,	0.8105,	0.81327,
 	0.8159,	0.8185,	0.8212,	0.8238,	0.8263,	0.8289,	0.8314,	0.8339,	0.8364,	0.83891,
 	0.8413,	0.8437,	0.8461,	0.8484,	0.8508,	0.8531,	0.8554,	0.8576,	0.8599,	0.86214,
 	0.8643,	0.8665,	0.8686,	0.8707,	0.8728,	0.8749,	0.8769,	0.8790,	0.8810,	0.88298,
 	0.8849,	0.8868,	0.8887,	0.8906,	0.8925,	0.8943,	0.8961,	0.8979,	0.8997,	0.90147,
 	0.9032,	0.9049,	0.9065,	0.9082,	0.9098,	0.9114,	0.9130,	0.9146,	0.9162,	0.91774,
 	0.9192,	0.9207,	0.9222,	0.9236,	0.9250,	0.9264,	0.9278,	0.9292,	0.9305,	0.93189,
 	0.9331,	0.9344,	0.9357,	0.9369,	0.9382,	0.9394,	0.9406,	0.9417,	0.9429,	0.94408,
 	0.9452,	0.9463,	0.9473,	0.9484,	0.9495,	0.9505,	0.9515,	0.9525,	0.9535,	0.95449,
 	0.9554,	0.9563,	0.9572,	0.9581,	0.9590,	0.9599,	0.9608,	0.9616,	0.9624,	0.96327,
 	0.9640,	0.9648,	0.9656,	0.9663,	0.9671,	0.9678,	0.9685,	0.9692,	0.9699,	0.97062,
 	0.9712,	0.9719,	0.9725,	0.9732,	0.9738,	0.9744,	0.9750,	0.9755,	0.9761,	0.97670,
 	0.9772,	0.9777,	0.9783,	0.9788,	0.9793,	0.9798,	0.9803,	0.9807,	0.9812,	0.98169,
 	0.9821,	0.9825,	0.9830,	0.9834,	0.9838,	0.9842,	0.9846,	0.9850,	0.9853,	0.98574,
 	0.9861,	0.9864,	0.9867,	0.9871,	0.9874,	0.9877,	0.9880,	0.9884,	0.9887,	0.98899,
 	0.9892,	0.9895,	0.9898,	0.9901,	0.9903,	0.9906,	0.9908,	0.9911,	0.9913,	0.99158,
 	0.9918,	0.9920,	0.9922,	0.9924,	0.9926,	0.9928,	0.9930,	0.9932,	0.9934,	0.99361,
 	0.9937,	0.9939,	0.9941,	0.9943,	0.9944,	0.9946,	0.9947,	0.9949,	0.9950,	0.99520,
 	0.9953,	0.9954,	0.9956,	0.9957,	0.9958,	0.9959,	0.9960,	0.9962,	0.9963,	0.99643,
 	0.9965,	0.9966,	0.9967,	0.9968,	0.9969,	0.9970,	0.9971,	0.9972,	0.9972,	0.99736,
 	0.9974,	0.9975,	0.9976,	0.9976,	0.9977,	0.9978,	0.9978,	0.9979,	0.9980,	0.99807,
 	0.9981,	0.9981,	0.9982,	0.9983,	0.9983,	0.9984,	0.9984,	0.9985,	0.9985,	0.99861,
 	0.9986,	0.9986,	0.9987,	0.9987,	0.9988,	0.9988,	0.9988,	0.9989,	0.9989,	0.99900,
 	0.9990,	0.9990,	0.9991,	0.9991,	0.9991,	0.9991,	0.9992,	0.9992,	0.9992,	0.99929,
 	0.9993,	0.9993,	0.9993,	0.9993,	0.9994,	0.9994,	0.9994,	0.9994,	0.9994,	0.99950,
 	0.9995,	0.9995,	0.9995,	0.9995,	0.9995,	0.9996,	0.9996,	0.9996,	0.9996,	0.99965,
 	0.9996,	0.9996,	0.9996,	0.9997,	0.9997,	0.9997,	0.9997,	0.9997,	0.9997,	0.99976,
 	0.9997,	0.9997,	0.9997,	0.9997,	0.9998,	0.9998,	0.9998,	0.9998,	0.9998,	0.99983,
 	0.9998,	0.9998,	0.9998,	0.9998,	0.9998,	0.9998,	0.9998,	0.9998,	0.9998,	0.99989,
 	0.9998,	0.9999,	0.9999,	0.9999,	0.9999,	0.9999,	0.9999,	0.9999,	0.9999,	0.99992,
 	0.9999,	0.9999,	0.9999,	0.9999,	0.9999,	0.9999,	0.9999,	0.9999,	0.9999,	0.99995,
 	0.9999,	0.9999,	0.9999,	0.9999,	0.9999,	0.9999,	0.9999,	0.9999,	0.9999,	0.99997,
 	0.9999,	0.9999,	0.9999,	0.9999,	0.9999,	0.9999,	0.9999,	0.9999,	0.9999,	0.99998
    };
    
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
	    double mu_i = this.Observations.get(i,0) - (alpha.get(i,0)/ invCovarianzMatrix.get(i,i));
	    double sigma_i = 1.0 / invCovarianzMatrix.get(i,i);

	    if (sigma_i < 0)
		sigma_i = -sigma_i;
	    //sigma_i = sigma_i*sigma_i;
	    sigma_i = Math.sqrt(sigma_i);
	    
	    logp += (- Math.log(sigma_i) - Math.pow((this.Observations.get(i,0) - mu_i)/sigma_i,2.0)/2.0 /* - 0.5*Math.log(2.0*Math.PI)*/);
	}
	return logp;
    }
    
    public double getLOOSquareError() {	
	//Matrix id = Matrix.identity(CovarianzMatrix.getRowDimension(),CovarianzMatrix.getRowDimension());	
	invCovarianzMatrix = this.CovarianzMatrix.inverse();
	double error = 0;
	
	for (int i=0;i<TrainLength;i++) {
	    //leave i out
	    double mu_i = this.Observations.get(i,0) - alpha.get(i,0)/ invCovarianzMatrix.get(i,i);
	    
	    //System.out.println("Leave " + i + " out -> " + mu_i + "  observation -> " + this.Observations.get(i,0));
	    error += (mu_i - this.Observations.get(i,0))*(mu_i - this.Observations.get(i,0));
	}
	return -error;
    }        
    
    public double getSplitValidationError() {
	double result[] = null;
	double correctValue[] = null;
	try {
	    result = this.Predict(false);
	    correctValue = ((double[])this.validationData.getObject("predict"));
	}
	catch(Exception e) {
	    System.out.println("GP SplitValidation - Error: " + e.toString());
	}
	double sum = 0.0;
	for (int i=0;i<result.length;i++) {
	    sum += (result[i] - correctValue[i])*(result[i] - correctValue[i]);
	}
	return -sum;
    }
    
    public double Train(int PerformanceMeasure) {
	CovarianzMatrix = new Matrix(TrainLength,TrainLength);
	Observations = new Matrix(TrainLength,1);
			
	theta = new double[this.kernel.getParameterCount()];
	logtheta = new double[this.kernel.getParameterCount()];
	
	//read params from file
	if (this.parameterFile != null && param_theta == null) {
	    BufferedReader reader;
	    try {
		reader = new BufferedReader(new FileReader(this.parameterFile.getValue()));
		for (int i=0;i<theta.length;i++) {
		    logtheta[i] = Math.log(new Double(reader.readLine()).doubleValue());	
		}
		reader.close();
	    }
	    catch (Exception e) {
	        System.out.println("Could not open or read parameter file, becauce:" + e.toString());
	        return 0.0;
	    }	
	}
	//try to use parameters directly
	if (param_theta != null) {
	    for (int i=0;i<logtheta.length;i++) {
		logtheta[i] = Math.log(param_theta.getValue()[i]);
	    }
	}
	
	for (int i=0;i<this.theta.length;i++) {
	    theta[i] = Math.exp(logtheta[i]);
	}
	if (!this.kernel.SetParameter(theta)) {
	    System.out.println("zu wenig Parametern");
	}
			
	for (int i=0;i<TrainLength;i++) {	    
	    for (int j=0;j<i;j++) {
		//calculate covariance for xi,xj
		double varianz = this.kernel.kernel(normalize(data[i]),normalize(data[j]),i,j);
		CovarianzMatrix.set(i,j,varianz);
		CovarianzMatrix.set(j,i,varianz);
	    }
	    double varianz = this.kernel.kernel(normalize(data[i]),normalize(data[i]),i,i);
	    CovarianzMatrix.set(i,i,varianz);	    
	}	
		
	Observations = this.kernel.MM.Transform(data,result);		
	Solver = CovarianzMatrix.chol();
	//error!!
	if (!Solver.isSPD()) {
	    System.out.println("NOT a SPD Matrix");
	    return -1000000000000.0;
	}
	alpha = Solver.solve(Observations);
		
	switch (PerformanceMeasure) {
	    case 1: return getMarginalLikelihood();
	    case 2: return this.getLOOlogPredictiveProbability();
	    case 3: return this.getLOOSquareError();
	    case 4: return this.getSplitValidationError();
	    default: return 0.0;
	}	
    }

    public double GetMean(double x[]) {
        
        Matrix kstar = new Matrix(1,TrainLength);
        for (int i=0;i<TrainLength;i++) {	    
            //calculate covariance for xi,x
            double variance = this.kernel.kernel(normalize(data[i]),normalize(x),i,-1);
            kstar.set(0,i,variance);
        }
        
        Matrix prediction = (kstar.times(alpha));
        
        double x_tmp[][] = new double[1][];
        x_tmp[0] = x;
        
        result = this.kernel.MM.ReTransform(x_tmp,prediction);
        
        return result[0];
    }
    
    public double GetVariance(double x[]) {
        
        Matrix kstar = new Matrix(1,TrainLength);
        Matrix kstarT = new Matrix(TrainLength,1);
        for (int i=0;i<TrainLength;i++) {	    
            //calculate covariance for xi,x
            double variance = this.kernel.kernel(normalize(data[i]),normalize(x),i,-1);
            kstar.set(0,i,variance);
            kstarT.set(i,0,variance);
        }                        
        Matrix alpha2 = Solver.solve(kstarT);
        Matrix prediction = (kstar.times(alpha2));
        
        double base = this.kernel.kernel(normalize(x),normalize(x),1,2);
        
        return base - prediction.get(0,0);
    }
    
    //berechnet wahrscheinlichkeit dafür, dass f(x) < target
    public double GetProbabilityForXLessY(double x[],double target){
        double mean = GetMean(x);
        double variance = GetVariance(x);
                                    
        //transform to 0/1 distriburion        
        target = target - mean;                            
        target /= variance;
        
        
        //wir suchen phi(target) := gauss(-unendl,target)
        //es gilt phi(target) = 1-phi(target)       
        //tabelliert sind werte von target = 0 ... 4.09
        
        double tmp = target;
        double prob;
        if (tmp<0)
            tmp = -tmp;
        
        int index = (int)Math.round((tmp/4.09)*(double)(gaussianDistribution.length));
        
        if (index >= gaussianDistribution.length){
            prob = 0.99998+0.00002*(1.0 - Math.sin((Math.PI/2.0) * (1.0/(tmp-3.0))));
            }
        else{            
            prob = gaussianDistribution[index];
            }
        
        if (target < 0){
            prob = 1.0 - prob;
        }       
        return prob;
    }
    
    public double[] Predict(boolean writeOutput) {	
	double x[][] = null;
	double correctValue[] = null;
	try {
	    x = (double[][])validationData.getObject("data");
	    correctValue = (double[])validationData.getObject("predict");
	}catch(Exception e) {
	    System.out.println("Could not find validation data. " + e.toString());
	    return null;
	}
	
	int m = x.length;
	Matrix kstar = new Matrix(m,TrainLength);
	
	for (int j=0;j<m;j++) {
	    for (int i=0;i<TrainLength;i++) {	    
		//calculate covariance for xi,x
		double varianz = this.kernel.kernel(normalize(data[i]),normalize(x[j]),i,-1);
		kstar.set(j,i,varianz);
	    }
	}
	Matrix prediction = (kstar.times(alpha));
	/*double result[] = new double[m];
	for (int j=0;j<m;j++) {
	    result[j] = prediction.get(j,0);
	}*/
			
	result = this.kernel.MM.ReTransform(x,prediction);
	/*double vresult[] = new double[result.length];
	
	for (int i=0;i<result.length;i++) {
	    
	}*/
	
	if (!writeOutput)
	    return result;
	
	BufferedWriter writer = null;
	try {
	    writer = new BufferedWriter(new FileWriter(this.resultFile.getValue(),true));	    
	}
	catch (Exception e) {
	    System.out.println("Could not open result file, becauce:" + e.toString());
	    System.out.println("results won't be saved");
	}
	    
	for (int i=0;i<x.length;i++) {			    
	    try {
		writer.write(new String(correctValue[i] + "\t" + result[i] + "\n"));		    
		writer.flush();
	    }
	    catch(Exception e) {
		System.out.println("could not write, because: " + e.toString());
	    }
	}
	try {
	    writer.close();
	}
	catch(Exception e) {
	    System.out.println("GP - Error" + e.toString());
	}
	return result;
    }  
                    
    public void optInit() {
	super.trainData = this.optimizationData;
	super.validationData = this.validationData;
		
	try {
	    super.run();
	}
	catch (Exception e) {
	    System.out.println("GP Init Fehler - " + e.toString());
	}			
    }
    
    public void trainInit() {
	super.trainData = this.trainData;
	super.validationData = this.validationData;
		
	try {
	    super.run();
	}
	catch (Exception e) {
	    System.out.println("GP Init Fehler - " + e.toString());
	}			
    }
                                            
    public double funct(double x[]) {
        double value = 0;
        
	if (param_theta == null) {
	    param_theta = new JAMSDoubleArray();
	    double array[] = new double[x.length];
	    param_theta.setValue(array);
	}
	
	for (int j=0;j<x.length;j++) {	    
	    param_theta.getValue()[j] = Math.exp(x[j]);
	}
        
        double performance = this.Train(this.PerformanceMeasure.getValue());

        if (MaximizeEff == MINIMIZATION)
            return performance;
        else if (MaximizeEff == ABSMINIMIZATION)
            return Math.abs(performance);
        else if (MaximizeEff == ABSMAXIMIZATION)
            return -Math.abs(performance);
        else if (MaximizeEff == MAXIMIZATION)
            return -performance;
        else
            return 0;
    }
         
    public void GradientDescent(double x[]) {
	double y1,y2,diff;						
	double [] grad = new double[x.length];		
	double [] alpha = new double[x.length];		
	double xp[] = new double[x.length];
	
	double alpha_min = 0.001;
	double diff_min = 0.025;
	double approxError = 0.0001;
		
	diff  = 1.0;
	
	y1 = funct(x);
	double y_alt;
	double y_neu = 1.0;
	double calpha = 0.1;
	
	for (int i=0;i<x.length;i++) {
	    alpha[i] = 0.1;
	}
	
	while ( calpha > alpha_min && diff > diff_min ) {	    	    
	    y_alt = y1;
	    //partial differences quotients
	    for (int i=0; i < x.length; i++) {	
		if (alpha[i] == 0) {
		    continue;
		}
	        for (int j=0; j < x.length; j++) {	
		    if (j == i) {
		        xp[j] = x[j]+approxError;			
		    }		    
		    else
		        xp[j] = x[j];			
		}
						
		y2 = funct(xp);		    		    	
		grad[i] = ((y2 - y1) / approxError);    
		
		if (grad[i] < 0) grad[i] = -1.0;
		else		 grad[i] = 1.0;
		//use armijo - method to obtain step width
		//decrease step - width until result is better than the last one
		
		//try to increase step - width
		alpha[i] *= 4.0;
		if (alpha[i] >= 2.0) alpha[i] = 2.0;
		while (true) {		
		    for (int k=0; k < x.length; k++) {	
			xp[k] = x[k];
			if (k==i) {
			     xp[k] = x[i] - alpha[i]*grad[i];
			     
			     if (xp[k] < -4.0)	xp[k] = -4.0;
			     if (xp[k] >  4.0)	xp[k] =  4.0;
			}
		    }
		
		    y_neu = funct(xp);
		
		    if (y_neu < y1)
			break;

		    alpha[i] /= 2.0;
		
		    if (alpha[i] < alpha_min) {
			xp[i] = x[i];
			alpha[i] = 0;
			y_neu = funct(xp);
			break;
		    }
		}
		y1 = y_neu;
		
		String info = "Gradient:\t";		
		String info2 = "Stelle:\t";		
		for (int k=0; k < x.length; k++) {	
		    x[k] = xp[k];
		    if (i == k)
			info += grad[i] + "\t";
		    else
			info += "0.0\t" ;
		    info2 += x[k] + "\t";
		}	
		getModel().getRuntime().println(info);		
		getModel().getRuntime().println(info2);									
		getModel().getRuntime().println("Funktionswert:\t" + y1 + "\t Alpha: " + calpha + "\t diff:" + diff);
	    }		    

	    for (int i=0;i<x.length;i++) {
		if (alpha[i]>calpha)
		    calpha = alpha[i];
	    }
	    
	    diff = Math.abs((y_neu-y_alt)/y_neu);
	    
	    y_alt = y_neu;	    	    
	}	
    }

    public void MomentumGradientDescent(double x[]) {
	double y1,y2,alpha,diff;						
	double [] grad = new double[x.length];	
	double xp[] = new double[x.length];
	
	double alpha_min = 0.000000000000001;
	double diff_min = 0.0000000001;
	double approxError = 0.0001;
	
	alpha = 0.1;
	diff  = 1.0;
	
	y1 = funct(x);
	
	while ( alpha > alpha_min && diff > diff_min ) {	    	    
	    //partial differences quotients
	    for (int i=0; i < x.length; i++) {	
	        for (int j=0; j < x.length; j++) {	
		    if (j == i) {
		        xp[j] = x[j]+approxError;			
		    }		    
		    else
		        xp[j] = x[j];			
		}	    
		y2 = funct(xp);		    		    	
		grad[i] = (y2 - y1) / approxError;    
	    }		    
	    
	    //normalize gradient
	    double sum = 0.0;
	    for (int i=0;i<grad.length;i++) {
		sum += grad[i]*grad[i];
	    }
	    sum = Math.sqrt(sum);
	    
	    for (int i=0;i<grad.length;i++) {
		grad[i] /= sum;
	    }	    
	    //use armijo - method to obtain step width
	    //decrease step - width until result is better than the last one
		
	    //try to increase step - width
	    alpha *= 4.0;
	    
	    double y_neu;
	    
	    while (true) {		
	        for (int i=0; i < x.length; i++) {	
		    xp[i] = x[i] - alpha*grad[i];		    
		}
		
		y_neu = funct(xp);
		
		if (y_neu < y1)
		    break;

		alpha /= 2.0;
		
		if (alpha < alpha_min)
		    break;
	    }
	    
	    diff = Math.abs((y1/y_neu) - 1.0);
	    
	    y1 = y_neu;
	    
	    String info = "Gradient:\t";		
	    for (int i=0; i < x.length; i++) {	
		x[i] -= alpha * grad[i];		    
		info += grad[i] + "\t";
	    }
	    getModel().getRuntime().println(info);
	    info = "Stelle:\t\t";
	    for (int i=0; i < x.length; i++) {	
		info += x[i] + "\t";
	    }
	    getModel().getRuntime().println(info);									
	    getModel().getRuntime().println("Funktionswert:\t" + y1 + "\t Alpha: " + alpha);
	}	
    }
    
    public void setKernels(){
        switch (this.kernelMethod.getValue()) {
	    case 0: this.kernel = new org.unijena.predictionnet.kernels.TestKernel(this.DataLength); break;
	    case 2: this.kernel = new org.unijena.predictionnet.kernels.Exponential(this.DataLength); break;
	    case 3: this.kernel = new org.unijena.predictionnet.kernels.MaternClass(this.DataLength); break;
	    case 5: this.kernel = new org.unijena.predictionnet.kernels.RationalQuadratic(this.DataLength); break;
	    case 6: this.kernel = new org.unijena.predictionnet.kernels.NeuralNetwork(this.DataLength); break;
	    
	    case 12: this.kernel = new org.unijena.predictionnet.kernels.SimpleExponential(this.DataLength); break;
	    case 13: this.kernel = new org.unijena.predictionnet.kernels.SimpleMatern(this.DataLength); break;
	    case 15: this.kernel = new org.unijena.predictionnet.kernels.SimpleRationalQuadratic(this.DataLength); break;
	    case 16: this.kernel = new org.unijena.predictionnet.kernels.SimpleNeuralNetwork(this.DataLength); break;
	    case 17: this.kernel = new org.unijena.predictionnet.kernels.SimplePeriodic(this.DataLength); break;
	    default: this.kernel = null; System.out.println("No valid Kernel specified, this will propably cause an error!");break;
	}
	switch (this.MeanMethod.getValue()) {
	    case 0: this.kernel.SetMeanModell(new org.unijena.predictionnet.kernels.FixedMeanModell(this.DataLength)); break;
	    case 1: this.kernel.SetMeanModell(new org.unijena.predictionnet.kernels.LinearMeanModell(this.DataLength)); break;
	    case 2: this.kernel.SetMeanModell(new org.unijena.predictionnet.kernels.QuadraticMeanModell(this.DataLength)); break;
	    default: this.kernel.SetMeanModell(new org.unijena.predictionnet.kernels.FixedMeanModell(this.DataLength)); break;
	}
    }
    
    public void run() {
	trainInit(); //this is necessary
        setKernels();
        
	if (doOptimization.getValue()) {
	    optInit();
	    double x[] = new double[kernel.getParameterCount()];
	    for (int i=0;i<x.length;i++)
		x[i] = 1.0 / kernel.getParameterCount();
	    
	    GradientDescent(x);
	}
	trainInit();
	Train(0); 
	Predict(true);
    }    
}
