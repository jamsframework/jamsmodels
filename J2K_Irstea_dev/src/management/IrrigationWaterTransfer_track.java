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
            description = "RD1 component in reach"
    )
    public Attribute.Double inRD1;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "RD2 component in reach"
    )
    public Attribute.Double inRD2;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "RG1 component in reach"
    )
    public Attribute.Double inRG1;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "RG2 component in reach"
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
            description = "Name of list of irrigated HRUs in reach entities",
            defaultValue = "irrigationEntities"
    )
    public Attribute.String irrigationEntitiesListName;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Name of attribute that stores irrigation demand of an HRU - plant water requirement / efficiency",
            defaultValue = "irrigationDemand"
    )
    public Attribute.String irrigationDemandName;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Name of attribute that stores water requirements of an HRU - the real plant requirements",
            defaultValue = "waterRequirements"
    )
    public Attribute.String waterRequirementsName;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Name of attribute that stores the irrigation water delivered HRU (totalTransfer minus losses due to efficiency)",
            defaultValue = "irrigationWater"
    )
    public Attribute.String irrigationWaterName;

            @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Ratio of water available for irrigation / water present in the reach (actR..)"
    )
    public Attribute.Double actPrel;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "Total demand of water for irrigation, including the enhancement by poor efficiency"
    )
    public Attribute.Double totalDemand;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "Total irrigation transfer (= prelemenents, enhanced by poor efficiency)"
    )
    public Attribute.Double totalTransfer;
    
        @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "Total input in the reach"
    )
    public Attribute.Double totalInput;
    
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

        //check if this reach even has irrigated HRUs in its catchment
        if (!currentReach.existsAttribute(irrigationEntitiesListName.getValue())) {
            double totalIn = inRD1.getValue() + inRD2.getValue() + inRG1.getValue() + inRG2.getValue();
            double totalAct = this.actPrel.getValue() * (actRD1.getValue() + actRD2.getValue() + actRG1.getValue() + actRG2.getValue());
            this.totalInput.setValue(totalIn + totalAct); // IG : ACHTUNG, cette variable n'est pas à jour !!
            return;
        }
        double totalIn = inRD1.getValue() + inRD2.getValue() + inRG1.getValue() + inRG2.getValue();
        double totalAct = this.actPrel.getValue() * (actRD1.getValue() + actRD2.getValue() + actRG1.getValue() + actRG2.getValue()); // eau du reach dispo pour l'irrigation.
        double totalAv = totalIn + totalAct; // all available water
        this.totalInput.setValue(totalIn + totalAct); // eau disponible pour l'irrigation à ce pas de temps
        //this.totalInput.setValue(totalIn);
        double totalDemand = 0;

        List<Attribute.Entity> l = (List) currentReach.getObject(irrigationEntitiesListName.getValue());
        for (Attribute.Entity hru : l) {
            double demand = hru.getDouble(irrigationDemandName.getValue());
            totalDemand += demand;
        }

        this.totalDemand.setValue(totalDemand);

        //calculate proportion of total water that is needed
        if (totalAv != 0){ // if there is water avilable (in and/or act)
            if (totalIn != 0){ // if there is water coming in

                double frac = totalDemand /totalIn;

                if (frac <= 1) {

                    //we can cover all only with in to the reach, reduce the components accordingly
                    inRD1.setValue(inRD1.getValue() * (1 - frac));
                    inRD2.setValue(inRD2.getValue() * (1 - frac));
                    inRG1.setValue(inRG1.getValue() * (1 - frac));
                    inRG2.setValue(inRG2.getValue() * (1 - frac));
                    totalTransfer.setValue(totalDemand);

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
                    //looking if we can cover the demand by including part of act...
                    frac = totalDemand / (totalIn+totalAct);

                    //we can cover only part of the demand with in, reduce the components to 0
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
                        //we can cover all of the demand but not only with in..., reduce the components accordingly
                        double actDemand = 0;
                        actDemand = totalDemand - totalIn;
                        double frac2 = actDemand/totalAct;
                        actRD1.setValue(actRD1.getValue() * (1 - frac2));
                        actRD2.setValue(actRD2.getValue() * (1 - frac2));
                        actRG1.setValue(actRG1.getValue() * (1 - frac2));
                        actRG2.setValue(actRG2.getValue() * (1 - frac2));
                        totalTransfer.setValue(totalDemand);

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
                        // we can cover part of the demand ; reduce the act... to (1 - actPrel)*act...
                        actRD1.setValue(actRD1.getValue() * (1 - actPrel.getValue()));
                        actRD2.setValue(actRD2.getValue() * (1 - actPrel.getValue()));
                        actRG1.setValue(actRG1.getValue() * (1 - actPrel.getValue()));
                        actRG2.setValue(actRG2.getValue() * (1 - actPrel.getValue()));
                        totalTransfer.setValue(totalIn+totalAct);

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
            } else {// no water coming into reach but water available
                //looking if we can cover the demand by including usable part of act...
                double frac = totalDemand / (totalAct);
                if (frac <= 1) {
                    //we can cover all of the demand with act, reduce the components accordingly
                    actRD1.setValue(actRD1.getValue() * (1 - frac));
                    actRD2.setValue(actRD2.getValue() * (1 - frac));
                    actRG1.setValue(actRG1.getValue() * (1 - frac));
                    actRG2.setValue(actRG2.getValue() * (1 - frac));
                    totalTransfer.setValue(totalDemand);
                        
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
                    totalTransfer.setValue(totalAct);
                        
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
            
            //distribute total transfer over all HRUs
            double providedFraction = totalTransfer.getValue()/totalDemand;
            double providedWater_tmp=0.;
            for (Attribute.Entity hru : l) {
                double waterRequirements = hru.getDouble(waterRequirementsName.getValue());
                hru.setDouble(irrigationWaterName.getValue(), waterRequirements * providedFraction);
                providedWater_tmp= providedWater_tmp + waterRequirements * providedFraction;
            }
            // restitute lost water to RD2 (when efficiency of the irrigation network <1) :
            inRD2.setValue(inRD2.getValue()+Math.max(0.,totalTransfer.getValue()-providedWater_tmp) );
        } else { // no water available
            for (Attribute.Entity hru : l) {
                hru.setDouble(irrigationWaterName.getValue(), 0); 
            }
            totalTransfer.setValue(0.); 
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
        //remove all HRUs from demand list
        l.removeAll(l);
    }
}
