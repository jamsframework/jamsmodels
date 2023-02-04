/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

import jams.JAMS;
import jams.JAMSProperties;
import jams.SystemProperties;
import jams.runtime.JAMSRuntime;
import jams.runtime.StandardRuntime;
import jams.tools.StringTools;
import jams.workspace.InvalidWorkspaceException;
import jams.workspace.JAMSWorkspace;
import jams.workspace.stores.InputDataStore;
import java.io.File;
import java.io.IOException;
import java.util.Observable;
import java.util.Observer;
import org.rosuda.JRI.REXP;
import org.rosuda.JRI.RVector;
import org.rosuda.JRI.Rengine;

/**
 *
 * @author nsk
 */
public class IHA_DischargeIndicators {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws IOException {

        SystemProperties properties = JAMSProperties.createProperties();
        properties.load(args[0]);
        String[] libs = StringTools.toArray(properties.getProperty("libs", ""), ";");

        JAMSRuntime runtime = new StandardRuntime(properties);
        runtime.setDebugLevel(JAMS.VERBOSE);
        runtime.addErrorLogObserver(new Observer() {

            @Override
            public void update(Observable o, Object arg) {
                System.out.print(arg);
            }
        });
        runtime.addInfoLogObserver(new Observer() {

            @Override
            public void update(Observable o, Object arg) {
                //System.out.print(arg);
            }
        });

        JAMSWorkspace ws;
        try {
            ws = new JAMSWorkspace(new File(args[1]), runtime, false);
            ws.init();
        } catch (InvalidWorkspaceException iwe) {
            System.out.println(iwe.getMessage());
            return;
        }

        InputDataStore store = ws.getInputDataStore(args[2]);
//        TSDumpProcessor asciiConverter = new TSDumpProcessor();
//        System.out.println(asciiConverter.toASCIIString((TSDataStore) store));

        Rengine re = new Rengine(null, false, null);
        re.waitForR();
        REXP x;
        //re.eval("print(1:10/3)");
        re.eval("library(flowregime)");
        System.out.println("dat_zoo <- read.zoo(\"" + args[3] + "\", index.column = 0, sep = \",\", format = \"%d.%m.%Y\")");
        re.eval("dat_zoo <- read.zoo(\"" + args[3] + "\", index.column = 0, sep = \",\", format = \"%d.%m.%Y\")");
        re.eval("dat_xts <- as.xts(dat_zoo)");
        x = re.eval("build_EFC_thresholds(dat_xts, method = \"advanced\")");
        RVector v = x.asVector();
        for (int i = 0; i < v.size(); i++) {
            System.out.print(v.getNames().get(i) + ": ");
            System.out.println(v.at(i).asDouble());
        }
        re.end();
    }
}
