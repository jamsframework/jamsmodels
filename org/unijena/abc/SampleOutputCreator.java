/*
 * Output.java
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

import org.unijena.jams.model.*;
import org.unijena.jams.data.*;
import org.unijena.jams.io.*;

/**
 *
 * @author S. Kralisch
 */
public class SampleOutputCreator extends JAMSComponent {
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.RUN
            )
            public JAMSDouble a;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.RUN
            )
            public JAMSDouble b;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.RUN
            )
            public JAMSDouble c;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.RUN
            )
            public JAMSDouble e2;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.INIT
            )
            public JAMSString fileName;
    
    
    private GenericDataWriter writer;
    
    public void init(){
        
        writer = new GenericDataWriter(fileName.getValue());

        writer.addComment("abc model sampling output");
        writer.addComment("");
        writer.addColumn("a");
        writer.addColumn("b");
        writer.addColumn("c");
        writer.addColumn("e2");
        writer.writeHeader();
        
    }
    
    
    public void run(){
        
        writer.addData(a.getValue());
        writer.addData(b.getValue());
        writer.addData(c.getValue());
        writer.addData(e2.getValue());
        try {
        writer.writeData();
        } catch (org.unijena.jams.runtime.JAMSRuntimeException jre) {
            System.out.println(jre.getMessage());
        }
        
    }
    
    public void cleanup(){
        
        writer.close();

    }
    
}
