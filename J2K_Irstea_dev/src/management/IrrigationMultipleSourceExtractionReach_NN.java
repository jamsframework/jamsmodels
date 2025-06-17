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
 * 
 */
@JAMSComponentDescription(
        title = "",
        author = "François Tilmant + Sven Kralisch, Nico Hachgenei",
        description = "Transfer water from reaches to HRUs depending on water"
        + " availability and irrigation demand."
	+ "Irrigation water comes from incoming water to the reach and water inside the reach (actRG1, etc..)."
        + "New names. Bugfix in water extraction from act. Use of more"
        + "internal variables. Application of extraction limitation to in and act, equal extraction"
        + "from both."
        + "totalIrrigAvailableReach is written only for reach sources"
        + "multiple source extraction",
        date = "2015-08-13 / 2025-03-25",
        version = "4.0_0"
)
@VersionComments(entries = {
    @VersionComments.Entry(version = "1.0_0", comment = "Initial version"),
    @VersionComments.Entry(version = "2.0_0", comment = "Modififed case where no water comes into the reach,"
            + " but water is inside the reach --> now water will be extracted from the reach in this case."),
    @VersionComments.Entry(version = "3.0_0", comment = "New names. Bugfix in water extraction from act. Use of more"
            + "internal variables. Application of extraction limitation to in and act, equal extraction"
            + "from both."),
    @VersionComments.Entry(version = "4.0_0", comment = "multiple source extraction")
})
public class IrrigationMultipleSourceExtractionReach_NN extends JAMSComponent {

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
                    "extracting water for irrigation. - input / state variable",
            unit = "L"
    )
    public Attribute.Double inRD1;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "Current time step RD2 inflow into reach. Will be updated by this component,"+
                    "extracting water for irrigation. - input / state variable",
            unit = "L"
    )
    public Attribute.Double inRD2;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "Current time step RG1 inflow into reach. Will be updated by this component,"+
                    "extracting water for irrigation. - input / state variable",
            unit = "L"
    )
    public Attribute.Double inRG1;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "Current time step RG2 inflow into reach. Will be updated by this component,"+
                    "extracting water for irrigation. - input / state variable",
            unit = "L"
    )
    public Attribute.Double inRG2;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "Current time step RD1 volume inside reach. Will be updated by this component"+
                    ", extracting water for irrigation."+
                    "- state variable",
            unit = "L"
    )
    public Attribute.Double actRD1;
        
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "Current time step RD2 volume inside reach. Will be updated by this component"+
                    ", extracting water for irrigation."+
                    "- state variable",
            unit = "L"
    )
    public Attribute.Double actRD2;
        
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "Current time step RG1 volume inside reach. Will be updated by this component"+
                    ", extracting water for irrigation."+
                    "- state variable",
            unit = "L"
    )
    public Attribute.Double actRG1;
            
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "Current time step RG2 volume inside reach. Will be updated by this component"+
                    ", extracting water for irrigation."+
                    "- state variable",
            unit = "L"
    )
    public Attribute.Double actRG2;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Name of list of irrigated HRUs in reach entities. List will be read by this"+
                    "component. - parameter / pointer",
            defaultValue = "irrigationReachEntities"
    )
    public Attribute.String irrigationReachEntitiesListName;

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
            defaultValue = "plantIrrigRequirements"
    )
    public Attribute.String plantIrrigRequirementsName;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Name of attribute that stores the irrigation water delivered to HRU for application (extraction"+
                    "minus losses due to efficiency). This attribute will be written to by this component."+
                    " - parameter / pointer",
            defaultValue = "irrigationApplication"
    )
    public Attribute.String irrigationApplicationName;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Ratio of water available (allowed to be taken) for irrigation over water present"+
                    "in the reach (actR.. + inR..). - parameter"
    )
    public Attribute.Double allowedExtractionFraction;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "Total demand of water for irrigation, including the enhancement by poor efficiency."+
                    "Should be attribute of the ReachLoop to write. This component cumulates the irrigation"+ 
                    "demands of irrigated HRUs and writes this attribute. - output",
            unit = "L"
    )
    public Attribute.Double totalIrrigDemand;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "Total extraction for irrigation (= totalIrrigDemand, but limited to available water)."+
                    "Calculated in this component. Losses due to poor efficiency are removed from this volume"+
                    "before transfer to irrigated HRUs. - output",
            unit = "L"
    )
    public Attribute.Double totalIrrigExtractionReach;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "Total water available for irrigation in the reach (act+in) as sum of the four components"+
                    "BEFORE EXTRACTION. Not used. - output",
            unit = "L"
    )
    public Attribute.Double totalIrrigAvailableReach;

    /*
     *  Component run stages
     */

    @Override
    public void run() {
        Attribute.Entity run_currentReach = reaches.getCurrent();
        //check if this reach even has irrigated HRUs in its catchment
        if (!run_currentReach.existsAttribute(irrigationReachEntitiesListName.getValue())) { // no irrigated HRUs --> step out of function
            return;
        }
        
        double run_inRD1 = inRD1.getValue();
        double run_inRD2 = inRD2.getValue();
        double run_inRG1 = inRG1.getValue();
        double run_inRG2 = inRG2.getValue();
        double run_actRD1 = actRD1.getValue();
        double run_actRD2 = actRD2.getValue();
        double run_actRG1 = actRG1.getValue();
        double run_actRG2 = actRG2.getValue();
        double run_allowedExtractionFraction = allowedExtractionFraction.getValue();
        
        double run_frac_irrig_applied;
        double run_totalExtraction; // local variable to store actually extracted volume
        double run_totalIn = run_inRD1 + run_inRD2 + run_inRG1 + run_inRG2; // all water in inflow (for proportional extraction)
        double run_totalAct = run_actRD1 + run_actRD2 + run_actRG1 + run_actRG2; // all water in act (for proportional extraction)
        double run_totalStorage = run_totalIn + run_totalAct; // all water in inflow and act
        double run_inAvailable = run_allowedExtractionFraction * run_totalIn;
        double run_actAvailable = run_allowedExtractionFraction * run_totalAct; // water in the reach for irrigation
        double run_totalAvailable = run_inAvailable + run_actAvailable; // all available water
        totalIrrigAvailableReach.setValue(run_totalAvailable); // water available for irrigation purpose at this time step
        
        double run_totalDemand = 0;
        double run_providedFraction;

        List<Attribute.Entity> run_l = (List) run_currentReach.getObject(irrigationReachEntitiesListName.getValue());
        for (Attribute.Entity run_hru : run_l) {
            double run_demand = run_hru.getDouble(irrigationDemandName.getValue());            
            double run_application = run_hru.getDouble(irrigationApplicationName.getValue()); // water already applied for irrigation in this hru
            double run_plantIrrigRequirements = run_hru.getDouble(plantIrrigRequirementsName.getValue()); // plant requirement, for calculating the ratio of water applied for irrigation
            // Ratio of water already applied for irrigation
            if (run_plantIrrigRequirements != 0) {
                run_frac_irrig_applied = run_application / run_plantIrrigRequirements;
            } else {
                run_frac_irrig_applied = 0; 
            }
                        
            double run_DemandRemaining = run_demand * (1-run_frac_irrig_applied);
            run_totalDemand += run_DemandRemaining;
        }
        totalIrrigDemand.setValue(run_totalDemand);

        //calculate proportion of total water that is needed
        if ((run_totalAvailable != 0) && (run_totalDemand != 0)){ // if there is water available (in and/or act) AND demanded for irrigation
            
            double run_availableDemandFraction = run_totalDemand / run_totalAvailable;// fraction of available water that is demanded for irrigation
            
            if (run_availableDemandFraction <=1){ // demand can be satisfied with available water from inflow and act
                double run_storageDemandFraction = run_totalDemand / run_totalStorage;// fraction of all stored water that is demanded for irrigation
                run_totalExtraction = run_totalDemand; // we can satisfy the demand (extract everything that is needed)
                run_providedFraction = 1;
                
                // extract proportionally from inflow (ratio demand over all water)
                inRD1.setValue(run_inRD1 * (1 - run_storageDemandFraction));
                inRD2.setValue(run_inRD2 * (1 - run_storageDemandFraction));
                inRG1.setValue(run_inRG1 * (1 - run_storageDemandFraction));
                inRG2.setValue(run_inRG2 * (1 - run_storageDemandFraction));
                // extract proportionally from act (ratio demand over all water)
                actRD1.setValue(run_actRD1 * (1 - run_storageDemandFraction));
                actRD2.setValue(run_actRD2 * (1 - run_storageDemandFraction));
                actRG1.setValue(run_actRG1 * (1 - run_storageDemandFraction));
                actRG2.setValue(run_actRG2 * (1 - run_storageDemandFraction));
            } else { // not all of the demand can be satisfied from available water. Only available water will be extracted
                run_totalExtraction = run_totalAvailable; // we extract all available water
                run_providedFraction = run_totalExtraction/run_totalDemand;
                
                // extract proportionally from inflow (ratio demand over all water)
                inRD1.setValue(run_inRD1 * (1 - run_allowedExtractionFraction));
                inRD2.setValue(run_inRD2 * (1 - run_allowedExtractionFraction));
                inRG1.setValue(run_inRG1 * (1 - run_allowedExtractionFraction));
                inRG2.setValue(run_inRG2 * (1 - run_allowedExtractionFraction));
                // extract proportionally from act (ratio demand over all water)
                actRD1.setValue(run_actRD1 * (1 - run_allowedExtractionFraction));
                actRD2.setValue(run_actRD2 * (1 - run_allowedExtractionFraction));
                actRG1.setValue(run_actRG1 * (1 - run_allowedExtractionFraction));
                actRG2.setValue(run_actRG2 * (1 - run_allowedExtractionFraction));
            }
            
            //distribute total transfer over all HRUs
            double run_totalProvidedWater = 0.;
            for (Attribute.Entity run_hru : run_l) { // loop over HRUs irrigated with water from this HRU
                double run_application = run_hru.getDouble(irrigationApplicationName.getValue()); // water already applied for irrigation in this hru
                double run_plantIrrigRequirements = run_hru.getDouble(plantIrrigRequirementsName.getValue()); // plant water requirements of current HRU
                double runWaterRemaining = run_plantIrrigRequirements - run_application;
                
                if(run_application != 0){
                    getModel().getRuntime().sendInfoMsg("irrigationApplication != 0 No reason ! value : "+ run_application);
                }
                
                run_application += runWaterRemaining * run_providedFraction;
                run_hru.setDouble(irrigationApplicationName.getValue(), run_application); // provide water to irrigated HRU (add to eventually remaining water)
                run_totalProvidedWater += runWaterRemaining * run_providedFraction; // add to counter of total provided water
            }
            // restitute lost water to RD2 (when efficiency of the irrigation network <1) :
            inRD2.setValue(inRD2.getValue() + Math.max(0., run_totalExtraction - run_totalProvidedWater) );            
            
        } else { // no water available OR no water demanded for irrigation -> no extraction, no delivery
            run_totalExtraction = 0; // No extraction
        }
        
        totalIrrigExtractionReach.setValue(run_totalExtraction);
        
        //remove all HRUs from demand list
        run_l.removeAll(run_l);
    }
}
