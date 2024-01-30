/*
 * WindSpeed10mTo2m.java
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
        title = "WindSpeed10mTo2m",
        author = "Annika Künne",
        description = "Approximation of wind speed at 2m above ground from measurements at 10m above ground",
        date = "2023-03-23",
        version = "1.0_1"
)
@VersionComments(entries = {
    @VersionComments.Entry(version = "1.0_0", comment = "Initial version"),
    @VersionComments.Entry(version = "1.0_1", comment = "Muliplied rhum value by 100 to get \"%\"")
})
public class WindSpeed10mTo2m extends JAMSComponent {

    /*
     *  Component attributes
     */
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "wind_10m",
            unit = "m/s"
    )
    public Attribute.DoubleArray v10;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "wind_2m",
            unit = "m/s"
    )
    public Attribute.DoubleArray v2;

    /*
     *  Component run stages
     */
    @Override
    public void init() {
        double[] x = new double[v10.getValue().length];
        v2.setValue(x);
    }

    @Override
    public void run() {
        int i = 0;
        double[] x = v2.getValue();
        for (double v : v10.getValue()) {
            double s = transformWindSpeed(v, 10, 2);
            x[i] = s;
            i++;
        }
    }

    public static double transformWindSpeed(double v10, double h10, double h2) {
        double alpha = 1.0 / 7.0; // Power law exponent
        double v2 = v10 * Math.pow(h2 / h10, alpha); // Apply power law
        return v2;
    }

    @Override
    public void cleanup() {
    }
}
