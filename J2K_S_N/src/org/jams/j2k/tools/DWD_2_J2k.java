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
import java.util.Date;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.SimpleTimeZone;
import java.util.TimeZone;

/**
 * Konvertierung von DWD-Stundenwerten ins tabgetrente-Format + Konvertierung
 * Taupunkt in relative Luftfeuchte + Zusammenfassung in Tageswerte
 *
 *
 * @author Manfred Fink TFW
 */
public class DWD_2_J2k {

    BufferedWriter writer;
    BufferedWriter writerrhum;
    BufferedWriter writer2;
    BufferedWriter writermax;
    BufferedWriter writermin;
    BufferedWriter writer2rhum;
    //public final static TimeZone DEFAULT_TIME_ZONE = new SimpleTimeZone(0, "UTC");
    
    public void convert(String inputFileName) throws IOException {
        TimeZone.setDefault(TimeZone.getTimeZone("Etc/GMT-1"));
        int jj = 0;

        int i = 0;
        BufferedReader reader;
        BufferedReader reader2;
        BufferedReader readermeta;
        String linemeta;
        String last_linemeta = "";
        String linehum2;
        String linehum = "";
        
        int f = 0;
        int e = 0;

        int ncolhum = 0;
        StringTokenizer tokhum;
        StringTokenizer tokhum_init;
        StringTokenizer tokfile;
        StringTokenizer tokmeta;
        
        String value, startdatestr;

        //String strDateFormat = "dd.MM.yyyy HH:mm"; //Date format is Specified
        String strDateFormat = "dd.MM.yyyy"; //Date format is Specified
        String strTimeFormat = "HH:mm";
        String strHourFormat = "HH";
        SimpleDateFormat objSDF = new SimpleDateFormat(strDateFormat); //Date format string is passed as an argument to the Date format object
        //objSDF.setTimeZone(TimeZone.getTimeZone("UTC"));
        
        SimpleDateFormat objSDFTime = new SimpleDateFormat(strTimeFormat);
        //objSDFTime.setTimeZone(TimeZone.getTimeZone("UTC"));
        
        SimpleDateFormat objSDFhour = new SimpleDateFormat(strHourFormat);
        //objSDFhour.setTimeZone(TimeZone.getTimeZone("UTC"));
        
        double datavalhum = 0;

        //day = null;
        //mounth = null;
        //year = null;
        //hour = null;
        //minute = null;
        String datestr = null;
        String Headerpr = null;

        tokfile = new StringTokenizer(inputFileName, "\\");
        int path_file_count = tokfile.countTokens();
        String[] path_file = new String[path_file_count];

        int k = 0;

        while (tokfile.hasMoreTokens()) {
            path_file[k] = tokfile.nextToken();
            k++;
        }
        //Beispiel Filename: produkt_td_stunde_19961018_20070630_05419.txt

        String paramet = path_file[k - 1].substring(8, 10);
        String st_num = path_file[k - 1].substring(36, 41);

        String Path = "";

        int l = k - 2;

        while (l >= 0) {
            Path = path_file[l] + "\\" + Path;
            l--;
        }

        String outputFileName = Path + "\\" + paramet + st_num;

        String outputFilemax = Path + "\\tmax" + st_num;
        String outputFilemin = Path + "\\tmin" + st_num;
        String outputFilehum = Path + "\\rhum" + st_num;
        // lesen der zugehörigen Metadatei
        String metafilename = Path + "\\" + "Metadaten_Geographie_" + st_num + ".txt";

        readermeta = new BufferedReader(new FileReader(metafilename));

        while ((linemeta = readermeta.readLine()) != null) {
            last_linemeta = linemeta;
        }

        tokmeta = new StringTokenizer(last_linemeta, ";");

        String stnr_meta = tokmeta.nextToken().trim();
        String hoehe = tokmeta.nextToken().trim();
        String y_coor = tokmeta.nextToken().trim();
        String x_coor = tokmeta.nextToken().trim();
        String startdate = tokmeta.nextToken().trim();
        String enddate = tokmeta.nextToken().trim();
        String Name = tokmeta.nextToken().trim();

        reader = new BufferedReader(new FileReader(inputFileName));
        reader2 = new BufferedReader(new FileReader(inputFileName));
        int j = 0;

        System.out.println("Datei und Matadaten Stationnsnummern Vergleich! Datei: " + st_num + " Meta 0" + stnr_meta);
        
        
        double hoch =  Double.parseDouble(y_coor); 
        double rechts =  Double.parseDouble(x_coor);
        double[] xy = wgs84_gk(rechts,hoch);
        
        String y_coord = String.valueOf(xy[1]);
        String x_coord = String.valueOf(xy[0]);

        //zaelen der Zeilen und Spalten ermitteln des Startdatums
        while ((linehum2 = reader.readLine()) != null) {

            if (j == 1) {
                linehum = linehum2;
            }

            j++;
        }

        tokhum_init = new StringTokenizer(linehum, ";");
        ncolhum = tokhum_init.countTokens();

        String lineraw[] = new String[j + 1];

        int ii = 0;

        // schreiben in einen String Array
        while ((lineraw[ii] = reader2.readLine()) != null) {
            ii++;
        }

        writer = new BufferedWriter(new FileWriter(outputFileName + ".txt"));

        if (paramet.equals("tu")) {
            writerrhum = new BufferedWriter(new FileWriter(outputFilehum + ".txt"));
            writermax = new BufferedWriter(new FileWriter(outputFilemax + "_tag.txt"));
            writermin = new BufferedWriter(new FileWriter(outputFilemin + "_tag.txt"));
            writer2rhum = new BufferedWriter(new FileWriter(outputFilehum + "_tag.txt"));
        }

        writer2 = new BufferedWriter(new FileWriter(outputFileName + "_tag.txt"));

        String[] Headerhum = new String[ncolhum];

        String[] hum_string = new String[ncolhum];
        int tag_int_alt = 0;

        int tag_int = 0;
        boolean flag = true;
        double result_tag = 0.0;
        double result_tag2 = 0.0;
        double result_sum = 0;
        double result_max = 0;
        double result_min = 0;
        double result2_sum = 0;

        double param1 = 0;
        double param2 = 0;

        double result = 0;
        double result2 = 0;
        startdatestr = linehum.substring(12, 23);

        int year_int = Integer.parseInt(startdatestr.substring(0, 4));
        int month_int = Integer.parseInt(startdatestr.substring(4, 6)) - 1;
        int day_int = Integer.parseInt(startdatestr.substring(6, 8));
        int hour_int = Integer.parseInt(startdatestr.substring(8, 10));
        // für Stündliche Zeitschritte
        int increment = 3600000;
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
        date.setMinutes(0);
        date.setSeconds(0);
        tag_int_alt = date.getDay();
        
        

        date_tag.setTime(date.getTime());
        date_tag.setHours(0);
        date_tag.setMinutes(0);
        date_tag.setSeconds(0);
        try {
            //metainformationen Schreiben
            writer.write("name" + "\t" + Name);
            writer.newLine();
            writer.write("ID" + "\t" + stnr_meta);
            writer.newLine();
            writer.write("elevation" + "\t" + hoehe);
            writer.newLine();
            writer.write("x" + "\t" + x_coord);
            writer.newLine();
            writer.write("y" + "\t" + y_coord);
            writer.newLine();
            writer2.write("name" + "\t" + Name);
            writer2.newLine();
            writer2.write("ID" + "\t" + stnr_meta);
            writer2.newLine();
            writer2.write("elevation" + "\t" + hoehe);
            writer2.newLine();
            writer2.write("x" + "\t" + x_coord);
            writer2.newLine();
            writer2.write("y" + "\t" + y_coord);
            writer2.newLine();

            if (paramet.equals("tu")) {

                writermax.write("name" + "\t" + Name);
                writermax.newLine();
                writermax.write("ID" + "\t" + stnr_meta);
                writermax.newLine();
                writermax.write("elevation" + "\t" + hoehe);
                writermax.newLine();
                writermax.write("x" + "\t" + x_coord);
                writermax.newLine();
                writermax.write("y" + "\t" + y_coord);
                writermax.newLine();
                writermin.write("name" + "\t" + Name);
                writermin.newLine();
                writermin.write("ID" + "\t" + stnr_meta);
                writermin.newLine();
                writermin.write("elevation" + "\t" + hoehe);
                writermin.newLine();
                writermin.write("x" + "\t" + x_coord);
                writermin.newLine();
                writermin.write("y" + "\t" + y_coord);
                writermin.newLine();
                writerrhum.write("name" + "\t" + Name);
                writerrhum.newLine();
                writerrhum.write("ID" + "\t" + stnr_meta);
                writerrhum.newLine();
                writerrhum.write("elevation" + "\t" + hoehe);
                writerrhum.newLine();
                writerrhum.write("x" + "\t" + x_coord);
                writerrhum.newLine();
                writerrhum.write("y" + "\t" + y_coord);
                writerrhum.newLine();
                writer2rhum.write("name" + "\t" + Name);
                writer2rhum.newLine();
                writer2rhum.write("ID" + "\t" + stnr_meta);
                writer2rhum.newLine();
                writer2rhum.write("elevation" + "\t" + hoehe);
                writer2rhum.newLine();
                writer2rhum.write("x" + "\t" + x_coord);
                writer2rhum.newLine();
                writer2rhum.write("y" + "\t" + y_coord);
                writer2rhum.newLine();
            }
            while (f <= (j - 1)) {

                tokhum = new StringTokenizer(lineraw[f], ";");
                if (f == 0) {
                    while (tokhum.hasMoreTokens()) {
                        value = tokhum.nextToken();
                        Headerhum[e] = value;
                        e++;
                    }
                } else {

                    int eee = 0;

                    while (eee < e) {
                        value = tokhum.nextToken();
                        value = value.trim();
                        hum_string[eee] = value;
                        eee++;
                    }

                    datestr = hum_string[1];

                    int Jahr = Integer.parseInt(datestr.substring(0, 4));
                    int Monat = Integer.parseInt(datestr.substring(4, 6)) - 1;
                    int Tag = Integer.parseInt(datestr.substring(6, 8));
                    int Stunde = Integer.parseInt(datestr.substring(8, 10));

                    date2.setYear(Jahr - 1900);
                    date2.setMonth(Monat);
                    date2.setDate(Tag);
                    date2.setHours(Stunde);
                    date2.setMinutes(0);
                    date2.setSeconds(0);

                    i = 0;
                    
                    itterationen = Math.round(((double) date2.getTime() - (double) date.getTime()) / (double) increment);
                    if (paramet.equals("td") || paramet.equals("tu")) {
                        param1 = Double.parseDouble(hum_string[3]);
                        param2 = Double.parseDouble(hum_string[4]);
                    } else if ((paramet.equals("rr")) || (paramet.equals("sd")) || paramet.equals("ff")) {
                        param1 = Double.parseDouble(hum_string[3]);
                    }

                    if (f > 1) {

                        while (i < itterationen) {

                            if (i == 0) {
                                date_itter.setTime(date.getTime());
                                writer.write(objSDF.format(date) + "\t" + objSDFTime.format(date) + "\t" + Double.toString(result));
                                writer.newLine();

                                if (paramet.equals("tu")) {
                                    writerrhum.write(objSDF.format(date) + "\t" + objSDFTime.format(date) + "\t" + Double.toString(result2));
                                    writerrhum.newLine();
                                }

                                flag = true;

                                jj++;
                                result_sum = result_sum + result;
                                result_max = Math.max(result_max, result);
                                result_min = Math.min(result_min, result);
                                result2_sum = result2_sum + result2;
                            } else {

                                Double fill = -9999.0;

                                if (paramet.equals("sd")) {
                                    int hour = Integer.parseInt(objSDFhour.format(date_tag));
                                    if (hour > 20 || hour < 3) {
                                        fill = 0.0;
                                    }

                                }

                                date_itter.setTime(date_itter.getTime() + increment);
                                writer.write(objSDF.format(date_itter) + "\t" + objSDFTime.format(date_itter) + "\t" + Double.toString(fill));
                                writer.newLine();

                                if (paramet.equals("tu")) {
                                    writerrhum.write(objSDF.format(date_itter) + "\t" + objSDFTime.format(date_itter) + "\t" + Double.toString(fill));
                                    writerrhum.newLine();
                                }

                                flag = false;
                            }
                            i++;

                            if (date_tag.getDate() != date_itter.getDate()) {
                                if (jj == 24 || (jj == 18 && paramet.equals("sd"))) {

                                    if (paramet.equals("td") || paramet.equals("tu") || paramet.equals("ff")) {
                                        result_tag = result_sum / jj;
                                    }

                                    if (paramet.equals("tu")) {
                                        result_tag2 = result2_sum / jj;

                                    }

                                    if (paramet.equals("rr") || paramet.equals("sd")) {
                                        result_tag = result_sum;
                                    }

                                    writer2.write(objSDF.format(date_tag) + "\t" + objSDFTime.format(date_tag) + "\t" + Double.toString(result_tag) + "\t" + Integer.toString(jj));
                                    writer2.newLine();

                                    if (paramet.equals("tu")) {
                                        writer2rhum.write(objSDF.format(date_tag) + "\t" + objSDFTime.format(date_tag) + "\t" + Double.toString(result_tag2) + "\t" + Integer.toString(jj));
                                        writer2rhum.newLine();
                                        writermax.write(objSDF.format(date_tag) + "\t" + objSDFTime.format(date_tag) + "\t" + Double.toString(result_max) + "\t" + Integer.toString(jj));
                                        writermax.newLine();
                                        writermin.write(objSDF.format(date_tag) + "\t" + objSDFTime.format(date_tag) + "\t" + Double.toString(result_min) + "\t" + Integer.toString(jj));
                                        writermin.newLine();
                                        result2_sum = 0;
                                        result_max = -1000;
                                        result_min = 1000;
                                    }

                                    result_sum = 0;
                                    jj = 0;
                                    date_tag.setDate(date_tag.getDate() + 1);
                                } else {
                                    writer2.write(objSDF.format(date_tag) + "\t" + objSDFTime.format(date_tag) + "\t" + Double.toString(-9999.0) + "\t" + Integer.toString(jj));
                                    writer2.newLine();

                                    if (paramet.equals("tu")) {
                                        writer2rhum.write(objSDF.format(date_tag) + "\t" + objSDFTime.format(date_tag) + "\t" + Double.toString(-9999.0) + "\t" + Integer.toString(jj));
                                        writer2rhum.newLine();
                                        writermax.write(objSDF.format(date_tag) + "\t" + objSDFTime.format(date_tag) + "\t" + Double.toString(-9999.0) + "\t" + Integer.toString(jj));
                                        writermax.newLine();
                                        writermin.write(objSDF.format(date_tag) + "\t" + objSDFTime.format(date_tag) + "\t" + Double.toString(-9999.0) + "\t" + Integer.toString(jj));
                                        writermin.newLine();
                                    }

                                    result_sum = 0;
                                    jj = 0;
                                    date_tag.setDate(date_tag.getDate() + 1);
                                }
                            }

                        }

                        date.setTime(date2.getTime());
                    }

                    if ((param1 == -999 || param1 == -999.0)) {
                        param1 = -9999.0;
                    }
                    if ((param2 == -999 || param2 == -999.0)) {
                        param2 = -9999.0;
                    }
                    if (paramet.equals("td")) {

                        double SDD_tau = SDD(param2);
                        double SDD_temp = SDD(param1);

                        result = (100 * SDD_tau) / SDD_temp;
                    } else if (paramet.equals("sd")) {
                        result = param1 / 60; //Sonnenscheindauer von Minute zu Stunde                            
                    } else if (paramet.equals("tu") || paramet.equals("rr") || paramet.equals("tu") || paramet.equals("ff")) {
                        result = param1;
                        result2 = param2;
                    }

                }

                f++;
            }

            date_itter.setTime(date_itter.getTime() + increment);
            writer.write(objSDF.format(date_itter) + "\t" + objSDFTime.format(date_itter) + "\t" + Double.toString(result));
            writer.newLine();
            if (jj == 24) {
                result_tag = result_sum / jj;
                writer2.write(objSDF.format(date_itter) + "\t" + objSDFTime.format(date_itter) + "\t" + Double.toString(result_tag) + "\t" + Integer.toString(jj));
                writer2.newLine();

                if (paramet.equals("tu")) {
                    writer2rhum.write(objSDF.format(date_tag) + "\t" + objSDFTime.format(date_tag) + "\t" + Double.toString(result_tag2) + "\t" + Integer.toString(jj));
                    writer2rhum.newLine();
                    writermax.write(objSDF.format(date_tag) + "\t" + objSDFTime.format(date_tag) + "\t" + Double.toString(result_max) + "\t" + Integer.toString(jj));
                    writermax.newLine();
                    writermin.write(objSDF.format(date_tag) + "\t" + objSDFTime.format(date_tag) + "\t" + Double.toString(result_min) + "\t" + Integer.toString(jj));
                    writermin.newLine();
                }

            } else {
                writer2.write(objSDF.format(date_itter) + "\t" + objSDFTime.format(date_itter) + "\t" + Double.toString(-9999.0) + "\t" + Integer.toString(jj));
                writer2.newLine();

                if (paramet.equals("tu")) {
                    writer2rhum.write(objSDF.format(date_tag) + "\t" + objSDFTime.format(date_tag) + "\t" + Double.toString(-9999.0) + "\t" + Integer.toString(jj));
                    writer2rhum.newLine();
                    writermax.write(objSDF.format(date_tag) + "\t" + objSDFTime.format(date_tag) + "\t" + Double.toString(-9999.0) + "\t" + Integer.toString(jj));
                    writermax.newLine();
                    writermin.write(objSDF.format(date_tag) + "\t" + objSDFTime.format(date_tag) + "\t" + Double.toString(-9999.0) + "\t" + Integer.toString(jj));
                    writermin.newLine();
                }

            }

            writer.close();
            writer2.close();
            reader.close();
            reader2.close();

        } catch (IOException ee) {
            System.out.println("io fehler");
            ee.printStackTrace();
        }
    }

    private double SDD(double temper) {
        //Saettigungsdampfdruck

        double ssd = 0;

        if (temper >= 0) {
            ssd = 6.1078 * Math.pow(10, (7.5 * temper) / (237.3 + temper));
        } else {
            // ueber Eis
            ssd = 6.1078 * Math.pow(10, (9.5 * temper) / (265.5 + temper));
        }

        return ssd;
    }
    
        private double[] wgs84_gk(double x, double y) {
        double[] x_y = new double[2];
        //Konstanten
        double rho = 180 / Math.PI;
        double e2 = 0.0067192188;
        double c = 6398786.849;
        //double c =  6377397;
        double sy = 4; //GK Meridianstreifen
        
        double bf = y / rho;
        double g = 111120.61962 * y  -15988.63853 * Math.sin(2*bf) +16.72995 * Math.sin(4*bf) -0.02178 * Math.sin(6*bf) +0.00003 * Math.sin(8*bf);
        double co = Math.cos(bf);
        double g2 = e2 * Math.pow(co, 2);
        double g1 = c / Math.sqrt(1+g2);
        double t = Math.sin(bf) / Math.cos(bf);
        double dl = x - sy * 3;
        double fa = co * dl / rho;
        double hoch = g +  (((Math.pow(fa, 2) * t * g1 / 2) +  (Math.pow(fa, 4) * t * g1) * ((5 - Math.pow(t, 2)) + (9 * g2))) / 24);
        double rm = (fa * g1) + (((Math.pow(fa, 3)* g1) * (1 - Math.pow(t, 2) + g2)) / 6) + ((Math.pow(fa, 5) * g1) * (5 - (18 * Math.pow(t, 6))) / 120);
        double rechts = rm + (sy * 1000000) + 500000;
        x_y[0] = rechts;
        x_y[1] = hoch + 400; //geschätzte korrektur bis bessere Parameter gefunden wurden, ca 200m fehler, was einer durchschnittlchen Modellaufloesung entspricht
        
        

        return x_y;
        
        
                /**     
                {==============================================================================}
        { Umrechnung von Geographischen Koordinaten in Gauss-Krueger-Koordinaten       }
        { Formel: Grossmann,W., Geodätische Abbildungen, 1964, Seite 151               }
        { Parameter: geo.Breite (Grad.Min.Sek) in Altgrad  : Twinkel                   }
        {            geo.Laenge (Grad.Min.Sek) in Altgrad  : Twinkel                   }
        {            Zielsystemnummer (Meridiankennziffer) : longint                   }
        {            Rechtswert (X) im Zielsystem          : double                    }
        {            Hochwert (Y) im Zielsystem            : double                    }
        {==============================================================================}
        procedure GeoGk(br,la:Twinkel;sy:Longint;var x,y:double);
        const
          {26}
          rho = 180 / pi;
        var
          brDezimal,laDezimal,rm,e2,c,bf,g,co,g2,g1,t,dl,fa,grad,min,sek :extended;
        begin
          {25}
          e2 := 0.0067192188;
          {27}
          c := 6398786.849;
          {in Dezimal}
          {Breite}
          brDezimal := br.grad + br.min / 60 + br.sek / 3600;
          {Laenge}
          laDezimal := la.grad + la.min / 60 + la.sek / 3600;
          {64}
          bf := brDezimal / rho;
          {65}
          g := 111120.61962 * brDezimal
               -15988.63853 * sin(2*bf)
               +16.72995 * sin(4*bf)
               -0.02178 * sin(6*bf)
               +0.00003 * sin(8*bf);
          {70}
          co := cos(bf);
          {71}
          g2 := e2 * (co * co);
          {72}
          g1 := c / sqrt(1+g2);
          {73}
          t := sin(bf) / cos(bf); {=tan(t)}
          {74}
          dl := laDezimal - sy * 3;
          {77}
          fa := co * dl / rho;
          {78}
          y := g
               + fa * fa * t * g1 / 2
               + fa * fa * fa * fa * t * g1 * (5 - t * t + 9 * g2) / 24;
          {81}
          rm := fa * g1
                + fa * fa * fa * g1 * (1 - t * t + g2) / 6
                + fa * fa * fa * fa * fa * g1 * (5 - 18 * t * t * t * t * t * t) / 120;
          {84}
          x := rm + sy * 1000000 + 500000;
        end;
        */       
        
    }
    

    public static void main(String[] args) throws IOException {
        DWD_2_J2k Precipi = new DWD_2_J2k();

        //Precipi.convert("D:\\\\cygwin64\\\\home\\\\mfi\\\\DWD\\\\produkt_ff_stunde_19730101_20181231_01270.txt");
        Precipi.convert(args[0]);
    }
}
