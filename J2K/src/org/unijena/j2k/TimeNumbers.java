/*
 * TimeNumbers.java
 *
 * Created on 17. Juli 2006, 17:21
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.unijena.j2k;

import org.unijena.jams.data.JAMSCalendar;

/**
 *
 * @author c0krpe
 */
public class TimeNumbers {
    
    /** Creates a new instance of TimeNumbers */
    public TimeNumbers() {
    }
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        JAMSCalendar time = new JAMSCalendar();
        System.out.println("seconds: " + time.SECOND);
        System.out.println("Minutes: " + time.MINUTE);
        System.out.println("Hours: " + time.HOUR);
        System.out.println("Days: " + time.DATE);
        System.out.println("Months: " + time.MONTH);
        System.out.println("Years: " + time.YEAR);
        
    }
    
}
