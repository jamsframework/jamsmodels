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
    public Attribute.Double consecutiveDaysDryYear;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.WRITE,
            description = "number of consecutive days with pool condition")
    public Attribute.Double consecutiveDaysPoolYear;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.WRITE,
            description = "number of consecutive days with flowing condition")
    public Attribute.Double consecutiveDaysFlowingYear;
    
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.WRITE,
            description = "length of period with dry condition")
    public Attribute.Double lengthPeriodDryYear;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.WRITE,
            description = "length of period with pool condition")
    public Attribute.Double lengthPeriodPoolYear;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.WRITE,
            description = "length of period with flowing condition")
    public Attribute.Double lengthPeriodFlowingYear;    
    
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.WRITE,
            description = "number of consecutive days with dry condition")
    public Attribute.Double consecutiveDaysDryMonth;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.WRITE,
            description = "number of consecutive days with pool condition")
    public Attribute.Double consecutiveDaysPoolMonth;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.WRITE,
            description = "number of consecutive days with flowing condition")
    public Attribute.Double consecutiveDaysFlowingMonth;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.WRITE,
            description = "length of period with dry condition")
    public Attribute.Double lengthPeriodDryMonth;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.WRITE,
            description = "length of period with pool condition")
    public Attribute.Double lengthPeriodPoolMonth;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.WRITE,
            description = "length of period with flowing condition")
    public Attribute.Double lengthPeriodFlowingMonth;
    
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
        lengthPeriodDryYear.setValue(0);
        lengthPeriodPoolYear.setValue(0);
        lengthPeriodFlowingYear.setValue(0);
        lengthPeriodDryMonth.setValue(0);
        lengthPeriodPoolMonth.setValue(0);
        lengthPeriodFlowingMonth.setValue(0);

        if (flowState.getValue() == 1) {
            isFlowing.setValue(1);
        } else if (flowState.getValue() == 1) {
            isDry.setValue(1);
        } else if (flowState.getValue() == 2) {
            isPool.setValue(1);
        }

        // consecutive days
        if (flowState.getValue() == 1) {
            consecutiveDaysFlowingYear.setValue(consecutiveDaysFlowingYear.getValue() + 1);
            lengthPeriodDryYear.setValue(consecutiveDaysDryYear.getValue());
            consecutiveDaysDryYear.setValue(0);
            lengthPeriodPoolYear.setValue(consecutiveDaysPoolYear.getValue());
            consecutiveDaysPoolYear.setValue(0);
            consecutiveDaysFlowingMonth.setValue(consecutiveDaysFlowingMonth.getValue() + 1);
            lengthPeriodDryMonth.setValue(consecutiveDaysDryMonth.getValue());
            consecutiveDaysDryMonth.setValue(0);
            lengthPeriodPoolMonth.setValue(consecutiveDaysPoolMonth.getValue());
            consecutiveDaysPoolMonth.setValue(0);
        } else if (flowState.getValue() == 0) {
            consecutiveDaysDryYear.setValue(consecutiveDaysDryYear.getValue() + 1);
            lengthPeriodFlowingYear.setValue(consecutiveDaysFlowingYear.getValue());
            consecutiveDaysFlowingYear.setValue(0);
            lengthPeriodPoolYear.setValue(consecutiveDaysPoolYear.getValue());
            consecutiveDaysPoolYear.setValue(0);
            consecutiveDaysDryMonth.setValue(consecutiveDaysDryMonth.getValue() + 1);
            lengthPeriodFlowingMonth.setValue(consecutiveDaysFlowingMonth.getValue());
            consecutiveDaysFlowingMonth.setValue(0);
            lengthPeriodPoolMonth.setValue(consecutiveDaysPoolMonth.getValue());
            consecutiveDaysPoolMonth.setValue(0);
        } else if (flowState.getValue() == 2) {
            consecutiveDaysPoolYear.setValue(consecutiveDaysPoolYear.getValue() + 1);
            lengthPeriodDryYear.setValue(consecutiveDaysDryYear.getValue());
            consecutiveDaysDryYear.setValue(0);
            lengthPeriodFlowingYear.setValue(consecutiveDaysFlowingYear.getValue());
            consecutiveDaysFlowingYear.setValue(0);
            consecutiveDaysPoolMonth.setValue(consecutiveDaysPoolMonth.getValue() + 1);
            lengthPeriodDryMonth.setValue(consecutiveDaysDryMonth.getValue());
            consecutiveDaysDryMonth.setValue(0);
            lengthPeriodFlowingMonth.setValue(consecutiveDaysFlowingMonth.getValue());
            consecutiveDaysFlowingMonth.setValue(0);
        }

        if (currentDayYear == lastDayYear) {
            if (lengthPeriodDryYear.getValue() == 0) {
                lengthPeriodDryYear.setValue(consecutiveDaysDryYear.getValue());
            }
            if (lengthPeriodPoolYear.getValue() == 0) {
                lengthPeriodPoolYear.setValue(consecutiveDaysPoolYear.getValue());
            }
            if (lengthPeriodFlowingYear.getValue() == 0) {
                lengthPeriodFlowingYear.setValue(consecutiveDaysFlowingYear.getValue());
            }
            consecutiveDaysDryYear.setValue(0);
            consecutiveDaysPoolYear.setValue(0);
            consecutiveDaysFlowingYear.setValue(0);
        }
        if (currentDayMonth == lastDayMonth) {
            if (lengthPeriodDryMonth.getValue() == 0) {
                lengthPeriodDryMonth.setValue(consecutiveDaysDryMonth.getValue());
            }
            if (lengthPeriodPoolMonth.getValue() == 0) {
                lengthPeriodPoolMonth.setValue(consecutiveDaysPoolMonth.getValue());
            }
            if (lengthPeriodFlowingMonth.getValue() == 0) {
                lengthPeriodFlowingMonth.setValue(consecutiveDaysFlowingMonth.getValue());
            }
            consecutiveDaysDryMonth.setValue(0);
            consecutiveDaysPoolMonth.setValue(0);
            consecutiveDaysFlowingMonth.setValue(0);
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
