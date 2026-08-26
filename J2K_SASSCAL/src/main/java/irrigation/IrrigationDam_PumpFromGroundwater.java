/*
 * IrrigationDam_PumpFromGroundwater.java
 * Created on 08.09.2020, 23:11:44
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
package irrigation;

import jams.data.*;
import jams.data.Attribute.Calendar;
import jams.model.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Sven Kralisch <sven.kralisch@uni-jena.de>
 */
@JAMSComponentDescription(
        title = "IrrigationDam_PumpFromGroundwater",
        author = "Sven Kralisch",
        description = "TBD",
        date = "2020-09-07",
        version = "1.0_0"
)
@VersionComments(entries = {
    @VersionComments.Entry(version = "1.0_0", comment = "Initial version")
})
public class IrrigationDam_PumpFromGroundwater extends JAMSComponent {

    /*
     *  Component attributes
     */
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Current date"
    )
    public Attribute.Calendar date;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "List of hrus"
    )
    public Attribute.EntityCollection hrus;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "List of reaches"
    )
    public Attribute.EntityCollection reaches;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Dam capacity",
            defaultValue = "0",
            unit = "m³",
            lowerBound = 0,
            upperBound = Double.POSITIVE_INFINITY
    )
    public Attribute.Double damCapacity;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Pumping capactiy from groundwater",
            defaultValue = "0",
            unit = "m³",
            lowerBound = 0,
            upperBound = Double.POSITIVE_INFINITY
    )
    public Attribute.Double groundwaterPump;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Julian day at which pumping from groundwater starts",
            defaultValue = "1",
            lowerBound = 0,
            upperBound = 366
    )
    public Attribute.Double groundwaterStart;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Julian day at which pumping from groundwater ends",
            defaultValue = "1",
            lowerBound = 0,
            upperBound = 366
    )
    public Attribute.Double groundwaterEnd;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Name of sub-basin attribute",
            defaultValue = "subbasin"
    )
    public Attribute.String subbasinAttributeName;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Name of groundwater attribute",
            defaultValue = "actRG1;actRG2"
    )
    public Attribute.StringArray groundwaterAttributeNames;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "Adaptation factor for reducing the water that is allowed to be pumped",
            defaultValue = "1",
            lowerBound = 0,
            upperBound = 1
    )
    public Attribute.Double adaptationFactor;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "Current dam storage",
            defaultValue = "0",
            unit = "L",
            lowerBound = 0,
            upperBound = Double.POSITIVE_INFINITY
    )
    public Attribute.Double damStorage;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "Water volume pumped at current time step",
            defaultValue = "0",
            unit = "L",
            lowerBound = 0,
            upperBound = Double.POSITIVE_INFINITY
    )
    public Attribute.Double pumpVolume;

    private Map<Attribute.Entity, List<Attribute.Entity>> reach2hruMap = new HashMap();

    /*
     *  Component run stages
     */
    @Override
    public void init() {

        Map<Long, Attribute.Entity> reachMap = new HashMap();
        for (Attribute.Entity reach : reaches.getEntities()) {
            reachMap.put(reach.getId(), reach);
        }

        for (Attribute.Entity hru : hrus.getEntities()) {
            double subbasinID = hru.getDouble(subbasinAttributeName.getValue());
            Attribute.Entity reach = reachMap.get((long) subbasinID);

            List<Attribute.Entity> hruList = reach2hruMap.get(reach);
            if (hruList == null) {
                hruList = new ArrayList();
                reach2hruMap.put(reach, hruList);
            }

            hruList.add(hru);
        }

//        List<Attribute.Entity> l = reach2hruMap.get(reachMap.get(380l));
//        for (Attribute.Entity e : l) {
//            System.out.println(e.getId());
//        }
    }

    @Override
    public void run() {

        double availableCapacity = damCapacity.getValue() * 1000 - damStorage.getValue();
        double pumpedWater = 0;

        //check dam full
        if (availableCapacity <= 0) {
            pumpVolume.setValue(0);
            return;
        }

        int julDay = date.getValue().get(Calendar.DAY_OF_YEAR);

        double gStart = this.groundwaterStart.getValue();
        double gEnd = this.groundwaterEnd.getValue();
        double gJD = julDay;

        if (gStart > gEnd) {
            gEnd = 366 + gEnd;
            gJD = 366 + gJD;
        }

        //pump from groundwater?
        if (gJD >= gStart && gJD <= gEnd) {

            // get the list of related HRUs
            Attribute.Entity reach = reaches.getCurrent();
            List<Attribute.Entity> hrus = reach2hruMap.get(reach);

            // sum up available groundwater
            double totalIn = 0;
            for (String groundwaterAttributeName : groundwaterAttributeNames.getValue()) {
                for (Attribute.Entity hru : hrus) {
                    totalIn += hru.getDouble(groundwaterAttributeName);
                }
            }

            // reduce the water that we consider to be available?
            totalIn *= adaptationFactor.getValue();

            // get amount of water that is available for pumping
            double totalAvail = Math.min(totalIn, groundwaterPump.getValue() * 1000);

            // calc fraction of available water that can be stored
            double frac = Math.min(availableCapacity / totalAvail, 1);

            // remove that fraction from single components
            double frac1 = 1 - frac;
            for (String groundwaterAttributeName : groundwaterAttributeNames.getValue()) {
                for (Attribute.Entity hru : hrus) {
                    hru.setDouble(groundwaterAttributeName, frac1 * hru.getDouble(groundwaterAttributeName));
                }
            }

            // add the water to the dam
            pumpedWater = totalAvail * frac;
            damStorage.setValue(damStorage.getValue() + pumpedWater);

        }

        pumpVolume.setValue(pumpedWater);
    }

}
