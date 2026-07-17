/*
 * EntitySubset.java
 * Created on 03.09.2020, 16:45:05
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
package org.unijena.j2k.io;

import jams.data.*;
import jams.model.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

/**
 *
 * @author Sven Kralisch <sven.kralisch@uni-jena.de>
 */
@JAMSComponentDescription(
        title = "EntitySubset",
        author = "Sven Kralisch",
        description = "Create an ordered subset from an entity list, using a "
        + "given attribute value for identification. The subset can be ordered "
        + "according to another attribute.",
        date = "2020-09-03",
        version = "1.0_0"
)
@VersionComments(entries = {
    @VersionComments.Entry(version = "1.0_0", comment = "Initial version")
})
public class EntitySubset extends JAMSComponent {

    /*
     *  Component attributes
     */
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "List of entities"
    )
    public Attribute.EntityCollection entities;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "attribute name for filtering"
    )
    public Attribute.String filterAttribute;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "value for filtering"
    )
    public Attribute.Double filterValue;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "attribute name for sorting"
    )
    public Attribute.String sortAttribute;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "List of filtered entities"
    )
    public Attribute.EntityCollection filteredEntities;

    /*
     *  Component run stages
     */
    @Override
    public void init() {

        List<Attribute.Entity> filteredList = new ArrayList();

        Iterator<Attribute.Entity> hruIterator = entities.getEntities().iterator();
        while (hruIterator.hasNext()) {

            Attribute.Entity e = hruIterator.next();
            double d = e.getDouble(filterAttribute.getValue());

            if (d == filterValue.getValue()) {
                filteredList.add(e);
            }
        }
        
        Collections.sort(filteredList, new Comparator<Attribute.Entity>() {
            @Override
            public int compare(Attribute.Entity o1, Attribute.Entity o2) {
                double d1 = o1.getDouble(sortAttribute.getValue());
                double d2 = o2.getDouble(sortAttribute.getValue());
                if (d1 < d2) {
                    return -1;
                } else if (d1 > d2) {
                    return 1;
                } else {
                    return 0;
                }
            }
        });
        
        filteredEntities.setEntities(filteredList);
        
//        for (Attribute.Entity e : filteredList) {
//            
//            System.out.println(e.getDouble(filterAttribute.getValue()) + " : " + e.getDouble(sortAttribute.getValue()));
//            
//        }

    }

}
