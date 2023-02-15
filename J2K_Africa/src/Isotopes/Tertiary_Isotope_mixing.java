package Isotopes;

/*
 * IrrigationDam_Init.java
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
        title = "Isotope Mixing",
        author = "Andrew Watson",
        description = "The isotope mixing model utilises isotope concentrations"
        + "of rainfall, soil-water and groundwater to validate"
        + "proportions of RD1, RD2, RG1 and RG2 combined"
        + "It provides a snapshot of flow component portioning at"
        + "discrete sampling intervals"
        + "Please cite:Development of an isotope-enabled rainfall-runoff model:"
        + "Improving the capability to capture hydrological and anthropogenic change"
        + "Authors: Andrew Watson, Yuliya Vystavna, Sven Kralisch, Jörg Helmschrot"
        + "Jared van Rooyen and Jodie Miller",
        date = "2022-03-16",
        version = "1.0_0"
)
@VersionComments(entries = {
    @VersionComments.Entry(version = "1.0_0", comment = "Initial version")
})
public class Tertiary_Isotope_mixing extends JAMSComponent {

    /*
    *   Component atrributes
     */
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Isotope rain",
            defaultValue = "0",
            unit = "‰",
            lowerBound = 0,
            upperBound = Double.NEGATIVE_INFINITY
    )
    public Attribute.Double isotopeRain;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Isotope stream",
            defaultValue = "0",
            unit = "‰",
            lowerBound = 0,
            upperBound = Double.NEGATIVE_INFINITY
    )
    public Attribute.Double isotopeStream;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Isotope soil-water",
            defaultValue = "0",
            unit = "‰",
            lowerBound = 0,
            upperBound = Double.NEGATIVE_INFINITY
    )
    public Attribute.Double[] isotopeSw;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Isotope groundwater",
            defaultValue = "0",
            unit = "‰",
            lowerBound = 0,
            upperBound = Double.NEGATIVE_INFINITY
    )
    public Attribute.Double isotopeGw;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "Component A",
            defaultValue = "0",
            unit = "‰",
            lowerBound = 0,
            upperBound = Double.NEGATIVE_INFINITY
    )
    public Attribute.Double compA;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "Component B",
            defaultValue = "0",
            unit = "‰",
            lowerBound = 0,
            upperBound = Double.NEGATIVE_INFINITY
    )
    public Attribute.Double compB;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "RD1 component",
            defaultValue = "0",
            unit = "‰",
            lowerBound = 0,
            upperBound = Double.NEGATIVE_INFINITY
    )
    public Attribute.Double catchmentRD1;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "RD2 component",
            defaultValue = "0",
            unit = "‰",
            lowerBound = 0,
            upperBound = Double.NEGATIVE_INFINITY
    )
    public Attribute.Double catchmentRD2;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "RG1 and RG2 component",
            defaultValue = "0",
            unit = "‰",
            lowerBound = 0,
            upperBound = Double.NEGATIVE_INFINITY
    )
    public Attribute.Double catchmentRG1RG2;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "catchment Simulated runoff",
            defaultValue = "0",
            unit = "‰",
            lowerBound = 0,
            upperBound = Double.NEGATIVE_INFINITY
    )
    public Attribute.Double catchmentSimRunoff;

    /**
     *
     */
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "time"
    )
    public Attribute.Calendar time;

    @Override
    public void init() {
    }

    @Override
    public void run() {

        compA.setValue(isotopeStream.getValue() * catchmentSimRunoff.getValue());

        /*
        compA used to proportion the isotope concentration of the total simulated runoff 
         */
        double curIsotopeSw;
        curIsotopeSw = this.isotopeSw[time.get(Attribute.Calendar.MONTH)].getValue();

        if (!(isotopeRain.getValue() == -99)) {

            compB.setValue(isotopeGw.getValue() * catchmentRG1RG2.getValue()
                    + curIsotopeSw * catchmentRD2.getValue());
        } else {
            compB.setValue(isotopeRain.getValue() * catchmentRD1.getValue() + isotopeGw.getValue() * catchmentRG1RG2.getValue()
                    + curIsotopeSw * catchmentRD2.getValue());
        }

        /*
        compB used to proportion the isotope concentration of runoff, interflow and baseflow 
         */
    }

    @Override
    public void cleanup() {
    }

}
