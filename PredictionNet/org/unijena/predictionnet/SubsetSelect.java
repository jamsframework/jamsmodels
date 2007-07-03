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
 * @author Christian(web)
 */
public class SubsetSelect extends JAMSContext {
        
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "TimeSerie of Temp Data"
            )
            public JAMSInteger n;
           
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "TimeSerie of Temp Data"
            )
            public JAMSEntity InputData;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "TimeSerie of Temp Data"
            )
            public JAMSEntity OutputData;
    
     @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "TimeSerie of Temp Data"
            )
            public JAMSBoolean active;
     
    public SubsetSelect() {
	 
    }
     
    public void init() {  
     
    }
     
          
    public void run() {  
	double data[][] = null;
	double predict[] = null;
	try {
	     data = (double[][])InputData.getObject("data");
	     predict = (double[])InputData.getObject("predict");
	}
	catch(Exception e) {
	    System.out.println("Konnte InputData nicht finden" + e.toString());
	}
	
	if (active.getValue() == false) {
	    OutputData.setObject("data",data);
	    OutputData.setObject("predict",predict);
	    return;
	}
	
	System.out.println("Optimiere Trainingsdaten!");
	
	int Ngoal = n.getValue();
	int N0 = data.length;
	int M = data[0].length;
		
	int set[] = new int[Ngoal];
	
	double variance[] = new double[N0];
	boolean inSet[] = new boolean[N0];
	for (int j=0;j<N0;j++) {
	    variance[j] = 0.0;
	    inSet[j] = false;
	}
	inSet[0] = true;
	set[0] = 0;
	
	for (int i=1;i<Ngoal;i++) {
	    int bestIndex = -1;
	    double bestValue = -1.0;
	    
	    for (int j=0;j<N0;j++) {		
		if (inSet[j])
		    continue;
		for (int l=0;l<M;l++) {
		    variance[j] += (data[j][l] - data[set[i-1]][l])*(data[j][l] - data[set[i-1]][l]);
		}
		if (variance[j] > bestValue) {
		    bestValue = variance[j];
		    bestIndex = j;
		}
	    }
	    inSet[bestIndex] = true;
	    set[i] = bestIndex;	 	    
	}
	
	double optimizedata[][] = new double[Ngoal][];
	double optimizepredict[] = new double[Ngoal];
	
	for (int i=0;i<Ngoal;i++) {
	    optimizedata[i] = data[set[i]];
	    optimizepredict[i] = predict[set[i]];
	}
	
	OutputData.setObject("data",optimizedata);
	OutputData.setObject("predict",optimizepredict);
    }
}
