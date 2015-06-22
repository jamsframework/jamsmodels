/*
 * J2KNRoutinglayer.java
 * Created on 11. March 2006, 09:21
 *
 * This file is part of JAMS
 * Copyright (C) 2005 FSU Jena, c0krpe & Manfred Fink
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

package org.jams.j2k.s_n;

import jams.data.*;
import jams.model.*;

/**
 *
 * @author Peter Krause & Manfred Fink
 */
@JAMSComponentDescription(
        title="J2KNRoutinglayer",
        author="Peter Krause & Manfred Fink",
        description="Passes the N output of the entities as N input to the respective reach or unit"
        )
        public class J2KNRoutinglayer extends JAMSComponent {
    
    /*
     *  Component variables
     */
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "The current hru entity"
            )
            public Attribute.EntityCollection entities;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Collection of reach objects"
            )
            public Attribute.EntityCollection reaches;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "RD1 N inflow in kgN"
            )
            public Attribute.Double SurfaceN_in;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "RD2 N inflow in kgN"
            )
            public Attribute.DoubleArray InterflowN_in;
    
/*    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "RD2 N inflow in kgN lumped"
            )
            public Attribute.Double InterflowN_sum;
 */
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "RG1 N inflow in kgN"
            )
            public Attribute.Double N_RG1_in;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "RG2 N inflow in kgN"
            )
            public Attribute.Double N_RG2_in;
    
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "RD1 N outflow in kgN"
            )
            public Attribute.Double SurfaceNabs;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "RD2 N outflow in kgN"
            )
            public Attribute.DoubleArray InterflowNabs;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "RG1 N outflow in kgN"
            )
            public Attribute.Double N_RG1_out;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "RG2 N outflow in kgN"
            )
            public Attribute.Double N_RG2_out;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "gwExcess"
            )
            public Attribute.Double NExcess;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = ""
            )
            public Attribute.Double NH4_in;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = ""
            )
            public Attribute.Double SurfaceSolubleP_in;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = ""
            )
            public Attribute.Double activN_in;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = ""
            )
            public Attribute.Double activP_in;    
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = ""
            )
            public Attribute.Double org_in_P;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = ""
            )
            public Attribute.Double insed;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = ""
            )
            public Attribute.Double residueN_in;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = ""
            )
            public Attribute.Double residue_in;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = ""
            )
            public Attribute.Double residue_in_P;    
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = ""
            )
            public Attribute.Double stableN_in; 

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = ""
            )
            public Attribute.Double stableP_in;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = ""
            )
            public Attribute.Double NH4_out;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = ""
            )
            public Attribute.Double SurfaceSolubleP_out;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = ""
            )
            public Attribute.Double activN_out;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = ""
            )
            public Attribute.Double activP_out;    
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = ""
            )
            public Attribute.Double org_out_P;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = ""
            )
            public Attribute.Double outsed;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = ""
            )
            public Attribute.Double residueN_out;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = ""
            )
            public Attribute.Double residue_out;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = ""
            )
            public Attribute.Double residue_out_P;    
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = ""
            )
            public Attribute.Double stableN_out; 

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = ""
            )
            public Attribute.Double stableP_out;     
        
    
    double[][] fracOut;
    double[] percNOut;
    /*
     *  Component run stages
     */
    
    public void init() {
        
    }
    
    public void run() {
        Attribute.Entity entity = entities.getCurrent();
        //receiving polygon
        Attribute.Entity toPoly = (Attribute.Entity) entity.getObject("to_poly");
        //receiving reach
        Attribute.Entity toReach = (Attribute.Entity) entity.getObject("to_reach");
        
        
        
        double NRD1out = SurfaceNabs.getValue();
        double[] NRD2out_h = InterflowNabs.getValue();
        double NRG1out = N_RG1_out.getValue();
        double NRG2out = N_RG2_out.getValue();
        
        double NH4out = NH4_out.getValue();
        double SurfaceSolublePout = SurfaceSolubleP_out.getValue();
        double activNout = activN_out.getValue();
        double activPout = activP_out.getValue();
        double orgPout = org_out_P.getValue();
        double outsediment = outsed.getValue();
        double residueNout = residueN_out.getValue();
        double residueout = residue_out.getValue();
        double residuePout = residue_out_P.getValue();
        double stableNout = stableN_out.getValue();
        double stablePout = stableP_out.getValue();
        
        
        double reachNRD2in = 0;
//        System.out.println("NRD2out: " + NRD2out);
        
       if(!toPoly.isEmpty()){
            double[] srcDepth = ((Attribute.DoubleArray)entity.getObject("depth_h")).getValue();
            double[] recDepth = ((Attribute.DoubleArray)toPoly.getObject("depth_h")).getValue();
            int srcHors = srcDepth.length;
            int recHors = recDepth.length;
            double[] NRD2in_h = new double[recHors];
            //this.calcParts(srcDepth, recDepth);
            
            double NRD1in = toPoly.getDouble("SurfaceN_in");
            double[] rdArN = ((Attribute.DoubleArray)toPoly.getObject("InterflowN_in")).getValue();
/*
            Object o = toPoly.getObject("InterflowN_in");
 
            double[] rdArN = null;
            try {
                rdArN = ((Attribute.DoubleArray)o).getValue();
 
            } catch (Exception e) {
                System.out.println("MIST");
            }*/
            double NRG1in = toPoly.getDouble("N_RG1_in");
            double NRG2in = toPoly.getDouble("N_RG2_in");          
//            double NRG1in = 0;
//            double NRG2in = 0;
            for(int j = 0; j < recHors; j++){
                NRD2in_h[j] = rdArN[j];
                for(int i = 0; i < srcHors; i++){
                    //NRD2in_h[j] = NRD2in_h[j] + NRD2out_h[i] * fracOut[i][j];
                    NRD2in_h[j] = NRD2in_h[j] + (NRD2out_h[i] / recHors);
                    //NRG1in = NRG1in + NRD2out_h[i] * this.percNOut[i];
                    //RD2out[i] -= RD2out[i] * fracOut[i][j];
                }
            }
            
            
            for(int i = 0; i < srcHors; i++){
                NRD2out_h[i] = 0;
            }
            NRD2in_h[recHors-1] += NExcess.getValue();
            double RD1in = toPoly.getDouble("inRD1");
            
            
            double RG2in = toPoly.getDouble("inRG2");
            
            NRD1in = NRD1in + NRD1out;
            NRG1in = NRG1in + NRG1out;
            NRG2in = NRG2in + NRG2out;
            
            NRD1out = 0;
            NRG1out = 0;
            NRG2out = 0;
            
            SurfaceNabs.setValue(0);
            InterflowNabs.setValue(NRD2out_h);
            N_RG1_out.setValue(0);
            N_RG2_out.setValue(0);
            NExcess.setValue(0);
            
            Attribute.DoubleArray rdAN = (Attribute.DoubleArray)toPoly.getObject("InterflowN_in");
            rdAN.setValue(NRD2in_h);
            toPoly.setDouble("SurfaceN_in",NRD1in);
            toPoly.setObject("InterflowN_in", rdAN);
            toPoly.setDouble("N_RG1_in", NRG1in);
            toPoly.setDouble("N_RG2_in", NRG2in);
        } else if(!toReach.isEmpty()){
            
            double NRD1in = toReach.getDouble("SurfaceN_in");
/*            
            try {
                reachNRD2in = toReach.getDouble("InterflowN_sum");
                
            } catch (Exception e) {
                System.out.println("MIST");
            }
*/            
            
            reachNRD2in = toReach.getDouble("InterflowN_sum");
            
            double NRG1in = toReach.getDouble("N_RG1_in");
            double NRG2in = toReach.getDouble("N_RG2_in");
            
            double NH4in = toReach.getDouble("NH4_in");
            double SurfaceSolublePin = toReach.getDouble("SurfaceSolubleP_in");
            double activNin = toReach.getDouble("activN_in");
            double activPin = toReach.getDouble("activP_in");
            double orgPin = toReach.getDouble("org_in_P");
            double insediment = toReach.getDouble("insed");
            double residueNin = toReach.getDouble("residueN_in");
            double residuein = toReach.getDouble("residue_in");
            double residuePin = toReach.getDouble("residue_in_P");
            double stableNin = toReach.getDouble("stableN_in");
            double stablePin = toReach.getDouble("stableP_in"); 
            
            for(int h = 0; h < NRD2out_h.length; h++){
                reachNRD2in = reachNRD2in + NRD2out_h[h];
                NRD2out_h[h] = 0;
            }
            NRD1in = NRD1in + NRD1out;
            reachNRD2in += NExcess.getValue();
            NRG1in = NRG1in + NRG1out;
            NRG2in = NRG2in + NRG2out;
            
            NRD1out = 0;
            
            NRG1out = 0;
            NRG2out = 0;
            
            
            NExcess.setValue(0);
            SurfaceNabs.setValue(0);
            toReach.setDouble("SurfaceN_in", NRD1in);
            InterflowNabs.setValue(NRD2out_h);
            toReach.setDouble("InterflowN_sum", reachNRD2in);
            N_RG1_out.setValue(NRG1out);
            toReach.setDouble("N_RG1_in", NRG1in);
            N_RG2_out.setValue(NRG2out);
            toReach.setDouble("N_RG2_in", NRG2in);
            
            NH4_out.setValue(NH4out);
            toReach.setDouble("NH4_in", NH4in);
            SurfaceSolubleP_out.setValue(SurfaceSolublePout);
            toReach.setDouble("SurfaceSolubleP_in", SurfaceSolublePin);
            activN_out.setValue(activNout);
            toReach.setDouble("activN_in", activNin);
            activP_out.setValue(activPout);
            toReach.setDouble("activP_in", activPin);
            org_out_P.setValue(orgPout);
            toReach.setDouble("org_in_P", orgPin);
            outsed.setValue(outsediment);
            toReach.setDouble("insed", insediment);
            residueN_out.setValue(residueNout);
            toReach.setDouble("residueN_in", residueNin);
            residue_out.setValue(residueout);
            toReach.setDouble("residue_in", residuein);
            residue_out_P.setValue(residuePout);
            toReach.setDouble("residue_in_P", residuePin);
            stableN_out.setValue(stableNout);
            toReach.setDouble("stableN_in", stableNin);
            stableP_out.setValue(stablePout);
            toReach.setDouble("stableP_in", stablePin);            
            
        } else{
            System.out.println("Current entity ID: " + entity.getDouble("ID") + " has no receiver.");
        }
        
    }
    public void cleanup() {
        
    }
    
  /*  private void calcParts(double[] depthSrc, double[] depthRec){
        int srcHorizons = depthSrc.length;
        int recHorizons = depthRec.length;
        
        double[] upBoundSrc = new double[srcHorizons];
        double[] lowBoundSrc = new double[srcHorizons];
        double low = 0;
        double up = 0;
        for(int i = 0; i < srcHorizons; i++){
            low += depthSrc[i];
            up = low - depthSrc[i];
            upBoundSrc[i] = up;
            lowBoundSrc[i] = low;
            //System.out.println("Src --> up: "+up+", low: "+low);
            
        }
        double[] upBoundRec = new double[recHorizons];
        double[] lowBoundRec = new double[recHorizons];
        low = 0;
        up = 0;
        for(int i = 0; i < recHorizons; i++){
            low += depthRec[i];
            up = low - depthRec[i];
            upBoundRec[i] = up;
            lowBoundRec[i] = low;
            //System.out.println("Rec --> up: "+up+", low: "+low);
        }
        
      
        fracOut = new double[depthSrc.length][depthRec.length];
        percNOut = new double[depthSrc.length];
        for(int i = 0; i < depthSrc.length; i++){
            double sumFrac = 0;
            for(int j = 0; j < depthRec.length; j++){
                if((lowBoundSrc[i] > upBoundRec[j]) && (upBoundSrc[i] < lowBoundRec[j])){
                    double relDepth = Math.min(lowBoundSrc[i], lowBoundRec[j]) - Math.max(upBoundSrc[i], upBoundRec[j]);
                    double fracDepth = relDepth / depthSrc[i];
                    sumFrac += fracDepth;
                    fracOut[i][j] = fracDepth;
                }
            }
            percNOut[i] = 1.0 - sumFrac;
        }
    }*/
}
