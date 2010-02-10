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
import java.util.Date;

/**
 *
 * @author c0krpe
 */
@JAMSComponentDescription(title = "StationDataWriter",
                          author = "Peter Krause",
                          description = "Writes standard ASCII timeseries data files")
public class StationDataWriter extends JAMSComponent{
    public static final String EMPTY_CHAR = "";
    public static final String SEPARATOR = "\t";
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
                        description = "the station names")
    public JAMSStringArray statNames;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
                        update = JAMSVarDescription.UpdateType.RUN,
                        description = "the station Ids")
    public JAMSDoubleArray statId;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
                        update = JAMSVarDescription.UpdateType.RUN,
                        description = "the station elevation")
    public JAMSDoubleArray statElev;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
                        update = JAMSVarDescription.UpdateType.RUN,
                        description = "the station x-coordinates")
    public JAMSDoubleArray statX;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
                        update = JAMSVarDescription.UpdateType.RUN,
                        description = "the station y-coordinates")
    public JAMSDoubleArray statY;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
                        update = JAMSVarDescription.UpdateType.INIT,
                        description = "data set description [type min max unit]")
    public JAMSString dataSetDesc;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.INIT,
            description = "temporal resolution",
            defaultValue=EMPTY_CHAR
            )
            public JAMSString tempRes;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.INIT,
            description = "missing data value"
            )
            public JAMSDouble missDataValue;

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

    /**
     * this attribute controls, whether an input header is written or only 1 simple header-line
     */
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.INIT,
            description = "write file with input header",
            defaultValue="true"
            )
            public JAMSString withInputHeader;

    private GenericDataWriter writer;
    private DateFormat dateFormat;


    /*
     *  Component run stages
     */
    public void init() throws JAMSEntity.NoSuchAttributeException {

        Date dt = new Date();
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

        writer = new GenericDataWriter(JAMSTools.CreateAbsoluteFileName(getModel().getWorkspace().getOutputDataDirectory().getPath(),fileName.getValue()));
        String inputHeader = withInputHeader.getValue();
        if (inputHeader != null && inputHeader.equalsIgnoreCase("true")) {

            //write station meta data as header
            writer.writeLine("#Calculated input data, generated: "+dt);
            writer.writeLine(JAMSVarDescription.MD_DATAVALUEATTRIBS);
            writer.writeLine(dataSetDesc.getValue());
            writer.writeLine(JAMSVarDescription.MD_DATASETATTRIBS);
            writer.writeLine(JAMSVarDescription.MD_MISSINGDATAVAL + SEPARATOR + missDataValue.getValue());
            writer.writeLine(JAMSVarDescription.MD_DATASTART + SEPARATOR + timeInterval.getStart().toString(dateFormat));
            writer.writeLine(JAMSVarDescription.MD_DATAEND + SEPARATOR + timeInterval.getEnd().toString(dateFormat));
            writer.writeLine(JAMSVarDescription.MD_TEMP_RES + SEPARATOR + tempRes);
            writer.writeLine(JAMSVarDescription.MD_STATATTRIBVAL);
            writer.addColumn("name");
            for (int i = 0; i < statNames.getValue().length; i++) {
                writer.addColumn(statNames.getValue()[i]);
            }
            writer.writeHeader();
            writer.write("ID");
            for(int i = 0; i < statId.getValue().length; i++){
                writer.write(SEPARATOR + statId.getValue()[i]);
            }
            writer.writeLine(EMPTY_CHAR);
            writer.write("elevation");
            for(int i = 0; i < statElev.getValue().length; i++){
                writer.write(SEPARATOR + statElev.getValue()[i]);
            }
            writer.writeLine(EMPTY_CHAR);
            writer.write("x");
            for(int i = 0; i < statX.getValue().length; i++){
                writer.write(SEPARATOR + statX.getValue()[i]);
            }
            writer.writeLine(EMPTY_CHAR);
            writer.write("y");
            for(int i = 0; i < statY.getValue().length; i++){
                writer.write(SEPARATOR + statY.getValue()[i]);
            }
            writer.writeLine(EMPTY_CHAR);
            writer.write("dataColumn");
            for(int i = 0; i < statX.getValue().length; i++){
                int col = i+1;
                writer.write(SEPARATOR + col);
            }
            writer.writeLine(EMPTY_CHAR);
            writer.writeLine(JAMSVarDescription.MD_DATAVAL);
        } else {

            //create and write a header from station names
            int cols = this.statNames.getValue().length + 1;
            String[] hdr = new String[cols];
            hdr[0] = "date";
            for(int i = 1; i < cols; i++)
                hdr[i] = this.statNames.getValue()[i-1];
            this.statNames.setValue(hdr);

            for (int i = 0; i < statNames.getValue().length; i++) {
                writer.addColumn(statNames.getValue()[i]);
            }
            writer.writeHeader();
        }
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
