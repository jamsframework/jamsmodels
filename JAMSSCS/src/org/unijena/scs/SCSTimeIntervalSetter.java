/*
 * SCSTimeIntervalSetter.java
 * Created on 01. October 2007, 17:15
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
package org.unijena.scs;
import org.unijena.jams.model.*;
import org.unijena.jams.data.*;

/**
 * Creates a model time interval for JAMSSCS. The user has
 * to specify a runtime length in hours and the duration of one time
 * step in seconds. From this information the model's time interval is
 * constructed and can now be used by other components.
 * @author P. Krause
 */
@JAMSComponentDescription(
        title="SCS-Input",
        author="Peter Krause",
        description="Creates a model time interval for JAMSSCS. The user has" +
        "to specify a runtime length in hours and the duration of one time " +
        "step in seconds. From this information the model's time interval is" +
        "constructed and can now be used by other components."
        )
public class SCSTimeIntervalSetter extends JAMSComponent {
    
    /**
     * the model's time interval, constructed in this component<br>
     * access: WRITE<br> 
     * update: INIT<br> 
     * unit: n/a
     */
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.WRITE,
            update = JAMSVarDescription.UpdateType.INIT,
            description = "The model time interval"
            )
            public JAMSTimeInterval modelTimeInterval;
    
    /**
     * the model's expected runtime in hours as first input for TI<br>
     * access: READ<br> 
     * update: INIT<br> 
     * unit: h
     */
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.INIT,
            description = "model duration",
            unit="h"
            )
            public JAMSInteger runtimeHours;
    
    /**
     * the model's expected runtime temporal resolution as second input for TI<br>
     * access: READ<br> 
     * update: INIT<br> 
     * unit: s
     */
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.INIT,
            description = "model runtime resolution",
            unit="s"
            )
            public JAMSInteger runtimeResolution;
    
    /**
     * the component's init() method
     * @throws org.unijena.jams.data.JAMSEntity.NoSuchAttributeException thrown when a model entity tries to access a non existent attribute
     */
    public void init() throws JAMSEntity.NoSuchAttributeException {
        JAMSCalendar start = new JAMSCalendar(1900,1,1,0,0,0);
        JAMSCalendar end = new JAMSCalendar(1900,1,1,0,0,0);
        end.add(JAMSCalendar.HOUR_OF_DAY, this.runtimeHours.getValue());
        
        this.modelTimeInterval.setStart(start);
        this.modelTimeInterval.setEnd(end);
        this.modelTimeInterval.setTimeUnit(13); //these are seconds
        this.modelTimeInterval.setTimeUnitCount(this.runtimeResolution.getValue());
    } 
}
