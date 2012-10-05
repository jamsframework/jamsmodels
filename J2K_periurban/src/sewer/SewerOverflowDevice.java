/*
 * SewerOverflowDevice.java
 * Created on 27. September 2012, 22:02
 *
 * This file is part of JAMS
 * Copyright (C) FSU Jena
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
package irstea.sewer;

import jams.data.*;
import jams.model.*;
import java.util.GregorianCalendar;

/**
 *
 * @author Sven Kralisch
 */
@JAMSComponentDescription(title = "DoubleTransfer",
author = "Sven Kralisch",
description = "Component for simply transferring multiple double "
+ "attributes) to a target entity. Can be used to implement a "
+ "simple routing mechanism (e.g. HRU to HRU or HRU to reach) by "
+ "taking a source entity's double data and moving it to specified.",
version = "1.0_0",
date = "2012-09-27")
public class SewerOverflowDevice extends JAMSComponent {

    /*
     * Component variables
     */
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    description = "sewer length",
    unit = "m")
    public Attribute.Double length;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    description = "sewer width",
    unit = "m")
    public Attribute.Double width;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    description = "SOD threshold",
    unit = "m")
    public Attribute.Double threshold;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    description = "Coefficient discharge",
    unit = "-")
    public Attribute.Double dischCoeff;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    description = "pipe width",
    unit = "m")
    public Attribute.Double pipeWidth;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    description = "pipe height",
    unit = "m")
    public Attribute.Double pipeHeight;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    description = "Target river reach")
    public Attribute.Entity to_river;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    description = "Target reach's receiving attributes")
    public Attribute.String[] inNames;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READWRITE,
    description = "Flow to be transferred to the SOD",
    unit = "L")
    public Attribute.Double[] inValues;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READWRITE,
    description = "Flow to be transferred",
    unit = "L")
    public Attribute.Double[] actValues;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.WRITE,
    description = "Flow to be transferred to the SOD",
    unit = "L")
    public Attribute.Double[] outValues;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    description = "Current time step",
    unit = "d")
    public Attribute.Calendar time;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    description = "time interval",
    unit = "d")
    public Attribute.TimeInterval ti;
    
    private int seconds;

    public void init() {
        
        if (ti.getTimeUnit() == GregorianCalendar.MINUTE) {
            seconds = 60*ti.getTimeUnitCount();
        } else if (ti.getTimeUnit() == GregorianCalendar.HOUR) {
            seconds = 3600*ti.getTimeUnitCount();
        } else if (ti.getTimeUnit() == GregorianCalendar.DAY_OF_MONTH) {
            seconds = 24*3600*ti.getTimeUnitCount();
        }  else if (ti.getTimeUnit() == GregorianCalendar.MONTH) {
            seconds = time.getActualMaximum(GregorianCalendar.DAY_OF_MONTH)*24*3600*ti.getTimeUnitCount();
        }
    }
    
    /*
     * Component run stages
     */
    public void run() throws Attribute.Entity.NoSuchAttributeException {

        if (to_river.getValue() == null) {
            return;
        }
        
        getModel().getRuntime().println(time.toString());

        double volume = 0;
        double[] frac = new double[inValues.length];

        for (int i = 0; i < inValues.length; i++) {
            volume = volume + inValues[i].getValue();
        }
        for (int i = 0; i < inValues.length; i++) {
            frac[i] = inValues[i].getValue() / volume;
        }
        for (int i = 0; i < actValues.length; i++) {
            volume = volume + actValues[i].getValue();
        }

        double maxVolume = threshold.getValue() * length.getValue() * width.getValue() * 1000; //in L
        double diffVolume = 0, height = 0, q;
        double g = 9.80665; //gravitionnal constant

        // overflow is happening?
        if (volume - maxVolume > 0) {
            diffVolume = volume - maxVolume; //in L
            height = (diffVolume / 1000) / (length.getValue() * width.getValue()); //in m

            //q = diffVolume;
            if (height <= pipeHeight.getValue()) {
                q = dischCoeff.getValue() * pipeWidth.getValue() * height * Math.sqrt(2 * g * height) * seconds / 1000;
            } else {
                q = dischCoeff.getValue() * pipeWidth.getValue() * threshold.getValue() * Math.sqrt(2 * g * height) * seconds / 1000;
            }

            double overflowComp;

            for (int i = 0; i < inValues.length; i++) {
                // The overflow of the SOD is limited by its pipe diameter               
                overflowComp = frac[i] * q;

                inValues[i].setValue(inValues[i].getValue() - overflowComp);
                to_river.setDouble(inNames[i].getValue(), overflowComp + to_river.getDouble(inNames[i].getValue()));
                outValues[i].setValue(overflowComp);
            }
        }
    }
}
