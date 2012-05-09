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
@JAMSComponentDescription(title = "Calculation of irrigation water N-concentration",
author = "c8fima",
description = "Calculation of irrigation water N-concentration")
public class calc_irigation_conc_N extends JAMSComponent {

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "HRU crop class"
            )
            public JAMSDouble storageInput;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "HRU crop class"
            )
            public JAMSDouble storageInputN;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "HRU crop class"
            )
            public JAMSDouble storage;



    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "HRU crop class"
            )
            public JAMSDouble irrigationsum;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "HRU crop class"
            )
            public JAMSDouble irrigationpart;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "N-concentration of the irrigation water kgN/l"
            )
            public JAMSDouble irrigationN_conc;


//Berechnung
    public void init(){
        irrigationpart.setValue(0.0);
    }
    public void run (){

        double irripart = 0;


        storage.setValue(storageInput.getValue()*1000); // from m³/day to l/day


        irripart = irrigationsum.getValue()/storage.getValue();

        irrigationN_conc.setValue( storageInputN.getValue()/storage.getValue());

        if (irripart > 1) {
            irripart = 1;
        }

        irrigationpart.setValue(irripart);



        irrigationsum.setValue(0.0);


    }



}