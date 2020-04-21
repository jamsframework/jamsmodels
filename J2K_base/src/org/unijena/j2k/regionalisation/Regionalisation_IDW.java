/*
 * Regionalisation_IDW.java
 * Created on 27.11.2019, 14:56:33
 *
 * This file is part of JAMS
 * Copyright (C) FSU Jena
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
package org.unijena.j2k.regionalisation;

import jams.JAMS;
import jams.data.*;
import jams.model.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import org.unijena.j2k.statistics.IDW;

/**
 *
 * @author Sven Kralisch <sven.kralisch@uni-jena.de>
 */
@JAMSComponentDescription(title = "Regionalisation_IDW",
        author = "Sven Kraisch based on Regionalisation.java by Peter Krause",
        version = "1.0_0",
        date = "2019-11-27",
        description = "Calculate local (regionalised) input values based on the "
        + "inverse distance weighting procedure.")
@VersionComments(entries = {
    @VersionComments.Entry(version = "1.0_0", comment = "Initial version")
})
public class Regionalisation_IDW extends JAMSComponent {

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
            description = "Array of data values for current time step")
    public Attribute.DoubleArray dataArray;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
            description = "Regression coefficients")
    public Attribute.DoubleArray regCoeff;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.INIT,
            description = "Array of station elevations")
    public Attribute.DoubleArray statElevation;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.INIT,
            description = "Array of stations' x coordinates")
    public Attribute.DoubleArray statXCoord;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.INIT,
            description = "Array of stations' y coordinates")
    public Attribute.DoubleArray statYCoord;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
            description = "Attribute name elevation")
    public Attribute.Double entityElevation;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.INIT,
            description = "Entity's x coordinate")
    public Attribute.Double entityXCoord;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.INIT,
            description = "Entity's y coordinate")
    public Attribute.Double entityYCoord;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
            description = "Max. number of nearest stations to be considered in IDW",
            defaultValue = "1000")
    public Attribute.Integer nidw;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
            description = "Power of IDW function",
            defaultValue = "1")
    public Attribute.Double pidw;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
            description = "Weights for Thiessen polygons",
            defaultValue = "false")
    public Attribute.Boolean equalWeights;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
            description = "Calculation with geographical coordinates lat, long",
            defaultValue = "false")
    public Attribute.Boolean latLong;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
            description = "Apply elevation correction to measured data")
    public Attribute.Boolean elevationCorrection;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
            description = "Minimum r² value for elevation correction application")
    public Attribute.Double rsqThreshold;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
            description = "Absolute possible minimum value for data set",
            defaultValue = "-Infinity")
    public Attribute.Double fixedMinimum;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
            description = "Absolute possible maximum value for data set",
            defaultValue = "Infinity")
    public Attribute.Double fixedMaximum;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READWRITE,
            description = "Array of station weights")
    public Attribute.DoubleArray statWeights;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READWRITE,
            description = "Array position of weights")
    public Attribute.IntegerArray statOrder;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READWRITE,
            description = "Array io stations distances")
    public Attribute.DoubleArray statDistance;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.WRITE,
            description = "regionalised data value")
    public Attribute.Double dataValue;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.WRITE,
            description = "Calculate statis values for IDW (see next three variables)?",
            defaultValue = "false")
    public Attribute.Boolean calcStats;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.WRITE,
            description = "Weights of individual stations (first element equals first station in list)")
    public Attribute.Double[] actualWeights;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.WRITE,
            description = "Weighted average elevation of source stations")
    public Attribute.Double averageSourceElevation;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.WRITE,
            description = "Weighted average distance of source stations")
    public Attribute.Double averageSourceDistance;

    boolean invalidDatasetReported = false;

    ArrayPool<double[]> memPool = new ArrayPool<>(double.class);
    ArrayPool<int[]> imemPool = new ArrayPool<>(int.class);
    IDW idw = new IDW();

    @Override
    public void initAll() {

        double[] statX = statXCoord.getValue();
        double[] statY = statYCoord.getValue();
        double entityX = entityXCoord.getValue();
        double entityY = entityYCoord.getValue();
        double power = this.pidw.getValue();
        
        // number of stations
        int nStations = statX.length;
        double[] weights = new double[nStations];
        double[] dists = new double[nStations];
        int[] order = new int[nStations];

        // calc weights
        if (equalWeights.getValue()) {
            for (int i = 0; i < nStations; i++) {
                weights[i] = 1 / nStations;
            }
        } else {

            // calc distances for lat/long or metric coordinates
            if (latLong.getValue()) {
                for (int i = 0; i < nStations; i++) {
                    dists[i] = getDistLatLong(entityX, entityY, statX[i], statY[i]);
                }
            } else {
                for (int i = 0; i < nStations; i++) {
                    dists[i] = getDist(entityX, entityY, statX[i], statY[i]);
                }
            }

            // get sum of all dists
            double sum = 0;
            for (int i = 0; i < nStations; i++) {
                sum += dists[i];
            }

            // calc weights
            int sameLocation = -1;
            for (int i = 0; i < nStations; i++) {
                if (dists[i] != 0) {
                    weights[i] = Math.pow(sum / dists[i], power);
                } else {
                    sameLocation = i;
                    break;
                }
            }

            // check whether one station is at the same location
            // if so, set its weight to 1 and all others to 0
            if (sameLocation > -1) {
                for (int i = 0; i < nStations; i++) {
                    if (sameLocation == i) {
                        weights[i] = 1;
                    } else {
                        weights[i] = 0;
                    }
                }
            }
        }

        // create ordered ID array      
        List<Station> stations = new ArrayList(nStations);
        for (int i = 0; i < nStations; i++) {
            stations.add(new Station(i, weights[i]));
        }
        Collections.sort(stations, new Comparator<Station>() {
            @Override
            public int compare(Station o1, Station o2) {
                if ((o1.weight - o2.weight) > 0) {
                    return -1;
                } else if ((o1.weight - o2.weight) < 0) {
                    return 1;
                } else {
                    return 0;
                }
            }
        });
        int i = 0;
        for (Station s : stations) {
            order[i++] = s.id;
        }

        if (calcStats.getValue()) {
            statDistance.setValue(dists);
        }
        statWeights.setValue(weights);
        statOrder.setValue(order);
    }

    @Override
    public void run() {

        //Retreiving data, elevations and weights
        double[] rc = this.regCoeff.getValue();
        double gradient = rc[1];
        double rsq = rc[2];

        double[] sourceElevations = statElevation.getValue();
        double[] sourceData = dataArray.getValue();
        double[] sourceWeights = statWeights.getValue();
        double targetElevation = entityElevation.getValue();
        int[] wA = this.statOrder.getValue();

        double value = 0;
        double deltaElev = 0;

        // set upper boundary to nidw or number of stations
        int nIDW = Math.min(this.nidw.getValue(), wA.length);
//        int nIDW = this.nidw.getValue();
        double[] data = memPool.alloc(nIDW);
        double[] weights = memPool.alloc(nIDW);
        double[] elev = memPool.alloc(nIDW);
        int[] source = imemPool.alloc(nIDW);

        double[] dist = null;
        double[] sourceDistances = null;
        if (calcStats.getValue()) {
            sourceDistances = statDistance.getValue();
            dist = memPool.alloc(nIDW);
            if (actualWeights != null) {
                for (Attribute.Double w : actualWeights) {
                    w.setValue(0);
                }
            }
        }

        int counter = 0, element = 0;
        boolean valid = false;

        //identify the source locations to be used for IDW
        while (counter < nIDW) {
            int col = wA[element];
            //check if data is valid or not
            if (sourceData[col] == JAMS.getMissingDataValue()) {
                element++;
            } else {
                valid = true;
                source[counter] = col;
                data[counter] = sourceData[col];
                weights[counter] = sourceWeights[col];
                elev[counter] = sourceElevations[col];
                if (calcStats.getValue()) {
                    dist[counter] = sourceDistances[col];
                }
                counter++;
                element++;
            }
            if (element >= wA.length) {
                break;
            }
        }

        //apply IDW if data are available
        if (valid) {

            //normalize weights
            double weightsum = 0;
            for (int i = 0; i < counter; i++) {
                weightsum += weights[i];
            }
            for (int i = 0; i < counter; i++) {
                weights[i] = weights[i] / weightsum;
            }

            //apply weights and calc actual value
            for (int i = 0; i < counter; i++) {
                if ((elevationCorrection.getValue()) && (rsq >= rsqThreshold.getValue())) {  //Elevation correction is applied
                    deltaElev = targetElevation - elev[i];  //Elevation difference between unit and Station
                    double tVal = ((deltaElev * gradient + data[i]) * weights[i]);
                    value = value + tVal;
                } else { //No elevation correction
                    value = value + (data[i] * weights[i]);
                }
            }

            //ensure upper/lower bounds
            value = Math.max(value, fixedMinimum.getValue());
            value = Math.min(value, fixedMaximum.getValue());

            //store weights, avg elevation if stats wanted
            if (calcStats.getValue()) {

                double avgElev = 0, avgDist = 0;
                for (int i = 0; i < counter; i++) {
                    avgElev += elev[i] * weights[i];
                    avgDist += dist[i] * weights[i];
                    if (actualWeights != null) {
                        actualWeights[source[i]].setValue(weights[i]);
                    }
                }
                averageSourceElevation.setValue(avgElev);
                averageSourceDistance.setValue(avgDist);
            }

        } else {

            //only report once
            if (!invalidDatasetReported) {

                getModel().getRuntime().sendInfoMsg("Invalid dataset found while regionalizing data in component " + this.getInstanceName() + "."
                        + "\nThis might occur if all of the provided values are missing data values.");
                invalidDatasetReported = true;

            }
            value = JAMS.getMissingDataValue();
        }

        dataValue.setValue(value);

        //free data
        data = memPool.free(data);
        weights = memPool.free(weights);
        elev = memPool.free(elev);
        if (calcStats.getValue()) {
            dist = memPool.free(dist);
        }
    }

    private static class Station {

        int id;
        double weight;

        public Station(int id, double weight) {
            this.id = id;
            this.weight = weight;
        }
    }

    private double getDist(double x1, double y1, double x2, double y2) {
        double dx = x1 - x2;
        double dy = y1 - y2;
        return Math.sqrt(dx * dx + dy * dy);
    }

    private double getDistLatLong(double x1, double y1, double x2, double y2) {
        //radius at the equator in meter
        final double R = 6378137.0;
        return R * Math.acos(Math.sin(rad(y1)) * Math.sin(rad(y2))
                + Math.cos(rad(y1)) * Math.cos(rad(y2))
                * Math.cos(rad(x2) - rad(x1)));
    }

    private static double rad(double decDeg) {
        return (decDeg * Math.PI / 180.);
    }

}
