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


/**
 *
 * @author Jeremie Bonneau <jeremie.bonneau at inrae.fr>
 */
@JAMSComponentDescription(
        title = "HRU device",
        author = "Jérémie Bonneau, Flora Branger, Nathan Pellerin",
        description = "For any type of water management device located within the HRU "
                + "- small farm dams, green infrastructures for urban water management, "
                + "works with daily time step",
        date = "2022-10-27",
        version = "2.0_0")
@VersionComments(entries = {
    @VersionComments.Entry(version = "1.0_0", comment = "Initial version"),
    @VersionComments.Entry(version = "2.0_0", comment = "Daily time step simulation"
            + "+ Simplification in the code for the initialization")
})
public class HRU_device_daily extends JAMSComponent {

    /*
     *  Component attributes test something
     */
    // time variables used for initialization
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Current time")
    public Attribute.Calendar time;
    
    // Input fluxes
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "RD1 from HRU",
            unit = "L")
    public Attribute.Double inRD1;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "RD2 from HRU",
            unit = "L")
    public Attribute.Double inRD2;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "RG1 from HRU",
            unit = "L")
    public Attribute.Double inRG1;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "RG2 from HRU",
            unit = "L")
    public Attribute.Double inRG2;
    
    // Total precipitation (including snow)
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Precipitation",
            unit = "mm")
    public Attribute.Double inPrecip;
    
    // Reference evapotranspiration // watch out this is in mm
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ, 
            description = "RefET", 
            unit = "mm", 
            lowerBound = 0, 
            upperBound = 1000)
    public Attribute.Double refET;

//// Parameters
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ, 
            description = "Crop coefficient for PotET calculation",
            unit = "-")
    public Attribute.Double deviceCropCoeff;    

    // Percentage of RD1 to flow into device 
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ, 
            description = "% of RD1 from HRU to device",
            unit = "-")
    public Attribute.Double frac_RD1;     
    
    // Percentage of RD2 to flow into device 
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ, 
            description = "% of RD2 from HRU to device",
            unit = "-")
    public Attribute.Double frac_RD2;    
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ, 
            description = "% of RG1 from HRU to device",
            unit = "-")
    public Attribute.Double frac_RG1;    
    
    // Percentage of RD2 to flow into device 
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ, 
            description = "% of RG2 from HRU to device",
            unit = "-")
    public Attribute.Double frac_RG2;    
    
    // Hydraulic conductivity of underlying soil - to be read from parameter file
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Hydraulic conductivity of underlying soil",
            unit = "m/s")
    public Attribute.Double deviceKs;
    
    // area of device in HRU
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Area of Device",
            unit = "m2")
    public Attribute.Double deviceArea;
    
    // State and output variables
    // volume stored in device 
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "Water volume stored in Device",
            unit = "L")
    public Attribute.Double deviceVol;
    
    // intercepted flow
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "Total inflow from HRU intercepted in device",
            unit = "L")
    public Attribute.Double deviceIn;

    // intercepted flow
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "Total RD1 from HRU intercepted in device",
            unit = "L")
    public Attribute.Double deviceRD1;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "Total RD2 from HRU intercepted in device",
            unit = "L")
    public Attribute.Double deviceRD2;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "Total RG1 from HRU intercepted in device",
            unit = "L")
    public Attribute.Double deviceRG1;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "Total RG2 from HRU intercepted in device",
            unit = "L")
    public Attribute.Double deviceRG2;
    
    // potential ET
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "Potential evapotranspiration from device",
            unit = "L")
    public Attribute.Double devicePET;
    
    // actual ET from device
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "Actual evapotranspiration from device",
            unit = "L")
    public Attribute.Double deviceActET;
    
    // device infiltration
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "Infiltration from device",
            unit = "L")
    public Attribute.Double deviceInfiltration;
    
    // rain in device
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "Precipitation in device",
            unit = "L")
    public Attribute.Double devicePrecip;
    
    // overflow when device is full
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "Overflow from device",
            unit = "L")
    public Attribute.Double deviceOverFlow;
                
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Julian day of device refill start")
    public Attribute.Double deviceInterceptionStart;
                
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Julian day of device refill end")
    public Attribute.Double deviceInterceptionEnd;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "Maximum capacity of the device",
            unit = "L")
    public Attribute.Double maxDeviceVol;
    
    //internal state variables
    double run_deviceArea ,run_QRD1, run_QRD2, run_QRG1, run_QRG2, run_Qin, 
            run_Qrain, run_PET, run_actET, run_Qinf, run_Qovf, run_actDeviceVol,
            run_fracRD1, run_fracRD2, run_fracRG1, run_fracRG2, run_Ks,
            run_cropCoeff, run_maxVolumeDevice, run_precip, run_refET, run_inRD1,
            run_inRD2, run_inRG1, run_inRG2;

    /*
     *  Component run stages
     */
    @Override
    public void init() {
    }

    @Override
    public void run() {
        
        run_deviceArea = deviceArea.getValue();

        // if no device (ie run_deviceArea == 0, do nothing and set all fluxes to zero
        if (run_deviceArea == 0) {
            run_PET = 0;
            run_actET = 0;
            run_Qin = 0;
            run_Qinf = 0;
            run_actDeviceVol = 0;
            run_QRD1 = 0;
            run_QRD2 = 0;
            run_QRG1 = 0;
            run_QRG2 = 0;
            run_Qrain = 0;
            run_Qovf = 0;
            
        } //if not, calculate fluxes
        else if (run_deviceArea > 0) {
            run_precip = inPrecip.getValue();// in mm!!
            run_refET = refET.getValue(); // in mm!!
            // Inflows produced by the HRU
            run_inRD1 = inRD1.getValue();
            run_inRD2 = inRD2.getValue();
            run_inRG1 = inRG1.getValue();
            run_inRG2 = inRG2.getValue();
            // Fraction of inflows intercepted
            run_fracRD1 = frac_RD1.getValue(); //fraction of RD1 in device
            run_fracRD2 = frac_RD2.getValue(); //fraction of RD2 in device
            run_fracRG1 = frac_RG1.getValue(); //fraction of RG1 in device
            run_fracRG2 = frac_RG2.getValue(); //fraction of RG2 in device
            // Device parameters
            run_Ks = deviceKs.getValue();
            run_cropCoeff = deviceCropCoeff.getValue();
            run_maxVolumeDevice = maxDeviceVol.getValue();
            run_actDeviceVol = deviceVol.getValue();

            // In flows            
            // interception of precipitation            
            run_Qrain = run_precip * run_deviceArea; // rain in mm, run_deviceArea GI in m2 so Qrain in L
            
            //Check if we are in a filling period or not.
            int jDay = time.get(Calendar.DAY_OF_YEAR);
            if (jDay >= deviceInterceptionStart.getValue() || jDay <= deviceInterceptionEnd.getValue()) {
                // interception of flows coming from the HRU
                run_QRD1 = run_fracRD1 * run_inRD1;
                run_QRD2 = run_fracRD2 * run_inRD2;
                run_QRG1 = run_fracRG1 * run_inRG1;
                run_QRG2 = run_fracRG2 * run_inRG2;
            } else { //In case we are not, the the device will not extract water from its environment.
                // No interception of flows coming from the HRU
                run_QRD1 = 0;
                run_QRD2 = 0;
                run_QRG1 = 0;
                run_QRG2 = 0;
            }
            
            run_Qin = run_QRD1 + run_QRD2 + run_QRG1 + run_QRG2; // FlowIn in L, Qin in L

            // update the total water volume in device
            run_actDeviceVol = run_actDeviceVol + run_Qrain + run_Qin;

            // check that there is not too much water - if so, overflow occurs and will be connected to the HRU RD1
            if (run_actDeviceVol > run_maxVolumeDevice) {
                run_Qovf = run_actDeviceVol - run_maxVolumeDevice;
                run_actDeviceVol = run_maxVolumeDevice;
            } else {
                run_Qovf = 0;
            }


            // Outflows
            // Evaporation + Infiltration
            // calculate Potential evapotranspiration and convert in L
            run_PET = run_refET * run_cropCoeff * run_deviceArea; // RefET in mm, run_deviceArea in m2 so PET in L
            // calculate Actual Evapotranspiration (ActET) - simple
            run_actET = Math.min(run_PET, run_actDeviceVol);
            // Calculate infiltration
            run_Qinf = Math.min(run_deviceArea * run_Ks * 1000 * 3600 * 24, run_actDeviceVol); //run_deviceArea in m2, Ks m/s, Qinf in L during the time step (hour) 
            
            // what if there is not enough water for ET and infiltration? split as a proportion
            if (run_actET + run_Qinf > run_actDeviceVol) {
                double share_Qet = run_actET / (run_actET + run_Qinf + 0.00001);
                double share_Qinf = run_Qinf / (run_actET + run_Qinf + 0.00001);

                run_actET = run_actDeviceVol * share_Qet;
                run_Qinf = run_actDeviceVol * share_Qinf;
            }
            
            // update volume
            run_actDeviceVol = Math.max(run_actDeviceVol - run_Qinf - run_actET, 0);

        } else {
            getModel().getRuntime().println("Device area < 0 ! Not possible");
        }

        // write values  
        deviceVol.setValue(run_actDeviceVol);
        deviceRD1.setValue(run_QRD1);
        deviceRD2.setValue(run_QRD2);
        deviceRG1.setValue(run_QRG1);
        deviceRG2.setValue(run_QRG2);
        deviceIn.setValue(run_Qin);
        devicePrecip.setValue(run_Qrain);
        devicePET.setValue(run_PET);
        deviceActET.setValue(run_actET);
        deviceInfiltration.setValue(run_Qinf);
        deviceOverFlow.setValue(run_Qovf);

    }

    @Override
    public void cleanup() {
    }
}
