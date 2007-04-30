/*
 * PredictionNETCreator.java
 * Created on 12. Mai 2006, 17:41
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

package org.unijena.predictionnet;

import org.unijena.j2k.*;
import org.unijena.jams.data.*;
import org.unijena.jams.model.*;
import org.unijena.jams.JAMS;
import org.unijena.jams.data.*;
import java.util.*;
import java.io.*;
import org.unijena.jams.JAMS;
import java.util.Random;
import Jama.*;
import Jama.Matrix;
import Jama.LUDecomposition;
import Jama.util.Maths;

/**
 *
 * @author Christian Fischer
 */
public class PredictionNETCreator extends JAMSComponent {
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "TimeSerie of Precip Data"
            )
             public JAMSString datafile;
                
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "TimeSerie of Temp Data"
            )
             public JAMSInteger ExampleLength;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "TimeSerie of Temp Data"
            )
             public JAMSInteger numOfExamples;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "TimeSerie of Temp Data"
            )
            public JAMSInteger trainStart;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "TimeSerie of Temp Data"
            )
            public JAMSInteger trainEnd;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "TimeSerie of Temp Data"
            )
            public JAMSInteger validationStart;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "TimeSerie of Temp Data"
            )
            public JAMSInteger validationEnd;
           
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "TimeSerie of Temp Data"
            )
            public JAMSInteger method;
    
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
            public JAMSString parameterFile;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.INIT,
            description = "TimeSerie of Temp Data"
            )
            public JAMSString resultFile;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.INIT,
            description = "TimeSerie of Temp Data"
            )
            public JAMSBoolean doOptimizing;
    
    static int RelevantTime = 3;
                         
    HashMap<Integer, double[]> rawData;    
    HashMap<Integer, double[]> rawPredict;

    double data[][];
    double predict[];
    
    double traindata[][];
    double trainpredict[];
    
    double validationdata[][];
    double validationpredict[];
    
    int ExamplLength,numOfExampl;

    LinearRegression meth1_learner = null;
    NNLearner meth2_nnlearner = null;
    GaussianLearner meth3_learner = null;
    
    public void loadData() throws JAMSEntity.NoSuchAttributeException {  
	BufferedReader reader = null;
	HashMap<Integer, double[]> rawData = new HashMap<Integer,double[]>();
	HashMap<Integer, double[]> rawPredict = new HashMap<Integer,double[]>();
	
	ExamplLength = ExampleLength.getValue();
	numOfExampl  = numOfExamples.getValue();
	
        try {
            reader = new BufferedReader(new FileReader(datafile.getValue()));
        } catch (IOException ioe) {
            JAMS.handle(ioe);
        }
	String nextString = null;
	try {
	int i = 0;
	while ((nextString = reader.readLine()) != null) {	    	    
	    StringTokenizer st = new StringTokenizer(nextString, "\t");
	    double[] Example = new double[ExamplLength];
	    double[] Predict = new double[1];
	    
	    try {
	    for (int j = 0; j < ExamplLength; j++) {		
		Example[j] = (new Double(st.nextToken())).doubleValue();
	    }
	    Predict[0] = (new Double(st.nextToken())).doubleValue();
	    } catch(Exception e) {
		System.out.println("Error in Dataset: " + i + " stop reading! (not enough tokens)");
		break;
	    }	    	    	    	    
	    if (st.hasMoreTokens()) {
		System.out.println("Error in Dataset: " + i + " stop reading! (too many tokens)");
		break;
	    }
	    
	    rawData.put(new Integer(i),Example);
	    rawPredict.put(new Integer(i),Predict);
	    
	    i++;
	}
	
	} catch (IOException ioe) {
            JAMS.handle(ioe);
        }
					
	data = new double[numOfExampl][RelevantTime*ExamplLength];
	predict = new double[numOfExampl];
	
	for (int i=0;i<numOfExampl;i++) {
	    for (int j=0;j<RelevantTime;j++) {
		double entry[] = rawData.get(new Integer(i+j));
		for (int k=0;k<ExamplLength;k++) {
		    data[i][j*ExamplLength+k] = entry[k];
		}
	    }
	    predict[i] = rawPredict.get(new Integer(i+RelevantTime-1))[0];
	}
	
	traindata = new double[trainEnd.getValue() - trainStart.getValue()][RelevantTime*ExamplLength];
	trainpredict = new double[trainEnd.getValue() - trainStart.getValue()];
	
	for (int i=trainStart.getValue();i<trainEnd.getValue();i++) {
	    traindata[i-trainStart.getValue()] = data[i];
	    trainpredict[i-trainStart.getValue()] = predict[i];
	}
	
	validationdata = new double[validationEnd.getValue() - validationStart.getValue()][RelevantTime*ExamplLength];
	validationpredict = new double[validationEnd.getValue() - validationStart.getValue()];
	
	for (int i=validationStart.getValue();i<validationEnd.getValue();i++) {
	    validationdata[i-validationStart.getValue()] = data[i];
	    validationpredict[i-validationStart.getValue()] = predict[i];
	}		
    }
    
    public void train() {
	System.out.println("*************************************");
	System.out.println("*********PHASE 1 - TRAINING**********");
	System.out.println("*************************************");
	    
	if (method.getValue() == 1) {
	    meth1_learner = new LinearRegression();
	    meth1_learner.normalizeData = true;
	    meth1_learner.InterpolationSize = 0.75;
	    meth1_learner.KernelMethod = 1;
	    
	    meth1_learner.setTrainingData(this.traindata,this.trainpredict);	    	   
	    meth1_learner.Train();	    
	}
	
	if (method.getValue() == 2) {
	    meth2_nnlearner = new NNLearner();	    
	    meth2_nnlearner.normalizeData = true;
	    meth2_nnlearner.decayLearningRate = false;
	    meth2_nnlearner.learningRate = 0.0002;
	    meth2_nnlearner.momentum = 0.2;
	    meth2_nnlearner.driftThreshold = 0.0;
	    meth2_nnlearner.numEpochs = 150;    		
	    meth2_nnlearner.setTrainingData(this.traindata,this.trainpredict);
	    meth2_nnlearner.setLayerSize(1);
	    
	    meth2_nnlearner.setupNET();
	    meth2_nnlearner.Train();
	}
	
	if (method.getValue() == 3) {	    	    
	    meth3_learner = new GaussianLearner();
	    meth3_learner.normalizeData = true;
	    meth3_learner.theta = new double[this.traindata[0].length+1];
	    meth3_learner.setTrainingData(this.traindata,this.trainpredict);
	    meth3_learner.kernelmethod = 5;
	    meth3_learner.normalizeAll();
	    
	    if (this.doOptimizing.getValue()) {
		for (int i=0;i<meth3_learner.theta.length;i++) {
		    meth3_learner.theta[i] = 1.0;
		}	
		System.out.println("*************************************");
		System.out.println("***PHASE 1a - PARAMETEROPTIMIZATION**");
		System.out.println("******you should wait some hours;)***");
		System.out.println("*************************************");
		meth3_learner.Train();
		meth3_learner.optimizeLength();
		//meth3_learner.Testoptimizer();
		System.out.println("Optimization Complete!");
		
		BufferedWriter writer;
		try {
		    writer = new BufferedWriter(new FileWriter(this.parameterFile.getValue()));
		    for (int i=0;i<meth3_learner.theta.length;i++) {
			writer.write((new Double(meth3_learner.theta[i]).toString()));
			writer.write("\n");
		    }
		    writer.close();
		}
		catch (Exception e) {
		    System.out.println("Could not open parameter result file, becauce:" + e.toString());
		    System.out.println("Optimizationresult won't be saved");
		}		
	    }
	    else {
		BufferedReader reader;
		try {
		    reader = new BufferedReader(new FileReader(this.parameterFile.getValue()));
		    for (int i=0;i<meth3_learner.theta.length;i++) {
			meth3_learner.theta[i] = (new Double(reader.readLine())).doubleValue();			
		    }
		    reader.close();
		}
		catch (Exception e) {
		    System.out.println("Could not open " + "parameter file, becauce:" + e.toString());
		    System.out.println("I will use Standardparameters!");
		}
	    }
	    	    	    	    	    
	    this.performance.setValue(meth3_learner.Train());
	    System.out.println("Training Complete!");
	    System.out.println("marginal likelihood was: " + this.performance.getValue());
	}
    }
    
    public void validate() {
	System.out.println("*************************************");
	System.out.println("*******PHASE 2 - VALIDATION**********");
	System.out.println("*************************************");
	    
	BufferedWriter writer = null;
	try {
	    writer = new BufferedWriter(new FileWriter(this.resultFile.getValue()));	    
	}
	catch (Exception e) {
	    System.out.println("Could not open result file, becauce:" + e.toString());
	    System.out.println("results won't be saved");
	}		
	
	for (int i=0;i<validationdata.length;i++) {
	    double result = 0.0,variance = 0.0;
	    double correctValue = this.validationpredict[i];
	    
	    if (method.getValue() == 1) {
		System.out.println(correctValue + "\t" + result);
		result = meth1_learner.Predict(this.validationdata[i]);
	    }
	    
	    if (method.getValue() == 2) {
		System.out.println(correctValue + "\t" + result);
		result = meth2_nnlearner.Predict(this.validationdata[i]);
	    }
	    	    
	    if (method.getValue() == 3) {								    
		result = meth3_learner.Predict(this.validationdata[i]);
		variance = meth3_learner.getVariance(this.validationdata[i]);
		
		System.out.println(correctValue + "\t" + result + "\t" + variance);
		try {
		writer.write(new String(correctValue + "\t" + result + "\t" + variance + "\n"));
		writer.write("\n");		
		} catch(Exception e) {
		    System.out.println("could not write, because: " + e.toString());
		}
	    }		
	}
	try {
	    writer.close();
	}catch(Exception e) {
	    System.out.println(e.toString());
	}
    }
    
    public void run() throws JAMSEntity.NoSuchAttributeException {                	
	loadData();
	train();		
	validate();	
    }
}
