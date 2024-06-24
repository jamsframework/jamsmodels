/*
 * HargreavesSamani.java
 * Created on 24. November 2005, 13:57
 *
 * This file is part of JAMS
 * Copyright (C) 2005 FSU Jena, c0krpe
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA
 *
 */
package org.unijena.j2k.potET;

import java.io.*;
import jams.data.*;
import jams.model.*;

/**
 *
 * @author Peter Krause
 */
@JAMSComponentDescription(title = "CalcPotentialETSamani",
        author = "Peter Krause",
        version = "1.2",
        description = "Calculates potential ET according to Hargreaves Samani")
@VersionComments(entries = {
    @VersionComments.Entry(version = "1.0_0", comment = "Initial version", date = "2011-05-30"),
    @VersionComments.Entry(version = "1.0_1", comment = "Corrected description of units of potET/actET", date = "2018-07-04"),
    @VersionComments.Entry(version = "1.1", comment = "Data caching removed. Increase robustness in case of tmin > tmax; this can occur if datagaps are unfortunate. Manfred ", date = "2021-09-01"),
    @VersionComments.Entry(version = "1.2", comment = "Including the use of a crop factor, Manfred ", date = "2021-10-22"),
    @VersionComments.Entry(version = "1.2", comment = "Including the use of an altitude factor, Manfred ", date = "2022-09-28")
})
public class HargreavesSamani extends JAMSComponent {

    /*
     *  Component variables
     */
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
            description = "Current time")
    public Attribute.Calendar time;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
            description = "temporal resolution [d | m]")
    public Attribute.String tempRes;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
            description = "state variable minimum air temperature",
            unit = "degC")
    public Attribute.Double tmin;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
            description = "state variable mean temperature",
            unit = "degC")
    public Attribute.Double tmean;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
            description = "state variable maximum air temperature",
            unit = "degC")
    public Attribute.Double tmax;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
            description = "state variable extraterrestrial radiation",
            unit = "MJ m^-2 day^-1")
    public Attribute.Double extRad;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
            description = "attribute area")
    public Attribute.Double area;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "HRU attribute name elevation",
            defaultValue = "0.0")
            public Attribute.Double elevation;
    
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
            description = "crop factor",
            defaultValue = "1.0")
    public Attribute.Double Kc;
    
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
            description = "Multplier for the adaptation per meter of ET according to altitude",
            defaultValue = "0.0",
            lowerBound = -0.002,
            upperBound = 0.002)
    public Attribute.Double Altituede_factor;    

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
            description = "Base altitude for the adaptation per meter of ET according to altitude",
            defaultValue = "400.0",
            lowerBound = -200.0,
            upperBound = 9000.0)
    public Attribute.Double Altituede_base;
    
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.WRITE,
            description = "potential ET",
            unit = "L")
    public Attribute.Double potET;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.WRITE,
            description = "actual ET",
            unit = "L")
    public Attribute.Double actET;


    /*
     *  Component run stages
     */
    public void init() throws Attribute.Entity.NoSuchAttributeException, IOException {

    }

    public void run() throws Attribute.Entity.NoSuchAttributeException, IOException {

        double extRad = this.extRad.getValue();
        double tmin = this.tmin.getValue();
        double tavg = this.tmean.getValue();
        double tmax = this.tmax.getValue();
        double area = this.area.getValue();
        double Kc = this.Kc.getValue();
        double deltaET = 0;
        double latH = org.unijena.j2k.physicalCalculations.ClimatologicalVariables.calc_latentHeatOfVaporization(tavg);

        double pET = 0;
        double aET = 0;
        // avoiding data problem with tmin or tmax; Manfred
        if (tmin > tmax) {
            if (tmax > tavg) {
                tmin = tavg - (tmax - tavg);
            } else if(tmin < tavg){
                tmax = tavg + (tavg - tmin);
            } else {
                tmin = tmax; // leads to zero ET
            }

        }

        pET = ((0.0023 * extRad * Math.sqrt(tmax - tmin) * (tavg + 17.8)) / latH) * Kc;

        //converting mm to litres
        pET = pET * area;

        //aggregation to monthly values
        if (this.time != null) {
            if (this.tempRes.getValue().equals("m")) {
                int daysInMonth = this.time.getActualMaximum(Attribute.Calendar.DATE);
                pET = pET * daysInMonth;
            }
        }

        //avoiding negative potETPs
        if (pET < 0) {
            pET = 0;
        }
         //Altitude adaptation; Altituede_factor = 0 swiched off 
         
        deltaET = ((Altituede_base.getValue() - elevation.getValue()) * Altituede_factor.getValue() * pET);   
         
        deltaET = Math.max(-pET, deltaET);
        deltaET = Math.min(pET, deltaET);
        
        
        pET =  pET + deltaET;
        
        
        this.potET.setValue(pET);

        if (pET >= 0.0) {

        } else {
            System.out.println("extRad = " + extRad + " tmin = " + tmin + " tavg = " + tavg + " tmax  = " + tmax + " area = " + area + "latH = " + latH);
        }
        
       
        
       
        
        
        this.actET.setValue(aET);

    }

    public void cleanup() throws IOException {

    }

}
