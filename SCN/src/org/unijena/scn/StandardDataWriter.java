/*
 * StandardDataWriter.java
 * Created on 21. November 2005, 11:05
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

package org.unijena.scn;

import org.unijena.jams.JAMS;
import org.unijena.jams.data.*;
import org.unijena.jams.model.*;
import org.unijena.jams.io.*;

/**
 *
 * @author S. Kralisch
 */
public class StandardDataWriter extends JAMSComponent {
    
    /*
     *  Component variables
     */
    
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
            public JAMSStringArray headers;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.INIT,
            description = "Output file attributes"
            )
            public JAMSDouble[] value;
    
    
    private GenericDataWriter writer;
    
    /*
     *  Component runstages
     */
    
    public void init() {
        writer = new GenericDataWriter(dirName.getValue()+"/"+fileName.getValue());
        
        writer.addComment("SCN Output");
        
        //always write time
        writer.addColumn("time");
        
        for (int i = 0; i < headers.getValue().length; i++) {
            writer.addColumn(headers.getValue()[i]);
        }
        
        writer.writeHeader();
    }
    
    public void run() throws JAMSEntity.NoSuchAttributeException {
        //always write time
        //the time also knows a toString() method with additional formatting parameters
        //e.g. time.toString("%1$tY-%1$tm-%1$td %1$tH:%1$tM")
        writer.addData(time.toString("%1$tH:%1$tM:%1$tS"));
        
        for (int i = 0; i < value.length; i++) {
            writer.addData(value[i]);
        }
        
        try {
            writer.writeData();
        } catch (org.unijena.jams.runtime.JAMSRuntimeException jre) {
            getModel().getRuntime().println(jre.getMessage());
        }
    }
    
    public void cleanup() {
        writer.close();
    }
}
