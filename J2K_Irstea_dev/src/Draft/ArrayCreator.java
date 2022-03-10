/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Draft;

import jams.JAMS;
import jams.data.*;
import jams.model.*;
import java.util.Arrays;

/**
 *
 * @author Olivier Grandjouan
 */
@JAMSComponentDescription(
        title = "ArrayCreator",
        author = "Olivier Grandjouan",
        description = "Create arrays full of 0 values",
        version = "1.0",
        date = "2022-03-08"
)
public class ArrayCreator extends JAMSComponent {
    
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
            access = JAMSVarDescription.AccessType.WRITE,
            description = "Tracked volume RD1 array"
            )
    public Attribute.DoubleArray trackedVolumeRD1Array;
        
        @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "Tracked volume RD2 array"
            )
    public Attribute.DoubleArray trackedVolumeRD2Array;  
        
        @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "Tracked volume RG1 array"
            )
    public Attribute.DoubleArray trackedVolumeRG1Array;  
        
        @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "Tracked volume RG2 array"
            )
    public Attribute.DoubleArray trackedVolumeRG2Array;  
        
        @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "Tracked volume Total array"
            )
    public Attribute.DoubleArray trackedVolumeTotalArray;   
             
            @JAMSVarDescription(
        access = JAMSVarDescription.AccessType.WRITE,
        description = "Array of RD1 remaining volume from tracked reach in actual reach after routing"
        )
    public Attribute.DoubleArray trackedVolumeRD1_actArray; 
             
            @JAMSVarDescription(
        access = JAMSVarDescription.AccessType.WRITE,
        description = "Array of RD2 remaining volume from tracked reach in actual reach after routing"
        )
    public Attribute.DoubleArray trackedVolumeRD2_actArray; 
             
            @JAMSVarDescription(
        access = JAMSVarDescription.AccessType.WRITE,
        description = "Array of RG1 remaining volume from tracked reach in actual reach after routing"
        )
    public Attribute.DoubleArray trackedVolumeRG1_actArray; 
             
            @JAMSVarDescription(
        access = JAMSVarDescription.AccessType.WRITE,
        description = "Array of RG2 remaining volume from tracked reach in actual reach after routing"
        )
    public Attribute.DoubleArray trackedVolumeRG2_actArray; 
            
            @JAMSVarDescription(
        access = JAMSVarDescription.AccessType.WRITE,
        description = "Array of total remaining volume from tracked reach in actual reach after routing"
        )
    public Attribute.DoubleArray trackedVolumeTotal_actArray;  
            
        @JAMSVarDescription(access = JAMSVarDescription.AccessType.WRITE,
        description = "List of upstream reaches")
    public Attribute.IntegerArray IndexListUpstreamReach;
            
public void init() {
    
    double[] Nom = this.names.getValue();
    int t = Nom.length;

//  Création et sauvegarde des tableaux pour les volumes tracés et restants au début du modèle
    double[] ArrayTrackedVolumeRD1 = new double[t];
    double[] ArrayTrackedVolumeRD2 = new double[t];
    double[] ArrayTrackedVolumeRG1 = new double[t];
    double[] ArrayTrackedVolumeRG2 = new double[t];
    double[] ArrayTrackedVolumeTotal = new double[t];

    double[] ArrayTrackedVolume_actRD1 = new double[t];
    double[] ArrayTrackedVolume_actRD2 = new double[t];
    double[] ArrayTrackedVolume_actRG1 = new double[t];
    double[] ArrayTrackedVolume_actRG2 = new double[t];    
    double[] ArrayTrackedVolume_actTotal = new double[t]; 
    
    int[] ArrayIndexUpstreamReach = new int[0] ;
    
    for(int i=0;i<t;i++)
    {
        ArrayTrackedVolumeRD1[i] = -999.;
        ArrayTrackedVolumeRD2[i] = -999;
        ArrayTrackedVolumeRG1[i] = -999;
        ArrayTrackedVolumeRG2[i] = -999;
        ArrayTrackedVolumeTotal[i] = -999;   
    }

    trackedVolumeRD1Array.setValue(ArrayTrackedVolumeRD1);
    trackedVolumeRD2Array.setValue(ArrayTrackedVolumeRD2);
    trackedVolumeRG1Array.setValue(ArrayTrackedVolumeRG1);
    trackedVolumeRG2Array.setValue(ArrayTrackedVolumeRG2);
    trackedVolumeTotalArray.setValue(ArrayTrackedVolumeTotal);

    trackedVolumeRD1_actArray.setValue(ArrayTrackedVolume_actRD1);
    trackedVolumeRD2_actArray.setValue(ArrayTrackedVolume_actRD2);
    trackedVolumeRG1_actArray.setValue(ArrayTrackedVolume_actRG1);
    trackedVolumeRG2_actArray.setValue(ArrayTrackedVolume_actRG2);
    trackedVolumeTotal_actArray.setValue(ArrayTrackedVolume_actTotal);

    IndexListUpstreamReach.setValue(ArrayIndexUpstreamReach);
    
    Attribute.Entity entity = entities.getCurrent();
}
public void run(){ 
    

}
}



