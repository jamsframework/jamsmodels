/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jams.j2k.s_salt;

import jams.data.*;
import jams.model.*;
import java.lang.Math.*;

/**
 *
 * @author manfred fink
 * 
 */
@JAMSComponentDescription(title = "J2KContourBanks_layerinterface_out",
author = "Manfred Fink",
description = "Interface for the layered soilwater modul after the calculation of the contour banks")
public class J2KContourBNaCl_layerinterface_out extends JAMSComponent {


    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READWRITE,
    update = JAMSVarDescription.UpdateType.RUN,
    description = "HRU statevar RD2 inflow NaCl")
    public JAMSDoubleArray inRD2_Nacl;
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READWRITE,
    update = JAMSVarDescription.UpdateType.RUN,
    description = "HRU statevar RD2 inflow")
    public JAMSDoubleArray inRD2;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READWRITE,
    update = JAMSVarDescription.UpdateType.RUN,
    description = "HRU statevar RD2 inflow procuced by the contourbanks module")
    public JAMSDouble inRD2_CB;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    update = JAMSVarDescription.UpdateType.INIT,
    description = "An- bzw. Ausschalten des Moduls")
    public JAMSBoolean cbModulAktiv;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    update = JAMSVarDescription.UpdateType.RUN,
    description = "The current hru entity")
    public JAMSEntityCollection hrus;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READWRITE,
    update = JAMSVarDescription.UpdateType.RUN,
    description = "actual LPS water content",
    unit = "l")
    public JAMSDoubleArray actLPS;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READWRITE,
    update = JAMSVarDescription.UpdateType.RUN,
    description = "maximum LPS water content",
    unit = "l")
    public JAMSDoubleArray maxLPS;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    description = " number of soil layers",
    unit = "-",
    lowerBound = 0,
    upperBound = 100)
    public JAMSDouble Layer;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    description = "depth of soil layer",
    unit = "cm",
    lowerBound = 0,
    upperBound = 10000)
    public JAMSDoubleArray layerdepth;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.WRITE,
    description = "depth of soil layers cutted by the trench",
    unit = "cm",
    lowerBound = 0,
    upperBound = 10000)
    public JAMSDouble sumlayer;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READWRITE,
    description = "number of the deepest soil layers cutted by the trench",
    unit = "-",
    lowerBound = 0,
    upperBound = 100)
    public JAMSDouble layermax;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    description = "average LPSsoil saturation in soil layers cutted by the trench",
    unit = "-",
    lowerBound = 0,
    upperBound = 1)
    public JAMSDouble avgsatsoil;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READWRITE,
    update = JAMSVarDescription.UpdateType.RUN,
    description = "RD2 outflow")
    public JAMSDoubleArray outRD2;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    update = JAMSVarDescription.UpdateType.RUN,
    description = "RD2 outflow from contour banks")
    public JAMSDouble outRD2cb;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    description = "surface runoff leaving the HRU without Conturbanks",
    unit = "l",
    lowerBound = 0,
    upperBound = 100000000)
    public JAMSDouble RD1_out_old;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.WRITE,
    description = "surface runoff leaving the HRU goes into Conturbanks",
    unit = "l",
    lowerBound = 0,
    upperBound = 100000000)
    public JAMSDouble RD1_out;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    update = JAMSVarDescription.UpdateType.RUN,
    description = "conturbanks outflow",
    unit = "l")
    public JAMSDouble CB_outflow;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    update = JAMSVarDescription.UpdateType.RUN,
    description = "conturbanks storage",
    unit = "l")
    public JAMSDouble CB_storage;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READWRITE,
    update = JAMSVarDescription.UpdateType.RUN,
    description = " NaCl in surface runoff in kgNaCl/ha")
    public JAMSDouble SurfaceNaCl;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READWRITE,
    update = JAMSVarDescription.UpdateType.RUN,
    description = " NaCl outflow in interflow in kgNaCl/ha")
    public JAMSDoubleArray InterflowNaCl;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READWRITE,
    update = JAMSVarDescription.UpdateType.RUN,
    description = " NaCl inflow in interflow in kgNaCl/ha")
    public JAMSDoubleArray InterflowNaCl_in;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READWRITE,
    update = JAMSVarDescription.UpdateType.RUN,
    description = " NaCl in Contour Banks storage in kgNaCl/ha")
    public JAMSDouble NaCl_CB;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.WRITE,
    update = JAMSVarDescription.UpdateType.RUN,
    description = "conturbanks NaCl outflow in kgNaCl",
    unit = "l")
    public JAMSDouble NaCl_CB_out;
    
    

    public void init() throws JAMSEntity.NoSuchAttributeException {
    }
    
    
    
    public void run() throws JAMSEntity.NoSuchAttributeException {
        Attribute.Entity actHRU;
        actHRU = hrus.getCurrent();
        int i = 0;
        int imax = (int) Layer.getValue();

        double[] runoutRD2 = new double[imax];
        double[] runInterflowNaCl = new double[imax];
        double[] runInterflowNaCl_in = new double[imax];
        double[] runinRD2 = new double[imax];
        double sumRD2 = 0;
        double sumRD2_NaCl = 0;



        while (i < imax) {

            runoutRD2[i] = outRD2.getValue()[i];
            runinRD2[i] = inRD2.getValue()[i];
            runInterflowNaCl[i] = InterflowNaCl.getValue()[i];
            runInterflowNaCl_in[i] = InterflowNaCl_in.getValue()[i];

            if (i < layermax.getValue()) {
                runoutRD2[i] = 0;
                runInterflowNaCl[i] = 0;
                sumRD2 =  sumRD2 + outRD2.getValue()[i];
                sumRD2_NaCl = sumRD2_NaCl + runInterflowNaCl[i];
            } else if (i == layermax.getValue()) {
                runoutRD2[i] = outRD2cb.getValue();
                sumRD2 =  sumRD2 + outRD2.getValue()[i];
                sumRD2_NaCl = sumRD2_NaCl + runInterflowNaCl[i];
                runinRD2[i] = runinRD2[i] + inRD2_CB.getValue();

            }
            i++;
        }


        // in means before the Contour banks module, out afterwards



        double watersum = sumRD2 + RD1_out_old.getValue() + CB_storage.getValue() + inRD2_CB.getValue() + 1.e-10;
        double NaClsum =  sumRD2_NaCl + SurfaceNaCl.getValue() + NaCl_CB.getValue();

        double CB_conc = NaClsum / watersum;

        double NaClrunoff_CB = CB_outflow.getValue() * CB_conc;
        double runoutRD2sum_NaCl = outRD2cb.getValue() * CB_conc;
        double runoutRD1_NaCl = RD1_out.getValue() * CB_conc;
        double runinRD2_NaCl = inRD2_CB.getValue() * CB_conc;

        NaCl_CB_out.setValue(NaClrunoff_CB);

        runInterflowNaCl[(int)layermax.getValue()] = runoutRD2sum_NaCl;
        runInterflowNaCl_in[(int)layermax.getValue()] = runinRD2_NaCl;

        InterflowNaCl.setValue(runInterflowNaCl);
        InterflowNaCl_in.setValue(runInterflowNaCl_in);
        outRD2.setValue(runoutRD2);
        SurfaceNaCl.setValue(runoutRD1_NaCl);
        actHRU.setDouble("cbrunofReachNaCl", NaClrunoff_CB);
        inRD2_CB.setValue(0);



    }
}
