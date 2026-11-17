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
@JAMSComponentDescription(title = "calc_irigation_conc_NaCl_bypass, Calculation of irrigation water Salt-concentration",
author = "c8fima",
description = "Calculation of irrigation water Salt-concentration")
public class calc_irigation_conc_NaCl_bypass extends JAMSComponent {

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "HRU crop class"
            )
            public Attribute.Double storageInput;
    
     @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "HRU crop class"
            )
            public Attribute.Double bypass_water;
     
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "HRU crop class"
            )
            public Attribute.Double bypass_N; 

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "HRU crop class"
            )
            public Attribute.Double bypass_NaCl; 

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
    }
    public void run (){

        double irripart = 0;


        double runstorage = (storageInput.getValue()*1000); // from m³/day to l/day  
        
        irrigationN_conc.setValue( storageInputN.getValue()/runstorage);
        irrigationNaCl_conc.setValue( storageInputNaCl.getValue()/runstorage);
        
        runstorage = runstorage - (bypass_water.getValue() * 86400000); //reduction of irrigation storage due to the bypasswater 
        
        runstorage = Math.max(runstorage, 0);
        
        double run_bypass_N =bypass_water.getValue() * 86400000 * irrigationN_conc.getValue();
        run_bypass_N = Math.min(run_bypass_N,storageInputN.getValue());        
        bypass_N.setValue(run_bypass_N);
        double run_bypass_NaCl =bypass_water.getValue() * 86400000 * irrigationNaCl_conc.getValue();
        run_bypass_NaCl = Math.min(run_bypass_NaCl,storageInputNaCl.getValue());
        bypass_NaCl.setValue(run_bypass_NaCl);
       
        irripart = runstorage / irrigationsum.getValue();
        
       

        irrigationpart.setValue(irripart);

        irrigationsum.setValue(0.0);

        storage.setValue(runstorage);

    }



}