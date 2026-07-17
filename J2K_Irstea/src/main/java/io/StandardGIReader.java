/*
 * StandardLUReader.java
 * Created on 13. January 2013, 16:39
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
package io;

import jams.JAMS;
import jams.data.*;
import jams.model.*;
import jams.tools.FileTools;
import java.util.*;


/**
 *
 * @author M. Labbas
 */
@JAMSComponentDescription(title = "StandardGIReader",
author = "J Bonneau",
description = "This component reads an ASCII file containing GI management "
+ "information and adds them to model entities.",
date = "2020-12-28",
version = "1.1_0")
public class StandardGIReader extends JAMSComponent {

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    description = "Green infrastructure parameter file name")
    public Attribute.String GIFileName;
    
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    description = "List of hru objects")
    public Attribute.EntityCollection hrus;

    public void init() {

        //read gi parameter
        Attribute.EntityCollection GITypes = getModel().getRuntime().getDataFactory().createEntityCollection();

        GITypes.setEntities(J2KFunctions.readParas(FileTools.createAbsoluteFileName(getModel().getWorkspaceDirectory().getPath(), GIFileName.getValue()), getModel()));

        HashMap<Double, Attribute.Entity> GIMap = new HashMap<Double, Attribute.Entity>();
        Attribute.Entity GI, e;
        Object[] attrs;

        //put all entities into a HashMap with their ID as key
        Iterator<Attribute.Entity> GIIterator = GITypes.getEntities().iterator();
        while (GIIterator.hasNext()) {
            GI = GIIterator.next();
            GIMap.put(GI.getDouble("GIID"), GI);
        }

        Iterator<Attribute.Entity> hruIterator = hrus.getEntities().iterator();
        while (hruIterator.hasNext()) {
            e = hruIterator.next();

            GI = GIMap.get(e.getDouble("GIID"));
            e.setObject("GIType", GI);

            if (GI == null) {
                getModel().getRuntime().println("GI unit defined in entity no. " + e.getDouble("GIID") + " is not defined in GI parameter table", JAMS.VERBOSE);
            }
            
            
            attrs = GI.getKeys();

            for (int i = 0; i < attrs.length; i++) {
                //e.setDouble((String) attrs[i], lu.getDouble((String) attrs[i]));
                Object o = GI.getObject((String) attrs[i]);
                if (!(o instanceof Attribute.String)) {
                    e.setObject((String) attrs[i], o);
                }
            }
        }
        getModel().getRuntime().println("GI parameter file processed ...", JAMS.VERBOSE);
    }
}
