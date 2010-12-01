package lm.model.DefaultModel;

import java.util.ArrayList;
import lm.Componet.Vector.LMArableVector;
import lm.Componet.Vector.LMCropVector;
import lm.Componet.Vector.LMFertVector;
import lm.Componet.Vector.LMTillVector;
import lm.Componet.Vector.LMArableID;

/**
 *
 * @author Jens Wipprich ==> jens.wipprich (at) uni-jena.de
 */
public class ArableVector implements LMArableVector {

    private LMArableID AID;
    private LMCropVector CID;
    private String Date;
    private LMTillVector TID;
    private LMFertVector FID;
    private Double FAmount;
    private Boolean PLANT;
    private Boolean HARVEST;
    private Double FRACHARV;
    

    public ArableVector(){
    this.AID=new ArableID();
    this.CID=new CropVector();
    this.Date=new String();
    this.TID=new TillVector();
    this.FID=new FertVector();
    this.FAmount=null;
    this.PLANT=null;
    this.HARVEST=null;
    this.FRACHARV=null;
    }

    public ArableVector (ArrayList<Object> a){
    this.AID=(LMArableID) a.get(0);
    this.CID=(LMCropVector) a.get(1);
    this.Date=(String) a.get(2);
    this.TID=(LMTillVector) a.get(3);
    this.FID=(LMFertVector) a.get(4);
    this.FAmount=(Double) a.get(5);
    this.PLANT=(Boolean) a.get(6);
    this.HARVEST=(Boolean) a.get(7);
    this.FRACHARV=(Double) a.get(8);
    }

    public LMArableVector getVector() {
        return this;
    }

    public Boolean CeckIfCorrect() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Boolean isEmpty() {
        if (AID.isEmpty()){
            return true;
        }else{
            return false;
        }
    }
    //Getter And Setter Methods
    //Getter And Setter ------>AID
    public void setAID(LMArableID ID){
        this.AID=ID;
    }
    public LMArableID getAID(){
        return this.AID;
    }
    //Getter And Setter ------>CID
    public void setCID(LMCropVector Vector) {
        this.CID=Vector;
    }
    public LMCropVector getCID() {
        return this.CID;
    }
    //Getter And Setter ------>Date
    public void setDate(String s) {
        this.Date=s;
    }
    public String getDate() {
        return this.Date;
    }
    //Getter And Setter ------>TID
    public void setTID(LMTillVector Vector) {
        this.TID=Vector;
    }
    public LMTillVector getTID() {
        return this.TID;
    }
    //Getter And Setter ------>FID
    public void setFID(LMFertVector Vector) {
        this.FID=Vector;
    }
    public LMFertVector getFID() {
        return this.FID;
    }
    //Getter And Setter ------>FAmount
    public void setFAmount(Double d) {
        this.FAmount=d;
    }
    public Double getFAmount() {
        return this.FAmount;
    }
    //Getter And Setter ------>PLANT
    public void setPLANT(Boolean b) {
        this.PLANT=b;
    }
    public Boolean getPLANT() {
        return this.PLANT;
    }
    //Getter And Setter ------>HARVEST
    public void setHARVEST(Boolean b) {
        this.HARVEST=b;
    }
    public Boolean getHARVEST() {
        return this.HARVEST;
    }
    //Getter And Setter ------>FRACHARV
    public void setFRACHARV(Double d) {
        this.FRACHARV=d;
    }
    public Double getFRACHARV() {
        return this.FRACHARV;
    }
}
