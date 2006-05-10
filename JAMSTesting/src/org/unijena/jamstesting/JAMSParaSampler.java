/*
 * JAMSParaSampler.java
 * Created on 10. Mai 2006, 17:03
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

import java.util.StringTokenizer;
import org.unijena.jams.JAMS;
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
        public class JAMSParaSampler extends JAMSContext {
    
    /*
     *  Component variables
     */
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.INIT,
            description = "List of parameter identifiers to be sampled"
            )
            public JAMSString parameterIDs;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.INIT,
            description = "List of parameter value bounaries corresponding to parameter identifiers"
            )
            public JAMSString boundaries;
    
    int i = 0;
    JAMSDouble[] parameters;
    
    private boolean hasNext() {
        return i<10;
    }
    
    private void updateValues() {
        i++;
    }
    
    private void resetValues() {
        i = 0;
    }
    
    public void init() {
        /*
        StringTokenizer tok = new StringTokenizer(parameterIDs.getValue(), ";");
        String key;
        parameters = new JAMSDouble[tok.countTokens()];
        int i = 0;
        while (tok.hasMoreTokens()) {
            key = tok.nextToken();
            parameters[i++] = (JAMSDouble) JAMS.getRuntime().getModel().getDataHandles().get(key);
        }

        tok = new StringTokenizer(boundaries.getValue(), ";");
        while (tok.hasMoreTokens()) {
            key = tok.nextToken();
            System.out.println(key);
        }*/
    }
    
    class RunEnumerator implements JAMSComponentEnumerator {
        
        JAMSComponentEnumerator ce = getChildrenEnumerator();
        
        public boolean hasNext() {
            boolean nextTime = JAMSParaSampler.this.hasNext();
            boolean nextComp = ce.hasNext();
            return (nextTime || nextComp) ;
        }
        
        public JAMSComponent next() {
            // check end of component elements list, if required switch to the next
            // sampling point and start with the new component list again
            if (!ce.hasNext() && JAMSParaSampler.this.hasNext()) {
                JAMSParaSampler.this.updateValues();
                ce.reset();
            }
            return ce.next();
        }
        
        public void reset() {
            JAMSParaSampler.this.resetValues();
            ce.reset();
        }
    }
    
    public JAMSComponentEnumerator getRunEnumerator() {
        return new RunEnumerator();
    }
    
}
