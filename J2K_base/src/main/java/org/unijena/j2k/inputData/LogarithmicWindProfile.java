/*
 * LogarithmicWindProfile.java
 * Created on 23.03.2023, 10:57:33
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
package org.unijena.j2k.inputData;

import jams.data.*;
import jams.model.*;

/**
 *
 * @author Annika Künne <annika.kuenne at uni-jena.de>
 */
@JAMSComponentDescription(
        title = "Calculate logarithmic wind profile",
        author = "Annika Künne",
        description = "Approximation of wind speed at 2m above ground using the e logarithmic wind profile at HRU level",
        date = "2024-04-09",
        version = "1.0_0"
)
@VersionComments(entries = {
    @VersionComments.Entry(version = "1.0_0", comment = "Initial version"),})

public class LogarithmicWindProfile extends JAMSComponent {

    /*
     *  Component attributes
     */
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "wind speed in 10m height above ground",
            unit = "m/s"
    )
    public Attribute.Double v10;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "roughness length",
            unit = "m"
    )
    public Attribute.Double z0; // provided by landuse.par or assuming a single value for simplicity, else the VON_KÁRMÁN_Constant would be needed 

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "wind speed in 2m height above ground",
            unit = "m/s"
    )
    public Attribute.Double v2;

    @Override
    public void init() {
    }

    // Method to calculate wind speed at 2m from wind speed at 10m using friction velocity
    private double calculateWindSpeedAt2m(double v10, double z0, double d) {
        // Estimate uStar using the logarithmic wind profile formula
        double v2_ = v10 * Math.log((2.0 - d) / z0) / Math.log((10.0 - d) / z0);
        return v2_;
    }

    //@Override
    public void run() {
        double v10_ = v10.getValue(); 
        double v2_; 
        double z0_ = z0.getValue(); 

        // Calculate the 2m wind speed using the 10m wind speed and the roughness length
        v2_ = calculateWindSpeedAt2m(v10_, z0_, 0);

        v2.setValue(v2_); 
    }

    @Override
    public void cleanup() {

    }
}
