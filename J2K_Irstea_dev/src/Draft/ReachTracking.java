/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
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
        description = "Test",
        version = "1.0",
        date = "2022-01-18"
)

public class ReachTracking extends JAMSComponent {
    
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
            description = "RD1 inflow to the Reach, saved and used for tracking "
                    + "in ReachTracking component"
    )
    public Attribute.Double inRD1Track;
        
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "RD2 inflow to the Reach, saved and used for tracking "
                    + "in ReachTracking component"
    )
    public Attribute.Double inRD2Track;
        
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "RG1 inflow to the Reach, saved and used for tracking "
                    + "in ReachTracking component"
    )
    public Attribute.Double inRG1Track;
        
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "RG2 inflow to the Reach, saved and used for tracking "
                    + "in ReachTracking component"
    )
    public Attribute.Double inRG2Track;
    
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
    public Attribute.Double TrackedVolumeRD1_1;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "RD2 contribution from tracked reach 1",
            unit = "L",
            defaultValue = "0"
    )
    public Attribute.Double TrackedVolumeRD2_1;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "RG1 contribution from tracked reach 1",
            unit = "L",
            defaultValue = "0"
    )
    public Attribute.Double TrackedVolumeRG1_1;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "RG2 contribution from tracked reach 1",
            unit = "L",
            defaultValue = "0"
    )
    public Attribute.Double TrackedVolumeRG2_1;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "contribution from tracked reach 1 in total simulated runoff",
            unit = "L",
            defaultValue = "0"
    )
    public Attribute.Double TrackedVolumeSimRunoff_1;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "tracked reach 1"
    )
    public Attribute.Integer TrackedReach_1;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "RD1 contribution from tracked reach 2",
            unit = "L",
            defaultValue = "0"
    )
    public Attribute.Double TrackedVolumeRD1_2;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "RD2 contribution from tracked reach 2",
            unit = "L",
            defaultValue = "0"
    )
    public Attribute.Double TrackedVolumeRD2_2;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "RG1 contribution from tracked reach 2",
            unit = "L",
            defaultValue = "0"
    )
    public Attribute.Double TrackedVolumeRG1_2;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "RG2 contribution from tracked reach 2",
            unit = "L",
            defaultValue = "0"
    )
    public Attribute.Double TrackedVolumeRG2_2;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "contribution from tracked reach 2 in total simulated runoff",
            unit = "L",
            defaultValue = "0"
    )
    public Attribute.Double TrackedVolumeSimRunoff_2;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "tracked reach 2"
    )
    public Attribute.Integer TrackedReach_2;

    public void init() {
        
    } 
    public void run(){
    
        Attribute.Entity entity = entities.getCurrent();
        Attribute.Entity DestReach = (Attribute.Entity) entity.getObject("to_reach");
        if (DestReach.isEmpty()) {
                    DestReach = null;
                }
        double inTot = inRD1Track.getValue() + inRD2Track.getValue() + inRG1Track.getValue() + inRG2Track.getValue();
                
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
        int ReachTracked_1 = this.TrackedReach_1.getValue();
        int ReachTracked_2 = this.TrackedReach_2.getValue();

        double RD1TrackedVolume_1 = this.TrackedVolumeRD1_1.getValue();
        double RD2TrackedVolume_1 = this.TrackedVolumeRD2_1.getValue();
        double RG1TrackedVolume_1 = this.TrackedVolumeRG1_1.getValue();
        double RG2TrackedVolume_1 = this.TrackedVolumeRG2_1.getValue();
        double SimRunoffTrackedVolume_1 = this.TrackedVolumeSimRunoff_1.getValue();
        double RD1TrackedVolume_2 = this.TrackedVolumeRD1_2.getValue();
        double RD2TrackedVolume_2 = this.TrackedVolumeRD2_2.getValue();
        double RG1TrackedVolume_2 = this.TrackedVolumeRG1_2.getValue();
        double RG2TrackedVolume_2 = this.TrackedVolumeRG2_2.getValue();
        double SimRunoffTrackedVolume_2 = this.TrackedVolumeSimRunoff_2.getValue();

        double destReachRD1TrackedVolume_1 = 0;
        double destReachRD2TrackedVolume_1 = 0;
        double destReachRG1TrackedVolume_1 = 0;
        double destReachRG2TrackedVolume_1 = 0;
        double destReachSimRunoffTrackedVolume_1 = 0;
        double destReachRD1TrackedVolume_2 = 0;
        double destReachRD2TrackedVolume_2 = 0;
        double destReachRG1TrackedVolume_2 = 0;
        double destReachRG2TrackedVolume_2 = 0;
        double destReachSimRunoffTrackedVolume_2 = 0;

        getModel().getRuntime().println("Processing Reach " + ID);

    //  Save volume and transferring it if tracked reach n°1
        if (ReachTracked_1 == ID){
            
//        getModel().getRuntime().println(ID + " est bien le Reach tracé n°1 !");

        RD1TrackedVolume_1 = outRD1.getValue();
        RD2TrackedVolume_1 = outRD2.getValue();
        RG1TrackedVolume_1 = outRG1.getValue();
        RG2TrackedVolume_1 = outRG2.getValue();
        SimRunoffTrackedVolume_1 = simRunoff.getValue();

//        getModel().getRuntime().println("Le volume sauvegardé et transferé au Reach " + DestID + " est : " + SimRunoffTrackedVolume_1);

        destReachRD1TrackedVolume_1 =   RD1TrackedVolume_1;         
        destReachRD2TrackedVolume_1 =   RD2TrackedVolume_1;         
        destReachRG1TrackedVolume_1 =   RG1TrackedVolume_1;         
        destReachRG2TrackedVolume_1 =   RG2TrackedVolume_1;         
        destReachSimRunoffTrackedVolume_1 =   SimRunoffTrackedVolume_1;  

        DestReach.setDouble("TrackedVolumeRD1_1", destReachRD1TrackedVolume_1);
        DestReach.setDouble("TrackedVolumeRD2_1", destReachRD2TrackedVolume_1);
        DestReach.setDouble("TrackedVolumeRG1_1", destReachRG1TrackedVolume_1);
        DestReach.setDouble("TrackedVolumeRG2_1", destReachRG2TrackedVolume_1);
        DestReach.setDouble("TrackedVolumeSimRunoff_1", destReachSimRunoffTrackedVolume_1);

        TrackedVolumeRD1_1.setValue(RD1TrackedVolume_1);
        TrackedVolumeRD2_1.setValue(RD2TrackedVolume_1);
        TrackedVolumeRG1_1.setValue(RG1TrackedVolume_1);
        TrackedVolumeRG2_1.setValue(RG2TrackedVolume_1);
        TrackedVolumeSimRunoff_1.setValue(SimRunoffTrackedVolume_1);
        this.TrackedVolumeSimRunoff_1.getValue();
        
    } else if (this.TrackedVolumeSimRunoff_1.getValue() != 0  && DestReach != null){   
        
        /* Calculation and transfer of tracked volume*/
        
//        getModel().getRuntime().println("Transfert de l'écoulement au reach suivant");  
//        getModel().getRuntime().println("Le volume entrant dans le Reach " + ID + " est " + inTot);
//        getModel().getRuntime().println("Le volume du Reach 2600 présent dans le Reach " + ID + " est " + SimRunoffTrackedVolume_1);
//        getModel().getRuntime().println("Le volume sortant du Reach " + ID + " est " + cumOutflow);

        RD1TrackedVolume_1 = (RD1TrackedVolume_1*RD1out)/inRD1Track.getValue();
        RD2TrackedVolume_1 = (RD2TrackedVolume_1*RD2out)/inRD2Track.getValue();
        RG1TrackedVolume_1 = (RG1TrackedVolume_1*RG1out)/inRG1Track.getValue();
        RG2TrackedVolume_1 = (RG2TrackedVolume_1*RG2out)/inRG2Track.getValue();
        SimRunoffTrackedVolume_1 = (SimRunoffTrackedVolume_1*cumOutflow)/inTot;

//        getModel().getRuntime().println("La partie de 2600 qui va dans  le Reach " + DestID + " est " + SimRunoffTrackedVolume_1);

        if(inRD1Track.getValue() == 0){
            RD1TrackedVolume_1 = 0;
        }
        if(inRD2Track.getValue() == 0){
            RD2TrackedVolume_1 = 0;
        }
        if(inRG1Track.getValue() == 0){
            RG1TrackedVolume_1 = 0;
        }
        if(inRG2Track.getValue() == 0){
            RG2TrackedVolume_1 = 0;
        }
        if(inTot == 0){
            SimRunoffTrackedVolume_1 = 0;
        }

        destReachRD1TrackedVolume_1 =   RD1TrackedVolume_1;         
        destReachRD2TrackedVolume_1 =   RD2TrackedVolume_1;         
        destReachRG1TrackedVolume_1 =   RG1TrackedVolume_1;         
        destReachRG2TrackedVolume_1 =   RG2TrackedVolume_1;         
        destReachSimRunoffTrackedVolume_1 =   SimRunoffTrackedVolume_1;          

        DestReach.setDouble("TrackedVolumeRD1_1", destReachRD1TrackedVolume_1);
        DestReach.setDouble("TrackedVolumeRD2_1", destReachRD2TrackedVolume_1);
        DestReach.setDouble("TrackedVolumeRG1_1", destReachRG1TrackedVolume_1);
        DestReach.setDouble("TrackedVolumeRG2_1", destReachRG2TrackedVolume_1);
        DestReach.setDouble("TrackedVolumeSimRunoff_1", destReachSimRunoffTrackedVolume_1);

        TrackedVolumeRD1_1.setValue(RD1TrackedVolume_1);
        TrackedVolumeRD2_1.setValue(RD2TrackedVolume_1);
        TrackedVolumeRG1_1.setValue(RG1TrackedVolume_1);
        TrackedVolumeRG2_1.setValue(RG2TrackedVolume_1);
        TrackedVolumeSimRunoff_1.setValue(SimRunoffTrackedVolume_1);
        
    } else if(DestReach == null){
        
        RD1TrackedVolume_1 = (RD1TrackedVolume_1*RD1out)/inRD1Track.getValue();
        RD2TrackedVolume_1 = (RD2TrackedVolume_1*RD2out)/inRD2Track.getValue();
        RG1TrackedVolume_1 = (RG1TrackedVolume_1*RG1out)/inRG1Track.getValue();
        RG2TrackedVolume_1 = (RG2TrackedVolume_1*RG2out)/inRG2Track.getValue();
        SimRunoffTrackedVolume_1 = (SimRunoffTrackedVolume_1*cumOutflow)/inTot;

        if(inRD1Track.getValue() == 0){
            RD1TrackedVolume_1 = 0;
        }
        if(inRD2Track.getValue() == 0){
            RD2TrackedVolume_1 = 0;
        }
        if(inRG1Track.getValue() == 0){
            RG1TrackedVolume_1 = 0;
        }
        if(inRG2Track.getValue() == 0){
            RG2TrackedVolume_1 = 0;
        }
        if(inTot == 0){
            SimRunoffTrackedVolume_1 = 0;
        }

        TrackedVolumeRD1_1.setValue(RD1TrackedVolume_1);
        TrackedVolumeRD2_1.setValue(RD2TrackedVolume_1);
        TrackedVolumeRG1_1.setValue(RG1TrackedVolume_1);
        TrackedVolumeRG2_1.setValue(RG2TrackedVolume_1);
        TrackedVolumeSimRunoff_1.setValue(SimRunoffTrackedVolume_1);
        
//        getModel().getRuntime().println("Le volume du Reach 2600 qui contribue au débit à l'exutoire est " + SimRunoffTrackedVolume_1);
        
    } else {
//        getModel().getRuntime().println("Ce n'est pas le Reach tracé");
    }
        
        //  Save volume and transferring it if tracked reach n°2
        if (ReachTracked_2 == ID){
            
        getModel().getRuntime().println(ID + " est bien le Reach tracé n°2 !");

        RD1TrackedVolume_2 = outRD1.getValue();
        RD2TrackedVolume_2 = outRD2.getValue();
        RG1TrackedVolume_2 = outRG1.getValue();
        RG2TrackedVolume_2 = outRG2.getValue();
        SimRunoffTrackedVolume_2 = simRunoff.getValue();

        getModel().getRuntime().println("Le volume sauvegardé et transferé au Reach " + DestID + " est : " + SimRunoffTrackedVolume_2);

        destReachRD1TrackedVolume_2 =   RD1TrackedVolume_2;         
        destReachRD2TrackedVolume_2 =   RD2TrackedVolume_2;         
        destReachRG1TrackedVolume_2 =   RG1TrackedVolume_2;         
        destReachRG2TrackedVolume_2 =   RG2TrackedVolume_2;         
        destReachSimRunoffTrackedVolume_2 =   SimRunoffTrackedVolume_2;  

        DestReach.setDouble("TrackedVolumeRD1_2", destReachRD1TrackedVolume_2);
        DestReach.setDouble("TrackedVolumeRD2_2", destReachRD2TrackedVolume_2);
        DestReach.setDouble("TrackedVolumeRG1_2", destReachRG1TrackedVolume_2);
        DestReach.setDouble("TrackedVolumeRG2_2", destReachRG2TrackedVolume_2);
        DestReach.setDouble("TrackedVolumeSimRunoff_2", destReachSimRunoffTrackedVolume_2);

        TrackedVolumeRD1_2.setValue(RD1TrackedVolume_2);
        TrackedVolumeRD2_2.setValue(RD2TrackedVolume_2);
        TrackedVolumeRG1_2.setValue(RG1TrackedVolume_2);
        TrackedVolumeRG2_2.setValue(RG2TrackedVolume_2);
        TrackedVolumeSimRunoff_2.setValue(SimRunoffTrackedVolume_2);
        this.TrackedVolumeSimRunoff_2.getValue();
        
    } else if (this.TrackedVolumeSimRunoff_2.getValue() != 0  && DestReach != null){   
        
        /* Calculation and transfer of tracked volume*/
        
        getModel().getRuntime().println("Transfert de l'écoulement au reach suivant"); 
        getModel().getRuntime().println("Le volume total entrant dans le Reach " + ID + " est " + inTot);
        getModel().getRuntime().println("Le volume du Reach 3600 entrant dans le Reach " + ID + " est " + SimRunoffTrackedVolume_2);
        getModel().getRuntime().println("Le volume total sortant du Reach " + ID + " est " + cumOutflow);
        
        RD1TrackedVolume_2 = (RD1TrackedVolume_2*RD1out)/inRD1Track.getValue();
        RD2TrackedVolume_2 = (RD2TrackedVolume_2*RD2out)/inRD2Track.getValue();
        RG1TrackedVolume_2 = (RG1TrackedVolume_2*RG1out)/inRG1Track.getValue();
        RG2TrackedVolume_2 = (RG2TrackedVolume_2*RG2out)/inRG2Track.getValue();
        SimRunoffTrackedVolume_2 = (SimRunoffTrackedVolume_2*cumOutflow)/inTot;

        if(inRD1Track.getValue() == 0){
            RD1TrackedVolume_2 = 0;
        }
        if(inRD2Track.getValue() == 0){
            RD2TrackedVolume_2 = 0;
        }
        if(inRG1Track.getValue() == 0){
            RG1TrackedVolume_2 = 0;
        }
        if(inRG2Track.getValue() == 0){
            RG2TrackedVolume_2 = 0;
        }
        if(inTot == 0){
            SimRunoffTrackedVolume_2 = 0;
        }
        
        getModel().getRuntime().println("La partie de 2600 qui va dans  le Reach " + DestID + " est " + SimRunoffTrackedVolume_2);


        destReachRD1TrackedVolume_2 =   RD1TrackedVolume_2;         
        destReachRD2TrackedVolume_2 =   RD2TrackedVolume_2;         
        destReachRG1TrackedVolume_2 =   RG1TrackedVolume_2;         
        destReachRG2TrackedVolume_2 =   RG2TrackedVolume_2;         
        destReachSimRunoffTrackedVolume_2 =   SimRunoffTrackedVolume_2;          

        DestReach.setDouble("TrackedVolumeRD1_2", destReachRD1TrackedVolume_2);
        DestReach.setDouble("TrackedVolumeRD2_2", destReachRD2TrackedVolume_2);
        DestReach.setDouble("TrackedVolumeRG1_2", destReachRG1TrackedVolume_2);
        DestReach.setDouble("TrackedVolumeRG2_2", destReachRG2TrackedVolume_2);
        DestReach.setDouble("TrackedVolumeSimRunoff_2", destReachSimRunoffTrackedVolume_2);

        TrackedVolumeRD1_2.setValue(RD1TrackedVolume_2);
        TrackedVolumeRD2_2.setValue(RD2TrackedVolume_2);
        TrackedVolumeRG1_2.setValue(RG1TrackedVolume_2);
        TrackedVolumeRG2_2.setValue(RG2TrackedVolume_2);
        TrackedVolumeSimRunoff_2.setValue(SimRunoffTrackedVolume_2);
        
    } else if(DestReach == null){
        
        RD1TrackedVolume_2 = (RD1TrackedVolume_2*RD1out)/inRD1Track.getValue();
        RD2TrackedVolume_2 = (RD2TrackedVolume_2*RD2out)/inRD2Track.getValue();
        RG1TrackedVolume_2 = (RG1TrackedVolume_2*RG1out)/inRG1Track.getValue();
        RG2TrackedVolume_2 = (RG2TrackedVolume_2*RG2out)/inRG2Track.getValue();
        SimRunoffTrackedVolume_2 = (SimRunoffTrackedVolume_2*cumOutflow)/inTot;

        if(inRD1Track.getValue() == 0){
            RD1TrackedVolume_2 = 0;
        }
        if(inRD2Track.getValue() == 0){
            RD2TrackedVolume_2 = 0;
        }
        if(inRG1Track.getValue() == 0){
            RG1TrackedVolume_2 = 0;
        }
        if(inRG2Track.getValue() == 0){
            RG2TrackedVolume_2 = 0;
        }
        if(inTot == 0){
            SimRunoffTrackedVolume_2 = 0;
        }

        TrackedVolumeRD1_2.setValue(RD1TrackedVolume_2);
        TrackedVolumeRD2_2.setValue(RD2TrackedVolume_2);
        TrackedVolumeRG1_2.setValue(RG1TrackedVolume_2);
        TrackedVolumeRG2_2.setValue(RG2TrackedVolume_2);
        TrackedVolumeSimRunoff_2.setValue(SimRunoffTrackedVolume_2);
        
        getModel().getRuntime().println("Le volume du Reach 2600 qui contribue au débit à l'exutoire est " + SimRunoffTrackedVolume_2);
        
    } else {
        getModel().getRuntime().println("Ce n'est pas le Reach tracé");
    }
}    
}
        
    
    
    