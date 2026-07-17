/*
 * SimpleSOD.java
 * Created on 24.04.2013, 17:44:17
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
package sewer;

import jams.data.*;
import jams.model.*;
import java.util.GregorianCalendar;

/**
 *
 * @author Sven Kralisch <sven.kralisch at uni-jena.de>
 */
@JAMSComponentDescription(
        title = "GreenInfrastructure",
        author = "Jérémie Bonneau",
        description = "This is to model Green Infrastructure in each HRUs.",
        date = "2020-12-15",
        version = "1.0_0")
public class SimpleGI_test extends JAMSComponent {

    /*
     *  Component attributes test something
     */
    
    // FLUXES TO BE READ
        // Reading flow
     @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "RD1 from impervous areas",
            unit = "L"
            )
            public Attribute.Double rd1;    
        // Reading Rainfall
     @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Rainfall",
            unit = "mm" 
            )
            public Attribute.Double rain;
        // potential ET
     @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Pot ET",
            unit = "L"
            )
            public Attribute.Double PET;
     
    
    
    
    
    // PARAMETERS TO BE READ

     // ID of GI 
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ, // type of access, i.e. READ, WRITE, READWRITE
            description = "ID of green infrastructure" // description of purpose
            )
    public Attribute.Double GIID;    // for a list of attribute types, see jams.data.Attribute  
	 
    // Percentage of HRU area covered by GI
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ, // type of access, i.e. READ, WRITE, READWRITE
            description = "% of HRU area covered by GI" // description of purpose
            )
    public Attribute.Double coverGI;    // for a list of attribute types, see jams.data.Attribute  
	
    
    // Percentage of RD1 to flow into GI 
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ, // type of access, i.e. READ, WRITE, READWRITE
            description = "% of RD1 from impervious area to LID" // description of purpose
            )
    public Attribute.Double fracGI_RD1;    // for a list of attribute types, see jams.data.Attribute  
	
    // Depth of the filter media  -  to be read from GI.par
	@JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
            description = "Filter depth",
            unit = "m")
    public Attribute.Double filterDepth; 
        
    // Porosity -  to be read from GI.par
	@JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
            description = "Porosity",
            unit = "-")
    public Attribute.Double porosity;
        
    // Hydraulic conductivity of underlying soil test
     @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Hydraulic conductivity of underlying soil",
            unit = "m/s"
            )
            public Attribute.Double Kssoil; 
        
    // area 
     @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Area of Green infrastructure",
            unit = "m2"
            )
            public Attribute.Double areaGI;   
        
    // Discharge coefficient
     @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Discharge coefficient - Cout",
            unit = "-"
            )
            public Attribute.Double dischCoeff;
     
     
     
     
     
     
     
        
        
    // FLUXES TO BE WRITTEN
	// volume stored in GI 
     @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "actGI",
            unit = "L"
            )
            public Attribute.Double actGI;
    
    // outflow underdrain
     @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "Qin",
            unit = "L"
            )
            public Attribute.Double FlowInGI;
     
     
    // outflow underdrain
     @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "outQ",
            unit = "L"
            )
            public Attribute.Double FlowfromGI;
        
    // outflow ET
     @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "out ET",
            unit = "L"
            )
            public Attribute.Double ETfromGI;
        
    // outflow infiltration
     @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "out infiltration",
            unit = "L"
            )
            public Attribute.Double InfilfromGI;

         // rain in GI
     @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "Qrain in GI",
            unit = "L"
            )
            public Attribute.Double RainInGI;


         // overflow when GI is full
     @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "Runoff when GI is full",
            unit = "L"
            )
            public Attribute.Double OVFfromGI;



       

// BOOLEANS whixh in fact are doubles because I cant get them to work with booleans  
    // isVegegated
     @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Is Vegetated ?",
            unit = "-"
            )
            public Attribute.Double isVeg;
    // isLined
     @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Is Lined ?",
            unit = "-"
            )
            public Attribute.Double isLined;
     
     //timestep
    @JAMSVarDescription(
               access = JAMSVarDescription.AccessType.READ,
                description = "time interval",
                unit = "d")
            public Attribute.TimeInterval dt;
    
//current timestep
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Current time step",
            unit = "d")
        public Attribute.Calendar time;

    
      //internal state variables
    double run_Rain, run_FlowIn, run_PET, isveg, isLin, run_Qin, run_Qrain, run_Qet, run_Qinf, run_Qout, run_Qovf, run_actvolumeInGI, run_covGI;
    private int seconds;
	
    /*
     *  Component run stages
     */
    @Override
    public void init() {
                if (dt.getTimeUnit() == GregorianCalendar.MINUTE) {
            seconds = 60*dt.getTimeUnitCount();
        } else if (dt.getTimeUnit() == GregorianCalendar.HOUR) {
            seconds = 3600*dt.getTimeUnitCount();
        } else if (dt.getTimeUnit() == GregorianCalendar.DAY_OF_YEAR) {
            seconds = 24*3600*dt.getTimeUnitCount();
        }  else if (dt.getTimeUnit() == GregorianCalendar.MONTH) {
            seconds = time.getActualMaximum(GregorianCalendar.DAY_OF_MONTH)*24*3600*dt.getTimeUnitCount();
        }
    }

    @Override
    public void run() {

	// constant parameters to read	
        double g = 9.80665;
        double Cout = dischCoeff.getValue();
        double H = filterDepth.getValue(); 
		double fracGI = fracGI_RD1.getValue(); //fraction of RD1 in GI
        double p = porosity.getValue();
        double area_GI = areaGI.getValue();
        double Ks = Kssoil.getValue();

        this.run_Rain = rain.getValue();
        this.run_FlowIn = rd1.getValue();
        this.run_PET = PET.getValue(); // in L

        this.run_actvolumeInGI = actGI.getValue();

        this.run_Qin = 0;
        this.run_Qrain = 0;
        this.run_Qet = 0;
        this.run_Qout = 0;
        this.run_Qovf = 0; 
        this.run_Qinf = 0; //infiltration flux set to zero

        this.isveg = isVeg.getValue();
        this.isLin = isLined.getValue();
        this.run_covGI = coverGI.getValue(); // fraction of hru cover by GI

               
        //Calculate max volume of GI;
	double MaxVolumeGI = p * H * area_GI * 1000;
            
        // if no GI, set all fluxes to zero
        if ( this.run_covGI == 0 )
        {
            this.run_Qin = 0;
            this.run_Qrain = 0;
            this.run_Qet = 0;
            this.run_Qinf = 0; 
            this.run_Qout = 0;
            this.run_Qovf = 0; 
            this.run_actvolumeInGI =0;
        }    
        
        
        //if not, calculate fluxes
        else if ( this.run_covGI > 0 )
        {
            
            this.run_Qrain = this.run_Rain * area_GI; // rain in mm, area GI in m2 so Qrain in L
            this.run_Qin = fracGI * this.run_FlowIn; //FlowIn in L, Qin in L 

        // how much water is there in the GI
         
        this.run_actvolumeInGI = this.run_actvolumeInGI + this.run_Qrain + this.run_Qin;
        

        // check that there is not too much water - if so, overflow occures and needs to be connected to the HRU RD1
        
        if (this.run_actvolumeInGI > MaxVolumeGI) {
            this.run_Qovf = this.run_actvolumeInGI - MaxVolumeGI ;
            this.run_actvolumeInGI = MaxVolumeGI;
                                    }
        
         // first, set a condition on the vegetation: if it not vegetated, Qetr = 0, if not, Qetr = Qetp modulated by the soil moisture of the filter
	
        if ( this.isveg == 1 ){
            if (this.run_actvolumeInGI/MaxVolumeGI > 0.2) {
                this.run_Qet  = Math.min(this.run_PET ,this.run_actvolumeInGI);   //Qetp in L
                                            }
            else if (this.run_actvolumeInGI/MaxVolumeGI < 0.1)  {
                this.run_Qet  = 0;
                                                 }
            else {
                this.run_Qet  = Math.min(this.run_PET * (this.run_actvolumeInGI/MaxVolumeGI - 0.1)/(0.2 - 0.1),this.run_actvolumeInGI);
                  }
                        }
        else if ( this.isveg == 0 ) {
                    this.run_Qet  = 0;
                                }
                 
        // infiltration 
		
		// /!\ need to chage 3600 by variable 'seconds' define earlier
		
            if ( this.isLin == 1 ){
                this.run_Qinf = 0;
                            }
            else if ( this.isLin == 0 ) {
                if (this.run_actvolumeInGI > 0 )
                    this.run_Qinf  = Math.min(area_GI * Ks * 1000 * 3600 ,this.run_actvolumeInGI); //area in m2, Kssoil m/s, Qinf in L
                else if (this.run_actvolumeInGI == 0 ){
                    this.run_Qinf  = 0;
                                }//  L
                }
                    
        // what if there is not enough water for ET an infiltration? split by demand ?
       double share_Qet = this.run_Qet/(this.run_Qet + this.run_Qinf + 0.00001);
       double share_Qinf = this.run_Qinf/(this.run_Qet + this.run_Qinf + 0.00001);
        
            if (this.run_Qet + this.run_Qinf > this.run_actvolumeInGI) {
            
                this.run_Qet = this.run_actvolumeInGI * share_Qet;
                this.run_Qinf = this.run_actvolumeInGI * share_Qinf;
                
            }

        // update volume
	this.run_actvolumeInGI = Math.max(this.run_actvolumeInGI - this.run_Qinf - this.run_Qet,0);
        
                        
        //Qout - underdrain flow - for the moment lets forget about that
        this.run_Qout = 0; //Math.min(Cout * Math.sqrt(2 * g * volumeInGI/volumeGI) , volumeInGI) ; // in L 
        //volumeInGI = Math.max(volumeInGI - Qout,0);

                        
        }

        // write values     
        
           ETfromGI.setValue(this.run_Qet);
           FlowfromGI.setValue(this.run_Qout);
           InfilfromGI.setValue(this.run_Qinf);
           actGI.setValue(this.run_actvolumeInGI);
           FlowInGI.setValue(this.run_Qin); 
           RainInGI.setValue(this.run_Qrain); 
           OVFfromGI.setValue(this.run_Qovf);
        
        }

    @Override
    public void cleanup() {
    }
}