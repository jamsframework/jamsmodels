/*
 * CalcAreaWeight.java
 * Created on 23. Februar 2006, 17:15
 *
 * This file is part of JAMS
 * Copyright (C) 2005 S. Kralisch and P. Krause
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA
 *
 */

package org.unijena.j2k;

import org.unijena.jams.model.*;
import org.unijena.jams.data.*;

/**
 *
 * @author S. Kralisch
 */
@JAMSComponentDescription(
        title="CalcAreaWeight",
        author="Peter Krause",
        description="Calculates the area weight"
        )
public class CalcAreaWeight extends JAMSComponent {
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "Entity area attribute"
            )
            public JAMSDouble entityArea;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "Catchment area attribute"
            )
            public JAMSDouble catchmentArea;    
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "Entity area weight"
            )
            public JAMSDouble areaWeight;
    
    public void run() throws JAMSEntity.NoSuchAttributeException {
        areaWeight.setValue(catchmentArea.getValue()/entityArea.getValue());
    }
    
}
