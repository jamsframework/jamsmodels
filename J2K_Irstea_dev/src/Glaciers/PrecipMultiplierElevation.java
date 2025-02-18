/*
 * PrecipMultiplierElevation.java
 * Created on 25. November 2005, 13:21
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
package Glaciers;


import jams.data.*;
import jams.model.*;
import java.util.Calendar;

/**
 *
 * @author Peter Krause
 */
@JAMSComponentDescription(title = "PrecipMultiplierElevation",
author = "Olivier Champagne,Manfred Fink, Santosh Nepal, Peter Krause",
description = "Multiplier of precipitation data (rain or snow) with elevation threshold" )
public class PrecipMultiplierElevation extends JAMSComponent {

    /*
     *  Component variables
     */
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    description = "entity elevation")
    public Attribute.Double entityElev;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    description = "precipitation input value")
    public Attribute.Double inputValue;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.WRITE,
    description = "calculated output value")
    public Attribute.Double outputValue;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    description = "multiplicator")
    public Attribute.Double multiplicator;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    description = "elevation threshold over which the multiplier is applied")
    public Attribute.Double elevationthreshold;

    public void init() throws Attribute.Entity.NoSuchAttributeException {
    }

    public void run() throws Attribute.Entity.NoSuchAttributeException {

        if (entityElev.getValue() > elevationthreshold.getValue()) {
            double multi = multiplicator.getValue();
            double correctedvalue = inputValue.getValue();
            outputValue.setValue(multi * correctedvalue);
        
        } else {
            outputValue.setValue(inputValue.getValue());       
        }
    }

    /**
     *
     */
    @Override
    public void cleanup() {
    }
}