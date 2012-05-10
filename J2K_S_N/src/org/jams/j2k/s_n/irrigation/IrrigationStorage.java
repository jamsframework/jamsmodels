/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jams.j2k.s_n.irrigation;

import jams.data.*;
import jams.model.*;
import java.util.ArrayList;
import java.util.Calendar;
import org.jams.j2k.s_n.crop.J2KSNCrop;

/**
 *
 * @author c6gohe2
 */
@JAMSComponentDescription(title = "IrrigationStorage",
author = "c6gohe2",
description = "IrrigationStorage calculation of irrigation demand")
public class IrrigationStorage extends JAMSComponent {

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    update = JAMSVarDescription.UpdateType.RUN,
    description = "Current organic fertilizer amount")
    public JAMSInteger RotPos;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    update = JAMSVarDescription.UpdateType.RUN,
    description = "Current hru object")
    public JAMSEntityCollection entities;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    update = JAMSVarDescription.UpdateType.RUN,
    description = "in [-] plant growth water stress factor")
    public JAMSDouble wstrs;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    update = JAMSVarDescription.UpdateType.RUN,
    description = "HRU attribute maximum MPS")
    public JAMSDoubleArray maxMPS;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    update = JAMSVarDescription.UpdateType.RUN,
    description = "HRU attribute maximum LPS")
    public JAMSDoubleArray maxLPS;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READWRITE,
    update = JAMSVarDescription.UpdateType.RUN,
    description = "HRU state var saturation of MPS")
    public JAMSDoubleArray satMPS;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READWRITE,
    update = JAMSVarDescription.UpdateType.RUN,
    description = "HRU state var saturation of LPS")
    public JAMSDoubleArray satLPS;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.WRITE,
    update = JAMSVarDescription.UpdateType.RUN,
    description = "HRU crop class")
    public JAMSDouble irrigation;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READWRITE,
    update = JAMSVarDescription.UpdateType.RUN,
    description = "HRU crop class")
    public JAMSDouble irrigationsum;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    update = JAMSVarDescription.UpdateType.RUN,
    description = "Waterstress thershold post season 0-1 (-)")
    public JAMSDouble wst_thr_out;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    update = JAMSVarDescription.UpdateType.RUN,
    description = "Waterstress thershold 0-1 (-)")
    public JAMSDouble wst_thr;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    update = JAMSVarDescription.UpdateType.RUN,
    description = "Irrigation multiplier (-)")
    public JAMSDouble Irr_mult;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    update = JAMSVarDescription.UpdateType.RUN,
    description = "Start of the Irrigation season in day of the year (-)")
    public JAMSDouble Irr_Start;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    update = JAMSVarDescription.UpdateType.RUN,
    description = "End of the Irrigation season in day of the year (-)")
    public JAMSDouble Irr_End;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    update = JAMSVarDescription.UpdateType.RUN,
    description = "Exponent of the water use efficiency function (-)")
    public JAMSDouble Eff_exp;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    update = JAMSVarDescription.UpdateType.RUN,
    description = "reduction factor for dripper irrigation (-)")
    public JAMSDouble dripperfactor;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    update = JAMSVarDescription.UpdateType.RUN,
    description = "HRU crop class")
    public JAMSDouble cropid;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    update = JAMSVarDescription.UpdateType.RUN,
    description = "Current time")            
    public JAMSCalendar time;
    
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    update = JAMSVarDescription.UpdateType.RUN,
    description = "Random intervall for the demandfactor (-)")            
    public JAMSDouble rand_intervall;
    
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.WRITE,
    update = JAMSVarDescription.UpdateType.RUN,
    description = "Test value for water use efficiency function (-)")
    public JAMSDouble Eff_test;
    
    

    //Berechnung
    public void run() throws JAMSEntity.NoSuchAttributeException {
        Attribute.Entity entity = entities.getCurrent();
        double wst = 1 - wstrs.getValue();
        double satMPSArray[] = satMPS.getValue();
        double maxMPSArray[] = maxMPS.getValue();
        double satLPSArray[] = satLPS.getValue();
        double maxLPSArray[] = maxLPS.getValue();
        double wstthr = 0;
        
        
        int act = time.get(Calendar.DAY_OF_YEAR);
        
/*        if (act < change.getValue()){
             wstthr = wst_thr.getValue();
        }else{
             wstthr = wst_thr_post.getValue();   
        }*/
        double irr_intervall = Irr_End.getValue() - Irr_Start.getValue();
        double irr_center = irr_intervall / 2 +  Irr_Start.getValue();
        double exp =  Eff_exp.getValue();
        double act_irr = act - Irr_Start.getValue();
                
        double a_irr = 0;
        double b_irr = 0;
        double half_exp = Math.pow(irr_intervall/2,exp);
        
        if (act < Irr_Start.getValue()){
             wstthr = wst_thr_out.getValue();
        }else if (act < irr_center) {
             
            a_irr =  (irr_intervall/2) - act_irr; 
            
            b_irr = (half_exp - Math.pow(a_irr,exp)) / half_exp;
            
            wstthr = ((wst_thr.getValue() - wst_thr_out.getValue()) * b_irr) +  wst_thr_out.getValue();   
            
        }else if (act <= Irr_End.getValue()) {
            
            a_irr = act_irr - (irr_intervall/2); 
            
            b_irr = (half_exp - Math.pow(a_irr,exp)) / half_exp;
            
            wstthr = ((wst_thr.getValue() - wst_thr_out.getValue()) * b_irr) +  wst_thr_out.getValue();
            
        }else if (act > Irr_End.getValue()){
            wstthr = wst_thr_out.getValue();
        }
        
        double random = (rand_intervall.getValue() * Math.random()) - (rand_intervall.getValue()/2);
        
        wstthr = wstthr +  random;
        
        double Irr_mul = Irr_mult.getValue();
        double irrsum = irrigationsum.getValue();
        double irr = 0;
        double cropID = cropid.getValue();
        double red = 0;

        if (cropID == 12) {
            irr = 0;
        } else {
            if (cropID == 98) {
                irr = 0;
            } else {
                if (wst <= wstthr) {
                    for (int i = 0; i < maxMPSArray.length; i++) {
                        irr += ((maxMPSArray[i] - (satMPSArray[i] * maxMPSArray[i])) * Irr_mul);
                        irr += ((maxLPSArray[i] - (satLPSArray[i] * maxLPSArray[i])) * Irr_mul);
                    }

                } else {
                    irr = 0;
                }

            }
        }

        if (cropID == 8) {
            red = (1 - wstthr);
            wstthr = wstthr + ((red / 2));

            if (wst <= wstthr) {
                for (int i = 0; i < maxMPSArray.length; i++) {
                    irr += ((maxMPSArray[i] - (satMPSArray[i] * maxMPSArray[i])) * Irr_mul);
                    irr += ((maxLPSArray[i] - (satLPSArray[i] * maxLPSArray[i])) * Irr_mul);

                }
                irr = irr * dripperfactor.getValue();
            }
        }
        irrsum = irrsum + irr;
        irrigation.setValue(irr);
        irrigationsum.setValue(irrsum);
        Eff_test.setValue(wstthr);
    }
}
