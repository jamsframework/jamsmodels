package tracking;

import jams.JAMS;
import jams.data.*;
import jams.model.*;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Olivier Grandjouan, Nico Hachgenei
 */
@JAMSComponentDescription(
        title = "SewerTracking",
        author = "Olivier Grandjouan & Nico Hachgenei",
        description = "Calculate the volume contribution from sewer in reach, modified after SODTracking",
        version = "1.0",
        date = "2024-05-28"
)

public class SewerTracking extends JAMSComponent {
    
/*
     *  Component variables
     */
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "The reach collection"
    )
    public Attribute.EntityCollection entities;
    
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
        description = "Array of station names")
    public Attribute.DoubleArray names;         
        
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "RD1 storage in the Reach before routing volume out of the actual Reach"
    )
    public Attribute.Double actTotalRD1;
        
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "RD2 storage in the Reach before routing volume out of the actual Reach"
    )
    public Attribute.Double actTotalRD2;
        
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "RG1 storage in the Reach before routing volume out of the actual Reach"
    )
    public Attribute.Double actTotalRG1;
        
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "RG2 storage in the Reach before routing volume out of the actual Reach"
    )
    public Attribute.Double actTotalRG2;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "Total storage in the Reach before routing volume out of the actual Reach"
    )
    public Attribute.Double actTotalTot;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "RD1 inflow from Sewer",
            unit = "L"
    )
    public Attribute.Double SewInRD1;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "RD2 inflow from Sewer",
            unit = "L"
    )
    public Attribute.Double SewInRD2;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "RG1 inflow from Sewer",
            unit = "L"
    )
    public Attribute.Double SewInRG1;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "RG2 inflow from Sewer",
            unit = "L"
    )
    public Attribute.Double SewInRG2;
    
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
            description = "Tracked RD1 volume from Sewer",
            unit = "L"
    )
    public Attribute.Double trackedVolumeSewRD1;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "Tracked RD2 volume from Sewer",
            unit = "L"
    )
    public Attribute.Double trackedVolumeSewRD2;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "Tracked RG1 volume from Sewer",
            unit = "L"
    )
    public Attribute.Double trackedVolumeSewRG1;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "Tracked RG2 volume from Sewer",
            unit = "L"
    )
    public Attribute.Double trackedVolumeSewRG2;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "Tracked total volume from Sewer",
            unit = "L"
    )
    public Attribute.Double trackedVolumeSewTotal;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "Remaining tracked RD1 volume from Sewer in reach",
            unit = "L"
    )
    public Attribute.Double trackedVolumeSewRD1_act;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "Remaining tracked RD2 volume from Sewer in reach",
            unit = "L"
    )
    public Attribute.Double trackedVolumeSewRD2_act;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "Remaining tracked RG1 volume from Sewer in reach",
            unit = "L"
    )
    public Attribute.Double trackedVolumeSewRG1_act;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "Remaining tracked RG2 volume from Sewer in reach",
            unit = "L"
    )
    public Attribute.Double trackedVolumeSewRG2_act;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "Remaining tracked total volume from Sewer in reach",
            unit = "L"
    )
    public Attribute.Double trackedVolumeSewTotal_act;
 
    // A modifier pour tracer un sewer en particulier. La on trace tout d'un coup
//    @JAMSVarDescription(
//            access = JAMSVarDescription.AccessType.READ,
//            description = "reach track or not"
//    )
//    public Attribute.Double track;        

    public void init(){
    

    } 
    
    public void run(){
        
//        getModel().getRuntime().println("TrackedVolumeTotalArray : " + Arrays.toString(this.trackedVolumeTotalArray.getValue()));

                
        Attribute.Entity entity = entities.getCurrent();
        Attribute.Entity DestReach = (Attribute.Entity) entity.getObject("to_reach");        
        if (DestReach.isEmpty()) {
                    DestReach = null;
                }
        // Array des noms des reachs
        double[] Nom = this.names.getValue();
        
        
        // double track = this.track.getValue();

        int ID = (int)entity.getDouble("ID");
        int DestID = 0;
        if(DestReach != null){
            DestID = (int)DestReach.getDouble("ID");
        }
//        int ReachTracked = this.trackedReach.getValue();
        // Index des reach tracé
//        int r = 0;
//        while (Nom[r] != ReachTracked) {
//            r++;
//        }        
//        int t = 0;
//        while (Nom[t] != ID) {
//            t++;
//        }    
        
        double actTotalTot = actTotalRD1.getValue() + actTotalRD2.getValue() + actTotalRG1.getValue() + actTotalRG2.getValue();
                
        double RD1out = this.outRD1.getValue();
        double RD2out = this.outRD2.getValue();
        double RG1out = this.outRG1.getValue();
        double RG2out = this.outRG2.getValue();
        double cumOutflow = this.simRunoff.getValue();
        
//      Lecture des données pour le reach actuel
//      Volume provenant du SEW entrant dans le reach
        double inRD1_SEW = SewInRD1.getValue();
        double inRD2_SEW = SewInRD2.getValue();
        double inRG1_SEW = SewInRG1.getValue();
        double inRG2_SEW = SewInRG2.getValue();
        double inTotal_SEW = inRD1_SEW + inRD2_SEW + inRG1_SEW + inRG2_SEW;

//      Volume tracé
        double TrackedVolumeSewRD1 = trackedVolumeSewRD1.getValue();
        double TrackedVolumeSewRD2 = trackedVolumeSewRD2.getValue();
        double TrackedVolumeSewRG1 = trackedVolumeSewRG1.getValue();
        double TrackedVolumeSewRG2 = trackedVolumeSewRG2.getValue();
        double TrackedVolumeSewTotal = trackedVolumeSewTotal.getValue();
        
//      Volume tracé présent dans le reach
        double TrackedVolumeSew_actRD1 = trackedVolumeSewRD1_act.getValue();
        double TrackedVolumeSew_actRD2 = trackedVolumeSewRD2_act.getValue();
        double TrackedVolumeSew_actRG1 = trackedVolumeSewRG1_act.getValue();
        double TrackedVolumeSew_actRG2 = trackedVolumeSewRG2_act.getValue();
        double TrackedVolumeSew_actTotal = trackedVolumeSewTotal_act.getValue(); 
        
        
        // Mise à jour du volume provenant de SEW tracé dans le brin :
        // Nouveau volume SEW = Volume déja présent + volume provenant directement d'un SEW + volume SEW transféré par des reachs amonts
        TrackedVolumeSew_actRD1 = TrackedVolumeSew_actRD1 + inRD1_SEW + TrackedVolumeSewRD1;
        TrackedVolumeSew_actRD2 = TrackedVolumeSew_actRD2 + inRD2_SEW + TrackedVolumeSewRD2;
        TrackedVolumeSew_actRG1 = TrackedVolumeSew_actRG1 + inRG1_SEW + TrackedVolumeSewRG1;
        TrackedVolumeSew_actRG2 = TrackedVolumeSew_actRG2 + inRG2_SEW + TrackedVolumeSewRG2;
        TrackedVolumeSew_actTotal = TrackedVolumeSew_actTotal + inTotal_SEW + TrackedVolumeSewTotal;
 
        // Calcul du volume provenant du SEW sortant du brin tracé
        TrackedVolumeSewRD1 = (TrackedVolumeSew_actRD1 * RD1out)/actTotalRD1.getValue();
        TrackedVolumeSewRD2 = (TrackedVolumeSew_actRD2 * RD2out)/actTotalRD2.getValue();
        TrackedVolumeSewRG1 = (TrackedVolumeSew_actRG1 * RG1out)/actTotalRG1.getValue();
        TrackedVolumeSewRG2 = (TrackedVolumeSew_actRG2 * RG2out)/actTotalRG2.getValue();
        TrackedVolumeSewTotal = (TrackedVolumeSew_actTotal * cumOutflow)/actTotalTot;
               
        if(actTotalRD1.getValue() == 0){
            TrackedVolumeSewRD1 = 0;
        }
        if(actTotalRD2.getValue() == 0){
            TrackedVolumeSewRD2 = 0;
        }
        if(actTotalRG1.getValue() == 0){
            TrackedVolumeSewRG1 = 0;
        }
        if(actTotalRG2.getValue() == 0){
            TrackedVolumeSewRG2 = 0;
        }
        if(actTotalTot == 0){
            TrackedVolumeSewTotal = 0;
        }  

      // Enregistrement temporaire du volume restant dans le brin.
        TrackedVolumeSew_actRD1 = TrackedVolumeSew_actRD1 - TrackedVolumeSewRD1;
        TrackedVolumeSew_actRD2 = TrackedVolumeSew_actRD2 - TrackedVolumeSewRD2;
        TrackedVolumeSew_actRG1 = TrackedVolumeSew_actRG1 - TrackedVolumeSewRG1;
        TrackedVolumeSew_actRG2 = TrackedVolumeSew_actRG2 - TrackedVolumeSewRG2;
        TrackedVolumeSew_actTotal = TrackedVolumeSew_actTotal - TrackedVolumeSewTotal;
        

        trackedVolumeSewRD1.setValue(TrackedVolumeSewRD1);
        trackedVolumeSewRD2.setValue(TrackedVolumeSewRD2);
        trackedVolumeSewRG1.setValue(TrackedVolumeSewRG1);
        trackedVolumeSewRG2.setValue(TrackedVolumeSewRG2);
        trackedVolumeSewTotal.setValue(TrackedVolumeSewTotal);

        trackedVolumeSewRD1_act.setValue(TrackedVolumeSew_actRD1);
        trackedVolumeSewRD2_act.setValue(TrackedVolumeSew_actRD2);
        trackedVolumeSewRG1_act.setValue(TrackedVolumeSew_actRG1);
        trackedVolumeSewRG2_act.setValue(TrackedVolumeSew_actRG2);   
        trackedVolumeSewTotal_act.setValue(TrackedVolumeSew_actTotal);
        
        SewInRD1.setValue(0);
        SewInRD2.setValue(0);
        SewInRG1.setValue(0);
        SewInRG2.setValue(0);

        //  Sauvegarde du volume SEW tracé dans le reach de destination
        if (DestReach != null){ 
            //  Importation des Double du brin de destination        
            Attribute.Double destReachDoubleTrackedVolumeSewRD1 = (Attribute.Double) DestReach.getObject("trackedVolumeSewRD1");
            Attribute.Double destReachDoubleTrackedVolumeSewRD2 = (Attribute.Double) DestReach.getObject("trackedVolumeSewRD2");
            Attribute.Double destReachDoubleTrackedVolumeSewRG1 = (Attribute.Double) DestReach.getObject("trackedVolumeSewRG1");
            Attribute.Double destReachDoubleTrackedVolumeSewRG2 = (Attribute.Double) DestReach.getObject("trackedVolumeSewRG2");
            Attribute.Double destReachDoubleTrackedVolumeSewTotal = (Attribute.Double) DestReach.getObject("trackedVolumeSewTotal");

            
            //  Conversion de DoubleArray à double[]
            double destReachTrackedVolumeSewRD1 = destReachDoubleTrackedVolumeSewRD1.getValue();
            double destReachTrackedVolumeSewRD2 = destReachDoubleTrackedVolumeSewRD2.getValue();
            double destReachTrackedVolumeSewRG1 = destReachDoubleTrackedVolumeSewRG1.getValue();
            double destReachTrackedVolumeSewRG2 = destReachDoubleTrackedVolumeSewRG2.getValue();
            double destReachTrackedVolumeSewTotal = destReachDoubleTrackedVolumeSewTotal.getValue();

            // Transfert du volume de SEW tracé dans le brin suivant
            destReachTrackedVolumeSewRD1 = TrackedVolumeSewRD1;
            destReachTrackedVolumeSewRD2 = TrackedVolumeSewRD2;
            destReachTrackedVolumeSewRG1 = TrackedVolumeSewRG1;
            destReachTrackedVolumeSewRG2 = TrackedVolumeSewRG2;
            destReachTrackedVolumeSewTotal = TrackedVolumeSewTotal;
            
            destReachDoubleTrackedVolumeSewRD1.setValue(destReachTrackedVolumeSewRD1);
            destReachDoubleTrackedVolumeSewRD2.setValue(destReachTrackedVolumeSewRD2);
            destReachDoubleTrackedVolumeSewRG1.setValue(destReachTrackedVolumeSewRG1);
            destReachDoubleTrackedVolumeSewRG2.setValue(destReachTrackedVolumeSewRG2);
            destReachDoubleTrackedVolumeSewTotal.setValue(destReachTrackedVolumeSewTotal);
            DestReach.setObject("trackedVolumeSewRD1", destReachDoubleTrackedVolumeSewRD1);
            DestReach.setObject("trackedVolumeSewRD2", destReachDoubleTrackedVolumeSewRD2);
            DestReach.setObject("trackedVolumeSewRG1", destReachDoubleTrackedVolumeSewRG1);
            DestReach.setObject("trackedVolumeSewRG2", destReachDoubleTrackedVolumeSewRG2);
            DestReach.setObject("trackedVolumeSewTotal", destReachDoubleTrackedVolumeSewTotal);

        }  
                
    }   

}

