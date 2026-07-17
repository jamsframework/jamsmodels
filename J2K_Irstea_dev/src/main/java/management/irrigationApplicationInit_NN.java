/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package management;

import jams.data.*;
import jams.model.*;


@JAMSComponentDescription(
    title="irrigationApplicationInit_np1",
    author="Nathan Pellerin",
    description="Initiate values",
    date = "2025-03-13",
    version = "1.0_0"
)
@VersionComments(entries = {
    @VersionComments.Entry(version = "1.0_0", comment = "Initial version")
})
/**
 *
 * @author tilmant
 */
public class irrigationApplicationInit_NN extends JAMSComponent {
            
            @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "stores the irrigation water delivered to HRU by the ground water reservoir (hru) for application (extraction"+
                    "minus losses due to efficiency). This attribute will be written to by this component."+
                    "parameter",
            unit = "L"
    )
    public Attribute.Double irrigationApplicationGW;
           
            @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "stores the irrigation water delivered to HRU by the farm dam for application (extraction"+
                    "minus losses due to efficiency). This attribute will be written to by this component."+
                    "parameter",
            unit = "L"
    )
    public Attribute.Double irrigationApplicationFarmdam;
            

    @Override
    public void run() {

        this.irrigationApplicationGW.setValue(0);
        this.irrigationApplicationFarmdam.setValue(0);
    }
}
