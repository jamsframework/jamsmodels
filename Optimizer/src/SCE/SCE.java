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

package org.unijena.Optimizer;

import java.util.Random;
import java.util.Vector;

import java.util.StringTokenizer;
import java.util.Collections;
import org.unijena.jams.data.*;
import org.unijena.jams.io.GenericDataWriter;
import org.unijena.jams.model.*;
import org.unijena.jams.tools.*;
import org.unijena.Optimizer.SCE_Comparator;
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
        public class SCE extends JAMSContext {
    
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
            public JAMSString effMethodName;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "efficiency values"
            )
            public JAMSDouble effValue;        
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "maximize efficiency?"
            )
            public JAMSInteger MaximizeEff;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "maximize efficiency?"
            )
            public JAMSInteger Population;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "maximize efficiency?"
            )
            public JAMSInteger Complexes;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "The current hru entity"
            )
            public JAMSEntityCollection entities;
    
    JAMSDouble[] parameters;
    String[] parameterNames;
    double[] lowBound;
    double[] upBound;

    int currentCount;
    Random generator;
    
    GenericDataWriter writer;
    
    int N; //parameter dimension
    int p; //number of complexes
    int s; //population size
    int m; //complex size; floor(s/q)
           
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
        
	entities = (JAMSEntityCollection)getModel().getRuntime().getDataHandles().get("hrus");
	
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
    }
            
    private double[] RandomSampler(){
        int paras = this.parameterNames.length;
        double[] sample = new double[paras];
        	
        for(int i = 0; i < paras; i++){
	    double d = generator.nextDouble();
	    
	    sample[i] = (lowBound[i] + d * (upBound[i]-lowBound[i]));
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
    private boolean IsSampleValid(double[] sample) {
	JAMSDouble conv_sample[] = new JAMSDouble[sample.length];
	for (int i = 0;i<sample.length;i++) {
	    conv_sample[i] = new JAMSDouble(sample[i]);
	}
	return IsSampleValid(conv_sample);
    }
    private boolean IsSampleValid(JAMSDouble [] sample) {
	int paras = this.parameterNames.length;
        boolean criticalPara = false;
        double criticalParaValue = 0; 
        	
        for(int i = 0; i < paras; i++){
	    if (sample[i].getValue() < lowBound[i] || sample[i].getValue() > upBound[i] )
		return false;
        }     
        return true;
    }
    
      public void sort(double data[][],int col,boolean decreasing_order) {
	SCE_Comparator comparator = new SCE_Comparator(col,decreasing_order);
	java.util.Arrays.sort(data,comparator);
    }
    
    public double[] getMean(double data[][],int size) {	    //no error here :)
	double[] mean = new double[N];
	
	java.util.Arrays.fill(mean,0);
	
	for (int j=0;j<N;j++) {	    
	    for (int i=0;i<size;i++) {
		mean[j] += data[i][j];
	    }
	    		
	    mean[j] /= size;
	}
	return mean;
    }
            
    public double[][] SampleAndCalculate(int s,int n) {
	double x[][] = new double[s][n+1];
	
	for (int i=0;i<s;i++) {
	    double tmp[] = new double[n];
	    //draw new point by random
	    tmp = RandomSampler();
	    //calulate function value
	    x[i][n] = ComputeFunction(tmp);
	    //copy point
	    for (int j=0;j<n;j++) {
		x[i][j] = tmp[j];
	    }	    	    
	}			
	
	sort(x,n,false);
	
	return x;
    }

    public double ComputeFunction(double x[]) {
	double value = 0;
	
	for (int j=0;j<parameters.length;j++) {
	    parameters[j].setValue(x[j]);
	}
	
	singleRun();
		  
	if (MaximizeEff.getValue() == 1)
	    return this.effValue.getValue();
	else if (MaximizeEff.getValue() == 2)
	    return Math.abs(this.effValue.getValue());
	else if (MaximizeEff.getValue() == 3)
	    return -Math.abs(this.effValue.getValue());
	else if (MaximizeEff.getValue() == 4)
	    return -this.effValue.getValue();
	else 
	    return 0;
    }
   
    public double[][][] PartComplexes(double D[][]) {
	double C[][][] = new double[p][m][N+1];
	
	for (int k=0;k<p;k++) {
	    for (int j=0;j<m;j++) {
		int index = p*j + k;
		for (int i=0;i<N+1;i++) {
		    C[k][j][i] = D[index][i];
		}		
	    }	    
	}
	return C;
    }

    public double[] min(double C[][]) {
	double minH[] = new double[N+1];
	for (int i=0;i<N;i++) {
	    minH[i] = Double.MAX_VALUE;
	}
	for (int i=0;i<C.length;i++) {
	    for (int j=0;j<N;j++) {
		if (C[i][j] < minH[j])
		    minH[j] = C[i][j];
	    }
	}
	return minH;
    }
    
    public double[] max(double C[][]) {
	double maxH[] = new double[N+1];
	for (int i=0;i<N;i++) {
	    maxH[i] = Double.MIN_VALUE;
	}
	for (int i=0;i<C.length;i++) {
	    for (int j=0;j<N;j++) {
		if (C[i][j] > maxH[j])
		    maxH[j] = C[i][j];
	    }
	}
	return maxH;
    }
    public double[] MinCubeSample(double C[][]) {
	double r[] = new double[N+1];
	//compute smallest hypercube H that contains Ak
	double minH[] = min(C);
	double maxH[] = max(C);
	    
	for(int i = 0; i < N; i++){
	    double d = generator.nextDouble();
	    //if omega isnt konvex add IsSampleValid check
	    r[i] = (minH[i] + d * (maxH[i]-minH[i]));
	}
	return r;
    }
    
    public void GenOffspring(double B[][],double C[][],int q) {
	//step 4.4a 
	//sort B and compute the centroid g
	sort(B,N,false);
	
	double g[] = getMean(B,q-1);
	
	//step 4.4b
	//compute the new point r = 2g -uq (reflection step)
	double r[] = new double[N+1];
	for (int i=0;i<N;i++) {
	    r[i] = g[i] - 2.0*B[q-1][i];
	}
	//step 4.4c
	//if r is valid
	if (!IsSampleValid(r)) {	    
	    r = MinCubeSample(C);
	}	   
	//compute function value
	r[N] = ComputeFunction(r);
	
	//step 4.4d
	if (r[N] >= B[q-1][N]) {
	    for (int i=0;i<N;i++) {
		r[i] = (g[i] + B[q-1][i]) / 2.0;
	    }	    
	    //step 4.4e
	    if (!IsSampleValid(r)) {
		r = MinCubeSample(C);
	    }
	    r[N] = ComputeFunction(r);
	    if (r[N] >= B[q-1][N]) {
		r = MinCubeSample(C);
	    }
	    r[N] = ComputeFunction(r);	    
	}
	//copy
	for (int i=0;i<N+1;i++)
	    B[q-1][i] = r[i];
    }
    public void CCE(double C[][]) {
	//step 4.1 init
	int q = m/2 + 1;
	int alpha = 1;

	//step 4.2 assign a triangular probability distribution prob to A
	double prob[] = new double[m];
	prob[0] = 2.0/(m+1);
	for (int i=2;i<=m;i++) {
	    prob[i-1] = prob[i-2] + 2.0 * ( m + 1.0 - i) / ( m * ( m + 1.0 ));
	}
	//step 4.3 select q points from A according to prob
	//store points in B and their position in L
	double B[][] = new double[q][N+2];
		
	for (int i=0;i<q;i++) {
	    int index = 0;
	    boolean inuse = false;
	    
	    do {
		double rnd = this.generator.nextDouble();
		index = 0;
		while (rnd > prob[index] )
		    index++;

		inuse = false;		
		for (int k=0;k<i;k++) {
		    if (B[k][N+1] == index)
			inuse = true;
		}
	    }while(inuse);
	    
	    for (int k=0;k<N+1;k++) {
		B[i][k] = C[index][k];
	    }
	    B[i][N+1] = index;
	}
	//step 4.4 generate offspring
	for (int i=0;i<alpha;i++) {
	    GenOffspring(B,C,q);
	}
	//step 4.5
	//replace parents by offspring
	for (int i=0;i<q;i++) {
	    for (int k=0;k<N+1;k++) {
		int index = (int)B[i][N+1];
	    	C[index][k] = B[i][k];
	    }	    
	}
	sort(C,N,false);
    }
    
    
    public void reshuffle(double C[][][],double D[][]) {
	int counter = 0;
	
	for (int k = 0; k < p; k++) {
	    for (int j = 0; j < m; j++) {
		for (int i = 0; i < N+1; i++) {
		    D[counter][i] = C[k][j][i];		    
		}		
		counter++;				
	    }
	}
	//D sortieren...
	sort(D,N,false);
	
	
	System.out.println("Current D:");
	for (int i=0;i<D.length;i++) {
	    for (int j=0;j<N;j++) {
		System.out.print(parameterNames[j] + ":" + D[i][j] + " ,");
	    }
	    if (MaximizeEff.getValue() % 2 == 1) {
		System.out.print(effMethodName + ":" + D[i][N]);
	    }
	    else {
		double outdata = -D[i][N];
		System.out.print(effMethodName + ":" + outdata + " ,");
	    }
	    System.out.println("\n");
	}		   
    }
    
       
    public void run() {	
	
        if (runEnumerator == null) {
            runEnumerator = getChildrenEnumerator();
        }

	resetValues();
		
	// Step 1. 
	//To initialize the process select p>=1 and m >= n+1, where p is the
	//number of compplexes, m is the number of points in each complex and
	//n is the dimension of the problem. s = sample size
	N = parameters.length;	
	p = Complexes.getValue();
	s = Population.getValue();
	s = (s/p)*p;	
	m = s/p;
	
	// Step 2 + 3
	//generate a sample and compute function value and sort points in order of
	//increasing function values
	double D[][] = new double[s][N+1];
	double x[][] = new double[s][N];

	D = SampleAndCalculate(s,N);	

	long iter = 0;
	int beta = 2*N+1;
	while (iter < sampleCount.getValue()) {
	    // Step 4
	    //Partition D into p Complexes C1, ..., CP each containing m pointa
	    double C[][][] = PartComplexes(D);
	    // Step 5 
	    //evolve each complex Ck according to the competitive complex evolution
	    //(CCE) algorithm
	    for (int i=0;i<p;i++) {
		for (int j=0;j<beta;j++) {
		    CCE(C[i]);
		}
	    }
	    reshuffle(C,D);
	    
	    iter++;
	}	
    }               
}