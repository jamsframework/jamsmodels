/*
 * AnimalExcretionReader.java
 * Created on 25. March 2024, 09:28
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
package AnimalWater;

import jams.JAMS;
import jams.data.*;
import jams.model.*;
import jams.tools.FileTools;
import java.util.*;


/**
 *
 * @author N. Hachgenei
 */
@JAMSComponentDescription(title = "AnimalExcretionReader",
author = "Nico Hachgenei",
description = "This component reads an ASCII file containing summer and winter animal excretion volumes per hru "
+ "and adds them to model hru entities.",
date = "2024-03-25",
version = "1.0")
public class AnimalExcretionReader extends JAMSComponent {

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    description = "Animal excretion parameter file name")
    public Attribute.String AEFileName;
    
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    description = "List of hru objects")
    public Attribute.EntityCollection hrus;

    public void init() {
        //read animal excretion parameter file
        Attribute.EntityCollection AEsources = getModel().getRuntime().getDataFactory().createEntityCollection();

        AEsources.setEntities(J2KFunctions.readParas(FileTools.createAbsoluteFileName(getModel().getWorkspaceDirectory().getPath(), AEFileName.getValue()), getModel()));

        HashMap<Double, Attribute.Entity> AEMap = new HashMap<Double, Attribute.Entity>();
        Attribute.Entity AE, e;
        Object[] attrs;

        //put all entities (animal excretions) into a HashMap with their ID as key
        Iterator<Attribute.Entity> AEIterator = AEsources.getEntities().iterator();
        while (AEIterator.hasNext()) {
            AE = AEIterator.next();
            AEMap.put(AE.getDouble("AEID"), AE);
        }
        
        // iterate over hrus
        Iterator<Attribute.Entity> hruIterator = hrus.getEntities().iterator();
        while (hruIterator.hasNext()) {
            e = hruIterator.next(); // hru entity

            AE = AEMap.get(e.getDouble("ID"));
            e.setObject("AnimalExcretion", AE);

            if (AE == null) {
                //getModel().getRuntime().println("HRU no. " + e.getDouble("ID") + " not in animal_excretion.par file, setting consumption to 0", JAMS.VERBOSE);
                //e.setObject((String) "excreting", 0); // no animal excreting into this HRU
                e.setDouble((String) "excr_su", 0);
                e.setDouble((String) "excr_wi", 0);
            } else {
                attrs = AE.getKeys();
                //e.setObject((String) "excreting", 1); // there are animals excreting into this HRU
                
                // loop over keys from animal drinking (summer and winter)
                for (int i = 0; i < attrs.length; i++) {
                    //e.setDouble((String) attrs[i], lu.getDouble((String) attrs[i]));
                    double o = AE.getDouble((String) attrs[i]);
                    e.setDouble((String) attrs[i], o);
                }
            }
        }
        getModel().getRuntime().println("Animal Excretion parameter file processed ...", JAMS.VERBOSE);
    }
}
