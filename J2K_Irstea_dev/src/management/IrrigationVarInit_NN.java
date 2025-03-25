/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package management;

import jams.data.*;
import jams.model.*;
import java.util.Calendar;


@JAMSComponentDescription(
    title="VariableInit",
    author="Francois TILMANT",
    description="Initiate values if you are not in the irrigation period // mod 1 by IG on 12-01-2016 : initialize st_plant_irrig_requirements also.",
    date = "2015-11-05",
    version = "1.0_0"
)
@VersionComments(entries = {
    @VersionComments.Entry(version = "1.0_0", comment = "Initial version")
})
/**
 *
 * @author tilmant
 */
public class IrrigationVarInit_NN extends JAMSComponent {
    
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE, 
            description = "Water actually applied for irrigation. Used to keep track of amount applied in current time step" +
                    "before overwriting irrigationApplication (by extraction component). - J2000 output",
            unit = "L"
    )
    public Attribute.Double irrigationApplicationOutput;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "Irrigation demand (amount of water to take = plantIrrigRequirements/efficiency) - output",
            unit = "L"
    )
    public Attribute.Double irrigationDemand;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "Real plant irrigation water requirement (= water deficit). - output",
            unit = "L"        
              )
    public Attribute.Double plantIrrigRequirements;
    
            
    @Override
    public void init() {

        }

    @Override
    public void run() {

        this.irrigationDemand.setValue(0);
        this.irrigationApplicationOutput.setValue(0);
	this.plantIrrigRequirements.setValue(0);
        
    }
}
