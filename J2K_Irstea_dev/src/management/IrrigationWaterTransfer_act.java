/*
 * IrrigationWaterTransferr.java
 * Created on 13.08.2015, 16:17:09
 *
 * This file is part of JAMS
 * Copyright (C) FSU Jena
 *
 * JAMS is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * JAMS is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with JAMS. If not, see <http://www.gnu.org/licenses/>.
 *
 */
package management;

import jams.data.*;
import jams.model.*;
import java.util.List;

/**
 *
 * @author Sven Kralisch <sven.kralisch at uni-jena.de>
 */
@JAMSComponentDescription(
        title = "",
        author = "François Tilmant + Sven Kralisch, Nico Hachgenei",
        description = "Transfer water from reaches to HRUs depending on water"
        + " availability and irrigation demand"
	+ "irrigation water comes from incoming water to the reach and water inside the reach (actRG1, etc..)",
        date = "2015-08-13",
        version = "2.0_0"
)
@VersionComments(entries = {
    @VersionComments.Entry(version = "1.0_0", comment = "Initial version"),
    @VersionComments.Entry(version = "2.0_0", comment = "Modififed case where no water comes into the reach,"
            + " but water is inside the reach --> now water will be extracted from the reach in this case.")
})
public class IrrigationWaterTransfer_act extends JAMSComponent {

    /*
     *  Component attributes
     */
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Reaches list"
    )
    public Attribute.EntityCollection reaches;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "Current time step RD1 inflow into reach. Will be updated by this component,"+
                    "extracting water for irrigation. - input / state variable"
    )
    public Attribute.Double inRD1;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "Current time step RD2 inflow into reach. Will be updated by this component,"+
                    "extracting water for irrigation. - input / state variable"
    )
    public Attribute.Double inRD2;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "Current time step RG1 inflow into reach. Will be updated by this component,"+
                    "extracting water for irrigation. - input / state variable"
    )
    public Attribute.Double inRG1;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "Current time step RG2 inflow into reach. Will be updated by this component,"+
                    "extracting water for irrigation. - input / state variable"
    )
    public Attribute.Double inRG2;
    
        @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "Current time step RD1 volume inside reach. Will be updated by this component"+
                    "(if not enough water in inflow), extracting water for irrigation."+
                    "- state variable"
    )
    public Attribute.Double actRD1;
        
        @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "Current time step RD2 volume inside reach. Will be updated by this component"+
                    "(if not enough water in inflow), extracting water for irrigation."+
                    "- state variable"
    )
    public Attribute.Double actRD2;
        
            @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "Current time step RG1 volume inside reach. Will be updated by this component"+
                    "(if not enough water in inflow), extracting water for irrigation."+
                    "- state variable"
    )
    public Attribute.Double actRG1;
            
                @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "Current time step RG2 volume inside reach. Will be updated by this component"+
                    "(if not enough water in inflow), extracting water for irrigation."+
                    "- state variable"
    )
    public Attribute.Double actRG2;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Name of list of irrigated HRUs in reach entities. List will be read by this"+
                    "component. - parameter / pointer",
            defaultValue = "irrigationEntities"
    )
    public Attribute.String irrigationEntitiesListName;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Name of attribute that stores irrigation demand of an HRU - (plant water"+
                    "requirement / efficiency). Irrigation demand will be read by this component."+
                    "- parameter / pointer",
            defaultValue = "irrigationDemand"
    )
    public Attribute.String irrigationDemandName;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Name of attribute that stores water requirements of an HRU - the real plant"+
                    "requirements. Water requirements will be read by this component. - parameter / pointer",
            defaultValue = "waterRequirements"
    )
    public Attribute.String waterRequirementsName;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Name of attribute that stores the irrigation water delivered to HRU (totalTransfer"+
                    "minus losses due to efficiency). This attribute will be written to by this component"+
                    "- parameter / pointer",
            defaultValue = "irrigationWater"
    )
    public Attribute.String irrigationWaterName;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Ratio of water available (allowed to be taken) for irrigation over water present"+
                    "in the reach (actR..). - parameter"
    )
    public Attribute.Double actPrel;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "Total demand of water for irrigation, including the enhancement by poor efficiency."+
                    "Should be attribute of the ReachLoop to write. This component cumulates the irrigation"+ 
                    "demands of irrigated HRUs and writes this attribute. - output"
    )
    public Attribute.Double totalDemand;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "Total irrigation transfer (= totalDemand, but limited to available water). Calculated in this component. - output"
    )
    public Attribute.Double totalTransfer;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "Total input in the reach as sum of the four components, after extraction for irrigation. - output"
    )
    public Attribute.Double totalInput;

    /*
     *  Component run stages
     */

    @Override
    public void run() {

        Attribute.Entity run_currentReach = reaches.getCurrent();
        //check if this reach even has irrigated HRUs in its catchment
        if (!run_currentReach.existsAttribute(irrigationEntitiesListName.getValue())) { // no irrigated HRUs --> step out of function
            double run_totalIn = inRD1.getValue() + inRD2.getValue() + inRG1.getValue() + inRG2.getValue();
            double run_totalAct = this.actPrel.getValue() * (actRD1.getValue() + actRD2.getValue() + actRG1.getValue() + actRG2.getValue());
            this.totalInput.setValue(run_totalIn + run_totalAct); // IG : ACHTUNG, cette variable n'est pas à jour !!
            return;
        }
//        getModel().getRuntime().println("START - incoming RD1: "+inRD1.getValue()+", RD2: "+inRD2.getValue()+", RG1: "+inRG1.getValue()+", RG2: "+inRG2.getValue()+"; actRD1: "+actRD1.getValue()+", actRD2: "+actRD2.getValue()+", actRG1: "+actRG1.getValue()+", actRG2: "+actRG2.getValue()+". Reach "+currentReach.getObject("ID"));
        double run_totalIn = inRD1.getValue() + inRD2.getValue() + inRG1.getValue() + inRG2.getValue();
        double run_totalAct = this.actPrel.getValue() * (actRD1.getValue() + actRD2.getValue() + actRG1.getValue() + actRG2.getValue()); // eau du reach dispo pour l'irrigation.
        double run_totalAv = run_totalIn + run_totalAct; // all available water
        this.totalInput.setValue(run_totalAv); // eau disponible pour l'irrigation à ce pas de temps
        //this.totalInput.setValue(totalIn);
        double run_totalDemand = 0;

        List<Attribute.Entity> run_l = (List) run_currentReach.getObject(irrigationEntitiesListName.getValue());
        for (Attribute.Entity run_hru : run_l) {
            double run_demand = run_hru.getDouble(irrigationDemandName.getValue());
            run_totalDemand += run_demand;
        }

        this.totalDemand.setValue(run_totalDemand);

        //calculate proportion of total water that is needed
        if (run_totalAv != 0){ // if there is water avilable (in and/or act)
            if (run_totalIn != 0){ // if there is water coming in
//                getModel().getRuntime().println("++ water coming in, "+totalAv+" available("+totalIn+" incoming, "+totalAct+" in reach), needing "+totalDemand+" . Reach "+currentReach.getObject("ID"));
                double run_frac = run_totalDemand /run_totalIn;

                if (run_frac <= 1) {

                    //we can cover all only with in to the reach, reduce the components accordingly
                    inRD1.setValue(inRD1.getValue() * (1 - run_frac));
                    inRD2.setValue(inRD2.getValue() * (1 - run_frac));
                    inRG1.setValue(inRG1.getValue() * (1 - run_frac));
                    inRG2.setValue(inRG2.getValue() * (1 - run_frac));
                    totalTransfer.setValue(run_totalDemand);
//                    getModel().getRuntime().println("took from incoming, remaining: inRD1: "+inRD1.getValue()+", inRD2: "+inRD2.getValue()+", inRG1: "+inRG1.getValue()+", inRG2: "+inRG2.getValue()+".");

                } else {
                    //looking if we can cover the demand by including part of act...
                    run_frac = run_totalDemand / (run_totalIn+run_totalAct);

                    //we can cover only part of the demand with in, reduce the components to 0
                    inRD1.setValue(0);
                    inRD2.setValue(0);
                    inRG1.setValue(0);
                    inRG2.setValue(0);

                    if (run_frac <= 1) {
                        //we can cover all of the demand but not only with in..., reduce the components accordingly
                        double run_actDemand;
                        run_actDemand = run_totalDemand - run_totalIn;
                        double run_frac2 = run_actDemand/run_totalAct;
                        actRD1.setValue(actRD1.getValue() * (1 - run_frac2));
                        actRD2.setValue(actRD2.getValue() * (1 - run_frac2));
                        actRG1.setValue(actRG1.getValue() * (1 - run_frac2));
                        actRG2.setValue(actRG2.getValue() * (1 - run_frac2));
                        totalTransfer.setValue(run_totalDemand);

                    } else {
                        // we can cover part of the demand ; reduce the act... to (1 - actPrel)*act...
                        actRD1.setValue(actRD1.getValue() * (1 - actPrel.getValue()));
                        actRD2.setValue(actRD2.getValue() * (1 - actPrel.getValue()));
                        actRG1.setValue(actRG1.getValue() * (1 - actPrel.getValue()));
                        actRG2.setValue(actRG2.getValue() * (1 - actPrel.getValue()));
                        totalTransfer.setValue(run_totalIn+run_totalAct);
                    }
                }
//                getModel().getRuntime().println("took "+totalTransfer);
            } else {// no water coming into reach but water available
                //looking if we can cover the demand by including usable part of act...
//                getModel().getRuntime().println("-- no water coming in, "+run_totalAct+" available, needing "+run_totalDemand);
                double run_frac = run_totalDemand / (run_totalAct);
                if (run_frac <= 1) {
                    //we can cover all of the demand with act, reduce the components accordingly
                    actRD1.setValue(actRD1.getValue() * (1 - run_frac));
                    actRD2.setValue(actRD2.getValue() * (1 - run_frac));
                    actRG1.setValue(actRG1.getValue() * (1 - run_frac));
                    actRG2.setValue(actRG2.getValue() * (1 - run_frac));
                    totalTransfer.setValue(run_totalDemand);

                } else {
                    // we can only cover part of the demand ; reduce the act... to (1 - actPrel)*act...
                    actRD1.setValue(actRD1.getValue() * (1 - actPrel.getValue()));
                    actRD2.setValue(actRD2.getValue() * (1 - actPrel.getValue()));
                    actRG1.setValue(actRG1.getValue() * (1 - actPrel.getValue()));
                    actRG2.setValue(actRG2.getValue() * (1 - actPrel.getValue()));
                    totalTransfer.setValue(run_totalAct);
                }
//                getModel().getRuntime().println("took "+totalTransfer);
            }
            
            //distribute total transfer over all HRUs
            double run_providedFraction;
            if (run_totalDemand != 0.){
                run_providedFraction = totalTransfer.getValue()/run_totalDemand;
            } else {
                run_providedFraction = 1;
            }
            
            double run_providedWater_tmp=0.;
            for (Attribute.Entity run_hru : run_l) {
                double run_waterRequirements = run_hru.getDouble(waterRequirementsName.getValue());
                run_hru.setDouble(irrigationWaterName.getValue(), run_waterRequirements * run_providedFraction);
                run_providedWater_tmp= run_providedWater_tmp + run_waterRequirements * run_providedFraction;
            }
            // restitute lost water to RD2 (when efficiency of the irrigation network <1) :
            inRD2.setValue(inRD2.getValue()+Math.max(0.,totalTransfer.getValue()-run_providedWater_tmp) );
            
            
        } else { // no water available
            for (Attribute.Entity run_hru : run_l) {
                run_hru.setDouble(irrigationWaterName.getValue(), 0); 
            }
            totalTransfer.setValue(0.); 
        }
        
//        getModel().getRuntime().println("END - incoming RD1: "+inRD1.getValue()+", RD2: "+inRD2.getValue()+", RG1: "+inRG1.getValue()+", RG2: "+inRG2.getValue()+"; actRD1: "+actRD1.getValue()+", actRD2: "+actRD2.getValue()+", actRG1: "+actRG1.getValue()+", actRG2: "+actRG2.getValue()+". Reach "+currentReach.getObject("ID"));
//remove all HRUs from demand list
        run_l.removeAll(run_l);
    }
}
