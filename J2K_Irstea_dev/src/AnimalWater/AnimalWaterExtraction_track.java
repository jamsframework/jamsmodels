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
        title = "",
        author = "Nico Hachgenei",
        description = "Extraction of water from reaches for animal consumption"
        + " using animal need and limit to available water"
	+ " water comes from incoming water to the reach and water inside the reach (actRG1, etc..)."
        + " modified for tracking version: also remove water from tracked volumes",
        date = "2024-05-29",
        version = "1.0_0"
)
@VersionComments(entries = {
    @VersionComments.Entry(version = "1.0_0", comment = "Initial version")
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
            description = "inflow into RD1 component in reach used to extract water from - state variable or input ?"
    )
    public Attribute.Double inRD1;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "inflow into RD2 component in reach used to extract water from - state variable or input ?"
    )
    public Attribute.Double inRD2;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "inflow into RG1 component in reach used to extract water from - state variable or input ?"
    )
    public Attribute.Double inRG1;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "inflow into RG2 component in reach used to extract water from - state variable or input ?"
    )
    public Attribute.Double inRG2;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "current volume in actRD1 component in reach, used to extract water from - state variable or input ?"
    )
    public Attribute.Double actRD1;
        
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "current volume in actRD2 component in reach, used to extract water from - state variable or input ?"
    )
    public Attribute.Double actRD2;
        
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "current volume in actRG1 component in reach, used to extract water from - state variable or input ?"
    )
    public Attribute.Double actRG1;
            
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "current volume in actRG2 component in reach, used to extract water from - state variable or input ?"
    )
    public Attribute.Double actRG2;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
            description = "Ratio of water available for animals / water present in the reach (actR..). Which fraction are we allowed to take? - parameter",
            defaultValue = "1.0"
    )
    public Attribute.Double actPrel;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "Total demand of water for animal drinking. For verification purposes. No need to be defined. - output"
    )
    public Attribute.Double totalDemand;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "volume extracted for animals, cummulative over all reaches - state variable (or output of this module?)"
    )
    public Attribute.Double animalExtractedAll;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "volume extracted for animals from current reach - output"
    )
    public Attribute.Double animalExtractedR;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "Total water in the reach available for animal drinking. For verification purposes. No need to be defined. - output"
    )
    public Attribute.Double totalAvail;
    
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
        description = "Array of reach names (IDs), to check if the reaches contribute and loop over them - parameter")
    public Attribute.DoubleArray names;   

    
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
        List<Integer> run_listIndex = new ArrayList<>();
        for(int i = 0; i < run_names.length; i++){
            if(run_ArrayTrackedVolumeTotal[i] != -999){
                run_listIndex.add(i);
            }
        }
        // check if animals are drinking from this reach --> not in here, but add switch context for this
        
        // calculate water available for animal drinking
        double run_totalIn = inRD1.getValue() + inRD2.getValue() + inRG1.getValue() + inRG2.getValue();
        double run_totalAct = this.actPrel.getValue() * (actRD1.getValue() + actRD2.getValue() + actRG1.getValue() + actRG2.getValue()); // water in the reach that is available for animal needs
        double run_totalAv = run_totalIn + run_totalAct; // all available water for animal drinking
        this.totalAvail.setValue(run_totalAv); // all available water for animal drinking
        double run_totalDemand;
        
        // check season in order to decide which quantity animals drink
        int run_jDay = time.get(Calendar.DAY_OF_YEAR);
        if (run_jDay >= summerStart.getValue() && run_jDay <= summerEnd.getValue()) {
            run_totalDemand = run_currentReach.getDouble("cons_su");
        } else {
            run_totalDemand = run_currentReach.getDouble("cons_wi");
        }
        this.totalDemand.setValue(run_totalDemand);
        
        //calculate proportion of total water that is needed
        if (run_totalAv != 0.0){ // if there is water available
            if (run_totalIn != 0){ // if there is water coming into reach

                double run_frac = run_totalDemand /run_totalIn;

                if (run_frac <= 1) {

                    //we can cover all only with input to the reach, reduce the components accordingly
                    inRD1.setValue(inRD1.getValue() * (1 - run_frac));
                    inRD2.setValue(inRD2.getValue() * (1 - run_frac));
                    inRG1.setValue(inRG1.getValue() * (1 - run_frac));
                    inRG2.setValue(inRG2.getValue() * (1 - run_frac));
                    animalExtractedR.setValue(run_totalDemand);
                    
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
                    //looking if we can cover the demand by including usable part of act...
                    run_frac = run_totalDemand / (run_totalIn+run_totalAct);

                    //we can cover only part of the demand with input, reduce the components to 0
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
                        //we can cover all of the demand with input and act together, reduce the components accordingly
                        double run_actDemand;
                        run_actDemand = run_totalDemand - run_totalIn;
                        double run_frac2 = run_actDemand/run_totalAct;
                        actRD1.setValue(actRD1.getValue() * (1 - run_frac2));
                        actRD2.setValue(actRD2.getValue() * (1 - run_frac2));
                        actRG1.setValue(actRG1.getValue() * (1 - run_frac2));
                        actRG2.setValue(actRG2.getValue() * (1 - run_frac2));
                        animalExtractedR.setValue(run_totalDemand);
                        
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
                        // we can only cover part of the demand ; reduce the act... to (1 - actPrel)*act...
                        actRD1.setValue(actRD1.getValue() * (1 - actPrel.getValue()));
                        actRD2.setValue(actRD2.getValue() * (1 - actPrel.getValue()));
                        actRG1.setValue(actRG1.getValue() * (1 - actPrel.getValue()));
                        actRG2.setValue(actRG2.getValue() * (1 - actPrel.getValue()));
                        animalExtractedR.setValue(run_totalIn+run_totalAct);
                        
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

            } else { // if no water coming into reach, but there is water in the reach act
                //looking if we can cover the demand by including usable part of act...
                double run_frac = run_totalDemand / (run_totalAct);
                if (run_frac <= 1) {
                    //we can cover all of the demand with act, reduce the components accordingly
                    actRD1.setValue(actRD1.getValue() * (1 - run_frac));
                    actRD2.setValue(actRD2.getValue() * (1 - run_frac));
                    actRG1.setValue(actRG1.getValue() * (1 - run_frac));
                    actRG2.setValue(actRG2.getValue() * (1 - run_frac));
                    animalExtractedR.setValue(run_totalDemand);
                        
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
                    animalExtractedR.setValue(run_totalAct);
                        
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
        } else { 
            animalExtractedR.setValue(0.);
        }
        // extracted volume for all animals (cumulative over reaches)
        this.animalExtractedAll.setValue(this.animalExtractedAll.getValue() + this.animalExtractedR.getValue());
        
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
