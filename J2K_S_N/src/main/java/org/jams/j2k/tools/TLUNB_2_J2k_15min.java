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
 * Konvertierung von TLUBN-15Minutenwerten ins tabgetrente-Format + Luecken fuellen
 *
 *
 *
 *
 * @author Manfred Fink TFW
 */
public class TLUNB_2_J2k_15min {

    BufferedWriter writer;

    //public final static TimeZone DEFAULT_TIME_ZONE = new SimpleTimeZone(0, "UTC");

    public void convert(String inputFileName) throws IOException {
        TimeZone.setDefault(TimeZone.getTimeZone("Etc/GMT-1"));
        int jj = 0;

        int i = 0;
        BufferedReader reader;
        BufferedReader reader2;

        String last_linemeta = "";
        String linehum2;
        String linehum = "";

        int f = 16;
        int e = 0;

        int ncolhum = 0;
        StringTokenizer tokhum;
        StringTokenizer tokhum_init;
        StringTokenizer tokfile;
        StringTokenizer tokmeta;

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

        SimpleDateFormat objSDFhour = new SimpleDateFormat(strHourFormat);
        //objSDFhour.setTimeZone(TimeZone.getTimeZone("UTC"));



        //day = null;
        //mounth = null;
        //year = null;
        //hour = null;
        //minute = null;
        String datestr = null;
        String timestr = null;
        

        tokfile = new StringTokenizer(inputFileName, "\\");
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

        String outputFileName = Path + "\\" + "J2K" + Filename;


        // lesen der zugehörigen Metadatei
        

       





        reader = new BufferedReader(new FileReader(inputFileName));
        reader2 = new BufferedReader(new FileReader(inputFileName));
        int j = 0;
        

        

 

        //zaelen der Zeilen und Spalten ermitteln des Startdatums
        while ((linehum2 = reader.readLine()) != null) {

            if (j == 16) {
                linehum = linehum2;
            }

            j++;
        }
        
    
        

        tokhum_init = new StringTokenizer(linehum, " ");
        ncolhum = tokhum_init.countTokens();

        String lineraw[] = new String[j + 1];

        int ii = 0;

        // schreiben in einen String Array
        while ((lineraw[ii] = reader2.readLine()) != null) {
            ii++;
        }

        int jjj = 0;
        
        while (jjj < 16){
            
            headerlines[jjj] = lineraw[jjj];
            jjj++;
        }
        
        writer = new BufferedWriter(new FileWriter(outputFileName));

        jjj = 0;
        
        while (jjj < 16){
            writer.write(headerlines[jjj]);
            writer.newLine();           
            jjj++;
        }



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

        String result = "";
        double result2 = 0;
        startdatestr = linehum.substring(0, 19);

        int year_int = Integer.parseInt(startdatestr.substring(6, 10));
        int month_int = Integer.parseInt(startdatestr.substring(3, 5)) - 1;
        int day_int = Integer.parseInt(startdatestr.substring(0, 2));
        int hour_int = Integer.parseInt(startdatestr.substring(11, 13));
        int min_int = Integer.parseInt(startdatestr.substring(14, 16));
        // für Stündliche Zeitschritte
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
        
        
        
        
        
        try {
            //metainformationen Schreiben




            while (f <= (j - 1)) {

                tokhum = new StringTokenizer(lineraw[f], " ");
                
                    while (tokhum.hasMoreTokens()) {
                        value = tokhum.nextToken();
                        hum_string[e] = value;
                        e++;
                    }
                    result =  hum_string[e-1];
                    e = 0;
                    
                    if (result.contains("RWLuecke")){
                        result = "-9999.0";
                    }
                        

                    datestr = hum_string[0];

                    int Jahr = Integer.parseInt(datestr.substring(6, 10));
                    int Monat = Integer.parseInt(datestr.substring(3, 5)) - 1;
                    int Tag = Integer.parseInt(datestr.substring(0, 2));
                    timestr = hum_string[1];
                    int Stunde = Integer.parseInt(timestr.substring(0, 2));
                    int Minute = Integer.parseInt(timestr.substring(3, 5));
                    int Sekunde = Integer.parseInt(timestr.substring(6, 8));
                    
                    if ((!(Minute == 30 || Minute == 45 || Minute == 0 || Minute == 15))|| Sekunde > 0){
                        f++;
                    }else{
                    
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

                            if (i == 0) {
                                date_itter.setTime(date.getTime());
                                writer.write(objSDF.format(date) + "\t" + objSDFTime.format(date) + "\t" + result);
                                writer.newLine();



                                flag = true;

                                jj++;
                                //result_sum = result_sum + result;
                                //result_max = Math.max(result_max, result);
                                //result_min = Math.min(result_min, result);
                                //result2_sum = result2_sum + result2;
                            } else {

                                Double fill = -9999.0;



                                date_itter.setTime(date_itter.getTime() + increment);
                                writer.write(objSDF.format(date_itter) + "\t" + objSDFTime.format(date_itter) + "\t" + Double.toString(fill));
                                writer.newLine();



                                flag = false;
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
                

                

                f++;
                    }
            }

            date_itter.setTime(date_itter.getTime() + increment);
            writer.write(objSDF.format(date_itter) + "\t" + objSDFTime.format(date_itter) + "\t" + result);
            writer.newLine();

            writer.close();
            reader.close();

        } catch (IOException ee) {
            System.out.println("io fehler");
            ee.printStackTrace();
        }
    }


   

    public static void main(String[] args) throws IOException {
        TLUNB_2_J2k_15min runoff = new TLUNB_2_J2k_15min();

        runoff.convert("H:\\\\Kleinspeicher\\\\Heichelheim\\\\buttelstedt_abfluss_produktion_zr-folge_kontinuierlich_version_1_m_s_2008-2020.asc");
        //runoff.convert(args[0]); H:\Kleinspeicher\Heichelheim
    }
}
