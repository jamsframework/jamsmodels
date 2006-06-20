/*
 * ABCSnowModule.java
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
        public class ABCSnowModule extends JAMSComponent {
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.INIT,
            description = "Parameter ddf"
            )
            public JAMSDouble ddf;
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.INIT,
            description = "Parameter threshold"
            )
            public JAMSDouble t_thres;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "the snow storage "
            )
            public JAMSDouble snowStorage;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "the precip input"
            )
            public JAMSDouble precip;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "the temperature input"
            )
            public JAMSDouble temperature;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "the snowmelt output"
            )
            public JAMSDouble snowMelt;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "the total output"
            )
            public JAMSDouble total_output;
    
    /*
     *  Component run stages
     */
    
    public void init() throws JAMSEntity.NoSuchAttributeException {
        
    }
    
    public void run() throws JAMSEntity.NoSuchAttributeException{
        //System.out.println("RUN ABCModel");
        double snowStorage = this.snowStorage.getValue();
        double precip = this.precip.getValue();
        double temperature = this.temperature.getValue();
        double snowMelt = 0;
        
        if(temperature < this.t_thres.getValue()){
            snowStorage = snowStorage + precip;
            precip = 0;
        }
        else if(temperature >= this.t_thres.getValue() && snowStorage > 0){
            double meltTemp = temperature - this.t_thres.getValue();
            double potMelt = meltTemp * this.ddf.getValue();
            if(snowStorage < potMelt){
                snowMelt = snowStorage;
                snowStorage = 0;
            }
            else{
                snowMelt = potMelt;
                snowStorage = snowStorage - snowMelt;
            }
        }
        
        this.precip.setValue(precip);
        this.snowStorage.setValue(snowStorage);
        this.snowMelt.setValue(snowMelt);
        this.total_output.setValue(precip + snowMelt);
    }
    
    public void cleanup() {
        this.snowStorage.setValue(0.0);
        
    }
    
}
