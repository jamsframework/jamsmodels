/*
 * GlacierModule.java
 * Created on 22. Febuary 2008, 13:57
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

package glacier;
;
import jams.JAMS;
import java.io.*;
import jams.data.*;
import jams.model.*;

/**
 *
 * @author Peter Krause, changed by Santosh Nepal, Jordi Bolibar and Isabelle Gouttevin (isabelle.gouttevin@meteo.fr)
 */



@JAMSComponentDescription(
        title="GlacierModuleAlps",
        author="Peter Krause; Santosh Nepal, Jordi Bolibar, Isabelle Gouttevin",
        description="Simple process module for glacier simulation. The module " +
        "calculates snow accumulation by a temperature threshold approach and " +
        "snow melt from the glacier with a day-degree-approach. Melt from the " +
        "glacier is implementing by the melt formula according to " +
        "Hock (1998, 1999) in a simple and a more complex form. " +
        "The simple form needs temperature only whereas" +
        "the complex form needs also radiation." +
        "Glacier runoff is calculated by the outflow from two reservoirs. The first" +
        "represents snow falling on the glacier whereas the second represents the" +
        "ice of the glacier. The same idea was implemented in WasimETH first."+
        "Changed:meltTemp is derived from the average of Tmax and Tmean"+
        "integrating melt correction factor to include slope and aspect in radiation based model//s.nepal., to be changed"+
        "Changed for the new version for the Alps: glaciers can now retreat and their surface area is changed with an annual"+
        "timestep + use of the simplified melt formula only (no radiation)"
        )

    public class GlacierModuleAlps extends JAMSComponent {


    /*
     *  Component variables
     */


     @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "the tmean input",
            unit="?C"
            )
            public Attribute.Double tmean;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "the actual rainfall",
            unit="L" // IG unit corrected
            )
            public Attribute.Double netRain;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "the total snowfall over the Glacier",
            unit="L" // IG unit corrected
            )
            public Attribute.Double adjSnow;
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "the total rainall over the Glacier",
            unit="L" 
            )
            public Attribute.Double adjRain;

        @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "attribute slope"
            )
            public Attribute.Double slope;

         @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "attribute elevation"
            )
            public Attribute.Double elevation;
            
         @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Elevation Threshold for debris covered Glacier"
            )
            public Attribute.Double elevationThreshold;

        
         @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Slope Threshold for debris covered Glacier"
            )
            public Attribute.Double slopeThreshold;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "the actual global radiation",
            unit = "MJ/day"
            )
            public Attribute.Double radiation;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "actual snow storage",
            unit = "L/m^2"
            )
            public Attribute.Double snowTotSWE_G;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "glacierized area",
            unit="m^2"
            )
            public Attribute.Double glacierArea;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "snow runoff of time step before",
            unit = "L"
            )
            public Attribute.Double snowRunofftm1;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "snow melt and water from rain-on-snow from glacier areas",
            unit = "L"
            )
            public Attribute.Double snowMelt_G;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "rain runoff of time step before",
            unit = "L"
            )
            public Attribute.Double rainRunofftm1;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "ice runoff of time step before",
            unit = "L"
            )
            public Attribute.Double iceRunofftm1;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "total runoff of unit",
            unit = "L"
            )
            public Attribute.Double glacierRunoff;


    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "runoff from glacier ice melt",
            unit="L"
            )
            public Attribute.Double iceRunoff;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "runoff from snow melt and rain",
            unit = "L"
            )
            public Attribute.Double snowRunoff;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "runoff from rain over the glacier without snow cover",
            unit = "L"
            )
            public Attribute.Double rainRunoff;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "remaining storage (only for balance calculation)",
            unit = "L"
            )
            public Attribute.Double glacStorage;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            update = JAMSVarDescription.UpdateType.INIT,
            description = "mass balance",
            unit = "L"
            )
            public Attribute.Double massBalance;

//    @JAMSVarDescription(
//            access = JAMSVarDescription.AccessType.READ,
//            update = JAMSVarDescription.UpdateType.INIT,
//            description = "generalised melt factor snow"
//            )
//            public Attribute.Double meltFactorSnow;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "generalised melt factor ice"
            )
            public Attribute.Double meltFactorIce;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "day degree factor for ice"
            )
            public Attribute.Double ddfIce;

//    @JAMSVarDescription(
//            access = JAMSVarDescription.AccessType.READ,
//            update = JAMSVarDescription.UpdateType.INIT,
//            description = "melt coefficient for snow"
//            )
//            public Attribute.Double alphaSnow;


    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "routing coefficient for snow"
            )
            public Attribute.Double kSnow;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "routing coefficient for ice"
            )
            public Attribute.Double kIce;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "routing coefficient for rain"
            )
            public Attribute.Double kRain;


    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "threshold temperature for icemelt"
            )
            public Attribute.Double tbase;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "melt formula [1 = simple, 2 = complex]"
            )
            public Attribute.Integer meltFormula;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "temporal resolution [d | h]"
            )
            public Attribute.String tempRes;


    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "debris factor based on the debris cover on glaciers"
            )
            public Attribute.Double debrisFactor;

        @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "state var slope-aspect-correction-factor"
            )
            public Attribute.Double actSlAsCf;

    /*
     *  Component run stages
     */

    public void run() throws Attribute.Entity.NoSuchAttributeException, IOException {

        //getModel().getRuntime().println("GlacierModuleAlps", JAMS.VVERBOSE);
        //getModel().getRuntime().println("SMB in -> adjSnow: " + this.adjSnow.getValue(), JAMS.VVERBOSE);
        //getModel().getRuntime().println("SMB in -> adjRain: " + this.adjRain.getValue(), JAMS.VVERBOSE);
	//getModel().getRuntime().println("SMB in -> netRain: " + this.netRain.getValue(), JAMS.VVERBOSE);
        
        //retreive the actual states and input
        double glacierArea = this.glacierArea.getValue();
        
        // do calculations only if glacier Area > 0.0
        if(glacierArea > 0.0){
            double snowStor = this.snowTotSWE_G.getValue();
            // double snowMelt_G = this.snowMelt_G.getValue();
            double tmean = this.tmean.getValue();
            // variables for glacier mass balance
            double glacIn = this.adjRain.getValue() + this.adjSnow.getValue(); //IG changed net-> adj
            double glacOut = 0;
            
            double meltTemp = tmean;
            double n = 0;
            if (this.tempRes.getValue().equals("d")) {
                n = 1;
                } else if (this.tempRes.getValue().equals("h")) {
                    n = 24;
                    } else if (this.tempRes.getValue().equals("m")) {
                        n = 1 / 30;
                        }
            //calc potential snow accumulation
            //        if (this.snow.getValue() > 0) {
            //            snowStor = snowStor + this.snow.getValue();
            //   double iceStorage = 9999;
            
            //calc potential melt
            //double snowMelt_G = 0;
            double iceMelt = 0;
            double totalMelt = 0;
            //getModel().getRuntime().println("n: "+n, JAMS.VVERBOSE);
            
            // calculation of icemelt
            if ((meltTemp > tbase.getValue()) && (snowStor == 0)) {
                if (this.meltFormula.getValue() == 1) {
                    iceMelt = (1 / n) * this.ddfIce.getValue() * (meltTemp - this.tbase.getValue());
                    iceMelt = iceMelt * this.glacierArea.getValue();
                    }
                
                if (this.meltFormula.getValue() == 2) {
                    iceMelt = (1 / n) * (this.meltFactorIce.getValue()) * (meltTemp - this.tbase.getValue());
                    iceMelt = iceMelt * this.glacierArea.getValue();
                    }
                } else {
                iceMelt = 0;
                }
            
            // correction of iceMelt if slope and elevation below treshold?
            if (this.slope.getValue() < this.slopeThreshold.getValue() &&
                    this.elevation.getValue() < this.elevationThreshold.getValue()) {
                iceMelt = iceMelt - (iceMelt * this.debrisFactor.getValue()/10) ;
                //getModel().getRuntime().println("CAREFULL, your glacier is considered debris-covered ; this.slope.getValue():"+ this.slope.getValue(), JAMS.VVERBOSE)
                }
            else {
                iceMelt = iceMelt;
                }
            
            totalMelt = snowMelt_G.getValue() + iceMelt;
            //getModel().getRuntime().println("Total melt: "+totalMelt );
            
            // snow melt and rain on snow routing
            double q_snow = this.snowRunofftm1.getValue() * Math.exp(-1/this.kSnow.getValue()) + (snowMelt_G.getValue()) * (1-Math.exp(-1/this.kSnow.getValue())); // IG : this includes rain-on-snow plus melt
            double q_rain = this.rainRunofftm1.getValue() * Math.exp(-1/this.kRain.getValue()) + (this.netRain.getValue()) * (1-Math.exp(-1/this.kRain.getValue())); // IG this is rain on ice only. netRain and not adjRain, since part of the adjRain may have fallen on snow
            // iceMelt routing
            double q_ice = this.iceRunofftm1.getValue() * Math.exp(-1/this.kIce.getValue()) + iceMelt * (1-Math.exp(-1/this.kIce.getValue()));
            
            //calc total glacier runoff
            double tot_q = q_ice + q_snow + q_rain;
            
            // FB: weird attempt at calculating mass balance. I don't get it
            double allIn = snowMelt_G.getValue() + this.netRain.getValue();
            //q_ice should not be included in the balance, since it is not provided as input. otherwise, waterbalnce is wrong
            //this.glacStorage.setValue(glacStorage.getValue()+ allIn - q_ice - q_snow - q_rain); //why is q_ice missing in that calculation??//water balance is wrong
            //water balance is right
            this.glacStorage.setValue(allIn - q_snow - q_rain); //q_ice is not considered as input
            glacOut = tot_q;
            
            //writing variables back
            // updating the "last time step" variables
            this.snowRunofftm1.setValue(q_snow);
            this.rainRunofftm1.setValue(q_rain);
            this.iceRunofftm1.setValue(q_ice);
            
            // glacier outputs
            this.rainRunoff.setValue(q_rain);
            this.snowRunoff.setValue(q_snow); //// IG I found this has to be de-commented
            this.iceRunoff.setValue(q_ice);
            this.glacierRunoff.setValue(tot_q);
            this.snowTotSWE_G.setValue(snowStor);
            //this.snowMelt_G.setValue(q_snow); ////IG  I found this line weird and commented it
            //this.snowMelt_G.setValue(snowMelt_G.getValue());
            
            //        getModel().getRuntime().println("-----------------------", JAMS.VVERBOSE);
            //        getModel().getRuntime().println("", JAMS.VVERBOSE);
            //        getModel().getRuntime().println("iceRunoff: " + iceRunoff.getValue(), JAMS.VVERBOSE);
            //        getModel().getRuntime().println("snowMelt_G: " + snowMelt_G.getValue(), JAMS.VVERBOSE);
            //        getModel().getRuntime().println("rainRunoff: " + rainRunoff.getValue(), JAMS.VVERBOSE);
            //        getModel().getRuntime().println("snowRunoff: " + snowRunoff.getValue(), JAMS.VVERBOSE);
            //        getModel().getRuntime().println("glacierRunoff: " + glacierRunoff.getValue(), JAMS.VVERBOSE);
            //        getModel().getRuntime().println("", JAMS.VVERBOSE);
            //        getModel().getRuntime().println("-----------------------", JAMS.VVERBOSE);
            
            this.massBalance.setValue(glacIn - glacOut);
            
            //        getModel().getRuntime().println("glacIn: " + glacIn, JAMS.VVERBOSE);
            //        getModel().getRuntime().println("glacOut: " + glacOut, JAMS.VVERBOSE);
            //        getModel().getRuntime().println("massBalance: " + this.massBalance.getValue(), JAMS.VVERBOSE);
            }
        
        // if glacier Area == 0! Set all variables to 0!!
        else{
            this.glacStorage.setValue(0.0);
            // updating the "last time step" variables
            this.snowRunofftm1.setValue(0.0);
            this.rainRunofftm1.setValue(0.0);
            this.iceRunofftm1.setValue(0.0);
            // glacier outputs
            this.rainRunoff.setValue(0.0);
            this.snowRunoff.setValue(0.0);
            this.iceRunoff.setValue(0.0);
            this.glacierRunoff.setValue(0.0);
            this.snowTotSWE_G.setValue(0.0);
            this.massBalance.setValue(0.0);
            }
    }

    public void cleanup()  throws IOException {

    }
}
