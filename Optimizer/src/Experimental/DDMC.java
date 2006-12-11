/*
 * DDMC.java
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

import java.util.*;
import org.unijena.jams.JAMS;
import org.unijena.jams.data.*;
import org.unijena.jams.io.GenericDataWriter;
import org.unijena.jams.model.*;
import java.io.*;

/**
 *
 * @author Christian Fischer
 */
@JAMSComponentDescription(
        title="GradientDescent",
        author="Christian Fischer",
        description="distance driven monte carlo optimization with gradient descent"
        )
        public class DDMC extends JAMSContext {
    
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
            description = "efficiency methods"
            )
            public JAMSString effMethodName;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "efficiency values"
            )
            public JAMSDouble[] effValue;   
        
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "points visited by gradient descent"
            )
            public JAMSEntity visitedPoints;  
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "efficiency values"
            )
            public JAMSDoubleArray paramBestPoint = new JAMSDoubleArray();;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "efficiency values"
            )
            public JAMSDouble paramBestValue;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "efficiency values"
            )
            public JAMSDoubleArray parametersTransfer = new JAMSDoubleArray();
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "result file"
            )
            public JAMSString resultFile;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "workspace directory"
            )
            public JAMSString dirName;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "result file"
            )
            public JAMSInteger MonteCarloParameter;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "minimal distance"
            )
            public JAMSDouble MinimalDistance;
    
    String[] parameterNames;
    double parameters[] = null;
    int pLen = 0;
    
    double[] lowBound;
    double[] upBound;
            
    double [][] pointList;
    
    //best parameter which has been found
    double [] bestpoint;
    double bestvalue;
    
    Random generator;    
    BufferedWriter writer;
    
    public void init() {
        generator = new Random();
	generator.setSeed(System.currentTimeMillis());
//add more checks!!!
        //retreiving parameter names
        int i;
        StringTokenizer tok = new StringTokenizer(parameterIDs.getValue(), ";");
        String key;
        parameters = new double[tok.countTokens()];
        parameterNames = new String[tok.countTokens()];
        
        i = 0;
        while (tok.hasMoreTokens()) {
            key = tok.nextToken();
            parameterNames[i] = key;
            parameters[i] = ((JAMSDouble)getModel().getRuntime().getDataHandles().get(key)).getValue();
            i++;
        }
        
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
        
        //retreiving effMethodNames
        i = 0;
        tok = new StringTokenizer(effMethodName.getValue(), ";");
        String[] effNames = new String[tok.countTokens()];
        i = 0;
        while (tok.hasMoreTokens()) {
            key = tok.nextToken();
            effNames[i] = key;
            i++;
        }      
	
	if (effNames.length != 1) {
	     getModel().getRuntime().sendHalt("Cant process multiobjective optimization problems!!");
	}
	
	pLen = this.parameters.length;
	pointList = null;
	
	this.bestvalue = -10000000.0;
    }
    
    private double[] RandomSampler(){
        int paras = this.parameterNames.length;        
        double[] sample = new double[paras];
        	
        for(int i = 0; i < paras; i++)	{
            double d = generator.nextDouble();
            // all other parameters
            sample[i] = (lowBound[i] + d * (upBound[i]-lowBound[i]));
        }     
        return sample;
    }
    
    private boolean hasNext() {
	return true;
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
        
    private boolean IsSampleValid(JAMSDouble [] sample) {
	int paras = this.parameterNames.length;
        boolean criticalPara = false;
        double criticalParaValue = 0; 
        	
        for(int i = 0; i < paras; i++){          
	    // all other parameters
	    if (sample[i].getValue() < lowBound[i] || sample[i].getValue() > upBound[i] )
	        return false;
        }     
        return true;
    }
    //calc distance to all other points
    private double calcDist(double x[]) {
	if (this.pointList == null) {
	    return 100000000.0;
	}
	double dist = 0;
	double mindist = 10000000000.0;
	
	
	for (int i=0;i<pointList.length;i++) {
	    dist = 0;
	    for (int j=0;j<pLen;j++) {
		dist += (pointList[i][j] - x[j])*(pointList[i][j] - x[j]);
	    }	    
	    if (dist < mindist) {
		mindist = dist;
	    }
	}
	return mindist;
    }
    //merge new visited point with old visited points
    private void SavePoints() {
	HashMap<Integer, double[]> AddList = null;
	
	try {
	    AddList = (HashMap<Integer, double[]>)this.visitedPoints.getObject("Hash");
	}
	catch(Exception e) {
	    System.out.println(e.toString());
	    return;
	}
	
	double update[][] = null;
	
	if (pointList == null) {
	   update = new double[AddList.size()][pLen]; 
	}
	else {
	    update = new double[AddList.size()+pointList.length][pLen];
	}
	
	int counter = 0;
	for (int i=0;i<AddList.size();i++) {	    
	    update[counter] = AddList.get(i);
	    counter++;
	}
	
	if (pointList != null) {
	    for (int i=0;i<pointList.length;i++) {	    
		update[counter] = pointList[i];
		counter++;
	    }
	}
	
	pointList = update;
    }
    
    public void run() {	
	String output;
	
	//try to open output file
	try {
	    writer = new BufferedWriter(new FileWriter(this.dirName.getValue() + resultFile.getValue()));
	}
	catch (Exception e) {
	    System.out.println("Could not open result file, becauce:" + e.toString());
	}
			
        if (runEnumerator == null) {
            runEnumerator = getChildrenEnumerator();
        }
	
	while (hasNext()) {
	    double x[] = null;
	    double distance = 0.0;
	    //generate n new samples
	    for (int i=0;i<MonteCarloParameter.getValue();i++) {
		double mc_point[] = RandomSampler();
		
		//calculate distance to other samples
		double istanceOfMCPoint = calcDist(mc_point);
		if (istanceOfMCPoint > distance) {
		    x = mc_point;
		    distance = istanceOfMCPoint;
		}
	    }
	    	    	    
	    output = "current minimal distance:" + distance;
	    
	    getModel().getRuntime().println(output);
	    try {
		writer.newLine();
		writer.write(output);
		writer.newLine();
	    }
	    catch(Exception e) {
		System.out.println("Could not write to output file because:" + e.toString());
	    }
	    
	    if (distance < MinimalDistance.getValue()) {
		output = "optimization stopped because: parameterspace has been sampled";
		getModel().getRuntime().println(output);
		try {
		    writer.write(output);
		    writer.newLine();
		}
		catch(Exception e) {
		    System.out.println("Could not write to output file because:" + e.toString());
		}
		return;
	    }
	    
	    for (int i=0; i < this.parameters.length; i++) {	
		parameters[i] = x[i];    	    		
	    }
	   
	    this.parametersTransfer.setValue(parameters);
	    
	    singleRun();

	    if (this.bestvalue < this.paramBestValue.getValue()) {
		this.bestvalue = this.paramBestValue.getValue();
		this.bestpoint = this.paramBestPoint.getValue();
		output = "A new best point has been found! Value: " + this.bestvalue;
		getModel().getRuntime().println(output);
		
		try {
		    writer.write(output);
		    writer.newLine();		    
		}
		catch(Exception e) {
		    System.out.println("Could not write to output file because:" + e.toString());
		}
		
		output = "Parameters: ";
		
		for (int k=0;k<this.bestpoint.length;k++) {
		    output += this.bestpoint[k] + ",";
		    
		}
		getModel().getRuntime().println(output);
		
		try {
		    writer.write(output);
		    writer.newLine();
		    writer.flush();
		}
		catch(Exception e) {
		    System.out.println("Could not write to output file because:" + e.toString());
		}
	    }
	    SavePoints();
	}
	
	try {
	
	output = "Result of Optimization: Value: " + this.bestvalue;
	getModel().getRuntime().println(output);
	writer.write(output);
	writer.newLine();		    
		
	output = "Parameters: ";
		
	for (int k=0;k<this.bestpoint.length;k++) {
	    output += this.bestpoint[k] + ",";		    
	}
	getModel().getRuntime().println(output);
		
	writer.write(output);
	writer.newLine();
	writer.flush();
    
	writer.close();
	}
	catch (Exception e) {
	    System.out.println("Could not close output file because:" + e.toString());
	}
    }               
}
