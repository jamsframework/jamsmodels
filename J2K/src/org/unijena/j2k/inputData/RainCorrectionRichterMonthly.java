/*
 * RainCorrectionRichterMonthly.java
 * Created on 24. November 2005, 09:48
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

package org.unijena.j2k.inputData;

import org.unijena.jams.data.*;
import org.unijena.jams.model.*;

/**
 *
 * @author Peter Krause
 */
@JAMSComponentDescription(
        title="RainCorrectionRichterMonthly",
        author="Peter Krause",
        description="Applies correction according to RICHTER 1985 for measured monthly precip sums"
        )
        public class RainCorrectionRichterMonthly extends JAMSComponent {
    
    /*
     *  Component variables
     */
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "time"
            )
            public JAMSCalendar time;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "the uncorrected precip values"
            )
            public JAMSDoubleArray inPrecip;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "the corrected precip values"
            )
            public JAMSDoubleArray corrPrecip;
    
    double NODATA = -9999;
    double[] richter3_b = {0.233, 0.245, 0.203, 0.151, 0.111, 0.098, 0.100, 0.095, 0.115, 0.127, 0.168, 0.198};
    double[] richter3_c = {0.173, 0.179, 0.155, 0.127, 0.101, 0.088, 0.091, 0.085, 0.102, 0.110, 0.133, 0.150};
    /*
     *  Component run stages
     */
    
    public void init() throws JAMSEntity.NoSuchAttributeException {
        
    }
    
    public void run() throws JAMSEntity.NoSuchAttributeException {
        
        /*
         *right at the moment the module assumes that all station are
         *in the same zone and are of the same type, should be enhanced 
         *later if necessary
         */
        double[] corrFactor = richter3_c;
        double[] precip = this.inPrecip.getValue();
        double[] rcorr = new double[precip.length];
        int month = time.get(time.MONTH);
        for(int i = 0; i < rcorr.length; i++){
            if(precip[i] == -9999){
                rcorr[i] = -9999;
            }else{
                //Applying the correction factors
                rcorr[i] = precip[i] + (precip[i] * corrFactor[month]);
            }
        }
        this.corrPrecip.setValue(rcorr);
        
    }
    
    public void cleanup() throws JAMSEntity.NoSuchAttributeException {
        
    }
}
