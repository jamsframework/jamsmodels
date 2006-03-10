/*
 * StandardEfficiencyCalculator_1.java
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
        public class StandardEfficiencyCalculator_1 extends JAMSComponent {
    
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
            description = "Entity providing observed and predicted data"
            )
            public JAMSEntity sourceEntity;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.INIT,
            description = "Efficiency method used"
            )
            public JAMSIntegerArray effMethod = new JAMSIntegerArray();
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "Input Prediction variable"
            )
            public JAMSDouble preInputData;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "Input validation variable"
            )
            public JAMSDouble valInputData;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "Prediction variable"
            )
            public JAMSDoubleArray preData = new JAMSDoubleArray();
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "Validation variable"
            )
            public JAMSDoubleArray valData = new JAMSDoubleArray();
    
    private final int E1 = 1;
    private final int E2 = 2;
    private final int LOG_E1 = 3;
    private final int LOG_E2 = 4;
    private final int IOA_1 = 5;
    private final int IOA_2 = 6;
    private final int R2 = 7;
    private final int WR2 = 8;
    
    private final int TOTAL_PERIOD = 0;
    private final int HYDROLOGICAL_YEAR = 1;
    private final int CALENDAR_YEAR = 2;
    
    //private double[] valData;
    //private double[] preData;
    
    private int counter = 0;
    
    /*
     *  Component run stages
     */
    
    public void init() throws JAMSEntity.NoSuchAttributeException {
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
        System.out.println("Startdate:\t" + sd.toString());
        System.out.println("Enddate:\t" + ed.toString());
        System.out.println("Tsteps:\t" + ts);
        
        double[] v = new double[ts];
        double[] p = new double[ts];
        valData.setValue(v);
        preData.setValue(p);
        
        counter = 0;
        
    }
    
    public void run() throws JAMSEntity.NoSuchAttributeException {
        double[] v = valData.getValue();
        double[] p = preData.getValue();
        
        v[counter] = valInputData.getValue();
        p[counter] = preInputData.getValue();
        
        valData.setValue(v);
        valData.setValue(p);
        
        this.counter++;
    }
    
    public void cleanup() throws JAMSEntity.NoSuchAttributeException {
        //eff calculations
        System.out.println("Counter is:\t " + counter);
        JAMSIntegerArray method = new JAMSIntegerArray();
        double[] valData_1, preData_1;
        //data without first year
        if(counter > 366){
            valData_1 = new double[counter - 366];
            preData_1 = new double[counter - 366];
            int count = 0;
            for(int i = 366; i < counter; i++){
                valData_1[count] = valData.getValue()[i];
                preData_1[count] = preData.getValue()[i];
                count++;
            }
            for(int i = 0; i < effMethod.getValue().length; i++){
                if(effMethod.getValue()[i] == this.E1){
                    double e1 = NashSutcliffe.efficiency(preData_1, valData_1, 1);
                    System.out.println("e1 a-1:\t " + e1);
                }else if(effMethod.getValue()[i] == this.E2){
                    double e2 = NashSutcliffe.efficiency(preData_1, valData_1, 2);
                    System.out.println("e2 a-1:\t " + e2);
                }else if(effMethod.getValue()[i] == this.LOG_E1){
                    double le1 = NashSutcliffe.logEfficiency(preData_1, valData_1, 1);
                    System.out.println("log e1 a-1:\t " + le1);
                }else if(effMethod.getValue()[i] == this.LOG_E2){
                    double le2 = NashSutcliffe.logEfficiency(preData_1, valData_1, 2);
                    System.out.println("log e2 a-1:\t " + le2);
                }else if(effMethod.getValue()[i] == this.IOA_1){
                    double ioa1 = IndexOfAgreement.calc_IOA(preData_1, valData_1, 1);
                    System.out.println("ioa1 a-1:\t " + ioa1);
                }else if(effMethod.getValue()[i] == this.IOA_2){
                    double ioa2 = IndexOfAgreement.calc_IOA(preData_1, valData_1, 2);
                    System.out.println("ioa2 a-1:\t " + ioa2);
                }else if(effMethod.getValue()[i] == this.R2){
                    double[] rCoeff = Regression.calcLinReg(valData_1, preData_1);
                    System.out.println("r² a-1:\t " + rCoeff[2]);
                    System.out.println("grad a-1:\t" + rCoeff[1]);
                }else if(effMethod.getValue()[i] == this.WR2){
                    double[] rCoeff = Regression.calcLinReg(valData_1, preData_1);
                    double wr;
                    if(rCoeff[1] <= 1)
                        wr = Math.abs(rCoeff[1]) * rCoeff[2];
                    else
                        wr = Math.pow(Math.abs(rCoeff[1]), -1.0) * rCoeff[2];
                    System.out.println("wr² a-1:\t " + wr);
                }
                
            }
        }
        for(int i = 0; i < effMethod.getValue().length; i++){
            if(effMethod.getValue()[i] == this.E1){
                double e1 = NashSutcliffe.efficiency(preData.getValue(), valData.getValue(), 1);
                System.out.println("e1:\t " + e1);
            }else if(effMethod.getValue()[i] == this.E2){
                double e2 = NashSutcliffe.efficiency(preData.getValue(), valData.getValue(), 2);
                System.out.println("e2:\t\t" + e2);
            }else if(effMethod.getValue()[i] == this.LOG_E1){
                double le1 = NashSutcliffe.logEfficiency(preData.getValue(), valData.getValue(), 1);
                System.out.println("log e1:\t " + le1);
            }else if(effMethod.getValue()[i] == this.LOG_E2){
                double le2 = NashSutcliffe.logEfficiency(preData.getValue(), valData.getValue(), 2);
                System.out.println("log e2:\t " + le2);
            }else if(effMethod.getValue()[i] == this.IOA_1){
                double ioa1 = IndexOfAgreement.calc_IOA(preData.getValue(), valData.getValue(), 1);
                System.out.println("ioa1:\t " + ioa1);
            }else if(effMethod.getValue()[i] == this.IOA_2){
                double ioa2 = IndexOfAgreement.calc_IOA(preData.getValue(), valData.getValue(), 2);
                System.out.println("ioa2:\t " + ioa2);
            }else if(effMethod.getValue()[i] == this.R2){
                double[] rCoeff = Regression.calcLinReg(valData.getValue(), preData.getValue());
                System.out.println("r²:\t " + rCoeff[2]);
                System.out.println("grad:\t" + rCoeff[1]);
            }else if(effMethod.getValue()[i] == this.WR2){
                double[] rCoeff = Regression.calcLinReg(valData.getValue(), preData.getValue());
                double wr;
                    if(rCoeff[1] <= 1)
                        wr = Math.abs(rCoeff[1]) * rCoeff[2];
                    else
                        wr = Math.pow(Math.abs(rCoeff[1]), -1.0) * rCoeff[2];
                System.out.println("wr²:\t " + wr);
            }
        }
    }
}
