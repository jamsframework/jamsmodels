/*
 * FarmDam.java
 * Created on 31.08.2016, 14:21:42
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
package agriculture;

import jams.data.*;
import jams.model.*;

/**
 *
 * @author Sven Kralisch <sven.kralisch at uni-jena.de>
 */
@JAMSComponentDescription(
        title = "FarmDam",
        author = "Sven Kralisch",
        description = "Component for representing impact of farm dams on runoff",
        date = "2016-08-21",
        version = "1.0_0"
)
@VersionComments(entries = {
    @VersionComments.Entry(version = "1.0_0", comment = "Initial version")
})
public class FarmDam extends JAMSComponent {

    /*
     *  Component attributes
     */
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Date")
    public Attribute.Calendar currentDate;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "ID of current reach"
    )
    public Attribute.Double currentReachID;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "ID of reach this dam belongs to"
    )
    public Attribute.Double targetReachID;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "Runoff components of the reach that should be used "
            + "for dam filling"
    )
    public Attribute.Double[] flowComponents;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Julian day at which all the water is consumed",
            defaultValue = "1"
    )
    public Attribute.Integer resetDay;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Proportion of incoming water that is used for filling",
            defaultValue = "1",
            lowerBound = 0,
            upperBound = 1
    )
    public Attribute.Double usageProportion;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Dam capacity",
            unit = "L"
    )
    public Attribute.Double capacity;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "Current dam volume",
            unit = "L"
    )
    public Attribute.Double volume;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "Overall dam storage",
            unit = "L"
    )
    public Attribute.Double overallStorage;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Dam identifyer"
    )
    public Attribute.String damID;


    /*
     *  Component run stages
     */
    @Override
    public void init() {
        volume.setValue(0);
        overallStorage.setValue(0);
    }

    @Override
    public void run() {
        if (currentReachID.getValue() != targetReachID.getValue()) {
            return;
        }

        int jd = currentDate.get(Attribute.Calendar.DAY_OF_YEAR);

        // check if dam should be emptied; if so, store the water in 
        // overallstorage for later balancing
        if (jd == resetDay.getValue()) {
            overallStorage.setValue(overallStorage.getValue() + volume.getValue());
            volume.setValue(0);
        }

        // calc overall incoming flow volume 
        double incomingVolume = 0;
        for (Attribute.Double flowComponent : flowComponents) {
            incomingVolume += flowComponent.getValue();
        }

        // calculate the volume that can be added to the dam based on incoming 
        // water, usageProportion and remaining dam volume
        double newStorage = Math.min(incomingVolume * usageProportion.getValue(), capacity.getValue() - volume.getValue());

        // add the water to the dam volume
        volume.setValue(volume.getValue() + newStorage);

        // calculate the fraction of incoming water that is stored
        double fraction = (incomingVolume == 0) ? 0 : newStorage / incomingVolume;

        // remove the stored fraction from the flow components
        for (int i = 0; i < flowComponents.length; i++) {
            flowComponents[i].setValue(flowComponents[i].getValue() * (1 - fraction));
        }

    }

    @Override
    public void cleanup() {
    }
}
