/*
 * MODIS_ET_Reader.java
 * Created on 08.04.2020, 11:53:27
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
package glacier;

import jams.JAMS;
import jams.data.*;
import jams.model.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Calendar;

import java.util.Arrays;

/**
 *
 * @author Sven Kralisch <sven.kralisch@uni-jena.de>
 */
@JAMSComponentDescription(
        title = "GlacierFraction_Reader",
        author = "Sven Kralisch",
        description = "Read glacier fractions data for HRUs from TAB-separated file",
        date = "2020-04-08",
        version = "1.0_0"
)
@VersionComments(entries = {
    @VersionComments.Entry(version = "1.0_0", date = "2020-04-08", comment = "Initial version")
})
public class GlacierFraction_Reader extends JAMSComponent {

    /*
     *  Component attributes
     */
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "GlacierFraction input file name"
    )
    public Attribute.String fileName;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Current model time"
    )
    public Attribute.Calendar time;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "Glacier data of current time step"
    )
    public Attribute.DoubleArray glacierFractionArray;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "Array of HRU IDs from GlacierFraction file"
    )
    public Attribute.IntegerArray glacierFractionIDArray;

    transient BufferedReader reader;
    Attribute.Calendar date, startDate;

    /*
     *  Component run stages
     */
    @Override
    public void init() {

        // get the correct filename
//        DataFactory factory = getModel().getRuntime().getDataFactory();
        DataFactory factory = DefaultDataFactory.getDataFactory();
        date = factory.createCalendar();
        startDate = factory.createCalendar();

        File file;
        File f = new File(fileName.getValue());
        if (f.isAbsolute()) {
            file = new File(fileName.getValue());
        } else {
            file = new File(getModel().getWorkspacePath(), fileName.getValue());
        }

        try {

            // create reader object
            reader = new BufferedReader(new FileReader(file));
            String[] header = reader.readLine().split("\\s+");

            // read header and parse HRU IDs 
            int[] ids = new int[header.length - 1];
            for (int i = 1; i < header.length; i++) {
                ids[i - 1] = Integer.parseInt(header[i]);
            }
            glacierFractionIDArray.setValue(ids);

            // read first line, set start date and reset
            String start = reader.readLine().split("\\s+")[0];
            startDate.setValue(start);
            reader.close();
            reader = new BufferedReader(new FileReader(file));
            reader.readLine();

        } catch (IOException ex) {
            getModel().getRuntime().handle(ex);
        }
    }

    @Override
    public void run() {
        
        File file;
        File f = new File(fileName.getValue());
        if (f.isAbsolute()) {
            file = new File(fileName.getValue());
        } else {
            file = new File(getModel().getWorkspacePath(), fileName.getValue());
        }

        double[] fractions = new double[glacierFractionIDArray.getValue().length];
        
        getModel().getRuntime().println("time: " + time.getValue(), JAMS.VVERBOSE);
        
        if (!time.before(startDate)) {

            try {

                int timeOffset = 0;
                String[] s = null;
                
                // skip forward until file date is not smaller than model time
                do {
                
                    // read new line
                    String line = reader.readLine();
                    
                    // return 0-array if end of file is reached
                    if (line == null) {
                        glacierFractionArray.setValue(fractions);
                        return;
                    }
                    
                    // parse line and compare date with model time
                    s = line.split("\\s+");
                    date.setValue(s[0]);
                    timeOffset = date.compareTo(time, Calendar.DAY_OF_YEAR);
                    
                    getModel().getRuntime().println("s: " + s, JAMS.VVERBOSE);
                    getModel().getRuntime().println("date: " + date.getValue(), JAMS.VVERBOSE);
                    
                    
                } while (timeOffset < 0);

                // check if there is stil a date missmatch, i.e. model time not found in file
                if (timeOffset != 0) {
                    getModel().getRuntime().println("Date not found: " + time, JAMS.VERBOSE);
                }

                // parse and assign fractions
                for (int i = 1; i < s.length; i++) {
                    fractions[i - 1] = Double.parseDouble(s[i]);
                }

            } catch (IOException ex) {
                getModel().getRuntime().handle(ex);
            }
        }
        else{
            try {
                getModel().getRuntime().println("Using initial ice fraction", JAMS.VVERBOSE);
                // We use the initial ice fraction before the start date
                int timeOffset = 0;
                String[] s = null;  
                              
                // read the first line
                reader.close();
                reader = new BufferedReader(new FileReader(file));
                reader.readLine();
                String line = reader.readLine();
                
                getModel().getRuntime().println("line: " + line, JAMS.VVERBOSE);

                // parse line and compare date with model time
                s = line.split("\\s+");
                
                getModel().getRuntime().println("s: " + s, JAMS.VVERBOSE);
                date.setValue(s[0]);
                timeOffset = date.compareTo(time, Calendar.DAY_OF_YEAR);

                // parse and assign fractions
                for (int i = 1; i < s.length; i++) {
                    fractions[i - 1] = Double.parseDouble(s[i]);
                }

            } catch (IOException ex) {
                getModel().getRuntime().handle(ex);
            }
        }

        glacierFractionArray.setValue(fractions);
        
    }

    @Override
    public void cleanup() {
        try {
            reader.close();
        } catch (IOException ex) {
            getModel().getRuntime().handle(ex);
        }
    }

}
