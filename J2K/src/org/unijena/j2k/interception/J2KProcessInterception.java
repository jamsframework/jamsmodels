

package org.unijena.j2k.interception;

import jams.data.*;
import jams.model.*;

/**
 *
 * @author Peter Krause
 */
//    language = "de"

@JAMSComponentDescription(
        title="J2KProcessInterception",
        author="Peter Krause",
        description="Calculates daily interception based on DICKINSON 1984",
        process="Interception",
        version="1.0",
        date="2010-09-07"
    
        )

        public class J2KProcessInterception extends JAMSComponent {
    

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "attribute area",
            unit = "m^2",
            lowerBound= 0,
            upperBound =1000000,
            defaultValue=""
            )
            public JAMSDouble area;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "state variable mean tempeature",
            unit = "°C",
            lowerBound= -70,
            upperBound = 70,
            defaultValue=""
            )
            public JAMSDouble tmean;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "state variable rain",
            unit = "L",
            lowerBound= 0,
            upperBound =100,
            defaultValue=""
            )
            public JAMSDouble rain;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "state variable snow",
            unit = "L",
            lowerBound= 0,
            upperBound =100,
            defaultValue=""
            )
            public JAMSDouble snow;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "state variable potET",
            unit = "L",
            lowerBound= 0,
            upperBound =100,
            defaultValue=""
            )
            public JAMSDouble potET;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "state variable actET",
            unit = "L",
            lowerBound= 0,
            upperBound =100,
            defaultValue=""
            )
            public JAMSDouble actET;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "state variable LAI",
            unit = "mm*m^-2",
            lowerBound= 0,
            upperBound =100,
            defaultValue=""
            )
            public JAMSDouble actLAI;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Snow parameter TRS",
            unit = "°C",
            lowerBound= -10,
            upperBound =10,
            defaultValue=""
            )
            public JAMSDouble snow_trs;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Snow parameter TRANS",
            unit = "°C",
            lowerBound= -10,
            upperBound =10,
            defaultValue=""
            )
            public JAMSDouble snow_trans;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Interception parameter a_rain",
            unit = "mm*m^-2",
            lowerBound= 0,
            upperBound =10,
            defaultValue=""
            )
            public JAMSDouble a_rain;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Interception parameter a_snow",
            unit = "mm*m^-2",
            lowerBound= 0,
            upperBound =10,
            defaultValue=""
            )
            public JAMSDouble a_snow;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "state variable net-rain",
            unit = "L",
            lowerBound= 0,
            upperBound =100,
            defaultValue=""
            )
            public JAMSDouble netRain;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "state variable net-snow",
            unit = "L",
            lowerBound= 0,
            upperBound =100,
            defaultValue=""
            )
            public JAMSDouble netSnow;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "state variable throughfall",
            unit = "L",
            lowerBound= 0,
            upperBound =100,
            defaultValue=""
            )
            public JAMSDouble throughfall;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "state variable dy-interception",
            unit = "L",
            lowerBound= 0,
            upperBound =100,
            defaultValue=""
            )
            public JAMSDouble interception;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "state variable interception storage",
            unit = "L",
            lowerBound= 0,
            upperBound =100,
            defaultValue=""
            )
            public JAMSDouble intercStorage;
    /*
     *  Component run stages
     */
    
    public void init() throws JAMSEntity.NoSuchAttributeException{
        this.intercStorage.setValue(0);
    }
    
    public void run() throws JAMSEntity.NoSuchAttributeException{
        
        double alpha = 0;
        double out_throughfall = 0;
        double out_interception = 0;
        
        double in_rain = rain.getValue();
        double in_snow = snow.getValue();
        double in_temp = tmean.getValue();
        
        double in_potETP = potET.getValue();
        double in_actETP = actET.getValue();
        
        double in_LAI = this.actLAI.getValue();
        double in_Area = area.getValue();
        
        double out_InterceptionStorage = intercStorage.getValue();
        double out_actETP = in_actETP;
        
        double sum_precip = in_rain + in_snow;
        
        double deltaETP = in_potETP - in_actETP;
                
        double relRain, relSnow;
        if(sum_precip > 0){
            relRain = in_rain / sum_precip;
            relSnow = in_snow / sum_precip;
        } else{
            relRain = 1.0; //throughfall without precip is in general considered to be liquid
            relSnow = 0;
        }
        
        //determining if precip falls as rain or snow
        if(in_temp < (snow_trs.getValue() - snow_trans.getValue())){
            //alpha = alpha_snow;
            alpha = a_snow.getValue();
        } else{
            //alpha = alpha_rain;
            alpha = a_rain.getValue();
        }
        
        //determinining maximal interception capacity of actual day
        double maxIntcCap = (in_LAI * alpha) * in_Area;
        
        //if interception storage has changed from snow to rain then throughfall
        //occur because interception storage of antecedend day might be larger
        //then the maximum storage capacity of the actual time step.
        if(out_InterceptionStorage > maxIntcCap){
            out_throughfall = out_InterceptionStorage - maxIntcCap;
            out_InterceptionStorage = maxIntcCap;
        }
        
        //determining the potential storage volume for daily Interception
        double deltaIntc = maxIntcCap - out_InterceptionStorage;
        
        //reducing rain and filling of Interception storage
        if(deltaIntc > 0){
            //double save_rain = sum_precip;
            if(sum_precip > deltaIntc){
                out_InterceptionStorage = maxIntcCap;
                sum_precip = sum_precip - deltaIntc;
                out_throughfall = out_throughfall + sum_precip;
                out_interception = deltaIntc;
                deltaIntc = 0;
            } else{
                out_InterceptionStorage = (out_InterceptionStorage + sum_precip);
                out_interception = sum_precip;
                sum_precip = 0;
            }
        } else{
            out_throughfall = out_throughfall + sum_precip;
        }
        
        //depletion of interception storage; beside the throughfall from above interc.
        //storage can only be depleted by evapotranspiration
        if(deltaETP > 0){
            if(out_InterceptionStorage > deltaETP){
                out_InterceptionStorage = out_InterceptionStorage - deltaETP;
                out_actETP = in_actETP + deltaETP;
                deltaETP = 0;
                
            } else{
                deltaETP = deltaETP - out_InterceptionStorage;
                out_actETP = in_actETP + (in_potETP - deltaETP);
                out_InterceptionStorage = 0;
            }
        } else{
            out_actETP = deltaETP;
        }
        
        this.netRain.setValue(out_throughfall * relRain);
        this.netSnow.setValue(out_throughfall * relSnow);
        this.actET.setValue(out_actETP);
        this.intercStorage.setValue(out_InterceptionStorage);
        this.interception.setValue(out_interception);
        this.throughfall.setValue(out_throughfall);
        
    }
    
    public void cleanup() {
        this.intercStorage.setValue(0);
    }
    
}
