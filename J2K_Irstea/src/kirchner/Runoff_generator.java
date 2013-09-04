/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mypackage;

/**
 *
 * @author admin_adamovic
 */
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
public class Runoff_generator extends JAMSComponent {

    /*
     *  Component attributes
     */
     
     @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE          
            )
            public Attribute.Double discharge;                // for a list of attribute types, see jams.data.Attribute 
     
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,       // type of access, i.e. READ, WRITE, READWRITE
            description = "Description",                       // description of purpose
            defaultValue = "0.0505",                                // default value, defaults to "%NULL%"
            unit = "mm/h²",                                     // unit of this var if numeric, defaults to ""
            lowerBound = 0,                                    // lowest allowed value of var if numeric, defaults to "0"
            upperBound = 1000,                                 // highest allowed value of var if numeric, defaults to "0"
            length = 0                                         // length of variable if string, defaults to "0"            
            )
            public Attribute.Double initialValue;   
    
    /*
     *  Component run stages
     */
    // The number of steps to use in the interval
  public static final int STEPS = 1000; 
    @Override
    public void init() {
        
        discharge.setValue(initialValue.getValue());
        
    }
    
public static void main(String[] argv)
  {
    // `h' is the size of each step.
    double h = 1.0 ;
    double s1, s2, s3, s4;
    double x, y;
    int i;
    int dt=1;
    double bypass_fraction=0.0074;
    double qout
    // Computation by Euclid's method
    // Initialize y
    y = 0;

for (i=1; i<date.length; i++)
    

        s1 = qout(i)+dt/2*exp(c1+(c2-2)*qout(i)+c3*qout(i)*qout(i))*(((P(i)*(1-bypass_fraction))-(et_crop*potET(i)))-exp(qout(i)));
        s2 = qout(i)+dt/2*exp(c1+(c2-2)*s1(i)+c3*s1(n,1)*s1(n,1))*(((P(i)*(1-bypass_fraction))-(et_crop*potET(i)))-exp(s1(i)));
        s3= qout(i)+dt*exp(c1+(c2-2)*s2(i)+c3*s2(i)*s2(i))*(((P(i)*(1-bypass_fraction))-(et_crop*potET(i)))-exp(s2(i)));
        s4= qout(i)+dt*exp(c1+(c2-2)*s3(i)+c3*s3(i)*s3(i))*(((P(i)*(1-bypass_fraction))-(et_crop*potET(i)))-exp(s3(i)));
        s=(s1(i)/3)+(s2(i)*2/3)+(s3(i)/3)+(s4(i)/6)-qout(i)/2;

        qout(i++)=s(i);
       qoutsim(i)=((0.5*(exp(qout(i))+exp(qout(i++))))+(P(i)*(bypass_fraction)));
     
    }

 