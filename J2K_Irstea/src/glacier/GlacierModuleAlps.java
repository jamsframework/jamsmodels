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

package snow;
;
import java.io.*;
import jams.data.*;
import jams.model.*;

/**
 *
 * @author Peter Krause, changed by Santosh Nepal and Jordi Bolibar.
 */



@JAMSComponentDescription(
        title="GlacierModuleAlps",
        author="Peter Krause",
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
        "timestep."
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
            unit="L/m^2"
            )
            public Attribute.Double netRain;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "the actual snowfall",
            unit="L/m^2"
            )
            public Attribute.Double netSnow;

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
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "snow melt from glacier areas",
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

//    @JAMSVarDescription(
//            access = JAMSVarDescription.AccessType.WRITE,
//            update = JAMSVarDescription.UpdateType.RUN,
//            description = "runoff from snow melt and rain",
//            unit = "L"
//            )
//            public Attribute.Double snowRunoff;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "runoff from snow rain",
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

        getModel().getRuntime().println("GlacierModuleAlps");
        
        getModel().getRuntime().println("SMB in -> netSnow: " + this.netSnow.getValue());
        getModel().getRuntime().println("SMB in -> netRain: " + this.netRain.getValue());

        //retreive the actual states and input

//        double in_rain = this.rain.getValue();
//        double in_snow = this.snow.getValue();

        double snowStor = this.snowTotSWE_G.getValue();
       // double snowMelt_G = this.snowMelt_G.getValue();
        double tmean = this.tmean.getValue();
        double glacIn = this.netRain.getValue() + this.netSnow.getValue();
        double glacOut = 0;

        double SAC = this.actSlAsCf.getValue();
		getModel().getRuntime().println("Tmean: "+tmean );

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
//        double snowMelt_G = 0;
        double iceMelt = 0;
        double totalMelt = 0;
		getModel().getRuntime().println("n: "+n );

 //if (time.equals(c) && (id.getValue() == 1787)) {
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
		//getModel().getRuntime().println("this.slope.getValue(): " + this.slope.getValue());
		//getModel().getRuntime().println("this.slopeThreshold.getValue(): " + this.slopeThreshold.getValue());
		//getModel().getRuntime().println("this.elevation.getValue(): " + this.elevation.getValue());
		//getModel().getRuntime().println("this.elevationThreshold.getValue(): " + this.elevationThreshold.getValue());
		
		if (this.slope.getValue() < this.slopeThreshold.getValue() &&
                this.elevation.getValue() < this.elevationThreshold.getValue()) {
            iceMelt = iceMelt - (iceMelt * this.debrisFactor.getValue()/10) ;
        }
		else {
			iceMelt = iceMelt;
		}
		
		// No use of SAC in this version
        //iceMelt = iceMelt *  SAC;

        totalMelt = snowMelt_G.getValue() + iceMelt;

		//getModel().getRuntime().println("-----------------------");
		//getModel().getRuntime().println("");
		//getModel().getRuntime().println("Total melt: "+totalMelt );

		double allIn = snowMelt_G.getValue() + this.netRain.getValue();
                
		//getModel().getRuntime().println("Ice melt: "+ iceMelt );
		//getModel().getRuntime().println("Snow melt: "+ snowMelt_G.getValue() );
		//getModel().getRuntime().println("Snow melt + rain: "+allIn );
		//getModel().getRuntime().println("");
		//getModel().getRuntime().println("-----------------------");

		//route runoff inside glacier
		//snow routing

			//snowRunofffftm1 += glacStorage
			// glacStorage = 0;


		double q_snow = this.snowRunofftm1.getValue() * Math.exp(-1/this.kSnow.getValue()) + (snowMelt_G.getValue()) * (1-Math.exp(-1/this.kSnow.getValue()));
		double q_rain = this.rainRunofftm1.getValue() * Math.exp(-1/this.kRain.getValue()) + (this.netRain.getValue()) * (1-Math.exp(-1/this.kRain.getValue()));
		//double q_snowRain = this.snowRunofftm1.getValue() * Math.exp(-1/this.kSnow.getValue()) + (snowMelt_G + this.rain.getValue()) * (1-Math.exp(-1/this.kSnow.getValue()));
		//ice routing
		double q_ice = this.iceRunofftm1.getValue() * Math.exp(-1/this.kIce.getValue()) + iceMelt * (1-Math.exp(-1/this.kIce.getValue()));

		//double iceStorage = 9999 -double q_ice
		
		//calc total glacier runoff
		//double tot_q = q_ice + snowMelt_G.getValue();

		double tot_q = q_ice + q_snow + q_rain;

		//q_ice should not be included in the balance, since it is not provided as input. otherwise, waterbalnce is wrong
        
        //this.glacStorage.setValue(glacStorage.getValue()+ allIn - q_ice - q_snow - q_rain); //why is q_ice missing in that calculation??//water balance is wrong
        
        
        //water balance is right
        this.glacStorage.setValue(allIn - q_snow - q_rain); //q_ice is not considered as input
        
        //getModel().getRuntime().println("glacStorage: " + this.glacStorage.getValue());
        //getModel().getRuntime().println("allIn: " + allIn);
        //getModel().getRuntime().println("q_snow: " + q_snow);
        //getModel().getRuntime().println("q_rain: " + q_rain);
        
     //   this.glacStorage.setValue(snowMelt_G - q_snow);
       // this.glacStorage.setValue(allIn - q_snow - q_rain);
       glacOut = tot_q;

        //writing variables back

        this.snowRunofftm1.setValue(q_snow);
        this.rainRunofftm1.setValue(q_rain);
        this.iceRunofftm1.setValue(q_ice);
        this.rainRunoff.setValue(q_rain);


        this.glacierRunoff.setValue(tot_q);
        this.iceRunoff.setValue(q_ice);
        this.snowTotSWE_G.setValue(snowStor);
        this.snowMelt_G.setValue(q_snow);
        //this.snowMelt_G.setValue(snowMelt_G.getValue());
   //     this.snowRunoff.setValue(q_snow);
   
        getModel().getRuntime().println("-----------------------");
        getModel().getRuntime().println("");
        getModel().getRuntime().println("iceRunoff: " + iceRunoff.getValue());
        getModel().getRuntime().println("snowMelt_G: " + snowMelt_G.getValue());
        getModel().getRuntime().println("rainRunoff: " + rainRunoff.getValue());
        getModel().getRuntime().println("glacierRunoff: " + glacierRunoff.getValue());
        getModel().getRuntime().println("");
        getModel().getRuntime().println("-----------------------");
        

        //this.snowTotSWE_G.setValue(snowStor);
       // this.precip.setValue(this.precip.getValue()*this.area.getValue());
       //double precip = this.precip.getValue();
        this.massBalance.setValue(glacIn - glacOut);
        
        getModel().getRuntime().println("glacIn: " + glacIn);
        getModel().getRuntime().println("glacOut: " + glacOut);
        getModel().getRuntime().println("massBalance: " + this.massBalance.getValue());
      //  this.snow.setValue(in_snow);
      //  this.rain.setValue(in_rain);
    }

    public void cleanup()  throws IOException {

    }
}
