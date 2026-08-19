/*
 * DoubleAdd_Time.java
 * Created on Apr 10, 2025, 11:23:53 AM
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
package calc;

import jams.data.*;
import jams.model.*;
import java.io.IOException;
import java.util.Calendar;

/**
 *
 * @author watso
 */
@JAMSComponentDescription(
        title = "DoubleAdd_Time",
        author = "Andrew Watson",
        description = "Add two double values and return the result for a specific period",
        date = "2025-04-10",
        version = "1.0_0"
)
@VersionComments(entries = {
    @VersionComments.Entry(version = "1.0_0", comment = "Initial version"),
    @VersionComments.Entry(version = "1.0_1", comment = "Some improvements")
})
public class DoubleAdd_Time extends JAMSComponent {

    /*
     *  Component attributes
     */
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "The current model time")
    public Attribute.Calendar time;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "First operand"
    )
    public Attribute.Double[] d1;
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Second operand"
    )
    public Attribute.Double[] d2;
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "Result of d1+d2 (element-wise)"
    )
    public Attribute.Double[] result;

    transient Runnable job;

    /*
     *  Component run stages
     */
    @Override

    public void initAll() {
        if (d2.length != 1 && d2.length != 12) {
            getModel().getRuntime().sendHalt("Number of addition values should be 1 or 12!");
        }
    }

    @Override
    public void init() {

        if (d1.length != result.length) {
            getModel().getRuntime().sendHalt("Attribute result has wrong length, should be length of d1");
        }

        if (d1.length == d2.length) {

            job = new Runnable() {
                @Override
                public void run() {
                    for (int i = 0; i < d1.length; i++) {
                        result[i].setValue(d1[i].getValue() + d2[i].getValue());
                    }
                }
            };

        } else if (d2.length == 1) {

            job = new Runnable() {
                @Override
                public void run() {
                    for (int i = 0; i < d1.length; i++) {
                        result[i].setValue(d1[i].getValue() + d2[0].getValue());
                    }
                }
            };

        } else if (d2.length == 12) {

            job = new Runnable() {
                @Override
                public void run() {
                    for (int i = 0; i < d1.length; i++) {
                        int nowMonth = time.get(Calendar.MONTH); // 0 = Jan, 11 = Dec
                        result[i].setValue(d1[i].getValue() + d2[nowMonth].getValue());

                    }
                }
            };

        } else {

            getModel().getRuntime().sendHalt("Attribute d2 has wrong length, should be 1, 12 or length of d1");
        }
    }

    private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        this.init();
    }

    @Override
    public void run() {
        job.run();
    }
}
