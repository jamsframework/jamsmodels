/*
 * Learner.java
 *
 * Created on 10. April 2007, 14:35
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

/**
 *
 * @author Christian(web)
 */
public class NNLearner extends Learner {

    int LayerCount;
    Vector<Neuron> Layers[];                
    Vector<Integer> LayerSize;
            
    Neuron outNeuron;
    	       
    public boolean decayLearningRate;
    public double learningRate;
    public double momentum;
    public double driftThreshold;
    public int numEpochs;

    
    public NNLearner() {
	normalizeData = true;
	decayLearningRate = false;
	learningRate = 0.3;
	momentum = 0.2;
	driftThreshold = 20.0;
	numEpochs = 500;    	
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
    
    public int Train() {
	//lernen ... 
	int iterationCount = 0;
	double Error = 1000;
	
	Neuron.learningRate = this.learningRate;
	NeuralConnection.momentum = this.momentum;
	
	while (Error > this.driftThreshold && iterationCount < this.numEpochs) {	    
	    iterationCount++;

	    Error = TrainCycle();
	    System.out.println("Trainerror:" + Error);
	}
	return iterationCount;
    }
}
