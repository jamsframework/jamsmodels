/*
 * J2KProcessReachRouting.java
 * Created on 28. November 2005, 10:01
 *
 * This file is part of JAMS
 * Copyright (C) 2005 FSU Jena, c0krpe
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
package org.unijena.j2k.tools;

import jams.data.*;
import jams.model.*;

/**
 *
 * @author c8fima
 */
@JAMSComponentDescription(
        title = "AreaFraction_evaluator",
        author = "Manfred Fink",
        description = "Sum up the area that meet a defined condition ",
        version = "1.0",
        date = "2015-05-20"
)

public class AreaFraction_evaluator extends JAMSComponent {

    /*
     *  Component variables
     */
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Switch for area weight (0 - off; 1 - on) for the Value",
            unit = "-"
    )
    public Attribute.Boolean areaweight;   
    
        
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Entity parameter area",
            unit = "m^2"
    )
    public Attribute.Double area;    
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "evaluated value",
            unit = "-"
    )
    public Attribute.Double Value;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Theshhold",
            unit = "-"
    )
    public Attribute.Double Theshhold;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "1, greater than; 2, greater equval; 3, smaller than; 4 smaller equval; 5, equal",
            unit = "-"
    )
    public Attribute.Integer CType;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "Sumarized area which meet the condidion",
            unit = "m^2"
    )
    public Attribute.Double AreaSum;

    /*
     *  Component run stages
     */
    public void initall() {
        AreaSum.setValue(0.0);
    }

    public void run() {
        
        
        Double runvalue = 0.0;
        Double run_areasum = AreaSum.getValue();
        
        
        
        if (areaweight.getValue()){
            runvalue = Value.getValue()/area.getValue();
        }else{
            runvalue = Value.getValue();
        }
        

        if (CType.getValue() == 1) {       

                if (runvalue > Theshhold.getValue()) {
                    run_areasum = run_areasum + area.getValue();
                }
            }else if (CType.getValue() == 2){
           
                

                if (runvalue >= Theshhold.getValue()) {
                    run_areasum = run_areasum + area.getValue();
                }
            }else if (CType.getValue() == 3){

                if (runvalue < Theshhold.getValue()) {
                    run_areasum = run_areasum + area.getValue();
                }
            }else if (CType.getValue() == 4){

                if (runvalue <= Theshhold.getValue()) {
                    run_areasum = run_areasum + area.getValue();
                }
            }else if (CType.getValue() == 5){

                if (runvalue == Theshhold.getValue()) {
                    run_areasum = run_areasum + area.getValue();
                }
            }

        
        
        AreaSum.setValue(run_areasum);

    }

    public void cleanup() {

    }
}
