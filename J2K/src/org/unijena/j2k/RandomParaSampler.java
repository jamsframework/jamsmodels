/*
 * RandomParaSampler.java
 * Created on 10. Mai 2006, 17:03
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

package org.unijena.j2k;

import java.util.Random;
import java.util.StringTokenizer;
import org.unijena.jams.JAMS;
import org.unijena.jams.data.*;
import org.unijena.jams.io.GenericDataWriter;
import org.unijena.jams.model.*;

/**
 *
 * @author nsk
 */
@JAMSComponentDescription(
        title="Title",
        author="Author",
        description="Description"
        )
        public class RandomParaSampler extends JAMSContext {
    
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
            description = "Number of samples to be taken"
            )
            public JAMSInteger sampleCount;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "efficiency criteria"
            )
            public JAMSDouble[] effMethod;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.INIT,
            description = "Flag for disabling this sampler"
            )
            public JAMSBoolean disable;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.INIT
            )
            public JAMSString fileName;
    
    JAMSDouble[] parameters;
    String[] parameterNames;
    double[] lowBound;
    double[] upBound;
    int currentCount;
    Random generator;
    double bestGoodness = Double.MAX_VALUE;
    double bestMinimum = Double.MAX_VALUE;
    double[] bestValues;
    GenericDataWriter writer;
    
    
    private boolean hasNext() {
        return currentCount < sampleCount.getValue();
    }
    
    private void updateValues() {
        getModel().getRuntime().println("Run No. " + this.currentCount + " of " + this.sampleCount.getValue());
        double[] sample = this.randomSampler(parameters.length);
                
        for (int i = 0; i < parameters.length; i++) {
            //System.out.println("Parameter: " + this.parameterIDs.getValue());
            //double d = generator.nextDouble();
            parameters[i].setValue(sample[i]);//lowBound[i] + d * (upBound[i]-lowBound[i]));
            getModel().getRuntime().println("Para: " + parameterNames[i] + " = " + sample[i]);
        }
        
        currentCount++;
    }
    
    private double[] randomSampler(int nSamples){
        double[] sample = new double[nSamples];
        for(int i = 0; i < nSamples; i++){
            double d = generator.nextDouble();
            sample[i] = (lowBound[i] + d * (upBound[i]-lowBound[i]));
        }
        return sample;
    }
    
    
    
    private void resetValues() {
        //set parameter values to initial values corresponding to their boundaries
        generator = new Random(System.currentTimeMillis());
        for (int i = 0; i < parameters.length; i++) {
            double d = generator.nextDouble();
            parameters[i].setValue(lowBound[i] + d * (upBound[i]-lowBound[i]));
        }
        currentCount = 0;
    }
    
    private void singleRun() {
        
        runEnumerator.reset();
        while(runEnumerator.hasNext() && doRun) {
            JAMSComponent comp = runEnumerator.next();
            //comp.updateInit();
            try {
                comp.init();
            } catch (Exception e) {
                
            }
        }
        
        
        runEnumerator.reset();
        while(runEnumerator.hasNext() && doRun) {
            JAMSComponent comp = runEnumerator.next();
            //comp.updateRun();
            try {
                comp.run();
            } catch (Exception e) {
                
            }
        }
        
        runEnumerator.reset();
        while(runEnumerator.hasNext() && doRun) {
            JAMSComponent comp = runEnumerator.next();
            try {
                comp.cleanup();
            } catch (Exception e) {
                
            }
        }
    }
    
    public void run() {
        /*double[] effValues = new double[this.effMethod.length];//
        for(int i = 0; i < effValues.length; i++)
            effValues[i] = this.effMethod[i].getValue();*/
        if (runEnumerator == null) {
            runEnumerator = getChildrenEnumerator();
        }
        
        if (disable.getValue()) {
            
            singleRun();
            
        } else {
            
            resetValues();
            
            while (hasNext()) {
                
                updateValues();
                
                singleRun();
                
                //JAMS.sendInfoMsg("Goodness: " + goodness);
                //write outputFile
                writer.addData(currentCount);
                for(int i = 0; i < this.parameters.length; i++)
                    writer.addData(this.parameters[i].getValue());
                for(int e = 0; e < effMethod.length; e++)
                    writer.addData(this.effMethod[e].getValue());
                try{
                    writer.writeData();
                }catch(org.unijena.jams.runtime.JAMSRuntimeException e){
                    
                }
                /*
                double minimumCrit = Math.abs(1 - goodness.getValue());
                
                if(minimumCrit < bestMinimum){
                    //if (goodness.getValue() > bestGoodness) {
                    bestMinimum = Math.abs(1 - goodness.getValue());
                    bestGoodness = goodness.getValue();
                    for (int i = 0; i < parameters.length; i++) {
                        bestValues[i] = parameters[i].getValue();
                    }
                    
                }*/
            }
            
            runEnumerator.reset();
            while(runEnumerator.hasNext() && doRun) {
                JAMSComponent comp = runEnumerator.next();
                try {
                    System.out.println("comp cleanup from parasampler");
                    //comp.cleanup();
                } catch (Exception e) {
                    //JAMS.handle(e, comp.getInstanceName());
                }
            }
            
            //System.out.println("Goodness: " + goodness);
        }
    }
    
    
    public void init() {
        
//add more checks!!!
        int i;
        StringTokenizer tok = new StringTokenizer(parameterIDs.getValue(), ";");
        String key;
        parameters = new JAMSDouble[tok.countTokens()];
        parameterNames = new String[tok.countTokens()];
        
        i = 0;
        while (tok.hasMoreTokens()) {
            key = tok.nextToken();
            parameterNames[i] = key;
            parameters[i++] = (JAMSDouble) getModel().getRuntime().getDataHandles().get(key);
        }
        
        tok = new StringTokenizer(boundaries.getValue(), ";");
        int n = tok.countTokens();
        lowBound = new double[n];
        upBound = new double[n];
        bestValues = new double[n];
        
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
        
        //create output file
        writer = new GenericDataWriter(this.fileName.getValue());
        writer.addColumn("Run");
        
        for(int j = 0; j < this.parameters.length; j++)
            writer.addColumn("para_" + j);
        
        writer.addColumn("e2");
        writer.addColumn("le2");
        
        writer.writeHeader();
        
    }
    
    public void cleanup() {
        if (!disable.getValue()) {
            /*System.out.println("overall max. goodness: " + bestGoodness);
            for (int i = 0; i < parameters.length; i++) {
                System.out.println("value["+i+"]: " + bestValues[i]);
            }*/
            writer.close();
        }
    }
    
}
