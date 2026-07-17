/*
 * InitHRUIrrigationSource.java
 * Created on 20.01.2026, 15:17:17
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
package management;

import jams.data.*;
import jams.model.*;
import java.util.HashSet;
import java.util.Set;
import java.util.Arrays;


@JAMSComponentDescription(
        title = "InitHRUIrrigationSource",
        author = "Nathan Pellerin",
        description = "Create two lists of HRU ID used to supply irrigation",
        date = "2026-01-20",
        version = "1.0_0")

public class InitHRUIrrigationSource extends JAMSComponent {

    /*
     *  Component attributes
     */
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "HRUs list"
    )
    public Attribute.EntityCollection hrus;
    
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.WRITE,
    description = "List of all HRU ID used as irrigation sources from GroundWater storage")
    public Attribute.StringArray irrigation_sources_GW_list;
    
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.WRITE,
    description = "List of all HRU ID used as irrigation sources from Farm Dam storage")
    public Attribute.StringArray irrigation_source_farmdam_list;


    @Override
public void init() {
    Set<Integer> gwSources = new HashSet<>();
    Set<Integer> fdSources = new HashSet<>();
    
    for (Attribute.Entity hru : hrus.getEntities()) {
        double gwsource = hru.getDouble("irrig_source_ID_hru");
        int gwsource_integer = (int)gwsource;

        if (gwsource_integer > 0){
            gwSources.add(gwsource_integer);
        } 
        
        double fdsource = hru.getDouble("irrig_source_ID_farmdams");
        int fdsource_integer = (int)fdsource;
        
        if (fdsource_integer > 0){
            fdSources.add(fdsource_integer);
        }
    }

    String[] fdArray = fdSources.stream()
        .map(Object::toString)
        .toArray(String[]::new);

    irrigation_source_farmdam_list.setValue(fdArray);

    String[] gwArray = gwSources.stream()
            .map(Object::toString)
            .toArray(String[]::new);

    irrigation_sources_GW_list.setValue(gwArray);

    getModel().getRuntime().println("GW list = " + Arrays.toString(gwArray)+
            " // FD list = " + Arrays.toString(fdArray));


    }
}