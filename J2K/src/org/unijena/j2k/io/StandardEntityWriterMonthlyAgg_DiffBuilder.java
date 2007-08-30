/*
 * StandardEntityWriterN.java
 * Created on 15. Febuary 2006, 11:05
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

package org.unijena.j2k.io;

import java.util.Locale;
import java.util.Calendar;
import org.unijena.jams.JAMS;
import org.unijena.jams.data.*;
import org.unijena.jams.model.*;
import org.unijena.jams.io.*;

/**
 *
 * @author S. Kralisch
 */
@JAMSComponentDescription(
title="Entity file writer (spatial+monthly)",
        author="D. Varga",
        description="Base: StandardEntityWriterN (S.Kralisch)." +
        "Use: For calculating monthly averages, the time Interval should be always one day longer."
        )
        public class StandardEntityWriterMonthlyAgg_DiffBuilder extends JAMSComponent {
    
    /*
     *  Component variables
     */
    
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "EntitySet"
            )
            public JAMSEntityCollection entities;
    
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.INIT,
            description = "Data file directory name"
            )
            public JAMSString dirName;
    
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.INIT,
            description = "Output file name"
            )
            public JAMSString fileName;
    
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.INIT,
            description = "The model time interval"
            )
            public JAMSTimeInterval modelTimeInterval;
    
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "The aggregation time interval"
            )
            public JAMSTimeInterval aggTimeInterval;
    
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "time"
            )
            public JAMSCalendar time;
    
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.INIT,
            description = "Output file header descriptions"
            )
            public JAMSString header;
    
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.INIT,
            description = "Output file attribute names"
            )
            public JAMSStringArray attributeNames;
    
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.INIT,
            description = "entity attribute name for weight [attName | none]"
            )
            public JAMSString weight;
    
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.INIT,
            description = "Output file header descriptions"
            )
            public JAMSBoolean monthlyValuesWriting;
    
    private GenericDataWriter writer;
    
    
    private double[][] valueMatrix;
    private double[] dailyValue;
    private double[] attrbValue;
    private double[] oldDailyValue;
    private double[] diffValue;
    private double[] weightVal;
    
    private String[] dateVals;
    private String timeFormat = "%1$td.%1$tm.%1$tY";
    private int tcounter;
    private int oldMonth;
    private int nEnts;
    private double[][] aggMatrix;
    private int[] aggCounter;
    private String[] aggFieldNames;
    
    private JAMSTimeInterval timeInterval2;
    
    private int nAttrbs;
    
    private int counter = 0;

    private int aggTsteps = 0;
    
    /*
     *  Component runstages
     */
    
    public void init() throws JAMSEntity.NoSuchAttributeException {
        
        //some checking if time intervals correlate well
        //....
        //....
        
        JAMSCalendar model_sd = this.modelTimeInterval.getStart().clone();
        JAMSCalendar model_ed = this.modelTimeInterval.getEnd().clone();
        int model_tres = this.modelTimeInterval.getTimeUnit();
        long sdMod = model_sd.getTimeInMillis();
        long edMod = model_ed.getTimeInMillis();
        long model_tsteps = 0;
        
        model_tsteps = modelTimeInterval.getNumberOfTimesteps();
        
        JAMSCalendar agg_sd = this.aggTimeInterval.getStart().clone();
        JAMSCalendar agg_ed = this.aggTimeInterval.getEnd().clone();
        int agg_tres = this.aggTimeInterval.getTimeUnit();
        long sdAgg = agg_sd.getTimeInMillis();
        long edAgg = agg_ed.getTimeInMillis();
        
        //check if aggTimeInterval is in the bounds of the model time interval
        //otherwise it will be set to the model interval bounds
        if(agg_sd.before(model_sd)){
            this.aggTimeInterval.setStart(model_sd);
            getModel().getRuntime().println("aggStartdate was set equal to model startdate", JAMS.STANDARD);
        }
        if(model_ed.before(agg_ed)){
            this.aggTimeInterval.setEnd(model_ed);
            getModel().getRuntime().println("aggEnddate was set equal to model enddate", JAMS.STANDARD);
        }
        
        aggTsteps = (int) aggTimeInterval.getNumberOfTimesteps();
        
        int ts = (int) this.getContext().getNumberOfIterations();
        getModel().getRuntime().println("aggStartdate:\t" + agg_sd.toString(), JAMS.VERBOSE);
        getModel().getRuntime().println("aggEnddate:\t" + agg_ed.toString(), JAMS.VERBOSE);
        
        
        
        writer = new GenericDataWriter(dirName.getValue()+"/"+fileName.getValue());
        
        timeInterval2 = new JAMSTimeInterval(this.aggTimeInterval.getStart(), this.aggTimeInterval.getEnd(), (int) JAMSCalendar.MONTH, 1);
        
        int tsteps = (int)this.timeInterval2.getNumberOfTimesteps();
        
        nEnts = this.entities.getEntityArray().length;
        valueMatrix = new double[tsteps+1][nEnts];
        dailyValue = new double[nEnts];
        attrbValue = new double[nEnts];
        oldDailyValue = new double[nEnts];
        weightVal = new double[nEnts];
        diffValue = new double[nEnts];
        dateVals = new String[tsteps];
        
        nAttrbs = this.attributeNames.getValue().length;
        
        //monthly values
        timeFormat = "%1$tb/%1$ty";
        aggMatrix = new double[12][nEnts];
        aggCounter = new int[12];
        aggFieldNames = new String[12];
        
        tcounter = 0;
        
        int oldMonth = time.get(time.MONTH);
    }
    
    public void run() throws JAMSEntity.NoSuchAttributeException {
        if (!time.after(aggTimeInterval.getEnd()) && !time.before(aggTimeInterval.getStart())) {
            
            int newMonth=time.get(time.MONTH);
            
            if(newMonth != oldMonth){
                
                //Einfügen: Lesen von Monat und Jahr für Header!
                dateVals[tcounter] = time.toString(timeFormat);
                
                //aggregated values
                //monthly values
                if(tcounter != 0){
                    int month = oldMonth;//time.get(time.MONTH);
                    aggFieldNames[month] = time.toString("%1$tb");
                    for(int i = 0; i < nEnts; i++){
                        aggMatrix[month][i] += valueMatrix[tcounter][i];
                    }
                    aggCounter[month]++;
                }
                tcounter++;
                
            }
            //no weight
            if(this.weight.getValue().equals("none")){
                for(int i = 0; i < nEnts; i++){
                    for(int a = 0; a < nAttrbs; a++){
                        attrbValue[i] = (((JAMSDouble)entities.getEntityArray()[i].getObject(this.attributeNames.getValue()[a])).getValue());
                        dailyValue[i] = dailyValue[i] + attrbValue[i];
                    }
                    
                    diffValue[i] = dailyValue[i] - oldDailyValue[i];
                    
                    valueMatrix[tcounter][i] = valueMatrix[tcounter][i] + diffValue[i];
                    
                    oldDailyValue[i] = dailyValue[i];
                }
            }
            //user selected weight attribute
            else{
                for(int i = 0; i < nEnts; i++){
                    weightVal[i] = (((JAMSDouble)entities.getEntityArray()[i].getObject(this.weight.getValue())).getValue());
                    dailyValue[i] = 0;
                    
                    for(int a = 0; a < nAttrbs; a++){
                        attrbValue[i] = (((JAMSDouble)entities.getEntityArray()[i].getObject(this.attributeNames.getValue()[a])).getValue());
                        dailyValue[i] = dailyValue[i] + (attrbValue[i] / weightVal[i]);
                    }
                                        
                    diffValue[i] = dailyValue[i] - oldDailyValue[i];
                                        
                    valueMatrix[tcounter][i] = valueMatrix[tcounter][i] + diffValue[i];
                    
                    oldDailyValue[i] = dailyValue[i];
                }
            }
            
            oldMonth = time.get(time.MONTH);
            
        }
    }
    
    public void cleanup() throws JAMSEntity.NoSuchAttributeException {
        
        getModel().getRuntime().println("Writing distributed output file ... may take a while ... please wait ...", JAMS.STANDARD);
        getModel().getRuntime().println("Number of entities: " + nEnts + ", number of timeSteps: " + dateVals.length);
        try {
            writer.addComment("J2K model output: "+header.getValue());
            
            writer.addComment("");
            
            //header//header
            writer.addColumn("ID");
            if(monthlyValuesWriting.getValue()){
                for(int i = 0; i < tcounter; i++){
                    writer.addColumn(dateVals[i]);
                }
            }
            
            //aggregated values
            //monthly values
            writer.addColumn(aggFieldNames[11]);
            for(int i = 0; i < 11; i++){
                writer.addColumn(aggFieldNames[i]);
            }
            writer.addColumn("Year");
            writer.writeHeader();
            
            //data matrix
            for(int e = 0; e < nEnts; e++){
                int ID = (int)(((JAMSDouble)entities.getEntityArray()[e].getObject("ID")).getValue());
                writer.addData(ID);
                if(monthlyValuesWriting.getValue()){
                    for(int t = 1; t < tcounter+1; t++){
                        String dStr = String.format(Locale.US,"%.3f",valueMatrix[t][e]);
                        writer.addData(dStr);
                    }
                }
                
                //aggregated values
                //monthly values
                double aggSum = 0;
                for(int t = 0; t < 12; t++){
                    aggSum += (aggMatrix[t][e] / aggCounter[t]);
                    String dStr = String.format(Locale.US,"%.3f",(aggMatrix[t][e]) / aggCounter[t]);
                    writer.addData(dStr);
                }
                if(this.weight.getValue().equals("none")){
                    String dStr = String.format(Locale.US,"%.3f",(aggSum));
                    writer.addData(dStr);
                }else{
                    String dStr = String.format(Locale.US,"%.3f",aggSum);
                    writer.addData(dStr);
                }
                writer.writeData();
            }
        } catch (org.unijena.jams.runtime.RuntimeException jre) {
            getModel().getRuntime().handle(jre);
        }
        writer.close();
        getModel().getRuntime().println("Finished distributed output file ... you may continue ...", JAMS.STANDARD);
    }
}
