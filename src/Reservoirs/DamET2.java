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
public class DamET2 extends JAMSComponent {

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
            access = JAMSVarDescription.AccessType.WRITE,
            description = "Potential evaporation",
            defaultValue = "0",
            unit = "m",
            lowerBound = 0,
            upperBound = Double.POSITIVE_INFINITY
    )
    public Attribute.Double potETm;

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
            access = JAMSVarDescription.AccessType.WRITE,
            description = "Surface water evaporation",
            defaultValue = "0",
            unit = "l",
            lowerBound = 0,
            upperBound = Double.POSITIVE_INFINITY
    )
    public Attribute.Double damETl;
    
        @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Dam capacity",
            defaultValue = "0",
            unit = "L",
            lowerBound = 0,
            upperBound = Double.POSITIVE_INFINITY
    )
    public Attribute.Double damCapacity;

    @Override
    public void init() {
    }

    @Override
    public void run() {

        double damArea = this.damArea.getValue();
        double potET = this.potET.getValue();
        double potETm3;
        double damET;
        double damCapacity = this.damCapacity.getValue();
        double damETl = this.damETl.getValue ();
        double damStorage = this.damStorage.getValue ();

        /*
   Conversion of Penman ET to Pan ET (to be used for Pot ET for reservoirs)
   After Allen et al., 1998
   Pot ET 1.05 =Pan ET     
         */
        if (damStorage > 0) {
            
            potET = potET * facET.getValue();
            damArea = (0.0002 * damStorage) + 7455.4;
            potETm3 = potET / 1000;
            damET = potETm3 / damArea;
            damETl = damET * 1000;
            damETl = Math.min(damStorage, damETl);
            damStorage = damStorage - damETl;
            

        } else {

            damET = 0;
            potETm3 = 0;

        }

        this.damArea.setValue(damArea);
        this.potET.setValue(potET);
        this.potETm.setValue(potETm3);
        this.damET.setValue(damET);
        this.damETl.setValue (damETl);
        this.damStorage.setValue(damStorage);
    }

    @Override
    public void cleanup() {
    }

}
