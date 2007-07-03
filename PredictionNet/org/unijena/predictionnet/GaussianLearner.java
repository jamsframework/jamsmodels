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
import jams.components.optimizer.SCE.SCE_Comparator;
import java.util.Arrays;
import java.util.Random;
import java.util.StringTokenizer;
import org.unijena.jams.io.GenericDataWriter;
import org.unijena.predictionnet.kernels.*;

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
        
    double average = 0.0;        
    double logtheta[];    
    double theta[];
                        
    static final int MAXIMIZATION = 1;
    static final int MINIMIZATION = 2;
    static final int ABSMAXIMIZATION = 3;  
    static final int ABSMINIMIZATION = 4;
     
    int MaximizeEff = 1;
    
    Kernel kernel;

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
	    sigma_i = sigma_i*sigma_i;
	    
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
	
	average = 0.0;
		
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
	    average += result[i];
	    for (int j=0;j<i;j++) {
		//calculate covariance for xi,xj
		double varianz = this.kernel.kernel(normalize(data[i]),normalize(data[j]),i,j);
		CovarianzMatrix.set(i,j,varianz);
		CovarianzMatrix.set(j,i,varianz);
	    }
	    double varianz = this.kernel.kernel(normalize(data[i]),normalize(data[i]),i,i);
	    CovarianzMatrix.set(i,i,varianz);	    
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
		
	switch (PerformanceMeasure) {
	    case 1: return getMarginalLikelihood();
	    case 2: return this.getLOOlogPredictiveProbability();
	    case 3: return this.getLOOSquareError();
	    case 4: return this.getSplitValidationError();
	    default: return 0.0;
	}	
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
	double result[] = new double[m];
	for (int j=0;j<m;j++) {
	    result[j] = prediction.get(j,0) + average;
	}
	
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
	double y1,y2,alpha,diff;						
	double [] grad = new double[x.length];	
	double xp[] = new double[x.length];
	
	double alpha_min = 0.0001;
	double diff_min = 0.001;
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
            
    public void run() {
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
			
	if (doOptimization.getValue()) {
	    optInit();
	    double x[] = new double[kernel.getParameterCount()];
	    for (int i=0;i<x.length;i++)
		x[i] = 0.0;
	    
	    GradientDescent(x);
	}
	trainInit();
	Train(0); 
	Predict(true);
    }    
}
