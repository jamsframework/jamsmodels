/*
 * SoilMoisture.java
 * Created on 30. September 2005, 11:37
 *
 * This file is part of JAMS
 * Copyright (C) 2005 S. Kralisch and P. Krause
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA
 *
 */
package gov.usgs.thornthwaite;

import jams.model.*;
import jams.data.*;

/**
 *
 * @author S. Kralisch
 */
@JAMSComponentDescription (title = "Thorntwaite soilmoisture (McCabe & Markstrom 2007)",
                           author = "Manfred Fink",
                           date = "17. 12 2024",
                           description = "This component calculates the soil moisture, actual ET and surface runoff based on " +
"old soil moisture, potET, temperature and precipitation (McCabe & Markstrom 2007)")
public class SoilMoisture_thorn extends JAMSComponent {

    @JAMSVarDescription (access = JAMSVarDescription.AccessType.READ,
                         description = "Soil moisture storage capacity",
                         defaultValue = "200.0")
    public Attribute.Double soilMoistStorCap;

    @JAMSVarDescription (access = JAMSVarDescription.AccessType.READ,
                         description = "Potential ET")
    public Attribute.Double potET;

    @JAMSVarDescription (access = JAMSVarDescription.AccessType.READ,
                         description = "Temperature")
    public Attribute.Double temp;

    @JAMSVarDescription (access = JAMSVarDescription.AccessType.READ,
                         description = "Precipitation")
    public Attribute.Double precip;

    @JAMSVarDescription (access = JAMSVarDescription.AccessType.READWRITE,
                         description = "runoff storage")
    public Attribute.Double prestor;

    @JAMSVarDescription (access = JAMSVarDescription.AccessType.READWRITE,
                         description = "Simulated soil moisture")
    public Attribute.Double soilMoistStor;

    @JAMSVarDescription (access = JAMSVarDescription.AccessType.WRITE,
                         description = "Surface runoff")
    public Attribute.Double surfaceRunoff;

    @JAMSVarDescription (access = JAMSVarDescription.AccessType.WRITE,
                         description = "Actual ET")
    public Attribute.Double actET;

    @JAMSVarDescription (access = JAMSVarDescription.AccessType.READ,
                         description = "Soil Runoff Factor",
                         lowerBound = 0.0,
                         upperBound = 1.0,
                         defaultValue = "0.8")
    public Attribute.Double RO_factor;
    
    public void run() {
        // get the parameter values
        double soilMoistStorCap = this.soilMoistStorCap.getValue();
        double prestor = this.prestor.getValue();

        // get the input values
        double temp = this.temp.getValue();
        double precip = this.precip.getValue();
        double potET = this.potET.getValue();

        double pmpe = precip - potET;

        double surfaceRunoff = 0.0;
        double soilMoistStor = this.soilMoistStor.getValue();
        double actET = 0.0;

        if (precip < potET) {
            actET = precip;           
            
            double soilWater = soilMoistStor = Math.min(Math.abs(precip-potET)*(soilMoistStor/soilMoistStorCap),0);
            actET = actET + soilWater;
            soilMoistStor = soilMoistStor - soilWater;
//  SOIL MOISTURE RECHARGE
        } else {

            actET = potET;
            double precip_sur = precip - potET;
            soilMoistStor = soilMoistStor + precip_sur;
            
            if (soilMoistStor > soilMoistStorCap){
                surfaceRunoff = soilMoistStor - soilMoistStorCap;
                soilMoistStor = soilMoistStorCap;
            }
            
            double pot_runoff = prestor + surfaceRunoff;
            surfaceRunoff = pot_runoff * this.RO_factor.getValue();
            prestor = pot_runoff * (1 - this.RO_factor.getValue());
         
            
            
            
        } 

        //SETTING THE OUTPUT VARIABLES
        this.surfaceRunoff.setValue(surfaceRunoff);
        this.soilMoistStor.setValue(soilMoistStor);
        this.actET.setValue(actET);
        this.prestor.setValue(prestor);
    }
}
