/*
 * StandardEntityReader.java
 * Created on 23.11.2017, 21:10:30
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

import jams.JAMS;
import jams.data.Attribute;
import jams.model.JAMSComponent;
import jams.model.JAMSComponentDescription;
import jams.model.JAMSVarDescription;
import jams.model.VersionComments;
import jams.tools.FileTools;
import java.io.File;
import java.util.List;
import org.unijena.j2k.J2KFunctions;

/**
 *
 * @author Sven Kralisch <sven.kralisch at uni-jena.de>
 */
@JAMSComponentDescription(title = "EntityParaReader",
        author = "Sven Kralisch & Christian Fischer",
        description = "This component reads an ASCII files containing data "
        + "of JAMS entities and creates a collection accordingly. ",
        date = "2017-11-23",
        version = "1.0_0")
@VersionComments(entries = {
    @VersionComments.Entry(version = "1.0_0", comment = "Initial version")
})
public class EntityParaReader extends JAMSComponent {

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
            description = "Entity parameter file name")
    public Attribute.String entityFileName;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.WRITE,
            description = "Collection of entity objects")
    public Attribute.EntityCollection entities;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
            description = "Name of the attribute containing the entity identifiers",
            defaultValue = "ID")
    public Attribute.String entityIDAttribute;

    List<Attribute.Entity> entityList;

    @Override
    public void init() {

        getModel().getRuntime().println("Reading model entities...", JAMS.VERBOSE);

        //read hru parameter
        String fileName = entityFileName.getValue();
        if (new File(fileName).exists()) {
            fileName = entityFileName.getValue();
        } else if (getModel().getWorkspaceDirectory() != null) {
            fileName = FileTools.createAbsoluteFileName(getModel().getWorkspaceDirectory().getPath(), entityFileName.getValue());
        }
        if (!new File(fileName).exists()) {
            getModel().getRuntime().sendErrorMsg("Couldn't read entity file " + fileName + "!\nIf you are not using an absolute path, "
                    + "please ensure you have defined a workspace directory!");
        }
        entityList = J2KFunctions.readParas(fileName, getModel());

        //assign IDs to all hru entities
        for (Attribute.Entity e : entityList) {
            try {
                e.setId((long) e.getDouble(entityIDAttribute.getValue()));
            } catch (Attribute.Entity.NoSuchAttributeException nsae) {
                getModel().getRuntime().sendErrorMsg("Couldn't find attribute \"ID\" while reading J2K entity parameter file (" + entityFileName.getValue() + ")!");
            }
        }

        entities.setEntities(entityList);

    }

}
