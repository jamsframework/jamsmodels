/*
 * IrrigationCounter.java
 * Created on 12.08.2015, 16:47:30
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
package management;

import jams.data.*;
import jams.model.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Sven Kralisch <sven.kralisch at uni-jena.de>
 */
@JAMSComponentDescription(
        title = "IrrigationCounter",
        author = "Nico Hachgenei",
        description = "Count HRUs in irrigation season, HRUs with irrigation demand and" +
                " HRUs with less water provided than required. Cumulate surfaces of each."+
                " Should be placed after irrigationApplication, to make sure everything"+
                " is initialized.",
        date = "2025-11-17",
        version = "1.0_0"
)
@VersionComments(entries = {
    @VersionComments.Entry(version = "1.0_0", comment = "Initial version")
})
public class IrrigationCounter extends JAMSComponent {

    /*
     *  Component attributes
     */
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Is the irrigation active for this HRU. - parameter"
    )
    public Attribute.Double irrigationActive;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "HRU area (get from HRULoop). - parameter",
            unit = "m²"
    )
    public Attribute.Double area;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Plant irrigation water requirement (= water deficit)"+
                    " of this HRU. - input",
            unit = "L"        
              )
    public Attribute.Double plantIrrigRequirements;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "Plant irrigation water requirement (= water deficit)"+
                    " of this HRU, FROM THE PREVIOUS TIME STEP, to compare to "+
                    "irrigationApplicationOutput. - output",
            unit = "L"        
              )
    public Attribute.Double previousPlantIrrigRequirements;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Volume of water for irrigation, delivered to this"+
                    " HRU. Should be irrigationApplicationOutput produced in"+
                    " IrrigationApplication context - input",
            unit = "L"        
              )
    public Attribute.Double irrigationApplication;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "Counter for number of HRUs with irrigation activated"+
                    " (= in irrigation season). - state variable"
    )
    public Attribute.Double HRUIrrigSeasonCounter;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "Cumulative area of HRUs with irrigation activated"+
                    " (= in irrigation season). - state variable"
    )
    public Attribute.Double HRUIrrigSeasonArea;
        
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "Counter for number of HRUs with irrigation demand"+
                    " >0. - state variable"
    )
    public Attribute.Double HRUIrrigDemandCounter;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "Cumulative area of HRUs with irrigation demand"+
                    " >0. - state variable"
    )
    public Attribute.Double HRUIrrigDemandArea;
        
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "Counter for number of HRUs with unsatisfied irrigation"+
                    " demand. - state variable"
    )
    public Attribute.Double HRUIrrigUnsatisfiedCounter;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "Cumulative area of HRUs with unsatisfied irrigation"+
                    " demand. - state variable"
    )
    public Attribute.Double HRUIrrigUnsatisfiedArea;
    
    /*
     *  Component run stages
     */

    @Override
    public void run() {
        
        double run_plantIrrigRequirements = plantIrrigRequirements.getValue();
        previousPlantIrrigRequirements.setValue(run_plantIrrigRequirements);

        if (irrigationActive.getValue() == 1) {// if we are in the irrigation season -> add to counters
            // read area, water requirements and delivery
            double run_area = area.getValue();
            double run_irrigationApplication = irrigationApplication.getValue();
            
            // add HRU to counters of activated irrigation
            HRUIrrigSeasonCounter.setValue(HRUIrrigSeasonCounter.getValue() + 1);
            HRUIrrigSeasonArea.setValue(HRUIrrigSeasonArea.getValue() + run_area);
            
            if (run_plantIrrigRequirements > 0) { // there is a demand for irrigation of this HRU
                // add HRU to counters of irrigation demand
                HRUIrrigDemandCounter.setValue(HRUIrrigDemandCounter.getValue() + 1);
                HRUIrrigDemandArea.setValue(HRUIrrigDemandArea.getValue() + run_area);
                
                if (run_plantIrrigRequirements > run_irrigationApplication) { // the demand is bigger than the delivered quantity
                    // add HRU to counters of unsatisfied irrigation demand
                    HRUIrrigUnsatisfiedCounter.setValue(HRUIrrigUnsatisfiedCounter.getValue() + 1);
                    HRUIrrigUnsatisfiedArea.setValue(HRUIrrigUnsatisfiedArea.getValue() + run_area);
                    
                }
            }
        }
    }
}
