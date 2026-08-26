/*
 * IrrigationWaterTransfer_act_farmdams.java
 * Created on 2024-12-17, 16:45:01
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
 * @author npellerin
 */
@JAMSComponentDescription(
        title = "",
        author = "Nathan Pellerin",
        description = "Transfer water from device to irrigated HRUs, which are using this type of irrigation",
        date = "2024-12-17",
        version = "1.0_0"
)
@VersionComments(entries = {
    @VersionComments.Entry(version = "1.0_0", comment = "Initial version")
})
public class IrrigationWaterTransfer_act_farmdams extends JAMSComponent {
    
    /*
     *  Component attributes
     */
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "HRUs list"
    )
    public Attribute.EntityCollection hrus;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Name of list of irrigated HRUs in reach entities",
            defaultValue = "irrigationEntities"
    )
    public Attribute.String irrigationEntitiesListName;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Name of attribute that stores irrigation demand of an HRU - plant water requirement / efficiency",
            defaultValue = "irrigationDemand",
            unit = "L"
    )
    public Attribute.String irrigationDemandName;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Maximum daily volume withdrawn from device "
                    + "Pump or pipe maximum capacity",
            defaultValue = "0",
            unit = "L/d"
    )
    public Attribute.Double deviceMaxWithdrawal;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "Total demand of water for irrigation, including the enhancement by poor efficiency",
            unit = "L"
    )
    public Attribute.Double totalDemand;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "Total irrigation transfer (= prelemenents, enhanced by poor efficiency)",
            unit = "L"
    )
    public Attribute.Double totalTransfer;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "Water volume stored in Device",
            unit = "L"
    )
    public Attribute.Double deviceVol;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Minimum Device storage volume",
            unit = "L"
    )
    public Attribute.Double deviceMinStorage;
    
    
    /*
     *  Component run stages
     */

    @Override
    public void run() {
        
        Attribute.Entity currentHRU = hrus.getCurrent(); // A device is a reservoir being part of an HRU
        
        //check if this device even has irrigated HRUs in its catchment
        if (!currentHRU.existsAttribute(irrigationEntitiesListName.getValue())) {
            return;
        }
        double totalDemand = 0;

        // cumulated demand from all HRUs supplied with water from the current HRU
        List<Attribute.Entity> l = (List) currentHRU.getObject(irrigationEntitiesListName.getValue());
        for (Attribute.Entity hru : l) {
            double demand = hru.getDouble(irrigationDemandName.getValue());
            totalDemand += demand;
        }
        this.totalDemand.setValue(totalDemand);
        
        
        double totalTransfer = 0;
        // extraction from device is possible in the limit of its capacity
        if (deviceVol.getValue() > deviceMinStorage.getValue()){
            // Reduction of the total transfer to the pump or pipe capacity 
            if (totalDemand >= deviceMaxWithdrawal.getValue()){
                totalTransfer = deviceMaxWithdrawal.getValue(); // Part of the demand is satisfied, in respect with deviceMaxWithdrawal
            }else {
                totalTransfer = totalDemand; // the entire demand is satisfied
            }
        }else{
            totalTransfer = 0;
        }
        
        deviceVol.setValue(deviceVol.getValue()-totalTransfer); // Substract water abstracted to the device volume
        this.totalTransfer.setValue(totalTransfer);             // Update of the water transferred
        
        
        //remove all HRUs from demand list
        l.removeAll(l);
    }
    
}
