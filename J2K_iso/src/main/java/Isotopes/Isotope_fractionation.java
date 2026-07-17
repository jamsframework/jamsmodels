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
            description = "Precipitation water vapor concentration",
            defaultValue = "0",
            unit = "permil",
            lowerBound = 0,
            upperBound = Double.NEGATIVE_INFINITY
    )
    public Attribute.Double pConc;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "concentration of water in the soil",
            defaultValue = "0",
            unit = "permil",
            lowerBound = 0,
            upperBound = Double.NEGATIVE_INFINITY
    )
    public Attribute.Double concS;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "isotopic composition of water in the atmosphere",
            defaultValue = "0",
            unit = "permil",
            lowerBound = 0,
            upperBound = Double.NEGATIVE_INFINITY
    )
    public Attribute.Double concA;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "isotopic composition of water evaporated from the soil",
            defaultValue = "0",
            unit = "permil",
            lowerBound = 0,
            upperBound = Double.NEGATIVE_INFINITY
    )
    public Attribute.Double concE;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "seasonality factor",
            defaultValue = "1",
            unit = "unitless",
            lowerBound = 0,
            upperBound = Double.NEGATIVE_INFINITY
    )
    public Attribute.Double k;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "exchange factor",
            defaultValue = "0.9",
            unit = "unitless",
            lowerBound = 0,
            upperBound = Double.NEGATIVE_INFINITY
    )
    public Attribute.Double x;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "initital soil-water Isototope composition",
            defaultValue = "0",
            unit = "permil",
            lowerBound = 0,
            upperBound = Double.NEGATIVE_INFINITY
    )
    public Attribute.Double init_concS;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "alphamas",
            defaultValue = "0",
            unit = "permil",
            lowerBound = 0,
            upperBound = Double.NEGATIVE_INFINITY
    )
    public Attribute.Double alphamas;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "epsimas",
            defaultValue = "0",
            unit = "permil",
            lowerBound = 0,
            upperBound = Double.NEGATIVE_INFINITY
    )
    public Attribute.Double epsimas;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "enrichment_slope",
            defaultValue = "0",
            unit = "permil",
            lowerBound = 0,
            upperBound = Double.NEGATIVE_INFINITY
    )
    public Attribute.Double enrichment_slope;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "dstar",
            defaultValue = "0",
            unit = "permil",
            lowerBound = 0,
            upperBound = Double.NEGATIVE_INFINITY
    )
    public Attribute.Double dstar;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "epsk_H",
            defaultValue = "0",
            unit = "permil",
            lowerBound = 0,
            upperBound = Double.NEGATIVE_INFINITY
    )
    public Attribute.Double epsk_H;

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
        double alphamas = this.alphamas.getValue();
        double epsimas = this.epsimas.getValue();
        double epsk_H = this.epsk_H.getValue();
        double enrichment_slope = this.enrichment_slope.getValue();
        double dstar = this.dstar.getValue();

        tempK.setValue(273.13 + temp.getValue());

        /* a+ is the liquid-vapor equilibrium isotopic fractionation, (Horita and Wesolowski)    
         */
        alphamas = Math.exp(1 / 1000. * (1158.8 * Math.pow(tempK.getValue(), 3) / Math.pow(10, 9) - 1620.1 * Math.pow(tempK.getValue(), 2)
                / Math.pow(10, 6) + 794.84 * tempK.getValue() / Math.pow(10, 3) - 161.04 + 2.9992 * Math.pow(10, 9) / Math.pow(tempK.getValue(), 3)));
        /*convert to permil notation
         */
        epsimas = (alphamas - 1) * 1000;
        /* get kinetic fractionation factors value from Merlivat (1978) #permil notation       
         */
        epsk_H = 0.9755 * (1 - 0.9755) * 1000 * (1 - rhum.getValue());

        /* compute the useful variables m and dstar ('enrichment slope and limiting isotopic composition)(Gibson et al.(2016))   
         */
        enrichment_slope = (rhum.getValue() - Math.pow(10, -3) * (epsk_H + epsimas / alphamas)) / (1 - rhum.getValue() + Math.pow(10, -3) * epsk_H);
        /*
        # get atmospheric composition from precipitation-equilibrium assumption (Gibson et al., 2008)k <- 1 #seasonality factor 
         */
        concA = (pConc.getValue() - k.getValue() * epsimas) / (1 + epsimas * Math.pow(10, -3));
        /*
       this is A/B in Gonfiantini 1986
         */
        dstar = (rhum.getValue() * concA + epsk_H + epsimas / alphamas) / (rhum.getValue() - Math.pow(10, -3) * (epsk_H + epsimas / alphamas));
        /*
        compute the isotopic composition of the residual liquid,desiccating water body
         */
        concS = (init_concS.getValue() - dstar * Math.pow(1 - x.getValue(), enrichment_slope) + dstar);
        /*
        compute vapor isotopic composition (Craig and Gordon 1965 formula, with notation by Gibson 2016)permil notation
         */
        concE = ((concS - epsimas) / alphamas - rhum.getValue() * concA - epsk_H) / (1 - rhum.getValue() + Math.pow(10, -3) * epsk_H);

        this.concA.setValue(concA);
        this.concS.setValue(concS);
        this.alphamas.setValue(alphamas);
        this.epsimas.setValue(epsimas);
        this.enrichment_slope.setValue(enrichment_slope);
        this.dstar.setValue(dstar);
        this.epsk_H.setValue(epsk_H);
        this.concE.setValue(concE);

    }

    @Override
    public void cleanup() {
    }

}
