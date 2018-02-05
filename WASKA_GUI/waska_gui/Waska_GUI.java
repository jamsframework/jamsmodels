/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package waska_gui;
import java.io.*;
/**
 *
 * @author manni
 */
public class Waska_GUI {

    /**
     * @param args the command line arguments
     */
    
     
    public static void main(String[] args) {
        java.awt.EventQueue.invokeLater(new Runnable() {
            String current = "";
            String currentbase = "";
            public void run() {
               try{
                current =  new java.io.File(".").getCanonicalPath(); //Problem with \\ only 2 need 4 backslashes
                
                current = "C:\\\\jamsprojects\\\\Waska_GUI";
                currentbase =  current + "\\\\..\\\\..\\\\JAMSApp_neu";
                
                System.out.println("Aufruf Pfad:" + current);
                
               }catch (IOException ex){
                System.out.println("Pfad_problem mit Pfad:" + current);
               }
                Model model = new Model(currentbase,currentbase + "\\\\j2k_wipfra_100_starter.jam");
                new WaskaFrame(model).setVisible(true);
            }
        });
    }
    
}
