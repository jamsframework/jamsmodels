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
        title="Qsim_irr_reach",
        author="c8fima",
        description=" calculation of the reduction factor to remove of irrigation water from storage water"
        )
        public class Qsim_irr_RG2_lumped extends JAMSComponent {


@JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "Factor reducing storage (smaller then one)"
            )
            public Attribute.Double Reduction_factor;


@JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "Storage filling of HRU",
            unit = "L"
            )
            public Attribute.Double Sum_storage;
 
@JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Irrigationamount in l"
            )
            public Attribute.Double irrigationactsum;

 @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "Reststorage from irrigation in l"
            )
            public Attribute.Double Storage;
 
 
 
    public void init() {
        
        
        Reduction_factor.setValue(1.0);
        
    }

    public void run() {
        
        double ratio = 0;
        
        Double run_Sum_storage = Sum_storage.getValue();
        
        if (run_Sum_storage > irrigationactsum.getValue()){
             run_Sum_storage = run_Sum_storage - irrigationactsum.getValue();
        }else{
             System.out.println("run_Sum_storage sould be " +  run_Sum_storage + " greater the irrigationactsum " + irrigationactsum.getValue());
             run_Sum_storage = 0.0;
             
        }            
        
       
        
        
        
        if (Sum_storage.getValue() == 0){
        
            ratio = 1;
        }else{
            ratio = run_Sum_storage /  Sum_storage.getValue(); 
        }
        
        Reduction_factor.setValue(ratio);      
        

        
        
        Storage.setValue(0.0);

       
        
        
    }
}
