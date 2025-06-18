/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.unijena.j2k.routing;

import jams.data.*;
import jams.model.*;
import java.util.Iterator;
import org.unijena.j2k.routing.J2KProcessReachRouting;

/**
 *
 * @author c8fima
 */
@JAMSComponentDescription(
        title = "Daily Reservoir module",
        author = "Manfred Fink",
        description = "Reservoir Management according to pool based management ;"
        + "reservoir plan for daily timesteps; no proper flood mode"
        + "integration into the reaches",
        date = "21.05.2025",
        version = "1.0"
)
public class ReservoirMan_plan extends J2KProcessReachRouting {


    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "time")
    public Attribute.Calendar time;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "outflow from reservoir",
            unit = "L"
    )
    public Attribute.Double Simrunoff;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "initial storage in the reservoir [l]",
            unit = "L",
            defaultValue = "0.0")
    public Attribute.Double res_init_l;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "storage in the reservoir",
            unit = "L"
    )
    public Attribute.Double res_storage;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "RD1 storage in the reservoir",
            unit = "L"
    )
    public Attribute.Double res_storage_RD1;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "RD2 storage in the reservoir",
            unit = "L"
    )
    public Attribute.Double res_storage_RD2;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "RG1 storage in the reservoir",
            unit = "L"
    )
    public Attribute.Double res_storage_RG1;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "RG2 storage in the reservoir",
            unit = "L"
    )
    public Attribute.Double res_storage_RG2;
    
/*    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "RD1 storage in the reservoir",
            unit = "L"
    )
    public Attribute.Double res_init_RD1;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "RD2 storage in the reservoir",
            unit = "L"
    )
    public Attribute.Double res_init_RD2;
    
        @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "RG1 storage in the reservoir",
            unit = "L"
    )
    public Attribute.Double res_init_RG1;
        
        @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "RG2 storage in the reservoir",
            unit = "L"
    )
    public Attribute.Double res_init_RG2;*/

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "release from reservoir",
            unit = "L"
    )
    public Attribute.Double outflow;

/*    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Number of Pools"
    )
    public Attribute.Integer numPools;
*/
//Berechnung
    public void init() {
        /*Attribute.Entity reach;

        Iterator<Attribute.Entity> reachIterator = entities.getEntities().iterator();

        while (reachIterator.hasNext()) {
            reach = reachIterator.next();
            if (reach.existsAttribute("nrPools")) {
                res_storage.setValue(res_init_l.getValue());
                res_storage_RD1.setValue(res_init_RD1.getValue());
                res_storage_RD2.setValue(res_init_RD2.getValue());
                res_storage_RG1.setValue(res_init_RG1.getValue());
                res_storage_RG2.setValue(res_init_RG2.getValue());
            }
        }
*/
    }

    public void run() {

        Attribute.Entity reach;

        Iterator<Attribute.Entity> reachIterator = entities.getEntities().iterator();

        while (reachIterator.hasNext()) {
            reach = reachIterator.next();
            if (reach.existsAttribute("nrPools")) {
                double pot_surplus = 0;
                double run_res_storage = res_storage.getValue();

                double run_demand = 0;
                double pot_inter = 0;
                double run_rele = 0;
                double run_outflow = 0;
                int month = time.get(Attribute.Calendar.MONTH) + 1;
                //int nrPools = numPools.getValue();
                int nrPools =  reach.getInt("nrPools");
                int actPoolnr = 0;

                //double reservoir_V = res_storage.getValue();


                double RD1act =  inRD1.getValue();
                double RD2act =  inRD2.getValue();
                double RG1act =  inRG1.getValue();
                double RG2act =  inRG2.getValue();

                //double addInAct = actAddIn.getValue() + this.inAddIn.getValue();

                inRD1.setValue(0);
                inRD2.setValue(0);
                inRG1.setValue(0);
                inRG2.setValue(0);

                //inAddIn.setValue(0);

               /* actRD1.setValue(0);
                actRD2.setValue(0);
                actRG1.setValue(0);
                actRG2.setValue(0);*/

                //actAddIn.setValue(0);

                double run_res_storage_RD1 = res_storage_RD1.getValue() + RD1act;
                double run_res_storage_RD2 = res_storage_RD2.getValue() + RD2act;
                double run_res_storage_RG1 = res_storage_RG1.getValue() + RG1act;
                double run_res_storage_RG2 = res_storage_RG2.getValue() + RG2act;

                double run_res_storageInput = (RD1act + RD2act + RG1act + RG2act);
                run_res_storage = run_res_storage + run_res_storageInput;

                double run_res_prop_RD1 = run_res_storage_RD1 / run_res_storage;
                double run_res_prop_RD2 = run_res_storage_RD2 / run_res_storage;
                double run_res_prop_RG1 = run_res_storage_RG1 / run_res_storage;
                double run_res_prop_RG2 = run_res_storage_RG2 / run_res_storage;

                //Calculation of poolbased outflow
                int i = 0;
                i = 0;
                while (i < nrPools) {
                    i++;
                    //getModel().getRuntime().sendErrorMsg("I Variable = " + i + "!");
                    
                    String begin = "V_begin" + i;
                    String end = "V_end" + i;
                    String begin1 = "V_begin" + i;
                    String Q_Mon_Pool = "Q" + month + i;
                    
                    
                    if (i < nrPools){
                        begin1 = "V_begin" + (i+1);
                    }
                    
                    
                    
                    if (reach.getDouble(begin) * 1000 <= run_res_storage && reach.getDouble(end) * 1000 > run_res_storage) {
                        run_outflow = 86400 * reach.getDouble(Q_Mon_Pool);
                        run_outflow = Math.min(run_outflow, run_res_storage);
                    } else if (reach.getDouble(begin1) * 1000 <= run_res_storage && reach.getDouble(end) * 1000 > run_res_storage) { // gap between pools
                        run_outflow = 86400 * reach.getDouble(Q_Mon_Pool);
                        run_outflow = Math.min(run_outflow, run_res_storage);
                    } else if (run_res_storage > reach.getDouble("V_end" + nrPools) * 1000) {  //stage over capacity
                        run_outflow = Math.max(86400 * reach.getDouble("Q" + month + nrPools), run_res_storageInput);
                        run_outflow = Math.min(run_outflow, run_res_storage);
                    }

                }
                
                
                double run_RD1_outflow = run_outflow * run_res_prop_RD1;
                double run_RD2_outflow = run_outflow * run_res_prop_RD2;
                double run_RG1_outflow = run_outflow * run_res_prop_RG1;
                double run_RG2_outflow = run_outflow * run_res_prop_RG2;
                
                res_storage_RD1.setValue(run_res_storage_RD1 - run_RD1_outflow);
                res_storage_RD2.setValue(run_res_storage_RD2 - run_RD2_outflow);
                res_storage_RG1.setValue(run_res_storage_RG1 - run_RG1_outflow);
                res_storage_RG2.setValue(run_res_storage_RG2 - run_RG2_outflow);
                
                res_storage.setValue(run_res_storage - run_outflow);
                                
                outflow.setValue(run_outflow);
                
                double res_storage_test = res_storage.getValue() - (res_storage_RD1.getValue() + res_storage_RD2.getValue() + res_storage_RG1.getValue() + res_storage_RG2.getValue());
                
                
                if (Math.abs(res_storage_test) > 0.1){
                    getModel().getRuntime().sendErrorMsg("Reservoir Storage Test not Zero = (" + res_storage_test + ")!");
                }
                
                inRD1.setValue(run_RD1_outflow);
                inRD2.setValue(run_RD2_outflow);
                inRG1.setValue(run_RG1_outflow);
                inRG2.setValue(run_RG2_outflow);
                
                
            }else{
            inRD1.setValue(inRD1.getValue());
            inRD2.setValue(inRD2.getValue());
            inRG1.setValue(inRG1.getValue());
            inRG2.setValue(inRG2.getValue());
            }
        }

    }

}
