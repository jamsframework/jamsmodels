/*
 * SewerOverflowDevice.java
 * Created on 05. October 2012, 17:02
 *
 * This file is part of JAMS
 * Copyright (C) FSU Jena
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

package tracking;

import jams.data.*;
import jams.model.*;
import java.util.GregorianCalendar;

/**
 *
 * @author Sven Kralisch & Mériem Labbas & Christian Fischer
 */
@JAMSComponentDescription(title = "DoubleTransfer",
author = "Nico Hachgenei",
description = "modified after SewerOverflowDevice_track and DoubleTransfer"
        + "module that tracks sewer to reach water transfer through a double transfer",
version = "1.0_0",
date = "2024-05-21")
public class Sewer_track extends JAMSComponent {

    /*
     * Component variables
     */
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    description = "Target river reach (entity) of the current sewer. Inflow (inNames) and sewer inflow (saveNames)"+
            "of the reach will be updated (seems to work even though read mode) - pointer")
    public Attribute.Entity to_river;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    description = "List of target reach's inflow attributes (e.g. inRD1 etc.). Those will be updated - pointer")
    public Attribute.String[] inNames;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    description = "List of target reach's sewer inflow attributes (e.g. SewInRD1 etc.). Those will be updated - pointer")
    public Attribute.String[] saveNames;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    description = "Outflow from the Sewer (list per component RD1 etc.). Read and set as input"+
            "into target reach - input",
    unit = "L")
    public Attribute.Double[] outValues;
    

    @Override
    public void init() {
        
    }
    
    @Override
    public void run() throws Attribute.Entity.NoSuchAttributeException {
        
        if (to_river.isEmpty()) {
            return;
        }
        
        int i = 0;
        for (Attribute.Double run_value : outValues) {   
            // add sewer outflow values to reach inflow values
            to_river.setDouble(inNames[i].getValue(), run_value.getValue() + to_river.getDouble(inNames[i].getValue()));
            // add sewer outflow values to reaches tracked sewer volumes
            to_river.setDouble(saveNames[i].getValue(), run_value.getValue() + to_river.getDouble(saveNames[i].getValue()));
            i++;
        } 
    }
}
