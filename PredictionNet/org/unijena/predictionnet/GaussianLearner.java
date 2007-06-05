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
            public JAMSEntity validationData;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.INIT,
            description = "TimeSerie of Temp Data"
            )
            public JAMSInteger kernelMethod;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.INIT,
            description = "TimeSerie of Temp Data"
            )
            public JAMSInteger PerformanceMeasure;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.INIT,
            description = "TimeSerie of Temp Data"
            )
            public JAMSDoubleArray param_theta;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.INIT,
            description = "TimeSerie of Temp Data"
            )
            public JAMSString parameterFile = null;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.INIT,
            description = "TimeSerie of Temp Data"
            )
            public JAMSString resultFile = null;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "TimeSerie of Temp Data"
            )
            public JAMSDouble performance;
           
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.INIT,
            description = "TimeSerie of Temp Data"
            )
            public JAMSBoolean doOptimization;
    
    Matrix CovarianzMatrix;
    CholeskyDecomposition Solver;
    Matrix Observations;
    Matrix alpha;
    Matrix invCovarianzMatrix;
        
    double average = 0.0;
    double sigma = 0.4;          
    
    double logtheta[];    
    double theta[];
        
    Matrix nnKernel;
    
    double marginal_likelihood;    
    
    Kernel kernel;
            	      
    Matrix BuildDerivedMatrix(int d) {
	Matrix diff = new Matrix(TrainLength,TrainLength);
	for (int i=0;i<this.TrainLength;i++) {
	    for (int j=i;j<this.TrainLength;j++) {
		double value = kernel.dkernel(this.normed_data[i],this.normed_data[j],d);
		diff.set(i,j,value);
		diff.set(j,i,value);
	    }
	}
	return diff;
    }
    
    double [] getGradient() {
	switch(PerformanceMeasure.getValue()) {
	    case 1: return getlogMargGradient();
	    case 2: return getLOOGradient();
	    case 3: return getLOOQuadGradient();
	    default: return null;
	}	
    }
    
    //compute gradient to minimize log marginal probability
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
	    gradient[i] = (0.5*tmp.get(0,0) - 0.5*sum);
	}		
	return gradient;
    }
    
    //compute gradient to minimize predictive log probability for LOO
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
	    gradient[j] = -sum;
	}
	return gradient;
    }

     //compute gradient to minimize root square error for LOO
    double [] getLOOQuadGradient() {			
	invCovarianzMatrix = CovarianzMatrix.inverse();
	double gradient[] = new double[theta.length];
		
	for (int j=0;j<this.theta.length;j++) {
	    Matrix diff = BuildDerivedMatrix(j);	    
	    Matrix Z = invCovarianzMatrix.times(diff);
	    Matrix Zalpha = Z.times(alpha);	    
	    Matrix ZinvK = Z.times(invCovarianzMatrix);
	    double sum = 0;
	    
	    for (int i=0;i<this.TrainLength;i++) {
		double yi_sub_mu_i = alpha.get(i,0)/invCovarianzMatrix.get(i,i);
		double factor1 = alpha.get(i,0)*ZinvK.get(i,i)/(Math.pow(invCovarianzMatrix.get(i,i),2.0));
		double factor2 = Zalpha.get(i,0)/invCovarianzMatrix.get(i,i);
				
		sum += 2.0*yi_sub_mu_i*(factor1 - factor2);				
	    }
	    gradient[j] = -sum;
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
    
    void optimizeLength(String file) {
	//gradient based!!
	double change = 1.0;
	double epsilon = 0.01;
	double step = 0.1;
	double avgchange = 1.0;		
	double beta = 0.1;
			
	double oldgradient[] = new double[param_theta.getValue().length];
	int iteration = 1;
	
	for (int i=0;i<param_theta.getValue().length;i++) {
	    oldgradient[i] = 0;
	}
	
	BufferedWriter writer = null;
	try {
	    writer = new BufferedWriter(new FileWriter(file));	    
	}
	catch (Exception e) {
	    System.out.println("Could not open parameter result file, becauce:" + e.toString());
	    System.out.println("Optimizationresult won't be saved");
	}	
	
	double value = this.Train();	
	while (avgchange > epsilon) {
	    System.out.println("step: " + iteration);
	    iteration++;
	    	    
	    double gradient[] = getGradient();	    	    	    	    
	    gradient = norm(gradient);
	    	    	    
	    System.out.println("actual value:" + value);	    
	    
	    String output = "";
	    for (int i=0;i<gradient.length;i++) {		
		output += "theta_" + i + " " + theta[i] + " ";
	    }
	    output += "\n";	    
	    for (int i=0;i<gradient.length;i++) {		
		output += "gradient_" + i + " " + gradient[i] + " ";
	    }
	    output += "\n";
	    System.out.println(output);
	    try {
		writer.write(output);
		writer.flush();
	    }catch(Exception e) {
		System.out.println("Could not open parameter result file, becauce:" + e.toString());
	    }
	    
	    double newvalue = value;	    	    
	    	    
	    for (int i=0;i<theta.length;i++) {
	        //logtheta[i] = logtheta[i] + step*gradient[i] + beta*oldgradient[i];
		param_theta.getValue()[i] = /*Math.exp*/(/*Math.log*/(param_theta.getValue()[i]) + step*gradient[i] + beta*oldgradient[i]);
	        oldgradient[i] = step*gradient[i] + beta*oldgradient[i];
	    }
	    newvalue = Train();				
							 
	    change = Math.abs(newvalue - value);
	    avgchange = 0.9*avgchange + 0.1*change;
	    value = newvalue;	
	}
	try {
	    writer.close();
	}
	catch(Exception e) {
	    System.out.println("Could not close parameter result file, becauce:" + e.toString());
	}
    }
    
    void Testoptimizer() {
	//gradient based!!
	double change = 1.0;
	double epsilon = 0.01;
	double step = 0.1;
	double avgchange = 1.0;		
	double beta = 0.0;
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
	    double oldtheta[] = new double[theta.length];
	    for (int i=0;i<theta.length;i++) {
		    oldtheta[i] = logtheta[i];
	    }
	    
	    step *= 4.0;
	    
	    while (newvalue <= value) {
		step /= 2.0;
		
		for (int i=0;i<theta.length;i++) {
		    logtheta[i] = oldtheta[i] - step*gradient[i];
		}
		newvalue = Train();
		
		System.out.println("Try with step:" + -step + "get:" + newvalue);
		
		for (int i=0;i<theta.length;i++) {
		    logtheta[i] = oldtheta[i] + step*gradient[i];
		}		
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
		
	theta = new double[this.kernel.getParameterCount()];
	logtheta = new double[this.kernel.getParameterCount()];
	
	//read params from file
	if (this.parameterFile != null) {
	    BufferedReader reader;
	    try {
		reader = new BufferedReader(new FileReader(this.parameterFile.getValue()));
		for (int i=0;i<theta.length;i++) {
		    logtheta[i] = Math.log(new Double(reader.readLine()).doubleValue());	
		}
		reader.close();
	    }
	    catch (Exception e) {
	        System.out.println("Could not open parameter result file, becauce:" + e.toString());
	        System.out.println("Optimizationresult won't be saved");
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
		double varianz = this.kernel.kernel(normalize(data[i]),normalize(data[j]));
		CovarianzMatrix.set(i,j,varianz);
		CovarianzMatrix.set(j,i,varianz);
	    }
	    double varianz = this.kernel.kernel(normalize(data[i]),normalize(data[i]));
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
		
	switch (PerformanceMeasure.getValue()) {
	    case 1: return getMarginalLikelihood();
	    case 2: return this.getLOOlogPredictiveProbability();
	    case 3: return this.getLOOSquareError();
	    default: return 0.0;
	}	
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
    
    public double getLOOSquareError() {	
	invCovarianzMatrix = this.CovarianzMatrix.inverse();	
	double error = 0;
	
	for (int i=0;i<TrainLength;i++) {
	    //leave i out
	    double mu_i = result[i] - alpha.get(i,0)/ invCovarianzMatrix.get(i,i);
	    	    
	    error += (mu_i - this.Observations.get(i,0))*(mu_i - this.Observations.get(i,0));
	}
	return -error;
    }        
    
/*    public double getVariance(double x[]) {
	double k = kernel(normalize(x),normalize(x));
	
	Matrix kstar = new Matrix(1,TrainLength);
	
	for (int i=0;i<TrainLength;i++) {	    
	    //calculate covariance for xi,x
	    double covariance = kernel(normalize(data[i]),normalize(x));
	    kstar.set(0,i,covariance);
	}
	
	Matrix tmp = Solver.solve(kstar.transpose());
	return k - kstar.times(tmp).get(0,0);
    }*/
    
    public double[] Predict(double x[][]) {	
	int m = x.length;
	Matrix kstar = new Matrix(m,TrainLength);
	
	for (int j=0;j<m;j++) {
	    for (int i=0;i<TrainLength;i++) {	    
		//calculate covariance for xi,x
		double varianz = this.kernel.kernel(normalize(data[i]),normalize(x[j]));
		kstar.set(j,i,varianz);
	    }
	}
	Matrix prediction = (kstar.times(alpha));
	double result[] = new double[m];
	for (int j=0;j<m;j++) {
	    result[j] = prediction.get(j,0) + average;
	}
	return result;
    }  
   
    public void run_gp() throws JAMSEntity.NoSuchAttributeException {					
	super.trainData = this.trainData;
	super.validationData = this.validationData;
	super.run();
	
	this.performance.setValue(this.Train());
	
	double result[] = this.Predict((double[][])validationData.getObject("data"));
	BufferedWriter writer = null;
	try {
	    writer = new BufferedWriter(new FileWriter(this.resultFile.getValue()));	    
	}
	catch (Exception e) {
	    System.out.println("Could not open result file, becauce:" + e.toString());
	    System.out.println("results won't be saved");
	}
	    
	for (int i=0;i<((double[][])this.validationData.getObject("data")).length;i++) {		
	    double correctValue = ((double[])this.validationData.getObject("predict"))[i];
	    try {
		writer.write(new String(correctValue + "\t" + result[i] + "\n"));		    
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
	    System.out.println(e.toString());
	}
    }
        
    double[] lowBound;
    double[] upBound;
    
    int currentCount;
    Random generator = new Random();    
    GenericDataWriter writer;
    
    static final int MAXIMIZATION = 1;
    static final int MINIMIZATION = 2;
    static final int ABSMAXIMIZATION = 3;  
    static final int ABSMINIMIZATION = 4;
     
    int MaximizeEff = 1;
    
    int N; //parameter dimension
    int p; //number of complexes
    int s; //population size
    int m; //complex size; floor(s/q)
    int icall=0;
    
    public void init() {
	super.trainData = this.trainData;
	super.validationData = this.validationData;
		
	try {
	    super.run();
	}
	catch (Exception e) {
	    System.out.println(e.toString());
	}
	N = this.DataLength + 1;
	
	lowBound = new double[N];
	upBound = new double[N];
	
	for (int i=0;i<N;i++) {
	    lowBound[i] = -10000.0;
	    upBound[i] = 10000.0;
	}
    }
    public double Custom_rand() {	
	return this.generator.nextDouble();
    }
    
    private double[] RandomSampler(){
        double[] sample = new double[N];
        
        for(int i = 0; i < N; i++){
            double d = Custom_rand();            
            sample[i] = (lowBound[i] + d * (upBound[i]-lowBound[i]));
        }
        return sample;
    }
         
    private void singleRun() {    		
	icall++;
        try {
	    run_gp();	    
	}
	catch (Exception e) {
	    System.out.println("och nööö" + e.toString());
	}
    }
           
    private boolean IsSampleValid(double[] sample) {
        JAMSDouble conv_sample[] = new JAMSDouble[sample.length];
        for (int i = 0;i<sample.length;i++) {
            conv_sample[i] = new JAMSDouble(sample[i]);
        }
        return IsSampleValid(conv_sample);
    }
    private boolean IsSampleValid(JAMSDouble [] sample) {        
        boolean criticalPara = false;
        double criticalParaValue = 0;
        
        for(int i = 0; i < N; i++){
            if (sample[i].getValue() < lowBound[i] || sample[i].getValue() > upBound[i] )
                return false;
        }
        return true;
    }
                    
    public double funct(double x[]) {
        double value = 0;
        
	for (int j=0;j<N;j++) {
	    param_theta.getValue()[j] = Math.exp(x[j]);
	}
        
        singleRun();

        if (MaximizeEff == MINIMIZATION)
            return this.performance.getValue();
        else if (MaximizeEff == ABSMINIMIZATION)
            return Math.abs(this.performance.getValue());
        else if (MaximizeEff == ABSMAXIMIZATION)
            return -Math.abs(this.performance.getValue());
        else if (MaximizeEff == MAXIMIZATION)
            return -this.performance.getValue();
        else
            return 0;
    }

    
    @SuppressWarnings("unchecked") public void sort(double x[][],double xf[]) {	
	if (x.length == 0)
	    return;
	int n = x[0].length;
	double t[][] = new double[x.length][n+1];
	for (int i=0;i<x.length;i++) {
	    for (int j=0;j<n;j++) {
		t[i][j] = x[i][j];
	    }
	    t[i][n] = xf[i];
	}
	
	SCE_Comparator comparator = new SCE_Comparator(n,false);
        java.util.Arrays.sort(t,comparator);
	
	for (int i=0;i<x.length;i++) {
	    for (int j=0;j<n;j++) {
		x[i][j] = t[i][j];
	    }
	    xf[i] = t[i][n];
	}
    }
    
    public void sort(int x[]) {
	Arrays.sort(x);
    }
    
    
    public double NormalizedgeometricRange(double x[][],double bound[]) {
	if (x.length == 0)
	    return 0;
	int n = x[0].length;
	
	double min[] = new double[n];
	double max[] = new double[n];
	
	double mean = 0;
	
	for (int i=0;i<n;i++) {
	    min[i] = Double.POSITIVE_INFINITY;
	    max[i] = Double.NEGATIVE_INFINITY;
	    
	    for (int j=0;j<x.length;j++) {
		if (x[j][i] < min[i])
		    min[i] = x[j][i];
		if (x[j][i] > max[i])
		    max[i] = x[j][i];				
	    }
	    
	    mean += Math.log(max[i] - min[i])/bound[i];
	}
	mean/=n;
	return Math.exp(mean);	
    }
    
    public double[] std(double x[][]) {
	if (x.length == 0)
	    return null;
	if (x.length == 1)
	    return null;
	
	int n = x[0].length;
	
	double mean[] = new double[n];
	double var[] = new double[n];
	
	for (int i=0;i<n;i++) {
	    mean[i] = 0;
	    for (int j=0;j<x.length;j++) {
		mean[i] += x[j][i];
	    }
	    mean[i] /= n;
	}
	
	for (int i=0;i<n;i++) {
	    var[i] = 0;
	    for (int j=0;j<x.length;j++) {
		var[i] += (mean[i] - x[j][i])*(mean[i] - x[j][i]);
	    }
	    var[i] = Math.sqrt(var[i])/(n-1);
	}
	
	return var;
    }
    
    public int find(int lcs[],int startindex,int endindex,int value) {
	for (int i=startindex;i<endindex;i++) {
	    if (lcs[i] == value) 
		return i;
	}
	return -1;
    }
    
    public double[] cceua( double s[][],double sf[],double bl[],double bu[]) {
	int nps = s.length;
	int nopt = s[0].length;
	
	int n = nps;
	int m = nopt;
	
	double alpha = 1.0;
	double beta = 0.5;
	
	// Assign the best and worst points:
	double sb[] = new double[nopt];
	double sw[] = new double[nopt];
	double fb = sf[0];
	double fw = sf[n-1];
	
	for (int i=0;i<nopt;i++) {
	    sb[i] = s[0][i];
	    sw[i] = s[n-1][i];
	}
	
	// Compute the centroid of the simplex excluding the worst point:
	double ce[] = new double[nopt];
	for (int i=0;i<nopt;i++) {
	    ce[i] = 0;
	    for (int j=0;j<n-1;j++) {
		ce[i] += s[j][i];
	    }
	    ce[i] /= (n-1);
	}

	// Attempt a reflection point
	double snew[] = new double[nopt];
	for (int i=0;i<nopt;i++) {
	    snew[i] = ce[i] + alpha*(ce[i]-sw[i]);
	}
	
	// Check if is outside the bounds:
	int ibound=0;
	for (int i=0;i<nopt;i++) {
	    if ( (snew[i]-bl[i]) < 0 ) 
		ibound = 1;
	    if ( (bu[i]-snew[i]) < 0 ) 
		ibound = 2;
	}
	
	if (ibound >=1) {
	    snew = this.RandomSampler();
	}
	    
	double fnew = funct(snew);
	
	// Reflection failed; now attempt a contraction point:
	if (fnew > fw) {
	    for (int i=0;i<nopt;i++) {
		snew[i] = sw[i] + beta*(ce[i]-sw[i]);		
	    }
	    fnew = funct(snew);
	}
	// Both reflection and contraction have failed, attempt a random point;
	if (fnew > fw) {
	    snew = this.RandomSampler();
	    fnew = funct(snew);
	}
    
	double result[] = new double[nopt+1];
	for (int i=0;i<nopt;i++) {
	    result[i] = snew[i];
	}
	result[nopt] = fnew;
	return result;
    }
    
    public double[] sceua(double[] x0,double[] bl,double []bu,int maxn,int kstop,double pcento,double peps,int ngs,int iseed,int iniflg) {
    
	int nopt = x0.length;
	int npg = 2*nopt+1;
	int nps = nopt+1;
	int nspl = npg;
	int mings = ngs;
	int npt = npg*ngs;
	
	double bound[] = new double[nopt];
	for (int i=0;i<nopt;i++) {
	    bound[i] = bu[i] - bl[i];
	}
	
	// Create an initial population to fill array x(npt,nopt):
	//this.generator.setSeed(iseed);
	this.generator.setSeed(System.currentTimeMillis());
	
	double x[][] = new double[npt][nopt];
	
	for (int i=0;i<npt;i++) {
	    x[i] = this.RandomSampler();
	}
	
	if (iniflg==1) {
	    x[0] = x0;
	}
	
	int nloop=0;	
	
	double xf[] = new double[npt];
	for (int i=0;i<npt;i++) {
	    xf[i] = funct(x[i]);
	}
	double f0 = xf[0];
	
	// Sort the population in order of increasing function values;
	sort(x,xf);
	
	// Record the best and worst points;
	double bestx[] = new double[nopt];
	double worstx[] = new double[nopt];
	double bestf,worstf;
	for (int i=0;i<nopt;i++) {
	    bestx[i] = x[0][i];
	    worstx[i] = x[npt-1][i];
	}
	bestf = xf[0];
	worstf = xf[npt-1];
	
	// Compute the standard deviation for each parameter
	double xnstd[] = std(x);	
	
	// Computes the normalized geometric range of the parameters
	double gnrng = NormalizedgeometricRange(x,bound); //exp(mean(log((max(x)-min(x))./bound)));
	
	System.out.println("The Inital Loop: 0");
	System.out.println("BestF: " + bestf);
	System.out.print("BestX");
	for (int i=0;i<nopt;i++) {
	    System.out.print("\t\t" + bestx[i]);
	}
	System.out.println("");
	System.out.println("WorstF: " + worstf);
	System.out.print("WorstX");
	for (int i=0;i<nopt;i++) {
	    System.out.print("\t\t" + worstx[i]);
	}
	System.out.println("");
	
	//Check for convergency;
	if (icall >= maxn) {
	    System.out.println("*** OPTIMIZATION SEARCH TERMINATED BECAUSE THE LIMIT");
	    System.out.println("ON THE MAXIMUM NUMBER OF TRIALS" + maxn);
	    System.out.println("HAS BEEN EXCEEDED.  SEARCH WAS STOPPED AT TRIAL NUMBER:" + icall);
	    System.out.println("OF THE INITIAL LOOP!");
	}
	
	if (gnrng < peps) {
	    System.out.println("THE POPULATION HAS CONVERGED TO A PRESPECIFIED SMALL PARAMETER SPACE");
	}
	
	// Begin evolution loops:
	nloop = 0;
	double criter[] =new double[kstop];
	double criter_change=100000;
	
	while (icall<maxn && gnrng>peps && criter_change>pcento) {
	    nloop++;

	    // Loop on complexes (sub-populations);
	    for (int igs=0;igs<ngs;igs++) {
		
		// Partition the population into complexes (sub-populations);
		int k1[] = new int[npg];
		int k2[] = new int[npg];
		
		for (int i=0;i<npg;i++) {
		    k1[i] = i;
		    k2[i] = k1[i]*ngs+igs;
		}
		double cx[][] = new double[npg][nopt];
		double cf[] = new double[npg];
		for (int i=0;i<npg;i++) {
		    for (int j=0;j<nopt;j++) {
			cx[k1[i]][j] = x[k2[i]][j];			
		    }
		    cf[k1[i]] = xf[k2[i]];
		}
		
		//Evolve sub-population igs for nspl steps:
		for (int loop=0;loop<nspl;loop++) {
		    // Select simplex by sampling the complex according to a linear
		    // probability distribution
		    int lcs[] = new int[nps];
		    lcs[0] = 0;
		    for (int k3=1;k3<nps;k3++) {
			int lpos = 0;
			for (int iter=0;iter<1000;iter++) {
			    lpos = (int)Math.floor(npg+0.5-Math.sqrt((npg+0.5)*(npg+0.5) - npg*(npg+1)*Custom_rand()));
			    
			    int idx = find(lcs,0,k3,lpos);
			    if (idx == -1) {
				break;
			    }			
			}
			lcs[k3] = lpos;
		    }
		    sort(lcs);
		    
		    // Construct the simplex:
		    double s[][] = new double[nps][nopt];
		    double sf[]  = new double[nps];
		    for (int i=0;i<nps;i++) {
			for (int j=0;j<nopt;j++) {
			    s[i][j] = cx[lcs[i]][j];			    
			}
			sf[i] = cf[lcs[i]];
		    }
		    
		    double snew[] = new double[nopt];
		    double fnew;
		    
		    double xnew[] = cceua(s,sf,bl,bu);
		    
		    //icall aktualisieren!!!
		    
		    for (int i=0;i<nopt;i++) {
			snew[i] = xnew[i];			
		    }		   
		    fnew = xnew[nopt];
		    		    
		    // Replace the worst point in Simplex with the new point:
		    s[nps-1] = snew;
		    sf[nps-1] = fnew;
            
		    //Replace the simplex into the complex;
		    for (int i=0;i<nps;i++) {
			for (int j=0;j<nopt;j++) {
			    cx[lcs[i]][j] = s[i][j];			    
			}
			cf[lcs[i]] = sf[i];
		    }
		    
		    // Sort the complex;
		    sort(cx,cf);
		    // End of Inner Loop for Competitive Evolution of Simplexes
		}
		// Replace the complex back into the population;
		for (int i=0;i<npg;i++) {
		    for (int j=0;j<nopt;j++) {
			x[k2[i]][j] = cx[k1[i]][j];					
		    }
		    xf[k2[i]] = cf[k1[i]];
		}		
	    // End of Loop on Complex Evolution;
	    }
	    // Shuffled the complexes;
	    sort(x,xf);
	
	    // Record the best and worst points;
	    for (int i=0;i<nopt;i++) {
	        bestx[i] = x[0][i];
	        worstx[i] = x[nopt-1][i];	   
	    }
	    bestf = xf[0];
	    worstf = xf[npt-1];
    
	    //Compute the standard deviation for each parameter
	    xnstd = std(x);
	
	    gnrng = NormalizedgeometricRange(x,bound);
	
	    System.out.println("Evolution Loop:" + nloop + " - Trial - " + icall);
	    System.out.println("BESTF:" + bestf);
	    System.out.print("BESTX:");
	    for (int i = 0;i<nopt;i++) {
	        System.out.print("\t" + bestx[i]);
	    }
	    System.out.println("\nWORSTF:" + worstf);
	    System.out.print("WORSTX:");
	    for (int i = 0;i<nopt;i++) {
	        System.out.print("\t" + worstx[i]);
	    }
	    System.out.println("");
	    
	    // Check for convergency;
	    if (icall >= maxn) {
		System.out.println("*** OPTIMIZATION SEARCH TERMINATED BECAUSE THE LIMIT");
		System.out.println("ON THE MAXIMUM NUMBER OF TRIALS " +  maxn + " HAS BEEN EXCEEDED!");
	    }
	    if (gnrng < peps) {
		System.out.println("THE POPULATION HAS CONVERGED TO A PRESPECIFIED SMALL PARAMETER SPACE");
	    }
	    	   
	    for (int i=0;i<kstop-1;i++) {
		criter[i] = criter[i+1];
	    }
	    criter[kstop-1] = bestf;
	    if (nloop >= kstop) {
		criter_change=Math.abs(criter[0]-criter[kstop-1])*100.0;
		double criter_mean = 0;
		for (int i=0;i<kstop;i++) {
		    criter_mean += Math.abs(criter[i]);
		}
		criter_mean /= kstop;
		criter_change /= criter_mean;
		
		if (criter_change < pcento) {
		    System.out.println("THE BEST POINT HAS IMPROVED IN LAST " + kstop + " LOOPS BY");
		    System.out.println("LESS THAN THE THRESHOLD " + pcento + "%");
		    System.out.println("CONVERGENCY HAS ACHIEVED BASED ON OBJECTIVE FUNCTION CRITERIA!!!");
		}
	    }
	}
	System.out.println("SEARCH WAS STOPPED AT TRIAL NUMBER: " + icall);
	System.out.println("NORMALIZED GEOMETRIC RANGE = " + gnrng);
	System.out.println("THE BEST POINT HAS IMPROVED IN LAST " + kstop + " LOOPS BY " + criter_change + "%");
	
	return bestx;
    }
    
    public void GradientDescent(double x[]) {
	double y1,y2,alpha,diff;						
	double [] grad = new double[this.param_theta.getValue().length];	
	double xp[] = new double[x.length];
	
	double alpha_min = 0.0001;
	double diff_min = 0.001;
	double approxError = 0.0001;
	
	alpha = 0.1;
	diff  = 1.0;
	
	while ( alpha > alpha_min && diff > diff_min ) {	    
	    y1 = funct(x);	    
	    //partial differences quotients
	    for (int i=0; i < x.length; i++) {	
	        for (int j=0; j < x.length; j++) {	
		    if (j == i) {
		        xp[j] = x[j]+approxError;			
		    }		    
		    else
		        xp[j] = x[j];			
		}	    
		if ( !IsSampleValid(xp) )
		    grad[i] = 0;
		else {
		    y2 = funct(xp);		    		    	
		    grad[i] = (y2 - y1) / approxError;    
		}		
	    }		    

	    //use armijo - method to obtain step width
	    //decrease step - width until result is better than the last one
		
	    //try to increase step - width
	    alpha *= 4.0;
	    
	    while (true) {
		double test;
	        for (int i=0; i < x.length; i++) {	
		    xp[i] = x[i] - alpha*grad[i];		    
		}
		
		if (this.IsSampleValid(xp)) {
		    test = funct(xp);
		
		    if (test < y1)
			break;
		}
		alpha /= 2.0;
		
		if (alpha < alpha_min)
		    break;
	    }
		
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
    
    public void run_optimizer() {                       
	int maxn=1000000;
	int kstop=10;
	double  pcento=0.01;
	double peps=0.0001;
	int iseed=10;
	int iniflg=0;
	int NumberOfComplexes = 2;
	
	double bestpoint[],bestx[],bestf;
	
	double x0[] = RandomSampler();
			
	bestpoint = sceua(x0,this.lowBound,this.upBound,maxn,kstop,pcento,peps,NumberOfComplexes,iseed,iniflg);	
    }
    
    public void run() {
	switch (this.kernelMethod.getValue()) {
	    case 2: this.kernel = new org.unijena.predictionnet.kernels.Exponential(this.DataLength); break;
	    case 3: this.kernel = new org.unijena.predictionnet.kernels.MaternClass(this.DataLength); break;
	    case 5: this.kernel = new org.unijena.predictionnet.kernels.RationalQuadratic(this.DataLength); break;
	    case 6: this.kernel = new org.unijena.predictionnet.kernels.NeuralNetwork(this.DataLength); break;
	    default: this.kernel = null; System.out.println("No valid Kernel specified, this will propably cause an error!");break;
	}
	
	if (doOptimization.getValue()) {
	    this.init();
	    //run_optimizer();
	    //this.optimizeLength(this.resultFile.getValue());
	    double x[] = new double[this.DataLength+1];
	    for (int i=0;i<x.length;i++)
		x[i] = 0.0;
	    
	    GradientDescent(x);
	}
	else
	    try {
		run_gp();
	    }
	    catch(Exception e) {
		System.out.println("bäää" + e.toString());
	    }
    }    
}
