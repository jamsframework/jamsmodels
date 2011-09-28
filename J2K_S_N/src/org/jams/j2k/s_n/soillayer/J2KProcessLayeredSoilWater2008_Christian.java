/*
 * J2KProcessLumpedSoilWater.java
 * Created on 25. November 2005, 13:21
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
package org.jams.j2k.s_n.soillayer;

import jams.data.*;
import jams.model.*;
import java.util.Random;

/**
 *
 * @author Peter Krause modifications by Manfred Fink
 */
/*

 */
@JAMSComponentDescription(title = "J2KProcessLayerdSoilWater",
author = "Peter Krause, modifications by Manfred Fink",
description = "Calculates soil water balance for each HRU with vertical layers")
public class J2KProcessLayeredSoilWater2008_Christian extends JAMSComponent {

    /*
     *  Component variables
     */
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    update = JAMSVarDescription.UpdateType.RUN,
    description = "time")
    public JAMSCalendar time;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    update = JAMSVarDescription.UpdateType.RUN,
    description = "The current hru entities")
    public JAMSEntityCollection entities;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    update = JAMSVarDescription.UpdateType.RUN,
    description = "attribute area")
    public JAMSDouble area;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    update = JAMSVarDescription.UpdateType.RUN,
    description = "attribute slope")
    public JAMSDouble slope;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    update = JAMSVarDescription.UpdateType.RUN,
    description = "sealed grade")
    public JAMSDouble sealedGrade;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READWRITE,
    update = JAMSVarDescription.UpdateType.RUN,
    description = "state variable net rain")
    public JAMSDouble netRain;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READWRITE,
    update = JAMSVarDescription.UpdateType.RUN,
    description = "state variable net snow")
    public JAMSDouble netSnow;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    update = JAMSVarDescription.UpdateType.RUN,
    description = "state variable potET")
    public JAMSDouble potET;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READWRITE,
    update = JAMSVarDescription.UpdateType.RUN,
    description = "state variable actET")
    public JAMSDouble actET;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    update = JAMSVarDescription.UpdateType.RUN,
    description = "snow depth")
    public JAMSDouble snowDepth;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    update = JAMSVarDescription.UpdateType.RUN,
    description = "daily snow melt")
    public JAMSDouble snowMelt;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    update = JAMSVarDescription.UpdateType.RUN,
    description = "horizons")
    public JAMSDouble horizons;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    update = JAMSVarDescription.UpdateType.RUN,
    description = "in cm depth of soil layer")
    public JAMSDoubleArray layerdepth;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    update = JAMSVarDescription.UpdateType.RUN,
    description = "in m actual depth of roots")
    public JAMSDouble rootdepth;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    update = JAMSVarDescription.UpdateType.RUN,
    description = "HRU attribute maximum MPS")
    public JAMSDoubleArray maxMPS;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    update = JAMSVarDescription.UpdateType.RUN,
    description = "HRU attribute maximum LPS")
    public JAMSDoubleArray maxLPS;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READWRITE,
    update = JAMSVarDescription.UpdateType.RUN,
    description = "HRU state var actual MPS")
    public JAMSDoubleArray actMPS;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READWRITE,
    update = JAMSVarDescription.UpdateType.RUN,
    description = "HRU state var actual LPS")
    public JAMSDoubleArray actLPS;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READWRITE,
    update = JAMSVarDescription.UpdateType.RUN,
    description = "HRU state var actual depression storage")
    public JAMSDouble actDPS;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READWRITE,
    update = JAMSVarDescription.UpdateType.RUN,
    description = "HRU state var saturation of MPS")
    public JAMSDoubleArray satMPS;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READWRITE,
    update = JAMSVarDescription.UpdateType.RUN,
    description = "HRU state var saturation of LPS")
    public JAMSDoubleArray satLPS;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.WRITE,
    update = JAMSVarDescription.UpdateType.RUN,
    description = "HRU attribute maximum MPS of soil")
    public JAMSDouble soilMaxMPS;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.WRITE,
    update = JAMSVarDescription.UpdateType.RUN,
    description = "HRU attribute maximum LPS of soil")
    public JAMSDouble soilMaxLPS;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.WRITE,
    update = JAMSVarDescription.UpdateType.RUN,
    description = "HRU state var actual MPS of soil")
    public JAMSDouble soilActMPS;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.WRITE,
    update = JAMSVarDescription.UpdateType.RUN,
    description = "HRU state var actual LPS of soil")
    public JAMSDouble soilActLPS;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.WRITE,
    update = JAMSVarDescription.UpdateType.RUN,
    description = "HRU state var saturation of MPS of soil")
    public JAMSDouble soilSatMPS;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.WRITE,
    update = JAMSVarDescription.UpdateType.RUN,
    description = "HRU state var saturation of LPS of soil")
    public JAMSDouble soilSatLPS;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READWRITE,
    update = JAMSVarDescription.UpdateType.RUN,
    description = "HRU state var saturation of whole soil")
    public JAMSDouble satSoil;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.WRITE,
    update = JAMSVarDescription.UpdateType.RUN,
    description = "HRU statevar infiltration")
    public JAMSDouble infiltration;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.WRITE,
    update = JAMSVarDescription.UpdateType.RUN,
    description = "HRU statevar interflow")
    public JAMSDouble interflow;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.WRITE,
    update = JAMSVarDescription.UpdateType.RUN,
    description = "HRU statevar percolation")
    public JAMSDouble percolation;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READWRITE,
    update = JAMSVarDescription.UpdateType.RUN,
    description = "HRU statevar RD1 inflow")
    public JAMSDouble inRD1;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.WRITE,
    update = JAMSVarDescription.UpdateType.RUN,
    description = "HRU statevar RD1 outflow")
    public JAMSDouble outRD1;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.WRITE,
    update = JAMSVarDescription.UpdateType.RUN,
    description = "HRU statevar RD1 generation")
    public JAMSDouble genRD1;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READWRITE,
    update = JAMSVarDescription.UpdateType.RUN,
    description = "HRU statevar RD2 inflow")
    public JAMSDoubleArray inRD2;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.WRITE,
    update = JAMSVarDescription.UpdateType.RUN,
    description = "HRU statevar RD2 outflow")
    public JAMSDoubleArray outRD2;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.WRITE,
    update = JAMSVarDescription.UpdateType.RUN,
    description = "HRU statevar RD2 generation")
    public JAMSDoubleArray genRD2;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    update = JAMSVarDescription.UpdateType.INIT,
    description = "maximum depression storage [mm]")
    public JAMSDouble soilMaxDPS;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    update = JAMSVarDescription.UpdateType.INIT,
    description = "poly reduction of ETP")
    public JAMSDouble soilPolRed;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    update = JAMSVarDescription.UpdateType.INIT,
    description = "linear reduction of ETP")
    public JAMSDouble soilLinRed;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    update = JAMSVarDescription.UpdateType.INIT,
    description = "maximum infiltration rate in summer [mm/d]")
    public JAMSDouble soilMaxInfSummer;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    update = JAMSVarDescription.UpdateType.INIT,
    description = "maximum infiltration rate in winter [mm/d]")
    public JAMSDouble soilMaxInfWinter;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    update = JAMSVarDescription.UpdateType.INIT,
    description = "maximum infiltration rate on snow [mm/d]")
    public JAMSDouble soilMaxInfSnow;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    update = JAMSVarDescription.UpdateType.INIT,
    description = "maximum infiltration part on sealed areas (gt 80%)")
    public JAMSDouble soilImpGT80;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    update = JAMSVarDescription.UpdateType.INIT,
    description = "maximum infiltration part on sealed areas (lt 80%)")
    public JAMSDouble soilImpLT80;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    update = JAMSVarDescription.UpdateType.INIT,
    description = "MPS/LPS distribution coefficient for inflow")
    public JAMSDouble soilDistMPSLPS;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    update = JAMSVarDescription.UpdateType.INIT,
    description = "MPS/LPS diffusion coefficient")
    public JAMSDouble soilDiffMPSLPS;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    update = JAMSVarDescription.UpdateType.INIT,
    description = "LPS outflow coefficient")
    public JAMSDouble soilOutLPS;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    update = JAMSVarDescription.UpdateType.INIT,
    description = "LPS lateral-vertical distribution coefficient")
    public JAMSDouble soilLatVertLPS;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    update = JAMSVarDescription.UpdateType.INIT,
    description = "maximum percolation rate in soil [mm/d]")
    public JAMSDouble soilMaxPerc;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    update = JAMSVarDescription.UpdateType.INIT,
    description = "maximum percolation rate out of soil [mm/d]")
    public JAMSDouble geoMaxPerc;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    update = JAMSVarDescription.UpdateType.INIT,
    description = "concentration coefficient for RD1")
    public JAMSDouble soilConcRD1;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    update = JAMSVarDescription.UpdateType.INIT,
    description = "concentration coefficient for RD2")
    public JAMSDouble soilConcRD2;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    update = JAMSVarDescription.UpdateType.INIT,
    description = "water-use distribution parameter for Transpiration")
    public JAMSDouble BetaW;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.WRITE,
    update = JAMSVarDescription.UpdateType.RUN,
    description = "intfiltration poritions for the single horizonts")
    public JAMSDoubleArray infiltration_hor;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.WRITE,
    update = JAMSVarDescription.UpdateType.RUN,
    description = "percolation out of the single horizonts")
    public JAMSDoubleArray perco_hor;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.WRITE,
    update = JAMSVarDescription.UpdateType.RUN,
    description = "evapotranspiration out of the single horizonts")
    public JAMSDoubleArray actETP_h;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.WRITE,
    update = JAMSVarDescription.UpdateType.RUN,
    description = "mps diffusion between layers value")
    public JAMSDoubleArray w_layer_diff;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    update = JAMSVarDescription.UpdateType.RUN,
    description = "Array of state variables LAI ")
    public JAMSDouble LAI;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    update = JAMSVarDescription.UpdateType.RUN,
    description = "estimated hydraulicconductivity in cm/d")
    public JAMSDouble Kf_geo;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    update = JAMSVarDescription.UpdateType.RUN,
    description = "in cm/d soil hydraulic conductivity")
    public JAMSDoubleArray kf_h;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.WRITE,
    update = JAMSVarDescription.UpdateType.RUN,
    description = "ID of soil")
    public JAMSDouble soilID;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    update = JAMSVarDescription.UpdateType.RUN,
    description = "Layer MPS diffusion factor > 1 [-]  default = 10")
    public JAMSDouble kdiff_layer;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    update = JAMSVarDescription.UpdateType.RUN,
    description = "Indicates whether roots can penetrate or not the soil layer [-]")
    public JAMSDoubleArray root_h;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.WRITE,
    update = JAMSVarDescription.UpdateType.RUN,
    description = "max Root depth in soil in m")
    public JAMSDouble soil_root;

    //internal state variables
    double run_actDPS, run_latComp, run_vertComp,
            run_actETP, run_area, run_inRD1, run_outRD1, run_genRD1;
    double[] run_maxMPS, run_maxLPS, run_actMPS, run_actLPS, run_satMPS, run_satLPS, run_inRD2, run_satHor, run_outRD2, run_genRD2;
    int nhor;
    boolean debug;

    @Override
    public void run() throws JAMSEntity.NoSuchAttributeException {
        double balMPSstart = 0, balMPSend = 0;
        double balLPSstart = 0, balLPSend = 0;
        double balDPSstart = 0, balDPSend = 0;
        double balIn = 0;
        double balOut = 0;
        double balET = 0;

        debug = false;

        this.run_area = area.getValue();
        double run_slope = slope.getValue();

        this.nhor = (int) horizons.getValue();

        double[] infilhor = new double[nhor];
        double[] perchor = new double[nhor];
        double[] actETP_hor = new double[nhor];

        this.run_satHor = new double[nhor];

        this.run_maxMPS = maxMPS.getValue();
        this.run_maxLPS = maxLPS.getValue();
        this.run_actMPS = actMPS.getValue();
        this.run_actLPS = actLPS.getValue();
        this.run_satMPS = satMPS.getValue();
        this.run_satLPS = satLPS.getValue();
        this.run_actDPS = actDPS.getValue();

        this.run_inRD1 = inRD1.getValue();
        this.run_inRD2 = inRD2.getValue();
        double runkf_h[] = kf_h.getValue();

        balIn += this.run_inRD1;
        double run_inRain = netRain.getValue();
        double run_inSnow = netSnow.getValue();
        double run_potETP = potET.getValue();
        this.run_actETP = actET.getValue();
        double run_snowDepth = snowDepth.getValue();
        double run_snowMelt = snowMelt.getValue();

        this.run_genRD2 = new double[nhor];
        this.run_outRD2 = new double[nhor];
        this.run_latComp = 0;
        this.run_vertComp = 0;
        this.run_genRD1 = 0;
        this.run_outRD1 = 0;


        balET = this.run_actETP;
        balDPSstart = this.run_actDPS;

        for (int h = 0; h < nhor; h++) {
            /** determining inflow of infiltration to MPS */
            balIn += this.run_inRD2[h];
            balMPSstart += this.run_actMPS[h];
            balLPSstart += this.run_actLPS[h];
            this.run_genRD2[h] = 0;
            this.run_outRD2[h] = 0;
        }
        //calculation of saturations first
        //3-4% time
        this.calcSoilSaturations(false);
        
        double flux_h_h1[] = this.layer_diffusion();
        w_layer_diff.setValue(flux_h_h1);

        this.calcSoilSaturations(false);

        // redistributing RD1 and RD2 inflow of antecedent unit
        this.redistRD1_RD2_in();

        // calculation of ETP from dep. Storage and open water bodies
        this.calcPreInfEvaporation(run_potETP);
        double preinfep = this.run_actETP;

        // determining available water for infiltration
        double run_infiltration = run_inRain + run_inSnow + run_snowMelt + this.run_actDPS;
        //balance
        balIn += run_inRain + run_inSnow + run_snowMelt;

        if (run_infiltration < 0) {
            /*System.out.println("negative infiltration!");
            System.out.println("inRain: " + this.run_inRain);
            System.out.println("inSnow: " + this.run_inSnow);
            System.out.println("inSnowMelt: " + this.run_snowMelt);
            System.out.println("inDPS: " + this.run_actDPS);*/
        }              
        this.run_actDPS = 0;        
        //infiltration on impervious areas and water bodies
        //is directly routed as DirectRunoff to the next polygon
        // a better implementation would be the next river reach */
        double run_overlandflow = this.calcOverlandflow(sealedGrade.getValue(), run_infiltration);
        run_infiltration = this.calcInfImperv(sealedGrade.getValue(), run_infiltration);      
        //determining maximal infiltration rate
        double maxInf = this.calcMaxInfiltration(run_snowDepth, runkf_h[0]);
        if (maxInf < run_infiltration) {
            //System.out.getRuntime().println("maxInf:");
            double deltaInf = run_infiltration - maxInf;
            this.run_actDPS = this.run_actDPS + deltaInf;
            run_infiltration = maxInf;
        }
        //5%
        double horETP[] = this.calcMPSEvapotranslayer(true, run_potETP, nhor);
        //5% time
        for (int h = 0; h < nhor; h++) {
            // determining inflow of infiltration to MPS
            infilhor[h] = run_infiltration;
            run_infiltration = this.calcMPSInflow(run_infiltration, h);
            run_infiltration = this.calcLPSInflow(run_infiltration, h);
            infilhor[h] -= run_infiltration;
        }

        if (run_infiltration > 0) {            
            this.run_actDPS += run_infiltration;            
        }

        // 50% time
        for (int h = 0; h < this.nhor; h++) {
            // determining inflow of infiltration to MPS
            //this.run_infiltration = this.calcMPSInflow(this.run_infiltration, h);
            //distributing vertComp from antecedent horzion
            this.run_vertComp = this.calcMPSInflow(this.run_vertComp, h);            
            //this.run_vertComp = this.calcLPSInflow(this.run_vertComp, h);
            // determining transpiration from MPS
            this.calcMPSTranspiration(false, horETP[h], h);
            actETP_hor[h] = run_actETP;
            // inflow to LPS
            this.run_vertComp = this.calcLPSInflow(this.run_vertComp, h);
            if (this.run_vertComp > 0) {
                //System.out.getRuntime().println("VertIn is not zero!");
                //we put it back where it came from, the horizon above!
                this.run_vertComp = this.calcMPSInflow(this.run_vertComp, h - 1);
                this.run_vertComp = this.calcLPSInflow(this.run_vertComp, h - 1);                
            }            
            // determining outflow from LPS
            double MobileWater = 0;
            if (this.run_actLPS[h] > 0) {
                MobileWater = this.calcLPSoutflow(h);
            } 
            // Distribution of MobileWater to the lateral (interflow) and
            // vertical (percolation) flowpaths
            this.calcIntfPercRates(MobileWater, runkf_h, run_slope, h);

            perchor[h] = run_vertComp;
            //determining internal area routing
            this.calcRD2_out(h);

            //determining diffusion from LPS to MPS
            this.calcDiffusion(h);            
        }

        //determining direct runoff from depression storage 
        run_overlandflow += this.calcDirectRunoff(run_slope);
        this.calcRD1_out(run_overlandflow);

        double sumactETP = 0;
        for (int h = 0; h < nhor; h++) {
            balMPSend += this.run_actMPS[h];
            balLPSend += this.run_actLPS[h];
            balOut += this.run_outRD2[h];
            sumactETP += actETP_hor[h];
        }
        balDPSend = this.run_actDPS;
        balET = sumactETP + preinfep;
        balOut += balET;
        balOut += this.run_outRD1;
        balOut += this.run_vertComp;
        
        double balance = balIn + (balMPSstart - balMPSend) + (balLPSstart - balLPSend) + (balDPSstart - balDPSend) - balOut;
        if (Math.abs(balance) > 0.00001) //System.out.println("balance error at : " + time.toString() + " --> "+ balance + " in entity: " + entities.getCurrent().getId());
        {
            satMPS.setValue(this.run_satMPS);
        }
        satLPS.setValue(this.run_satLPS);
        actMPS.setValue(this.run_actMPS);
        actLPS.setValue(this.run_actLPS);
        actDPS.setValue(this.run_actDPS);
        actET.setValue(balET);
        inRD1.setValue(this.run_inRD1);
        inRD2.setValue(this.run_inRD2);
        outRD1.setValue(this.run_outRD1);
        outRD2.setValue(this.run_outRD2);
        genRD1.setValue(this.run_genRD1);
        genRD2.setValue(this.run_genRD2);
        percolation.setValue(this.run_vertComp);
        interflow.setValue(this.run_latComp);
        infiltration_hor.setValue(infilhor);
        perco_hor.setValue(perchor);
        actETP_h.setValue(actETP_hor);

        calcSoilSaturationsOutput();        
    }
    @Override
    public void cleanup() {
    }

    private double calcRunSatSoil1() {
        double soilMaxMps = 0;
        double soilActMps = 0;
        double soilMaxLps = 0;
        double soilActLps = 0;

        for (int h = 0; h < nhor; h++) {
            soilMaxMps += this.run_maxMPS[h];
            soilActMps += this.run_actMPS[h];
            soilMaxLps += this.run_maxLPS[h];
            soilActLps += this.run_actLPS[h];
        }

        if (((soilMaxLps > 0) | (soilMaxMps > 0)) & ((soilActLps > 0) | (soilActMps > 0))) {
            return ((soilActLps + soilActMps) / (soilMaxLps + soilMaxMps));
        }
        return 0;
    }

    /*private double calcTopSatsoil() {
        double soilMaxMps = 0;
        double soilActMps = 0;
        double soilMaxLps = 0;
        double soilActLps = 0;

        double upperMaxMps = 0;
        double upperActMps = 0;
        double upperMaxLps = 0;
        double upperActLps = 0;

        double[] infil_depth = new double[nhor];
        double partdepth = 0;
        double soilinfil = 50;

        for (int h = 0; h < nhor; h++) {
            infil_depth[h] += layerdepth.getValue()[h];
            if (infil_depth[h] <= soilinfil || h == 0) {
                upperMaxMps += this.run_maxMPS[h] * layerdepth.getValue()[h];
                upperActMps += this.run_actMPS[h] * layerdepth.getValue()[h];
                upperMaxLps += this.run_maxLPS[h] * layerdepth.getValue()[h];
                upperActLps += this.run_actLPS[h] * layerdepth.getValue()[h];
                partdepth += layerdepth.getValue()[h];
            } else if (infil_depth[h - 1] <= soilinfil) {
                double lowpart = soilinfil - partdepth;
                upperMaxMps += this.run_maxMPS[h] * lowpart;
                upperActMps += this.run_actMPS[h] * lowpart;
                upperMaxLps += this.run_maxLPS[h] * lowpart;
                upperActMps += this.run_actLPS[h] * lowpart;
            }
            soilMaxMps += this.run_maxMPS[h];
            soilActMps += this.run_actMPS[h];
            soilMaxLps += this.run_maxLPS[h];
            soilActLps += this.run_actLPS[h];
        }

        if (((soilMaxLps > 0) | (soilMaxMps > 0)) & ((soilActLps > 0) | (soilActMps > 0))) {
            return ((upperActLps + upperActMps) / (upperMaxLps + upperMaxMps));
        }
        return 0;
    }*/

    private void calcSoilSaturationsOutput() {
        double soilMaxMps = 0;
        double soilActMps = 0;
        double soilMaxLps = 0;
        double soilActLps = 0;
        double soilSatMps = 0;
        double soilSatLps = 0;

        for (int h = 0; h < nhor; h++) {            
            soilMaxMps += this.run_maxMPS[h];
            soilActMps += this.run_actMPS[h];
            soilMaxLps += this.run_maxLPS[h];
            soilActLps += this.run_actLPS[h];
        }

        if (((soilMaxLps > 0) | (soilMaxMps > 0)) & ((soilActLps > 0) | (soilActMps > 0))) {
            soilSatMps = (soilActMps / soilMaxMps);
            soilSatLps = (soilActLps / soilMaxLps);
        }
        soilMaxMPS.setValue(soilMaxMps);
        soilMaxLPS.setValue(soilMaxLps);
        soilActMPS.setValue(soilActMps);
        soilActLPS.setValue(soilActLps);
        soilSatMPS.setValue(soilSatMps);
        soilSatLPS.setValue(soilSatLps);
    }

    private void calcSoilSaturation(int h, boolean debug) {
        if ((this.run_actLPS[h] > 0) && (this.run_maxLPS[h] > 0)) {
            this.run_satLPS[h] = this.run_actLPS[h] / this.run_maxLPS[h];
        } else {
            this.run_satLPS[h] = 0;
        }

        if ((this.run_actMPS[h] > 0) && (this.run_maxMPS[h] > 0)) {
            this.run_satMPS[h] = this.run_actMPS[h] / this.run_maxMPS[h];
        } else {
            this.run_satMPS[h] = 0;
        }

        if (((this.run_maxLPS[h] > 0) | (this.run_maxMPS[h] > 0)) & ((this.run_actLPS[h] > 0) | (this.run_actMPS[h] > 0))) {
            this.run_satHor[h] = ((this.run_actLPS[h] + this.run_actMPS[h]) / (this.run_maxLPS[h] + this.run_maxMPS[h]));
        } else {
            //this.run_satSoil1 = 0;
        }
    }

    private void calcSoilSaturations(boolean debug) {        
        for (int h = 0; h < nhor; h++) {
            calcSoilSaturation(h, debug);
        }        
    }

    private void redistRD1_RD2_in() throws JAMSEntity.NoSuchAttributeException {
        //RD1 is put to DPS first
        if (this.run_inRD1 > 0) {
            this.run_actDPS += this.run_inRD1;
            this.run_inRD1 = 0;
        }

        for (int h = 0; h < this.nhor; h++) {
            if (this.run_inRD2[h] > 0) {
                this.run_inRD2[h] = this.calcMPSInflow(this.run_inRD2[h], h);
                this.run_inRD2[h] = this.calcLPSInflow(this.run_inRD2[h], h);
                if (this.run_inRD2[h] > 0) {
                    //System.out.getRuntime().println("RD2 of entity " + entity.getDouble("ID") + " and horizon " + h +  " is routed through RD2out: "+this.run_inRD2[h]);
                    this.run_outRD2[h] += this.run_inRD2[h];
                    this.run_inRD2[h] = 0;
                }
            }
        }        
    }

    private double[] layer_diffusion() {
        double flux_h_h1[] = new double[this.nhor - 1];

        for (int h = 0; h < this.nhor - 1; h++) {
            //calculate diffussion factor - order horizontal
            //diffusion only occur when gravitative flux is not dominating
            if ((run_satLPS[h] < 0.05) && (run_satMPS[h] < 0.8 || run_satMPS[h + 1] < 0.8) && (run_satMPS[h] > 0 || run_satMPS[h + 1] > 0)) {
                //calculate gradient
                double gradient_h_h1 = (Math.log10(2 - this.run_satMPS[h]) - Math.log10(2 - this.run_satMPS[h + 1]));

                //calculate resistance

                double satbalance = Math.pow((Math.pow(this.run_satMPS[h], 2) + (Math.pow(this.run_satMPS[h + 1], 2))) / 2.0, 0.5);

                double resistance_h_h1 = Math.log10(satbalance) * -kdiff_layer.getValue();
                //calculate amount of water to equilize saturations in layers

                double avg_sat = ((this.run_maxMPS[h] * this.run_satMPS[h]) + (this.run_maxMPS[h + 1] * this.run_satMPS[h + 1])) / (this.run_maxMPS[h] + this.run_maxMPS[h + 1]);
                double pot_flux = Math.abs((avg_sat - this.run_satMPS[h]) * this.run_maxMPS[h]);

                //calculate water fluxes
                double flux = (pot_flux * gradient_h_h1 / resistance_h_h1);

                if (flux >= 0) {
                    flux_h_h1[h] = Math.min(flux, pot_flux);
                } else {
                    flux_h_h1[h] = Math.max(flux, -pot_flux);
                }
            } else {
                flux_h_h1[h] = 0;
            }
            this.run_actMPS[h] += flux_h_h1[h];
            this.run_actMPS[h + 1] -= flux_h_h1[h];
        }
        return flux_h_h1;
    }

    private void calcPreInfEvaporation(double run_potETP) {
        double deltaETP = run_potETP - this.run_actETP;
        this.run_actETP = run_potETP;

        if (this.run_actDPS > 0) {
            if (this.run_actDPS >= deltaETP) {
                this.run_actDPS -= deltaETP;
            } else {
                this.run_actETP -= (deltaETP - this.run_actDPS);
                this.run_actDPS = 0;
            }
        }
        /** @todo implementation for open water bodies has to be implemented here */
    }

    private double calcOverlandflow(double sealedGrade, double run_infiltration) {
        if (sealedGrade > 0.8) {
            return (1 - soilImpGT80.getValue()) * run_infiltration;
        } else if (sealedGrade > 0 && sealedGrade <= 0.8) {
            return (1 - soilImpLT80.getValue()) * run_infiltration;
        }
        return 0;
    }

    private double calcInfImperv(double sealedGrade, double run_infiltration) {
        double infiltration_result=0;
        if (sealedGrade > 0.8) {            
            return run_infiltration * soilImpGT80.getValue();
        } else if (sealedGrade > 0 && sealedGrade <= 0.8) {            
            return run_infiltration * soilImpLT80.getValue();
        }
        return 0;
    }

    private double calcMaxInfiltration(double run_snowDepth, double runkf_h0) {
        if (run_snowDepth > 0) {
            return this.soilMaxInfSnow.getValue() * runkf_h0 * this.run_area;
        } else {
            return (1 - calcRunSatSoil1()) * soilMaxInfWinter.getValue() * runkf_h0 * this.run_area;
        }
    }

    private double[] calcMPSEvapotranslayer(boolean debug, double run_potETP, int nhor) { //author: Manfred Fink; Method after SWAT
        double[] horETP_local = new double[nhor];
        double pTransp = 0;
        double pEvap = 0;
        double deltaETP = run_potETP - this.run_actETP;
        double soilroot = 0;
        double runrootdepth = (rootdepth.getValue() * 1000) + 10;
        // drifferentiation between evaporation and transpiration
        pTransp = deltaETP;
        double runLAI = LAI.getValue();
        if (runLAI <= 3) {
            pTransp = (deltaETP * runLAI) / 3;
        }
        pEvap = deltaETP - pTransp;
        // EvapoTranspiration loop 1: calculating layer poritions within rootdepth
        for (int i = 0; i < nhor; i++) {
            if (root_h.getValue()[i] == 1.0) {
                soilroot += layerdepth.getValue()[i] * 10;
            }
        }
        double[] transp_hord = new double[nhor];
        double[] evapo_hord = new double[nhor];
        double runlayerdepth = 0;
        for (int i = 0; i < nhor; i++) {
            runlayerdepth += layerdepth.getValue()[i] * 10;
            runrootdepth = Math.min(runrootdepth, soilroot);

            // Transpiration loop 2: calculating transpiration distribution function with depth in layers
            transp_hord[i] = (pTransp * (1 - Math.exp(-BetaW.getValue() * (runlayerdepth / runrootdepth)))) / (1 - Math.exp(-BetaW.getValue()));
            transp_hord[i] = Math.min(transp_hord[i], pTransp);
            // Evaporation loop 2: calculating evaporation distribution function with depth in layers
            evapo_hord[i] = pEvap * (runlayerdepth / (runlayerdepth + (Math.exp(2.374 - (0.00713 * runlayerdepth)))));
            evapo_hord[i] = Math.min(evapo_hord[i], pEvap);

            //allocation of the rest Evap to the lowest horizon ............
            if (i == nhor - 1) {
                evapo_hord[i] = pEvap;
                transp_hord[i] = pTransp;
            }
        }


        double test = 0;
        double horbal = 0;
        for (int i = 0; i < nhor; i++) {
            double transp_hor = transp_hord[i];
            double evapo_hor = evapo_hord[i];
            if (i > 0) {
                transp_hor -= transp_hord[i - 1];
                evapo_hor -= evapo_hord[i - 1];
            }
            horETP_local[i] = transp_hor + evapo_hor;

            if (debug) {
                horbal = horbal + horETP_local[i];
                test = deltaETP - horbal;
            }
        }

        if ((test > 0.0000001 || test < -0.0000001) && debug) {
            //System.out.println("evaporation balance error = " + test);
        }
        this.soil_root.setValue(soilroot / 1000);
        return horETP_local;
    }

    private void calcMPSTranspiration(boolean debug, double horETP, int hor) {
        double maxTrans = 0;
        /** delta ETP */
        double deltaETP = horETP;

        /**linear reduction after MENZEL 1997 was chosen*/
        //if(this.etp_reduction == 0){
        if (this.soilLinRed.getValue() > 0) {
            /** reduction if actual saturation is smaller than linear factor */
            if (this.run_satMPS[hor] < soilLinRed.getValue()) {
                //if(this.sat_MPS < this.etp_linRed){
                double reductionFactor = this.run_satMPS[hor] / soilLinRed.getValue();

                //double reductionFactor = this.sat_MPS / etp_linRed;
                maxTrans = deltaETP * reductionFactor;
            } else {
                maxTrans = deltaETP;
            }
        } /** polynomial reduction after KRAUSE 2001 was chosen */
        else if (soilPolRed.getValue() > 0) {
            //else if(this.etp_reduction == 1){
            double sat_factor = -10. * Math.pow((1 - this.run_satMPS[hor]), soilPolRed.getValue());
            //double sat_factor = Math.pow((1 - this.sat_MPS), etp_polRed);
            double reductionFactor = Math.pow(10, sat_factor);
            maxTrans = Math.min(deltaETP * reductionFactor, deltaETP);
        }
        maxTrans = Math.min(maxTrans, run_actMPS[hor]);

        this.run_actMPS[hor] -= maxTrans;

        /** recalculation actual ETP */
        this.run_actETP = maxTrans;
        this.calcSoilSaturation(hor,debug);

        /* @todo: ETP from water bodies has to be implemented here */
    }

    private double calcMPSInflow(double infiltration, int hor) {
        double inflow = infiltration;

        /**checking if MPS can take all the water */
        if (inflow < (this.run_maxMPS[hor] - this.run_actMPS[hor])) {
            /** if MPS is empty it takes all the water */
            if (this.run_actMPS[hor] == 0) {
                this.run_actMPS[hor] += inflow;
                inflow = 0;
            } /** MPS is partly filled and gets part of the water */
            else {
                double alpha = this.soilDistMPSLPS.getValue();
                //if sat_MPS is 0 the next equation would produce an error,
                //therefore it is set to MPS_sat is set to 0.0000001 in that case
                if (this.run_satMPS[hor] == 0) {
                    this.run_satMPS[hor] = 0.0000001;
                }
                double inMPS = inflow * (1. - Math.exp(-1 * alpha / this.run_satMPS[hor]));
                this.run_actMPS[hor] += inMPS;
                inflow -= inMPS;
            }
        } /** infiltration exceeds storage capacity of MPS */
        else {
            double deltaMPS = this.run_maxMPS[hor] - this.run_actMPS[hor];
            this.run_actMPS[hor] = this.run_maxMPS[hor];
            inflow -= deltaMPS;
        }
        /** updating saturations */
        this.calcSoilSaturation(hor,false);
        return inflow;
    }
    /*
     *problem overflow is put to DPS, we have to deal with that problem
     */

    private double calcLPSInflow(double infiltration, int hor) {
        this.run_actLPS[hor] += infiltration;
        infiltration = 0;
        /** if LPS is saturated depression Storage occurs */
        if (this.run_actLPS[hor] > this.run_maxLPS[hor]) {
            infiltration = (this.run_actLPS[hor] - this.run_maxLPS[hor]);
            this.run_actLPS[hor] = this.run_maxLPS[hor];
        }
        /** updating saturations */
        calcSoilSaturation(hor,false);
        return infiltration;
    }

    private double calcLPSoutflow(int hor) {
        double alpha = this.soilOutLPS.getValue();

        if (this.run_satLPS[hor] == 1.0) {
            this.run_satLPS[hor] = 0.999999;
        }

        double LPSoutflow = Math.pow(this.run_satHor[hor], alpha) * this.run_actLPS[hor];
        LPSoutflow = Math.min(LPSoutflow, this.run_actLPS[hor]);
        LPSoutflow = Math.max(LPSoutflow, 0);

        this.run_actLPS[hor] -= LPSoutflow;

        return LPSoutflow;
    }

    private void calcIntfPercRates(double MobileWater, double runkf_h[], double run_slope, int hor) {
        if (MobileWater > 0) {
            double slope_weight = (Math.tan(run_slope * (Math.PI / 180.))) * this.soilLatVertLPS.getValue();

            /** potential part of percolation */
            double part_perc = (1 - slope_weight);
            part_perc = Math.max(part_perc, 0);
            part_perc = Math.min(part_perc, 1);

            /** potential part of interflow */
            double part_intf = (1 - part_perc);

            this.run_latComp += MobileWater * part_intf;
            this.run_vertComp += MobileWater * part_perc;
            double maxPerc = 0;
            /** checking if percolation rate is limited by parameter */
            if (hor == nhor - 1) {
                maxPerc = this.geoMaxPerc.getValue() * this.run_area * this.Kf_geo.getValue() / 86.4;
                /*if (Kf_geo.getValue() < 10){
                maxPerc = 0;
                }*/
                // 86.4 cm/d "middle" hydraulic conductivity in geology (1 E-5 m/s)
                if (this.run_vertComp > maxPerc) {
                    double rest = this.run_vertComp - maxPerc;
                    this.run_vertComp = maxPerc;
                    this.run_latComp = this.run_latComp + rest;
                }
            } else {
                maxPerc = this.soilMaxPerc.getValue() * this.run_area * runkf_h[hor + 1] / 86.4;
                // 86.4 cm/d "middle" hydraulic conductivity in geology (1 E-5 m/s)
                if (this.run_vertComp > maxPerc) {
                    double rest = this.run_vertComp - maxPerc;
                    this.run_vertComp = maxPerc;
                    this.run_latComp = this.run_latComp + rest;
                }
            }
        } /** no MobileWater available */
        else {
            this.run_latComp = 0;
            this.run_vertComp = 0;
        }
    }

    private double calcDirectRunoff(double run_slope) {
        double directRunoff = 0;
        if (this.run_actDPS > 0) {
            double maxDep = this.soilMaxDPS.getValue() * this.run_area;
            /** depression storage on slopes is half the normal dep. storage */
            if (run_slope > 5.0) {
                maxDep /= 2.0;
            }

            if (this.run_actDPS > maxDep) {
                directRunoff = this.run_actDPS - maxDep;
                this.run_actDPS = maxDep;
            }
        }
        if (directRunoff < 0) {
            //System.out.println("directRunoff is negative! --> " + directRunoff );
        }
        return directRunoff;
    }

    private void calcRD2_out(int h) {
        /** lateral interflow */
        //switched of 15-03-2004
        //double RD2_output_factor = this.conc_index / this.parameter.getDouble("conc_recRD2");
        double RD2_output_factor = 1. / this.soilConcRD2.getValue();
        RD2_output_factor = Math.max(RD2_output_factor, 0);
        RD2_output_factor = Math.min(RD2_output_factor, 1);

        if (RD2_output_factor > 1) {
            RD2_output_factor = 1;
        } else if (RD2_output_factor < 0) {
            RD2_output_factor = 0;
        }

        /** real RD2 output */
        double RD2_output = this.run_latComp * RD2_output_factor;
        /** rest is put back to LPS Storage */
        this.run_actLPS[h] += (this.run_latComp - RD2_output);
        this.run_outRD2[h] += RD2_output;
        this.run_genRD2[h] = this.run_outRD2[h];// - this.in_RD2;
        if (this.run_genRD2[h] < 0) {
            this.run_genRD2[h] = 0;
        }

        this.run_latComp = 0;
    }

    private void calcRD1_out(double run_overlandflow) {
        /** DIRECT OVERLANDFLOW */
        //switched off 15-03-2004
        //double RD1_output_factor = this.conc_index / this.parameter.getDouble("conc_recRD1");
        double RD1_output_factor = 1. / this.soilConcRD1.getValue();
        RD1_output_factor = Math.max(RD1_output_factor, 0);
        RD1_output_factor = Math.min(RD1_output_factor, 1);

        double RD1_output = run_overlandflow * RD1_output_factor;
        /** rest is put back to dep. Storage */
        this.run_actDPS += (run_overlandflow - RD1_output);
        this.run_outRD1 += RD1_output;
        this.run_genRD1 = this.run_outRD1;       
    }

    private void calcDiffusion(int h) {
        double diffusion = 0;       
        double deltaMPS = this.run_maxMPS[h] - this.run_actMPS[h];
        //if sat_MPS is 0 the diffusion equation would produce an error,
        //for this (unlikely) case diffusion is set to zero
        if (this.run_satMPS[h] != 0.0) {
            double diff = this.soilDiffMPSLPS.getValue();
            //new equation like all other exps 04.03.04
            diffusion = this.run_actLPS[h] * (1. - Math.exp((-1. * diff) / this.run_satMPS[h]));
        }
        if (diffusion > this.run_actLPS[h]) {
            diffusion = this.run_actLPS[h];
        }
        /** MPS can take all the water from diffusion */
        if (diffusion < deltaMPS) {
            this.run_actMPS[h] = this.run_actMPS[h] + diffusion;
            this.run_actLPS[h] = this.run_actLPS[h] - diffusion;
        } /** MPS can take only part of the water */
        else {
            double rest = this.run_maxMPS[h] - this.run_actMPS[h];
            this.run_actMPS[h] = this.run_maxMPS[h];
            this.run_actLPS[h] = this.run_actLPS[h] - rest;
        }
        calcSoilSaturation(h,debug);
    }
    static Random generator;

    public static double rnd(double low, double high) {
        return generator.nextDouble() * (high - low) + low;
    }

    public static void main(String[] args) throws Exception {
        long time_ref = 0;
        long time_test = 0;
        double diff = 0;
        generator = new Random(0);
        for (int x = 0; x < 10000; x++) {
            J2KProcessLayeredSoilWater2008_Christian test = new J2KProcessLayeredSoilWater2008_Christian();
            J2KProcessLayeredSoilWater2008 ref = new J2KProcessLayeredSoilWater2008();

            int horz = generator.nextInt(20) + 1;

            ref.time = (JAMSCalendar) JAMSDataFactory.createCalendar();
            ref.time.set(2000, 10, 10);
            test.time = ref.time;

            ref.soil_root = (JAMSDouble) JAMSDataFactory.createDouble();
            test.soil_root = (JAMSDouble) JAMSDataFactory.createDouble();

            ref.soilMaxMPS = (JAMSDouble) JAMSDataFactory.createDouble();
            test.soilMaxMPS = (JAMSDouble) JAMSDataFactory.createDouble();

            ref.soilMaxLPS = (JAMSDouble) JAMSDataFactory.createDouble();
            test.soilMaxLPS = (JAMSDouble) JAMSDataFactory.createDouble();

            ref.soilActMPS = (JAMSDouble) JAMSDataFactory.createDouble();
            test.soilActMPS = (JAMSDouble) JAMSDataFactory.createDouble();

            ref.soilActLPS = (JAMSDouble) JAMSDataFactory.createDouble();
            test.soilActLPS = (JAMSDouble) JAMSDataFactory.createDouble();

            ref.soilSatMPS = (JAMSDouble) JAMSDataFactory.createDouble();
            test.soilSatMPS = (JAMSDouble) JAMSDataFactory.createDouble();

            ref.soilSatLPS = (JAMSDouble) JAMSDataFactory.createDouble();
            test.soilSatLPS = (JAMSDouble) JAMSDataFactory.createDouble();

            ref.infiltration = (JAMSDouble) JAMSDataFactory.createDouble();
            test.infiltration = (JAMSDouble) JAMSDataFactory.createDouble();

            ref.interflow = (JAMSDouble) JAMSDataFactory.createDouble();
            test.interflow = (JAMSDouble) JAMSDataFactory.createDouble();

            ref.percolation = (JAMSDouble) JAMSDataFactory.createDouble();
            test.percolation = (JAMSDouble) JAMSDataFactory.createDouble();

            ref.outRD1 = (JAMSDouble) JAMSDataFactory.createDouble();
            test.outRD1 = (JAMSDouble) JAMSDataFactory.createDouble();

            ref.genRD1 = (JAMSDouble) JAMSDataFactory.createDouble();
            test.genRD1 = (JAMSDouble) JAMSDataFactory.createDouble();

            double area = rnd(10, 100000);
            ref.area = (JAMSDouble) JAMSDataFactory.createDouble();
            test.area = (JAMSDouble) JAMSDataFactory.createDouble();
            ref.area.setValue(area);
            test.area.setValue(area);

            double slope = rnd(0.01, 2);
            ref.slope = (JAMSDouble) JAMSDataFactory.createDouble();
            test.slope = (JAMSDouble) JAMSDataFactory.createDouble();
            ref.slope.setValue(slope);
            test.slope.setValue(slope);

            double sealedGrade = rnd(0.01, 2);
            ref.sealedGrade = (JAMSDouble) JAMSDataFactory.createDouble();
            test.sealedGrade = (JAMSDouble) JAMSDataFactory.createDouble();
            ref.sealedGrade.setValue(sealedGrade);
            test.sealedGrade.setValue(sealedGrade);

            double netRain = rnd(0.0, 200);
            if (netRain > 150) {
                netRain = 0;
            }
            ref.netRain = (JAMSDouble) JAMSDataFactory.createDouble();
            test.netRain = (JAMSDouble) JAMSDataFactory.createDouble();
            ref.netRain.setValue(netRain);
            test.netRain.setValue(netRain);

            double netSnow = rnd(0.0, 200);
            ref.netSnow = (JAMSDouble) JAMSDataFactory.createDouble();
            test.netSnow = (JAMSDouble) JAMSDataFactory.createDouble();
            if (netSnow > 100) {
                netSnow = 0;
            }

            ref.netSnow.setValue(netSnow);
            test.netSnow.setValue(netSnow);

            double potET = rnd(0.0, 50);
            ref.potET = (JAMSDouble) JAMSDataFactory.createDouble();
            test.potET = (JAMSDouble) JAMSDataFactory.createDouble();
            ref.potET.setValue(potET);
            test.potET.setValue(potET);

            double actET = rnd(0.0, 50);
            ref.actET = (JAMSDouble) JAMSDataFactory.createDouble();
            test.actET = (JAMSDouble) JAMSDataFactory.createDouble();
            ref.actET.setValue(actET);
            test.actET.setValue(actET);

            double snowDepth = rnd(0.0, 100);
            ref.snowDepth = (JAMSDouble) JAMSDataFactory.createDouble();
            test.snowDepth = (JAMSDouble) JAMSDataFactory.createDouble();
            ref.snowDepth.setValue(snowDepth);
            test.snowDepth.setValue(snowDepth);

            double snowMelt = rnd(0.0, 50);
            ref.snowMelt = (JAMSDouble) JAMSDataFactory.createDouble();
            test.snowMelt = (JAMSDouble) JAMSDataFactory.createDouble();
            ref.snowMelt.setValue(snowMelt);
            test.snowMelt.setValue(snowMelt);

            ref.horizons = (JAMSDouble) JAMSDataFactory.createDouble();
            test.horizons = (JAMSDouble) JAMSDataFactory.createDouble();
            ref.horizons.setValue(horz);
            test.horizons = ref.horizons;

            double layerdepth[] = new double[horz];
            double maxMPS[] = new double[horz];
            double maxLPS[] = new double[horz];
            double actMPS[] = new double[horz];
            double actLPS[] = new double[horz];
            double satMPS[] = new double[horz];
            double satLPS[] = new double[horz];
            double kf_h[] = new double[horz];
            double root_h[] = new double[horz];
            double in_RD2[] = new double[horz];
            double out_RD2[] = new double[horz];

            for (int i = 0; i < horz; i++) {
                layerdepth[i] = rnd(1, 100);
                maxMPS[i] = rnd(-0.01, 10);
                maxLPS[i] = rnd(-0.01, 10);

                satMPS[i] = rnd(-0.01, 1);
                satLPS[i] = rnd(-0.01, 1);

                actMPS[i] = rnd(-0.01, maxMPS[i]);
                actLPS[i] = rnd(-0.01, maxLPS[i]);

                kf_h[i] = rnd(0.05, 10.0);
                in_RD2[i] = rnd(0.05, 20.0);

                root_h[i] = generator.nextInt(2);
            }

            ref.inRD2 = (JAMSDoubleArray) JAMSDataFactory.createDoubleArray();
            test.inRD2 = (JAMSDoubleArray) JAMSDataFactory.createDoubleArray();
            ref.inRD2.setValue(in_RD2);
            test.inRD2.setValue(in_RD2.clone());

            ref.infiltration_hor = (JAMSDoubleArray) JAMSDataFactory.createDoubleArray();
            test.infiltration_hor = (JAMSDoubleArray) JAMSDataFactory.createDoubleArray();
            ref.infiltration_hor.setValue(new double[horz]);
            test.infiltration_hor.setValue(new double[horz]);

            ref.perco_hor = (JAMSDoubleArray) JAMSDataFactory.createDoubleArray();
            test.perco_hor = (JAMSDoubleArray) JAMSDataFactory.createDoubleArray();
            ref.perco_hor.setValue(new double[horz]);
            test.perco_hor.setValue(new double[horz]);

            ref.actETP_h = (JAMSDoubleArray) JAMSDataFactory.createDoubleArray();
            test.actETP_h = (JAMSDoubleArray) JAMSDataFactory.createDoubleArray();
            ref.actETP_h.setValue(new double[horz]);
            test.actETP_h.setValue(new double[horz]);

            ref.w_layer_diff = (JAMSDoubleArray) JAMSDataFactory.createDoubleArray();
            test.w_layer_diff = (JAMSDoubleArray) JAMSDataFactory.createDoubleArray();
            ref.w_layer_diff.setValue(new double[horz]);
            test.w_layer_diff.setValue(new double[horz]);

            ref.outRD2 = (JAMSDoubleArray) JAMSDataFactory.createDoubleArray();
            test.outRD2 = (JAMSDoubleArray) JAMSDataFactory.createDoubleArray();
            ref.outRD2.setValue(new double[horz]);
            test.outRD2.setValue(new double[horz]);

            ref.genRD2 = (JAMSDoubleArray) JAMSDataFactory.createDoubleArray();
            test.genRD2 = (JAMSDoubleArray) JAMSDataFactory.createDoubleArray();
            ref.genRD2.setValue(new double[horz]);
            test.genRD2.setValue(new double[horz]);



            ref.layerdepth = (JAMSDoubleArray) JAMSDataFactory.createDoubleArray();
            test.layerdepth = (JAMSDoubleArray) JAMSDataFactory.createDoubleArray();
            ref.layerdepth.setValue(layerdepth);
            test.layerdepth.setValue(layerdepth.clone());

            double rootdepth = rnd(0.0, 10);
            ref.rootdepth = (JAMSDouble) JAMSDataFactory.createDouble();
            test.rootdepth = (JAMSDouble) JAMSDataFactory.createDouble();
            ref.rootdepth.setValue(rootdepth);
            test.rootdepth.setValue(rootdepth);

            ref.maxMPS = (JAMSDoubleArray) JAMSDataFactory.createDoubleArray();
            test.maxMPS = (JAMSDoubleArray) JAMSDataFactory.createDoubleArray();
            ref.maxMPS.setValue(maxMPS);
            test.maxMPS.setValue(maxMPS.clone());

            ref.maxLPS = (JAMSDoubleArray) JAMSDataFactory.createDoubleArray();
            test.maxLPS = (JAMSDoubleArray) JAMSDataFactory.createDoubleArray();
            ref.maxLPS.setValue(maxLPS);
            test.maxLPS.setValue(maxLPS.clone());

            ref.actMPS = (JAMSDoubleArray) JAMSDataFactory.createDoubleArray();
            test.actMPS = (JAMSDoubleArray) JAMSDataFactory.createDoubleArray();
            ref.actMPS.setValue(actMPS);
            test.actMPS.setValue(actMPS.clone());

            ref.actLPS = (JAMSDoubleArray) JAMSDataFactory.createDoubleArray();
            test.actLPS = (JAMSDoubleArray) JAMSDataFactory.createDoubleArray();
            ref.actLPS.setValue(actLPS);
            test.actLPS.setValue(actLPS.clone());

            ref.satMPS = (JAMSDoubleArray) JAMSDataFactory.createDoubleArray();
            test.satMPS = (JAMSDoubleArray) JAMSDataFactory.createDoubleArray();
            ref.satMPS.setValue(satMPS);
            test.satMPS.setValue(satMPS.clone());

            ref.satLPS = (JAMSDoubleArray) JAMSDataFactory.createDoubleArray();
            test.satLPS = (JAMSDoubleArray) JAMSDataFactory.createDoubleArray();
            ref.satLPS.setValue(satLPS);
            test.satLPS.setValue(satLPS.clone());

            double actDPS = rnd(0.0, 10);
            ref.actDPS = (JAMSDouble) JAMSDataFactory.createDouble();
            test.actDPS = (JAMSDouble) JAMSDataFactory.createDouble();
            ref.actDPS.setValue(actDPS);
            test.actDPS.setValue(actDPS);

            double satSoil = rnd(0.0, 10);
            ref.satSoil = (JAMSDouble) JAMSDataFactory.createDouble();
            test.satSoil = (JAMSDouble) JAMSDataFactory.createDouble();
            ref.satSoil.setValue(satSoil);
            test.satSoil.setValue(satSoil);

            double inRD1 = rnd(0.0, 50);
            ref.inRD1 = (JAMSDouble) JAMSDataFactory.createDouble();
            test.inRD1 = (JAMSDouble) JAMSDataFactory.createDouble();
            ref.inRD1.setValue(inRD1);
            test.inRD1.setValue(inRD1);

            double soilMaxDPS = rnd(0.0, 50);
            ref.soilMaxDPS = (JAMSDouble) JAMSDataFactory.createDouble();
            test.soilMaxDPS = (JAMSDouble) JAMSDataFactory.createDouble();
            ref.soilMaxDPS.setValue(soilMaxDPS);
            test.soilMaxDPS.setValue(soilMaxDPS);

            double soilID = rnd(0.0, 50);
            ref.soilID = (JAMSDouble) JAMSDataFactory.createDouble();
            test.soilID = (JAMSDouble) JAMSDataFactory.createDouble();
            ref.soilID.setValue(soilMaxDPS);
            test.soilID.setValue(soilMaxDPS);

            double soilPolRed = rnd(0.05, 10);
            ref.soilPolRed = (JAMSDouble) JAMSDataFactory.createDouble();
            test.soilPolRed = (JAMSDouble) JAMSDataFactory.createDouble();
            ref.soilPolRed.setValue(soilPolRed);
            test.soilPolRed.setValue(soilPolRed);

            double soilLinRed = rnd(0.05, 10);
            ref.soilLinRed = (JAMSDouble) JAMSDataFactory.createDouble();
            test.soilLinRed = (JAMSDouble) JAMSDataFactory.createDouble();
            ref.soilLinRed.setValue(soilLinRed);
            test.soilLinRed.setValue(soilLinRed);

            double soilMaxInfSummer = rnd(10.05, 250);
            ref.soilMaxInfSummer = (JAMSDouble) JAMSDataFactory.createDouble();
            test.soilMaxInfSummer = (JAMSDouble) JAMSDataFactory.createDouble();
            ref.soilMaxInfSummer.setValue(soilMaxInfSummer);
            test.soilMaxInfSummer.setValue(soilMaxInfSummer);

            double soilMaxInfWinter = rnd(10.05, 250);
            ref.soilMaxInfWinter = (JAMSDouble) JAMSDataFactory.createDouble();
            test.soilMaxInfWinter = (JAMSDouble) JAMSDataFactory.createDouble();
            ref.soilMaxInfWinter.setValue(soilMaxInfWinter);
            test.soilMaxInfWinter.setValue(soilMaxInfWinter);

            double soilMaxInfSnow = rnd(0.05, 50);
            ref.soilMaxInfSnow = (JAMSDouble) JAMSDataFactory.createDouble();
            test.soilMaxInfSnow = (JAMSDouble) JAMSDataFactory.createDouble();
            ref.soilMaxInfSnow.setValue(soilMaxInfSnow);
            test.soilMaxInfSnow.setValue(soilMaxInfSnow);

            double soilImpGT80 = rnd(0.05, 50);
            ref.soilImpGT80 = (JAMSDouble) JAMSDataFactory.createDouble();
            test.soilImpGT80 = (JAMSDouble) JAMSDataFactory.createDouble();
            ref.soilImpGT80.setValue(soilImpGT80);
            test.soilImpGT80.setValue(soilImpGT80);

            double soilImpLT80 = rnd(0.05, 50);
            ref.soilImpLT80 = (JAMSDouble) JAMSDataFactory.createDouble();
            test.soilImpLT80 = (JAMSDouble) JAMSDataFactory.createDouble();
            ref.soilImpLT80.setValue(soilImpLT80);
            test.soilImpLT80.setValue(soilImpLT80);

            double soilDistMPSLPS = rnd(0.0, 1.0);
            ref.soilDistMPSLPS = (JAMSDouble) JAMSDataFactory.createDouble();
            test.soilDistMPSLPS = (JAMSDouble) JAMSDataFactory.createDouble();
            ref.soilDistMPSLPS.setValue(soilDistMPSLPS);
            test.soilDistMPSLPS.setValue(soilDistMPSLPS);

            double soilDiffMPSLPS = rnd(0.0, 1.0);
            ref.soilDiffMPSLPS = (JAMSDouble) JAMSDataFactory.createDouble();
            test.soilDiffMPSLPS = (JAMSDouble) JAMSDataFactory.createDouble();
            ref.soilDiffMPSLPS.setValue(soilDiffMPSLPS);
            test.soilDiffMPSLPS.setValue(soilDiffMPSLPS);

            double soilOutLPS = rnd(0.0, 1.0);
            ref.soilOutLPS = (JAMSDouble) JAMSDataFactory.createDouble();
            test.soilOutLPS = (JAMSDouble) JAMSDataFactory.createDouble();
            ref.soilOutLPS.setValue(soilOutLPS);
            test.soilOutLPS.setValue(soilOutLPS);

            double soilLatVertLPS = rnd(0.0, 10.0);
            ref.soilLatVertLPS = (JAMSDouble) JAMSDataFactory.createDouble();
            test.soilLatVertLPS = (JAMSDouble) JAMSDataFactory.createDouble();
            ref.soilLatVertLPS.setValue(soilLatVertLPS);
            test.soilLatVertLPS.setValue(soilLatVertLPS);

            double soilMaxPerc = rnd(5.0, 100.0);
            ref.soilMaxPerc = (JAMSDouble) JAMSDataFactory.createDouble();
            test.soilMaxPerc = (JAMSDouble) JAMSDataFactory.createDouble();
            ref.soilMaxPerc.setValue(soilMaxPerc);
            test.soilMaxPerc.setValue(soilMaxPerc);

            double geoMaxPerc = rnd(5.0, 100.0);
            ref.geoMaxPerc = (JAMSDouble) JAMSDataFactory.createDouble();
            test.geoMaxPerc = (JAMSDouble) JAMSDataFactory.createDouble();
            ref.geoMaxPerc.setValue(geoMaxPerc);
            test.geoMaxPerc.setValue(geoMaxPerc);

            double soilConcRD1 = rnd(0.0, 10.0);
            ref.soilConcRD1 = (JAMSDouble) JAMSDataFactory.createDouble();
            test.soilConcRD1 = (JAMSDouble) JAMSDataFactory.createDouble();
            ref.soilConcRD1.setValue(soilConcRD1);
            test.soilConcRD1.setValue(soilConcRD1);

            double soilConcRD2 = rnd(0.0, 10.0);
            ref.soilConcRD2 = (JAMSDouble) JAMSDataFactory.createDouble();
            test.soilConcRD2 = (JAMSDouble) JAMSDataFactory.createDouble();
            ref.soilConcRD2.setValue(soilConcRD2);
            test.soilConcRD2.setValue(soilConcRD2);

            double BetaW = rnd(0.05, 10.0);
            ref.BetaW = (JAMSDouble) JAMSDataFactory.createDouble();
            test.BetaW = (JAMSDouble) JAMSDataFactory.createDouble();
            ref.BetaW.setValue(BetaW);
            test.BetaW.setValue(BetaW);

            double LAI = rnd(0.05, 10.0);
            ref.LAI = (JAMSDouble) JAMSDataFactory.createDouble();
            test.LAI = (JAMSDouble) JAMSDataFactory.createDouble();
            ref.LAI.setValue(LAI);
            test.LAI.setValue(LAI);

            double Kf_geo = rnd(0.05, 10.0);
            ref.Kf_geo = (JAMSDouble) JAMSDataFactory.createDouble();
            test.Kf_geo = (JAMSDouble) JAMSDataFactory.createDouble();
            ref.Kf_geo.setValue(Kf_geo);
            test.Kf_geo.setValue(Kf_geo);


            ref.kf_h = (JAMSDoubleArray) JAMSDataFactory.createDoubleArray();
            test.kf_h = (JAMSDoubleArray) JAMSDataFactory.createDoubleArray();
            ref.kf_h.setValue(kf_h);
            test.kf_h.setValue(kf_h);

            ref.root_h = (JAMSDoubleArray) JAMSDataFactory.createDoubleArray();
            test.root_h = (JAMSDoubleArray) JAMSDataFactory.createDoubleArray();
            ref.root_h.setValue(root_h);
            test.root_h.setValue(root_h);

            double kdiff_layer = rnd(1, 20.0);
            ref.kdiff_layer = (JAMSDouble) JAMSDataFactory.createDouble();
            test.kdiff_layer = (JAMSDouble) JAMSDataFactory.createDouble();
            ref.kdiff_layer.setValue(kdiff_layer);
            test.kdiff_layer.setValue(kdiff_layer);

            if (generator.nextBoolean()) {
                long t1 = System.nanoTime();
                ref.run();
                long t2 = System.nanoTime();
                time_ref += (t2 - t1);

                t1 = System.nanoTime();
                test.run();
                t2 = System.nanoTime();
                time_test += (t2 - t1);
            } else {
                long t1 = System.nanoTime();
                test.run();
                long t2 = System.nanoTime();
                time_test += (t2 - t1);

                t1 = System.nanoTime();
                ref.run();
                t2 = System.nanoTime();
                time_ref += (t2 - t1);
            }


            double dnetRain = Math.abs(test.netRain.getValue() - ref.netRain.getValue());
            double dnetSnow = Math.abs(test.netSnow.getValue() - ref.netSnow.getValue());
            double dactET = Math.abs(test.actET.getValue() - ref.actET.getValue());
            double dactDPS = Math.abs(test.actDPS.getValue() - ref.actDPS.getValue());
            double dsatSoil = Math.abs(test.satSoil.getValue() - ref.satSoil.getValue());
            double dinRD1 = Math.abs(test.inRD1.getValue() - ref.inRD1.getValue());
            double dsoil_root = Math.abs(test.soil_root.getValue() - ref.soil_root.getValue());

            diff = diff + dnetRain + dnetSnow + dactET + dactDPS + dsatSoil + dinRD1 + dsoil_root;

            double dsoilMaxMPS = Math.abs(test.soilMaxMPS.getValue() - ref.soilMaxMPS.getValue());
            double dsoilMaxLPS = Math.abs(test.soilMaxLPS.getValue() - ref.soilMaxLPS.getValue());
            double dsoilActMPS = Math.abs(test.soilActMPS.getValue() - ref.soilActMPS.getValue());
            double dsoilActLPS = Math.abs(test.soilActLPS.getValue() - ref.soilActLPS.getValue());

            diff += dsoilMaxMPS + dsoilMaxLPS + dsoilActMPS + dsoilActLPS;

            double dsoilSatMPS = Math.abs(test.soilSatMPS.getValue() - ref.soilSatMPS.getValue());
            double dsoilSatLPS = Math.abs(test.soilSatLPS.getValue() - ref.soilSatLPS.getValue());
            double dinfiltration = Math.abs(test.infiltration.getValue() - ref.infiltration.getValue());
            double dpercolation = Math.abs(test.percolation.getValue() - ref.percolation.getValue());

            diff += dsoilSatMPS + dsoilSatLPS + dinfiltration + dpercolation;

            double doutRD1 = Math.abs(test.outRD1.getValue() - ref.outRD1.getValue());
            double dgenRD1 = Math.abs(test.genRD1.getValue() - ref.genRD1.getValue());

            diff += doutRD1 + dgenRD1;

            double dsatMPS = 0;
            double dsatLPS = 0;
            double dinRD2 = 0;
            double dinfiltration_hor = 0;
            double dperco_hor = 0;
            double dactETP_h = 0;
            double doutRD2 = 0;
            double dw_layer_diff = 0;
            double dgenRD2 = 0;

            for (int k = 0; k < horz; k++) {
                dsatMPS += Math.abs(test.satMPS.getValue()[k] - ref.satMPS.getValue()[k]);
                dsatLPS += Math.abs(test.satLPS.getValue()[k] - ref.satLPS.getValue()[k]);
                dinRD2 += Math.abs(test.inRD2.getValue()[k] - ref.inRD2.getValue()[k]);
                dinfiltration_hor += Math.abs(test.infiltration_hor.getValue()[k] - ref.infiltration_hor.getValue()[k]);
                dperco_hor += Math.abs(test.perco_hor.getValue()[k] - ref.perco_hor.getValue()[k]);
                dactETP_h += Math.abs(test.actETP_h.getValue()[k] - ref.actETP_h.getValue()[k]);
                if (k < horz - 1) {
                    dw_layer_diff += Math.abs(test.w_layer_diff.getValue()[k] - ref.w_layer_diff.getValue()[k]);
                }
                doutRD2 += Math.abs(test.outRD2.getValue()[k] - ref.outRD2.getValue()[k]);
                dgenRD2 += Math.abs(test.genRD2.getValue()[k] - ref.genRD2.getValue()[k]);
            }

            diff += dsatMPS + dsatLPS + dinRD2 + dinfiltration_hor + dperco_hor + dactETP_h + dw_layer_diff + doutRD2 + dgenRD2;
        }
        System.out.println("Time reference:" + time_ref);
        System.out.println("Time test:" + time_test);
        System.out.println("Speed-Up:" + (double) time_ref / time_test);
        System.out.println("Difference:" + diff);
    }
}
