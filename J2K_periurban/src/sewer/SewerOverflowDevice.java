/*
 * SewerOverflowDevice.java
 * Created on 05. October 2012, 17:02
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
package sewer;

import jams.data.*;
import jams.model.*;
import java.util.GregorianCalendar;

/**
 *
 * @author Sven Kralisch & Mériem Labbas & Christian Fischer
 */
@JAMSComponentDescription(title = "DoubleTransfer",
author = "Sven Kralisch & Mériem Labbas & Christian Fischer",
description = "Component used for the simulation of an overflow device. It takes the different components outflows"
        + "coming from a sewer reach(threshold test) and adds it to the receiving reach river.",
version = "1.0_0",
date = "2012-10-05")
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
    description = "Actual flow inside the sewer or river reach",
    unit = "L")
    public Attribute.Double[] actValues;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.WRITE,
    description = "outflow from the SOD",
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
        } else if (ti.getTimeUnit() == GregorianCalendar.DAY_OF_YEAR) {
            seconds = 24*3600*ti.getTimeUnitCount();
        }  else if (ti.getTimeUnit() == GregorianCalendar.MONTH) {
            seconds = time.getActualMaximum(GregorianCalendar.DAY_OF_MONTH)*24*3600*ti.getTimeUnitCount();
        }
    }
    int ts = 0;
    /*
     * Component run stages
     */
    public void run() {

        if (ts++ > 60) {
            System.out.println("");
        }
        
        if (to_river.getValue() == null) {
            return;
        }
        
        getModel().getRuntime().println(time.toString());

        double volume = 0;
        double[] frac = new double[inValues.length];

        for (int i = 0; i < inValues.length; i++) {
            volume = volume + inValues[i].getValue();
        }
        for (int i = 0; i < actValues.length; i++) {
            volume = volume + actValues[i].getValue();
        }
        for (int i = 0; i < inValues.length; i++) {
            if (volume > 0) {
                frac[i] = (inValues[i].getValue() + actValues[i].getValue())/ volume;
            }
        }        

        double maxVolume = threshold.getValue() * length.getValue() * width.getValue() * 1000; //in L
        double diffVolume = 0, height = 0, q = 0;
        double overflowComp = 0;
        double g = 9.80665; //gravitationnal constant

        // overflow is happening?
        if (volume - maxVolume > 0) {
            diffVolume = volume - maxVolume; //in L
            height = (diffVolume / 1000) / (length.getValue() * width.getValue()); //in m
            q = diffVolume;
            
         if (height <= pipeHeight.getValue()) {
                q = dischCoeff.getValue() * pipeWidth.getValue() * height * Math.sqrt(2 * g * height) * seconds * 1000;
//              getModel().getRuntime().println("");
//                getModel().getRuntime().println("x");
            } else {
                q = dischCoeff.getValue() * pipeWidth.getValue() * pipeHeight.getValue() * Math.sqrt(2 * g * height) * seconds * 1000;
//                getModel().getRuntime().println("y");
            }
            q = Math.min(q, diffVolume); 
            
            

            for (int i = 0; i < inValues.length; i++) {
                // The overflow of the SOD is limited by its pipe diameter               
                  overflowComp = frac[i] * q;

                inValues[i].setValue(inValues[i].getValue() - overflowComp);
                to_river.setDouble(inNames[i].getValue(), overflowComp + to_river.getDouble(inNames[i].getValue()));
                outValues[i].setValue(overflowComp);
            }
        }else
        {
            for (int i = 0; i < inValues.length; i++) {
                outValues[i].setValue(overflowComp);
            }
        }
    }
}
