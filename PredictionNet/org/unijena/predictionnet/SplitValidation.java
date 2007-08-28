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
public class SplitValidation extends JAMSContext {
        
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "TimeSerie of Temp Data"
            )
            public JAMSInteger trainingStart;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "TimeSerie of Temp Data"
            )
            public JAMSInteger trainingEnd;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "TimeSerie of Temp Data"
            )
            public JAMSInteger validationStart;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "TimeSerie of Temp Data"
            )
            public JAMSInteger validationEnd;
           
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "TimeSerie of Temp Data"
            )
            public JAMSEntity Data;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "TimeSerie of Temp Data"
            )
            public JAMSEntity trainingData;
    
     @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "TimeSerie of Temp Data"
            )
            public JAMSEntity validationData;
     
     public SplitValidation() {
	 
     }
     
     public void init() {  
     
     }
     
     private void singleRun() {    
	 if (runEnumerator == null) {
            runEnumerator = getChildrenEnumerator();
        }
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
     
    public void run() {  
	double data[][] = null;
	double predict[] = null;
	try {
	    data =(double[][])Data.getObject("data");
	    predict = (double[])Data.getObject("predict");
	}
	catch(Exception e) {
	    System.out.println("Konnte Daten nicht finden!!" + e.toString());
	}	
	
	System.out.println("Split Validation");
	
	//split up data	    	    	    
	double valData[][] = new double[validationEnd.getValue()-validationStart.getValue()][];
	double valPredict[] = new double[validationEnd.getValue()-validationStart.getValue()]; 
	double trainData[][] = new double[trainingEnd.getValue()-trainingStart.getValue()][];	    	    
	double trainPredict[] = new double[trainingEnd.getValue()-trainingStart.getValue()]; 
	 
	for (int j=0;j<data.length;j++) {
	    if ( j >= validationStart.getValue() && j < validationEnd.getValue()) {
	        valData[j - validationStart.getValue()] = data[j];
	        valPredict[j - validationStart.getValue()] = predict[j];
	    }
	
	    if ( j >= trainingStart.getValue() && j < trainingEnd.getValue()) {
	        trainData[j - trainingStart.getValue()] = data[j];
	        trainPredict[j - trainingStart.getValue()] = predict[j];
	    }
	}	 
	    
	trainingData.setObject("data",trainData);
	trainingData.setObject("predict",trainPredict);
	    
	validationData.setObject("data",valData);
	validationData.setObject("predict",valPredict);
	    
	singleRun();
    }
}
