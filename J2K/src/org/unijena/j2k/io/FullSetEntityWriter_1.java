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
public class FullSetEntityWriter_1 extends JAMSComponent {
    
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
            update = JAMSVarDescription.UpdateType.RUN,
            description = "time interval"
            )
            public JAMSTimeInterval timeInterval;
    
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
    int tsteps = 0;
    double[][] valArray;
    int tcounter = 0;
    String[] dateStr;
    
    /*
     *  Component runstages
     */
    
    public void init() throws JAMSEntity.NoSuchAttributeException {
        writer = new GenericDataWriter(dirName.getValue()+"/"+fileName.getValue());
        
        writer.addComment("J2K model output"+header.getValue());
        
        writer.addComment("");
        
        nEnts = this.entitySet.getEntityArray().length;
        tsteps = (int)this.timeInterval.getNumberOfTimesteps();
        valArray = new double[nEnts][tsteps];
        dateStr = new String[tsteps];
    }
    
    public void run() throws JAMSEntity.NoSuchAttributeException {
       
        dateStr[tcounter] = this.time.toString();
        JAMSEntityEnumerator ee = entitySet.getEntityEnumerator();
        ee.reset();
        
        int setCounter = 0;
        int entCounter = 0;
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
                    this.valArray[entCounter][tcounter] = val;
                    //writer.addData(""+val);
                }
            } else{
                //System.out.println("Primitive");
                double da = ((JAMSDouble)entitySet.getCurrent().getObject(this.attributeName.getValue())).getValue();
                double val = da / weightVal;
                this.valArray[entCounter][tcounter] = val;
                //writer.addData(""+val);
            }
            if(setCounter < (nEnts - 1)){
                setCounter++;
            }

            //writer.addData(""+entitySet.getCurrent().getDouble(this.attributeName.getValue()));
            if(ee.hasNext() && (setCounter < nEnts)){
                ee.next();
                cont = true;
                entCounter++;
            }else
                cont = false;
        }
        this.tcounter++;
        
    }
    
    public void cleanup() throws JAMSEntity.NoSuchAttributeException {
        try{
            //always write time
            writer.addColumn("ID");
            for(int i = 0; i < tcounter; i++){
                writer.addColumn(dateStr[i]);
            }
            writer.writeHeader();
                
            for(int e = 0; e < nEnts; e++){
                writer.addData(entitySet.getEntityArray()[e].getDouble("ID"));
                for(int t = 0; t < tcounter; t++){
                    writer.addData(valArray[e][t]);
                }
                writer.writeData();
            }
            
        } catch (org.unijena.jams.runtime.RuntimeException jre) {
            this.getModel().getRuntime().handle(jre);
        }
        writer.flush();
        writer.close();
    }
}
