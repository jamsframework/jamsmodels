/*
 * AnimalDrinkingReader.java
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
import jams.tools.JAMSTools;


/**
 *
 * @author N. Hachgenei
 */
@JAMSComponentDescription(title = "AnimalDrinkingReader",
author = "Nico Hachgenei",
description = "This component reads an ASCII file containing summer and winter animal drinking volumes per source reach "
+ "and adds them to model reach entities.",
date = "2024-03-25",
version = "1.0")
public class AnimalDrinkingReader extends JAMSComponent {

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    description = "Animal drinking parameter file name")
    public Attribute.String ADFileName;
    
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    description = "List of reach objects")
    public Attribute.EntityCollection reaches;

    public void init() {
        //read animal drinking parameter file
        Attribute.EntityCollection ADsources = getModel().getRuntime().getDataFactory().createEntityCollection();

        ADsources.setEntities(J2KFunctions.readParas(FileTools.createAbsoluteFileName(getModel().getWorkspaceDirectory().getPath(), ADFileName.getValue()), getModel()));

        HashMap<Double, Attribute.Entity> ADMap = new HashMap<Double, Attribute.Entity>();
        Attribute.Entity AD, e;
        Object[] attrs;

        //put all entities (animal consumptions) into a HashMap with their ID as key
        Iterator<Attribute.Entity> ADIterator = ADsources.getEntities().iterator();
        while (ADIterator.hasNext()) {
            AD = ADIterator.next();
            ADMap.put(AD.getDouble("ADID"), AD);
        }
        
        // iterate over reaches
        Iterator<Attribute.Entity> reachIterator = reaches.getEntities().iterator();
        while (reachIterator.hasNext()) {
            e = reachIterator.next(); // reach entity

            AD = ADMap.get(e.getDouble("ID"));
            e.setObject("AnimalDrinking", AD);

            if (AD == null) {
                //getModel().getRuntime().println("Reach no. " + e.getDouble("ID") + " not in animal_drinking.par file, setting consumption to 0", JAMS.VERBOSE);
                //e.setObject((String) "drinking", 0); // no animal drinking water is extracted from this reach
                e.setDouble((String) "cons_su", 0);
                e.setDouble((String) "cons_wi", 0);
            } else {
                //e.setObject((String) "drinking", 1); // animal drinking water is extracted from this reach
                attrs = AD.getKeys();

                // loop over keys from animal drinking (summer and winter)
                for (int i = 0; i < attrs.length; i++) {
                    //e.setDouble((String) attrs[i], lu.getDouble((String) attrs[i]));
                    double o = AD.getDouble((String) attrs[i]);
                    e.setDouble((String) attrs[i], o);
                }
            }
        }
        getModel().getRuntime().println("Animal drinking parameter file processed ...", JAMS.VERBOSE);
    }
}
