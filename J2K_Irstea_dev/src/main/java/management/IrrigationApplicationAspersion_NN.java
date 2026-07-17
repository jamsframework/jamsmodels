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
public class IrrigationApplicationAspersion_NN extends JAMSComponent {

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
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "HRU precipitation - irrigation will be added to this.",
            unit = "mm"
    )
    public Attribute.Double precip;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "HRU area to convert L in mm before adding to P",
            unit = "m²"
    )
    public Attribute.Double area;

    /*
     *  Component run stages
     */

    @Override
    public void run() {
        double run_irrigationApplication = irrigationApplication.getValue();
        if (run_irrigationApplication != 0) { // only calculate if any irrigation water applied
            
            double run_irrigationInMM = run_irrigationApplication / area.getValue();
            precip.setValue(precip.getValue() + run_irrigationInMM);
            irrigationApplication.setValue(0);
        } else {
            run_irrigationApplication = 0;
        }
        irrigationApplicationOutput.setValue(run_irrigationApplication);
        
    }

}
