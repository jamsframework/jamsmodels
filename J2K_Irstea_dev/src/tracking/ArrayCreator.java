/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tracking;

import jams.JAMS;
import jams.data.*;
import jams.model.*;
import jams.workspace.DataSetDefinition;
import jams.workspace.stores.InputDataStore;
import jams.workspace.stores.TSDataStore;
import java.util.ArrayList;
import java.util.Arrays;

/**
 *
 * @author Olivier Grandjouan
 */
@JAMSComponentDescription(
        title = "ArrayCreator",
        author = "Olivier Grandjouan",
        description = "Create arrays full of -999 values to write the spatial decomposition results",
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
            description = "Datastore ID")
    public Attribute.String dataStoreID;  
            
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.WRITE,
        description = "Array of reach IDs")
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
            
    private TSDataStore store;
    boolean shifted = false;

            
    public void init() {

        shifted = false;
        InputDataStore is = null;

        if (dataStoreID != null) {
            is = getModel().getWorkspace().getInputDataStore(dataStoreID.getValue());
        }

        // check if store exists
            if (is == null) {
                getModel().getRuntime().sendHalt("Error accessing datastore \""
                        + dataStoreID + "\" from " + getInstanceName() + ": Datastore could not be found!");
                return;
            }
        store = (TSDataStore) is;

        // extract some meta information

        DataSetDefinition dsDef = store.getDataSetDefinition();

        names.setValue(listToDoubleArray(dsDef.getAttributeValues("X")));

        double[] Nom = this.names.getValue();

        int t = Nom.length;
//        getModel().getRuntime().println("+++ ArrayCreator - length: "+t+" tracked reaches, names: "+Arrays.toString(Nom));

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
    }

    
    private double[] listToDoubleArray(ArrayList<Object> list) {
    double[] result = new double[list.size()];
    int i = 0;
    for (Object o : list) {
        result[i] = ((Double) o).doubleValue();
        i++;
    }
    return result;
    }

    public void run(){ 


    }
}



