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
import java.util.List;

/**
 *
 * @author Sven Kralisch <sven.kralisch at uni-jena.de>
 */
@JAMSComponentDescription(
        title = "",
        author = "François Tilmant + Sven Kralisch, Nico Hachgenei",
        description = "Transfer water from reaches to HRUs depending on water"
        + " availability and irrigation demand"
	+ "irrigation water comes from incoming water to the reach and water inside the reach (actRG1, etc..)",
        date = "2015-08-13",
        version = "2.0_0"
)
@VersionComments(entries = {
    @VersionComments.Entry(version = "1.0_0", comment = "Initial version"),
    @VersionComments.Entry(version = "2.0_0", comment = "Modififed case where no water comes into the reach,"
            + " but water is inside the reach --> now water will be extracted from the reach in this case.")
})
public class IrrigationWaterTransfer_act extends JAMSComponent {

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

    /*
     *  Component run stages
     */

    @Override
    public void run() {

        Attribute.Entity currentReach = reaches.getCurrent();

        //check if this reach even has irrigated HRUs in its catchment
        if (!currentReach.existsAttribute(irrigationEntitiesListName.getValue())) { // no irrigated HRUs --> step out of function
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

                } else {
                    //looking if we can cover the demand by including part of act...
                    frac = totalDemand / (totalIn+totalAct);

                    //we can cover only part of the demand with in, reduce the components to 0
                    inRD1.setValue(0);
                    inRD2.setValue(0);
                    inRG1.setValue(0);
                    inRG2.setValue(0);

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

                    } else {
                        // we can cover part of the demand ; reduce the act... to (1 - actPrel)*act...
                        actRD1.setValue(actRD1.getValue() * (1 - actPrel.getValue()));
                        actRD2.setValue(actRD2.getValue() * (1 - actPrel.getValue()));
                        actRG1.setValue(actRG1.getValue() * (1 - actPrel.getValue()));
                        actRG2.setValue(actRG2.getValue() * (1 - actPrel.getValue()));
                        totalTransfer.setValue(totalIn+totalAct);
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

                } else {
                    // we can only cover part of the demand ; reduce the act... to (1 - actPrel)*act...
                    actRD1.setValue(actRD1.getValue() * (1 - actPrel.getValue()));
                    actRD2.setValue(actRD2.getValue() * (1 - actPrel.getValue()));
                    actRG1.setValue(actRG1.getValue() * (1 - actPrel.getValue()));
                    actRG2.setValue(actRG2.getValue() * (1 - actPrel.getValue()));
                    totalTransfer.setValue(totalAct);
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
//remove all HRUs from demand list
        l.removeAll(l);
    }
}
