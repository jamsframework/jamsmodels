/*
 * IrrigationDam_ApplicationAspersion.java
 * Created on 10.09.2020, 23:04:51
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
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Sven Kralisch <sven.kralisch@uni-jena.de>
 */
@JAMSComponentDescription(
        title = "IrrigationDam_ApplicationAspersion",
        author = "Sven Kralisch",
        description = "TBD",
        date = "2020-09-10",
        version = "1.0_0"
)
@VersionComments(entries = {
    @VersionComments.Entry(version = "1.0_0", comment = "Initial version")
})
public class IrrigationDam_ApplicationAspersion extends JAMSComponent {

    /*
     *  Component attributes
     */
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Subbasin ID"
    )
    public Attribute.Double subbasinID;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "All reaches"
    )
    public Attribute.EntityCollection reaches;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "The subbasin reach"
    )
    public Attribute.Entity subbasinReach;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "maximum MPS",
            unit = "L"
    )
    public Attribute.Double maxMPS;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "state var actual MPS",
            unit = "L"
    )
    public Attribute.Double actMPS;    

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "correction factor for irrigation applied [0,1]",
            defaultValue = "1"
    )
    public Attribute.Double correctionFactor;    
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Name of reach dam storage attribute",
            defaultValue = "damStorage"
    )
    public Attribute.String damStorageAttributeName;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "Applied irrigation volume",
            unit = "L"
    )
    public Attribute.Double irrigationApplied;

        @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "Irrigation demand",
            unit = "L"
    )
    public Attribute.Double demand;
    

    Map<Long, Attribute.Entity> reachMap = new HashMap();

    /*
     *  Component run stages
     */
    @Override
    public void init() {
        for (Attribute.Entity reach : reaches.getEntities()) {
            reachMap.put(reach.getId(), reach);
        }
    }

    @Override
    public void initAll() {
        Attribute.Entity reach = reachMap.get((long) subbasinID.getValue());
        subbasinReach.setValue(reach.getValue());
        subbasinReach.setId(reach.getId());
    }

    @Override
    public void run() {
        
        // calculation of irrigation demand
        double demand = maxMPS.getValue() - actMPS.getValue();
        
        // get available water from corresponding dam
        double storage = subbasinReach.getDouble(damStorageAttributeName.getValue());
        
        // irrigation is the min of both
        double irrigation = Math.min(demand, storage);
        
        // adapt irrigation
        irrigation *= correctionFactor.getValue();

        // increase actMPS by the irrigation volume
        actMPS.setValue(actMPS.getValue() + irrigation);

        // decrease irrigationWater by the irrigation volume
        subbasinReach.setDouble(damStorageAttributeName.getValue(), storage - irrigation);

        // set irrigationApplied to the irrigation volume
        irrigationApplied.setValue(irrigation);        
     
       this.demand.setValue (demand); 
        
    }

    @Override
    public void cleanup() {
    }
}
