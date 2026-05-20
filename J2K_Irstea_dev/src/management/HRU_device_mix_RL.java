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
        author = "Jérémie Bonneau, Flora Branger, Nico Hachgenei, Nathan Pellerin",
        description = "For any type of water management device located within the HRU - small farm dams, green infrastructures for urban water management",
        date = "2022-10-27",
        version = "2.0_0")
@VersionComments(entries = {
    @VersionComments.Entry(version = "1.0_0", comment = "Initial version"),
    @VersionComments.Entry(version = "2.0_0", comment = "New version with "
            + "the following substatial changes: "
            + "1) storage of components of origin (RD1, RD2, etc.) in device, "
            + "complete mixing inside device, and proportional overflow "
            + "instead of all RD1. "
            + "2) optional limitation of inflow to filling fraction threshold, "
            + "allowing to bypass HRU if [almost] full. "
            + "3) optional, external input (e.g. treated waste water). ")
})
public class HRU_device_mix_RL extends JAMSComponent {

    /*
     *  Component attributes test something
     */
    // Input fluxes
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "RD1 from HRU",
            unit = "L"
    )
    public Attribute.Double sourceRD1;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "RD2 from HRU",
            unit = "L"
    )
    public Attribute.Double sourceRD2;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "RG1 from HRU",
            unit = "L"
    )
    public Attribute.Double sourceRG1;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "RG2 from HRU",
            unit = "L"
    )
    public Attribute.Double sourceRG2;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Additional inflow into device. e.g. treated waste "
            + "water. Will be added to addInComp.",
            unit = "L",
            defaultValue = "0.0"
    )
    public Attribute.Double addInflow;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Where to put additional inflow into device. One of "
            + "'RD1', 'RD2', 'RG1', 'RG2'. Only used if addInflow > 0.",
            unit = "L",
            defaultValue = "RD2"
    )
    public Attribute.String addInComp;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Filling threshold for device beyond which no further "
            + "water will be added to HRU. Between 0 and 1. -1 for no threshold (continue filling "
            + "and overflowing). This variable does not limit pricipitation input, "
            + "neither the additional inflow addInflow.",
            unit = "L",
            defaultValue = "-1.0"
    )
    public Attribute.Double maxFillFracThreshold;
    
    // Total precipitation (including snow)
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Precipitation",
            unit = "mm"
    )
    public Attribute.Double InPrecip;
    
    // Reference evapotranspiration // watch out this is in mm
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ, // type of access, i.e. READ, WRITE, READWRITE
            description = "RefET", // description of purpose
            unit = "mm", // unit of this var if numeric, defaults to ""
            lowerBound = 0, // lowest allowed value of var if numeric, defaults to "0"
            upperBound = 1000 // highest allowed value of var if numeric, defaults to "0"        
    )
    public Attribute.Double RefET;

//// Parameters
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ, // type of access, i.e. READ, WRITE, READWRITE
            description = "Crop coefficient for PotET calculation", // description of purpose
            unit = "-" 
    )
    public Attribute.Double DeviceCropCoeff;    // for a list of attribute types, see jams.data.Attribute

    // Percentage of RD1 to flow into device 
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ, // type of access, i.e. READ, WRITE, READWRITE
            description = "Fraction of RD1 from HRU to device", // description of purpose
            unit = "-", 
            lowerBound = 0, // lowest allowed value
            upperBound = 1 // highest allowed value
    )
    public Attribute.Double frac_RD1;    // for a list of attribute types, see jams.data.Attribute  
    
    // Percentage of RD2 to flow into device 
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ, // type of access, i.e. READ, WRITE, READWRITE
            description = "Fraction of RD2 from HRU to device", // description of purpose
            unit = "-"
    )
    public Attribute.Double frac_RD2;    // for a list of attribute types, see jams.data.Attribute 
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ, // type of access, i.e. READ, WRITE, READWRITE
            description = "Fraction of RG1 from HRU to device", // description of purpose
            unit = "-"
    )
    public Attribute.Double frac_RG1;    // for a list of attribute types, see jams.data.Attribute  
    
    // Percentage of RD2 to flow into device 
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ, // type of access, i.e. READ, WRITE, READWRITE
            description = "Fraction of RG2 from HRU to device", // description of purpose
            unit = "-" 
    )
    public Attribute.Double frac_RG2;    // for a list of attribute types, see jams.data.Attribute 
    
    // Hydraulic conductivity of underlying soil - to be read from parameter file
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Hydraulic conductivity of underlying soil",
            unit = "m/s"
    )
    public Attribute.Double DeviceKs;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Infiltration coeficient of the device regarding time step conversion",
            unit = "-"
    )
    public Attribute.Double coef_Qinf;
    
    // area of device in HRU
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Area of Device",
            unit = "m2"
    )
    public Attribute.Double DeviceArea;
    
    // time variables used for initialization
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Current time"
    )
    public Attribute.Calendar time;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "The time interval"
    )
    public Attribute.TimeInterval timeInterval;
                
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Julian day of device refill start (end of the year)"
            )
            public Attribute.Double DeviceInterceptionStart;
                
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Julian day of device refill end (beginnig of the year)"
            )
            public Attribute.Double DeviceInterceptionEnd;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Maximum capacity of the device",
            unit = "L")
    public Attribute.Double maxDeviceVol;
    
//    // Device Discharge coefficient
//    @JAMSVarDescription(
//            access = JAMSVarDescription.AccessType.READ,
//            description = "Device discharge coefficient controlling outflow",
//            unit = "-"
//    )
//    public Attribute.Double deviceDisCoeff;
    
    // State and output variables
    // volume stored in device 
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "Water volume stored in Device",
            unit = "L"
    )
    public Attribute.Double DeviceVol;
    
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

    // intercepted flow
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "Total inflow from HRU intercepted in device",
            unit = "L"
    )
    public Attribute.Double DeviceIn;

    // intercepted flow
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "Total RD1 from HRU intercepted in device",
            unit = "L"
    )
    public Attribute.Double Device_inRD1;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "Total RD2 from HRU intercepted in device",
            unit = "L"
    )
    public Attribute.Double Device_inRD2;
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "Total RG1 from HRU intercepted in device",
            unit = "L"
    )
    public Attribute.Double Device_inRG1;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "Total RG2 from HRU intercepted in device",
            unit = "L"
    )
    public Attribute.Double Device_inRG2;

    // outflow underdrain
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "Emptying outflow from device",
            unit = "L"
    )
    public Attribute.Double DeviceOut;
    
    // potential ET
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "Potential evapotranspiration from device",
            unit = "L"
    )
    public Attribute.Double DevicePET;
    
    // actual ET from device
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "Actual evapotranspiration from device",
            unit = "L"
    )
    public Attribute.Double DeviceActET;
    
    // outflow infiltration
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "Infiltration from device",
            unit = "L"
    )
    public Attribute.Double DeviceInfiltration;
    
    // rain in GI
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "Precipitation in device",
            unit = "L"
    )
    public Attribute.Double DevicePrecip;
    
    // overflow when device is full
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "Overflow from device",
            unit = "L"
    )
    public Attribute.Double DeviceOverFlow;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "RD1 overflow from device",
            unit = "L"
    )
    public Attribute.Double Device_outRD1;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "RD2 overflow from device",
            unit = "L"
    )
    public Attribute.Double Device_outRD2;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "RG1 overflow from device",
            unit = "L"
    )
    public Attribute.Double Device_outRG1;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "RG2 overflow from device",
            unit = "L"
    )
    public Attribute.Double Device_outRG2;
    

    //internal state variables
    double run_deviceArea, run_Precip, run_sourceRD1, run_sourceRD2, run_sourceRG1, run_sourceRG2, 
            run_addInflow, run_RefET, run_coef_Qinf,
            run_QRD1, run_QRD2, run_QRG1, run_QRG2, run_Qin, run_Qrain, run_PET,
            run_actET, run_Qinf, run_Qout, run_Qovf, run_actVolDevice,
            run_actRD1, run_actRD2, run_actRG1, run_actRG2,
            run_outRD1, run_outRD2, run_outRG1, run_outRG2;
    
    /*
     *  Component run stages
     */
    @Override
    public void init() {
    }

    @Override
    public void run() {
        
        run_deviceArea = DeviceArea.getValue();      

        // if no device (ie run_deviceArea == 0, do nothing and set all fluxes to zero
        if (run_deviceArea == 0) {
            run_actVolDevice = 0;
        } //if not, calculate fluxes
        else if (run_deviceArea > 0) {

        double fracRD1 = frac_RD1.getValue(); //fraction of RD1 in device
        double fracRD2 = frac_RD2.getValue(); //fraction of RD2 in device
        double fracRG1 = frac_RG1.getValue(); //fraction of RG1 in device
        double fracRG2 = frac_RG2.getValue(); //fraction of RG2 in device

        double Ks = DeviceKs.getValue();
        double CropCoeff = DeviceCropCoeff.getValue();
        
        double MaxVolumeDevice = maxDeviceVol.getValue();
        double run_maxFillFracThreshold = maxFillFracThreshold.getValue();
        
        // In flows            
        // interception of precipitation 
        run_Precip = InPrecip.getValue();// in mm!!
        run_sourceRD1 = sourceRD1.getValue();
        run_sourceRD2 = sourceRD2.getValue();
        run_sourceRG1 = sourceRG1.getValue();
        run_sourceRG2 = sourceRG2.getValue();
        run_addInflow = addInflow.getValue();
        run_RefET = RefET.getValue(); // in mm!!
        run_actVolDevice = DeviceVol.getValue();
        run_actRD1 = Device_actRD1.getValue();
        run_actRD2 = Device_actRD2.getValue();
        run_actRG1 = Device_actRG1.getValue();
        run_actRG2 = Device_actRG2.getValue();
//        getModel().getRuntime().println("FD volume : " + run_actVolDevice);


        run_QRD1 = 0;
        run_QRD2 = 0;
        run_QRG1 = 0;
        run_QRG2 = 0;
        run_Qin = 0;
        run_Qrain = 0;
        run_PET = 0;
        run_actET = 0;
        run_Qout = 0;
        run_Qovf = 0;
        run_outRD1 = 0;
        run_outRD2 = 0;
        run_outRG1 = 0;
        run_outRG2 = 0;
        run_Qinf = 0; //infiltration flux set to zero

            // In flows            
            // interception of precipitation            
            run_Qrain = run_Precip * run_deviceArea; // rain in mm, area GI in m2 so Qrain in L
            
            //Check if we are in a filling period or not.
            int jDay = time.get(Calendar.DAY_OF_YEAR);
            if (jDay >= DeviceInterceptionStart.getValue() || jDay <= DeviceInterceptionEnd.getValue()) {
                // interception of flows coming from the HRU
                run_QRD1 = fracRD1 * run_sourceRD1;
                run_QRD2 = fracRD2 * run_sourceRD2;
                run_QRG1 = fracRG1 * run_sourceRG1;
                run_QRG2 = fracRG2 * run_sourceRG2;
            } else { //In case we are not, the device will not extract water from its environment.
                // No interception of flows coming from the HRU
                run_QRD1 = 0;
                run_QRD2 = 0;
                run_QRG1 = 0;
                run_QRG2 = 0;
            }
            
            // Check if there is a limitation to only partially fill device
            if (run_maxFillFracThreshold >= 0) {
                double run_maxInputVol = (run_maxFillFracThreshold * MaxVolumeDevice) - run_actVolDevice;
                double run_totalInGoal = run_QRD1 + run_QRD2 + run_QRG1 + run_QRG2;
//                getModel().getRuntime().println("run_totalInGoal : " + run_totalInGoal);
                if (run_totalInGoal > run_maxInputVol) { // if trying to intercept more water than allowed
                    // limit to allowed volume
                    double run_inflowFrac = run_maxInputVol / run_totalInGoal;
                    run_QRD1 *= run_inflowFrac;
                    run_QRD2 *= run_inflowFrac;
                    run_QRD1 *= run_inflowFrac;
                    run_QRD1 *= run_inflowFrac;
                } // otherwise keep as is (no need for else statement)
            }
            
            
            if (run_addInflow > 0) { // add additional inflow
                switch (addInComp.getValue()) {
                    case "RD1":
                        run_QRD1 += run_addInflow;
                        break;
                    case "RD2":
                        run_QRD2 += run_addInflow;
                        break;
                    case "RG1":
                        run_QRG1 += run_addInflow;
                        break;
                    case "RG2":
                        run_QRG2 += run_addInflow;
                        break;
                    default:
                        getModel().getRuntime().println("WARNING: unknown component "
                        + addInComp.getValue() + ". Unable to add additional "
                        + "inflow.");
                }
            }

            run_Qin = run_QRD1 + run_QRD2
                    + run_QRG1 + run_QRG2; //FlowIn in L, Qin in L
            
            // add all inflows to storage (add rain to RD1)
            run_actRD1 += run_QRD1 + run_Qrain;
            run_actRD2 += run_QRD2;
            run_actRG1 += run_QRG1;
            run_actRG2 += run_QRG2;

            // update the total water volume in device
//            run_actVolDevice = run_actVolDevice + run_Qrain + run_Qin;
            run_actVolDevice = run_actRD1 + run_actRD2 + run_actRG1 + run_actRG2;

            // check that there is not too much water - if so, overflow occurs and will be connected to the HRU RD1
            if (run_actVolDevice > MaxVolumeDevice) {
                double run_maxFrac = MaxVolumeDevice / run_actVolDevice;
                double run_outFrac = 1 - run_maxFrac;
                run_outRD1 = run_outFrac * run_actRD1;
                run_outRD2 = run_outFrac * run_actRD2;
                run_outRG1 = run_outFrac * run_actRG1;
                run_outRG2 = run_outFrac * run_actRG2;
                
                run_actRD1 -= run_outRD1;
                run_actRD2 -= run_outRD2;
                run_actRG1 -= run_outRG1;
                run_actRG2 -= run_outRG2;

                run_Qovf = run_actVolDevice - MaxVolumeDevice;
                run_actVolDevice = MaxVolumeDevice;
            }

            // Outflows
            if (run_actVolDevice > 0) {
                // Infiltration
                run_Qinf = Math.min(run_deviceArea * Ks * run_coef_Qinf, run_actVolDevice); //area in m2, Ks m/s, Qinf in L
               
                // Evaporation or Evapotranspiration
                // calculate Potential evapotranspiration and convert in L
                run_PET = run_RefET * CropCoeff * run_deviceArea; // RefET in mm, area in m2 so PET in L
                // calculate Actual Evapotranspiration (ActET) -simple
                run_actET = Math.min(run_PET, run_actVolDevice);
                
                // what if there is not enough water for ET and infiltration? split as a proportion
                if (run_actET + run_Qinf > run_actVolDevice) {
                    double share_Qet = run_actET / (run_actET + run_Qinf + 0.00001);
                    double share_Qinf = run_Qinf / (run_actET + run_Qinf + 0.00001);
                    run_actET = run_actVolDevice * share_Qet;
                    run_Qinf = run_actVolDevice * share_Qinf;
                }
            
                //Qout - underdrain flow - for the moment lets forget about that
                run_Qout = 0; 
                //Math.min(Cout * Math.sqrt(2 * g * volumeInGI/volumeGI) , volumeInGI) ; // in L 
                //volumeInGI = Math.max(volumeInGI - Qout,0);

                // update volume
                double run_remainingFracETInf = 1 - Math.min((run_Qinf + run_actET) / run_actVolDevice, 1);
                run_actRD1 *= run_remainingFracETInf;
                run_actRD2 *= run_remainingFracETInf;
                run_actRG1 *= run_remainingFracETInf;
                run_actRG2 *= run_remainingFracETInf;
                run_actVolDevice *= run_remainingFracETInf;
                //run_actVolDevice = Math.max(run_actVolDevice - run_Qinf - run_ActET, 0);
                
            } else if (run_actVolDevice == 0) {
                run_Qinf = 0;
                run_actET = 0;
            }
            
        }else{
            getModel().getRuntime().println("Device area < 0 ! Not possible");
        }

        // write values  
        DevicePET.setValue(run_PET);
        DeviceActET.setValue(run_actET);
        DeviceOut.setValue(run_Qout);
        DeviceInfiltration.setValue(run_Qinf);
        DeviceVol.setValue(run_actVolDevice);
        Device_actRD1.setValue(run_actRD1);
        Device_actRD2.setValue(run_actRD2);
        Device_actRG1.setValue(run_actRG1);
        Device_actRG2.setValue(run_actRG2);
        Device_inRD1.setValue(run_QRD1);
        Device_inRD2.setValue(run_QRD2);
        Device_inRG1.setValue(run_QRG1);
        Device_inRG2.setValue(run_QRG2);
        DeviceIn.setValue(run_Qin);
        DevicePrecip.setValue(run_Qrain);
        DeviceOverFlow.setValue(run_Qovf);

    }

    @Override
    public void cleanup() {
    }
}
