package org.jams.j2k.s_n.erosion;

import jams.data.*;
import jams.model.*;
import java.util.Calendar;

@JAMSComponentDescription(title = "JAMSMusle_j2ks_holm",
description = "JAMS native Version of MusleMay",
author = "Holm + Manfred")
public class JAMSMusle_j2ks_holm extends JAMSComponent {

//Read access variables
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    update = JAMSVarDescription.UpdateType.RUN,
    description = "The current hru entity")
    public JAMSEntityCollection entities;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    update = JAMSVarDescription.UpdateType.RUN,
    description = "in cm depth of soil layer")
    public JAMSDoubleArray layerdepth;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    update = JAMSVarDescription.UpdateType.RUN,
    description = "")
    public JAMSDouble area;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    update = JAMSVarDescription.UpdateType.RUN,
    description = "")
    public JAMSDouble slope;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    update = JAMSVarDescription.UpdateType.RUN,
    description = "")
    public JAMSDouble Cfac;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    update = JAMSVarDescription.UpdateType.RUN,
    description = "")
    public JAMSDouble ROK;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    update = JAMSVarDescription.UpdateType.RUN,
    description = "")
    public JAMSDouble flowlength;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    update = JAMSVarDescription.UpdateType.RUN,
    description = "")
    public JAMSDouble Kfac;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    update = JAMSVarDescription.UpdateType.RUN,
    description = "Current time")
    public JAMSCalendar time;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    update = JAMSVarDescription.UpdateType.RUN,
    description = "HRU statevar RD1")
    public JAMSDouble outRD1;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    update = JAMSVarDescription.UpdateType.RUN,
    description = "snow depth")
    public JAMSDouble snowDepth;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    update = JAMSVarDescription.UpdateType.RUN,
    description = "precipitation mm")
    public JAMSDouble precip;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    update = JAMSVarDescription.UpdateType.RUN,
    description = "surface temperature")
    public JAMSDouble surfacetemp;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    update = JAMSVarDescription.UpdateType.RUN,
    description = "ID")
    public JAMSDouble ID;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    update = JAMSVarDescription.UpdateType.RUN,
    description = "Bypass factor for surface runoff sediment from upstream")
    public JAMSDouble Surrun_bypass;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READWRITE,
    update = JAMSVarDescription.UpdateType.RUN,
    description = "HRU statevar sediment inflow")
    public JAMSDouble insed;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READWRITE,
    update = JAMSVarDescription.UpdateType.RUN,
    description = "HRU statevar sediment outflow")
    public JAMSDouble sedpool;
//Write Access variables
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.WRITE,
    update = JAMSVarDescription.UpdateType.RUN,
    description = "HRU statevar sediment outflow")
    public JAMSDouble outsed;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.WRITE,
    update = JAMSVarDescription.UpdateType.RUN,
    description = "soil loss")
    public JAMSDouble gensed;

    public void run() throws JAMSEntity.NoSuchAttributeException {

        // passing reads into in's
        Double bysed = 0.0;
        Double area = this.area.getValue();
        Double ID = this.ID.getValue();
        Double slope = this.slope.getValue();
        Double Cfac = this.Cfac.getValue();
        Double ROK = this.ROK.getValue();
        Double slopelength = this.flowlength.getValue();
        Double Kfac = this.Kfac.getValue();

        Double outRD1 = this.outRD1.getValue();
        Double snowDepth = this.snowDepth.getValue();
        Double surfacetemp = this.surfacetemp.getValue();
        Double precip = this.precip.getValue();
        Double insed = this.insed.getValue();
        Double sedpool = this.sedpool.getValue();


        bysed = insed * (Surrun_bypass.getValue());
        insed = insed * (1 - Surrun_bypass.getValue());
        
        // calling the oms3 execute


        Double gensed = 0.0;


        if ((slope > 0) && (surfacetemp > 0.1) && (snowDepth == 0) && (outRD1 > 0)) {

            // slope-Umrechung von degree in %
            Double slopeperc = Math.tan(Math.toRadians(slope)) * 100;
            //System.out.println("ID: "+ ID  + " slope_deg : " + slope + " slope_perc: " + slopeperc);
            // slope Sfac = S-Factor fuer slope kleiner 9% und goesser gleich 9%
            Double Sfac = 0.0;
            if (slopeperc > 9) {
                Sfac = 16.8 * Math.sin(Math.toRadians(slope)) - 0.5;   // steil
            } else {
                Sfac = 10.8 * Math.sin(Math.toRadians(slope)) + 0.03; // flach
            }

            Double Lfacbeta = (Math.sin(Math.toRadians(slope)) / 0.0896)
                    / (3 * Math.pow(Math.sin(Math.toRadians(slope)), 0.8) + 0.56);
            Double Lfacm = Lfacbeta / (1 + Lfacbeta);
            Double Lfac = Math.pow(slopelength / 22.13, Lfacm);
            Double LSfac = Lfac * Sfac;
            Double Pvorl = 0.4 * 0.02 * slopeperc;
            Double HLkrit = 170 * Math.pow(Math.E, -0.13 * slopeperc);
            Double Pfac = slopelength < HLkrit ? Pvorl : 1;
            Double ROKF = Math.pow(Math.E, -0.053 * ROK);

            Double peaktime = 0.0; // hours
            if (time.get(Calendar.MONTH) > 4 & time.get(Calendar.MONTH) < 10) {
                peaktime = 4.0;               // Summer
            } else {
                peaktime = 14.0;              // Winter
            }

            if ((precip == 0.0) && (outRD1 > 0)) { // only snowmelting
                //    //System.out.println(" outRD1:" + outRD1/area + " und NS:" + precip + " PeakTime old: "+ peaktime);
                peaktime = 24.0;
            }

            Double area_ha = area / 10000; //m2 to ha
            Double area_km2 = area / 1000000; //m2 to km2
            Double Qsurf_peak_m3s = 2.08 * ((((outRD1 / area) / 10) * area_km2) / peaktime);// m3/s
            Double Qsurf_peak_mm_h = (outRD1 / area) / peaktime; // mm/h 

            //System.out.println("ID: "+ ID + " Qsurf_peak_m3:" + Qsurf_peak_m3 + " Qsurf_mm_ha:" + Qsurf_mm_ha + " und NS:" + precip + " PeakTime: "+ peaktime);

            Double X = 0.0;
            int equation = 0; // switch fuer Formel !!!!    0 ist MUSLE usw.

            if (equation == 0) {//MUSLE
                double Yield_MUSLE_total = 11.8 * Math.pow(((outRD1 / area) * Qsurf_peak_m3s * 1000 * area_km2), 0.56); // SWAT-MULSE Williams, 1995
                X = Yield_MUSLE_total;           // MUSLE metric tonns 
                //------------------------------------------------------------------------------------------------------------------
            }
            if (equation == 1) {//MUST
                double Yield_MUST = 2.5 * Math.pow((outRD1 / area) * Qsurf_peak_mm_h, 0.5); // metric tonns ha-1
                X = Yield_MUST;       // MUST metric tonns ha-1
                //------------------------------------------------------------------------------------------------------------------
            }
            if (equation == 2) {//MUSS
                double Yield_MUSS = 0.79 * Math.pow((outRD1 / area) * Qsurf_peak_mm_h, 0.65) * Math.pow(area_ha, 0.009);  //metric tonns ha-1
                X = Yield_MUSS;       // MUSS metric tonns ha-1
                //------------------------------------------------------------------------------------------------------------------
            }
            if (equation == 3) {//MUSI
                double musi_co1 = 0.5; // MUSI coefficients sind Nutzer frei !!! waehlbar; auto Calibration 0.0001 - 3.0
                double musi_co2 = 0.5; // auto Calibration 0.0001 - 1.0
                double musi_co3 = 0.5; // auto Calibration 0.0001 - 1.0
                double musi_co4 = 0.5; // auto Calibration 0.0001 - 1.0
                double Yield_MUSI = musi_co1 * Math.pow((outRD1 / area), musi_co2) * Math.pow(Qsurf_peak_mm_h, musi_co3) * Math.pow(area_ha, musi_co4); // metric tonns ha-1
                X = Yield_MUSI;       // MUSI metric tonns ha-1
                //--------------------------------------------------------------------------------------------------------------------
            }
            
            // !!!!
            // ich pruefe noch ob Biomasse auf dem Feld ist; sonst Cfac fuer Fallow
            // if (hru.BioAct < 0.1 && hru.landuseID == 7) {
            //    Cfac = 0.4;
            //}
            // !!!!
            
            
            double Sed_Yield = X * Kfac * LSfac * Pfac * Cfac * ROKF;

            if (equation > 0) {
                Sed_Yield = Sed_Yield * area_ha; // Umrechnung in total tonns Sediment/HRU
            }

            gensed = Sed_Yield * 1000; // tonns/hru   to      kg/hru

        }


        double out = 0;
        double bal = gensed - insed;
        double neuaccpool = sedpool - bal;

        if (neuaccpool < 0) {
            out = (-1) * neuaccpool;
            neuaccpool = 0; // sediment pool
        } else {
            if (bal < 0) {
                double acc = (-1) * bal;
                neuaccpool = sedpool + acc;
                if (outRD1 > 0) {
                    out = 0.05 * acc;
                }
            }
        }

        //if (out > 0) {
        //   System.out.println(" ID " + ID + " Pool: " + neuaccpool + " gen: " + gensed + " in: " + insed + " out: " + out);
        //}

        sedpool = neuaccpool;

        out = out + bysed;



        // reading the outs
        this.insed.setValue(0);
        this.sedpool.setValue(sedpool);
        this.outsed.setValue(out);
        this.gensed.setValue(gensed);
    }
}
