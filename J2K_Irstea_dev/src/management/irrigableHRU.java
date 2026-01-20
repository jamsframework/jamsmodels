/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package management;

import jams.data.*;
import jams.model.*;


@JAMSComponentDescription(
    title="irrigableHRU",
    author="Nathan Pellerin",
    description="Create a new HRU attribute (irrigable), in order to filter "+
            "only irrigable HRU during some irrigation calculations",
    date = "2025-03-13",
    version = "1.0_0"
)
@VersionComments(entries = {
    @VersionComments.Entry(version = "1.0_0", comment = "Initial version")
})


public class irrigableHRU extends JAMSComponent {
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "landuseID",
            unit = "-"
    )
    public Attribute.Double landuseID;
            
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "Irrigable state of an HRU (0/1), parameter",
            unit = "-"
    )
    public Attribute.Double irrigable;
    
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READ,
    description = "Values of landuseID that make HRU irrigable"
    )
    public Attribute.String[] attributeValues;
            

    @Override
    public void run() {
        
        double run_landuseID = landuseID.getValue();
        double run_irrigable = 0;
        
        for (Attribute.String attr : attributeValues) {
            int v = Integer.parseInt(attr.getValue());
            if (run_landuseID == v) {
                run_irrigable = 1;
                getModel().getRuntime().println("Condition validée !");
                break;
            }
        }
        
        irrigable.setValue(run_irrigable);
        
        getModel().getRuntime().println("LanduseID = " + run_landuseID +
                " | irrigable = "+ run_irrigable);
    }
}
