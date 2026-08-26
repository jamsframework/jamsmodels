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
public class calc_irigation_conc_NaCl extends JAMSComponent {

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "HRU crop class"
            )
            public Attribute.Double storageInput;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "HRU crop class"
            )
            public Attribute.Double storageInputN;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "HRU crop class"
            )
            public Attribute.Double storageInputNaCl;   
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "Fixed minimal bypass factor 0 - 1 [-]",
            defaultValue = "0"
            )
            public Attribute.Double Bypassfactor;
        
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "Water amount in bypass [l]"
            )
            public Attribute.Double Bypasswater;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "HRU crop class"
            )
            public Attribute.Double storage;


    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "HRU crop class"
            )
            public Attribute.Double irrigationsum;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "HRU crop class"
            )
            public Attribute.Double irrigationpart;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "N-concentration of the irrigation water kgN/l"
            )
            public Attribute.Double irrigationN_conc;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "Salt concentration of the irrigation water kg salt/l"
            )
            public Attribute.Double irrigationNaCl_conc;    



//Berechnung
    public void init(){
        irrigationpart.setValue(0.0);
        storageInput.setValue(0);
        Bypassfactor.setValue(0.0);
        
    }
    public void run (){

        double irripart = 0;

        double run_storage = storage.getValue();

        double irrstorage_inp = (storageInput.getValue()*1000); // from m³/day to l/day

        double irrifactor = irrigationsum.getValue()/irrstorage_inp;
        
        irrifactor = Math.min(irrifactor, 1);
        
        double runBypassfactor = Bypassfactor.getValue(); //*(1-irrifactor);
        
        
        
        double irrstorage = (irrstorage_inp * (1 - runBypassfactor)) + run_storage;
        
        
        
        Bypasswater.setValue(irrstorage_inp * runBypassfactor);
        
        irripart = irrstorage/irrigationsum.getValue();

        irrigationN_conc.setValue( storageInputN.getValue()/(storageInput.getValue()*1000));
        irrigationNaCl_conc.setValue( storageInputNaCl.getValue()/(storageInput.getValue()*1000));

        if (irripart > 1) {
            irripart = 1;
        }

        irrigationpart.setValue(irripart);



        irrigationsum.setValue(0.0);
        
        storage.setValue(irrstorage);


    }



}