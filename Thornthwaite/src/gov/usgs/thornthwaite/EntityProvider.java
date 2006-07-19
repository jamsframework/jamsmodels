/*
 * EntityProvider.java
 *
 * Created on 6. Oktober 2005, 19:12
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
package gov.usgs.thornthwaite;

import org.unijena.jams.data.*;
import org.unijena.jams.model.*;
import org.unijena.jams.io.*;
import java.util.*;

/**
 *
 * @author S. Kralisch
 */
public class EntityProvider extends JAMSComponent {
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.INIT
            )
            public JAMSString fileName;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            update = JAMSVarDescription.UpdateType.INIT
            )
            public JAMSEntityCollection entities;
    
    
    
    private JAMSTableDataStore store;
    
    public void init(){
        
        ArrayList <JAMSEntity> entityList = new ArrayList<JAMSEntity>();
        
        store = new GenericDataReader(fileName.getValue(), false, 4, 6);
        
        while (store.hasNext()) {
            
            JAMSEntity e = JAMSDataFactory.createEntity();
            
            JAMSTableDataArray da = store.getNext();
            double[] vals = JAMSTableDataConverter.toDouble(da);
            
            e.setObject("latitude", new JAMSDouble(vals[0]));
            e.setObject("soilMoistStorCap", new JAMSDouble(vals[1]));
            e.setObject("snowStorage", new JAMSDouble(vals[2]));
            e.setObject("runoffFactor", new JAMSDouble(vals[3]));
            e.setObject("prestor", new JAMSDouble(vals[4]));
            e.setObject("remain", new JAMSDouble(vals[5]));
/*            
            e.setDouble("latitude", vals[0]);
            e.setDouble("soilMoistStorCap", vals[1]);
            e.setDouble("snowStorage", vals[2]);
            e.setDouble("runoffFactor", vals[3]);
            e.setDouble("prestor", vals[4]);
            e.setDouble("remain", vals[5]);
*/            
            entityList.add(e);
            
        }
        
        entities.setEntities(entityList);
        
    }
    
    
}
