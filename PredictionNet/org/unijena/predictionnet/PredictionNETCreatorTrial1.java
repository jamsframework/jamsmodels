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
import java.util.*;
import java.io.*;
import org.unijena.jams.JAMS;
import java.util.Random;

/**
 *
 * @author Christian Fischer
 */
public class PredictionNETCreatorTrial1 extends JAMSComponent {
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "TimeSerie of Precip Data"
            )
             public JAMSEntity CompletePrecipArray;
       
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "TimeSerie of Temp Data"
            )
             public JAMSEntity CompleteTempArray;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "TimeSerie of ObsRunoffData Data"
            )
             public JAMSEntity ObsRunoff;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "TimeSerie of Temp Data"
            )
            public JAMSInteger PrecipStationCount;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "TimeSerie of Temp Data"
            )
            public JAMSInteger TempStationCount;
    
    static int RelevantTime = 7;

    static int RUNSTART = 0;
    static int RUNEND = 1000;
        
    static int TRAININGLENGTH = 20;
    static int FORECAST = 2;
    
    Random generator = new Random();
    
    HashMap<Integer, double[]> PrecipData;
    HashMap<Integer, double[]> TempData;
    HashMap<Integer, double[]> RunOffData;
    int iPrecipStationCount;
    int iTempStationCount;

    Vector<Neuron> Layers[] = new Vector[3];
    InputNeuron PrecipNeurons[][];
    InputNeuron TempNeurons[][];
    Neuron outNeuron = new Neuron();
    
    //learning rate = 0.1
    public void setupNET() throws JAMSEntity.NoSuchAttributeException {                	
	getModel().getRuntime().println("Setup PredictionNET");
	generator.setSeed(System.currentTimeMillis());
	
	PrecipData = (HashMap<Integer, double[]>)CompletePrecipArray.getObject("Data");
	TempData = (HashMap<Integer, double[]>)CompleteTempArray.getObject("Data");
	RunOffData = (HashMap<Integer, double[]>)ObsRunoff.getObject("Data");;
				
	iPrecipStationCount = PrecipStationCount.getValue();
	iTempStationCount = TempStationCount.getValue();
		
	PrecipNeurons = new InputNeuron[RelevantTime][iPrecipStationCount];
	TempNeurons = new InputNeuron[RelevantTime][iTempStationCount];
	
	Layers[0] = new Vector<Neuron>();
	Layers[0].setSize((iPrecipStationCount+iTempStationCount)*RelevantTime);
	
	Layers[1] = new Vector<Neuron>();
	Layers[1].setSize((iPrecipStationCount+iTempStationCount)*RelevantTime);
	
	Layers[2] = new Vector<Neuron>();
	Layers[2].setSize(1);
		
	outNeuron.initalize();
	outNeuron.setID(0);
	
	InputNeuron BiasNeuron  = new InputNeuron();
	BiasNeuron.initalize();
	BiasNeuron.setID(1);
	BiasNeuron.SetInput(1.0);
			
	int ID = 1000;
	int Layer0ID = 0;
	int Layer1ID = 0;
	
	for (int i=0;i<iPrecipStationCount+iTempStationCount;i++) {
	    for (int j=0;j<RelevantTime;j++) {			    
		Neuron innerLayer  = new Neuron();
			    	    	    				
		LogisticFunction lf = new LogisticFunction(0.01);		
		GenericFunction gf = new GenericFunction(lf);
				
		innerLayer.setID(ID++);
		innerLayer.initalize();
		innerLayer.addFilter(gf);		
		
		Layers[1].set(Layer1ID,innerLayer);
		Layer1ID++;		
		innerLayer.AddConnection(outNeuron,generator.nextDouble()-0.5);
		
		BiasNeuron.AddConnection(innerLayer,generator.nextDouble()-0.5);
	    }	    
	}
	
	for (int i=0;i<iPrecipStationCount;i++) {
	    for (int j=0;j<RelevantTime;j++) {
		InputNeuron precipNeuron = new InputNeuron();
			    		
		precipNeuron.setID(ID++);
		precipNeuron.initalize();
	   	    								
		Layers[0].set(Layer0ID,precipNeuron);
		Layer0ID++;		
		
		PrecipNeurons[j][i] = precipNeuron;		
		
		for (int k=0;k<Layers[1].size();k++) {
		    PrecipNeurons[j][i].AddConnection(Layers[1].get(k),generator.nextDouble()-0.5);		    
		}				
	    }	    
	}
	
	for (int i=0;i<iTempStationCount;i++) {
	    for (int j=0;j<RelevantTime;j++) {
		InputNeuron tempNeuron   = new InputNeuron();		
	    	    
		tempNeuron.setID(ID++);
		tempNeuron.initalize();
	    								
		Layers[0].set(Layer0ID,tempNeuron);
		Layer0ID++;
		
		TempNeurons[j][i] = tempNeuron;	
		
		for (int k=0;k<Layers[1].size();k++) {
		    TempNeurons[j][i].AddConnection(Layers[1].get(k),generator.nextDouble()-0.5);		    
		}	
	    }	    
	}
    }

    public void SendDataToNet(int p) {
	for (int i=0;i<iTempStationCount;i++) {
	    for (int j=0;j<RelevantTime;j++) {
		TempNeurons[j][i].SetInput(TempData.get(j+p)[i]);
		PrecipNeurons[j][i].SetInput(PrecipData.get(j+p)[i]);
	    }
	}	
    }
    
    public int Train(int start) {
	//lernen ... 
	double accError = 1000;
	int iterationCount = 0;
	
	while (accError > 0.5) {	    
	    accError = 0;
	    iterationCount++;
	    //lege nacheinander eingaben a
	    for (int p=start;p<start+TRAININGLENGTH;p++) {						
		SendDataToNet(p);
		
		for (int i=0;i<Layers[0].size();i++) {
		    Layers[0].get(i).propagate();
		}
	    
		for (int i=0;i<Layers[1].size();i++) {
		    Layers[1].get(i).propagate();
		}
	    
		outNeuron.propagate();
	    
		double runoff = RunOffData.get(p+RelevantTime+FORECAST-1)[0];
	    
		outNeuron.addToError(runoff-outNeuron.getActivation());
		System.out.println(runoff-outNeuron.getActivation());
	    
		accError += Math.abs(runoff-outNeuron.getActivation());
		
		outNeuron.backpropagate();
		outNeuron.adjustWeight();
	    
		for (int i=0;i<Layers[1].size();i++) {
		    Layers[1].get(i).backpropagate();
		    Layers[1].get(i).adjustWeight();
		}
	    
		for (int i=0;i<Layers[0].size();i++) {
		    Layers[0].get(i).backpropagate();
		}				
	    }
	    
	    System.out.println("Cycle finished: AccError = " + accError);
	}
	return iterationCount;
    }
    
    public void run() throws JAMSEntity.NoSuchAttributeException {                	
	BufferedWriter writer = null;
			
	try {	
	writer = new BufferedWriter(new FileWriter("E:\\ergebnisse.txt"));
	} catch (Exception e) {
	    System.out.println("Fehler Datei konnte nicht geˆffnet werden!");
	}
	
	for (int T=RUNSTART;T<RUNEND;T++) {
	    //sinnvoller?
	    setupNET();
	    
	    int iterationCount = Train(T);
	    
	    //lege testdatensatz an
	    SendDataToNet(T+TRAININGLENGTH);

	    for (int i=0;i<Layers[0].size();i++) {
		Layers[0].get(i).propagate();
	    }
	    
	    for (int i=0;i<Layers[1].size();i++) {
		Layers[1].get(i).propagate();
	    }
	    
	    outNeuron.propagate();
	    
	    double runoff = RunOffData.get(T+TRAININGLENGTH+RelevantTime+FORECAST-1)[0];
	    System.out.println("RunOff:" + runoff);
	    System.out.println("outNeuron.getActivation():" + outNeuron.getActivation());
	    System.out.println("TATATATATATA -> der Kaffeesatzleser hat einen Fehler von:" + (runoff-outNeuron.getActivation()) + " gemacht!");	    	
	    
	    try {	    	    
		writer.write(T + "\t" + runoff + "\t" + outNeuron.getActivation() + "\t" + (runoff-outNeuron.getActivation()) + "\t" + iterationCount);
		writer.newLine();
		writer.flush();
	    }catch (Exception e) {
		System.out.println("Fehler beim schreiben!!");
	    }
	}	
	try {
	writer.close();
	}catch (Exception e) {
	    System.out.println("Fehler beim schlieﬂen");
	}
    }
}

