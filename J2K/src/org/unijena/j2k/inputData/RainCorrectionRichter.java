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
package org.unijena.j2k.inputData;

import jams.JAMS;
import jams.data.*;
import jams.io.GenericDataWriter;
import jams.model.*;

/**
 *
 * @author Peter Krause
 */
@JAMSComponentDescription(
        title = "RainCorrection_Richter",
        author = "Peter Krause",
        description = "Applies correction according to RICHTER 1985 for measured daily precip sums",
        version="1.0_0",
        date="2011-05-30")
public class RainCorrectionRichter extends JAMSComponent {

    /*
     *  Component variables
     */
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
                        description = "time")
    public Attribute.Calendar time;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
                        description = "the precip values")
    public Attribute.DoubleArray precip;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
                        description = "temperature for the correction function",
                        unit = "°C")
    public Attribute.DoubleArray temperature;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.WRITE,
                        description = "corrected precip values",
                        unit = "mm")
    public Attribute.DoubleArray rcorr;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
                        description = "array of temperature station elevations",
                        unit = "m")
    public Attribute.DoubleArray tempElevation;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
                        description = "array of temperature station's x coordinate")
    public Attribute.DoubleArray tempXCoord;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
                        description = "array of temperature station's y coordinate")
    public Attribute.DoubleArray tempYCoord;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
                        description = "Regression coefficients for temperature")
    public Attribute.DoubleArray tempRegCoeff;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
                        description = "array of precip station elevations",
                        unit = "m")
    public Attribute.DoubleArray rainElevation;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
                        description = "array of precip station's x coordinate")
    public Attribute.DoubleArray rainXCoord;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
                        description = "array of precip station's y coordinate")
    public Attribute.DoubleArray rainYCoord;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
                        description = "number of temperature station for IDW")
    public Attribute.Integer tempNIDW;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
                        description = "power for IDW function")
    public Attribute.Double pIDW;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
                        description = "regression threshold")
    public Attribute.Double regThres;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
                        description = "snow_trs",
                        upperBound = 5.0,
                        lowerBound = -5.0,
                        unit = "°C")
    public Attribute.Double snow_trs;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
                        description = "snow_trans",
                        upperBound = 10,
                        lowerBound = 0,
                        unit = "K")
    public Attribute.Double snow_trans;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
                        description = "Caching configuration: 0 - write cache, 1 - use cache, 2 - caching off",
                        defaultValue = "0")
    public Attribute.Integer dataCaching;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
                        description = "Data file directory name")
    public Attribute.String dirName;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
                        description = "Output file name")
    public Attribute.String fileName;

    private GenericDataWriter writer,  writer2;

    private boolean headerWritten;

    private final int NODATA = -9999;

    /*
     *  Component run stages
     */
    public void init() throws Attribute.Entity.NoSuchAttributeException {
        if (this.fileName != null) {
            writer = new GenericDataWriter(dirName.getValue() + "/" + fileName.getValue());
            writer2 = new GenericDataWriter(dirName.getValue() + "/" + "rainTempFile.dat");

        }
    }

    public void run() throws Attribute.Entity.NoSuchAttributeException {

        if (this.fileName != null) {
            int nstat = precip.getValue().length;
            if (!this.headerWritten) {
                String comment = "xCoord\t";
                for (int i = 0; i < nstat; i++) {
                    comment = comment + rainXCoord.getValue()[i] + "\t";
                }
                writer.addComment(comment);
                writer2.addComment(comment);
                comment = "yCoord\t";
                for (int i = 0; i < nstat; i++) {
                    comment = comment + rainYCoord.getValue()[i] + "\t";
                }
                writer.addComment(comment);
                writer2.addComment(comment);
                comment = "elevation\t";
                for (int i = 0; i < nstat; i++) {
                    comment = comment + rainElevation.getValue()[i] + "\t";
                }
                writer.addComment(comment);
                writer2.addComment(comment);
                //always write time
                writer.addColumn("date/time");
                writer2.addColumn("date/time");
                for (int i = 0; i < nstat; i++) {
                    writer.addColumn("" + (i + 1));
                    writer2.addColumn("" + (i + 1));
                }
                writer.writeHeader();
                writer2.writeHeader();
                this.headerWritten = true;
            }
        }
        if (dataCaching.getValue() != 1) {
            double[] precip = this.precip.getValue();
            double[] temperature = this.temperature.getValue();
            double[] rcorr = new double[precip.length];
            double[] rainTemp = new double[precip.length];
            double[] rainElev = new double[precip.length];
            double[] rainX = new double[precip.length];
            double[] rainY = new double[precip.length];

            //we need arrays for the temperature stations dist weight and id
            double[] statWeights = new double[temperature.length];
            //double[][] statDists   = new double[precip.length][temperature.length];

            //parameterization of rain stations
            for (int i = 0; i < precip.length; i++) {
                rainElev[i] = this.rainElevation.getValue()[i];
                rainX[i] = this.rainXCoord.getValue()[i];
                rainY[i] = this.rainYCoord.getValue()[i];

            }
            int[] wArray = null;
            for (int i = 0; i < rcorr.length; i++) {
                //Calculating weights for nidw stations
            }


            double rsq = this.tempRegCoeff.getValue()[2];
            double grad = this.tempRegCoeff.getValue()[1];

            //temperature for each rain station
            for (int r = 0; r < rcorr.length; r++) {
                rainTemp[r] = 0;
                //calc weights
                double[] dist = org.unijena.j2k.statistics.IDW.calcDistances(rainX[r], rainY[r], this.tempXCoord.getValue(), this.tempYCoord.getValue(), this.pIDW.getValue());
                statWeights = org.unijena.j2k.statistics.IDW.calcWeights(dist, this.temperature.getValue());
                wArray = org.unijena.j2k.statistics.IDW.computeWeightArray(statWeights);

                //selecting the nidw closest temperature stations and avoiding no data values
                int counter = 0;
                boolean cont = true;
                boolean valid = false;
                double[] data = new double[this.tempNIDW.getValue()];
                double[] weights = new double[this.tempNIDW.getValue()];
                double[] elev = new double[this.tempNIDW.getValue()];
                int element = counter;
                while (counter < this.tempNIDW.getValue() && cont) {
                    int t = wArray[element];
                    //check if data is valid or no data
                    if (this.temperature.getValue()[t] == NODATA) {

                        element++;
                        if (element >= wArray.length) {
                            getModel().getRuntime().println("BREAK1: too less data NIDW had been reduced!", JAMS.VERBOSE);
                            cont = false;
                        //value = NODATA;
                        } else {
                            t = wArray[element];
                        }
                    } else {
                        valid = true;
                        data[counter] = this.temperature.getValue()[t];
                        weights[counter] = statWeights[t];
                        elev[counter] = this.tempElevation.getValue()[t];

                        counter++;
                        element++;
                   

                    }

                }
                //normalising weights
                double weightsum = 0;
                for (int i = 0; i < counter; i++) {
                    weightsum += weights[i];
                }
                for (int i = 0; i < counter; i++) {
                    weights[i] = weights[i] / weightsum;
                }
                for (int t = 0; t < this.tempNIDW.getValue(); t++) {
                    if (rsq >= this.regThres.getValue()) {  //Elevation correction is applied
                        double deltaElev = this.rainElevation.getValue()[r] - elev[t];  //Elevation difference between unit and Station
                        rainTemp[r] += ((deltaElev * grad + data[t]) * weights[t]);
                    } else { //No elevation correction
                        rainTemp[r] += (data[t] * weights[t]);
                    }
                }
                //determine rain and snow amount of precip
                double pSnow = (snow_trs.getValue() + snow_trans.getValue() - rainTemp[r]) /
                        (2 * snow_trans.getValue());

                //fixing upper and lower bound for pSnow (has to be between 0 and 1
                if (pSnow > 1.0) {
                    pSnow = 1.0;
                } else if (pSnow < 0) {
                    pSnow = 0;                //dividing input precip into rain and snow
                }
                double rain = (1 - pSnow) * precip[r];
                double snow = pSnow * precip[r];


                //Calculating relative Winderror acc to RICHTER 1995
                double windErr = 0;
                if (snow > 0) {//if(pSnow >= 1.0){      //set to all snow (5/11/01), rechanged 1.03.02
                    if (snow <= 0.1) {
                        snow = snow + (snow * 0.938);
                    } else {
                        double relSnow = 0.5319 * Math.pow(snow, -0.197);
                        snow = snow + (snow * relSnow);
                    }
                }
                if (rain > 0) { //if(pSnow < 1.0){//
                    if (rain < 0.1) {
                        rain += (rain * 0.492);
                    } else {
                        rain += (rain * (0.1349 * Math.pow(rain, -0.494)));
                    }
                }

                //Calculating error from evaporation and wetting acc. to Richter
                double wetErr = 0;
                if (precip[r] < 0.1) {
                    wetErr = 0;
                } else {
                    if (time.get(JAMSCalendar.MONTH) >= 4 & time.get(JAMSCalendar.MONTH) < 10) { //Summer half of the year
                        if (precip[r] >= 9.0) {
                            wetErr = 0.47;
                        } else {
                            wetErr = 0.08 * Math.log(precip[r]) + 0.225;
                        }
                    } else {   //Winter half of the year
                        if (precip[r] >= 9.0) {
                            wetErr = 0.3;
                        } else {
                            wetErr = 0.05 * Math.log(precip[r]) + 0.13;
                        }
                    }
                }

                //Calculating corrected rain_value
                if (precip[r] == -9999) {
                    rcorr[r] = -9999;
                } else {
                    rcorr[r] = rain + snow + wetErr;
                }
            }

            this.rcorr.setValue(rcorr);

            if (this.fileName != null) {
                writer.addData(time);
                writer2.addData(time);
                for (int i = 0; i < rcorr.length; i++) {
                    writer.addData(rcorr[i]);
                    writer2.addData(rainTemp[i]);
                }
                try {
                    writer.writeData();
                    writer2.writeData();
                } catch (jams.runtime.RuntimeException jre) {
                    this.getModel().getRuntime().handle(jre);
                }
            }
        }
    }

    public void cleanup() throws Attribute.Entity.NoSuchAttributeException {
        if (this.fileName != null) {
            writer.write("#eof");
            writer2.write("#eof");
            writer.close();
            writer2.close();
            System.out.println("Rcorr writer successfully closed");

        }
    }
}
