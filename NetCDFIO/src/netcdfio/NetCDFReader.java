/*
 * NetCDFReader.java
 * Created on 17.01.2023, 22:04:20
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
package netcdfio;

import jams.data.*;
import jams.model.*;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import ucar.ma2.Array;
import ucar.ma2.InvalidRangeException;
import ucar.nc2.Dimension;
import ucar.nc2.NetcdfFile;
import ucar.nc2.NetcdfFiles;
import ucar.nc2.Variable;

/**
 *
 * @author Sven Kralisch <sven at kralisch.com>
 */
@JAMSComponentDescription(
        title = "NetCDFReader",
        author = "Sven Kralisch",
        description = "Reader for NetCDF files",
        date = "2023-01-17",
        version = "1.0_0"
)
@VersionComments(entries = {
    @VersionComments.Entry(version = "1.0_0", comment = "Initial version")
})
public class NetCDFReader extends JAMSComponent {

    /*
     *  Component attributes
     */
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Current time"
    )
    public Attribute.Calendar currentTime;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Current entity"
    )
    public Attribute.Entity currentEntity;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "NetCDF file name"
    )
    public Attribute.String fileName;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "name of temporal dimension",
            defaultValue = "date"
    )
    public Attribute.String timeDimName;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "name of spatial dimension",
            defaultValue = "reachID"
    )
    public Attribute.String spaceDimName;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Date from which time units are counted",
            defaultValue = "1950-01-01 00:00"
    )
    public Attribute.Calendar baseDate;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "names of variables to read"
    )
    public Attribute.String[] varNames;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "output attributes"
    )
    public Attribute.Double[] values;

    NetcdfFile ncfile;
    List<Variable> vars = new ArrayList();
    List<List<Dimension>> varDims = new ArrayList();
    Dimension time, space;
    int index1, index2;
    Map<Long, Integer> timeMap = new HashMap();
    Map<Long, Integer> spaceMap = new HashMap();

    /*
     *  Component run stages
     */
    @Override
    public void init() {
        try {
            ncfile = NetcdfFiles.open(fileName.getValue());

            time = ncfile.findDimension(timeDimName.getValue());
            space = ncfile.findDimension(spaceDimName.getValue());

            if (time == null || space == null) {
                List<Dimension> dimensions = ncfile.getDimensions();
                String error = "Please choose one of the following dimensions:";
                for (Dimension dimension : dimensions) {
                    error += "\nDimension: " + dimension;
                }
                getModel().getRuntime().sendHalt("Wrong dimension name. " + error);
                return;
            }

            Variable timeVar = ncfile.findVariable(time.getShortName());
            Array timeValues = timeVar.read();
            Variable spaceVar = ncfile.findVariable(space.getShortName());
            Array spaceValues = spaceVar.read();

            long baseMillis = baseDate.getTimeInMillis();
            for (int i = 0; i < timeValues.getSize(); i++) {
                long millis = Math.round(timeValues.getDouble(i) * 24 * 60 * 60 * 1000);
                timeMap.put(baseMillis + millis, i);
            }

            for (int i = 0; i < spaceValues.getSize(); i++) {
                System.out.println(spaceValues.getInt(i));
                spaceMap.put(spaceValues.getLong(i), i);
            }

            List<Variable> variables = ncfile.getVariables();
            for (Attribute.String varName : varNames) {
                Variable var = ncfile.findVariable(varName.getValue());
                if (var == null) {
                    String error = "Please choose one of the following variables:";
                    List<Variable> allVars = ncfile.getVariables();
                    for (Variable variable : allVars) {
                        error += "\nVariable: " + variable.getName();
                    }
                    getModel().getRuntime().sendHalt("Wrong variable name. " + error);

                    return;
                }

                vars.add(var);
//                List<Dimension> dims = var.getDimensions();
//                varDims.add(dims);
//
//                for (Dimension dim : dims) {
//                    System.out.println(dim);
//                }
//
//                System.out.println(var.getShape()[0]);
//                System.out.println(var.getShape()[1]);

            }
        } catch (FileNotFoundException ex) {
            getModel().getRuntime().sendHalt("Error reading NetCDF file " + fileName.getValue() + "\n" + ex);
        } catch (IOException ex) {
            getModel().getRuntime().sendHalt("Error reading NetCDF file " + fileName.getValue() + "\n" + ex);
        }
        System.out.println("----------------------------------------------");
    }

    long oldMillis = -1;
    int tIndex, sIndex;

    @Override
    public void run() throws IOException, InvalidRangeException {

        long millis = currentTime.getTimeInMillis();
        if (millis != oldMillis) {
            oldMillis = millis;
            tIndex = timeMap.get(currentTime.getTimeInMillis());
        }
        sIndex = spaceMap.get(currentEntity.getId());

        int[] origin = new int[]{sIndex, tIndex};
        int[] shape = new int[]{1, 1};

        for (int i = 0; i < vars.size(); i++) {
            Array dataArray = vars.get(i).read(origin, shape);
            double value = dataArray.getDouble(0);
//            System.out.println(value);
        }

//        System.out.println(tIndex + " - " + sIndex);
//        int timeIndex = timeIdx.get(time);
//        int entityIndex = entityIdx.get(id);
//
//        int[] origin = new int[]{index1, index2};
//        int[] shape = new int[]{1, 1};
//
//        for (Variable var : vars) {
//
//            int timeIndex = timeIdx.get(time);
//
//            int timeIndex = timeIdx.get(time);
//
//            var.readScalarFloat(new int[]{0, 1});
//        }
    }

    @Override
    public void cleanup() {
    }

}
