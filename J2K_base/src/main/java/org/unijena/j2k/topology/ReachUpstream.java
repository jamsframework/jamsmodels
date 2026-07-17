/*
 * ReachUpstream.java
 * Created on 18.05.2022, 23:33:28
 *
 * This file is part of JAMS
 * Copyright (C) Sven Kralisch <sven at kralisch.com>
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
package org.unijena.j2k.topology;

import jams.data.*;
import jams.model.*;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Sven Kralisch <sven at kralisch.com>
 */
@JAMSComponentDescription(
        title = "ReachUpstream",
        author = "Sven Kralisch",
        description = "Retrieve the (full, upstream) subbasin of a given reach",
        date = "2022-05-19",
        version = "1.0_0"
)
@VersionComments(entries = {
    @VersionComments.Entry(version = "1.0_0", comment = "Initial version")
})
public class ReachUpstream extends ReachSubbasin {

    /*
     *  Component attributes
     */
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Name of reach downstream linkage",
            defaultValue = "to_reach"
    )
    public Attribute.String reach2reachAttributeName;

    /*
     *  Component run stages
     */
    @Override
    public void init() {

        super.init();

        for (Attribute.Entity reach : reaches.getEntities()) {
            reach.setObject("__upstreamHRUs", new ArrayList<Attribute.Entity>());
        }

        for (Attribute.Entity reach : reaches.getEntities()) {

            List<Attribute.Entity> upstreamList = (List<Attribute.Entity>) reach.getObject("__upstreamHRUs");
            List<Attribute.Entity> subbasinList = reach2hruMap.get(reach);
            if (subbasinList != null) {
                upstreamList.addAll(subbasinList);
            }

            Attribute.EntityCollection hrus = getModel().getRuntime().getDataFactory().createEntityCollection();
            hrus.setEntities(upstreamList);
            reach.setObject(subbasinEntitiesAttributeName.getValue(), hrus);

            Attribute.Entity destReach = (Attribute.Entity) reach.getObject(reach2reachAttributeName.getValue());
            if (!destReach.isEmpty()) {
                List<Attribute.Entity> destReachList = (List<Attribute.Entity>) destReach.getObject("__upstreamHRUs");
                destReachList.addAll(upstreamList);
            }

        }

    }
}
