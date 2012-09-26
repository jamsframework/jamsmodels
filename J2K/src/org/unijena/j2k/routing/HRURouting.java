/*
 * J2KProcessRouting.java
 * Created on 28. November 2005, 09:21
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

package org.unijena.j2k.routing;

import jams.data.*;
import jams.model.*;

/**
 *
 * @author Peter Krause
 */
@JAMSComponentDescription(
        title="HRURouting",
        author="Peter Krause",
        description="Streamlined version of J2KProcessRouting with flexible "
        + "attribute names. Passes the output of the entities as input to the"
        + " respective reach or unit",
        version="1.0_0",
        date="2012-09-26"
        )
        public class HRURouting extends JAMSComponent {
    
    /*
     *  Component variables
     */
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Downstream hru entity"
            )
            public Attribute.Entity toPoly;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Downstream reach entity"
            )
            public Attribute.Entity toReach;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Reservoir entity"
            )
            public Attribute.Entity toReservoir;        
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "RD1 outflow from modellig entity",
            unit = "L"
            )
            public Attribute.Double outRD1;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "RD2 outflow from modellig entity",
            unit = "L"
            )
            public Attribute.Double outRD2;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "RG1 outflow from modellig entity",
            unit = "L"
            )
            public Attribute.Double outRG1;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "RG2 outflow from modellig entity",
            unit = "L"
            )
            public Attribute.Double outRG2;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "groundwater excess as input to modelling entity",
            unit = "L",
            defaultValue= "0"
            )
            public Attribute.Double inGWExcess;    
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "RD1 inflow attribute name for HRUs / reaches"
            )
            public Attribute.String inRD1Name;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "RD2 inflow attribute name for HRUs / reaches"
            )
            public Attribute.String inRD2Name;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "RG1 inflow attribute name for HRUs / reaches"
            )
            public Attribute.String inRG1Name;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "RG2 inflow attribute name for HRUs / reaches"
            )
            public Attribute.String inRG2Name;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "RD1 component attribute name for reservoirs"
            )
            public Attribute.String compRD1Name;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "RD2 component attribute name for reservoirs"
            )
            public Attribute.String compRD2Name;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "RG1 component attribute name for reservoirs"
            )
            public Attribute.String compRG1Name;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "RG2 component attribute name for reservoirs"
            )
            public Attribute.String compRG2Name;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "HRU id"
            )
            public Attribute.Double id;    
    /*
     *  Component run stages
     */
    
    public void init() throws Attribute.Entity.NoSuchAttributeException {
    }

    public void run() throws Attribute.Entity.NoSuchAttributeException {

        double RD1out = outRD1.getValue();
        double RD2out = outRD2.getValue();
        double RG1out = outRG1.getValue();
        double RG2out = outRG2.getValue();
        
        
        if(toPoly.getValue() != null){
            double RD1in = toPoly.getDouble(inRD1Name.getValue());
            double RD2in = toPoly.getDouble(inRD2Name.getValue());
            double RG1in = toPoly.getDouble(inRG1Name.getValue());
            double RG2in = toPoly.getDouble(inRG2Name.getValue());
            
            RD1in = RD1in + RD1out;
            RD2in = RD2in + RD2out;
            RG1in = RG1in + RG1out;
            RG2in = RG2in + RG2out;
          
            RD2in += inGWExcess.getValue();
            
            outRD1.setValue(0);
            outRD2.setValue(0);
            outRG1.setValue(0);
            outRG2.setValue(0);
            inGWExcess.setValue(0);
            
            toPoly.setDouble(inRD1Name.getValue(), RD1in);
            toPoly.setDouble(inRD2Name.getValue(), RD2in);
            toPoly.setDouble(inRG1Name.getValue(), RG1in);
            toPoly.setDouble(inRG2Name.getValue(), RG2in);
            
        } else if(toReach.getValue() != null){
            
            double RD1in = toReach.getDouble(inRD1Name.getValue());
            double RD2in = toReach.getDouble(inRD2Name.getValue());
            double RG1in = toReach.getDouble(inRG1Name.getValue());
            double RG2in = toReach.getDouble(inRG2Name.getValue());
            
            RD1in = RD1in + RD1out;
            RD2in = RD2in + RD2out;
            RG1in = RG1in + RG1out;
            RG2in = RG2in + RG2out;
            RD2in += inGWExcess.getValue();
            
            RD1out = 0;
            RD2out = 0;
            RG1out = 0;
            RG2out = 0;
            
            outRD1.setValue(RD1out);
            toReach.setDouble(inRD1Name.getValue(), RD1in);
            outRD2.setValue(RD2out);
            toReach.setDouble(inRD2Name.getValue(), RD2in);
            outRG1.setValue(RG1out);
            toReach.setDouble(inRG1Name.getValue(), RG1in);
            outRG2.setValue(RG2out);
            inGWExcess.setValue(0);
            toReach.setDouble(inRG2Name.getValue(), RG2in);
            
        }else if(toReservoir != null){
            double resRD1 = toReservoir.getDouble(compRD1Name.getValue());
            double resRD2 = toReservoir.getDouble(compRD2Name.getValue());
            double resRG1 = toReservoir.getDouble(compRG1Name.getValue());
            double resRG2 = toReservoir.getDouble(compRG2Name.getValue());
            
            resRD1 = resRD1 + RD1out;
            resRD2 = resRD2 + RD2out;
            resRG1 = resRG1 + RG1out;
            resRG2 = resRG2 + RG2out;
            
            RD1out = 0;
            RD2out = 0;
            RG1out = 0;
            RG2out = 0;
            
            outRD1.setValue(RD1out);
            toReservoir.setDouble(compRD1Name.getValue(), resRD1);
            outRD2.setValue(RD2out);
            toReservoir.setDouble(compRD2Name.getValue(), resRD2);
            outRG1.setValue(RG1out);
            toReservoir.setDouble(compRG1Name.getValue(), resRG1);
            outRG2.setValue(RG2out);
            toReservoir.setDouble(compRG2Name.getValue(), resRG2);
        } 
        else{
            getModel().getRuntime().println("Current entity ID: " + id + " has no receiver.");
        }
        
    }
    
    public void cleanup() {
        
    }
}
