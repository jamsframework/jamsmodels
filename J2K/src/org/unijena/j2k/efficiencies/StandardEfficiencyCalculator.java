/*
 * StandardEfficiencyCalculator.java
 * Created on 24. November 2005, 09:\t48
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

import org.unijena.j2k.statistics.Regression;
import org.unijena.jams.JAMS;
import org.unijena.jams.data.*;
import org.unijena.jams.model.*;

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
            description = "Nash-Sutcliffe-efficiency with power 1.0"
            )
            public JAMSDouble e1;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "Nash-Sutcliffe-efficiency with power 2.0"
            )
            public JAMSDouble e2;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "logarithmic Nash-Sutcliffe-efficiency with power 1.0"
            )
            public JAMSDouble le1;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "logarithmic Nash-Sutcliffe-efficiency with power 2.0"
            )
            public JAMSDouble le2;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "Willmot's index of agreement with power 1.0"
            )
            public JAMSDouble ioa1;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "Willmot's index of agreement with power 2.0"
            )
            public JAMSDouble ioa2;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "coefficient of determination r²"
            )
            public JAMSDouble rsq;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "gradient of linear regression"
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
            description = "gradient of double sum regression"
            )
            public JAMSDouble dsGrad;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "absolute volume error"
            )
            public JAMSDouble absVolErr;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "root mean square error"
            )
            public JAMSDouble rmse;
    
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
    
    private final int TOTAL_PERIOD = 0;
    private final int HYDROLOGICAL_YEAR = 1;
    private final int CALENDAR_YEAR = 2;
    
    private double[] valData;
    private double[] preData;
    
    private int counter = 0;
    
    private int interValStart = 0;
    private int interValEnd = 0;
    
    private int effTsteps = 0;
    
    /*
     *  Component run stages
     */
    
    public void init() {
        
        //some checking if time intervals correlate well
        //....
        //....
        this.counter = 0;
        JAMSCalendar model_sd = this.modelTimeInterval.getStart();
        JAMSCalendar model_ed = this.modelTimeInterval.getEnd();
        int model_tres = this.modelTimeInterval.getTimeUnit();
        long sdMod = model_sd.getTimeInMillis();
        long edMod = model_ed.getTimeInMillis();
        long model_tsteps = 0;
        if(model_tres == model_sd.DAY_OF_YEAR){
            model_tsteps = (edMod - sdMod) / (1000 * 60 * 60 * 24);
            model_tsteps = model_tsteps + 1 + 1;
        }
        
        JAMSCalendar eff_sd = this.effTimeInterval.getStart();
        JAMSCalendar eff_ed = this.effTimeInterval.getEnd();
        int eff_tres = this.effTimeInterval.getTimeUnit();
        long sdEff = eff_sd.getTimeInMillis();
        long edEff = eff_ed.getTimeInMillis();
        
        if(eff_tres == eff_sd.DAY_OF_YEAR){
            this.effTsteps = (int)((edEff - sdEff) / (1000 * 60 * 60 * 24));
            this.effTsteps = this.effTsteps + 1;
        }
        //int ts = (int)tsteps;
        int ts = (int) this.getContext().getNumberOfIterations();
        getModel().getRuntime().println("effStartdate:\t" + eff_sd.toString(), JAMS.STANDARD);
        getModel().getRuntime().println("effEnddate:\t" + eff_ed.toString(), JAMS.STANDARD);
        
        
        valData = new double[(int)model_tsteps];
        preData = new double[(int)model_tsteps];
        
        counter = 0;
        
        //determine start and end array index for timeInterval
        
        if(eff_tres == eff_sd.DAY_OF_YEAR){
            this.interValStart =(int)((sdEff - sdMod) / (1000 * 60 * 60 * 24));
            this.interValEnd = this.interValStart + this.effTsteps;
        }
        int junk = 0;
    }
    
    public void run() {
        this.valData[counter] = validation.getValue();
        this.preData[counter] = prediction.getValue();
        this.counter++;
    }
    
    public void cleanup() {
        
        getModel().getRuntime().println("\n*************************************************************", JAMS.STANDARD);
        getModel().getRuntime().println("Efficiencies for period:\t " + this.effTimeInterval.toString(), JAMS.STANDARD);
        getModel().getRuntime().println("*************************************************************", JAMS.STANDARD);
        JAMSIntegerArray method = new JAMSIntegerArray();
        double[] valData_1, preData_1;
        
        valData_1 = new double[this.effTsteps];
        preData_1 = new double[this.effTsteps];
        int count = 0;
        for(int i = this.interValStart; i < this.interValEnd; i++){
            valData_1[count] = valData[i];
            preData_1[count] = preData[i];
            count++;
        }
        for(int i = 0; i < effMethod.getValue().length; i++){
            if(effMethod.getValue()[i] == this.E1){
                double e1 = NashSutcliffe.efficiency(preData_1, valData_1, 1);
                getModel().getRuntime().println("e1:\t " + e1, JAMS.STANDARD);
            }else if(effMethod.getValue()[i] == this.E2){
                double e2 = NashSutcliffe.efficiency(preData_1, valData_1, 2);
                getModel().getRuntime().println("e2:\t " + e2, JAMS.STANDARD);
            }else if(effMethod.getValue()[i] == this.LOG_E1){
                double le1 = NashSutcliffe.logEfficiency(preData_1, valData_1, 1);
                getModel().getRuntime().println("log_e1:\t " + le1, JAMS.STANDARD);
            }else if(effMethod.getValue()[i] == this.LOG_E2){
                double le2 = NashSutcliffe.logEfficiency(preData_1, valData_1, 2);
                getModel().getRuntime().println("log_e2:\t " + le2, JAMS.STANDARD);
            }else if(effMethod.getValue()[i] == this.IOA_1){
                double ioa1 = IndexOfAgreement.calc_IOA(preData_1, valData_1, 1, getModel());
                getModel().getRuntime().println("ioa1:\t " + ioa1, JAMS.STANDARD);
            }else if(effMethod.getValue()[i] == this.IOA_2){
                double ioa2 = IndexOfAgreement.calc_IOA(preData_1, valData_1, 2, getModel());
                getModel().getRuntime().println("ioa2:\t " + ioa2, JAMS.STANDARD);
            }else if(effMethod.getValue()[i] == this.R2){
                double[] rCoeff = Regression.calcLinReg(valData_1, preData_1);
                getModel().getRuntime().println("r²:\t " + rCoeff[2], JAMS.STANDARD);
                getModel().getRuntime().println("grad:\t" + rCoeff[1], JAMS.STANDARD);
            }else if(effMethod.getValue()[i] == this.WR2){
                double[] rCoeff = Regression.calcLinReg(valData_1, preData_1);
                double wr;
                if(rCoeff[1] <= 1)
                    wr = Math.abs(rCoeff[1]) * rCoeff[2];
                else
                    wr = Math.pow(Math.abs(rCoeff[1]), -1.0) * rCoeff[2];
                getModel().getRuntime().println("wr²:\t " + wr, JAMS.STANDARD);
            }else if(effMethod.getValue()[i] == this.DSGRAD){
                double dsGrad = DoubleSumAnalysis.dsGrad(valData_1, preData_1);
                this.dsGrad.setValue(dsGrad);
                getModel().getRuntime().println("dsGrad:\t" + dsGrad, JAMS.STANDARD);
            }else if(effMethod.getValue()[i] == this.ABSVOLERROR){
                double volErr = VolumeError.absVolumeError(valData_1, preData_1);
                this.absVolErr.setValue(volErr);
                getModel().getRuntime().println("absVolumeError:\t" + volErr, JAMS.STANDARD);
            }else if(effMethod.getValue()[i] == this.RMSE){
                double rmse = PredictionErrors.rootMeanSquareError(valData_1, preData_1);
                this.rmse.setValue(rmse);
                getModel().getRuntime().println("RMSE:\t" + rmse, JAMS.STANDARD);
            }
            
        }
    }
}
