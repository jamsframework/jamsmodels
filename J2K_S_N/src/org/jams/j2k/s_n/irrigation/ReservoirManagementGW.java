/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jams.j2k.s_n.irrigation;

import jams.data.*;
import jams.model.*;

/**
 *
 * @author c6gohe2
 */
@JAMSComponentDescription(title = "ReservoirManagementGW",
author = "c8fima",
description = "Calculation of the groundwater outtake due to pumping for irrigation")
public class ReservoirManagementGW extends JAMSComponent {

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READWRITE,
    update = JAMSVarDescription.UpdateType.RUN,
    description = "RG2storage")
    public JAMSDouble actRG2;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READWRITE,
    update = JAMSVarDescription.UpdateType.RUN,
    description = "RG2storage")
    public JAMSDouble NActRG2;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    update = JAMSVarDescription.UpdateType.RUN,
    description = "Amount of irrigation Water")
    public JAMSDouble irrigation;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READWRITE,
    update = JAMSVarDescription.UpdateType.RUN,
    description = "Amount of irrigation Water surplus")
    public JAMSDouble irrigation_deficit;

//Berechnung
    public void init() {
        irrigation_deficit.setValue(0.0);

    }

    public void run() {
        double runirri = irrigation.getValue();
        double runactRG2 = actRG2.getValue();
        double rundeficit = 0;

        if (runirri > runactRG2) {

            rundeficit = runirri - runactRG2;

            runactRG2 = 0;

        } else {

            runactRG2 = runactRG2 - runirri;


        }
        actRG2.setValue(runactRG2);
        irrigation_deficit.setValue(rundeficit);




    }
}
