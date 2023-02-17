package Reservoirs;

/*
 * IrrigationDam_IrrigationDamET.java
 * Created on 15.08.2022, 14:53:00
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
import jams.data.*;
import jams.model.*;

/**
 *
 * @author Andrew Watson <awatson@sun.ac.za>
 */
@JAMSComponentDescription(
        title = "IrrigationDamET",
        author = "Andrew Watson",
        description = "ET loss_surface area",
        date = "2022-15-08",
        version = "1.0_0"
)
@VersionComments(entries = {
    @VersionComments.Entry(version = "1.0_0", comment = "Initial version"),})
public class DamET extends JAMSComponent {

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "Current dam storage",
            defaultValue = "0",
            unit = "L",
            lowerBound = 0,
            upperBound = Double.POSITIVE_INFINITY
    )
    public Attribute.Double damStorage;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "Current dam surface area",
            defaultValue = "0",
            unit = "m2",
            lowerBound = 0,
            upperBound = Double.POSITIVE_INFINITY
    )
    public Attribute.Double damArea;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Potential evaporation",
            defaultValue = "0",
            unit = "mm",
            lowerBound = 0,
            upperBound = Double.POSITIVE_INFINITY
    )
    public Attribute.Double potET;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "Scaling factor for PotET",
            defaultValue = "1",
            unit = "unitless",
            lowerBound = 0,
            upperBound = Double.POSITIVE_INFINITY
    )
    public Attribute.Double facET;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "Surface water evaporation",
            defaultValue = "0",
            unit = "mm",
            lowerBound = 0,
            upperBound = Double.POSITIVE_INFINITY
    )
    public Attribute.Double damET;
    
   
        @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Dam capacity",
            defaultValue = "0",
            unit = "L",
            lowerBound = 0,
            upperBound = Double.POSITIVE_INFINITY
    )
    public Attribute.Double damCapacity;
    
                @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Minimum surface area",
            defaultValue = "0",
            unit = "L",
            lowerBound = 0,
            upperBound = Double.POSITIVE_INFINITY
    )
    public Attribute.Double minArea;
                
                                @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Maximum surface area",
            defaultValue = "0",
            unit = "L",
            lowerBound = 0,
            upperBound = Double.POSITIVE_INFINITY
    )
    public Attribute.Double maxArea;
        

    @Override
    public void init() {
    }

    @Override
    public void run() {

        double damArea = this.damArea.getValue();
        double potET = this.potET.getValue();
        double damCapacity = this.damCapacity.getValue();
        double damET = this.damET.getValue ();
        double damStorage = this.damStorage.getValue ();
        double maxArea= this.maxArea.getValue();
        double minArea= this.minArea.getValue();

        /*
   Conversion of Penman ET to Pan ET (to be used for Pot ET for reservoirs)
   After Allen et al., 1998
   Pot ET 1.05 =Pan ET     
         */
        if (damStorage > 0) {
            
            damET = potET * facET.getValue();
            damArea = (maxArea - minArea)*(damStorage/damCapacity);
           // damET = Math.min(damStorage, damET);
            damStorage = damStorage - damET;
          
        } else {

            damET = 0;
        }

        this.damArea.setValue(damArea);
        this.potET.setValue(potET);
        this.damET.setValue (damET);
        this.damStorage.setValue(damStorage);
    }

    @Override
    public void cleanup() {
    }

}
