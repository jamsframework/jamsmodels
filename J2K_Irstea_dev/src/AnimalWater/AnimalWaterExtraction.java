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
	+ " water comes from incoming water to the reach and water inside the reach (actRG1, etc..)",
        date = "2024-04-16",
        version = "1.0_0"
)
@VersionComments(entries = {
    @VersionComments.Entry(version = "1.0_0", comment = "Initial version")
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
            description = "inflow into RD1 component in reach, used to extract water from - state variable or input ?"
    )
    public Attribute.Double inRD1;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "inflow into RD2 component in reach, used to extract water from - state variable or input ?"
    )
    public Attribute.Double inRD2;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "inflow into RG1 component in reach, used to extract water from - state variable or input ?"
    )
    public Attribute.Double inRG1;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "inflow into RG2 component in reach, used to extract water from - state variable or input ?"
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

    /*
     *  Component run stages
     */

    @Override
    public void run() {

        Attribute.Entity run_currentReach = reaches.getCurrent();
        
        // check if animals are drinking from this reach --> not in here, but add switch context for this
        
        // calculate water available for animal drinking
        double run_totalIn = inRD1.getValue() + inRD2.getValue() + inRG1.getValue() + inRG2.getValue();
        double run_totalAct = this.actPrel.getValue() * (actRD1.getValue() + actRD2.getValue() + actRG1.getValue() + actRG2.getValue()); // water in the reach that is available for animal needs
        double run_totalAv = run_totalIn + run_totalAct; // all available water for animal drinking
        this.totalAvail.setValue(run_totalAv); // all available water for animal drinking
        double run_totalDemand;
        double run_animalExtractedR = 0;
        double run_frac;
        
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

                run_frac = run_totalDemand /run_totalIn;

                if (run_frac <= 1) {

                    //we can cover all only with input to the reach, reduce the components accordingly
                    inRD1.setValue(inRD1.getValue() * (1 - run_frac));
                    inRD2.setValue(inRD2.getValue() * (1 - run_frac));
                    inRG1.setValue(inRG1.getValue() * (1 - run_frac));
                    inRG2.setValue(inRG2.getValue() * (1 - run_frac));
                    run_animalExtractedR = run_totalDemand ;

                } else {
                    //looking if we can cover the demand by including usable part of act...
                    run_frac = run_totalDemand / (run_totalAv);

                    //we can cover only part of the demand with input, reduce the components to 0
                    inRD1.setValue(0);
                    inRD2.setValue(0);
                    inRG1.setValue(0);
                    inRG2.setValue(0);

                    if (run_frac <= 1) {
                        //we can cover all of the demand with input and act together, reduce the components accordingly
                        double run_actDemand;
                        run_actDemand = run_totalDemand - run_totalIn;
                        double run_frac2 = run_actDemand/run_totalAct;
                        actRD1.setValue(actRD1.getValue() * (1 - run_frac2));
                        actRD2.setValue(actRD2.getValue() * (1 - run_frac2));
                        actRG1.setValue(actRG1.getValue() * (1 - run_frac2));
                        actRG2.setValue(actRG2.getValue() * (1 - run_frac2));
                        run_animalExtractedR = run_totalDemand ;

                    } else {
                        // we can only cover part of the demand ; reduce the act... to (1 - actPrel)*act...
                        actRD1.setValue(actRD1.getValue() * (1 - actPrel.getValue()));
                        actRD2.setValue(actRD2.getValue() * (1 - actPrel.getValue()));
                        actRG1.setValue(actRG1.getValue() * (1 - actPrel.getValue()));
                        actRG2.setValue(actRG2.getValue() * (1 - actPrel.getValue()));
                        run_animalExtractedR = run_totalAv ;
                    }
                }

            } else { // if no water coming into reach, but there is water in the reach act
                //looking if we can cover the demand by including usable part of act...
                run_frac = run_totalDemand / (run_totalAct);
                if (run_frac <= 1) {
                    //we can cover all of the demand with act, reduce the components accordingly
                    actRD1.setValue(actRD1.getValue() * (1 - run_frac));
                    actRD2.setValue(actRD2.getValue() * (1 - run_frac));
                    actRG1.setValue(actRG1.getValue() * (1 - run_frac));
                    actRG2.setValue(actRG2.getValue() * (1 - run_frac));
                    run_animalExtractedR = run_totalDemand ;

                } else {
                    // we can only cover part of the demand ; reduce the act... to (1 - actPrel)*act...
                    actRD1.setValue(actRD1.getValue() * (1 - actPrel.getValue()));
                    actRD2.setValue(actRD2.getValue() * (1 - actPrel.getValue()));
                    actRG1.setValue(actRG1.getValue() * (1 - actPrel.getValue()));
                    actRG2.setValue(actRG2.getValue() * (1 - actPrel.getValue()));
                    run_animalExtractedR = run_totalAct ;
                }
            }
        } else { 
            animalExtractedR.setValue(0.);
        }
        // extracted volume for all animals (cumulative over reaches)
        this.animalExtractedAll.setValue(this.animalExtractedAll.getValue() + run_animalExtractedR);
    }
}
