/*
 * ED.java
 * Created on 13.08.2015, 16:17:09
 *
 * This file is part of JAMS
 * Copyright (C) FSU Jena
 *
 * JAMS is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * JAMS is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with JAMS. If not, see <http://www.gnu.org/licenses/>.
 *
 */
package forOptas;

import jams.data.*;
import jams.model.*;

/**
 *
 * @author Nico Hachgenei
 */
@JAMSComponentDescription(
        title = "",
        author = "Nico Hachgenei",
        description = "Calculation of Euclidian distance to one (or the defined optimum value) of a list of efficiencies",
        date = "2024-04-18",
        version = "1.0_0"
)
@VersionComments(entries = {
    @VersionComments.Entry(version = "1.0_0", comment = "Initial version")
})
public class ED extends JAMSComponent {

    /*
     *  Component attributes
     */
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "list of the efficiency values to take into account"
    )
    public Attribute.Double[] efficiencies;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "weights for the different efficiencies." +
            "default value 'null' applies a weight of 1 to each efficiency." +
            "otherwise needs to be of the same length as 'efficiencies'",
            defaultValue = "null"
    )
    public Attribute.Double[] weights;
    /**
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "write (unchanged) efficiency values (for export to optimizer context)"
    )
    public Attribute.Double[] efficiencies_out;
    **/
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "value of perfect fit (1 for KGE, 0 for KGE_normalized)"
    )
    public Attribute.Double optimum;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "calculated euclidean distance"
    )
    public Attribute.Double ED;
    /*
     *  Component run stages
     */

    @Override
    public void cleanup() {
        //efficiencies_out = efficiencies;
        
        if (weights != null) {
            if (weights.length != efficiencies.length) {
                throw new IllegalArgumentException("'weights' has to have the same length as 'efficiencies'");
            }
        }
        
        double pow = 2.0;
        double diffSquareSum = 0;
        double weight = 1; // by default equal weight of 1 for each component / subbassin
        
        //for (Attribute.Double efficiency : efficiencies) {
        for (int i = 0; i < efficiencies.length; i++) {
            Attribute.Double efficiency = efficiencies[i];
            if (weights != null){
                weight = weights[i].getValue();
            }
            
            diffSquareSum += weight * Math.pow(optimum.getValue() - efficiency.getValue(), pow);
        }
        
        double EDi;
        EDi = Math.sqrt(diffSquareSum);
        this.ED.setValue(EDi);
    }

}
