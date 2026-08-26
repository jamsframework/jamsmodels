/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package waska_gui;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author manni
 */
public class Model {
 
    private String jamFile;
    private String currentbase;
    private Map<String, Parameter> parameters = new HashMap();
    private List<Parameter> pList = new ArrayList();
    
    public Model(String Path, String jamFile) {
        this.jamFile = jamFile;
        this.currentbase = Path;
        Parameter p;
        
        p = new Parameter();
        p.setName("workspace");
        p.setDefaultValue(currentbase + "\\\\JAMS-Wipfra_CCC_RCP85");
        parameters.put(p.getName(), p);
        pList.add(p);
        
        p = new Parameter();
        p.setName("time");
        p.setDefaultValue("2070-11-01 07:30 2099-10-31 07:30 6 1");
        parameters.put(p.getName(), p);
        pList.add(p);

        p = new Parameter();
        p.setName("Land");
        p.setDefaultValue("1.5");
        parameters.put(p.getName(), p);
        pList.add(p);        
        
        p = new Parameter();
        p.setName("Stadt");
        p.setDefaultValue("0.5");
        parameters.put(p.getName(), p);
        pList.add(p);
        
        p = new Parameter();
        p.setName("Acker");
        p.setDefaultValue("63");
        parameters.put(p.getName(), p);
        pList.add(p);
        
        p = new Parameter();
        p.setName("Gruen");
        p.setDefaultValue("12");
        parameters.put(p.getName(), p);
        pList.add(p);
        
        p = new Parameter();
        p.setName("Laub");
        p.setDefaultValue("5");
        parameters.put(p.getName(), p);
        pList.add(p);
        
        p = new Parameter();
        p.setName("Nadel");
        p.setDefaultValue("18");
        parameters.put(p.getName(), p);
        pList.add(p);
        
        p = new Parameter();
        p.setName("Irriprop");
        p.setDefaultValue("0");
        parameters.put(p.getName(), p);
        pList.add(p);
        
        p = new Parameter();
        p.setName("Res_activ");
        p.setDefaultValue("false");
        parameters.put(p.getName(), p);
        pList.add(p);
        
        p = new Parameter();
        p.setName("Res_vol");
        p.setDefaultValue("5320000000");
        parameters.put(p.getName(), p);
        pList.add(p);
        
        p = new Parameter();
        p.setName("Res_min_flux");
        p.setDefaultValue("12000000");
        parameters.put(p.getName(), p);
        pList.add(p);
        
        p = new Parameter();
        p.setName("Res_min_fill");
        p.setDefaultValue("0.65");
        parameters.put(p.getName(), p);
        pList.add(p);
        
        p = new Parameter();
        p.setName("Res_max_fill");
        p.setDefaultValue("0.9");
        parameters.put(p.getName(), p);
        pList.add(p);

        p = new Parameter();
        p.setName("Res_opt_fill");
        p.setDefaultValue("0.85");
        parameters.put(p.getName(), p);
        pList.add(p);

        p = new Parameter();
        p.setName("settl_water_use");
        p.setDefaultValue("1682400");
        parameters.put(p.getName(), p);
        pList.add(p);
        
    }

    /**
     * @return the jamFile
     */
    public String getJamFile() {
        return jamFile;
    }

    /**
     * @param jamFile the jamFile to set
     */
    public void setJamFile(String jamFile) {
        this.jamFile = jamFile;
    }

    /**
     * @return the parameters
     */
    public Map<String, Parameter> getParameters() {
        return parameters;
    }

    /**
     * @return the pList
     */
    public List<Parameter> getPList() {
        return pList;
    }

    /**
     * @return the currentbase
     */
    public String getCurrentbase() {
        return currentbase;
    }

    /**
     * @param currentbase the currentbase to set
     */
    public void setCurrentbase(String currentbase) {
        this.currentbase = currentbase;
    }
    
   
}
