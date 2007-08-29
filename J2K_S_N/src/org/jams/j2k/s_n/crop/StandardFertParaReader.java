/*
 * StandardFertParaReader.java
 * used to describe fertilization options
 * Created on 9. Dezember 2005, 15:37
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
public class StandardFertParaReader extends JAMSComponent {
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.INIT,
            description = "Data file directory name"
            )
            public JAMSString dirName;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.INIT,
            description = "Fertilizer parameter file name"
            )
            public JAMSString ftFileName;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.INIT,
            description = "Collection of hru objects"
            )
            public JAMSEntityCollection hrus;
    
    
    
    public void init() throws JAMSEntity.NoSuchAttributeException {
        
        //read fertilizer parameter
        JAMSEntityCollection fert = new JAMSEntityCollection();
        fert.setEntities(J2KFunctions.readParas(dirName.getValue()+"/"+ftFileName.getValue(), getModel()));
        
        HashMap<Double, JAMSEntity> ftMap = new HashMap<Double, JAMSEntity>();
        JAMSEntity ft, e;
        Object[] attrs;
        
        //put all entities into a HashMap with their ID as key
        
        Iterator<JAMSEntity> ftIterator = fert.getEntities().iterator();
        while (ftIterator.hasNext()) {
            ft = ftIterator.next();
            ftMap.put(ft.getDouble("ID"),  ft);//put all entities into a HashMap with their ID as key
                            }
        
        Iterator<JAMSEntity> hruIterator = hrus.getEntities().iterator();
        while (hruIterator.hasNext()) {
            e = hruIterator.next();
            ft = ftMap.get(e.getDouble("fertID"));
            e.setObject("fert", ft);
            
            attrs = ft.getKeys();
            for (int i = 0; i < attrs.length; i++) {
                e.setDouble((String) attrs[i], ft.getDouble((String) attrs[i]));
            }
            
        }
    }
      
}

