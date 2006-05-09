/*
 * HandleTest.java
 * Created on 9. Mai 2006, 17:07
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

package org.unijena.jamstesting;

import org.unijena.jams.data.*;
import org.unijena.jams.model.*;

/**
 *
 * @author nsk
 */
@JAMSComponentDescription(
        title="Title",
        author="Author",
        description="Description"
        )
        public class HandleTest extends JAMSComponent {
    
    /*
     *  Component variables
     */
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "Description"
            )
            public JAMSDouble number;
    
    
    /*
     *  Component run stages
     */
    
    public void init() {
        
    }
    
    public void run() {
        System.out.println("RUN");
    }
    
    public void cleanup() {
        
    }
}
