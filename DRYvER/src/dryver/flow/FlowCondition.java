/*
 * FlowCondition.java
 * Created on 06.04.2022, 22:15:57
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
package dryver.flow;

import jams.data.*;
import jams.model.*;

/**
 *
 * @author Sven Kralisch <sven at kralisch.com>
 */
@JAMSComponentDescription(
        title = "Component to calculate flow conditions",
        author = "Sven Kralisch & Annika Künne",
        description = "Uses a threshold for the simulated runoff to define different flow conditions",
        date = "2022-04-06",
        version = "1.0_0"
)
@VersionComments(entries = {
    @VersionComments.Entry(version = "1.0_0", comment = "Initial version")
})
public class FlowCondition extends JAMSComponent {

    /*
     *  Component attributes
     */
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "current runoff",
            unit = "L"
    )
    public Attribute.Double runoff;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "runoff threshold value for flow/no-flow separation",
            unit = "L"
    )
    public Attribute.Double[] threshold;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "flow state (0 - no flow, 1 - flow, 2 - pools)",
            unit = "L"
    )
    public Attribute.Double flowState;

    /*
     *  Component run stages
     */
    @Override
    public void run() {
        if (threshold.length == 1) {

            if (runoff.getValue() < threshold[0].getValue()) {
                flowState.setValue(0);
            } else {
                flowState.setValue(1);
            }

        } else {
            // to be implenented
        }
    }

}
