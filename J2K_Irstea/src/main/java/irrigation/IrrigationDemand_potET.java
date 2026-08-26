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
package irrigation;

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
        author = "Sven Kralisch",
        description = "Calculate irrigation demand",
        date = "2015-08-12",
        version = "1.0_0"
)
@VersionComments(entries = {
    @VersionComments.Entry(version = "1.0_0", comment = "Initial version")
})
public class IrrigationDemand_potET extends JAMSComponent {

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
            description = "Subbasin ID"
    )
    public Attribute.Double subBasin;

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
            description = "Minimal allowed ET deficit (actET/potET)",
            defaultValue = "0.9"
    )
    public Attribute.Double etDeficit;
    
        @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Correction factor for irrigation demand based on potET - actET",
            defaultValue = "1"
    )
    public Attribute.Double irrigationDemandCorrectionET; 
        
        @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "maximal dosis for irrigation",
            unit = "mm",
            defaultValue = "0"
            
    )
    public Attribute.Double maxDosis; 
            
        

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Name of list of irrigated HRUs in reach entities",
            defaultValue = "irrigationEntities"
    )
    public Attribute.String irrigationEntitiesListName;


    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "Irrigation demand"
    )
    public Attribute.Double irrigationDemand;

    private Map<Long, Attribute.Entity> reachMap = new HashMap();

    /*
     *  Component run stages
     */
    @Override
    public void init() {
        //put all reaches to a map for easier access
        for (Attribute.Entity reach : reaches.getEntities()) {
            reachMap.put(reach.getId(), reach);
        }
    }

    @Override
    public void run() {

        irrigationDemand.setValue(0);
        
        if (potET.getValue() == 0) {
            return;
        }

        //check if ET deficit is higher than a given threshold
        if ((actET.getValue() / potET.getValue()) < etDeficit.getValue()) {

            //need to irrigate, now check water deficit
            double deficiteVolume = (potET.getValue() - actET.getValue()) * irrigationDemandCorrectionET.getValue();
              if (maxDosis.getValue() > 0) {
                  deficiteVolume = Math.min(maxDosis.getValue() * area.getValue(),(potET.getValue() - actET.getValue()) * irrigationDemandCorrectionET.getValue());
              }

            //set the demand
            irrigationDemand.setValue(deficiteVolume);

            //get the matching reach for the current HRU
            Attribute.Entity hru = hrus.getCurrent();
            Attribute.Entity reach = reachMap.get((long) subBasin.getValue());
            
            if (reach == null) {
                //this should never happen
                return;
            }
            
            //add the current HRU to the list of HRUs to be irrigated by that reach
            if (!reach.existsAttribute(irrigationEntitiesListName.getValue())) {
                reach.setObject(irrigationEntitiesListName.getValue(), new ArrayList<Attribute.Entity>());
            }
            List<Attribute.Entity> l = (List) reach.getObject(irrigationEntitiesListName.getValue());

            l.add(hru);
        }

    }

}
