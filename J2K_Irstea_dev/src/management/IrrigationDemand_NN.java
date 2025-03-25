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
        author = "Sven Kralisch / LC / Nathan Pellerin / Nico Hachgenei",
        description = "Calculate irrigation demand including maximum irrigation dose," +
                " saturation goal, groundwater demand, new names. Modification for the case" +
                "where source reach and hru are defined -> warn and take from reach.",
        date = "2015-08-12 / 2021-05-26 / 2025-03-25",
        version = "2.0_0"
)
@VersionComments(entries = {
    @VersionComments.Entry(version = "1.0_0", comment = "Initial version"),
    @VersionComments.Entry(version = "2.0_0", comment = "New names. Modification for the case" +
                "where source reach and hru are defined -> warn and take from reach.")
})
public class IrrigationDemand_NN extends JAMSComponent {

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
            description = "Reach/Subbasin ID for irrigation source (get from HRULoop). - parameter"
    )
    public Attribute.Double sourceReach;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "HRU ID for irrigation source (get from HRULoop). - parameter"
    )
    public Attribute.Double sourceHRU;

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
            description = "Name of list of irrigated HRUs in source entities",
            defaultValue = "irrigationEntities"
    )
    public Attribute.String irrigationEntitiesListName;

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
    public Attribute.Double maxDosis; 
    
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
    public Attribute.Double efficiency; 
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "Real plant irrigation water requirement (= water deficit). - output",
            unit = "L"        
              )
    public Attribute.Double plantIrrigRequirements;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "Irrigation demand (amount of water to take = plantIrrigRequirements/efficiency) - output",
            unit = "L"
    )
    public Attribute.Double irrigationDemand;


                
                
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

        if (potET.getValue() == 0) {
            return;
        }

        //check if ET deficit is higher than a given threshold
        if ((actET.getValue() / potET.getValue()) < etDeficit.getValue()) {

            //need to irrigate, now check water deficit
            double run_deficiteVolume = Math.max(0,(satLPSexp.getValue() - satLPS.getValue())) * maxLPS.getValue() * irrigationDemandCorrectionLPS.getValue() +
                                    Math.max(0,(satMPSexp.getValue() - satMPS.getValue())) * maxMPS.getValue() * irrigationDemandCorrectionMPS.getValue();
            if (maxDosis.getValue() > 0) {
                run_deficiteVolume = Math.min(run_deficiteVolume, maxDosis.getValue() * area.getValue());
            }
            
            //set the demand
            plantIrrigRequirements.setValue(run_deficiteVolume);
            irrigationDemand.setValue(run_deficiteVolume/efficiency.getValue());

            //get the matching reach/hru for the current HRU
            Attribute.Entity run_hruID = hrus.getCurrent();
            Attribute.Entity run_reach = run_reachMap.get((long) sourceReach.getValue());
            Attribute.Entity run_hru = run_hruMap.get((long) sourceHRU.getValue());
            
            
            // now in case source reach and HRU are both defined, water will be taken in reach only,
            // and a warning is shown.
            // (in the old version, nothing was extracted)
            if (run_reach != null){ // an irrigation source reach is defined --> take water from there
                if (run_hru != null) { // an irrigation source HRU is also defined (in addition to reach). this is an error!
                    // send message to log to warn user and only take from source reach only.
                    getModel().getRuntime().sendInfoMsg("WARNING: HRU " + run_hruID + " has source reach (" + run_reach + 
                            ") and source HRU (" + run_hru + ") defined. Water will be taken from reach only.");
                }
                //add the current reach to the list of HRUs to be irrigated by that reach
                if (!run_reach.existsAttribute(irrigationEntitiesListName.getValue())) {
                    run_reach.setObject(irrigationEntitiesListName.getValue(), new ArrayList<Attribute.Entity>());
                }
                List<Attribute.Entity> run_l1 = (List) run_reach.getObject(irrigationEntitiesListName.getValue());

                run_l1.add(run_hruID);
                
                //Check : Display source for each irrigated HRU
                //getModel().getRuntime().sendInfoMsg("HRU ID " + hruID.getId() + " has source " + sourceReach.getValue()+". Irrigation demand: "+irrigationDemand.getValue());
                
            } else if (run_hru != null) { // an irrigation source HRU is defined --> take water from there
                //add the current HRU to the list of HRUs to be irrigated by that HRU/Subbasin
                if (!run_hru.existsAttribute(irrigationEntitiesListName.getValue())) {
                    run_hru.setObject(irrigationEntitiesListName.getValue(), new ArrayList<Attribute.Entity>());
                }
                List<Attribute.Entity> run_l2 = (List) run_hru.getObject(irrigationEntitiesListName.getValue());

                run_l2.add(run_hruID);
                
                //Check : Display source for each irrigated HRU
                //getModel().getRuntime().sendInfoMsg("HRU ID " + hruID.getId() + " has source " + sourceHRU.getValue());
                
            } else { // neither source reach nor source HRU are defined. This should never happen.
                getModel().getRuntime().sendInfoMsg("WARNING: HRU " + run_hruID + ": No irrigation source is defined."+
                        "Nothing is extracted.");
                //return;
            }
        } else { // if there is no water deficit, no water is demanded
            irrigationDemand.setValue(0);
            plantIrrigRequirements.setValue(0);
        }
    }
}
