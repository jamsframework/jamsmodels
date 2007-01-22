/*
 * StandardEntityReader.java
 * Created on 2. November 2005, 15:49
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

package org.unijena.j2k.io;

import org.unijena.j2k.*;
import org.unijena.jams.data.*;
import org.unijena.jams.model.*;
import java.util.*;
import org.unijena.jams.JAMS;

/**
 *
 * @author S. Kralisch
 */
public class StandardEntityReader_1 extends JAMSComponent {
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.INIT,
            description = "Workspace directory name"
            )
            public JAMSString dirName;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.INIT,
            description = "entity parameter file name"
            )
            public JAMSString entityFileName;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "entity collection"
            )
            public JAMSEntityCollection entities;
    
    public void init() throws JAMSEntity.NoSuchAttributeException {
        
        //read entity parameter
        entities.setEntities(J2KFunctions.readParas(dirName.getValue() + "/" + entityFileName.getValue(), getModel()));
        getModel().getRuntime().println("Entities read and created successfull!", JAMS.STANDARD);
        
    }
    
    
}
