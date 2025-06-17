/*
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
        author = "Sven Kralisch / LC / Nathan Pellerin",
        description = "Transfer water from HRUs to HRUs depending on water"
        + " availability and demand"
        + "Irrigation water comes from incoming water to the GW and water inside the GW (actRG1, etc..)."
        + "New names. Bugfix in water extraction from act. Use of more"
        + "internal variables. Application of extraction limitation to in and act, equal extraction"
        + "from both."
        + "totalIrrigAvailableGW is written only for GW sources"
        + "multiple source extraction",
        date = "2015-08-13 / 2025-03-25",
        version = "4.0_0"
)
@VersionComments(entries = {
    @VersionComments.Entry(version = "1.0_0", comment = "Initial version"),
    @VersionComments.Entry(version = "2.0_0", comment = "Modififed case where no water comes into the GW,"
            + " but water is inside the GW --> now water will be extracted from the GW in this case."),
    @VersionComments.Entry(version = "3.0_0", comment = "New names. Bugfix in water extraction from act. Use of more"
            + "internal variables. Application of extraction limitation to in and act, equal extraction"
            + "from both."),
    @VersionComments.Entry(version = "4.0_0", comment = "multiple source extraction"),
    @VersionComments.Entry(version = "4.1_0", comment = "All variables contain irrigation terminology"
            + "Extraction variable is subdivided into irrigation source")
})
public class IrrigationMultipleSourceExtractionGW_NN extends JAMSComponent {

    /*
     *  Component attributes
     */
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "HRUs list"
    )
    public Attribute.EntityCollection hrus;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "Current time step RD2 inflow into hru GW. Will be updated by this component,"+
                    "extracting water for irrigation. - input / state variable",
            unit = "L"
    )
    public Attribute.Double inRD2;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "Current time step RG1 inflow into hru GW. Will be updated by this component,"+
                    "extracting water for irrigation. - input / state variable",
            unit = "L"
    )
    public Attribute.Double inRG1;
            
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "Current time step RG2 inflow into hru GW. Will be updated by this component,"+
                    "extracting water for irrigation. - input / state variable",
            unit = "L"
    )
    public Attribute.Double inRG2;
   
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "Current time step RG1 volume inside hru. Will be updated by this component"+
                    ", extracting water for irrigation."+
                    "- state variable",
            unit = "L"
    )
    public Attribute.Double actRG1;
            
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "Current time step RG2 volume inside hru. Will be updated by this component"+
                    ", extracting water for irrigation."+
                    "- state variable",
            unit = "L"
    )
    public Attribute.Double actRG2;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Name of list of irrigated HRUs in GW entities (hru reservoir). List will be read by this"+
                    "component. - parameter / pointer",
            defaultValue = "irrigationGWEntities"
    )
    public Attribute.String irrigationGWEntitiesListName;

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
            description = "Name of attribute that stores the irrigation water delivered to HRU by the ground water reservoir (hru) for application (extraction"+
                    "minus losses due to efficiency). This attribute will be written to by this component."+
                    " - parameter / pointer",
            defaultValue = "irrigationApplicationGW",
            unit = "L"
    )
    public Attribute.String irrigationApplicationGWName;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Ratio of water available (allowed to be taken) for irrigation over water present"+
                    "in the hru GW (actR.. + inR..). - parameter"
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
    public Attribute.Double totalIrrigExtractionGW;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "Total water available for irrigation in the hru GW (act+in) as sum of the two components"+
                    "BEFORE EXTRACTION. Not used. - output",
            unit = "L"
    )
    public Attribute.Double totalIrrigAvailableGW;
    
    /*
     *  Component run stages
     */

    @Override
    public void run() {
    
        Attribute.Entity run_currentHRU = hrus.getCurrent();

        // check if this hru is an irrigation source for withdrawal
        if (!run_currentHRU.existsAttribute(irrigationGWEntitiesListName.getValue())) {// no irrigated HRUs --> step out of function
            return;
        }
        
        double run_inRG1 = inRG1.getValue();
        double run_inRG2 = inRG2.getValue();
        double run_actRG1 = actRG1.getValue();
        double run_actRG2 = actRG2.getValue();
        double run_allowedExtractionFraction = allowedExtractionFraction.getValue();
        
        double run_frac_irrig_applied;
        double run_totalExtraction; // local variable to store actually extracted volume
        double run_totalIn =  run_inRG1 + run_inRG2; // all water in inflow (for proportional extraction)
        double run_totalAct = run_actRG1 + run_actRG2; // all water in act (for proportional extraction)
        double run_totalStorage = run_totalIn + run_totalAct; // all water in inflow and act
        double run_inAvailable = run_allowedExtractionFraction * run_totalIn;
        double run_actAvailable = run_allowedExtractionFraction * run_totalAct; // hru GW water available for irrigation.
        double run_totalAvailable = run_inAvailable + run_actAvailable; // all available water
        totalIrrigAvailableGW.setValue(run_totalAvailable);

        double run_totalDemand = 0;
        double run_totalDemandRemaining = 0;
        double run_providedFraction;

        List<Attribute.Entity> run_l = (List) run_currentHRU.getObject(irrigationGWEntitiesListName.getValue());
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
            run_totalDemandRemaining += run_DemandRemaining;
            run_totalDemand += run_demand;
        }
        totalIrrigDemand.setValue(run_totalDemand);

        //calculate proportion of total water that is needed
        if ((run_totalAvailable != 0) && (run_totalDemandRemaining != 0)){ // if there is water available (in and/or act) AND demanded for irrigation
            
            double run_availableDemandFraction = run_totalDemandRemaining / run_totalAvailable;// fraction of available water that is demanded for irrigation
            
            if (run_availableDemandFraction <=1){ // demand can be satisfied with available water from inflow and act
                double run_storageDemandFraction = run_totalDemandRemaining / run_totalStorage;// fraction of all stored water that is demanded for irrigation
                run_totalExtraction = run_totalDemandRemaining; // we can satisfy the demand (extract everything that is needed)
                run_providedFraction = 1;
                
                // extract proportionally from inflow (ratio demand over all water)
                inRG1.setValue(run_inRG1 * (1 - run_storageDemandFraction));
                inRG2.setValue(run_inRG2 * (1 - run_storageDemandFraction));
                // extract proportionally from act (ratio demand over all water)
                actRG1.setValue(run_actRG1 * (1 - run_storageDemandFraction));
                actRG2.setValue(run_actRG2 * (1 - run_storageDemandFraction));
            } else { // not all of the demand can be satisfied from available water. Only available water will be extracted
                run_totalExtraction = run_totalAvailable; // we extract all available water
                run_providedFraction = run_totalExtraction/run_totalDemandRemaining;
                
                // extract proportionally from inflow (ratio demand over all water)
                inRG1.setValue(run_inRG1 * (1 - run_allowedExtractionFraction));
                inRG2.setValue(run_inRG2 * (1 - run_allowedExtractionFraction));
                // extract proportionally from act (ratio demand over all water)
                actRG1.setValue(run_actRG1 * (1 - run_allowedExtractionFraction));
                actRG2.setValue(run_actRG2 * (1 - run_allowedExtractionFraction));
            }
            
            //distribute total transfer over all HRUs
            double run_totalProvidedWater = 0.;
            for (Attribute.Entity run_hru : run_l) { // loop over HRUs irrigated with water from this HRU
                double run_application = run_hru.getDouble(irrigationApplicationName.getValue()); // water already applied for irrigation in this hru
                double run_plantIrrigRequirements = run_hru.getDouble(plantIrrigRequirementsName.getValue()); // plant water requirements of current HRU
                double runWaterRemaining = run_plantIrrigRequirements - run_application;
                
                run_application += runWaterRemaining * run_providedFraction;
                run_hru.setDouble(irrigationApplicationName.getValue(), run_application); // provide water to irrigated HRU (add to eventually remaining water)
                run_totalProvidedWater += runWaterRemaining * run_providedFraction; // add to counter of total provided water
                
                run_hru.setDouble(irrigationApplicationGWName.getValue(), runWaterRemaining * run_providedFraction);
            }
            // restitute lost water to RD2 (when efficiency of the irrigation network <1) :
            inRD2.setValue(inRD2.getValue() + Math.max(0., run_totalExtraction - run_totalProvidedWater) );            
            
        } else { // no water available OR no water demanded for irrigation -> no extraction, no delivery
            run_totalExtraction = 0; // No extraction
        }
        totalIrrigExtractionGW.setValue(run_totalExtraction);
        
        if(run_totalDemandRemaining == 0) {
            for (Attribute.Entity hru : run_l) {
                hru.setDouble(irrigationApplicationGWName.getValue(), 0); 
            }
        }
        if(run_totalDemand == 0){
            for (Attribute.Entity hru : run_l) {
                hru.setDouble(irrigationApplicationName.getValue(), 0); 
            }             
        }
        if(run_totalAvailable == 0) {
            for (Attribute.Entity hru : run_l) {
                hru.setDouble(irrigationApplicationGWName.getValue(), 0); 
            }
        }
        
    //remove all HRUs from demand list
        run_l.removeAll(run_l);
    }
}