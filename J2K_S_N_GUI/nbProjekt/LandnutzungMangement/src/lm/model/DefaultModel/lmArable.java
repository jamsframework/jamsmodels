/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package lm.model.DefaultModel;

import java.util.ArrayList;
import lm.Componet.Vector.LMArableVector;
import lm.Componet.Vector.LMFertVector;
import lm.Componet.Vector.LMTillVector;
import lm.Componet.Vector.LMArableID;

/**
 *
 * @author Jens Wipprich ==> jens.wipprich (at) uni-jena.de
 */
public class lmArable implements LMArableVector {

    private ArableID AID;
    private CropVector CID;
    private String Date;
    private TillVector TID;
    private FertVector FID;
    private Double FAmount;
    private Boolean PLANT;
    private Boolean HARVEST;
    private Double FRACHARV;
    
    
    public lmArable (ArrayList<String> a){
        
    }

    public lmArable(){


    }
    public LMArableVector getVector() {
        return this;
    }

    public Boolean CeckIfCorrect() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Boolean isEmpty() {
        if (CID.getID()==0){
            return true;
        }else{
            return false;
        }
    }



    public void setAID(LMArableID ID){
        this.AID=ID;
    }
    public LMArableID getAID(){
        return this.AID;
    }
    public void setCID(CropVector Vector) {
        this.CID=Vector;
    }

    public CropVector getCID() {
        return this.CID;
    }

    public void setDate(String s) {
        this.Date=s;
    }

    public String getDate() {
        return this.Date;
    }

    public void setTID(TillVector Vector) {
        this.TID=Vector;
    }

    public TillVector getTID() {
        return this.TID;
    }

    public void setFID(FertVector Vector) {
        this.FID=Vector;
    }

    public FertVector getFID() {
        return this.FID;
    }

    public void setFAmount(Double d) {
        this.FAmount=d;
    }

    public Double getFAmount() {
        return this.FAmount;
    }

    public void setPLANT(Boolean b) {
        this.PLANT=b;
    }

    public Boolean getPLANT() {
        return this.PLANT;
    }

    public void setHARVEST(Boolean b) {
        this.HARVEST=b;
    }

    public Boolean getHARVEST() {
        return this.HARVEST;
    }

    public void setFRACHARV(Double d) {
        this.FRACHARV=d;
    }

    public Double getFRACHARV() {
        return this.FRACHARV;
    }



}
