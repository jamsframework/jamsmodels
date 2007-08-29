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

package gov.usgs.thornthwaite;

import org.unijena.jams.model.*;
import org.unijena.jams.data.*;
import org.unijena.jams.io.*;

/**
 *
 * @author S. Kralisch
 */
public class Output extends JAMSComponent {
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.RUN
            )
            public JAMSDouble daylength;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.RUN
            )
            public JAMSCalendar time;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.RUN
            )
            public JAMSDouble potET;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.RUN
            )
            public JAMSDouble snowMelt;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.RUN
            )
            public JAMSDouble runoff;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.INIT
            )
            public JAMSString fileName;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.RUN
            )
            public JAMSDouble soilMoistStor;
    
    
    
    
    private GenericDataWriter writer;
    
    public void init(){
        
        writer = new GenericDataWriter(fileName.getValue());

        writer.addComment("Thornthwaite model output");
        writer.addComment("");
        writer.addColumn("time");
        writer.addColumn("daylength");
        writer.addColumn("potet");
        writer.addColumn("snowMelt");
        writer.addColumn("runoff");
        writer.addColumn("soilMoistStor");
        writer.writeHeader();
        
    }
    
    
    public void run(){
        
        writer.addData(time);
        writer.addData(daylength);
        writer.addData(potET);
        writer.addData(snowMelt);
        writer.addData(runoff);
        writer.addData(soilMoistStor);
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
