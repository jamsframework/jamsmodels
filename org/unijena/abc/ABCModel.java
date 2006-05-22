/*
 * ABCModel.java
 *
 * Created on 18. Mai 2006, 15:20
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.unijena.abc;

import org.unijena.jams.data.*;
import org.unijena.jams.data.JAMSEntity.NoSuchAttributeException;
import org.unijena.jams.model.*;

/**
 *
 * @author c0krpe
 */
@JAMSComponentDescription(
        title="ABCModel",
        author="Peter Krause",
        description="The abc model is a simple linear model relating precipitation to streamflow on an annual basis. It " +
                    "was developed by Fiering (1967), purely for educational purposes. The model is a simple water " +
                    "balance calculation assuming that losses to evaporation and transpiration can simply be described " +
                    "by a constant factor, while the watershed generally is assumed to behave like a linear reservoir. " +
                    "The abc model has the following form: Qt = (1 – a – b)Pt + cSt-1 " +
                    "where Q is the streamflow, P is the precipitation, a is a parameter describing the fraction of" +
                    "precipitation that percolates through the soil to the groundwater, b is a parameter describing the" +
                    "fraction of precipitation directly lost to the atmosphere through evapotranspiration, and c is a" +
                    "parameter describing the amount of groundwater that leaves the aquifer storage S and drains into" +
                    "the stream. The index t describes the year (t=1,2,…,N). Streamflow, precipitation and storage are" +
                    "measured in volume units so that the additive relations derived are dimensionally homogeneous. " +
                    "The groundwater storage at the end of the year t is: St = aPt + (1 – c)St-1" +
                    "The following constraints are required:" +
                    "0 ? a,b,c ? 1 ," +
                    "0 ? a + b ? 1 ," +
                    "Pt, St ? 0"
        )
        public class ABCModel extends JAMSComponent {
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.INIT,
            description = "Parameter a"
            )
            public JAMSDouble a;
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.INIT,
            description = "Parameter b"
            )
            public JAMSDouble b;
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.INIT,
            description = "Parameter c"
            )
            public JAMSDouble c;
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.INIT,
            description = "the initial storage content [S_0]"
            )
            public JAMSDouble initStor;
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "the storage [S_t]"
            )
            public JAMSDouble storage;
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "the storage at the time step before [S_t-1]"
            )
            public JAMSDouble storageTm1;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "the precip input"
            )
            public JAMSDouble precip;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "the runoff output"
            )
            public JAMSDouble simRunoff;
    
    /*
     *  Component run stages
     */
    
    public void init() throws JAMSEntity.NoSuchAttributeException {
        this.storageTm1.setValue(this.initStor.getValue());
        this.storage.setValue(this.initStor.getValue());
        
        System.out.println("para a: " + this.a.getValue());
        System.out.println("para b: " + this.b.getValue());
        System.out.println("para c: " + this.c.getValue());
    }
    
    public void run() throws JAMSEntity.NoSuchAttributeException{
        double storage = this.storage.getValue();
        double storageTm1 = this.storageTm1.getValue();
        double precip = this.precip.getValue();
        double runoff = 0;
        
        double a = this.a.getValue();
        double b = this.b.getValue();
        double c = this.c.getValue();
        
        if(a+b > 1.0){
            System.out.println("Constraint violated: a + b is larger than 1.0");
            return;
        }
        
        runoff = ( 1 - a - b) * precip + c * storageTm1;
        
        storageTm1 = a * precip + (1-c) * storageTm1;
        
        
        this.simRunoff.setValue(runoff);
        this.storage.setValue(storageTm1);
        this.storageTm1.setValue(storageTm1);
    }
    
    public void cleanup() {
        
    }
    
}
