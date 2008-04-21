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
import org.unijena.jams.JAMS;
import org.unijena.jams.data.*;
import org.unijena.jams.model.*;

/**
 *
 * @author Peter Krause
 */
public class Regionalisation_1 extends JAMSComponent {
    
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.INIT,
            description = "Workspace directory name"
            )
            public JAMSString dirName;
    
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "Array of data values for current time step"
            )
            public JAMSDoubleArray dataArray;
    
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "Regression coefficients"
            )
            public JAMSDoubleArray regCoeff = new JAMSDoubleArray();
    
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "Array of station elevations"
            )
            public JAMSDoubleArray statElevation = new JAMSDoubleArray();
    
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "data set descriptor"
            )
            public JAMSString dataSetName;
    
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "Array of station's x coordinates"
            )
            public JAMSDoubleArray statX = new JAMSDoubleArray();
    
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "Array of station's y coordinates"
            )
            public JAMSDoubleArray statY = new JAMSDoubleArray();
    
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "Array of station's weights"
            )
            public JAMSDoubleArray statWeights = new JAMSDoubleArray();
    
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "Array position of weights"
            )
            public JAMSIntegerArray wArray = new JAMSIntegerArray();
    
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.INIT,
            description = "Attribute name x coordinate (hru)"
            )
            public JAMSDouble unitX;
    
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.INIT,
            description = "Attribute name y coordinate (hru)"
            )
            public JAMSDouble unitY;
    
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.WRITE,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "regionalised data value"
            )
            public JAMSDouble dataValue;
    
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "Attribute name elevation"
            )
            public JAMSDouble entityElevation;
    
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.INIT,
            description = "Number of IDW stations"
            )
            public JAMSInteger nidw;
    
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.INIT,
            description = "Apply elevation correction to measured data"
            )
            public JAMSBoolean elevationCorrection;
    
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.INIT,
            description = "Minimum r² value for elevation correction application"
            )
            public JAMSDouble rsqThreshold;
    
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.INIT,
            description = "Absolute possible minimum value for data set"
            )
            public JAMSDouble fixedMinimum;
    
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.INIT,
            description = "Use caching of regionalised data?"
            )
            public JAMSBoolean dataCaching;
    
    private File cacheFile;
    private boolean useCache = false;
    private boolean writeCache = false;
    private ObjectOutputStream writer;
    private ObjectInputStream reader;
    double NODATA = -9999;
    
    public void init() throws JAMSEntity.NoSuchAttributeException, IOException {
        
        //first, check if cached data are available
        cacheFile = new File(dirName.getValue() + "/$" + this.getInstanceName() + ".cache");
        
        if (!cacheFile.exists() && dataCaching.getValue()) {
            getModel().getRuntime().println(this.getInstanceName() + ": data caching is switched on but no cache file available!", JAMS.STANDARD);
            writeCache = true;
        }
        
        //cache file existent and cache should be used
        if (dataCaching.getValue() && !writeCache) {
            useCache = true;
            reader = new ObjectInputStream(new BufferedInputStream(new FileInputStream(cacheFile)));//new FileInputStream(cacheFile));
            writer = null;
        } 
        //cache file not existent but cache should be used
        else if (dataCaching.getValue() && writeCache) {
            useCache = false;
            writer = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(cacheFile)));
        }
        //cache should not be used
        else {
            useCache = false;
            writer = null;
        }
    }
    
    public void run() throws JAMSEntity.NoSuchAttributeException, IOException {
        
        
        if (!useCache) {
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
            for(int i = 0; i < nIDW;i++){
                data[i] = 0;
                weights[i] = 0;
                elev[i] = 0;
            }
            
//@TODO: Recheck this for correct calculation, the Doug Boyle Problem!!
            
            //Retreiving data, elevations and weights
            int[] wA = this.wArray.getValue();
            int counter = 0;
            int element = counter;
            boolean cont = true;
            boolean valid = false;
            
            while(counter < nIDW && cont){
                int t = wA[element];
                //check if data is valid or no data
                if(sourceData[t] == NODATA){
                    element++;
                    if(element >= wA.length){
                        System.out.println("BREAK1: too less data NIDW had been reduced!");
                        cont = false;
                        //value = NODATA;
                    } else{
                        t = wA[element];
                    }
                } else{
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
            for(int i = 0; i < counter; i++)
                weightsum += weights[i];
            for(int i = 0; i < counter; i++)
                weights[i] = weights[i] / weightsum;
            
            if(valid){
                for (int i = 0; i < counter; i++) {
                    if((rsq >= rsqThreshold.getValue()) && (elevationCorrection.getValue())) {  //Elevation correction is applied
                        deltaElev = targetElevation - elev[i];  //Elevation difference between unit and Station
                        double tVal = ((deltaElev * gradient + data[i]) * weights[i]);
                        //checking for minimum
                        if(tVal < this.fixedMinimum.getValue())
                            tVal = this.fixedMinimum.getValue();
                        value = value + tVal;
                        
                        
                    } else{ //No elevation correction
                        
                        value = value + (data[i] * weights[i]);
                    }
                    
                }
            } else{
                //System.out.println("All data are no-data values!");
                value = NODATA;
            }
            
            dataValue.setValue(value);
            //System.out.getRuntime().println("R2 entity: "+ targetElevation + "weights: " + sourceWeights[0] + ", "+ sourceWeights[1] + ", "+ sourceWeights[2] + ", ");
            if(writer != null)
                writer.writeDouble(value);
            
            
        } else {
            dataValue.setValue(reader.readDouble());
        }
    }
    
    public void cleanup() throws IOException {
        if (!useCache && writeCache) {
            writer.flush();
            writer.close();
        } else if(useCache && !writeCache){
            reader.close();
        }
    }
}
