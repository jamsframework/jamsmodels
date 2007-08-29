/*
 * J2KSoilTemp.java
 * Created on 23. November 2005, 16:40
 *
 * This file is part of JAMS
 * Copyright (C) 2005 FSU Jena, c8fima
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

import org.unijena.jams.data.*;
import org.unijena.jams.model.*;

/**
 *
 * @author Manfred Fink
 */
@JAMSComponentDescription(
        title="J2KSoilTemp",
        author="Manfred Fink",
        description="Calculates soil temperature in diffrent depths"
        )
        public class J2KSoilTemp extends JAMSComponent {
    
    /*
     *  Component variables
     */
    
    
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "HRU attribute name area in m²"
            )
            public JAMSDouble area;
    
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "in °C daily max. temperature"
            )
            public JAMSDouble atemp_max;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "in °C dayly min. temperature"
            )
            public JAMSDouble atemp_min;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "in °C dayly mean. temperature"
            )
            public JAMSDouble atemp_mean;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "in °C anual mean temperature"
            )
            public JAMSDouble anatemp_mean;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "in mm depth of soil layer"
            )
            public JAMSDouble layerdepth;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "in mm depth of soil profile"
            )
            public JAMSDouble totaldepth;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "in kg/dm³ soil bulk density"
            )
            public JAMSDouble soil_bulk_density;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.INIT,
            description = "Temperature lag coefficient perhaps to calibrate, typcal value 0.8, range  0 - 1"
            )
            public JAMSDouble temp_lag;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "actual LPS in portion of sto_LPS soil water content"
            )
            public JAMSDouble Sat_LPS;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "actual MPS in portion of sto_MPS soil water content"
            )
            public JAMSDouble Sat_MPS;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "maximum MPS  in l soil water content"
            )
            public JAMSDouble stohru_MPS;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "maximum LPS  in l soil water content"
            )
            public JAMSDouble stohru_LPS;
/*    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "maximum FPS  in l soil water content"
            )
 
            public JAMSString aNamestohru_FPS;
 */
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "snowcover in mm water aequivalent"
            )
            public JAMSDouble snowcover;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "soilalbedo"
            )
            public JAMSDouble soilalbedo;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.INIT,
            description = "global radiation in MJ/(m²*d)"
            )
            public JAMSDouble radiation;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "soil temperature in layerdepth in °C"
            )
            public JAMSDouble Soil_Temp_Layer;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "in °C *  Output soil surface temperature"
            )
            public JAMSDouble Surfacetemp;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "aboveground biomass in dt/ha"
            )
            public JAMSDouble biomass;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "Array of state variables LAI "
            )
            public JAMSDoubleArray LAIArray = new JAMSDoubleArray();
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "time"
            )
            public JAMSCalendar time;
    
    double Soil_Temp;
    double surfacet;
    double radiat;
    /*
     *  Component run stages
     */
    
    public void init() throws JAMSEntity.NoSuchAttributeException{
        
/*
     JAMSEntityEnumerator eEnum = hrus.getEntityEnumerator();
     JAMSEntity[] entities = hrus.getEntityArray();
 
 
     for (int i = 0; i < entities.length; i++) {
         entities[i].setDouble(aNameSoil_Temp_Layer.getValue(), 7);
         entities[i].setDouble(aNameSurfacetemp.getValue(), 7);
//         entities[i].setDouble(aNameanatemp_mean.getValue(), 7);
    } */
    }
    
    public void run() throws JAMSEntity.NoSuchAttributeException {
        this.radiat = radiation.getValue();
        this.Soil_Temp = Soil_Temp_Layer.getValue();
        double runSoil_Temp_Layer = calc_Soil_Temp_Layer();
        Soil_Temp_Layer.setValue(runSoil_Temp_Layer);
    }
    
    private double calc_Soil_Temp_Layer() throws JAMSEntity.NoSuchAttributeException {
        
        double depthfactor = calc_Soil_Temp_Depth_Factor();
        double surfacetemp = calc_Soil_Surface_Temp();
        
        double temp_lag1 = temp_lag.getValue();
        double anavgtemp = anatemp_mean.getValue();
        
        this.Soil_Temp = temp_lag1 * this.Soil_Temp + (1 - temp_lag1) * (depthfactor *(anavgtemp - surfacetemp)+surfacetemp);
        
        /**
         * Frostroitine
         * Wärmekapazität von Wasser 4.18 kJ/(kg K)
         * Wärmekapazität von Eis 2.1 kJ/(kg K)
         * Schmelzwärme von Wasser 332 kJ/(kg K)
         * Annahme ca. 33% des Bodens ist Wasser
         *
         *//*
          if ((Soil_Temp_Layer < 0 && Soil_Temp_Layer_1 > 0)||(Soil_Temp_Layer > 0 && Soil_Temp_Layer_1 < 0)) {
          Soil_Temp_Layer = 0.9924 * Soil_Temp_Layer_1 + (1 - 0.9924) * (depthfactor *(anatemp_mean-surfacetemp)+surfacetemp);
          }
            
          if (Soil_Temp_Layer < -1) {
          Soil_Temp_Layer = 0.7 * Soil_Temp_Layer_1 + (1 - 0.7) * (depthfactor *(anatemp_mean-surfacetemp)+surfacetemp) +3 ;
          }
        /**/
        return Soil_Temp;
    }
    
    private double calc_water_content() throws JAMSEntity.NoSuchAttributeException {
        double soilwater = 0;
        double area_ = area.getValue();
        double sto_LPS = stohru_LPS.getValue() / area_;
        double sto_MPS = stohru_MPS.getValue() / area_;
//     double sto_FPS = stohru_FPS.getValue() / area_;
        
        double sto_FPS = 0.3 * sto_MPS; //     Swat definition of FPS
        double act_LPS = sto_LPS * Sat_LPS.getValue();         /** actual LPS in mm soil water content */
        double act_MPS = sto_MPS * Sat_MPS.getValue();         /** actual MPS in mm soil water content */
        
        soilwater = act_LPS + act_MPS + sto_FPS;
//     soilwater = act_LPS + act_MPS + (sto_MPS + sto_LPS) *  sto_FPS;
        
        return soilwater;
    }
    private double calc_Soil_Temp_Depth_Factor() throws JAMSEntity.NoSuchAttributeException {
        double depthfactor;
        
        double dampingdepth = calc_Soil_Temp_Dampingdepth();
        
        depthfactor = dampingdepth / (dampingdepth + (Math.exp(-0.867-(2.078*dampingdepth)))) ;
        
        return depthfactor;
    }
    
    private double calc_Soil_Temp_Dampingdepth() throws JAMSEntity.NoSuchAttributeException {
        double dampingdepth;
        double dd;
        double dd_max;
        double lamda;
        double l_depth = layerdepth.getValue() * 10 / 2;
        double t_depth = totaldepth.getValue() * 10;
        double soil_bulk_dens = soil_bulk_density.getValue();
//        double soil_bulk_dens = 1.4;
        double soilwater = calc_water_content();
        
        dd_max = 1000 + ((2500*soil_bulk_dens)/(soil_bulk_dens+ 686*Math.exp(-5.63*soil_bulk_dens)) );
        
        lamda = soilwater/((0.356-0.144*soil_bulk_dens)*t_depth);
        
        dd = dd_max * Math.exp(Math.log(500/dd_max)*((1-lamda)/(1+lamda))*((1-lamda)/(1+lamda)));
        
        dampingdepth = l_depth/dd ;
        
        return dampingdepth;
    }
    private double calc_Soil_Surface_Temp() throws JAMSEntity.NoSuchAttributeException {   /* after SWAT */
        int day = time.get(time.DAY_OF_YEAR) - 1;
        double coverweightsnow;
        double coverweightveg;
        double coverweight;
        double epsilon_solar;
        double temp_bare_soil;
        
        double LAI_temp = this.LAIArray.getValue()[day];
        double snowcov = snowcover.getValue();
        
// dummy calculation for vegetationcover /**vegetationcover estimated from measurements of Paul et. al. 2002*/
        double vegetationcover = biomass.getValue();
     // double vegetationcover =  728.3 * LAI_temp + 326;
        double radiation = radiat / 1000;
/*        epsilon_solar = (entity.getDouble(aNameradiation.getValue())*(1-entity.getDouble(aNamesoilalbedo.getValue()))-14)/20;
 */
        epsilon_solar = (radiation * (1 - 0.2) - 14) / 20;
        coverweightveg = vegetationcover/(vegetationcover + Math.exp(7.563-(0.0001297*vegetationcover)));
        
        coverweightsnow = snowcov/(snowcov + Math.exp(6.055-(0.3002*snowcov)));
        
        coverweight = Math.max(coverweightveg,coverweightsnow);
        
        
        /*        temp_bare_soil = atemp_mean + epsilon_solar * ((atemp_max - atemp_min)/2); /*SWAT Orginal*/
        /*       Combination of SWAT and Epic used to Calculate bare Soiltemp*/
        temp_bare_soil = calc_Soil_Surface_Temp2();
        
        surfacet = (coverweight * Soil_Temp) + ((1- coverweight) * temp_bare_soil);
        
        Surfacetemp.setValue(surfacet);
        
        
        return surfacet;
    }
    
    
    private double calc_Soil_Surface_Temp2() throws JAMSEntity.NoSuchAttributeException {   /* after ArcEgmo  "Williams-algorithm"*/
        double  albedofactor;
        double temp_min = atemp_min.getValue();
        double temp_max = atemp_max.getValue();
        double  temp_bare_soil;
        
//     albedofactor = soilalbedo * Math.exp(-0.5 * LAI) +  0.25 * (1 - Math.exp(-0.5 * LAI));/*orignal*/
        albedofactor = 0.01 ;/*modified for bare Soil*/
        temp_bare_soil = (1 - albedofactor) * (temp_min + (temp_max - temp_min) * Math.pow(0.03 * radiat, 0.5)) +  surfacet * albedofactor;
        
/*        if (unitnr < 2 && datumjul == 20 ) {
        j2k_org.core.message.deb_msg("calc_Soil_Temp_Layer \n"+
                                     "ID = " + unitnr + "\n" +
                                     "datumjul = " + datumjul + "\n" +
                                     "LAI = " + LAI + "\n" +
                                     "albedofactor = " + albedofactor + "\n" +
                                     "Soil_Temp_Layer = " + Soil_Temp_Layer + "\n" +
                                     "layerdepth = " + layerdepth + "\n" );
 
        }
 */
        
        return temp_bare_soil;
    }
    
    
    public void cleanup() {
        
    }
}

/*
   <component class="org.jams.j2k.s_n.J2KSoilTemp" name="J2KSoilTemp">
		<jamsvar name="time" provider="TemporalContext" providervar="current"/>
		<jamsvar name="area"  provider="HRUContext" providervar="currentEntity.area"/>
		<jamsvar name="atemp_mean" provider="HRUContext" providervar="currentEntity.tmean"/>
		<jamsvar name="atemp_max" provider="HRUContext" providervar="currentEntity.tmax"/>
		<jamsvar name="atemp_min" provider="HRUContext" providervar="currentEntity.tmin"/>
		<jamsvar name="LAIArray" provider="HRUContext" providervar="currentEntity.LAIArray"/>
		<jamsvar name="snowcover" provider="HRUContext" providervar="currentEntity.snowTotSWE"/>
		<jamsvar name="stohru_MPS" provider="HRUContext" providervar="currentEntity.maxMPS"/>
		<jamsvar name="stohru_LPS" provider="HRUContext" providervar="currentEntity.maxLPS"/>
		<jamsvar name="Sat_MPS" provider="HRUContext" providervar="currentEntity.satMPS"/>
		<jamsvar name="Sat_LPS" provider="HRUContext" providervar="currentEntity.satLPS"/>
		<jamsvar name="radiation" provider="HRUContext" providervar="currentEntity.solRad"/>
		<jamsvar name="soilalbedo" provider="HRUContext" providervar="currentEntity.albedo"/>
		<jamsvar name="Surfacetemp" provider="HRUContext" providervar="currentEntity.surfacetemp"/>
		<jamsvar name="Soil_Temp_Layer" provider="HRUContext" providervar="currentEntity.Soil_Temp_Layer"/>
		<!--<!Prelinimary variable settings">-->
		<jamsvar name="layerdepth" provider="HRUContext" providervar="currentEntity.rootDepth"/>
		<jamsvar name="totaldepth" provider="HRUContext" providervar="currentEntity.rootDepth"/>
		<!--<!Prelinimary variable settings end">-->
		<jamsvar name="temp_lag" value="0.8"/>
		<jamsvar name="anatemp_mean" provider="HRUContext" providervar="currentEntity.tmeanavg"/>
	</component>
 */