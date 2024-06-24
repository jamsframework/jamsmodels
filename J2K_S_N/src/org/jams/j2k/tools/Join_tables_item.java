/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jams.j2k.tools;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.StringTokenizer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Liest alles JAMS Datastore Timeloop Tabellen in einem Verzeichnis 
 * und extrahiert eine Spalte und scheibt sie in einen gemeinasmen File
 *
 *
 *
 * @author Manfred Fink TFW
 */
public class Join_tables_item {

    BufferedWriter writer;
    BufferedWriter writer_max;
    BufferedReader reader;
    StringTokenizer tokline;
    StringTokenizer tokhead;

    public void convert(String inputDirectory, String itemname, int numberlines) throws IOException {

        try {
            List<File> timeseries = Files.walk(Paths.get(inputDirectory))
                    .filter(Files::isRegularFile)
                    .map(Path::toFile)
                    .collect(Collectors.toList());

            int fcount = timeseries.size();

            String namestr = "";
            String pathstr = "";
            String line = "";
            int itemNr = 0;
            int ii = 0;
            int jj = 0;
            int i = 0;
            String outputFileName = inputDirectory + "////Join_Timeloop";
            String outputFileMax = inputDirectory + "////MAX_Timeloop";
            String[][] Filematrix = new String[fcount+1][numberlines + 2];
            String[][] MAXmatrix = new String[fcount+1][5];
            writer = new BufferedWriter(new FileWriter(outputFileName + ".txt"));
            writer_max = new BufferedWriter(new FileWriter(outputFileMax + ".txt"));
            while (i < fcount) {
                File Act_file = timeseries.get(i);
                pathstr = Act_file.getAbsolutePath();
                namestr = Act_file.getName();

                if (namestr.startsWith("TimeLoo") && namestr.endsWith(".dat") && namestr.length() > 12) {

                    String itemhead = namestr.substring(8, namestr.length() - 4);
                    tokhead = new StringTokenizer(itemhead, "_");
                    System.out.println("Processed File, " + i + ", " + namestr);
                    String dura = tokhead.nextToken();
                    String yearly = tokhead.nextToken();

                    ii++;

                    reader = new BufferedReader(new FileReader(pathstr));

                    int j = 0;
                    int k = 0;
                    jj = 0;
                    while ((line = reader.readLine()) != null) {

                        if (j < 5) {

                        } else if (j == 5) {
                            tokline = new StringTokenizer(line, "\t");

                            while (tokline.hasMoreTokens()) {
                                String test = tokline.nextToken();
                                if (test.contentEquals(itemname)) {
                                    itemNr = k;
                                    Filematrix[ii][jj] = itemhead;
                                    MAXmatrix[ii][jj] = itemhead;
                                    MAXmatrix[0][jj] = "Name";
                                    Filematrix[0][jj] = "Name";
                                    Filematrix[ii][jj + 1] = dura;
                                    MAXmatrix[ii][jj + 1] = dura;
                                    MAXmatrix[0][jj + 1] = "Dauer";
                                    Filematrix[0][jj + 1] = "Dauer";
                                    Filematrix[ii][jj + 2] = yearly;
                                    MAXmatrix[ii][jj + 2] = yearly;
                                    MAXmatrix[0][jj + 2] = "HQ(t)";
                                    Filematrix[0][jj + 2] = "HQ(t)";

                                    jj++;
                                }
                                k++;
                            }

                        } else if (j > 9) {
                            tokline = new StringTokenizer(line, "\t");
                            int kk = 0;
                            String selvalue = "";

                            while (tokline.hasMoreTokens()) {
                                String value = tokline.nextToken();
                                if (kk == itemNr) {
                                    selvalue = value;
                                    Filematrix[ii][jj + 2] = selvalue;
                                    Filematrix[0][jj + 2] = String.valueOf(jj - 1);
                                    jj++;
                                }
                                kk++;

                            }

                        }
                        j++;
                    }
                }
                i++;
            }

            //extrat max HQ and durarion in time steps
            int n = 1;
            int m = 3;

            
            int durati = 0;

            MAXmatrix[0][3] = "MaxHQ";
            MAXmatrix[0][4] = "Zeitschritt";

            while (n <= ii) {

                m = 3;
                double maxi = 0;

                while (m <= jj) {
                    Double value2 = Double.valueOf(Filematrix[n][m]);
                    if (maxi < value2) {
                        maxi = value2;
                        durati = m;
                    }

                    m++;

                }

                MAXmatrix[n][3] = String.valueOf(maxi);
                MAXmatrix[n][4] = String.valueOf(durati);
                n++;
            }

            ii = 0;

            int x_file = Filematrix.length;
            int y_file = Filematrix[0].length;

            while (ii < y_file) {
                String zeile = "";

                int iii = 0;
                while (iii < x_file) {
                    if (Filematrix[iii][ii] == null) {
                        Filematrix[iii][ii] = "";
                    }
                    zeile = zeile + Filematrix[iii][ii] + "\t";
                    iii++;
                }

                try {

                    writer.write(zeile);
                    writer.newLine();

                } catch (IOException ee) {
                    System.out.println("io fehler joinfile");
                    ee.printStackTrace();
                }
                ii++;

            }
            
            int x_max = MAXmatrix.length;
            int y_max = MAXmatrix[0].length;
            ii = 0;

            while (ii < y_max) {
                String zeile = "";

                int iii = 0;
                while (iii < x_max) {
                    if (MAXmatrix[iii][ii] == null) {
                        MAXmatrix[iii][ii] = "";
                    }
                    zeile = zeile + MAXmatrix[iii][ii] + "\t";
                    iii++;
                }

                try {

                    writer_max.write(zeile);
                    writer_max.newLine();

                } catch (IOException ee) {
                    System.out.println("io fehler maxfile");
                    ee.printStackTrace();
                }
                ii++;

            }
            
            writer.close();
            writer_max.close();

            reader.close();
            timeseries.clear();
        } catch (IOException ee) {
            System.out.println("io fehler generell");
            ee.printStackTrace();
        }
        
    }

    public static void main(String[] args) throws IOException {
        Join_tables_item JAMS_files = new Join_tables_item();

        JAMS_files.convert("D:\\\\JAMSApplications\\\\J2k_Ziegenr_1h2\\\\output\\\\current\\\\Kostra50", "catchmentSimRunoff_qm", 1200);

        //JAMS_files.convert(args[0]);
    }
}
