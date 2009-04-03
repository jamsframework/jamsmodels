/*
 * StandardEfficiencyCalculator.java
 * Created on 24. November 2005, 09:48
 *
 * This file is part of JAMS
 * Copyright (C) 2005 FSU Jena, c0krpe
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

package org.unijena.j2k.efficiencies;

import java.util.Locale;
import java.util.Vector;

import org.unijena.j2k.statistics.Regression;
import jams.JAMS;
import jams.data.*;
import jams.model.*;

/**
 *
 * @author Peter Krause
 */
@JAMSComponentDescription(
title="StandardEfficiencyCalculator",
        author="Peter Krause",
        description="Calculates various efficiency measures"
        )
        public class StandardEfficiencyCalculator extends JAMSComponent {
    
    /*
     *  Component variables
     */
    
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "time"
            )
            public JAMSCalendar time;
    
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.INIT,
            description = "The model time interval"
            )
            public JAMSTimeInterval modelTimeInterval;
    
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.INIT,
            description = "The efficiency time interval"
            )
            public JAMSTimeInterval effTimeInterval;
    
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.INIT,
            description = "The months to be evaluated interval"
            )
            public JAMSIntegerArray effMonthList;
    
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.INIT,
            description = "Efficiency method used"
            )
            public JAMSIntegerArray effMethod;
    
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "Prediction value"
            )
            public JAMSDouble prediction;
    
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "Validation value"
            )
            public JAMSDouble validation;
    
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.WRITE,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "Nash-Sutcliffe-efficiency with power 1.0",
            defaultValue= "0"
            )
            public JAMSDouble e1;
    
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.WRITE,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "Nash-Sutcliffe-efficiency with power 2.0",
            defaultValue= "0"
            )
            public JAMSDouble e2;
    
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.WRITE,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "logarithmic Nash-Sutcliffe-efficiency with power 1.0",
            defaultValue= "0"
            )
            public JAMSDouble le1;
    
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.WRITE,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "logarithmic Nash-Sutcliffe-efficiency with power 2.0",
            defaultValue= "0"
            )
            public JAMSDouble le2;
    
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.WRITE,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "Willmot's index of agreement with power 1.0",
            defaultValue= "0"
            )
            public JAMSDouble ioa1;
    
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.WRITE,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "Willmot's index of agreement with power 2.0",
            defaultValue= "0"
            )
            public JAMSDouble ioa2;
    
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.WRITE,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "coefficient of determination r²",
            defaultValue= "0"
            )
            public JAMSDouble rsq;
    
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.WRITE,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "gradient of linear regression",
            defaultValue= "0"
            )
            public JAMSDouble grad;
    
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.WRITE,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "weighted r²"
            )
            public JAMSDouble wrsq;
    
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.WRITE,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "gradient of double sum regression",
            defaultValue= "0"
            )
            public JAMSDouble dsGrad;
    
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.WRITE,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "absolute volume error",
            defaultValue= "0"
            )
            public JAMSDouble absVolErr;
    
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.WRITE,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "root mean square error",
            defaultValue= "0"
            )
            public JAMSDouble rmse;
    
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.WRITE,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "percent bias",
            defaultValue= "0"
            )
            public JAMSDouble pbias;
    
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.WRITE,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "full set of predicted values"
            )
            public JAMSDoubleArray predictionValues;
    
    private final int E1 = 1;
    private final int E2 = 2;
    private final int LOG_E1 = 3;
    private final int LOG_E2 = 4;
    private final int IOA_1 = 5;
    private final int IOA_2 = 6;
    private final int R2 = 7;
    private final int WR2 = 8;
    private final int DSGRAD = 9;
    private final int ABSVOLERROR = 10;
    private final int RMSE = 11;
    private final int PBIAS = 12;
    
    private final int TOTAL_PERIOD = 0;
    private final int HYDROLOGICAL_YEAR = 1;
    private final int CALENDAR_YEAR = 2;
    
    private double[] valData;
    private double[] preData;
    
    private int counter = 0;
    
    private int interValStart = 0;
    private int interValEnd = 0;
    
    private int effTsteps = 0;
    
    private boolean monthly = false;
    private int monthCount = 0;
    
    /*
     *  Component run stages
     */
    
    public void init() {
        
        //some checking if time intervals correlate well
        //....
        //....
        this.counter = 0;
        this.monthCount = 0;
        JAMSCalendar model_sd = this.modelTimeInterval.getStart().clone();
        JAMSCalendar model_ed = this.modelTimeInterval.getEnd().clone();
        int model_tres = this.modelTimeInterval.getTimeUnit();
        long sdMod = model_sd.getTimeInMillis();
        long edMod = model_ed.getTimeInMillis();
        long model_tsteps = 0;
        /*if(model_tres == model_sd.DAY_OF_YEAR){
            model_tsteps = (edMod - sdMod) / (1000 * 60 * 60 * 24);
            model_tsteps = model_tsteps + 1 + 1;
        }*/
        model_tsteps = modelTimeInterval.getNumberOfTimesteps();
        
        JAMSCalendar eff_sd = this.effTimeInterval.getStart().clone();
        JAMSCalendar eff_ed = this.effTimeInterval.getEnd().clone();
        int eff_tres = this.effTimeInterval.getTimeUnit();
        long sdEff = eff_sd.getTimeInMillis();
        long edEff = eff_ed.getTimeInMillis();
        
        //check if effTimeInterval is in the bounds of the model time interval
        //otherwise it will be set to the model interval bounds
        if(eff_sd.before(model_sd)){
            this.effTimeInterval.setStart(model_sd);
            getModel().getRuntime().println("effStartdate was set equal to model startdate", JAMS.STANDARD);
        }
        if(model_ed.before(eff_ed)){
            this.effTimeInterval.setEnd(model_ed);
            getModel().getRuntime().println("effEnddate was set equal to model enddate", JAMS.STANDARD);
        }
        if(eff_ed.before(model_sd)){
            this.effTimeInterval.setEnd(model_ed);
            getModel().getRuntime().println("effEnddate was set equal to model enddate", JAMS.STANDARD);
        }
        /*if(eff_tres == eff_sd.DAY_OF_YEAR){
            this.effTsteps = (int)((edEff - sdEff) / (1000 * 60 * 60 * 24));
            this.effTsteps = this.effTsteps + 1;
        }*/
        effTsteps = (int) effTimeInterval.getNumberOfTimesteps();
        
        //int ts = (int)tsteps;
        int ts = (int) this.getContext().getNumberOfIterations();
        getModel().getRuntime().println("effStartdate:\t" + eff_sd.toString(), JAMS.VERBOSE);
        getModel().getRuntime().println("effEnddate:\t" + eff_ed.toString(), JAMS.VERBOSE);
        
        
        valData = new double[(int)model_tsteps];
        preData = new double[(int)model_tsteps];
        
        counter = 0;
        
        //determine start and end array index for timeInterval
        
        if(eff_tres == eff_sd.DAY_OF_YEAR){
            this.interValStart =(int)((sdEff - sdMod) / (1000 * 60 * 60 * 24));
            this.interValEnd = this.interValStart + this.effTsteps;
        } else if(eff_tres == eff_sd.HOUR_OF_DAY){
            this.interValStart =(int)((sdEff - sdMod) / (1000 * 60 * 60));
            this.interValEnd = this.interValStart + this.effTsteps;
        } else if(eff_tres == eff_sd.MONTH){
            JAMSCalendar modStart = modelTimeInterval.getStart().clone();
            JAMSCalendar effStart = effTimeInterval.getStart().clone();
            int startStep = 0;
            while(modStart.before(effStart)){
                startStep++;
                modStart.add(JAMSCalendar.MONTH,1);
            }
            this.interValStart = startStep;
            this.interValEnd = this.interValStart + this.effTsteps;
        } else if(eff_tres == eff_sd.YEAR){
            JAMSCalendar modStart = modelTimeInterval.getStart().clone();
            JAMSCalendar effStart = effTimeInterval.getStart().clone();
            int startStep = 0;
            while(modStart.before(effStart)){
                startStep++;
                modStart.add(JAMSCalendar.YEAR,1);
            }
            this.interValStart = startStep;
            this.interValEnd = this.interValStart + this.effTsteps;
        }
        int junk = 0;
        
        if(this.effMonthList != null){
            this.monthly = true;
        }
    }
    
    public void run() {
        if(monthly){
            int month = time.get(time.MONTH) + 1;
            for(int i = 0; i < this.effMonthList.getValue().length; i++){
                if(month == this.effMonthList.getValue()[i]){
                    this.valData[counter] = validation.getValue();
                    this.preData[counter] = prediction.getValue();
                    this.counter++;
                    this.monthCount++;
                }
            }
        } else{
            this.valData[counter] = validation.getValue();
            this.preData[counter] = prediction.getValue();
            this.counter++;
        }
    }
    
    public void cleanup() {
        getModel().getRuntime().println("", JAMS.STANDARD);
        getModel().getRuntime().println("*************************************************************", JAMS.STANDARD);
        getModel().getRuntime().println("Efficiencies for period:\t " + this.effTimeInterval.toString(), JAMS.STANDARD);
        getModel().getRuntime().println("Sampler: " + this.getInstanceName(), JAMS.STANDARD);
        getModel().getRuntime().println("*************************************************************", JAMS.STANDARD);
        
        Vector<Double> valVector = new Vector<Double>();
        Vector<Double> preVector = new Vector<Double>();
        
        this.predictionValues.setValue(preData);
        
        for(int i = this.interValStart; i < this.interValEnd; i++){
            //consider valid values only
            if(valData[i] > -9999 && preData[i] > -9999){
                valVector.add(valData[i]);
                preVector.add(preData[i]);
            }
        }
        
        int dataCount = valVector.size();
        if(monthly){
            dataCount = this.monthCount;
        }
        double[] valData_1 = new double[dataCount];
        double[] preData_1 = new double[dataCount];
        
        //converting Vectors to arrays
        for(int i = 0; i < dataCount; i++){
            valData_1[i] = valVector.get(i).doubleValue();
            preData_1[i] = preVector.get(i).doubleValue();
        }
        
        for(int i = 0; i < effMethod.getValue().length; i++){
            if(effMethod.getValue()[i] == this.E1){
                double e1 = NashSutcliffe.efficiency(preData_1, valData_1, 1);
                this.e1.setValue(e1);
                getModel().getRuntime().println("e1:\t\t" + String.format(Locale.US,"%.5f",e1), JAMS.STANDARD);
            }else if(effMethod.getValue()[i] == this.E2){
                double e2 = NashSutcliffe.efficiency(preData_1, valData_1, 2);
                this.e2.setValue(e2);
                getModel().getRuntime().println("e2:\t\t" + String.format(Locale.US,"%.5f",e2), JAMS.STANDARD);
            }else if(effMethod.getValue()[i] == this.LOG_E1){
                double le1 = NashSutcliffe.logEfficiency(preData_1, valData_1, 1);
                this.le1.setValue(le1);
                getModel().getRuntime().println("log_e1:\t\t" + String.format(Locale.US,"%.5f",le1), JAMS.STANDARD);
            }else if(effMethod.getValue()[i] == this.LOG_E2){
                double le2 = NashSutcliffe.logEfficiency(preData_1, valData_1, 2);
                this.le2.setValue(le2);
                getModel().getRuntime().println("log_e2:\t\t" + String.format(Locale.US,"%.5f",le2), JAMS.STANDARD);
            }else if(effMethod.getValue()[i] == this.IOA_1){
                double ioa1 = IndexOfAgreement.calc_IOA(preData_1, valData_1, 1, getModel());
                this.ioa1.setValue(ioa1);
                getModel().getRuntime().println("ioa1:\t\t" + String.format(Locale.US,"%.5f",ioa1), JAMS.STANDARD);
            }else if(effMethod.getValue()[i] == this.IOA_2){
                double ioa2 = IndexOfAgreement.calc_IOA(preData_1, valData_1, 2, getModel());
                this.ioa2.setValue(ioa2);
                getModel().getRuntime().println("ioa2:\t\t" + String.format(Locale.US,"%.5f",ioa2), JAMS.STANDARD);
            }else if(effMethod.getValue()[i] == this.R2){
                double[] rCoeff = Regression.calcLinReg(valData_1, preData_1);
                getModel().getRuntime().println("r²:\t\t" + String.format(Locale.US,"%.5f",rCoeff[2]), JAMS.STANDARD);
                getModel().getRuntime().println("grad:\t\t" + String.format(Locale.US,"%.5f",rCoeff[1]), JAMS.STANDARD);
                this.rsq.setValue(rCoeff[2]);
                this.grad.setValue(rCoeff[1]);
            }else if(effMethod.getValue()[i] == this.WR2){
                double[] rCoeff = Regression.calcLinReg(valData_1, preData_1);
                double wr;
                if(rCoeff[1] <= 1)
                    wr = Math.abs(rCoeff[1]) * rCoeff[2];
                else
                    wr = Math.pow(Math.abs(rCoeff[1]), -1.0) * rCoeff[2];
                this.wrsq.setValue(wr);
                getModel().getRuntime().println("wr²:\t\t" + String.format(Locale.US,"%.5f",wr), JAMS.STANDARD);
            }else if(effMethod.getValue()[i] == this.DSGRAD){
                double dsGrad = DoubleSumAnalysis.dsGrad(valData_1, preData_1);
                this.dsGrad.setValue(dsGrad);
                getModel().getRuntime().println("dsGrad:\t\t" + String.format(Locale.US,"%.5f",dsGrad), JAMS.STANDARD);
            }else if(effMethod.getValue()[i] == this.ABSVOLERROR){
                double volErr = VolumeError.absVolumeError(valData_1, preData_1);
                this.absVolErr.setValue(volErr);
                getModel().getRuntime().println("AVE:\t\t" + String.format(Locale.US,"%.5f",volErr), JAMS.STANDARD);
            }else if(effMethod.getValue()[i] == this.RMSE){
                double rmse = PredictionErrors.rootMeanSquareError(valData_1, preData_1);
                this.rmse.setValue(rmse);
                getModel().getRuntime().println("RMSE:\t\t" + String.format(Locale.US,"%.5f",rmse), JAMS.STANDARD);
            }else if(effMethod.getValue()[i] == this.PBIAS){
                double pbias = VolumeError.pbias(valData_1, preData_1);
                this.pbias.setValue(pbias);
                getModel().getRuntime().println("PBIAS:\t\t" + String.format(Locale.US,"%.5f",pbias), JAMS.STANDARD);
            }
            
        }
    }
}
