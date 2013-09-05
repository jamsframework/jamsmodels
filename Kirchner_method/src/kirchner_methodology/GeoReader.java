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
            description = "Parameters distribution according to the geology, 1-schists;, 2-granites; 3-limestones"                       // description of purpose
                                                    // length of variable if string, defaults to "0"            
            )
            public Attribute.Double geology;                // for a list of attribute types, see jams.data.Attribute  

    
    public Attribute.Integer schists;
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Description")
    public Attribute.Integer granites;
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Description")
    
    public Attribute.Integer limestones;
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Description")
    
    /*
     *  Component run stages
     */
    
    @Override
    public void init() {
    }

    @Override
    public void run() {
        
        schists.setValue(1);
        granites.setValue(2);
        limestones.setValue(3);
    }

    @Override
    public void cleanup() {
    }
}