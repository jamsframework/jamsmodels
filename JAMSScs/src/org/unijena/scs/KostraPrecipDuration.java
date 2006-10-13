/*
 * KostraPrecipDuration.java
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

package org.unijena.scs;

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
        title="KostraPrecipDuration",
        author="Peter Krause",
        description="Provides a set of precip durations"
        )
        public class KostraPrecipDuration extends JAMSContext {
    
    /*
     *  Component variables
     */
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.INIT,
            description = "Data file directory name"
            )
            public JAMSString dirName;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.INIT,
            description = "Representation of precip duration inside the model"
            )
            public JAMSString parameterName;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.INIT,
            description = "The set of precip durations"
            )
            public JAMSIntegerArray kostraValues;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.INIT,
            description = "Flag for enabling this sampler"
            )
            public JAMSBoolean enable;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.INIT
            )
            public JAMSString paraFileName;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.INIT
            )
            public JAMSString attribFileName;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.INIT,
            description = "The model time interval"
            )
            public JAMSTimeInterval modelTimeInterval;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.INIT,
            description = "Output file header descriptions"
            )
            public JAMSString attribHeader;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "Output file attribute"
            )
            public JAMSDoubleArray targetValue;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "Legend entry"
            )
            public JAMSString legendEntry;
    
    JAMSDouble parameter;
    
    
    int currentCount;
    GenericDataWriter paraWriter;
    GenericDataWriter attribWriter;
    double[][] valueArray;
    int timeStepCounter = 0;
    int runCounter = 0;
    int timeSteps = 0;
    
    int currentStep = 0;
    //double[] currentVal;
    
    int sampleCount = 0;
    
    private boolean hasNext() {
        int sampleCount = this.kostraValues.getValue().length;
        return currentCount < sampleCount;
    }
    public void init() {
        if(this.enable.getValue()){
            //add more checks!!!
            //retreiving parameter names
            String para = this.parameterName.getValue();
            parameter = (JAMSDouble) getModel().getRuntime().getDataHandles().get(para);
                        
            //create para output file
            paraWriter = new GenericDataWriter(dirName.getValue()+"/"+this.paraFileName.getValue());
            paraWriter.addColumn("Run");
            
            paraWriter.addColumn(para);
            
            
            
            paraWriter.writeHeader();
            
            //the attribute output file
            attribWriter = new GenericDataWriter(dirName.getValue()+"/"+attribFileName.getValue());
            
            attribWriter.addComment("J2K model output");
            attribWriter.addComment("");
            
            //always write time
            attribWriter.addColumn("date/time");
            sampleCount = this.kostraValues.getValue().length;
            for(int s = 0; s < this.sampleCount; s++){
                int counter = s + 1;
                attribWriter.addColumn(attribHeader.getValue() + "_run_" + counter);
            }
            
            
            attribWriter.writeHeader();
            
            //setting up the dataArray
            this.timeSteps = (int)modelTimeInterval.getNumberOfTimesteps();
            this.valueArray = new double[sampleCount][timeSteps];
            this.timeStepCounter = 0;
            this.runCounter = 0;
            
            //determine x and y stepSize
            this.parameter.setValue(this.kostraValues.getValue()[0]);
            this.currentStep = 0;
        }
    }
    
    private void updateValues() {
        int sampleCount = this.kostraValues.getValue().length;
        int count = this.currentCount + 1;
        getModel().getRuntime().println("Run No. " + count + " of " + sampleCount);
        //double[] sample = this.regularSampler();
        if(currentCount > 0){
            if(this.currentStep < this.kostraValues.getValue().length){
                this.parameter.setValue(this.kostraValues.getValue()[currentCount]);
            } 
        }
        String lE = "Niederschlagsdauer " + (int)parameter.getValue() + " sec";
        this.legendEntry.setValue(lE);
        getModel().getRuntime().println("Para: " + this.parameterName.getValue() + " = " + parameter);
        currentCount++;
        this.currentStep++;
    }
    
    
    
    
    
    private void resetValues() {
        //set parameter values to initial values corresponding to their boundaries
        parameter.setValue(this.kostraValues.getValue()[0]);
        
        currentCount = 0;
        
        
    }
    
    private void singleRun() {
        
        System.gc();
        long start = System.currentTimeMillis();
        
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
        long end = System.currentTimeMillis();
        getModel().getRuntime().println("Exec time: " + (end-start) + " ms", JAMS.STANDARD);
    }
    
    public void run() {
        
        if (runEnumerator == null) {
            runEnumerator = getChildrenEnumerator();
        }
        
        if (!enable.getValue()) {
            singleRun();
        } else {
            resetValues();
            while (hasNext()) {
                updateValues();
                singleRun();
                
                paraWriter.addData(currentCount);
                
                paraWriter.addData(this.parameter.getValue());
                try{
                    paraWriter.writeData();
                    paraWriter.flush();
                }catch(org.unijena.jams.runtime.JAMSRuntimeException e){
                    
                }
                
                this.valueArray[runCounter] = this.targetValue.getValue();
                this.runCounter++;
                
            }
            
            runEnumerator.reset();
            while(runEnumerator.hasNext() && doRun) {
                runEnumerator.next();
            }
        }
        
    }

    public void cleanup() {
        if (enable.getValue()) {
            int sampleCount = this.kostraValues.getValue().length;
            JAMSCalendar timeStamp = this.modelTimeInterval.getStart();
            for(int t = 0; t < this.timeSteps; t++){
                attribWriter.addData(timeStamp.toString("%1$tH:%1$tM:%1$tS"));
                timeStamp.add(modelTimeInterval.getTimeUnit(), modelTimeInterval.getTimeUnitCount());
                for(int r = 0; r < sampleCount; r++){
                    attribWriter.addData(this.valueArray[r][t]);
                }
                try {
                    attribWriter.writeData();
                } catch (org.unijena.jams.runtime.JAMSRuntimeException jre) {
                    getModel().getRuntime().println(jre.getMessage());
                }
            }
            attribWriter.close();
            paraWriter.close();
        }
    }
    
}
