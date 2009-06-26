/*
 * J2KProcessLumpedSoilWater.java
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

package org.unijena.j2k.soilWater;

import jams.data.*;
import jams.model.*;

/**
 *
 * @author Peter Krause
 */
@JAMSComponentDescription(
        title="J2KProcessLumpedSoilWater",
        author="Peter Krause",
        description="Calculates soil water balance for each HRU without vertical layers"
        )
        public class InitJ2KProcessLumpedSoilWaterStates extends JAMSComponent {
    
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
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "HRU statevar rooting depth"
            )
            public JAMSDouble rootDepth;
    
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
            description = "HRU state var actual LPS"
            )
            public JAMSDouble actLPS;
   
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "HRU state var saturation of MPS"
            )
            public JAMSDouble satMPS;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "HRU state var saturation of LPS"
            )
            public JAMSDouble satLPS;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "HRU state var saturation of whole soil"
            )
            public JAMSDouble satSoil;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.INIT,
            description = "start saturation of LPS"
            )
            public JAMSDouble satStartLPS;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.INIT,
            description = "start saturation of MPS"
            )
            public JAMSDouble satStartMPS;
    
    
    
    
    /*
     *  Component run stages
     */
    
    public void init() throws Attribute.Entity.NoSuchAttributeException {
        
        
    }
    
    public void run() throws Attribute.Entity.NoSuchAttributeException {
        
        Attribute.Entity entity = entities.getCurrent();
        
        double rootDepth = this.rootDepth.getValue();
        double mxMPS = 0;
        String aNameFC = "fc_";
        for(int d = 0; d < rootDepth; d++){
            int count = d + 1;
            String mpsDesc = aNameFC + count;
            double mpsVal = entity.getDouble(mpsDesc);
            mxMPS = mxMPS + mpsVal;
        }
        mxMPS = mxMPS * this.area.getValue();
        mxMPS = mxMPS * this.FCAdaptation.getValue();
        
        double mxLPS = entity.getDouble("aircap") * area.getValue();
        mxLPS = mxLPS * this.ACAdaptation.getValue();    

        if(satStartLPS != null){
        	this.actLPS.setValue(mxLPS * this.satStartLPS.getValue());
        }
        
        if(satStartMPS != null){
        	this.actMPS.setValue(mxMPS * this.satStartMPS.getValue());
        }
        
        this.maxMPS.setValue(mxMPS);
        this.maxLPS.setValue(mxLPS);
        //this.actMPS.setValue(acMPS);
        //this.actLPS.setValue(acLPS);
        this.satMPS.setValue(this.actMPS.getValue()/mxMPS);
        this.satLPS.setValue(this.actLPS.getValue()/mxLPS);
        this.satSoil.setValue((this.actMPS.getValue()+this.actLPS.getValue()) / (mxMPS+mxLPS));
    }
    
    public void cleanup() {
        
    }
    
    
}
