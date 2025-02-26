/*
 * PenmanMonteith.java
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
package Draft;

import java.io.*;
import jams.data.*;
import jams.model.*;

/**
 *
 * @author Peter Krause
 */
@JAMSComponentDescription(title = "CalcDailyRefET_Oudin",
        author = "Louise Mimeau",
        description = "Calculates reference evapotranspiration according to Oudin (daily time step only)",
        version = "1.0_1",
        date = "2024-10-22")
@VersionComments(entries = {
    @VersionComments.Entry(version = "1.0_0", comment = "Initial version", date = "2024-10-22")
})
public class ETOudin extends JAMSComponent {

    public final double K1 = 100;
    public final double K2 = 5;
    public final double lambda = 2.45;
    public final double ro = 1000;

    /*
     *  Component variables
     */

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
            description = "state variable mean temperature",
            unit = "°C")
    public Attribute.Double tmean;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
            description = "extraterretrial radiation",
            unit = "MJ m^-2 d^-1")
    public Attribute.Double extRad;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.WRITE,
            description = "reference ET [mm/ timeUnit]",
            unit = "mm")
    public Attribute.Double RefET;
    

    public void run() throws IOException {

        double Re = this.extRad.getValue();
        double temperature = this.tmean.getValue();
        
        double ET = this.calcETOudin(Re, temperature);

        //avoiding negative refETs
        if (ET < 0) {
            ET = 0;
        }

        this.RefET.setValue(ET);

    }
    
    private double calcETOudin(double extRad, double tmean) {
        double et = 1000 * (extRad / (lambda * ro)) * (tmean + K2)/K1;
        return et;
    }

    
}
