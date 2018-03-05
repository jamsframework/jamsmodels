/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package waska_gui;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author manni
 */
public class JAMSStarter {

    private Model model;
    private static String JAMS_PATH = "C:\\jams_WASKA";

    public JAMSStarter(Model model) {
        this.model = model;
    }

    public void start() {

        String jamFile = model.getJamFile();
        String parameterList = "";
        for (Parameter p : model.getPList()) {
            parameterList += p.getValue() + ";";
        }

        try {
            String all = " -m " + jamFile + " -p " + parameterList;
            ProcessBuilder pb = new ProcessBuilder("java", "-jar", "-Xms128M",
                    "-splash:", "-Xmx1024M", JAMS_PATH + "\\jams-starter.jar",
                    "-m", jamFile, "-p", parameterList);
            for (String s : pb.command()) {
                System.out.print(s + " ");
            }
            System.out.println("");
            pb.directory(new File("."));
            Process p = pb.start();
//            BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
//            String line;
//            while ((line = reader.readLine()) != null) {
//                System.out.println(line);
//            }

        } catch (IOException ex) {
            Logger.getLogger(JAMSStarter.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

//    public static void main(String[] args) {
//        Model model = new Model("C:\\JAMSApp_neu\\j2k_wipfra_100_starter.jam");
//        JAMSStarter starter = new JAMSStarter(model);
//        starter.start();
//
//    }

}
