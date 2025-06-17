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
        version = "1.0_0"
)
@VersionComments(entries = {
    @VersionComments.Entry(version = "1.0_0", comment = "Initial version")
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
    public Attribute.Double Volume;
    
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
                    "in the reach (actR..). - parameter",
            defaultValue = "1.0"
    )
    public Attribute.Double actPrel;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "Total demand of water. For verification purposes, not needed."+
                    "Careful not to overwrite irrigation water demand with this! - output"
    )
    public Attribute.Double totalDemand;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "Total input of water. For verification purposes, not needed. - output"
    )
    public Attribute.Double totalInput;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "Total volume extracted from all reaches -> cummulative, should be"+
                    "added to catchment resetter - output / state variable"
    )
    public Attribute.Double ExtractedAll;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "volume extracted from current reach - output"
    )
    public Attribute.Double ExtractedR;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "Total volume added to all reaches -> cummulative, should be"+
                    "added to catchment resetter - output / state variable"
    )
    public Attribute.Double addedAll;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "volume added to current reach - output"
    )
    public Attribute.Double addedR;
    
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

        Attribute.Entity run_currentReach = reaches.getCurrent();
        
        // calculate water available for extraction
        double run_totalIn = inRD1.getValue() + inRD2.getValue() + inRG1.getValue() + inRG2.getValue();
        double run_totalAct = this.actPrel.getValue() * (actRD1.getValue() + actRD2.getValue() + actRG1.getValue() + actRG2.getValue()); // water in the reach that is available for animal needs
        double run_totalAv = run_totalIn + run_totalAct; // all available water for extraction 
//        this.totalAvail.setValue(run_totalIn + run_totalAct); // all available water for extraction
        
        double run_totalDemand = 0;
        double run_volume = Volume.getValue();
        // check if extraction (negative Volume) or injection (positive Volume) or none (Volume = 0)
        if (run_volume<0) { // extract water from the reach 
            
            run_totalDemand = run_volume * (-1);
            this.totalDemand.setValue(run_totalDemand);
            
            //calculate proportion of total water that is needed
            if (run_totalAv != 0.0){ // if there is water available
                if (run_totalIn != 0){ // if there is water coming into reach
//                    getModel().getRuntime().println("DW ++ water coming in, "+run_totalAv+" available("+run_totalIn+" incoming, "+run_totalAct+" in reach), needing "+run_totalDemand+" . Reach "+currentReach.getObject("ID"));
                    
                    double run_frac = run_totalDemand /run_totalIn;

                    if (run_frac <= 1) {

                        //we can cover all only with input to the reach, reduce the components accordingly
                        inRD1.setValue(inRD1.getValue() * (1 - run_frac));
                        inRD2.setValue(inRD2.getValue() * (1 - run_frac));
                        inRG1.setValue(inRG1.getValue() * (1 - run_frac));
                        inRG2.setValue(inRG2.getValue() * (1 - run_frac));
                        ExtractedR.setValue(run_totalDemand);

                    } else {
                        //looking if we can cover the demand by including usable part of act...
                        run_frac = run_totalDemand / (run_totalIn+run_totalAct);

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
                            ExtractedR.setValue(run_totalDemand);

                        } else {
                            // we can only cover part of the demand ; reduce the act... to (1 - actPrel)*act...
                            actRD1.setValue(actRD1.getValue() * (1 - actPrel.getValue()));
                            actRD2.setValue(actRD2.getValue() * (1 - actPrel.getValue()));
                            actRG1.setValue(actRG1.getValue() * (1 - actPrel.getValue()));
                            actRG2.setValue(actRG2.getValue() * (1 - actPrel.getValue()));
                            ExtractedR.setValue(run_totalIn+run_totalAct);
                        }
                    }
//                    getModel().getRuntime().println("DW took "+ExtractedR.getValue());
                } else { // if no water coming into reach, but there is water in the reach act
//                    getModel().getRuntime().println("DW ++ water coming in, "+run_totalAv+" available("+run_totalIn+" incoming, "+run_totalAct+" in reach), needing "+run_totalDemand+" . Reach "+run_currentReach.getObject("ID"));
                    //looking if we can cover the demand by including usable part of act...
                    double run_frac = run_totalDemand / (run_totalAct);
                    if (run_frac <= 1) {
                        //we can cover all of the demand with act, reduce the components accordingly
                        actRD1.setValue(actRD1.getValue() * (1 - run_frac));
                        actRD2.setValue(actRD2.getValue() * (1 - run_frac));
                        actRG1.setValue(actRG1.getValue() * (1 - run_frac));
                        actRG2.setValue(actRG2.getValue() * (1 - run_frac));
                        ExtractedR.setValue(run_totalDemand);

                    } else {
                        // we can only cover part of the demand ; reduce the act... to (1 - actPrel)*act...
                        actRD1.setValue(actRD1.getValue() * (1 - actPrel.getValue()));
                        actRD2.setValue(actRD2.getValue() * (1 - actPrel.getValue()));
                        actRG1.setValue(actRG1.getValue() * (1 - actPrel.getValue()));
                        actRG2.setValue(actRG2.getValue() * (1 - actPrel.getValue()));
                        ExtractedR.setValue(run_totalAct);
                    }
//                    getModel().getRuntime().println("DW took "+ExtractedR.getValue());
                }
            } else { 
                this.ExtractedR.setValue(0.);
            }
            // if water is extracted --> no water is added
            this.addedR.setValue(0.);
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
                        run_AddRD1 = run_volume * inRD1.getValue() / run_totalIn;
                        run_AddRD2 = run_volume * inRD2.getValue() / run_totalIn;
                        run_AddRG1 = run_volume * inRG1.getValue() / run_totalIn;
                        run_AddRG2 = run_volume * inRG2.getValue() / run_totalIn;

                    } else if (run_totalAct > 0) { // no incoming water, but water in reach: distribute proportionally to what is in reach
                        run_AddRD1 = run_volume * actRD1.getValue() / run_totalAct;
                        run_AddRD2 = run_volume * actRD2.getValue() / run_totalAct;
                        run_AddRG1 = run_volume * actRG1.getValue() / run_totalAct;
                        run_AddRG2 = run_volume * actRG2.getValue() / run_totalAct;

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
            inRD1.setValue(inRD1.getValue() + run_AddRD1);
            inRD2.setValue(inRD2.getValue() + run_AddRD2);
            inRG1.setValue(inRG1.getValue() + run_AddRG1);
            inRG2.setValue(inRG2.getValue() + run_AddRG2);
            addedR.setValue(run_AddRD1 + run_AddRD2 + run_AddRG1 + run_AddRG2);
//            getModel().getRuntime().println("++ WW added "+addedR.getValue());
        } else { // neither extraction nor injection (volume = 0)
            this.ExtractedR.setValue(0.);
            this.addedR.setValue(0.);
        }
        this.addedAll.setValue(this.addedAll.getValue() + this.addedR.getValue());
        // extracted volume for all animals (cumulative over reaches)
        this.ExtractedAll.setValue(this.ExtractedAll.getValue() + this.ExtractedR.getValue());
    }
       
    
}
