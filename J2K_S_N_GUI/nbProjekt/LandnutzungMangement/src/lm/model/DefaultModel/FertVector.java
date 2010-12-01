package lm.model.DefaultModel;

import java.util.ArrayList;
import lm.Componet.Vector.*;

/**
 *
 * @author Jens Wipprich ==> jens.wipprich (at) uni-jena.de
 */
public class FertVector implements LMFertVector {

    private int ID;
    private String fertnm;
    private double fminn;
    private double fminp;
    private double forgn;
    private double forgp;
    private double fnh3n;
    private double bactpdb;
    private double bactldb;
    private double bactddb;
    private String desc;

    public FertVector() {
        this.ID=0;
        this.fertnm="";
        this.fminn=0.0;
        this.fminp=0.0;
        this.forgn=0.0;
        this.forgp=0.0;
        this.fnh3n=0.0;
        this.bactpdb=0.0;
        this.bactldb=0.0;
        this.bactddb=0.0;
        this.desc="";
    }
    public FertVector(ArrayList<String> toVector){
        this.ID=Integer.parseInt(toVector.get(0));
        this.fertnm=toVector.get(1);
        this.fminn=Double.valueOf(toVector.get(2));
        this.fminp=Double.valueOf(toVector.get(3));
        this.forgn=Double.valueOf(toVector.get(4));
        this.forgp=Double.valueOf(toVector.get(5));
        this.fnh3n=Double.valueOf(toVector.get(6));
        this.bactpdb=Double.valueOf(toVector.get(7));
        this.bactldb=Double.valueOf(toVector.get(8));
        this.bactddb=Double.valueOf(toVector.get(9));
        this.desc=toVector.get(10);
    }

    public LMFertVector getVector() {
        return this;
    }

    public boolean isEmpty(){
        if (this.ID==0){
            return true;
        }else{
            return false;
        }
    }

    public Boolean CeckIfCorrect(){
        throw new UnsupportedOperationException("Not supported yet.");

    }

    //Getter And Setter Methods
    //Getter And Setter ---->ID
    public void setID(int i) {
        this.ID=i;
    }
    public int getID() {
        return this.ID;
    }
    //Getter And Setter ---->fertnm
    public void setfertnm(String s) {
        this.fertnm=s;
    }
    public String getfertnm() {
        return this.fertnm;
    }
    //Getter And Setter ---->fminn
    public void setfminn(double i) {
        this.fminn=i;
    }
    public double getfminn() {
        return this.fminn;
    }
    //Getter And Setter ---->fminp
    public void setfminp(double i) {
        this.fminp=i;
    }
    public double getfminp() {
        return this.fminp;
    }
    //Getter And Setter ---->forgn
    public void setforgn(double i) {
        this.forgn=i;
    }
    public double getforgn() {
        return this.forgn;
    }
    //Getter And Setter ---->forgp
    public void setforgp(double i) {
        this.forgp=i;
    }
    public double getforgp() {
        return this.forgp;
    }
    //Getter And Setter ---->fnh3n
    public void setfnh3n(double i) {
        this.fnh3n=i;
    }
    public double getfnh3n() {
        return this.fnh3n;
    }
    //Getter And Setter ---->bactpdb
    public void setbactpdb(double i) {
        this.bactpdb=i;
    }
    public double getbactpdb() {
        return this.bactpdb;
    }
    //Getter And Setter ---->bactldb
    public void setbactldb(double i) {
        this.bactldb=i;
    }
    public double getbactldb() {
        return this.bactldb;
    }
    //Getter And Setter ---->bactddb
    public void setbactddb(double i) {
        this.bactddb=i;
    }
    public double getbactddb() {
        return this.bactddb;
    }
    //Getter And Setter ---->desc
    public void setdesc(String s) {
        this.desc=s;
    }
    public String getdesc() {
        return this.desc;
    }

}
