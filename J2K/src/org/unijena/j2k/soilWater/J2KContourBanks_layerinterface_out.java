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
 * 
 */
@JAMSComponentDescription(
title="J2KContourBanks_layerinterface_out",
        author="Manfred Fink",
        description="Interface for the layered soilwater modul after the calculation of the contour banks"
        )
public class J2KContourBanks_layerinterface_out extends JAMSComponent {

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
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.WRITE,
    update = JAMSVarDescription.UpdateType.RUN,
    description = "RD2 outflow")
    public JAMSDoubleArray outRD2;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    update = JAMSVarDescription.UpdateType.RUN,
    description = "RD2 outflow from contour banks")
    public JAMSDouble outRD2cb;
    boolean modulCBaktiv;
    double kalibZufluss;

    public void init() throws JAMSEntity.NoSuchAttributeException {
      
    }

    public void run() throws JAMSEntity.NoSuchAttributeException {

        int i = 0;
        int imax = (int)Layer.getValue();
        
        double[]  runoutRD2 = new double[imax];


        

        while (i <= imax) {

            runoutRD2[i] = outRD2.getValue()[i];

            if (i < layermax.getValue()) {
                runoutRD2[i] = 0;
            }else if(i == layermax.getValue()){
                runoutRD2[i] = outRD2cb.getValue();
            }
            
        }
        
        outRD2.setValue(runoutRD2);


    }
}
