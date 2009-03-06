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

import jams.model.*;
import jams.data.*;
import jams.io.*;
import java.io.File;

/**
 *
 * @author S. Kralisch
 */
public class Output extends JAMSComponent {

    @JAMSVarDescription (access = JAMSVarDescription.AccessType.READ)
    public JAMSDouble daylength;

    @JAMSVarDescription (access = JAMSVarDescription.AccessType.READ)
    public JAMSCalendar time;

    @JAMSVarDescription (access = JAMSVarDescription.AccessType.READ)
    public JAMSDouble potET;

    @JAMSVarDescription (access = JAMSVarDescription.AccessType.READ)
    public JAMSDouble snowMelt;

    @JAMSVarDescription (access = JAMSVarDescription.AccessType.READ)
    public JAMSDouble runoff;

    @JAMSVarDescription (access = JAMSVarDescription.AccessType.READ)
    public JAMSString fileName;

    @JAMSVarDescription (access = JAMSVarDescription.AccessType.READ)
    public JAMSDouble soilMoistStor;

    private GenericDataWriter writer;

    public void init() {

        writer = new GenericDataWriter(getModel().getWorkspace().getOutputDataDirectory().getPath() + File.separator + fileName.getValue());

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

    public void run() {

        writer.addData(time);
        writer.addData(daylength);
        writer.addData(potET);
        writer.addData(snowMelt);
        writer.addData(runoff);
        writer.addData(soilMoistStor);
        try {
            writer.writeData();
        } catch (jams.runtime.RuntimeException jre) {
            getModel().getRuntime().handle(jre);
        }

    }

    public void cleanup() {

        writer.close();

    }
}
