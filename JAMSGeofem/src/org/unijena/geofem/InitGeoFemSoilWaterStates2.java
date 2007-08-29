/*
 * InitGeoFemSoilWaterStates2.java
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

package org.unijena.geofem;

import org.unijena.jams.data.*;
import org.unijena.jams.model.*;

/**
 *
 * @author Peter Krause
 */
@JAMSComponentDescription(
        title="InitGeoFemSoilWaterStates",
        author="Peter Krause",
        description="Defines soil water attributes for each HRU"
        )
        public class InitGeoFemSoilWaterStates2 extends JAMSComponent {
    
    /*
     *  Component variables
     */
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "The current hru entity"
            )
            public JAMSEntityCollection entities;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "attribute area"
            )
            public JAMSDouble area;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.INIT,
            description = "field capacity adaptation factor"
            )
            public JAMSDouble FCAdaptation;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.INIT,
            description = "air capacity adaptation factor"
            )
            public JAMSDouble ACAdaptation;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "HRU attribute maximum MPS"
            )
            public JAMSDouble maxMPS;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "HRU attribute maximum LPS"
            )
            public JAMSDouble maxLPS;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "HRU state var actual MPS"
            )
            public JAMSDouble actMPS;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "HRU state var actual MPS"
            )
            public JAMSDouble actLPS;
    
    /*
     *  Component run stages
     */
    
    public void init() throws JAMSEntity.NoSuchAttributeException {
        
        
    }
    
    public void run() throws JAMSEntity.NoSuchAttributeException {
        
        JAMSEntity entity = entities.getCurrent();
        
        /*double rootDepth = this.rootDepth.getValue();
        double mxMPS = 0;
        String aNameFC = "fc_";
        for(int d = 0; d < rootDepth; d++){
            int count = d + 1;
            String mpsDesc = aNameFC + count;
            double mpsVal = entity.getDouble(mpsDesc);
            mxMPS = mxMPS + mpsVal;
        }*/
        double mxMPS = entity.getDouble("fc_sum");
        double mxLPS = entity.getDouble("aircap");
        mxMPS = mxMPS * this.area.getValue();
        mxLPS = mxLPS * this.area.getValue();
        
        mxMPS = mxMPS * this.FCAdaptation.getValue();
        mxLPS = mxLPS * this.ACAdaptation.getValue();
        this.maxMPS.setValue(mxMPS);
        this.maxLPS.setValue(mxLPS);
        
    }
    
    public void cleanup() {
        
    }
    
    
}
