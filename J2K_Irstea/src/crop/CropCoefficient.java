/**
 * CropCoeff
 * Component for calculation of Potential Evapotranspiration (MaxEt) according to
 * Penman Monteith RefET and a crop coeff
 * following the FAO method
 * Author = Flora Branger, 17 April 2012
 * 
 * 26-05-2021 : Modified by Louise Mimeau
 * adding conversion of etpot from mm to Liters 
 */

package crop;

import jams.data.*;
import jams.model.*;

/**
 *
 * @author F. Branger
 */
/**
 *
 * @author nsk
 */
 @JAMSComponentDescription(
        title="CropCoeff",
        author="Flora Branger, Louise Mimeau",
        description="CropCoeff"
         + "Component for calculation of Potential Evapotranspiration (MaxEt)"
         + "according to Penman Monteith RefET and a crop coeff"
         + "following the FAO method"
         + "+ conversion of PET from mm to L",
        date = "2012-04-17",
        version = "0.1_0"
        )

public class CropCoefficient extends JAMSComponent {

    /*
     *  Component attributes
     */
     
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "attribute area",
            unit="m^2"
            )
            public JAMSDouble area;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,       // type of access, i.e. READ, WRITE, READWRITE
            description = "RefET",                       // description of purpose
            unit = "mm",                                     // unit of this var if numeric, defaults to ""
            lowerBound = 0,                                    // lowest allowed value of var if numeric, defaults to "0"
            upperBound = 1000                                 // highest allowed value of var if numeric, defaults to "0"        
            )
            public Attribute.Double RefET;                // for a list of attribute types, see jams.data.Attribute  

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,       // type of access, i.e. READ, WRITE, READWRITE
            description = "CropCoeff",                       // description of purpose
            defaultValue = "1",                                // default value, defaults to "%NULL%"
            unit = "-",                                     // unit of this var if numeric, defaults to ""
            lowerBound = 0,                                    // lowest allowed value of var if numeric, defaults to "0"
            upperBound = 2                                 // highest allowed value of var if numeric, defaults to "0"        
            )
            public Attribute.Double CropCoeff;                // for a list of attribute types, see jams.data.Attribute  

     @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "PotET",
            unit = "L"
            )
            public Attribute.Double PotET;
    
    /*
     *  Component run stages
     */
    
    @Override
    public void init() {
    }

    @Override
    public void run() {
        
        double RefET = this.RefET.getValue();
        double kc = this.CropCoeff.getValue();
        double area = this.area.getValue();
        double MaxET = 0.0;
             
        // calculate MaxET
        MaxET = RefET*kc;
        
        // convert MaxET into Liters
        MaxET = MaxET*area;
        
        // return the calculated value        
        this.PotET.setValue(MaxET);
    }

    @Override
    public void cleanup() {
    }
}