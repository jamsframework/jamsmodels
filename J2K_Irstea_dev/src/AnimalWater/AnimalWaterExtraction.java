/*
 * AnimalWaterExtraction.java
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
	+ " water comes from incoming water to the reach and water inside the reach (actRG1, etc..)"
        + "New names. Bugfix in water extraction from act. Use of more"
        + "internal variables. Application of extraction limitation to in and act, equal extraction"
        + "from both.",
        date = "2024-04-16 / 2025-06-04",
        version = "2.0_0"
)
@VersionComments(entries = {
    @VersionComments.Entry(version = "1.0_0", comment = "Initial version"),
    @VersionComments.Entry(version = "2.0_0", comment = "Modififed case where no water comes into the reach,"
            + " but water is inside the reach --> now water will be extracted from the reach in this case."
            + "New names. Bugfix in water extraction from act. Use of more"
            + "internal variables. Application of extraction limitation to in and act, equal extraction"
            + "from both. All variables contain animal terminology")
})
public class AnimalWaterExtraction extends JAMSComponent {

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

    /*
     *  Component run stages
     */

    @Override
    public void run() {
        Attribute.Entity run_currentReach = reaches.getCurrent();
        // no need to check if animals are drinking from this reach --> this component is only executed if this is the case
        
        // define internal variables
        double run_inRD1 = inRD1.getValue();
        double run_inRD2 = inRD2.getValue();
        double run_inRG1 = inRG1.getValue();
        double run_inRG2 = inRG2.getValue();
        double run_actRD1 = actRD1.getValue();
        double run_actRD2 = actRD2.getValue();
        double run_actRG1 = actRG1.getValue();
        double run_actRG2 = actRG2.getValue();
        double run_allowedExtractionFraction = allowedAnimExtractionFraction.getValue();
        
        // calculate amount of water available
        double run_totalIn = run_inRD1 + run_inRD2 + run_inRG1 + run_inRG2; // all water in inflow (for proportional extraction)
        double run_totalAct = run_actRD1 + run_actRD2 + run_actRG1 + run_actRG2; // all water in act (for proportional extraction)
        double run_totalStorage = run_totalIn + run_totalAct; // all water in inflow and act
        // calculate part that is allowed to be taken
        double run_inAvailable = run_allowedExtractionFraction * run_totalIn;
        double run_actAvailable = run_allowedExtractionFraction * run_totalAct; // water in the reach for irrigation
        double run_totalAvailable = run_inAvailable + run_actAvailable; // all available water
        this.totalAnimAvailable.setValue(run_totalAvailable); // all available water for animal drinking
        
        double run_totalAnimDemand;
        double run_animExtractedReach; // internal variable to store actually extracted volume
//        double run_providedFraction; // internal variable to store provided fraction of initial demand
        
        // check season in order to decide which quantity animals drink
        int run_jDay = time.get(Calendar.DAY_OF_YEAR);
        if (run_jDay >= summerStart.getValue() && run_jDay <= summerEnd.getValue()) {
            run_totalAnimDemand = run_currentReach.getDouble("cons_su");
        } else {
            run_totalAnimDemand = run_currentReach.getDouble("cons_wi");
        }
        this.totalAnimDemand.setValue(run_totalAnimDemand);
        
        //calculate proportion of total water that is needed
        if ((run_totalAvailable != 0.0) & (run_totalAnimDemand !=0.0)){ // if there is water available & water needed
            
            double run_availableDemandFraction = run_totalAnimDemand / run_totalAvailable;// fraction of available water that is demanded for animal drinking
            
            if (run_availableDemandFraction <=1){ // demand can be satisfied with available water from inflow and act
                double run_storageDemandFraction = run_totalAnimDemand / run_totalStorage;// fraction of all stored water that is demanded for animal drinking
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
            }
            
        } else { 
            run_animExtractedReach = 0;
        }
        // extracted volume for animals from this reach
        animExtractedReach.setValue(run_animExtractedReach);
        // extracted volume for all animals (cumulative over reaches)
        this.totalAnimExtracted.setValue(this.totalAnimExtracted.getValue() + run_animExtractedReach);
    }
}
