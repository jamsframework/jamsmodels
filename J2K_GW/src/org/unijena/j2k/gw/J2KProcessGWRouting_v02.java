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
public class J2KProcessGWRouting_v02 extends JAMSComponent {

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
    public JAMSDouble outRG2;
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
        @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "Downstream hru entity"
            )
            public Attribute.Entity toPoly;
        @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "Downstream reach entity"
            )
            public Attribute.Entity toReach;
    /*
     *  Component run stages
     */
    double[] gradientNew;
    double gwVolume, run_area, run_Peff, run_gwTableUpper, run_gwTableLower, run_gwDepthUpper, run_gwDepthLower, run_baseHeigth,
            pot_gwTable, sumRG2in, sumRG2in_new, run_RG2act;
    //JAMSEntity[] fP;

    public void init() throws JAMSEntity.NoSuchAttributeException {
    }

    public void run() throws JAMSEntity.NoSuchAttributeException {

        Attribute.Entity entity = entities.getCurrent();

        getModel().getRuntime().println("Current entity ID: " + (int) entity.getDouble("ID") + ".");

        //fP = (JAMSEntity[]) entity.getObject("from_poly");

        //gradientNew = new double[fP.length];

        run_RG2act = this.actRG2.getValue();
        run_area = this.area.getValue();
        int oR = outflowReduction.getValue();

        // ‹bergabe des ausflieﬂenden Grundwassers an den Unterlieger (falls vorhanden)
        if(toPoly.getValue() != null){
            //double RG1out = outRG1.getValue();
            double RG2out = outRG2.getValue();

            //double RG1in = toPoly.getDouble("inRG1");
            double RG2in = toPoly.getDouble("inRG2");

            //RG1in = RG1in + RG1out;
            RG2in = RG2in + RG2out;

            updateGWTable(RG2out);

            toPoly.setDouble("inRG2", RG2in);
            outRG2.setValue(0);

        }else if(toReach.getValue() != null){
            double RG2out = outRG2.getValue();
            double RG2in = toReach.getDouble("inRG2");
            RG2in = RG2in + RG2out;
            RG2out = 0;
            outRG2.setValue(RG2out);
            toReach.setDouble("inRG2", RG2in);
        }else{
            getModel().getRuntime().println("Current entity ID: " + (int)entity.getDouble("ID") + " has no receiver.");
        }

        if (oR != 0) {
            gradientNew = calcGradientReduction();
            run_RG2act = recalcDarcyGWOut(gradientNew);
            updateGWTable(sumRG2in_new);
        }
        

        // actRG2.setValue(run_RG2act);
        // inRG2.setValue(sumRG2in_new);
    }

    private boolean updateGWTable(double sumRG2In) throws JAMSEntity.NoSuchAttributeException {

        run_area = area.getValue();
        run_Peff = Peff.getValue();
        run_baseHeigth = baseHeigth.getValue();
        /*
        for (int i = 0; i < fromPoly.length; i++) {
        gwVolume = fromPoly[i].getDouble("actRG2") / 1000;
        gwVolume = gwVolume - (sumRG2In / 1000);
        run_gwDepthUpper = gwVolume / fromPoly[i].getDouble("area") / fromPoly[i].getDouble("Peff");
        run_gwTableUpper = run_gwDepthUpper + fromPoly[i].getDouble("baseHeigth");
        fromPoly[i].setDouble("pot_gwTable", run_gwTableUpper);
        }
         */

        gwVolume = (run_RG2act - sumRG2In) / 1000;

        run_gwDepthLower = gwVolume / run_area / run_Peff;
        run_gwTableLower = run_gwDepthLower + run_baseHeigth;  // + baseHeigth
        pot_gwTable = run_gwTableLower;

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
        sumRG2in_new = 0;
        for (int i = 0; i < fP.length; i++) {
            rg2out_new = (fP[i].getDouble("calcFactor") * gradientNew[i]) * 86400 * 1000;
            rg2act_new = fP[i].getDouble("actRG2") + fP[i].getDouble("outRG2") - rg2out_new;
            fP[i].setDouble("actRG2", rg2act_new);
            fP[i].setDouble("outRG2", rg2out_new);
            sumRG2in_new = sumRG2in_new + rg2out_new;
        }
        rg2act_new = run_RG2act + sumRG2in_new;
        return rg2act_new;
    }

    public void cleanup() {
    }
}