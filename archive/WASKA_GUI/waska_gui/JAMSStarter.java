/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package waska_gui;

/**
 *
 * @author manni
 */
public class JAMSStarter {

    private Model model;

    public JAMSStarter(Model model) {
        this.model = model;
    }

    public void start() {

        String jamFile = model.getJamFile();
        String parameterList = "";
        for (Parameter p : model.getPList()) {
            parameterList += p.getValue() + ";";
            System.out.println(p.getValue());
        }

        String args[] = {"-c", "default.jap", "-m", jamFile, "-p", parameterList};
        jamsui.launcher.JAMSui.main(args);

    }

}
