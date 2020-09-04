/*
 * GlacierReducer.java
 * Created on 03.09.2020, 17:17:34
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
package org.unijena.j2k.snow;

import jams.data.*;
import jams.model.*;
import java.util.Iterator;
import java.util.List;

/**
 *
 * @author Sven Kralisch <sven.kralisch@uni-jena.de>
 */
@JAMSComponentDescription(
        title = "GlacierReducer",
        author = "Sven Kralisch",
        description = "Convert glacier entities to non-glacier entities, controlled "
        + "by given areal change",
        date = "2020-09-02",
        version = "1.0_0"
)
@VersionComments(entries = {
    @VersionComments.Entry(version = "1.0_0", comment = "Initial version")
})
public class GlacierReducer extends JAMSComponent {

    /*
     *  Component attributes
     */
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "List of glacier entities"
    )
    public Attribute.EntityCollection glacierEntities;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Initial glacier area",
            unit = "m²"
    )
    public Attribute.Double initialGlacierArea;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Glacier area change",
            unit = "%"
    )
    public Attribute.Double glacierAreaChange;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Area attribute in glacier entities"
    )
    public Attribute.String areaAttribute;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Entity's landuse attribute name",
            defaultValue = "landuseID"
    )
    public Attribute.String landuseAttribute;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "New land use ID of changed, non-glacier entities"
    )
    public Attribute.Double newLanduseID;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "Glacier area after reduction"
    )
    public Attribute.Double currentGlacierArea;    
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "List of entities that changed from glacier to non-glacier"
    )
    public Attribute.EntityCollection changedEntities;    
    
    /*
     *  Component run stages
     */

    double oldArea = -1;
    
    @Override
    public void run() {

        double targetGlacierArea = initialGlacierArea.getValue() * (glacierAreaChange.getValue() / 100);
        
        if (oldArea == -1) {
            oldArea = initialGlacierArea.getValue();
        } else {
            oldArea = currentGlacierArea.getValue();
        }
        getModel().getRuntime().println("Trying to reduce glacier area by " + (oldArea - targetGlacierArea) + " m²...");
        
        List<Attribute.Entity> list = glacierEntities.getEntities();
        double newArea = 0;
            
        int i;
        for (i = list.size()-1; i >= 0; i--) {
            Attribute.Entity e = list.get(i);
            double area = e.getDouble(areaAttribute.getValue());
            newArea += area;
            if (newArea > targetGlacierArea) {
                // check whether we are closer to the target area if we
                // remove the last (lowest) entity
                double delta1 = Math.abs(targetGlacierArea - newArea);
                double delta2 = Math.abs(targetGlacierArea - newArea + area);
                if (delta2 < delta1) {
                    i++;
                    newArea -= area;
                }
                break;
            }
        }
        
        List<Attribute.Entity> changedList = changedEntities.getEntities();
        changedList.clear();
        for (int j = 0; j < i; j++) {
            Attribute.Entity entity = list.remove(0);
            entity.setDouble(landuseAttribute.getValue(), newLanduseID.getValue());
            changedList.add(entity);
        }

        currentGlacierArea.setValue(newArea);
        getModel().getRuntime().println("Reduced glacier area by " + (oldArea - newArea) + " m² (" + changedList.size() + "/" + (list.size()+changedList.size()) + " entities)!");
        
    }

}
