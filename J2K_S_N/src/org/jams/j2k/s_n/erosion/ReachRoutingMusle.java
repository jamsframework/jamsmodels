/*
 * J2KProcessReachRouting.java
 * Created on 28. November 2005, 10:01
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

package org.jams.j2k.s_n.erosion;

import jams.data.*;
import jams.model.*;

/**
 *
 * @author c8kiho
 */
@JAMSComponentDescription(
        title="Title",
        author="Author",
        description="Description"
        )
        public class ReachRoutingMusle extends JAMSComponent {
    
    /*
     *  Component variables
     */
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "The reach collection"
            )
            public JAMSEntityCollection entities;

    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "reach statevar sediment inflow")
            public JAMSDouble insed;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "reach statevar sediment outflow")
            public JAMSDouble outsed;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "Catchment outlet sediment storage",
            defaultValue= "0"
            )
            public JAMSDouble catchmentSed;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "Reach statevar sed storage"
            )
            public JAMSDouble actsed;
    /*
     *  Component run stages
     */
    
    public void init() throws JAMSEntity.NoSuchAttributeException {
        
    }
    
    public void run() throws JAMSEntity.NoSuchAttributeException {
        
        Attribute.Entity entity = entities.getCurrent();
        
        JAMSEntity DestReach = (JAMSEntity)entity.getObject("to_reach");
        if (DestReach.getValue() == null) {
            DestReach = null;
        }
        JAMSEntity DestReservoir = null;
        
        try{
            DestReservoir = (JAMSEntity)entity.getObject("to_reservoir");
        }catch(JAMSEntity.NoSuchAttributeException e){
            DestReservoir = null;
        }
       
        double SEDact = actsed.getValue() + insed.getValue();
        //System.out.println(DestReach.getObject("ID")+ "- " + SEDact + " Sedact ");

        insed.setValue(0);

        actsed.setValue(0);

        double SedDestIn = 0;

        if(DestReach == null && DestReservoir == null){
            SedDestIn = 0;
           
        } 
        else{
            SedDestIn = DestReach.getDouble("insed");
            //System.out.println(DestReach.getObject("ID")+ " " + DestReach.getDouble("insed")+ " in from Reach-Field");
              
           }

        double Sedout = SEDact;

        SedDestIn = SedDestIn + Sedout;
        //System.out.println(Sedout + " Sedout");
        //System.out.println(SedDestIn + " SedDestIn__");
       
        SEDact = 0;
 
        double cumSed = Sedout;

        insed.setValue(0);
       
        actsed.setValue(SEDact);
        
        outsed.setValue(Sedout);
       
        //reach
        if(DestReach != null && DestReservoir == null){
           
            DestReach.setDouble("insed",SedDestIn);
     
        }
        
        //outlet
        else if(DestReach == null && DestReservoir == null){
           
            //System.out.println(cumOutSed + " CCumOutSed");
            catchmentSed.setValue(cumSed);

        }
        
    }
    
    public void cleanup() {
        
    }
    
   
   
}
