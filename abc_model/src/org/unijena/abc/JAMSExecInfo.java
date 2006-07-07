/*
 * JAMSExecInfo.java
 * Created on 1. Dezember 2005, 19:46
 *
 * This file is part of JAMS
 * Copyright (C) 2005 S. Kralisch and P. Krause
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA
 *
 */

package org.unijena.abc;

import org.unijena.jams.data.*;
import org.unijena.jams.model.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Observable;
import java.util.Observer;
import javax.swing.*;
import org.unijena.jams.JAMS;

/**
 *
 * @author S. Kralisch
 */

@JAMSComponentDescription(
        title="JAMS execution info frame",
        author="Sven Kralisch",
        date="17. June 2006",
        description="This visual component creates a frame with progress bar and log informations")
        public class JAMSExecInfo extends JAMSGUIComponent {
    
    
    private JPanel progressPanel;
    private JButton cancelButton;
    private JScrollPane scrollPanel;
    private JTextArea logArea;
    private int counter;
    private JPanel panel;
    
    
    public JPanel getPanel() {
        createPanel();
        return panel;
    }
    
    public void init() {
        
    }
    
    private void createPanel() {
        
        panel = new JPanel();
        panel.setLayout(new BorderLayout());
        
        progressPanel = new JPanel();
        progressPanel.setLayout(new BorderLayout());
        
        
        cancelButton = new JButton();
        //cancelButton.setText("Cancel");
        cancelButton.setPreferredSize(new Dimension(40, 0));
        cancelButton.setIcon(new ImageIcon(getClass().getResource("/resources/images/ModelStop.png")));
        cancelButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                getModel().getRuntime().sendHalt();
            }
        });
//        progressPanel.add(cancelButton, BorderLayout.EAST);
        
        progressPanel.setPreferredSize(new Dimension(0, 40));
        panel.add(progressPanel, BorderLayout.NORTH);
        
        scrollPanel = new JScrollPane();
        logArea = new JTextArea();
        logArea.setColumns(20);
        logArea.setRows(5);
        logArea.setLineWrap(false);
        logArea.setEditable(false);
        logArea.setFont(JAMS.STANDARD_FONT);
        scrollPanel.setViewportView(logArea);
        
        panel.add(scrollPanel, BorderLayout.CENTER);
        
        logArea.append(this.getModel().getRuntime().getInfoLog());
        logArea.append(this.getModel().getRuntime().getErrorLog());
        
        this.getModel().getRuntime().addInfoLogObserver(new Observer() {
            public void update(Observable obs, Object obj) {
                updateLog(obj.toString());
            }
        });
        this.getModel().getRuntime().addErrorLogObserver(new Observer() {
            public void update(Observable obs, Object obj) {
                updateLog(obj.toString());
            }
        });
    }
    
    private void updateLog(final String value) {
        Runnable logUpdate = new Runnable() {
            public void run() {
                logArea.append(value);
                logArea.setCaretPosition(logArea.getText().length());
            }
        };
        SwingUtilities.invokeLater(logUpdate);
    }
    
    public void run() {
        
    }
    
    public void cleanup() {
        cancelButton.setEnabled(false);
        //panel.dispose();
    }
    
}
