package Fire;



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
        + "Rating System for South Africa. CSIR, Pretoria. Pp 76."
        + "Adding additional spatial variables which impact fire risk spatially"
        + "after: Maniatis, Doganis and Chatzigeorgiadis 2022"
        + "Fire Risk Probability Mapping Using Machine Learning Tools"
        + "and Multi-Criteria Decision Analysis in the GIS Environment: A "
        + "Case Study in the National Park Forest Dadia-Lefkimi-Soufli, Greece",
        date = "2022-08-08",
        version = "1.0_0"
)
@VersionComments(entries = {
    @VersionComments.Entry(version = "1.0_0", comment = "Initial version")
})
public class FDI_ranked extends JAMSComponent {

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
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "Fire Danger Index-FDI",
            defaultValue = "0",
            unit = "index",
            lowerBound = 0,
            upperBound = Double.POSITIVE_INFINITY
    )
    public Attribute.Double fdi;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Ranking land use with fire danger",
            defaultValue = "0",
            unit = "index",
            lowerBound = 0,
            upperBound = Double.POSITIVE_INFINITY
    )
    public Attribute.Double fdiLanduse;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Slope",
            defaultValue = "0",
            unit = "%",
            lowerBound = 0,
            upperBound = Double.POSITIVE_INFINITY
    )
    public Attribute.Double slope;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Elevation",
            defaultValue = "0",
            unit = "m",
            lowerBound = 0,
            upperBound = Double.POSITIVE_INFINITY
    )
    public Attribute.Double elevation;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Aspect",
            defaultValue = "0",
            unit = "degrees",
            lowerBound = 0,
            upperBound = Double.POSITIVE_INFINITY
    )
    public Attribute.Double aspect;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "Slope ranked for fire danger",
            defaultValue = "0",
            unit = "index",
            lowerBound = 0,
            upperBound = Double.POSITIVE_INFINITY
    )
    public Attribute.Double slopeRank;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "Elevation ranked for fire danger",
            defaultValue = "0",
            unit = "index",
            lowerBound = 0,
            upperBound = Double.POSITIVE_INFINITY
    )
    public Attribute.Double elevationRank;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "Aspect ranked for fire danger",
            defaultValue = "0",
            unit = "index",
            lowerBound = 0,
            upperBound = Double.POSITIVE_INFINITY
    )
    public Attribute.Double aspectRank;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "Aspect ranked for fire danger",
            defaultValue = "0",
            unit = "index",
            lowerBound = 0,
            upperBound = Double.POSITIVE_INFINITY
    )
    public Attribute.Double fdiRank;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "Aspect weight",
            defaultValue = "0",
            unit = "fraction",
            lowerBound = 0,
            upperBound = Double.POSITIVE_INFINITY
    )
    public Attribute.Double aspectW;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "fdi weight",
            defaultValue = "0",
            unit = "fraction",
            lowerBound = 0,
            upperBound = Double.POSITIVE_INFINITY
    )
    public Attribute.Double fdiW;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "Slope weight",
            defaultValue = "0",
            unit = "fraction",
            lowerBound = 0,
            upperBound = Double.POSITIVE_INFINITY
    )
    public Attribute.Double slopeW;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "Elevation weight",
            defaultValue = "0",
            unit = "fraction",
            lowerBound = 0,
            upperBound = Double.POSITIVE_INFINITY
    )
    public Attribute.Double elevationW;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "fdiLanduseW",
            defaultValue = "0",
            unit = "fraction",
            lowerBound = 0,
            upperBound = Double.POSITIVE_INFINITY
    )
    public Attribute.Double fdiLanduseW;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "Fire Danger Index-FDI ranked",
            defaultValue = "0",
            unit = "index",
            lowerBound = 0,
            upperBound = Double.POSITIVE_INFINITY
    )
    public Attribute.Double fdiR;
    /**
     *
     */
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "time"
    )
    public Attribute.Calendar time;

    @Override
    public void initAll() {

        double slope = this.slope.getValue();
        double elevation = this.elevation.getValue();
        double aspect = this.aspect.getValue();
        double slopeRank = this.slopeRank.getValue();
        double elevationRank = this.elevationRank.getValue();
        double aspectRank = this.aspectRank.getValue();

        if (slope > 16.7) {
            slopeRank = 5;
        }
        if (slope >= 11.3) {
            if (slope < 16.7) {
                slopeRank = 4;
            }
        }
        if (slope >= 5.71) {
            if (slope < 11.3) {
                slopeRank = 3;
            }
        }
        if (slope >= 2.86) {
            if (slope < 5.71) {
                slopeRank = 2;
            }
        } else if (slope >= 0) {
            if (slope < 2.86) {
                slopeRank = 1;
            }
        }
        if (elevation > 400) {
            elevationRank = 1;
        }
        if (elevation >= 400) {
            if (elevation < 300) {
                elevationRank = 2;
            }
        }
        if (elevation >= 300) {
            if (elevation < 200) {
                elevationRank = 3;
            }
        }
        if (elevation >= 200) {
            if (elevation < 100) {
                elevationRank = 4;
            }
        }
        if (elevation >= 10) {
            if (elevation < 100) {
                elevationRank = 5;
            }
        } else if (elevation < 10) {
            elevationRank = 5;
        }

        if (aspect >= 202.5) {
            if (aspect < 247.5) {
                aspectRank = 5;
            }
        }

        if (aspect >= 157.5) {
            if (aspect < 202.5) {
                aspectRank = 4;
            }
        }
        if (aspect >= 112.5) {
            if (aspect < 157.5) {
                aspectRank = 3;
            }
        }
        if (aspect >= 67.5) {
            if (aspect < 112.5) {
                aspectRank = 3;
            }
        }
        if (aspect >= 247.5) {
            if (aspect < 292.5) {
                aspectRank = 2;
            }
        }

        if (aspect >= 292.5) {
            if (aspect < 337.5) {
                aspectRank = 2;
            }
        }
        if (aspect >= 0) {
            if (aspect < 22.5) {
                aspectRank = 1;
            }
        }
        if (aspect >= 337.5) {
            if (aspect < 360) {
                aspectRank = 1;
            }
        } else if (aspect >= 22.5) {
            if (aspect < 67.5) {
                aspectRank = 1;
            }
        }
        this.slopeRank.setValue(slopeRank);
        this.elevationRank.setValue(elevationRank);
        this.aspectRank.setValue(aspectRank);
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
        double fdiR = this.fdiR.getValue();
        double fdiLanduse = this.fdiLanduse.getValue();
        double fdiW = this.fdiW.getValue();
        double slopeW = this.slopeW.getValue();
        double aspectW = this.aspectW.getValue();
        double elevationW = this.elevationW.getValue();
        double fdiRank = this.fdiRank.getValue();

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

        fdiRank = fdi / 20;

        fdiR = (fdiRank * fdiW) + (aspectRank.getValue() * aspectW) + (fdiLanduse * fdiLanduseW.getValue())
                + (slopeRank.getValue() * slopeW) + (elevationRank.getValue() * elevationW);

        this.successiveDaysWithoutRain.setValue(DaysNoRain);
        this.bi.setValue(bi);
        this.bi_u.setValue(bi_u);
        this.rcf.setValue(rcf);
        this.fdi.setValue(fdi);
        this.fdiRank.setValue(fdiRank);
        this.fdiR.setValue(fdiR);
       
    }

    @Override
    public void cleanup() {
    }
}
