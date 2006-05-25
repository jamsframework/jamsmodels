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
            public JAMSTimeInterval timeInterval;
    
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
            description = ""
            )
            public JAMSDouble e1;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            update = JAMSVarDescription.UpdateType.RUN,
            description = ""
            )
            public JAMSDouble e2;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            update = JAMSVarDescription.UpdateType.RUN,
            description = ""
            )
            public JAMSDouble le1;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            update = JAMSVarDescription.UpdateType.RUN,
            description = ""
            )
            public JAMSDouble le2;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            update = JAMSVarDescription.UpdateType.RUN,
            description = ""
            )
            public JAMSDouble ioa1;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            update = JAMSVarDescription.UpdateType.RUN,
            description = ""
            )
            public JAMSDouble ioa2;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            update = JAMSVarDescription.UpdateType.RUN,
            description = ""
            )
            public JAMSDouble rsq;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            update = JAMSVarDescription.UpdateType.RUN,
            description = ""
            )
            public JAMSDouble grad;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            update = JAMSVarDescription.UpdateType.RUN,
            description = ""
            )
            public JAMSDouble wrsq;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            update = JAMSVarDescription.UpdateType.RUN,
            description = ""
            )
            public JAMSDouble dsGrad;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            update = JAMSVarDescription.UpdateType.RUN,
            description = ""
            )
            public JAMSDouble absVolErr;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            update = JAMSVarDescription.UpdateType.RUN,
            description = ""
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
    
    /*
     *  Component run stages
     */
    
    public void init() {
        JAMSCalendar sd = this.timeInterval.getStart();
        JAMSCalendar ed = this.timeInterval.getEnd();
        int tres = this.timeInterval.getTimeUnit();
        long sdMS = sd.getTimeInMillis();
        long edMS = ed.getTimeInMillis();
        this.counter = 0;
        long tsteps = 0;
        if(tres == sd.DAY_OF_YEAR){
            tsteps = (edMS - sdMS) / (1000 * 60 * 60 * 24);
            tsteps = tsteps + 1 + 1;
            
        }
        //int ts = (int)tsteps;
        int ts = (int) this.getContext().getNumberOfIterations();
        JAMS.sendInfoMsg("Startdate:\t" + sd.toString());
        JAMS.sendInfoMsg("Enddate:\t" + ed.toString());
        JAMS.sendInfoMsg("Tsteps:\t" + ts);
        
        valData = new double[ts];
        preData = new double[ts];
        
        counter = 0;
        
    }
    
    public void run() {
        this.valData[counter] = validation.getValue();
        this.preData[counter] = prediction.getValue();
        this.counter++;
    }
    
    public void cleanup() {
        //eff calculations
        JAMS.sendInfoMsg("Counter is:\t " + counter);
        JAMSIntegerArray method = new JAMSIntegerArray();
        double[] valData_1, preData_1;
        //data without first year
        if(counter > 366){
            valData_1 = new double[counter - 366];
            preData_1 = new double[counter - 366];
            int count = 0;
            for(int i = 366; i < counter; i++){
                valData_1[count] = valData[i];
                preData_1[count] = preData[i];
                count++;
            }
            for(int i = 0; i < effMethod.getValue().length; i++){
                if(effMethod.getValue()[i] == this.E1){
                    double e1 = NashSutcliffe.efficiency(preData_1, valData_1, 1);
                    JAMS.sendInfoMsg("e1 a-1:\t " + e1);
                }else if(effMethod.getValue()[i] == this.E2){
                    double e2 = NashSutcliffe.efficiency(preData_1, valData_1, 2);
                    JAMS.sendInfoMsg("e2 a-1:\t " + e2);
                }else if(effMethod.getValue()[i] == this.LOG_E1){
                    double le1 = NashSutcliffe.logEfficiency(preData_1, valData_1, 1);
                    JAMS.sendInfoMsg("log e1 a-1:\t " + le1);
                }else if(effMethod.getValue()[i] == this.LOG_E2){
                    double le2 = NashSutcliffe.logEfficiency(preData_1, valData_1, 2);
                    JAMS.sendInfoMsg("log e2 a-1:\t " + le2);
                }else if(effMethod.getValue()[i] == this.IOA_1){
                    double ioa1 = IndexOfAgreement.calc_IOA(preData_1, valData_1, 1);
                    JAMS.sendInfoMsg("ioa1 a-1:\t " + ioa1);
                }else if(effMethod.getValue()[i] == this.IOA_2){
                    double ioa2 = IndexOfAgreement.calc_IOA(preData_1, valData_1, 2);
                    JAMS.sendInfoMsg("ioa2 a-1:\t " + ioa2);
                }else if(effMethod.getValue()[i] == this.R2){
                    double[] rCoeff = Regression.calcLinReg(valData_1, preData_1);
                    JAMS.sendInfoMsg("r² a-1:\t " + rCoeff[2]);
                    JAMS.sendInfoMsg("grad a-1:\t" + rCoeff[1]);
                }else if(effMethod.getValue()[i] == this.WR2){
                    double[] rCoeff = Regression.calcLinReg(valData_1, preData_1);
                    double wr;
                    if(rCoeff[1] <= 1)
                        wr = Math.abs(rCoeff[1]) * rCoeff[2];
                    else
                        wr = Math.pow(Math.abs(rCoeff[1]), -1.0) * rCoeff[2];
                    JAMS.sendInfoMsg("wr² a-1:\t " + wr);
                }else if(effMethod.getValue()[i] == this.DSGRAD){
                    double dsGrad = DoubleSumAnalysis.dsGrad(valData_1, preData_1);
                    this.dsGrad.setValue(dsGrad);
                    JAMS.sendInfoMsg("dsGrad a-1:\t" + dsGrad);
                }else if(effMethod.getValue()[i] == this.ABSVOLERROR){
                    double volErr = VolumeError.absVolumeError(valData_1, preData_1);
                    this.absVolErr.setValue(volErr);
                    JAMS.sendInfoMsg("absVolumeError a-1:\t" + volErr);
                }else if(effMethod.getValue()[i] == this.RMSE){
                    double rmse = PredictionErrors.rootMeanSquareError(valData_1, preData_1);
                    this.rmse.setValue(rmse);
                    JAMS.sendInfoMsg("RMSE a-1:\t" + rmse);
                }
                
            }
        }
        for(int i = 0; i < effMethod.getValue().length; i++){
            if(effMethod.getValue()[i] == this.E1){
                double e1 = NashSutcliffe.efficiency(preData, valData, 1);
                this.e1.setValue(e1);
                JAMS.sendInfoMsg("e1:\t " + e1);
            }else if(effMethod.getValue()[i] == this.E2){
                double e2 = NashSutcliffe.efficiency(preData, valData, 2);
                this.e2.setValue(e2);
                JAMS.sendInfoMsg("e2:\t" + e2);
            }else if(effMethod.getValue()[i] == this.LOG_E1){
                double le1 = NashSutcliffe.logEfficiency(preData, valData, 1);
                this.le1.setValue(le1);
                JAMS.sendInfoMsg("log e1:\t " + le1);
            }else if(effMethod.getValue()[i] == this.LOG_E2){
                double le2 = NashSutcliffe.logEfficiency(preData, valData, 2);
                this.le2.setValue(le2);
                JAMS.sendInfoMsg("log e2:\t " + le2);
            }else if(effMethod.getValue()[i] == this.IOA_1){
                double ioa1 = IndexOfAgreement.calc_IOA(preData, valData, 1);
                this.ioa1.setValue(ioa1);
                JAMS.sendInfoMsg("ioa1:\t " + ioa1);
            }else if(effMethod.getValue()[i] == this.IOA_2){
                double ioa2 = IndexOfAgreement.calc_IOA(preData, valData, 2);
                this.ioa2.setValue(ioa2);
                JAMS.sendInfoMsg("ioa2:\t " + ioa2);
            }else if(effMethod.getValue()[i] == this.R2){
                double[] rCoeff = Regression.calcLinReg(valData, preData);
                this.rsq.setValue(rCoeff[2]);
                this.grad.setValue(rCoeff[1]);
                JAMS.sendInfoMsg("r²:\t " + rCoeff[2]);
                JAMS.sendInfoMsg("grad:\t" + rCoeff[1]);
            }else if(effMethod.getValue()[i] == this.WR2){
                double[] rCoeff = Regression.calcLinReg(valData, preData);
                double wr;
                if(rCoeff[1] <= 1)
                    wr = Math.abs(rCoeff[1]) * rCoeff[2];
                else
                    wr = Math.pow(Math.abs(rCoeff[1]), -1.0) * rCoeff[2];
                this.wrsq.setValue(wr);
                JAMS.sendInfoMsg("wr²:\t " + wr);
            }else if(effMethod.getValue()[i] == this.DSGRAD){
                double dsGrad = DoubleSumAnalysis.dsGrad(valData, preData);
                this.dsGrad.setValue(dsGrad);
                JAMS.sendInfoMsg("dsGrad:\t" + dsGrad);
            }else if(effMethod.getValue()[i] == this.ABSVOLERROR){
                double volErr = VolumeError.absVolumeError(valData, preData);
                this.absVolErr.setValue(volErr);
                JAMS.sendInfoMsg("absVolumeError a-1:\t" + volErr);
            }else if(effMethod.getValue()[i] == this.RMSE){
                double rmse = PredictionErrors.rootMeanSquareError(valData, preData);
                this.rmse.setValue(rmse);
                JAMS.sendInfoMsg("RMSE a-1:\t" + rmse);
            }
            
        }
    }
}
