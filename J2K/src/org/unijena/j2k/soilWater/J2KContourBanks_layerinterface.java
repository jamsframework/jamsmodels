/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.unijena.j2k.soilWater;

import jams.data.*;
import jams.model.*;
import java.lang.Math.*;

/**
 *
 * @author manfred fink
 */
@JAMSComponentDescription(title = "J2KContourBanks_layerinterface",
author = "Manfred Fink",
description = "Interface for the layered soilwater modul before the calculation of the contour banks")
public class J2KContourBanks_layerinterface extends JAMSComponent {

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    update = JAMSVarDescription.UpdateType.INIT,
    description = "An- bzw. Ausschalten des Moduls")
    public JAMSBoolean cbModulAktiv;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    update = JAMSVarDescription.UpdateType.RUN,
    description = "The current hru entity")
    public JAMSEntityCollection hrus;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    update = JAMSVarDescription.UpdateType.RUN,
    description = "actual LPS water content",
    unit = "l")
    public JAMSDoubleArray actLPS;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    update = JAMSVarDescription.UpdateType.RUN,
    description = "maximum LPS water content",
    unit = "l")
    public JAMSDoubleArray maxLPS;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    description = "interflow reaching the HRU in every layer",
    unit = "L",
    lowerBound = 0,
    upperBound = 100000000)
    public JAMSDoubleArray RD2_in;
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
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.WRITE,
    description = "interflow reaching the HRU in the layers affected by the trench",
    unit = "l",
    lowerBound = 0,
    upperBound = 100000000)
    public JAMSDouble RD2_insum;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.WRITE,
    description = "average soil saturation of the layers affected by the trench",
    unit = "l",
    lowerBound = 0,
    upperBound = 100000000)
    public JAMSDouble avgsatsoil;
    
    
    boolean modulCBaktiv;
    

    public void init() throws JAMSEntity.NoSuchAttributeException {
        Attribute.Entity actHRU;
        double runsumlayer = 0;
        //which Layer are cutted by the trench 
        int i = 0;
        int runlayermax = 0;

        actHRU = hrus.getCurrent();

        double tiefeGraben_vorn = actHRU.getDouble("cbGrabentiefe_vorn") * 100; //in cm

        while (i < Layer.getValue()) {
            if (runsumlayer < tiefeGraben_vorn) {
                runsumlayer = runsumlayer + layerdepth.getValue()[i];
                runlayermax = i;

            }

            i++;
        }
        sumlayer.setValue(runsumlayer);
        layermax.setValue(runlayermax);
    }

    public void run() throws JAMSEntity.NoSuchAttributeException {

        int i = 0;
        double sumactLPS = 0;
        double summaxLPS = 0;
        double sumRD2_in = 0;
        //calculate soil average saturation and interflow sum in layers affected by the trench

        while (i <= layermax.getValue()) {
            sumactLPS = sumactLPS + actLPS.getValue()[i];
            summaxLPS = summaxLPS + maxLPS.getValue()[i];
            sumRD2_in = sumRD2_in + RD2_in.getValue()[i];
        }

        avgsatsoil.setValue(sumactLPS / summaxLPS);
        RD2_insum.setValue(sumRD2_in);

    }
}
