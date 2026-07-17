/*
 * Runoff.java
 * Created on 30. September 2005, 11:37
 *
 * This file is part of JAMS
 * Copyright (C) 2005 S. Kralisch and P. Krause
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
package gov.usgs.thornthwaite;

import jams.model.*;
import jams.data.*;

/**
 *
 * @author S. Kralisch
 */
@JAMSComponentDescription (title = "Thorntwaite runoff (2007)",
                           author = "Sven Kralisch",
                           date = "30. September 2005",
                           description = "This component calculates the runoff based on a runoff factor " +
                           "surface runoff and snowmelt")
public class Direct_Runoff extends JAMSComponent {

    @JAMSVarDescription (access = JAMSVarDescription.AccessType.READ,
    description="A factorn defining how much water leaves the model - the remain will be stored in the model",
                         defaultValue = "0.05"
    )
    public Attribute.Double runoffFactor;

    @JAMSVarDescription (access = JAMSVarDescription.AccessType.READ,
    description="precip (storage)")
    public Attribute.Double precip;

    @JAMSVarDescription (access = JAMSVarDescription.AccessType.WRITE,
    description="Surface runoff water")
    public Attribute.Double directRunoff;

    @JAMSVarDescription (access = JAMSVarDescription.AccessType.READ,
    description="Water coming from snow melt")
    public Attribute.Double snowMelt;

    @JAMSVarDescription (access = JAMSVarDescription.AccessType.WRITE,
    description="Water that potenially infiltrates")
    public Attribute.Double surface_water;


    public void run() {
        double runoffFactor = this.runoffFactor.getValue();
        double directRunoff = this.directRunoff.getValue();
        double snowMelt = this.snowMelt.getValue();
        double precip = this.precip.getValue();

        double ro1 = (snowMelt + precip) * runoffFactor;
        double surface_water = (snowMelt + precip) * (1.0 - runoffFactor);
        this.surface_water.setValue(surface_water);
        this.directRunoff.setValue(ro1);
        this.precip.setValue(precip);
    }
}
