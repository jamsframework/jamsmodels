/*
 * PatchyIndex.java
 * Created on 06.10.2022, 22:50:16
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
        title = "PatchyIndex",
        author = "Sven Kralisch",
        description = "Calculates the 'patchy' index",
        date = "2022-10-06",
        version = "1.0_0"
)
@VersionComments(entries = {
    @VersionComments.Entry(version = "1.0_0", comment = "Initial version")
})
public class PatchyIndex extends JAMSComponent {

    /*
     *  Component attributes
     */
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
            description = "daily flow state")
    public Attribute.Double flowState;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
            description = "daily flow state attribute name",
            defaultValue = "flowstate")
    public Attribute.String flowStatAttributeName;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Description"
    )
    public Attribute.Entity toReach;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "Patchy state"
    )
    public Attribute.Double patchy;

    /*
     *  Component run stages
     */
    @Override
    public void init() {
    }

    @Override
    public void run() {
        if (toReach.getKeys().length != 0) {
            
            double nextState = toReach.getDouble(flowStatAttributeName.getValue());
            double thisState = flowState.getValue();
            
            // consider only flowing/intermittend states
            nextState = Math.min(nextState, 1);
            thisState = Math.min(thisState, 1);
            
            if (thisState == nextState) {
                patchy.setValue(0);
            } else {
                patchy.setValue(1);
            }
        } else {
            patchy.setValue(0);
        }
    }

    @Override
    public void cleanup() {
    }
}
