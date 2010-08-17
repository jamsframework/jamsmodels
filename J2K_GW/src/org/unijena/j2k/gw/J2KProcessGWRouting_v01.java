/*
 * J2KProcessRouting.java
 * Created on 28. November 2005, 09:21
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
package org.unijena.j2k.gw;

import jams.data.*;
import jams.model.*;

/**
 *
 * @author Peter Krause
 */
@JAMSComponentDescription(title = "J2KProcessRouting",
author = "Peter Krause",
description = "Passes the output of the entities as input to the respective reach or unit")
public class J2KProcessGWRouting_v01 extends JAMSComponent {

    /*
     *  Component variables
     */
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READWRITE,
    update = JAMSVarDescription.UpdateType.RUN,
    description = "HRU statevar RG2 inflow")
    public JAMSDouble inRG2;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READWRITE,
    update = JAMSVarDescription.UpdateType.RUN,
    description = "HRU statevar RG2 outflow")
    public JAMSDouble outRG2;                                                   //not used
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READWRITE,
    update = JAMSVarDescription.UpdateType.RUN,
    description = "sum attribute")
    public JAMSEntity[] fP;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    update = JAMSVarDescription.UpdateType.RUN,
    description = "attribute area")
    public JAMSDouble area;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    update = JAMSVarDescription.UpdateType.RUN,
    description = "Heigth of the Aquifer Base in m + NN")
    public JAMSDouble baseHeigth;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    update = JAMSVarDescription.UpdateType.RUN,
    description = "estimated Porosity")
    public JAMSDouble Peff;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READWRITE,
    update = JAMSVarDescription.UpdateType.RUN,
    description = "actual RG2 storage")
    public JAMSDouble actRG2;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READWRITE,
    update = JAMSVarDescription.UpdateType.RUN,
    description = "Groundwater Level")
    public JAMSDouble gwTable;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READWRITE,
    update = JAMSVarDescription.UpdateType.RUN,
    description = "Groundwater Level")
    public JAMSDouble calcFactor;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    update = JAMSVarDescription.UpdateType.RUN,
    description = "Reduction of outflows, 0 = off, 1 = average, 2 = exponential")
    public JAMSInteger outflowReduction;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READWRITE,
    update = JAMSVarDescription.UpdateType.RUN,
    description = "Number of sender-HRUs")
    public JAMSDouble sender;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    update = JAMSVarDescription.UpdateType.RUN,
    description = "The current hru entity")
    public JAMSEntityCollection entities;
    /*
     *  Component run stages
     */
    double[]    gradientNew;
    double      gwVolume, run_area, run_Peff, run_gwTableUpper, run_gwTableLower, run_gwDepthUpper, run_gwDepthLower, run_baseHeigth,
                pot_gwTable, sumRG2in, sumRG2in_new, run_RG2act, run_outRG2;
    int         oR;
    //JAMSEntity[] fP;

    public void init() throws JAMSEntity.NoSuchAttributeException {
    }

    public void run() throws JAMSEntity.NoSuchAttributeException {

        Attribute.Entity entity = entities.getCurrent();

        //getModel().getRuntime().println("Current entity ID: " + (int) entity.getDouble("ID") + ".");

        fP = (JAMSEntity[]) entity.getObject("from_poly");        
        
        //aktuelle Entity:
        run_RG2act = this.actRG2.getValue();

        run_area = this.area.getValue();
        run_Peff = Peff.getValue();
        run_baseHeigth = baseHeigth.getValue();
        
        //Oberlieger:
        
        
        

        gradientNew = new double[fP.length];
        oR = this.outflowReduction.getValue();
        
        if (fP.length != 0) {   //is there any sender-HRU?
            sumRG2in_new = 0;

            // Calculation of the accumulated input
            for (int i = 0; i < fP.length; i++) {
                sumRG2in_new = fP[i].getDouble("pot_outRG2") + sumRG2in_new;
            }

            //sumRG2in_new ist nun der Zwischenspeicher aus dem verteilt wird
            gwVolume = (run_RG2act + sumRG2in_new) / 1000;

            //Calculation of the potential GW-Levels
            boolean flag = true;

            if (oR != 0) {
                updateGWTable(flag);
                gradientNew = calcGradientReduction();
            }else{
                for (int i = 0; i < fP.length; i++) {
                    
                    //es wird der Gradient von Zeitschritt t-1 unverändert verwendet
                    gradientNew[i] = fP[i].getDouble("gwTable") - gwTable.getValue();

                }
            
            flag = false;           //neue Grundwasserspiegellage auf Basis der neu berechneten Zuflüsse

            run_RG2act = recalcDarcyGWOut(gradientNew);
            gwVolume = (run_RG2act + sumRG2in_new) / 1000;
            updateGWTable(flag);
            }
            }else{            double newActRG2;
            double newGenRG2;
                double newOutRG2;
                double newGWTable;
                for (int i = 0; i < fP.length; i++) {
                    newOutRG2 = 0;
                    newActRG2 = fP[i].getDouble("pot_actRG2");
                    newGenRG2 = fP[i].getDouble("pot_genRG2");
                    newGWTable = fP[i].getDouble("pot_gwTable");
                    fP[i].setDouble("actRG2", newActRG2);
                    fP[i].setDouble("outRG2", newOutRG2);
                    fP[i].setDouble("genRG2", newGenRG2);
                    fP[i].setDouble("gwTable", newGWTable);
                }


            }

        actRG2.setValue(run_RG2act);
        inRG2.setValue(sumRG2in_new);
    }

    private boolean updateGWTable(boolean flag) throws JAMSEntity.NoSuchAttributeException {

        for (int i = 0; i < fP.length; i++) {
           if (flag){
           }else{
               gwVolume = fP[i].getDouble("actRG2") / 1000;
               run_gwDepthUpper = gwVolume / fP[i].getDouble("area") / fP[i].getDouble("Peff");
               run_gwTableUpper = run_gwDepthUpper + fP[i].getDouble("baseHeigth");
           }
           
           if (flag){               
           }else{
                fP[i].setDouble("gwTable", run_gwTableUpper);
           }
        }
        
        gwVolume = run_RG2act / 1000;

        run_gwDepthLower = gwVolume / run_area / run_Peff;
        run_gwTableLower = run_gwDepthLower + run_baseHeigth;

        if (flag){
            pot_gwTable = run_gwTableLower;
        }else{
            gwTable.setValue(run_gwTableLower);
        }

        return true;
    }

    private double[] calcGradientReduction() throws JAMSEntity.NoSuchAttributeException {
        double gradientPre;
        double gradientPost;

        for (int i = 0; i < fP.length; i++) {
            gradientPre = fP[i].getDouble("gwTable") - gwTable.getValue();
            gradientPost = fP[i].getDouble("pot_gwTable") - pot_gwTable;
            gradientNew[i] = (gradientPre + gradientPost) / 2;
        }
        return gradientNew;
    }

    private double recalcDarcyGWOut(double[] gradientNew) throws JAMSEntity.NoSuchAttributeException {
        double rg2out_new;
        double rg2act_new;

        if (this.run_gwTableUpper >= this.run_gwTableLower){
        sumRG2in_new = 0;
        for (int i = 0; i < fP.length; i++) {
            rg2out_new = (fP[i].getDouble("calcFactor") * gradientNew[i]) * 86400 * 1000;
            //rg2act_new = fP[i].getDouble("actRG2") - rg2out_new;
            rg2act_new = fP[i].getDouble("pot_actRG2") - rg2out_new;
            fP[i].setDouble("actRG2", rg2act_new);
            double rg2out_sum = fP[i].getDouble("preOutRG2") + rg2out_new;
            fP[i].setDouble("outRG2", rg2out_sum);
            fP[i].setDouble("genRG2", rg2out_new);
            sumRG2in_new = sumRG2in_new + rg2out_new;
        }
        }else{
            getModel().getRuntime().println("Groundwater-Table in Receiver-HRU is higher.");
        }

        rg2act_new = run_RG2act + sumRG2in_new;
        return rg2act_new;
    }

    public void cleanup() {
    }
}