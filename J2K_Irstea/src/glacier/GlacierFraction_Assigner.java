/*
 * GlacierFraction_Assigner.java
 * Created on 08.04.2020, 11:55:01
 *
 * This file is part of JAMS
 * Copyright (C) FSU Jena
 *
 * JAMS is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * JAMS is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with JAMS. If not, see <http://www.gnu.org/licenses/>.
 *
 */
package glacier;

import jams.data.*;
import jams.model.*;
import java.util.HashMap;
import java.util.Map;


/**
 *
 * @author Sven Kralisch <sven.kralisch@uni-jena.de>
 */
@JAMSComponentDescription(
        title = "GlacierFraction_Assigner",
        author = "Sven Kralisch; updated by Jordi Bolibar",
        description = "Assign glacier fractions to model entities",
        date = "2020-04-08",
        version = "1.0_0"
)
@VersionComments(entries = {
    @VersionComments.Entry(version = "1.0_0", date = "2016-04-14", comment = "Initial version")
})
public class GlacierFraction_Assigner extends JAMSComponent {

    /*
     *  Component attributes
     */
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Glacier data of current time step"
    )
    public Attribute.DoubleArray glacierFractionArray;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Array of HRU IDs from GlacierFraction file"
    )
    public Attribute.IntegerArray hruIDArray;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "HRU ID"
    )
    public Attribute.Double hruID;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "Glacier fraction"
    )
    public Attribute.Double glacierFraction;

    Map<Integer, Integer> hru2idMap;
    
    @JAMSVarDescription(
        access = JAMSVarDescription.AccessType.READ,
        description = "attribute area",
        unit="m^2"
    )
    public Attribute.Double area;
    
    @JAMSVarDescription(
        access = JAMSVarDescription.AccessType.WRITE,
        description = "glacierized area",
        unit="m^2"
    )
    public Attribute.Double glacierArea;
    
    @JAMSVarDescription(
        access = JAMSVarDescription.AccessType.WRITE,
        description = "non glacierized area",
        unit="m^2"
    )
    public Attribute.Double noGlacierArea;
    
    @JAMSVarDescription(
        access = JAMSVarDescription.AccessType.WRITE,
        description = "the relative area weight of the glacierized fraction entity",
        unit = "n/a"
        )
    public Attribute.Double glacierAreaWeight;
    
    @JAMSVarDescription(
        access = JAMSVarDescription.AccessType.WRITE,
        description = "the relative area weight of the non glacierized fraction entity",
        unit = "n/a"
        )
    public Attribute.Double noGlacierAreaWeight;
    
    @JAMSVarDescription(
        access = JAMSVarDescription.AccessType.READ,
        description = "the area of the catchment",
        unit = "m^2"
        )
    public Attribute.Double catchmentArea;  
    
    @JAMSVarDescription(
        access = JAMSVarDescription.AccessType.WRITE,
        description = "flag to determine if the current HRU is glacierized or not"
        )
    public Attribute.Double glacierized;  
    
    
    /*
     *  Component run stages
     */
    @Override
    public void init() {

        // create a mapping HRU ID -> array position
        hru2idMap = new HashMap();
        int[] hruID = hruIDArray.getValue();
        for (int i = 0; i < hruID.length; i++) {
            hru2idMap.put(hruID[i], i);
        }

    }

    @Override
    public void run() {

        double fraction = 0;
        
        // get glacier fraction if HRU ID is listed
        int pos = hru2idMap.getOrDefault((int) hruID.getValue(), -1);
        if (pos > -1) {
          fraction = glacierFractionArray.getValue()[pos];
        }

        glacierFraction.setValue(fraction);
        
        if(glacierFraction.getValue() > 0){
           glacierized.setValue(1);
        }
        else{
            glacierized.setValue(0);
        }
        
        getModel().getRuntime().println("HRU ID: " + hruID.getValue());
        getModel().getRuntime().println("glacierFraction: " + glacierFraction.getValue());
        
        // Set the glacierized and non glacierized areas
        glacierArea.setValue(area.getValue()*glacierFraction.getValue());
        noGlacierArea.setValue(area.getValue()*(1-glacierFraction.getValue()));
        
        // Update the area weight
        glacierAreaWeight.setValue(catchmentArea.getValue() / glacierArea.getValue());
        noGlacierAreaWeight.setValue(catchmentArea.getValue() / noGlacierArea.getValue());

    }

}
