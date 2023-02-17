package Reservoirs;

/*
 * Dam_ReleaseRule.java
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
//import java.util.Calendar;

/**
 *
 * @author Andrew Watson <awatson@sun.ac.za>
 */
@JAMSComponentDescription(
        title = "Dam_InflowTransferRule",
        author = "Andrew Watson",
        description = "A reservoir component used to release water into a downstream reach",
        date = "2022-09-06",
        version = "1.0_0"
)
@VersionComments(entries = {
    @VersionComments.Entry(version = "1.0_0", comment = "Initial version")
})
public class Dam_Outflow extends JAMSComponent {

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
            description = "Minimum release amount",
            defaultValue = "0",
            unit = "L",
            lowerBound = 0,
            upperBound = Double.POSITIVE_INFINITY
    )
    public Attribute.Double minRelease;
                
                        @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Maximum release amount",
            defaultValue = "0",
            unit = "L",
            lowerBound = 0,
            upperBound = Double.POSITIVE_INFINITY
    )
    public Attribute.Double maxRelease;
        
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Threshold till which release commences",
            defaultValue = "0",
            unit = "L",
            lowerBound = 0,
            upperBound = Double.POSITIVE_INFINITY
    )
    public Attribute.Double damReleaseAdapt;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "Volume of water release program based on rules",
            defaultValue = "0",
            unit = "L",
            lowerBound = 0,
            upperBound = Double.POSITIVE_INFINITY
    )
    public Attribute.Double releaseProgram;

    /*
     *  Component run stages
     */
    @Override
    public void initAll() {
            }
    
    @Override
    public void run() {
     
       double _damRelease;

        if (damStorage.getValue() > 0) {
            if (damStorage.getValue() <= ((damReleaseAdapt.getValue()
                    * damCapacity.getValue()))) {

                //remove water from the dam
                _damRelease = 0;
            }

            //now generate a timeseries of the release
        } else {
            _damRelease = 0;
        }
        _damRelease=(maxRelease.getValue() - minRelease.getValue())*(damStorage.getValue()/damCapacity.getValue());
        damStorage.setValue(damStorage.getValue() - _damRelease);
        
        releaseProgram.setValue(_damRelease);
        }
   
    @Override
    public void cleanup() {
    }
}
