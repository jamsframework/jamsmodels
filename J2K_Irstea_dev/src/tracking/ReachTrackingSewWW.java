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
        title = "ReachTrackingSewWW",
        author = "Olivier Grandjouan, Nico Hachgenei",
        description = "Compute the volume contribution from all Reaches to the output discharge."
        + " If reach is tracked, remove sewer and waste water part from tracked volume from this reach"
        + " to avoid double-counting it. The WW volume will be removed from RD1 as it is stored as RD1.",
        version = "1.0",
        date = "2023-05-31"
)

public class ReachTrackingSewWW extends JAMSComponent {
    
/*
     *  Component variables
     */
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "The reach collection"
    )
    public Attribute.EntityCollection entities;
    
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
        description = "Array of reach IDs")
    public Attribute.DoubleArray names;         
        
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "RD1 volume in the current reach before routing water out of it. Read by this component"+
                    "for proportional substraction from tracked volumes. - input"
    )
    public Attribute.Double actTotalRD1;
        
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "RD2 volume in the current reach before routing water out of it. Read by this component"+
                    "for proportional substraction from tracked volumes. - input"
    )
    public Attribute.Double actTotalRD2;
        
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "RG1 volume in the current reach before routing water out of it. Read by this component"+
                    "for proportional substraction from tracked volumes. - input"
    )
    public Attribute.Double actTotalRG1;
        
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "RG2 volume in the current reach before routing water out of it. Read by this component"+
                    "for proportional substraction from tracked volumes. - input"
    )
    public Attribute.Double actTotalRG2;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "Total volume in the current reach before routing water out of it. Written by this component"+
                    "as sum of the four flow components. - input"
    )
    public Attribute.Double actTotalTot;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "RD1 outflow from reach. Read by this component in order to distribute over tracked volumes. - input",
            unit = "L"
    )
    public Attribute.Double outRD1;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "RD2 outflow from reach. Read by this component in order to distribute over tracked volumes. - input",
            unit = "L"
    )
    public Attribute.Double outRD2;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "RG1 outflow from reach. Read by this component in order to distribute over tracked volumes. - input",
            unit = "L"
    )
    public Attribute.Double outRG1;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "RG2 outflow from reach. Read by this component in order to distribute over tracked volumes. - input",
            unit = "L"
    )
    public Attribute.Double outRG2;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Simulated total runoff leaving current reach. Read by this component in order to distribute over tracked volumes. - input",
            unit = "L"
    )
    public Attribute.Double simRunoff;

    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "Array of current time step tracked RD1 per source reach. Read (containing -999, for structure)"+
            "and overwrite value for current reach with the reaches outflow. - (output)"
            )
    public Attribute.DoubleArray trackedVolumeRD1Array;
        
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "Array of current time step tracked RD2 per source reach. Read (containing -999, for structure)"+
            "and overwrite value for current reach with the reaches outflow. - (output)"
            )
    public Attribute.DoubleArray trackedVolumeRD2Array;  
        
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "Array of current time step tracked RG1 per source reach. Read (containing -999, for structure)"+
            "and overwrite value for current reach with the reaches outflow. - (output)"
            )
    public Attribute.DoubleArray trackedVolumeRG1Array;  
        
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "Array of current time step tracked RG2 per source reach. Read (containing -999, for structure)"+
            "and overwrite value for current reach with the reaches outflow. - (output)"
            )
    public Attribute.DoubleArray trackedVolumeRG2Array;  
        
        @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "Array of current time step tracked outflow volume per source reach. Read (containing -999, for structure)"+
            "and overwrite value for current reach with the reaches outflow. - (output)"
            )
    public Attribute.DoubleArray trackedVolumeTotalArray;   
             
    @JAMSVarDescription(
        access = JAMSVarDescription.AccessType.READWRITE,
        description = "Array of RD1 remaining volume from each tracked reach in actual reach after routing."+
                "Read, overwrite value for current reach and set for next reach. - (output)"
        )
    public Attribute.DoubleArray trackedVolumeRD1_actArray; 
             
    @JAMSVarDescription(
        access = JAMSVarDescription.AccessType.READWRITE,
        description = "Array of RD2 remaining volume from each tracked reach in actual reach after routing."+
                "Read, overwrite value for current reach and set for next reach. - (output)"
        )
    public Attribute.DoubleArray trackedVolumeRD2_actArray; 
             
    @JAMSVarDescription(
        access = JAMSVarDescription.AccessType.READWRITE,
        description = "Array of RG1 remaining volume from each tracked reach in actual reach after routing."+
                "Read, overwrite value for current reach and set for next reach. - (output)"
        )
    public Attribute.DoubleArray trackedVolumeRG1_actArray; 
             
    @JAMSVarDescription(
        access = JAMSVarDescription.AccessType.READWRITE,
        description = "Array of RG2 remaining volume from each tracked reach in actual reach after routing."+
                "Read, overwrite value for current reach and set for next reach. - (output)"
        )
    public Attribute.DoubleArray trackedVolumeRG2_actArray;
            
    @JAMSVarDescription(
        access = JAMSVarDescription.AccessType.READWRITE,
        description = "Array of total remaining volume from each tracked reach in actual reach after routing."+
                "Read, overwrite value for current reach and set for next reach. - (output)"
        )
    public Attribute.DoubleArray trackedVolumeTotal_actArray;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Is this reach tracked? Information to read from ReachLoop, originating from"+
                    "reach par file. - parameter"
    )
    public Attribute.Double track;    

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Volume in RD1 in reach coming from Sewer. Read by this component and removed"+
                    "from other tracked volume. - input",
            unit = "L"
    )
    public Attribute.Double trackedVolumeSewRD1;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Volume in RD2 in reach coming from Sewer. Read by this component and removed"+
                    "from other tracked volume. - input",
            unit = "L"
    )
    public Attribute.Double trackedVolumeSewRD2;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Volume in RG1 in reach coming from Sewer. Read by this component and removed"+
                    "from other tracked volume. - input",
            unit = "L"
    )
    public Attribute.Double trackedVolumeSewRG1;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Volume in RG2 in reach coming from Sewer. Read by this component and removed"+
                    "from other tracked volume. - input",
            unit = "L"
    )
    public Attribute.Double trackedVolumeSewRG2;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Total volume in reach coming from Sewer. Read by this component and removed"+
                    "from other tracked volume. - input",
            unit = "L"
    )
    public Attribute.Double trackedVolumeSewTotal; 
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Remaining volume in reach RD1 coming from Sewer. Read by this component and removed"+
                    "from other tracked volume. - input",
            unit = "L"
    )
    public Attribute.Double trackedVolumeSewRD1_act;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Remaining volume in reach RD2 coming from Sewer. Read by this component and removed"+
                    "from other tracked volume. - input",
            unit = "L"
    )
    public Attribute.Double trackedVolumeSewRD2_act;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Remaining volume in reach RG1 coming from Sewer. Read by this component and removed"+
                    "from other tracked volume. - input",
            unit = "L"
    )
    public Attribute.Double trackedVolumeSewRG1_act;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Remaining volume in reach RG2 coming from Sewer. Read by this component and removed"+
                    "from other tracked volume. - input",
            unit = "L"
    )
    public Attribute.Double trackedVolumeSewRG2_act;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Remaining total volume in reach coming from Sewer. Read by this component and removed"+
                    "from other tracked volume. - input",
            unit = "L"
    )
    public Attribute.Double trackedVolumeSewTotal_act;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Tracked volume from WWTP in reach, to remove from other tracked volume. - input",
            unit = "L"
    )
    public Attribute.Double trackedVolumeWW;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Remaining tracked volume from WWTP in reach, to remove from other tracked volume. - input",
            unit = "L"
    )
    public Attribute.Double trackedVolumeWW_act;
    
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
        // Array des noms des reachs
        double[] run_names = this.names.getValue();
        
        
        double run_track = this.track.getValue();

        int run_ID = (int)run_entity.getDouble("ID");
//        int run_DestID = 0;
//        if(run_DestReach != null){
//            run_DestID = (int)run_DestReach.getDouble("ID");
//        }
        
//        // bugfix
//        if(DestID == 425601){
//            Attribute.Double destReachDoubleTrackedVolumeWW = (Attribute.Double) DestReach.getObject("trackedVolumeWW");
//            double destReachTrackedVolumeWW = destReachDoubleTrackedVolumeWW.getValue();
//            getModel().getRuntime().println("++ ReackTracking ("+ ID +") -- next reach (DestID " + DestID + ") got " + destReachTrackedVolumeWW + " WW. Did this work?");
//        }
        
        int t = 0;
        while (run_names[t] != run_ID) {
            t++;
        }    
        
        double run_actTotalTot = actTotalRD1.getValue() + actTotalRD2.getValue() + actTotalRG1.getValue() + actTotalRG2.getValue();
        actTotalTot.setValue(run_actTotalTot);
                
        double run_RD1out = this.outRD1.getValue();
        double run_RD2out = this.outRD2.getValue();
        double run_RG1out = this.outRG1.getValue();
        double run_RG2out = this.outRG2.getValue();
        double run_cumOutflow = this.simRunoff.getValue();
        

        
//        getModel().getRuntime().println("Processing Reach " + ID + ". 'track' = "+track);
        
      
        //      Lecture des données pour le reach actuel
        //      Volume tracé
        double[] run_ArrayTrackedVolumeRD1 = trackedVolumeRD1Array.getValue();
        double[] run_ArrayTrackedVolumeRD2 = trackedVolumeRD2Array.getValue();
        double[] run_ArrayTrackedVolumeRG1 = trackedVolumeRG1Array.getValue();
        double[] run_ArrayTrackedVolumeRG2 = trackedVolumeRG2Array.getValue();
        double[] run_ArrayTrackedVolumeTotal = trackedVolumeTotalArray.getValue();
        
        //      Volume tracé présent dans le reach
        double[] run_ArrayTrackedVolume_actRD1 = trackedVolumeRD1_actArray.getValue();
        double[] run_ArrayTrackedVolume_actRD2 = trackedVolumeRD2_actArray.getValue();
        double[] run_ArrayTrackedVolume_actRG1 = trackedVolumeRG1_actArray.getValue();
        double[] run_ArrayTrackedVolume_actRG2 = trackedVolumeRG2_actArray.getValue();
        double[] run_ArrayTrackedVolume_actTotal = trackedVolumeTotal_actArray.getValue();
        
        // Liste des indices de Reachs contribuant au brin actuel
        List<Integer> run_listIndex = new ArrayList<Integer>();
//        List<Double> run_trackedIn = new ArrayList<Double>();
        for(int i = 0; i < run_names.length; i++){
            if(run_ArrayTrackedVolumeTotal[i] != -999){
                run_listIndex.add(i);
//                run_trackedIn.add(run_ArrayTrackedVolumeTotal[i]);
            }
        }
        
//        if((ID == 427600) & trackedVolumeWW.getValue() != 0){
////            getModel().getRuntime().println("reach "+ ID + ": t = "+ t + " tracked indexes = " + listIndex); // ATTENTION: the index list "listIndex" includes the current reach!
//            getModel().getRuntime().println("reach "+ ID + ": RD1out = "+ RD1out + " trackedVolumeWW = " + trackedVolumeWW.getValue());
////            if (trackedVolumeWW.getValue() > RD1out){ // this is never the case
////                getModel().getRuntime().println("reach "+ ID + ": RD1out = "+ RD1out + " trackedVolumeWW = " + trackedVolumeWW.getValue());
////            }
//        }

//        getModel().getRuntime().println("contributing tracked reaches = " + listIndex);
//        getModel().getRuntime().println("total tracked volumes entering reach = " + trackedIn);
        
        // Création d'un tableau temporaire où sauvegarder le volume du brin tracé qui ira au reach suivant
        double[] run_destReachArrayTrackedVolumeRD1_temp = new double[run_names.length];
        double[] run_destReachArrayTrackedVolumeRD2_temp = new double[run_names.length];
        double[] run_destReachArrayTrackedVolumeRG1_temp = new double[run_names.length];
        double[] run_destReachArrayTrackedVolumeRG2_temp = new double[run_names.length];
        double[] run_destReachArrayTrackedVolumeTotal_temp = new double[run_names.length];
                    
        // Si il s'agit d'un reach tracé
        if(run_track == 1){
            
//            getModel().getRuntime().println("This reach is tracked");

            // Calcul et transfert du volume tracé provenant du brin
            // Le volume que l'on veut tracer est le volume sortant de ce brin
            run_ArrayTrackedVolumeRD1[t] = run_RD1out;
            run_ArrayTrackedVolumeRD2[t] = run_RD2out;
            run_ArrayTrackedVolumeRG1[t] = run_RG1out;
            run_ArrayTrackedVolumeRG2[t] = run_RG2out;
            run_ArrayTrackedVolumeTotal[t] = run_cumOutflow;  
            
              // Enregistrement temporaire du volume restant dans le brin.
            run_ArrayTrackedVolume_actRD1[t] = actTotalRD1.getValue() - run_RD1out;
            run_ArrayTrackedVolume_actRD2[t] = actTotalRD2.getValue() - run_RD2out;
            run_ArrayTrackedVolume_actRG1[t] = actTotalRG1.getValue() - run_RG1out;
            run_ArrayTrackedVolume_actRG2[t] = actTotalRG2.getValue() - run_RG2out;
            run_ArrayTrackedVolume_actTotal[t] = run_actTotalTot - run_cumOutflow;
            
            // TODO: the following is problematic: it removes the tracked volume from sewers / WWTP from EVERY reach that has some. (so if it is routed over several reaches, it gets removed several times)
            // no, it's actually correct, because tracked outflow is first set to all outflow (incl Sew / WW).
            // test without removing tracked ww / sew volumes from tracked arrays --> has to be removed manually in post-processing!
            // an alternative would be to du this removal in the corresponding WW / Sewer tracking module
            if(trackedVolumeSewTotal.getValue() != 0){ // remove tracked sewer contribution from tracked volume from this reach to avoid double counting it
                
                run_ArrayTrackedVolumeRD1[t] = run_ArrayTrackedVolumeRD1[t] - trackedVolumeSewRD1.getValue();
                run_ArrayTrackedVolumeRD2[t] = run_ArrayTrackedVolumeRD2[t] - trackedVolumeSewRD2.getValue();
                run_ArrayTrackedVolumeRG1[t] = run_ArrayTrackedVolumeRG1[t] - trackedVolumeSewRG1.getValue();
                run_ArrayTrackedVolumeRG2[t] = run_ArrayTrackedVolumeRG2[t] - trackedVolumeSewRG2.getValue();
                run_ArrayTrackedVolumeTotal[t] = run_ArrayTrackedVolumeTotal[t] - trackedVolumeSewTotal.getValue(); 
//                getModel().getRuntime().println("SEW ++ tracked " + trackedVolumeSewTotal.getValue()+" from sewer leaving to next reach (allready added to next reaches inflow), removed it from current ("+ID+") reaches tracked volume, "+ArrayTrackedVolumeTotal[t]+" remaining");
//                getModel().getRuntime().println("tracked " + trackedVolumeWW.getValue()+" from waster water (reach "+ID+").");
                
                run_ArrayTrackedVolume_actRD1[t] = run_ArrayTrackedVolume_actRD1[t] - trackedVolumeSewRD1_act.getValue();
                run_ArrayTrackedVolume_actRD2[t] = run_ArrayTrackedVolume_actRD2[t] - trackedVolumeSewRD2_act.getValue();
                run_ArrayTrackedVolume_actRG1[t] = run_ArrayTrackedVolume_actRG1[t] - trackedVolumeSewRG1_act.getValue();
                run_ArrayTrackedVolume_actRG2[t] = run_ArrayTrackedVolume_actRG2[t] - trackedVolumeSewRG2_act.getValue();
                run_ArrayTrackedVolume_actTotal[t] = run_ArrayTrackedVolume_actTotal[t] - trackedVolumeSewTotal_act.getValue();  
            }
            if(trackedVolumeWW.getValue() != 0){ // if outflow contains waste water, remove it from tracked reach volume
                run_ArrayTrackedVolumeRD1[t] = run_ArrayTrackedVolumeRD1[t] - trackedVolumeWW.getValue(); // WW is stocked as RD1
                run_ArrayTrackedVolumeTotal[t] = run_ArrayTrackedVolumeTotal[t] - trackedVolumeWW.getValue(); // remove from total as well
//                getModel().getRuntime().println("WW  ++ tracked " + trackedVolumeWW.getValue()+" from waste water, removed it from current ("+ID+") reaches tracked volume, "+ArrayTrackedVolumeTotal[t]+" remaining");
                
                // remove from stocked tracked volume as well (which is set to everything stocked in the reach just above)
                run_ArrayTrackedVolume_actRD1[t] = run_ArrayTrackedVolume_actRD1[t] - trackedVolumeWW_act.getValue(); // WW is stocked as RD1
                run_ArrayTrackedVolume_actTotal[t] = run_ArrayTrackedVolume_actTotal[t] - trackedVolumeWW_act.getValue(); // remove from total as well
            }

            // Transfert du volume tracé dans le tableau de sauvegarde pour transfert dans le reach suivant       
            run_destReachArrayTrackedVolumeRD1_temp[t] = run_ArrayTrackedVolumeRD1[t];
            run_destReachArrayTrackedVolumeRD2_temp[t] = run_ArrayTrackedVolumeRD2[t];
            run_destReachArrayTrackedVolumeRG1_temp[t] = run_ArrayTrackedVolumeRG1[t];
            run_destReachArrayTrackedVolumeRG2_temp[t] = run_ArrayTrackedVolumeRG2[t];
            run_destReachArrayTrackedVolumeTotal_temp[t] = run_ArrayTrackedVolumeTotal[t];
            
        }

        // Calcul et transfert des volumes tracés provenant des autres brins

        // La il faut trouver les indices pour lesquels ArrayTrackedVolumeTotal != -999
        // Si il y a une valeur != -999 dans ArrayTrackedVolumeTotal c'est qu'il y 
        // y a une contribution de l'amont. --> on garde les indices 
        // Sinon c'est qu'il n'y en a pas
        for(int i : run_listIndex){
            // ATTENTION: the index list "listIndex" includes the current reach!
            // therefore the current reach needs to be excluded from this block in order to avoid overwriting its tracked volume (which should still correspond to R..out and not to "(ArrayTrackedVolume_actRD1[i]*RD1out)/actTotalRD1.getValue()", as it was allready removed from "ArrayTrackedVolume_actRD1[i]" above)
            if (i != t){ // exclude current reach --> avoids double calculation ( add, recalculate, remove)
//                if((ID == 427600) & trackedVolumeWW.getValue() != 0){
//                    getModel().getRuntime().println(" -- reach "+ ID + ": t = "+ t + " i = " + i + " ArrayTrackedVolumeRD1[i] = "+ ArrayTrackedVolumeRD1[i]);
//                }
    //            if (Arrays.asList(427600,425601,425600,425200,424800,422000,300800,300600,300200,300000,298800,298601,9999).contains(ID) & (i==t)){
    //                getModel().getRuntime().println("reach "+ ID + ": t = "+ t + " i = " + i + " ArrayTrackedVolumeRD1[i] = "+ ArrayTrackedVolumeRD1[i]);
    //            }    
                //   Ajout du nouveau volume tracé au volume tracé déjà présent           
                run_ArrayTrackedVolume_actRD1[i] = run_ArrayTrackedVolumeRD1[i] + run_ArrayTrackedVolume_actRD1[i];
                run_ArrayTrackedVolume_actRD2[i] = run_ArrayTrackedVolumeRD2[i] + run_ArrayTrackedVolume_actRD2[i];
                run_ArrayTrackedVolume_actRG1[i] = run_ArrayTrackedVolumeRG1[i] + run_ArrayTrackedVolume_actRG1[i];
                run_ArrayTrackedVolume_actRG2[i] = run_ArrayTrackedVolumeRG2[i] + run_ArrayTrackedVolume_actRG2[i];
                run_ArrayTrackedVolume_actTotal[i] = run_ArrayTrackedVolumeTotal[i] + run_ArrayTrackedVolume_actTotal[i];

                //   Règle de trois pour calculer le volume tracé sortant du brin
                run_ArrayTrackedVolumeRD1[i] = (run_ArrayTrackedVolume_actRD1[i]*run_RD1out)/actTotalRD1.getValue();
                run_ArrayTrackedVolumeRD2[i] = (run_ArrayTrackedVolume_actRD2[i]*run_RD2out)/actTotalRD2.getValue();
                run_ArrayTrackedVolumeRG1[i] = (run_ArrayTrackedVolume_actRG1[i]*run_RG1out)/actTotalRG1.getValue();
                run_ArrayTrackedVolumeRG2[i] = (run_ArrayTrackedVolume_actRG2[i]*run_RG2out)/actTotalRG2.getValue();
                run_ArrayTrackedVolumeTotal[i] = (run_ArrayTrackedVolume_actTotal[i]*run_cumOutflow)/run_actTotalTot;

                if(actTotalRD1.getValue() == 0){
                    run_ArrayTrackedVolumeRD1[i] = 0;
                }
                if(actTotalRD2.getValue() == 0){
                    run_ArrayTrackedVolumeRD2[i] = 0;
                }
                if(actTotalRG1.getValue() == 0){
                    run_ArrayTrackedVolumeRG1[i] = 0;
                }
                if(actTotalRG2.getValue() == 0){
                    run_ArrayTrackedVolumeRG2[i] = 0;
                }
                if(run_actTotalTot == 0){
                    run_ArrayTrackedVolumeTotal[i] = 0;
                }  

                //    Calcul du volume tracé restant dans le brin
                run_ArrayTrackedVolume_actRD1[i] = run_ArrayTrackedVolume_actRD1[i] - run_ArrayTrackedVolumeRD1[i];
                run_ArrayTrackedVolume_actRD2[i] = run_ArrayTrackedVolume_actRD2[i] - run_ArrayTrackedVolumeRD2[i];
                run_ArrayTrackedVolume_actRG1[i] = run_ArrayTrackedVolume_actRG1[i] - run_ArrayTrackedVolumeRG1[i];
                run_ArrayTrackedVolume_actRG2[i] = run_ArrayTrackedVolume_actRG2[i] - run_ArrayTrackedVolumeRG2[i];
                run_ArrayTrackedVolume_actTotal[i] = run_ArrayTrackedVolume_actTotal[i] - run_ArrayTrackedVolumeTotal[i];

                // Transfert des volumes tracés au brin suivant - 1ere partie
                run_destReachArrayTrackedVolumeRD1_temp[i] = run_ArrayTrackedVolumeRD1[i];
                run_destReachArrayTrackedVolumeRD2_temp[i] = run_ArrayTrackedVolumeRD2[i];
                run_destReachArrayTrackedVolumeRG1_temp[i] = run_ArrayTrackedVolumeRG1[i];
                run_destReachArrayTrackedVolumeRG2_temp[i] = run_ArrayTrackedVolumeRG2[i];
                run_destReachArrayTrackedVolumeTotal_temp[i] = run_ArrayTrackedVolumeTotal[i];
    //            if (Arrays.asList(427600,425601,425600,425200,424800,422000,300800,300600,300200,300000,298800,298601,9999).contains(ID) & (i==t)){
    //                getModel().getRuntime().println("--> [after] reach "+ ID + ": t = "+ t + " i = " + i + " ArrayTrackedVolumeRD1[i] = "+ ArrayTrackedVolumeRD1[i]);
    //            }    

            }
            
        }   
            
//        getModel().getRuntime().println("Sortie du reach = " + Arrays.toString(ArrayTrackedVolumeTotal));
//        if((ID == 427600) & trackedVolumeWW.getValue() != 0){
//            getModel().getRuntime().println("reach "+ ID + ": RD1out = "+ RD1out + ", trackedVolumeWW = " + trackedVolumeWW.getValue() + ", ArrayTrackedVolumeRD1[t] = "+ ArrayTrackedVolumeRD1[t]);
//        }
//        if((ID == 9999) & trackedVolumeWW.getValue() != 0){
//            getModel().getRuntime().println("reach "+ ID + ": RD1out = "+ RD1out + ", trackedVolumeWW = " + trackedVolumeWW.getValue() + ", ArrayTrackedVolumeRD1[t] = "+ ArrayTrackedVolumeRD1[t]);
//        }
//        if(Arrays.asList(427600,425601,425600,425200,424800,422000,300800,300600,300200,300000,298800,298601,9999).contains(ID) & trackedVolumeWW.getValue() != 0){
//            getModel().getRuntime().println(" -- reach "+ ID + ": RD1out = "+ RD1out + ", trackedVolumeWW = " + trackedVolumeWW.getValue() + ", ArrayTrackedVolumeRD1[t] = "+ ArrayTrackedVolumeRD1[t]);
//        }
        
        // Enregistrement du tableau des volumes tracés
        trackedVolumeRD1Array.setValue(run_ArrayTrackedVolumeRD1);
        trackedVolumeRD2Array.setValue(run_ArrayTrackedVolumeRD2);
        trackedVolumeRG1Array.setValue(run_ArrayTrackedVolumeRG1);
        trackedVolumeRG2Array.setValue(run_ArrayTrackedVolumeRG2);
        trackedVolumeTotalArray.setValue(run_ArrayTrackedVolumeTotal);

        // Enregistrement du tableau des volumes volume tracé restant
        trackedVolumeRD1_actArray.setValue(run_ArrayTrackedVolume_actRD1);
        trackedVolumeRD2_actArray.setValue(run_ArrayTrackedVolume_actRD2);
        trackedVolumeRG1_actArray.setValue(run_ArrayTrackedVolume_actRG1);
        trackedVolumeRG2_actArray.setValue(run_ArrayTrackedVolume_actRG2);
        trackedVolumeTotal_actArray.setValue(run_ArrayTrackedVolume_actTotal);
        
        
        //  Sauvegarde des volumes tracés dans le tableau du reach de destination
        if(run_DestReach != null){
            // Lecture du tableau du reach de destination
            Attribute.DoubleArray run_destReachDoubleArrayTrackedVolumeRD1 = (Attribute.DoubleArray) run_DestReach.getObject("trackedVolumeRD1Array");
            Attribute.DoubleArray run_destReachDoubleArrayTrackedVolumeRD2 = (Attribute.DoubleArray) run_DestReach.getObject("trackedVolumeRD2Array");
            Attribute.DoubleArray run_destReachDoubleArrayTrackedVolumeRG1 = (Attribute.DoubleArray) run_DestReach.getObject("trackedVolumeRG1Array");
            Attribute.DoubleArray run_destReachDoubleArrayTrackedVolumeRG2 = (Attribute.DoubleArray) run_DestReach.getObject("trackedVolumeRG2Array");
            Attribute.DoubleArray run_destReachDoubleArrayTrackedVolumeTotal = (Attribute.DoubleArray) run_DestReach.getObject("trackedVolumeTotalArray");
            // Conversion de DoubleArray à double[]
            double[] run_destReachArrayTrackedVolumeRD1 = run_destReachDoubleArrayTrackedVolumeRD1.getValue();
            double[] run_destReachArrayTrackedVolumeRD2 = run_destReachDoubleArrayTrackedVolumeRD2.getValue();
            double[] run_destReachArrayTrackedVolumeRG1 = run_destReachDoubleArrayTrackedVolumeRG1.getValue();
            double[] run_destReachArrayTrackedVolumeRG2 = run_destReachDoubleArrayTrackedVolumeRG2.getValue();
            double[] run_destReachArrayTrackedVolumeTotal = run_destReachDoubleArrayTrackedVolumeTotal.getValue();  
            
            // Transfert des volumes tracés sauvegardés, uniquement pour les reach concernés. Pour ne pas écraser d'autres infos du reach suivant
            if(run_track == 1){
                run_destReachArrayTrackedVolumeRD1[t] = run_destReachArrayTrackedVolumeRD1_temp[t];
                run_destReachArrayTrackedVolumeRD2[t] = run_destReachArrayTrackedVolumeRD2_temp[t];
                run_destReachArrayTrackedVolumeRG1[t] = run_destReachArrayTrackedVolumeRG1_temp[t];
                run_destReachArrayTrackedVolumeRG2[t] = run_destReachArrayTrackedVolumeRG2_temp[t];
                run_destReachArrayTrackedVolumeTotal[t] = run_destReachArrayTrackedVolumeTotal_temp[t];                
            }
            for(int i : run_listIndex){
                run_destReachArrayTrackedVolumeRD1[i] = run_destReachArrayTrackedVolumeRD1_temp[i];
                run_destReachArrayTrackedVolumeRD2[i] = run_destReachArrayTrackedVolumeRD2_temp[i];
                run_destReachArrayTrackedVolumeRG1[i] = run_destReachArrayTrackedVolumeRG1_temp[i];
                run_destReachArrayTrackedVolumeRG2[i] = run_destReachArrayTrackedVolumeRG2_temp[i];
                run_destReachArrayTrackedVolumeTotal[i] = run_destReachArrayTrackedVolumeTotal_temp[i];
            }
            
            //  Transfert du volume tracé dans l'array du reach suivant      
            run_destReachDoubleArrayTrackedVolumeRD1.setValue(run_destReachArrayTrackedVolumeRD1);
            run_destReachDoubleArrayTrackedVolumeRD2.setValue(run_destReachArrayTrackedVolumeRD2);
            run_destReachDoubleArrayTrackedVolumeRG1.setValue(run_destReachArrayTrackedVolumeRG1);
            run_destReachDoubleArrayTrackedVolumeRG2.setValue(run_destReachArrayTrackedVolumeRG2);
            run_destReachDoubleArrayTrackedVolumeTotal.setValue(run_destReachArrayTrackedVolumeTotal);
            run_DestReach.setObject("trackedVolumeRD1Array", run_destReachDoubleArrayTrackedVolumeRD1);
            run_DestReach.setObject("TrackedVolumeRD2Array", run_destReachDoubleArrayTrackedVolumeRD2);
            run_DestReach.setObject("TrackedVolumeRG1Array", run_destReachDoubleArrayTrackedVolumeRG1);
            run_DestReach.setObject("TrackedVolumeRG2Array", run_destReachDoubleArrayTrackedVolumeRG2);
            run_DestReach.setObject("TrackedVolumeTotalArray", run_destReachDoubleArrayTrackedVolumeTotal);            
        }
//        // bugfix
//        if(DestID == 425601){
//            Attribute.Double destReachDoubleTrackedVolumeWW = (Attribute.Double) DestReach.getObject("trackedVolumeWW");
//            double destReachTrackedVolumeWW = destReachDoubleTrackedVolumeWW.getValue();
//            getModel().getRuntime().println("++ ReackTracking END -- next reach (DestID " + DestID + ") got " + destReachTrackedVolumeWW + " WW. Did this work?");
//        }
    }   

}

