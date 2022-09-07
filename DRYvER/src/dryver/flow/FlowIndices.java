/*
 * FlowIndices.java
 * Created on 10.08.2022, 23:55:35
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
package dryver.flow;

import jams.data.*;
import jams.model.*;

/**
 *
 * @author Sven Kralisch <sven at kralisch.com>
 */
@JAMSComponentDescription(
        title = "FlowIndices",
        author = "Sven Kralisch",
        description = "Calculates various flow indices",
        date = "2022-08-10",
        version = "1.0_0"
)
@VersionComments(entries = {
    @VersionComments.Entry(version = "1.0_0", comment = "Initial version", date = "2022-08-10")
})
public class FlowIndices extends JAMSComponent {

    /*
     *  Component attributes
     */
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
            description = "Current time")
    public Attribute.Calendar time;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
            description = "daily flow state")
    public Attribute.Double flowState;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.WRITE,
            description = "calculates if dry condition did occur")
    public Attribute.Double isDry;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.WRITE,
            description = "calculates if pool condition did occur")
    public Attribute.Double isPool;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.WRITE,
            description = "calculates if flowing condition did occur")
    public Attribute.Double isFlowing;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.WRITE,
            description = "number of consecutive days with dry condition")
    public Attribute.Double consecutiveDaysDry;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.WRITE,
            description = "number of consecutive days with pool condition")
    public Attribute.Double consecutiveDaysPool;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.WRITE,
            description = "number of consecutive days with flowing condition")
    public Attribute.Double consecutiveDaysFlowing;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.WRITE,
            description = "length of period with dry condition")
    public Attribute.Double lengthPeriodDry;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.WRITE,
            description = "length of period with pool condition")
    public Attribute.Double lengthPeriodPool;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.WRITE,
            description = "length of period with flowing condition")
    public Attribute.Double lengthPeriodFlowing;

    /*
     *  Component run stages
     */
    @Override
    public void init() {
    }

    @Override
    public void run() {

        int lastDayMonth = time.getActualMaximum(Attribute.Calendar.DAY_OF_MONTH);
        int lastDayYear = time.getActualMaximum(Attribute.Calendar.DAY_OF_YEAR);
        int currentDayMonth = time.get(Attribute.Calendar.DAY_OF_MONTH);
        int currentDayYear = time.get(Attribute.Calendar.DAY_OF_YEAR);

        isDry.setValue(0);
        isPool.setValue(0);
        isFlowing.setValue(0);
        lengthPeriodDry.setValue(0);
        lengthPeriodPool.setValue(0);
        lengthPeriodFlowing.setValue(0);

        if (flowState.getValue() == 1) {
            isFlowing.setValue(1);
        } else if (flowState.getValue() == 1) {
            isDry.setValue(1);
        } else if (flowState.getValue() == 2) {
            isPool.setValue(1);
        }


        // consecutive days
        if (flowState.getValue() == 1) {
            consecutiveDaysFlowing.setValue(consecutiveDaysFlowing.getValue() + 1);
            lengthPeriodDry.setValue(consecutiveDaysDry.getValue());
            consecutiveDaysDry.setValue(0);
            lengthPeriodPool.setValue(consecutiveDaysPool.getValue());
            consecutiveDaysPool.setValue(0);
        } else if (flowState.getValue() == 0) {
            consecutiveDaysDry.setValue(consecutiveDaysDry.getValue() + 1);
            lengthPeriodFlowing.setValue(consecutiveDaysFlowing.getValue());
            consecutiveDaysFlowing.setValue(0);
            lengthPeriodPool.setValue(consecutiveDaysPool.getValue());
            consecutiveDaysPool.setValue(0);
        } else if (flowState.getValue() == 2) {
            consecutiveDaysPool.setValue(consecutiveDaysPool.getValue() + 1);
            lengthPeriodDry.setValue(consecutiveDaysDry.getValue());
            consecutiveDaysDry.setValue(0);
            lengthPeriodFlowing.setValue(consecutiveDaysFlowing.getValue());
            consecutiveDaysFlowing.setValue(0);
        }
        
        if (currentDayMonth == lastDayMonth || currentDayYear == lastDayYear) {
            lengthPeriodDry.setValue(consecutiveDaysDry.getValue());
            lengthPeriodPool.setValue(consecutiveDaysPool.getValue());
            lengthPeriodFlowing.setValue(consecutiveDaysFlowing.getValue());
            consecutiveDaysDry.setValue(0);
            consecutiveDaysPool.setValue(0);
            consecutiveDaysFlowing.setValue(0);
        }
        

//            if (successiveDaysWithoutRain.getValue() == 11.0) {
//                isDryPeriod.setValue(11.0);
//                isBeginningOfDryPeriod.setValue(1.0);
//            } else if (successiveDaysWithoutRain.getValue() > 11.0) {
//                isDryPeriod.setValue(1.0);
//
//            }
//        } else {
//            successiveDaysWithRain.setValue(successiveDaysWithRain.getValue() + 1);
//            longestPeriodWithoutRain.setValue(Math.max(successiveDaysWithoutRain.getValue(), longestPeriodWithoutRain.getValue()));
//            successiveDaysWithoutRain.setValue(0.0);
//        }
    }

    @Override
    public void cleanup() {
    }
}
