/*
 * J2KGlacier2ReachRouting.java
 * Created on 10. April 2008, 09:21
 *
 * This file is part of JAMS
 * Copyright (C) 2005 FSU Jena, c0krpe
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA
 *
 */

package Glaciers;

import jams.data.*;
import jams.model.*;

/**
 *
 * @author Peter Krause
 */
@JAMSComponentDescription(
        title="J2KGlacier2ReachRouting",
        author="Peter Krause",
        description="Passes the melt of a glacier HRU to a corresponding" +
        "reach as component RD1"
        )
        public class J2KGlacier2HRURouting extends JAMSComponent {

    /*
     *  Component variables
     */
     @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "HRU statevar glacier melt outflow"
            )
            public Attribute.Double glacierRunoff;
     
     @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "HRU RD1 from upper HRUs"
            )
            public Attribute.Double RD1;

     @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "the receiving glacier HRU"
            )
            public Attribute.Entity toPoly;

     /*@JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "The reach collection"
            )
            public Attribute.EntityCollection entities;*/

    /*
     *  Component run stages
     */

    public void init() throws Attribute.Entity.NoSuchAttributeException {
    }

    public void run() throws Attribute.Entity.NoSuchAttributeException {
        double gm = glacierRunoff.getValue();
        //Attribute.Entity ent = entities.getCurrent();
        //long id = ent.getId();
        //System.out.println("ID: " + ent.getId());
        if (!toPoly.isEmpty()) {
            double RD1inside = this.RD1.getValue();
            double RD1in = toPoly.getDouble("RD1");
            RD1in = RD1in + gm + RD1inside;
            RD1.setValue(0);
            toPoly.setDouble("RD1", RD1in);
        } 
    }

    public void cleanup() {

    }
}

