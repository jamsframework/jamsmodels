/*
 * Climate.java
 * Created on 30. September 2005, 11:37
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

package org.unijena.abc;

import jams.data.JAMSDouble;
import jams.data.JAMSDoubleArray;
import jams.data.JAMSString;
import jams.data.JAMSStringArray;
import jams.io.GenericDataReader;
import jams.io.JAMSTableDataArray;
import jams.io.JAMSTableDataConverter;
import jams.io.JAMSTableDataStore;
import jams.model.JAMSComponent;
import jams.model.JAMSVarDescription;

/**
 *
 * @author S. Kralisch
 */
public class Climate extends JAMSComponent {
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.INIT
            )
            public JAMSString fileName;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            update = JAMSVarDescription.UpdateType.RUN
            )
            public JAMSDouble precip;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            update = JAMSVarDescription.UpdateType.RUN
            )
            public JAMSDouble temperature;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            update = JAMSVarDescription.UpdateType.RUN
            )
            public JAMSDouble obsRunoff;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            update = JAMSVarDescription.UpdateType.RUN
            )
            public JAMSDouble compRD1;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            update = JAMSVarDescription.UpdateType.RUN
            )
            public JAMSDouble compRD2;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            update = JAMSVarDescription.UpdateType.RUN
            )
            public JAMSDouble compRG1;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            update = JAMSVarDescription.UpdateType.RUN
            )
            public JAMSDouble compRG2;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            update = JAMSVarDescription.UpdateType.RUN
            )
            public JAMSDouble compQdir;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            update = JAMSVarDescription.UpdateType.RUN
            )
            public JAMSDouble compQbas;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            update = JAMSVarDescription.UpdateType.RUN
            )
            public JAMSDoubleArray data;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            update = JAMSVarDescription.UpdateType.RUN
            )
            public JAMSStringArray dataNames;
    
    
    
    
    private JAMSTableDataStore store;
    public java.util.HashMap dataMap = new java.util.HashMap();
    private String[] md;
    public void init(){
        
        store = new GenericDataReader(fileName.getValue(), true, 1, 2);
        md = store.getMetadata();
        this.dataNames.setValue(md);
        for(int i = 0; i < md.length; i++)
            System.out.println("metadata["+i+"]: "+md[i]);
    }
    
    public void run(){
        
        JAMSTableDataArray da = store.getNext();
        double[] vals = JAMSTableDataConverter.toDouble(da);
        this.data.setValue(vals);
        
        for(int i = 0; i < md.length; i++)
            dataMap.put(md[i], vals[i]);
        
        this.precip.setValue(((Double)dataMap.get("precip")).doubleValue());//vals[0]);
        this.temperature.setValue(((Double)dataMap.get("tmean")).doubleValue());
        this.obsRunoff.setValue(((Double)dataMap.get("obsRO")).doubleValue());
        this.compRD1.setValue(((Double)dataMap.get("RD1")).doubleValue());
        this.compRD2.setValue(((Double)dataMap.get("RD2")).doubleValue());
        this.compRG1.setValue(((Double)dataMap.get("RG1")).doubleValue());
        this.compRG2.setValue(((Double)dataMap.get("RG2")).doubleValue());
        this.compQdir.setValue(((Double)dataMap.get("QD")).doubleValue());
        this.compQbas.setValue(((Double)dataMap.get("QB")).doubleValue());

    }
    
    public void cleanup(){
        store.close();
    }
    
}
