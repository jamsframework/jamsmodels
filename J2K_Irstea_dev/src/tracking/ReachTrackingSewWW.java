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
        description = "Calcule the volume contribution from all Reaches to the output discharge."
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
            description = "Tracked volume RD1 array"
            )
    public Attribute.DoubleArray trackedVolumeRD1Array;
        
        @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "Tracked volume RD2 array"
            )
    public Attribute.DoubleArray trackedVolumeRD2Array;  
        
        @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "Tracked volume RG1 array"
            )
    public Attribute.DoubleArray trackedVolumeRG1Array;  
        
        @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "Tracked volume RG2 array"
            )
    public Attribute.DoubleArray trackedVolumeRG2Array;  
        
        @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "Tracked volume Total array"
            )
    public Attribute.DoubleArray trackedVolumeTotalArray;   
             
            @JAMSVarDescription(
        access = JAMSVarDescription.AccessType.READWRITE,
        description = "Array of RD1 remaining volume from tracked reach in actual reach after routing"
        )
    public Attribute.DoubleArray trackedVolumeRD1_actArray; 
             
            @JAMSVarDescription(
        access = JAMSVarDescription.AccessType.READWRITE,
        description = "Array of RD2 remaining volume from tracked reach in actual reach after routing"
        )
    public Attribute.DoubleArray trackedVolumeRD2_actArray; 
             
            @JAMSVarDescription(
        access = JAMSVarDescription.AccessType.READWRITE,
        description = "Array of RG1 remaining volume from tracked reach in actual reach after routing"
        )
    public Attribute.DoubleArray trackedVolumeRG1_actArray; 
             
            @JAMSVarDescription(
        access = JAMSVarDescription.AccessType.READWRITE,
        description = "Array of RG2 remaining volume from tracked reach in actual reach after routing"
        )
    public Attribute.DoubleArray trackedVolumeRG2_actArray;
            
            @JAMSVarDescription(
        access = JAMSVarDescription.AccessType.READWRITE,
        description = "Array of total remaining volume from tracked reach in actual reach after routing"
        )
    public Attribute.DoubleArray trackedVolumeTotal_actArray;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "reach track or not"
    )
    public Attribute.Double track;    

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Tracked RD1 volume from Sewer in reach",
            unit = "L"
    )
    public Attribute.Double trackedVolumeSewRD1;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Tracked RD2 volume from Sewer in reach",
            unit = "L"
    )
    public Attribute.Double trackedVolumeSewRD2;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Tracked RG1 volume from Sewer in reach",
            unit = "L"
    )
    public Attribute.Double trackedVolumeSewRG1;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Tracked RG2 volume from Sewer in reach",
            unit = "L"
    )
    public Attribute.Double trackedVolumeSewRG2;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Tracked total volume from Sewer in reach",
            unit = "L"
    )
    public Attribute.Double trackedVolumeSewTotal; 
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Remaining tracked RD1 volume from Sewer",
            unit = "L"
    )
    public Attribute.Double trackedVolumeSewRD1_act;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Remaining tracked RD2 volume from Sewer in reach",
            unit = "L"
    )
    public Attribute.Double trackedVolumeSewRD2_act;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Remaining tracked RG1 volume from Sewer in reach",
            unit = "L"
    )
    public Attribute.Double trackedVolumeSewRG1_act;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Remaining tracked RG2 volume from Sewer in reach",
            unit = "L"
    )
    public Attribute.Double trackedVolumeSewRG2_act;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Remaining tracked total volume from Sewer in reach",
            unit = "L"
    )
    public Attribute.Double trackedVolumeSewTotal_act;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Tracked volume from WWTP in reach",
            unit = "L"
    )
    public Attribute.Double trackedVolumeWW;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Remaining tracked volume from WWTP in reach",
            unit = "L"
    )
    public Attribute.Double trackedVolumeWW_act;
    
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
        
        
        double track = this.track.getValue();

        int ID = (int)entity.getDouble("ID");
        int DestID = 0;
        if(DestReach != null){
            DestID = (int)DestReach.getDouble("ID");
        }
        
//        // bugfix
//        if(DestID == 425601){
//            Attribute.Double destReachDoubleTrackedVolumeWW = (Attribute.Double) DestReach.getObject("trackedVolumeWW");
//            double destReachTrackedVolumeWW = destReachDoubleTrackedVolumeWW.getValue();
//            getModel().getRuntime().println("++ ReackTracking ("+ ID +") -- next reach (DestID " + DestID + ") got " + destReachTrackedVolumeWW + " WW. Did this work?");
//        }
        
        int t = 0;
        while (Nom[t] != ID) {
            t++;
        }    
        
        double actTotalTot = actTotalRD1.getValue() + actTotalRD2.getValue() + actTotalRG1.getValue() + actTotalRG2.getValue();
                
        double RD1out = this.outRD1.getValue();
        double RD2out = this.outRD2.getValue();
        double RG1out = this.outRG1.getValue();
        double RG2out = this.outRG2.getValue();
        double cumOutflow = this.simRunoff.getValue();
        

        
//        getModel().getRuntime().println("Processing Reach " + ID + ". 'track' = "+track);
        
      
        //      Lecture des données pour le reach actuel
        //      Volume tracé
        double[] ArrayTrackedVolumeRD1 = trackedVolumeRD1Array.getValue();
        double[] ArrayTrackedVolumeRD2 = trackedVolumeRD2Array.getValue();
        double[] ArrayTrackedVolumeRG1 = trackedVolumeRG1Array.getValue();
        double[] ArrayTrackedVolumeRG2 = trackedVolumeRG2Array.getValue();
        double[] ArrayTrackedVolumeTotal = trackedVolumeTotalArray.getValue();
        
        //      Volume tracé présent dans le reach
        double[] ArrayTrackedVolume_actRD1 = trackedVolumeRD1_actArray.getValue();
        double[] ArrayTrackedVolume_actRD2 = trackedVolumeRD2_actArray.getValue();
        double[] ArrayTrackedVolume_actRG1 = trackedVolumeRG1_actArray.getValue();
        double[] ArrayTrackedVolume_actRG2 = trackedVolumeRG2_actArray.getValue();
        double[] ArrayTrackedVolume_actTotal = trackedVolumeTotal_actArray.getValue();
        
        // Liste des indices de Reachs contribuant au brin actuel
        List<Integer> listIndex = new ArrayList<Integer>();
        List<Double> trackedIn = new ArrayList<Double>();
        for(int i = 0; i < Nom.length; i++){
            if(ArrayTrackedVolumeTotal[i] != -999){
                listIndex.add(i);
                trackedIn.add(ArrayTrackedVolumeTotal[i]);
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
        double[] destReachArrayTrackedVolumeRD1_temp = new double[Nom.length];
        double[] destReachArrayTrackedVolumeRD2_temp = new double[Nom.length];
        double[] destReachArrayTrackedVolumeRG1_temp = new double[Nom.length];
        double[] destReachArrayTrackedVolumeRG2_temp = new double[Nom.length];
        double[] destReachArrayTrackedVolumeTotal_temp = new double[Nom.length];
                    
        // Si il s'agit d'un reach tracé
        if(track == 1){
            
//            getModel().getRuntime().println("This reach is tracked");

            // Calcul et transfert du volume tracé provenant du brin
            // Le volume que l'on veut tracer est le volume sortant de ce brin
            ArrayTrackedVolumeRD1[t] = RD1out;
            ArrayTrackedVolumeRD2[t] = RD2out;
            ArrayTrackedVolumeRG1[t] = RG1out;
            ArrayTrackedVolumeRG2[t] = RG2out;
            ArrayTrackedVolumeTotal[t] = cumOutflow;  
            
              // Enregistrement temporaire du volume restant dans le brin.
            ArrayTrackedVolume_actRD1[t] = actTotalRD1.getValue() - RD1out;
            ArrayTrackedVolume_actRD2[t] = actTotalRD2.getValue() - RD2out;
            ArrayTrackedVolume_actRG1[t] = actTotalRG1.getValue() - RG1out;
            ArrayTrackedVolume_actRG2[t] = actTotalRG2.getValue() - RG2out;
            ArrayTrackedVolume_actTotal[t] = actTotalTot - cumOutflow;
            
            // TODO: the following is problematic: it removes the tracked volume from sewers / WWTP from EVERY reach that has some. (so if it is routed over several reaches, it gets removed several times)
            // no, it's actually correct, because tracked outflow is first set to all outflow (incl Sew / WW).
            // test without removing tracked ww / sew volumes from tracked arrays --> has to be removed manually in post-processing!
            // an alternative would be to du this removal in the corresponding WW / Sewer tracking module
            if(trackedVolumeSewTotal.getValue() != 0){ // remove tracked sewer contribution from tracked volume from this reach to avoid double counting it
                
                ArrayTrackedVolumeRD1[t] = ArrayTrackedVolumeRD1[t] - trackedVolumeSewRD1.getValue();
                ArrayTrackedVolumeRD2[t] = ArrayTrackedVolumeRD2[t] - trackedVolumeSewRD2.getValue();
                ArrayTrackedVolumeRG1[t] = ArrayTrackedVolumeRG1[t] - trackedVolumeSewRG1.getValue();
                ArrayTrackedVolumeRG2[t] = ArrayTrackedVolumeRG2[t] - trackedVolumeSewRG2.getValue();
                ArrayTrackedVolumeTotal[t] = ArrayTrackedVolumeTotal[t] - trackedVolumeSewTotal.getValue(); 
//                getModel().getRuntime().println("SEW ++ tracked " + trackedVolumeSewTotal.getValue()+" from sewer leaving to next reach (allready added to next reaches inflow), removed it from current ("+ID+") reaches tracked volume, "+ArrayTrackedVolumeTotal[t]+" remaining");
//                getModel().getRuntime().println("tracked " + trackedVolumeWW.getValue()+" from waster water (reach "+ID+").");
                
                ArrayTrackedVolume_actRD1[t] = ArrayTrackedVolume_actRD1[t] - trackedVolumeSewRD1_act.getValue();
                ArrayTrackedVolume_actRD2[t] = ArrayTrackedVolume_actRD2[t] - trackedVolumeSewRD2_act.getValue();
                ArrayTrackedVolume_actRG1[t] = ArrayTrackedVolume_actRG1[t] - trackedVolumeSewRG1_act.getValue();
                ArrayTrackedVolume_actRG2[t] = ArrayTrackedVolume_actRG2[t] - trackedVolumeSewRG2_act.getValue();
                ArrayTrackedVolume_actTotal[t] = ArrayTrackedVolume_actTotal[t] - trackedVolumeSewTotal_act.getValue();  
            }
            if(trackedVolumeWW.getValue() != 0){ // if outflow contains waste water, remove it from tracked reach volume
                ArrayTrackedVolumeRD1[t] = ArrayTrackedVolumeRD1[t] - trackedVolumeWW.getValue(); // WW is stocked as RD1
                ArrayTrackedVolumeTotal[t] = ArrayTrackedVolumeTotal[t] - trackedVolumeWW.getValue(); // remove from total as well
//                getModel().getRuntime().println("WW  ++ tracked " + trackedVolumeWW.getValue()+" from waste water, removed it from current ("+ID+") reaches tracked volume, "+ArrayTrackedVolumeTotal[t]+" remaining");
                
                // remove from stocked tracked volume as well (which is set to everything stocked in the reach just above)
                ArrayTrackedVolume_actRD1[t] = ArrayTrackedVolume_actRD1[t] - trackedVolumeWW_act.getValue(); // WW is stocked as RD1
                ArrayTrackedVolume_actTotal[t] = ArrayTrackedVolume_actTotal[t] - trackedVolumeWW_act.getValue(); // remove from total as well
            }

            // Transfert du volume tracé dans le tableau de sauvegarde pour transfert dans le reach suivant       
            destReachArrayTrackedVolumeRD1_temp[t] = ArrayTrackedVolumeRD1[t];
            destReachArrayTrackedVolumeRD2_temp[t] = ArrayTrackedVolumeRD2[t];
            destReachArrayTrackedVolumeRG1_temp[t] = ArrayTrackedVolumeRG1[t];
            destReachArrayTrackedVolumeRG2_temp[t] = ArrayTrackedVolumeRG2[t];
            destReachArrayTrackedVolumeTotal_temp[t] = ArrayTrackedVolumeTotal[t];
            
        }

        // Calcul et transfert des volumes tracés provenant des autres brins

        // La il faut trouver les indices pour lesquels ArrayTrackedVolumeTotal != -999
        // Si il y a une valeur != -999 dans ArrayTrackedVolumeTotal c'est qu'il y 
        // y a une contribution de l'amont. --> on garde les indices 
        // Sinon c'est qu'il n'y en a pas
        for(int i : listIndex){
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
                ArrayTrackedVolume_actRD1[i] = ArrayTrackedVolumeRD1[i] + ArrayTrackedVolume_actRD1[i];
                ArrayTrackedVolume_actRD2[i] = ArrayTrackedVolumeRD2[i] + ArrayTrackedVolume_actRD2[i];
                ArrayTrackedVolume_actRG1[i] = ArrayTrackedVolumeRG1[i] + ArrayTrackedVolume_actRG1[i];
                ArrayTrackedVolume_actRG2[i] = ArrayTrackedVolumeRG2[i] + ArrayTrackedVolume_actRG2[i];
                ArrayTrackedVolume_actTotal[i] = ArrayTrackedVolumeTotal[i] + ArrayTrackedVolume_actTotal[i];

                //   Règle de trois pour calculer le volume tracé sortant du brin
                ArrayTrackedVolumeRD1[i] = (ArrayTrackedVolume_actRD1[i]*RD1out)/actTotalRD1.getValue();
                ArrayTrackedVolumeRD2[i] = (ArrayTrackedVolume_actRD2[i]*RD2out)/actTotalRD2.getValue();
                ArrayTrackedVolumeRG1[i] = (ArrayTrackedVolume_actRG1[i]*RG1out)/actTotalRG1.getValue();
                ArrayTrackedVolumeRG2[i] = (ArrayTrackedVolume_actRG2[i]*RG2out)/actTotalRG2.getValue();
                ArrayTrackedVolumeTotal[i] = (ArrayTrackedVolume_actTotal[i]*cumOutflow)/actTotalTot;

                if(actTotalRD1.getValue() == 0){
                    ArrayTrackedVolumeRD1[i] = 0;
                }
                if(actTotalRD2.getValue() == 0){
                    ArrayTrackedVolumeRD2[i] = 0;
                }
                if(actTotalRG1.getValue() == 0){
                    ArrayTrackedVolumeRG1[i] = 0;
                }
                if(actTotalRG2.getValue() == 0){
                    ArrayTrackedVolumeRG2[i] = 0;
                }
                if(actTotalTot == 0){
                    ArrayTrackedVolumeTotal[i] = 0;
                }  

                //    Calcul du volume tracé restant dans le brin
                ArrayTrackedVolume_actRD1[i] = ArrayTrackedVolume_actRD1[i] - ArrayTrackedVolumeRD1[i];
                ArrayTrackedVolume_actRD2[i] = ArrayTrackedVolume_actRD2[i] - ArrayTrackedVolumeRD2[i];
                ArrayTrackedVolume_actRG1[i] = ArrayTrackedVolume_actRG1[i] - ArrayTrackedVolumeRG1[i];
                ArrayTrackedVolume_actRG2[i] = ArrayTrackedVolume_actRG2[i] - ArrayTrackedVolumeRG2[i];
                ArrayTrackedVolume_actTotal[i] = ArrayTrackedVolume_actTotal[i] - ArrayTrackedVolumeTotal[i];

                // Transfert des volumes tracés au brin suivant - 1ere partie
                destReachArrayTrackedVolumeRD1_temp[i] = ArrayTrackedVolumeRD1[i];
                destReachArrayTrackedVolumeRD2_temp[i] = ArrayTrackedVolumeRD2[i];
                destReachArrayTrackedVolumeRG1_temp[i] = ArrayTrackedVolumeRG1[i];
                destReachArrayTrackedVolumeRG2_temp[i] = ArrayTrackedVolumeRG2[i];
                destReachArrayTrackedVolumeTotal_temp[i] = ArrayTrackedVolumeTotal[i];
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
        trackedVolumeRD1Array.setValue(ArrayTrackedVolumeRD1);
        trackedVolumeRD2Array.setValue(ArrayTrackedVolumeRD2);
        trackedVolumeRG1Array.setValue(ArrayTrackedVolumeRG1);
        trackedVolumeRG2Array.setValue(ArrayTrackedVolumeRG2);
        trackedVolumeTotalArray.setValue(ArrayTrackedVolumeTotal);

        // Enregistrement du tableau des volumes volume tracé restant
        trackedVolumeRD1_actArray.setValue(ArrayTrackedVolume_actRD1);
        trackedVolumeRD2_actArray.setValue(ArrayTrackedVolume_actRD2);
        trackedVolumeRG1_actArray.setValue(ArrayTrackedVolume_actRG1);
        trackedVolumeRG2_actArray.setValue(ArrayTrackedVolume_actRG2);
        trackedVolumeTotal_actArray.setValue(ArrayTrackedVolume_actTotal);
        
        
        //  Sauvegarde des volumes tracés dans le tableau du reach de destination
        if(DestReach != null){
            // Lecture du tableau du reach de destination
            Attribute.DoubleArray destReachDoubleArrayTrackedVolumeRD1 = (Attribute.DoubleArray) DestReach.getObject("trackedVolumeRD1Array");
            Attribute.DoubleArray destReachDoubleArrayTrackedVolumeRD2 = (Attribute.DoubleArray) DestReach.getObject("trackedVolumeRD2Array");
            Attribute.DoubleArray destReachDoubleArrayTrackedVolumeRG1 = (Attribute.DoubleArray) DestReach.getObject("trackedVolumeRG1Array");
            Attribute.DoubleArray destReachDoubleArrayTrackedVolumeRG2 = (Attribute.DoubleArray) DestReach.getObject("trackedVolumeRG2Array");
            Attribute.DoubleArray destReachDoubleArrayTrackedVolumeTotal = (Attribute.DoubleArray) DestReach.getObject("trackedVolumeTotalArray");
            // Conversion de DoubleArray à double[]
            double[] destReachArrayTrackedVolumeRD1 = destReachDoubleArrayTrackedVolumeRD1.getValue();
            double[] destReachArrayTrackedVolumeRD2 = destReachDoubleArrayTrackedVolumeRD2.getValue();
            double[] destReachArrayTrackedVolumeRG1 = destReachDoubleArrayTrackedVolumeRG1.getValue();
            double[] destReachArrayTrackedVolumeRG2 = destReachDoubleArrayTrackedVolumeRG2.getValue();
            double[] destReachArrayTrackedVolumeTotal = destReachDoubleArrayTrackedVolumeTotal.getValue();  
            
            // Transfert des volumes tracés sauvegardés, uniquement pour les reach concernés. Pour ne pas écraser d'autres infos du reach suivant
            if(track == 1){
                destReachArrayTrackedVolumeRD1[t] = destReachArrayTrackedVolumeRD1_temp[t];
                destReachArrayTrackedVolumeRD2[t] = destReachArrayTrackedVolumeRD2_temp[t];
                destReachArrayTrackedVolumeRG1[t] = destReachArrayTrackedVolumeRG1_temp[t];
                destReachArrayTrackedVolumeRG2[t] = destReachArrayTrackedVolumeRG2_temp[t];
                destReachArrayTrackedVolumeTotal[t] = destReachArrayTrackedVolumeTotal_temp[t];                
            }
            for(int i : listIndex){
                destReachArrayTrackedVolumeRD1[i] = destReachArrayTrackedVolumeRD1_temp[i];
                destReachArrayTrackedVolumeRD2[i] = destReachArrayTrackedVolumeRD2_temp[i];
                destReachArrayTrackedVolumeRG1[i] = destReachArrayTrackedVolumeRG1_temp[i];
                destReachArrayTrackedVolumeRG2[i] = destReachArrayTrackedVolumeRG2_temp[i];
                destReachArrayTrackedVolumeTotal[i] = destReachArrayTrackedVolumeTotal_temp[i];
            }
            
            //  Transfert du volume tracé dans l'array du reach suivant      
            destReachDoubleArrayTrackedVolumeRD1.setValue(destReachArrayTrackedVolumeRD1);
            destReachDoubleArrayTrackedVolumeRD2.setValue(destReachArrayTrackedVolumeRD2);
            destReachDoubleArrayTrackedVolumeRG1.setValue(destReachArrayTrackedVolumeRG1);
            destReachDoubleArrayTrackedVolumeRG2.setValue(destReachArrayTrackedVolumeRG2);
            destReachDoubleArrayTrackedVolumeTotal.setValue(destReachArrayTrackedVolumeTotal);
            DestReach.setObject("trackedVolumeRD1Array", destReachDoubleArrayTrackedVolumeRD1);
            DestReach.setObject("TrackedVolumeRD2Array", destReachDoubleArrayTrackedVolumeRD2);
            DestReach.setObject("TrackedVolumeRG1Array", destReachDoubleArrayTrackedVolumeRG1);
            DestReach.setObject("TrackedVolumeRG2Array", destReachDoubleArrayTrackedVolumeRG2);
            DestReach.setObject("TrackedVolumeTotalArray", destReachDoubleArrayTrackedVolumeTotal);            
        }
//        // bugfix
//        if(DestID == 425601){
//            Attribute.Double destReachDoubleTrackedVolumeWW = (Attribute.Double) DestReach.getObject("trackedVolumeWW");
//            double destReachTrackedVolumeWW = destReachDoubleTrackedVolumeWW.getValue();
//            getModel().getRuntime().println("++ ReackTracking END -- next reach (DestID " + DestID + ") got " + destReachTrackedVolumeWW + " WW. Did this work?");
//        }
    }   

}

