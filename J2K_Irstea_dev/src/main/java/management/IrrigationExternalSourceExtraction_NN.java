/*
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
 *
 */
@JAMSComponentDescription(
        title = "IrrigationExternalSourceExtraction",
        author = "Nico Hachgenei",
        description = "Transfer irrigation water from external source to HRUs "
        + "depending on water.",
        date = "2025-11-20",
        version = "1.0_0"
)
@VersionComments(entries = {
    @VersionComments.Entry(version = "1.0_0", comment = "Initial version")
})
public class IrrigationExternalSourceExtraction_NN extends JAMSComponent {

    /*
     *  Component attributes
     */
//    @JAMSVarDescription(
//            access = JAMSVarDescription.AccessType.READWRITE,
//            description = "Current time step RD1 inflow into hru. Will be updated by this component,"+
//                    "adding losses from poor efficiency. - input / state variable",
//            unit = "L"
//    )
//    public Attribute.Double inRD1;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Water demand of the HRU (= plantIrrigRequirements / efficiency). "+
                    "The unsatisfied part of this will de taken from external source. - parameter"
    )
    public Attribute.Double irrigationDemand;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Water requirements of an HRU - the real plant"+
                    "requirements. Water requirements will be read by this component. - parameter"
    )
    public Attribute.Double plantIrrigRequirements;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "Water delivered to HRU for application (extraction "+
                    "minus losses due to efficiency). This attribute will be updated by this component."+
                    " - input"
    )
    public Attribute.Double irrigationApplication;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Allowed volume to grab from external sources over the whole season. "+
                    "Default -1 -> unlimited ressource.",
            unit = "L",
            defaultValue = "-1"
    )
    public Attribute.Double irrigSeasonAllowedExternalVolume;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "Total use of water for irrigation from external sources. "+
                    "= part of demand actually applied. "+
                    "Should be attribute of the TimeLoop to add to. This component cumulates the irrigation"+ 
                    "demands of irrigated HRUs over this timestep and writes this attribute. - output",
            unit = "L"
    )
    public Attribute.Double totalExternalIrrigUseDT;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "Total use of water for irrigation from external sources. "+
                    "= part of demand actually applied. "+
                    "Should be attribute of the TimeLoop to add to. This component cumulates the irrigation"+ 
                    "demands of irrigated HRUs over the whole season and writes this attribute. - output",
            unit = "L"
    )
    public Attribute.Double totalExternalIrrigUseSeason;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "Total demand of water for irrigation from external sources. "+
                    "= use, amplified by poor efficiency. "+
                    "Should be attribute of the TimeLoop to add to. This component cumulates the irrigation"+ 
                    "demands of irrigated HRUs over this timestep and writes this attribute. - output",
            unit = "L"
    )
    public Attribute.Double totalExternalIrrigDemandDT;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "Total demand of water for irrigation from external sources. "+
                    "= use, amplified by poor efficiency. "+
                    "Should be attribute of the TimeLoop to add to. This component cumulates the irrigation"+ 
                    "demands of irrigated HRUs over the whole season and writes this attribute. - output",
            unit = "L"
    )
    public Attribute.Double totalExternalIrrigDemandSeason;

    /*
     *  Component run stages
     */

    @Override
    public void run() {
        double run_plantIrrigRequirements = plantIrrigRequirements.getValue() ;
        double run_irrigationApplication = irrigationApplication.getValue() ;
        
        // check if this hru has an unsatisfied irrigation need
        if ( run_plantIrrigRequirements > run_irrigationApplication) {// there are unsatisfied irrigation requirements
            double run_irrigDeficit = run_plantIrrigRequirements - run_irrigationApplication ; // deficit of plat requirement
            double run_irrigEfficiency = run_plantIrrigRequirements / irrigationDemand.getValue() ; // recalculate efficiency from requirement and demand
            double run_irrigDemandDeficit = run_irrigDeficit / run_irrigEfficiency ; // calculate deficit in irrigation demand
            double run_totalExternalUseSeason = totalExternalIrrigUseSeason.getValue(); // how much already used 
            double run_totalExternalDemandSeason = totalExternalIrrigDemandSeason.getValue(); // how much already demanded 
            double run_irrigSeasonAllowedExternalVolume = irrigSeasonAllowedExternalVolume.getValue(); // how much can be imported

            if (run_irrigSeasonAllowedExternalVolume == -1) {// external Volume is unlimited -> set to infinity
                run_irrigSeasonAllowedExternalVolume = Double.POSITIVE_INFINITY;
                //run_irrigSeasonAllowedExternalVolume = 1.0/0.0;
            }

            double run_irrigSeasonAvailableExternalVolume = run_irrigSeasonAllowedExternalVolume-run_totalExternalDemandSeason; //available volume is allowed - already taken
            
            if (run_irrigSeasonAvailableExternalVolume > 0) {// only run the following, if there is water available
//                double run_inRD1 = inRD1.getValue();
                double run_externalUseDT; // initialize applied water from external source
                double run_externalDemandDT; // initialize demand from external sources
//                double run_losses; // losses due to poor efficiency
                if (run_irrigSeasonAvailableExternalVolume > run_irrigDemandDeficit) { // enough water available to fulfill needs
                    // take all needed water from external source
                    run_externalDemandDT = run_irrigDemandDeficit ;
                } else { // not enough water available to fulfill external demand
                    run_externalDemandDT = run_irrigSeasonAvailableExternalVolume ;
                }
                run_externalUseDT = run_externalDemandDT * run_irrigEfficiency ; // part that actually reaches the plant
//                run_losses = run_externalDemandDT - run_externalUseDT;
//                getModel().getRuntime().println("plant requirements: "+ run_plantIrrigRequirements + " L, "+
//                        "internally supplied: " + run_irrigationApplication + " L.");
//                getModel().getRuntime().println("--> applied "+ run_externalUseDT + " L from ext sources, "+
//                        "demanded " + run_externalDemandDT + " L.");
                irrigationApplication.setValue(run_irrigationApplication + run_externalUseDT);// add this HRUs external water use to irrigation volume
                totalExternalIrrigUseDT.setValue(totalExternalIrrigUseDT.getValue() + run_externalUseDT); // add this HRUs external water use to total external water use of this time step
                totalExternalIrrigUseSeason.setValue(run_totalExternalUseSeason + run_externalUseDT); // add this HRUs external water use to total external water use of this season
                totalExternalIrrigDemandDT.setValue(totalExternalIrrigDemandDT.getValue() + run_externalDemandDT); // add this HRUs external water use to total external water use of this time step
                totalExternalIrrigDemandSeason.setValue(run_totalExternalDemandSeason + run_externalDemandDT); // add this HRUs external water use to total external water use of this season
                
//                difference between demand and application exists, but losses are expected outside of the catchment
//                inRD1.setValue(run_inRD1 + run_losses); // add losses from poor efficiency to RD1 inflow of HRU
                
            } else {// no water remaining in external source
                // no volumes to change (included for readability)
            }
        } else {// irrigation needs are already satisfied internally, no need for external imports
            // no volumes to change (included for readability)
        }
    }
}