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
        title = "",
        author = "Nico Hachgenei",
        description = "Extraction of water from reaches (e.g. for drinking water)"
        + " and input into reach (e.g. treated waste water), using a file with one"
        + " value per tiem step and limit to available water."
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
    

    /*
     *  Component run stages
     */

    @Override
    public void run() {

        Attribute.Entity currentReach = reaches.getCurrent();
        
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

                    } else {
                        //looking if we can cover the demand by including usable part of act...
                        frac = totalDemand / (totalIn+totalAct);

                        //we can cover only part of the demand with input, reduce the components to 0
                        inRD1.setValue(0);
                        inRD2.setValue(0);
                        inRG1.setValue(0);
                        inRG2.setValue(0);

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

                        } else {
                            // we can only cover part of the demand ; reduce the act... to (1 - actPrel)*act...
                            actRD1.setValue(actRD1.getValue() * (1 - actPrel.getValue()));
                            actRD2.setValue(actRD2.getValue() * (1 - actPrel.getValue()));
                            actRG1.setValue(actRG1.getValue() * (1 - actPrel.getValue()));
                            actRG2.setValue(actRG2.getValue() * (1 - actPrel.getValue()));
                            ExtractedR.setValue(totalIn+totalAct);
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

                    } else {
                        // we can only cover part of the demand ; reduce the act... to (1 - actPrel)*act...
                        actRD1.setValue(actRD1.getValue() * (1 - actPrel.getValue()));
                        actRD2.setValue(actRD2.getValue() * (1 - actPrel.getValue()));
                        actRG1.setValue(actRG1.getValue() * (1 - actPrel.getValue()));
                        actRG2.setValue(actRG2.getValue() * (1 - actPrel.getValue()));
                        ExtractedR.setValue(totalAct);
                    }
                }
            } else { 
                this.ExtractedR.setValue(0.);
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
