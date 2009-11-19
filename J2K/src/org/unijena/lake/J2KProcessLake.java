/*
 * J2KProcessInterception.java
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

 */
package org.unijena.lake;

import jams.data.*;
import jams.model.*;

/**
 *
 * @author Peter Krause
 */
@JAMSComponentDescription(
        title="J2KProcessLake",
        author="Peter Krause",
        description="A module for integration of lakes"
        )
        public class J2KProcessLake extends JAMSComponent {
    
    /*
     *  Component variables
     */
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "state variable rain"
            )
            public JAMSDouble precip;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "state variable potET"
            )
            public JAMSDouble potET;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "state variable actET"
            )
            public JAMSDouble actET;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "state variable lake storage"
            )
            public JAMSDouble lakeStorage;
    /*
     *  Component run stages
     */
    
    public void init() throws JAMSEntity.NoSuchAttributeException{
        this.lakeStorage.setValue(0);
    }
    
    public void run() throws JAMSEntity.NoSuchAttributeException{
        double ls = this.lakeStorage.getValue();
        ls = ls + precip.getValue();
        ls = ls - potET.getValue();

        actET.setValue(potET.getValue());
        lakeStorage.setValue(ls);
        
    }
    
    public void cleanup() {
        this.lakeStorage.setValue(0);
    }
    
}
