/*
 * IrrigationApplication.java
 * Created on 13.08.2015, 17:42:55
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

/**
 *
 * @author Sven Kralisch <sven.kralisch at uni-jena.de>
 */
@JAMSComponentDescription(
        title = "",
        author = "Sven Kralisch",
        description = "Apply irrigation on an HRU based on available water." +
                "New names. Limit calculation to case where irrigationApplication>0.",
        date = "2015-08-13 / 2025-03-25",
        version = "2.0_0"
)
@VersionComments(entries = {
    @VersionComments.Entry(version = "1.0_0", comment = "Initial version"),
    @VersionComments.Entry(version = "2.0_0", comment = "New names. Limit calculation to case where irrigationApplication>0.")
})
public class IrrigationApplicationDrip_NN extends JAMSComponent {

    /*
     *  Component attributes
     */
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "Water extracted from source and transferred to this HRU, available for irrigation."
                    +"will be updated, substractin the part actually used. - input",
            unit = "L"
    )
    public Attribute.Double irrigationApplication;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE, 
            description = "Water actually applied for irrigation. Used to keep track of amount applied in current time step" +
                    "before overwriting irrigationApplication (by extraction component). - J2000 output",
            unit = "L"
    )
    public Attribute.Double irrigationApplicationOutput;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "capacity of medium pore space MPS - par",
            unit = "L"
    )
    public Attribute.Double maxMPS;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "current storage in medium pore space MPS - state var",
            unit = "L"
    )
    public Attribute.Double actMPS;

    /*
     *  Component run stages
     */
    @Override
    public void run() {
        double run_irrigationApplication = irrigationApplication.getValue();
        double run_actIrrigation;
        if (run_irrigationApplication != 0) { // only calculate if any irrigation water applied
            double run_actMPS = actMPS.getValue();
            // irrigationApplicationOutput.setValue(0); // this has no use!
            // actual irrigation is the availabe irrigation water, but limited to  
            // available storage capacity
            run_actIrrigation = Math.min(maxMPS.getValue() - run_actMPS, run_irrigationApplication);

            // increase actMPS by the irrigation volume
            actMPS.setValue(run_actMPS + run_actIrrigation);

            // decrease irrigationWater by the irrigation volume
            run_irrigationApplication -= run_actIrrigation;
            // this means if ever we have tranferesd too much water to the HRU, it is lost! but this should never occur
            if(run_irrigationApplication != 0){ // is there any water remaining after irrigation application that would be lost?
                getModel().getRuntime().println("WARNING: Too much water available for irrigation!"+
                        "The remainder (" + run_irrigationApplication + " L) will be lost!");
            }
            irrigationApplication.setValue(run_irrigationApplication);
        } else {
            run_actIrrigation = 0;
        }

        // set irrigationTotal to the irrigation volume
        irrigationApplicationOutput.setValue(run_actIrrigation);
        
    }

}
