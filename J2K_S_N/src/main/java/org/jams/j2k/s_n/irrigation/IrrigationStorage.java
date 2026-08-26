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
author = "c6gohe2, c8fima",
description = "IrrigationStorage calculation of irrigation demand",
version = "1.1")
public class IrrigationStorage extends JAMSComponent {

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    description = "Current organic fertilizer amount")
    public Attribute.Integer RotPos;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    description = "Current hru object")
    public Attribute.EntityCollection entities;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    description = "in [-] plant growth water stress factor")
    public Attribute.Double wstrs;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    description = "HRU attribute maximum MPS")
    public Attribute.DoubleArray maxMPS;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    description = "HRU attribute maximum LPS")
    public Attribute.DoubleArray maxLPS;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READWRITE,
    description = "HRU state var saturation of MPS")
    public Attribute.DoubleArray satMPS;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READWRITE,
    description = "HRU state var saturation of LPS")
    public Attribute.DoubleArray satLPS;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.WRITE,
    description = "HRU crop class")
    public Attribute.Double irrigation;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READWRITE,
    description = "HRU crop class")
    public Attribute.Double irrigationsum;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    description = "Waterstress thershold post season 0-1 (-)")
    public Attribute.Double wst_thr_out;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    description = "Waterstress thershold 0-1 (-)")
    public Attribute.Double wst_thr;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    description = "Irrigation multiplier (-)")
    public Attribute.Double Irr_mult;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    description = "Start of the Irrigation season in day of the year (-)")
    public Attribute.Double Irr_Start;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    description = "End of the Irrigation season in day of the year (-)")
    public Attribute.Double Irr_End;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    description = "Exponent of the water use efficiency function (-)")
    public Attribute.Double Eff_exp;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    description = "reduction factor for dripper irrigation (-)")
    public Attribute.Double dripperfactor;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    description = "HRU crop class")
    public Attribute.Double cropid;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    description = "Current time")
    public Attribute.Calendar time;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    description = "Random intervall for the demandfactor (-)")
    public Attribute.Double rand_intervall;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.WRITE,
    description = "Test value for water use efficiency function (-)")
    public Attribute.Double Eff_test;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    description = "Bypass factor (average portion of bypass water from the irrigation water) (0 - 1) (-)",
    defaultValue = "0")
    public Attribute.Double Bypass_factor;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    description = "dynamic bypass range (for the calulation portion of bypass water from the irrigation water) (0 - 1) (-)",
    defaultValue = "0")
    public Attribute.Double Bypass_range;    
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.WRITE,
    description = "dynamic bypass value (-)")
    public Attribute.Double Bypass_dyn;    
    

    //Berechnung
    public void run() {
        Attribute.Entity entity = entities.getCurrent();
        double wst = 1 - wstrs.getValue();
        double satMPSArray[] = satMPS.getValue();
        double maxMPSArray[] = maxMPS.getValue();
        double satLPSArray[] = satLPS.getValue();
        double maxLPSArray[] = maxLPS.getValue();
        double wstthr = 0;


        int act = time.get(Calendar.DAY_OF_YEAR);

        /*
         * if (act < change.getValue()){ wstthr = wst_thr.getValue(); }else{
         * wstthr = wst_thr_post.getValue(); }
         */
        double irr_intervall = Irr_End.getValue() - Irr_Start.getValue();
        double irr_center = irr_intervall / 2 + Irr_Start.getValue();
        double exp = Eff_exp.getValue();
        double act_irr = act - Irr_Start.getValue();
        double run_by_fac = Bypass_factor.getValue();
        double by_range = Bypass_range.getValue();

        double a_irr = 0;
        double b_irr = 0;
        double half_exp = Math.pow(irr_intervall / 2, exp);
        
        double run_by_dyn = 0;

        if (act < Irr_Start.getValue()) {
            wstthr = wst_thr_out.getValue();
            run_by_dyn = (run_by_fac + (by_range/2));
        } else if (act < irr_center) {

            a_irr = (irr_intervall / 2) - act_irr;

            b_irr = (half_exp - Math.pow(a_irr, exp)) / half_exp;

            wstthr = ((wst_thr.getValue() - wst_thr_out.getValue()) * b_irr) + wst_thr_out.getValue();
            
            run_by_dyn = (run_by_fac + (by_range/2)) - (by_range * b_irr);

        } else if (act <= Irr_End.getValue()) {

            a_irr = act_irr - (irr_intervall / 2);

            b_irr = (half_exp - Math.pow(a_irr, exp)) / half_exp;

            wstthr = ((wst_thr.getValue() - wst_thr_out.getValue()) * b_irr) + wst_thr_out.getValue();
            
            run_by_dyn = (run_by_fac + (by_range/2)) - (by_range * b_irr);

        } else if (act > Irr_End.getValue()) {
            wstthr = wst_thr_out.getValue();
            run_by_dyn = (run_by_fac + (by_range/2));
        }

        run_by_dyn = Math.max(run_by_dyn, 0);
        run_by_dyn = Math.min(run_by_dyn, 1);
        
        Bypass_dyn.setValue(run_by_dyn);

        double Irr_mul = Irr_mult.getValue();
        double irrsum = irrigationsum.getValue();
        double irr = 0;
        double cropID = cropid.getValue();
        double red = 0;

        if (wstthr == 0) {
        } else {

            double random = (rand_intervall.getValue() * Math.random()) - (rand_intervall.getValue() / 2);



            wstthr = wstthr + random;
            if (wstthr < 0) {
                wstthr = 0;
            } else if (wstthr > wst_thr.getValue()) {
                wstthr = wst_thr.getValue();
            }




            if (cropID == 12) {
                irr = 0;
            } else {
                if (cropID == 98) {
                    irr = 0;
                } else {
                    if (wst < wstthr) {
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

                if (wst < wstthr) {
                    for (int i = 0; i < maxMPSArray.length; i++) {
                        irr += ((maxMPSArray[i] - (satMPSArray[i] * maxMPSArray[i])) * Irr_mul);
                        irr += ((maxLPSArray[i] - (satLPSArray[i] * maxLPSArray[i])) * Irr_mul);

                    }
                    irr = irr * dripperfactor.getValue();
                }
            }
        }
        irrsum = irrsum + irr;
        irrigation.setValue(irr);
        irrigationsum.setValue(irrsum);
        Eff_test.setValue(wstthr);
    }
}
