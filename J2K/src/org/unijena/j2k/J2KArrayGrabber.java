/*
 * J2KArrayGrabber.java
 * Created on 24. November 2005, 10:52
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

/*
 <component class="org.unijena.j2k.J2KArrayGrabber" name="j2kArrayGrabber">
    <jamsvar name="time" provider="TemporalContext" providervar="current"/>
    <jamsvar name="LAIArray" provider="HRUContext" providervar="currentEntity.LAIArray"/>
    <jamsvar name="effHArray" provider="HRUContext" providervar="currentEntity.effHArray"/>
    <jamsvar name="slAsCfArray" provider="HRUContext" providervar="currentEntity.slAsCfArray"/>
    <jamsvar name="rsc0Array" provider="HRUContext" providervar="currentEntity.rsc0Array"/>
    <jamsvar name="extRadArray" provider="HRUContext" providervar="currentEntity.extRadArray"/>
    <jamsvar name="actLAI" provider="HRUContext" providervar="currentEntity.actLAI"/>
    <jamsvar name="actEffH" provider="HRUContext" providervar="currentEntity.actEffH"/>
    <jamsvar name="actSlAsCf" provider="HRUContext" providervar="currentEntity.actSlAsCf"/>
    <jamsvar name="actRsc0" provider="HRUContext" providervar="currentEntity.actRsc0"/>
    <jamsvar name="actExtRad" provider="HRUContext" providervar="currentEntity.actExtRad"/>
    <jamsvar name="tempRes" value="d"/>
</component>
 */

package org.unijena.j2k;

import org.unijena.jams.data.*;
import org.unijena.jams.model.*;

/**
 *
 * @author Peter Krause
 */
@JAMSComponentDescription(
        title="J2KArrayGrabber",
        author="Peter Krause",
        description=""
        )
        public class J2KArrayGrabber extends JAMSComponent {
    
    /*
     *  Component variables
     */
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.INIT,
            description = "temporal resolution [m | d | h]"
            )
            public JAMSString tempRes;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "time"
            )
            public JAMSCalendar time;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "extraTerrRadiationArray"
            )
            public JAMSDoubleArray extRadArray;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "LeafAreaIndexArray"
            )
            public JAMSDoubleArray LAIArray;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "EffectiveHeightArray"
            )
            public JAMSDoubleArray effHArray;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "rsc0 Array"
            )
            public JAMSDoubleArray rsc0Array;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "slopeAscpectCorrectionFactorArray"
            )
            public JAMSDoubleArray slAsCfArray;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "actExtraTerrRadiation"
            )
            public JAMSDouble actExtRad;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "actLAI"
            )
            public JAMSDouble actLAI;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "actEffH"
            )
            public JAMSDouble actEffH;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "actRsc0"
            )
            public JAMSDouble actRsc0;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "Haude factor array"
            )
            public JAMSDoubleArray haudeFactorArray;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "actHaudeFactor"
            )
            public JAMSDouble actHaudeFactor;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "actSlopeAscpectCorrectionFactor"
            )
            public JAMSDouble actSlAsCf;
    /*
     *  Component run stages
     */
    
    public void init() throws JAMSEntity.NoSuchAttributeException{
        
    }
    
    public void run() throws JAMSEntity.NoSuchAttributeException{
    	
        int monthCount = time.get(time.MONTH);
        int dayCount = time.get(time.DAY_OF_YEAR) - 1;
        int hourCount = time.get(time.HOUR_OF_DAY) + (24 * dayCount);
        
        double in_LAI = -9999;
        double in_effH = -9999;
        double in_extRad = -9999;
        double in_scf = -9999;
        double in_rsc0 = -9999;
        double in_haudeF = -9999;
        
        if(this.rsc0Array != null)
            in_rsc0 = this.rsc0Array.getValue()[monthCount];
        
        if(this.haudeFactorArray != null)
            in_haudeF = this.haudeFactorArray.getValue()[monthCount];
        
        if(this.tempRes.getValue().equals("m")){
            if(this.LAIArray != null)
                in_LAI = this.LAIArray.getValue()[monthCount];
            if(this.effHArray != null)
                in_effH = this.effHArray.getValue()[monthCount];
            if(this.extRadArray != null)
                in_extRad = this.extRadArray.getValue()[monthCount];
            if(this.slAsCfArray != null)
                in_scf = this.slAsCfArray.getValue()[monthCount];
        }
        else if(this.tempRes.getValue().equals("d")){
            if(this.LAIArray != null)
                in_LAI = this.LAIArray.getValue()[dayCount];
            if(this.effHArray != null)
                in_effH = this.effHArray.getValue()[dayCount];
            if(this.extRadArray != null)
                in_extRad = this.extRadArray.getValue()[dayCount];
            if(this.slAsCfArray != null)
                in_scf = this.slAsCfArray.getValue()[dayCount];
        }
        else if(this.tempRes.getValue().equals("h")){
            if(this.LAIArray != null)
                in_LAI = this.LAIArray.getValue()[dayCount];
            if(this.effHArray != null)
                in_effH = this.effHArray.getValue()[dayCount];
            if(this.extRadArray != null)
                in_extRad = this.extRadArray.getValue()[hourCount];
            if(this.slAsCfArray != null)
                in_scf = this.slAsCfArray.getValue()[dayCount];
        }
        this.actLAI.setValue(in_LAI);
        this.actEffH.setValue(in_effH);
        this.actRsc0.setValue(in_rsc0);
        this.actSlAsCf.setValue(in_scf);
        this.actExtRad.setValue(in_extRad);
        this.actHaudeFactor.setValue(in_haudeF);
        
    }
    
    public void cleanup() {
        
    }
    
}
