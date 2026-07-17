/*
 * Snow.java
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
@JAMSComponentDescription (title = "Thorntwaite snowmelt (McCabe & Markstrom 2007)",
                           author = "Sven Kralisch & Manfred Fink",
                           date = "17. 12 2024",
                           description = "This component calculates the snow melt based on a snow storage, potET, according to McCabe & Markstrom 2007 " +
"temperature and precipitation")
public class Snow_thorn extends JAMSComponent {

    @JAMSVarDescription (access = JAMSVarDescription.AccessType.READWRITE,
                         description = "Amount of water currently stored as snow")
    public Attribute.Double snowStorage;

    @JAMSVarDescription (access = JAMSVarDescription.AccessType.READ,
                         description = "Current temperature")
    public Attribute.Double temp;

    @JAMSVarDescription (access = JAMSVarDescription.AccessType.READWRITE,
                         description = "Current precipitation")
    public Attribute.Double precip;

    @JAMSVarDescription (access = JAMSVarDescription.AccessType.WRITE,
                         description = "Simulated snow melt water")
    public Attribute.Double snowMelt;
    
    @JAMSVarDescription (access = JAMSVarDescription.AccessType.READ,
                         description = "Calibration parameter rain temperature")
    public Attribute.Double rainTemp;
    
    @JAMSVarDescription (access = JAMSVarDescription.AccessType.READ,
                         description = "Calibration parameter snow temperature")
    public Attribute.Double snowTemp;
    
    @JAMSVarDescription (access = JAMSVarDescription.AccessType.READ,
                         description = "Calibration parameter maximum melt proportion per timestep",
                         defaultValue = "0.5")
    public Attribute.Double meltmax;

    public void run() {

        double snowStorage = this.snowStorage.getValue();

        double temp = this.temp.getValue();
        double snowTemp = this.snowTemp.getValue();
        double rainTemp = this.rainTemp.getValue();
        double meltmax = this.meltmax.getValue();
        
        double precip = this.precip.getValue();
        
        double snowMelt = 0.0;

        
        //Snow Accumulation
        double P_snow = Math.max(Math.min(precip*(rainTemp-temp)/(rainTemp-snowTemp), precip),0.0);
        precip = precip - P_snow;
        snowStorage = snowStorage + P_snow;
        //Snow Melt
        double SMF = Math.max(Math.min((temp-snowTemp)/(rainTemp-snowTemp)*meltmax, meltmax), 0.0);
        snowMelt = SMF * snowStorage;
        snowStorage = snowStorage - snowMelt;
        
        // melt the remaining snow for summer
        if (snowStorage < 1 && temp > rainTemp + 1){
            snowMelt = snowMelt + snowStorage;
            snowStorage = 0.0;
        }
        

        this.snowStorage.setValue(snowStorage);
        this.snowMelt.setValue(snowMelt);
        this.precip.setValue(precip);
    }
}
