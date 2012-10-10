/*
 * RainCorrectionRichter.java
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
package org.unijena.j2kHimalaya.inputData;

import jams.JAMS;
import jams.data.*;
import jams.io.GenericDataWriter;
import jams.model.*;

/**
 *
 * @author 
 */
@JAMSComponentDescription(title = "PrecipCorrection",
author = "Santosh Nepal",
description = "A simple method to correct the precipitation by months, years and elevation. This module is also applicable to understand"
        + "precipitation change scenarios where precipitaiton can be increased by 10% or 20% for all months, or monsoon season")
public class PrecipCorrection extends JAMSComponent {

    /*
     *  Component variables
     */
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    update = JAMSVarDescription.UpdateType.RUN,
    description = "the precip values")
    public JAMSDoubleArray inputValues;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.WRITE,
    update = JAMSVarDescription.UpdateType.RUN,
    description = "the precip values")
    public JAMSDoubleArray outputValues;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    update = JAMSVarDescription.UpdateType.RUN,
    description = "Precipitation correction Factor",
    defaultValue = "1.0")
    public JAMSDouble correctionFactor;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    update = JAMSVarDescription.UpdateType.RUN,
    description = "Correction Factor for specific months",
    defaultValue = "1.0")
    public JAMSDouble correctionMonth;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    update = JAMSVarDescription.UpdateType.INIT,
    description = "Correction Factor for specific months")
    public JAMSDouble correctionElevation;


    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    update = JAMSVarDescription.UpdateType.RUN,
    description = "entity elevation")
    public JAMSDouble entityElev;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    update = JAMSVarDescription.UpdateType.INIT,
    description = "lower elevation for precip correction")
    public JAMSDouble lowerElevation;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    update = JAMSVarDescription.UpdateType.INIT,
    description = "higher elevation for precip correction")
    public JAMSDouble higherElevation;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    update = JAMSVarDescription.UpdateType.RUN,
    description = "time")
    public JAMSCalendar time;

    /*
     *  Component run stages
     */
    public void init() throws JAMSEntity.NoSuchAttributeException {
    }

    public void run() throws JAMSEntity.NoSuchAttributeException {

        int nowmonth = time.get(time.MONTH);
        // this.in_elevation = elevation.getValue();
        double[] inputValues = this.inputValues.getValue();
        double[] outputValues = new double[inputValues.length];
       
        for (int i = 0; i < inputValues.length; i++) {
            outputValues[i] = inputValues[i] * correctionFactor.getValue();
            if ((nowmonth >= 7) && (nowmonth <= 9)) {
                outputValues[i] = outputValues[i] * correctionMonth.getValue();
            }
            if ((entityElev.getValue() <= higherElevation.getValue()) && (entityElev.getValue() <= lowerElevation.getValue())) {
                outputValues[i] = outputValues[i] * correctionElevation.getValue();
            }
        }
        this.outputValues.setValue(outputValues);
    }



    public void cleanup() {
    }
}


