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
        author = "Nathan Pellerin",
        description = "Transfer water from farmdams to HRUs depending on water"
        + " availability and irrigation demand."
        + "totalIrrigAvailableFarmDam is written only for farmdams sources"
        + "multiple source extraction",
        date = "2025-03-25",
        version = "1.0_0"
)
@VersionComments(entries = {
    @VersionComments.Entry(version = "1.0_0", comment = "Initial version"),
    @VersionComments.Entry(version = "1.1_0", comment = "All variables contain irrigation terminology"
            + "Extraction variable is subdivided into irrigation source")
})
public class IrrigationMultipleSourceExtractionFarmDams_NN extends JAMSComponent {

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
            description = "Current time step RD2 inflow into hru. Will be updated by this component,"+
                    "extracting water for irrigation. - input / state variable",
            unit = "L"
    )
    public Attribute.Double inRD2;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Maximum daily volume withdrawn from farmdams "
                    + "Pump or pipe maximum capacity",
            defaultValue = "0",
            unit = "L/d"
    )
    public Attribute.Double farmdamMaxExtraction;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Restriction on farmdams extraction for irrigation purpose",
            defaultValue = "1"
    )
    public Attribute.Double irrigFarmdamRestriction;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "Water volume stored in Device",
            unit = "L"
    )
    public Attribute.Double farmdamStorage;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Farm dam area",
            unit = "m²"
    )
    public Attribute.Double farmdamArea;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Farm dam porosity",
            unit = "-"
    )
    public Attribute.Double farmdamPorosity;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Farm dam depth",
            unit = "m"
    )
    public Attribute.Double farmdamDepth;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Minimum farm dam storage ratio",
            unit = "%",
            defaultValue = "0.1"
    )
    public Attribute.Double farmdamMinStorageRatio;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Name of list of irrigated HRUs in Farm Dams entities (hru reservoir). List will be read by this"+
                    "component. - parameter / pointer",
            defaultValue = "irrigationFarmdamsEntities"
    )
    public Attribute.String irrigationFarmdamEntitiesListName;

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
            description = "Name of attribute that stores the irrigation water delivered to HRU by the farm dam for application (extraction"+
                    "minus losses due to efficiency). This attribute will be written to by this component."+
                    " - parameter / pointer",
            defaultValue = "irrigationApplicationFarmDam",
            unit = "L"
    )
    public Attribute.String irrigationApplicationFarmDamName;
    
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
        public Attribute.Double totalIrrigExtractionFarmDam;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "Total water available for irrigation in the Farm Dams (hru reservoir)"+
                    "BEFORE EXTRACTION. Not used. - output",
            unit = "L"
    )
    public Attribute.Double totalIrrigAvailableFarmDam;

    /*
     *  Component run stages
     */

    @Override
    public void run() {
        Attribute.Entity run_currentHRU = hrus.getCurrent();
        //check if this hru even has irrigated HRUs in its catchment
        if (!run_currentHRU.existsAttribute(irrigationFarmdamEntitiesListName.getValue())) { // no irrigated HRUs --> step out of function
            return;
        }
        
        double run_frac_irrig_applied;
        double run_farmdamMaxExtraction = farmdamMaxExtraction.getValue();
        double run_irrigFarmdamRestriction = irrigFarmdamRestriction.getValue();
        double run_farmdamStorage = farmdamStorage.getValue();
        
        //Calculate max volume of device in L; area and H in m -> m3, convert to L
        double run_maxFarmdamStorage= farmdamPorosity.getValue() * farmdamDepth.getValue() * farmdamArea.getValue() * 1000;
        double run_farmdamMinStorage = farmdamMinStorageRatio.getValue() * run_maxFarmdamStorage;
        double run_totalExtraction; // local variable to store actually extracted volume        
        
        // Consider restriction withdrawal on the maximum capacity of extraction
        double run_farmdamMaxExtractionActual = run_farmdamMaxExtraction * run_irrigFarmdamRestriction;
        double run_totalAvailable = Math.max(0, run_farmdamStorage - run_farmdamMinStorage);
        totalIrrigAvailableFarmDam.setValue(run_totalAvailable); // water available at this time step

        // totalIrrigDemand calulation
        double run_totalDemand = 0;
        double run_totalDemandRemaining = 0;
        double run_providedFraction;
        List<Attribute.Entity> run_l = (List) run_currentHRU.getObject(irrigationFarmdamEntitiesListName.getValue());
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
                        
            double run_DemandRemaining = run_demand * (1 - run_frac_irrig_applied);
            run_totalDemandRemaining += run_DemandRemaining;
            run_totalDemand += run_demand;
        }
        totalIrrigDemand.setValue(run_totalDemand);
        

        //calculate proportion of total water that is needed
        if ((run_totalAvailable != 0) && (run_totalDemandRemaining != 0)){ // if there is water available (in and/or act) AND demanded for irrigation
            
            // Extraction of the totalIrrigDemand in the farm dam
            // Define the maximum capacity of extraction in the farmdams in regard with the water available
            run_farmdamMaxExtractionActual = Math.min(run_farmdamMaxExtractionActual, run_totalAvailable);
            // Define the maximum capacity of extraction in the farmdams in regard with the irrigation demand
            run_totalExtraction = Math.min(run_farmdamMaxExtractionActual, run_totalDemandRemaining);
            // Storage update
            farmdamStorage.setValue(run_farmdamStorage - run_totalExtraction);

            // Distribute total extracted water over all HRUs supllied by this farmdams
            run_providedFraction = run_totalExtraction / run_totalDemandRemaining;
            
            //distribute total transfer over all HRUs
            double run_totalProvidedWater = 0.;
            for (Attribute.Entity run_hru : run_l) { // loop over HRUs irrigated with water from this HRU
                double run_application = run_hru.getDouble(irrigationApplicationName.getValue()); // water already applied for irrigation in this hru
                double run_plantIrrigRequirements = run_hru.getDouble(plantIrrigRequirementsName.getValue()); // plant water requirements of current HRU
                double runWaterRemaining = run_plantIrrigRequirements - run_application;
                
                run_application += runWaterRemaining * run_providedFraction;
                run_hru.setDouble(irrigationApplicationName.getValue(), run_application); // provide water to irrigated HRU (add to eventually remaining water)
                run_totalProvidedWater += runWaterRemaining * run_providedFraction; // add to counter of total provided water
                
                run_hru.setDouble(irrigationApplicationFarmDamName.getValue(), runWaterRemaining * run_providedFraction);
            }
            // restitute lost water to RD2 (when efficiency of the irrigation network <1) :
            inRD2.setValue(inRD2.getValue() + Math.max(0., run_totalExtraction - run_totalProvidedWater) );            
            
        } else { // no water available OR no water demanded for irrigation -> no extraction, no delivery
            run_totalExtraction = 0; // No extraction
        }
        totalIrrigExtractionFarmDam.setValue(run_totalExtraction);
        
        if(run_totalDemandRemaining == 0) {
            for (Attribute.Entity hru : run_l) {
                hru.setDouble(irrigationApplicationFarmDamName.getValue(), 0); 
            }
        }
        if(run_totalDemand == 0){
            for (Attribute.Entity hru : run_l) {
                hru.setDouble(irrigationApplicationName.getValue(), 0); 
            }             
        }
        if(run_totalAvailable == 0) {
            for (Attribute.Entity hru : run_l) {
                hru.setDouble(irrigationApplicationFarmDamName.getValue(), 0); 
            }
        }
        
        //remove all HRUs from demand list
        run_l.removeAll(run_l);
    }
}
