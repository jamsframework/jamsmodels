/*
 * WaterInOut.java
 * Created on 30.05.2024, 16:17:09
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
package tracking;

import management.*;
import jams.data.*;
import jams.model.*;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 *
 * @author Nico Hachgenei
 */
@JAMSComponentDescription(
        title = "WaterInOut_track",
        author = "Nico Hachgenei",
        description = "Extraction of water from reaches (e.g. for drinking water)"
        + " and input into reach (e.g. treated waste water), using a file with one"
        + " value per tiem step and limit to available water."
	+ " Water comes from incoming water to the reach and water inside the reach (inR.. / actR..)."
        + " Water is put into specified component or distributed."
        + " Modifications for tracking: 1) take proportionally from tracked volumes."
        + " 2) track injected wastewater",
        date = "2024-05-30",
        version = "2.0_0"
)
@VersionComments(entries = {
    @VersionComments.Entry(version = "1.0_0", comment = "Initial version"),
    @VersionComments.Entry(version = "2.0_0", comment = "improved variable names,"
            + "take water proportionally from both, in and act,"
            + "corrected fractions.")
})
public class WaterInOut_track extends JAMSComponent {

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
            description = "Volume to extract (negative) or inject (positive)"
    )
    public Attribute.Double VolumeIO;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "target component name ('R..'). Needed in case of injection. 'distr' for distributing over all components"
    )
    public Attribute.String targetComp;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "incoming RD1 component into reach"
    )
    public Attribute.Double inRD1;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "incoming RD2 component into reach"
    )
    public Attribute.Double inRD2;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "incoming RG1 component into reach"
    )
    public Attribute.Double inRG1;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "incoming RG2 component into reach"
    )
    public Attribute.Double inRG2;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "actRD1 component in reach"
    )
    public Attribute.Double actRD1;
        
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "actRD2 component in reach"
    )
    public Attribute.Double actRD2;
        
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "actRG1 component in reach"
    )
    public Attribute.Double actRG1;
            
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "actRG2 component in reach"
    )
    public Attribute.Double actRG2;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Ratio of water available (allowed to be taken) for extraction over water present"+
                    "in the reach (actR..). Between 0 and 1. - parameter",
            defaultValue = "1.0"
    )
    public Attribute.Double allowedIOExtractionFraction;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "Total demand of water. For verification purposes"
    )
    public Attribute.Double totalIODemand;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "Total input of water. For verification purposes"
    )
    public Attribute.Double totalIOInput;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "volume extracted, cummulative -> all reaches"
    )
    public Attribute.Double IOExtractedAll;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "volume extracted from current reach"
    )
    public Attribute.Double IOExtractedR;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "volume added, cummulative -> all reaches"
    )
    public Attribute.Double IOaddedAll;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "volume added to current reach"
    )
    public Attribute.Double IOaddedR;
    
//    @JAMSVarDescription(
//            access = JAMSVarDescription.AccessType.WRITE,
//            description = "Total water in the reach available for irrigation"
//    )
//    public Attribute.Double totalAvail;
    
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
        description = "Array of reach names (IDs)")
    public Attribute.DoubleArray names;   

    
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
        access = JAMSVarDescription.AccessType.READWRITE,
        description = "Are there tracked sewers in the model? (1/0)"
        )
    public Attribute.Double trackSewers;
            
    @JAMSVarDescription(
        access = JAMSVarDescription.AccessType.READWRITE,
        description = "Is there a tracked waste water treatment plant in the model? (1/0)"
        )
    public Attribute.Double trackWW;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "Tracked RD1 volume from Sewer in reach",
            unit = "L"
    )
    public Attribute.Double trackedVolumeSewRD1;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "Tracked RD2 volume from Sewer in reach",
            unit = "L"
    )
    public Attribute.Double trackedVolumeSewRD2;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "Tracked RG1 volume from Sewer in reach",
            unit = "L"
    )
    public Attribute.Double trackedVolumeSewRG1;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "Tracked RG2 volume from Sewer in reach",
            unit = "L"
    )
    public Attribute.Double trackedVolumeSewRG2;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "Tracked total volume from Sewer in reach",
            unit = "L"
    )
    public Attribute.Double trackedVolumeSewTotal; 
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "Remaining tracked RD1 volume from Sewer",
            unit = "L"
    )
    public Attribute.Double trackedVolumeSewRD1_act;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "Remaining tracked RD2 volume from Sewer in reach",
            unit = "L"
    )
    public Attribute.Double trackedVolumeSewRD2_act;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "Remaining tracked RG1 volume from Sewer in reach",
            unit = "L"
    )
    public Attribute.Double trackedVolumeSewRG1_act;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "Remaining tracked RG2 volume from Sewer in reach",
            unit = "L"
    )
    public Attribute.Double trackedVolumeSewRG2_act;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "Remaining tracked total volume from Sewer in reach",
            unit = "L"
    )
    public Attribute.Double trackedVolumeSewTotal_act;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "Tracked incoming volume from waste water treatment plant in reach",
            unit = "L"
    )
    public Attribute.Double InWW;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "Tracked volume from waste water treatment plant in reach",
            unit = "L"
    )
    public Attribute.Double trackedVolumeWW;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "Remaining tracked volume from waste water treatment plant",
            unit = "L"
    )
    public Attribute.Double trackedVolumeWW_act;
    

    /*
     *  Component run stages
     */

    @Override
    public void run() {

//        Attribute.Entity currentReach = reaches.getCurrent();

        boolean run_SewTrack = trackSewers.getValue() == 1;
        boolean run_WWTrack = trackWW.getValue() == 1;
        
        // read tracked volumes of the current reach --> incoming
        double[] ArrayTrackedVolumeRD1 = trackedVolumeRD1Array.getValue();
        double[] ArrayTrackedVolumeRD2 = trackedVolumeRD2Array.getValue();
        double[] ArrayTrackedVolumeRG1 = trackedVolumeRG1Array.getValue();
        double[] ArrayTrackedVolumeRG2 = trackedVolumeRG2Array.getValue();
        double[] ArrayTrackedVolumeTotal = trackedVolumeTotalArray.getValue();
        // available
        double[] ArrayTrackedVolume_actRD1 = trackedVolumeRD1_actArray.getValue();
        double[] ArrayTrackedVolume_actRD2 = trackedVolumeRD2_actArray.getValue();
        double[] ArrayTrackedVolume_actRG1 = trackedVolumeRG1_actArray.getValue();
        double[] ArrayTrackedVolume_actRG2 = trackedVolumeRG2_actArray.getValue();
        double[] ArrayTrackedVolume_actTotal = trackedVolumeTotal_actArray.getValue();
        
        double TrackedVolumeSewRD1, TrackedVolumeSewRD2, 
                TrackedVolumeSewRG1, TrackedVolumeSewRG2, 
                TrackedVolumeSewTotal, 
                TrackedVolumeSewRD1_act, TrackedVolumeSewRD2_act, 
                TrackedVolumeSewRG1_act, TrackedVolumeSewRG2_act, 
                TrackedVolumeSewTotal_act,
                run_WWin, TrackedVolumeWW, TrackedVolumeWW_act;
        TrackedVolumeSewRD1 = TrackedVolumeSewRD2 = TrackedVolumeSewRG1 =
                TrackedVolumeSewRG2 = TrackedVolumeSewTotal =
                TrackedVolumeSewRD1_act = TrackedVolumeSewRD2_act = 
                TrackedVolumeSewRG1_act = TrackedVolumeSewRG2_act =  
                TrackedVolumeSewTotal_act = 
                run_WWin = TrackedVolumeWW = TrackedVolumeWW_act = 0.0;
        if(run_SewTrack){
            // read tracked volumes of the current reach --> incoming
            TrackedVolumeSewRD1 = trackedVolumeSewRD1.getValue();
            TrackedVolumeSewRD2 = trackedVolumeSewRD2.getValue();
            TrackedVolumeSewRG1 = trackedVolumeSewRG1.getValue();
            TrackedVolumeSewRG2 = trackedVolumeSewRG2.getValue();
            TrackedVolumeSewTotal = trackedVolumeSewTotal.getValue();
            // available
            TrackedVolumeSewRD1_act = trackedVolumeSewRD1_act.getValue();
            TrackedVolumeSewRD2_act = trackedVolumeSewRD2_act.getValue();
            TrackedVolumeSewRG1_act = trackedVolumeSewRG1_act.getValue();
            TrackedVolumeSewRG2_act = trackedVolumeSewRG2_act.getValue();
            TrackedVolumeSewTotal_act = trackedVolumeSewTotal_act.getValue();
        }
        
        if(run_WWTrack){
            // read tracked volumes of the current reach --> incoming
            run_WWin = InWW.getValue();
            // available
            TrackedVolumeWW_act = trackedVolumeWW_act.getValue();
        }
        
        // Array of reach names
        double[] Nom = this.names.getValue();
        
        // Liste des indices de Reachs contribuant au brin actuel
        List<Integer> listIndex = new ArrayList<Integer>();
        for(int i = 0; i < Nom.length; i++){
            if(ArrayTrackedVolumeTotal[i] != -999){
                listIndex.add(i);
            }
        }
        
        // define internal variables
        double run_inRD1 = inRD1.getValue();
        double run_inRD2 = inRD2.getValue();
        double run_inRG1 = inRG1.getValue();
        double run_inRG2 = inRG2.getValue();
        double run_actRD1 = actRD1.getValue();
        double run_actRD2 = actRD2.getValue();
        double run_actRG1 = actRG1.getValue();
        double run_actRG2 = actRG2.getValue();
        double run_allowedIOExtractionFraction = this.allowedIOExtractionFraction.getValue();

        // calculate water available for extraction
        double run_totalIn = run_inRD1 + run_inRD2 + run_inRG1 + run_inRG2; // all water in inflow (for proportional extraction)
        double run_totalAct = run_actRD1 + run_actRD2 + run_actRG1 + run_actRG2; // all water in act (for proportional extraction)
        double run_totalStorage = run_totalIn + run_totalAct; // all water in inflow and act
        double run_inAvailable = run_allowedIOExtractionFraction * run_totalIn; // water inflow available for irrigation
        double run_actAvailable = run_allowedIOExtractionFraction * run_totalAct; // water in the reach available for irrigation
        double run_totalAvailable = run_inAvailable + run_actAvailable; // all available water
        
        // define variables for storing extracted / added volumes
        double run_IOExtractedR; // local variable to store actually extracted volume
        double run_IOaddedR;
        
        double run_totalIODemand = 0;
        double run_volume = VolumeIO.getValue();
        // check if extraction (negative Volume) or injection (positive Volume) or none (Volume = 0)
        if ((run_totalAvailable != 0.0) & (run_volume<0)) { // if there is water available and water should be extracted, extract water from the reach 
            
            run_totalIODemand = run_volume * (-1);
            this.totalIODemand.setValue(run_totalIODemand);
            
            double run_availableDemandFraction = run_totalIODemand / run_totalAvailable;// fraction of available water that is demanded for extraction
            
            if (run_availableDemandFraction <=1){ // demand can be satisfied with available water from inflow and act
                double run_storageDemandFraction = run_totalIODemand / run_totalStorage;// fraction of all stored water that is demanded for extraction
                run_IOExtractedR = run_totalIODemand; // we can satisfy the demand (extract everything that is needed)
                
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
                for(int i : listIndex){ // per upstream reach
                    // tracked inflow
                    ArrayTrackedVolumeRD1[i] = ArrayTrackedVolumeRD1[i] * (1 - run_storageDemandFraction);
                    ArrayTrackedVolumeRD2[i] = ArrayTrackedVolumeRD2[i] * (1 - run_storageDemandFraction);
                    ArrayTrackedVolumeRG1[i] = ArrayTrackedVolumeRG1[i] * (1 - run_storageDemandFraction);
                    ArrayTrackedVolumeRG2[i] = ArrayTrackedVolumeRG2[i] * (1 - run_storageDemandFraction);
                    ArrayTrackedVolumeTotal[i] = ArrayTrackedVolumeTotal[i] * (1 - run_storageDemandFraction);
                    
                    // tracked channel storage
                    ArrayTrackedVolume_actRD1[i] = ArrayTrackedVolume_actRD1[i] * (1 - run_storageDemandFraction);
                    ArrayTrackedVolume_actRD2[i] = ArrayTrackedVolume_actRD2[i] * (1 - run_storageDemandFraction);
                    ArrayTrackedVolume_actRG1[i] = ArrayTrackedVolume_actRG1[i] * (1 - run_storageDemandFraction);
                    ArrayTrackedVolume_actRG2[i] = ArrayTrackedVolume_actRG2[i] * (1 - run_storageDemandFraction);
                    ArrayTrackedVolume_actTotal[i] = ArrayTrackedVolume_actTotal[i] * (1 - run_storageDemandFraction);
                }
                if(run_SewTrack){ // from sewer
                    // in inflow
                    TrackedVolumeSewRD1 = TrackedVolumeSewRD1 * (1 - run_storageDemandFraction);
                    TrackedVolumeSewRD2 = TrackedVolumeSewRD2 * (1 - run_storageDemandFraction);
                    TrackedVolumeSewRG1 = TrackedVolumeSewRG1 * (1 - run_storageDemandFraction);
                    TrackedVolumeSewRG2 = TrackedVolumeSewRG2 * (1 - run_storageDemandFraction);
                    TrackedVolumeSewTotal = TrackedVolumeSewTotal * (1 - run_storageDemandFraction);
                    
                    // in channel storage
                    TrackedVolumeSewRD1_act = TrackedVolumeSewRD1_act * (1 - run_storageDemandFraction);
                    TrackedVolumeSewRD2_act = TrackedVolumeSewRD2_act * (1 - run_storageDemandFraction);
                    TrackedVolumeSewRG1_act = TrackedVolumeSewRG1_act * (1 - run_storageDemandFraction);
                    TrackedVolumeSewRG2_act = TrackedVolumeSewRG2_act * (1 - run_storageDemandFraction);
                    TrackedVolumeSewTotal_act = TrackedVolumeSewTotal_act * (1 - run_storageDemandFraction);
                }
                if(run_WWTrack){ // from WWTP
                    // in inflow
                    run_WWin = run_WWin * (1 - run_storageDemandFraction);
                    
                    // in channel storage
                    TrackedVolumeWW_act = TrackedVolumeWW_act * (1 - run_storageDemandFraction);
                }
                
            } else { // not all of the demand can be satisfied from available water. Only available (and allowed) water will be extracted
                run_IOExtractedR = run_totalAvailable; // we extract all available water
                
                // extract proportionally from inflow (allowed fraction)
                inRD1.setValue(run_inRD1 * (1 - run_allowedIOExtractionFraction));
                inRD2.setValue(run_inRD2 * (1 - run_allowedIOExtractionFraction));
                inRG1.setValue(run_inRG1 * (1 - run_allowedIOExtractionFraction));
                inRG2.setValue(run_inRG2 * (1 - run_allowedIOExtractionFraction));
                // extract proportionally from act (allowed fraction)
                actRD1.setValue(run_actRD1 * (1 - run_allowedIOExtractionFraction));
                actRD2.setValue(run_actRD2 * (1 - run_allowedIOExtractionFraction));
                actRG1.setValue(run_actRG1 * (1 - run_allowedIOExtractionFraction));
                actRG2.setValue(run_actRG2 * (1 - run_allowedIOExtractionFraction));
                
                
                // reduce tracked volumes proportionally
                for(int i : listIndex){ // per upstream reach
                    // tracked inflow
                    ArrayTrackedVolumeRD1[i] = ArrayTrackedVolumeRD1[i] * (1 - run_allowedIOExtractionFraction);
                    ArrayTrackedVolumeRD2[i] = ArrayTrackedVolumeRD2[i] * (1 - run_allowedIOExtractionFraction);
                    ArrayTrackedVolumeRG1[i] = ArrayTrackedVolumeRG1[i] * (1 - run_allowedIOExtractionFraction);
                    ArrayTrackedVolumeRG2[i] = ArrayTrackedVolumeRG2[i] * (1 - run_allowedIOExtractionFraction);
                    ArrayTrackedVolumeTotal[i] = ArrayTrackedVolumeTotal[i] * (1 - run_allowedIOExtractionFraction);
                    
                    // tracked channel storage
                    ArrayTrackedVolume_actRD1[i] = ArrayTrackedVolume_actRD1[i] * (1 - run_allowedIOExtractionFraction);
                    ArrayTrackedVolume_actRD2[i] = ArrayTrackedVolume_actRD2[i] * (1 - run_allowedIOExtractionFraction);
                    ArrayTrackedVolume_actRG1[i] = ArrayTrackedVolume_actRG1[i] * (1 - run_allowedIOExtractionFraction);
                    ArrayTrackedVolume_actRG2[i] = ArrayTrackedVolume_actRG2[i] * (1 - run_allowedIOExtractionFraction);
                    ArrayTrackedVolume_actTotal[i] = ArrayTrackedVolume_actTotal[i] * (1 - run_allowedIOExtractionFraction);
                }
                if(run_SewTrack){ // from sewer
                    // in inflow
                    TrackedVolumeSewRD1 = TrackedVolumeSewRD1 * (1 - run_allowedIOExtractionFraction);
                    TrackedVolumeSewRD2 = TrackedVolumeSewRD2 * (1 - run_allowedIOExtractionFraction);
                    TrackedVolumeSewRG1 = TrackedVolumeSewRG1 * (1 - run_allowedIOExtractionFraction);
                    TrackedVolumeSewRG2 = TrackedVolumeSewRG2 * (1 - run_allowedIOExtractionFraction);
                    TrackedVolumeSewTotal = TrackedVolumeSewTotal * (1 - run_allowedIOExtractionFraction);
                    
                    // in channel storage
                    TrackedVolumeSewRD1_act = TrackedVolumeSewRD1_act * (1 - run_allowedIOExtractionFraction);
                    TrackedVolumeSewRD2_act = TrackedVolumeSewRD2_act * (1 - run_allowedIOExtractionFraction);
                    TrackedVolumeSewRG1_act = TrackedVolumeSewRG1_act * (1 - run_allowedIOExtractionFraction);
                    TrackedVolumeSewRG2_act = TrackedVolumeSewRG2_act * (1 - run_allowedIOExtractionFraction);
                    TrackedVolumeSewTotal_act = TrackedVolumeSewTotal_act * (1 - run_allowedIOExtractionFraction);
                }
                if(run_WWTrack){ // from WWTP
                    // in inflow
                    run_WWin = run_WWin * (1 - run_allowedIOExtractionFraction);
                    
                    // in channel storage
                    TrackedVolumeWW_act = TrackedVolumeWW_act * (1 - run_allowedIOExtractionFraction);
                }
                
            }
            
            run_IOaddedR = 0.; // if water is extracted --> no water is added
            
            // read tracked volumes of the current reach --> incoming
            trackedVolumeRD1Array.setValue(ArrayTrackedVolumeRD1);
            trackedVolumeRD2Array.setValue(ArrayTrackedVolumeRD2);
            trackedVolumeRG1Array.setValue(ArrayTrackedVolumeRG1);
            trackedVolumeRG2Array.setValue(ArrayTrackedVolumeRG2);
            trackedVolumeTotalArray.setValue(ArrayTrackedVolumeTotal);
            // available
            trackedVolumeRD1_actArray.setValue(ArrayTrackedVolume_actRD1);
            trackedVolumeRD2_actArray.setValue(ArrayTrackedVolume_actRD2);
            trackedVolumeRG1_actArray.setValue(ArrayTrackedVolume_actRG1);
            trackedVolumeRG2_actArray.setValue(ArrayTrackedVolume_actRG2);
            trackedVolumeTotal_actArray.setValue(ArrayTrackedVolume_actTotal);
            if(run_SewTrack){
                // read tracked volumes of the current reach --> incoming
                trackedVolumeSewRD1.setValue(TrackedVolumeSewRD1);
                trackedVolumeSewRD2.setValue(TrackedVolumeSewRD2);
                trackedVolumeSewRG1.setValue(TrackedVolumeSewRG1);
                trackedVolumeSewRG2.setValue(TrackedVolumeSewRG2);
                trackedVolumeSewTotal.setValue(TrackedVolumeSewTotal);
                // available
                trackedVolumeSewRD1_act.setValue(TrackedVolumeSewRD1_act);
                trackedVolumeSewRD2_act.setValue(TrackedVolumeSewRD2_act);
                trackedVolumeSewRG1_act.setValue(TrackedVolumeSewRG1_act);
                trackedVolumeSewRG2_act.setValue(TrackedVolumeSewRG2_act);
                trackedVolumeSewTotal_act.setValue(TrackedVolumeSewTotal_act);
            }
            if(run_WWTrack){
                // read tracked volumes of the current reach --> incoming
                trackedVolumeWW_act.setValue(TrackedVolumeWW_act);
                // available
                InWW.setValue(run_WWin);
            }
        } else if (run_volume > 0) { // water injected
            double run_AddRD1, run_AddRD2, run_AddRG1, run_AddRG2;
            run_AddRD1 = run_AddRD2 = run_AddRG1 = run_AddRG2 = 0.0;
            switch (targetComp.getValue()) {
                case "distr":
                    // distribute incoming water over the four compounds, 
                    // - if any incoming water: distribute proportionally to what is incomping
                    // - else if any water in reach: distribute proportionally to what is in reach
                    // - else: distribute equally
                    if (run_totalIn > 0) { // if any incoming water: distribute proportionally to what is incoming
                        run_AddRD1 = run_volume * run_inRD1 / run_totalIn;
                        run_AddRD2 = run_volume * run_inRD2 / run_totalIn;
                        run_AddRG1 = run_volume * run_inRG1 / run_totalIn;
                        run_AddRG2 = run_volume * run_inRG2 / run_totalIn;

                    } else if (run_totalAct > 0) { // no incoming water, but water in reach: distribute proportionally to what is in reach
                        run_AddRD1 = run_volume * run_actRD1 / run_totalAct;
                        run_AddRD2 = run_volume * run_actRD2 / run_totalAct;
                        run_AddRG1 = run_volume * run_actRG1 / run_totalAct;
                        run_AddRG2 = run_volume * run_actRG2 / run_totalAct;

                    } else { // nothing coming in, nothing in stock: distribute equally
                        run_AddRD1 = run_volume * 0.25;
                        run_AddRD2 = run_volume * 0.25;
                        run_AddRG1 = run_volume * 0.25;
                        run_AddRG2 = run_volume * 0.25;
                    }
                    break;
                case "RD1":
                    run_AddRD1 = run_volume;
                    break;
                case "RD2":
                    run_AddRD2 = run_volume;
                    break;
                case "RG1":
                    run_AddRG1 = run_volume;
                    break;
                case "RG2":
                    run_AddRG2 = run_volume;
                    break;
                default:
                    throw new IllegalArgumentException(targetComp.getValue() + " is not a valid target component. if there is a water input, targetComp needs to be in (RD1, RD2, RG1, RG2, distr)");
            }
            if (run_WWTrack){
                run_WWin = run_WWin + run_volume;
                InWW.setValue(run_WWin);
            }
            
            inRD1.setValue(run_inRD1 + run_AddRD1);
            inRD2.setValue(run_inRD2 + run_AddRD2);
            inRG1.setValue(run_inRG1 + run_AddRG1);
            inRG2.setValue(run_inRG2 + run_AddRG2);
            run_IOaddedR = run_AddRD1 + run_AddRD2 + run_AddRG1 + run_AddRG2;
//            getModel().getRuntime().println("++ WW added "+run_IOaddedR);
            
            run_IOExtractedR = 0.; // if water is added, no water is extracted
            
        } else { // neither extraction nor injection (volume = 0), or extraction demanded but nothing available to extract
            run_IOExtractedR = 0.;
            run_IOaddedR = 0.;
        }
        this.IOExtractedR.setValue(run_IOExtractedR);
        this.IOaddedR.setValue(run_IOaddedR);
        
        this.IOaddedAll.setValue(this.IOaddedAll.getValue() + run_IOaddedR);
        // extracted volume for all animals (cumulative over reaches)
        this.IOExtractedAll.setValue(this.IOExtractedAll.getValue() + run_IOExtractedR);
    }
       
    
}