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
        author = "Sven Kralisch / LC",
        description = "Transfer water from HRUs to HRUs depending on water"
        + " availability and demand"
	+ "irrigation water comes from incoming water to the reach and water inside the reach (actRG1, etc..)",
        date = "2021-06-09",
        version = "1.0_0"
)
@VersionComments(entries = {
    @VersionComments.Entry(version = "1.0_0", comment = "Initial version")
})
public class WaterTransfer_act_hru extends JAMSComponent {

    /*
     *  Component attributes
     */
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "HRUs list"
    )
    public Attribute.EntityCollection hrus;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "RD1 component in HRU"
    )
    public Attribute.Double RD1;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "RD2 component in HRU"
    )
    public Attribute.Double RD2;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "RG1 component in HRU"
    )
    public Attribute.Double RG1;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "RG2 component in HRU"
    )
    public Attribute.Double RG2;
   
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "actRG1 component in HRU"
    )
    public Attribute.Double actRG1;
            
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "actRG2 component in HRU"
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
            description = "Ratio of water available for irrigation / water present in the HRU (actR..)"
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
            description = "Water available for transferin the HRU"
    )
    public Attribute.Double totalInput;

    /*
     *  Component run stages
     */

    @Override
    public void run() {

        Attribute.Entity currentHRU = hrus.getCurrent();
 
        double totalIn = RD1.getValue() + RD2.getValue() + RG1.getValue() + RG2.getValue(); 
        double totalAct = this.actPrel.getValue() * (actRG1.getValue() + actRG2.getValue());
        double total = totalAct+totalIn;
        this.totalInput.setValue(total); // eau disponible pour l'irrigation à ce pas de temps
        
        //check if this reach even has irrigated HRUs in its catchment
        if (!currentHRU.existsAttribute(irrigationEntitiesListName.getValue())) {
            return;
        }
        double totalDemand = 0;

        // cumulated demand from all HRUs supplied with water from the current HRU
        List<Attribute.Entity> l = (List) currentHRU.getObject(irrigationEntitiesListName.getValue());
        for (Attribute.Entity hru : l) {
            double demand = hru.getDouble(irrigationDemandName.getValue());
            totalDemand += demand;
        }
        this.totalDemand.setValue(totalDemand);

        //calculate proportion of total water that is needed
        if (total != 0.0){
                
            double frac = totalDemand /totalIn;
  
            if (frac <= 1) {

                //we can cover all only with inputs to the reach, reduce the components accordingly
                RD1.setValue(RD1.getValue() * (1 - frac));
                RD2.setValue(RD2.getValue() * (1 - frac));
                RG1.setValue(RG1.getValue() * (1 - frac));
                RG2.setValue(RG2.getValue() * (1 - frac));
                totalTransfer.setValue(totalDemand);

            } else {
                
                //looking if we can cover the demand by including part of act...
                frac = totalDemand / (totalIn+totalAct);

                //we can cover only part of the demand with in, reduce the components to 0
                RD1.setValue(0);
                RD2.setValue(0);
                RG1.setValue(0);
                RG2.setValue(0);
                    
                if (frac <= 1) {
                    //we can cover all of the demand but not only with in..., reduce the components accordingly
                    double actDemand = 0;
                    actDemand = totalDemand - totalIn;
                    double frac2 = actDemand/totalAct;
                    actRG1.setValue(actRG1.getValue() * (1 - frac2));
                    actRG2.setValue(actRG2.getValue() * (1 - frac2));
                    totalTransfer.setValue(totalDemand);

                } else {
                    // we can cover part of the demand ; reduce the act... to (1 - actPrel)*act...
                    actRG1.setValue(actRG1.getValue() * (1 - actPrel.getValue()));
                    actRG2.setValue(actRG2.getValue() * (1 - actPrel.getValue()));
                    totalTransfer.setValue(totalIn+totalAct);
                }
                
            }
            //in case frac = 0 (meaning Demand = 0), just to avoid problem with 1/frac
            if (frac == 0){frac=1;}
            //distribute total transfer over all HRUs
            double providedFraction = Math.min(1, 1 / frac);
            double providedWater_tmp=0.;
            for (Attribute.Entity hru : l) {
                double waterRequirements = hru.getDouble(waterRequirementsName.getValue());
                hru.setDouble(irrigationWaterName.getValue(), waterRequirements * providedFraction);
                providedWater_tmp= providedWater_tmp + waterRequirements * providedFraction;
            }
            // restitute lost water to RD2 (when efficiency of the irrigation network <1) :
            RD2.setValue(RD2.getValue()+Math.max(0.,totalTransfer.getValue()-providedWater_tmp) );
            
        } else {
            
            for (Attribute.Entity hru : l) {
                hru.setDouble(irrigationWaterName.getValue(), 0); 
            }
            totalTransfer.setValue(0.); 
        }
        //remove all HRUs from demand list
        l.removeAll(l);
    }
}
