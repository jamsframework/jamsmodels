/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package lm.Componet.Vector;

/**
 *
 * @author Jens Wipprich ==> jens.wipprich (at) uni-jena.de
 */
public interface LMArableID {


    public int ID=0;
    public int Stufe=0;
    public int maxStufe=0;

    public Boolean isEmpty();

    public Boolean isLast();


    //Getter And Setter MEthods
    //Getter And Setter ------>ID
    public void setID(int i);
    public int getID();
    //Getter And Setter ------>Stufe
    public void setStufe(int i);
    public int getStufe();
    //Getter And Setter ------>maxStufe
    public void setMaxStufe(int i);
    public int getMaxStufe();

}
