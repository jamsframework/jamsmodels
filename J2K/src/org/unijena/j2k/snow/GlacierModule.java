/*
 * GlacierModule.java
 * Created on 22. Febuary 2008, 13:57
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

package org.unijena.j2k.snow;
;
import java.io.*;
import org.unijena.jams.data.*;
import org.unijena.jams.model.*;

/**
 *
 * @author Peter Krause
 */

    
    
@JAMSComponentDescription(
        title="GlacierModule",
        author="Peter Krause",
        description="Simple process module for glacier simulation. The module " +
        "calculates snow accumulation by a temperature threshold approach and " +
        "snow melt from the glacier with a day-degree-approach. Melt from the " +
        "glacier is implementing by the melt formula according to " +
        "Hock (1998, 1999) in a simple and a more complex form. " +
        "The simple form needs temperature only whereas" +
        "the complex form needs also radiation." +
        "Glacier runoff is calculated by the outflow from two reservoirs. The first" +
        "represents snow falling on the glacier whereas the second represents the" +
        "ice of the glacier. The same idea was implemented in WasimETH first."
        )
    
    public class GlacierModule extends JAMSComponent {
    
        
    /*
     *  Component variables
     */
        
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "the actual air temperature",
            unit="°C"
            )
            public JAMSDouble temperature;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "the actual rainfall",
            unit="L/m^2"
            )
            public JAMSDouble rain;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "the actual snowfall",
            unit="L/m^2"
            )
            public JAMSDouble snow;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "the actual global radiation",
            unit = "MJ/day"
            )
            public JAMSDouble radiation;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "actual snow storage",
            unit = "L/m^2"
            )
            public JAMSDouble snowStorage;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.INIT,
            description = "attribute area",
            unit="m^2"
            )
            public JAMSDouble area;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "snow runoff of time step before",
            unit = "L"
            )
            public JAMSDouble snowRunofftm1;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "ice runoff of time step before",
            unit = "L"
            )
            public JAMSDouble iceRunofftm1;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "total runoff of unit", 
            unit = "L"
            )
            public JAMSDouble glacierRunoff;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "runoff from glacier melt",
            unit="L"
            )
            public JAMSDouble iceRunoff;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "runoff from snow melt and rain",
            unit = "L"
            )
            public JAMSDouble snowRunoff;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.INIT,
            description = "generalised melt factor for ice and snow"
            )
            public JAMSDouble meltFactor;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.INIT,
            description = "day degree factor for snow"
            )
            public JAMSDouble ddfSnow;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.INIT,
            description = "day degree factor for ice"
            )
            public JAMSDouble ddfIce;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.INIT,
            description = "melt coefficient for snow"
            )
            public JAMSDouble alphaSnow;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.INIT,
            description = "melt coefficient for ice"
            )
            public JAMSDouble alphaIce;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.INIT,
            description = "routing coefficient for snow"
            )
            public JAMSDouble kSnow;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.INIT,
            description = "routing coefficient for ice"
            )
            public JAMSDouble kIce;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.INIT,
            description = "threshold temperature"
            )
            public JAMSDouble tbase;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.INIT,
            description = "melt formula [1 = simple, 2 = complex]"
            )
            public JAMSInteger meltFormula;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.INIT,
            description = "temporal resolution [d | h]"
            )
            public JAMSString tempRes;
    
    
    
    /*
     *  Component run stages
     */
    
    public void run() throws JAMSEntity.NoSuchAttributeException, IOException {
        //retreive the actual states and input
        double snowStor = this.snowStorage.getValue();
        double temp = this.temperature.getValue();
        
        int n = 0;
        if (this.tempRes.getValue().equals("d")) {
            n = 1;
        } else if (this.tempRes.getValue().equals("h")) {
            n = 24;
        }
        //calc potential snow accumulation
        if (this.snow.getValue() > 0) {
            snowStor = snowStor + this.snow.getValue();
            this.snow.setValue(0);
        }
        //calc potential melt
        double snowMelt = 0;
        double iceMelt = 0;
        double totalMelt = 0;
        
        if (temp > this.tbase.getValue()) {
            if (this.meltFormula.getValue() == 1) {
                //simple formula
                //snow melt
                snowMelt = (1 / n) * this.ddfSnow.getValue() * (temp - this.tbase.getValue());
                snowMelt = snowMelt * this.area.getValue();
                if (snowMelt >= snowStor) {
                    snowMelt = snowStor;
                    snowStor = 0;
                } else {
                    snowStor = snowStor - snowMelt;
                }
                //ice melt only when no snow is available
                if(snowStor == 0){
                    iceMelt = (1 / n) * this.ddfIce.getValue() * (temp - this.tbase.getValue());
                    iceMelt = iceMelt * this.area.getValue();
                }
                else
                    iceMelt = 0;
            } else if (this.meltFormula.getValue() == 2) {
                //complex formula
                //snow melt
                snowMelt = (1 / n) * this.meltFactor.getValue() + this.alphaSnow.getValue() * this.radiation.getValue();
                snowMelt = snowMelt * this.area.getValue();
                if (snowMelt >= snowStor) {
                    snowMelt = snowStor;
                    snowStor = 0;
                } else {
                    snowStor = snowStor - snowMelt;
                }
                //ice melt only when no snow is available
                if(snowStor == 0){
                    iceMelt = (1 / n) * this.meltFactor.getValue() + this.alphaIce.getValue() * this.radiation.getValue();
                    iceMelt = iceMelt * this.area.getValue();
                }
                else{
                    iceMelt = 0;
                }
            }
            
            totalMelt = snowMelt + iceMelt;
            
        }
        //route runoff inside glacier
        //snow routing
        double q_snow = this.snowRunofftm1.getValue() * Math.exp(-1/this.kSnow.getValue()) + (snowMelt + this.rain.getValue()) * (1-Math.exp(-1/this.kSnow.getValue()));
        this.rain.setValue(0);
        //ice routing
        double q_ice = this.iceRunofftm1.getValue() * Math.exp(-1/this.kIce.getValue()) + iceMelt * (1-Math.exp(-1/this.kIce.getValue()));
        //calc total glacier runoff
        double tot_q = q_snow + q_ice;
        
        //writing variables back
        this.snowRunofftm1.setValue(q_snow);
        this.iceRunofftm1.setValue(q_ice);
        this.glacierRunoff.setValue(tot_q);
        this.iceRunoff.setValue(q_ice);
        this.snowRunoff.setValue(q_snow);
        this.snowStorage.setValue(snowStor);
    }
    
    public void cleanup()  throws IOException {
        
    }
}
