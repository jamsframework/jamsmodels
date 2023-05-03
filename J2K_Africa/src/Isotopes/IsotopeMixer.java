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
            description = "intial_volume",
            defaultValue = "0",
            unit = "L",
            lowerBound = 0,
            upperBound = Double.POSITIVE_INFINITY
    )
    public Attribute.Double inVol;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Initial_concentration",
            defaultValue = "0",
            unit = "L",
            lowerBound = 0,
            upperBound = Double.NEGATIVE_INFINITY
    )
    public Attribute.Double inConc;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Actual_volume",
            defaultValue = "0",
            unit = "L",
            lowerBound = 0,
            upperBound = Double.POSITIVE_INFINITY
    )
    public Attribute.Double actVol;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Actual_concentration",
            defaultValue = "0",
            unit = "L",
            lowerBound = 0,
            upperBound = Double.NEGATIVE_INFINITY
    )
    public Attribute.Double actConc;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Actual_Mass",
            defaultValue = "0",
            unit = "unitless",
            lowerBound = 0,
            upperBound = Double.NEGATIVE_INFINITY
    )
    public Attribute.Double actM;

    /*
     *  Component run stages
     */
    @Override
    public void init() {

        // Set the actual volume and concentration to the initial values
        this.actVol.setValue(this.inVol.getValue());
        this.actConc.setValue(this.inConc.getValue());
    }

    @Override
    public void run() {

// Get the incoming volume and isotope concentration
        double incomingVolume = this.inVol.getValue();
        double incomingConcentration = this.inConc.getValue();

        // Calculate the total volume
        double totalVolume = this.actVol.getValue() + incomingVolume;

        // Calculate the new concentration as a weighted average of the old and new concentrations
        double newConcentration = (this.actConc.getValue() * this.actVol.getValue()
                + incomingConcentration * incomingVolume) / totalVolume;

        // Update the actual volume and concentration
        this.actVol.setValue(totalVolume);
        this.actConc.setValue(newConcentration);

        // Calculate the new mass
        double newMass = newConcentration * totalVolume * 1000;
        this.actM.setValue(newMass);

    }

    @Override
    public void cleanup() {
    }

}
