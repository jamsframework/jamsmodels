/*
 * CalcRainSnowParts.java
 * Created on 23. November 2005, 17:33
 *
 * This file is part of JAMS
 * Copyright (C) 2005 FSU Jena, c0krpe
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

package snow;

import jams.data.*;
import jams.model.*;

/**
 *
 * @author Peter Krause
 */
@JAMSComponentDescription(
        title="CalcRainSnowParts",
        author="Peter Krause",
        description="Distributes precipitation into rain and snow based on air temperature",
        version="1.0_0",
        date="2011-05-30"
        )
        public class CalcRainSnowParts_IRSTEA extends JAMSComponent {
    
    /*
     *  Component variables
     */
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "HRU attribute name area",
            unit = "m²"
            )
            public Attribute.Double par_area;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Snow parameter TRS",
            lowerBound = -10.0,
            upperBound = 10.0,
            defaultValue = "0.0",
            unit = "°C"
            )
            public Attribute.Double par_snow_trs;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Snow parameter TRANS",
            lowerBound = 0.0,
            upperBound = 5.0,
            defaultValue = "2.0",
            unit = "K"
            )
            public Attribute.Double par_snow_trans;
    
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "state variable mean temperature",
            unit = "°C"
            )
            public Attribute.Double par_tmean;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "state variable precipitation",
            unit = "mm"
            )
            public Attribute.Double par_precip;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "state variable rain",
            unit = "L"
            )
            public Attribute.Double st_rain;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "state variable snow",
            unit = "L"
            )
            public Attribute.Double st_snow;
        
    
    /*
     *  Component run stages
     */
    
    public void init() {

    }
    
    public void run() throws Attribute.Entity.NoSuchAttributeException{
        double run_temperature = this.par_tmean.getValue();
        //determinining relative snow amount of total precip depending on temperature
        double run_pSnow = (par_snow_trs.getValue() + par_snow_trans.getValue() - run_temperature) /
                (2 * par_snow_trans.getValue());
        
        //fixing upper and lower bound for pSnow (has to be between 0 and 1
        if(run_pSnow > 1.0)
            run_pSnow = 1.0;
        else if(run_pSnow < 0)
            run_pSnow = 0;
        
        //converting mm/m² to absolute litres
        double run_precip = this.par_precip.getValue() * this.par_area.getValue();
        if (run_precip < 0){
           run_precip = 0; 
        }
        //dividing input precip into rain and snow
        double run_rain = (1 - run_pSnow) * run_precip;
        double run_snow = run_pSnow * run_precip;
      
        this.st_snow.setValue(run_snow);
        this.st_rain.setValue(run_rain);
		
		//getModel().getRuntime().println("CalcRainSnowParts temperature: "+ temperature );
		//getModel().getRuntime().println("CalcRainSnowParts pSnow: "+ pSnow );
		//getModel().getRuntime().println("CalcRainSnowParts precip: "+this.precip );
		//getModel().getRuntime().println("CalcRainSnowParts rain: "+this.rain );
		//getModel().getRuntime().println("CalcRainSnowParts snow: "+this.snow );
        
    }
    
    public void cleanup() {
        
    }
}
