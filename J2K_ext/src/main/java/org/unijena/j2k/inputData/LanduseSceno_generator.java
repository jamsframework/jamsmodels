/*
 * CalcLanduseStateVars.java
 * Created on 23. November 2005, 13:48
 *
 * This file is part of JAMS
 * Copyright (C) 2005 FSU Jena, c0krpe
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
package org.unijena.j2k.inputData;

import jams.data.*;
import jams.model.*;
import java.util.Iterator;

/**
 *
 * @author c8fima
 */
@JAMSComponentDescription(
        title = "LanduseSceno_generator",
        author = "Manfred Fink",
        description = "allows the redistributon of land use ID according to a proportion variable",
        version = "1.0_0",
        date = "2017-03-16"
)
public class LanduseSceno_generator extends JAMSComponent {

    /*
     *  Component variables
     */
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "List of hru objects"
    )
    public Attribute.EntityCollection hrus;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "The current spatial entity"
    )
    public Attribute.EntityCollection entities;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Proportion of settlement (rual) between 0 and 100%",
            defaultValue = "1.5",
            lowerBound = 0,
            upperBound = 100
    )
    public Attribute.Double class1prop;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Proportion of arable land between 0 and 100%",
            defaultValue = "63",
            lowerBound = 0,
            upperBound = 100
    )
    public Attribute.Double class2prop;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Proportion of grass land between 0 and 100%",
            defaultValue = "12",
            lowerBound = 0,
            upperBound = 100
    )
    public Attribute.Double class3prop;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Proportion of deciduos forrest between 0 and 100%",
            defaultValue = "5",
            lowerBound = 0,
            upperBound = 100
    )
    public Attribute.Double class4prop;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Proportion of coinferous forrest between 0 and 100%",
            defaultValue = "18",
            lowerBound = 0,
            upperBound = 100
    )
    public Attribute.Double class5prop;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Proportion of settlement (urban) between 0 and 100%",
            defaultValue = "0.5",
            lowerBound = 0,
            upperBound = 100
    )
    public Attribute.Double class6prop;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "List of Landuse IDs (ordered)",
            defaultValue = "1,3,9,5,6,2"
    )
    public Attribute.IntegerArray Land_use_IDs;

    int[] IDs = {1, 3, 9, 5, 6, 2};
    double[] default_LUIDvals = {0, 0.0156, 0.658, 0.7644, 0.814, 0.995, 1};

    /*
     *  Component run stages
     */
    public void init() {
        Attribute.Entity lu, e;
        int j = 0;
        double class1prop = this.class1prop.getValue();
        double class2prop = this.class2prop.getValue();
        double class3prop = this.class3prop.getValue();
        double class4prop = this.class4prop.getValue();
        double class5prop = this.class5prop.getValue();
        double class6prop = this.class6prop.getValue();

        double[] props_percent = {class1prop, class2prop, class3prop, class4prop, class5prop, class6prop};

        double sumprop = class1prop + class2prop + class3prop + class4prop + class5prop + class6prop;

        double tempprop = 0;

        while (j <= props_percent.length) {

            if (j == 0) {
                this.default_LUIDvals[j] = 0.0;
            } else if (j < props_percent.length) {
                tempprop = tempprop + props_percent[j - 1];
                //normalisation to 1                
                this.default_LUIDvals[j] = tempprop / sumprop;
            } else {
                this.default_LUIDvals[j] = 1.0;
            }
            j++;
        }

        IDs = Land_use_IDs.getValue();

        

        int count = IDs.length;

        Iterator<Attribute.Entity> hruIterator = hrus.getEntities().iterator();
        while (hruIterator.hasNext()) {

            e = hruIterator.next();
            
            int i = 0;
            
            double newlandID = 0;
            double proportion = e.getDouble("proportion");

            while (i < count) {

                if (proportion > this.default_LUIDvals[i] && proportion <= this.default_LUIDvals[i + 1]) {
                    newlandID = this.IDs[i];
                }
                i++;
            }

        //System.out.println("vorher " + entity.getDouble("LID"));
            e.setDouble("landuseID", newlandID);

        //System.out.println("nachher " + entity.getDouble("LID"));
        }
    }

    public void run() {

    }

    public void cleanup() {

    }

}
