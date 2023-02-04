
package Draft;

import jams.JAMS;
import jams.data.*;
import jams.model.*;

/**
 *
 * @author c0krpe
 */
@JAMSComponentDescription(
        title = "ReachRouting_Tracking",
        author = "Peter Krause, Olivier Grandjouan",
        description = "Calculates flow processes in the river network by a simplified kinematic wave approach, and track volume contribution from a specific reach",
        version = "Tracking",
        date = "2022-01-24"
)
@VersionComments(entries = {
    @VersionComments.Entry(version = "Tracking", comment = "Original J2KProcessReachRouting module"
            + "modified with tracking variables in order to save and transfer the volume"
            + "coming from a specific reach ant its contribution to the output.")
})

public class J2KProcessReachRouting_Tracking_V2 extends JAMSComponent {

    /*
     *  Component variables
     */
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "The reach collection"
    )
    public Attribute.EntityCollection entities;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "reach length",
            unit = "m"
    )
    public Attribute.Double length;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "reach slope",
            unit = "%"
    )
    public Attribute.Double slope;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Is slope provided as proportion of length and elevation difference [m/m]?",
            defaultValue = "false"
    )
    public Attribute.Boolean slopeAsProportion;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "If true, slopes of all reaches will be checked if they are compliant to the value of the \"slopeAsProportion\" parameter",
            defaultValue = "true"
    )
    public Attribute.Boolean checkSlopes;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "reach width",
            unit = "m"
    )
    public Attribute.Double width;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "reach roughness"
    )
    public Attribute.Double roughness;

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
            description = "additional inflow to reach",
            unit = "L",
            defaultValue = "0"
    )
    public Attribute.Double inAddIn;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "RD1 outflow from reach",
            unit = "L"
    )
    public Attribute.Double outRD1;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "RD2 outflow from reach",
            unit = "L"
    )
    public Attribute.Double outRD2;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "RG1 outflow from reach",
            unit = "L"
    )
    public Attribute.Double outRG1;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "RG2 outflow from reach",
            unit = "L"
    )
    public Attribute.Double outRG2;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "additional outflow from reach",
            unit = "L",
            defaultValue = "0"
    )
    public Attribute.Double outAddIn;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "simulated runoff from reach",
            unit = "L"
    )
    public Attribute.Double simRunoff;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "RD1 storage inside reach",
            unit = "L"
    )
    public Attribute.Double actRD1;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "RD2 storage inside reach",
            unit = "L"
    )
    public Attribute.Double actRD2;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "RG1 storage inside reach",
            unit = "L"
    )
    public Attribute.Double actRG1;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "RG2 storage inside reach",
            unit = "L"
    )
    public Attribute.Double actRG2;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "additional inflow storage inside reach",
            unit = "L",
            defaultValue = "0"
    )
    public Attribute.Double actAddIn;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "Channel storage inside reach",
            unit = "L"
    )
    public Attribute.Double channelStorage;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "flow routing coefficient TA",
            lowerBound = 0.0,
            upperBound = 50.0,
            defaultValue = "1.0"
    )
    public Attribute.Double flowRouteTA;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "Catchment outlet RD1 storage",
            unit = "L"
    )
    public Attribute.Double catchmentRD1;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "Catchment outlet RD2 storage",
            unit = "L"
    )
    public Attribute.Double catchmentRD2;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "Catchment outlet RG1 storage",
            unit = "L"
    )
    public Attribute.Double catchmentRG1;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "Catchment outlet RG2 storage",
            unit = "L"
    )
    public Attribute.Double catchmentRG2;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "Catchment additional input outlet storage",
            unit = "L",
            defaultValue = "0"
    )
    public Attribute.Double catchmentAddIn;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "Catchment outlet RG2 storage",
            unit = "L"
    )
    public Attribute.Double catchmentSimRunoff;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "temporal resolution [d or h]"
    )
    public Attribute.String tempRes;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "water level in reach"
    )
    public Attribute.Double waterLevel;
    
    /* New attributes for tracking */
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "RD1 contribution from tracked reach",
            unit = "L",
            defaultValue = "0"
    )
    public Attribute.Double TrackedVolumeRD1;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "RD2 contribution from tracked reach",
            unit = "L",
            defaultValue = "0"
    )
    public Attribute.Double TrackedVolumeRD2;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "RG1 contribution from tracked reach",
            unit = "L",
            defaultValue = "0"
    )
    public Attribute.Double TrackedVolumeRG1;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "RG2 contribution from tracked reach",
            unit = "L",
            defaultValue = "0"
    )
    public Attribute.Double TrackedVolumeRG2;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "contribution from tracked reach in total simulated runoff",
            unit = "L",
            defaultValue = "0"
    )
    public Attribute.Double TrackedVolumeSimRunoff;
    
@JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "RD1 volume from tracked Reach in actual reach"
    )
    public Attribute.Double TrackedVolumeRD1_act;
   
   @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "RD2 volume from tracked Reach in actual reach"
    )
    public Attribute.Double TrackedVolumeRD2_act;
   
   @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "RG1 volume from tracked Reach in actual reach"
    )
    public Attribute.Double TrackedVolumeRG1_act;
   
   @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "RG2 volume from tracked Reach in actual reach"
    )
    public Attribute.Double TrackedVolumeRG2_act;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Tracked reach"
    )
    public Attribute.Integer TrackedReach;
    /*
     *  Component run stages
     */
    int count = 0;
    double avg = 0;
    int slopefactor;

    public void init() {

        if (slopeAsProportion.getValue()) {
            slopefactor = 100;
        } else {
            slopefactor = 1;
        }
    }

    public void initAll() {

        if (checkSlopes.getValue()) {

            avg = (avg * count + slopefactor * slope.getValue()) / ++count;

            if (avg >= 100) {
                // getModel().getRuntime().sendHalt("Average reach slope exceeds 100%. please check your reach parameter file and \"slopeAsProportion\" parameter value!");
            }
            if (avg <= 0.1) {
                // getModel().getRuntime().sendHalt("Average reach slope is below 0.1%. please check your reach parameter file and \"slopeAsProportion\" parameter value!");
            }
        }
        
        if (this.slope.getValue() == 0) {
            // getModel().getRuntime().println("WARNING: Found zero slope in reach entity which will prevent water routing!", JAMS.VERBOSE);
        }        
    }

    public void run() {

        
        Attribute.Entity entity = entities.getCurrent();

        Attribute.Entity DestReach = (Attribute.Entity) entity.getObject("to_reach");
        if (DestReach.isEmpty()) {
            DestReach = null;
        }
        Attribute.Entity DestReservoir = null;

        if (entity.existsAttribute("to_reservoir")) {
            DestReservoir = (Attribute.Entity) entity.getObject("to_reservoir");
        } else {
            DestReservoir = null;
        }

        double width = this.width.getValue();
        double rough = this.roughness.getValue();
        double length = this.length.getValue();

        double slope = this.slope.getValue();
        if (!slopeAsProportion.getValue()) {
            slope = slope / 100;
        }

        double RD1act = actRD1.getValue() + inRD1.getValue();
        double RD2act = actRD2.getValue() + inRD2.getValue();
        double RG1act = actRG1.getValue() + inRG1.getValue();
        double RG2act = actRG2.getValue() + inRG2.getValue();
              
        double addInAct = actAddIn.getValue() + this.inAddIn.getValue();
        
        /* NEW
        Saving actual storage for Tracking
        */
        double actRD1Temp = RD1act;
        double actRD2Temp = RD2act;
        double actRG1Temp = RG1act;
        double actRG2Temp = RG2act;
        double totalactTemp = actRD1Temp + actRD2Temp + actRG1Temp + actRG2Temp + addInAct;


        inRD1.setValue(0);
        inRD2.setValue(0);
        inRG1.setValue(0);
        inRG2.setValue(0);

        inAddIn.setValue(0);

        /* On fait _a plus tard
        actRD1.setValue(0);
        actRD2.setValue(0);
        actRG1.setValue(0);
        actRG2.setValue(0); */

        actAddIn.setValue(0);

        double RD1DestIn = 0;
        double RD2DestIn = 0;
        double RG1DestIn = 0;
        double RG2DestIn = 0;
        double addInDestIn = 0;

        if (DestReach == null && DestReservoir == null) {
            RD1DestIn = 0;//entity.getDouble(aNameCatchmentOutRD1.getValue());
            RD2DestIn = 0;//entity.getDouble(aNameCatchmentOutRD2.getValue());
            RG1DestIn = 0;//entity.getDouble(aNameCatchmentOutRG1.getValue());
            RG2DestIn = 0;//entity.getDouble(aNameCatchmentOutRG2.getValue());

            addInDestIn = 0;
        } else if (DestReservoir != null) {
            RD1DestIn = DestReservoir.getDouble("compRD1");
            RD2DestIn = DestReservoir.getDouble("compRD2");
            RG1DestIn = DestReservoir.getDouble("compRG1");
            RG2DestIn = DestReservoir.getDouble("compRG2");
        } else {
            RD1DestIn = DestReach.getDouble("inRD1");
            RD2DestIn = DestReach.getDouble("inRD2");
            RG1DestIn = DestReach.getDouble("inRG1");
            RG2DestIn = DestReach.getDouble("inRG2");

            try {
                addInDestIn = DestReach.getDouble("inAddIn");
            } catch (jams.data.Attribute.Entity.NoSuchAttributeException e) {
                addInDestIn = 0;
            }
        }

        double q_act_tot = RD1act + RD2act + RG1act + RG2act + addInAct;
                
        
        int ID = (int)entity.getDouble("ID");
            
        int DestID = 0;
        if(DestReach != null){
            DestID = (int)DestReach.getDouble("ID");
        }
        
//        // getModel().getRuntime().println("Processing reach: " + ID);

        
        if (q_act_tot == 0) {
            outRD1.setValue(0);
            outRD2.setValue(0);
            outRG1.setValue(0);
            outRG2.setValue(0);

            this.outAddIn.setValue(0);

            //nothing more to do here
            return;
            
        }
        
        //relative parts of the runoff components for later redistribution
        double RD1_part = RD1act / q_act_tot;
        double RD2_part = RD2act / q_act_tot;
        double RG1_part = RG1act / q_act_tot;
        double RG2_part = RG2act / q_act_tot;
   
        double addInPart = addInAct / q_act_tot;

        //calculation of flow velocity
        int sec_inTStep = 0;
        if (this.tempRes.getValue().equals("d")) {
            sec_inTStep = 86400;
        } else if (this.tempRes.getValue().equals("h")) {
            sec_inTStep = 3600;
        }
        double flow_veloc = this.calcFlowVelocity(q_act_tot, width, slope, rough, sec_inTStep);

        //recession coefficient
        double Rk = (flow_veloc / length) * this.flowRouteTA.getValue() * 3600;

        //the whole outflow
        double q_act_out;
        if (Rk > 0) {
            q_act_out = q_act_tot * Math.exp(-1 / Rk);
        } else {
            q_act_out = 0;
        }

        //the actual outflow from the reach
        double RD1out = q_act_out * RD1_part;
        double RD2out = q_act_out * RD2_part;
        double RG1out = q_act_out * RG1_part;
        double RG2out = q_act_out * RG2_part;
        
       

        double addInOut = q_act_out * addInPart;

        //transferring runoff from this reach to the next one or a reservoir
        RD1DestIn = RD1DestIn + RD1out;
        RD2DestIn = RD2DestIn + RD2out;
        RG1DestIn = RG1DestIn + RG1out;
        RG2DestIn = RG2DestIn + RG2out;

        addInDestIn = addInDestIn + addInOut;

        //reducing the actual storages
        RD1act = RD1act - q_act_out * RD1_part;
        RD2act = RD2act - q_act_out * RD2_part;
        RG1act = RG1act - q_act_out * RG1_part;
        RG2act = RG2act - q_act_out * RG2_part;

        addInAct = addInAct - q_act_out * addInPart;

        double channelStorage = RD1act + RD2act + RG1act + RG2act + addInAct;

        double cumOutflow = RD1out + RD2out + RG1out + RG2out + addInOut;
        /*if (reachID.getValue()==800)
        {System.out.println(RD1out);
        System.out.println(RD2out);
        System.out.println(RG1out);
        System.out.println(RG2out);
        }
         */
        
        simRunoff.setValue(cumOutflow);
        this.channelStorage.setValue(channelStorage);
        /* On fait ça plus tard
        inRD1.setValue(0);
        inRD2.setValue(0);
        inRG1.setValue(0);
        inRG2.setValue(0); */

        inAddIn.setValue(0);

        actRD1.setValue(RD1act);
        actRD2.setValue(RD2act);
        actRG1.setValue(RG1act);
        actRG2.setValue(RG2act);

        actAddIn.setValue(addInAct);

        outRD1.setValue(RD1out);
        outRD2.setValue(RD2out);
        outRG1.setValue(RG1out);
        outRG2.setValue(RG2out);

        outAddIn.setValue(addInOut);
        double verzoegerung;
        
        // AJOUT
     
        //int ID = (int)entity.getDouble("ID");
        int ReachTracked = this.TrackedReach.getValue();
                
        // getModel().getRuntime().println("Processing Reach " + ID);

        // Save volume and transferring it if tracked reach
        if (ReachTracked == ID){
        // getModel().getRuntime().println(ID + " est bien le Reach tracé !");
            
        double RD1TrackedVolume = RD1out;
        double RD2TrackedVolume = RD2out;
        double RG1TrackedVolume = RG1out;
        double RG2TrackedVolume = RG2out;
        double SimRunoffTrackedVolume = cumOutflow;
            
        // getModel().getRuntime().println("C'est bien le Reach tracé. Volume sauvegardé et transmis au reach " + DestID);
        // getModel().getRuntime().println("Le volume sauvegardé et transferé est : " + SimRunoffTrackedVolume);


        double destReachRD1TrackedVolume =   RD1TrackedVolume;         
        double destReachRD2TrackedVolume =   RD2TrackedVolume;         
        double destReachRG1TrackedVolume =   RG1TrackedVolume;         
        double destReachRG2TrackedVolume =   RG2TrackedVolume;         
        double destReachSimRunoffTrackedVolume =   SimRunoffTrackedVolume;  

        DestReach.setDouble("TrackedVolumeRD1", destReachRD1TrackedVolume);
        DestReach.setDouble("TrackedVolumeRD2", destReachRD2TrackedVolume);
        DestReach.setDouble("TrackedVolumeRG1", destReachRG1TrackedVolume);
        DestReach.setDouble("TrackedVolumeRG2", destReachRG2TrackedVolume);
        DestReach.setDouble("TrackedVolumeSimRunoff", destReachSimRunoffTrackedVolume);

        TrackedVolumeRD1.setValue(RD1TrackedVolume);
        TrackedVolumeRD2.setValue(RD2TrackedVolume);
        TrackedVolumeRG1.setValue(RG1TrackedVolume);
        TrackedVolumeRG2.setValue(RG2TrackedVolume);
        TrackedVolumeSimRunoff.setValue(SimRunoffTrackedVolume);
              
            
        } else if (this.TrackedVolumeSimRunoff.getValue() != 0  && DestReach != null){            /* Calculation and transfer of tracked volume*/
            
        // getModel().getRuntime().println("Transfert de l'écoulement à " + DestID);
                      

        double RD1TrackedVolume_old = this.TrackedVolumeRD1.getValue();
        double RD2TrackedVolume_old = this.TrackedVolumeRD2.getValue();
        double RG1TrackedVolume_old = this.TrackedVolumeRG1.getValue();
        double RG2TrackedVolume_old = this.TrackedVolumeRG2.getValue();
        double SimRunoffTrackedVolume_old = this.TrackedVolumeSimRunoff.getValue();
        
        // getModel().getRuntime().println("Le volume du Reach 2600 entrant dans le Reach " + ID + " est " + SimRunoffTrackedVolume_old);

        double RD1TrackedVolume_act_old = this.TrackedVolumeRD1_act.getValue();
        double RD2TrackedVolume_act_old = this.TrackedVolumeRD2_act.getValue();
        double RG1TrackedVolume_act_old = this.TrackedVolumeRG1_act.getValue();
        double RG2TrackedVolume_act_old = this.TrackedVolumeRG2_act.getValue();
        double totalTrackedVolume_act_old = RD1TrackedVolume_act_old + RD2TrackedVolume_act_old + RG1TrackedVolume_act_old + RG2TrackedVolume_act_old;

        // getModel().getRuntime().println("Le volume du Reach 2600 déjà présent dans le Reach " + ID + " est " + totalTrackedVolume_act_old);

        double RD1TrackedVolume_act_new = RD1TrackedVolume_old + RD1TrackedVolume_act_old;
        double RD2TrackedVolume_act_new = RD2TrackedVolume_old + RD2TrackedVolume_act_old;
        double RG1TrackedVolume_act_new = RG1TrackedVolume_old + RG1TrackedVolume_act_old;
        double RG2TrackedVolume_act_new = RG2TrackedVolume_old + RG2TrackedVolume_act_old;
        double totalTrackedVolume_act_new = SimRunoffTrackedVolume_old + totalTrackedVolume_act_old;
            
        // getModel().getRuntime().println("Le nouveau volume du Reach 2600 présent dans le Reach " + ID + " est " + totalTrackedVolume_act_new);
        // getModel().getRuntime().println("Le volume total présent dans le Reach " + ID + " est " + totalactTemp);
        // getModel().getRuntime().println("Le volume total sortant du Reach " + ID + " est " + cumOutflow);
   
        double RD1TrackedVolume_new = (RD1TrackedVolume_act_new*RD1out)/actRD1Temp;
        double RD2TrackedVolume_new = (RD2TrackedVolume_act_new*RD2out)/actRD2Temp;
        double RG1TrackedVolume_new = (RG1TrackedVolume_act_new*RG1out)/actRG1Temp;
        double RG2TrackedVolume_new = (RG2TrackedVolume_act_new*RG2out)/actRG2Temp;
        double SimRunoffTrackedVolume_new = (totalTrackedVolume_act_new*cumOutflow)/totalactTemp;
             
            if(actRD1Temp == 0){
                RD1TrackedVolume_new = 0;
            }
            if(actRD2Temp == 0){
                RD2TrackedVolume_new = 0;
            }
            if(actRG1Temp == 0){
                RG1TrackedVolume_new = 0;
            }
            if(actRG2Temp == 0){
                RG2TrackedVolume_new = 0;
            }
            if(totalactTemp == 0){
                SimRunoffTrackedVolume_new = 0;
            }
            
            // getModel().getRuntime().println("Le volume du Reach 2600 sortant du Reach " + ID + " est " + SimRunoffTrackedVolume_new);

            double actTrackedVolumeRD1 = RD1TrackedVolume_act_new - RD1TrackedVolume_new;
            double actTrackedVolumeRD2 = RD2TrackedVolume_act_new - RD2TrackedVolume_new;
            double actTrackedVolumeRG1 = RG1TrackedVolume_act_new - RG1TrackedVolume_new;
            double actTrackedVolumeRG2 = RG2TrackedVolume_act_new - RG2TrackedVolume_new;
            double totalactTrackedVolume = actTrackedVolumeRD1 + actTrackedVolumeRD2 + actTrackedVolumeRG1 + actTrackedVolumeRG2;
           
            // getModel().getRuntime().println("Le volume du Reach 2600 restant dans le Reach " + ID + " est " + totalactTrackedVolume);

            
            double destReachRD1TrackedVolume =   RD1TrackedVolume_new;         
            double destReachRD2TrackedVolume =   RD2TrackedVolume_new;         
            double destReachRG1TrackedVolume =   RG1TrackedVolume_new;         
            double destReachRG2TrackedVolume =   RG2TrackedVolume_new;         
            double destReachSimRunoffTrackedVolume =   SimRunoffTrackedVolume_new;          

            DestReach.setDouble("TrackedVolumeRD1", destReachRD1TrackedVolume);
            DestReach.setDouble("TrackedVolumeRD2", destReachRD2TrackedVolume);
            DestReach.setDouble("TrackedVolumeRG1", destReachRG1TrackedVolume);
            DestReach.setDouble("TrackedVolumeRG2", destReachRG2TrackedVolume);
            DestReach.setDouble("TrackedVolumeSimRunoff", destReachSimRunoffTrackedVolume);

            TrackedVolumeRD1.setValue(RD1TrackedVolume_new);
            TrackedVolumeRD2.setValue(RD2TrackedVolume_new);
            TrackedVolumeRG1.setValue(RG1TrackedVolume_new);
            TrackedVolumeRG2.setValue(RG2TrackedVolume_new);
            TrackedVolumeSimRunoff.setValue(SimRunoffTrackedVolume_new);

            TrackedVolumeRD1_act.setValue(actTrackedVolumeRD1);
            TrackedVolumeRD2_act.setValue(actTrackedVolumeRD2);
            TrackedVolumeRG1_act.setValue(actTrackedVolumeRG1);
            TrackedVolumeRG2_act.setValue(actTrackedVolumeRG2);

        } else if(DestReach == null){
        
         
        // getModel().getRuntime().println("EXUTOIRE");

        double RD1TrackedVolume_old = this.TrackedVolumeRD1.getValue();
        double RD2TrackedVolume_old = this.TrackedVolumeRD2.getValue();
        double RG1TrackedVolume_old = this.TrackedVolumeRG1.getValue();
        double RG2TrackedVolume_old = this.TrackedVolumeRG2.getValue();
        double SimRunoffTrackedVolume_old = this.TrackedVolumeSimRunoff.getValue();
        
        // getModel().getRuntime().println("Le volume du Reach 2600 entrant dans le Reach " + ID + " est " + SimRunoffTrackedVolume_old);

        double RD1TrackedVolume_act_old = this.TrackedVolumeRD1_act.getValue();
        double RD2TrackedVolume_act_old = this.TrackedVolumeRD2_act.getValue();
        double RG1TrackedVolume_act_old = this.TrackedVolumeRG1_act.getValue();
        double RG2TrackedVolume_act_old = this.TrackedVolumeRG2_act.getValue();
        double totalTrackedVolume_act_old = RD1TrackedVolume_act_old + RD2TrackedVolume_act_old + RG1TrackedVolume_act_old + RG2TrackedVolume_act_old;

        // getModel().getRuntime().println("Le volume du Reach 2600 déjà présent dans le Reach " + ID + " est " + totalTrackedVolume_act_old);

        double RD1TrackedVolume_act_new = RD1TrackedVolume_old + RD1TrackedVolume_act_old;
        double RD2TrackedVolume_act_new = RD2TrackedVolume_old + RD2TrackedVolume_act_old;
        double RG1TrackedVolume_act_new = RG1TrackedVolume_old + RG1TrackedVolume_act_old;
        double RG2TrackedVolume_act_new = RG2TrackedVolume_old + RG2TrackedVolume_act_old;
        double totalTrackedVolume_act_new = SimRunoffTrackedVolume_old + totalTrackedVolume_act_old;
            
        // getModel().getRuntime().println("Le nouveau volume du Reach 2600 présent dans le Reach " + ID + " est " + totalTrackedVolume_act_new);
        // getModel().getRuntime().println("Le volume total présent dans le Reach " + ID + " est " + totalactemp);
        // getModel().getRuntime().println("Le volume total sortant du Reach " + ID + " est " + cumOutflow);
   
        double RD1TrackedVolume_new = (RD1TrackedVolume_act_new*RD1out)/actRD1Temp;
        double RD2TrackedVolume_new = (RD2TrackedVolume_act_new*RD2out)/actRD2Temp;
        double RG1TrackedVolume_new = (RG1TrackedVolume_act_new*RG1out)/actRG1Temp;
        double RG2TrackedVolume_new = (RG2TrackedVolume_act_new*RG2out)/actRG2Temp;
        double SimRunoffTrackedVolume_new = (totalTrackedVolume_act_new*cumOutflow)/totalactTemp;
             
            if(actRD1Temp == 0){
                RD1TrackedVolume_new = 0;
            }
            if(actRD2Temp == 0){
                RD2TrackedVolume_new = 0;
            }
            if(actRG1Temp == 0){
                RG1TrackedVolume_new = 0;
            }
            if(actRG2Temp == 0){
                RG2TrackedVolume_new = 0;
            }
            if(totalactTemp == 0){
                SimRunoffTrackedVolume_new = 0;
            }
            
            // getModel().getRuntime().println("Le volume du Reach 2600 sortant du Reach " + ID + " est " + SimRunoffTrackedVolume_new);

            double actTrackedVolumeRD1 = RD1TrackedVolume_act_new - RD1TrackedVolume_new;
            double actTrackedVolumeRD2 = RD2TrackedVolume_act_new - RD2TrackedVolume_new;
            double actTrackedVolumeRG1 = RG1TrackedVolume_act_new - RG1TrackedVolume_new;
            double actTrackedVolumeRG2 = RG2TrackedVolume_act_new - RG2TrackedVolume_new;
            double totalactTrackedVolume = actTrackedVolumeRD1 + actTrackedVolumeRD2 + actTrackedVolumeRG1 + actTrackedVolumeRG2;
           
            // getModel().getRuntime().println("Le volume du Reach 2600 restant dans le Reach " + ID + " est " + totalactTrackedVolume);

            TrackedVolumeRD1.setValue(RD1TrackedVolume_new);
            TrackedVolumeRD2.setValue(RD2TrackedVolume_new);
            TrackedVolumeRG1.setValue(RG1TrackedVolume_new);
            TrackedVolumeRG2.setValue(RG2TrackedVolume_new);
            TrackedVolumeSimRunoff.setValue(SimRunoffTrackedVolume_new);
            
            TrackedVolumeRD1_act.setValue(actTrackedVolumeRD1);
            TrackedVolumeRD2_act.setValue(actTrackedVolumeRD2);
            TrackedVolumeRG1_act.setValue(actTrackedVolumeRG1);
            TrackedVolumeRG2_act.setValue(actTrackedVolumeRG2);
            

        
        // getModel().getRuntime().println("Le volume du Reach 2600 qui contribue au débit à l'exutoire est " + SimRunoffTrackedVolume_new);
        
    } else {
        // getModel().getRuntime().println("Ce n'est pas le Reach tracé");
    }
        
        
        
             
//// getModel().getRuntime().println("TrackedVolumeSimRunoff = " + TrackedVolumeSimRunoff);
        
            

        // On fait ça maintenant
        inRD1.setValue(0);
        inRD2.setValue(0);
        inRG1.setValue(0);
        inRG2.setValue(0);
        
        //reach
        if (DestReach != null && DestReservoir == null) {
            DestReach.setDouble("inRD1", RD1DestIn);
            DestReach.setDouble("inRD2", RD2DestIn);
            DestReach.setDouble("inRG1", RG1DestIn);
            DestReach.setDouble("inRG2", RG2DestIn);

            DestReach.setDouble("inAddIn", addInDestIn);

        } //reservoir
        else if (DestReservoir != null) {
            DestReservoir.setDouble("compRD1", RD1DestIn);
            DestReservoir.setDouble("compRD2", RD2DestIn);
            DestReservoir.setDouble("compRG1", RG1DestIn);
            DestReservoir.setDouble("compRG2", RG2DestIn);
        } //outlet
        else if (DestReach == null && DestReservoir == null) {
            catchmentRD1.setValue(RD1out);
            catchmentRD2.setValue(RD2out);
            catchmentRG1.setValue(RG1out);
            catchmentRG2.setValue(RG2out);

            this.catchmentAddIn.setValue(addInOut);
            //neu verzoegerung

            catchmentSimRunoff.setValue(cumOutflow);
        }

        waterLevel.setValue(channelStorage / (1000 * width * length));

    }

    public void cleanup() {

    }

    /**
     * Calculates flow velocity in specific reach
     *
     * @param q the runoff in the reach
     * @param width the width of reach
     * @param slope the slope of reach
     * @param rough the roughness of reach
     * @param secondsOfTimeStep the current time step in seconds
     * @return flow_velocity in m/s
     */
    public static double calcFlowVelocity(double q, double width, double slope, double rough, int secondsOfTimeStep) {
        double afv = 1;
        double veloc = 0;

        /**
         * transfering liter/d to m³/s
         *
         */
        double q_m = q / (1000 * secondsOfTimeStep);
        double rh = calcHydraulicRadius(afv, q_m, width);
        boolean cont = true;
        while (cont) {
            veloc = (rough) * Math.pow(rh, (2.0 / 3.0)) * Math.sqrt(slope);
            if ((Math.abs(veloc - afv)) > 0.001) {
                afv = veloc;
                rh = calcHydraulicRadius(afv, q_m, width);
            } else {
                cont = false;
                afv = veloc;
            }
        }
        return afv;
    }

    /**
     * Calculates the hydraulic radius of a rectangular stream bed depending on
     * daily runoff and flow_velocity
     *
     * @param v the flow velocity
     * @param q the daily runoff
     * @param width the width of reach
     * @return hydraulic radius in m
     */
    public static double calcHydraulicRadius(double v, double q, double width) {
        double A = (q / v);

        double rh = A / (width + 2 * (A / width));

        return rh;
    }
}
