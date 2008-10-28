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
import jams.JAMSTools;
import jams.data.*;
import jams.model.*;

/**
 *
 * @author S. Kralisch
 */
public class Regionalisation extends JAMSComponent {

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    update = JAMSVarDescription.UpdateType.RUN,
    description = "Array of data values for current time step")
    public JAMSDoubleArray dataArray;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    update = JAMSVarDescription.UpdateType.RUN,
    description = "Regression coefficients")
    public JAMSDoubleArray regCoeff = new JAMSDoubleArray();

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    update = JAMSVarDescription.UpdateType.RUN,
    description = "Array of station elevations")
    public JAMSDoubleArray statElevation = new JAMSDoubleArray();

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    update = JAMSVarDescription.UpdateType.RUN,
    description = "data set descriptor")
    public JAMSString dataSetName;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    update = JAMSVarDescription.UpdateType.RUN,
    description = "Array of station's x coordinates")
    public JAMSDoubleArray statX = new JAMSDoubleArray();

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    update = JAMSVarDescription.UpdateType.RUN,
    description = "Array of station's y coordinates")
    public JAMSDoubleArray statY = new JAMSDoubleArray();

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    update = JAMSVarDescription.UpdateType.RUN,
    description = "Array of station's weights")
    public JAMSDoubleArray statWeights = new JAMSDoubleArray();

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    update = JAMSVarDescription.UpdateType.INIT,
    description = "Attribute name x coordinate (hru)")
    public JAMSDouble unitX;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    update = JAMSVarDescription.UpdateType.INIT,
    description = "Attribute name y coordinate (hru)")
    public JAMSDouble unitY;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.WRITE,
    update = JAMSVarDescription.UpdateType.RUN,
    description = "regionalised data value")
    public JAMSDouble dataValue;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    update = JAMSVarDescription.UpdateType.RUN,
    description = "Attribute name elevation")
    public JAMSDouble entityElevation;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    update = JAMSVarDescription.UpdateType.INIT,
    description = "Number of IDW stations")
    public JAMSInteger nidw;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    update = JAMSVarDescription.UpdateType.INIT,
    description = "Power of IDW function")
    public JAMSDouble pidw;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    update = JAMSVarDescription.UpdateType.INIT,
    description = "Apply elevation correction to measured data")
    public JAMSBoolean elevationCorrection;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    update = JAMSVarDescription.UpdateType.INIT,
    description = "Minimum r² value for elevation correction application")
    public JAMSDouble rsqThreshold;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    update = JAMSVarDescription.UpdateType.INIT,
    description = "Caching configuration: 0 - write cache, 1 - use cache, 2 - caching off",
    defaultValue = "0")
    public JAMSInteger dataCaching;

    private File cacheFile;

    transient private ObjectOutputStream writer;

    transient private ObjectInputStream reader;

    public void init() throws JAMSEntity.NoSuchAttributeException, IOException {

        //first, check if cached data are available
        cacheFile = new File(getModel().getWorkspace().getTempDirectory(), this.getInstanceName() + ".cache");

        if (!cacheFile.exists() && (dataCaching.getValue() == 1)) {
            getModel().getRuntime().sendHalt(this.getInstanceName() + ": data caching is switched on but no cache file available!");
        }

        if (dataCaching.getValue() == 1) {

            reader = new ObjectInputStream(new BufferedInputStream(new FileInputStream(cacheFile)));//new FileInputStream(cacheFile));

        } else if (dataCaching.getValue() == 0) {
            //checking validity of input information
            //int nstat = statX.getValue().length;
            //if(nidw.getValue() > nstat){
            //    System.out.getRuntime().println("Number of stations is smaller than parameter nidw");
            //}

            writer = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(cacheFile)));

        //JAMSDoubleArray idwWeights = new JAMSDoubleArray();
        //idwWeights.setValue(IDW.calcNidwWeights(unitX.getValue(), unitY.getValue(), statX.getValue(), statY.getValue(), pidw.getValue(), nidw.getValue()));

        //statWeights.setValue(idwWeights.getValue());

        }
    }

    public void run() throws JAMSEntity.NoSuchAttributeException, IOException {

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
//@TODO: Recheck this for correct calculation, the Doug Boyle Problem!!
            for (int i = 0; i < sourceElevations.length; i++) {
                if ((rsq >= rsqThreshold.getValue()) && (elevationCorrection.getValue())) {  //Elevation correction is applied
                    deltaElev = targetElevation - sourceElevations[i];  //Elevation difference between unit and Station
                    double tVal = ((deltaElev * gradient + sourceData[i]) * sourceWeights[i]);
                    value = value + tVal;


                } else { //No elevation correction
                    value = value + (sourceData[i] * sourceWeights[i]);
                }

            }

            dataValue.setValue(value);
            //System.out.getRuntime().println("R2 entity: "+ targetElevation + "weights: " + sourceWeights[0] + ", "+ sourceWeights[1] + ", "+ sourceWeights[2] + ", ");

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
