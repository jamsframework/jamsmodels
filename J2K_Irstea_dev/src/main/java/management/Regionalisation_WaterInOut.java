/*
 * SewerOverflowDevice.java
 * Created on 05. October 2012, 17:02
 *
 * This file is part of JAMS
 * Copyright (C) FSU Jena
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
package management;

import jams.JAMS;
import java.io.*;
import jams.data.*;
import jams.model.*;


/**
 *
 * @author Sven Kralisch & Mériem Labbas & Christian Fischer
 */
@JAMSComponentDescription(title = "Regionalisation_WaterInOut",
        author = "Francois Tilmant, Nico Hachgenei",
        description = "Component used to select reach to extract / inject the objective function"
        + " modified after Regionalisation_Dam",
        version = "1.0_0",
        date = "2024-05-30")
public class Regionalisation_WaterInOut extends JAMSComponent {

    /*
     * Component variables
     */
    @JAMSVarDescription (access = JAMSVarDescription.AccessType.READ,
                         description = "Array of data values for current time step (volumes to add to or substract from"+
                                 "reach for each concerned reach) - input")
    public Attribute.DoubleArray dataArray;
    
    @JAMSVarDescription (access = JAMSVarDescription.AccessType.WRITE,
                         description = "Regionalised data value (volume to inject into / extract from current reach)."+
                                 "This should be the attribute of the ReachLoop to set this value to. - output")
    public Attribute.Double dataValue;
    
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
            description = "Array of reach names (IDs) for which volumes are added / substracted, corresponding to dataArray. - pointer")
    public Attribute.DoubleArray names;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "All reaches - pointer"
            )
    public Attribute.EntityCollection entities;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "does this reach have Input / Output? - property of the reach"
            )
    public Attribute.Double IO;
  
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "ID of the current reach - property of the reach"
            )
    public Attribute.Double ID;
    
    //boolean invalidDatasetReported = false;
    //ArrayPool<double[]> memPool = new ArrayPool<double[]>(double.class);
   
    
   
            
    @Override
    public void run() throws IOException {
        //Retreiving data, elevations and weights
        double run_value;
        double[] run_sourceData = dataArray.getValue();
        //Attribute.Entity run_entity = entities.getCurrent();
        //double run_Smax = run_entity.getDouble("Smax");
        double[] run_names = this.names.getValue();
        if (this.IO.getValue() == 1) {
            double run_reach = this.ID.getValue();
            int run_t = 0;
            while (run_names[run_t] != run_reach) {
                //getModel().getRuntime().println("no match -- reach: " + run_reach + ", name: " + run_names[t]);
                run_t++;
            }
            //getModel().getRuntime().println("+++ match -- reach: " + run_reach + ", name: " + run_names[t] + ", value: " + run_sourceData[t]);
            run_value = run_sourceData[run_t];
            dataValue.setValue(run_value);
        }
    }
}