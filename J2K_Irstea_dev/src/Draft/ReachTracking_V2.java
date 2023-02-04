package Draft;

import jams.JAMS;
import jams.data.*;
import jams.model.*;

/**
 *
 * @author Olivier Grandjouan
 */
@JAMSComponentDescription(
        title = "ReachTracking",
        author = "Olivier Grandjouan",
        description = "Calcule the volume contribution from up to 2 specific Reaches to the output discharge",
        version = "1.0",
        date = "2022-01-24"
)

public class ReachTracking_V2 extends JAMSComponent {
    
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
            description = "RD1 storage in the Reach before routing volume out of the actual Reach"
    )
    public Attribute.Double actRD1Temp;
        
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "RD2 storage in the Reach before routing volume out of the actual Reach"
    )
    public Attribute.Double actRD2Temp;
        
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "RG1 storage in the Reach before routing volume out of the actual Reach"
    )
    public Attribute.Double actRG1Temp;
        
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "RG2 storage in the Reach before routing volume out of the actual Reach"
    )
    public Attribute.Double actRG2Temp;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "RD1 outflow from reach",
            unit = "L"
    )
    public Attribute.Double outRD1;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "RD2 outflow from reach",
            unit = "L"
    )
    public Attribute.Double outRD2;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "RG1 outflow from reach",
            unit = "L"
    )
    public Attribute.Double outRG1;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "RG2 outflow from reach",
            unit = "L"
    )
    public Attribute.Double outRG2;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "simulated runoff from reach",
            unit = "L"
    )
    public Attribute.Double simRunoff;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "RD1 contribution from tracked reach",
            unit = "L",
            defaultValue = "0"
    )
    public Attribute.Double TrackedVolumeRD1;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "RD2 contribution from tracked reach 1",
            unit = "L",
            defaultValue = "0"
    )
    public Attribute.Double TrackedVolumeRD2;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "RG1 contribution from tracked reach 1",
            unit = "L",
            defaultValue = "0"
    )
    public Attribute.Double TrackedVolumeRG1;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "RG2 contribution from tracked reach 1",
            unit = "L",
            defaultValue = "0"
    )
    public Attribute.Double TrackedVolumeRG2;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "contribution from tracked reach 1 in total simulated runoff",
            unit = "L",
            defaultValue = "0"
    )
    public Attribute.Double TrackedVolumeSimRunoff;  
    
   @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "RD1 remaining volume from tracked reach 1 in actual reach after routing"
    )
    public Attribute.Double TrackedVolumeRD1_act;
   
   @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "RD2 remaining volume from tracked reach 1 in actual reach after routing"
    )
    public Attribute.Double TrackedVolumeRD2_act;
   
   @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "RG1 remaining volume from tracked reach 1 in actual reach after routing"
    )
    public Attribute.Double TrackedVolumeRG1_act;
   
   @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "RG2 remaining volume from tracked reach 1 in actual reach after routing"
    )
    public Attribute.Double TrackedVolumeRG2_act;
   
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Tracked reach 1"
    )
    public Attribute.Integer TrackedReach;

    public void init() {
        
    } 
    public void run(){
    
        Attribute.Entity entity = entities.getCurrent();
        Attribute.Entity DestReach = (Attribute.Entity) entity.getObject("to_reach");
        if (DestReach.isEmpty()) {
                    DestReach = null;
                }
        double actTotTemp = actRD1Temp.getValue() + actRD2Temp.getValue() + actRG1Temp.getValue() + actRG2Temp.getValue();
                
        double RD1out = this.outRD1.getValue();
        double RD2out = this.outRD2.getValue();
        double RG1out = this.outRG1.getValue();
        double RG2out = this.outRG2.getValue();
        double cumOutflow = this.simRunoff.getValue();
        
        int ID = (int)entity.getDouble("ID");
        int DestID = 0;
        if(DestReach != null){
            DestID = (int)DestReach.getDouble("ID");
        }
        int ReachTracked = this.TrackedReach.getValue();

       getModel().getRuntime().println("Processing Reach " + ID);

    //  Save volume and transferring it if tracked reach n°1
        if (ReachTracked == ID){
            
        getModel().getRuntime().println(ID + " est bien le Reach tracé !");

        double RD1TrackedVolume = RD1out;
        double RD2TrackedVolume = RD2out;
        double RG1TrackedVolume = RG1out;
        double RG2TrackedVolume = RG2out;
        double SimRunoffTrackedVolume = cumOutflow;

//        getModel().getRuntime().println("Le volume sauvegardé et transferé au Reach " + DestID + " est : " + SimRunoffTrackedVolume);

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
        this.TrackedVolumeSimRunoff.getValue();
        
    } else if (this.TrackedVolumeSimRunoff.getValue() != 0  && DestReach != null){   
        
        /* Calculation and transfer of tracked volume*/
        
//        getModel().getRuntime().println("Transfert de l'écoulement au reach suivant");  

        double RD1TrackedVolume_old = this.TrackedVolumeRD1.getValue();
        double RD2TrackedVolume_old = this.TrackedVolumeRD2.getValue();
        double RG1TrackedVolume_old = this.TrackedVolumeRG1.getValue();
        double RG2TrackedVolume_old = this.TrackedVolumeRG2.getValue();
        double SimRunoffTrackedVolume_old = this.TrackedVolumeSimRunoff.getValue();
        
//        getModel().getRuntime().println("Le volume du Reach " + ReachTracked + " entrant dans le Reach " + ID + " est " + SimRunoffTrackedVolume_old);

        double RD1TrackedVolume_act_old = this. TrackedVolumeRD1_act.getValue();
        double RD2TrackedVolume_act_old = this. TrackedVolumeRD2_act.getValue();
        double RG1TrackedVolume_act_old = this. TrackedVolumeRG1_act.getValue();
        double RG2TrackedVolume_act_old = this. TrackedVolumeRG2_act.getValue();
        double totalTrackedVolume_act_old = RD1TrackedVolume_act_old + RD2TrackedVolume_act_old + RG1TrackedVolume_act_old + RG2TrackedVolume_act_old;
        
        getModel().getRuntime().println("Le volume du Reach 2600 déjà présent dans le Reach " + ID + " est " + totalTrackedVolume_act_old);

        double RD1TrackedVolume_act_new = RD1TrackedVolume_old + RD1TrackedVolume_act_old;
        double RD2TrackedVolume_act_new = RD2TrackedVolume_old + RD2TrackedVolume_act_old;
        double RG1TrackedVolume_act_new = RG1TrackedVolume_old + RG1TrackedVolume_act_old;
        double RG2TrackedVolume_act_new = RG2TrackedVolume_old + RG2TrackedVolume_act_old;
        double totalTrackedVolume_act_new = SimRunoffTrackedVolume_old + totalTrackedVolume_act_old;
        
//        getModel().getRuntime().println("Le nouveau volume du Reach 2600 présent dans le Reach " + ID + " est " + totalTrackedVolume_act_new);
//        getModel().getRuntime().println("Le volume total présent dans le Reach " + ID + " est " + actTotTemp);
//        getModel().getRuntime().println("Le volume total sortant du Reach " + ID + " est " + cumOutflow);
   
        double RD1TrackedVolume_new = (RD1TrackedVolume_act_new*RD1out)/actRD1Temp.getValue();
        double RD2TrackedVolume_new = (RD2TrackedVolume_act_new*RD2out)/actRD2Temp.getValue();
        double RG1TrackedVolume_new = (RG1TrackedVolume_act_new*RG1out)/actRG1Temp.getValue();
        double RG2TrackedVolume_new = (RG2TrackedVolume_act_new*RG2out)/actRG2Temp.getValue();
        double SimRunoffTrackedVolume_new = (totalTrackedVolume_act_new*cumOutflow)/actTotTemp;
                
        if(actRD1Temp.getValue() == 0){
            RD1TrackedVolume_new = 0;
        }
        if(actRD2Temp.getValue() == 0){
            RD2TrackedVolume_new = 0;
        }
        if(actRG1Temp.getValue() == 0){
            RG1TrackedVolume_new = 0;
        }
        if(actRG2Temp.getValue() == 0){
            RG2TrackedVolume_new = 0;
        }
        if(actTotTemp == 0){
            SimRunoffTrackedVolume_new = 0;
        }
        
//        getModel().getRuntime().println("Le volume du Reach 2600 sortant du Reach " + ID + " est " + SimRunoffTrackedVolume_new);

        double actTrackedVolumeRD1 = RD1TrackedVolume_act_new - RD1TrackedVolume_new;
        double actTrackedVolumeRD2 = RD2TrackedVolume_act_new - RD2TrackedVolume_new;
        double actTrackedVolumeRG1 = RG1TrackedVolume_act_new - RG1TrackedVolume_new;
        double actTrackedVolumeRG2 = RG2TrackedVolume_act_new - RG2TrackedVolume_new;
        double totalactTrackedVolume = actTrackedVolumeRD1 + actTrackedVolumeRD2 + actTrackedVolumeRG1 + actTrackedVolumeRG2;

//        getModel().getRuntime().println("Le volume du Reach 2600 restant dans le Reach " + ID + " est " + totalactTrackedVolume);

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
        
        double RD1TrackedVolume_old = this.TrackedVolumeRD1.getValue();
        double RD2TrackedVolume_old = this.TrackedVolumeRD2.getValue();
        double RG1TrackedVolume_old = this.TrackedVolumeRG1.getValue();
        double RG2TrackedVolume_old = this.TrackedVolumeRG2.getValue();
        double SimRunoffTrackedVolume_old = this.TrackedVolumeSimRunoff.getValue();
        double RD1TrackedVolume_act_old = this. TrackedVolumeRD1_act.getValue();
        double RD2TrackedVolume_act_old = this. TrackedVolumeRD2_act.getValue();
        double RG1TrackedVolume_act_old = this. TrackedVolumeRG1_act.getValue();
        double RG2TrackedVolume_act_old = this. TrackedVolumeRG2_act.getValue();
        double totalTrackedVolume_act_old = RD1TrackedVolume_act_old + RD2TrackedVolume_act_old + RG1TrackedVolume_act_old + RG2TrackedVolume_act_old;
        
        double RD1TrackedVolume_act_new = RD1TrackedVolume_old + RD1TrackedVolume_act_old;
        double RD2TrackedVolume_act_new = RD2TrackedVolume_old + RD2TrackedVolume_act_old;
        double RG1TrackedVolume_act_new = RG1TrackedVolume_old + RG1TrackedVolume_act_old;
        double RG2TrackedVolume_act_new = RG2TrackedVolume_old + RG2TrackedVolume_act_old;
        double totalTrackedVolume_act_new = SimRunoffTrackedVolume_old + totalTrackedVolume_act_old;
        
             
        double RD1TrackedVolume_new = (RD1TrackedVolume_act_new*RD1out)/actRD1Temp.getValue();
        double RD2TrackedVolume_new = (RD2TrackedVolume_act_new*RD2out)/actRD2Temp.getValue();
        double RG1TrackedVolume_new = (RG1TrackedVolume_act_new*RG1out)/actRG1Temp.getValue();
        double RG2TrackedVolume_new = (RG2TrackedVolume_act_new*RG2out)/actRG2Temp.getValue();
        double SimRunoffTrackedVolume_new = (totalTrackedVolume_act_new*cumOutflow)/actTotTemp;
 
//        getModel().getRuntime().println("La partie de 2600 qui va dans  le Reach " + DestID + " est " + SimRunoffTrackedVolume_new);

        if(actRD1Temp.getValue() == 0){
            RD1TrackedVolume_new = 0;
        }
        if(actRD2Temp.getValue() == 0){
            RD2TrackedVolume_new = 0;
        }
        if(actRG1Temp.getValue() == 0){
            RG1TrackedVolume_new = 0;
        }
        if(actRG2Temp.getValue() == 0){
            RG2TrackedVolume_new = 0;
        }
        if(actTotTemp == 0){
            SimRunoffTrackedVolume_new = 0;
        }
        
        double actTrackedVolumeRD1 = RD1TrackedVolume_act_new - RD1TrackedVolume_new;
        double actTrackedVolumeRD2 = RD2TrackedVolume_act_new - RD2TrackedVolume_new;
        double actTrackedVolumeRG1 = RG1TrackedVolume_act_new - RG1TrackedVolume_new;
        double actTrackedVolumeRG2 = RG2TrackedVolume_act_new - RG2TrackedVolume_new;
        double totalactTrackedVolume = actTrackedVolumeRD1 + actTrackedVolumeRD2 + actTrackedVolumeRG1 + actTrackedVolumeRG2;
        
        TrackedVolumeRD1.setValue(RD1TrackedVolume_new);
        TrackedVolumeRD2.setValue(RD2TrackedVolume_new);
        TrackedVolumeRG1.setValue(RG1TrackedVolume_new);
        TrackedVolumeRG2.setValue(RG2TrackedVolume_new);
        TrackedVolumeSimRunoff.setValue(SimRunoffTrackedVolume_new);
        
        TrackedVolumeRD1_act.setValue(actTrackedVolumeRD1);
        TrackedVolumeRD2_act.setValue(actTrackedVolumeRD2);
        TrackedVolumeRG1_act.setValue(actTrackedVolumeRG1);
        TrackedVolumeRG2_act.setValue(actTrackedVolumeRG2);
        
//        getModel().getRuntime().println("Le volume du Reach 2600 qui contribue au débit à l'exutoire est " + SimRunoffTrackedVolume_new_1);
        
    } else {
//        getModel().getRuntime().println("Ce n'est pas le Reach tracé");
    }
}    
}
//        
//    
//    
//    