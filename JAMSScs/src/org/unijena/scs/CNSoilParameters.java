/*
 * CNSoilParameters.java
 * Created on 17. July 2006, 17:15
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
package org.unijena.scs;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.StringTokenizer;
import java.util.Vector;

import org.unijena.jams.model.*;
import org.unijena.jams.data.*;

/**
 *
 * @author P. Krause
 */
@JAMSComponentDescription(
        title="CN-SoilParameters",
        author="Peter Krause",
        description="Preliminary class for estimation of soil CN values"
        )
public class CNSoilParameters extends JAMSComponent {
    
	@JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.INIT,
            description = "Workspace directory name"
            )
            public JAMSString dirName;
	
	@JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.INIT,
            description = "CN-Number parameter file name"
            )
            public JAMSString cnFileName;
	
	@JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.INIT,
            description = "HRUs parameter file name"
            )
            public JAMSString hruFileName;
	
	@JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "catchment CN value"
            )
            public JAMSDouble cnValue;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.INIT,
            description = "catchment CN setter"
            )
            public JAMSDouble cnSetter;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "catchment area"
            )
            public JAMSDouble catchmentArea;
    
    int luEntries = 0;
    int hruEntries = 0;
    
    public void init() throws JAMSEntity.NoSuchAttributeException {
       
        
    } 
    
    public void run() throws JAMSEntity.NoSuchAttributeException {
    	this.catchmentArea.setValue(0);
        this.cnValue.setValue(0);
        
        luEntries = 0;
        hruEntries = 0;
        
         //cnValue.setValue(cnSetter.getValue());
    	String cnFN = this.dirName.getValue() + java.io.File.separator + this.cnFileName.getValue();
    	String hruFN = this.dirName.getValue() + java.io.File.separator + this.hruFileName.getValue();
    	readParameters(cnFN, hruFN, this.getModel());
    	
    	getModel().getRuntime().println("Einzugsgebietsgröße [km²]: " + this.catchmentArea.getValue());
    }
    
    public void readParameters(String cnFileName, String hruFileName, JAMSModel model){
    	BufferedReader cnReader, hruReader;
        StringTokenizer tokenizer;
        
        //scan the cn-file to get number of entries
        try{
        	cnReader = new BufferedReader(new FileReader(cnFileName));
        	String line = "#";
        	// get rid of comments
            while (line.startsWith("#")) {
                line = cnReader.readLine();
            }
            
            // get first line of data
            line = cnReader.readLine();
            // counting entries
            while (line != null)  {
                luEntries++;
                line = cnReader.readLine();
            }
        	
            cnReader.close();
        }catch (IOException ioe) {
            model.getRuntime().handle(ioe);
        }
        
        // scan the hru-file to get number of entries
        try{
        	hruReader = new BufferedReader(new FileReader(hruFileName));
        	String line = "#";
        	// get rid of comments
            while (line.startsWith("#")) {
                line = hruReader.readLine();
            }
            
            // get first line of data
            line = hruReader.readLine();
            // counting entries
            while (line != null)  {
                hruEntries++;
                line = hruReader.readLine();
            }
        	
            hruReader.close();
        }catch (IOException ioe) {
            model.getRuntime().handle(ioe);
        }
        
        //matrix to read in the cn file values
        double[][] cnMatrix = new double[luEntries][5];
        
        //scan the cn-file to set up cnMatrix
        try{
        	cnReader = new BufferedReader(new FileReader(cnFileName));
        	String line = "#";
        	// get rid of comments
            while (line.startsWith("#")) {
                line = cnReader.readLine();
            }
            // get first line of data
            line = cnReader.readLine();
            // filling the matrix
            int entry = 0;
            while (line != null)  {
                tokenizer = new StringTokenizer(line, "\t");
                //retreiving the landuse id
                cnMatrix[entry][0] = new Double(tokenizer.nextToken()).doubleValue();
                //skipping literal description
                tokenizer.nextToken();
                //retreiving the cn-Values [A to D]
                for(int i = 1; i < 5; i++){
                	cnMatrix[entry][i] = new Double(tokenizer.nextToken()).doubleValue();
                }
                entry++;
                line = cnReader.readLine();
            }
            cnReader.close();
        }catch (IOException ioe) {
            model.getRuntime().handle(ioe);
        }
        
        String[][] hruMatrix = new String[hruEntries][4];
        //scan the hru file to set up hruMatrix
        try {
            hruReader = new BufferedReader(new FileReader(hruFileName));
            
            String line = "#";
            
            // get rid of comments
            while (line.startsWith("#")) {
                line = hruReader.readLine();
            }
            
            //put the attribure names into a vector
            Vector<String> attributeNames = new Vector<String>();
            tokenizer = new StringTokenizer(line, "\t");
            while (tokenizer.hasMoreTokens()) {
                attributeNames.add(tokenizer.nextToken());
            }
            
            //get first line of hru data
            line = hruReader.readLine();
            int currentEntry = 0;
            while (line != null)  {
            	tokenizer = new StringTokenizer(line, "\t");
            	//retreiving the values
            	hruMatrix[currentEntry][0] = tokenizer.nextToken();
            	hruMatrix[currentEntry][1] = tokenizer.nextToken();
            	hruMatrix[currentEntry][2] = tokenizer.nextToken();
            	hruMatrix[currentEntry][3] = tokenizer.nextToken();
                
            	currentEntry++;
                line = hruReader.readLine();
            }
            hruReader.close();
            
        } catch (IOException ioe) {
            model.getRuntime().handle(ioe);
        }
        
        //now everything is put together
        
        //compute catchment area
        for(int i = 0; i < hruEntries; i++){
        	this.catchmentArea.setValue(this.catchmentArea.getValue() + new Double(hruMatrix[i][1]).doubleValue());
        }
        
        //compute catchment cn-value
        for(int i = 0; i < hruEntries; i++){
        	double relArea = new Double(hruMatrix[i][1]).doubleValue() / this.catchmentArea.getValue();
        	int landuseID = new Integer(hruMatrix[i][2]).intValue();
        	String soilType = hruMatrix[i][3];
        	
        	//looking for the correct entry in cnMatrix
        	for(int j = 0; j < luEntries; j++){
        		if(landuseID == (int)cnMatrix[j][0]){
        			if(soilType.equals("A")){
        				this.cnValue.setValue(this.cnValue.getValue() + (cnMatrix[j][1] * relArea));
        				break;
        			}
        			else if(soilType.equals("B")){
        				this.cnValue.setValue(this.cnValue.getValue() + (cnMatrix[j][2] * relArea));
        				break;
        			}
        			else if(soilType.equals("C")){
        				this.cnValue.setValue(this.cnValue.getValue() + (cnMatrix[j][3] * relArea));
        				break;
        			}
        			else if(soilType.equals("D")){
        				this.cnValue.setValue(this.cnValue.getValue() + (cnMatrix[j][4] * relArea));
        				break;
        			}
        			else{
        				getModel().getRuntime().println("Soil entry of HRU " + hruMatrix[i][0] + " is not valid");
        				break;
        			}
        		}
        	}
        }
        double cn = this.cnValue.getValue();
        int cni = (int)cn;
        double rest = cn - cni;
        if(rest >= 0.5)
            cni++;
        this.cnValue.setValue(cni);
    }
    
}
