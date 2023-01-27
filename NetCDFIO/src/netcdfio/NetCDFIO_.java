/*
 * Copyright (C) 2023 Sven Kralisch <sven at kralisch.com>.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA 02110-1301  USA
 */
package netcdfio;

import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import ucar.ma2.Array;
import ucar.nc2.Dimension;
import ucar.nc2.NetcdfFile;
import ucar.nc2.NetcdfFiles;
import ucar.nc2.Variable;

/**
 *
 * @author Sven Kralisch <sven at kralisch.com>
 */
public class NetCDFIO_ {

    String path;
    String varName;

    private void openNCDF() {

        try {
            NetcdfFile ncfile = NetcdfFiles.open(path);

            // Get the list of variables
            List<Variable> variables = ncfile.getVariables();

            // Iterate through the variables
            for (Variable variable : variables) {
                System.out.println("Variable: " + variable.getName());
                System.out.println("Variable: " + variable.getDescription() + " [" + variable.getUnitsString() + "]");
                for (Dimension d : variable.getDimensions()) {
                    System.out.println(d.getLength());
                }
                // Read the data
//                Array data = variable.read();
//                System.out.println("Data: " + data);
            }

            Variable v = ncfile.findVariable(varName);
            Array data = v.read();
            System.out.println(data);
            int n = data.getShape()[0];
            System.out.println(n);

//            Array data = v.read(sectionSpec);
//            String arrayStr = Ncdump.printArray(data, varName, null);
        } catch (IOException ex) {
            Logger.getLogger(NetCDFIO_.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    public static void main(String[] args) {

        NetCDFIO_ io = new NetCDFIO_();
        io.path = "D:\\jamsmodeldata\\Europa\\DRYvER\\netcdf\\Albarine_2022-12-16.nc";
        io.varName = "baseflow";

        io.openNCDF();

    }

}
