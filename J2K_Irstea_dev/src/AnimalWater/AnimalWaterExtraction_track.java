/*
 * AnimalWaterExtraction_track.java
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
package AnimalWater;

import jams.data.*;
import jams.model.*;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 *
 * @author Sven Kralisch <sven.kralisch at uni-jena.de>
 */
@JAMSComponentDescription(
        title = "AnimalWaterExtraction_track",
        author = "Nico Hachgenei",
        description = "Extraction of water from reaches for animal consumption"
        + " using animal need and limit to available water"
	+ " water comes from incoming water to the reach and water inside the reach (actRG1, etc..)."
        + " modified for tracking version: also remove water from tracked volumes"
        + "New names. Bugfix in water extraction from act. Use of more"
        + "internal variables. Application of extraction limitation to in and act, equal extraction"
        + "from both.",
        date = "2024-05-29",
        version = "2.1_0"
)
@VersionComments(entries = {
    @VersionComments.Entry(version = "1.0_0", comment = "Initial version"),
    @VersionComments.Entry(version = "2.0_0", comment = "Modififed case where no water comes into the reach,"
            + " but water is inside the reach --> now water will be extracted from the reach in this case."
            + "New names. Bugfix in water extraction from act. Use of more"
            + "internal variables. Application of extraction limitation to in and act, equal extraction"
            + "from both. All variables contain animal terminology"),
    @VersionComments.Entry(version = "2.1_0", comment = "Added allowedMinDischarge -> min remaining discharge"
            + " after extraction")
})
public class AnimalWaterExtraction_track extends JAMSComponent {

    /*
     *  Component attributes
     */
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Reaches list"
    )
    public Attribute.EntityCollection reaches;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Current time"
            )
            public Attribute.Calendar time;
                
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Julian day of beginning of summer (hot, dry conditions) - parameter"
            )
            public Attribute.Double summerStart;
                
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Julian day of end of summer (hot, dry conditions) - parameter"
            )
            public Attribute.Double summerEnd;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "Current time step RD1 inflow into reach. Will be updated by this component,"+
                    "extracting water for irrigation. - input / state variable",
            unit = "L"
    )
    public Attribute.Double inRD1;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "Current time step RD2 inflow into reach. Will be updated by this component,"+
                    "extracting water for irrigation. - input / state variable",
            unit = "L"
    )
    public Attribute.Double inRD2;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "Current time step RG1 inflow into reach. Will be updated by this component,"+
                    "extracting water for irrigation. - input / state variable",
            unit = "L"
    )
    public Attribute.Double inRG1;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "Current time step RG2 inflow into reach. Will be updated by this component,"+
                    "extracting water for irrigation. - input / state variable",
            unit = "L"
    )
    public Attribute.Double inRG2;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "Current time step RD1 volume inside reach. Will be updated by this component"+
                    ", extracting water for irrigation."+
                    "- state variable",
            unit = "L"
    )
    public Attribute.Double actRD1;
        
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "Current time step RD2 volume inside reach. Will be updated by this component"+
                    ", extracting water for irrigation."+
                    "- state variable",
            unit = "L"
    )
    public Attribute.Double actRD2;
        
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "Current time step RG1 volume inside reach. Will be updated by this component"+
                    ", extracting water for irrigation."+
                    "- state variable",
            unit = "L"
    )
    public Attribute.Double actRG1;
            
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "Current time step RG2 volume inside reach. Will be updated by this component"+
                    ", extracting water for irrigation."+
                    "- state variable",
            unit = "L"
    )
    public Attribute.Double actRG2;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
            description = "Ratio of water available (allowed to be taken) for animals over water present" +
                    "in the reach (actR.. + inR..). - parameter",
            defaultValue = "1.0"
    )
    public Attribute.Double allowedAnimExtractionFraction;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Legal minimum discharge that needs to be kept in the stream after irrgigation"+
                    " extraction - parameter",
            defaultValue = "0.0",
            unit = "L"
    )
    public Attribute.Double allowedMinDischarge;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "Total demand of water for animal drinking. For verification purposes. No need to be defined. - output"
    )
    public Attribute.Double totalAnimDemand;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "volume extracted for animals, cummulative over all reaches - state variable (or output of this module?)"
    )
    public Attribute.Double totalAnimExtracted;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "volume extracted for animals from current reach - output"
    )
    public Attribute.Double animExtractedReach;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "Total water in the reach available for animal drinking in the reach (in + act; sum of the four components)"+
                    "BEFORE EXTRACTION. For verification purposes. No need to be defined. - output"
    )
    public Attribute.Double totalAnimAvailable;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
        description = "Array of reach names (IDs). Used for tracked volumes from each reach.")
    public Attribute.DoubleArray reachIDs;   

    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "Array of tracked volumes per source reach in RD1. Water can be taken from here - state variable"
            )
    public Attribute.DoubleArray trackedVolumeRD1Array;
        
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "Array of tracked volumes per source reach in RD2. Water can be taken from here - state variable"
            )
    public Attribute.DoubleArray trackedVolumeRD2Array;  
        
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "Array of tracked volumes per source reach in RG1. Water can be taken from here - state variable"
            )
    public Attribute.DoubleArray trackedVolumeRG1Array;  
        
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "Array of tracked volumes per source reach in RG2. Water can be taken from here - state variable"
            )
    public Attribute.DoubleArray trackedVolumeRG2Array;  
        
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "Array of total tracked volumes per source reach. Water can be taken from here - state variable"
            )
    public Attribute.DoubleArray trackedVolumeTotalArray;   
             
    @JAMSVarDescription(
        access = JAMSVarDescription.AccessType.READWRITE,
        description = "Array of RD1 remaining volume from tracked reach in current reach after routing. Water can be taken from here - state variable"
        )
    public Attribute.DoubleArray trackedVolumeRD1_actArray; 
             
    @JAMSVarDescription(
        access = JAMSVarDescription.AccessType.READWRITE,
        description = "Array of RD2 remaining volume from tracked reach in current reach after routing. Water can be taken from here - state variable"
        )
    public Attribute.DoubleArray trackedVolumeRD2_actArray; 
             
    @JAMSVarDescription(
        access = JAMSVarDescription.AccessType.READWRITE,
        description = "Array of RG1 remaining volume from tracked reach in current reach after routing. Water can be taken from here - state variable"
        )
    public Attribute.DoubleArray trackedVolumeRG1_actArray; 
             
    @JAMSVarDescription(
        access = JAMSVarDescription.AccessType.READWRITE,
        description = "Array of RG2 remaining volume from tracked reach in current reach after routing. Water can be taken from here - state variable"
        )
    public Attribute.DoubleArray trackedVolumeRG2_actArray;
            
    @JAMSVarDescription(
        access = JAMSVarDescription.AccessType.READWRITE,
        description = "Array of total remaining volume from tracked reach in current reach after routing. Water can be taken from here - state variable"
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
            description = "Tracked RD1 volume from Sewer in reach. Water can be taken from here - state variable",
            unit = "L"
    )
    public Attribute.Double trackedVolumeSewRD1;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "Tracked RD2 volume from Sewer in reach. Water can be taken from here - state variable",
            unit = "L"
    )
    public Attribute.Double trackedVolumeSewRD2;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "Tracked RG1 volume from Sewer in reach. Water can be taken from here - state variable",
            unit = "L"
    )
    public Attribute.Double trackedVolumeSewRG1;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "Tracked RG2 volume from Sewer in reach. Water can be taken from here - state variable",
            unit = "L"
    )
    public Attribute.Double trackedVolumeSewRG2;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "Tracked total volume from Sewer in reach. Water can be taken from here - state variable",
            unit = "L"
    )
    public Attribute.Double trackedVolumeSewTotal; 
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "Remaining tracked RD1 volume from Sewer in current reach after routing. Water can be taken from here - state variable",
            unit = "L"
    )
    public Attribute.Double trackedVolumeSewRD1_act;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "Remaining tracked RD2 volume from Sewer in reach in current reach after routing. Water can be taken from here - state variable",
            unit = "L"
    )
    public Attribute.Double trackedVolumeSewRD2_act;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "Remaining tracked RG1 volume from Sewer in reach in current reach after routing. Water can be taken from here - state variable",
            unit = "L"
    )
    public Attribute.Double trackedVolumeSewRG1_act;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "Remaining tracked RG2 volume from Sewer in reach in current reach after routing. Water can be taken from here - state variable",
            unit = "L"
    )
    public Attribute.Double trackedVolumeSewRG2_act;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "Remaining tracked total volume from Sewer in reach in current reach after routing. Water can be taken from here - state variable",
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
            description = "Tracked volume from waste water treatment plant incoming into reach. Water can be taken from here - state variable",
            unit = "L"
    )
    public Attribute.Double inWW;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "Remaining tracked volume from waste water treatment plant stored in current reach. Water can be taken from here - state variable",
            unit = "L"
    )
    public Attribute.Double trackedVolumeWW_act;
            
    

    /*
     *  Component run stages
     */

    @Override
    public void run() {
        Attribute.Entity run_currentReach = reaches.getCurrent();
        // no need to check if animals are drinking from this reach --> this component is only executed if this is the case
        
        double run_inRD1 = inRD1.getValue();
        double run_inRD2 = inRD2.getValue();
        double run_inRG1 = inRG1.getValue();
        double run_inRG2 = inRG2.getValue();
        double run_actRD1 = actRD1.getValue();
        double run_actRD2 = actRD2.getValue();
        double run_actRG1 = actRG1.getValue();
        double run_actRG2 = actRG2.getValue();
        double run_allowedExtractionFraction = allowedAnimExtractionFraction.getValue();
        double run_allowedMinDischarge = allowedMinDischarge.getValue();
        
        boolean run_SewTrack = trackSewers.getValue() == 1;
        boolean run_WWTrack = trackWW.getValue() == 1;
        
        // read tracked volumes of the current reach --> incoming
        double[] run_ArrayTrackedVolumeRD1 = trackedVolumeRD1Array.getValue();
        double[] run_ArrayTrackedVolumeRD2 = trackedVolumeRD2Array.getValue();
        double[] run_ArrayTrackedVolumeRG1 = trackedVolumeRG1Array.getValue();
        double[] run_ArrayTrackedVolumeRG2 = trackedVolumeRG2Array.getValue();
        double[] run_ArrayTrackedVolumeTotal = trackedVolumeTotalArray.getValue();
        // stored
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
        double[] run_reachIDs = this.reachIDs.getValue();
        
        // List of indexes of Reaches contributing to current reach
        List<Integer> run_listIndex = new ArrayList<>();
        for(int i = 0; i < run_reachIDs.length; i++){
            if(run_ArrayTrackedVolumeTotal[i] != -999){
                run_listIndex.add(i);
            }
        }
        
        // calculate water available for animal drinking
        double run_totalIn = run_inRD1 + run_inRD2 + run_inRG1 + run_inRG2; // all water in inflow (for proportional extraction)
        double run_totalAct = run_actRD1 + run_actRD2 + run_actRG1 + run_actRG2; // all water in act (for proportional extraction)
        double run_totalStorage = run_totalIn + run_totalAct; // all water in inflow and act
        if (run_allowedMinDischarge > 0.0) { // there is a minimum absolute discharge to respect
            if (run_totalStorage>run_allowedMinDischarge){ // there is more water -> we can extract
                // limit allowed extraction fraction to whichever is smaller (run_allowedExtractionFraction or excess of run_allowedMinDischarge)
                run_allowedExtractionFraction = Math.min( (run_totalStorage-run_allowedMinDischarge)/run_totalStorage , run_allowedExtractionFraction);              
            } else { // there is not enough water -> no extraction allowed
                run_allowedExtractionFraction = 0.0;
            }
        }
        double run_inAvailable = run_allowedExtractionFraction * run_totalIn;
        double run_actAvailable = run_allowedExtractionFraction * run_totalAct; // water in the reach available for animal drinking
        double run_totalAvailable = run_inAvailable + run_actAvailable; // all available water
        totalAnimAvailable.setValue(run_totalAvailable); // water available for animal drinking purpose at this time step
        
        double run_totalAnimDemand;
        double run_animExtractedReach; // internal variable to store actually extracted volume
//        double run_providedFraction;
        
        // check season in order to decide which quantity animals drink
        int run_jDay = time.get(Calendar.DAY_OF_YEAR);
        if (run_jDay >= summerStart.getValue() && run_jDay <= summerEnd.getValue()) {
            run_totalAnimDemand = run_currentReach.getDouble("cons_su");
        } else {
            run_totalAnimDemand = run_currentReach.getDouble("cons_wi");
        }
        this.totalAnimDemand.setValue(run_totalAnimDemand);
        
        //calculate proportion of total water that is needed
        if (run_totalAvailable != 0.0 & (run_totalAnimDemand != 0.0)){ // if there is water available (in and/or act) AND demanded for irrigation
            double run_availableDemandFraction = run_totalAnimDemand / run_totalAvailable;// fraction of available water that is demanded for irrigation
            
            if (run_availableDemandFraction <=1){ // demand can be satisfied with available water from inflow and act
                double run_storageDemandFraction = run_totalAnimDemand / run_totalStorage;// fraction of all stored water that is demanded for irrigation
                run_animExtractedReach = run_totalAnimDemand; // we can satisfy the demand (extract everything that is needed)
//                run_providedFraction = 1;
                
                // extract proportionally from inflow (ratio demand over all water)
                inRD1.setValue(run_inRD1 * (1 - run_storageDemandFraction));
                inRD2.setValue(run_inRD2 * (1 - run_storageDemandFraction));
                inRG1.setValue(run_inRG1 * (1 - run_storageDemandFraction));
                inRG2.setValue(run_inRG2 * (1 - run_storageDemandFraction));
                // extract proportionally from act (ratio demand over all water)
                actRD1.setValue(run_actRD1 * (1 - run_storageDemandFraction));
                actRD2.setValue(run_actRD2 * (1 - run_storageDemandFraction));
                actRG1.setValue(run_actRG1 * (1 - run_storageDemandFraction));
                actRG2.setValue(run_actRG2 * (1 - run_storageDemandFraction));
                
                // reduce tracked volumes proportionally
                for(int i : run_listIndex){ // loop over tracked reaches
                    // incoming
                    run_ArrayTrackedVolumeRD1[i] = run_ArrayTrackedVolumeRD1[i] * (1 - run_storageDemandFraction);
                    run_ArrayTrackedVolumeRD2[i] = run_ArrayTrackedVolumeRD2[i] * (1 - run_storageDemandFraction);
                    run_ArrayTrackedVolumeRG1[i] = run_ArrayTrackedVolumeRG1[i] * (1 - run_storageDemandFraction);
                    run_ArrayTrackedVolumeRG2[i] = run_ArrayTrackedVolumeRG2[i] * (1 - run_storageDemandFraction);
                    run_ArrayTrackedVolumeTotal[i] = run_ArrayTrackedVolumeTotal[i] * (1 - run_storageDemandFraction);
                    // stored in reach
                    run_ArrayTrackedVolume_actRD1[i] = run_ArrayTrackedVolume_actRD1[i] * (1 - run_storageDemandFraction);
                    run_ArrayTrackedVolume_actRD2[i] = run_ArrayTrackedVolume_actRD2[i] * (1 - run_storageDemandFraction);
                    run_ArrayTrackedVolume_actRG1[i] = run_ArrayTrackedVolume_actRG1[i] * (1 - run_storageDemandFraction);
                    run_ArrayTrackedVolume_actRG2[i] = run_ArrayTrackedVolume_actRG2[i] * (1 - run_storageDemandFraction);
                    run_ArrayTrackedVolume_actTotal[i] = run_ArrayTrackedVolume_actTotal[i] * (1 - run_storageDemandFraction);
                }
                if(run_SewTrack){
                    // reduce tracked volumes from sewer proportionally
                    run_TrackedVolumeSewRD1 = run_TrackedVolumeSewRD1 * (1 - run_storageDemandFraction);
                    run_TrackedVolumeSewRD2 = run_TrackedVolumeSewRD2 * (1 - run_storageDemandFraction);
                    run_TrackedVolumeSewRG1 = run_TrackedVolumeSewRG1 * (1 - run_storageDemandFraction);
                    run_TrackedVolumeSewRG2 = run_TrackedVolumeSewRG2 * (1 - run_storageDemandFraction);
                    run_TrackedVolumeSewTotal = run_TrackedVolumeSewTotal * (1 - run_storageDemandFraction);
                    // reduce tracked stocked volumes from sewer proportionally
                    run_TrackedVolumeSewRD1_act = run_TrackedVolumeSewRD1_act * (1 - run_storageDemandFraction);
                    run_TrackedVolumeSewRD2_act = run_TrackedVolumeSewRD2_act * (1 - run_storageDemandFraction);
                    run_TrackedVolumeSewRG1_act = run_TrackedVolumeSewRG1_act * (1 - run_storageDemandFraction);
                    run_TrackedVolumeSewRG2_act = run_TrackedVolumeSewRG2_act * (1 - run_storageDemandFraction);
                    run_TrackedVolumeSewTotal_act = run_TrackedVolumeSewTotal_act * (1 - run_storageDemandFraction);
                }
                if(run_WWTrack){
                    // reduce tracked volumes from WWTP proportionally
                    run_InWW = run_InWW * (1 - run_storageDemandFraction);
                    // reduce tracked stocked volumes from WWTP proportionally
                    run_TrackedVolumeWW_act = run_TrackedVolumeWW_act * (1 - run_storageDemandFraction);
                }
                
            } else { // not all of the demand can be satisfied from available water. Only available water will be extracted
                run_animExtractedReach = run_totalAvailable; // we extract all available water
//                run_providedFraction = run_animExtractedReach/run_totalAnimDemand;
                
                // extract proportionally from inflow (ratio demand over all water)
                inRD1.setValue(run_inRD1 * (1 - run_allowedExtractionFraction));
                inRD2.setValue(run_inRD2 * (1 - run_allowedExtractionFraction));
                inRG1.setValue(run_inRG1 * (1 - run_allowedExtractionFraction));
                inRG2.setValue(run_inRG2 * (1 - run_allowedExtractionFraction));
                // extract proportionally from act (ratio demand over all water)
                actRD1.setValue(run_actRD1 * (1 - run_allowedExtractionFraction));
                actRD2.setValue(run_actRD2 * (1 - run_allowedExtractionFraction));
                actRG1.setValue(run_actRG1 * (1 - run_allowedExtractionFraction));
                actRG2.setValue(run_actRG2 * (1 - run_allowedExtractionFraction));
                
                // reduce tracked volumes proportionally
                for(int i : run_listIndex){ // loop over tracked reaches
                    // incoming
                    run_ArrayTrackedVolumeRD1[i] = run_ArrayTrackedVolumeRD1[i] * (1 - run_allowedExtractionFraction);
                    run_ArrayTrackedVolumeRD2[i] = run_ArrayTrackedVolumeRD2[i] * (1 - run_allowedExtractionFraction);
                    run_ArrayTrackedVolumeRG1[i] = run_ArrayTrackedVolumeRG1[i] * (1 - run_allowedExtractionFraction);
                    run_ArrayTrackedVolumeRG2[i] = run_ArrayTrackedVolumeRG2[i] * (1 - run_allowedExtractionFraction);
                    run_ArrayTrackedVolumeTotal[i] = run_ArrayTrackedVolumeTotal[i] * (1 - run_allowedExtractionFraction);
                    // stored in reach
                    run_ArrayTrackedVolume_actRD1[i] = run_ArrayTrackedVolume_actRD1[i] * (1 - run_allowedExtractionFraction);
                    run_ArrayTrackedVolume_actRD2[i] = run_ArrayTrackedVolume_actRD2[i] * (1 - run_allowedExtractionFraction);
                    run_ArrayTrackedVolume_actRG1[i] = run_ArrayTrackedVolume_actRG1[i] * (1 - run_allowedExtractionFraction);
                    run_ArrayTrackedVolume_actRG2[i] = run_ArrayTrackedVolume_actRG2[i] * (1 - run_allowedExtractionFraction);
                    run_ArrayTrackedVolume_actTotal[i] = run_ArrayTrackedVolume_actTotal[i] * (1 - run_allowedExtractionFraction);
                }
                if(run_SewTrack){
                    // reduce tracked volumes from sewer proportionally
                    run_TrackedVolumeSewRD1 = run_TrackedVolumeSewRD1 * (1 - run_allowedExtractionFraction);
                    run_TrackedVolumeSewRD2 = run_TrackedVolumeSewRD2 * (1 - run_allowedExtractionFraction);
                    run_TrackedVolumeSewRG1 = run_TrackedVolumeSewRG1 * (1 - run_allowedExtractionFraction);
                    run_TrackedVolumeSewRG2 = run_TrackedVolumeSewRG2 * (1 - run_allowedExtractionFraction);
                    run_TrackedVolumeSewTotal = run_TrackedVolumeSewTotal * (1 - run_allowedExtractionFraction);
                    // reduce tracked stocked volumes from sewer proportionally
                    run_TrackedVolumeSewRD1_act = run_TrackedVolumeSewRD1_act * (1 - run_allowedExtractionFraction);
                    run_TrackedVolumeSewRD2_act = run_TrackedVolumeSewRD2_act * (1 - run_allowedExtractionFraction);
                    run_TrackedVolumeSewRG1_act = run_TrackedVolumeSewRG1_act * (1 - run_allowedExtractionFraction);
                    run_TrackedVolumeSewRG2_act = run_TrackedVolumeSewRG2_act * (1 - run_allowedExtractionFraction);
                    run_TrackedVolumeSewTotal_act = run_TrackedVolumeSewTotal_act * (1 - run_allowedExtractionFraction);
                }
                if(run_WWTrack){
                    // reduce tracked volumes from WWTP proportionally
                    run_InWW = run_InWW * (1 - run_allowedExtractionFraction);
                    // reduce tracked stocked volumes from WWTP proportionally
                    run_TrackedVolumeWW_act = run_TrackedVolumeWW_act * (1 - run_allowedExtractionFraction);
                }
            }
        } else { 
            run_animExtractedReach = 0;
        }
        // extracted volume for animals from this reach
        animExtractedReach.setValue(run_animExtractedReach);
        // extracted volume for all animals (cumulative over reaches)
        this.totalAnimExtracted.setValue(this.totalAnimExtracted.getValue() + run_animExtractedReach);
            
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
    }
}
