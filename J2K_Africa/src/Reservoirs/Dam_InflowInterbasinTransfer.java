package Reservoirs;



/*
 * Dam_InflowTransferRule.java
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
import jams.data.*;
import jams.model.*;
import java.util.Calendar;

/**
 *
 * @author Andrew Watson <awatson@sun.ac.za>
 */
@JAMSComponentDescription(
        title = "Dam_InflowTransferRule",
        author = "Andrew Watson",
        description = "A reservoir component used to transfer water from another "
        + "reservoir to an exisiting reservoir in the model",
        date = "2022-09-06",
        version = "1.0_0"
)
@VersionComments(entries = {
    @VersionComments.Entry(version = "1.0_0", comment = "Initial version")
})
public class Dam_InflowInterbasinTransfer extends JAMSComponent {

    /*
     *  Component attributes
     */
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "The current model time")
    public Attribute.Calendar time;

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
            access = JAMSVarDescription.AccessType.READ,
            description = "Dam capacity",
            defaultValue = "0",
            unit = "L",
            lowerBound = 0,
            upperBound = Double.POSITIVE_INFINITY
    )
    public Attribute.Double damCapacity;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Water volume pumped to the reservoir"
            + "either as a single or as 12 (monthly) ",
            defaultValue = "0",
            unit = "L",
            lowerBound = 0,
            upperBound = Double.POSITIVE_INFINITY
    )
    public Attribute.DoubleArray transferDam;
    
        @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Threshold till which transfer commences",
            defaultValue = "0",
            unit = "L",
            lowerBound = 0,
            upperBound = Double.POSITIVE_INFINITY
    )
    public Attribute.Double damTransferAdapt;

    
        @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "Volume of water release program based on rules",
            defaultValue = "0",
            unit = "L",
            lowerBound = 0,
            upperBound = Double.POSITIVE_INFINITY
    )
    public Attribute.Double transferProgram;

    /*
     *  Component run stages
     */
    @Override
    public void initAll() {
        if (transferDam.getValue().length != 1 && transferDam.getValue().length != 12) {
            getModel().getRuntime().sendHalt("Number of abstraction values must be either 1 or 12!");
        }
    }

    @Override
    public void run() {

        double _transferDam;
                if (transferDam.getValue().length > 1) {
            int nowmonth = time.get(Calendar.MONTH);
            _transferDam = transferDam. getValue()[nowmonth];
        } else {
            _transferDam = transferDam.getValue()[0];
        }
        //check if there is a dam in the reach
        //and storage is above a threshold
        if (damStorage.getValue() > 0) {
            if (damStorage.getValue() >= ((damTransferAdapt.getValue()
                    * damCapacity.getValue()))){

                //remove water from the dam
                _transferDam = 0;
            }
    } else {
    _transferDam= 0;
}

            //now generate a timeseries of the release

        damStorage.setValue(damStorage.getValue() + _transferDam);

        transferProgram.setValue(_transferDam);
    }

    @Override
    public void cleanup() {
    }
}
