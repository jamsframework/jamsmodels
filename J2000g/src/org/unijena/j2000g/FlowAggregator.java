/*
 * J2KProcessReachRouting.java
 * Created on 28. November 2005, 10:01
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
 * @author c0krpe
 */
@JAMSComponentDescription(
        title="Title",
        author="Author",
        description="Description"
        )
        public class FlowAggregator extends JAMSComponent {
    
    /*
     *  Component variables
     */
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "catchment area"
            )
            public JAMSDouble cArea;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "direct runoff"
            )
            public JAMSDouble dirQ;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "interflow"
            )
            public JAMSDouble interflow;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "base flow"
            )
            public JAMSDouble basQ;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "direct runoff cbm"
            )
            public JAMSDouble dirQcbm;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "interflow runoff cbm"
            )
            public JAMSDouble infQcbm;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "baseflow cbm"
            )
            public JAMSDouble basQcbm;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "total outflow cbm"
            )
            public JAMSDouble totQcbm;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "total outflow mm"
            )
            public JAMSDouble totQmm;
    
    /*
     *  Component run stages
     */
    
    public void init() throws JAMSEntity.NoSuchAttributeException {
        
    }
    
    public void run() throws JAMSEntity.NoSuchAttributeException {
        
        double totOut = this.dirQ.getValue() + this.interflow.getValue() + this.basQ.getValue();
        
        this.totQmm.setValue(totOut);
        //conversion from mm to m^3/time
        totOut = (totOut * cArea.getValue()) / (86400 * 1000);
        
        this.totQcbm.setValue(totOut);
        this.dirQcbm.setValue((dirQ.getValue() * cArea.getValue()) / (86400 * 1000));
        this.infQcbm.setValue((interflow.getValue() * cArea.getValue()) / (86400 * 1000));
        this.basQcbm.setValue((basQ.getValue() * cArea.getValue()) / (86400 * 1000));
    }
    
    public void cleanup() {
        
    }
    
    
}
