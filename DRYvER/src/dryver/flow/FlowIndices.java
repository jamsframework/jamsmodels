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

//    // debugging
//    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
//            description = "daily flow state")
//    public Attribute.Double ID;
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
            description = "number of consecutive days with flowing condition")
    public Attribute.Double consecutiveDaysFlowing;
    
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.WRITE,
            description = "number of consecutive days with pool condition")
    public Attribute.Double consecutiveDaysPool;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.WRITE,
            description = "number of consecutive days with non-dry condition")
    public Attribute.Double consecutiveDaysNoDry;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.WRITE,
            description = "number of consecutive days with non-flowing condition")
    public Attribute.Double consecutiveDaysNoFlowing;    

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.WRITE,
            description = "number of consecutive days with non-pool condition")
    public Attribute.Double consecutiveDaysNoPool;    

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.WRITE,
            description = "number of periods with non-dry condition")
    public Attribute.Double periodsNoDry;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.WRITE,
            description = "number of periods with non-flowing condition")
    public Attribute.Double periodsNoFlowing;    

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.WRITE,
            description = "number of periods with non-pool condition")
    public Attribute.Double periodsNoPool;    

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.WRITE,
            description = "number of consecutive days with dry condition within a year")
    public Attribute.Double consecutiveDaysDryYear;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.WRITE,
            description = "number of consecutive days with pool condition within a year")
    public Attribute.Double consecutiveDaysPoolYear;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.WRITE,
            description = "number of consecutive days with flowing condition within a year")
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
            description = "number of consecutive days with dry condition within a month")
    public Attribute.Double consecutiveDaysDryMonth;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.WRITE,
            description = "number of consecutive days with pool condition within a month")
    public Attribute.Double consecutiveDaysPoolMonth;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.WRITE,
            description = "number of consecutive days with flowing condition within a month")
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

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.WRITE,
            description = "start of a rewetting event")
    public Attribute.Double startFlowing;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.WRITE,
            description = "start of a drying event")
    public Attribute.Double startDrying;

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

//        // debugging
//        if (ID.getValue() == 4644) {
//            System.out.println("");
//            if (currentDayMonth == 1) {
//                System.out.println("");
//            }
//        }
        isDry.setValue(0);
        isPool.setValue(0);
        isFlowing.setValue(0);
        startDrying.setValue(0);
        startFlowing.setValue(0);
        lengthPeriodDryYear.setValue(0);
        lengthPeriodPoolYear.setValue(0);
        lengthPeriodFlowingYear.setValue(0);
        lengthPeriodDryMonth.setValue(0);
        lengthPeriodPoolMonth.setValue(0);
        lengthPeriodFlowingMonth.setValue(0);
        
        consecutiveDaysNoFlowing.setValue(0);
        consecutiveDaysNoDry.setValue(0);
        consecutiveDaysNoPool.setValue(0);

        // flowstate and consecutive days
        if (flowState.getValue() == 1) {
            isFlowing.setValue(1);
            consecutiveDaysFlowing.setValue(consecutiveDaysFlowing.getValue() + 1);
            
            if (consecutiveDaysFlowing.getValue() == 1) {
                consecutiveDaysNoFlowing.setValue(consecutiveDaysDry.getValue() + consecutiveDaysPool.getValue());
                if (consecutiveDaysNoFlowing.getValue() == 0) {
//                    consecutiveDaysNoFlowing.setValue(jams.JAMS.getMissingDataValue());
                } else {
                    periodsNoFlowing.setValue(periodsNoFlowing.getValue() + 1);
                }
            }
            
            consecutiveDaysDry.setValue(0);
            consecutiveDaysPool.setValue(0);
            
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
            isDry.setValue(1);
            consecutiveDaysDry.setValue(consecutiveDaysDry.getValue() + 1);

            if (consecutiveDaysDry.getValue() == 1) {
                consecutiveDaysNoDry.setValue(consecutiveDaysFlowing.getValue() + consecutiveDaysPool.getValue());
                if (consecutiveDaysNoDry.getValue() == 0) {
//                    consecutiveDaysNoDry.setValue(jams.JAMS.getMissingDataValue());
                } else {
                    periodsNoDry.setValue(periodsNoDry.getValue() + 1);
                }
            }

            consecutiveDaysFlowing.setValue(0);
            consecutiveDaysPool.setValue(0);
            
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
            isPool.setValue(1);
            consecutiveDaysPool.setValue(consecutiveDaysPool.getValue() + 1);

            if (consecutiveDaysPool.getValue() == 1) {
                consecutiveDaysNoPool.setValue(consecutiveDaysFlowing.getValue() + consecutiveDaysDry.getValue());
                if (consecutiveDaysNoPool.getValue() == 0) {
//                    consecutiveDaysNoPool.setValue(jams.JAMS.getMissingDataValue());
                } else {
                    periodsNoPool.setValue(periodsNoPool.getValue() + 1);                
                }
            }

            consecutiveDaysDry.setValue(0);
            consecutiveDaysFlowing.setValue(0);
            
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
        
        // start of event?
        if (consecutiveDaysFlowing.getValue() == 1) {
            startFlowing.setValue(1);
        }
        if (consecutiveDaysDry.getValue() == 1) {
            startDrying.setValue(1);
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

        if (lengthPeriodDryYear.getValue() == 0) {
            lengthPeriodDryYear.setValue(jams.JAMS.getMissingDataValue());
        }
        if (lengthPeriodPoolYear.getValue() == 0) {
            lengthPeriodPoolYear.setValue(jams.JAMS.getMissingDataValue());
        }
        if (lengthPeriodFlowingYear.getValue() == 0) {
            lengthPeriodFlowingYear.setValue(jams.JAMS.getMissingDataValue());
        }
        if (lengthPeriodDryMonth.getValue() == 0) {
            lengthPeriodDryMonth.setValue(jams.JAMS.getMissingDataValue());
        }
        if (lengthPeriodPoolMonth.getValue() == 0) {
            lengthPeriodPoolMonth.setValue(jams.JAMS.getMissingDataValue());
        }
        if (lengthPeriodFlowingMonth.getValue() == 0) {
            lengthPeriodFlowingMonth.setValue(jams.JAMS.getMissingDataValue());
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
