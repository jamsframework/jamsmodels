/*
 * StandardLUReader.java
 * Created on 13. February 2022, 22:28
 *
 * This file is part of JAMS
 * Copyright (C) 2005 S. Kralisch and P. Krause
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
package org.unijena.j2k.io;

import org.unijena.j2k.*;
import jams.data.*;
import jams.model.*;
import java.util.*;
import jams.JAMS;
import jams.tools.FileTools;

/**
 *
 * @author S. Kralisch
 */
@JAMSComponentDescription(title = "StandardLUReader",
        author = "Sven Kralisch",
        description = "This component reads an ASCII file containing any kind of "
        + "information and adds it to model entities.",
        date = "2022-02-13",
        version = "1.1_0")
public class UniversalParameterReader extends JAMSComponent {

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Land use parameter file name"
    )
    public Attribute.String parameterFileName;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "List of hru objects"
    )
    public Attribute.EntityCollection entities;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
            description = "Name of the attribute containing the entity identifiers",
            defaultValue = "ID")
    public Attribute.String entityIDAttribute;

    public void init() {
        //read lu parameter
        Attribute.EntityCollection params = getModel().getRuntime().getDataFactory().createEntityCollection();

        params.setEntities(J2KFunctions.readParas(FileTools.createAbsoluteFileName(getModel().getWorkspaceDirectory().getPath(), parameterFileName.getValue()), getModel()));

        HashMap<Double, Attribute.Entity> paramMap = new HashMap<Double, Attribute.Entity>();
        Attribute.Entity param, e;
        Object[] attrs;

        //put all entities into a HashMap with their ID as key
        Iterator<Attribute.Entity> paramIterator = params.getEntities().iterator();
        while (paramIterator.hasNext()) {
            param = paramIterator.next();
            paramMap.put(param.getDouble(entityIDAttribute.getValue()), param);
        }

        Iterator<Attribute.Entity> entityIterator = entities.getEntities().iterator();
        while (entityIterator.hasNext()) {
            e = entityIterator.next();
            param = paramMap.get(e.getDouble(entityIDAttribute.getValue()));

            if (param == null) {
                getModel().getRuntime().sendHalt("No data found for entity " + e.getId() + "!");
                return;
            }

            attrs = param.getKeys();

            for (int i = 0; i < attrs.length; i++) {
                e.setDouble((String) attrs[i], param.getDouble((String) attrs[i]));
            }

        }
        getModel().getRuntime().println("parameter file processed ...", JAMS.VERBOSE);
    }

}
