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
@JAMSComponentDescription(
        title="SumQsim_Bypass_N_NaCl_2",
        author="c6gohe2",
        description="Calculation of bypass water and substances to the catchment outlet"
        )
        public class SumQsim_Bypass_N_NaCl_2 extends JAMSComponent {


 @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "HRU crop class"
            )
                public Attribute.Double Storage;


 @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "HRU crop class"
            )
            public Attribute.Double catchmentSimRunoff_qm;
 
 @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "HRU crop class"
            )
            public Attribute.Double catchmentSimRunoffN;

  @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "HRU crop class"
            )
            public Attribute.Double catchmentSimRunoffNaCl;

  @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Amount of bypass water [l]"
            )
            public Attribute.Double Irrigation_Bypass_water;

 @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "HRU crop class"
            )
            public Attribute.Double irrigationN_conc;
 
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "HRU crop class"
            )
            public Attribute.Double irrigationNaCl_conc;
  
 @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Damping factor for irrigation return flow, 0 - 1, default 0, [-]"
            )
            public Attribute.Double irr_damping;
 
 
 
 

 

    public void run() {
        
        double runstorage  = Storage.getValue();
        
        runstorage = runstorage * irr_damping.getValue();
        
        
                
        
        catchmentSimRunoff_qm.setValue(((runstorage + Irrigation_Bypass_water.getValue()) / 86400000) + catchmentSimRunoff_qm.getValue());
        catchmentSimRunoffN.setValue((((runstorage + Irrigation_Bypass_water.getValue())) * irrigationN_conc.getValue())  + catchmentSimRunoffN.getValue());
        catchmentSimRunoffNaCl.setValue((((runstorage + Irrigation_Bypass_water.getValue())) * irrigationNaCl_conc.getValue())  + catchmentSimRunoffNaCl.getValue());
        
        
        Storage.setValue(Storage.getValue() - runstorage);
        
    }
}
