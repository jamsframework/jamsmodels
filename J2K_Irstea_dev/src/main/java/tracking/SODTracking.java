package tracking;

import jams.JAMS;
import jams.data.*;
import jams.model.*;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Olivier Grandjouan
 */
@JAMSComponentDescription(
        title = "SewerTracking_V1",
        author = "Olivier Grandjouan",
        description = "Calculate the volume contribution from sewer overflow devices in reach",
        version = "1.0",
        date = "2022-03-07"
)

public class SODTracking extends JAMSComponent {
    
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
            description = "RD1 inflow from SOD",
            unit = "L"
    )
    public Attribute.Double sodInRD1;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "RD2 inflow from SOD",
            unit = "L"
    )
    public Attribute.Double sodInRD2;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "RG1 inflow from SOD",
            unit = "L"
    )
    public Attribute.Double sodInRG1;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "RG2 inflow from SOD",
            unit = "L"
    )
    public Attribute.Double sodInRG2;
    
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
            description = "Tracked RD1 volume from SOD",
            unit = "L"
    )
    public Attribute.Double trackedVolumeSodRD1;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "Tracked RD2 volume from SOD",
            unit = "L"
    )
    public Attribute.Double trackedVolumeSodRD2;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "Tracked RG1 volume from SOD",
            unit = "L"
    )
    public Attribute.Double trackedVolumeSodRG1;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "Tracked RG2 volume from SOD",
            unit = "L"
    )
    public Attribute.Double trackedVolumeSodRG2;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "Tracked total volume from SOD",
            unit = "L"
    )
    public Attribute.Double trackedVolumeSodTotal;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "Remaining tracked RD1 volume from SOD",
            unit = "L"
    )
    public Attribute.Double trackedVolumeSodRD1_act;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "Remaining tracked RD2 volume from SOD in reach",
            unit = "L"
    )
    public Attribute.Double trackedVolumeSodRD2_act;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "Remaining tracked RG1 volume from SOD in reach",
            unit = "L"
    )
    public Attribute.Double trackedVolumeSodRG1_act;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "Remaining tracked RG2 volume from SOD in reach",
            unit = "L"
    )
    public Attribute.Double trackedVolumeSodRG2_act;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "Remaining tracked total volume from SOD in reach",
            unit = "L"
    )
    public Attribute.Double trackedVolumeSodTotal_act;
 
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
//      Volume provenant du SOD entrant dans le reach
        double inRD1_SOD = sodInRD1.getValue();
        double inRD2_SOD = sodInRD2.getValue();
        double inRG1_SOD = sodInRG1.getValue();
        double inRG2_SOD = sodInRG2.getValue();
        double inTotal_SOD = inRD1_SOD + inRD2_SOD + inRG1_SOD + inRG2_SOD;

//      Volume tracé
        double TrackedVolumeSodRD1 = trackedVolumeSodRD1.getValue();
        double TrackedVolumeSodRD2 = trackedVolumeSodRD2.getValue();
        double TrackedVolumeSodRG1 = trackedVolumeSodRG1.getValue();
        double TrackedVolumeSodRG2 = trackedVolumeSodRG2.getValue();
        double TrackedVolumeSodTotal = trackedVolumeSodTotal.getValue();
        
//      Volume tracé présent dans le reach
        double TrackedVolumeSod_actRD1 = trackedVolumeSodRD1_act.getValue();
        double TrackedVolumeSod_actRD2 = trackedVolumeSodRD2_act.getValue();
        double TrackedVolumeSod_actRG1 = trackedVolumeSodRG1_act.getValue();
        double TrackedVolumeSod_actRG2 = trackedVolumeSodRG2_act.getValue();
        double TrackedVolumeSod_actTotal = trackedVolumeSodTotal_act.getValue(); 
        
        
        // Mise à jour du volume provenant de SOD tracé dans le brin :
        // Nouveau volume SOD = Volume déja présent + volume provenant directement d'un SOD + volume SOD transféré par des reachs amonts
        TrackedVolumeSod_actRD1 = TrackedVolumeSod_actRD1 + inRD1_SOD + TrackedVolumeSodRD1;
        TrackedVolumeSod_actRD2 = TrackedVolumeSod_actRD2 + inRD2_SOD + TrackedVolumeSodRD2;
        TrackedVolumeSod_actRG1 = TrackedVolumeSod_actRG1 + inRG1_SOD + TrackedVolumeSodRG1;
        TrackedVolumeSod_actRG2 = TrackedVolumeSod_actRG2 + inRG2_SOD + TrackedVolumeSodRG2;
        TrackedVolumeSod_actTotal = TrackedVolumeSod_actTotal + inTotal_SOD + TrackedVolumeSodTotal;
 
        // Calcul du volume provenant du SOD sortant du brin tracé
        TrackedVolumeSodRD1 = (TrackedVolumeSod_actRD1 * RD1out)/actTotalRD1.getValue();
        TrackedVolumeSodRD2 = (TrackedVolumeSod_actRD2 * RD2out)/actTotalRD2.getValue();
        TrackedVolumeSodRG1 = (TrackedVolumeSod_actRG1 * RG1out)/actTotalRG1.getValue();
        TrackedVolumeSodRG2 = (TrackedVolumeSod_actRG2 * RG2out)/actTotalRG2.getValue();
        TrackedVolumeSodTotal = (TrackedVolumeSod_actTotal * cumOutflow)/actTotalTot;
               
        if(actTotalRD1.getValue() == 0){
            TrackedVolumeSodRD1 = 0;
        }
        if(actTotalRD2.getValue() == 0){
            TrackedVolumeSodRD2 = 0;
        }
        if(actTotalRG1.getValue() == 0){
            TrackedVolumeSodRG1 = 0;
        }
        if(actTotalRG2.getValue() == 0){
            TrackedVolumeSodRG2 = 0;
        }
        if(actTotalTot == 0){
            TrackedVolumeSodTotal = 0;
        }  

      // Enregistrement temporaire du volume restant dans le brin.
        TrackedVolumeSod_actRD1 = TrackedVolumeSod_actRD1 - TrackedVolumeSodRD1;
        TrackedVolumeSod_actRD2 = TrackedVolumeSod_actRD2 - TrackedVolumeSodRD2;
        TrackedVolumeSod_actRG1 = TrackedVolumeSod_actRG1 - TrackedVolumeSodRG1;
        TrackedVolumeSod_actRG2 = TrackedVolumeSod_actRG2 - TrackedVolumeSodRG2;
        TrackedVolumeSod_actTotal = TrackedVolumeSod_actTotal - TrackedVolumeSodTotal;
        

        trackedVolumeSodRD1.setValue(TrackedVolumeSodRD1);
        trackedVolumeSodRD2.setValue(TrackedVolumeSodRD2);
        trackedVolumeSodRG1.setValue(TrackedVolumeSodRG1);
        trackedVolumeSodRG2.setValue(TrackedVolumeSodRG2);
        trackedVolumeSodTotal.setValue(TrackedVolumeSodTotal);

        trackedVolumeSodRD1_act.setValue(TrackedVolumeSod_actRD1);
        trackedVolumeSodRD2_act.setValue(TrackedVolumeSod_actRD2);
        trackedVolumeSodRG1_act.setValue(TrackedVolumeSod_actRG1);
        trackedVolumeSodRG2_act.setValue(TrackedVolumeSod_actRG2);   
        trackedVolumeSodTotal_act.setValue(TrackedVolumeSod_actTotal);
        
        sodInRD1.setValue(0);
        sodInRD2.setValue(0);
        sodInRG1.setValue(0);
        sodInRG2.setValue(0);

        //  Sauvegarde du volume SOD tracé dans le reach de destination
        if (DestReach != null){ 
            //  Importation des Double du brin de destination        
            Attribute.Double destReachDoubleTrackedVolumeSodRD1 = (Attribute.Double) DestReach.getObject("trackedVolumeSodRD1");
            Attribute.Double destReachDoubleTrackedVolumeSodRD2 = (Attribute.Double) DestReach.getObject("trackedVolumeSodRD2");
            Attribute.Double destReachDoubleTrackedVolumeSodRG1 = (Attribute.Double) DestReach.getObject("trackedVolumeSodRG1");
            Attribute.Double destReachDoubleTrackedVolumeSodRG2 = (Attribute.Double) DestReach.getObject("trackedVolumeSodRG2");
            Attribute.Double destReachDoubleTrackedVolumeSodTotal = (Attribute.Double) DestReach.getObject("trackedVolumeSodTotal");

            
            //  Conversion de DoubleArray à double[]
            double destReachTrackedVolumeSodRD1 = destReachDoubleTrackedVolumeSodRD1.getValue();
            double destReachTrackedVolumeSodRD2 = destReachDoubleTrackedVolumeSodRD2.getValue();
            double destReachTrackedVolumeSodRG1 = destReachDoubleTrackedVolumeSodRG1.getValue();
            double destReachTrackedVolumeSodRG2 = destReachDoubleTrackedVolumeSodRG2.getValue();
            double destReachTrackedVolumeSodTotal = destReachDoubleTrackedVolumeSodTotal.getValue();

            // Transfert du volume de SOD tracé dans le brin suivant
            destReachTrackedVolumeSodRD1 = TrackedVolumeSodRD1;
            destReachTrackedVolumeSodRD2 = TrackedVolumeSodRD2;
            destReachTrackedVolumeSodRG1 = TrackedVolumeSodRG1;
            destReachTrackedVolumeSodRG2 = TrackedVolumeSodRG2;
            destReachTrackedVolumeSodTotal = TrackedVolumeSodTotal;
            
            destReachDoubleTrackedVolumeSodRD1.setValue(destReachTrackedVolumeSodRD1);
            destReachDoubleTrackedVolumeSodRD2.setValue(destReachTrackedVolumeSodRD2);
            destReachDoubleTrackedVolumeSodRG1.setValue(destReachTrackedVolumeSodRG1);
            destReachDoubleTrackedVolumeSodRG2.setValue(destReachTrackedVolumeSodRG2);
            destReachDoubleTrackedVolumeSodTotal.setValue(destReachTrackedVolumeSodTotal);
            DestReach.setObject("trackedVolumeSodRD1", destReachDoubleTrackedVolumeSodRD1);
            DestReach.setObject("trackedVolumeSodRD2", destReachDoubleTrackedVolumeSodRD2);
            DestReach.setObject("trackedVolumeSodRG1", destReachDoubleTrackedVolumeSodRG1);
            DestReach.setObject("trackedVolumeSodRG2", destReachDoubleTrackedVolumeSodRG2);
            DestReach.setObject("trackedVolumeSodTotal", destReachDoubleTrackedVolumeSodTotal);

        }  
                
    }   

}

