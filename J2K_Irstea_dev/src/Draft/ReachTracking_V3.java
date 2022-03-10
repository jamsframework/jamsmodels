package Draft;

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
        title = "ReachTracking",
        author = "Olivier Grandjouan",
        description = "Calcule the volume contribution from all Reaches to the output discharge",
        version = "1.0",
        date = "2022-03-07"
)

public class ReachTracking_V3 extends JAMSComponent {
    
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
    public Attribute.Double trackedVolumeRD1;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "RD2 contribution from tracked reach",
            unit = "L",
            defaultValue = "0"
    )
    public Attribute.Double trackedVolumeRD2;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "RG1 contribution from tracked reach",
            unit = "L",
            defaultValue = "0"
    )
    public Attribute.Double trackedVolumeRG1;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "RG2 contribution from tracked reach",
            unit = "L",
            defaultValue = "0"
    )
    public Attribute.Double trackedVolumeRG2;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "contribution from tracked reach in total simulated runoff",
            unit = "L",
            defaultValue = "0"
    )
    public Attribute.Double trackedVolumeTotal;  
   
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Tracked reach"
    )
    public Attribute.Integer trackedReach;
    
        @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "Tracked volume RD1 array",
            unit = "L"
            )
    public Attribute.DoubleArray trackedVolumeRD1Array;
        
        @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "Tracked volume RD2 array",
            unit = "L",
            defaultValue = "0"
            )
    public Attribute.DoubleArray trackedVolumeRD2Array;  
        
        @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "Tracked volume RG1 array",
            unit = "L",
            defaultValue = "0"
            )
    public Attribute.DoubleArray trackedVolumeRG1Array;  
        
        @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "Tracked volume RG2 array",
            unit = "L",
            defaultValue = "0"
            )
    public Attribute.DoubleArray trackedVolumeRG2Array;  
        
        @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "Tracked volume Total array",
            unit = "L",
            defaultValue = "0"
            )
    public Attribute.DoubleArray trackedVolumeTotalArray;   
             
            @JAMSVarDescription(
        access = JAMSVarDescription.AccessType.READWRITE,
        description = "Array of RD1 remaining volume from tracked reach in actual reach after routing",
            unit = "L",
            defaultValue = "0"
        )
    public Attribute.DoubleArray trackedVolumeRD1_actArray; 
             
            @JAMSVarDescription(
        access = JAMSVarDescription.AccessType.READWRITE,
        description = "Array of RD2 remaining volume from tracked reach in actual reach after routing",
            unit = "L",
            defaultValue = "0"
        )
    public Attribute.DoubleArray trackedVolumeRD2_actArray; 
             
            @JAMSVarDescription(
        access = JAMSVarDescription.AccessType.READWRITE,
        description = "Array of RG1 remaining volume from tracked reach in actual reach after routing",
            unit = "L",
            defaultValue = "0"
        )
    public Attribute.DoubleArray trackedVolumeRG1_actArray; 
             
            @JAMSVarDescription(
        access = JAMSVarDescription.AccessType.READWRITE,
        description = "Array of RG2 remaining volume from tracked reach in actual reach after routing",
            unit = "L",
            defaultValue = "0"
        )
    public Attribute.DoubleArray trackedVolumeRG2_actArray;
            
            @JAMSVarDescription(
        access = JAMSVarDescription.AccessType.READWRITE,
        description = "Array of total remaining volume from tracked reach in actual reach after routing",
            unit = "L",
            defaultValue = "0"
        )
    public Attribute.DoubleArray trackedVolumeTotal_actArray;
            
        @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
        description = "Array of station names")
    public Attribute.DoubleArray names;   
           
        @JAMSVarDescription(access = JAMSVarDescription.AccessType.READWRITE,
        description = "List of upstream reaches")
    public Attribute.IntegerArray IndexListUpstreamReach;        

    public void init(){
    
//    double[] Nom = this.names.getValue();
//    int t = Nom.length;
//
////  Création et sauvegarde des tableaux pour les volumes tracés et restants au début du modèle
//    double[] ArrayTrackedVolumeRD1 = new double[t];
//    double[] ArrayTrackedVolumeRD2 = new double[t];
//    double[] ArrayTrackedVolumeRG1 = new double[t];
//    double[] ArrayTrackedVolumeRG2 = new double[t];
//    double[] ArrayTrackedVolumeTotal = new double[t];
//
//    double[] ArrayTrackedVolume_actRD1 = new double[t];
//    double[] ArrayTrackedVolume_actRD2 = new double[t];
//    double[] ArrayTrackedVolume_actRG1 = new double[t];
//    double[] ArrayTrackedVolume_actRG2 = new double[t];
//    double[] ArrayTrackedVolume_actTotal = new double[t];
//    
//    
//    getModel().getRuntime().println("INIT");
//    getModel().getRuntime().println("Contenu de ArrayTrackedVolumeRD1 : " + Arrays.toString(Nom));
//    getModel().getRuntime().println("Contenu de ArrayTrackedVolumeRD1 : " +Arrays.toString(ArrayTrackedVolumeRD1));
//
//    trackedVolumeRD1Array.setValue(ArrayTrackedVolumeRD1);
//    trackedVolumeRD2Array.setValue(ArrayTrackedVolumeRD2);
//    trackedVolumeRG1Array.setValue(ArrayTrackedVolumeRG1);
//    trackedVolumeRG2Array.setValue(ArrayTrackedVolumeRG2);
//    trackedVolumeTotalArray.setValue(ArrayTrackedVolumeTotal);
//
//    trackedVolumeRD1_actArray.setValue(ArrayTrackedVolume_actRD1);
//    trackedVolumeRD2_actArray.setValue(ArrayTrackedVolume_actRD2);
//    trackedVolumeRG1_actArray.setValue(ArrayTrackedVolume_actRG1);
//    trackedVolumeRG2_actArray.setValue(ArrayTrackedVolume_actRG2);
//
//    getModel().getRuntime().println("TrackedVolumeRD1Array : " + Arrays.toString(trackedVolumeRD1Array.getValue()));
        
    } 
    
    public void run(){
                
        Attribute.Entity entity = entities.getCurrent();
        Attribute.Entity DestReach = (Attribute.Entity) entity.getObject("to_reach");        
        if (DestReach.isEmpty()) {
                    DestReach = null;
                }
        // Array des noms des reachs
        double[] Nom = this.names.getValue();
        
        int ID = (int)entity.getDouble("ID");
        int DestID = 0;
        if(DestReach != null){
            DestID = (int)DestReach.getDouble("ID");
        }
        int ReachTracked = this.trackedReach.getValue();
        // Index du reach tracé
        int r = 0;
        while (Nom[r] != ReachTracked) {
            r++;
        }        
        int t = 0;
        while (Nom[t] != ID) {
            t++;
        }    
        
        double actTotTemp = actRD1Temp.getValue() + actRD2Temp.getValue() + actRG1Temp.getValue() + actRG2Temp.getValue();
                
        double RD1out = this.outRD1.getValue();
        double RD2out = this.outRD2.getValue();
        double RG1out = this.outRG1.getValue();
        double RG2out = this.outRG2.getValue();
        double cumOutflow = this.simRunoff.getValue();
        

        
        getModel().getRuntime().println("Processing Reach " + ID);
        
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
        
        // Est-ce que le reach contient des données de débit ou non?
        // Si c'est true, toutes les données sont égales à -999
        // Si c'est false, il y a une donnée différente qui correspond à un volume d'eau
        boolean areAllSame = AreAllSame(ArrayTrackedVolumeTotal);
        
//        double RD1TrackedVolume = 0;
//        double RD2TrackedVolume = 0;
//        double RG1TrackedVolume = 0;
//        double RG2TrackedVolume = 0;
//        double TotalTrackedVolume = 0;
//        

//        ArrayTrackedVolumeRD1[t] = RD1out;
//        ArrayTrackedVolumeRD2[t] = RD2out;
//        ArrayTrackedVolumeRG1[t] = RG1out;
//        ArrayTrackedVolumeRG2[t] = RG2out;
//        ArrayTrackedVolumeTotal[t] = cumOutflow;

        getModel().getRuntime().println("Entrée ArrayTrackedVolumeTotal = " + Arrays.toString(ArrayTrackedVolumeTotal));

        // Liste des indices de Reachs contribuant à chaque Reach
        List<Integer> listIndex = new ArrayList<Integer>();
        for(int i = 0; i < Nom.length; i++){
            if(ArrayTrackedVolumeTotal[i] != -999){
                listIndex.add(i);
            }
        }
        getModel().getRuntime().println("Elements  contribuant à ce reach = " + listIndex);

        // Liste des indices de reach amonts
        int[] ArrayIndexUpstreamReach = IndexListUpstreamReach.getValue();
        List<Integer> listIndexUpstreamReach = new ArrayList<Integer>(ArrayIndexUpstreamReach.length);
        for (int i : ArrayIndexUpstreamReach)
        {
            listIndexUpstreamReach.add(i);
        }


        // Si il s'agit d'un reach amont, sans contribution en amont
        if(listIndex.size() == 0){
                   
            //      Lecture des données du reach de destination
            //      Volume tracé pour le reach de destination
            //      Importation des DoubleArray du brin de destination        
            Attribute.DoubleArray destReachDoubleArrayTrackedVolumeRD1 = (Attribute.DoubleArray) DestReach.getObject("trackedVolumeRD1Array");
            Attribute.DoubleArray destReachDoubleArrayTrackedVolumeRD2 = (Attribute.DoubleArray) DestReach.getObject("trackedVolumeRD2Array");
            Attribute.DoubleArray destReachDoubleArrayTrackedVolumeRG1 = (Attribute.DoubleArray) DestReach.getObject("trackedVolumeRG1Array");
            Attribute.DoubleArray destReachDoubleArrayTrackedVolumeRG2 = (Attribute.DoubleArray) DestReach.getObject("trackedVolumeRG2Array");
            Attribute.DoubleArray destReachDoubleArrayTrackedVolumeTotal = (Attribute.DoubleArray) DestReach.getObject("trackedVolumeTotalArray");
            //      Conversion de DoubleArray à double[]
            double[] destReachArrayTrackedVolumeRD1 = destReachDoubleArrayTrackedVolumeRD1.getValue();
            double[] destReachArrayTrackedVolumeRD2 = destReachDoubleArrayTrackedVolumeRD2.getValue();
            double[] destReachArrayTrackedVolumeRG1 = destReachDoubleArrayTrackedVolumeRG1.getValue();
            double[] destReachArrayTrackedVolumeRG2 = destReachDoubleArrayTrackedVolumeRG2.getValue();
            double[] destReachArrayTrackedVolumeTotal = destReachDoubleArrayTrackedVolumeTotal.getValue();  

    //      Enregistrement du volume tracé dans le array.
            ArrayTrackedVolumeRD1[t] = RD1out;
            ArrayTrackedVolumeRD2[t] = RD2out;
            ArrayTrackedVolumeRG1[t] = RG1out;
            ArrayTrackedVolumeRG2[t] = RG2out;
            ArrayTrackedVolumeTotal[t] = cumOutflow;  
            
            trackedVolumeRD1Array.setValue(ArrayTrackedVolumeRD1);
            trackedVolumeRD2Array.setValue(ArrayTrackedVolumeRD2);
            trackedVolumeRG1Array.setValue(ArrayTrackedVolumeRG1);
            trackedVolumeRG2Array.setValue(ArrayTrackedVolumeRG2);
            trackedVolumeTotalArray.setValue(ArrayTrackedVolumeTotal);

    //      Transfert du volume tracé dans l'array du reach suivant       
            destReachArrayTrackedVolumeRD1[t] = ArrayTrackedVolumeRD1[t];
            destReachArrayTrackedVolumeRD2[t] = ArrayTrackedVolumeRD2[t];
            destReachArrayTrackedVolumeRG1[t] = ArrayTrackedVolumeRG1[t];
            destReachArrayTrackedVolumeRG2[t] = ArrayTrackedVolumeRG2[t];
            destReachArrayTrackedVolumeTotal[t] = ArrayTrackedVolumeTotal[t];

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
            
            getModel().getRuntime().println("Sortie ArrayTrackedVolumeRD1 = " + Arrays.toString(ArrayTrackedVolumeRD1));           
            getModel().getRuntime().println("Sortie ArrayTrackedVolumeTotal = " + Arrays.toString(ArrayTrackedVolumeTotal));
            
            listIndexUpstreamReach.add(t);
            ArrayIndexUpstreamReach = new int[listIndexUpstreamReach.size()];    
            for(int i = 0; i < listIndexUpstreamReach.size(); i++) ArrayIndexUpstreamReach[i] = listIndexUpstreamReach.get(i);
            DestReach.setObject("IndexListUpstreamReach", ArrayIndexUpstreamReach);



            getModel().getRuntime().println("Reach amonts = " + listIndexUpstreamReach);


        } else if (!listIndexUpstreamReach.contains(t) & DestReach != null){ 
            
        //      Lecture des données du reach de destination
        //          Volume tracé pour le reach de destination
        //              Importation des DoubleArray du brin de destination        
        Attribute.DoubleArray destReachDoubleArrayTrackedVolumeRD1 = (Attribute.DoubleArray) DestReach.getObject("trackedVolumeRD1Array");
        Attribute.DoubleArray destReachDoubleArrayTrackedVolumeRD2 = (Attribute.DoubleArray) DestReach.getObject("trackedVolumeRD2Array");
        Attribute.DoubleArray destReachDoubleArrayTrackedVolumeRG1 = (Attribute.DoubleArray) DestReach.getObject("trackedVolumeRG1Array");
        Attribute.DoubleArray destReachDoubleArrayTrackedVolumeRG2 = (Attribute.DoubleArray) DestReach.getObject("trackedVolumeRG2Array");
        Attribute.DoubleArray destReachDoubleArrayTrackedVolumeTotal = (Attribute.DoubleArray) DestReach.getObject("trackedVolumeTotalArray");
        //              Conversion de DoubleArray à double[]
        double[] destReachArrayTrackedVolumeRD1 = destReachDoubleArrayTrackedVolumeRD1.getValue();
        double[] destReachArrayTrackedVolumeRD2 = destReachDoubleArrayTrackedVolumeRD2.getValue();
        double[] destReachArrayTrackedVolumeRG1 = destReachDoubleArrayTrackedVolumeRG1.getValue();
        double[] destReachArrayTrackedVolumeRG2 = destReachDoubleArrayTrackedVolumeRG2.getValue();
        double[] destReachArrayTrackedVolumeTotal = destReachDoubleArrayTrackedVolumeTotal.getValue();  
        
        /* Calculation and transfer of tracked volume*/
        double[] ArrayTrackedVolume_actRD1_new = new double[Nom.length];
        double[] ArrayTrackedVolume_actRD2_new = new double[Nom.length];
        double[] ArrayTrackedVolume_actRG1_new = new double[Nom.length];
        double[] ArrayTrackedVolume_actRG2_new = new double[Nom.length];
        double[] ArrayTrackedVolume_actTotal_new = new double[Nom.length];
        
        double[] ArrayTrackedVolumeRD1_new = new double[Nom.length];
        double[] ArrayTrackedVolumeRD2_new = new double[Nom.length];
        double[] ArrayTrackedVolumeRG1_new = new double[Nom.length];
        double[] ArrayTrackedVolumeRG2_new = new double[Nom.length];
        double[] ArrayTrackedVolumeTotal_new = new double[Nom.length];
        
        // La il faut trouver les indices pour lesquels ArrayTrackedVolumeTotal != -999
        // Si il y a une valeur != -999 dans ArrayTrackedVolumeTotal c'est qu'il y 
        // y a une contribution de l'amont. --> on garde les indices 
        // Sinon c'est qu'il n'y en a pas

        for(int i : listIndex){
            
            //   Ajout du nouveau volume tracé au volume tracé déjà présent           
            ArrayTrackedVolume_actRD1_new[i] = ArrayTrackedVolumeRD1[i] + ArrayTrackedVolume_actRD1[i];
            ArrayTrackedVolume_actRD2_new[i] = ArrayTrackedVolumeRD2[i] + ArrayTrackedVolume_actRD2[i];
            ArrayTrackedVolume_actRG1_new[i] = ArrayTrackedVolumeRG1[i] + ArrayTrackedVolume_actRG1[i];
            ArrayTrackedVolume_actRG2_new[i] = ArrayTrackedVolumeRG2[i] + ArrayTrackedVolume_actRG2[i];
            ArrayTrackedVolume_actTotal_new[i] = ArrayTrackedVolumeTotal[i] + ArrayTrackedVolume_actTotal[i];
            

            
            //   Règle de trois pour calculer le volume tracé sortant du brin
            ArrayTrackedVolumeRD1_new[i] = (ArrayTrackedVolume_actRD1_new[i]*RD1out)/actRD1Temp.getValue();
            ArrayTrackedVolumeRD2_new[i] = (ArrayTrackedVolume_actRD2_new[i]*RD2out)/actRD2Temp.getValue();
            ArrayTrackedVolumeRG1_new[i] = (ArrayTrackedVolume_actRG1_new[i]*RG1out)/actRG1Temp.getValue();
            ArrayTrackedVolumeRG2_new[i] = (ArrayTrackedVolume_actRG2_new[i]*RG2out)/actRG2Temp.getValue();
            ArrayTrackedVolumeTotal_new[i] = (ArrayTrackedVolume_actTotal_new[i]*cumOutflow)/actTotTemp;
            
            
            if(actRD1Temp.getValue() == 0){
                ArrayTrackedVolumeRD1_new[i] = 0;
            }
            if(actRD2Temp.getValue() == 0){
                ArrayTrackedVolumeRD2_new[i] = 0;
            }
            if(actRG1Temp.getValue() == 0){
                ArrayTrackedVolumeRG1_new[i] = 0;
            }
            if(actRG2Temp.getValue() == 0){
                ArrayTrackedVolumeRG2_new[i] = 0;
            }
            if(actTotTemp == 0){
                ArrayTrackedVolumeTotal_new[i] = 0;
            }  
            // Enregistrement dans le tableau originel
            ArrayTrackedVolumeRD1[i] = ArrayTrackedVolumeRD1_new[i];
            ArrayTrackedVolumeRD2[i] = ArrayTrackedVolumeRD2_new[i];
            ArrayTrackedVolumeRG1[i] = ArrayTrackedVolumeRG1_new[i];
            ArrayTrackedVolumeRG2[i] = ArrayTrackedVolumeRG2_new[i];
            ArrayTrackedVolumeTotal[i] = ArrayTrackedVolumeTotal_new[i];
            
            //    Calcul du volume tracé restant dans le brin
            ArrayTrackedVolume_actRD1[i] = ArrayTrackedVolume_actRD1_new[i] - ArrayTrackedVolumeRD1_new[i];
            ArrayTrackedVolume_actRD2[i] = ArrayTrackedVolume_actRD2_new[i] - ArrayTrackedVolumeRD2_new[i];
            ArrayTrackedVolume_actRG1[i] = ArrayTrackedVolume_actRG1_new[i] - ArrayTrackedVolumeRG1_new[i];
            ArrayTrackedVolume_actRG2[i] = ArrayTrackedVolume_actRG2_new[i] - ArrayTrackedVolumeRG2_new[i];
            ArrayTrackedVolume_actTotal[i] = ArrayTrackedVolume_actTotal_new[i] - ArrayTrackedVolumeTotal_new[i];
            
            // Transfert des volumes tracés au brin suivant - 1ere partie
            destReachArrayTrackedVolumeRD1[i] = ArrayTrackedVolumeRD1[i];
            destReachArrayTrackedVolumeRD2[i] = ArrayTrackedVolumeRD2[i];
            destReachArrayTrackedVolumeRG1[i] = ArrayTrackedVolumeRG1[i];
            destReachArrayTrackedVolumeRG2[i] = ArrayTrackedVolumeRG2[i];
            destReachArrayTrackedVolumeTotal[i] = ArrayTrackedVolumeTotal[i];
  
        }   
        getModel().getRuntime().println("Sortie ArrayTrackedVolumeTotal = " + Arrays.toString(ArrayTrackedVolumeTotal));

        
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
  
    //      Transfert du volume tracé dans l'array du reach suivant - 2e partie       
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
        
        // Extraction du reach tracé           
        trackedVolumeRD1.setValue(ArrayTrackedVolumeRD1[r]);
        trackedVolumeRD2.setValue(ArrayTrackedVolumeRD2[r]);
        trackedVolumeRG1.setValue(ArrayTrackedVolumeRG1[r]);
        trackedVolumeRG2.setValue(ArrayTrackedVolumeRG2[r]);
        trackedVolumeTotal.setValue(ArrayTrackedVolumeTotal[r]);
        
    } else if(DestReach == null){
        
            /* Calculation and transfer of tracked volume*/
        double[] ArrayTrackedVolume_actRD1_new = new double[Nom.length];
        double[] ArrayTrackedVolume_actRD2_new = new double[Nom.length];
        double[] ArrayTrackedVolume_actRG1_new = new double[Nom.length];
        double[] ArrayTrackedVolume_actRG2_new = new double[Nom.length];
        double[] ArrayTrackedVolume_actTotal_new = new double[Nom.length];
        
        double[] ArrayTrackedVolumeRD1_new = new double[Nom.length];
        double[] ArrayTrackedVolumeRD2_new = new double[Nom.length];
        double[] ArrayTrackedVolumeRG1_new = new double[Nom.length];
        double[] ArrayTrackedVolumeRG2_new = new double[Nom.length];
        double[] ArrayTrackedVolumeTotal_new = new double[Nom.length];  
        
 
        //        getModel().getRuntime().println("Elements  contribuant à ce reach = " + listIndex);

        for(int i : listIndex){
            
            //   Ajout du nouveau volume tracé au volume tracé déjà présent           
            ArrayTrackedVolume_actRD1_new[i] = ArrayTrackedVolumeRD1[i] + ArrayTrackedVolume_actRD1[i];
            ArrayTrackedVolume_actRD2_new[i] = ArrayTrackedVolumeRD2[i] + ArrayTrackedVolume_actRD2[i];
            ArrayTrackedVolume_actRG1_new[i] = ArrayTrackedVolumeRG1[i] + ArrayTrackedVolume_actRG1[i];
            ArrayTrackedVolume_actRG2_new[i] = ArrayTrackedVolumeRG2[i] + ArrayTrackedVolume_actRG2[i];
            ArrayTrackedVolume_actTotal_new[i] = ArrayTrackedVolumeTotal[i] + ArrayTrackedVolume_actTotal[i];
            
            //   Règle de trois pour calculer le volume tracé sortant du brin
            ArrayTrackedVolumeRD1_new[i] = (ArrayTrackedVolume_actRD1_new[i]*RD1out)/actRD1Temp.getValue();
            ArrayTrackedVolumeRD2_new[i] = (ArrayTrackedVolume_actRD2_new[i]*RD2out)/actRD2Temp.getValue();
            ArrayTrackedVolumeRG1_new[i] = (ArrayTrackedVolume_actRG1_new[i]*RG1out)/actRG1Temp.getValue();
            ArrayTrackedVolumeRG2_new[i] = (ArrayTrackedVolume_actRG2_new[i]*RG2out)/actRG2Temp.getValue();
            ArrayTrackedVolumeTotal_new[i] = (ArrayTrackedVolume_actTotal_new[i]*cumOutflow)/actTotTemp;
            
            if(actRD1Temp.getValue() == 0){
                ArrayTrackedVolumeRD1_new[i] = 0;
            }
            if(actRD2Temp.getValue() == 0){
                ArrayTrackedVolumeRD2_new[i] = 0;
            }
            if(actRG1Temp.getValue() == 0){
                ArrayTrackedVolumeRG1_new[i] = 0;
            }
            if(actRG2Temp.getValue() == 0){
                ArrayTrackedVolumeRG2_new[i] = 0;
            }
            if(actTotTemp == 0){
                ArrayTrackedVolumeTotal_new[i] = 0;
            }  
            // Enregistrement dans le tableau originel
            ArrayTrackedVolumeRD1[i] = ArrayTrackedVolumeRD1_new[i];
            ArrayTrackedVolumeRD2[i] = ArrayTrackedVolumeRD2_new[i];
            ArrayTrackedVolumeRG1[i] = ArrayTrackedVolumeRG1_new[i];
            ArrayTrackedVolumeRG2[i] = ArrayTrackedVolumeRG2_new[i];
            ArrayTrackedVolumeTotal[i] = ArrayTrackedVolumeTotal_new[i];
            
            //    Calcul du volume tracé restant dans le brin
            ArrayTrackedVolume_actRD1[i] = ArrayTrackedVolume_actRD1_new[i] - ArrayTrackedVolumeRD1_new[i];
            ArrayTrackedVolume_actRD2[i] = ArrayTrackedVolume_actRD2_new[i] - ArrayTrackedVolumeRD2_new[i];
            ArrayTrackedVolume_actRG1[i] = ArrayTrackedVolume_actRG1_new[i] - ArrayTrackedVolumeRG1_new[i];
            ArrayTrackedVolume_actRG2[i] = ArrayTrackedVolume_actRG2_new[i] - ArrayTrackedVolumeRG2_new[i];
            ArrayTrackedVolume_actTotal[i] = ArrayTrackedVolume_actTotal_new[i] - ArrayTrackedVolumeTotal_new[i];
  
        }   
        
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
        
        // Extraction du reach tracé           
        trackedVolumeRD1.setValue(ArrayTrackedVolumeRD1[r]);
        trackedVolumeRD2.setValue(ArrayTrackedVolumeRD2[r]);
        trackedVolumeRG1.setValue(ArrayTrackedVolumeRG1[r]);
        trackedVolumeRG2.setValue(ArrayTrackedVolumeRG2[r]);
        trackedVolumeTotal.setValue(ArrayTrackedVolumeTotal[r]);
                
    } else {
//        getModel().getRuntime().println("Ce n'est pas le Reach tracé");
    }
    }   
    
    public static boolean AreAllSame(double[] array)
{
    for(int i = 1; i < array.length; i++)
    {
        if(array[i]!= -999) return false;
    }
    return true;
    }
}

//        
//    
//    
//    