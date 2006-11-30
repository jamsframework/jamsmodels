/*
 * Neuron.java
 * Created on 12. Mai 2006, 18:21
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

import java.util.*;
import java.util.Map.Entry;
import javax.swing.*;
import java.math.*;
import java.io.FileWriter;

/**
 *
 * @author Christian Fischer
 */

public class Neuron {

    protected double activation;
    protected double delta;
    protected double error;
    
    protected double lastInput;
    protected double input;

    protected long ID;
    protected double lastWeightDelta;
    
    protected Vector ActivationFunctions = new Vector();
    public HashMap<Neuron,Double> Successors = new HashMap<Neuron,Double>();
    protected HashMap<Neuron,Double> Predecessors = new HashMap<Neuron,Double>();
    
    protected HashMap<Neuron,Double> WeightBuffer = new HashMap<Neuron,Double>();
    protected HashMap<Neuron,Double> OldWeight = new HashMap<Neuron,Double>();
    
    static double learningRate = 0.01;
    static double momentum = 0.0;
    static Random generator = new Random();
    
    public Neuron() {
        this.initalize();
    }
    
    public void setID(long ID) {
	this.ID = ID;
    }
    
    public void initalize() {
        activation = 0;
        delta = 0;
        lastInput = 0;
	//ID = 0;
	lastWeightDelta = 0;
    }
    public void reset() {
	this.activation = 0;
	this.input = 0;
	this.delta = 0;
	this.lastWeightDelta = 0;
    }
    
    public void addToInput(double value) {
        input += value;
    }
	
    public void addFilter(GenericFunction af) {
        ActivationFunctions.add(af);
    }
            
    public GenericFunction getFilter(int i) {	
	return (GenericFunction)ActivationFunctions.get(i);
    }
     
   public void propagate() {
        //calculate activation
        calc(input);
        //reset input
	input = 0;
        
	Iterator<Entry<Neuron,Double>> e = Successors.entrySet().iterator();
	
	while (e.hasNext()) {
	    Entry<Neuron,Double> entr = e.next();	    
	    entr.getKey().addToInput(this.activation*entr.getValue());
	}
    }
   
    public void backpropagate() {
	calcDelta(lastInput,error,1);
	//reset error
	error = 0;
	
	Iterator<Entry<Neuron,Double>> e = Predecessors.entrySet().iterator();
		
	while (e.hasNext()) {	   	    
	    Entry<Neuron,Double> entr = e.next();	    	    		    
	    entr.getKey().addToError(this.delta*entr.getValue());
	}
    }
       
    protected double calc(double value) {        
        GenericFunction gc;
        ActivationFunction af;
        
	lastInput = input;
        //no activation function
        if (ActivationFunctions.size() == 0) {
            activation = input;	    	    
	    return activation;
        }  	
	
	activation = 0;
        for (Enumeration e = ActivationFunctions.elements(); e.hasMoreElements(); ) {
            gc = (GenericFunction) e.nextElement();
            af = gc.getFunction();

            activation += af.calculate(value);
        }
	
        return activation;
    }
        
    protected double calcDelta(double value,double error, double buffer) {
        ActivationFunction af;
        GenericFunction gc;
        delta = 0;                        

	if (ActivationFunctions.size() == 0) {
            return delta = error;
        }     
	
        for (Enumeration e = ActivationFunctions.elements(); e.hasMoreElements(); ) {
            gc = (GenericFunction) e.nextElement();
            af = gc.getDFunction();

            delta += af.calculate(value) * error * buffer;
        }                    
        return delta;
    }
        
    protected void BufferAdjustWeight() {
	Iterator<Entry<Neuron,Double>> e = Predecessors.entrySet().iterator();
		
	while (e.hasNext()) {
	    Entry<Neuron,Double> entr = e.next();	    

	    if (!WeightBuffer.containsKey(entr.getKey())) {
		WeightBuffer.put(entr.getKey(),0.0);
		OldWeight.put(entr.getKey(),entr.getValue());
	    }
	    
	    double old = WeightBuffer.get(entr.getKey());	    
	    double next = delta*entr.getKey().getActivation() + old;
	    	  	    	    
	    WeightBuffer.remove(entr.getKey());
	    WeightBuffer.put(entr.getKey(),next);	    	     
	    }	    
    }
    
    protected void cleanWeightBuffer() {
	WeightBuffer.clear();
	OldWeight.clear();
    }
    
    protected void commitWeightBuffer(boolean probFct) {
	Iterator<Entry<Neuron,Double>> e = Predecessors.entrySet().iterator();
		
	while (e.hasNext()) {
	    Entry<Neuron,Double> entr = e.next();	    

	    double next = 0;
/*	    if (probFct) {
		if ( this.generator.nextDouble() < 0.005)
		    next = (generator.nextDouble() - 0.5) * 1.0;
		else
		    next = OldWeight.get(entr.getKey()) + WeightBuffer.get(entr.getKey())*this.learningRate;
	    }
	    else*/
		next = OldWeight.get(entr.getKey()) + WeightBuffer.get(entr.getKey())*this.learningRate;
	    	  	    
	    if (next < -10)
		next = -10;
	    if (next > 10)
		next = 10;
	    
	    entr.setValue(next);	    
	    entr.getKey().Successors.remove(this);
	    entr.getKey().Successors.put(this,next);	    
	    }
    } 
    
    protected double adjustWeight() {
	Iterator<Entry<Neuron,Double>> e = Predecessors.entrySet().iterator();
		
	while (e.hasNext()) {
	    Entry<Neuron,Double> entr = e.next();	    
	    	  
	    double next = entr.getValue()+delta*entr.getKey().getActivation()*learningRate;
	    	  	    
	    if (next < -10)
		next = -10;
	    if (next > 10)
		next = 10;
	    
	    entr.setValue(next);
	    
	    entr.getKey().Successors.remove(this);
	    entr.getKey().Successors.put(this,next);	    
	    }	    
	return 0;
    }

     protected void AddConnection(Neuron Predecessors,Neuron Successor,double weight) {
	Predecessors.Successors.put(Successor,weight);
	Successor.Predecessors.put(Predecessors,weight);
    }
     
    protected void AddConnection(Neuron Successor,double weight) {
	this.Successors.put(Successor,weight);
	Successor.Predecessors.put(this,weight);
    }
    
    public void addToError(double delta) {
        this.error += delta;
    }
    
    public int getFilterCount() {
	return ActivationFunctions.size();
    }
    
    public double getInput() {
	return this.input;
    }
    
    public long getID() {
	return this.ID;
    }
    
    public double getDelta() {
        return delta;
    }
    
    public double getActivation() {
        return (activation);
    }
        
    public void resetFunctions() {
        this.ActivationFunctions.clear();
    }
    
    public void writeData(FileWriter f) {
        //insert debug output here!!
    }
}
