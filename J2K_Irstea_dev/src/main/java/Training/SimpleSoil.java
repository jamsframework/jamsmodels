/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Training;

import jams.JAMS;
import jams.data.*;
import jams.model.*;

/**
 *
 * @author Flora Branger
 */
@JAMSComponentDescription(
        title = "SimpleSoil",
        author = "Flora Branger - formation dev",
        description = "Simple soil leaching reservoir for each spatial modelling unit",
        version = "0.1",
        date = "2022-05-09")
public class SimpleSoil extends JAMSComponent {

    /*  Component variables
     */
//    @JAMSVarDescription(
//            access = JAMSVarDescription.AccessType.READ,
//            description = "time"
//            )
//            public Attribute.Calendar time;
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "The current spatial modelling entity"
    )
    public Attribute.Entity entity;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "attribute area",
            unit = "m²"
    )
    public Attribute.Double area;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "reservoir size",
    unit  = "mm"
    )
            public Attribute.Double Smax;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "reservoir outflow parameter",
    unit  = "-"
    )
            public Attribute.Double Tau;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "state variable net rain",
            unit = "L"
    )
    public Attribute.Double netRain;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "state variable snow melt",
            unit = "L"
    )
    public Attribute.Double SnowMelt;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "state variable potET",
            unit = "L"
    )
    public Attribute.Double potET;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "state variable actET",
            unit = "L"
    )
    public Attribute.Double actET;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "state var actual reservoir level",
            unit = "L"
    )
    public Attribute.Double actS;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "statevar RD1 inflow",
            unit = "L"
    )
    public Attribute.Double inRD1;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "statevar RD1 outflow",
            unit = "L"
    )
    public Attribute.Double outRD1;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "statevar RD2 inflow",
            unit = "L"
    )
    public Attribute.Double inRD2;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "statevar RD2 outflow",
            unit = "L"
    )
    public Attribute.Double outRD2;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "statevar RG1 inflow",
            unit = "L"
    )
    public Attribute.Double inRG1;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "statevar RG1 outflow",
            unit = "L"
    )
    public Attribute.Double outRG1;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "statevar RG2 inflow",
            unit = "L"
    )
    public Attribute.Double inRG2;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "statevar RG2 outflow",
            unit = "L"
    )
    public Attribute.Double outRG2;

//internal state variables
    double run_area, run_Smax, run_Tau, run_netRain, run_SnowMelt, run_potET, run_actET, run_actS,
            run_inRD1, run_outRD1, run_inRD2, run_outRD2, run_inRG1, run_outRG1, run_inRG2, run_outRG2; //run_soilDistMPSLPS2

    /*
     *  Component run stages
     */
    public void init() {

    }

    public void run() {

        this.run_area = area.getValue();
        this.run_Smax = Smax.getValue();
        this.run_Tau = Tau.getValue();
        this.run_netRain = netRain.getValue();
        this.run_SnowMelt = SnowMelt.getValue();
        this.run_potET = potET.getValue();
        this.run_actET = actET.getValue();
        this.run_actS = actS.getValue();

        this.run_outRD1 = 0;
        this.run_outRD2 = 0;
        this.run_outRG1 = 0;
        this.run_outRG2 = 0;

        // calculate precip / ETP balance (netRain + netSnow + inflows from other HRUs - potET)
        double Inflow = Math.max((this.run_netRain + this.run_SnowMelt + this.run_inRD1 + this.run_inRD2) - this.run_potET, 0);
        // calculate actET
        if ((this.run_netRain + this.run_SnowMelt) - this.run_potET < 0) {
            this.run_actET = run_potET - (this.run_netRain + this.run_SnowMelt);
        } else {
            this.run_actET = this.run_potET;
        }

        // calculate new reservoir level
        double ReservoirVolume = Inflow + this.run_actS;
        // if there is too much water
        if (ReservoirVolume > this.run_Smax * this.run_area) {
            // the reservoir is full
            this.run_actS = this.run_Smax * this.run_area;
            // rests flows out as surface runoff
            this.run_outRD1 = ReservoirVolume - Inflow;
        } else {
            this.run_actS = ReservoirVolume;
        }

        // calculate outflow with current reservoir level
        this.run_outRD2 = this.run_actS / this.run_Tau;

        // update all variables
        actET.setValue(this.run_actET);
        actS.setValue(this.run_actS);
        outRD1.setValue(this.run_outRD1);
        outRD2.setValue(this.run_outRD2);
        outRG1.setValue(this.run_outRG1);
        outRG2.setValue(this.run_outRG2);

    }

    public void cleanup() {

    }

}
