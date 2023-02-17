package fire;



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
        title = "Fire Danger Index",
        author = "Andrew Watson",
        description = "The lowveld fire danger index: Multiple non linear regression"
        + "Reference:Savage MJ, 2010. A spreadsheet for the lowveld fire danger index rating."
        + " Soil-Plant-Atmosphere Continuum Research Unit, School of Environmental Sciences,"
        + " University of KwaZulu-Natal. "
        + "Source for RCF: Willis C, van Wilgen B, Tolhurst K, Everson C, D’Abreton P, Pero P and"
        + "Fleming G (2001). The Development of a National Fire Danger"
        + "Rating System for South Africa. CSIR, Pretoria. Pp 76.",
        date = "2022-08-08",
        version = "1.0_0"
)
@VersionComments(entries = {
    @VersionComments.Entry(version = "1.0_0", comment = "Initial version")
})
public class FDI extends JAMSComponent {

    /*
    *   Component atrributes
     */
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Air temperature",
            defaultValue = "0",
            unit = "Celsuis",
            lowerBound = 0,
            upperBound = Double.POSITIVE_INFINITY
    )
    public Attribute.Double airTemp;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Relative Humdity",
            defaultValue = "0",
            unit = "‰",
            lowerBound = 0,
            upperBound = Double.POSITIVE_INFINITY
    )
    public Attribute.Double rh;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Windspeed",
            defaultValue = "0",
            unit = "m/s",
            lowerBound = 0,
            upperBound = Double.POSITIVE_INFINITY
    )
    public Attribute.Double wind;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Precipitation",
            defaultValue = "0",
            unit = "mm",
            lowerBound = 0,
            upperBound = Double.POSITIVE_INFINITY
    )
    public Attribute.Double precip;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "Days since last rain",
            defaultValue = "0",
            unit = "days",
            lowerBound = 0,
            upperBound = Double.POSITIVE_INFINITY
    )
    public Attribute.Double successiveDaysWithoutRain;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "Burning index-BI",
            defaultValue = "0",
            unit = "index",
            lowerBound = 0,
            upperBound = Double.POSITIVE_INFINITY
    )
    public Attribute.Double bi;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "Burning index-BI corrected for windspeed",
            defaultValue = "0",
            unit = "index",
            lowerBound = 0,
            upperBound = Double.POSITIVE_INFINITY
    )
    public Attribute.Double bi_u;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "Rainfall correction factor-RCF",
            defaultValue = "0",
            unit = "index",
            lowerBound = 0,
            upperBound = Double.POSITIVE_INFINITY
    )
    public Attribute.Double rcf;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "Fire Danger Index-FDI",
            defaultValue = "0",
            unit = "index",
            lowerBound = 0,
            upperBound = Double.POSITIVE_INFINITY
    )
    public Attribute.Double fdi;
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

        double precip = this.precip.getValue();
        double DaysNoRain = this.successiveDaysWithoutRain.getValue();
        double airTemp = this.airTemp.getValue();
        double rh = this.rh.getValue();
        double wind = this.wind.getValue();
        double bi = this.bi.getValue();
        double bi_u = this.bi_u.getValue();
        double rcf = this.rcf.getValue();
        double fdi = this.fdi.getValue();

        if (precip < 0.1) {
            DaysNoRain = DaysNoRain + 1;
        } else {
            DaysNoRain = 0;
        }
        /*
            Used to calculate the number of dry days
         */
        bi = (airTemp + 30) / (rh * 0.3158);

        if (wind < 0.555) {
            bi_u = bi + 0.1;
        }
        if (wind >= 0.1) {
            if (wind < 2.222) {
                bi_u = bi + 5;
            }
        }
        if (wind >= 2.222) {
            if (wind < 4.444) {
                bi_u = bi + 10;
            }
        }
        if (wind >= 4.444) {
            if (wind < 6.666) {
                bi_u = bi + 15;
            }
        }
        if (wind >= 6.666) {
            if (wind < 8.888) {
                bi_u = bi + 20;
            }
        }
        if (wind >= 8.888) {
            if (wind < 10) {
                bi_u = bi + 25;
            }
        }
        if (wind >= 10) {
            if (wind < 11.3888) {
                bi_u = bi + 30;
            }
        }
        if (wind >= 11.3888) {
            if (wind < 12.5) {
                bi_u = bi + 35;
            }
        } else if (wind > 12.5) {
            bi_u = bi + 40;
        }

        rcf = 0.49194 - (0.03072 * precip) + (0.22151 * DaysNoRain) + (5.0275 * Math.pow(10, -4))
                * Math.pow(precip, 2) - 0.02156 * Math.pow(DaysNoRain, 2) - 3.10334 * Math.pow(10, -6)
                * Math.pow(precip, 3) + 0.00107 * (Math.pow(DaysNoRain, 3));

        /*        
        rcf= (0.4907009-0.03469392 * precip + 0.0006906988 * Math.pow (precip, 2-0.000004597443)*
                Math.pow(precip, 3+0.25240022)* DaysNoRain - 0.03070337 * Math.pow (DaysNoRain, 2+0.001744842)*
                Math.pow (DaysNoRain, 3-0.00006064842) * DaysNoRain - 0.032763) /0.947574;
         */
        if (rcf >= 1) {
            rcf = 1;
        } else if (rcf < 0.1) {
            rcf = 0.1;
        }

        fdi = rcf * bi_u;

        this.successiveDaysWithoutRain.setValue(DaysNoRain);
        this.bi.setValue(bi);
        this.bi_u.setValue(bi_u);
        this.rcf.setValue(rcf);
        this.fdi.setValue(fdi);

    }

    @Override
    public void cleanup() {
    }
}
