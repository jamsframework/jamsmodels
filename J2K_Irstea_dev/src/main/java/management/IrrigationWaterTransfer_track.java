/*
 * IrrigationWaterTransferr.java
 * Created on 13.08.2015, 16:17:09
 *
 * This file is part of JAMS
 * Copyright (C) FSU Jena
 *
 * JAMS is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * JAMS is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with JAMS. If not, see <http://www.gnu.org/licenses/>.
 *
 */
package management;

import jams.data.*;
import jams.model.*;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Sven Kralisch <sven.kralisch at uni-jena.de>
 */
@JAMSComponentDescription(
        title = "IrrigationWaterTransfer_track",
        author = "François Tilmant + Sven Kralisch, Nico Hachgenei",
        description = "Transfer water from reaches to HRUs depending on water"
        + " availability and irrigation demand"
	+ "irrigation water comes from incoming water to the reach and water inside the reach (actRG1, etc..)."
        + " Modified for tracking: removed extracted water from tracked volumes as well."
        + " Modified after IrrigationWaterTransfer_act version 2",
        date = "2024-05-31",
        version = "1.0_0"
)
@VersionComments(entries = {
    @VersionComments.Entry(version = "1.0_0", comment = "Initial version")
})
public class IrrigationWaterTransfer_track extends JAMSComponent {

    /*
     *  Component attributes
     */
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Reaches list"
    )
    public Attribute.EntityCollection reaches;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "Current time step RD1 inflow into reach. Will be updated by this component,"+
                    "extracting water for irrigation. - input / state variable"
    )
    public Attribute.Double inRD1;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "Current time step RD2 inflow into reach. Will be updated by this component,"+
                    "extracting water for irrigation. - input / state variable"
    )
    public Attribute.Double inRD2;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "Current time step RG1 inflow into reach. Will be updated by this component,"+
                    "extracting water for irrigation. - input / state variable"
    )
    public Attribute.Double inRG1;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "Current time step RG2 inflow into reach. Will be updated by this component,"+
                    "extracting water for irrigation. - input / state variable"
    )
    public Attribute.Double inRG2;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "Current time step RD1 volume inside reach. Will be updated by this component"+
                    "(if not enough water in inflow), extracting water for irrigation."+
                    "- state variable"
    )
    public Attribute.Double actRD1;
        
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "Current time step RD2 volume inside reach. Will be updated by this component"+
                    "(if not enough water in inflow), extracting water for irrigation."+
                    "- state variable"
    )
    public Attribute.Double actRD2;
        
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "Current time step RG1 volume inside reach. Will be updated by this component"+
                    "(if not enough water in inflow), extracting water for irrigation."+
                    "- state variable"
    )
    public Attribute.Double actRG1;
            
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "Current time step RG2 volume inside reach. Will be updated by this component"+
                    "(if not enough water in inflow), extracting water for irrigation."+
                    "- state variable"
    )
    public Attribute.Double actRG2;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Name of list of irrigated HRUs in reach entities. List will be read by this"+
                    "component. - parameter / pointer",
            defaultValue = "irrigationEntities"
    )
    public Attribute.String irrigationEntitiesListName;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Name of attribute that stores irrigation demand of an HRU - (plant water"+
                    "requirement / efficiency). Irrigation demand will be read by this component."+
                    "- parameter / pointer",
            defaultValue = "irrigationDemand"
    )
    public Attribute.String irrigationDemandName;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Name of attribute that stores water requirements of an HRU - the real plant"+
                    "requirements. Water requirements will be read by this component. - parameter / pointer",
            defaultValue = "waterRequirements"
    )
    public Attribute.String waterRequirementsName;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Name of attribute that stores the irrigation water delivered to HRU (totalTransfer"+
                    "minus losses due to efficiency). This attribute will be written to by this component"+
                    "- parameter / pointer",
            defaultValue = "irrigationWater"
    )
    public Attribute.String irrigationWaterName;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Ratio of water available (allowed to be taken) for irrigation over water present"+
                    "in the reach (actR..). - parameter"
    )
    public Attribute.Double actPrel;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "Total demand of water for irrigation, including the enhancement by poor efficiency."+
                    "Should be attribute of the ReachLoop to write. This component cumulates the irrigation"+ 
                    "demands of irrigated HRUs and writes this attribute. - output"
    )
    public Attribute.Double totalDemand;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "Total irrigation transfer (= totalDemand, but limited to available water). Calculated in this component. - output"
    )
    public Attribute.Double totalTransfer;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "Total input in the reach as sum of the four components, after extraction for irrigation. - output"
    )
    public Attribute.Double totalInput;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
        description = "Array of reach names (IDs). Used for tracked volumes from each reach.")
    public Attribute.DoubleArray names;   

    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "Array of current time step RD1 inflow into reach per source reach. Will be updated by this component,"+
                    "extracting water for irrigation. - input / state variable"
            )
    public Attribute.DoubleArray trackedVolumeRD1Array;
        
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "Array of current time step RD2 inflow into reach per source reach. Will be updated by this component,"+
                    "extracting water for irrigation. - input / state variable"
            )
    public Attribute.DoubleArray trackedVolumeRD2Array;  
        
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "Array of current time step RG1 inflow into reach per source reach. Will be updated by this component,"+
                    "extracting water for irrigation. - input / state variable"
            )
    public Attribute.DoubleArray trackedVolumeRG1Array;  
        
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "Array of current time step RG2 inflow into reach per source reach. Will be updated by this component,"+
                    "extracting water for irrigation. - input / state variable"
            )
    public Attribute.DoubleArray trackedVolumeRG2Array;  
        
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "Total array of input into the reach as sum of the four components per source reach. - output"
            )
    public Attribute.DoubleArray trackedVolumeTotalArray;   
             
    @JAMSVarDescription(
        access = JAMSVarDescription.AccessType.READWRITE,
        description = "Array of RD1 volume inside reach per source reach. Will be updated by this component"+
                    "(if not enough water in inflow), extracting water for irrigation."+
                    "- state variable"
        )
    public Attribute.DoubleArray trackedVolumeRD1_actArray; 
             
    @JAMSVarDescription(
        access = JAMSVarDescription.AccessType.READWRITE,
        description = "Array of RD2 volume inside reach per source reach. Will be updated by this component"+
                    "(if not enough water in inflow), extracting water for irrigation."+
                    "- state variable"
        )
    public Attribute.DoubleArray trackedVolumeRD2_actArray; 
             
    @JAMSVarDescription(
        access = JAMSVarDescription.AccessType.READWRITE,
        description = "Array of RG1 volume inside reach per source reach. Will be updated by this component"+
                    "(if not enough water in inflow), extracting water for irrigation."+
                    "- state variable"
        )
    public Attribute.DoubleArray trackedVolumeRG1_actArray; 
             
    @JAMSVarDescription(
        access = JAMSVarDescription.AccessType.READWRITE,
        description = "Array of RG2 volume inside reach per source reach. Will be updated by this component"+
                    "(if not enough water in inflow), extracting water for irrigation."+
                    "- state variable"
        )
    public Attribute.DoubleArray trackedVolumeRG2_actArray;
            
    @JAMSVarDescription(
        access = JAMSVarDescription.AccessType.READWRITE,
        description = "Array of total volume inside reach per source reach (sum of the four flow components)."+
                "Will be updated by this component (if not enough water in inflow), extracting water for irrigation."+
                "- state variable"
        )
    public Attribute.DoubleArray trackedVolumeTotal_actArray;
            
    @JAMSVarDescription(
        access = JAMSVarDescription.AccessType.READ,
        description = "Are there tracked sewers in the model? (1/0) - parameter"
        )
    public Attribute.Double trackSewers;
            
    @JAMSVarDescription(
        access = JAMSVarDescription.AccessType.READ,
        description = "Is there a tracked waste water treatment plant in the model? (1/0) - parameter"
        )
    public Attribute.Double trackWW;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "Volume in RD1 inflow into reach coming from Sewer. Will be updated by this component,"+
                    "extracting water for irrigation. - input / state variable",
            unit = "L"
    )
    public Attribute.Double trackedVolumeSewRD1;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "Volume in RD2 inflow into reach coming from Sewer. Will be updated by this component,"+
                    "extracting water for irrigation. - input / state variable",
            unit = "L"
    )
    public Attribute.Double trackedVolumeSewRD2;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "Volume in RG1 inflow into reach coming from Sewer. Will be updated by this component,"+
                    "extracting water for irrigation. - input / state variable",
            unit = "L"
    )
    public Attribute.Double trackedVolumeSewRG1;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "Volume in RG2 inflow into reach coming from Sewer. Will be updated by this component,"+
                    "extracting water for irrigation. - input / state variable",
            unit = "L"
    )
    public Attribute.Double trackedVolumeSewRG2;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "Total volume of inflow into reach coming from Sewer. Will be updated by this component,"+
                    "extracting water for irrigation. - input / state variable",
            unit = "L"
    )
    public Attribute.Double trackedVolumeSewTotal; 
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "Volume of RD1 inside reach coming from Sewer. Will be updated by this component"+
                    "(if not enough water in inflow), extracting water for irrigation. - state variable",
            unit = "L"
    )
    public Attribute.Double trackedVolumeSewRD1_act;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "Volume of RD2 inside reach coming from Sewer. Will be updated by this component"+
                    "(if not enough water in inflow), extracting water for irrigation. - state variable",
            unit = "L"
    )
    public Attribute.Double trackedVolumeSewRD2_act;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "Volume of RG1 inside reach coming from Sewer. Will be updated by this component"+
                    "(if not enough water in inflow), extracting water for irrigation. - state variable",
            unit = "L"
    )
    public Attribute.Double trackedVolumeSewRG1_act;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "Volume of RG2 inside reach coming from Sewer. Will be updated by this component"+
                    "(if not enough water in inflow), extracting water for irrigation. - state variable",
            unit = "L"
    )
    public Attribute.Double trackedVolumeSewRG2_act;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "Total volume inside reach coming from Sewer. Will be updated by this component"+
                    "(if not enough water in inflow), extracting water for irrigation. - state variable",
            unit = "L"
    )
    public Attribute.Double trackedVolumeSewTotal_act;

//    @JAMSVarDescription(
//            access = JAMSVarDescription.AccessType.READWRITE,
//            description = "Tracked volume from waste water treatment plant in reach",
//            unit = "L"
//    )
//    public Attribute.Double trackedVolumeWW;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "Tracked volume from waste water treatment plant incoming into reach. Will be"+
                    "updated by this component, extracting water for irrigation. - input / state variable",
            unit = "L"
    )
    public Attribute.Double inWW;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "Tracked volume from waste water treatment plant inside reach. Will be"+
                    "updated by this component (if not enough water in inflow), extracting water for irrigation."+
                    "- input / state variable",
            unit = "L"
    )
    public Attribute.Double trackedVolumeWW_act;

    /*
     *  Component run stages
     */

    @Override
    public void run() {

        Attribute.Entity run_currentReach = reaches.getCurrent();
        
        boolean run_SewTrack = trackSewers.getValue() == 1;
        boolean run_WWTrack = trackWW.getValue() == 1;
        
        // read tracked volumes of the current reach --> incoming
        double[] run_ArrayTrackedVolumeRD1 = trackedVolumeRD1Array.getValue();
        double[] run_ArrayTrackedVolumeRD2 = trackedVolumeRD2Array.getValue();
        double[] run_ArrayTrackedVolumeRG1 = trackedVolumeRG1Array.getValue();
        double[] run_ArrayTrackedVolumeRG2 = trackedVolumeRG2Array.getValue();
        double[] run_ArrayTrackedVolumeTotal = trackedVolumeTotalArray.getValue();
        // available
        double[] run_ArrayTrackedVolume_actRD1 = trackedVolumeRD1_actArray.getValue();
        double[] run_ArrayTrackedVolume_actRD2 = trackedVolumeRD2_actArray.getValue();
        double[] run_ArrayTrackedVolume_actRG1 = trackedVolumeRG1_actArray.getValue();
        double[] run_ArrayTrackedVolume_actRG2 = trackedVolumeRG2_actArray.getValue();
        double[] run_ArrayTrackedVolume_actTotal = trackedVolumeTotal_actArray.getValue();
        
        double run_TrackedVolumeSewRD1, run_TrackedVolumeSewRD2, 
                run_TrackedVolumeSewRG1, run_TrackedVolumeSewRG2, 
                run_TrackedVolumeSewTotal, 
                run_TrackedVolumeSewRD1_act, run_TrackedVolumeSewRD2_act, 
                run_TrackedVolumeSewRG1_act, run_TrackedVolumeSewRG2_act, 
                run_TrackedVolumeSewTotal_act,
                run_InWW, run_TrackedVolumeWW_act;
        run_TrackedVolumeSewRD1 = run_TrackedVolumeSewRD2 = run_TrackedVolumeSewRG1 =
                run_TrackedVolumeSewRG2 = run_TrackedVolumeSewTotal =
                run_TrackedVolumeSewRD1_act = run_TrackedVolumeSewRD2_act = 
                run_TrackedVolumeSewRG1_act = run_TrackedVolumeSewRG2_act =  
                run_TrackedVolumeSewTotal_act = 
                run_InWW = run_TrackedVolumeWW_act = 0.0;
        if(run_SewTrack){
            // read tracked volumes of the current reach --> incoming
            run_TrackedVolumeSewRD1 = trackedVolumeSewRD1.getValue();
            run_TrackedVolumeSewRD2 = trackedVolumeSewRD2.getValue();
            run_TrackedVolumeSewRG1 = trackedVolumeSewRG1.getValue();
            run_TrackedVolumeSewRG2 = trackedVolumeSewRG2.getValue();
            run_TrackedVolumeSewTotal = trackedVolumeSewTotal.getValue();
            // available
            run_TrackedVolumeSewRD1_act = trackedVolumeSewRD1_act.getValue();
            run_TrackedVolumeSewRD2_act = trackedVolumeSewRD2_act.getValue();
            run_TrackedVolumeSewRG1_act = trackedVolumeSewRG1_act.getValue();
            run_TrackedVolumeSewRG2_act = trackedVolumeSewRG2_act.getValue();
            run_TrackedVolumeSewTotal_act = trackedVolumeSewTotal_act.getValue();
        }
        
        if(run_WWTrack){
            // read tracked volumes of the current reach --> incoming
            run_InWW = inWW.getValue();
            // available
            run_TrackedVolumeWW_act = trackedVolumeWW_act.getValue();
        }
        
        // Array of reach names
        double[] run_names = this.names.getValue();
        
        // Liste des indices de Reachs contribuant au brin actuel
        List<Integer> run_listIndex = new ArrayList<Integer>();
        for(int i = 0; i < run_names.length; i++){
            if(run_ArrayTrackedVolumeTotal[i] != -999){
                run_listIndex.add(i);
            }
        }

        //check if this reach even has irrigated HRUs in its catchment
        if (!run_currentReach.existsAttribute(irrigationEntitiesListName.getValue())) {
            double run_totalIn = inRD1.getValue() + inRD2.getValue() + inRG1.getValue() + inRG2.getValue();
            double run_totalAct = this.actPrel.getValue() * (actRD1.getValue() + actRD2.getValue() + actRG1.getValue() + actRG2.getValue());
            this.totalInput.setValue(run_totalIn + run_totalAct); // IG : ACHTUNG, cette variable n'est pas à jour !!
            return;
        }
        double run_totalIn = inRD1.getValue() + inRD2.getValue() + inRG1.getValue() + inRG2.getValue();
        double run_totalAct = this.actPrel.getValue() * (actRD1.getValue() + actRD2.getValue() + actRG1.getValue() + actRG2.getValue()); // eau du reach dispo pour l'irrigation.
        double run_totalAv = run_totalIn + run_totalAct; // all available water
        this.totalInput.setValue(run_totalIn + run_totalAct); // eau disponible pour l'irrigation à ce pas de temps
        //this.totalInput.setValue(totalIn);
        double run_totalDemand = 0;

        List<Attribute.Entity> run_l = (List) run_currentReach.getObject(irrigationEntitiesListName.getValue());
        for (Attribute.Entity run_hru : run_l) {
            double run_demand = run_hru.getDouble(irrigationDemandName.getValue());
            run_totalDemand += run_demand;
        }

        this.totalDemand.setValue(run_totalDemand);

        //calculate proportion of total water that is needed
        if (run_totalAv != 0){ // if there is water avilable (in and/or act)
            if (run_totalIn != 0){ // if there is water coming in

                double run_frac = run_totalDemand /run_totalIn;

                if (run_frac <= 1) {

                    //we can cover all only with in to the reach, reduce the components accordingly
                    inRD1.setValue(inRD1.getValue() * (1 - run_frac));
                    inRD2.setValue(inRD2.getValue() * (1 - run_frac));
                    inRG1.setValue(inRG1.getValue() * (1 - run_frac));
                    inRG2.setValue(inRG2.getValue() * (1 - run_frac));
                    totalTransfer.setValue(run_totalDemand);

                    // reduce tracked volumes proportionally
                    for(int i : run_listIndex){
                        run_ArrayTrackedVolumeRD1[i] = run_ArrayTrackedVolumeRD1[i] * (1 - run_frac);
                        run_ArrayTrackedVolumeRD2[i] = run_ArrayTrackedVolumeRD2[i] * (1 - run_frac);
                        run_ArrayTrackedVolumeRG1[i] = run_ArrayTrackedVolumeRG1[i] * (1 - run_frac);
                        run_ArrayTrackedVolumeRG2[i] = run_ArrayTrackedVolumeRG2[i] * (1 - run_frac);
                        run_ArrayTrackedVolumeTotal[i] = run_ArrayTrackedVolumeTotal[i] * (1 - run_frac);
                    }
                    if(run_SewTrack){
                        // reduce tracked volumes from sewer proportionally
                        run_TrackedVolumeSewRD1 = run_TrackedVolumeSewRD1 * (1 - run_frac);
                        run_TrackedVolumeSewRD2 = run_TrackedVolumeSewRD2 * (1 - run_frac);
                        run_TrackedVolumeSewRG1 = run_TrackedVolumeSewRG1 * (1 - run_frac);
                        run_TrackedVolumeSewRG2 = run_TrackedVolumeSewRG2 * (1 - run_frac);
                        run_TrackedVolumeSewTotal = run_TrackedVolumeSewTotal * (1 - run_frac);
                    }
                    if(run_WWTrack){
                        // reduce tracked volumes from WWTP proportionally
                        run_InWW = run_InWW * (1 - run_frac);
                    }

                } else {
                    //looking if we can cover the demand by including part of act...
                    run_frac = run_totalDemand / (run_totalIn+run_totalAct);

                    //we can cover only part of the demand with in, reduce the components to 0
                    inRD1.setValue(0);
                    inRD2.setValue(0);
                    inRG1.setValue(0);
                    inRG2.setValue(0);

                    // set incoming tracked volumes to 0
                    for(int i : run_listIndex){
                        run_ArrayTrackedVolumeRD1[i] = 0;
                        run_ArrayTrackedVolumeRD2[i] = 0;
                        run_ArrayTrackedVolumeRG1[i] = 0;
                        run_ArrayTrackedVolumeRG2[i] = 0;
                        run_ArrayTrackedVolumeTotal[i] = 0;
                    }
                    if(run_SewTrack){
                        // set incoming tracked sewer volumes to 0
                        run_TrackedVolumeSewRD1 = 0;
                        run_TrackedVolumeSewRD2 = 0;
                        run_TrackedVolumeSewRG1 = 0;
                        run_TrackedVolumeSewRG2 = 0;
                        run_TrackedVolumeSewTotal = 0;
                    }
                    if(run_WWTrack){
                        // set incoming tracked WW volumes to 0
                        run_InWW = 0;
                    }

                    if (run_frac <= 1) {
                        //we can cover all of the demand but not only with in..., reduce the components accordingly
                        double run_actDemand;
                        run_actDemand = run_totalDemand - run_totalIn;
                        double run_frac2 = run_actDemand/run_totalAct;
                        actRD1.setValue(actRD1.getValue() * (1 - run_frac2));
                        actRD2.setValue(actRD2.getValue() * (1 - run_frac2));
                        actRG1.setValue(actRG1.getValue() * (1 - run_frac2));
                        actRG2.setValue(actRG2.getValue() * (1 - run_frac2));
                        totalTransfer.setValue(run_totalDemand);

                        // reduce tracked stocked volumes proportionally
                        for(int i : run_listIndex){
                            run_ArrayTrackedVolume_actRD1[i] = run_ArrayTrackedVolume_actRD1[i] * (1 - run_frac2);
                            run_ArrayTrackedVolume_actRD2[i] = run_ArrayTrackedVolume_actRD2[i] * (1 - run_frac2);
                            run_ArrayTrackedVolume_actRG1[i] = run_ArrayTrackedVolume_actRG1[i] * (1 - run_frac2);
                            run_ArrayTrackedVolume_actRG2[i] = run_ArrayTrackedVolume_actRG2[i] * (1 - run_frac2);
                            run_ArrayTrackedVolume_actTotal[i] = run_ArrayTrackedVolume_actTotal[i] * (1 - run_frac2);
                        }
                        if(run_SewTrack){
                            // reduce tracked stocked volumes from sewer proportionally
                            run_TrackedVolumeSewRD1_act = run_TrackedVolumeSewRD1_act * (1 - run_frac2);
                            run_TrackedVolumeSewRD2_act = run_TrackedVolumeSewRD2_act * (1 - run_frac2);
                            run_TrackedVolumeSewRG1_act = run_TrackedVolumeSewRG1_act * (1 - run_frac2);
                            run_TrackedVolumeSewRG2_act = run_TrackedVolumeSewRG2_act * (1 - run_frac2);
                            run_TrackedVolumeSewTotal_act = run_TrackedVolumeSewTotal_act * (1 - run_frac2);
                        }
                        if(run_WWTrack){
                            // reduce tracked stocked volumes from WWTP proportionally
                            run_TrackedVolumeWW_act = run_TrackedVolumeWW_act * (1 - run_frac2);
                        }

                    } else {
                        // we can cover part of the demand ; reduce the act... to (1 - actPrel)*act...
                        actRD1.setValue(actRD1.getValue() * (1 - actPrel.getValue()));
                        actRD2.setValue(actRD2.getValue() * (1 - actPrel.getValue()));
                        actRG1.setValue(actRG1.getValue() * (1 - actPrel.getValue()));
                        actRG2.setValue(actRG2.getValue() * (1 - actPrel.getValue()));
                        totalTransfer.setValue(run_totalIn+run_totalAct);

                        // reduce tracked volumes to zero (except leaving minimum flow)
                        for(int i : run_listIndex){
                            run_ArrayTrackedVolume_actRD1[i] = run_ArrayTrackedVolume_actRD1[i] * (1 - actPrel.getValue());
                            run_ArrayTrackedVolume_actRD2[i] = run_ArrayTrackedVolume_actRD2[i] * (1 - actPrel.getValue());
                            run_ArrayTrackedVolume_actRG1[i] = run_ArrayTrackedVolume_actRG1[i] * (1 - actPrel.getValue());
                            run_ArrayTrackedVolume_actRG2[i] = run_ArrayTrackedVolume_actRG2[i] * (1 - actPrel.getValue());
                            run_ArrayTrackedVolume_actTotal[i] = run_ArrayTrackedVolume_actTotal[i] * (1 - actPrel.getValue());
                        }
                        if(run_SewTrack){
                            // reduce tracked stocked volumes from sewer to zero (except leaving minimum flow)
                            run_TrackedVolumeSewRD1_act = run_TrackedVolumeSewRD1_act * (1 - actPrel.getValue());
                            run_TrackedVolumeSewRD2_act = run_TrackedVolumeSewRD2_act * (1 - actPrel.getValue());
                            run_TrackedVolumeSewRG1_act = run_TrackedVolumeSewRG1_act * (1 - actPrel.getValue());
                            run_TrackedVolumeSewRG2_act = run_TrackedVolumeSewRG2_act * (1 - actPrel.getValue());
                            run_TrackedVolumeSewTotal_act = run_TrackedVolumeSewTotal_act * (1 - actPrel.getValue());
                        }
                        if(run_WWTrack){
                            // reduce tracked stocked volumes from WWTP to zero (except leaving minimum flow)
                            run_TrackedVolumeWW_act = run_TrackedVolumeWW_act * (1 - actPrel.getValue());
                        }
                    }
                }
            } else {// no water coming into reach but water available
                //looking if we can cover the demand by including usable part of act...
                double run_frac = run_totalDemand / (run_totalAct);
                if (run_frac <= 1) {
                    //we can cover all of the demand with act, reduce the components accordingly
                    actRD1.setValue(actRD1.getValue() * (1 - run_frac));
                    actRD2.setValue(actRD2.getValue() * (1 - run_frac));
                    actRG1.setValue(actRG1.getValue() * (1 - run_frac));
                    actRG2.setValue(actRG2.getValue() * (1 - run_frac));
                    totalTransfer.setValue(run_totalDemand);
                        
                    // reduce tracked volumes proportionally
                    for(int i : run_listIndex){
                        run_ArrayTrackedVolume_actRD1[i] = run_ArrayTrackedVolume_actRD1[i] * (1 - run_frac);
                        run_ArrayTrackedVolume_actRD2[i] = run_ArrayTrackedVolume_actRD2[i] * (1 - run_frac);
                        run_ArrayTrackedVolume_actRG1[i] = run_ArrayTrackedVolume_actRG1[i] * (1 - run_frac);
                        run_ArrayTrackedVolume_actRG2[i] = run_ArrayTrackedVolume_actRG2[i] * (1 - run_frac);
                        run_ArrayTrackedVolume_actTotal[i] = run_ArrayTrackedVolume_actTotal[i] * (1 - run_frac);
                    }
                    if(run_SewTrack){
                        // reduce tracked stocked volumes from sewer proportionally
                        run_TrackedVolumeSewRD1_act = run_TrackedVolumeSewRD1_act * (1 - run_frac);
                        run_TrackedVolumeSewRD2_act = run_TrackedVolumeSewRD2_act * (1 - run_frac);
                        run_TrackedVolumeSewRG1_act = run_TrackedVolumeSewRG1_act * (1 - run_frac);
                        run_TrackedVolumeSewRG2_act = run_TrackedVolumeSewRG2_act * (1 - run_frac);
                        run_TrackedVolumeSewTotal_act = run_TrackedVolumeSewTotal_act * (1 - run_frac);
                    }
                    if(run_WWTrack){
                        // reduce tracked stocked volumes from WWTP proportionally
                        run_TrackedVolumeWW_act = run_TrackedVolumeWW_act * (1 - run_frac);
                    }

                } else {
                    // we can only cover part of the demand ; reduce the act... to (1 - actPrel)*act...
                    actRD1.setValue(actRD1.getValue() * (1 - actPrel.getValue()));
                    actRD2.setValue(actRD2.getValue() * (1 - actPrel.getValue()));
                    actRG1.setValue(actRG1.getValue() * (1 - actPrel.getValue()));
                    actRG2.setValue(actRG2.getValue() * (1 - actPrel.getValue()));
                    totalTransfer.setValue(run_totalAct);
                        
                    // reduce tracked volumes to zero (except leaving minimum flow)
                    for(int i : run_listIndex){
                        run_ArrayTrackedVolume_actRD1[i] = run_ArrayTrackedVolume_actRD1[i] * (1 - actPrel.getValue());
                        run_ArrayTrackedVolume_actRD2[i] = run_ArrayTrackedVolume_actRD2[i] * (1 - actPrel.getValue());
                        run_ArrayTrackedVolume_actRG1[i] = run_ArrayTrackedVolume_actRG1[i] * (1 - actPrel.getValue());
                        run_ArrayTrackedVolume_actRG2[i] = run_ArrayTrackedVolume_actRG2[i] * (1 - actPrel.getValue());
                        run_ArrayTrackedVolume_actTotal[i] = run_ArrayTrackedVolume_actTotal[i] * (1 - actPrel.getValue());
                    }
                    if(run_SewTrack){
                        // reduce tracked stocked volumes from sewer to zero (except leaving minimum flow)
                        run_TrackedVolumeSewRD1_act = run_TrackedVolumeSewRD1_act * (1 - actPrel.getValue());
                        run_TrackedVolumeSewRD2_act = run_TrackedVolumeSewRD2_act * (1 - actPrel.getValue());
                        run_TrackedVolumeSewRG1_act = run_TrackedVolumeSewRG1_act * (1 - actPrel.getValue());
                        run_TrackedVolumeSewRG2_act = run_TrackedVolumeSewRG2_act * (1 - actPrel.getValue());
                        run_TrackedVolumeSewTotal_act = run_TrackedVolumeSewTotal_act * (1 - actPrel.getValue());
                    }
                    if(run_WWTrack){
                        // reduce tracked stocked volumes from WWTP proportionally
                        run_TrackedVolumeWW_act = run_TrackedVolumeWW_act * (1 - actPrel.getValue());
                    }
                }
            }
            
            //distribute total transfer over all HRUs
            double run_providedFraction;
            if (run_totalDemand != 0.){
                run_providedFraction = totalTransfer.getValue()/run_totalDemand;
            } else {
                run_providedFraction = 1;
            }
            double run_providedWater_tmp=0.;
            for (Attribute.Entity run_hru : run_l) {
                double run_waterRequirements = run_hru.getDouble(waterRequirementsName.getValue());
                run_hru.setDouble(irrigationWaterName.getValue(), run_waterRequirements * run_providedFraction);
                run_providedWater_tmp= run_providedWater_tmp + run_waterRequirements * run_providedFraction;
            }
            // restitute lost water to RD2 (when efficiency of the irrigation network <1) :
            inRD2.setValue(inRD2.getValue()+Math.max(0.,totalTransfer.getValue()-run_providedWater_tmp) );
        } else { // no water available
            for (Attribute.Entity run_hru : run_l) {
                run_hru.setDouble(irrigationWaterName.getValue(), 0); 
            }
            totalTransfer.setValue(0.); 
        }
        // read tracked volumes of the current reach --> incoming
        trackedVolumeRD1Array.setValue(run_ArrayTrackedVolumeRD1);
        trackedVolumeRD2Array.setValue(run_ArrayTrackedVolumeRD2);
        trackedVolumeRG1Array.setValue(run_ArrayTrackedVolumeRG1);
        trackedVolumeRG2Array.setValue(run_ArrayTrackedVolumeRG2);
        trackedVolumeTotalArray.setValue(run_ArrayTrackedVolumeTotal);
        // available
        trackedVolumeRD1_actArray.setValue(run_ArrayTrackedVolume_actRD1);
        trackedVolumeRD2_actArray.setValue(run_ArrayTrackedVolume_actRD2);
        trackedVolumeRG1_actArray.setValue(run_ArrayTrackedVolume_actRG1);
        trackedVolumeRG2_actArray.setValue(run_ArrayTrackedVolume_actRG2);
        trackedVolumeTotal_actArray.setValue(run_ArrayTrackedVolume_actTotal);
        if(run_SewTrack){
            // read tracked volumes of the current reach --> incoming
            trackedVolumeSewRD1.setValue(run_TrackedVolumeSewRD1);
            trackedVolumeSewRD2.setValue(run_TrackedVolumeSewRD2);
            trackedVolumeSewRG1.setValue(run_TrackedVolumeSewRG1);
            trackedVolumeSewRG2.setValue(run_TrackedVolumeSewRG2);
            trackedVolumeSewTotal.setValue(run_TrackedVolumeSewTotal);
            // available
            trackedVolumeSewRD1_act.setValue(run_TrackedVolumeSewRD1_act);
            trackedVolumeSewRD2_act.setValue(run_TrackedVolumeSewRD2_act);
            trackedVolumeSewRG1_act.setValue(run_TrackedVolumeSewRG1_act);
            trackedVolumeSewRG2_act.setValue(run_TrackedVolumeSewRG2_act);
            trackedVolumeSewTotal_act.setValue(run_TrackedVolumeSewTotal_act);
        }
        if(run_WWTrack){
            // read tracked volumes of the current reach --> incoming
            trackedVolumeWW_act.setValue(run_TrackedVolumeWW_act);
            // available
            inWW.setValue(run_InWW);
        }
        //remove all HRUs from demand list
        run_l.removeAll(run_l);
    }
}
