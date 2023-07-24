package Isotopes;

/*
 * IsotopeMixer.java
 * Created on 07.09.2020, 11:23:03
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
import jams.data.*;
import jams.model.*;

/**
 *
 * @author Andrew Watson <awatson@sun.ac.za>
 */
@JAMSComponentDescription(
        title = "IsotopeMixer",
        author = "Andrew Watson, Christian Birkel and Sven Kralisch",
        description = "The isotope mixing component used to mix the incoming isotope"
        + "composition with an existing value as a mass flux",
        date = "2023-04-04",
        version = "1.0_0"
)
@VersionComments(entries = {
    @VersionComments.Entry(version = "1.0_0", comment = "Initial version")
})
public class IsotopeMixer extends JAMSComponent {

    /*
    *   Component atrributes
     */
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Updates both concentrations if true, otherwise "
                    + "just concB",
            defaultValue = "true"
    )
    public Attribute.Boolean bidirectional;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Updates both concentrations if true, otherwise "
                    + "just concB",
            defaultValue = "1",
            lowerBound = 0,
            upperBound = 1
    )
    public Attribute.Double mixingProportion;    

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "intial_volume",
            defaultValue = "0",
            unit = "L",
            lowerBound = 0,
            upperBound = Double.POSITIVE_INFINITY
    )
    public Attribute.Double[] volA;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "Initial_concentration",
            defaultValue = "0",
            unit = "mol/L",
            lowerBound = 0,
            upperBound = Double.NEGATIVE_INFINITY
    )
    public Attribute.Double[] concA;

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

        for (int i = 0; i < volA.length; i++) {
            double x;
            double volA_, volB_;
            if (mixingProportion.getValue() == 0) {
                continue;
            } else {
                volA_ = volA[i].getValue();
                volB_ = volB[i].getValue() / mixingProportion.getValue();
            }
            
            if (volA[i].getValue() + volB[i].getValue() == 0)  {
                x = 0;
            } else {
                x = (concA[i].getValue() * volA_ + concB[i].getValue() * volB_) / (volA_ + volB_);
            }
            if (bidirectional.getValue()) {
                concA[i].setValue(x);
            }
            concB[i].setValue(x);
        }

    }
    
}
