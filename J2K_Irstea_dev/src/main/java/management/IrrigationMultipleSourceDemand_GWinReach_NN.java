/*
 * IrrigationDemand.java
 * Created on 12.08.2015, 16:47:30
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Sven Kralisch <sven.kralisch at uni-jena.de>
 */
@JAMSComponentDescription(
        title = "",
        author = "Sven Kralisch / LC / Nico Hachgenei / Nathan Pellerin",
        description = "Calculate irrigation demand including maximum irrigation dose," +
                " saturation goal, groundwater demand, new names. Modification for the case" +
                "where source reach and hru are defined -> warn and take from reach."
                + "multiple source extraction",
        date = "2015-08-12 / 2021-05-26 / 2025-03-25",
        version = "3.1_0"
)
@VersionComments(entries = {
    @VersionComments.Entry(version = "1.0_0", comment = "Initial version"),
    @VersionComments.Entry(version = "2.0_0", comment = "New names. Modification for the case" +
                "where source reach and hru are defined -> warn and take from reach."),
    @VersionComments.Entry(version = "3.0_0", comment = "multiple source extraction"),
    @VersionComments.Entry(version = "3.1_0", comment = "All variables contain irrigation terminology"),
    @VersionComments.Entry(version = "3.2_0", comment = "GW extraction is done in the reach (RG1 contribution)")
})
public class IrrigationMultipleSourceDemand_GWinReach_NN extends JAMSComponent {

    /*
     *  Component attributes
     */
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Reaches list"
    )
    public Attribute.EntityCollection reaches;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "HRUs list"
    )
    public Attribute.EntityCollection hrus;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "HRU area (get from HRULoop). - parameter",
            unit = "m²"
    )
    public Attribute.Double area;
        
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Reach/Subbasin ID (reach extraction) for irrigation source (get from ReachLoop). - parameter"
    )
    public Attribute.Double irrig_source_ID_reach;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Reach/Subbasin ID (reach extraction (groundwater extraction) for irrigation source (get from ReachLoop). - parameter"
    )
    public Attribute.Double irrig_source_ID_GWinReach;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "HRU ID (farm dam extraction) for irrigation source (get from HRULoop). - parameter"
    )
    public Attribute.Double irrig_source_ID_farmdam;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "potential evapotranspiration value (get from HRULoop).",
            unit = "L"
    )
    public Attribute.Double potET;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "actual evapotranspiration value (get from HRULoop).",
            unit = "L"
    )
    public Attribute.Double actET;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Large pore space LPS saturation value (get from HRULoop). - state"
    )
    public Attribute.Double satLPS;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Medium pore space MPS saturation value (get from HRULoop). - state"
    )
    public Attribute.Double satMPS;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Large pore space LPS capacity (get from HRULoop). - parameter",
            unit = "L"
    )
    public Attribute.Double maxLPS;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Medium pore space MPS capacity (get from HRULoop). - parameter",
            unit = "L"
    )
    public Attribute.Double maxMPS;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Minimal allowed actual evapotranspiration fraction (actET/potET). Below this threshold, irrigation is required. - parameter [comment: deficit is not the right name]",
            defaultValue = "0.9"
    )
    public Attribute.Double etDeficit;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Name of list of irrigated HRUs in hru entities (GW reservoir). List will be read by this"+
                    "component. - parameter / pointer",
            defaultValue = "irrigationGWinReachEntities"
    )
    public Attribute.String irrigationGWinReachEntitiesListName;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Name of list of irrigated HRUs in reach entities. List will be read by this"+
                    "component. - parameter / pointer",
            defaultValue = "irrigationReachEntities"
    )
    public Attribute.String irrigationReachEntitiesListName;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Name of list of irrigated HRUs in hru entities (farm dam reservoir). List will be read by this"+
                    "component. - parameter / pointer",
            defaultValue = "irrigationFarmdamsEntities"
    )
    public Attribute.String irrigationFarmdamEntitiesListName;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Correction factor for irrigation demand based on medium pore space MPS - parameter",
            defaultValue = "1"
    )
    public Attribute.Double irrigationDemandCorrectionMPS;    
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Correction factor for irrigation demand based on large pore space LPS - parameter",
            defaultValue = "1"
    )
    public Attribute.Double irrigationDemandCorrectionLPS;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Maximal dose for irrigation - parameter",
            unit = "mm",
            defaultValue = "0"
    )
    public Attribute.Double maxIrrigDosis; 
    
        @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Expected saturation of medium pore space MPS (saturation goal of irrigation). - parameter"
    )
    public Attribute.Double satMPSexp; 
    
                @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "expected saturation of large pore space LPS (saturation goal of irrigation). - parameter" 
    )
    public Attribute.Double satLPSexp; 
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Efficiency of the irrigation system. The lower this efficiency, the higher the irrigation" +
                "demand for the same plant water requirement. Difference lost into RD2. - parameter",
 	    defaultValue = "1"
    )
    public Attribute.Double irrigSystemEfficiency; 
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "Real plant irrigation water requirement (= water deficit). - output",
            unit = "L"        
              )
    public Attribute.Double plantIrrigRequirements;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "Irrigation demand (amount of water to take = plantIrrigRequirements/irrigSystemEfficiency) - output",
            unit = "L"
    )
    public Attribute.Double irrigationDemand;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Irrigation configuration for supply (hru only, reach only,"
            + " farmdams only, reach + hru, reach + farmdams, farmdams + hru )"
    )
    public Attribute.Double irrigationSourceTypeConfiguration;

                
                
    private Map<Long, Attribute.Entity> run_reachMap = new HashMap();
    private Map<Long, Attribute.Entity> run_hruMap = new HashMap();

    /*
     *  Component run stages
     */
    @Override
    public void init() {
        //put all reaches to a map for easier access
        for (Attribute.Entity run_reach : reaches.getEntities()) {
            run_reachMap.put(run_reach.getId(), run_reach);
        }
        //put all hrus to a map for easier access
        for (Attribute.Entity run_hru : hrus.getEntities()) {
            run_hruMap.put(run_hru.getId(), run_hru);
        }
    }

    @Override
    public void run() {

        if (potET.getValue() == 0) { // if no potET -> only reset demanded water = 0 and step out of the function
            irrigationDemand.setValue(0);
            plantIrrigRequirements.setValue(0);
            return;
        }

        //check if ET deficit is higher than a given threshold
        if ((actET.getValue() / potET.getValue()) < etDeficit.getValue()) {

            //need to irrigate, now check water deficit
            double run_deficiteVolume = Math.max(0,(satLPSexp.getValue() - satLPS.getValue())) * maxLPS.getValue() * irrigationDemandCorrectionLPS.getValue() +
                                    Math.max(0,(satMPSexp.getValue() - satMPS.getValue())) * maxMPS.getValue() * irrigationDemandCorrectionMPS.getValue();
            if (maxIrrigDosis.getValue() > 0) {
                run_deficiteVolume = Math.min(run_deficiteVolume, maxIrrigDosis.getValue() * area.getValue());
            }
            
            //set the demand
            plantIrrigRequirements.setValue(run_deficiteVolume);
            irrigationDemand.setValue(run_deficiteVolume/irrigSystemEfficiency.getValue());

            //get the matching reach/hru for the current HRU
            Attribute.Entity run_hruID = hrus.getCurrent();
            
            // Add the hru ID which is irrigated in list, depending on the source type configuration
            // Source type configurations
            /* 1 : Groundwater
             * 2 : Reach
             * 3 : farmdams
             * 4 : Reach + HRU
             * 5 : Reach + farmdams
             * 6 : farmdams + HRU
             */
            double run_irrigationSourceTypeConfiguration = irrigationSourceTypeConfiguration.getValue();
            
            if (run_irrigationSourceTypeConfiguration == 1 || run_irrigationSourceTypeConfiguration == 4 || run_irrigationSourceTypeConfiguration == 6){
                Attribute.Entity reach = run_reachMap.get((long) irrig_source_ID_GWinReach.getValue());
                //add the current HRU to the list of HRUs to be irrigated by that HRU/Subbasin
                if (!reach.existsAttribute(irrigationGWinReachEntitiesListName.getValue())) {
                    reach.setObject(irrigationGWinReachEntitiesListName.getValue(), new ArrayList<Attribute.Entity>());
                }
                List<Attribute.Entity> l1 = (List) reach.getObject(irrigationGWinReachEntitiesListName.getValue());
                l1.add(run_hruID);
            }
            
            if (run_irrigationSourceTypeConfiguration == 2 || run_irrigationSourceTypeConfiguration == 4 || run_irrigationSourceTypeConfiguration == 5){
                Attribute.Entity reach = run_reachMap.get((long) irrig_source_ID_reach.getValue());
                //add the current HRU to the list of HRUs to be irrigated by that reach
                if (!reach.existsAttribute(irrigationReachEntitiesListName.getValue())) {
                    reach.setObject(irrigationReachEntitiesListName.getValue(), new ArrayList<Attribute.Entity>());
                }
                List<Attribute.Entity> l2 = (List) reach.getObject(irrigationReachEntitiesListName.getValue());
                l2.add(run_hruID);
            }
            
            if (run_irrigationSourceTypeConfiguration == 3 || run_irrigationSourceTypeConfiguration == 5 || run_irrigationSourceTypeConfiguration == 6){
                Attribute.Entity farmdams = run_hruMap.get((long) irrig_source_ID_farmdam.getValue()); 
                //add the current HRU to the list of HRUs to be irrigated by that farmdams
                if (!farmdams.existsAttribute(irrigationFarmdamEntitiesListName.getValue())) {
                    farmdams.setObject(irrigationFarmdamEntitiesListName.getValue(), new ArrayList<Attribute.Entity>());
                }
                List<Attribute.Entity> l3 = (List) farmdams.getObject(irrigationFarmdamEntitiesListName.getValue());
                l3.add(run_hruID);
            }
            
        } else { // if there is no water deficit, no water is demanded
            irrigationDemand.setValue(0);
            plantIrrigRequirements.setValue(0);
        }
    }
}
