/*
 * DataWriter.java
 *
 * Created on 5. Juli 2007, 12:44
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.unijena.predictionnet;
import org.unijena.jams.data.*;
import org.unijena.jams.model.*;
import java.io.*;
import Jama.*;
import Jama.Matrix;
import jams.components.optimizer.SCE.SCE_Comparator;
import java.util.Arrays;
import java.util.Random;
import java.util.StringTokenizer;
import org.unijena.jams.io.GenericDataWriter;

/**
 *
 * @author Christian(web)
 */
public class DataWriter {
    
    /** Creates a new instance of DataWriter */
    public DataWriter() {
    }
    
    public void write(double data[][],String file) {
	BufferedWriter writer = null;
	try {
	    writer = new BufferedWriter(new FileWriter(file,true));	    
	}
	catch (Exception e) {
	    System.out.println("Could not open result file, becauce:" + e.toString());
	    System.out.println("results won't be saved");
	}
	    
	for (int i=0;i<data.length;i++) {
	    String line = "";
	    for (int j=0;j<data[i].length;j++) {
		line += data[i][j] + "\t";
	    }
	    line += "\n";
	    try {
		writer.write(line);		    
		writer.flush();
	    }
	    catch(Exception e) {
		System.out.println("could not write, because: " + e.toString());
	    }
	}
	try {
	    writer.close();
	}
	catch(Exception e) {
	    System.out.println("GP - Error" + e.toString());
	}
    }
}
