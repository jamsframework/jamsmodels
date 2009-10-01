/*
 * InitJ2KProcessGroundwater.java
 * Created on 25. November 2005, 16:54
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
package org.unijena.j2k.gw;

import jams.data.*;
import jams.model.*;

/**
 *
 * @author c0krpe
 */
@JAMSComponentDescription(title = "J2KGroundwater",
author = "Peter Krause modifications Daniel Varga",
description = "Description")
public class InitJ2KProcessGroundwater_D_1_1_v02 extends JAMSComponent {

    /*
     *  Component variables
     */
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    update = JAMSVarDescription.UpdateType.RUN,
    description = "The current hru entity")
    public JAMSEntityCollection hrus;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    update = JAMSVarDescription.UpdateType.RUN,
    description = "The current reach entity")
    public JAMSEntityCollection reaches;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    update = JAMSVarDescription.UpdateType.RUN,
    description = "attribute area")
    public JAMSDouble area;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.WRITE,
    update = JAMSVarDescription.UpdateType.RUN,
    description = "Distance between adjacent entities")
    public JAMSDouble gwFlowLength;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.WRITE,
    update = JAMSVarDescription.UpdateType.RUN,
    description = "Length of the border arc between adjacent entities")
    public JAMSDouble gwArcLength;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.WRITE,
    update = JAMSVarDescription.UpdateType.RUN,
    description = "maximum RG1 storage")
    public JAMSDouble maxRG1;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.WRITE,
    update = JAMSVarDescription.UpdateType.RUN,
    description = "maximum RG2 storage")
    public JAMSDouble maxRG2;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.WRITE,
    update = JAMSVarDescription.UpdateType.RUN,
    description = "actual RG1 storage")
    public JAMSDouble actRG1;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.WRITE,
    update = JAMSVarDescription.UpdateType.RUN,
    description = "actual RG2 storage")
    public JAMSDouble actRG2;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    update = JAMSVarDescription.UpdateType.INIT,
    description = "relative initial RG1 storage")
    public JAMSDouble initRG1;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    update = JAMSVarDescription.UpdateType.INIT,
    description = "relative initial RG2 storage")
    public JAMSDouble initRG2;
    
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READWRITE,
    update = JAMSVarDescription.UpdateType.RUN,
    description = "Downstream hru entity")
    public JAMSEntity toPoly;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READWRITE,
    update = JAMSVarDescription.UpdateType.RUN,
    description = "Downstream reach entity")
    public JAMSEntity toReach;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    description = "entity x-coordinate")
    public JAMSDouble entityX;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    description = "entity y-coordinate")
    public JAMSDouble entityY;

        @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "Groundwater Level"
            )
            public JAMSDouble gwTable;


    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.WRITE,
    description = "Groundwater Level")
    public JAMSDouble waterTable_NN;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    update = JAMSVarDescription.UpdateType.RUN,
    description = "estimated hydraulic conductivity in cm/d")
    public JAMSDouble Kf_geo;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    update = JAMSVarDescription.UpdateType.RUN,
    description = "estimated hydraulic conductivity in cm/d")
    public JAMSDouble KfAdaptation;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.WRITE,
    update = JAMSVarDescription.UpdateType.RUN,
    description = "estimated porosity")
    public JAMSDouble Peff;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.WRITE,
    update = JAMSVarDescription.UpdateType.RUN,
    description = "adapted Kf_geo")
    public JAMSDouble Kf_geo_adapt;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    update = JAMSVarDescription.UpdateType.RUN,
    description = "Thickness of the aquifer")
    public JAMSDouble aqThickness;


    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READWRITE,
    update = JAMSVarDescription.UpdateType.RUN,
    description = "Height of the aquifer base in m + NN")
    public JAMSDouble baseHeigth;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READWRITE,
    update = JAMSVarDescription.UpdateType.RUN,
    description = "Height of the aquifer base in m + NN")
    public JAMSDouble baseHeigth_v;


    /*
     *  Component run stages
     */

    double run_area, run_Peff, run_Kf_geo, run_KfAdaptation, run_aqThickness, run_baseHeigth,  upX, upY, downX, downY, run_Kf_geo_adapted, run_gwFlowLength,
           run_gwDepth, run_gwTable, run_actRG2, run_maxRG2;
       

    public void init() throws JAMSEntity.NoSuchAttributeException {

    }

    public void run() throws JAMSEntity.NoSuchAttributeException {

        Attribute.Entity entity = hrus.getCurrent();

        run_area = area.getValue();                                 //[m]
        run_Kf_geo = Kf_geo.getValue();                             //[cm/d]
        run_KfAdaptation = KfAdaptation.getValue();                 //[-]
        run_aqThickness = aqThickness.getValue();                   //[m]
        run_baseHeigth = baseHeigth.getValue();
        double run_baseHeigth_v = baseHeigth_v.getValue();


        upX = entityX.getValue();
        upY = entityY.getValue();

        if (toPoly.getValue() != null) {
            downX = toPoly.getDouble("x");
            downY = toPoly.getDouble("y");
        }
        if (toReach.getValue() != null) {
            downX = toReach.getDouble("x");   
            downY = toReach.getDouble("y");
        }
        run_gwFlowLength = Math.pow((Math.pow((upY - downY), 2) + (Math.pow((upX - downX), 2))), 0.5);

        run_Kf_geo_adapted = run_Kf_geo * run_KfAdaptation / 100 /(60* 60* 24);
        run_Peff = 0.462 + 0.045 * Math.log(run_Kf_geo_adapted);
        if (run_Peff < 0.01){
            run_Peff = 0.01;
        }
        run_maxRG2 = run_aqThickness * run_area * run_Peff * 1000;  //[l]
        run_actRG2 = run_maxRG2 * initRG2.getValue();

        run_gwDepth = run_actRG2 / 1000 / run_area / run_Peff;
        run_baseHeigth = run_baseHeigth + run_baseHeigth_v;

        run_gwTable = run_gwDepth + run_baseHeigth; //baseHeigth

        baseHeigth.setValue(run_baseHeigth);
        gwFlowLength.setValue(run_gwFlowLength);                    //[m]
        gwTable.setValue(run_gwTable);                              //[m]
        Kf_geo_adapt.setValue(run_Kf_geo_adapted);                  //[m/s]
        Peff.setValue(run_Peff);
        actRG1.setValue(0);
        actRG2.setValue(run_actRG2);                                //[l]
        maxRG2.setValue(run_maxRG2);                                //[l]
    }

    public void cleanup() {
    }
}
