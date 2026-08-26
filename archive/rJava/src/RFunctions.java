/*
 * RFunctions.java
 * Created on 18.04.2019, 22:19:50
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

import java.awt.FileDialog;
import java.awt.Frame;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import org.rosuda.JRI.REXP;
import org.rosuda.JRI.RMainLoopCallbacks;
import org.rosuda.JRI.RVector;
import org.rosuda.JRI.Rengine;

/**
 *
 * @author Sven Kralisch <sven.kralisch@uni-jena.de>
 */
public class RFunctions {

    public static void main(String[] args) {

        Rengine re;
        if (args.length == 0) {

            re = new Rengine(args, false, new TextConsole());
            if (!re.waitForR()) {
                System.out.println("Cannot load R");
                return;
            }

            if (true) {
                System.out.println("Now the console is yours ... have fun");
                re.startMainLoop();
            } else {
                re.end();
                System.out.println("end");
            }
        } else {

//System.out.println("R_HOME =" + System.getenv("R_HOME"));
//  String path =System.getenv("R_HOME") + "\\library" ;
//   File folder = new File(path);
//  File[] listOfFiles = folder.listFiles();
//
//    for (int i = 0; i < listOfFiles.length; i++) {
//      if (listOfFiles[i].isFile()) {
//        System.out.println("File " + listOfFiles[i].getName());
//      } else if (listOfFiles[i].isDirectory()) {
//        System.out.println("Directory " + listOfFiles[i].getName());
//      }
//    }        
            re = new Rengine(null, false, null);
            re.waitForR();
            REXP x;
            //re.eval("print(1:10/3)");
            re.eval("library(flowregime)");
            System.out.println("dat_zoo <- read.zoo(\"" + args[0] + "\", index.column = 0, sep = \",\", format = \"%d.%m.%Y\")");
            re.eval("dat_zoo <- read.zoo(\"" + args[0] + "\", index.column = 0, sep = \",\", format = \"%d.%m.%Y\")");
            re.eval("dat_xts <- as.xts(dat_zoo)");

//            x = re.eval("index(dat_xts)");
//            System.out.println(x.getType());
//            double[] da = x.asDoubleArray();
//            for (double d: da) {
//                System.out.println(d);
//            }

            x = re.eval("build_EFC_thresholds(dat_xts, method = \"advanced\")");
            RVector v = x.asVector();
            for (int i = 0; i < v.size(); i++) {
                System.out.print(v.getNames().get(i) + ": ");
                System.out.println(v.at(i).asDouble());
            }

            System.out.println("-------------------------------");

            x = re.eval("build_EFC_thresholds(dat_xts, method = \"standard\")");
            v = x.asVector();
            for (int i = 0; i < v.size(); i++) {
                System.out.print(v.getNames().get(i) + ": ");
                System.out.println(v.at(i).asDouble());
            }

            System.out.println("-------------------------------");

            x = re.eval("EFC(dat_xts, method = \"advanced\")");
            String[] sa = x.asStringArray();
            for (String s : sa) {
                System.out.println(s);
            }
            re.end();
        }

//        System.out.println(x = re.eval("iris"));
//        RVector v = x.asVector();
//        if (v.getNames() != null) {
//            System.out.println("has names:");
//            for (Enumeration e = v.getNames().elements(); e.hasMoreElements();) {
//                System.out. rintln(e.nextElement());
//            }
//        }
//
//        if (true) {
//            System.out.println("Now the console is yours ... have fun");
//            re.startMainLoop();
//        } else {
//            re.end();
//            System.out.println("end");
//        }
    }
}

class TextConsole implements RMainLoopCallbacks {

    public void rWriteConsole(Rengine re, String text, int oType) {
        System.out.print(text);
    }

    public void rBusy(Rengine re, int which) {
        System.out.println("rBusy(" + which + ")");
    }

    public String rReadConsole(Rengine re, String prompt, int addToHistory) {
        System.out.print(prompt);
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
            String s = br.readLine();
            return (s == null || s.length() == 0) ? s : s + "\n";
        } catch (Exception e) {
            System.out.println("jriReadConsole exception: " + e.getMessage());
        }
        return null;
    }

    public void rShowMessage(Rengine re, String message) {
        System.out.println("rShowMessage \"" + message + "\"");
    }

    public String rChooseFile(Rengine re, int newFile) {
        FileDialog fd = new FileDialog(new Frame(), (newFile == 0) ? "Select a file" : "Select a new file", (newFile == 0) ? FileDialog.LOAD : FileDialog.SAVE);
        fd.show();
        String res = null;
        if (fd.getDirectory() != null) {
            res = fd.getDirectory();
        }
        if (fd.getFile() != null) {
            res = (res == null) ? fd.getFile() : (res + fd.getFile());
        }
        return res;
    }

    public void rFlushConsole(Rengine re) {
    }

    public void rLoadHistory(Rengine re, String filename) {
    }

    public void rSaveHistory(Rengine re, String filename) {
    }
}
