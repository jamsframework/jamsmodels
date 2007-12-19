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
import java.util.StringTokenizer;
import org.unijena.jams.JAMS;
import org.unijena.jams.data.*;
import org.unijena.jams.data.JAMSEntity.NoSuchAttributeException;
import org.unijena.jams.io.GenericDataWriter;
import org.unijena.jams.model.*;

/**
 * Context component which iterates through a set of precipitation events. These
 * events are provided by the KOSTRA input file. Precipitation events for extreme 
 * annualities beyond 100 years are calculated also in this component. The output
 * is written to a detail and summary file.
 * @author Peter Krause
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
    /**
     * the model's workspace directory<br>
     * access: READ<br>
     * update: INIT<br>
     * unit: n/a
     */
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.INIT,
            description = "Data file directory name"
            )
            public JAMSString dirName;
    
    /**
     * the variable name for the parameter precipDuration as used in the XML model
     * description -- this is a workaround for variable retreival<br>
     * access: READ<br>
     * update: RUN<br>
     * unit: n/a
     */
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "current precip duration"
            )
            public JAMSString precipDurationName;
    
    /**
     * the variable name for the parameter "input precipitation" as used in the XML model
     * description -- this is a workaround for variable retreival<br>
     * access: READ<br>
     * update: RUN<br>
     * unit: n/a
     */
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READWRITE,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "current precip height"
            )
            public JAMSString precipHeightName;
    
    /**
     * the maximum peak flow for the specific precipitation event<br>
     * access: READ<br>
     * update: RUN<br>
     * unit: m^3/s
     */
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READWRITE,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "peak runoff"
            )
            public JAMSDouble maxRunoff;
    
    /**
     * the cumulated runoff of the specific precipitation event<br>
     * access: READ<br>
     * update: RUN<br>
     * unit: million m^3
     */
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READWRITE,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "cumulated volume of all runoff"
            )
            public JAMSDouble cumVolume;
    
    /**
     * flag for en/disabling the component<br>
     * access: WRITE<br>
     * update: RUN<br>
     * unit: m^3/s
     */
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.INIT,
            description = "Flag for enabling this component"
            )
            public JAMSBoolean enable;
    
    /**
     * filename for the output summary<br>
     * access: READ<br>
     * update: INIT<br>
     * unit: n/a
     */
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.INIT
            )
            public JAMSString kostraSummaryFile;
    
    /**
     * filename for the detailed output <br>
     * access: READ<br>
     * update: INIT<br>
     * unit: n/a
     */
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.INIT
            )
            public JAMSString kostraDetailFile;
    
    /**
     * the model't time interval<br>
     * access: READ<br>
     * update: INIT<br>
     * unit: n/a
     */
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.INIT,
            description = "The model time interval"
            )
            public JAMSTimeInterval modelTimeInterval;
    
    /**
     * an array of values used in the detailed output<br>
     * access: READ<br>
     * update: RUN<br>
     * unit: n/a
     */
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READWRITE,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "Output file attribute"
            )
            public JAMSDoubleArray targetValue;
    
    /**
     * a title used for the of the graphical output<br>
     * access: WRITE<br>
     * update: RUN<br>
     * unit: n/a
     */
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.WRITE,
            update = JAMSVarDescription.UpdateType.INIT,
            description = "runoff plot title"
            )
            public JAMSString runoffPlotTitle;
    
    /**
     * a title used for the of the graphical output<br>
     * access: WRITE<br>
     * update: RUN<br>
     * unit: n/a
     */
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.WRITE,
            update = JAMSVarDescription.UpdateType.INIT,
            description = "volume plot title"
            )
            public JAMSString volumePlotTitle;
    
    /**
     * a text used for the of the graphical output<br>
     * access: WRITE<br>
     * update: RUN<br>
     * unit: n/a
     */
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READWRITE,
            update = JAMSVarDescription.UpdateType.INIT,
            description = "Legend entry"
            )
            public JAMSString legendEntry;
    
    /**
     * additional information for the header of the output file<br>
     * access: WRITE<br>
     * update: RUN<br>
     * unit: n/a
     */
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READWRITE,
            update = JAMSVarDescription.UpdateType.INIT,
            description = "outfile header"
            )
            public JAMSString outfileHeader;
    /**
     * a two dimensional table containing precipitation sums for a combination of 
     * various precipitation duration and return periods.<br>
     * access: READ<br>
     * update: RUN<br>
     * unit: n/a
     */
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.INIT,
            description = "the kostra table object as input"
            )
            public JAMSEntity kostraTable;
    
    /**
     * the annuality of the precipitation event. Is either iterated or set to a fixed 
     * value.<br>
     * access: READ<br>
     * update: RUN<br>
     * unit: n/a
     */
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READWRITE,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "precip annualtiy"
            )
            public JAMSDouble precipAnnuality;
    
    /**
     * flag for selection of variable precipitation annualities<br>
     * access: READ<br>
     * update: INIT<br>
     * unit: n/a
     */
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.INIT,
            description = "variable annualtiy"
            )
            public JAMSBoolean varAnnuality;
    
    /**
     * flag for selection of variable precipitatio durations<br>
     * access: READ<br>
     * update: INIT<br>
     * unit: n/a
     */
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.INIT,
            description = "variable duration"
            )
            public JAMSBoolean varDuration;
    
    /**
     * a semicolon separated list of precipitation annualities higher than 100 years<br>
     * access: READ<br>
     * update: INIT<br>
     * unit: n/a
     */
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.INIT,
            description = "the annualities for penPrecip computations"
            )
            public JAMSString penPrecipAnnualities;
    
    /**
     * flag for computation of precipitation events for extreme annuality.<br>
     * access: READ<br>
     * update: INIT<br>
     * unit: n/a
     */
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.INIT,
            description = "switch for penPrecip computations"
            )
            public JAMSBoolean penPrecipOn;
    
    //JAMSDouble pDuration;
    JAMSDouble pIn;
    JAMSDouble pDur;
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
    
    JAMSDoubleArray varValues = new JAMSDoubleArray();
    double[] pA;// = {200,500,1000,5000,10000};
    JAMSDoubleArray penAnnualities = new JAMSDoubleArray();
    double[] penPrecip;
    
    
    private boolean hasNext() {
        int sampleCount = varValues.getValue().length;
        return currentCount < sampleCount;
    }
    
    //a workaround function as long as array cannot be an input
    private void convertPenPrecip(){
        StringTokenizer str = new StringTokenizer(this.penPrecipAnnualities.getValue(),";");
        int nToks = str.countTokens();
        pA = new double[nToks];
        for(int i = 0; i < nToks; i++)
            pA[i] = Double.parseDouble(str.nextToken());
        
        this.penAnnualities.setValue(pA);
    }
    
    private void computePenPrecip(){
        //variable duration fixed annuality
        this.penAnnualities.setValue(pA);
        if(this.varDuration.getValue()){
            //number of durations in kostra table
            try {
                JAMSDouble[] durHeaders  = (JAMSDouble[])this.kostraTable.getObject("HeaderD");
                JAMSDouble[] annHeaders  = (JAMSDouble[])this.kostraTable.getObject("HeaderA");
                int nDur = durHeaders.length;
                int nAnn = annHeaders.length;
                
                double[] kostra_1 = new double[nDur];
                double[] kostra_100 = new double[nDur];
                penPrecip = new double[nDur];
                
                //retrieve the position of annuality 1 and 100
                int pos_1 = -1;
                int pos_100 = -1;
                for(int i = 0; i < nAnn; i++){
                    if(annHeaders[i].getValue() == 1){
                        pos_1 = i;
                    }
                    if(annHeaders[i].getValue() == 100){
                        pos_100 = i;
                    }
                }
                //retrieve the 1 and 100 annual precip events
                //the precip value table
                JAMSDouble[][] pV = (JAMSDouble[][])kostraTable.getObject("table");
                for(int i = 0; i < nDur; i++){
                    kostra_1[i] = pV[i][pos_1].getValue();
                    kostra_100[i] = pV[i][pos_100].getValue();
                }
                //the annuality of the respective gui field
                double actAnnuality = this.precipAnnuality.getValue();
                
                //compute penPrecip value for each duration
                for(int i = 0; i < nDur; i++){
                    double w = ((kostra_100[i] * 1.2) - (kostra_1[i] * 0.9))/(Math.log(100));
                    int val = (int)((kostra_1[i] + (w * Math.log(actAnnuality))) * 100);

                    penPrecip[i] = (double)(val/100.0);
                }
                
            } catch (NoSuchAttributeException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        //variable annuality and fixed duration
        else if(this.varAnnuality.getValue()){
            int nAnn = this.penAnnualities.getValue().length;
            penPrecip = new double[nAnn];
           
            try {
                String para = this.precipDurationName.getValue();
                JAMSDouble pD = (JAMSDouble)getModel().getRuntime().getDataHandles().get(para);
                
                JAMSDouble[] durHeaders  = (JAMSDouble[])this.kostraTable.getObject("HeaderD");
                JAMSDouble[] annHeaders  = (JAMSDouble[])this.kostraTable.getObject("HeaderA");
                
                //determine the position of the duration in the kostra table and
                //retrieve the position of annuality 1 and 100
                int durPos = -1;
                int pos_1 = -1;
                int pos_100 = -1;
                for(int i = 0; i < durHeaders.length; i++){
                    if(durHeaders[i].getValue() == pD.getValue()){
                        durPos = i;
                    }
                }
                for(int i = 0; i < annHeaders.length;i++){
                    if(annHeaders[i].getValue() == 1){
                        pos_1 = i;
                    }
                    if(annHeaders[i].getValue() == 100){
                        pos_100 = i;
                    }
                }
                
                //retrieve precip events with 1 and 100 year annuality
                JAMSDouble[][] pV = (JAMSDouble[][])kostraTable.getObject("table");
                double kostra_1 = pV[durPos][pos_1].getValue();
                double kostra_100 = pV[durPos][pos_100].getValue();
                
                //compute penPrecip value for the respective duration and each annuality
                for(int i = 0; i < nAnn; i++){
                    double actAnnuality = penAnnualities.getValue()[i];
                    double w = ((kostra_100 * 1.2) - (kostra_1 * 0.9))/(Math.log(100));
                    int val = (int)((kostra_1 + (w * Math.log(actAnnuality))) * 100);
                    
                    penPrecip[i] = (double)(val/100.0);
                }
                
            } catch (NoSuchAttributeException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }
    
    
    /**
     * the component's init() method
     */
    public void init() {
        if(this.enable.getValue()){
            convertPenPrecip();
            if(this.penPrecipOn.getValue())
                this.computePenPrecip();
            
            //add more checks!!!
            String para = this.precipDurationName.getValue();
            String para2 = this.precipHeightName.getValue();
            pIn = (JAMSDouble)getModel().getRuntime().getDataHandles().get(para2);
            pDur = (JAMSDouble)getModel().getRuntime().getDataHandles().get(para);
            //create para output file
            summaryWriter = new GenericDataWriter(dirName.getValue()+"/"+this.kostraSummaryFile.getValue());
            
            if(varDuration.getValue()){
                this.runoffPlotTitle.setValue("Kostra Abfluss für Wiederkehrintervall T = " + String.format(Locale.US,"%.0f",this.precipAnnuality.getValue()) + " a");
                this.volumePlotTitle.setValue("Kostra Volumen für Wiederkehrintervall T = " + String.format(Locale.US,"%.0f",this.precipAnnuality.getValue()) + " a");
                summaryWriter.writeLine(this.outfileHeader.getValue());
                summaryWriter.writeLine("***********************************************************************************************");
                summaryWriter.writeLine("Wiederkehrintervall T = " + this.precipAnnuality.getValue() + " a");
                summaryWriter.writeLine("***********************************************************************************************");
                
                summaryWriter.addColumn("Dauerstufe D [min]");
                summaryWriter.addColumn("Niederschlag [mm]");
                summaryWriter.addColumn("Scheitelabfluss [m³/s]");
                summaryWriter.addColumn("Wellenvolumen [Mio m³]");
                summaryWriter.writeHeader();
                try {
                    JAMSDouble[] tmp  = (JAMSDouble[])this.kostraTable.getObject("HeaderD");
                    double[] tmp2 = new double[tmp.length];
                    for(int i = 0; i < tmp.length; i++)
                        tmp2[i] = tmp[i].getValue();
                    varValues.setValue(tmp2);
                } catch (JAMSEntity.NoSuchAttributeException ex) {
                    ex.printStackTrace();
                }
            } else if(varAnnuality.getValue()){
                this.runoffPlotTitle.setValue("Kostra Abfluss für Dauerstufe D = " + String.format(Locale.US, "%.0f",this.pDur.getValue()) + " Min.");
                this.volumePlotTitle.setValue("Kostra Volumen für Dauerstufe D = " + String.format(Locale.US, "%.0f",this.pDur.getValue()) + " Min.");
                
                summaryWriter.writeLine(this.outfileHeader.getValue());
                summaryWriter.writeLine("***********************************************************************************************");
                summaryWriter.writeLine("Dauerstufe D = " + (pDur) + " min.");
                summaryWriter.writeLine("***********************************************************************************************");
                summaryWriter.addColumn("Jährlichkeit T [a]");
                summaryWriter.addColumn("Niederschlag [mm]");
                summaryWriter.addColumn("Scheitelabfluss [m³/s]");
                summaryWriter.addColumn("Wellenvolumen [Mio m³]");
                summaryWriter.writeHeader();
                try {
                    JAMSDouble[] tmp  = (JAMSDouble[])this.kostraTable.getObject("HeaderA");
                    int arrLen = 0;
                    double[] tmp2 = null;
                    if(this.penPrecipOn.getValue()){
                        arrLen = tmp.length;
                        arrLen = arrLen + this.penAnnualities.getValue().length;
                        
                        tmp2 = new double[arrLen];
                        int tmpCount = 0;
                        for(int i = 0; i < tmp.length; i++){
                            tmp2[i] = tmp[i].getValue();
                            tmpCount++;
                        }
                        for(int i = 0; i < this.penAnnualities.getValue().length; i++){
                            tmp2[tmpCount] = this.penAnnualities.getValue()[i];
                            tmpCount++;
                        }
                    }
                    else{
                        tmp2 = new double[tmp.length];
                        for(int i = 0; i < tmp.length; i++)
                            tmp2[i] = tmp[i].getValue();
                    }
                    
                    varValues.setValue(tmp2);
                } catch (JAMSEntity.NoSuchAttributeException ex) {
                    ex.printStackTrace();
                }
            }
            //the attribute output file
            detailWriter = new GenericDataWriter(dirName.getValue()+"/"+this.kostraDetailFile.getValue());
            sampleCount = varValues.getValue().length;
            this.legEntries = new String[varValues.getValue().length];
            
            
            //setting up the dataArray
            this.timeSteps = (int)modelTimeInterval.getNumberOfTimesteps();
            this.valueArray = new double[sampleCount][timeSteps];
            this.timeStepCounter = 0;
            this.runCounter = 0;
            
            //determine x and y stepSize
            if(varDuration.getValue()){
                pDur.setValue(varValues.getValue()[0]);
            } else if(varAnnuality.getValue()){
                this.precipAnnuality.setValue(varValues.getValue()[0]);
            }
            this.currentStep = 0;
            try {
                JAMSDouble[] hA = (JAMSDouble[]) kostraTable.getObject("HeaderA");
                //precip duration
                JAMSDouble[] hD = (JAMSDouble[])kostraTable.getObject("HeaderD");
                //precip values
                JAMSDouble[][] pV = (JAMSDouble[][])kostraTable.getObject("table");
                
                if(varDuration.getValue())
                    inPrecip = new double[hD.length];
                else if(varAnnuality.getValue()){
                    if(this.penPrecipOn.getValue()){
                       int arrLen = hA.length + this.penAnnualities.getValue().length;
                       inPrecip = new double[arrLen];
                    }
                    else{
                        inPrecip = new double[hA.length];
                    }
                }
                //first we look in the kostra table for the precip based on annuality
                if(varDuration.getValue()){
                    int aIdx = -1;
                    int pIdx = -1;
                    boolean found = false;
                    for(int i = 0; i < hA.length; i++){
                        if(this.precipAnnuality.getValue() == hA[i].getValue()){
                            aIdx = i;
                            found = true;
                        }
                    }
                    //precip is not in the kostra table so we look up pen
                    if(!found){
                        if(this.penPrecipOn.getValue()){
                            //if annuality is in the PEN list PEN is used
                            for(int i = 0; i < this.penAnnualities.getValue().length; i++){
                                if(this.precipAnnuality.getValue() == this.penAnnualities.getValue()[i]){
                                    found = true;
                                    pIdx = i;
                                }
                            }
                            //annuality is not in the PEN List
                            if(!found){
                                
                            }
                        }
                        getModel().getRuntime().println("Wrong annuality was given!!");
                    }
                    found = false;
                    //Kostra annuality
                    if(aIdx >= 0){
                        for(int i = 0; i < hD.length; i++){
                            inPrecip[i] = pV[i][aIdx].getValue();
                        }
                    }
                    else if(this.precipAnnuality.getValue() > 100){
                        for(int i = 0; i < hD.length; i++){
                            inPrecip[i] = this.penPrecip[i];
                        }
                    }
                    else{
                        System.out.println("Annuality is not a valid value!");
                    }
                } else if(varAnnuality.getValue()){
                    int dIdx = 0;
                    boolean found = false;
                    for(int i = 0; i < hD.length; i++){
                        if((pDur.getValue()) == hD[i].getValue()){
                            dIdx = i;
                            found = true;
                        }
                    }
                    if(!found){
                        getModel().getRuntime().println("Wrong duration was given!!");
                    }
                    found = false;
                    
                    int pCount = 0;
                    for(int i = 0; i < hA.length; i++){
                        inPrecip[i] = pV[dIdx][i].getValue();
                        pCount++;
                    }
                    if(this.penPrecipOn.getValue()){
                        for(int i = 0; i < this.penAnnualities.getValue().length; i++){
                            inPrecip[pCount] = this.penPrecip[i];
                            pCount++;
                        }
                    }
                    
                }
                this.pIn.setValue(inPrecip[0]);
                
                //the attribute output file
                detailWriter = new GenericDataWriter(dirName.getValue()+"/"+this.kostraDetailFile.getValue());
                double[] kv = null;
                
                if(varDuration.getValue()){
                    detailWriter.writeLine(this.outfileHeader.getValue());
                    detailWriter.writeLine("***********************************************************************************************");
                    detailWriter.writeLine("  Wiederkehrintervall T = " + this.precipAnnuality.getValue() + " a");
                    detailWriter.writeLine("***********************************************************************************************");
                    
                    detailWriter.write("N-Dauer [min]:");
                    kv = varValues.getValue();
                    for(int i = 0; i < kv.length; i++){
                        detailWriter.write("\t"+String.format(Locale.US,"%.0f",kv[i]));
                    }
                    detailWriter.write("\n");
                    detailWriter.write("N-Höhe [mm]:");
                    for(int i = 0; i < inPrecip.length; i++){
                        detailWriter.write("\t"+String.format(Locale.US,"%.1f",inPrecip[i]));
                    }
                    detailWriter.write("\n");
                    detailWriter.addColumn("Zeit in h");
                    sampleCount = varValues.getValue().length;
                    for(int s = 0; s < this.sampleCount; s++){
                        //format of output strings
                        String fStr = "%.1f";
                        String dStr = "%.0f";
                        this.legEntries[s] = "[N: " + String.format(Locale.US,fStr,inPrecip[s]) +" mm; D: "+String.format(Locale.US,dStr,kv[s])+" min]";
                        detailWriter.addColumn(legEntries[s]);
                    }
                   
                    
                } else if(varAnnuality.getValue()){
                    detailWriter.writeLine(this.outfileHeader.getValue());
                    detailWriter.writeLine("***********************************************************************************************");
                    detailWriter.writeLine("  Dauerstufe D = " + pDur.getValue() + " min.");
                    detailWriter.writeLine("***********************************************************************************************");
                    
                    detailWriter.write("N-Jährlichkeit [a]:");
                    kv = varValues.getValue();
                    for(int i = 0; i < kv.length; i++){
                        detailWriter.write("\t"+String.format(Locale.US,"%.1f",kv[i]));
                    }
                    detailWriter.write("\n");
                    detailWriter.write("N-Höhe [mm]:");
                    for(int i = 0; i < inPrecip.length; i++){
                        detailWriter.write("\t"+String.format(Locale.US,"%.1f",inPrecip[i]));
                    }
                    detailWriter.write("\n");
                    detailWriter.addColumn("Zeit in h");
                    sampleCount = varValues.getValue().length;
                    for(int s = 0; s < this.sampleCount; s++){
                        //format of output strings
                        String pStr = "%.1f";
                        String jStr = null;
                        if(kv[s] < 1)
                            jStr = "%.1f";
                        else
                            jStr = "%.0f";
                        this.legEntries[s] = "[N: " + String.format(Locale.US,pStr,inPrecip[s]) +" mm; T: "+String.format(Locale.US,jStr,kv[s])+" a]";
                        detailWriter.addColumn(legEntries[s]);
                    }
                    
                    
                }
                
                detailWriter.writeHeader();
                
            } catch (JAMSEntity.NoSuchAttributeException ex) {
                ex.printStackTrace();
            }
            
            
            
        }
    }
    
    private void updateValues() {
        int sampleCount = varValues.getValue().length;
        
        int count = this.currentCount + 1;
        getModel().getRuntime().println("Run No. " + count + " of " + sampleCount);
        
        if(currentCount > 0){
            if(this.currentStep < varValues.getValue().length){
                if(varDuration.getValue())
                    pDur.setValue(varValues.getValue()[currentCount]);
                else if(varAnnuality.getValue())
                    this.precipAnnuality.setValue(varValues.getValue()[currentCount]);
                
                this.pIn.setValue(inPrecip[currentCount]);
            }
        }
        this.legendEntry.setValue(this.legEntries[currentCount]);
        currentCount++;
        this.currentStep++;
    }
    
    
    
    
    
    private void resetValues() {
        //set parameter values to initial values corresponding to their boundaries
        if(varDuration.getValue())
            pDur.setValue(varValues.getValue()[0]);
        else if(varAnnuality.getValue())
            precipAnnuality.setValue(varValues.getValue()[0]);
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
    
    /**
     * the component's run() method
     */
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
                
                if(varDuration.getValue())
                    summaryWriter.addData(pDur.getValue(), 0);
                else if(varAnnuality.getValue())
                    summaryWriter.addData(this.precipAnnuality.getValue());
                summaryWriter.addData(this.pIn.getValue(),2);
                summaryWriter.addData(this.maxRunoff.getValue(),3);
                double vol = this.cumVolume.getValue() / 1000000;
                summaryWriter.addData(vol,3);
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
    
    /**
     * the component's cleanup() method
     */
    public void cleanup() {
        if (enable.getValue()) {
            int sampleCount = varValues.getValue().length;
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
