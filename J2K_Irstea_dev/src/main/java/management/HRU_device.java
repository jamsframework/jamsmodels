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
        author = "Jérémie Bonneau, Flora Branger",
        description = "For any type of water management device located within the HRU - small farm dams, green infrastructures for urban water management",
        date = "2022-10-27",
        version = "1.0_0")
public class HRU_device extends JAMSComponent {

    /*
     *  Component attributes test something
     */
    // Input fluxes
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "RD1 from HRU",
            unit = "L"
    )
    public Attribute.Double InRD1;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "RD2 from HRU",
            unit = "L"
    )
    public Attribute.Double InRD2;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "RG1 from HRU",
            unit = "L"
    )
    public Attribute.Double InRG1;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "RG2 from HRU",
            unit = "L"
    )
    public Attribute.Double InRG2;
    
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
            description = "% of RD1 from HRU to device" // description of purpose
    )
    public Attribute.Double frac_RD1;    // for a list of attribute types, see jams.data.Attribute  
    
    // Percentage of RD2 to flow into device 
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ, // type of access, i.e. READ, WRITE, READWRITE
            description = "% of RD2 from HRU to device" // description of purpose
    )
    public Attribute.Double frac_RD2;    // for a list of attribute types, see jams.data.Attribute 
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ, // type of access, i.e. READ, WRITE, READWRITE
            description = "% of RG1 from HRU to device" // description of purpose
    )
    public Attribute.Double frac_RG1;    // for a list of attribute types, see jams.data.Attribute  
    
    // Percentage of RD2 to flow into device 
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ, // type of access, i.e. READ, WRITE, READWRITE
            description = "% of RG2 from HRU to device" // description of purpose
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
    public Attribute.Double DeviceRD1;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "Total RD2 from HRU intercepted in device",
            unit = "L"
    )
    public Attribute.Double DeviceRD2;
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "Total RG1 from HRU intercepted in device",
            unit = "L"
    )
    public Attribute.Double DeviceRG1;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "Total RG2 from HRU intercepted in device",
            unit = "L"
    )
    public Attribute.Double DeviceRG2;

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
    
    // overflow when GI is full
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "Overflow from device",
            unit = "L"
    )
    public Attribute.Double DeviceOverFlow;
    
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
    double run_Precip, run_InRD1, run_InRD2, run_InRG1, run_InRG2, run_RefET,
            run_QRD1, run_QRD2, run_QRG1, run_QRG2, run_Qin, run_Qrain, run_PET,
            run_ActET, run_Qinf, run_Qout, run_Qovf, run_actvolumeInDevice;
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
        
        //Initialize device volume
        String Date = time.toString();
        String Start = timeInterval.getStart().toString();
        if ((Date.equals(Start))) {
            this.DeviceVol.setValue(this.DeviceInitVol.getValue()*MaxVolumeDevice);
        }

        this.run_Precip = InPrecip.getValue();// in mm!!
        this.run_InRD1 = InRD1.getValue();
        this.run_InRD2 = InRD2.getValue();
        this.run_InRG1 = InRG1.getValue();
        this.run_InRG2 = InRG2.getValue();
        this.run_RefET = RefET.getValue(); // in mm!!
        this.run_actvolumeInDevice = DeviceVol.getValue();

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
        this.run_Qinf = 0; //infiltration flux set to zero

        

        // if no device (ie area == 0, do nothing and set all fluxes to zero
        if (area == 0) {
            this.run_QRD1 = 0;
            this.run_QRD2 = 0;
            this.run_QRG1 = 0;
            this.run_QRG2 = 0;
            this.run_Qin = 0;
            this.run_Qrain = 0;
            this.run_ActET = 0;
            this.run_Qinf = 0;
            this.run_Qout = 0;
            this.run_Qovf = 0;
            this.run_actvolumeInDevice = 0;
        } //if not, calculate fluxes
        else if (area > 0) {

            // In flows            
            // interception of precipitation            
            this.run_Qrain = this.run_Precip * area; // rain in mm, area GI in m2 so Qrain in L
            
            //Check if we are in a filling period or not.
            int jDay = time.get(Calendar.DAY_OF_YEAR);
            if (jDay >= deviceInterceptionStart.getValue() || jDay <= deviceInterceptionEnd.getValue()) {
                // interception of flows coming from the HRU
                this.run_QRD1 = fracRD1 * this.run_InRD1;
                this.run_QRD2 = fracRD2 * this.run_InRD2;
                this.run_QRG1 = fracRG1 * this.run_InRG1;
                this.run_QRG2 = fracRG2 * this.run_InRG2;
            } else { //In case we are not, the the device will not extract water from its environment.
                // No interception of flows coming from the HRU
                this.run_QRD1 = 0;
                this.run_QRD2 = 0;
                this.run_QRG1 = 0;
                this.run_QRG2 = 0;
            }
            

            this.run_Qin = this.run_QRD1 + this.run_QRD2
                    + this.run_QRG1 + this.run_QRG2; //FlowIn in L, Qin in L

            // update the total water volume in device
            this.run_actvolumeInDevice = this.run_actvolumeInDevice + this.run_Qrain + this.run_Qin;

            // check that there is not too much water - if so, overflow occurs and will be connected to the HRU RD1
            if (this.run_actvolumeInDevice > MaxVolumeDevice) {
                this.run_Qovf = this.run_actvolumeInDevice - MaxVolumeDevice;
                this.run_actvolumeInDevice = MaxVolumeDevice;
            }



            // Outflows
            // Evaporation or Evapotranspiration
            // calculate Potential evapotranspiration and convert in L
            this.run_PET = this.run_RefET * CropCoeff * area; // RefET in mm, area in m2 so PET in L
            // calculate Actual Evapotranspiration (ActET) -simple
            this.run_ActET = Math.min(this.run_PET, this.run_actvolumeInDevice);

            // Infiltration 
                if (this.run_actvolumeInDevice > 0) {
                    this.run_Qinf = Math.min(area * Ks * 1000 * 3600, this.run_actvolumeInDevice); //area in m2, Ks m/s, Qinf in L
                } else if (this.run_actvolumeInDevice == 0) {
                    this.run_Qinf = 0;
                }//

            // what if there is not enough water for ET and infiltration? split as a proportion
            if (this.run_ActET + this.run_Qinf > this.run_actvolumeInDevice) {
                double share_Qet = this.run_ActET / (this.run_ActET + this.run_Qinf + 0.00001);
                double share_Qinf = this.run_Qinf / (this.run_ActET + this.run_Qinf + 0.00001);

                this.run_ActET = this.run_actvolumeInDevice * share_Qet;
                this.run_Qinf = this.run_actvolumeInDevice * share_Qinf;
            }
            
            //Qout - underdrain flow - for the moment lets forget about that
            this.run_Qout = 0; //Math.min(Cout * Math.sqrt(2 * g * volumeInGI/volumeGI) , volumeInGI) ; // in L 
            //volumeInGI = Math.max(volumeInGI - Qout,0);

            // update volume
            this.run_actvolumeInDevice = Math.max(this.run_actvolumeInDevice - this.run_Qinf - this.run_ActET, 0);
        }else{
            getModel().getRuntime().println("Device area < 0 ! Not possible");
        }

        // write values  
        DevicePET.setValue(this.run_PET);
        DeviceActET.setValue(this.run_ActET);
        DeviceOut.setValue(this.run_Qout);
        DeviceInfiltration.setValue(this.run_Qinf);
        DeviceVol.setValue(this.run_actvolumeInDevice);
        DeviceRD1.setValue(this.run_QRD1);
        DeviceRD2.setValue(this.run_QRD2);
        DeviceRG1.setValue(this.run_QRG1);
        DeviceRG2.setValue(this.run_QRG2);
        DeviceIn.setValue(this.run_Qin);
        DevicePrecip.setValue(this.run_Qrain);
        DeviceOverFlow.setValue(this.run_Qovf);

    }

    @Override
    public void cleanup() {
    }
}
