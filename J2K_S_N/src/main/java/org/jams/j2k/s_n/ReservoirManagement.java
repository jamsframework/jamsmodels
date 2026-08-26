/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jams.j2k.s_n;

import jams.data.*;
import jams.model.*;

/**
 *
 * @author c8fima
 */
@JAMSComponentDescription(
        title = "ReservoirManagement",
        author = "Manfred Fink",
        description = "Simple Reservoir module"
)
public class ReservoirManagement extends JAMSComponent {

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Desinged volume",
            unit = "L"
    )
    public Attribute.Double desVolume;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "inflow from reach",
            unit = "L"
    )
    public Attribute.Double storageInput;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "storage in the reservoir",
            unit = "L"
    )
    public Attribute.Double res_storage;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "release from reservoir",
            unit = "L"
    )
    public Attribute.Double outflow;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "actual water use for water uses like irrigation, drinking water supply, etc.",
            unit = "L"
    )
    public Attribute.Double use_outflow;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "actual water use for water uses like irrigation, drinking water supply, etc. (same as 'use_outflow' but not set to 0.0)",
            unit = "L"
    )
    public Attribute.Double water_used;
    

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "Usable reservoir for water uses like irrigation, drinking water supply, etc. ",
            unit = "L"
    )
    public Attribute.Double useVolume;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "actual fill proportion of the reservoir",
            lowerBound = 0,
            upperBound = 1
    )
    public Attribute.Double act_fillrate;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "proportion filled at the beginning",
            lowerBound = 0,
            upperBound = 1
    )
    public Attribute.Double initfilrate;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "proportion were overproportional storage begin",
            lowerBound = 0,
            upperBound = 1
    )
    public Attribute.Double add_filrate;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "proportion were overproportional release begin",
            lowerBound = 0,
            upperBound = 1
    )
    public Attribute.Double rele_filrate;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "optimal proportion",
            lowerBound = 0,
            upperBound = 1
    )
    public Attribute.Double opt_filrate;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "minimum outflow",
            unit = "L",
            lowerBound = 0,
            upperBound = 1
    )
    public Attribute.Double min_flux;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "normal outflow",
            unit = "L",
            lowerBound = 0
    )
    public Attribute.Double normal_flow;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "floodprotection threshold",
            unit = "L",
            lowerBound = 0
    )
    public Attribute.Double interception_inflow;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Damping factor",
            lowerBound = 0
    )
    public Attribute.Double outDamping;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "Floodmode"
    )
    public Attribute.Boolean Floodmode;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "duration counter of the floodmode"
    )
    public Attribute.Double fl_index;

//Berechnung
    public void init() {

        res_storage.setValue(desVolume.getValue() * initfilrate.getValue());
        Floodmode.setValue(false);
        fl_index.setValue(0);

    }

    public void run() {
        double pot_surplus = 0;
        double run_res_storage = res_storage.getValue();
        double run_res_storageInput = storageInput.getValue();
        double run_min_flux = min_flux.getValue();
        double run_add_filrate = add_filrate.getValue();
        double run_rele_filrate = rele_filrate.getValue();
        double run_normal_flow = normal_flow.getValue();
        double run_interception_inflow = interception_inflow.getValue();
        double run_desVolume = desVolume.getValue();
        double run_demand = 0;
        double pot_inter = 0;
        double run_rele = 0;
        double run_outflow = 0;

        Double run_opt_filrate = opt_filrate.getValue();

        run_res_storage = run_res_storage - use_outflow.getValue();

        run_res_storage = run_res_storage + run_res_storageInput;

        double fillrate = run_res_storage / desVolume.getValue();

        double i = 0;

        if (Floodmode.getValue()) {

            i = fl_index.getValue();

            double floodroom = run_desVolume - (run_desVolume * run_rele_filrate);

            double targetvolume = (run_desVolume * run_rele_filrate) + ((floodroom * (outDamping.getValue() - i)) / outDamping.getValue());

            if (run_res_storage > targetvolume) {

                run_outflow = run_res_storage - targetvolume;

            } else {
                run_outflow = run_normal_flow;
            }

            i++;

            fl_index.setValue(i);

            if (i == outDamping.getValue()) {
                Floodmode.setValue(false);
                i = 0;
                fl_index.setValue(i);
            }

        } else {

            if (fillrate < run_add_filrate * 1.1) {

                run_outflow = run_min_flux;

            //   run_outflow = run_min_flux * ((run_add_filrate - fillrate) / run_add_filrate);
            //    run_outflow = (run_outflow + ((outDamping.getValue() / 100) * outflow.getValue())) / (1 + (outDamping.getValue() / 100));
                Floodmode.setValue(false);

            } else if (fillrate < run_opt_filrate) {

                pot_surplus = run_normal_flow - run_min_flux;

                run_demand = ((run_opt_filrate - fillrate) / (run_opt_filrate - run_add_filrate));

                run_outflow = Math.max(run_min_flux + (pot_surplus * run_demand), run_min_flux);

                run_outflow = (run_outflow + (outDamping.getValue() * outflow.getValue())) / (1 + outDamping.getValue());

                Floodmode.setValue(false);

            } else if (fillrate < 1) {

                pot_inter = run_interception_inflow - run_normal_flow;

                run_rele = ((fillrate - run_opt_filrate) / (run_rele_filrate - run_opt_filrate));

                run_outflow = Math.min(run_normal_flow + (pot_inter * run_rele), run_interception_inflow);

                run_outflow = (run_outflow + (outDamping.getValue() * outflow.getValue())) / (1 + outDamping.getValue());

                Floodmode.setValue(false);

            } else {

                run_outflow = run_res_storage - run_desVolume;

                Floodmode.setValue(true);

                //run_outflow = (run_outflow + ((outDamping.getValue()/100) * outflow.getValue())) / (1 + (outDamping.getValue()/100));
            }
        }

        run_outflow = Math.max(run_outflow, run_min_flux / 2);

        if (run_res_storage > run_outflow) {
            run_res_storage = run_res_storage - run_outflow;
        } else {
            run_outflow = run_res_storage;
            run_res_storage = 0;
        }

        fillrate = run_res_storage / desVolume.getValue();

        double run_useVolume = 0;

        if (fillrate > run_add_filrate) {

            run_useVolume = run_res_storage - (run_add_filrate * run_desVolume);

        }

        
        water_used.setValue(use_outflow.getValue());
        useVolume.setValue(run_useVolume);
        act_fillrate.setValue(fillrate);
        outflow.setValue(run_outflow);
        res_storage.setValue(run_res_storage);
        use_outflow.setValue(0.0);

    }

}
