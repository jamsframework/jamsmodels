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
        title = "Isotope_fractionation",
        author = "Andrew Watson, Christian Birkel, Sven Kralisch",
        description = "liquid-vapor equilibrium isotopic fractionation, (Horita and Wesolowski)"
        + "and kinetic isotopic separation",
        date = "2023-04-04",
        version = "1.0_0"
)
@VersionComments(entries = {
    @VersionComments.Entry(version = "1.0_0", comment = "Initial version")
})
public class Isotope_fractionation extends JAMSComponent {

    /*
    *   Component atrributes
     */
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "temp",
            defaultValue = "0",
            unit = "°C",
            lowerBound = 0,
            upperBound = Double.POSITIVE_INFINITY
    )
    public Attribute.Double temp;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "tempK",
            defaultValue = "0",
            unit = "Kelvin",
            lowerBound = 0,
            upperBound = Double.POSITIVE_INFINITY
    )
    public Attribute.Double tempK;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "rhum",
            defaultValue = "0",
            unit = "%",
            lowerBound = 0,
            upperBound = Double.POSITIVE_INFINITY
    )
    public Attribute.Double rhum;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "atmospheric water vapor concentration",
            defaultValue = "0",
            unit = "mol/mol",
            lowerBound = 0,
            upperBound = Double.NEGATIVE_INFINITY
    )
    public Attribute.Double pConc;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "actual evapotranspiration",
            defaultValue = "0",
            unit = "L",
            lowerBound = 0,
            upperBound = Double.POSITIVE_INFINITY
    )
    public Attribute.Double actET;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "actual evapotranspiration using isotopes",
            defaultValue = "0",
            unit = "L",
            lowerBound = 0,
            upperBound = Double.POSITIVE_INFINITY
    )
    public Attribute.Double actETiso;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "concentration of water in the soil",
            defaultValue = "0",
            unit = "mol/L",
            lowerBound = 0,
            upperBound = Double.NEGATIVE_INFINITY
    )
    public Attribute.Double concS;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "isotopic composition of water in the soil",
            defaultValue = "0",
            unit = "mol/L",
            lowerBound = 0,
            upperBound = Double.NEGATIVE_INFINITY
    )
    public Attribute.Double concA;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "isotopic composition of water evaporated from the soil",
            defaultValue = "0",
            unit = "mol/L",
            lowerBound = 0,
            upperBound = Double.NEGATIVE_INFINITY
    )
    public Attribute.Double concE;

    /*
     *  Component run stages
     */
    @Override
    public void init() {
    }

    @Override
    public void run() {

        double concA = this.concA.getValue();
        double concE = this.concE.getValue();
        double concS = this.concS.getValue();
        double actETiso = this.actETiso.getValue();

        tempK.setValue(273.13 + temp.getValue());

        /* a+ is the liquid-vapor equilibrium isotopic fractionation, (Horita and Wesolowski)    
         */
        double alphamas = Math.exp(((1158.8 * Math.pow(tempK.getValue(), 3) / Math.pow(10, 9))
                - (1620.1 * Math.pow(tempK.getValue(), 2) / Math.pow(10, 6))
                + (794.84 * tempK.getValue() / Math.pow(10, 3)) - 161.04
                + (2.9992 * (Math.pow(10, 9) / Math.pow(tempK.getValue(), 3)))) / Math.pow(10, 3));

        /*  e+ the equilibrium isotopic separation between liquid and vapor, calculated as e+ = (a+  1);
         */
        double epsimas = alphamas - 1;
        /*     
   #eK kinetic isotopic separation, 12.5 per mil for 2H on eK = nCK o q (1  h) where CK o is25.0%
   # and 28.6% for deuterium and oxygen-18, respectively,           
         */
        double epsiK = 12.5 * (1 - (rhum.getValue() / 100));

        concA = (pConc.getValue() - epsimas) / alphamas;

        concE = ((1 / (1 - (rhum.getValue() / 100) + epsiK))
                * ((((pConc.getValue() / 1000) - epsimas) / alphamas)
                - ((rhum.getValue() / 100) * (concA / 1000)) - epsiK));

        // Only calculate soil-water evaporation
        actETiso = actET.getValue() - (concE / concS) * actET.getValue();

    }

    @Override
    public void cleanup() {
    }

}
