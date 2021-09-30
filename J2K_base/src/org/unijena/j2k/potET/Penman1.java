/*
 * Penman.java
 * Created on 24. November 2005, 13:57
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
package org.unijena.j2k.potET;

import java.io.*;
import jams.data.*;
import jams.model.*;

/**
 *
 * @author Peter Krause
 */
@JAMSComponentDescription(title = "Penman",
        author = "Peter Krause",
        description = "Calculates potential evaporation according to Penman",
        date = "2021-02-18",
        version = "1.0_1")
@VersionComments(entries = {
    @VersionComments.Entry(version = "1.0_0", comment = "Initial version", date = "2005-11-24"),
    @VersionComments.Entry(version = "1.0_1", comment = "Cleaned up component", date = "2021-02-18")
})
public class Penman1 extends JAMSComponent {

    public final double CP = 1.031E-3; //MJ/kg°C

    public final double RSS = 150;

    /*
     *  Component variables
     */
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
            description = "Current time")
    public Attribute.Calendar time;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
            description = "temporal resolution [d | h | m]")
    public Attribute.String tempRes;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
            description = "state variable wind")
    public Attribute.Double wind;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
            description = "state variable mean temperature")
    public Attribute.Double tmean;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
            description = "state variable relative humidity")
    public Attribute.Double rhum;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
            description = "state variable net radiation")
    public Attribute.Double netRad;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
            description = "attribute elevation")
    public Attribute.Double elevation;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
            description = "attribute area")
    public Attribute.Double area;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.WRITE,
            description = "potential evaporation",
            unit = "L")
    public Attribute.Double potE;

    /*
     *  Component run stages
     */
    public void run() throws Attribute.Entity.NoSuchAttributeException, IOException {

        double netRad = this.netRad.getValue();
        double temperature = this.tmean.getValue();
        double rhum = this.rhum.getValue();
        double wind = this.wind.getValue();
        double elevation = this.elevation.getValue();
        double area = this.area.getValue();

        double abs_temp = org.unijena.j2k.physicalCalculations.ClimatologicalVariables.calc_absTemp(temperature, "degC");
        double delta_s = org.unijena.j2k.physicalCalculations.ClimatologicalVariables.calc_slopeOfSaturationPressureCurve(temperature);
        double pz = org.unijena.j2k.physicalCalculations.ClimatologicalVariables.calc_atmosphericPressure(elevation, abs_temp);
        double est = org.unijena.j2k.physicalCalculations.ClimatologicalVariables.calc_saturationVapourPressure(temperature);
        double ea = org.unijena.j2k.physicalCalculations.ClimatologicalVariables.calc_vapourPressure(rhum, est);

        double latH = org.unijena.j2k.physicalCalculations.ClimatologicalVariables.calc_latentHeatOfVaporization(temperature);
        double psy = org.unijena.j2k.physicalCalculations.ClimatologicalVariables.calc_psyConst(pz, latH);

        double groundHF = this.calc_groundHeatFlux(netRad);
        double vT = org.unijena.j2k.physicalCalculations.ClimatologicalVariables.calc_VirtualTemperature(abs_temp, pz, ea);
        double pa = org.unijena.j2k.physicalCalculations.ClimatologicalVariables.calc_AirDensityAtConstantPressure(vT, pz);

        double pE = 0;

        double letP = this.calcPenman(delta_s, netRad, groundHF, pa, CP, est, ea, psy, wind);

        pE = letP / latH;

        //converting mm to litres
        pE = pE * area;

        //aggregation to monthly values
        if (this.time != null) {
            if (this.tempRes.getValue().equals("m")) {
                int daysInMonth = this.time.getActualMaximum(Attribute.Calendar.DATE);
                pE = pE * daysInMonth;
            }
        }
        //avoiding negative potE values
        if (pE < 0) {
            pE = 0;
        }

        this.potE.setValue(pE);

    }

    private double calcPenman(double ds, double netRad, double groundHF, double pa, double CP, double est, double ea, double psy, double wind) {
        double fu = (0.27 + 0.2333 * wind);
        double Letp = (ds * (netRad - groundHF) + (pa * CP * (est - ea) * fu)) / (ds + psy);
        return Letp;
    }

    private double calc_groundHeatFlux(double netRad) {
        double g = 0.1 * netRad;
        return g;
    }

}
