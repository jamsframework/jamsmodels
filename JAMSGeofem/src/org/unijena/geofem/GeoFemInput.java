/*
 * GeoFemSnowModule.java
 *
 * Created on 18. Mai 2006, 15:20
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.unijena.geofem;

import org.unijena.jams.data.*;
import org.unijena.jams.data.JAMSEntity.NoSuchAttributeException;
import org.unijena.jams.model.*;

/**
 *
 * @author c0krpe
 */
@JAMSComponentDescription(
title="GeoFemInputModule",
        author="Peter Krause",
        description="Computes the input data to provide the correct units etc."
        )
        public class GeoFemInput extends JAMSComponent {
    
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "Entity area",
            unit = "m²"
            )
            public JAMSDouble area;
    
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.INIT,
            description = "Parameter ddf"
            )
            public JAMSDouble ddf;
    
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READWRITE,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "precip"
            )
            public JAMSDouble precip;
    
   
        /*
         *  Component run stages
         */
    
    public void init() throws JAMSEntity.NoSuchAttributeException {
        
    }
    
    public void run() throws JAMSEntity.NoSuchAttributeException{
            double precip = this.precip.getValue() * this.area.getValue();
            this.precip.setValue(precip);
    }
    
    public void cleanup() {
        
        
    }
    
}
