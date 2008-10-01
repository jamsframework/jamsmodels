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

import jams.JAMSTools;
import jams.data.*;
import jams.model.*;
import jams.io.*;

/**
 *
 * @author Peter Krause
 */
public class FullSetEntityWriter extends JAMSComponent {
    
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
            description = "entity attribute name for weight [attName | none]"
            )
            public JAMSString weight;
    
   
    private GenericDataWriter writer;
    private String[] attrs;
    private boolean headerWritten;
    int nEnts = 0;
    /*
     *  Component runstages
     */
    
    public void init() throws JAMSEntity.NoSuchAttributeException {
        writer = new GenericDataWriter(JAMSTools.CreateAbsoluteFileName(getModel().getWorkspaceDirectory().getPath(),fileName.getValue()));
        
        writer.addComment("J2K model output"+header.getValue());
        
        writer.addComment("");
        
        nEnts = this.entitySet.getEntityArray().length;
        
        
    }
    
    public void run() throws JAMSEntity.NoSuchAttributeException {
        if(!this.headerWritten){
            //always write time
            writer.addColumn("date/time");
            
            JAMSEntityEnumerator enEnum = entitySet.getEntityEnumerator();
            enEnum.reset();
            boolean cont = true;
            
            while(cont){
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
            
            double weightVal = 1.0;
            if(!this.weight.getValue().equals("none")){
                weightVal = (((JAMSDouble)entitySet.getCurrent().getObject(this.weight.getValue())).getValue());
            }
            Object ob = entitySet.getCurrent().getObject(this.attributeName.getValue());
            if(ob.getClass().getName().contains("DoubleArray")){
                //System.out.println("HRUNo: " +((JAMSDouble)entitySet.getCurrent().getObject("ID")).getValue());
                double[] da = ((JAMSDoubleArray)entitySet.getCurrent().getObject(this.attributeName.getValue())).getValue();
                for(int i = 0; i < da.length; i++){
                    double val = da[i] / weightVal;
                    writer.addData(""+val);
                }
            } else{
                //System.out.println("Primitive");
                double da = ((JAMSDouble)entitySet.getCurrent().getObject(this.attributeName.getValue())).getValue();
                double val = da / weightVal;
                System.out.println("Val:" + val + " da: " + da);
                writer.addData(""+val);
            }
            if(setCounter < (nEnts - 1)){
                setCounter++;
            }

            //writer.addData(""+entitySet.getCurrent().getDouble(this.attributeName.getValue()));
            if(ee.hasNext() && (setCounter < nEnts)){
                ee.next();
                cont = true;
            }else
                cont = false;
        }
        
        try {
            writer.writeData();
        } catch (jams.runtime.RuntimeException jre) {
            this.getModel().getRuntime().handle(jre);
        }
    }
    
    public void cleanup() {
        
        writer.close();
    }
}
