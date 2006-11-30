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
import java.util.Map.Entry;
import javax.swing.*;
import java.math.*;

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
    static int FORECAST = 0;
    final int LayerCount = 3;
    
    Random generator = new Random();
    
    HashMap<Integer, double[]> PrecipData;
    HashMap<Integer, double[]> TempData;
    HashMap<Integer, double[]> RunOffData;
    int iPrecipStationCount;
    int iTempStationCount;
        
    Vector<Neuron> Layers[] = new Vector[LayerCount];
    InputNeuron PrecipNeurons[][];
    InputNeuron TempNeurons[][];
    InputNeuron RunOffNeurons[];
    
    Neuron outNeuron = new Neuron();
        
    Vector<Integer> LayerSize;
    InputNeuron BiasNeuron  = new InputNeuron();
    
    public void writeGraphFile(String fileName) {
	BufferedWriter writer = null;
	
	try {	
	writer = new BufferedWriter(new FileWriter(fileName));
	
	writer.write("digraph nnetwork {");
	writer.newLine();
	
	}catch (Exception e) {
	    System.out.println("Fehler->" + e.toString());
	}	
	//schreibe header
	
	
	for (int i=0;i<LayerCount;i++) {
	    for (int j=0;j<LayerSize.get(i);j++) {
		Neuron n = Layers[i].get(j);
	    	
	
		Iterator<Entry<Neuron,Double>> e = n.Successors.entrySet().iterator();
	
		while (e.hasNext()) {
		    Entry<Neuron,Double> entr = e.next();	    

		    long nID = n.getID();
		    long entrID = entr.getKey().getID();
		    double value = entr.getValue();
		    
		    try {
			writer.write("node" + nID + " -> ");
			writer.write("node" + entrID );
			writer.write("[ label=\" " + value  + "\" ];\n");		    
		    }catch (Exception e2) {
			System.out.println("Fehler->" + e2.toString());
		    }	
		    
		    if (BiasNeuron.Successors.containsKey(n)) {
		    
			double bvalue = BiasNeuron.Successors.get(n);
		    
			try {
			    writer.write("BIAS -> ");		
			    writer.write("node" + nID );
			    writer.write("[ label=\" " + bvalue + "\" ];\n");
			    writer.flush();
			}catch (Exception e3) {
			    System.out.println("Fehler->" + e3.toString());
			}
		    }
		}
	    }	    
	}
	try {
	    writer.write("}\n");	
	    writer.flush();
	    writer.close();
	}catch (Exception e) {
	    System.out.println("Fehler->" + e.toString());
	}
    }
    
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
	RunOffNeurons = new InputNeuron[1];
	
	LayerSize = new Vector<Integer>();
	LayerSize.setSize(LayerCount);
	
	LayerSize.set(0,RelevantTime*(iPrecipStationCount+iTempStationCount)+1);
	LayerSize.set(1,(int)(RelevantTime*(iPrecipStationCount+iTempStationCount)+1));
//	LayerSize.set(2,RelevantTime*(iPrecipStationCount+iTempStationCount));
//	LayerSize.set(3,500);
	LayerSize.set(2,1);
	
	for (int i=0;i<LayerCount;i++) {
	    Layers[i] = new Vector<Neuron>();
	    Layers[i].setSize(LayerSize.get(i));
	}
			
	outNeuron.initalize();
	outNeuron.setID(0);

	Layers[LayerCount-1].set(0, outNeuron);
	
	BiasNeuron.initalize();
	BiasNeuron.setID(1);
	BiasNeuron.SetInput(1.0);
			
	int ID = 1000;
	int Layer0ID = 0;
	
	//setup layer 0
	for (int i=0;i<iPrecipStationCount;i++) {
	    for (int j=0;j<RelevantTime;j++) {
		InputNeuron precipNeuron = new InputNeuron();
			    		
		precipNeuron.setID(ID++);
		precipNeuron.initalize();
	   	    								
		Layers[0].set(Layer0ID,precipNeuron);
		Layer0ID++;		
		
		PrecipNeurons[j][i] = precipNeuron;				
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
	    }	    
	}

	InputNeuron obsRunoffNeuron   = new InputNeuron();		
	    	    
	obsRunoffNeuron.setID(ID++);
	obsRunoffNeuron.initalize();
	    								
	Layers[0].set(Layer0ID,obsRunoffNeuron);
	Layer0ID++;
		
	RunOffNeurons[0] = obsRunoffNeuron;	 
	
	for (int m=1;m<LayerCount-1;m++) {	
	    for (int i=0;i<LayerSize.get(m);i++) {
		Neuron innerNeuron  = new Neuron();

		
		GenFunction genf = new GenFunction(generator.nextDouble()*1.0,generator.nextDouble()*1.0,generator.nextDouble()*1.0);		
		LogisticFunction logf = new LogisticFunction(generator.nextDouble()*0.1);		
		
		GenericFunction gf=null;
		
		if (m == 1)
		    gf = new GenericFunction(logf);
/*		if (m == 1)		
		    gf = new GenericFunction(genf);*/
		
		innerNeuron.setID(ID++);
		innerNeuron.initalize();
		innerNeuron.addFilter(gf);		
		
		Layers[m].set(i,innerNeuron);
		
		for (int k=0;k<Layers[m-1].size();k++) {
		    if (this.generator.nextDouble() >= 0.0)
			innerNeuron.AddConnection(Layers[m-1].get(k),innerNeuron,generator.nextDouble()-0.5);
		}
		
		if (m == LayerCount-2)
		    innerNeuron.AddConnection(innerNeuron,outNeuron,generator.nextDouble()-0.5);
		
		BiasNeuron.AddConnection(BiasNeuron,innerNeuron,generator.nextDouble()-0.5);
	    }
	}
	
	//writeGraphFile("E:\\test.dot");
    }

    public void SendDataToNet(int p) {
	for (int i=0;i<iTempStationCount;i++) {
	    for (int j=0;j<RelevantTime;j++) {
		TempNeurons[j][i].SetInput(TempData.get(j+p)[i]);		
	    }
	}	
	
	for (int i=0;i<iPrecipStationCount;i++) {
	    for (int j=0;j<RelevantTime;j++) {
		PrecipNeurons[j][i].SetInput(PrecipData.get(j+p)[i]);
	    }
	}	
	
	
	RunOffNeurons[0].SetInput(RunOffData.get(RelevantTime+p-1)[0]);	 
    }
    
    public double Propagate() {
	for (int k=0;k<LayerCount;k++) {
	    for (int i=0;i<Layers[k].size();i++) {
		Layers[k].get(i).propagate();
		}
	    }		
	//outNeuron.propagate();
	
	return outNeuron.getActivation();
    }
    
    public void BackPropagate(double error) {
	outNeuron.addToError(error);
			
	outNeuron.backpropagate();
	outNeuron.adjustWeight();
	    
	for (int k=LayerCount-2;k>=0;k--) {
	    for (int i=0;i<Layers[k].size();i++) {
		Layers[k].get(i).backpropagate();
		if (k != 0)
		    Layers[k].get(i).adjustWeight();
	    }
	}	
    }
    
    public double TrainCycle(int start) {
	double accError = 0;
	
	//lege nacheinander eingaben an	    
	boolean hak[] = new boolean[TRAININGLENGTH];
	    
	for (int p=0;p<TRAININGLENGTH;p++) {
	    hak[p] = true;
	}
	    
	for (int p=start;p<start+TRAININGLENGTH;p++) {								
	    int rWert = generator.nextInt(TRAININGLENGTH);
	    int t = 0;
	    int c = 0;
	    while (t<=rWert) {
	        while (hak[c] == false) {
		    c++;
		    if (c >= TRAININGLENGTH) {
			c = 0;
		    }
		}
		t++;
	    }
		
	    int pstrich =  start + c;
	    hak[c] = false;
		
	    SendDataToNet(pstrich);
	    Propagate();
				
	    double runoff = RunOffData.get(pstrich+RelevantTime+FORECAST-1)[0];
	    
	    //System.out.println(runoff-outNeuron.getActivation());	    
	    
	    if (Math.abs(runoff-outNeuron.getActivation()) > accError)
		accError = Math.abs(runoff-outNeuron.getActivation());
	    //accError += Math.abs(runoff-outNeuron.getActivation());
	    
	    BackPropagate(runoff-outNeuron.getActivation());							
	}    
    return accError;
    }
       
    public int Train(int start) {
	//lernen ... 
	double Error = 1000;
	double lastError = 1000;
		
	int iterationCount = 0;
	
	while (Error > 0.05) {	    

	    iterationCount++;

	    lastError = TrainCycle(start);
	    Error = lastError;
	    	    
	    System.out.println("Commited with = " + Error);	    	    
	}
	return iterationCount;
    }
    
    public void run() throws JAMSEntity.NoSuchAttributeException {                	
	BufferedWriter writer = null;
			
	try {	
	writer = new BufferedWriter(new FileWriter("E:\\ergebnisse.txt"));
	} catch (Exception e) {
	    System.out.println("Fehler Datei konnte nicht geöffnet werden!");
	}
	//setupNET();
	
	for (int T=RUNSTART;T<RUNEND;T++) {
	    //sinnvoller?
	    setupNET();
	    
	    int iterationCount = Train(T);
	    
	    //lege testdatensatz an
	    SendDataToNet(T+TRAININGLENGTH);

	    Propagate();
	    
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
	    System.out.println("Fehler beim schließen");
	}
    }
}

