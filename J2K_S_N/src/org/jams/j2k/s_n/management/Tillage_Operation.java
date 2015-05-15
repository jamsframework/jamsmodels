/*
 * ManageLanduse.java
 *
 * Created on 16. März 2006, 13:20
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */
package org.jams.j2k.s_n.management;

import java.util.ArrayList;
import org.jams.j2k.s_n.crop.*;
import jams.model.*;
import jams.data.*;

/**
 *
 * @author c8fima
 */
@JAMSComponentDescription(
        title = "Tillage_Operation",
        author = "Manfred Fink",
        description = "Calculates redistribution of nutrient pools in soils in Soil. Method after SWAT2000 with adaptions",
        version = "1.0",
        date = "2015-05-06"
)

public class Tillage_Operation extends JAMSComponent {

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "Mixing efficiency",
            unit = "-",
            lowerBound = 0,
            upperBound = 1
    )
    public Attribute.Double Mixeff;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "Mixing depth",
            unit = "cm",
            lowerBound = 0,
            upperBound = 1
    )
    public Attribute.Double Mixdepth;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Depth of the single soil layers",
            unit = "cm",
            lowerBound = 0,
            upperBound = 2000
    )
    public Attribute.DoubleArray soillayerdepth;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "List of pools to mix"
    )
    public Attribute.DoubleArray[] Pool;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = " number of soil layers",
            unit = "-",
            lowerBound = 0,
            upperBound = 100
    )
    public Attribute.Double Layer;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = " daily mixing rates due to biological mixing",
            unit = "-",
            lowerBound = 0.00005,
            upperBound = 0.001,
            defaultValue = "0.0005 (derived from the default value of SWAT)"
    )
    public Attribute.Double BioMix;

   

    public void run() {
        double biomixdepth = 30.0; // depth of the bioturbation in cm (after SWAT)  
        int layernum = (int) Layer.getValue() + 1;
        double[] depth = new double[layernum];
        double[] depthsum = new double[layernum];
        int v = 0;
        int varnum = Pool.length;
        double[] restpool = new double[layernum];
        double[] newpool = new double[layernum];
        double[] partpool = new double[layernum];
        double[] mixpool = new double[layernum];
        double mixpoolsum = 0;
        double runMixdepth = Mixdepth.getValue();
        double runMixeff = Mixeff.getValue();
        
        
        //Daily bioturbation values
        if (runMixeff == 0){
            runMixeff = BioMix.getValue();
            runMixdepth = biomixdepth;
        }
        
        
        
        int i = 0;

        while (i < layernum) {
            if (i == 0) {
                depth[i] = 1;
                depthsum[i] = 1;
            } else if (i == 1) {
                depth[i] = this.soillayerdepth.getValue()[i - 1] - 1.0;
                depthsum[i] = depthsum[i - 1] + depth[i];
            } else {
                depth[i] = this.soillayerdepth.getValue()[i - 1];
                depthsum[i] = depthsum[i - 1] + depth[i];
            }
            i++;
        }
        i = 0;
        while (v > varnum) {
            double tillrest = runMixdepth;
            
            i = 0;
            double testinsum = 0;
            double testoutsum = 0;
            
            while (i < layernum) {
                mixpool[i] = Pool[v].getValue()[i] * runMixeff;
                restpool[i] = Pool[v].getValue()[i] * (1 - runMixeff);
                testinsum = testinsum + Pool[v].getValue()[i];
                tillrest = tillrest - depth[i];
                if (tillrest < 0) {
                    //calculation for completely mixed horizons
                    mixpoolsum = mixpoolsum + mixpool[i];
                    partpool[i] = depth[i] / runMixdepth;                    
                }else if (depthsum[i - 1] < runMixdepth){
                    //calculation for partly mixed horizons
                    double parthor = (depth[i] + tillrest)/depth[i];
                    double partsub = mixpool[i] * parthor;
                    mixpoolsum = mixpoolsum + partsub;
                    restpool[i] = restpool[i] + (mixpool[i] - partsub);
                    partpool[i] = (depth[i] + tillrest) / runMixdepth;
                }else{
                    //calculation for unmixed horizons
                    restpool[i] = mixpool[i];
                    partpool[i] = 0;                    
                }

                i++;
            }
            
            i = 0;
            
            while (i < layernum) {
                
                newpool[i] = (mixpoolsum * partpool[i]) + restpool[i];
                
                testoutsum = testoutsum +  newpool[i];
                
            }
            
            if (testoutsum != testinsum){
                double deriva = testoutsum - testinsum;
                getModel().getRuntime().println("Tillage calculation problem in pool balance, derivation: " +  deriva);
            } 
                        
            Pool[v].setValue(newpool);
            
            v++;
        }
        
        
        
    }

}
