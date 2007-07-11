/*
 * Learner.java
 *
 * Created on 10. April 2007, 14:35
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.unijena.predictionnet;

import java.io.BufferedWriter;
import java.io.FileWriter;
import org.unijena.jams.data.*;
import org.unijena.jams.model.*;
import java.util.*;
import weka.core.Attribute;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;

/**
 *
 * @author Christian(web)
 */
public class NNLearner extends Learner {

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
            update = JAMSVarDescription.UpdateType.RUN,
            description = "TimeSerie of Temp Data"
            )
            public JAMSDouble learningrate;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "TimeSerie of Temp Data"
            )
            public JAMSDouble momentum;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "TimeSerie of Temp Data"
            )
            public JAMSString layers;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "TimeSerie of Temp Data"
            )
            public JAMSInteger epochen;
                            
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "TimeSerie of Temp Data"
            )
            public JAMSString resultFile = null;
                           
    int LayerCount;
    Vector<Neuron> Layers[];                
    Vector<Integer> LayerSize;
            
    Neuron outNeuron;
    weka.classifiers.functions.MultilayerPerceptron MLP = null;
    
    public NNLearner() {
	normalizeData = true;
	LayerCount = 3;	
	
	Layers = new Vector[LayerCount];                
	LayerSize = new Vector<Integer>();
    }
        
    public void setLayerSize(int option) {
	//this is standard
	if (option == 1) {
	    this.LayerCount = 3;
	    
	    this.LayerSize.add(0,new Integer(DataLength)+1);
	    this.LayerSize.add(1,new Integer((DataLength+1)/2));
	    this.LayerSize.add(2,1);
	}
	if (option == 2) {
	    this.LayerCount = 4;
	    this.LayerSize.add(0,new Integer(DataLength)+1);
	    this.LayerSize.add(1,new Integer((DataLength+1))/2);
	    this.LayerSize.add(2,new Integer((DataLength+1))/2);
	    this.LayerSize.add(3,1);
	}
	if (option == 3) {
	    this.LayerCount = 3;
	    
	    this.LayerSize.add(0,new Integer(DataLength)+1);
	    this.LayerSize.add(1,new Integer(2));
	    this.LayerSize.add(2,1);
	}
    }
        
    public void setupNET() {                		
	generator.setSeed(-1);
	
	for (int i=0;i<LayerCount;i++) {
	    Layers[i] = new Vector<Neuron>();
	    Layers[i].setSize(LayerSize.get(i));
	}
														
	//setup layer 0
	for (int i=0;i<LayerSize.get(0).intValue();i++) {
	    InputNeuron inNeuron = new InputNeuron();

	    inNeuron.initalize();
	    Layers[0].set(i,inNeuron);		
	}	    
		
	for (int m=1;m<LayerCount;m++) {	
	    for (int i=0;i<LayerSize.get(m);i++) {
		Neuron innerNeuron  = new Neuron();
		
		LogisticFunction logf = new LogisticFunction(1.0);		
		GenericFunction gf = new GenericFunction(logf);
		
		innerNeuron.initalize();
		if (m != LayerCount - 1)
		    innerNeuron.addFilter(gf);		
		
		Layers[m].set(i,innerNeuron);
		
	/*	if (m==1) {
		if (i == 0) {
		    innerNeuron.AddConnection(Layers[0].get(0),innerNeuron,-0.5);
		    innerNeuron.AddConnection(Layers[0].get(1),innerNeuron,-0.22);
		    innerNeuron.AddConnection(Layers[0].get(2),innerNeuron,0);
		}
		if (i == 1) {
		    innerNeuron.AddConnection(Layers[0].get(0),innerNeuron,-0.44);
		    innerNeuron.AddConnection(Layers[0].get(1),innerNeuron,-0.09);
		    innerNeuron.AddConnection(Layers[0].get(2),innerNeuron,0);
		}
		}
		if (m==2) {
		    innerNeuron.AddConnection(Layers[1].get(0),innerNeuron,0.05);
		    innerNeuron.AddConnection(Layers[1].get(1),innerNeuron,0.16);
		}*/
		
		for (int k=0;k<Layers[m-1].size();k++) {		    
		    innerNeuron.AddConnection(Layers[m-1].get(k),innerNeuron,(generator.nextDouble()*1.0) - 0.5);
		}				
	    }
	}
	
	outNeuron = Layers[LayerCount-1].get(0);
    }
            
    public double Predict(double p[]) {
	double tmp[] = new double[p.length];
	
	if (this.normalizeData) {
	    for (int i=0;i<p.length;i++) {
		tmp[i] = 2.0 * (p[i] - base[i]) / (max[i] - min[i]);
	    }
	}
	else
	    tmp = p;
			
	for (int i=0;i<=this.DataLength;i++) {
	    InputNeuron inNeuron = (InputNeuron)Layers[0].get(i);
	    if (i == this.DataLength)
		inNeuron.SetInput(1.0);
	    else
		inNeuron.SetInput(tmp[i]);
	}
			
	Propagate();
	
	return this.outNeuron.getActivation();//*0.5*(this.pmax - this.pmin) + this.pbase;
    }
    
    public double Propagate() {
	for (int k=0;k<LayerCount;k++) {
	    for (int i=0;i<Layers[k].size();i++) {
		Layers[k].get(i).propagate();
		}
	    }				
	return outNeuron.getActivation();
    }
    
    public void BackPropagate(double error) {
	outNeuron.addToError(error);			
		    
	for (int k=LayerCount-1;k>=0;k--) {
	    for (int i=0;i<Layers[k].size();i++) {
		Layers[k].get(i).backpropagate();
		Layers[k].get(i).updateWeightDelta();		
	    }
	}	
    }
    
    public void AdjustWeights() {
	for (int k=1;k<LayerCount;k++) {
	    for (int i=0;i<Layers[k].size();i++) {
		Layers[k].get(i).adjustWeight();
		}
	    }	
    }
    
    public double TrainCycle() {
	double accError = 0;
		    
	for (int p=0;p<TrainLength;p++) {
//	    int ps = this.generator.nextInt(TrainLength);
	    double predValue = Predict(this.data[p]);
	    double correctValue = this.result[p];
	   /* if (this.normalizeData) {
		predValue = 2.0*(predValue)/(pmax-pmin);
		correctValue = 2.0*(correctValue)/(pmax-pmin);
	    }*/
	    accError += Math.abs(correctValue - predValue);
	    
	    BackPropagate(correctValue - predValue);	    
	}    		
	AdjustWeights();
	return accError;
    }
    public double[] Predict(double data[][],double predict[],boolean writeResult) {
	int M = data[0].length;
	int P = data.length;
		
	FastVector atts = new FastVector(M+1);
	for (int i=0;i<M+1;i++) {
	    atts.addElement(new Attribute("data" + Integer.toString(i)));	    
	}
	
	Instances dataSet = new Instances("data", atts, P);
	
	for (int i=0;i<P;i++) {
	    double next[] = new double[M+1];
	    for (int j=0;j<M;j++) {
		next[j] = data[i][j];
	    }
	    next[M] = 0;
	    Instance singleInst = new Instance(1,next);	    	    
	    dataSet.add(singleInst);
	}
	dataSet.setClassIndex(M);
	double result[] = new double[P];
	
	for (int i=0;i<P;i++) {	   	    	    
	    try {
		result[i] = MLP.classifyInstance(dataSet.instance(i));
	    }
	    catch(Exception e) {
		System.out.println("Could not classify instance -> " + e.toString());
	    }
	    //System.out.println(validation_predict[i] + "\t" + result);
	}
	if (!writeResult)
	    return result;
	
	BufferedWriter writer = null;
	try {
	    writer = new BufferedWriter(new FileWriter(this.resultFile.getValue(),true));	    
	}
	catch (Exception e) {
	    System.out.println("Could not open result file, becauce:" + e.toString());
	    System.out.println("results won't be saved");
	}
	    
	for (int i=0;i<P;i++) {			    
	    try {
		writer.write(new String(predict[i] + "\t" + result[i] + "\n"));		    
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
	    System.out.println("NN - Error" + e.toString());
	}
	return result;
    }
    
    public void Train(double data[][],double predict[]) {
	int M = data[0].length;
	int N = data.length;
		
	FastVector atts = new FastVector(M+1);
	for (int i=0;i<M+1;i++) {
	    atts.addElement(new Attribute("data" + Integer.toString(i)));	    
	}
	Instances dataSet = new Instances("data", atts, N);

	for (int i=0;i<N;i++) {
	    double next[] = new double[M+1];
	    for (int j=0;j<M;j++) {
		next[j] = data[i][j];
	    }
	    next[M] = predict[i];
	    Instance singleInst = new Instance(1,next);	    	    
	    dataSet.add(singleInst);
	}
	dataSet.setClassIndex(M);
	MLP = new weka.classifiers.functions.MultilayerPerceptron();
	MLP.setGUI(false);
	MLP.setHiddenLayers(this.layers.getValue());
	MLP.setLearningRate(this.learningrate.getValue());
	MLP.setMomentum(this.momentum.getValue());
	MLP.setTrainingTime(this.epochen.getValue());
	try {
	    MLP.buildClassifier(dataSet);
	}
	catch(Exception e) {
	    System.out.println("MLP didn´t want to train ... " + e.toString());
	}
    }
    
    public double SingleRun(double trainData[][],double trainPredict[],double valData[][],double valPredict[],boolean writeResult)  {
	Train(trainData,trainPredict);
	double result[] = Predict(valData,valPredict,writeResult);
	double MSE = 0;
	
	for (int i=0;i<result.length;i++) {
	    MSE += (valPredict[i] - result[i])*(valPredict[i] - result[i]);
	}
	    
	return MSE;
    }
    
    public double crossvalidation(double data[][],double predict[]) {
	long t1 = System.currentTimeMillis();
	
	int k = 5;
	double error = 0;
	
	int N = data.length;
	int M = data[0].length;
	int d = N / k;
	//aufrunden
	if (d * k != N) {
	    d += 1;
	}
	//split up data
	for (int i=0;i<k;i++) {
	    int trainCounter = 0;
	    int valCounter = 0;
	    	    	    	    
	    //testrun
	    for (int j=0;j<N;j++) {
		if ( (j / d) == i) {
		    valCounter++;
		}
		else {		    		    
		    trainCounter++;
		}
	    }	    
	    double valData[][] = new double[valCounter][];
	    double valPredict[] = new double[valCounter]; 
	    double trainData[][] = new double[trainCounter][];	    	    
	    double trainPredict[] = new double[trainCounter]; 
	    trainCounter = 0;
	    valCounter = 0;
	    
	    for (int j=0;j<N;j++) {
		if ( (j / d) == i) {
		    valData[valCounter] = data[j];
		    valPredict[valCounter] = predict[j];
		    valCounter++;
		}
		else {		    		    
		    trainData[trainCounter] = data[j];
		    trainPredict[trainCounter] = predict[j];
		    trainCounter++;
		}
	    }	    
	    error += SingleRun(trainData,trainPredict,valData,valPredict,false);	    
	}   
	long t2 = System.currentTimeMillis();
	
	//System.out.println("CV - Time:" + (double)(t2 - t1) / 1000.0);
	return error;
    }
    
    public void SetParams(int EpochCounter,double LearningRate, double Momentum) {
	if (EpochCounter <= 0) EpochCounter = 1;
	if (LearningRate < 0.01)   LearningRate = 0.01;
	if (Momentum < 0.0 ) Momentum = 0.0;
	
	if (this.epochen == null)	this.epochen = new JAMSInteger();
	if (this.learningrate == null)  this.learningrate = new JAMSDouble();
	if (this.momentum == null)	this.momentum = new JAMSDouble();
	
	this.epochen.setValue(EpochCounter);
	this.learningrate.setValue(LearningRate);
	this.momentum.setValue(Momentum);
    }
    
    public void optimize(double data[][],double predict[]) {
	//5 fold cross validation
	System.out.println("Optimization");
	boolean noImprovement = false;
	
	int EpochCounter = 1;
	double LearningRate = 0.3;
	double Momentum = 0.2;
	
	double delta = 0.001;
	double alpha_min = 0.0001;
	
	int alpha_Epoch = 1;
	double alpha_lrate = 0.1,alpha_mom = 0.1;
		
	int igradient;
	double gradient;
	
	SetParams(EpochCounter,LearningRate,Momentum);
	double y_alt = crossvalidation(data,predict);
	double y_neu = y_alt;
	double y_last;
	
	do {
	    y_last = y_alt;
	    //improve epochs
	    do {
		SetParams(EpochCounter+10,LearningRate,Momentum);
		y_neu = crossvalidation(data,predict);
		
		//alpha_Epoch *= 2;
		igradient = 1;		
		/*if (y_neu <= y_alt) igradient = +1;
		else		    igradient = -1;*/
		
		do{		    
		    alpha_Epoch *= 2;
		    
		    SetParams(EpochCounter+alpha_Epoch*igradient,LearningRate,Momentum);
		    y_neu = crossvalidation(data,predict);
		    
 		    if (alpha_Epoch >= 3000) {
			break;
		    }
		}while(y_neu > y_alt);
		
		EpochCounter+=alpha_Epoch*igradient;
		System.out.println("Wert:" + y_neu + "\t Stelle: Epochen:" + EpochCounter + "\t alpha: " + LearningRate + "\t beta: " + Momentum);
		y_alt = y_neu;
		
	    }while(y_neu < y_alt);
	    /*
	    
	    //improve lrate
	    do {
		SetParams(EpochCounter,LearningRate+delta,Momentum);
		y_neu = crossvalidation(data,predict);
		
		alpha_lrate *= 4.0;
				
		if (y_neu <= y_alt) gradient = +1.0;
		else		    gradient = -1.0;
		do{		    
		    alpha_lrate /= 2.0;
		    
		    SetParams(EpochCounter,LearningRate+alpha_lrate*gradient,Momentum);
		    y_neu = crossvalidation(data,predict);
		    
		    if (alpha_lrate <= alpha_min) {
			alpha_lrate = 0.0;
			break;
		    }
		}while(y_neu > y_alt);
		LearningRate+=alpha_lrate*gradient;				
		System.out.println("Wert:" + y_neu + "\t Stelle: Epochen:" + EpochCounter + "\t alpha: " + LearningRate + "\t beta: " + Momentum);
		y_alt = y_neu;
	    }while(y_neu < y_alt);
	    	    
	    //improve Momentum
	    do {
		SetParams(EpochCounter,LearningRate,Momentum+delta);
		y_neu = crossvalidation(data,predict);
		
		alpha_mom *= 4.0;
				
		if (y_neu <= y_alt) gradient = +1.0;
		else		    gradient = -1.0;
		do{		    
		    alpha_mom /= 2.0;
		    
		    SetParams(EpochCounter,LearningRate,Momentum+alpha_mom*gradient);
		    y_neu = crossvalidation(data,predict);
		    
		    if (alpha_mom <= alpha_min) {
			alpha_mom = 0.0;
			break;
		    }
		}while(y_neu > y_alt);
		
		Momentum+=alpha_mom*gradient;				
		System.out.println("Wert:" + y_neu + "\t Stelle: Epochen:" + EpochCounter + "\t alpha: " + LearningRate + "\t beta: " + Momentum);
		y_alt = y_neu;
		
	    }while(y_neu < y_alt);*/	    	    
	}while( (y_last - y_alt) / y_last > 0.01);
    }
    
    public void run() {			
	double data[][] = null;
	double predict[] = null;
	
	double validation_data[][] = null;
	double validation_predict[] = null;
	try {
	    data = (double[][])trainData.getObject("data");
	    predict = (double[])trainData.getObject("predict");
	    
	    validation_data = (double[][])validationData.getObject("data");
	    validation_predict = (double[])validationData.getObject("predict");
	} catch(Exception e) {
	    System.out.println("could not find data!!" + e.toString());
	}
	
	optimize(data,predict);
	
	this.Predict(validation_data,validation_predict,true);
    }
}
