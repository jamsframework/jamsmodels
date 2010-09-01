/*
 * InitSoilWaterStates.java
 * Created on 25. November 2005, 13:21
 *
 * This file is part of JAMS
 * Copyright (C) 2005 FSU Jena, c0krpe
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

package org.unijena.j2000g;

import jams.data.*;
import jams.model.*;

/**
 *
 * @author Peter Krause
 */
@JAMSComponentDescription(
        title="TemperatureLapseRate",
        author="Santosh Nepal, Peter Krause",
        description="Regionalisation of Temp through general adiabatic rate"+
        "depends upon given adaiabatic rate"
        )
        public class TemperatureLapseRate extends JAMSComponent {
    
    /*
     *  Component variables
     */
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "station elevation"
            )
            public JAMSDoubleArray statElev;

   @JAMSVarDescription(
   access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "entity elevation"
            )
            public JAMSDouble entityElev;

    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "the measured input from a base station"
            )
            public JAMSDoubleArray inputValue;

    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.WRITE,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "calculated output for the modelling entity"
            )
            public JAMSDouble outputValue;

   @JAMSVarDescription(
   access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.INIT,
            description = "lapse rate per 100 m elevation difference"
            )
            public JAMSDouble lapseRate;

   @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "position array to determine best weights"
            )
            public JAMSIntegerArray statOrder;
        /*
         *  Component run stages
         */

    public void init() throws JAMSEntity.NoSuchAttributeException {

    }
    public void run() throws JAMSEntity.NoSuchAttributeException{
            int closestStation = statOrder.getValue()[0];
            //elevation difference
            double elevationdiff = (statElev.getValue()[closestStation] - entityElev.getValue()) ;
            //temp calculation
            outputValue.setValue(elevationdiff * (lapseRate.getValue()/100.) + inputValue.getValue()[closestStation]);
            

    }

    public void cleanup() {

    }
   
    
    
}
