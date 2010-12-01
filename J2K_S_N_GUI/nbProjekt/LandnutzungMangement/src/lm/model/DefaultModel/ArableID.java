package lm.model.DefaultModel;

import java.util.ArrayList;
import lm.Componet.Vector.LMArableID;
/**
 *
 * @author Jens Wipprich ==> jens.wipprich (at) uni-jena.de
 */
public class ArableID implements LMArableID {

    private int ID;
    private int Stufe;
    private int maxStufe;


    public ArableID(){
        this.ID=0;
        this.Stufe=0;
        this.maxStufe=0;
    }

    public ArableID(ArrayList<String> a){
        this.ID=Integer.parseInt(a.get(0));
        this.Stufe=Integer.parseInt(a.get(1));
        this.maxStufe=Integer.parseInt(a.get(2));
    }

    public Boolean isEmpty() {
        if(this.ID==0){
            return true;
        }else{
            return false;
        }
    }

    public Boolean isLast() {
        if (this.Stufe==this.maxStufe){
            return true;
        }else{
            return false;
        }
    }
    public Boolean CeckIfCorrect() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    //Getter And Setter MEthods
    //Getter And Setter ------>ID
    public void setID(int i) {
        this.ID=i;
    }
    public int getID() {
        return this.ID;
    }
    //Getter And Setter ------>Stufe
    public void setStufe(int i) {
        this.Stufe=i;
    }
    public int getStufe() {
        return this.Stufe;
    }
    //Getter And Setter ------>MaxStufe
    public void setMaxStufe(int i) {
        this.maxStufe=i;
    }
    public int getMaxStufe() {
        return this.maxStufe;
    }
}
