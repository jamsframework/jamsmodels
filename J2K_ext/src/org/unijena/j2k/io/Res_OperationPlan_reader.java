/*
 * StandardLUReader.java
 * Created on 10. November 2005, 10:53
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
package org.unijena.j2k.io;

import org.unijena.j2k.*;
import jams.data.*;
import jams.model.*;
import java.util.*;
import jams.JAMS;
import jams.tools.FileTools;

/**
 *
 * @author M. Fink
 */
@JAMSComponentDescription(title = "Res_OperationPlan_reader",
        author = "Manfred Fink",
        description = "This component reads an ASCII file containing Pool-based "
        + "Operating Plan information and adds them to model entities.",
        date = "2025-05-20",
        version = "1.0_0")
public class Res_OperationPlan_reader extends JAMSComponent {

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Pool-based Operating Plan parameter file name"
    )
    public Attribute.String LamelleName;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "List of Reach objects"
    )
    public Attribute.EntityCollection reaches;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "ID of reach where the reservoir is located"
    )
    public Attribute.Double RID;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "initial storage in the reservoir [m³]",
            unit = "m³",
            defaultValue = "0.0")
    public Attribute.Double res_init;
    
           @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Initial RD1 storage proportion in the reservoir",
            unit = "-",
            defaultValue = "0.25"
    )
    public Attribute.Double res_init_porp_RD1;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Initial RD2 storage proportion in the reservoir",
            unit = "-",
            defaultValue = "0.25"
    )
    public Attribute.Double res_init_porp_RD2;
    
        @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Initial RG1 storage proportion in the reservoir",
            unit = "-",
            defaultValue = "0.25"
    )
    public Attribute.Double res_init_porp_RG1;
        
        @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Initial RG2 storage proportion in the reservoir",
            unit = "-",
            defaultValue = "0.25"
    )
    public Attribute.Double res_init_porp_RG2;
            
/*   @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "initial storage in the reservoir [l]",
            unit = "l",
            defaultValue = "0.0")
    public Attribute.Double res_storage;        
   
       @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "RD1 storage in the reservoir",
            unit = "L"
    )
    public Attribute.Double res_storage_RD1;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "RD2 storage in the reservoir",
            unit = "L"
    )
    public Attribute.Double res_storage_RD2;
    
        @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "RG1 storage in the reservoir",
            unit = "L"
    )
    public Attribute.Double res_storage_RG1;
        
        @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "RG2 storage in the reservoir",
            unit = "L"
    )
    public Attribute.Double res_storage_RG2;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "Number of Pools"
    )
    public Attribute.Integer numPools;
*/
    public void init() {
        //read lu parameter
        Attribute.EntityCollection PBOPS = getModel().getRuntime().getDataFactory().createEntityCollection();

        PBOPS.setEntities(J2KFunctions.readParas(FileTools.createAbsoluteFileName(getModel().getWorkspaceDirectory().getPath(), LamelleName.getValue()), getModel()));

        HashMap<Double, Attribute.Entity> PBOPSMap = new HashMap<Double, Attribute.Entity>();
        Attribute.Entity PBOP, e;
        Object[] attrs;

        //put all entities into a HashMap with their ID as key
        Iterator<Attribute.Entity> luIterator = PBOPS.getEntities().iterator();
        while (luIterator.hasNext()) {
            PBOP = luIterator.next();

            PBOPSMap.put(PBOP.getDouble("Pool"), PBOP);
        }

        Iterator<Attribute.Entity> reachIterator = reaches.getEntities().iterator();
        int nrPools = 0;
        while (reachIterator.hasNext()) {
            e = reachIterator.next();

            if (e.getDouble("ID") == RID.getValue()) {

                nrPools = PBOPSMap.size();

                int i = 0;
                while (i < nrPools) {
                    i = i + 1;
                    double j = i;
                    PBOP = PBOPSMap.get(j);
                    attrs = PBOP.getKeys();

                    for (int k = 0; k < attrs.length; k++) {
                        //e.setDouble((String) attrs[i], lu.getDouble((String) attrs[i]));
                        Object o = PBOP.getObject((String) attrs[k]);

                        e.setObject((String) attrs[k] + i, o);

                    }


                    e.setObject("attributes", attrs);

                    //Object nrPoolso = nrPools;
                    //e.setObject((String) "nrPools", nrPoolso);
                    e.setInt("nrPools", nrPools);
                    double res_storage =  res_init.getValue()*1000;                    
                    double propsum  = res_init_porp_RD1.getValue() + res_init_porp_RD2.getValue() + res_init_porp_RG1.getValue() +res_init_porp_RG2.getValue();
                    e.setDouble("res_storage", res_init.getValue()*1000);
                    e.setDouble("res_storage_RD1",res_storage * res_init_porp_RD1.getValue() /propsum);
                    e.setDouble("res_storage_RD2",res_storage * res_init_porp_RD2.getValue() /propsum);
                    e.setDouble("res_storage_RG1",res_storage * res_init_porp_RG1.getValue() /propsum);
                    e.setDouble("res_storage_RG2",res_storage * res_init_porp_RG2.getValue() /propsum);
                
                }
                
                //numPools.setValue(nrPools);
;

            }
        }
        getModel().getRuntime().println("Pool-based Operating Plan parameter file for RID: " + RID.getValue() +" processed", JAMS.VERBOSE);
    }

}
