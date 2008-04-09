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
    LUDecomposition Solver;
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
    static final double resolution = 0.001;
    static final double limit = 20;
    static double gaussianDistribution[];

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
		    logtheta[i] = (new Double(reader.readLine()).doubleValue());	
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
	Solver = CovarianzMatrix.lu();
        
	//error!!
	if (!Solver.isNonsingular()) {
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
        
        Matrix one = new Matrix(1,TrainLength);
        Matrix oneT = new Matrix(TrainLength,1);
        for (int i=0;i<TrainLength;i++) {	    
            //calculate covariance for xi,x
            double variance = this.kernel.kernel(normalize(data[i]),normalize(x),i,-1);
            kstar.set(0,i,variance);
            kstarT.set(i,0,variance);
            
            one.set(0,i,1.0);
            oneT.set(i,0,1.0);            
        }                        
        Matrix RMinus1r = Solver.solve(kstarT);
        Matrix rRMinus1r = (kstar.times(RMinus1r));
        
        Matrix RMinus1Eins = Solver.solve(oneT);
        Matrix EinsRMinus1Eins = (one.times(RMinus1Eins));
        
        double t = 1.0 - rRMinus1r.get(0,0);
        double tOne = EinsRMinus1Eins.get(0,0);
        
        //double sigma2 = (1.0/(double)this.Observations.getColumnDimension())*(this.Observations.transpose().times(alpha)).get(0,0);
        
        double my_hat = one.times(alpha).get(0,0) / tOne;
        
        Matrix tmp = new Matrix(1,TrainLength);
        for (int i=0;i<TrainLength;i++) {
            tmp.set(0,i,Observations.get(i,0)-my_hat);
        }
        double k = (double)this.Observations.getRowDimension();
        double my_sigma = tmp.times(Solver.solve(tmp.transpose())).get(0,0) / k;
        
        double sigma2 = 1.0 - kstar.times(Solver.solve(kstarT)).get(0,0);
        
                
        return Math.abs(my_sigma)*Math.sqrt(sigma2 + sigma2*sigma2 / tOne);
        
        //heuristic approach
        
        //find nearest neighbour
/*        double minDistance = 0.0;
        for (int i=0;i<TrainLength;i++) {
            double value = this.kernel.kernel(normalize(data[i]),normalize(x),i,-1);
            if (value > minDistance)
                minDistance = value;
        }        
        return 1.0 - minDistance;*/
    }
    
    static public double NormalDensityFunction(double a){
        return (1.0/Math.sqrt(2*Math.PI))*Math.exp(-0.5*a*a);
    }
    
    static public void BuildGaussDistributionTable(){
        double x1 = 0;
        double x2 = resolution;
                
        gaussianDistribution = new double[(int)(limit/resolution)+1];
        int counter = 0;
        double integral = 0.5;
        while (x1 < limit){
            //Simpsonsche Formel
            integral += (x2 - x1)/6.0 * (NormalDensityFunction(x1) + 4*NormalDensityFunction(0.5*(x1+x2))+NormalDensityFunction(x2));
            gaussianDistribution[counter++] = integral;
            x1 = x2;
            x2 = x2 + resolution;
        }
    }

    public double CumulativeNormalDistributionFunction(double x){        
        long index = (long)((Math.abs(x)/limit)*(double)(gaussianDistribution.length));
        double probability = 0.0;
        if (index >= (long)gaussianDistribution.length){
            System.out.println("gp out of range!!");
            probability = 1.0;
            }
        else{            
            probability = gaussianDistribution[(int)index];
            }
        
        if (x < 0){
            probability = 1.0 - probability;
        }       
        return probability;
    }
    
    //probability for value of f(x) < y
    public double GetProbabilityForXLessY(double x[],double target){
        double mean = GetMean(x);
        //variancecontrol, because probability decreases very fast at edges of distrubtion
        double variance = GetVariance(x);
        
        if (variance < 0.00001)
            return -1000;
                
        //transform to 0/1 distriburion        
        target = target - mean;                            
        target /= variance;       
                       
        return CumulativeNormalDistributionFunction(target);
    }
    
    public double GetExpectedImprovement(double x[],double fmin){
        double mean = GetMean(x);
        //variancecontrol, because probability decreases very fast at edges of distrubtion
        double variance = GetVariance(x);
        
        if (variance < 0.00001)
            return -1000;
                
        //transform to 0/1 distriburion        
        double u = (fmin - mean) / variance;  
        
        return variance*(CumulativeNormalDistributionFunction(u)*u + NormalDensityFunction(u));
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
				
	result = this.kernel.MM.ReTransform(x,prediction);
	
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
	    alpha[i] = 0.01;
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
			     
			     if (xp[k] < -10.0)	xp[k] = -10.0;
			     if (xp[k] >  10.0)	xp[k] =  10.0;
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
            case 7: this.kernel = new org.unijena.predictionnet.kernels.NoNoiseExponential(this.DataLength); break;
            case 8: this.kernel = new org.unijena.predictionnet.kernels.NeuralNetworkFull(this.DataLength); break;
	    
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
            double x[] = new double[kernel.getParameterCount()];
	    for (int i=0;i<x.length;i++){
                if (this.param_theta != null)
                    x[i] = Math.log(this.param_theta.getValue()[i]);//1.0 / kernel.getParameterCount();
                else
                    x[i] = 1.0/x.length;
            }
            while (Train(0) < -100000.0){
                for (int i=0;i<x.length;i++){
                    x[i] = (generator.nextDouble()*10.0);
                }
                if (this.param_theta != null)
                    this.param_theta.setValue(x);
            }
	    optInit();
	    	    
	    GradientDescent(x);                        
	}
	trainInit();
        double performance;
	while ((performance = Train(0)) < -100000.0){            
	    double x[] = new double[kernel.getParameterCount()];
	    for (int i=0;i<x.length;i++)
		x[i] = (generator.nextDouble()*10.0);
            if (this.param_theta != null)
                this.param_theta.setValue(x);
	    if (performance > -100000.0){
                optInit();
                GradientDescent(x);                                
            }
        }
	Predict(true);
    }    
}
