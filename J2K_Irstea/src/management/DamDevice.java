/*
 * SewerOverflowDevice.java
 * Created on 05. October 2012, 17:02
 *
 * This file is part of JAMS
 * Copyright (C) FSU Jena
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
package management;

import jams.data.*;
import jams.model.*;
import jams.workspace.DataSetDefinition;
import jams.workspace.stores.InputDataStore;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.Calendar;
//import java.jamsui.juice;


/**
 *
 * @author Sven Kralisch & Mériem Labbas & Christian Fischer
 */
@JAMSComponentDescription(title = "DamDevice",
        author = "Francois Tilmant & Flora Branger",
        description = "Component used for the simulation of an overflow device. It takes the different components outflows"
        + "coming from a sewer reach(threshold test) and adds it to the receiving reach river.",
        version = "3.0_0",
        date = "2014-04-17")
public class DamDevice extends JAMSComponent {

    /*
     * Component variables
     */
       @JAMSVarDescription (access = JAMSVarDescription.AccessType.READ,
                         description = "regionalised data value (objective function)")
    public Attribute.Double FO;
        
            @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Initial volume in the reservoir",
            unit = "m3"
            )
            public Attribute.Double V0;
        
                @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Maximum storage of the reservoir",
            unit = "m3"
            )
            public Attribute.Double Smax;
                
                  @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "RD1 inflow to reach",
            unit = "L"
            )
            public Attribute.Double inRD1;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "RD2 inflow to reach",
            unit = "L"
            )
            public Attribute.Double inRD2;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "RG1 inflow to reach",
            unit = "L"
            )
            public Attribute.Double inRG1;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "RG2 inflow to reach",
            unit = "L"
            )
            public Attribute.Double inRG2;
    
        @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "state variable - Storage in the reservoir",
            unit = "L"
            )
            public Attribute.Double Storage;
        
            @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    description = "Current time")
    public Attribute.Calendar time;
    

    
    
  int nComp = 4;
    double[] relComp;
    double[] runComp;
    double[] outComp;
    double currVolume = 0;
    String Date2 = "1985-01-01 07:30";
    
    public void init() {
        relComp = new double[nComp];
        runComp = new double[nComp];
        outComp = new double[nComp];    
        
    }
    
   

   public void run() {
       
       if (this.Smax.getValue() > 0) {
           
        String Date = time.toString();
        System.out.println(Date.equals(Date2) );
        if ( (Date.equals(Date2) )) {
            this.Storage.setValue(Math.pow(10,9)*this.V0.getValue());
        }
        
        double test = this.inRD1.getValue();
        test = test + this.inRD2.getValue();
        test = test + this.inRG1.getValue();
        test = test + this.inRG2.getValue();
        double runOutflow = 0;
        double newS = 0;
               
       calcRelComponents(); 
        
        this.runComp[0] = this.inRD1.getValue();
        this.runComp[1] = this.inRD2.getValue();
        this.runComp[2] = this.inRG1.getValue();
        this.runComp[3] = this.inRG2.getValue();
        
        calcRelComponents();
        
           
    if(this.FO.getValue() >= 0){
        //Cas de restitution
        newS = Math.max(this.Storage.getValue() - 3600*24*this.FO.getValue(),0);
}    else  {
        //Cas de stockage
        // in case test < FO, we put FO = test
        if( (test+this.FO.getValue()) <0) { this.FO.setValue(test);}
        
        newS = Math.min(this.Storage.getValue() - 3600*24*this.FO.getValue(),Math.pow(10,9)*this.Smax.getValue());        
}
  
    // Calcul de la restitution réelle
    runOutflow = Math.max(0,test -(newS- this.Storage.getValue()));
    this.Storage.setValue(newS);   
      
       for(int i = 0; i < runComp.length; i++){
                outComp[i] = runOutflow * relComp[i];
                runComp[i] = runComp[i] - outComp[i];
            }
    this.inRD1.setValue(outComp[0]);  
    this.inRD2.setValue(outComp[1]);  
    this.inRG1.setValue(outComp[2]);  
    this.inRG2.setValue(outComp[3]);  
       
       }    else {
              this.Storage.setValue(0.0); 
       }
   }
    
      private void calcRelComponents(){
        currVolume = 0;
        for(int i = 0; i < nComp; i++){
            currVolume = currVolume + runComp[i];
        }
        for(int i = 0; i < nComp; i++){
            if(currVolume > 0)
                relComp[i] = runComp[i] / currVolume;
            else
                relComp[i] = 0;
        }
    }
}
