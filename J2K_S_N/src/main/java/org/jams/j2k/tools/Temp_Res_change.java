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
import java.util.Date;
import java.text.SimpleDateFormat;
import java.util.TimeZone;

/**
 * Konvertierung von verschiedenen zeitlichen Auflösungen (von Minuten bis zu 1
 * Tag) vollständige Zeitreihen in den Datums und Zeitspalten notwendig.
 *
 *
 * @author Manfred Fink TFW
 */
public class Temp_Res_change {

    String strDateFormat = "dd.MM.yyyy"; //Date format is Specified
    String strTimeFormat = "HH:mm";
    SimpleDateFormat objSDF = new SimpleDateFormat(strDateFormat); //Date format string is passed as an argument to the Date format object
    SimpleDateFormat objSDFTime = new SimpleDateFormat(strTimeFormat);
    BufferedWriter writer;
    boolean[] flag;
    int dl_sav_cont = 8;

    public void convert(String Consoleinput) throws IOException {
        //public void convert(String inputfile) throws IOException {

        TimeZone new_time_default = TimeZone.getTimeZone("Etc/GMT-1");
        //TimeZone.getDefault();
        //new_time_default.useDaylightTime();
        //new_time_default.getDSTSavings();

        //TimeZone.setDefault(TimeZone.getTimeZone("Etc/GMT-1"));
        TimeZone.setDefault(new_time_default);
        //TimeZone.getDefault();
        this.objSDF = new SimpleDateFormat(strDateFormat); //Date format string is passed as an argument to the Date format object
        this.objSDFTime = new SimpleDateFormat(strTimeFormat);

        boolean flag1 = false;
        //int jj = 0;

        //int i = 0;
        BufferedReader reader;
        BufferedReader reader2;
        boolean wrote = false;

        String linehum2;

        int f = 0;
        int e = 0;

        StringTokenizer tokcons;
        StringTokenizer tokval;

        StringTokenizer tokfile;

        String initstr1, initstr2;

        //String strDateFormat = "dd.MM.yyyy HH:mm"; //Date format is Specified
        //day = null;
        //mounth = null;
        //year = null;
        //hour = null;
        //minute = null;
        tokcons = new StringTokenizer(Consoleinput);

        int ll = 0;

        String[] inputArray = new String[4];

        while (tokcons.hasMoreTokens()) {
            inputArray[ll] = tokcons.nextToken();
            ll++;
        }
        if (ll != 3) {
            System.out.println("Falsche anzahl von Parametern: Usage: Inputfilename, resolution in sec, number of header lines, average (0) or sum");
        }

        tokfile = new StringTokenizer(inputArray[0], "\\");
        //int inres_sec = Integer.parseInt(inputArray[1]) * 1000;
        int output_res = Integer.parseInt(inputArray[1]) * 1000;
        int header_count = Integer.parseInt(inputArray[2]);
        int avg_sum = Integer.parseInt(inputArray[3]);

        String[] headerlines = new String[header_count];

        //double numdoub = 1 / ratio;
        //long rest = 0;

        /*
        while (numdoub > 1){        
            numres++; 
            numdoub = numdoub -1;
        }
        double rest = numdoub;
        if (rest > 0){
             numres++; 
        }
        
         */
        int path_file_count = tokfile.countTokens();
        String[] path_file = new String[path_file_count];

        int k = 0;

        while (tokfile.hasMoreTokens()) {
            path_file[k] = tokfile.nextToken();
            k++;
        }
        //Beispiel Filename: produkt_td_stunde_19961018_20070630_05419.txt

        String file_name = path_file[k - 1];

        String Path = "";

        int l = k - 2;

        while (l >= 0) {
            Path = path_file[l] + "\\" + Path;
            l--;
        }

        String outputFileName = Path + "\\" + inputArray[3] + file_name;
        try {
            reader = new BufferedReader(new FileReader(inputArray[0]));
            reader2 = new BufferedReader(new FileReader(inputArray[0]));
            int j = 0;

            Date startdate = new Date();
            Date startdate2 = new Date();

            //zaelen der Zeilen und Spalten ermitteln des Startdatums und der input Aufloesung
            while ((linehum2 = reader.readLine()) != null) {

                if (j < header_count) {
                    headerlines[j] = linehum2;
                } else if (j == header_count) {
                    initstr1 = linehum2;
                    StringTokenizer tokinit1;
                    tokinit1 = new StringTokenizer(initstr1);
                    String datum1 = tokinit1.nextToken();
                    String zeit1 = tokinit1.nextToken();
                    if (zeit1.equals("24:00")) {
                        zeit1 = "00:00";
                    }
                    startdate = dateparser(datum1, zeit1);

                } else if (j == header_count + 1) {
                    initstr2 = linehum2;
                    StringTokenizer tokinit2;
                    tokinit2 = new StringTokenizer(initstr2);
                    String datum2 = tokinit2.nextToken();
                    String zeit2 = tokinit2.nextToken();
                    if (zeit2.equals("24:00")) {
                        zeit2 = "00:00";
                    }
                    startdate2 = dateparser(datum2, zeit2);
                }

                j++;
            }

            long input_res = startdate2.getTime() - startdate.getTime();

            double ratio = (double) (input_res) / (double) (output_res);

            String lineraw[] = new String[j + 1];

            int ii = 0;

            // schreiben in einen String Array
            while ((lineraw[ii] = reader2.readLine()) != null) {
                ii++;
            }

            writer = new BufferedWriter(new FileWriter(outputFileName));

            int v = 0;

            Date dateinput = new Date();
            Date dateoutput = startdate;

            int ih = 0;
            while (ih < header_count) {
                writer.write(headerlines[ih]);
                writer.newLine();
                ih++;
            }

            f = header_count;

            tokval = new StringTokenizer(lineraw[f], "\t");
            int colunms = tokval.countTokens();
            String[] valstring = new String[colunms];

            double[] num_value = new double[colunms - 2];

            double[] num_sum = new double[colunms - 2];
            this.flag = new boolean[colunms - 2];
            double weightlores = 0; //Gewicht im Verhaeltnis zum Inputzeitschritt
            double weighthires = 0; //Gewicht im Verhaeltnis zum Inputzeitschritt
            double weightsum = 0;
            long inpsec = 0;
            long oldsec = 0;
            long outsec = 0;
            long inres_sec = 0;
            long outres_sec = 0;

            inres_sec = input_res / 1000;
            outres_sec = output_res / 1000;

            int ressteps = (int) (1 / ratio);
            int n = 0;

            while (f < j) {

                //System.out.println("f: " + Integer.toString(f));
                tokval = new StringTokenizer(lineraw[f], "\t");
                e = 0;
                while (tokval.hasMoreTokens()) {
                    if (e == 1) {
                        String Uhrzeit = tokval.nextToken();
                        if (Uhrzeit.equals("24:00")) {
                            Uhrzeit = "00:00";
                        }
                        valstring[e] = Uhrzeit;
                    } else {
                        valstring[e] = tokval.nextToken();
                    }
                    e++;
                }

                oldsec = dateinput.getTime() / 1000;
                dateinput = dateparser(valstring[0], valstring[1]);
                inpsec = dateinput.getTime() / 1000;
                outsec = dateoutput.getTime() / 1000;
                //Daylight saving spring check for cadenza output only used by 15 min data for gauging stations
                if (oldsec == inpsec - (3600 + input_res / 1000)) {
                    inpsec = inpsec - 3600;
                    dl_sav_cont = 0;

                } else if (dl_sav_cont < 6) {

                    switch (dl_sav_cont) {
                        case 0:
                            inpsec = inpsec - 3 * (input_res / 1000);
                            break;
                        case 1:
                            inpsec = inpsec - 3 * (input_res / 1000);
                            break;
                        case 2:
                            inpsec = inpsec - 2 * (input_res / 1000);
                            break;
                        case 3:
                            inpsec = inpsec - 2 * (input_res / 1000);
                            break;
                        case 4:
                            inpsec = inpsec - 1 * (input_res / 1000);
                            break;
                        case 5:
                            inpsec = inpsec - 1 * (input_res / 1000);
                            break;
                    }
                    dl_sav_cont++;
                } else {

                }

                if (ratio <= 1) { //resolution decreases

                    //long diff = outsec - inpsec;
                    //System.out.println("differenz in out in sec: " + (diff));
                    if ((inpsec < outsec) && (inpsec + inres_sec > outsec) && (inpsec + inres_sec < outsec + outres_sec)) {
                        weightlores = (((double) inpsec + (double) inres_sec) - (double) outsec) / (double) inres_sec;
                        weightsum = weightlores;

                        v = 0;

                        while (v < colunms - 2) {
                            num_value[v] = Double.parseDouble(valstring[v + 2]);

                            if (num_value[v] == -9999 || e < colunms) {
                                this.flag[v] = true;
                            }
                            num_sum[v] = num_value[v] * weightlores;

                            v++;
                        }

                        wrote = false;

                    } else if ((inpsec >= outsec) && (inpsec + inres_sec > outsec) && (inpsec + inres_sec <= outsec + outres_sec)) {

                        v = 0;

                        while (v < colunms - 2) {
                            num_value[v] = Double.parseDouble(valstring[v + 2]);

                            if (num_value[v] == -9999 || e < colunms) {
                                n++;
                                if (n >= ressteps - 1) {
                                    this.flag[v] = true;
                                    n = 0;
                                }
                            } else {
                                n = 0;
                            }
                            num_sum[v] = num_sum[v] + num_value[v];
                            v++;
                        }

                        weightsum = weightsum + 1;

                        wrote = false;

                        if (inpsec + inres_sec == outsec + outres_sec) {

                            wrote = linewriter(dateoutput, num_sum, weightsum, avg_sum);
                            dateoutput.setTime(dateoutput.getTime() + output_res);

                            int iii = 0;
                            while (iii < num_sum.length) {
                                num_sum[iii] = 0;
                                iii++;
                            }
                            weightsum = 0;

                        }

                    } else if ((inpsec < outsec + outres_sec) && (inpsec + inres_sec > outsec) && (inpsec + inres_sec > outsec + outres_sec) && !wrote) {
                        weightlores = (((double) outsec + (double) outres_sec) - ((double) inpsec)) / (double) inres_sec;

                        v = 0;

                        while (v < colunms - 2) {

                            num_value[v] = Double.parseDouble(valstring[v + 2]);
                            if (num_value[v] == -9999 || e < colunms) {
                                n++;
                                if (n >= ressteps - 1) {
                                    this.flag[v] = true;
                                    n = 0;
                                }
                            } else {
                                n = 0;
                            }

                            num_sum[v] = num_sum[v] + (num_value[v] * weightlores);
                            v++;
                        }

                        weightsum = weightsum + weightlores;

                        wrote = linewriter(dateoutput, num_sum, weightsum, avg_sum);
                        dateoutput.setTime(dateoutput.getTime() + output_res);

                        int iii = 0;
                        while (iii < num_sum.length) {
                            num_sum[iii] = 0;
                            iii++;
                        }
                        weightsum = 0;

                        wrote = false;
                        f--;

                    }

                } else { //resolution increases
                    long diff = outsec - inpsec;
                    //System.out.println("differenz in out in sec: " + diff);

                    if (flag1) { //Fall 1, Anfang der Reihe
                        //if ((inpsec + inres_sec < outsec) && (inpsec + inres_sec > outsec - outres_sec))

                        double weight = ((double) outres_sec / (double) inres_sec) - weighthires;

                        v = 0;

                        while (v < colunms - 2) {
                            num_value[v] = Double.parseDouble(valstring[v + 2]);

                            if (num_value[v] == -9999 || e < colunms) {
                                this.flag[v] = true;
                            }
                            num_sum[v] = num_sum[v] + (num_value[v] * weight);

                            v++;
                        }

                        wrote = linewriter(dateoutput, num_sum, weight, avg_sum);
                        //System.out.println("Geschrieben Fall 1 + 3, dateoutput, num_sum, weightsum, avg_sum: " + ", " + dateoutput + ", " + num_sum + ", " + weight + ", " + avg_sum);
                        dateoutput.setTime(dateoutput.getTime() + output_res);
                        outsec = dateoutput.getTime() / 1000;

                        int iii = 0;
                        while (iii < num_sum.length) {
                            num_sum[iii] = 0;
                            iii++;
                        }
                        weight = 0;
                        weighthires = 0;

                        /*  if (weightsum >= ((double) outres_sec / (double)inres_sec)) {

                                wrote = linewriter(dateoutput, num_sum, weightsum, avg_sum);
                                dateoutput.setTime(dateoutput.getTime() + output_res);
                                outsec = dateoutput.getTime() / 1000;

                                int iii = 0;
                                while (iii < num_sum.length) {
                                    num_sum[iii] = 0;
                                    iii++;
                                }
                                weightsum = 0;

                            }else{
                                //outsec = (long)(((double)outsec + ((double)outres_sec * weighthires)));
                            } */
                        flag1 = false;
                    }

                    //while (inpsec + inres_sec >= outsec) {
                    while ((inpsec <= outsec) && (inpsec + inres_sec >= outsec + outres_sec) && !flag1) {
                        //if ((inpsec <= outsec) && (inpsec + inres_sec >= outsec + outres_sec && !flag1)) { //Fall 2, Mitte der Reihe

                        weightsum = (double) outres_sec / (double) inres_sec;

                        v = 0;

                        while (v < colunms - 2) {
                            num_value[v] = Double.parseDouble(valstring[v + 2]);

                            if (num_value[v] == -9999 || e < colunms) {
                                this.flag[v] = true;
                            }
                            num_sum[v] = num_value[v] * weightsum;

                            v++;
                        }

                        wrote = linewriter(dateoutput, num_sum, weightsum, avg_sum);
                        //System.out.println("Geschrieben Fall 2, dateoutput, num_sum, weightsum, avg_sum: " + ", " + dateoutput + ", " + num_sum + ", " + weightsum + ", " + avg_sum);
                        dateoutput.setTime(dateoutput.getTime() + output_res);
                        outsec = dateoutput.getTime() / 1000;

                        int iii = 0;
                        while (iii < num_sum.length) {
                            num_sum[iii] = 0;
                            iii++;
                        }

                        weightsum = 0;

                        //}else if ((inpsec < outsec) && (inpsec + inres_sec > outsec - outres_sec) && (inpsec + inres_sec < outsec + outres_sec)) { //Fall 3, Ende der Reihe
                    }
                    if (!(inpsec == outsec) || (inpsec + inres_sec == outsec + outres_sec)) {
                        weighthires = ((((double) inpsec) + (double) inres_sec) - (double) outsec) / (double) inres_sec;

                        v = 0;

                        while (v < colunms - 2) {
                            num_value[v] = Double.parseDouble(valstring[v + 2]);

                            if (num_value[v] == -9999 || e < colunms) {
                                this.flag[v] = true;
                            }
                            num_sum[v] = num_sum[v] + (num_value[v] * weighthires);

                            v++;
                        }
                        flag1 = true;
                    }

                    //if (weightsum >= ((double) outres_sec / (double) inres_sec)) {
                    //} else {
                    //outsec = (long)(((double)outsec + ((double)outres_sec * weighthires)));
                    //}
                    //dateoutput.setTime(dateoutput.getTime() + output_res);
                    //outsec = dateoutput.getTime() / 1000;
                }

                f++;

                System.out.println("Zeilennummer: " + f);

            }
            writer.close();
            reader.close();

        } catch (IOException ee) {
            System.out.println("io fehler");
            ee.printStackTrace();
        }
    }

    private Date dateparser(String datestring, String timestring) {

        Date date = new Date();

        int Jahr = Integer.parseInt(datestring.substring(6, 10));
        int Monat = Integer.parseInt(datestring.substring(3, 5));
        int Tag = Integer.parseInt(datestring.substring(0, 2));
        int Stunde = Integer.parseInt(timestring.substring(0, 2));
        int Minute = Integer.parseInt(timestring.substring(3, 5));
        date.setYear(Jahr - 1900);
        date.setMonth(Monat - 1);
        date.setDate(Tag);
        date.setHours(Stunde);
        date.setMinutes(Minute);
        date.setSeconds(0);

        return date;
    }

    private boolean linewriter(Date writedate, double[] values, Double weightsum, int avg_sum) throws IOException {

        String result = "";

        int i = 0;
        if (avg_sum == 0) { //hier Durchschnttsberechnung; sonst Summe 

            while (i < values.length) {
                values[i] = values[i] / weightsum;
                i++;
            }

        }

        i = 0;

        while (i < values.length) {

            if (this.flag[i]) {
                values[i] = -9999.0;
                this.flag[i] = false;
            }

            result = result + "\t" + Double.toString(values[i]);

            i++;
        }

        this.writer.write(this.objSDF.format(writedate) + "\t" + this.objSDFTime.format(writedate) + result);
        this.writer.newLine();

        return true;
    }

    public static void main(String[] args) throws IOException {
        Temp_Res_change Precipi = new Temp_Res_change();

        Precipi.convert("d:\\\\\\Manfred\\\\GEO415a\\\\Ziegenrueck_D.dat 900 16 0");
        //Precipi.convert(args[0]);

    }
}
