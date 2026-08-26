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
        title="SumQsim_NaCl_N",
        author="c8fima",
        description=""
        )
        public class SumQsim_NaCl_N extends JAMSComponent {


 @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Bypasswater in l"
            )
                public Attribute.Double Storage;


 @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "catchmentSimRunoff_qm in m3/s"
            )
            public Attribute.Double catchmentSimRunoff_qm;
 
 @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "catchmentSimRunoffN in kg"
            )
            public Attribute.Double catchmentSimRunoffN;
 
  @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "catchmentSimRunoffNaCl"
            )
            public Attribute.Double catchmentSimRunoffNaCl;
 
 @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "NO3 concentration of bypasswater in kg/l"
            )
            public Attribute.Double irrigationN_conc;

  @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "Salt concentration of bypasswater in kg/l"
            )
            public Attribute.Double irrigationNaCl_conc;


    public void run() {
        
        catchmentSimRunoff_qm.setValue((Storage.getValue() / 86400000) + catchmentSimRunoff_qm.getValue() );
        catchmentSimRunoffN.setValue(catchmentSimRunoffN.getValue() + (Storage.getValue() * irrigationN_conc.getValue()));
        catchmentSimRunoffNaCl.setValue(catchmentSimRunoffNaCl.getValue() + (Storage.getValue() * irrigationNaCl_conc.getValue()));
    }
}
