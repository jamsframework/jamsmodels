/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.jams.j2k.s_n.irrigation;
import jams.data.*;
import jams.model.*;

/**
 *
 * @author c8fima
 */
@JAMSComponentDescription(
        title="SumQsim_bypass_N_NaCl",
        author="c8fima",
        description="Calculation of the irrigation bypasswater added to the outlet in the case of known water amounts. Including Salt and Nirtogen loads"
        )
        public class SumQsim_bypass_N_NaCl extends JAMSComponent {


 @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Not used irrigation water in l"
            )
                public Attribute.Double Storage;


 @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "runoff in m3/s"
            )
            public Attribute.Double catchmentSimRunoff_qm;
 
 @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "Nitrogen runoff in kgN"
            )
            public Attribute.Double catchmentSimRunoffN;
 
@JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "Salt runoff in kg salt"
            )
            public Attribute.Double catchmentSimRunoffNaCl;

 @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "Bypass water in l"
            )
            public Attribute.Double Irrigation_Bypass_water;

 @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "N-concentration of the irrigation water [kgN/l]"
            )
            public Attribute.Double irrigationN_conc;
 
 @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "Salt-concentration of the irrigation water [kg salt/l]"
            )
            public Attribute.Double irrigationNaCl_conc;

 
 @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "N-Load of the bypasswater in kgN"
            )
            public Attribute.Double bypass_N;
 
 @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "NaCl-Load of the bypasswater in kgNaCl"
            )
            public Attribute.Double bypass_NaCl;
 
    public void run() {
        
        catchmentSimRunoff_qm.setValue((Storage.getValue() / 86400000) + catchmentSimRunoff_qm.getValue() + Irrigation_Bypass_water.getValue());
        catchmentSimRunoffN.setValue((((Storage.getValue()) * irrigationN_conc.getValue()) + bypass_N.getValue()) + catchmentSimRunoffN.getValue());
        catchmentSimRunoffNaCl.setValue((((Storage.getValue()) * irrigationNaCl_conc.getValue()) + bypass_NaCl.getValue()) + catchmentSimRunoffNaCl.getValue());
    }
}
