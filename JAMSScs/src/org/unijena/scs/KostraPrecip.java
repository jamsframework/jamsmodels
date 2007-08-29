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

import java.util.Locale;
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
        public class KostraPrecip extends JAMSContext {
    
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
            access = JAMSVarDescription.AccessType.READWRITE,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "current precip duration"
            )
            public JAMSString precipDurationName;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "current precip height"
            )
            public JAMSString precipHeightName;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "peak runoff"
            )
            public JAMSDouble maxRunoff;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "cumulated volume of all runoff"
            )
            public JAMSDouble cumVolume;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.INIT,
            description = "The set of precip durations"
            )
            public JAMSIntegerArray kostraValues;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.INIT,
            description = "Flag for enabling this component"
            )
            public JAMSBoolean enable;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.INIT
            )
            public JAMSString kostraSummaryFile;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.INIT
            )
            public JAMSString kostraDetailFile;
    
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
            access = JAMSVarDescription.AccessType.READWRITE,
            update = JAMSVarDescription.UpdateType.INIT,
            description = "Legend entry"
            )
            public JAMSString legendEntry;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.INIT,
            description = "the kostra table object as input"
            )
            public JAMSEntity kostraTable;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            update = JAMSVarDescription.UpdateType.INIT,
            description = "precip duration"
            )
            public JAMSDouble precipDuration;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.INIT,
            description = "precip annualtiy"
            )
            public JAMSDouble precipAnnuality;
    
    
    //JAMSDouble pDuration;
    JAMSDouble pIn;
    double[] inPrecip;
    String[] legEntries;        
    int currentCount;
    GenericDataWriter summaryWriter;
    GenericDataWriter detailWriter;
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
            String para = this.precipDurationName.getValue();
            this.precipDuration = (JAMSDouble) getModel().getRuntime().getDataHandles().get(para);
            this.precipDuration.setValue(this.precipDuration.getValue() * 60);
            String para2 = this.precipHeightName.getValue();
            pIn = (JAMSDouble)getModel().getRuntime().getDataHandles().get(para2);
            
            //create para output file
            summaryWriter = new GenericDataWriter(dirName.getValue()+"/"+this.kostraSummaryFile.getValue());
            try {
                summaryWriter.writeLine("Wiederkehrintervall T = " + this.precipAnnuality.getValue() + " a");
            } catch (org.unijena.jams.runtime.RuntimeException ex) {
                ex.printStackTrace();
            }
            summaryWriter.addColumn("Dauerstufe D [min]");
            summaryWriter.addColumn("Niederschlag [mm]");
            summaryWriter.addColumn("Scheitelabfluss [m³/s]");
            summaryWriter.addColumn("Wellenvolumen [Mio m³]");
            summaryWriter.writeHeader();
            
            //the attribute output file
            detailWriter = new GenericDataWriter(dirName.getValue()+"/"+this.kostraDetailFile.getValue());
            sampleCount = this.kostraValues.getValue().length;
            this.legEntries = new String[this.kostraValues.getValue().length];
            
            
            //setting up the dataArray
            this.timeSteps = (int)modelTimeInterval.getNumberOfTimesteps();
            this.valueArray = new double[sampleCount][timeSteps];
            this.timeStepCounter = 0;
            this.runCounter = 0;
            
            //determine x and y stepSize
            this.precipDuration.setValue(this.kostraValues.getValue()[0] * 60);
            this.currentStep = 0;
            try {
                JAMSDouble[] hA = (JAMSDouble[]) kostraTable.getObject("HeaderA");
                //precip duration
                JAMSDouble[] hD = (JAMSDouble[])kostraTable.getObject("HeaderD");
                //precip values
                JAMSDouble[][] pV = (JAMSDouble[][])kostraTable.getObject("table");
                
                inPrecip = new double[hD.length];
                
                //find the precip in the kostra table
                int aIdx = 0;
                int dIdx = 0;
                boolean found = false;
                for(int i = 0; i < hA.length; i++){
                    if(this.precipAnnuality.getValue() == hA[i].getValue()){
                        aIdx = i;
                        found = true;
                    }
                }
                if(!found){
                    getModel().getRuntime().println("Wrong annuality was given!!");
                }
                found = false;
                for(int i = 0; i < hD.length; i++){
                    inPrecip[i] = pV[i][aIdx].getValue();
                }
                this.pIn.setValue(inPrecip[0]);
                
                //the attribute output file
                detailWriter = new GenericDataWriter(dirName.getValue()+"/"+this.kostraDetailFile.getValue());
                int[] kv;
                
                try {
                    detailWriter.writeLine("***********************************************************************************************");
                    detailWriter.writeLine("  Wiederkehrintervall T = " + this.precipAnnuality.getValue() + " a");
                    detailWriter.writeLine("***********************************************************************************************");
                    
                    detailWriter.write("N-Dauer [min]:");
                    kv = this.kostraValues.getValue();
                    for(int i = 0; i < kv.length; i++){
                        detailWriter.write("\t"+kv[i]);
                    }
                    detailWriter.write("\n");
                    detailWriter.write("N-Höhe [mm]:");
                    for(int i = 0; i < inPrecip.length; i++){
                        detailWriter.write("\t"+inPrecip[i]);
                    }
                    detailWriter.write("\n");
                    
                    //always write time
                    detailWriter.addColumn("Zeit in h");
                    sampleCount = this.kostraValues.getValue().length;
                    for(int s = 0; s < this.sampleCount; s++){
                        int counter = s + 1;
                        this.legEntries[s] = "Q[" + inPrecip[s] +","+kv[s]+"]";
                        detailWriter.addColumn(legEntries[s]);
                    }
                    detailWriter.writeHeader();
                } catch (org.unijena.jams.runtime.RuntimeException ex) {
                    ex.printStackTrace();
                }

                
                
            } catch (JAMSEntity.NoSuchAttributeException ex) {
                ex.printStackTrace();
            }
            
            
        }
    }
    
    private void updateValues() {
        int sampleCount = this.kostraValues.getValue().length;
        
        int count = this.currentCount + 1;
        getModel().getRuntime().println("Run No. " + count + " of " + sampleCount);
        //double[] sample = this.regularSampler();
        if(currentCount > 0){
            if(this.currentStep < this.kostraValues.getValue().length){
                this.precipDuration.setValue(this.kostraValues.getValue()[currentCount] * 60);
                this.pIn.setValue(inPrecip[currentCount]);
            } 
        }
        this.legendEntry.setValue(this.legEntries[currentCount]);
        currentCount++;
        this.currentStep++;
    }
    
    
    
    
    
    private void resetValues() {
        //set parameter values to initial values corresponding to their boundaries
        precipDuration.setValue(this.kostraValues.getValue()[0] * 60);
        
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
                
                summaryWriter.addData(this.precipDuration.getValue() / 60);
                summaryWriter.addData(this.pIn.getValue());
                summaryWriter.addData(this.maxRunoff.getValue());
                double vol = this.cumVolume.getValue() / 1000000;
                summaryWriter.addData(vol);
                try{
                    summaryWriter.writeData();
                    summaryWriter.flush();
                }catch(org.unijena.jams.runtime.RuntimeException e){
                    
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
                detailWriter.addData(timeStamp.toString("%1$tH:%1$tM:%1$tS"));
                timeStamp.add(modelTimeInterval.getTimeUnit(), modelTimeInterval.getTimeUnitCount());
                for(int r = 0; r < sampleCount; r++){
                    String dStr = String.format(Locale.US,"%.4f",this.valueArray[r][t]);
                    detailWriter.addData(dStr);
                }
                try {
                    detailWriter.writeData();
                } catch (org.unijena.jams.runtime.RuntimeException jre) {
                    getModel().getRuntime().println(jre.getMessage());
                }
            }
            detailWriter.close();
            summaryWriter.close();
        }
    }
    
}
