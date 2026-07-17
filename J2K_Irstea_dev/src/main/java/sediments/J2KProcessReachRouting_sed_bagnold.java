/*
 * J2KProcessReachRouting.java
 * Created on 28. November 2005, 10:01
 *
 * This file is part of JAMS
 * Copyright (C) 2005 FSU Jena, c0krpe
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA
 *
 */
package sediments;

import jams.JAMS;
import jams.data.*;
import jams.model.*;

/**
 *
 * @author c0krpe
 */
@JAMSComponentDescription(
        title = "ReachRouting_KinematicWave",
        author = "Peter Krause + VT + olivier champagne",
        description = "Calculates flow processes in the river network by a simplified kinematic wave approach",
        version = "1.0_1",
        date = "2011-05-30"
)
@VersionComments(entries = {
    @VersionComments.Entry(version = "1.0_0", comment = "Initial version"),
    @VersionComments.Entry(version = "1.0_1", comment = "Added slopeAsProportion parameter to allow "
            + "switching between reaches providiong slope either in % or in proportions "
            + "(elevation difference / length). When using old models with this component, make sure to "
            + "check if this value was set correctly. Otherwise you might experience a damped signal."),
    @VersionComments.Entry(version = "1.0_2", date = "2016-05-24", comment = "Added checking of reach "
            + "slopes to avoid misconfiguration of slope parameters. Use \"checkSlopes\" switch to "
            + "turn this off!")
})
public class J2KProcessReachRouting_sed_bagnold extends JAMSComponent {

    /*
     *  Component variables
     */
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "The reach collection"
    )
    public Attribute.EntityCollection entities;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "reach length",
            unit = "m"
    )
    public Attribute.Double length;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "reach slope",
            unit = "%"
    )
    public Attribute.Double slope;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Is slope provided as proportion of length and elevation difference [m/m]?",
            defaultValue = "false"
    )
    public Attribute.Boolean slopeAsProportion;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "If true, slopes of all reaches will be checked if they are compliant to the value of the \"slopeAsProportion\" parameter",
            defaultValue = "true"
    )
    public Attribute.Boolean checkSlopes;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "reach width",
            unit = "m"
    )
    public Attribute.Double width;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "reach roughness"
    )
    public Attribute.Double roughness;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "Sediment inflow to reach",
            unit = "T"
    )
    public Attribute.Double insed;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "additional sediment to reach",
            unit = "T",
            defaultValue = "0"
    )
    public Attribute.Double inAddsed;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "Sediment outflow from reach",
            unit = "T"
    )
    public Attribute.Double outsed;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "Sediment outflow from reach",
            unit = "T"
    )
    public Attribute.Double Degsed;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "Sediment outflow from reach",
            unit = "T"
    )
    public Attribute.Double Depsed;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "Sediment outflow from reach",
            unit = "T"
    )
    public Attribute.Double sedConc;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "Sediment outflow from reach",
            unit = "T"
    )
    public Attribute.Double maxConc;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "Sediment outflow from reach",
            unit = "T"
    )
    public Attribute.Double qactot;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "additional sediment outflow from reach",
            unit = "T",
            defaultValue = "0"
    )
    public Attribute.Double outAddsed;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "simulated runoff from reach",
            unit = "L"
    )
    public Attribute.Double simRunoff;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "Sediment storage inside reach",
            unit = "T"
    )
    public Attribute.Double actsed;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "additional sediment inflow storage inside reach",
            unit = "T",
            defaultValue = "0"
    )
    public Attribute.Double actAddsed;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "flow routing coefficient TA",
            lowerBound = 0.0,
            upperBound = 50.0,
            defaultValue = "1.0"
    )
    public Attribute.Double flowRouteTA;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "Catchment outlet Sediment storage",
            unit = "T"
    )
    public Attribute.Double catchmentsed;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "Catchment additional Sediment input outlet storage",
            unit = "T",
            defaultValue = "0"
    )
    public Attribute.Double catchmentAddsedIn;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "temporal resolution [d or h]"
    )
    public Attribute.String tempRes;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "water level in reach"
    )
    public Attribute.Double waterLevel;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "water velocity in reach"
    )
    public Attribute.Double waterVelocity;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Channel Storage inside Reach"
    )
    public Attribute.Double channelStorage; 
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "coefficient for maximum amount of sediment"
    )
    public Attribute.Double csp;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "exponent coefficient for maximum amount of sediment"
    )
    public Attribute.Double spexp;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "peak flow rate"
    )
    public Attribute.Double prf;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Channel erodibility and cover factors"
    )
    public Attribute.Double KCCH;
    
    /*
     *  Component run stages
     */
    int count = 0;
    double avg = 0;
    int slopefactor;

    public void init() {

        if (slopeAsProportion.getValue()) {
            slopefactor = 100;
        } else {
            slopefactor = 1;
        }
    }

    public void initAll() {

        if (checkSlopes.getValue()) {

            avg = (avg * count + slopefactor * slope.getValue()) / ++count;

            if (avg >= 100) {
                getModel().getRuntime().sendHalt("Average reach slope exceeds 100%. please check your reach parameter file and \"slopeAsProportion\" parameter value!");
            }
            if (avg <= 0.1) {
                getModel().getRuntime().sendHalt("Average reach slope is below 0.1%. please check your reach parameter file and \"slopeAsProportion\" parameter value!");
            }
        }
        
        if (this.slope.getValue() == 0) {
            getModel().getRuntime().println("WARNING: Found zero slope in reach entity which will prevent water routing!", JAMS.VERBOSE);
        }        
    }

    public void run() {

        Attribute.Entity entity = entities.getCurrent();

        Attribute.Entity DestReach = (Attribute.Entity) entity.getObject("to_reach");
        if (DestReach.isEmpty()) {
            DestReach = null;
        }
        Attribute.Entity DestReservoir = null;

        if (entity.existsAttribute("to_reservoir")) {
            DestReservoir = (Attribute.Entity) entity.getObject("to_reservoir");
        } else {
            DestReservoir = null;
        }

        double width = this.width.getValue();
        double rough = this.roughness.getValue();
        double length = this.length.getValue();

        double slope = this.slope.getValue();
        if (!slopeAsProportion.getValue()) {
            slope = slope / 100;
        }

        double sedact = actsed.getValue() + insed.getValue();

        double addInActsed = actAddsed.getValue() + this.inAddsed.getValue();

        insed.setValue(0);

        inAddsed.setValue(0);

        actsed.setValue(0);

        actAddsed.setValue(0);

        double sedDestIn = 0;
        double addsedDestIn = 0;

        if (DestReach == null && DestReservoir == null) {
            sedDestIn = 0;//entity.getDouble(aNameCatchmentOutRD1.getValue());

            addsedDestIn = 0;
        } else if (DestReservoir != null) {
            sedDestIn = DestReservoir.getDouble("compsed");
        } else {
            sedDestIn = DestReach.getDouble("insed");

            try {
                addsedDestIn = DestReach.getDouble("sedAddIn");
            } catch (jams.data.Attribute.Entity.NoSuchAttributeException e) {
                addsedDestIn = 0;
            }
        }

        double q_act_tot = sedact + addInActsed;

        //int ID = (int)entity.getDouble("ID");
        // System.out.getRuntime().println("Processing reach: " + ID);
        if (q_act_tot == 0) {
            outsed.setValue(0);

            this.outAddsed.setValue(0);

            //nothing more to do here
            return;
        }

        //relative parts of the runoff components for later redistribution
        //double RD1_part = RD1act / q_act_tot;

        //double addInPart = addInAct / q_act_tot;

        //calculation of flow velocity
        int sec_inTStep = 0;
        if (this.tempRes.getValue().equals("d")) {
            sec_inTStep = 86400;
        } else if (this.tempRes.getValue().equals("h")) {
            sec_inTStep = 3600;
        }
        //double flow_veloc = this.calcFlowVelocity(q_act_tot, width, slope, rough, sec_inTStep);
        double flow_veloc = waterVelocity.getValue();
        //calculate concentration of sediments
        
        double channelstor = this.channelStorage.getValue();
        double concsed;
        if (channelstor == 0 || q_act_tot == 0) {
            concsed = 0;
        } else {     
            //concsed = (float)((1000 * q_act_tot) / channelstor);
            concsed = q_act_tot;
        }
        //maximum concentration of sediment that can be transported
        double concmax = this.csp.getValue() * Math.pow(flow_veloc,this.spexp.getValue());
        
        //concentration to quantity from T/m3 to T
        concmax = concmax  * (channelstor / 1000);
        //calculate deposition (sedDep) or degradation (sedDeg) of sediment 
        double sedDep;
        double sedDeg;
        if (concsed > concmax) {
            sedDep = (concsed - concmax);
            sedDeg = 0;
        } else {
            sedDep = 0;
            sedDeg = (concmax - concsed) * KCCH.getValue();
        }
        
        double sedpool;
        
        sedpool = q_act_tot - sedDep + sedDeg ;
        
        if (sedpool < 0) {
            sedpool = 0;
        }
        else {
       // do nothing
        }
        
                //recession coefficient
        double Rk = (flow_veloc / length) * this.flowRouteTA.getValue() * 3600;
        //the whole outflow
        double q_act_out;
        if (Rk > 0) {
            q_act_out = sedpool * Math.exp(-1 / Rk);
        } else {
            q_act_out = 0;
        }

        //the actual outflow from the reach
        double sedout = q_act_out;

        //transferring runoff from this reach to the next one or a reservoir
        sedDestIn = sedDestIn + sedout;

        //addInDestIn = addInDestIn + addInOut;

        //reducing the actual storages
        sedact = sedpool - q_act_out;

        //addInAct = addInAct - q_act_out * addInPart;

        //double channelStorage = RD1act + RD2act + RG1act + RG2act + addInAct;

        //double cumOutflow = RD1out + RD2out + RG1out + RG2out + addInOut;
        /*if (reachID.getValue()==800)
        {System.out.println(RD1out);
        System.out.println(RD2out);
        System.out.println(RG1out);
        System.out.println(RG2out);
        }
         */

        //simRunoff.setValue(cumOutflow);
        //this.channelStorage.setValue(channelStorage);
        insed.setValue(0);
        //inAddIn.setValue(0);

        actsed.setValue(sedact);

        //actAddIn.setValue(addInAct);

        outsed.setValue(sedout);
        
        Degsed.setValue(sedDeg);
        Depsed.setValue(sedDep);
        sedConc.setValue(concsed);
        maxConc.setValue(concmax);
        qactot.setValue(q_act_tot);
        //outAddIn.setValue(addInOut);
        //double verzoegerung; it means delay
        //reach
        if (DestReach != null && DestReservoir == null) {
            DestReach.setDouble("insed", sedDestIn);

            //DestReach.setDouble("inAddIn", addInDestIn);

        } //reservoir
        else if (DestReservoir != null) {
            DestReservoir.setDouble("compsed", sedDestIn);
        } //outlet
        else if (DestReach == null && DestReservoir == null) {
            catchmentsed.setValue(sedout);

            //this.catchmentAddIn.setValue(addInOut);
            //neu verzoegerung

            //catchmentSimRunoff.setValue(cumsed);
        }

        //waterLevel.setValue(channelStorage / (1000 * width * length));

    }

    public void cleanup() {
        
    }
    }