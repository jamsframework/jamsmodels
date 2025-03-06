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
package routing;

import jams.JAMS;
import jams.data.*;
import jams.model.*;

/**
 *
 * @author c0krpe
 */
@JAMSComponentDescription(
        title = "ReachRouting_KinematicWave",
        author = "Peter Krause",
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
public class J2KProcessReachRouting_sa extends JAMSComponent {

    /*
     *  Component variables
     */
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "The reach collection"
    )
    public Attribute.EntityCollection st_entities;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "reach length",
            unit = "m"
    )
    public Attribute.Double par_length;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "reach slope",
            unit = "%"
    )
    public Attribute.Double par_slope;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Is slope provided as proportion of length and elevation difference [m/m]?",
            defaultValue = "false"
    )
    public Attribute.Boolean par_slope_as_proportion;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "If true, slopes of all reaches will be checked if they are compliant to the value of the \"slopeAsProportion\" parameter",
            defaultValue = "true"
    )
    public Attribute.Boolean par_check_slopes;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "reach width",
            unit = "m"
    )
    public Attribute.Double par_width;
	
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "reach width multiplicative adaptation factor",
            unit = "-",
            defaultValue = "1"
    )
    public Attribute.Double par_width_maf;
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "reach width additive adaptation factor",
            unit = "-",
            defaultValue = "0"
    )
    public Attribute.Double par_width_aaf;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "reach roughness"
    )
    public Attribute.Double par_roughness;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "reach roughness mutliplicative adaptation factor",
            defaultValue = "1"
    )
    public Attribute.Double par_roughness_maf;
	
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "reach roughness additive adaptation factor",
            defaultValue = "0"
    )
    public Attribute.Double par_roughness_aaf;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "RD1 inflow to reach",
            unit = "L"
    )
    public Attribute.Double in_rd1;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "RD2 inflow to reach",
            unit = "L"
    )
    public Attribute.Double in_rd2;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "RG1 inflow to reach",
            unit = "L"
    )
    public Attribute.Double in_rg1;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "RG2 inflow to reach",
            unit = "L"
    )
    public Attribute.Double in_rg2;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "additional inflow to reach",
            unit = "L",
            defaultValue = "0"
    )
    public Attribute.Double in_add_in;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "RD1 outflow from reach",
            unit = "L"
    )
    public Attribute.Double out_rd1;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "RD2 outflow from reach",
            unit = "L"
    )
    public Attribute.Double out_rd2;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "RG1 outflow from reach",
            unit = "L"
    )
    public Attribute.Double out_rg1;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "RG2 outflow from reach",
            unit = "L"
    )
    public Attribute.Double out_rg2;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "additional outflow from reach",
            unit = "L",
            defaultValue = "0"
    )
    public Attribute.Double out_add_in;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "simulated runoff from reach",
            unit = "L"
    )
    public Attribute.Double out_sim_runoff;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "RD1 storage inside reach",
            unit = "L"
    )
    public Attribute.Double out_act_rd1;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "RD2 storage inside reach",
            unit = "L"
    )
    public Attribute.Double out_act_rd2;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "RG1 storage inside reach",
            unit = "L"
    )
    public Attribute.Double out_act_rg1;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "RG2 storage inside reach",
            unit = "L"
    )
    public Attribute.Double out_act_rg2;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "additional inflow storage inside reach",
            unit = "L",
            defaultValue = "0"
    )
    public Attribute.Double out_act_add_in;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "Channel storage inside reach",
            unit = "L"
    )
    public Attribute.Double out_channel_storage;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "flow routing coefficient TA",
            lowerBound = 0.0,
            upperBound = 50.0,
            defaultValue = "1.0"
    )
    public Attribute.Double par_flow_route_ta;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "Catchment outlet RD1 storage",
            unit = "L"
    )
    public Attribute.Double out_catchment_rd1;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "Catchment outlet RD2 storage",
            unit = "L"
    )
    public Attribute.Double out_catchment_rd2;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "Catchment outlet RG1 storage",
            unit = "L"
    )
    public Attribute.Double out_catchment_rg1;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "Catchment outlet RG2 storage",
            unit = "L"
    )
    public Attribute.Double out_catchment_rg2;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "Catchment additional input outlet storage",
            unit = "L",
            defaultValue = "0"
    )
    public Attribute.Double out_catchment_add_in;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "Catchment outlet RG2 storage",
            unit = "L"
    )
    public Attribute.Double out_catchment_sim_runoff;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "temporal resolution [d or h]"
    )
    public Attribute.String par_temp_res;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "water level in reach"
    )
    public Attribute.Double out_water_level;

    /*
     *  Component run stages
     */
    int run_count = 0;
    double run_avg = 0;
    int run_slope_factor;

    public void init() {

        if (par_slope_as_proportion.getValue()) {
            run_slope_factor = 100;
        } else {
            run_slope_factor = 1;
        }
    }

    public void initAll() {

        if (par_check_slopes.getValue()) {

            run_avg = (run_avg * run_count + run_slope_factor * par_slope.getValue()) / ++run_count;

            if (run_avg >= 100) {
                getModel().getRuntime().sendHalt("Average reach slope exceeds 100%. please check your reach parameter file and \"slopeAsProportion\" parameter value!");
            }
            if (run_avg <= 0.1) {
                getModel().getRuntime().sendHalt("Average reach slope is below 0.1%. please check your reach parameter file and \"slopeAsProportion\" parameter value!");
            }
        }
    }

    public void run() {

        Attribute.Entity run_entity = st_entities.getCurrent();

        Attribute.Entity run_dest_reach = (Attribute.Entity) run_entity.getObject("to_reach");
        if (run_dest_reach.isEmpty()) {
            run_dest_reach = null;
        }
        Attribute.Entity run_dest_reservoir = null;

        if (run_entity.existsAttribute("to_reservoir")) {
            run_dest_reservoir = (Attribute.Entity) run_entity.getObject("to_reservoir");
        } else {
            run_dest_reservoir = null;
        }

        double run_width = this.par_width.getValue() * this.par_width_maf.getValue() + this.par_width_aaf.getValue();
        double run_rough = this.par_roughness.getValue() * this.par_roughness_maf.getValue() + this.par_roughness_aaf.getValue();
        double run_length = this.par_length.getValue();

        double run_slope = this.par_slope.getValue();
        if (!par_slope_as_proportion.getValue()) {
            run_slope = run_slope / 100;
        }

        if (run_slope == 0) {
            getModel().getRuntime().println("WARNING: Found zero slope in reach entity which will prevent water routing!", JAMS.VERBOSE);
        }

        double run_rd1_act = out_act_rd1.getValue() + in_rd1.getValue();
        double run_rd2_act = out_act_rd2.getValue() + in_rd2.getValue();
        double run_rg1_act = out_act_rg1.getValue() + in_rg1.getValue();
        double run_rg2_act = out_act_rg2.getValue() + in_rg2.getValue();

        double run_add_in_act = out_act_add_in.getValue() + this.in_add_in.getValue();

        in_rd1.setValue(0);
        in_rd2.setValue(0);
        in_rg1.setValue(0);
        in_rg2.setValue(0);

        in_add_in.setValue(0);

        out_act_rd1.setValue(0);
        out_act_rd2.setValue(0);
        out_act_rg1.setValue(0);
        out_act_rg2.setValue(0);

        out_act_add_in.setValue(0);

        double run_rd1_dest_in = 0;
        double run_rd2_dest_in = 0;
        double run_rg1_dest_in = 0;
        double run_rg2_dest_in = 0;
        double run_add_in_dest_in = 0;

        if (run_dest_reach == null && run_dest_reservoir == null) {
            run_rd1_dest_in = 0;//entity.getDouble(aNameCatchmentOutRD1.getValue());
            run_rd2_dest_in = 0;//entity.getDouble(aNameCatchmentOutRD2.getValue());
            run_rg1_dest_in = 0;//entity.getDouble(aNameCatchmentOutRG1.getValue());
            run_rg2_dest_in = 0;//entity.getDouble(aNameCatchmentOutRG2.getValue());

            run_add_in_dest_in = 0;
        } else if (run_dest_reservoir != null) {
            run_rd1_dest_in = run_dest_reservoir.getDouble("compRD1");
            run_rd2_dest_in = run_dest_reservoir.getDouble("compRD2");
            run_rg1_dest_in = run_dest_reservoir.getDouble("compRG1");
            run_rg2_dest_in = run_dest_reservoir.getDouble("compRG2");
        } else {
            run_rd1_dest_in = run_dest_reach.getDouble("in_rd1");
            run_rd2_dest_in = run_dest_reach.getDouble("in_rd2");
            run_rg1_dest_in = run_dest_reach.getDouble("in_rg1");
            run_rg2_dest_in = run_dest_reach.getDouble("in_rg2");

            try {
                run_add_in_dest_in = run_dest_reach.getDouble("in_add_in");
            } catch (jams.data.Attribute.Entity.NoSuchAttributeException e) {
                run_add_in_dest_in = 0;
            }
        }

        double run_q_act_tot = run_rd1_act + run_rd2_act + run_rg1_act + run_rg2_act + run_add_in_act;

        //int ID = (int)entity.getDouble("ID");
        // System.out.getRuntime().println("Processing reach: " + ID);
        if (run_q_act_tot == 0) {
            out_rd1.setValue(0);
            out_rd2.setValue(0);
            out_rg1.setValue(0);
            out_rg2.setValue(0);

            this.out_add_in.setValue(0);

            //nothing more to do here
            return;
        }

        //relative parts of the runoff components for later redistribution
        double run_rd1_part = run_rd1_act / run_q_act_tot;
        double run_rd2_part = run_rd2_act / run_q_act_tot;
        double run_rg1_part = run_rg1_act / run_q_act_tot;
        double run_rg2_part = run_rg2_act / run_q_act_tot;

        double run_add_in_part = run_add_in_act / run_q_act_tot;

        //calculation of flow velocity
        int run_sec_in_t_step = 0;
        if (this.par_temp_res.getValue().equals("d")) {
            run_sec_in_t_step = 86400;
        } else if (this.par_temp_res.getValue().equals("h")) {
            run_sec_in_t_step = 3600;
        }
        double run_flow_veloc = this.calcFlowVelocity(run_q_act_tot, run_width, run_slope, run_rough, run_sec_in_t_step);

        //recession coefficient
        double run_rk = (run_flow_veloc / run_length) * this.par_flow_route_ta.getValue() * 3600;

        //the whole outflow
        double run_q_act_out;
        if (run_rk > 0) {
            run_q_act_out = run_q_act_tot * Math.exp(-1 / run_rk);
        } else {
            run_q_act_out = 0;
        }

        //the actual outflow from the reach
        double run_rd1_out = run_q_act_out * run_rd1_part;
        double run_rd2_out = run_q_act_out * run_rd2_part;
        double run_rg1_out = run_q_act_out * run_rg1_part;
        double run_rg2_out = run_q_act_out * run_rg2_part;

        double run_add_in_out = run_q_act_out * run_add_in_part;

        //transferring runoff from this reach to the next one or a reservoir
        run_rd1_dest_in = run_rd1_dest_in + run_rd1_out;
        run_rd2_dest_in = run_rd2_dest_in + run_rd2_out;
        run_rg1_dest_in = run_rg1_dest_in + run_rg1_out;
        run_rg2_dest_in = run_rg2_dest_in + run_rg2_out;

        run_add_in_dest_in = run_add_in_dest_in + run_add_in_out;

        //reducing the actual storages
        run_rd1_act = run_rd1_act - run_q_act_out * run_rd1_part;
        run_rd2_act = run_rd2_act - run_q_act_out * run_rd2_part;
        run_rg1_act = run_rg1_act - run_q_act_out * run_rg1_part;
        run_rg2_act = run_rg2_act - run_q_act_out * run_rg2_part;

        run_add_in_act = run_add_in_act - run_q_act_out * run_add_in_part;

        double run_channel_storage = run_rd1_act + run_rd2_act + run_rg1_act + run_rg2_act + run_add_in_act;

        double run_cum_outflow = run_rd1_out + run_rd2_out + run_rg1_out + run_rg2_out + run_add_in_out;
        /*if (reachID.getValue()==800)
        {System.out.println(RD1out);
        System.out.println(RD2out);
        System.out.println(RG1out);
        System.out.println(RG2out);
        }
         */

        out_sim_runoff.setValue(run_cum_outflow);
        this.out_channel_storage.setValue(run_channel_storage);
        in_rd1.setValue(0);
        in_rd2.setValue(0);
        in_rg1.setValue(0);
        in_rg2.setValue(0);

        in_add_in.setValue(0);

        out_act_rd1.setValue(run_rd1_act);
        out_act_rd2.setValue(run_rd2_act);
        out_act_rg1.setValue(run_rg1_act);
        out_act_rg2.setValue(run_rg2_act);

        out_act_add_in.setValue(run_add_in_act);

        out_rd1.setValue(run_rd1_out);
        out_rd2.setValue(run_rd2_out);
        out_rg1.setValue(run_rg1_out);
        out_rg2.setValue(run_rg2_out);

        out_add_in.setValue(run_add_in_out);
       // double verzoegerung; mise en commentaire par Michael Rabotin mars 2025
        //reach
        if (run_dest_reach != null && run_dest_reservoir == null) {
            run_dest_reach.setDouble("in_rd1", run_rd1_dest_in);
            run_dest_reach.setDouble("in_rd2", run_rd2_dest_in);
            run_dest_reach.setDouble("in_rg1", run_rg1_dest_in);
            run_dest_reach.setDouble("in_rg2", run_rg2_dest_in);

            run_dest_reach.setDouble("in_add_in", run_add_in_dest_in);

        } //reservoir
        else if (run_dest_reservoir != null) {
            run_dest_reservoir.setDouble("compRD1", run_rd1_dest_in);
            run_dest_reservoir.setDouble("compRD2", run_rd2_dest_in);
            run_dest_reservoir.setDouble("compRG1", run_rg1_dest_in);
            run_dest_reservoir.setDouble("compRG2", run_rg2_dest_in);
        } //outlet
        else if (run_dest_reach == null && run_dest_reservoir == null) {
            out_catchment_rd1.setValue(run_rd1_out);
            out_catchment_rd2.setValue(run_rd2_out);
            out_catchment_rg1.setValue(run_rg1_out);
            out_catchment_rg2.setValue(run_rg2_out);

            this.out_catchment_add_in.setValue(run_add_in_out);
            //neu verzoegerung

            out_catchment_sim_runoff.setValue(run_cum_outflow);
        }

        out_water_level.setValue(run_channel_storage / (1000 * run_width * run_length));

    }

    public void cleanup() {

    }

    /**
     * Calculates flow velocity in specific reach
     *
     * @param run_q the runoff in the reach
     * @param run_width the width of reach
     * @param run_slope the slope of reach
     * @param run_rough the roughness of reach
     * @param run_seconds_of_time_step the current time step in seconds
     * @return flow_velocity in m/s
     */
    public static double calcFlowVelocity(double run_q, double run_width, double run_slope, double run_rough, int run_seconds_of_time_step) {
        double run_afv = 1;
        double run_veloc = 0;

        /**
         * transfering liter/d to m³/s
         *
         */
        double run_q_m = run_q / (1000 * run_seconds_of_time_step);
        double run_rh = calcHydraulicRadius(run_afv, run_q_m, run_width);
        boolean run_cont = true;
        while (run_cont) {
            run_veloc = (run_rough) * Math.pow(run_rh, (2.0 / 3.0)) * Math.sqrt(run_slope);
            if ((Math.abs(run_veloc - run_afv)) > 0.001) {
                run_afv = run_veloc;
                run_rh = calcHydraulicRadius(run_afv, run_q_m, run_width);
            } else {
                run_cont = false;
                run_afv = run_veloc;
            }
        }
        return run_afv;
    }

    /**
     * Calculates the hydraulic radius of a rectangular stream bed depending on
     * daily runoff and flow_velocity
     *
     * @param run_v the flow velocity
     * @param run_q the daily runoff
     * @param run_width the width of reach
     * @return hydraulic radius in m
     */
    public static double calcHydraulicRadius(double run_v, double run_q, double run_width) {
        double run_a = (run_q / run_v);

        double run_rh = run_a / (run_width + 2 * (run_a / run_width));

        return run_rh;
    }
}
