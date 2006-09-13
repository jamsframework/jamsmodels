/*
 * SelectiveEntityWriter.java
 * Created on 21. March 2006, 11:05
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

package org.unijena.j2k.io;

import org.unijena.jams.data.*;
import org.unijena.jams.model.*;
import org.unijena.jams.io.*;

/**
 *
 * @author S. Kralisch
 */
public class SelectiveEntityWriter extends JAMSComponent {
    
    /*
     *  Component variables
     */
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.INIT,
            description = "EntitySet"
            )
            public JAMSEntityCollection entitySet;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.INIT,
            description = "Data file directory name"
            )
            public JAMSString dirName;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.INIT,
            description = "Output file name"
            )
            public JAMSString fileName;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "Current time"
            )
            public JAMSCalendar time;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.INIT,
            description = "Output file header descriptions"
            )
            public JAMSString header;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.INIT,
            description = "Output file attribute names"
            )
            public JAMSString attributeName;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.INIT,
            description = "Output entities"
            )
            public JAMSIntegerArray eIDs;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.INIT,
            description = "per area"
            )
            public JAMSBoolean perArea;
    
    private GenericDataWriter writer;
    private String[] attrs;
    private boolean headerWritten;
    /*
     *  Component runstages
     */
    
    public void init() throws JAMSEntity.NoSuchAttributeException {
        writer = new GenericDataWriter(dirName.getValue()+"/"+fileName.getValue());
        
        writer.addComment("J2K model output"+header.getValue());
        
        writer.addComment("");
        
        
    }
    
    public void run() throws JAMSEntity.NoSuchAttributeException {
        int[] entSet = this.eIDs.getValue();
        int numEntities = entSet.length;
        
        if(!this.headerWritten){
            //always write time
            writer.addColumn("date/time");
            
            JAMSEntityEnumerator enEnum = entitySet.getEntityEnumerator();
            enEnum.reset();
            boolean cont = true;
            
            while(cont){
                boolean output = false;
                //selecting entities from the eIDs list
                int curID = (int)(int)entitySet.getCurrent().getDouble("ID");
                for(int e = 0; e < numEntities; e++){
                    if(this.eIDs.getValue()[e] == curID){
                        output = true;
                    }
                }
                if(output){
                    Object ob = entitySet.getCurrent().getObject(this.attributeName.getValue());
                    int length = 0;
                    //output variable is of type array
                    if(ob.getClass().getName().contains("DoubleArray")){
                        //System.out.println("JAMSArray");
                        length = ((JAMSDoubleArray)entitySet.getCurrent().getObject(this.attributeName.getValue())).getValue().length;
                        //output variable is a single value
                    } else{
                        length = 0;
                    }
                    for(int i = 0; i < length; i++){
                        writer.addColumn("HRU_"+(int)entitySet.getCurrent().getDouble("ID")+"["+i+"]");
                    }
                    if(length == 0){
                        writer.addColumn("HRU_"+(int)entitySet.getCurrent().getDouble("ID"));
                    }
                    
                }
                if(enEnum.hasNext()){
                    enEnum.next();
                    cont = true;
                }else
                    cont = false;
            }
            
            writer.writeHeader();
            this.headerWritten = true;
        }
        //always write time
        //the time also knows a toString() method with additional formatting parameters
        //e.g. time.toString("%1$tY-%1$tm-%1$td %1$tH:%1$tM")
        writer.addData(time);
        
        JAMSEntityEnumerator ee = entitySet.getEntityEnumerator();
        ee.reset();
        int setCounter = 0;
        boolean cont = true;
        while(cont){
            //selecting entities from the eIDs list
            boolean output = false;
            //selecting entities from the eIDs list
            int curID = (int)(int)entitySet.getCurrent().getDouble("ID");
            for(int e = 0; e < numEntities; e++){
                if(this.eIDs.getValue()[e] == curID){
                    output = true;
                }
            }
            if(output){
                double area = 1.0;
                if(this.perArea.getValue()){
                    area = ((JAMSDouble)entitySet.getCurrent().getObject("area")).getValue();
                }
                Object ob = entitySet.getCurrent().getObject(this.attributeName.getValue());
                if(ob.getClass().getName().contains("DoubleArray")){
                    //System.out.println("HRUNo: " +((JAMSDouble)entitySet.getCurrent().getObject("ID")).getValue());
                    double[] da = ((JAMSDoubleArray)entitySet.getCurrent().getObject(this.attributeName.getValue())).getValue();
                    for(int i = 0; i < da.length; i++){
                        double val = da[i] / area;
                        writer.addData(""+val);
                    }
                } else{
                    //System.out.println("Primitive");
                    double da = ((JAMSDouble)entitySet.getCurrent().getObject(this.attributeName.getValue())).getValue();
                    double val = da / area;
                    writer.addData(""+val);
                }
                if(setCounter < (numEntities - 1)){
                    setCounter++;
                }
            }
            //writer.addData(""+entitySet.getCurrent().getDouble(this.attributeName.getValue()));
            if(ee.hasNext() && (setCounter < numEntities)){
                ee.next();
                cont = true;
            }else
                cont = false;
        }
        
        try {
            writer.writeData();
        } catch (org.unijena.jams.runtime.JAMSRuntimeException jre) {
            System.out.println(jre.getMessage());
        }
    }
    
    public void cleanup() {
        
        writer.close();
    }
}
