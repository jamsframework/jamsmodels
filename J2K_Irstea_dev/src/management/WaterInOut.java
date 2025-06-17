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
package management;

import jams.data.*;
import jams.model.*;
import java.util.Calendar;
import java.util.List;

/**
 *
 * @author Nico Hachgenei
 */
@JAMSComponentDescription(
        title = "WaterInOut",
        author = "Nico Hachgenei",
        description = "Extraction of water from reaches (e.g. for drinking water)"
        + " and input into reach (e.g. treated waste water), using a file with one"
        + " value per time step and limit to available water."
	+ " Water comes from incoming water to the reach and water inside the reach (inR.. / actR..)."
        + " Water is put into specified component or distributed",
        date = "2024-05-30",
        version = "2.0_0"
)
@VersionComments(entries = {
    @VersionComments.Entry(version = "1.0_0", comment = "Initial version"),
    @VersionComments.Entry(version = "2.0_0", comment = "improved variable names,"
            + "take water proportionally from both, in and act,"
            + "corrected fractions.")
})
public class WaterInOut extends JAMSComponent {

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
            description = "Volume to extract (negative) or inject (positive). Set by Regionalisation_WaterInOut component"+
                    "and read by this one. - input"
    )
    public Attribute.Double VolumeIO;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Target component name ('R..'). Needed in case of injection. 'distr' for distributing over all components."+
                    "- parameter"
    )
    public Attribute.String targetComp;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "Current time step RD1 inflow into reach. Will be updated by this component,"+
                    "extracting or adding water. - input / state variable"
    )
    public Attribute.Double inRD1;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "Current time step RD2 inflow into reach. Will be updated by this component,"+
                    "extracting or adding water. - input / state variable"
    )
    public Attribute.Double inRD2;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "Current time step RG1 inflow into reach. Will be updated by this component,"+
                    "extracting or adding water. - input / state variable"
    )
    public Attribute.Double inRG1;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "Current time step RG2 inflow into reach. Will be updated by this component,"+
                    "extracting or adding water. - input / state variable"
    )
    public Attribute.Double inRG2;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "Current time step RD1 volume inside reach. Will be updated by this component,"+
                    "extracting or adding water. Used for extraction if not enough incoming water into reach."+
                    "used for injection if no incoming water - input / state variable"
    )
    public Attribute.Double actRD1;
        
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "Current time step RD2 volume inside reach. Will be updated by this component,"+
                    "extracting or adding water. Used for extraction if not enough incoming water into reach."+
                    "used for injection if no incoming water - input / state variable"
    )
    public Attribute.Double actRD2;
        
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "Current time step RG1 volume inside reach. Will be updated by this component,"+
                    "extracting or adding water. Used for extraction if not enough incoming water into reach."+
                    "used for injection if no incoming water - input / state variable"
    )
    public Attribute.Double actRG1;
            
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "Current time step RG2 volume inside reach. Will be updated by this component,"+
                    "extracting or adding water. Used for extraction if not enough incoming water into reach."+
                    "used for injection if no incoming water - input / state variable"
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
            description = "Total demand of water. For verification purposes, not needed."+
                    "Careful not to overwrite irrigation water demand with this! - output"
    )
    public Attribute.Double totalIODemand;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "Total input of water. For verification purposes, not needed. - output"
    )
    public Attribute.Double totalIOInput;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "Total volume extracted from all reaches -> cummulative, should be"+
                    "added to catchment resetter - output / state variable"
    )
    public Attribute.Double IOExtractedAll;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "volume extracted from current reach - output"
    )
    public Attribute.Double IOExtractedR;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "Total volume added to all reaches -> cummulative, should be"+
                    "added to catchment resetter - output / state variable"
    )
    public Attribute.Double IOaddedAll;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "volume added to current reach - output"
    )
    public Attribute.Double IOaddedR;
    
//    @JAMSVarDescription(
//            access = JAMSVarDescription.AccessType.WRITE,
//            description = "Total water in the reach available for irrigation"
//    )
//    public Attribute.Double totalAvail;
    

    /*
     *  Component run stages
     */

    @Override
    public void run() {

//        Attribute.Entity run_currentReach = reaches.getCurrent();
        
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
        
//        this.totalAvail.setValue(run_totalIn + run_totalAct); // all available water for extraction
        
        // define variables for storing extracted / added volumes
        double run_IOExtractedR; // local variable to store actually extracted volume
        double run_IOaddedR;
        
        double run_totalIODemand = 0;
        double run_volume = VolumeIO.getValue();
        // check if extraction (negative VolumeIO) or injection (positive VolumeIO) or none (VolumeIO = 0)
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
            } else { // not all of the demand can be satisfied from available water. Only available water will be extracted
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
            }
            
            run_IOaddedR = 0.; // if water is extracted --> no water is added
            
        } else if (run_volume > 0) { // water injected
//            getModel().getRuntime().println("++ WW will be injecting "+run_volume);
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
