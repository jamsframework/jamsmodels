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
        author = "Sven Kralisch / LC",
        description = "Calculate irrigation demand including groundwater demand",
        date = "2015-08-12 / 2021-05-26",
        version = "1.0_0"
)
@VersionComments(entries = {
    @VersionComments.Entry(version = "1.0_0", comment = "Initial version")
})
public class IrrigationDemand_maxDose_MPS_gw extends JAMSComponent {

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
            description = "HRU area",
            unit = "m²"
    )
    public Attribute.Double area;
        
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Reach/Subbasin ID for irrigation source"
    )
    public Attribute.Double sourceReach;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "HRU ID for irrigation source"
    )
    public Attribute.Double sourceHRU;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "potET value"
    )
    public Attribute.Double potET;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "actET value"
    )
    public Attribute.Double actET;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "satLPS value"
    )
    public Attribute.Double satLPS;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "satMPS value"
    )
    public Attribute.Double satMPS;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "maxLPS value"
    )
    public Attribute.Double maxLPS;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "maxMPS value"
    )
    public Attribute.Double maxMPS;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Minimal allowed ET deficit (actET/potET)",
            defaultValue = "0.9"
    )
    public Attribute.Double etDeficit;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Name of list of irrigated HRUs in reach entities",
            defaultValue = "irrigationEntities"
    )
    public Attribute.String irrigationEntitiesListName;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Correction factor for irrigation demand based on MPS",
            defaultValue = "1"
    )
    public Attribute.Double irrigationDemandCorrectionMPS;    
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Correction factor for irrigation demand based on LPS",
            defaultValue = "1"
    )
    public Attribute.Double irrigationDemandCorrectionLPS;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "maximal dosis for irrigation",
            unit = "mm",
            defaultValue = "0"
    )
    public Attribute.Double maxDosis; 
    
        @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "expected saturation of MPS"
            
    )
    public Attribute.Double satMPSexp; 
    
                @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "expected saturation of LPS"
            
    )
    public Attribute.Double satLPSexp; 
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "efficiency of the irrigation system",
 	    defaultValue = "1"

    )
        public Attribute.Double efficiency; 
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "Real plant irrigation water requirement"            
              )
    public Attribute.Double waterRequirements;
    
        @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "Irrigation demand (= waterRequirements/efficiency)"
    )
    public Attribute.Double irrigationDemand;


                
                
    private Map<Long, Attribute.Entity> reachMap = new HashMap();
    private Map<Long, Attribute.Entity> hruMap = new HashMap();

    /*
     *  Component run stages
     */
    @Override
    public void init() {
        //put all reaches to a map for easier access
        for (Attribute.Entity reach : reaches.getEntities()) {
            reachMap.put(reach.getId(), reach);
        }
        //put all hrus to a map for easier access
        for (Attribute.Entity hru : hrus.getEntities()) {
            hruMap.put(hru.getId(), hru);
        }
    }

    @Override
    public void run() {

        irrigationDemand.setValue(0);
        waterRequirements.setValue(0); 
        if (potET.getValue() == 0) {
            return;
        }

        //check if ET deficit is higher than a given threshold
        if ((actET.getValue() / potET.getValue()) < etDeficit.getValue()) {

            //need to irrigate, now check water deficit
            //double deficiteVolume = (1 - satLPS.getValue()) * maxLPS.getValue() * irrigationDemandCorrectionLPS.getValue() +
            //                      (1 - satMPS.getValue()) * maxMPS.getValue() * irrigationDemandCorrectionMPS.getValue();
             //  double deficiteVolume = Math.max(0,(0.2 - satLPS.getValue())) * maxLPS.getValue() * irrigationDemandCorrectionLPS.getValue() +
            //                      Math.max(0,(0.2 - satMPS.getValue())) * maxMPS.getValue() * irrigationDemandCorrectionMPS.getValue();
          double deficiteVolume = Math.max(0,(satLPSexp.getValue() - satLPS.getValue())) * maxLPS.getValue() * irrigationDemandCorrectionLPS.getValue() +
                                    Math.max(0,(satMPSexp.getValue() - satMPS.getValue())) * maxMPS.getValue() * irrigationDemandCorrectionMPS.getValue();
            if (maxDosis.getValue() > 0) {
            deficiteVolume = Math.min(deficiteVolume, maxDosis.getValue() * area.getValue());
            }
         
            //set the demand
            waterRequirements.setValue(deficiteVolume);
            irrigationDemand.setValue(deficiteVolume/efficiency.getValue());

            //get the matching reach/hru for the current HRU
            Attribute.Entity hruID = hrus.getCurrent();
            Attribute.Entity reach = reachMap.get((long) sourceReach.getValue());
            Attribute.Entity hru = hruMap.get((long) sourceHRU.getValue());
            
            if (reach == null) {
                if (hru == null) {
                    //this should never happen
                    return;
                }
                
                //add the current HRU to the list of HRUs to be irrigated by that HRU/Subbasin
                if (!hru.existsAttribute(irrigationEntitiesListName.getValue())) {
                    hru.setObject(irrigationEntitiesListName.getValue(), new ArrayList<Attribute.Entity>());
                }
                List<Attribute.Entity> l2 = (List) hru.getObject(irrigationEntitiesListName.getValue());

                l2.add(hruID);
                
                //Check : Display source for each irrigated HRU
                //getModel().getRuntime().sendInfoMsg("HRU ID " + hruID.getId() + " has source " + sourceHRU.getValue());
            }
            
            if (hru == null) {
                if (reach == null) {
                    //this should never happen
                    return;
                }
                
                //add the current HRU to the list of HRUs to be irrigated by that reach
                if (!reach.existsAttribute(irrigationEntitiesListName.getValue())) {
                    reach.setObject(irrigationEntitiesListName.getValue(), new ArrayList<Attribute.Entity>());
                }
                List<Attribute.Entity> l1 = (List) reach.getObject(irrigationEntitiesListName.getValue());

                l1.add(hruID);
                
                //Check : Display source for each irrigated HRU
                //getModel().getRuntime().sendInfoMsg("HRU ID " + hruID.getId() + " has source " + sourceReach.getValue());
            }
            
        }

    }

}
