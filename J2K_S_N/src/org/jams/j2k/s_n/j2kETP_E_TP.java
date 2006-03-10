/*
 * j2kETP_E_TP.java
 * Created on 25. November 2005, 16:54
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

package org.jams.j2k.s_n;

import org.unijena.jams.data.*;
import org.unijena.jams.model.*;

/**
 *
 * @author Manfred Fink
 */
@JAMSComponentDescription(
        title="j2kETP_E_TP",
        author="Manfred Fink",
        description="Module for the calculation of seperate evaportion and transpiratoin from the actual evapotranspiration very simple Method in SWAT"
        )
        public class j2kETP_E_TP extends JAMSComponent {
    
    
    
    
    /*
     *  Component variables
     */
    
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "Array of state variables LAI "
            )
            public JAMSDoubleArray LAIArray = new JAMSDoubleArray();
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "HRU actual Evapotranspiration in mm"
            )
            public JAMSDouble aETP;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "HRU actual Evaporation in mm"
            )
            public JAMSDouble aEP;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "HRU actual Transpiration in mm"
            )
            public JAMSDouble aTP;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "HRU potential Evapotranspiration in mm"
            )
            public JAMSDouble pETP;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "HRU potential Evaporation in mm"
            )
            public JAMSDouble pEP;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "HRU potential Transpiration in mm"
            )
            public JAMSDouble pTP;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "time"
            )
            public JAMSCalendar time;
    /*
     *  Component run stages
     */
    
    public void init() {
        
    }
    
    public void run() throws JAMSEntity.NoSuchAttributeException{
        int day = time.get(time.DAY_OF_YEAR) - 1;
        double runpETP = pETP.getValue(); /*potential evapotranspiration in mm*/
        double runaETP = aETP.getValue(); /*actual evapotranspiration in mm*/
        double aTransp = 0; /*actual transpiration in mm*/
        double aEvap = 0; /*actual evaporation in mm*/
        double pTransp = 0; /*potential transpiration in mm*/
        double pEvap = 0; /*potential evaporation in mm*/
        double LAI = this.LAIArray.getValue()[day]; /*Leaf area index*/
        
        if (LAI <= 3){
            aTransp = (runaETP * LAI) / 3;
            pTransp = (runpETP * LAI) / 3;
        } else if (LAI > 3){
            aTransp = runaETP;
            pTransp = runpETP;
        }
        aEvap = runaETP - aTransp;
        pEvap = runpETP - pTransp;
        
        
        aEP.setValue(aEvap);
        aTP.setValue(aTransp);
        pEP.setValue(pEvap);
        pTP.setValue(pTransp);
    }
    
    public void cleanup() {
        
    }
}

/*
 
			<component class="org.jams.j2k.s_n.j2kETP_E_TP" name="j2kETP_E_TP">
				<jamsvar name="time" provider="TemporalContext" providervar="current"/>
			    <jamsvar name="aETP" provider="HRUContext" providervar="currentEntity.actETP"/>
				<jamsvar name="LAIArray" provider="HRUContext" providervar="currentEntity.LAIArray"/>
				<jamsvar name="aEP" provider="HRUContext" providervar="currentEntity.aEvap"/>
				<jamsvar name="aTP" provider="HRUContext" providervar="currentEntity.aTransp"/>
				<jamsvar name="pETP" provider="HRUContext" providervar="currentEntity.potETP"/>
				<jamsvar name="pEP" provider="HRUContext" providervar="currentEntity.pEvap"/>
				<jamsvar name="pTP" provider="HRUContext" providervar="currentEntity.pTransp"/>
			</component>               
               
 */
