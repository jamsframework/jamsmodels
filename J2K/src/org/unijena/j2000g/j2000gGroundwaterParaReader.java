/*
 * j2000gGroundwaterParaReader.java
 * Created on 10. November 2007, 10:53
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

package org.unijena.j2000g;

import org.unijena.j2k.*;
import org.unijena.jams.JAMS;
import org.unijena.jams.data.*;
import org.unijena.jams.model.*;
import java.util.*;
import java.io.*;

/**
 *
 * @author P. Krause
 */
public class j2000gGroundwaterParaReader extends JAMSComponent {
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.INIT,
            description = "Data file directory name"
            )
            public JAMSString dirName;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.INIT,
            description = "Hydrogeology parameter file name"
            )
            public JAMSString gwFileName;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "Collection of hru objects"
            )
            public JAMSEntityCollection hrus;
    
    
    
    public void init() throws JAMSEntity.NoSuchAttributeException {
        
        //read gw parameter
        JAMSEntityCollection gwTypes = new JAMSEntityCollection();
        gwTypes.setEntities(J2KFunctions.readParas(dirName.getValue()+"/"+gwFileName.getValue(), getModel()));
        
        HashMap<Double, JAMSEntity> gwMap = new HashMap<Double, JAMSEntity>();
        JAMSEntity gw, e;
        Object[] attrs;
        
        //put all entities into a HashMap with their ID as key
        Iterator<JAMSEntity> gwIterator = gwTypes.getEntities().iterator();
        while (gwIterator.hasNext()) {
            gw = gwIterator.next();
            gwMap.put(gw.getDouble("GID"),  gw);
        }
        
        Iterator<JAMSEntity> hruIterator = hrus.getEntities().iterator();
        while (hruIterator.hasNext()) {
            e = hruIterator.next();
            //System.out.println("proc hru " + e.getDouble("ID"));
            gw = gwMap.get(e.getDouble("hgeoID"));
            e.setObject("hgeoType", gw);
            
            attrs = gw.getKeys();
            
            for (int i = 0; i < attrs.length; i++) {
                //e.setDouble((String) attrs[i], lu.getDouble((String) attrs[i]));
                Object o = gw.getObject((String)attrs[i]);
                if(!(o instanceof JAMSString))
                    e.setObject((String)attrs[i], o);
            }
        }
        getModel().getRuntime().println("Groundwater parameter file processed ...", JAMS.VERBOSE);
    }
}
