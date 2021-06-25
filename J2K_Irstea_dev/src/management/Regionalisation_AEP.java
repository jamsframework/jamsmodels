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

import java.io.*;
import jams.data.*;
import jams.model.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 *
 * @author Sven Kralisch & Mériem Labbas & Christian Fischer
 */
@JAMSComponentDescription(title = "Regionalisation_AEP",
        author = "Francois Tilmant / LC",
        description = "Component used extract the objective function at one reach",
        version = "1.0_0",
        date = "2014-06-04")
public class Regionalisation_AEP extends JAMSComponent {

    /*
     * Component variables
     */
    @JAMSVarDescription (
            access = JAMSVarDescription.AccessType.READ,
            description = "Array of data values for current time step"
    )
    public Attribute.DoubleArray dataArray;
    
    @JAMSVarDescription (
            access = JAMSVarDescription.AccessType.WRITE,
            description = "regionalised data value"
    )
    public Attribute.Double dataValue;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Array of station names"
    )
    public Attribute.DoubleArray names;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "The reach/hru collection"
    )
    public Attribute.EntityCollection entities;
           
       
    
    private Map<Long, Attribute.Entity> entityMap = new HashMap();

    /*
     *  Component run stages
     */
    @Override
    public void init() {
        //put all entities to a map for easier access
        for (Attribute.Entity e : entities.getEntities()) {
            entityMap.put(e.getId(), e);
        }
    }
   
            
    @Override
    public void run() throws IOException {
                
        //Retreiving data
        double value = 0;
        double[] sourceData = dataArray.getValue();
        double[] name = this.names.getValue();
        double entityID = this.entities.getCurrent().getId();
        
        // Number of names to avoid infinite loop
        double n = this.names.getValue().length;
        
        
        // Find if reach in AEP table 
        // !! while loop OK with just a few extraction points, but may need revision with larger/real datasets
        int t = 0;
        while (t < n) {
            if(name[t] == entityID) break;
            t++;
        }
        
        
        //get the current entity form the map
        //Attribute.Entity entity = entityMap.get((long) this.entities.getCurrent().getId());
        Attribute.Entity entity = this.entities.getCurrent();
        
        // If reach in AEP table, associate AEP column with reach
        double isUsed;
        if(t < n) {
            
            isUsed = 1.0;
            value = sourceData[t];
            dataValue.setValue(value);
            
        } else {
            
            isUsed = 0.0;
        }
                
        entity.setDouble("AEP", isUsed);
        
        // check
        //if(entity.getDouble("AEP")==1.0) {
        //    getModel().getRuntime().println("Drinking water in HRU:"+this.entities.getCurrent().getId());
        //} else {
        //    getModel().getRuntime().println("No drinking water in HRU:"+this.entities.getCurrent().getId());
        //}
        
        
    }
}