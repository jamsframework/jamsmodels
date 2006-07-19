/*
 * ABCGradientDescent.java
 * Created on 30. Juni 2006, 15:12
 *
 * This file is part of JAMS
 * Copyright (C) 2005 S. Kralisch and P. Krause
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA
 *
 */

package org.unijena.abc;

import java.util.Random;
import java.util.Vector;

import java.util.StringTokenizer;
import java.util.Collections;
import org.unijena.jams.data.*;
import org.unijena.jams.io.GenericDataWriter;
import org.unijena.jams.model.*;
import org.unijena.abc.ABCEvoluAlgComparator;
import org.unijena.abc.ABC_SCEM_UAComp;
import Jama.*;
/**
 *
 * @author Christian Fischer
 */
@JAMSComponentDescription(
        title="Title",
        author="Author",
        description="Description"
        )
        public class ABC_SCEM_UA extends JAMSContext {
    
    /*
     *  Component variables
     */
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.INIT,
            description = "List of parameter identifiers to be sampled"
            )
            public JAMSString parameterIDs;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.INIT,
            description = "List of parameter value bounaries corresponding to parameter identifiers"
            )
            public JAMSString boundaries;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.INIT,
            description = "Number of samples to be taken"
            )
            public JAMSInteger sampleCount;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.INIT,
            description = "efficiency methods"
            )
            public JAMSString effMethodNames;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "efficiency values"
            )
            public JAMSDouble[] effValues;        
    
    JAMSDouble[] parameters;
    String[] parameterNames;
    double[] lowBound;
    double[] upBound;

    int currentCount;
    Random generator;
    
    GenericDataWriter writer;
    
    int n; //parameter dimension
    int q; //number of complexes
    int s; //population size
    int m; //complex size; floor(s/q)
    int L; //number of offspring generated per iteration
    double gamma; //Kurtosis parameter Bayesian Inference Scheme (Thiemann et al., 2001)
    double ndraw; //Number of accepted draws to infer posterior distribution on (?)
           
    public void init() {
//generalise this!!
	
	
//add more checks!!!
        //retreiving parameter names
        int i;
        StringTokenizer tok = new StringTokenizer(parameterIDs.getValue(), ";");
        String key;
        parameters = new JAMSDouble[tok.countTokens()];
        parameterNames = new String[tok.countTokens()];
        
        i = 0;
        while (tok.hasMoreTokens()) {
            key = tok.nextToken();
            parameterNames[i] = key;
            parameters[i] = (JAMSDouble) getModel().getRuntime().getDataHandles().get(key);
            i++;
        }
        
        //retreiving boundaries
        tok = new StringTokenizer(boundaries.getValue(), ";");
        int n = tok.countTokens();
        lowBound = new double[n];
        upBound = new double[n];
        
        //check if number of parameter ids and boundaries match
        if (n != i) {
            getModel().getRuntime().sendHalt("Component " + this.getInstanceName() + ": Different number of parameterIDs and boundaries!");
        }
        
        i = 0;
        while (tok.hasMoreTokens()) {
            key = tok.nextToken();
            key = key.substring(1, key.length()-1);
            
            StringTokenizer boundTok = new StringTokenizer(key, ">");
            lowBound[i] = Double.parseDouble(boundTok.nextToken());
            upBound[i] = Double.parseDouble(boundTok.nextToken());
            
            //check if upBound is higher than lowBound
            if (upBound[i] <= lowBound[i]) {
                getModel().getRuntime().sendHalt("Component " + this.getInstanceName() + ": upBound must be higher than lowBound!");
            }
            
            i++;
        }
        
        //retreiving effMethodNames
        i = 0;
        tok = new StringTokenizer(effMethodNames.getValue(), ";");
        String[] effNames = new String[tok.countTokens()];
        i = 0;
        while (tok.hasMoreTokens()) {
            key = tok.nextToken();
            effNames[i] = key;
            i++;
        }   	
    }
            
    private double[] abcRandomSampler(){
        int paras = this.parameterNames.length;
        boolean criticalPara = false;
        double criticalParaValue = 0; 
        double[] sample = new double[paras];
        	
        for(int i = 0; i < paras; i++){
            if(parameterNames[i].equals("abcModel.a") || parameterNames[i].equals("abcModel.b")){
                //either a or b has already been sampled!
                if(criticalPara){
                    double d = generator.nextDouble();
                    double upperBound = 1.0 - criticalParaValue;
                    sample[i] = (lowBound[i] + d * (upperBound-lowBound[i]));
                }
                else{
                    //first criticalPara
                    double d = generator.nextDouble();
                    sample[i] = (lowBound[i] + d * (upBound[i]-lowBound[i]));
                    criticalPara = true;
                    criticalParaValue = sample[i];
                }
            }else{
                double d = generator.nextDouble();
                // all other parameters
                sample[i] = (lowBound[i] + d * (upBound[i]-lowBound[i]));
            }
            getModel().getRuntime().sendInfoMsg("Para: " + parameterNames[i] + " = " + sample[i]);
        }     
        return sample;
    }
    
    private void resetValues() {
        //set parameter values to initial values corresponding to their boundaries
        generator = new Random(System.currentTimeMillis());
        for (int i = 0; i < parameters.length; i++) {
            double d = generator.nextDouble();
            parameters[i].setValue(lowBound[i] + d * (upBound[i]-lowBound[i]));
        }
        currentCount = 0;
    }
    
    private void singleRun() {
        
        runEnumerator.reset();
        while(runEnumerator.hasNext() && doRun) {
            JAMSComponent comp = runEnumerator.next();
            try {
                comp.init();
            } catch (Exception e) {
		System.out.println(e.getMessage());
            }
        }
                
        runEnumerator.reset();
        while(runEnumerator.hasNext() && doRun) {
            JAMSComponent comp = runEnumerator.next();
            try {
                comp.run();
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
        }
        
        runEnumerator.reset();
        while(runEnumerator.hasNext() && doRun) {
            JAMSComponent comp = runEnumerator.next();
            try {
                comp.cleanup();
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
        }
    }
    
    private boolean IsSampleValid(JAMSDouble [] sample) {
	int paras = this.parameterNames.length;
        boolean criticalPara = false;
        double criticalParaValue = 0; 
        	
        for(int i = 0; i < paras; i++){
            if(parameterNames[i].equals("abcModel.a") || parameterNames[i].equals("abcModel.b")){
                //either a or b has already been sampled!
                if(criticalPara){                    
                    double upperBound = 1.0 - criticalParaValue;
		    if (sample[i].getValue() < lowBound[i] || sample[i].getValue() > upperBound )
			return false;
                }
                else{
                    //first criticalPara
		    if (sample[i].getValue() < lowBound[i] || sample[i].getValue() > upBound[i] )
			return false;
                    criticalPara = true;
                    criticalParaValue = sample[i].getValue();
                }
            }else{
                // all other parameters
		if (sample[i].getValue() < lowBound[i] || sample[i].getValue() > upBound[i] )
		    return false;
            }
        }     
        return true;
    }
    
     public void sort(double data[][],int col,boolean decreasing_order) {
	ABC_SCEM_UAComp comparator = new ABC_SCEM_UAComp(col,decreasing_order);
	java.util.Arrays.sort(data,comparator);
    }
    
    public double[] getMean(double data[][]) {	    //no error here :)
	double[] mean = new double[n];
	
	java.util.Arrays.fill(mean,0);
	
	for (int j=0;j<n;j++) {	    
	    for (int i=0;i<data.length;i++) {
		mean[j] += data[i][j];
	    }
	    mean[j] /= data.length;
	}
	return mean;
    }
           
    public double[][] getCoVarMatrix(double data[][]) { //no error here :)
	double[][] coV = new double[n][n];	
	double[] mean = getMean(data);
	double vecLength = data.length;

	for (int i=0;i<n;i++) {
	    java.util.Arrays.fill(coV[i],0);
	}
		
	for (int i=0;i<n;i++) {
	    for (int j=0;j<n;j++) {
		for (int k=0;k<vecLength;k++) {
		    coV[i][j] += data[k][i]*data[k][j];
		}
		coV[i][j] -= vecLength*mean[i]*mean[j];
		coV[i][j] /= (vecLength - 1);				
	    }
	}
		
	return coV;
    }
    
    public double[][] latin(int s,int n) {
	double x[][] = new double[s][n];
	
	for (int i=0;i<s;i++) {
	    x[i] = abcRandomSampler();
	}
	return x;
    }

    public double ComputeDensity(double x[]) {
	int e = 0;	
	double pset;
	
	for (int j=0;j<x.length;j++) {
	    parameters[j].setValue(x[j]);
	}
	
	singleRun();
	
	pset = this.effValues[e].getValue();

    return pset;
    }

    public double[][] ComputeDensity(double x[][]) {
	int e = 0;	
	double pset[][] = new double[x.length][2];
			
	for (int i=0;i<x.length;i++) {
	    pset[i][0] = ComputeDensity(x[i]);
	    pset[i][1] = i;
	}
	return pset;
    }
    
    public double[][][] PartComplexes(double D[][],double x[][]) {
	double C[][][] = new double[m][n+1][q];
	
	for (int kk=0;kk<q;kk++) {
	    for (int j=0;j<m;j++) {
		int idx = q*j + kk;
		for (int i=0;i<n;i++) {
		    C[j][i][kk] = x[(int)D[idx][1]][i];
		}		
		C[j][n][kk] = D[idx][0];
	    }	    
	}
	return C;
    }
   
    public void SEM(double D[][],double C[][][],Vector<Vector<double[]>> Sequences, double x[][]) {
	for (int kk=0;kk<q;kk++) {
	    for (int bb=0;bb<L;bb++) {
		double Complex_kk[][] = new double[m][n+1];
		for (int i=0;i<m;i++) {
		    for (int j=0;j<n+1;j++) {
			Complex_kk[i][j] = C[i][j][kk];
		    }
		}
		OffMetro(Sequences.get(kk),Complex_kk,kk,bb);
		//rückschreiben :(
		for (int i=0;i<m;i++) {
		    for (int j=0;j<n+1;j++) {
			C[i][j][kk] = Complex_kk[i][j];
		    }
		}		
	    }
	}
    }
    
    public double [] randn(int n) {
	double randn[] = new double[n];
	
	for (int i=0;i<n;i++) {
	    randn[i] = generator.nextGaussian();
	}
	
	return randn;
    }
    
    //Generates offspring using METROPOLIS HASTINGS monte-carlo markov chain
    public void OffMetro(Vector<double[]> Seq, double C[][],int kk,int bb) {
	Vector vv;
	int s = Seq.size();

	double offspring[] = null;
	double b[];
	//draw point from sequence 
	s = generator.nextInt(s);
	b = Seq.get(s);	
		
	//Sort the m points in Ckk in order of decreasing posterior density
	sort(C,n,true);
			
	//Compute the covariance of complex k 
	double coV[][] = getCoVarMatrix(C);
	double mean[]  = getMean(C);
	//convert to matrix
	Matrix MatrixCoV = new Matrix(coV);		
	CholeskyDecomposition sqrtCoV = new CholeskyDecomposition(MatrixCoV);
					
	//Step 4b, compute new candidate point
	int accept = -1;
		
	JAMSDouble tmpArray[] = new JAMSDouble[n];
	
	int critical_counter = 0;
	
	do {
	    //forget it!!
	    if (critical_counter++ > 100)
		return;
	    
	    Matrix ru = new Matrix(randn(n),n);
	    //generate new point
	    offspring = sqrtCoV.getL().times(ru).getColumnPackedCopy();	    	    	    	    
	    for (int i=0;i<n;i++) {
		offspring[i] += b[i];
	    }
	    for (int i=0;i<n;i++) {
		tmpArray[i] = new JAMSDouble(offspring[i]);
	    }	    
	}while (!IsSampleValid(tmpArray));
	
	//Step 4c, compute posterior probability
	double fm = ComputeDensity(offspring);
	// Replacement rule

	//determine fitness of point s
	int fitness1 = 0,fitness2 = 0;
	while (C[fitness1][n] > b[n]) {
	    fitness1++;
	    if (fitness1 >= C.length)
		break;
	}
	//...and new point
	while (C[fitness2][n] > fm) {
	    fitness2++;
	    if (fitness2 >= C.length)
		break;
	}
	
	double ratio = (double)(fitness1+1) / (double)(fitness2 + 1);
	
	ratio = Math.pow(ratio,0.5*(double)(fitness2 + 1));
	
	//METROPOLIS HASTINGS selection step
	double Z = generator.nextDouble();
	
	double newgen[] = new double[n+1];
	
	if (Z <= ratio) {
	    for (int i=0;i<n;i++) {
		newgen[i] = offspring[i];	   
	    }
	    newgen[n] = fm;
	}
	else {
	    for (int i=0;i<n+1;i++) {
		newgen[i] = b[i];
	    }
	}
	// maybe we add b another time here ... strange
	Seq.add(newgen);

	for (int i=0;i<n+1;i++) {
	    C[m-1][i] = newgen[i];
	}		
    }
    
    public void reshuffle(double C[][][],double D[][],double x[][]) {
	int counter = 0;
	
	for (int qq = 0; qq < q; qq++) {
	    for (int ii = 0; ii < m; ii++) {
		for (int j = 0; j < n; j++) {
		    x[counter][j] = C[ii][j][qq];		    
		}		
		D[counter][0] = C[ii][n][qq];
		D[counter][1] = counter;
		counter++;				
	    }
	}
	//D sortieren...
	sort(D,0,true);
	
	
	System.out.println("D:" + D[0][0]);
	for (int i=0;i<n;i++) {
	    System.out.print(x[(int)D[0][1]][i] + ",");
	}
	System.out.println("");
    }
    
/*    public double[] gelman(double Sequences[][][] ) {
	int npts = Sequences.length;
	int nparp1 = Sequences[0].length;
	int mseq = Sequences[0][0].length;
	int nstart = 1;
	//Check the convergence on the the last 50% of the sequence
	if (npts > 10) {
	    nstart = npts-(npts/2)+1;
	}
	int ntst = npts - nstart; 
	//Calculate convergence parameter for the sequence of each parameter
	int NPar = nparp1-1;
	
	for (int pp=0;pp<=NPar; pp++) {
	    double TestArr[][] = new double[ntst][mseq];
	    for (int i=nstart;i<npts;i++) {
		for (int j=0;j<mseq;j++) {
		    TestArr[i][j] = Sequences[i][pp][j];		    		    
		}
	    }
	    int n = ntst;
	    int m = mseq;
	    
	    double mutot = 0;
	    for (int i=0;i<n;i++) {
		for (int j=0;j<m;j++) {
		    mutot += TestArr[i][j];
		}
	    }
	    mutot /= ntst*mseq;
	    
	    double museq[] = new double[m];
	    java.util.Arrays.fill(museq,0);
	    for (int i=0;i<n;i++) {
		for (int j=0;j<m;j++) {
		    museq[j] += TestArr[i][j];
		}
	    }
	    double B = 0;
	    for (int j=0;j<m;j++) {
		B += (museq[j] - mutot)*(museq[j] - mutot);
	    }
	    B *= n;
	    B /= (m-1);
	    
	    double varseq[] = new double[m];
	    java.util.Arrays.fill(varseq,0);
	    
	    for (int i=0;i<n;i++) {
		for (int j=0;j<m;j++) {
		    varseq[j] += (TestArr[i][j] - museq[j])*(TestArr[i][j] - museq[j]);
		}
	    }
	    
	    double W = 0;
	    for (int j=0;j<m;j++) {
		W += (varseq[j] /= (n - 1));
	    }
	    W /= m;
	    
	    double varhat = ((double)(n - 1)/(double)(n))*W + B / n;
	    
	    
	}
    }*/
    
    public void run() {	
	
        if (runEnumerator == null) {
            runEnumerator = getChildrenEnumerator();
        }

	resetValues();
		
	// Step 1. Complete the initialization of the process by calculating the number 
	// of points in each complex, m and the number of offspring generated per iteration, L
	n = parameters.length;
	s = 100; //get from outside ...
	q = 10; //get from outside ...
	m = s/q;
	L = Math.max(1,m/5); 
				
	double D[][] = new double[s][2];

	Vector<Vector<double[]>> Sequences = new Vector<Vector<double[]>>();
	Vector too;
	double x[][] = new double[s][n];
	//Step 2a. Sample s points in the parameter space
	x = latin(s,n);

	// Calculate densities pset associated with the sampled parameter set
	// pset will be an array with the probability in the first column and the index of 
	// the sample in the second column;
	double pset[][] = ComputeDensity(x);
	// Step 3: Sort the points in order of decreasing posterior probability
	sort(pset,0,true);	
	D = pset;
	
	// Step 4: Initialize starting points of sequences which the Metropolis Hastings algorithm will use...
	for (int i = 0; i<q; i++) {
	    Vector<double[]> Sequence_k = new Vector<double[]>();
	    double extX[] = new double[n+1];
	    
	    for (int j=0;j<n;j++) {
		extX[j] = x[(int)D[i][1]][j];
	    }
	    extX[n] = D[i][0];
	    
	    Sequence_k.add(extX);	
	    Sequences.add(Sequence_k);
	}
	
	// Iterate until the Gelman-Rubin convergence criterium is satisfied...
	int converged = -1,convergence = 2;
	int iter = 0;
	
	while (iter < 100) {
	    double C[][][] = PartComplexes(D,x);
	    
	    SEM(D,C,Sequences,x);
	    
	    reshuffle(C,D,x);
	    
	    iter++;
	    //double convergence[] = gelman(Sequences);
	}
    }               
}
