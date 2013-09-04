package kirchner_methodology;

/**
 * JAMS example component - can be used as template for new components
 */

import jams.data.*;
import jams.model.*;

/**
 *
 * @author John Doe
 */
 @JAMSComponentDescription(
    title="Title",
    author="Author",
    description="Description",
    date = "YYYY-MM-DD",
    version = "1.0_0"
)
@VersionComments(entries = {
    @VersionComments.Entry(version = "1.0_0", comment = "Initial version"),
    @VersionComments.Entry(version = "1.0_1", comment = "Some improvements")
})        
public class GeoReader extends JAMSComponent {

    /*
     *  Component attributes
     */
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,       // type of access, i.e. READ, WRITE, READWRITE
            description = "Description"                       // description of purpose
                                                    // length of variable if string, defaults to "0"            
            )
            public Attribute.Double attribName;                // for a list of attribute types, see jams.data.Attribute  

    
    /*
     *  Component run stages
     */
    
    @Override
    public void init() {
    }

    @Override
    public void run() {
        
        getModel().getRuntime().println(" schists, granite, limestones");
        
    }

    @Override
    public void cleanup() {
    }
}