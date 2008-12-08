/*
 * Regionalisation.java
 * Created on 17. November 2005, 14:20
 *
 * This file is part of JAMS
 * Copyright (C) 2005 S. Kralisch and P. Krause
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
package org.unijena.j2k.regionalisation;

import java.io.*;
import jams.data.*;
import jams.model.*;

/**
 *
 * @author Peter Krause
 */
public class Regionalisation extends JAMSComponent {

    @JAMSVarDescription (access = JAMSVarDescription.AccessType.READ,
                         description = "Array of data values for current time step")
    public JAMSDoubleArray dataArray;

    @JAMSVarDescription (access = JAMSVarDescription.AccessType.READ,
                         description = "Regression coefficients")
    public JAMSDoubleArray regCoeff = new JAMSDoubleArray();

    @JAMSVarDescription (access = JAMSVarDescription.AccessType.READ,
                         description = "Array of station elevations")
    public JAMSDoubleArray statElevation = new JAMSDoubleArray();

    @JAMSVarDescription (access = JAMSVarDescription.AccessType.READ,
                         description = "Array of station's weights")
    public JAMSDoubleArray statWeights = new JAMSDoubleArray();

    @JAMSVarDescription (access = JAMSVarDescription.AccessType.READ,
                         description = "Array position of weights")
    public JAMSIntegerArray statOrder = new JAMSIntegerArray();

    @JAMSVarDescription (access = JAMSVarDescription.AccessType.WRITE,
                         description = "regionalised data value")
    public JAMSDouble dataValue;

    @JAMSVarDescription (access = JAMSVarDescription.AccessType.READ,
                         description = "Attribute name elevation")
    public JAMSDouble entityElevation;

    @JAMSVarDescription (access = JAMSVarDescription.AccessType.READ,
                         description = "Number of IDW stations")
    public JAMSInteger nidw;

    @JAMSVarDescription (access = JAMSVarDescription.AccessType.READ,
                         description = "Apply elevation correction to measured data")
    public JAMSBoolean elevationCorrection;

    @JAMSVarDescription (access = JAMSVarDescription.AccessType.READ,
                         description = "Minimum r² value for elevation correction application")
    public JAMSDouble rsqThreshold;

    @JAMSVarDescription (access = JAMSVarDescription.AccessType.READ,
                         description = "Absolute possible minimum value for data set",
                         defaultValue = "-Infinity")
    public JAMSDouble fixedMinimum;

    @JAMSVarDescription (access = JAMSVarDescription.AccessType.READ,
                         description = "Caching configuration: 0 - write cache, 1 - use cache, 2 - caching off",
                         defaultValue = "0")
    public JAMSInteger dataCaching;

    @JAMSVarDescription (access = JAMSVarDescription.AccessType.READ,
                         description = "Result value if no value is available",
                         defaultValue = "-9999.0")
    public JAMSDouble noData;

    private File cacheFile;

    transient private ObjectOutputStream writer;

    transient private ObjectInputStream reader;

    public void init() throws IOException {

        //first, check if cached data are available
        cacheFile = new File(getModel().getWorkspace().getTempDirectory(), this.getInstanceName() + ".cache");

        if (!cacheFile.exists() && (dataCaching.getValue() == 1)) {
            getModel().getRuntime().sendHalt(this.getInstanceName() + ": data caching is switched on but no cache file available!");
        }

        if (dataCaching.getValue() == 1) {
            reader = new ObjectInputStream(new BufferedInputStream(new FileInputStream(cacheFile)));
        } else if (dataCaching.getValue() == 0) {
            writer = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(cacheFile)));
        }
    }

    public void run() throws IOException {
        //data is read from cache file
        if (dataCaching.getValue() == 1) {
            dataValue.setValue(reader.readDouble());
        } else {
            double[] regCoeff = this.regCoeff.getValue();
            double gradient = regCoeff[1];
            double rsq = regCoeff[2];

            double[] sourceElevations = statElevation.getValue();
            double[] sourceData = dataArray.getValue();

            double[] sourceWeights = statWeights.getValue();
            double targetElevation = entityElevation.getValue();

            double value = 0;
            double deltaElev = 0;
            int nIDW = this.nidw.getValue();

            double[] data = new double[nIDW];
            double[] weights = new double[nIDW];
            double[] elev = new double[nIDW];
            //make sure that the arrays are intialized with 0s
            for (int i = 0; i < nIDW; i++) {
                data[i] = 0;
                weights[i] = 0;
                elev[i] = 0;
            }

//@TODO: Recheck this for correct calculation, the Doug Boyle Problem!!

            //Retreiving data, elevations and weights
            int[] wA = this.statOrder.getValue();
            int counter = 0;
            int element = counter;
            boolean cont = true;
            boolean valid = false;

            while (counter < nIDW && cont) {
                int t = wA[element];
                //check if data is valid or no data
                if (sourceData[t] == noData.getValue()) {

                    element++;
                    if (element >= wA.length) {
                        getModel().getRuntime().println("BREAK1: too less data NIDW had been reduced!");
                        cont = false;
                    //value = NODATA;
                    } else {
                        t = wA[element];
                    }
                } else {
                    valid = true;
                    data[counter] = sourceData[t];
                    weights[counter] = sourceWeights[t];
                    elev[counter] = sourceElevations[t];

                    counter++;
                    element++;
                /*if(element >= wA.length){
                if(element <= nIDW)
                System.out.println("NIDW has been reduced, because of too less valid data!");
                cont = false;
                }*/

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

            if (valid) {
                for (int i = 0; i < counter; i++) {
                    if ((rsq >= rsqThreshold.getValue()) && (elevationCorrection.getValue())) {  //Elevation correction is applied
                        deltaElev = targetElevation - elev[i];  //Elevation difference between unit and Station
                        double tVal = ((deltaElev * gradient + data[i]) * weights[i]);
                        //checking for minimum
                        if (tVal < this.fixedMinimum.getValue()) {
                            tVal = this.fixedMinimum.getValue();
                        }
                        value = value + tVal;


                    } else { //No elevation correction

                        value = value + (data[i] * weights[i]);
                    }

                }
            } else {
                //System.out.println("All data are no-data values!");
                getModel().getRuntime().println("Invalid dataset found while regionalizing data in component " + this.getInstanceName());
                value = noData.getValue();
            }

            dataValue.setValue(value);

            //Write cache file
            if (dataCaching.getValue() == 0) {
                writer.writeDouble(value);
            }
        }
    }

    public void cleanup() throws IOException {
        if (dataCaching.getValue() == 0) {
            writer.flush();
            writer.close();
        } else if (dataCaching.getValue() == 1) {
            reader.close();
        }
    }
}
