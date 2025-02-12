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
            description = "Name of datastore listing reaches to track. Should contain 'X' field,"+
                    "storing ID of the reaches.")
    public Attribute.String dataStoreID;  
            
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.WRITE,
        description = "Array of reach IDs to generate by this component. (Taken from datastore) - output")
    public Attribute.DoubleArray names;  

        @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "Array of current time step RD1 inflow into reach per source reach. Created by this component"+
                    "and filled with NANs (-999) - output"
            )
    public Attribute.DoubleArray trackedVolumeRD1Array;
        
        @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "Array of current time step RD2 inflow into reach per source reach. Created by this component"+
                    "and filled with NANs (-999) - output"
            )
    public Attribute.DoubleArray trackedVolumeRD2Array;  
        
        @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "Array of current time step RG1 inflow into reach per source reach. Created by this component"+
                    "and filled with NANs (-999) - output"
            )
    public Attribute.DoubleArray trackedVolumeRG1Array;  
        
        @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "Array of current time step RG2 inflow into reach per source reach. Created by this component"+
                    "and filled with NANs (-999) - output"
            )
    public Attribute.DoubleArray trackedVolumeRG2Array;  
        
        @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "Array of current time step total inflow into reach per source reach. Created by this component"+
                    "and filled with NANs (-999) - output"
            )
    public Attribute.DoubleArray trackedVolumeTotalArray;   
             
            @JAMSVarDescription(
        access = JAMSVarDescription.AccessType.WRITE,
        description = "Array of RD1 remaining volume from each tracked reach in actual reach after routing. Created by this component"+
                    "and filled with NANs (-999) - output"
        )
    public Attribute.DoubleArray trackedVolumeRD1_actArray; 
             
            @JAMSVarDescription(
        access = JAMSVarDescription.AccessType.WRITE,
        description = "Array of RD2 remaining volume from each tracked reach in actual reach after routing. Created by this component"+
                    "and filled with NANs (-999) - output"
        )
    public Attribute.DoubleArray trackedVolumeRD2_actArray; 
             
            @JAMSVarDescription(
        access = JAMSVarDescription.AccessType.WRITE,
        description = "Array of RG1 remaining volume from each tracked reach in actual reach after routing. Created by this component"+
                    "and filled with NANs (-999) - output"
        )
    public Attribute.DoubleArray trackedVolumeRG1_actArray; 
             
            @JAMSVarDescription(
        access = JAMSVarDescription.AccessType.WRITE,
        description = "Array of RG2 remaining volume from each tracked reach in actual reach after routing. Created by this component"+
                    "and filled with NANs (-999) - output"
        )
    public Attribute.DoubleArray trackedVolumeRG2_actArray; 
            
            @JAMSVarDescription(
        access = JAMSVarDescription.AccessType.WRITE,
        description = "Array of total remaining volume from each tracked reach in actual reach after routing. Created by this component"+
                    "and filled with NANs (-999) - output"
        )
    public Attribute.DoubleArray trackedVolumeTotal_actArray;  
            
    private TSDataStore run_store;
    //boolean run_shifted = false;

            
    @Override
    public void init() {

        //run_shifted = false;
        InputDataStore run_is = null;

        if (dataStoreID != null) {
            run_is = getModel().getWorkspace().getInputDataStore(dataStoreID.getValue());
        }

        // check if store exists
            if (run_is == null) {
                getModel().getRuntime().sendHalt("Error accessing datastore \""
                        + dataStoreID + "\" from " + getInstanceName() + ": Datastore could not be found!");
                return;
            }
        run_store = (TSDataStore) run_is;

        // extract some meta information

        DataSetDefinition run_dsDef = run_store.getDataSetDefinition();

        names.setValue(listToDoubleArray(run_dsDef.getAttributeValues("X")));

        double[] run_names = this.names.getValue();

        int run_t = run_names.length;
//        getModel().getRuntime().println("+++ ArrayCreator - length: "+t+" tracked reaches, names: "+Arrays.toString(Nom));

    //  Création et sauvegarde des tableaux pour les volumes tracés et restants au début du modèle
        double[] run_ArrayTrackedVolumeRD1 = new double[run_t];
        double[] run_ArrayTrackedVolumeRD2 = new double[run_t];
        double[] run_ArrayTrackedVolumeRG1 = new double[run_t];
        double[] run_ArrayTrackedVolumeRG2 = new double[run_t];
        double[] run_ArrayTrackedVolumeTotal = new double[run_t];

        double[] run_ArrayTrackedVolume_actRD1 = new double[run_t];
        double[] run_ArrayTrackedVolume_actRD2 = new double[run_t];
        double[] run_ArrayTrackedVolume_actRG1 = new double[run_t];
        double[] run_ArrayTrackedVolume_actRG2 = new double[run_t];    
        double[] run_ArrayTrackedVolume_actTotal = new double[run_t]; 

        for(int i=0;i<run_t;i++)
        {
            run_ArrayTrackedVolumeRD1[i] = -999.;
            run_ArrayTrackedVolumeRD2[i] = -999;
            run_ArrayTrackedVolumeRG1[i] = -999;
            run_ArrayTrackedVolumeRG2[i] = -999;
            run_ArrayTrackedVolumeTotal[i] = -999;   
        }

        trackedVolumeRD1Array.setValue(run_ArrayTrackedVolumeRD1);
        trackedVolumeRD2Array.setValue(run_ArrayTrackedVolumeRD2);
        trackedVolumeRG1Array.setValue(run_ArrayTrackedVolumeRG1);
        trackedVolumeRG2Array.setValue(run_ArrayTrackedVolumeRG2);
        trackedVolumeTotalArray.setValue(run_ArrayTrackedVolumeTotal);

        trackedVolumeRD1_actArray.setValue(run_ArrayTrackedVolume_actRD1);
        trackedVolumeRD2_actArray.setValue(run_ArrayTrackedVolume_actRD2);
        trackedVolumeRG1_actArray.setValue(run_ArrayTrackedVolume_actRG1);
        trackedVolumeRG2_actArray.setValue(run_ArrayTrackedVolume_actRG2);
        trackedVolumeTotal_actArray.setValue(run_ArrayTrackedVolume_actTotal);
    }

    
    private double[] listToDoubleArray(ArrayList<Object> run_list) {
    double[] run_result = new double[run_list.size()];
    int run_i = 0;
    for (Object o : run_list) {
        run_result[run_i] = ((Double) o).doubleValue();
        run_i++;
    }
    return run_result;
    }

    @Override
    public void run(){ 


    }
}



