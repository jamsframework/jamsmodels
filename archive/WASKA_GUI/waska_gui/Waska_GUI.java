/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package waska_gui;
import java.io.*;
import javax.swing.JFrame;
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

            public void run() {
               
               String baseDir = new File("data/WASKA_Wipfra").getAbsolutePath();
               String baseModel = new File("data/WASKA_Wipfra/j2k_wipfra_100_starter.jam").getAbsolutePath();
               
                Model model = new Model(baseDir, baseModel);
                JFrame f = new WaskaFrame(model);
                f.setIconImage(new javax.swing.ImageIcon(getClass().getResource("WASKA_logo.png")).getImage());
                f.setVisible(true);
            }
        });
    }
    
}
