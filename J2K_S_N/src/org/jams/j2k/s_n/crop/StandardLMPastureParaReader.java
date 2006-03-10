/*
 * StandardLMPastureParaReader.java
 * Created on 10. Dezember 2005, 13:08
 *
 * This file is part of JAMS
 * Copyright (C) 2005 FSU Jena, c5ulbe
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

package org.jams.j2k.s_n.crop;


import org.unijena.jams.data.*;
import org.unijena.jams.model.*;
import java.util.*;
import org.unijena.j2k.J2KFunctions;

/**
 *
 * @author S. Kralisch file edited by U. Bende-Michl
 */
public class StandardLMPastureParaReader extends JAMSComponent {
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.INIT,
            description = "Data file directory name"
            )
            public JAMSString dirName;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.INIT,
            description = "Pasture management parameter file name"
            )
            public JAMSString lpFileName;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.INIT,
            description = "Collection of hru objects"
            )
            public JAMSEntityCollection hrus;
    
    
    
    public void init() throws JAMSEntity.NoSuchAttributeException {
        
        //read pasture management parameter
        JAMSEntityCollection lmpas = new JAMSEntityCollection();
        lmpas.setEntities(J2KFunctions.readParas(dirName.getValue()+"/"+lpFileName.getValue()));
        
        HashMap<Double, JAMSEntity> lpMap = new HashMap<Double, JAMSEntity>();
        JAMSEntity lp, e;
        Object[] attrs;
        
        //put all entities into a HashMap with their ID as key
        
        Iterator<JAMSEntity> lpIterator = lmpas.getEntities().iterator();
        while (lpIterator.hasNext()) {
            lp = lpIterator.next();
            lpMap.put(lp.getDouble("ID"),  lp);//put all entities into a HashMap with their ID as key
                            }
        
        Iterator<JAMSEntity> hruIterator = hrus.getEntities().iterator();
        while (hruIterator.hasNext()) {
            e = hruIterator.next();
            lp = lpMap.get(e.getDouble("lmpasID"));
            e.setObject("lmPas", lp);
            
            attrs = lp.getKeys();
            for (int i = 0; i < attrs.length; i++) {
                e.setDouble((String) attrs[i], lp.getDouble((String) attrs[i]));
            }
            
        }
    }
      
}

