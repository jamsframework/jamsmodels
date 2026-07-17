/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jams.j2k.tools;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.StringTokenizer;
import jams.data.Attribute;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.List;
import java.util.SimpleTimeZone;
import java.util.TimeZone;
import java.util.stream.Collectors;

/**
 *Konvertierung von TLUBN-15Minutenrohwerten direkt vom Logger z.B. Geoergental2 
 * ins tabgetrente-Format. 
 * Ergebnis muss nnachträglich noch nach Datum sortiert werden
 *
 *
 *
 *
 *
 * @author Manfred Fink TFW
 */
public class TLUNB_Pegel_2_J2k_15min {

    BufferedWriter writer;

    //public final static TimeZone DEFAULT_TIME_ZONE = new SimpleTimeZone(0, "UTC");
    public void convert(String inputDirectory) throws IOException {
        TimeZone.setDefault(TimeZone.getTimeZone("Etc/GMT-1"));

        BufferedReader reader;
        BufferedReader reader2;

        StringTokenizer tokhum;
        StringTokenizer tokhum_init;
        StringTokenizer tokfile;
        writer = new BufferedWriter(new FileWriter(inputDirectory + "outputTab_join.dat"));

        try {

            List<File> timeseries = Files.walk(Paths.get(inputDirectory))
                    .filter(Files::isRegularFile)
                    .map(Path::toFile)
                    .collect(Collectors.toList());

            int fcount = timeseries.size();

            String namestr = "";
            String pathstr = "";
            String line = "";
            String path = "";
            String[] comparelist = new String[1000000];
            int n = 0;
            int d = 0;
            while (d < fcount) {
                File Act_file = timeseries.get(d);
                pathstr = Act_file.getAbsolutePath();
                namestr = Act_file.getName();
                path = Act_file.getParent();

                if (!namestr.startsWith("J2K") && namestr.endsWith(".MIS")) {

                    //metainformationen Schreiben
                    //int jj = 0;
                    int i = 0;

                    String last_linemeta = "";
                    String linehum2;
                    String linehum = "";

                    int f = 1;
                    int e = 0;

                    int ncolhum = 0;

                    String value, startdatestr;
                    String[] headerlines = new String[16];

                    //String strDateFormat = "dd.MM.yyyy HH:mm"; //Date format is Specified
                    String strDateFormat = "dd.MM.yyyy"; //Date format is Specified
                    String strTimeFormat = "HH:mm";
                    String strHourFormat = "HH";
                    SimpleDateFormat objSDF = new SimpleDateFormat(strDateFormat); //Date format string is passed as an argument to the Date format object
                    //objSDF.setTimeZone(TimeZone.getTimeZone("UTC"));

                    SimpleDateFormat objSDFTime = new SimpleDateFormat(strTimeFormat);
                    //objSDFTime.setTimeZone(TimeZone.getTimeZone("UTC"));

                    //SimpleDateFormat objSDFhour = new SimpleDateFormat(strHourFormat);
                    //objSDFhour.setTimeZone(TimeZone.getTimeZone("UTC"));
                    //day = null;
                    //mounth = null;
                    //year = null;
                    //hour = null;
                    //minute = null;
                    String datestr = null;
                    String timestr = null;

                    tokfile = new StringTokenizer(namestr, "\\");
                    int path_file_count = tokfile.countTokens();
                    String[] path_file = new String[path_file_count];

                    int k = 0;

                    while (tokfile.hasMoreTokens()) {
                        path_file[k] = tokfile.nextToken();
                        k++;
                    }
                    //Beispiel Filename: produkt_zehn_min_rr_20041019_20091231_05763.txt

                    String Filename = path_file[k - 1];

                    String Path = "";

                    int l = k - 2;

                    while (l >= 0) {
                        Path = path_file[l] + "\\" + Path;
                        l--;
                    }

                    String outputFileName = path + "\\" + "J2K" + namestr;

                    // lesen der zugehörigen Metadatei
                    reader = new BufferedReader(new FileReader(pathstr));
                    reader2 = new BufferedReader(new FileReader(pathstr));
                    int j = 0;
                    boolean point = true;
                    //zaelen der Zeilen und Spalten ermitteln des Startdatums
                    while ((linehum2 = reader.readLine()) != null && point) {

                        if (j == 1) {
                            linehum = linehum2;
                        }
                        if (!linehum2.contains("SENSOR>0020<")) {
                            j++;
                        } else {
                            point = false;
                        }

                    }

                    tokhum_init = new StringTokenizer(linehum, ";");
                    ncolhum = tokhum_init.countTokens();

                    String lineraw[] = new String[j + 2];

                    int ii = 0;

                    // schreiben in einen String Array
                    while ((lineraw[ii] = reader2.readLine()) != null && (ii < j + 1)) {
                        ii++;
                    }

                    int jjj = 0;

                    while (jjj < 1) {

                        headerlines[jjj] = lineraw[jjj];
                        jjj++;
                    }

                    //writer = new BufferedWriter(new FileWriter(outputFileName));
                    jjj = 0;

                    while (jjj < 1 && headerlines[jjj] != null && d == 0) {
                        writer.write(headerlines[jjj]);
                        writer.newLine();
                        jjj++;
                    }

                    String[] Headerhum = new String[ncolhum];

                    String[] hum_string = new String[ncolhum];
                    int tag_int_alt = 0;

                    int tag_int = 0;
                    //boolean flag = true;
                    double result_tag = 0.0;
                    double result_tag2 = 0.0;
                    double result_sum = 0;
                    double result_max = 0;
                    double result_min = 0;
                    double result2_sum = 0;

                    double param1 = 0;
                    double param2 = 0;

                    String result = "";
                    double result2 = 0;

                    int year_int = 0;
                    int month_int = 0;
                    int day_int = 0;
                    int hour_int = 0;
                    int min_int = 0;

                    if (!linehum.isEmpty()) {

                        startdatestr = linehum.substring(0, 19);

                        year_int = Integer.parseInt(startdatestr.substring(0, 4));
                        month_int = Integer.parseInt(startdatestr.substring(4, 6)) - 1;
                        day_int = Integer.parseInt(startdatestr.substring(6, 8));
                        hour_int = Integer.parseInt(startdatestr.substring(9, 11));
                        min_int = Integer.parseInt(startdatestr.substring(11, 13));
                    }
                    // für 1/4 Stündliche Zeitschritte
                    int increment = 900000;
                    long itterationen = 0;
                    Date date = new Date();
                    Date date2 = new Date();
                    Date date_itter = new Date();
                    Date date_tag = new Date();
                    Date date_itter_tag = new Date();
                    date.setYear(year_int - 1900);
                    date.setMonth(month_int);
                    date.setDate(day_int);
                    date.setHours(hour_int);
                    date.setMinutes(min_int);
                    date.setSeconds(0);
                    tag_int_alt = date.getDay();

                    date_tag.setTime(date.getTime());
                    date_tag.setHours(0);
                    date_tag.setMinutes(0);
                    date_tag.setSeconds(0);

                    while (f <= (j - 1)) {

                        tokhum = new StringTokenizer(lineraw[f], ";");

                        while (tokhum.hasMoreTokens()) {
                            value = tokhum.nextToken();
                            hum_string[e] = value;
                            e++;
                        }
                        result = hum_string[e - 1];
                        e = 0;

                        datestr = hum_string[0];

                        int Jahr = Integer.parseInt(datestr.substring(0, 4));
                        int Monat = Integer.parseInt(datestr.substring(4, 6)) - 1;
                        int Tag = Integer.parseInt(datestr.substring(6, 8));
                        timestr = hum_string[1];
                        int Stunde = Integer.parseInt(timestr.substring(0, 2));
                        int Minute = Integer.parseInt(timestr.substring(2, 4));
                        int Sekunde = Integer.parseInt(timestr.substring(4, 6));

                        if ((!(Minute == 30 || Minute == 45 || Minute == 0 || Minute == 15)) || Sekunde > 0) {
                            f++;
                        } else {

                            date2.setYear(Jahr - 1900);
                            date2.setMonth(Monat);
                            date2.setDate(Tag);
                            date2.setHours(Stunde);
                            date2.setMinutes(Minute);
                            date2.setSeconds(0);

                            i = 0;

                            itterationen = Math.round(((double) date2.getTime() - (double) date.getTime()) / (double) increment);

                            if (f > 1) {

                                while (i < itterationen) {
                                    //System.out.println("n = " + n + "   " + objSDF.format(date) + " " + objSDFTime.format(date));
                                    if (i == 0) {
                                        date_itter.setTime(date.getTime());
                                        boolean flag = true;
                                        int nn = 0;
                                        //avoid double date
                                        while (nn < n) {

                                            if (comparelist[nn].equals(objSDF.format(date) + " " + objSDFTime.format(date))) {
                                                flag = false;
                                            }
                                            nn++;
                                        }

                                        if (flag) {

                                            writer.write(objSDF.format(date) + " " + objSDFTime.format(date) + "\t" + result);
                                            writer.newLine();
                                            comparelist[n] = objSDF.format(date) + " " + objSDFTime.format(date);

                                            n++;
                                        }
                                        flag = true;

                                        //jj++;
                                        //result_sum = result_sum + result;
                                        //result_max = Math.max(result_max, result);
                                        //result_min = Math.min(result_min, result);
                                        //result2_sum = result2_sum + result2;
                                    } else {

                                        Double fill = -9999.0;

                                        date_itter.setTime(date_itter.getTime() + increment);

                                        boolean flag = true;
                                        int nn = 0;
                                        //avoid double date
                                        while (nn < n) {

                                            if (comparelist[nn].equals(objSDF.format(date) + " " + objSDFTime.format(date))) {
                                                flag = false;
                                            }
                                            nn++;
                                        }

                                        if (flag) {

                                            writer.write(objSDF.format(date_itter) + " " + objSDFTime.format(date_itter) + "\t" + Double.toString(fill));
                                            writer.newLine();
                                            comparelist[n] = objSDF.format(date) + " " + objSDFTime.format(date);
                                            n++;
                                        }
                                        flag = true;
                                    }
                                    i++;

                                }

                                date.setTime(date2.getTime());
                            }

                            if ((param1 == -999 || param1 == -999.0)) {
                                param1 = -9999.0;
                            }
                            if ((param2 == -999 || param2 == -999.0)) {
                                param2 = -9999.0;
                            }

                        }
                        f++;
                    }

                    date_itter.setTime(date_itter.getTime() + increment);
                    if (!result.isEmpty()) {

                        boolean flag = true;
                        int nn = 0;
                        //avoid double date
                        while (nn < n) {

                            if (comparelist[nn].equals(objSDF.format(date) + " " + objSDFTime.format(date))) {
                                flag = false;
                            }
                            nn++;
                        }

                        if (flag) {

                            writer.write(objSDF.format(date_itter) + " " + objSDFTime.format(date_itter) + "\t" + result);
                            writer.newLine();
                            comparelist[n] = objSDF.format(date) + " " + objSDFTime.format(date);
                            n++;
                        }
                        flag = true;

                    }

                    reader.close();

                }
                d++;
            }
            writer.close();
        } catch (IOException ee) {
            System.out.println("io fehler");
            ee.printStackTrace();
        }
    }

    public static void main(String[] args) throws IOException {
        TLUNB_Pegel_2_J2k_15min runoff = new TLUNB_Pegel_2_J2k_15min();

        runoff.convert("D:\\\\H_laufwerk\\\\MFi\\\\KleineAnfragen\\\\apfelstaedt\\\\TLUBN_georg2\\\\");
        //runoff.convert(args[0]); H:\Kleinspeicher\Heichelheim
    }
}
