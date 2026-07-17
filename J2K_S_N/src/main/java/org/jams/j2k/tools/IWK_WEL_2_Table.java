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

/**
 * Konvertierung von IWK-HW .Wel Dateien in Excel verwendbare Tabellen
 *
 *
 *
 * @author Manfred Fink TFW
 */
public class IWK_WEL_2_Table {

    BufferedWriter writer;

    public void convert(String inputFileName) throws IOException {
        int jj = 0;

        BufferedReader reader;

        int f = 0;
        int e = 0;

        StringTokenizer tokline;
        StringTokenizer tokfile;

        String valuestr = "";
        String line = "";
        int tokcount = 0;
        int y = 0;
        int x = 1;
        tokfile = new StringTokenizer(inputFileName, "\\");
        int path_file_count = tokfile.countTokens();
        String[] path_file = new String[path_file_count];
        String count;
        String resolu;
        double resolution = 0.0;
        double delta = 0.0;
        int k = 0;

        while (tokfile.hasMoreTokens()) {
            path_file[k] = tokfile.nextToken();
            k++;
        }
        //Beispiel Filename: HEI_KO_100A.WEL

        String Path = "";

        int l = k - 2;

        while (l >= 0) {
            Path = path_file[l] + "\\" + Path;
            l--;
        }

        String dauer = "";
        String jaehrl = "";
        String Name = "";
        String temp = "";
        String[][] matrix = new String[50][5000];
        reader = new BufferedReader(new FileReader(inputFileName));

        int j = 0;

        //zaelen der Zeilen und Spalten ermitteln des Startdatums
        while ((line = reader.readLine()) != null) {
            tokline = new StringTokenizer(line, " ");
            if (j == 0) {

            } else if (j == 1) {
                tokcount = tokline.countTokens();
                temp = tokline.nextToken();
                Name = tokline.nextToken();
                jaehrl = tokline.nextToken();
                dauer = tokline.nextToken();
                if (tokcount == 5) {
                    Name = tokline.nextToken() + Name ;
                }
                matrix[x][0] = dauer;
                y = 1;
            } else if (j == 2) {

                count = tokline.nextToken();
                resolu = tokline.nextToken();

                delta = Double.parseDouble(resolu);

                resolution = delta;

                matrix[0][0] = "Zeit";

            } else {
                tokcount = tokline.countTokens();
                int i = 0;
                while (i < tokcount) {
                    valuestr = tokline.nextToken();
                    if (valuestr.equals(temp)) {
                        Name = tokline.nextToken();
                        String jaehrl1 = tokline.nextToken();
                        dauer = tokline.nextToken();
                        if (tokcount == 5) {
                            Name = tokline.nextToken() + Name;
                        }
                        line = reader.readLine();
                        x++;
                        matrix[x][0] = dauer;
                        y = 1;
                        i = tokcount;
                    } else {
                        matrix[x][y] = valuestr;
                        y++;

                    }

                    System.out.println("x: " + x + " y: " + y);
                    i++;
                }

            }

            j++;

        }

        String outputFileName = Path + "\\" + Name + jaehrl;

        System.out.println("Datei: " + outputFileName);

        writer = new BufferedWriter(new FileWriter(outputFileName + ".txt"));

        int ii = 0;

        while (ii < y) {
            String zeile = "";

            int iii = 0;
            while (iii <= 42) {
                if (matrix[iii][ii] == null) {
                    matrix[iii][ii] = "";
                }
                zeile = zeile + matrix[iii][ii] + "\t";
                iii++;
            }

            try {

                writer.write(zeile);
                writer.newLine();

            } catch (IOException ee) {
                System.out.println("io fehler");
                ee.printStackTrace();
            }
            ii++;
            matrix[0][ii] = String.valueOf(resolution);
            resolution = resolution + delta;
        }

        writer.close();

        reader.close();

    }

    public static void main(String[] args) throws IOException {
        IWK_WEL_2_Table Welle = new IWK_WEL_2_Table();

        //Welle.convert("D:\\\\wechmar\\\\NachtragBerechnung\\\\Kostra\\\\HOPF_KOSTRA.WEL");
        Welle.convert(args[0]);
    }
}
