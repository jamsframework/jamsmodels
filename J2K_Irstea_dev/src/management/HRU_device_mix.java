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
        author = "Jérémie Bonneau, Flora Branger, Nico Hachgenei",
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
public class HRU_device_mix extends JAMSComponent {

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
            description = "Crop coefficient for PotET calculation" // description of purpose
    )
    public Attribute.Double deviceCropCoeff;    // for a list of attribute types, see jams.data.Attribute

    // Percentage of RD1 to flow into device 
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ, // type of access, i.e. READ, WRITE, READWRITE
            description = "Fraction of RD1 from HRU to device", // description of purpose
            lowerBound = 0, // lowest allowed value
            upperBound = 1 // highest allowed value
    )
    public Attribute.Double frac_RD1;    // for a list of attribute types, see jams.data.Attribute  
    
    // Percentage of RD2 to flow into device 
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ, // type of access, i.e. READ, WRITE, READWRITE
            description = "Fraction of RD2 from HRU to device" // description of purpose
    )
    public Attribute.Double frac_RD2;    // for a list of attribute types, see jams.data.Attribute 
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ, // type of access, i.e. READ, WRITE, READWRITE
            description = "Fraction of RG1 from HRU to device" // description of purpose
    )
    public Attribute.Double frac_RG1;    // for a list of attribute types, see jams.data.Attribute  
    
    // Percentage of RD2 to flow into device 
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ, // type of access, i.e. READ, WRITE, READWRITE
            description = "Fraction of RG2 from HRU to device" // description of purpose
    )
    public Attribute.Double frac_RG2;    // for a list of attribute types, see jams.data.Attribute 
    
    // Depth of the device -  to be read from parameter file
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
            description = "Device depth",
            unit = "m")
    public Attribute.Double deviceDepth;
    
    // Device Porosity -  to be read from parameter file
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
            description = "Device porosity",
            unit = "-")
    public Attribute.Double devicePorosity;
    
    // Hydraulic conductivity of underlying soil - to be read from parameter file
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Hydraulic conductivity of underlying soil",
            unit = "m/s"
    )
    public Attribute.Double deviceKs;
    
    // Initial volume stored in device 
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Initial water volume stored in Device as fraction of max volume",
            unit = "-"
    )
    public Attribute.Double DeviceInitVol;
    
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
    
    // area of device in HRU
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Area of Device",
            unit = "m2"
    )
    public Attribute.Double deviceArea;
    
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
    
    // actual ET
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
            description = "Julian day of device refill start"
            )
            public Attribute.Double deviceInterceptionStart;
                
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Julian day of device refill end"
            )
            public Attribute.Double deviceInterceptionEnd;
    
//    //timestep
//    @JAMSVarDescription(
//            access = JAMSVarDescription.AccessType.READ,
//            description = "time interval",
//            unit = "d")
//    public Attribute.TimeInterval dt;

//current timestep
//    @JAMSVarDescription(
//            access = JAMSVarDescription.AccessType.READ,
//            description = "Current time step",
//            unit = "d")
//    public Attribute.Calendar time;

    //internal state variables
    double run_Precip, run_sourceRD1, run_sourceRD2, run_sourceRG1, run_sourceRG2, 
            run_addInflow, run_RefET,
            run_QRD1, run_QRD2, run_QRG1, run_QRG2, run_Qin, run_Qrain, run_PET,
            run_ActET, run_Qinf, run_Qout, run_Qovf, run_actVolDevice,
            run_actRD1, run_actRD2, run_actRG1, run_actRG2,
            run_outRD1, run_outRD2, run_outRG1, run_outRG2;
    //private int seconds;

    /*
     *  Component run stages
     */
    @Override
    public void init() {
//        if (dt.getTimeUnit() == GregorianCalendar.MINUTE) {
//            seconds = 60 * dt.getTimeUnitCount();
//        } else if (dt.getTimeUnit() == GregorianCalendar.HOUR) {
//            seconds = 3600 * dt.getTimeUnitCount();
//        } else if (dt.getTimeUnit() == GregorianCalendar.DAY_OF_YEAR) {
//            seconds = 24 * 3600 * dt.getTimeUnitCount();
//        } else if (dt.getTimeUnit() == GregorianCalendar.MONTH) {
//            seconds = time.getActualMaximum(GregorianCalendar.DAY_OF_MONTH) * 24 * 3600 * dt.getTimeUnitCount();
//        }
    }

    @Override
    public void run() {
        


        // constant parameters to read	
        double g = 9.80665;
        //double Cout = deviceDisCoeff.getValue();
        double H = deviceDepth.getValue();
        double fracRD1 = frac_RD1.getValue(); //fraction of RD1 in device
        double fracRD2 = frac_RD2.getValue(); //fraction of RD2 in device
        double fracRG1 = frac_RG1.getValue(); //fraction of RG1 in device
        double fracRG2 = frac_RG2.getValue(); //fraction of RG2 in device

        double p = devicePorosity.getValue();
        double area = deviceArea.getValue();
        double Ks = deviceKs.getValue();
        double CropCoeff = deviceCropCoeff.getValue();
        
        //Calculate max volume of device in L; area and H in m -> m3, convert to L
        double MaxVolumeDevice = p * H * area * 1000;
        double run_maxFillFracThreshold = maxFillFracThreshold.getValue();
        
        //Initialize device volume
        String Date = time.toString();
        String Start = timeInterval.getStart().toString();
        if ((Date.equals(Start))) {
            double run_initVol = this.DeviceInitVol.getValue()*MaxVolumeDevice;
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
//                run_fracSum = run_initFracRD1 + run_initFracRD2 + run_initFracRG1 + run_initFracRG2;
            } else if (run_fracSum != 1.0) { // otherwise, adapt proportionally
                getModel().getRuntime().println("WARNING: sum of device init fractions "
                + " is "+ run_fracSum + ", will be corrected to 1.");
                run_initFracRD1 /= run_fracSum;
                run_initFracRD2 /= run_fracSum;
                run_initFracRG1 /= run_fracSum;
                run_initFracRG2 /= run_fracSum;
//                run_fracSum = run_initFracRD1 + run_initFracRD2 + run_initFracRG1 + run_initFracRG2;
            }
            
            this.Device_actRD1.setValue(run_initFracRD1 * run_initVol);
            this.Device_actRD2.setValue(run_initFracRD2 * run_initVol);
            this.Device_actRG1.setValue(run_initFracRG1 * run_initVol);
            this.Device_actRG2.setValue(run_initFracRG2 * run_initVol);
            this.DeviceVol.setValue(run_initVol);
        }

        this.run_Precip = InPrecip.getValue();// in mm!!
        this.run_sourceRD1 = sourceRD1.getValue();
        this.run_sourceRD2 = sourceRD2.getValue();
        this.run_sourceRG1 = sourceRG1.getValue();
        this.run_sourceRG2 = sourceRG2.getValue();
        this.run_addInflow = addInflow.getValue();
        this.run_RefET = RefET.getValue(); // in mm!!
        this.run_actVolDevice = DeviceVol.getValue();
        this.run_actRD1 = Device_actRD1.getValue();
        this.run_actRD2 = Device_actRD2.getValue();
        this.run_actRG1 = Device_actRG1.getValue();
        this.run_actRG2 = Device_actRG2.getValue();
        

        this.run_QRD1 = 0;
        this.run_QRD2 = 0;
        this.run_QRG1 = 0;
        this.run_QRG2 = 0;
        this.run_Qin = 0;
        this.run_Qrain = 0;
        this.run_PET = 0;
        this.run_ActET = 0;
        this.run_Qout = 0;
        this.run_Qovf = 0;
        this.run_outRD1 = 0;
        this.run_outRD2 = 0;
        this.run_outRG1 = 0;
        this.run_outRG2 = 0;
        this.run_Qinf = 0; //infiltration flux set to zero

        

        // if no device (ie area == 0), do nothing and set all fluxes to zero
        if (area == 0) {
//            this.run_QRD1 = 0;
//            this.run_QRD2 = 0;
//            this.run_QRG1 = 0;
//            this.run_QRG2 = 0;
//            this.run_Qin = 0;
//            this.run_Qrain = 0;
//            this.run_ActET = 0;
//            this.run_Qinf = 0;
//            this.run_Qout = 0;
//            this.run_Qovf = 0;
            this.run_actVolDevice = 0;
        } //if not, calculate fluxes
        else if (area > 0) {

            // In flows            
            // interception of precipitation            
            this.run_Qrain = this.run_Precip * area; // rain in mm, area GI in m2 so Qrain in L
            
            //Check if we are in a filling period or not.
            int jDay = time.get(Calendar.DAY_OF_YEAR);
            if (jDay >= deviceInterceptionStart.getValue() || jDay <= deviceInterceptionEnd.getValue()) {
                // interception of flows coming from the HRU
                this.run_QRD1 = fracRD1 * this.run_sourceRD1;
                this.run_QRD2 = fracRD2 * this.run_sourceRD2;
                this.run_QRG1 = fracRG1 * this.run_sourceRG1;
                this.run_QRG2 = fracRG2 * this.run_sourceRG2;
            } else { //In case we are not, the the device will not extract water from its environment.
                // No interception of flows coming from the HRU
                this.run_QRD1 = 0;
                this.run_QRD2 = 0;
                this.run_QRG1 = 0;
                this.run_QRG2 = 0;
            }
            
            // Check if there is a limitation to only partially fill device
            if (run_maxFillFracThreshold >= 0) {
                double run_maxInputVol = (run_maxFillFracThreshold * MaxVolumeDevice) - this.run_actVolDevice;
                double run_totalInGoal = this.run_QRD1 + this.run_QRD2 + this.run_QRG1 + this.run_QRG2;
                if (run_totalInGoal > run_maxInputVol) { // if trying to intercept more water than allowed
                    // limit to allowed volume
                    double run_inflowFrac = run_maxInputVol / run_totalInGoal;
                    this.run_QRD1 *= run_inflowFrac;
                    this.run_QRD2 *= run_inflowFrac;
                    this.run_QRD1 *= run_inflowFrac;
                    this.run_QRD1 *= run_inflowFrac;
                } // otherwise keep as is (no need for else statement)
            }
            
            
            if (run_addInflow > 0) { // add additional inflow
                switch (addInComp.getValue()) {
                    case "RD1":
                        this.run_QRD1 += run_addInflow;
                        break;
                    case "RD2":
                        this.run_QRD2 += run_addInflow;
                        break;
                    case "RG1":
                        this.run_QRG1 += run_addInflow;
                        break;
                    case "RG2":
                        this.run_QRG2 += run_addInflow;
                        break;
                    default:
                        getModel().getRuntime().println("WARNING: unknown component "
                        + addInComp.getValue() + ". Unable to add additional "
                        + "inflow.");
                }
            }

            this.run_Qin = this.run_QRD1 + this.run_QRD2
                    + this.run_QRG1 + this.run_QRG2; //FlowIn in L, Qin in L
            
            // add all inflows to storage (add rain to RD1)
            this.run_actRD1 += this.run_QRD1 + this.run_Qrain;
            this.run_actRD2 += this.run_QRD2;
            this.run_actRG1 += this.run_QRG1;
            this.run_actRG2 += this.run_QRG2;

            // update the total water volume in device
//            this.run_actVolDevice = this.run_actVolDevice + this.run_Qrain + this.run_Qin;
            this.run_actVolDevice = this.run_actRD1 + this.run_actRD2 + this.run_actRG1 + this.run_actRG2;

            // check that there is not too much water - if so, overflow occurs and will be connected to the HRU RD1
            if (this.run_actVolDevice > MaxVolumeDevice) {
                double run_maxFrac = MaxVolumeDevice / this.run_actVolDevice;
                double run_outFrac = 1 - run_maxFrac;
                this.run_outRD1 = run_outFrac * this.run_actRD1;
                this.run_outRD2 = run_outFrac * this.run_actRD2;
                this.run_outRG1 = run_outFrac * this.run_actRG1;
                this.run_outRG2 = run_outFrac * this.run_actRG2;
                
                this.run_actRD1 -= this.run_outRD1;
                this.run_actRD2 -= this.run_outRD2;
                this.run_actRG1 -= this.run_outRG1;
                this.run_actRG2 -= this.run_outRG2;

                this.run_Qovf = this.run_actVolDevice - MaxVolumeDevice;
                this.run_actVolDevice = MaxVolumeDevice;
            }



            // Outflows
            // Evaporation or Evapotranspiration
            // calculate Potential evapotranspiration and convert in L
            this.run_PET = this.run_RefET * CropCoeff * area; // RefET in mm, area in m2 so PET in L
            // calculate Actual Evapotranspiration (ActET) -simple
            this.run_ActET = Math.min(this.run_PET, this.run_actVolDevice);

            // Infiltration 
                if (this.run_actVolDevice > 0) {
                    this.run_Qinf = Math.min(area * Ks * 1000 * 3600, this.run_actVolDevice); //area in m2, Ks m/s, Qinf in L
                } else if (this.run_actVolDevice == 0) {
                    this.run_Qinf = 0;
                }//

            // what if there is not enough water for ET and infiltration? split as a proportion
            if (this.run_ActET + this.run_Qinf > this.run_actVolDevice) {
                double share_Qet = this.run_ActET / (this.run_ActET + this.run_Qinf + 0.00001);
                double share_Qinf = this.run_Qinf / (this.run_ActET + this.run_Qinf + 0.00001);

                this.run_ActET = this.run_actVolDevice * share_Qet;
                this.run_Qinf = this.run_actVolDevice * share_Qinf;
            }
            
            //Qout - underdrain flow - for the moment lets forget about that
            this.run_Qout = 0; //Math.min(Cout * Math.sqrt(2 * g * volumeInGI/volumeGI) , volumeInGI) ; // in L 
            //volumeInGI = Math.max(volumeInGI - Qout,0);
            
            // update volume
            double run_remainingFracETInf = 1 - Math.min((this.run_Qinf - this.run_ActET) / this.run_actVolDevice, 1);
            this.run_actRD1 *= run_remainingFracETInf;
            this.run_actRD2 *= run_remainingFracETInf;
            this.run_actRG1 *= run_remainingFracETInf;
            this.run_actRG2 *= run_remainingFracETInf;
            this.run_actVolDevice *= run_remainingFracETInf;
//            this.run_actVolDevice = Math.max(this.run_actVolDevice - this.run_Qinf - this.run_ActET, 0);
        }else{
            getModel().getRuntime().println("Device area < 0 ! Not possible");
        }

        // write values  
        DevicePET.setValue(this.run_PET);
        DeviceActET.setValue(this.run_ActET);
        DeviceOut.setValue(this.run_Qout);
        DeviceInfiltration.setValue(this.run_Qinf);
        DeviceVol.setValue(this.run_actVolDevice);
        Device_actRD1.setValue(this.run_actRD1);
        Device_actRD2.setValue(this.run_actRD2);
        Device_actRG1.setValue(this.run_actRG1);
        Device_actRG2.setValue(this.run_actRG2);
        Device_inRD1.setValue(this.run_QRD1);
        Device_inRD2.setValue(this.run_QRD2);
        Device_inRG1.setValue(this.run_QRG1);
        Device_inRG2.setValue(this.run_QRG2);
        DeviceIn.setValue(this.run_Qin);
        DevicePrecip.setValue(this.run_Qrain);
        DeviceOverFlow.setValue(this.run_Qovf);

    }

    @Override
    public void cleanup() {
    }
}
