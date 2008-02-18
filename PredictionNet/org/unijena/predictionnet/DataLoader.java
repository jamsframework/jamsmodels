/*
 * DataLoader.java
 *
 * Created on 3. Juli 2007, 12:01
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
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
 * @author Christian(web)
 */
public class DataLoader extends JAMSComponent {
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.INIT,
            description = "TimeSerie of Precip Data"
            )
             public JAMSString datafile;
                
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.INIT,
            description = "TimeSerie of Temp Data"
            )
             public JAMSInteger ExampleLength;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.INIT,
            description = "TimeSerie of Temp Data"
            )
             public JAMSInteger numOfExamples;
                                      
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.INIT,
            description = "TimeSerie of Temp Data"
            )
            public JAMSInteger relevantTime;
                                    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            update = JAMSVarDescription.UpdateType.INIT,
            description = "TimeSerie of Temp Data"
            )
            public JAMSEntity Data;
            
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "TimeSerie of Temp Data"
            )
            public JAMSInteger dataShift;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "TimeSerie of Temp Data"
            )
            public JAMSString Gaps;
    
    public DataLoader() {        
    }
    
    public void init() throws JAMSEntity.NoSuchAttributeException {  
	BufferedReader reader = null;
	HashMap<Integer, double[]> rawData = new HashMap<Integer,double[]>();
	HashMap<Integer, double[]> rawPredict = new HashMap<Integer,double[]>();
	Vector<Integer> excluded = new Vector<Integer>();

        if (Gaps != null){
            StringTokenizer GapTokenizer = new StringTokenizer(Gaps.getValue(),";");
            while(GapTokenizer.hasMoreElements()){
                excluded.add(new Integer(GapTokenizer.nextToken()));
            }
        }
	int ExamplLength = ExampleLength.getValue();
	int numOfExampl  = numOfExamples.getValue();
	int RelevantTime = this.relevantTime.getValue();
	
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

        Vector<double[]> data = new Vector();
        Vector<Double> predict = new Vector();       
        
	/*double data[][] = new double[numOfExampl][RelevantTime*ExamplLength];
	double predict[] = new double[numOfExampl];*/
	
        boolean isExcluded = false;
	for (int i=0;i<numOfExampl;i++) {
            isExcluded = false;
             double Sample[] = new double[RelevantTime*ExamplLength];
	    for (int j=0;j<RelevantTime;j++) {                
                if (excluded.contains(new Integer(i+j))){
                    isExcluded = true;
                    break;
                }                
		double entry[] = rawData.get(new Integer(i+j));
		for (int k=0;k<ExamplLength;k++) {
		    Sample[j*ExamplLength+k] = entry[k];
		}
                
	    }
            if (isExcluded)
                continue;
	    if (i+RelevantTime-1+dataShift.getValue() < 0) {
		System.out.println("Warning: Dataset: " + i + "contains no prediction!!");
		continue;
	    }
            if (excluded.contains(new Integer(i+RelevantTime-1+dataShift.getValue()))){
                isExcluded = true;
                continue;
            } 
            data.add(Sample);
	    predict.add(rawPredict.get(new Integer(i+RelevantTime-1+dataShift.getValue()))[0]);
	}
	double dataAsArray[][] = new double[data.size()][RelevantTime*ExamplLength];
        double predictAsArray[] = new double[data.size()];
        for (int i=0;i<data.size();i++){
            dataAsArray[i] = data.get(i);
            predictAsArray[i] = predict.get(i);
        }
	Data.setObject("data",dataAsArray);
	Data.setObject("predict",predictAsArray);
    }    
}
