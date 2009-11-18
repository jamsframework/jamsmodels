/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.unijena.j2k.io;

import java.io.IOException;
import jams.tools.JAMSTools;
import jams.data.*;
import jams.model.*;
import jams.io.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

/**
 *
 * @author c0krpe
 */
@JAMSComponentDescription(title = "StationDataWriter",
                          author = "Peter Krause",
                          description = "Writes standard ASCII timeseries data files")
public class StationDataWriter extends JAMSComponent{
    /*
     *  Component variables
     */
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "time interval"
            )
            public JAMSTimeInterval timeInterval;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
                        update = JAMSVarDescription.UpdateType.RUN,
                        description = "time")
    public JAMSCalendar time;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
                        update = JAMSVarDescription.UpdateType.RUN,
                        description = "the data values")
    public JAMSDoubleArray values;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
                        update = JAMSVarDescription.UpdateType.RUN,
                        description = "the header information")
    public JAMSStringArray headers;

    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.INIT,
            description = "Output data precision"
            )
            public JAMSInteger precision;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
                        update = JAMSVarDescription.UpdateType.INIT,
                        description = "Output file name")
    public JAMSString fileName;

    private GenericDataWriter writer;
    private DateFormat dateFormat;


    /*
     *  Component run stages
     */
    public void init() throws JAMSEntity.NoSuchAttributeException {
        writer = new GenericDataWriter(JAMSTools.CreateAbsoluteFileName(getModel().getWorkspace().getOutputDataDirectory().getPath(),fileName.getValue()));
        //create and write a header first
        int cols = this.headers.getValue().length + 1;
        String[] hdr = new String[cols];
        hdr[0] = "date";
        for(int i = 1; i < cols; i++)
            hdr[i] = this.headers.getValue()[i-1];
        this.headers.setValue(hdr);

        for (int i = 0; i < headers.getValue().length; i++) {
            writer.addColumn(headers.getValue()[i]);
        }

        writer.writeHeader();

        int tu = this.timeInterval.getTimeUnit();
        String timeFormat = "%1$tY-%1$tm-%1$td %1$tH:%1$tM";
        dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        //hourly values
        if(tu == 11) {
            timeFormat = "%1$td.%1$tm.%1$tY %1$tH:%1$tM";
            dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        //daily values
        } else if(tu == 6) {
            timeFormat = "%1$td.%1$tm.%1$tY";
            dateFormat = new SimpleDateFormat("dd.MM.yyyy");
        //monthly values
        } else if(tu == 2) {
            timeFormat = "%1$tm/%1$tY";
            dateFormat = new SimpleDateFormat("MM/yyyy");
        //annual values
        } else if(tu == 1) {
            timeFormat = "%1$tY";
            dateFormat = new SimpleDateFormat("yyyy");
        }
        dateFormat.setTimeZone(JAMSCalendar.STANDARD_TIME_ZONE);
    }

    public void run() throws JAMSEntity.NoSuchAttributeException {
        writer.addData(time.toString(dateFormat));
        for(int i = 0; i < values.getValue().length;i++){
            writer.addData(values.getValue()[i], precision.getValue());
        }
        try {
            writer.writeData();
        } catch (jams.runtime.RuntimeException jre) {
            getModel().getRuntime().println(jre.getMessage());
        }

    }

    public void cleanup() throws JAMSEntity.NoSuchAttributeException {
        try {
            writer.writer.flush();
            writer.writer.close();
        } catch (IOException ex) {
        }
    }
}
