/*
 * newJAMSComponent.java
 * Created on 30. September 2008, 18:51
 *
 * This file is part of JAMS
 * Copyright (C) 2005 FSU Jena, c8fima
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
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
@JAMSComponentDescription(
        title = "Multiply values",
        author = "Manfred Fink",
        description = "Multiply values (run stage)"
)
public class value_multiplier extends JAMSComponent {

    /*
     *  Component variables
     */
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "product of the values"
    )
    public Attribute.Double product;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Factor or dividend"
    )
    public Attribute.Double factor1;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Factor or divisor"
    )
    public Attribute.Double factor2;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "aggregation mode: 1 = multiplication, 2 = division"
    )
    public Attribute.Integer mode;

    /*
     *  Component run stages
     */
    public void init() {

    }

    public void run() {
        double prod = 0;
            
            if (mode.getValue() == 1) {
                prod = factor1.getValue() * factor2.getValue();
            } else {
                if (factor2.getValue() > 0) {
                    prod = factor1.getValue() / factor2.getValue();
                } else {
                    prod = 0;
                }         

           

        }
             product.setValue(prod);
    }

    

    public void cleanup() {

    }
}
