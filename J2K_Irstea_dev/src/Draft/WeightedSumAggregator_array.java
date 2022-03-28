package Draft;

/*
 * WeightedSumAggregator.java
 * Created on 22. Februar 2005, 15:01
 *
 * This file is part of JAMS
 * Copyright (C) 2005 S. Kralisch and P. Krause
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA
 *
 */
import jams.JAMS;
import jams.model.*;
import jams.data.*;
import java.util.List;
import java.util.Arrays;
import java.util.ArrayList;


/**
 *
 * @author S. Kralisch
 */
@JAMSComponentDescription(
        title = "WeightedSumAggregator",
        author = "Sven Kralisch",
        version = "1.0",        
        description = "Takes a list of double values as input and adds each "
        + "element to a corresponding element of an equally sized list of "
        + "output values. During aggrgation, the values are divided by weight. "
        + "Can be used to sum up any given number of double "
        + "values over a number of iteration steps."
)
@VersionComments(entries = {@VersionComments.Entry(
        version = "1.1",
        date = "2016-05-29",
        comment = "Added option to multiply with weight instead of dividing only."
)})
public class WeightedSumAggregator_array extends JAMSComponent {

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "The value(s) to be summed up"
    )
    public Attribute.DoubleArray value;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "The resulting weighted sum(s) of the given values"
    )
    public Attribute.DoubleArray sum;

    
    public void initAll() {

    }

    public void run() {
        
        double[] Tab = value.getValue();
        // nombre de brins qui contribuent au brin actuel
        int t = 0;
        for (int i = 0; i < Tab.length; i++) {
            if(Tab[i] != -999){
                t++;
            }            
        }
        
        // création d'un nouvel array si le nombre de brins qui contribue est !=0 
        if(t!=0){
            double[] new_Tab = new double[t];
            int j = 0;
            for(int i=0;i<Tab.length;i++){
                if(Tab[i] != -999){
                  new_Tab[j]= Tab[i];
                       j++;
                }
             }
            sum.setValue(new_Tab);

        }


    }

}
