package org.rosuda.JGR;
//
//  RDataFileDialog.java
//  JGR
//
//  Created by Markus Helbig on Fri Mar 05 2004.
//  Copyright (c) 2004 __MyCompanyName__. All rights reserved.
//

import java.awt.*;
import java.awt.event.*;
import java.beans.*;
import java.io.*;
import java.util.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;

import org.rosuda.JGR.toolkit.*;
import org.rosuda.ibase.*;
import org.rosuda.ibase.toolkit.*;


public class RDataFileDialog extends JDialog implements ActionListener, ItemListener, PropertyChangeListener {

    private GridBagLayout layout = new GridBagLayout();
    private GridBagLayout layout2 = new GridBagLayout();
    private JFileChooser fileChooser = new JFileChooser();
    private JPanel sepPanel = new JPanel();
    private JTextField dataName = new JTextField();
    private JTextField otherSeps = new JTextField();
    private ButtonGroup sepButtons = new ButtonGroup();
    private JCheckBox header = new JCheckBox("",true);
    private JRadioButton def = new JRadioButton("Default",true);
    private JRadioButton tab = new JRadioButton("Tab");
    private JRadioButton comma = new JRadioButton(",");
    private JRadioButton semicol = new JRadioButton(";");
    private JRadioButton pipe = new JRadioButton("|");
    private JRadioButton other = new JRadioButton("Others");
    /*private JCheckBox def = new JCheckBox("Default",true);
    private JCheckBox tab = new JCheckBox("Tab");
    private JCheckBox comma = new JCheckBox(",");
    private JCheckBox semicol = new JCheckBox(";");
    private JCheckBox pipe = new JCheckBox("|");
    private JCheckBox other = new JCheckBox("Others");*/

    private String seps = "";
    private boolean useHeader = true;

    private Dimension screenSize = Common.getScreenRes();

    public RDataFileDialog(Frame f,String directory) {
        super(f,"Load DataFile",true);

        sepButtons.add(def);
        sepButtons.add(tab);
        sepButtons.add(comma);
        sepButtons.add(semicol);
        sepButtons.add(pipe);
        sepButtons.add(other);
        header.addItemListener(this);
        def.addItemListener(this);
        tab.addItemListener(this);
        comma.addItemListener(this);
        semicol.addItemListener(this);
        pipe.addItemListener(this);
        other.addItemListener(this);

        dataName.setMinimumSize(new Dimension(200,25));
        dataName.setMaximumSize(new Dimension(400,25));
        otherSeps.setEditable(false);
        otherSeps.setMinimumSize(new Dimension(100,50));
        otherSeps.setMaximumSize(new Dimension(200,50));

        fileChooser.addActionListener(this);
        fileChooser.addPropertyChangeListener(this);
        fileChooser.setPreferredSize(new Dimension(380,300));
        if (directory != null && new File(directory).exists()) fileChooser.setCurrentDirectory(new File(directory));

        this.getContentPane().setLayout(layout);

        sepPanel.setLayout(layout2);

        sepPanel.add(def,  new GridBagConstraints(0, 1, 1, 1, 0.0, 1.0
            , GridBagConstraints.WEST, GridBagConstraints.NONE,
            new Insets(1, 5, 1, 5), 0, 0));
        sepPanel.add(tab,  new GridBagConstraints(0, 2, 1, 1, 0.0, 1.0
            , GridBagConstraints.WEST, GridBagConstraints.NONE,
            new Insets(1, 5, 1, 5), 0, 0));
        sepPanel.add(comma,  new GridBagConstraints(0, 3, 1, 1, 0.0, 1.0
            , GridBagConstraints.WEST, GridBagConstraints.NONE,
            new Insets(1, 5, 1, 5), 0, 0));
        sepPanel.add(other,  new GridBagConstraints(0, 4, 1, 1, 0.0, 1.0
            , GridBagConstraints.WEST, GridBagConstraints.NONE,
            new Insets(1, 5, 1, 5), 0, 0));
        sepPanel.add(semicol,  new GridBagConstraints(1,2, 1, 1, 0.0, 1.0
            , GridBagConstraints.WEST, GridBagConstraints.NONE,
            new Insets(1, 5, 1, 5), 0, 0));
        sepPanel.add(pipe,  new GridBagConstraints(1, 3, 1, 1, 0.0, 1.0
            , GridBagConstraints.WEST, GridBagConstraints.NONE,
            new Insets(1, 5, 1, 5), 0, 0));
        sepPanel.add(otherSeps,  new GridBagConstraints(1, 4, 2, 1, 1.0, 1.0
            , GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL,
            new Insets(1, 5, 1, 50), 0, 0));


        this.getContentPane().add(fileChooser,  new GridBagConstraints(0, 0, 3, 1, 1.0, 5.0
            , GridBagConstraints.CENTER, GridBagConstraints.BOTH,
            new Insets(1, 5, 1, 5), 0, 0));
        this.getContentPane().add(new JLabel("Name :"),  new GridBagConstraints(0, 1, 1, 1, 0.0, 1.0
            , GridBagConstraints.WEST, GridBagConstraints.NONE,
            new Insets(1, 5, 1, 5), 0, 0));
        this.getContentPane().add(dataName,  new GridBagConstraints(1, 1, 2, 1, 1.0, 1.0
            , GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL,
            new Insets(1, 5, 1, 50), 0, 0));
        this.getContentPane().add(new JLabel("Header :"),  new GridBagConstraints(0, 2, 1, 1, 0.0, 1.0
            , GridBagConstraints.WEST, GridBagConstraints.NONE,
            new Insets(1, 5, 1, 5), 0, 0));
        this.getContentPane().add(header,  new GridBagConstraints(1, 2, 1, 1, 1.0, 1.0
            , GridBagConstraints.WEST, GridBagConstraints.BOTH,
            new Insets(1, 5, 1, 50), 0, 0));
        this.getContentPane().add(new JLabel("Separators :"),  new GridBagConstraints(0, 3, 1, 1, 0.0, 1.0
            , GridBagConstraints.NORTHWEST, GridBagConstraints.NONE,
            new Insets(1, 5, 1, 5), 0, 0));
        this.getContentPane().add(sepPanel,  new GridBagConstraints(1, 3, 2, 1, 0.0, 1.0
            , GridBagConstraints.WEST, GridBagConstraints.BOTH,
            new Insets(1, 5, 1, 5), 0, 0));

        this.setSize(new Dimension(450, 500));
        this.setLocation((screenSize.width-450)/2,(screenSize.height-500)/2);
        if (Preferences.isMac) this.setResizable(false);
        /*this.addComponentListener( new java.awt.event.ComponentAdapter() {
            public void componentResized(java.awt.event.ComponentEvent evt) {
                fileChooser.setSize((int) (getWidth()-10),(int) (getHeight()-150));
            }
        });*/
        this.addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                dispose();
            }
        });
        this.show();
    }

    public void loadFile() {
        if (fileChooser.getSelectedFile() != null) {
            RConsole.directory = fileChooser.getCurrentDirectory().getAbsolutePath()+File.separator;
            String file = fileChooser.getSelectedFile().toString();
            if (System.getProperty("os.name").startsWith("Windows")) file = file.replace('\\','/');
            String cmd = dataName.getText().trim()+" <- read.table(\""+file+"\",header="+(useHeader?"T":"F")+",sep=\""+seps.trim()+"\")";
            JGR.MAINRCONSOLE.execute(cmd);
        }
        dispose();
    }

    public void actionPerformed(ActionEvent e) {
        String cmd = e.getActionCommand();
        System.out.println(cmd);
        if (cmd == "ApproveSelection") loadFile();
        else if (cmd == "CancelSelection") dispose();
    }

    public void itemStateChanged(ItemEvent e) {
        Object source = e.getItemSelectable();

        if (source == other) otherSeps.setEditable(!otherSeps.isEditable());
        else if (source == header) useHeader = !useHeader;
        else if (source == tab ) {
            if (seps.indexOf("\\t") == -1) seps += "\\t";
            else seps = seps.replaceAll("\\\\t","");
        }
        else if (source == comma ) {
            if (seps.indexOf(",") == -1) seps += ",";
            else seps = seps.replaceAll(",","");
        }
        else if (source == semicol ) {
            if (seps.indexOf(";") == -1) seps += ";";
            else seps = seps.replaceAll(";","");
        }
        else if (source == pipe ) {
            if (seps.indexOf("|") == -1) seps += "|";
            else seps = seps.replaceAll("\\|","");
        }
    }

    public void mouseClicked(MouseEvent e) {
    }

    public void mouseEntered(MouseEvent e) {
    }

    public void mousePressed(MouseEvent e) {
    }

    public void mouseReleased(MouseEvent e) {
    }

    public void mouseExited(MouseEvent e) {
    }

    public void propertyChange(PropertyChangeEvent e) {
        File file = fileChooser.getSelectedFile();
        if(file!=null && !file.isDirectory()) dataName.setText(file.getName().replaceAll("\\..*", ""));
        else dataName.setText(null);
    }

}
