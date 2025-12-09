/*
 * HRU_device.java
 * Created on 27.10.2022, 15:44:17
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
import java.util.Calendar;
import java.util.GregorianCalendar;



@JAMSComponentDescription(
        title = "Initialized HRU device volume and area",
        author = "Nathan Pellerin",
        description = "Initiate device area and its volume in the Initialization Context",
        date = "2025-12-09",
        version = "1.0_0")

public class Init_HRU_device extends JAMSComponent {

    /*
     *  Component attributes
     */
    @JAMSVarDescription(
        access = JAMSVarDescription.AccessType.READ,
        description = "HRU area",
        unit = "m²")
    public Attribute.Double area;
    
    @JAMSVarDescription(
        access = JAMSVarDescription.AccessType.READ,
        description = "Fraction of the HRU that composed the device",
        unit = "-")
    public Attribute.Double fracAreaDevice;
    
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
            description = "Device depth",
            unit = "m")
    public Attribute.Double deviceDepth;
    
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
            description = "Device porosity",
            unit = "-")
    public Attribute.Double devicePorosity;
    
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
            description = "Fraction of the volume at the initial state",
            unit = "-")
    public Attribute.Double fracDeviceInitVol;
    
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.WRITE,
            description = "Device area inside the HRU",
            unit = "m²")
    public Attribute.Double areaDevice;
    
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.WRITE,
            description = "HRU area minus the device",
            unit = "m²")
    public Attribute.Double areaHRUminusDevice;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "Maximum capacity of the device",
            unit = "L")
    public Attribute.Double maxDeviceVol;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "Volume in the device",
            unit = "L")
    public Attribute.Double deviceVol;
    
    
    /*
     *  Component run stages
     */
    @Override
    public void init() {
        getModel().getRuntime().println("Farm dams initialized !");
    }

    @Override
    public void run() {
        
        double run_area = area.getValue();
        double run_deviceArea = fracAreaDevice.getValue();
        double run_h = deviceDepth.getValue();
        double run_p = devicePorosity.getValue();
        
        //Calculate max volume of device in L; area and H in m -> m3, convert to L
        double run_maxVolumeDevice = run_deviceArea * run_p * run_h * 1000;
        // Calculate the initial volume in the device, from the full capacity 
        double run_fracDeviceInitVol = fracDeviceInitVol.getValue();
        double run_deviceInitVol = run_fracDeviceInitVol * run_maxVolumeDevice;
        
        //Calculate surfaces concerning the device and the rest of the HRU
        double run_areaDevice = run_area * run_deviceArea; //in m²
        double run_areaHRUminusDevice = run_area - run_areaDevice; //in m²
        
        // write variables
        areaDevice.setValue(run_areaDevice);
        areaHRUminusDevice.setValue(run_areaHRUminusDevice);
        maxDeviceVol.setValue(run_maxVolumeDevice);
        deviceVol.setValue(run_deviceInitVol);
    }

    @Override
    public void cleanup() {
    }
}
