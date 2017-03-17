/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.jams.j2k.s_n;
import jams.data.*;
import jams.model.*;
/**
 *
 * @author c8fima
 */
@JAMSComponentDescription(
        title="ReservoirManagement",
        author="Manfred Fink",
        description="Simple Reservoir module"
        )
        public class ReservoirManagement extends JAMSComponent {

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Desinged volume",
            unit = "L"
            )
            public Attribute.Double desVolume;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "inflow from reach",
            unit = "L"
            )
            public Attribute.Double storageInput;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "storage",
            unit = "L"
            )
            public Attribute.Double storage;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "release from reservoir",
            unit = "L"
            )
            public Attribute.Double outflow;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "actual fill proportion of the reservoir",
            lowerBound = 0,
            upperBound = 1
            )
            public Attribute.Double act_fillrate;
    
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "proportion filled at the beginning",
            lowerBound = 0,
            upperBound = 1
            )
            public Attribute.Double initfilrate;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "proportion were overproportional storage begin",
            lowerBound = 0,
            upperBound = 1
            )
            public Attribute.Double add_filrate;

    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "proportion were overproportional release begin",
            lowerBound = 0,
            upperBound = 1
            )
            public Attribute.Double rele_filrate;
    
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "optimal proportion",
            lowerBound = 0,
            upperBound = 1
            )
            public Attribute.Double opt_filrate;    
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "minimum outflow",
            unit = "L",
            lowerBound = 0,
            upperBound = 1
            )
            public Attribute.Double min_flux;  

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "normal outflow",
            unit = "L",
            lowerBound = 0,
            upperBound = 1
            )
            public Attribute.Double normal_flow;     
    
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "floodprotection threshold",
            unit = "L",
            lowerBound = 0,
            upperBound = 1
            )
            public Attribute.Double interception_inflow;  
    
    


//Berechnung
    public void init (){
        
        storage.setValue(desVolume.getValue() * initfilrate.getValue()); 
        
    }
    
    
    
    public void run (){
        double pot_surplus = 0;
        double run_storage = storage.getValue();
        double run_storageInput = storageInput.getValue();
        double run_min_flux = min_flux.getValue();
        double run_add_filrate = add_filrate.getValue();
        double run_rele_filrate = rele_filrate.getValue();
        double run_normal_flow = normal_flow.getValue();
        double run_interception_inflow = interception_inflow.getValue();
        double run_demand = 0;
        double pot_inter = 0;
        double run_rele = 0;
        double run_outflow = 0;
        
        
        Double run_opt_filrate = opt_filrate.getValue();
        
        run_storage = run_storage + run_storageInput;
                
        double fillrate = storage.getValue()/desVolume.getValue();
        
        
        if (fillrate < run_opt_filrate){
            
            pot_surplus = run_normal_flow - run_min_flux;
            
                      
            run_demand = ((run_opt_filrate - fillrate)/(run_opt_filrate - run_add_filrate));
            
            run_outflow = Math.max(run_min_flux + (pot_surplus * run_demand), run_min_flux);
            
            
        }else if (fillrate < 1){
            
            pot_inter =  run_interception_inflow - run_normal_flow;
            
            run_rele = ((fillrate - run_opt_filrate)/(run_rele_filrate - run_opt_filrate));
            
            run_outflow = Math.min(run_normal_flow + (pot_inter * run_rele), run_interception_inflow);     
            
            
            
        }else{
            
            run_outflow = run_storage - desVolume.getValue();
            
        
        }
        
        
        if (run_storage > run_outflow){
            run_storage = run_storage - run_outflow;
        }else{
           run_outflow = run_storage;
           run_storage  = 0;
        }
        
        fillrate = run_storage/desVolume.getValue();
        
        act_fillrate.setValue(fillrate);
        outflow.setValue(run_outflow);
        storage.setValue(run_storage);
        
        
        
        
        

    }



}
