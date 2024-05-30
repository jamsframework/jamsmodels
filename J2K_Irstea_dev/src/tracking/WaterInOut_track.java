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
        version = "1.0_0"
)
@VersionComments(entries = {
    @VersionComments.Entry(version = "1.0_0", comment = "Initial version")
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
    public Attribute.Double Volume;
    
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

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
            description = "Ratio of water available for extraction / water present in the reach (actR..)",
            defaultValue = "1.0"
    )
    public Attribute.Double actPrel;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "Total demand of water. For verification purposes"
    )
    public Attribute.Double totalDemand;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "Total input of water. For verification purposes"
    )
    public Attribute.Double totalInput;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "volume extracted, cummulative -> all reaches"
    )
    public Attribute.Double ExtractedAll;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "volume extracted from current reach"
    )
    public Attribute.Double ExtractedR;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "volume added, cummulative -> all reaches"
    )
    public Attribute.Double addedAll;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "volume added to current reach"
    )
    public Attribute.Double addedR;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "Total water in the reach available for irrigation"
    )
    public Attribute.Double totalAvail;
    
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
        access = JAMSVarDescription.AccessType.READ,
        description = "Are there tracked sewers in the model? (1/0)"
        )
    public Attribute.Double trackSewers;
            
    @JAMSVarDescription(
        access = JAMSVarDescription.AccessType.READ,
        description = "Is there a tracked waste water treatment plant in the model? (1/0)"
        )
    public Attribute.Double trackWW;

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
            description = "Tracked volume from waste water treatment plant in reach",
            unit = "L"
    )
    public Attribute.Double trackedVolumeWW;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Remaining tracked volume from waste water treatment plant",
            unit = "L"
    )
    public Attribute.Double trackedVolumeWW_act;
    

    /*
     *  Component run stages
     */

    @Override
    public void run() {

        Attribute.Entity currentReach = reaches.getCurrent();
        
        boolean SewTrack = trackSewers.getValue() == 1;
        boolean WWTrack = trackWW.getValue() == 1;
        
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
                TrackedVolumeWW, TrackedVolumeWW_act;
        TrackedVolumeSewRD1 = TrackedVolumeSewRD2 = TrackedVolumeSewRG1 =
                TrackedVolumeSewRG2 = TrackedVolumeSewTotal =
                TrackedVolumeSewRD1_act = TrackedVolumeSewRD2_act = 
                TrackedVolumeSewRG1_act = TrackedVolumeSewRG2_act =  
                TrackedVolumeSewTotal_act = 
                TrackedVolumeWW = TrackedVolumeWW_act = 0.0;
        if(SewTrack){
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
        
        if(WWTrack){
            // read tracked volumes of the current reach --> incoming
            TrackedVolumeWW = trackedVolumeWW.getValue();
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
        
        
        // calculate water available for extraction
        double totalIn = inRD1.getValue() + inRD2.getValue() + inRG1.getValue() + inRG2.getValue();
        double totalAct = this.actPrel.getValue() * (actRD1.getValue() + actRD2.getValue() + actRG1.getValue() + actRG2.getValue()); // water in the reach that is available for animal needs
        double totalAv = totalIn + totalAct; // all available water for extraction 
        this.totalAvail.setValue(totalIn + totalAct); // all available water for extraction
        
        double totalDemand = 0;
        double volume = Volume.getValue();
        // check if extraction (negative Volume) or injection (positive Volume) or none (Volume = 0)
        if (volume<0) { // extract water from the reach 
            
            totalDemand = volume * (-1);
            this.totalDemand.setValue(totalDemand);

            //calculate proportion of total water that is needed
            if (totalAv != 0.0){ // if there is water available
                if (totalIn != 0){ // if there is water coming into reach

                    double frac = totalDemand /totalIn;

                    if (frac <= 1) {

                        //we can cover all only with input to the reach, reduce the components accordingly
                        inRD1.setValue(inRD1.getValue() * (1 - frac));
                        inRD2.setValue(inRD2.getValue() * (1 - frac));
                        inRG1.setValue(inRG1.getValue() * (1 - frac));
                        inRG2.setValue(inRG2.getValue() * (1 - frac));
                        ExtractedR.setValue(totalDemand);
                    
                        // reduce tracked volumes proportionally
                        for(int i : listIndex){
                            ArrayTrackedVolumeRD1[i] = ArrayTrackedVolumeRD1[i] * (1 - frac);
                            ArrayTrackedVolumeRD2[i] = ArrayTrackedVolumeRD2[i] * (1 - frac);
                            ArrayTrackedVolumeRG1[i] = ArrayTrackedVolumeRG1[i] * (1 - frac);
                            ArrayTrackedVolumeRG2[i] = ArrayTrackedVolumeRG2[i] * (1 - frac);
                            ArrayTrackedVolumeTotal[i] = ArrayTrackedVolumeTotal[i] * (1 - frac);
                        }
                        if(SewTrack){
                            // reduce tracked volumes from sewer proportionally
                            TrackedVolumeSewRD1 = TrackedVolumeSewRD1 * (1 - frac);
                            TrackedVolumeSewRD2 = TrackedVolumeSewRD2 * (1 - frac);
                            TrackedVolumeSewRG1 = TrackedVolumeSewRG1 * (1 - frac);
                            TrackedVolumeSewRG2 = TrackedVolumeSewRG2 * (1 - frac);
                            TrackedVolumeSewTotal = TrackedVolumeSewTotal * (1 - frac);
                        }
                        if(WWTrack){
                            // reduce tracked volumes from WWTP proportionally
                            TrackedVolumeWW = TrackedVolumeWW * (1 - frac);
                        }

                    } else {
                        //looking if we can cover the demand by including usable part of act...
                        frac = totalDemand / (totalIn+totalAct);

                        //we can cover only part of the demand with input, reduce the components to 0
                        inRD1.setValue(0);
                        inRD2.setValue(0);
                        inRG1.setValue(0);
                        inRG2.setValue(0);
                    
                        // set incoming tracked volumes to 0
                        for(int i : listIndex){
                            ArrayTrackedVolumeRD1[i] = 0;
                            ArrayTrackedVolumeRD2[i] = 0;
                            ArrayTrackedVolumeRG1[i] = 0;
                            ArrayTrackedVolumeRG2[i] = 0;
                            ArrayTrackedVolumeTotal[i] = 0;
                        }
                        if(SewTrack){
                            // set incoming tracked sewer volumes to 0
                            TrackedVolumeSewRD1 = 0;
                            TrackedVolumeSewRD2 = 0;
                            TrackedVolumeSewRG1 = 0;
                            TrackedVolumeSewRG2 = 0;
                            TrackedVolumeSewTotal = 0;
                        }
                        if(WWTrack){
                            // set incoming tracked WW volumes to 0
                            TrackedVolumeWW = 0;
                        }

                        if (frac <= 1) {
                            //we can cover all of the demand with input and act together, reduce the components accordingly
                            double actDemand = 0;
                            actDemand = totalDemand - totalIn;
                            double frac2 = actDemand/totalAct;
                            actRD1.setValue(actRD1.getValue() * (1 - frac2));
                            actRD2.setValue(actRD2.getValue() * (1 - frac2));
                            actRG1.setValue(actRG1.getValue() * (1 - frac2));
                            actRG2.setValue(actRG2.getValue() * (1 - frac2));
                            ExtractedR.setValue(totalDemand);

                            // reduce tracked stocked volumes proportionally
                            for(int i : listIndex){
                                ArrayTrackedVolume_actRD1[i] = ArrayTrackedVolume_actRD1[i] * (1 - frac2);
                                ArrayTrackedVolume_actRD2[i] = ArrayTrackedVolume_actRD2[i] * (1 - frac2);
                                ArrayTrackedVolume_actRG1[i] = ArrayTrackedVolume_actRG1[i] * (1 - frac2);
                                ArrayTrackedVolume_actRG2[i] = ArrayTrackedVolume_actRG2[i] * (1 - frac2);
                                ArrayTrackedVolume_actTotal[i] = ArrayTrackedVolume_actTotal[i] * (1 - frac2);
                            }
                            if(SewTrack){
                                // reduce tracked stocked volumes from sewer proportionally
                                TrackedVolumeSewRD1_act = TrackedVolumeSewRD1_act * (1 - frac2);
                                TrackedVolumeSewRD2_act = TrackedVolumeSewRD2_act * (1 - frac2);
                                TrackedVolumeSewRG1_act = TrackedVolumeSewRG1_act * (1 - frac2);
                                TrackedVolumeSewRG2_act = TrackedVolumeSewRG2_act * (1 - frac2);
                                TrackedVolumeSewTotal_act = TrackedVolumeSewTotal_act * (1 - frac2);
                            }
                            if(WWTrack){
                                // reduce tracked stocked volumes from WWTP proportionally
                                TrackedVolumeWW_act = TrackedVolumeWW_act * (1 - frac2);
                            }

                        } else {
                            // we can only cover part of the demand ; reduce the act... to (1 - actPrel)*act...
                            actRD1.setValue(actRD1.getValue() * (1 - actPrel.getValue()));
                            actRD2.setValue(actRD2.getValue() * (1 - actPrel.getValue()));
                            actRG1.setValue(actRG1.getValue() * (1 - actPrel.getValue()));
                            actRG2.setValue(actRG2.getValue() * (1 - actPrel.getValue()));
                            ExtractedR.setValue(totalIn+totalAct);

                            // reduce tracked volumes to zero (except leaving minimum flow)
                            for(int i : listIndex){
                                ArrayTrackedVolume_actRD1[i] = ArrayTrackedVolume_actRD1[i] * (1 - actPrel.getValue());
                                ArrayTrackedVolume_actRD2[i] = ArrayTrackedVolume_actRD2[i] * (1 - actPrel.getValue());
                                ArrayTrackedVolume_actRG1[i] = ArrayTrackedVolume_actRG1[i] * (1 - actPrel.getValue());
                                ArrayTrackedVolume_actRG2[i] = ArrayTrackedVolume_actRG2[i] * (1 - actPrel.getValue());
                                ArrayTrackedVolume_actTotal[i] = ArrayTrackedVolume_actTotal[i] * (1 - actPrel.getValue());
                            }
                            if(SewTrack){
                                // reduce tracked stocked volumes from sewer to zero (except leaving minimum flow)
                                TrackedVolumeSewRD1_act = TrackedVolumeSewRD1_act * (1 - actPrel.getValue());
                                TrackedVolumeSewRD2_act = TrackedVolumeSewRD2_act * (1 - actPrel.getValue());
                                TrackedVolumeSewRG1_act = TrackedVolumeSewRG1_act * (1 - actPrel.getValue());
                                TrackedVolumeSewRG2_act = TrackedVolumeSewRG2_act * (1 - actPrel.getValue());
                                TrackedVolumeSewTotal_act = TrackedVolumeSewTotal_act * (1 - actPrel.getValue());
                            }
                            if(WWTrack){
                                // reduce tracked stocked volumes from WWTP to zero (except leaving minimum flow)
                                TrackedVolumeWW_act = TrackedVolumeWW_act * (1 - actPrel.getValue());
                            }
                        }
                    }

                } else { // if no water coming into reach, but there is water in the reach act
                    //looking if we can cover the demand by including usable part of act...
                    double frac = totalDemand / (totalAct);
                    if (frac <= 1) {
                        //we can cover all of the demand with act, reduce the components accordingly
                        actRD1.setValue(actRD1.getValue() * (1 - frac));
                        actRD2.setValue(actRD2.getValue() * (1 - frac));
                        actRG1.setValue(actRG1.getValue() * (1 - frac));
                        actRG2.setValue(actRG2.getValue() * (1 - frac));
                        ExtractedR.setValue(totalDemand);
                        
                        // reduce tracked volumes proportionally
                        for(int i : listIndex){
                            ArrayTrackedVolume_actRD1[i] = ArrayTrackedVolume_actRD1[i] * (1 - frac);
                            ArrayTrackedVolume_actRD2[i] = ArrayTrackedVolume_actRD2[i] * (1 - frac);
                            ArrayTrackedVolume_actRG1[i] = ArrayTrackedVolume_actRG1[i] * (1 - frac);
                            ArrayTrackedVolume_actRG2[i] = ArrayTrackedVolume_actRG2[i] * (1 - frac);
                            ArrayTrackedVolume_actTotal[i] = ArrayTrackedVolume_actTotal[i] * (1 - frac);
                        }
                        if(SewTrack){
                            // reduce tracked stocked volumes from sewer proportionally
                            TrackedVolumeSewRD1_act = TrackedVolumeSewRD1_act * (1 - frac);
                            TrackedVolumeSewRD2_act = TrackedVolumeSewRD2_act * (1 - frac);
                            TrackedVolumeSewRG1_act = TrackedVolumeSewRG1_act * (1 - frac);
                            TrackedVolumeSewRG2_act = TrackedVolumeSewRG2_act * (1 - frac);
                            TrackedVolumeSewTotal_act = TrackedVolumeSewTotal_act * (1 - frac);
                        }
                        if(WWTrack){
                            // reduce tracked stocked volumes from WWTP proportionally
                            TrackedVolumeWW_act = TrackedVolumeWW_act * (1 - frac);
                        }

                    } else {
                        // we can only cover part of the demand ; reduce the act... to (1 - actPrel)*act...
                        actRD1.setValue(actRD1.getValue() * (1 - actPrel.getValue()));
                        actRD2.setValue(actRD2.getValue() * (1 - actPrel.getValue()));
                        actRG1.setValue(actRG1.getValue() * (1 - actPrel.getValue()));
                        actRG2.setValue(actRG2.getValue() * (1 - actPrel.getValue()));
                        ExtractedR.setValue(totalAct);

                        // reduce tracked volumes to zero (except leaving minimum flow)
                        for(int i : listIndex){
                            ArrayTrackedVolume_actRD1[i] = ArrayTrackedVolume_actRD1[i] * (1 - actPrel.getValue());
                            ArrayTrackedVolume_actRD2[i] = ArrayTrackedVolume_actRD2[i] * (1 - actPrel.getValue());
                            ArrayTrackedVolume_actRG1[i] = ArrayTrackedVolume_actRG1[i] * (1 - actPrel.getValue());
                            ArrayTrackedVolume_actRG2[i] = ArrayTrackedVolume_actRG2[i] * (1 - actPrel.getValue());
                            ArrayTrackedVolume_actTotal[i] = ArrayTrackedVolume_actTotal[i] * (1 - actPrel.getValue());
                        }
                        if(SewTrack){
                            // reduce tracked stocked volumes from sewer to zero (except leaving minimum flow)
                            TrackedVolumeSewRD1_act = TrackedVolumeSewRD1_act * (1 - actPrel.getValue());
                            TrackedVolumeSewRD2_act = TrackedVolumeSewRD2_act * (1 - actPrel.getValue());
                            TrackedVolumeSewRG1_act = TrackedVolumeSewRG1_act * (1 - actPrel.getValue());
                            TrackedVolumeSewRG2_act = TrackedVolumeSewRG2_act * (1 - actPrel.getValue());
                            TrackedVolumeSewTotal_act = TrackedVolumeSewTotal_act * (1 - actPrel.getValue());
                        }
                        if(WWTrack){
                            // reduce tracked stocked volumes from WWTP proportionally
                            TrackedVolumeWW_act = TrackedVolumeWW_act * (1 - actPrel.getValue());
                        }
                    }
                }
            } else { 
                this.ExtractedR.setValue(0.);
            }
        
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
            if(SewTrack){
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
            if(WWTrack){
                // read tracked volumes of the current reach --> incoming
                trackedVolumeWW_act.setValue(TrackedVolumeWW_act);
                // available
                trackedVolumeWW.setValue(TrackedVolumeWW);
            }
            // if water is extracted --> no water is added
            this.addedR.setValue(0.);
        } else if (volume > 0) { // water injected
            double AddRD1, AddRD2, AddRG1, AddRG2;
            AddRD1 = AddRD2 = AddRG1 = AddRG2 = 0.0;
            if (targetComp.getValue() == "distr"){
                // distribute incoming water over the four compounds, 
                // - if any incoming water: distribute proportionally to what is incomping
                // - else if any water in reach: distribute proportionally to what is in reach
                // - else: distribute equally
                if (totalIn > 0) { // if any incoming water: distribute proportionally to what is incoming
                    AddRD1 = volume * inRD1.getValue() / totalIn;
                    AddRD2 = volume * inRD2.getValue() / totalIn;
                    AddRG1 = volume * inRG1.getValue() / totalIn;
                    AddRG2 = volume * inRG2.getValue() / totalIn;
                    
                } else if (totalAct > 0) { // no incoming water, but water in reach: distribute proportionally to what is in reach
                    AddRD1 = volume * actRD1.getValue() / totalAct;
                    AddRD2 = volume * actRD2.getValue() / totalAct;
                    AddRG1 = volume * actRG1.getValue() / totalAct;
                    AddRG2 = volume * actRG2.getValue() / totalAct;
                    
                } else { // nothing coming in, nothing in stock: distribute equally
                    AddRD1 = volume * 0.25;
                    AddRD2 = volume * 0.25;
                    AddRG1 = volume * 0.25;
                    AddRG2 = volume * 0.25;
                }
            } else {
                switch (targetComp.getValue()) {
                    case "RD1":
                        AddRD1 = volume;
                        break;
                    case "RD2":
                        AddRD2 = volume;
                        break;
                    case "RG1":
                        AddRG1 = volume;
                        break;
                    case "RG2":
                        AddRG2 = volume;
                        break;
                    default:
                        throw new IllegalArgumentException(targetComp.getValue() + " is not a valid target component. if there is a water input, targetComp needs to be in (RD1, RD2, RG1, RG2, distr)");
                }
            }
            if (WWTrack){
                TrackedVolumeWW = TrackedVolumeWW + volume;
                trackedVolumeWW.setValue(TrackedVolumeWW);
            }
            
            inRD1.setValue(inRD1.getValue() + AddRD1);
            inRD2.setValue(inRD2.getValue() + AddRD2);
            inRG1.setValue(inRG1.getValue() + AddRG1);
            inRG2.setValue(inRG2.getValue() + AddRG2);
            
        } else { // neither extraction nor injection (volume = 0)
            this.ExtractedR.setValue(0.);
            this.addedR.setValue(0.);
        }
        this.addedAll.setValue(this.addedAll.getValue() + this.addedR.getValue());
        // extracted volume for all animals (cumulative over reaches)
        this.ExtractedAll.setValue(this.ExtractedAll.getValue() + this.ExtractedR.getValue());
    }
       
    
}
