/*
 * newJAMSComponent.java
 * Created on 30. September 2008, 18:51
 *
 * This file is part of JAMS
 * Copyright (C) 2005 FSU Jena, c8fima
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
package org.jams.j2k.tools;

import jams.data.*;
import jams.model.*;

/**
 *
 * @author c8fima
 */
@JAMSComponentDescription(title = "Array_cracker1ß",
author = "Manfred Fink",
description = "split the frist 10 values of a JAMSDoubleArray into 10 JAMSDouble values")
public class Array_cracker10 extends JAMSComponent {

    /*
     * Component variables
     */
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    update = JAMSVarDescription.UpdateType.RUN,
    description = "Array to be splited")
    public JAMSDoubleArray Array;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.WRITE,
    update = JAMSVarDescription.UpdateType.RUN,
    description = "value 0")
    public JAMSDouble value0;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.WRITE,
    update = JAMSVarDescription.UpdateType.RUN,
    description = "value 1")
    public JAMSDouble value1;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.WRITE,
    update = JAMSVarDescription.UpdateType.RUN,
    description = "value 2")
    public JAMSDouble value2;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.WRITE,
    update = JAMSVarDescription.UpdateType.RUN,
    description = "value 3")
    public JAMSDouble value3;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.WRITE,
    update = JAMSVarDescription.UpdateType.RUN,
    description = "value 4")
    public JAMSDouble value4;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.WRITE,
    update = JAMSVarDescription.UpdateType.RUN,
    description = "value 5")
    public JAMSDouble value5;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.WRITE,
    update = JAMSVarDescription.UpdateType.RUN,
    description = "value 6")
    public JAMSDouble value6;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.WRITE,
    update = JAMSVarDescription.UpdateType.RUN,
    description = "value 7")
    public JAMSDouble value7;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.WRITE,
    update = JAMSVarDescription.UpdateType.RUN,
    description = "value 8")
    public JAMSDouble value8;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.WRITE,
    update = JAMSVarDescription.UpdateType.RUN,
    description = "value 9")
    public JAMSDouble value9;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.WRITE,
    update = JAMSVarDescription.UpdateType.RUN,
    description = "value 10")
    public JAMSDouble value10;

    /*
     * Component run stages
     */
    public void init() {
    }

    public void run() {

        int i = 0;

        double[] runarray = Array.getValue();
        int length = runarray.length;



        while (i < length) {
            switch (i) {
                case 0:
                    value0.setValue(Array.getValue()[i]);
                case 1:
                    value1.setValue(Array.getValue()[i]);
                case 2:
                    value2.setValue(Array.getValue()[i]);
                case 3:
                    value3.setValue(Array.getValue()[i]);
                case 4:
                    value4.setValue(Array.getValue()[i]);
                case 5:
                    value5.setValue(Array.getValue()[i]);
                case 6:
                    value6.setValue(Array.getValue()[i]);
                case 7:
                    value7.setValue(Array.getValue()[i]);
                case 8:
                    value8.setValue(Array.getValue()[i]);
                case 9:
                    value9.setValue(Array.getValue()[i]);
                case 10:
                    value10.setValue(Array.getValue()[i]);
            }
            i++;
        }


    }

    public void cleanup() {
    }
}
