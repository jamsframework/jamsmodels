/*
 * SnowEvaporation.java
 *
 * Created on 22. Nov 2009, 15:20
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.unijena.j2k.snow;

import jams.data.*;
import jams.model.*;

/**
 *
 * @author c0krpe
 */
@JAMSComponentDescription(
title="SnowEvaporation",
        author="Peter Krause",
        description="Very simple snow evaporation module which is estimates" +
        "snow ET as a constant fraction of potET"
        )
        public class SnowEvaporation extends JAMSComponent {
    
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "the potential ET"
            )
            public JAMSDouble potET;

    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READWRITE,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "the actual ET"
            )
            public JAMSDouble actET;
    
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.INIT,
            description = "snow ET coefficient"
            )
            public JAMSDouble set_factor;
    
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READWRITE,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "snow water equivalent"
            )
            public JAMSDouble swe;
    
    
    
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READWRITE,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "the snow storage "
            )
            public JAMSDouble snowET;
    
    
        /*
         *  Component run stages
         */
    
    public void init() throws JAMSEntity.NoSuchAttributeException {
        
    }
    
    public void run() throws JAMSEntity.NoSuchAttributeException{
        double snow_et = 0;
        double run_swe = swe.getValue();
        double res_et = this.potET.getValue() * this.set_factor.getValue();
        double run_aET = this.actET.getValue();

        if(run_swe >= res_et){
            snow_et = res_et;
            run_swe = run_swe - snow_et;
        }
        else{
            snow_et = run_swe;
            run_swe = 0;
        }
        run_aET = run_aET + snow_et;
        this.actET.setValue(run_aET);
        this.snowET.setValue(snow_et);
        this.swe.setValue(run_swe);

    }
    
    public void cleanup() {
        
        
    }
    
}
