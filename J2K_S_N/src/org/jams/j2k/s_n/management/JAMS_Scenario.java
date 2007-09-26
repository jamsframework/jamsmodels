/*
 * JAMS_Scenario.java
 * Created on 14. August 2007, 15:28
 *
 * This file is part of JAMS
 * Copyright (C) 2005 FSU Jena, c8fima
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

package org.jams.j2k.s_n.management;

import org.unijena.jams.data.*;
import org.unijena.jams.model.*;

/**
 *
 * @author c8fima
 */
@JAMSComponentDescription(
title="JAMS_Scenario",
        author="Manfred Fink",
        description="Sets Parameter for Scenario calulation in dependence of the calendar"
        )
        public class JAMS_Scenario extends JAMSComponent {
    
    /*
     *  Component variables
     */
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.INIT,
            description = "Date to start the scenario"
            )
            public JAMSCalendar start;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.INIT,
            description = "Date to end the scenario"
            )
            public JAMSCalendar end;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.INIT,
            description = "Normal Value"
            )
            public JAMSDouble StandardValue;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.INIT,
            description = "Scenario Value"
            )
            public JAMSDouble ScenarioValue;
    
     @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            update = JAMSVarDescription.UpdateType.INIT,
            description = "Output Value"
            )
            public JAMSDouble OutputValue;
     
      @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.INIT,
            description = "Encapsulating time interval"
            )
            public JAMSTimeInterval timeInterval;
      
      @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "Current time"
            )
            public JAMSCalendar time;
    
    
    /*
     *  Component run stages
     */
    
     private JAMSTimeInterval ti;
     
    public void init() {
     ti = new JAMSTimeInterval(start, end, timeInterval.getTimeUnit(), timeInterval.getTimeUnitCount());   
    }
    
    public void run() {
     double value = 0;
        if (time.after(ti.getStart()) && time.before(ti.getEnd())) {
        
         value = ScenarioValue.getValue();           
        } else {
         value = StandardValue.getValue();  
        }
     
     OutputValue.setValue(value);
     
        
    }
    
    public void cleanup() {
        
    }
}
