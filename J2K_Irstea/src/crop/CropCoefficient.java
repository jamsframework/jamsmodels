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
            public JAMSDouble par_area;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,       // type of access, i.e. READ, WRITE, READWRITE
            description = "RefET",                       // description of purpose
            unit = "mm",                                     // unit of this var if numeric, defaults to ""
            lowerBound = 0,                                    // lowest allowed value of var if numeric, defaults to "0"
            upperBound = 1000                                 // highest allowed value of var if numeric, defaults to "0"        
            )
            public Attribute.Double par_ref_et;                // for a list of attribute types, see jams.data.Attribute  

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,       // type of access, i.e. READ, WRITE, READWRITE
            description = "CropCoeff",                       // description of purpose
            defaultValue = "1",                                // default value, defaults to "%NULL%"
            unit = "-",                                     // unit of this var if numeric, defaults to ""
            lowerBound = 0,                                    // lowest allowed value of var if numeric, defaults to "0"
            upperBound = 2                                 // highest allowed value of var if numeric, defaults to "0"        
            )
            public Attribute.Double par_crop_coeff;                // for a list of attribute types, see jams.data.Attribute  

     @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "PotET",
            unit = "L"
            )
            public Attribute.Double out_pot_et;
    
    /*
     *  Component run stages
     */
    
    @Override
    public void init() {
    }

    @Override
    public void run() {
        
        double run_ref_et = this.par_ref_et.getValue();
        double run_kc = this.par_crop_coeff.getValue();
        double run_area = this.par_area.getValue();
        double run_max_et = 0.0;
             
        // calculate run_max_et
        run_max_et = run_ref_et*run_kc;
        
        // convert run_max_et into Liters
        run_max_et = run_max_et*run_area;
        
        // return the calculated value        
        this.out_pot_et.setValue(run_max_et);
    }

    @Override
    public void cleanup() {
    }
}
