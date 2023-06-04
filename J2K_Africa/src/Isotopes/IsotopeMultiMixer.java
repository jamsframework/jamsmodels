/*
 * IsotopeMultiMixer.java
 * Created on 04.06.2023, 21:14:40
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
package Isotopes;

import jams.data.*;
import jams.model.*;

/**
 *
 * @author Sven Kralisch <sven at kralisch.com>
 */
@JAMSComponentDescription(
        title = "Title",
        author = "Author",
        description = "Description",
        date = "YYYY-MM-DD",
        version = "1.0_0"
)
@VersionComments(entries = {
    @VersionComments.Entry(version = "1.0_0", comment = "Initial version"),
    @VersionComments.Entry(version = "1.0_1", comment = "Some improvements")
})
public class IsotopeMultiMixer extends JAMSComponent {

    /*
    *   Component atrributes
     */
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "intial_volume",
            defaultValue = "0",
            unit = "L",
            lowerBound = 0,
            upperBound = Double.POSITIVE_INFINITY
    )
    public Attribute.Double volA;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "Initial_concentration",
            defaultValue = "0",
            unit = "mol/L",
            lowerBound = 0,
            upperBound = Double.NEGATIVE_INFINITY
    )
    public Attribute.Double concA;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "intial_volume",
            defaultValue = "0",
            unit = "L",
            lowerBound = 0,
            upperBound = Double.POSITIVE_INFINITY
    )
    public Attribute.Double[] volB;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "Initial_concentration",
            defaultValue = "0",
            unit = "mol/L",
            lowerBound = 0,
            upperBound = Double.NEGATIVE_INFINITY
    )
    public Attribute.Double[] concB;

    /*
     *  Component run stages
     */
    @Override
    public void run() {

        // calc sum of all volumes
        double volume = 0;
        for (int i = 0; i < volB.length; i++) {
            volume += volB[i].getValue();
        }

        // calc new concentrations
        double concSum = 0;
        for (int i = 0; i < volB.length; i++) {
            double x;
            if (volA.getValue() + volB[i].getValue() == 0) {
                x = 0;
            } else {
                // calc weight of volume B[i], i.e. its proportion of the overall volume
                double weight = volB[i].getValue() / volume;
                
                // calc weighted concentration of mixing A and B[i]
                double volA_weighted = volA.getValue() * weight;
                x = (concA.getValue() * volA_weighted + concB[i].getValue() * volB[i].getValue()) / (volA_weighted + volB[i].getValue());
                
                // sum up the weighted concentration for A
                concSum += x * weight;
            }
            concB[i].setValue(x);
        }
        concA.setValue(concSum);
    }

}
