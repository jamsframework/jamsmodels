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
        title = "Waste water Tracking",
        author = "Olivier Grandjouan & Nico Hachgenei",
        description = "Calculate the volume contribution from waster water in reach, modified after SODTracking",
        version = "1.0",
        date = "2024-05-31"
)

public class WWTracking extends JAMSComponent {
    
/*
     *  Component variables
     */
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "The reach collection"
    )
    public Attribute.EntityCollection entities;
    
//    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
//        description = "Array of station names")
//    public Attribute.DoubleArray names;         
        
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "RD1 storage in the Reach before routing volume out of the current Reach"
    )
    public Attribute.Double actTotalRD1;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "RD1 outflow from reach",
            unit = "L"
    )
    public Attribute.Double outRD1;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "Tracked volume from WWTP",
            unit = "L"
    )
    public Attribute.Double trackedVolumeWW;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "incoming tracked volume from WWTP",
            unit = "L"
    )
    public Attribute.Double InWW;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "Remaining tracked volume from WWTP in reach",
            unit = "L"
    )
    public Attribute.Double trackedVolumeWW_act;
 
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
//        // Array des noms des reachs
//        double[] Nom = this.names.getValue();
//        
//        
//        // double track = this.track.getValue();
//
        int ID = (int)entity.getDouble("ID");
//        int DestID = 0;
//        if(DestReach != null){
//            DestID = (int)DestReach.getDouble("ID");
//        }
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
                
        double RD1out = this.outRD1.getValue();
        
//        // TODO: test different 
//        Attribute.Double ReachDoubleTrackedVolumeWW = (Attribute.Double) entity.getObject("trackedVolumeWW");
//        //  Convert Double to double
//        double ReachTrackedVolumeWW = ReachDoubleTrackedVolumeWW.getValue();
//        int ID = (int)entity.getDouble("ID");
//        getModel().getRuntime().println("-- reach " + ID + " got : " + ReachTrackedVolumeWW + " coming in.");

//        // chasing bugs:
//        Attribute.Double testReachDoubleTrackedVolumeWW = (Attribute.Double) entity.getObject("trackedVolumeWW");
//        double testReachTrackedVolumeWW = testReachDoubleTrackedVolumeWW.getValue();
//        if(testReachTrackedVolumeWW > 0 || ID == 427601.0){
////                int ID = (int)entity.getDouble("ID");
//            getModel().getRuntime().println("-- reach " + ID + " received : " + testReachTrackedVolumeWW + ". Did this work?");
//
//        }

//      tracked incoming volume (directly from WWTP or from WWTP further upstream
        double WWin = InWW.getValue();

//      tracked volume
        double TrackedVolumeWW = trackedVolumeWW.getValue();
        
//      tracked WW volume already in reach
        double TrackedVolumeWW_act = trackedVolumeWW_act.getValue();
        
//        // chasing bugs:
//        if(TrackedVolumeWW > 0 || TrackedVolumeWW_act > 0){
////            int ID = (int)entity.getDouble("ID");
//            getModel().getRuntime().println("WWtracking: " + TrackedVolumeWW + " tracked, " + WWin + " incoming, " + TrackedVolumeWW_act + " present (reach "+ID+").");
//            if (DestReach != null){ 
//                getModel().getRuntime().println("-- destination reach: " + DestReach.getDouble("ID"));
//            } else {
//                getModel().getRuntime().println("-- there is no destination reach!");
//            }
//        }
        
        // update tracked WW volume in reach:
        // New WW volume = present tracked WW + tracked incoming volume (directly from WWTP or from WWTP further upstream
        TrackedVolumeWW_act = TrackedVolumeWW_act + WWin;
 
        // calculate outflow of tracked waste water from reach
        TrackedVolumeWW = (TrackedVolumeWW_act * RD1out)/actTotalRD1.getValue();
               
        if(actTotalRD1.getValue() == 0){
            TrackedVolumeWW = 0;
        }
        
//        // print percent of available water discharged
//        if ((ID == 298800) & (TrackedVolumeWW >0)){
//            getModel().getRuntime().println(" -- WWT ++ reach " + ID + " discharging " + RD1out/actTotalRD1.getValue()*100 + "% (= "+ RD1out +") of present RD1 (" + actTotalRD1.getValue() + "). TrackedVolumeWW = "+ TrackedVolumeWW);
//        }

        // remaining tracked WW volume in reach
        TrackedVolumeWW_act = TrackedVolumeWW_act - TrackedVolumeWW;
        
        WWin = 0;
        InWW.setValue(WWin);

        trackedVolumeWW.setValue(TrackedVolumeWW);

        trackedVolumeWW_act.setValue(TrackedVolumeWW_act);

        //  Set tracked WW volume incoming into destination reach
        if (DestReach != null){ 
            //  get destination reach incoming tracked volume (Double)        
            Attribute.Double destReachDoubleTrackedVolumeWW = (Attribute.Double) DestReach.getObject("InWW");

            
            //  Convert Double to double
            double destReachTrackedVolumeWW = destReachDoubleTrackedVolumeWW.getValue();
            
//            // chasing bugs:
//            if(TrackedVolumeWW > 0 || TrackedVolumeWW_act > 0){
////                int ID = (int)entity.getDouble("ID");
//                getModel().getRuntime().println("-- sending " + TrackedVolumeWW + " from present ("+ID+") to destination reach(" + DestReach.getDouble("ID") + "), previously containing " + destReachTrackedVolumeWW + ". " + TrackedVolumeWW_act + " remaining in current reach. RD1 out = " + RD1out + ". RD1_act before = " + actTotalRD1 + ".");
//                
//            }

            // Transfer into destination reach +++ modified to add to previous incoming volume
            destReachTrackedVolumeWW += TrackedVolumeWW;
            
            destReachDoubleTrackedVolumeWW.setValue(destReachTrackedVolumeWW); // couldn't this be set directly to TrackedVolumeWW, skipping the previous two steps?
            DestReach.setObject("InWW", destReachDoubleTrackedVolumeWW);
            
//            // chasing bugs:
//            if(TrackedVolumeWW > 0 || TrackedVolumeWW_act > 0){
////                int ID = (int)entity.getDouble("ID");
//                // reload destination reach in order to check if the transfer worked
//                Attribute.Entity newDestReach = (Attribute.Entity) entity.getObject("to_reach");  
//                Attribute.Double newdestReachDoubleTrackedVolumeWW = (Attribute.Double) newDestReach.getObject("trackedVolumeWW");
//                double newdestReachTrackedVolumeWW = newdestReachDoubleTrackedVolumeWW.getValue();
//                getModel().getRuntime().println("++ actually sent " + newdestReachTrackedVolumeWW + ". Did this work?");
//            }

        }  
                
    }   

}

