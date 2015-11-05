/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package irrigation;

import jams.data.*;
import jams.model.*;
import java.util.Calendar;


@JAMSComponentDescription(
    title="VariableInit",
    author="Francois TILMANT",
    description="Initiate values if you are not in the irrigation period",
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
public class VariableInit extends JAMSComponent {
    
    
        @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "Added water for irrigation",
            unit = "l"
    )
    public Attribute.Double irrigationTotal;
        
        
            @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "Irrigation demand"
    )
    public Attribute.Double irrigationDemand;
            
            
            
             public void init() {

        }

    @Override
    public void run() {

        this.irrigationDemand.setValue(0);
        this.irrigationTotal.setValue(0);
       
        }

}
