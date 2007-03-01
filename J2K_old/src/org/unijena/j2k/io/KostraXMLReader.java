/*
 * KostraXMLReader.java
 *
 * Created on 25. Januar 2007, 17:53
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.unijena.j2k.io;

import org.unijena.j2k.*;
import org.unijena.jams.data.*;
import org.unijena.jams.model.*;
import java.util.*;
import org.unijena.jams.JAMS;
import org.unijena.jams.io.XMLIO;
import javax.xml.parsers.*;
import java.lang.reflect.*;
import org.xml.sax.*;
import org.w3c.dom.*;
/**
 *
 * @author Christian(web)
 */
public class KostraXMLReader extends JAMSComponent {

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.INIT,
            description = "Workspace directory name"
            )
            public JAMSString dirName;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.INIT,
            description = "HRU parameter file name"
            )
            public JAMSString KostraXMLFile;
        
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            update = JAMSVarDescription.UpdateType.INIT,
            description = "Collection of hru objects"
            )
            public JAMSEntity table;
    
    int numDEntries = 0, numAEntries = 0;
    HashMap<Integer,Map<Integer,JAMSDouble>>	tmpTable;
    HashMap<Integer,JAMSDouble>			tmpHeaderD;
    HashMap<Integer,JAMSDouble>			tmpHeaderA;
    
    JAMSDouble Table[][];
    JAMSDouble HeaderD[];
    JAMSDouble HeaderA[];
    
    public void init() throws JAMSEntity.NoSuchAttributeException {        	
	Document dwdData = null;
	
	tmpTable = new HashMap<Integer,Map<Integer,JAMSDouble>>();
	tmpHeaderD = new HashMap<Integer,JAMSDouble>();
	tmpHeaderA = new HashMap<Integer,JAMSDouble>();
	//read document
	try {
	    dwdData = XMLIO.getDocument(dirName + "/" + KostraXMLFile);
	}
	catch (Exception e) {	    
	    this.getModel().getRuntime().println("Can't read XML because:" + e.toString());
	}
	//iterate over all childs an extract information
	Node node = dwdData.getFirstChild();
	
	if (node == null) {
	    this.getModel().getRuntime().println("XML - Document contains nothing");
	}
	while (node != null) {
	    ProcessNode(node);
	    node = node.getNextSibling();
	}
	//convert hashmaps to arrays 
	Table = new JAMSDouble[numDEntries][numAEntries];
	HeaderD = new JAMSDouble[numDEntries];
	HeaderA = new JAMSDouble[numAEntries];
	
	for (int i=0;i<numDEntries;i++) {
	    HeaderD[i] = this.tmpHeaderD.get(new Integer(i));
	    for (int j=0;j<numAEntries;j++) {
		Table[i][j] = this.tmpTable.get(new Integer(i)).get(new Integer(j));
	    }
	}
	for (int i=0;i<numAEntries;i++) {
	    HeaderA[i] = this.tmpHeaderA.get(new Integer(i));
	}
	//save arrays in output entity
	table.setObject("table",Table);
	table.setObject("HeaderA",HeaderA);
	table.setObject("HeaderD",HeaderD);
    }
    
    private void ProcessNode(Node node) {
	//D nodes contain interessting data
	if (node.getNodeName().compareTo("D") == 0 && node.getParentNode().getNodeName().compareTo("Daten") == 0) {
	    ProcessDNode(node);
	    return;
	}
	//skip all other nodes
	Node childnode = node.getFirstChild();
	
	while (childnode != null) {
	    ProcessNode(childnode);
	    childnode = childnode.getNextSibling();
	}
    }
    
    private void ProcessDNode(Node node) {
	Element element = (Element)node;
	//this is a new row in our table --> add to header
	JAMSDouble dauer = new JAMSDouble(new Double(element.getAttribute("dauer")).doubleValue());
			
	tmpHeaderD.put(new Integer(numDEntries),dauer);
	tmpTable.put(new Integer(numDEntries),new HashMap<Integer,JAMSDouble>());
	
	Node childnode = node.getFirstChild();
	
	int curAEntry = 0;
	//iterate over children
	while (childnode != null) {	    
	    //T nodes contain interessting data
	    if (childnode.getNodeName().compareTo("T") == 0) {		
		if (numDEntries == 0) {
		    element = (Element)childnode;
		    JAMSDouble a = new JAMSDouble(new Double(element.getAttribute("a")).doubleValue());
		    tmpHeaderA.put(new Integer(curAEntry),a);		    
		}
		ProcessTNode(childnode,numDEntries,curAEntry);
		curAEntry++;
	    }	    
	    childnode = childnode.getNextSibling();
	}
	//update number of cols/rows
	if (numAEntries == 0)
	    this.numAEntries = curAEntry;
	else if (numAEntries != curAEntry) {
	    this.getModel().getRuntime().println("Keine Tabelle in XML File!!");
	}
	this.numDEntries++;
    }
    
    private void ProcessTNode(Node node,int DIndex,int AIndex) {
	Element element = (Element)node;
	JAMSDouble a = new JAMSDouble(new Double(element.getAttribute("a")).doubleValue());
	
	Node childnode = node.getFirstChild();
	
	while (childnode != null) {
	    if (childnode.getNodeName().compareTo("hN") == 0) {		
		this.tmpTable.get(DIndex).put(new Integer(AIndex),
			new JAMSDouble(new Double(childnode.getTextContent()).doubleValue()));
	    }	    
	    childnode = childnode.getNextSibling();
	}
    }
}
