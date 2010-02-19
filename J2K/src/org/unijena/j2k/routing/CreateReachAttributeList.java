/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.unijena.j2k.routing;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.StringTokenizer;
import jams.data.*;
import jams.model.*;
import java.util.ArrayList;
import java.awt.Point;
import javax.naming.directory.NoSuchAttributeException;
/**
 *
 * @author sa63kul
 */
public class CreateReachAttributeList extends JAMSComponent {
 
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "The reach collection"
            )
            public JAMSEntityCollection entities;
    
 
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "attribute slope"
            )
            public JAMSDouble slope;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "attribute width"
            )
            public JAMSDouble width;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "attribute roughness"
            )
            public JAMSDouble roughness;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.RUN,
            description = ""
            )
            public JAMSEntity information;
     @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "Reach statevar RG2 storage"
            )
            public JAMSDouble reachID; 
      
    
    public void init()throws JAMSEntity.NoSuchAttributeException
{

}
    
@SuppressWarnings("unchecked")    
public void run()throws JAMSEntity.NoSuchAttributeException{
 
// JAMSEntity entity = entities.getCurrent();
 double reachID=this.reachID.getValue();
 double width = this.width.getValue();
 double slope = this.slope.getValue();
 double rough = this.roughness.getValue();
 
 ArrayList<ArrayList<Point>> actreachlist = new ArrayList<ArrayList<Point>>();
        
        try
        {actreachlist=(ArrayList<ArrayList<Point>>)information.getObject("reachlist");}
        catch (Exception e) {
	    System.out.println(e.toString());
	}
  
 double [][] actreachinfo =null; 
 
        try
        {actreachinfo=(double[][])information.getObject("reachinfo");}
        catch (Exception e) {
	    System.out.println(e.toString());
	}
 
actreachinfo[0][(int)reachID]=reachID;
actreachinfo[1][(int)reachID]=slope; 
actreachinfo[2][(int)reachID]=width;
actreachinfo[3][(int)reachID]=rough;


information.setObject("reachinfo", actreachinfo);

}

   
public void cleanup() 
{
}
}
