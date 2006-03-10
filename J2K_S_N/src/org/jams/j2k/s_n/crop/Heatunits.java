/*
 * Heatunits.java
 *
 * Created on 19. Januar 2006, 10:41
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 /*
 *
 * @author Ulrike Bende-Michl
 *
 */
package org.jams.j2k.s_n.crop;

import org.unijena.jams.data.*;
import org.unijena.jams.model.*;
import java.io.*;

 public class Heatunits extends JAMSComponent {
     
 @JAMSComponentDescription(
        title="j2k_HeatUnits",
        author="Ulrike Bende-Michl",
        description="Module for calculation heat units"
        )
     /*
     *  Component variables & description
     */
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "The current hru entity"
            )
            public JAMSEntity entity;
 
 
     @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "start date of growing season"
            )
            public JAMSDouble GRWstart;
     
      @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "end date of growing season"
            )
            public JAMSDouble GRWend;

     @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.INIT,
            description = "HRU daily mean temperature [°C]"
            )
            public JAMSDouble Tmean;
     
      @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.INIT,
            description = "Plants base temperature required for growing[°C]"
            )
            public JAMSDouble Tbase;
    
      @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.INIT,
            description = "Plants heat units sum to reach maturity [-]"
            )
            public JAMSDouble PHU;
      
        @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            update = JAMSVarDescription.UpdateType.INIT,
            description = "Daily plants heat units sum [-]"
            )
            public JAMSDouble PHUact;
    
       @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            update = JAMSVarDescription.UpdateType.INIT,
            description = "HRU fraction daily potential heat units"
            )
            public JAMSDouble FPHUact;
       
           @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            update = JAMSVarDescription.UpdateType.INIT,
            description = "HRU half of annual potential heat units"
            )
            public JAMSDouble PHU_50;
           
                 
    /*
     *  Component run stages
     */  
           
     private double phu_abs;
     private double phu_act;
     private double fphu_act;
     private double phu_50;
     private double phu_year;
     
     private double startdate;
     private double enddate;
     
     private double tmean;
     private double tbase;
     
    public void init() throws JAMSEntity.NoSuchAttributeException, IOException {
        
            }
    
    public void run() throws JAMSEntity.NoSuchAttributeException{
        
        this.phu_abs = PHU.getValue(); /* absolut plant specific heat units sum needed */
        this.tmean = Tmean.getValue();
        this.tbase = Tbase.getValue();
        
        calc_dailyHU(); 
        
        PHUact.setValue(phu_act);
        FPHUact.setValue(fphu_act);
        PHU_50.setValue(phu_50);
             
 }
     private boolean calc_dailyHU() throws JAMSEntity.NoSuchAttributeException {
         /*@todo wenn Wintergetreide dann startdate auf enddate gesetzt */
         if 
                (this.startdate >=  this.enddate){
                double phu_delta = this.tmean - this.tbase;
                phu_act = phu_delta + (this.tmean - this.tbase);
                }
                
                fphu_act = phu_act / this.phu_abs;
       
        if  
                        (this.startdate <= this.enddate){
                         phu_act = phu_year; 
                         phu_50 = phu_year / 2 ;
        
        }
       
              
         return true;
     }
 }
 