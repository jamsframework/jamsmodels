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
@JAMSComponentDescription(title = "Regionalisation_Dam",
        author = "Francois Tilmant",
        description = "Component used extract the objective function at one reach",
        version = "1.0_0",
        date = "2014-06-04")
public class Regionalisation_Dam extends JAMSComponent {

    /*
     * Component variables
     */
    @JAMSVarDescription (access = JAMSVarDescription.AccessType.READ,
                         description = "Array of data values for current time step")
    public Attribute.DoubleArray in_data_array;
    
    @JAMSVarDescription (access = JAMSVarDescription.AccessType.WRITE,
                         description = "regionalised data value")
    public Attribute.Double out_data_value;
    
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
            description = "Array of station names")
    public Attribute.DoubleArray in_names;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "The reach collection"
            )
    public Attribute.EntityCollection st_entities;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Maximum storage of the reservoir",
            unit = "Mm3"
            )
    public Attribute.Double st_smax;
  
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "reach ID"
            )
    public Attribute.Double in_id;
    
    boolean run_invalid_dataset_reported = false;
    ArrayPool<double[]> run_mem_pool = new ArrayPool<double[]>(double.class);
   
    
   
            
    @Override
    public void run() throws IOException {
        //Retreiving data, elevations and weights
        double run_value=0;
        double[] run_source_data = in_data_array.getValue();
        //Attribute.Entity entity = entities.getCurrent();
        //double Smax = entity.getDouble("Smax");
        double[] run_nom = this.in_names.getValue();
        if (this.st_smax.getValue() > 0) {
            double run_reach = this.in_id.getValue();
            int run_t = 0;
            while (run_nom[run_t] != run_reach) {
                run_t++;
            }
            run_value = run_source_data[run_t];
            out_data_value.setValue(run_value);
        }
    }
}