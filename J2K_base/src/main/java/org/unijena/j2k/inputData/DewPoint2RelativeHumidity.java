/*
 * DewPoint2RelativeHumidity.java
 * Created on 26.07.2017, 10:57:33
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
 * @author Sven Kralisch <sven.kralisch at uni-jena.de>
 */
@JAMSComponentDescription(
        title = "DewPoint2RelativeHumidity",
        author = "Sven Kralisch",
        description = "Calculation of relative humidity from dew point based "
                + "on Magnus formula, taken from "
                + "https://en.wikipedia.org/wiki/Dew_point",
        date = "2025-02-16",
        version = "1.0_2"
)
@VersionComments(entries = {
    @VersionComments.Entry(version = "1.0_0", comment = "Initial version"),
    @VersionComments.Entry(version = "1.0_1", comment = "Muliplied rhum value by 100 to get \"%\""),
    @VersionComments.Entry(version = "1.0_2", comment = "Calcultion slightly adapted and mode switch removed")
})
public class DewPoint2RelativeHumidity extends JAMSComponent {

    /*
     *  Component attributes
     */
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "dew point temperature",
            unit = "°C"
    )
    public Attribute.Double dewPoint;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "temperature",
            unit = "°C"
    )
    public Attribute.Double temp;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "relative humidity",
            unit = "%"
    )
    public Attribute.Double rhum;

    /*
     *  Component run stages
     */
    @Override
    public void init() {
    }

    @Override
    public void run() {

        double b = 17.62;
        double c = 243.12;
        double d = dewPoint.getValue();
        double T = temp.getValue();

        double h = Math.exp((d * b) / (c + d) - (b * T) / (c + T));

        rhum.setValue(h * 100);
        
    }
    
    @Override
    public void cleanup() {
    }
}
