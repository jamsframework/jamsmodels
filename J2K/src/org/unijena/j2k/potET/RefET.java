/*
 * RefET.java
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

/**********************XML Description********************************************
 <component class="org.unijena.j2k.potET.RefET" name="CalcRefET">
    <jamsvar name="dirName" globvar="workspaceDir"/>
    <jamsvar name="dataCaching" globvar="data_caching"/>
    <jamsvar name="netRad" provider="HRUContext" providervar="currentEntity.netRad"/>
    <jamsvar name="tmean" provider="HRUContext" providervar="currentEntity.tmean"/>
    <jamsvar name="rhum" provider="HRUContext" providervar="currentEntity.rhum"/>
    <jamsvar name="wind" provider="HRUContext" providervar="currentEntity.wind"/>
    <jamsvar name="elevation" provider="HRUContext" providervar="currentEntity.elevation"/>
    <jamsvar name="area" provider="HRUContext" providervar="currentEntity.area"/>
    <jamsvar name="refET" provider="HRUContext" providervar="currentEntity.refETP"/>
    <jamsvar name="actET" provider="HRUContext" providervar="currentEntity.actETP"/>
    <jamsvar name="tempRes" value="d"/>
</component>
 *********************************************************************************/

package org.unijena.j2k.potET;

import java.io.*;
import org.unijena.jams.JAMS;
import org.unijena.jams.data.*;
import org.unijena.jams.model.*;

/**
 *
 * @author Peter Krause
 */



@JAMSComponentDescription(
        title="CalcDailyETP_PenmanMonteith",
        author="Peter Krause",
        description="Calculates potential ETP after Penman-Monteith"
        )
        
        public class RefET extends JAMSComponent {
    
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
            update = JAMSVarDescription.UpdateType.RUN,
            description = "Current time"
            )
            public JAMSCalendar time;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.INIT,
            description = "temporal resolution [d | h | m]"
            )
            public JAMSString tempRes;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "state variable wind"
            )
            public JAMSDouble wind;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "state variable mean temperature"
            )
            public JAMSDouble tmean;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "state variable relative humidity"
            )
            public JAMSDouble rhum;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "state variable net radiation"
            )
            public JAMSDouble netRad;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "attribute elevation"
            )
            public JAMSDouble elevation;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "attribute area"
            )
            public JAMSDouble area;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "potential refET [mm/ timeUnit]"
            )
            public JAMSDouble refET;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "actual ET [mm/ timeUnit]"
            )
            public JAMSDouble actET;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.INIT,
            description = "Use caching of regionalised data?"
            )
            public JAMSBoolean dataCaching;
    
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
        
        if (!cacheFile.exists() && dataCaching.getValue()) {
            getModel().getRuntime().sendHalt(this.getInstanceName() + ": dataCaching is true but no cache file available!");
        }
        
        if (dataCaching.getValue()) {
            useCache = true;
            
            reader = new ObjectInputStream(new BufferedInputStream(new FileInputStream(cacheFile)));//new FileInputStream(cacheFile));
        } else {
            useCache = false;
            writer = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(cacheFile)));
        }
    }
    
    public void run() throws JAMSEntity.NoSuchAttributeException, IOException {
        if (!useCache) {
            double netRad      = this.netRad.getValue();
            double temperature = this.tmean.getValue();
            double rhum        = this.rhum.getValue();
            double wind        = this.wind.getValue();
            double elevation   = this.elevation.getValue();
            double area        = this.area.getValue();
            
            //refET standard parameters for short grass with effH 0.12 and LAI 2.88
            double rs = 70;
            double ra = 208. / wind;
            
            double abs_temp = org.unijena.j2k.physicalCalculations.ClimatologicalVariables.calc_absTemp(temperature, "degC");
            double delta_s = org.unijena.j2k.physicalCalculations.ClimatologicalVariables.calc_slopeOfSaturationPressureCurve(temperature);
            double pz = org.unijena.j2k.physicalCalculations.ClimatologicalVariables.calc_atmosphericPressure(elevation, abs_temp);
            double est = org.unijena.j2k.physicalCalculations.ClimatologicalVariables.calc_saturationVapourPressure(temperature);
            double ea = org.unijena.j2k.physicalCalculations.ClimatologicalVariables.calc_vapourPressure(rhum, est);
            double latH = org.unijena.j2k.physicalCalculations.ClimatologicalVariables.calc_latentHeatOfVaporization(temperature);
            double psy = org.unijena.j2k.physicalCalculations.ClimatologicalVariables.calc_psyConst(pz, latH);
            
            double G = this.calc_groundHeatFlux(netRad);
            
            double tempFactor = 0;
            double pET = 0;
            double aET = 0;
            
            if(this.tempRes.getValue().equals("d"))
                tempFactor = 891;
            else if(this.tempRes.getValue().equals("h"))
                tempFactor = 37;
            else if(this.tempRes.getValue().equals("m"))
                tempFactor = 891;
            
            pET = (0.408 * delta_s *(netRad - G) + psy * (tempFactor/(temperature + 273)) * wind * (est - ea))
                    / (delta_s + psy * (1 + 0.34 * wind));
            
            
            //converting mm to litres
            pET = pET * area;
            
            //aggregation to monthly values
            if(this.time != null){
                if(this.tempRes.getValue().equals("m")){
                    int daysInMonth = this.time.getActualMaximum(time.DATE);
                    pET = pET * daysInMonth;
                }
            }
            
            //avoiding negative potETPs
            if(pET < 0){
                pET = 0;
            }
            this.refET.setValue(pET);
            this.actET.setValue(aET);
            writer.writeDouble(pET);
        } else {
            this.refET.setValue(reader.readDouble());
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
    
    private double calcETAllen(double ds, double netRad, double G, double pa, double CP, double est, double ea, double ra, double rs, double psy, double tempFactor){
        ds = ds / 10;
        est = est / 10;
        ea = ea / 10;
        psy = psy / 10000;
        CP = CP / 1000;
        double Letp = (ds * (netRad - G) + ((pa * CP * (est-ea)/ra)*tempFactor))
        / (ds + psy * (1+rs/ra));
        
        return Letp;
    }
    
    private double calc_groundHeatFlux(double netRad){
        double g = 0.1 * netRad;
        return g;
    }
    
    /*
    private double calc_raAllen(double veg_height, double windspeed){
        double w = Math.log((2 - 2. / 3. * veg_height)/(0.123 * veg_height));
        double r = Math.log((2 - 2. / 3. * veg_height)/(0.1 * 0.123 * veg_height));
        double v = Math.pow(0.41,2) * windspeed;
     
        double ra = (w*r) / v ;
        return ra;
    }
     */
    private static double calcRa(double eff_height, double wind_speed){
        double ra;
        if(wind_speed <= 0)
            wind_speed = 0.5;
        if(eff_height < 10){
            ra = (1.5 * Math.pow(Math.log(2/(0.125 * eff_height)),2)) / (Math.pow(0.41,2) * wind_speed);
        } else{
            ra = 64 / (Math.pow(0.41,2) * wind_speed);
        }
        return ra;
    }
    
    private double calcRs(double LAI, double rsc, double rss){
        double A = Math.pow(0.7, LAI);
        double rs = 1. / (((1-A)/rsc)+((A / rss)));
        
        return rs;
    }
}
