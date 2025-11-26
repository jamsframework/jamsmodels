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
        title = "Binary Isotope Mixing",
        author = "Andrew Watson",
        description = "The isotope mixing model utilises isotope concentrations"
        + "of rainfall, soil-water and groundwater to validate"
        + "proportions of RD1, RD2, RG1 and RG2 combined"
        + "It provides a snapshot of flow component portioning at"
        + "discrete sampling intervals"
        + "Publication status: Under review"
        + "Please cite:Development of an isotope-enabled rainfall-runoff model:"
        + "Improving the capability to capture hydrological and anthropogenic change"
        + "Authors: Andrew Watson, Yuliya Vystavna, Sven Kralisch, Jörg Helmschrot,"
        + "Jared van Rooyen and Jodie Miller",
        date = "2022-09-20",
        version = "1.0_0"
)
@VersionComments(entries = {
    @VersionComments.Entry(version = "1.0_0", comment = "Initial version")
})
public class Binary_isotope_mixing extends JAMSComponent {

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
            description = "Isotope groundwater",
            defaultValue = "0",
            unit = "‰",
            lowerBound = 0,
            upperBound = Double.NEGATIVE_INFINITY
    )
    public Attribute.Double isotopeGw;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "Composition rain",
            defaultValue = "0",
            unit = "fraction",
            lowerBound = 0,
            upperBound = Double.POSITIVE_INFINITY
    )
    public Attribute.Double compRain;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Composition soil-water",
            defaultValue = "0",
            unit = "fraction",
            lowerBound = 0,
            upperBound = Double.POSITIVE_INFINITY
    )
    public Attribute.Double compSW;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "Composition groundwater",
            defaultValue = "0",
            unit = "fraction",
            lowerBound = 0,
            upperBound = Double.POSITIVE_INFINITY
    )
    public Attribute.Double compGw;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "calibration factor minimum surface runoff contribution/rain isotopes",
            defaultValue = "0",
            unit = "fraction",
            lowerBound = 0,
            upperBound = Double.POSITIVE_INFINITY
    )
    public Attribute.Double minCompRain;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "calibration factor minimum groundwater contribution",
            defaultValue = "0",
            unit = "fraction",
            lowerBound = 0,
            upperBound = Double.POSITIVE_INFINITY
    )
    public Attribute.Double minCompGw;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "Available Runoff and GW proportion considering simulated RD2",
            defaultValue = "0",
            unit = "fraction",
            lowerBound = 0,
            upperBound = Double.POSITIVE_INFINITY
    )
    public Attribute.Double FracCompRainGw;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "Normalized Composition rain",
            defaultValue = "0",
            unit = "fraction",
            lowerBound = 0,
            upperBound = Double.POSITIVE_INFINITY
    )
    public Attribute.Double compRainN;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "Normalized Composition GW",
            defaultValue = "0",
            unit = "fraction",
            lowerBound = 0,
            upperBound = Double.POSITIVE_INFINITY
    )
    public Attribute.Double compGwN;

    /*
     *  Component run stages
     */
    @Override
    public void init() {
    }

    @Override
    public void run() {
        compRain.setValue(isotopeStream.getValue() - (isotopeRain.getValue()) / (isotopeRain.getValue() - (isotopeGw.getValue())));
        /*
        * Calculate the fraction of rain water (or runoff) in the hydrograph using stream, rain and gw isotopes
         */

        compGw.setValue(isotopeStream.getValue() - (isotopeRain.getValue()) / (isotopeGw.getValue() - (isotopeRain.getValue())));
        /*
        * Calculate the fraction of groundwater in the hydrograph using stream, rain and gw isotopes
         */

        if (compRain.getValue() == 0) {
            compRain.setValue(minCompRain.getValue());
            return;
        }
        /*
        * Uses a calibration factor to set the min surface ruunoff contribtution
         */

        if (compGw.getValue() == 0) {
            compGw.setValue(minCompGw.getValue());
        }
        /*
        * Uses a calibration factor to set the min groundwater contribtution
         */

        double fracCompRainGw = 1 - compSW.getValue();

        /*
        * Available proportion of runoff and groundwater based on simulated RD1
         */
        compRainN.setValue(compRain.getValue() * fracCompRainGw);

        compGwN.setValue(compGw.getValue() * fracCompRainGw);

        /*
        * Final flow portions consider simulated RD2
         */
    }

    @Override
    public void cleanup() {
    }

}
