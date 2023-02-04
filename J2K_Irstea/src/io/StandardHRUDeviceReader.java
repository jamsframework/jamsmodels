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
@JAMSComponentDescription(title = "StandardHRUDeviceReader",
author = "J. Bonneau, F. Branger",
description = "This component reads an ASCII file containing HRU Device "
+ "information and adds them to model entities.",
date = "2020-12-28",
version = "1.1_0")
public class StandardHRUDeviceReader extends JAMSComponent {

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    description = "HRU Device parameter file name")
    public Attribute.String HRUDeviceFileName;
    
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    description = "List of hru objects")
    public Attribute.EntityCollection hrus;

    public void init() {

        //read HRU Device parameter
        Attribute.EntityCollection HRUDeviceTypes = getModel().getRuntime().getDataFactory().createEntityCollection();

        HRUDeviceTypes.setEntities(J2KFunctions.readParas(FileTools.createAbsoluteFileName(getModel().getWorkspaceDirectory().getPath(), HRUDeviceFileName.getValue()), getModel()));

        HashMap<Double, Attribute.Entity> HRUDeviceMap = new HashMap<Double, Attribute.Entity>();
        Attribute.Entity HRUDevice, e;
        Object[] attrs;

        //put all entities into a HashMap with their ID as key
        Iterator<Attribute.Entity> HRUDeviceIterator = HRUDeviceTypes.getEntities().iterator();
        while (HRUDeviceIterator.hasNext()) {
            HRUDevice = HRUDeviceIterator.next();
            HRUDeviceMap.put(HRUDevice.getDouble("DID"), HRUDevice);
        }

        Iterator<Attribute.Entity> hruIterator = hrus.getEntities().iterator();
        while (hruIterator.hasNext()) {
            e = hruIterator.next();
            HRUDevice = HRUDeviceMap.get(e.getDouble("deviceID"));
            e.setObject("HRUDeviceType", HRUDevice);

            if (HRUDevice == null) {
                getModel().getRuntime().println("HRU Device unit defined in entity no. " + e.getDouble("deviceID") + " is not defined in HRU Device parameter table", JAMS.VERBOSE);
            }
            
            
            attrs = HRUDevice.getKeys();

            for (int i = 0; i < attrs.length; i++) {
                //e.setDouble((String) attrs[i], lu.getDouble((String) attrs[i]));
                Object o = HRUDevice.getObject((String) attrs[i]);
                if (!(o instanceof Attribute.String)) {
                    e.setObject((String) attrs[i], o);
                }
            }
        }
        getModel().getRuntime().println("HRU Device parameter file processed ...", JAMS.VERBOSE);
    }
}
