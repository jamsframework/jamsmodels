/*
 * CalcDailyETP_PenmanMonteith.java
 * Created on 24. November 2005, 13:57
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

package org.unijena.j2k.potET;

import java.io.*;
import org.unijena.jams.data.*;
import org.unijena.jams.model.*;

/**
 *
 * @author Peter Krause
 */

    
    
@JAMSComponentDescription(
        title="CalcDailyETP_Haude",
        author="Peter Krause",
        description="Calculates daily potential ETP after Penman-Monteith"
        )
    
    public class CalcDailyETP_Haude extends JAMSComponent {
    
        
    /*
     *  Component variables
     */
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.INIT,
            description = "Workspace directory name"
            )
            public JAMSString dirName;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.INIT,
            description = "Collection of hru objects"
            )
            public JAMSEntityCollection hrus;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "The current hru entity"
            )
            public JAMSEntity entity;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "Current time"
            )
            public JAMSCalendar time;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.INIT,
            description = "state variable mean temperature"
            )
            public JAMSString aNameTmean;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.INIT,
            description = "state variable relative humidity"
            )
            public JAMSString aNameRhum;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.INIT,
            description = "state variable haude factor"
            )
            public JAMSString aNameHaudeFactor;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.INIT,
            description = "attribute area"
            )
            public JAMSString aNameArea;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.INIT,
            description = "daily potential ETP [mm/d]"
            )
            public JAMSString aNamePotETP;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.INIT,
            description = "daily actual ETP [mm/d]"
            )
            public JAMSString aNameActETP;
    
    private File cacheFile;
    private boolean useCache = false;
    private ObjectOutputStream writer;
    private ObjectInputStream reader;
    
    /*
     *  Component run stages
     */
    
    public void init() throws JAMSEntity.NoSuchAttributeException, IOException {
        //first, check if cached data are available
        cacheFile = new File(dirName.getValue() + "/$" + this.getInstanceName() + ".cache");
        if (cacheFile.exists() && false) {
            useCache = true;
                       
            reader = new ObjectInputStream(new BufferedInputStream(new FileInputStream(cacheFile)));//new FileInputStream(cacheFile));
        } else {
            useCache = false;
            writer = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(cacheFile)));
        }
    }
    
    public void run() throws JAMSEntity.NoSuchAttributeException, IOException {
        if (!useCache) {
            int month = time.get(time.MONTH) + 1;
            double temperature = entity.getDouble(aNameTmean.getValue());
            double rhum = entity.getDouble(aNameRhum.getValue());
            double area = entity.getDouble(aNameArea.getValue());
            String hFactStr = aNameHaudeFactor.getValue() + month;
            double h_factor = entity.getDouble(hFactStr);
            double est = org.unijena.j2k.physicalCalculations.ClimatologicalVariables.calc_saturationVapourPressure(temperature);
            //kPa -> hPa
            est = 10 * est;
                        
            double pETP = est * (1 - (rhum/100.)) * h_factor; 
            
            double aETP = 0;
            
            //converting mm to litres
            pETP = pETP * area;
            
            //avoiding negative potETPs
            if(pETP < 0){
                pETP = 0;
            }
            
            //conversion from daily to hourly values
            pETP = pETP / 24;
            
            entity.setDouble(aNamePotETP.getValue(), pETP);
            writer.writeDouble(pETP);
            entity.setDouble(aNameActETP.getValue(), aETP);
        } 
        else {
            entity.setDouble(aNamePotETP.getValue(), reader.readDouble());
            entity.setDouble(aNameActETP.getValue(), 0);
        }
    }
    
    public void cleanup()  throws IOException {
        if (!useCache) {
            writer.flush();
            writer.close();
        } else {
            reader.close();
        }
    }
}
