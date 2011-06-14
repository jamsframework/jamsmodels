/*
 * IDWWeightCalculator.java
 * Created on 7. December 2008, 19:45
 *
 * This file is a JAMS component
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
package org.unijena.j2k.regionalisation;

import jams.data.*;
import jams.model.*;
import jams.workspace.DataSetDefinition;
import jams.workspace.stores.InputDataStore;
import java.io.IOException;
import java.util.ArrayList;
import org.unijena.j2k.statistics.IDW;

/**
 *
 * @author Sven Kralisch <sven.kralisch at uni-jena.de>
 */
@JAMSComponentDescription(title = "IDWWeightCalculator",
                          author = "Peter Krause and Sven Kralisch",
                          date = "2008-12-07",
                          version = "1.0_0",
                          description = "Get stations coordinates from DataStore and calculate " +
"inverse distance weights for the regionalisation procedure. Based on " +
"org.unijena.j2k.regionalization.CalcIDWeights.")
public class IDWWeightCalculator extends JAMSComponent {

    /*
     *  Component variables
     */
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
                        description = "entity x-coordinate")
    public JAMSDouble entityX;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
                        description = "entity y-coordinate")
    public JAMSDouble entityY;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
                        description = "Power of IDW function")
    public JAMSDouble pidw;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.WRITE,
                        description = "weights for IDW part of regionalisation")
    public JAMSDoubleArray statWeights;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.WRITE,
                        description = "position array to determine best weights")
    public JAMSIntegerArray statOrder;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
                        description = "Weights for Thiessen polygons",
                        defaultValue = "false")
    public JAMSBoolean equalWeights;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
                        description = "Calculation with geographical coordinates lat, long",
                        defaultValue = "false")
    public JAMSBoolean latLong;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
                        description = "ID of the datastore to read station coordinates from")
    public JAMSString dataStoreID;

    private double[] statX;

    private double[] statY;
    InputDataStore store = null;
    /*
     *  Component run stages
     */
    public void init() {        
        if (dataStoreID != null) {
            store = getModel().getWorkspace().getInputDataStore(dataStoreID.getValue());
        }

        // check if store exists
        if (store == null) {
            getModel().getRuntime().sendHalt("Error accessing datastore \"" +
                    dataStoreID + "\" from " + getInstanceName() + ": Datastore could not be found!");
            return;
        }

        DataSetDefinition dsDef = store.getDataSetDefinition();
        ArrayList<Object> xList = dsDef.getAttributeValues("X");
        ArrayList<Object> yList = dsDef.getAttributeValues("Y");

        if (xList.size() != yList.size()) {
            getModel().getRuntime().sendHalt("Error accessing datastore \"" +
                    dataStoreID + "\" from " + getInstanceName() + ": Number of x and y coordinates differ!");
            return;
        }

        statX = listToDoubleArray(xList);
        statY = listToDoubleArray(yList);
    }

    public void run() throws JAMSEntity.NoSuchAttributeException {
        double[] idwWeights;
        int[] wA;
        double[] dist;
        if (!equalWeights.getValue()) {
            if (!latLong.getValue()) {
                dist = IDW.calcDistances(entityX.getValue(), entityY.getValue(), statX, statY, pidw.getValue());
            } else {
                dist = IDW.calcLatLongDistances(entityX.getValue(), entityY.getValue(), statX, statY, pidw.getValue());
            }
            idwWeights = IDW.calcWeights(dist);
            wA = IDW.computeWeightArray(idwWeights);

        } else {
            int nstat = statX.length;
            idwWeights = IDW.equalWeights(nstat);
            int[] tmp = new int[nstat];
            for (int i = 0; i < nstat; i++) {
                tmp[i] = i;
            }
            wA = tmp;
        }

        statWeights.setValue(idwWeights);
        statOrder.setValue(wA);
    }

    public void cleanup() throws JAMSEntity.NoSuchAttributeException {
        int nstat = statWeights.getValue().length;
        double[] sw = new double[nstat];
        for (int i = 0; i < nstat; i++) {
            sw[i] = 0;
        }
        statWeights.setValue(sw);
        if (store!=null){
            try{
                store.close();
            }catch(IOException ioe){
                ioe.printStackTrace();
            }
        }
    }

    private double[] listToDoubleArray(ArrayList<Object> list) {
        double[] result = new double[list.size()];
        int i = 0;
        for (Object o : list) {
            result[i] = ((Double) o).doubleValue();
            i++;
        }
        return result;
    }
}
