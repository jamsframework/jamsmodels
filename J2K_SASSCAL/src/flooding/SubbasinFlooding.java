/*
 * SubbasinFlooding.java
 * Created on 11.07.2015, 00:45:46
 *
 * This file is part of JAMS
 * Copyright (C) FSU Jena
 *
 * JAMS is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * JAMS is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with JAMS. If not, see <http://www.gnu.org/licenses/>.
 *
 */
package flooding;

import jams.data.*;
import jams.model.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 *
 * @author Sven Kralisch <sven.kralisch at uni-jena.de>
 */
@JAMSComponentDescription(
        title = "SubbasinInnundation",
        author = "Sven Kralisch & Markus Meinhardt",
        description = "iterate over (elevation-sorted) HRUs and distribute reach water; needs: Array list from StandardEntityReaderUpstreamTopo",
        date = "2015-12-11",
        version = "1.0_0"
)
@VersionComments(entries = {
    @VersionComments.Entry(version = "1.0_0", comment = "Initial version")
})
public class SubbasinFlooding extends JAMSComponent {

    /*
     *  Component attributes
     */
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "The HRUs that drain into the reach"
    )
    public Attribute.EntityCollection subbasinHRUs;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "The name of the HRU's elevation attribute",
            defaultValue = "elevation",
            unit = "m"
    )
    public Attribute.String elevationAttributeName;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "The name of the HRU's elevation attribute",
            defaultValue = "area",
            unit = "sq m"
    )
    public Attribute.String areaAttributeName;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "The name of the HRU's flood volume attribute that should be used for flooding",
            defaultValue = "floodVolume",
            unit = "liter"
    )
    public Attribute.String floodVolumeAttributeName;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "Runoff components of the reach that should be used for flooding",
            unit = "liter"
    )
    public Attribute.Double[] runoffComponents;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "reach width attribute",
            unit = "m"
    )
    public Attribute.Double reachWidth;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "reach length attribute",
            unit = "m"
    )
    public Attribute.Double reachLength;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "reach height attribute",
            unit = "m"
    )
    public Attribute.Double reachHeight;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "reach deepening attribute",
            unit = "m"
    )
    public Attribute.Double deepening;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "The name of the HRU's flood height attribute that was used for flooding",
            unit = "cubic m"
    )
    public Attribute.Double floodHeight;


    /*
     *  Component run stages
     */
    @Override
    public void initAll() {
        // sort subbasin HRUs according to their height
        Collections.sort(subbasinHRUs.getEntities(), (Attribute.Entity e1, Attribute.Entity e2)
                -> (int) (e1.getDouble(elevationAttributeName.getValue())
                - e2.getDouble(elevationAttributeName.getValue())));
    }

    @Override
    public void run() {

        // calc overall sum and store proportions
        double sum = 0;
        double[] proportion = new double[runoffComponents.length];

        for (Attribute.Double d : runoffComponents) {
            sum += d.getValue();
        }

        int i = 0;
        for (Attribute.Double d : runoffComponents) {
            proportion[i++] = d.getValue() / sum;
        }

        // calc initial water height above sea level in reach
        double floodLevel = sum / 1000 / reachLength.getValue() / reachWidth.getValue() - deepening.getValue();

        if (floodLevel <= 0) {
            for (Attribute.Entity e : subbasinHRUs.getEntities()) {
                e.setDouble(floodVolumeAttributeName.getValue(), 0);
            }
            this.floodHeight.setValue(0);
            return;
        }

        double floodArea = reachLength.getValue() * reachWidth.getValue();
        double floodVolume = floodLevel * floodArea;

        this.floodHeight.setValue(floodLevel);
        List<Attribute.Entity> floodedHRUs = new ArrayList();

        // iterate over (elevation-sorted) HRUs and distribute water...
        for (Attribute.Entity e : subbasinHRUs.getEntities()) {

            double hruHeight = e.getDouble(elevationAttributeName.getValue());
            if ((floodLevel + reachHeight.getValue()) > hruHeight) {

//                if (e.getId()==16127) {
//                    int h = 0;
//                }
                // calc new floodheight in m
                floodArea += e.getDouble(areaAttributeName.getValue());
                floodLevel = floodVolume / floodArea;
                floodedHRUs.add(e);
            }
        }

        double floodVolumeHRUSum = 0;
        for (Attribute.Entity e : floodedHRUs) {
            double HRUArea = e.getDouble(areaAttributeName.getValue());
            double floodVolumeHRU = HRUArea * floodLevel;
            floodVolumeHRUSum += floodVolumeHRU;
            e.setDouble(floodVolumeAttributeName.getValue(), floodVolumeHRU * 1000);
        }

        i = 0;
        for (Attribute.Double d : runoffComponents) {
            runoffComponents[i].setValue(runoffComponents[i].getValue() - proportion[i] * floodVolumeHRUSum * 1000);
            i++;
        }

    }

    @Override
    public void cleanup() {
    }
}
