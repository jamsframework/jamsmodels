package Draft;

import jams.data.*;
import jams.model.*;
import java.util.Calendar;

@JAMSComponentDescription(title = "JAMSMusle_j2ks",
description = "JAMS native Version of MusleMay Adpated for hourly models",
author = "Holm + Manfred + VT & JB")
public class JAMSMusle_j2ks_hourly extends JAMSComponent {

//Read access variables
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    update = JAMSVarDescription.UpdateType.RUN,
    description = "The current hru entity")
    public JAMSEntityCollection entities;
   
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
    description = "HRU statevar RD1")
    public JAMSDouble outRD1;
    
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    update = JAMSVarDescription.UpdateType.RUN,
    description = "snow depth")
    public JAMSDouble snowDepth;
    
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    update = JAMSVarDescription.UpdateType.RUN,
    description = "surface temperature")
    public JAMSDouble surfacetemp;
    
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    update = JAMSVarDescription.UpdateType.RUN,
    description = "ID")
    public JAMSDouble ID;
    
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
    
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    update = JAMSVarDescription.UpdateType.RUN,
    description = "additional P-Factor, for scenario building")
    public JAMSDouble p_managm;
    
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    update = JAMSVarDescription.UpdateType.RUN,
    description = "HRU irrigation Waterinput  [l], Default = 0 ")
    public JAMSDouble irrigation_act;

    public void run() throws JAMSEntity.NoSuchAttributeException {

        // passing reads into in's
        Double ID = this.ID.getValue();
        Double slope = this.slope.getValue();
        Double Cfac = this.Cfac.getValue();
        Double slopelength = this.flowlength.getValue();
        Double Kfac = this.Kfac.getValue();

        Double outRD1 = this.outRD1.getValue();
        Double snowDepth = this.snowDepth.getValue();
        Double surfacetemp = this.surfacetemp.getValue();
        Double insed = this.insed.getValue();
        Double sedpool = this.sedpool.getValue();

      
        Double gensed = 0.0;
        

        if ((slope > 0) && (surfacetemp > 0.1) && (snowDepth == 0) && (outRD1 > 0)) {

            // converting slope in %
            double slopeperc = Math.tan(Math.toRadians(slope)) * 100;
            //System.out.println("ID: "+ ID  + " slope_deg : " + slope + " slope_perc: " + slopeperc);
            // calculation of S factor
            double Sfac = 0;
            if (slopeperc >= 9) {
                Sfac = 16.8 * Math.sin(Math.toRadians(slope)) - 0.5;   // steil
            } else {
                Sfac = 10.8 * Math.sin(Math.toRadians(slope)) + 0.03; // flach
            }

             // caculation of L factor
            double Lfacbeta = (Math.sin(Math.toRadians(slope)) / 0.0896)
                    / (3 * Math.pow(Math.sin(Math.toRadians(slope)), 0.8) + 0.56);
                        
            double Lfacm = Lfacbeta / (1 + Lfacbeta);
            double Lfac = Math.pow(slopelength / 22.13, Lfacm);
            double LSfac = Lfac * Sfac;
            
            
            // no irrigation in J2KP
            
            // in J2KP model, not used
            //double Pvorl = 0.4 * 0.02 * slopeperc;
            //double HLkrit = 170 * Math.pow(Math.E, -0.13 * slopeperc);
            
            // in J2KP model, not used
            //double Pfac = slopelength < HLkrit ? Pvorl : 1;
            
            // in J2KP model, not used
            // double ROKF = Math.pow(Math.E, -0.053 * ROK);
            
            // in J2KP model, not used
            // double p_mgt = 1;
            //if (p_managm != null){
            //    p_mgt = p_managm.getValue();
            //}
            //method used for paddy rice fields (ponded water protects from erosion)
            //if (irri_act > 0){
            //    p_mgt = 0;
            //}
            
            
      
            // lines below not used for hourly models 
            
            //if (time.get(Calendar.MONTH) > 4 & time.get(Calendar.MONTH) < 10) {
            //    peaktime = 4;               // Summer
            //} else {
            //    peaktime = 14;              // Winter
            //}
            //if ((precip == 0.0) && (outRD1 > 0)) { // only snowmelting
            //    //    //System.out.println(" outRD1:" + outRD1/area + " und NS:" + precip + " PeakTime old: "+ peaktime);
            //    peaktime = 24;
            //}

            
            double Qsurf_peak_m3 = outRD1 / 1000; // m3
                       
            double Qsurf_m3_s = (outRD1 / 1000 / 3600); //m3/s

            double Lamb = 11.8 * Math.pow((Qsurf_m3_s * Qsurf_peak_m3 ), 0.56); // MUSLE
            double sedperinto = Lamb * Kfac * LSfac * Cfac;  // MUSLE
            gensed = sedperinto; // t / hru

        }


        double out = 0;
        double bal = (gensed ) - insed;
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
                    neuaccpool = neuaccpool - out;
                    
                }
            }
        }

        //if (out > 0) {
        //   System.out.println(" ID " + ID + " Pool: " + neuaccpool + " gen: " + gensed + " in: " + insed + " out: " + out);
        //}

        sedpool = neuaccpool;



        // reading the outs
        this.insed.setValue(0);
        this.sedpool.setValue(sedpool);
        this.outsed.setValue(out);
        this.gensed.setValue(gensed);
    }
}
