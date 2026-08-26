/*
 * SnowSublimation.java
 * Created on 06.02.2022, 22:34:30
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
package org.unijena.j2k.snow;

import jams.data.*;
import jams.model.*;

/**
 *
 * @author Sven Kralisch <sven at kralisch.com>
 */
@JAMSComponentDescription(
        title = "SnowSublimation",
        author = "Sven Kralisch",
        description = "Calculate snow sublimation after https://doi.org/10.1029/2020WR029266",
        date = "2022-02-16",
        version = "1.0_0"
)
@VersionComments(entries = {
    @VersionComments.Entry(version = "1.0_0", comment = "Initial version")
})
public class SnowSublimation extends JAMSComponent {

    /*
     *  Component attributes
     */
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
            description = "daily mean air temperature",
            unit = "°C")
    public Attribute.Double tmean;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
            description = "precipitation",
            unit = "mm")
    public Attribute.Double precip;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
            description = "daily shortwave radiation",
            unit = "MJ m^-2 d^-1")
    public Attribute.Double swRad;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
            description = "minimum daily shortwave radiation",
            unit = "MJ m^-2 d^-1")
    public Attribute.Double swRadMin;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
            description = "maximum daily shortwave radiation",
            unit = "MJ m^-2 d^-1")
    public Attribute.Double swRadMax;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
            description = "entity elevation",
            unit = "m")
    public Attribute.Double elevation;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
            description = "entity area",
            unit = "m²")
    public Attribute.Double area;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
            description = "topographic exposure")
    public Attribute.Double topex;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
            description = "albedo")
    public Attribute.Double albedo;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
            description = "minimum elevation where sublimation occurs",
            defaultValue = "2500")
    public Attribute.Double minElevation;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
            description = "maximum elevation where sublimation occurs",
            defaultValue = "6500")
    public Attribute.Double maxElevation;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
            description = "sublimation factor",
            defaultValue = "1")
    public Attribute.Double sFact;
    
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READWRITE,
            description = "total snow water equivalent",
            unit = "L")
    public Attribute.Double totSWE;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.WRITE,
            description = "actual sublimation",
            unit = "L")
    public Attribute.Double sublimation;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
            description = "actual sublimation",
            unit = "L")
    public Attribute.Double ID;    
    public static final double L = 2.838;
    
    /*
     *  Component run stages
     */
    @Override
    public void run() {

        //make sure scaledElevation is between min/max elevation
        double scaledElevation = Math.min(Math.max(elevation.getValue(), minElevation.getValue()), maxElevation.getValue());

        //scale inputs to sublimation probability to [0, 1]
        scaledElevation -= minElevation.getValue();
        scaledElevation /= maxElevation.getValue() - minElevation.getValue();
              
        //calc sublimation probability
        double pSub = swRad.getValue() * topex.getValue() * scaledElevation;
        
        double pSubMin = swRadMin.getValue() * topex.getValue() * scaledElevation;
        double pSubMax = swRadMax.getValue() * topex.getValue() * scaledElevation;
        
        if (pSub != 0) {
            pSub = (pSub - pSubMin) / (pSubMax - pSubMin);
        }
        
        //calc theoretical maximum sublimation
        double sMax = (1 - albedo.getValue()) * swRad.getValue() * L;
        
        //calc favorable sublimation condition
        boolean sCon = (tmean.getValue() <= 0) && (precip.getValue() <= 1);
        
        //calc potential sublimation
        double sPot;
        
        if (!sCon) {
            sPot = 0;
        } else {
            sPot = sFact.getValue() * pSub * sMax;
        }
        
        sPot *= area.getValue();
        
        //reduce to actual sublimation
        double sAct = Math.min(sPot, totSWE.getValue());
        
        totSWE.setValue(totSWE.getValue() - sAct);
        sublimation.setValue(sAct);
        
    }

    @Override
    public void cleanup() {
    }
}
