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
@JAMSComponentDescription(title = "Calculation of irrigation input water, nitrogen and salt",
author = "c8fima",
description = "Calculation of irrigation input water and nitrogen and salt")
public class IrrigationinputN_NaCl extends JAMSComponent {

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    description = "in [-] plant growth water stress factor")
    public Attribute.Double netRain;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READWRITE,
    description = "HRU crop class",
    defaultValue = "0")
    public Attribute.Double irrigation;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READWRITE,
    description = "HRU crop class")
    public Attribute.Double storage;

    /*     @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READ,
    description = "HRU crop class"
    )
    public Attribute.Double area;*/
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.WRITE,
    description = "HRU crop class")
    public Attribute.Double irrigationAct;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.WRITE,
    description = "HRU crop class")
    public Attribute.Double Waterinput;

    /*@JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    description = "HRU crop class")
    public Attribute.Double re_use;*/

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    description = "HRU crop class")
    public Attribute.Double irrigationpart;

   @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "N-concentration of the irrigation water kgN/l"
            )
            public Attribute.Double irrigationN_conc;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "N-amount of the irrigation water [kgN]"
            )
            public Attribute.Double irrigationN;
    
       @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "NaCl-concentration of the irrigation water kg NaCl/l"
            )
            public Attribute.Double irrigationNaCl_conc;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "NaCl-amount of the irrigation water [kgNaCl]"
            )
            public Attribute.Double irrigationNaCl;


    public void init() {
        // irrigation.setValue(0);
    }

//Berechnung
    public void run() {
        double rain = netRain.getValue();
        double Irrsoll = irrigation.getValue();
        double part = irrigationpart.getValue();
        double Storage = storage.getValue();

        double irract = 0;
        double x = 0;



        //Demand gap with rain

        //double Demandgap = Irrsoll * (1 - part);




        //Irrsoll kommt bereits in litern


        x = (Irrsoll * part);
        
        /* //consideration of the precip in the irrigation amount
        
        x = x - rain;*/

        if (x <= 0) {
            x = 0;
        }


        //Storage = (Storage);   // Umrechnng in liter

        if (Storage >= x) {
            irract = x;

        } else {
            irract = Storage;

        }



        //Storage = Storage - (irract*re_use.getValue());
        Storage = Storage - irract;
        irrigationAct.setValue(irract);

//        Calculation of N-Amount

        irrigationN.setValue(irract * irrigationN_conc.getValue());
        irrigationNaCl.setValue(irract * irrigationNaCl_conc.getValue());


        //Storage = (Storage);   //zurückrechznung in m3/s
        //System.out.println("Storage " + Storage);


        storage.setValue(Storage);

        Waterinput.setValue(rain + irract);

    }
}
