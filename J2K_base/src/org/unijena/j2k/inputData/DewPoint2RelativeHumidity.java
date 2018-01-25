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
        description = "Approximation of relative humidity from dew point, taken "
                + "from https://en.wikipedia.org/wiki/Dew_point",
        date = "2018-01-25",
        version = "1.0_1"
)
@VersionComments(entries = {
    @VersionComments.Entry(version = "1.0_0", comment = "Initial version"),
    @VersionComments.Entry(version = "1.0_1", comment = "Muliplied rhum value by 100 to get \"%\"")
})
public class DewPoint2RelativeHumidity extends JAMSComponent {

    /*
     *  Component attributes
     */
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Description",
            unit = "°C"
    )
    public Attribute.Double dewPoint;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Description",
            unit = "°C"
    )
    public Attribute.Double temp;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "Description",
            unit = "%"
    )
    public Attribute.Double rhum;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "0: Magnus formula; 1: Simple approximation",
            defaultValue = "0"
    )
    public Attribute.Integer mode;

    /*
     *  Component run stages
     */
    @Override
    public void init() {
    }

    @Override
    public void run() {

        switch (mode.getValue()) {

            case 0:

                double b = 17.67;
                double c = 243.5;
                double d = dewPoint.getValue();
                double T = temp.getValue();

                double h = Math.exp((d * b) / (c + d) - (b * T) / (c + T));

                rhum.setValue(h * 100);

                break;

            case 1:

                rhum.setValue(100 - 5 * (temp.getValue() - dewPoint.getValue()));

                break;

            default:
                getModel().getRuntime().sendHalt("You must set a valid mode value!");
                break;
        }

    }

    @Override
    public void cleanup() {
    }
}
