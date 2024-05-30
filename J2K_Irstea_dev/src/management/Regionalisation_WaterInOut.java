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
@JAMSComponentDescription(title = "Regionalisation_WaterInOutRegionalisation_Dam",
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
                         description = "Array of data values for current time step")
    public Attribute.DoubleArray dataArray;
    
    @JAMSVarDescription (access = JAMSVarDescription.AccessType.WRITE,
                         description = "regionalised data value (volume to inject / extract")
    public Attribute.Double dataValue;
    
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
            description = "Array of reach names (IDs")
    public Attribute.DoubleArray names;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "The reach collection"
            )
    public Attribute.EntityCollection entities;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "does this reach have Input / Output?"
            )
    public Attribute.Double IO;
  
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "reach ID"
            )
    public Attribute.Double ID;
    
    boolean invalidDatasetReported = false;
    ArrayPool<double[]> memPool = new ArrayPool<double[]>(double.class);
   
    
   
            
    @Override
    public void run() throws IOException {
        //Retreiving data, elevations and weights
        double value=0;
        double[] sourceData = dataArray.getValue();
        //Attribute.Entity entity = entities.getCurrent();
        //double Smax = entity.getDouble("Smax");
        double[] Nom = this.names.getValue();
        if (this.IO.getValue() == 1) {
            double reach = this.ID.getValue();
            int t = 0;
            while (Nom[t] != reach) {
                //getModel().getRuntime().println("no match -- reach: " + reach + ", name: " + Nom[t]);
                t++;
            }
            //getModel().getRuntime().println("+++ match -- reach: " + reach + ", name: " + Nom[t] + ", value: " + sourceData[t]);
            value = sourceData[t];
            dataValue.setValue(value);
        }
    }
}