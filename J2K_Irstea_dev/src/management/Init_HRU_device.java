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
        title = "Initialised HRU device volume and area",
        author = "Nathan Pellerin",
        description = "Initialise device area and its volume in the Initialization Context."
                + "Initialise infiltration constants",
        date = "2025-12-09",
        version = "1.0_0")

@VersionComments(entries = {
    @VersionComments.Entry(version = "1.0_0", comment = "Initial version"),
    @VersionComments.Entry(version = "1.1_0", comment = "Equivalent surface of device are declared in hru.par"),
    @VersionComments.Entry(version = "1.1_1", comment = "Initialise infiltration constants")
})

public class Init_HRU_device extends JAMSComponent {

    /*
     *  Component attributes
     */
    
    //timestep
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Simulation time interval: 'h' for hourly and 'd' for daily")
    public Attribute.String dt;
    
    @JAMSVarDescription(
        access = JAMSVarDescription.AccessType.READ,
        description = "HRU area",
        unit = "m²")
    public Attribute.Double area;
    
    @JAMSVarDescription(
        access = JAMSVarDescription.AccessType.READ,
        description = "Device area, present in the HRU",
        unit = "m²")
    public Attribute.Double deviceArea;
    
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
            description = "Device depth",
            unit = "m")
    public Attribute.Double deviceDepth;
    
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
            description = "Device porosity",
            unit = "-")
    public Attribute.Double devicePorosity;
    
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
            description = "Initial water volume stored in Device as fraction of max volume",
            unit = "-")
    public Attribute.Double fracDeviceInitVol;
    
    // Initial volume stored in device 
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Fraction of initial water volume stored in Device "
            + "being from RD1.",
            unit = "-",
            defaultValue = "0.4"
    )
    public Attribute.Double DeviceInitFracRD1;
    
    // Initial volume stored in device 
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Fraction of initial water volume stored in Device "
            + "being from RD2.",
            unit = "-",
            defaultValue = "0.4"
    )
    public Attribute.Double DeviceInitFracRD2;
    
    // Initial volume stored in device 
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Fraction of initial water volume stored in Device "
            + "being from RG1.",
            unit = "-",
            defaultValue = "0.2"
    )
    public Attribute.Double DeviceInitFracRG1;
    
    // Initial volume stored in device 
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Fraction of initial water volume stored in Device "
            + "being from RG2.",
            unit = "-",
            defaultValue = "0.0"
    )
    public Attribute.Double DeviceInitFracRG2;
    
    
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
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "Infiltration coeficient of the device regarding time step conversion",
            unit = "-")
    public Attribute.Double coef_Qinf;
    
     // intercepted flow
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "Total RD1 stored in device",
            unit = "L"
    )
    public Attribute.Double Device_actRD1;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "Total RD2 stored in device",
            unit = "L"
    )
    public Attribute.Double Device_actRD2;
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "Total RG1 stored in device",
            unit = "L"
    )
    public Attribute.Double Device_actRG1;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "Total RG2 stored in device",
            unit = "L"
    )
    public Attribute.Double Device_actRG2;
    
    /*
     *  Component run stages
     */
    @Override
    public void init() {
        getModel().getRuntime().println("Farm dams initialized !");
    }

    @Override
    public void run() {
        
        double run_hru_area = area.getValue();  //in m²
        double run_deviceArea = deviceArea.getValue();  //in m²
        double run_h = deviceDepth.getValue();  //in m
        double run_p = devicePorosity.getValue();
        
        //Calculate max volume of device in L; area and H in m -> m3, convert to L
        double run_maxVolumeDevice = run_deviceArea * run_p * run_h * 1000;
        
        // Calculate the initial volume in the device, from the full capacity 
        double run_fracDeviceInitVol = fracDeviceInitVol.getValue();
        double run_initVol = run_fracDeviceInitVol * run_maxVolumeDevice;
        getModel().getRuntime().println("Initial FD volume : " + run_initVol);
        double run_initFracRD1 = DeviceInitFracRD1.getValue();
        double run_initFracRD2 = DeviceInitFracRD2.getValue();
        double run_initFracRG1 = DeviceInitFracRG1.getValue();
        double run_initFracRG2 = DeviceInitFracRG2.getValue();
        double run_fracSum = run_initFracRD1 + run_initFracRD2 + run_initFracRG1 + run_initFracRG2;
        // check whether fractions per component sum to 1, otherwise correct and warn
        if (run_fracSum == 0.0) { // if 0: set to default
            getModel().getRuntime().println("WARNING: sum of device init fractions "
            + " is 0, will be set to default values.");
            run_initFracRD1 = 0.4;
            run_initFracRD2 = 0.4;
            run_initFracRG1 = 0.2;
            run_initFracRG2 = 0.0;
            // run_fracSum = run_initFracRD1 + run_initFracRD2 + run_initFracRG1 + run_initFracRG2;
        } else if (run_fracSum != 1.0) { // otherwise, adapt proportionally
            getModel().getRuntime().println("WARNING: sum of device init fractions "
            + " is "+ run_fracSum + ", will be corrected to 1.");
            run_initFracRD1 /= run_fracSum;
            run_initFracRD2 /= run_fracSum;
            run_initFracRG1 /= run_fracSum;
            run_initFracRG2 /= run_fracSum;
            // run_fracSum = run_initFracRD1 + run_initFracRD2 + run_initFracRG1 + run_initFracRG2;
        }       
        
        //Calculate surfaces concerning the rest of the HRU
        double run_areaHRUminusDevice = run_hru_area - run_deviceArea; //in m²
        
        String run_dt = dt.getValue();
        double run_coef_Qinf = 0;
        if("h".equals(run_dt)){ // daily simulation time step
            run_coef_Qinf = 1000 * 3600 * 24;
        }else{ // hourly simulation time step
            run_coef_Qinf = 1000 * 3600;
        }
//        getModel().getRuntime().println("run_dt vaut : " + run_dt + ", et le coef vaut :"+ run_coef_Qinf);
        
        // write variables
        this.areaHRUminusDevice.setValue(run_areaHRUminusDevice);
        this.maxDeviceVol.setValue(run_maxVolumeDevice);
        this.Device_actRD1.setValue(run_initFracRD1 * run_initVol);
        this.Device_actRD2.setValue(run_initFracRD2 * run_initVol);
        this.Device_actRG1.setValue(run_initFracRG1 * run_initVol);
        this.Device_actRG2.setValue(run_initFracRG2 * run_initVol);
        this.deviceVol.setValue(run_initVol);
        this.coef_Qinf.setValue(run_coef_Qinf);
    }

    @Override
    public void cleanup() {
    }
}
