/*
 * EntityReader.java
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
package org.unijena.scn;
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
        title="EntityReader",
        author="Peter Krause",
        description="Reader for spatial entities of catchment"
        )
        
        public class EntityReader extends JAMSComponent {
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.INIT,
            description = "Workspace directory name"
            )
            public JAMSString dirName;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.INIT,
            description = "entity parameter file name"
            )
            public JAMSString entityFileName;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.INIT,
            description = "CN mulitplier"
            )
            public JAMSDouble cnMulti;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            update = JAMSVarDescription.UpdateType.RUN,
            unit = "km^2",
            description = "the entire area of the catchment"
            )
            public JAMSDouble catchmentArea;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "catchment CN value"
            )
            public JAMSDouble cnValue;
    
    public void init() throws JAMSEntity.NoSuchAttributeException {
        
    }
    
    public void run() throws JAMSEntity.NoSuchAttributeException {
        Vector<String> attributeNames = new Vector<String>();
        Vector<Double> areaValues = new Vector<Double>();
        Vector<Double> cnValues = new Vector<Double>();
        try {
            
            BufferedReader reader = new BufferedReader(new FileReader(dirName.getValue()+"/"+entityFileName.getValue()));
            
            String s = "#";
            
            // get rid of comments
            while (s.startsWith("#")) {
                s = reader.readLine();
            }
            
            //put the attribure names into a vector
            
            StringTokenizer tokenizer = new StringTokenizer(s, "\t");
            while (tokenizer.hasMoreTokens()) {
                attributeNames.add(tokenizer.nextToken());
            }
            
            //get first line of hru data
            s = reader.readLine();
            
            while ((s != null) && !s.startsWith("#"))  {
                tokenizer = new StringTokenizer(s, "\t");
                String token;
                for (int i = 0; i < attributeNames.size(); i++) {
                    token = tokenizer.nextToken();
                    if(attributeNames.get(i).equals("HID")){
                        //System.out.println("do nothing " + i);
                    } else if(attributeNames.get(i).equals("Area")){
                        areaValues.add(new Double(token));
                        //System.out.println("getArea");
                    } else if(attributeNames.get(i).equals("CN")){
                        cnValues.add(new Double(token));
                        //System.out.println("getCN");
                    }
                }
                s = reader.readLine();
            }
        } catch (IOException ioe) {
            System.out.println("IOError in entity reader!");
        }
        
        double areaSum = 0;
        double meanCN = 0;
        for (int i = 0; i < areaValues.size(); i++) {
            areaSum = areaSum + areaValues.get(i).doubleValue();
        }
        for (int i = 0; i < areaValues.size(); i++) {
            meanCN += areaValues.get(i).doubleValue() / areaSum * cnValues.get(i);
        }
        double mCN = meanCN * this.cnMulti.getValue();
        if(mCN > 100)
            mCN = 100;
        this.catchmentArea.setValue(areaSum);
        this.cnValue.setValue(mCN);
        
        System.out.println("AreaSum: " + areaSum + " cnValue: " + mCN);
        
    }
    
}
