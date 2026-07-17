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
        description = "Calculate the volume contribution from sewer in reach and set to destination reach. Modified after SODTracking",
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
            description = "RD1 volume in the current reach before routing water out of it. Read by this component"+
                    "for proportional calculation of tracked volumes. - input"
    )
    public Attribute.Double actTotalRD1;
        
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "RD2 volume in the current reach before routing water out of it. Read by this component"+
                    "for proportional calculation of tracked volumes. - input"
    )
    public Attribute.Double actTotalRD2;
        
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "RG1 volume in the current reach before routing water out of it. Read by this component"+
                    "for proportional calculation of tracked volumes. - input"
    )
    public Attribute.Double actTotalRG1;
        
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "RG2 volume in the current reach before routing water out of it. Read by this component"+
                    "for proportional calculation of tracked volumes. - input"
    )
    public Attribute.Double actTotalRG2;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "Total volume in the current reach before routing water out of it. Read by this component"+
                    "for proportional calculation of tracked volumes. - input"
    )
    public Attribute.Double actTotalTot;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "RD1 inflow into reach from Sewer. Read, added to tracked and reset to 0. - input",
            unit = "L"
    )
    public Attribute.Double SewInRD1;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "RD2 inflow into reach from Sewer. Read, added to tracked and reset to 0. - input",
            unit = "L"
    )
    public Attribute.Double SewInRD2;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "RG1 inflow into reach from Sewer. Read, added to tracked and reset to 0. - input",
            unit = "L"
    )
    public Attribute.Double SewInRG1;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "RG2 inflow into reach from Sewer. Read, added to tracked and reset to 0. - input",
            unit = "L"
    )
    public Attribute.Double SewInRG2;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "RD1 outflow from reach. Read to calulate tracked volumes to route to next reach - input",
            unit = "L"
    )
    public Attribute.Double outRD1;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "RD2 outflow from reach. Read to calulate tracked volumes to route to next reach - input",
            unit = "L"
    )
    public Attribute.Double outRD2;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "RG1 outflow from reach. Read to calulate tracked volumes to route to next reach - input",
            unit = "L"
    )
    public Attribute.Double outRG1;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "RG2 outflow from reach. Read to calulate tracked volumes to route to next reach - input",
            unit = "L"
    )
    public Attribute.Double outRG2;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Total outflow from reach. Read to calulate tracked volumes to route to next reach - input",
            unit = "L"
    )
    public Attribute.Double simRunoff;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "Volume in RD1 in reach coming from Sewer (for routing to next reach). Set by this component."+
                    "- output",
            unit = "L"
    )
    public Attribute.Double trackedVolumeSewRD1;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "Volume in RD2 in reach coming from Sewer (for routing to next reach). Set by this component."+
                    "- output",
            unit = "L"
    )
    public Attribute.Double trackedVolumeSewRD2;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "Volume in RG1 in reach coming from Sewer (for routing to next reach). Set by this component."+
                    "- output",
            unit = "L"
    )
    public Attribute.Double trackedVolumeSewRG1;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "Volume in RG2 in reach coming from Sewer (for routing to next reach). Set by this component."+
                    "- output",
            unit = "L"
    )
    public Attribute.Double trackedVolumeSewRG2;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "Total volume in reach coming from Sewer (for routing to next reach). Set by this component."+
                    "- output",
            unit = "L"
    )
    public Attribute.Double trackedVolumeSewTotal;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "Remaining volume in reach RD1 coming from Sewer. Updated by this component."+
                    "- state variable",
            unit = "L"
    )
    public Attribute.Double trackedVolumeSewRD1_act;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "Remaining volume in reach RD2 coming from Sewer. Updated by this component."+
                    "- state variable",
            unit = "L"
    )
    public Attribute.Double trackedVolumeSewRD2_act;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "Remaining volume in reach RG1 coming from Sewer. Updated by this component."+
                    "- state variable",
            unit = "L"
    )
    public Attribute.Double trackedVolumeSewRG1_act;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "Remaining volume in reach RG2 coming from Sewer. Updated by this component."+
                    "- state variable",
            unit = "L"
    )
    public Attribute.Double trackedVolumeSewRG2_act;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "Remaining total volume in reach coming from Sewer. Updated by this component."+
                    "- state variable",
            unit = "L"
    )
    public Attribute.Double trackedVolumeSewTotal_act;
 
    // A modifier pour tracer un sewer en particulier. La on trace tout d'un coup
//    @JAMSVarDescription(
//            access = JAMSVarDescription.AccessType.READ,
//            description = "reach track or not"
//    )
//    public Attribute.Double track;        

    @Override
    public void init(){
    

    } 
    
    @Override
    public void run(){
        
//        getModel().getRuntime().println("TrackedVolumeTotalArray : " + Arrays.toString(this.trackedVolumeTotalArray.getValue()));

                
        Attribute.Entity run_entity = entities.getCurrent();
        Attribute.Entity run_DestReach = (Attribute.Entity) run_entity.getObject("to_reach");        
        if (run_DestReach.isEmpty()) {
                    run_DestReach = null;
                }
//        // Array des noms des reachs
//        double[] run_names = this.names.getValue();
        
        
        // double track = this.track.getValue();

//        int run_ID = (int)run_entity.getDouble("ID");
//        int run_DestID = 0;
//        if(run_DestReach != null){
//            run_DestID = (int)run_DestReach.getDouble("ID");
//        }
//        int ReachTracked = this.trackedReach.getValue();
        // Index des reach tracé
//        int r = 0;
//        while (run_names[r] != ReachTracked) {
//            r++;
//        }        
//        int t = 0;
//        while (run_names[t] != ID) {
//            t++;
//        }    
        
        double run_actTotalTot = actTotalRD1.getValue() + actTotalRD2.getValue() + actTotalRG1.getValue() + actTotalRG2.getValue();
        actTotalTot.setValue(run_actTotalTot);
        
        double run_RD1out = this.outRD1.getValue();
        double run_RD2out = this.outRD2.getValue();
        double run_RG1out = this.outRG1.getValue();
        double run_RG2out = this.outRG2.getValue();
        double run_cumOutflow = this.simRunoff.getValue();
        
//      Lecture des données pour le reach actuel
//      Volume provenant du SEW entrant dans le reach
        double run_inRD1_SEW = SewInRD1.getValue();
        double run_inRD2_SEW = SewInRD2.getValue();
        double run_inRG1_SEW = SewInRG1.getValue();
        double run_inRG2_SEW = SewInRG2.getValue();
        double run_inTotal_SEW = run_inRD1_SEW + run_inRD2_SEW + run_inRG1_SEW + run_inRG2_SEW;

//      Volume tracé
//        double run_TrackedVolumeSewRD1 = trackedVolumeSewRD1.getValue();
//        double run_TrackedVolumeSewRD2 = trackedVolumeSewRD2.getValue();
//        double run_TrackedVolumeSewRG1 = trackedVolumeSewRG1.getValue();
//        double run_TrackedVolumeSewRG2 = trackedVolumeSewRG2.getValue();
//        double run_TrackedVolumeSewTotal = trackedVolumeSewTotal.getValue();
        double run_TrackedVolumeSewRD1;
        double run_TrackedVolumeSewRD2;
        double run_TrackedVolumeSewRG1;
        double run_TrackedVolumeSewRG2;
        double run_TrackedVolumeSewTotal;
        
//      Volume tracé présent dans le reach
        double run_TrackedVolumeSew_actRD1 = trackedVolumeSewRD1_act.getValue();
        double run_TrackedVolumeSew_actRD2 = trackedVolumeSewRD2_act.getValue();
        double run_TrackedVolumeSew_actRG1 = trackedVolumeSewRG1_act.getValue();
        double run_TrackedVolumeSew_actRG2 = trackedVolumeSewRG2_act.getValue();
        double run_TrackedVolumeSew_actTotal = trackedVolumeSewTotal_act.getValue(); 
        
//        if (inTotal_SEW>0 || TrackedVolumeSew_actTotal>0){
//            getModel().getRuntime().println("reach " + ID + " got " + inTotal_SEW + " sewer water. " + TrackedVolumeSew_actTotal + " were allready present. ");
//        }
        
        
        
        // update tracked sewer volume present in reach:
        // new volume = present volume + incoming volume
        run_TrackedVolumeSew_actRD1 = run_TrackedVolumeSew_actRD1 + run_inRD1_SEW;
        run_TrackedVolumeSew_actRD2 = run_TrackedVolumeSew_actRD2 + run_inRD2_SEW;
        run_TrackedVolumeSew_actRG1 = run_TrackedVolumeSew_actRG1 + run_inRG1_SEW;
        run_TrackedVolumeSew_actRG2 = run_TrackedVolumeSew_actRG2 + run_inRG2_SEW;
        run_TrackedVolumeSew_actTotal = run_TrackedVolumeSew_actTotal + run_inTotal_SEW;
 
        // Calcul du volume provenant du SEW sortant du brin tracé
        run_TrackedVolumeSewRD1 = (run_TrackedVolumeSew_actRD1 * run_RD1out)/actTotalRD1.getValue();
        run_TrackedVolumeSewRD2 = (run_TrackedVolumeSew_actRD2 * run_RD2out)/actTotalRD2.getValue();
        run_TrackedVolumeSewRG1 = (run_TrackedVolumeSew_actRG1 * run_RG1out)/actTotalRG1.getValue();
        run_TrackedVolumeSewRG2 = (run_TrackedVolumeSew_actRG2 * run_RG2out)/actTotalRG2.getValue();
        run_TrackedVolumeSewTotal = (run_TrackedVolumeSew_actTotal * run_cumOutflow)/run_actTotalTot;
               
        if(actTotalRD1.getValue() == 0){
            run_TrackedVolumeSewRD1 = 0;
        }
        if(actTotalRD2.getValue() == 0){
            run_TrackedVolumeSewRD2 = 0;
        }
        if(actTotalRG1.getValue() == 0){
            run_TrackedVolumeSewRG1 = 0;
        }
        if(actTotalRG2.getValue() == 0){
            run_TrackedVolumeSewRG2 = 0;
        }
        if(run_actTotalTot == 0){
            run_TrackedVolumeSewTotal = 0;
        }  

      // Enregistrement temporaire du volume restant dans le brin.
        run_TrackedVolumeSew_actRD1 = run_TrackedVolumeSew_actRD1 - run_TrackedVolumeSewRD1;
        run_TrackedVolumeSew_actRD2 = run_TrackedVolumeSew_actRD2 - run_TrackedVolumeSewRD2;
        run_TrackedVolumeSew_actRG1 = run_TrackedVolumeSew_actRG1 - run_TrackedVolumeSewRG1;
        run_TrackedVolumeSew_actRG2 = run_TrackedVolumeSew_actRG2 - run_TrackedVolumeSewRG2;
        run_TrackedVolumeSew_actTotal = run_TrackedVolumeSew_actTotal - run_TrackedVolumeSewTotal;
        
        // this is still the outflow
        trackedVolumeSewRD1.setValue(run_TrackedVolumeSewRD1);
        trackedVolumeSewRD2.setValue(run_TrackedVolumeSewRD2);
        trackedVolumeSewRG1.setValue(run_TrackedVolumeSewRG1);
        trackedVolumeSewRG2.setValue(run_TrackedVolumeSewRG2);
        trackedVolumeSewTotal.setValue(run_TrackedVolumeSewTotal);

        trackedVolumeSewRD1_act.setValue(run_TrackedVolumeSew_actRD1);
        trackedVolumeSewRD2_act.setValue(run_TrackedVolumeSew_actRD2);
        trackedVolumeSewRG1_act.setValue(run_TrackedVolumeSew_actRG1);
        trackedVolumeSewRG2_act.setValue(run_TrackedVolumeSew_actRG2);   
        trackedVolumeSewTotal_act.setValue(run_TrackedVolumeSew_actTotal);
        
        SewInRD1.setValue(0);
        SewInRD2.setValue(0);
        SewInRG1.setValue(0);
        SewInRG2.setValue(0);

        //  Sauvegarde du volume SEW tracé dans le reach de destination
        if (run_DestReach != null){ 
            //  Importation des Double du brin de destination        
            Attribute.Double run_destReachDoubleTrackedVolumeSewRD1 = (Attribute.Double) run_DestReach.getObject("SewInRD1");
            Attribute.Double run_destReachDoubleTrackedVolumeSewRD2 = (Attribute.Double) run_DestReach.getObject("SewInRD2");
            Attribute.Double run_destReachDoubleTrackedVolumeSewRG1 = (Attribute.Double) run_DestReach.getObject("SewInRG1");
            Attribute.Double run_destReachDoubleTrackedVolumeSewRG2 = (Attribute.Double) run_DestReach.getObject("SewInRG2");

            
            //  Conversion de DoubleArray à double[]
            double run_destReachTrackedVolumeSewRD1 = run_destReachDoubleTrackedVolumeSewRD1.getValue();
            double run_destReachTrackedVolumeSewRD2 = run_destReachDoubleTrackedVolumeSewRD2.getValue();
            double run_destReachTrackedVolumeSewRG1 = run_destReachDoubleTrackedVolumeSewRG1.getValue();
            double run_destReachTrackedVolumeSewRG2 = run_destReachDoubleTrackedVolumeSewRG2.getValue();

            // Transfert du volume de SEW tracé dans le brin suivant +++ modified: add instead of overwriting
            run_destReachTrackedVolumeSewRD1 += run_TrackedVolumeSewRD1;
            run_destReachTrackedVolumeSewRD2 += run_TrackedVolumeSewRD2;
            run_destReachTrackedVolumeSewRG1 += run_TrackedVolumeSewRG1;
            run_destReachTrackedVolumeSewRG2 += run_TrackedVolumeSewRG2;
            
//            if ((TrackedVolumeSewRD1 + TrackedVolumeSewRD2 + TrackedVolumeSewRG1 + TrackedVolumeSewRG2) > 0){
//                getModel().getRuntime().println("sent " + (TrackedVolumeSewRD1 + TrackedVolumeSewRD2 + TrackedVolumeSewRG1 + TrackedVolumeSewRG2)+ " to reach " + DestID + ".");
//            }
            
            
            run_destReachDoubleTrackedVolumeSewRD1.setValue(run_destReachTrackedVolumeSewRD1); // couldn't this be set directly to TrackedVolumeSewRD1, skipping the previous two steps?
            run_destReachDoubleTrackedVolumeSewRD2.setValue(run_destReachTrackedVolumeSewRD2);
            run_destReachDoubleTrackedVolumeSewRG1.setValue(run_destReachTrackedVolumeSewRG1);
            run_destReachDoubleTrackedVolumeSewRG2.setValue(run_destReachTrackedVolumeSewRG2);
            run_DestReach.setObject("SewInRD1", run_destReachDoubleTrackedVolumeSewRD1);
            run_DestReach.setObject("SewInRD2", run_destReachDoubleTrackedVolumeSewRD2);
            run_DestReach.setObject("SewInRG1", run_destReachDoubleTrackedVolumeSewRG1);
            run_DestReach.setObject("SewInRG2", run_destReachDoubleTrackedVolumeSewRG2);

        }  
                
    }   

}

