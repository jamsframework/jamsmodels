/*
 * InitJ2KNSoillayer.java
 * Created on 17. February 2006, 14:49
 *
 * This file is part of JAMS
 * Copyright (C) 2006 FSU Jena, Manfred Fink
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

package org.jams.j2k.s_n.init;

import org.unijena.jams.data.*;
import org.unijena.jams.model.*;

/**
 *
 * @author Manfred Fink
 */
@JAMSComponentDescription(
title="InitJ2KNSoillayer",
        author="Manfred Fink",
        description="intitiallizing Nitrogen Pools in Soil and additional variables. Method after SWAT2000"
        )
        public class InitJ2KNSoillayer extends JAMSComponent  {
    
    /*
     *  Component variables
     */
    
    
    
    
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "in cm depth of soil layer"
            )
            public JAMSDoubleArray layerdepth = new JAMSDoubleArray();
    
    
    
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.INIT,
            description = "in kg/dm³ soil bulk density"
            )
            public JAMSDoubleArray soil_bulk_density = new JAMSDoubleArray();
    
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.INIT,
            description = " in % organic Carbon in soil"
            )
            public JAMSDoubleArray C_org = new JAMSDoubleArray();
    
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READWRITE,
            update = JAMSVarDescription.UpdateType.RUN,
            description = " NO3-Pool in kgN/ha"
            )
            public JAMSDoubleArray NO3_Pool = new JAMSDoubleArray();
    
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READWRITE,
            update = JAMSVarDescription.UpdateType.RUN,
            description = " NH4-Pool in kgN/ha"
            )
            public JAMSDoubleArray NH4_Pool = new JAMSDoubleArray();
    
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READWRITE,
            update = JAMSVarDescription.UpdateType.RUN,
            description = " N-Organic Pool with reactive organic matter in kgN/ha"
            )
            public JAMSDoubleArray N_activ_pool = new JAMSDoubleArray();
    
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READWRITE,
            update = JAMSVarDescription.UpdateType.RUN,
            description = " N-Organic Pool with stable organic matter in kgN/ha"
            )
            public JAMSDoubleArray N_stabel_pool = new JAMSDoubleArray();
    
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READWRITE,
            update = JAMSVarDescription.UpdateType.RUN,
            description = " Residue in Layer in kgN/ha"
            )
            public JAMSDoubleArray Residue_pool = new JAMSDoubleArray();
    
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READWRITE,
            update = JAMSVarDescription.UpdateType.RUN,
            description = " N-Organic fresh Pool from Residue in kgN/ha"
            )
            public JAMSDoubleArray N_residue_pool_fresh = new JAMSDoubleArray();
    
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READWRITE,
            update = JAMSVarDescription.UpdateType.RUN,
            description = " Nitrate in interflow in added to HRU layer in kgN"
            )
            public JAMSDoubleArray InterflowN_in  = new JAMSDoubleArray();
    
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.WRITE,
            update = JAMSVarDescription.UpdateType.RUN,
            description = " Nitrate input due to Fertilisation in kgN/ha"
            )
            public JAMSDouble fertNO3;
    
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.WRITE,
            update = JAMSVarDescription.UpdateType.RUN,
            description = " Ammonium input due to Fertilisation in kgN/ha"
            )
            public JAMSDouble fertNH4;
    
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.WRITE,
            update = JAMSVarDescription.UpdateType.RUN,
            description = " Stable organig N input due to Fertilisation in kgN/ha"
            )
            public JAMSDouble fertstableorg;
    
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.WRITE,
            update = JAMSVarDescription.UpdateType.RUN,
            description = " Activ organig N input due to Fertilisation in kgN/ha"
            )
            public JAMSDouble fertactivorg;
    
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.WRITE,
            update = JAMSVarDescription.UpdateType.RUN,
            description = " Input of plant residues kg/ha"
            )
            public JAMSDouble inp_biomass;
    
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.WRITE,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "Nitrogen input of plant residues in kgN/ha"
            )
            public JAMSDouble inpN_biomass;
    
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "number of layers in soil profile in [-]"
            )
            public JAMSDouble Layer;
    
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.WRITE,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "flag plant existing yes or no " // attention its a boolean!
            )
            public JAMSBoolean plantExisting = new JAMSBoolean();
    
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.WRITE,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "Actual rooting depth [m]"
            )
            public JAMSDouble ZRootD;
    
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.WRITE,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "actual LAI"
            )
            public JAMSDouble LAI;
    
    
    /*
     *  Component run stages
     */
    
    
    
    
    
    public void init() throws JAMSEntity.NoSuchAttributeException{
        
        
    }
    
    public void run() throws JAMSEntity.NoSuchAttributeException{
        int i = 0;
        double orgNhum = 0; /*concentration of humic organic nitrogen in the layer (mg/kg)*/
        int layer = (int)Layer.getValue();
        double runlayerdepth;
        plantExisting.setValue(true);
        double runsoil_bulk_density;
        
        double runC_org;
        
        double hor_dept = 0;
        
        double runNO3_Pool;
        double[] NO3_Poolvals = new double[layer];
        
        double runNH4_Pool;
        double[] NH4_Poolvals = new double[layer];
        
        double runN_activ_pool;
        double[] N_activ_poolvals = new double[layer];
        
        double runN_stabel_pool;
        double[] N_stabel_poolvals = new double[layer];
        
        double runN_residue_pool_fresh;
        double[] N_residue_pool_freshvals = new double[layer];
        
        double runResidue_pool;
        double[] Residue_poolvals = new double[layer];
        
        double[] InterflowN_invals = new double[layer];
        
        
        double fr_actN = 0.02;      /** nitrogen active pool fraction. The fraction of organic nitrogen in the active pool. */
        
        
        
        
        while (i < layer){
            
            runC_org = C_org.getValue()[i] / 1.72;
            runsoil_bulk_density = soil_bulk_density.getValue()[i];
            runlayerdepth = layerdepth.getValue()[i] * 10; //from cm to mm
            hor_dept = hor_dept + runlayerdepth;
            runResidue_pool = 10;
            runNO3_Pool = ((7 * Math.exp(-hor_dept/1000)) * runsoil_bulk_density * runlayerdepth)/1000;
            runNH4_Pool = 0.1 * runNO3_Pool;
            orgNhum = 10000 * runC_org / 17;
            runN_activ_pool = ((orgNhum * fr_actN) * runsoil_bulk_density * runlayerdepth)/100;
            runN_stabel_pool = ((orgNhum * (1 - fr_actN)) * runsoil_bulk_density * runlayerdepth)/100;
            runN_residue_pool_fresh = 0.0015 * runResidue_pool;
            NO3_Poolvals[i] = runNO3_Pool;
            NH4_Poolvals[i] = runNH4_Pool;
            N_activ_poolvals[i] = runN_activ_pool;
            N_stabel_poolvals[i] = runN_stabel_pool;
            Residue_poolvals[i] = runResidue_pool;
            N_residue_pool_freshvals[i] = runN_residue_pool_fresh;
            InterflowN_invals[i] = 0;
            
            
            i++;
        }
        
        NO3_Pool.setValue(NO3_Poolvals);
        NH4_Pool.setValue(NH4_Poolvals);
        N_activ_pool.setValue(N_activ_poolvals);
        N_stabel_pool.setValue(N_stabel_poolvals);
        Residue_pool.setValue(Residue_poolvals);
        N_residue_pool_fresh.setValue(N_residue_pool_freshvals);
        InterflowN_in.setValue(InterflowN_invals);
        fertNO3.setValue(0);
        fertNH4.setValue(0);
        fertstableorg.setValue(0);
        fertactivorg.setValue(0);
        inp_biomass.setValue(0);
        inpN_biomass.setValue(0);
        ZRootD.setValue(0);
        LAI.setValue(0);
    }
    
    
    public void cleanup() throws JAMSEntity.NoSuchAttributeException{
        
    }
}
/*
                <component class="org.jams.j2k.s_n.init.initJ2KNSoil" name="initJ2KNSoil">
                        <jamsvar name="NO3_Pool" provider="InitHRUContext" providervar="currentEntity.NO3_Pool"/>
                        <jamsvar name="NH4_Pool" provider="InitHRUContext" providervar="currentEntity.NH4_Pool"/>
                        <jamsvar name="N_activ_pool" provider="InitHRUContext" providervar="currentEntity.N_activ_pool"/>
                        <jamsvar name="N_stabel_pool" provider="InitHRUContext" providervar="currentEntity.N_stabel_pool"/>
                        <jamsvar name="Residue_pool" provider="InitHRUContext" providervar="currentEntity.residue_pool"/>
                        <jamsvar name="N_residue_pool_fresh" provider="InitHRUContext" providervar="currentEntity.N_residue_pool_fresh"/>
                        <jamsvar name="Volati_trans" provider="InitHRUContext" providervar="currentEntity.Volati_rate"/>
                        <jamsvar name="NH4inp" provider="InitHRUContext" providervar="currentEntity.NH4inp"/>
                        <jamsvar name="PlantupN" provider="InitHRUContext" providervar="currentEntity.PlantupN"/>
                        <jamsvar name="Nitri_trans" provider="InitHRUContext" providervar="currentEntity.Nitri_rate"/>
                        <jamsvar name="Denit_trans" provider="InitHRUContext" providervar="currentEntity.Denit_rate"/>
                        <jamsvar name="SurfaceN" provider="InitHRUContext" providervar="currentEntity.SurfaceN"/>
                        <jamsvar name="InterflowN" provider="InitHRUContext" providervar="currentEntity.InterflowN"/>
                        <jamsvar name="PercoN" provider="InitHRUContext" providervar="currentEntity.PercoN"/>
                        <jamsvar name="SurfaceNabs" provider="InitHRUContext" providervar="currentEntity.SurfaceNabs"/>
                        <jamsvar name="InterflowNabs" provider="InitHRUContext" providervar="currentEntity.InterflowNabs"/>
                        <jamsvar name="PercoNabs" provider="InitHRUContext" providervar="currentEntity.PercoNabs"/>
                        <jamsvar name="SurfaceN_in" provider="InitHRUContext" providervar="currentEntity.SurfaceN_in"/>
                        <jamsvar name="InterflowN_in" provider="InitHRUContext" providervar="currentEntity.InterflowN_in"/>
                        <jamsvar name="PercoN_in" provider="InitHRUContext" providervar="currentEntity.PercoN_in"/>
 
                        <jamsvar name="layerdepth" provider="InitHRUContext" providervar="currentEntity.rootDepth"/>
                        <jamsvar name="totaldepth" provider="InitHRUContext" providervar="currentEntity.rootDepth"/>
                        <jamsvar name="soil_bulk_density" value="1.3"/>
                        <jamsvar name="C_org" value="1.5"/>
                </component>
 */