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
import javax.swing.*;

import org.rosuda.ibase.*;


public class JGRDataFileDialog extends JDialog implements ActionListener, ItemListener, PropertyChangeListener {

    private JFileChooser fileChooser = new JFileChooser();
    private JTextField dataName = new JTextField();
    private JTextField otherSeps = new JTextField();
    private JCheckBox header = new JCheckBox("",true);
    private JCheckBox attach = new JCheckBox("",false);

    private JComboBox sepsBox = new JComboBox(new String[] {"Default","\\t",",",";","|",""});
    private String[] seps = new String[] {"","\\t",",",";","|"};

    private JComboBox quoteBox = new JComboBox(new String[] {"Default",""});
    private String[] quotes = new String[] {""};

    private boolean useHeader = true;

    private Dimension screenSize = Common.getScreenRes();

    public JGRDataFileDialog(Frame f,String directory) {
        super(f,"Load DataFile",true);

        dataName.setMinimumSize(new Dimension(200,20));
        dataName.setPreferredSize(new Dimension(200,20));
        dataName.setMaximumSize(new Dimension(400,20));

        quoteBox.addItemListener(this);
        sepsBox.addItemListener(this);

        fileChooser.addActionListener(this);
        fileChooser.addPropertyChangeListener(this);
        //fileChooser.setPreferredSize(new Dimension(380,300));
        if (directory != null && new File(directory).exists()) fileChooser.setCurrentDirectory(new File(directory));

        this.getContentPane().setLayout(new GridBagLayout());

        JPanel options = new JPanel(new GridBagLayout());

        this.getContentPane().add(fileChooser,  new GridBagConstraints(0, 0, 6, 1, 1.0, 6.0
            , GridBagConstraints.CENTER, GridBagConstraints.BOTH,
            new Insets(1, 5, 1, 5), 0, 0));
        this.getContentPane().add(options,  new GridBagConstraints(0, 1, 3, 1, 0.0, 0.0
            , GridBagConstraints.WEST, GridBagConstraints.NONE,
            new Insets(1, /*iPreferences.isMac?*/5/*:105*/, 1, 5), 0, 0));
        options.add(new JLabel("Assign to:"),  new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0
            , GridBagConstraints.WEST, GridBagConstraints.NONE,
            new Insets(1, 5, 1, 5), 0, 0));
        options.add(dataName,  new GridBagConstraints(1, 0, 4, 1, 0.0, 0.0
            , GridBagConstraints.WEST, GridBagConstraints.NONE,
            new Insets(1, 7, 1, 50), 0, 0));
        options.add(new JLabel("Header :"),  new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0
            , GridBagConstraints.WEST, GridBagConstraints.NONE,
            new Insets(1, 5, 1, 5), 0, 0));
        options.add(header,  new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0
            , GridBagConstraints.WEST, GridBagConstraints.NONE,
            new Insets(1, 5, 1, 5), 0, 0));
        options.add(new JLabel("Attach :"),  new GridBagConstraints(2, 1, 1, 1, 0.0, 0.0
                                                                    , GridBagConstraints.WEST, GridBagConstraints.NONE,
                                                                    new Insets(1, 5, 1, 5), 0, 0));
        options.add(attach,  new GridBagConstraints(3, 1, 1, 1, 0.0, 0.0
                                                    , GridBagConstraints.WEST, GridBagConstraints.NONE,
                                                    new Insets(1, 5, 1, 5), 0, 0));
        options.add(new JLabel("Separator :"),  new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0
            , GridBagConstraints.WEST, GridBagConstraints.NONE,
            new Insets(1, 5, 1, 5), 0, 0));
        options.add(sepsBox,  new GridBagConstraints(1, 2, 1, 1, 0.0, 0.0
            , GridBagConstraints.WEST, GridBagConstraints.NONE,
            new Insets(1, 7, 1, 5), 0, 0));
        options.add(new JLabel("Quote :"),  new GridBagConstraints(2, 2, 1, 1, 0.0, 0.0
            , GridBagConstraints.WEST, GridBagConstraints.NONE,
            new Insets(1, 5, 1, 5), 0, 0));
        options.add(quoteBox,  new GridBagConstraints(3, 2, 1, 1, 0.0, 0.0
            , GridBagConstraints.WEST, GridBagConstraints.NONE,
            new Insets(1, 5, 1, 5), 0, 0));




        this.setSize(new Dimension(500, 450));
        this.setLocation((screenSize.width-500)/2,(screenSize.height-450)/2);
        //if (iPreferences.isMac) this.setResizable(false);
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
            JGRConsole.directory = fileChooser.getCurrentDirectory().getAbsolutePath()+File.separator;
            String file = fileChooser.getSelectedFile().toString();

            String useSep;
            if (sepsBox.getSelectedIndex() >= seps.length) useSep = sepsBox.getSelectedItem().toString();
            else useSep = seps[sepsBox.getSelectedIndex()];
            String useQuote;
            if (quoteBox.getSelectedIndex() >= quotes.length) useQuote = quoteBox.getSelectedItem().toString();
            else useQuote = quotes[quoteBox.getSelectedIndex()];

            String cmd = dataName.getText().trim().replaceAll("\\s","")+ "<- read.table(\""+file.replace('\\','/')+"\",header="+(header.isSelected()?"T":"F")+",sep=\""+useSep+"\", quote=\""+useQuote+"\")"+(attach.isSelected()?";attach("+dataName.getText().trim().replaceAll("\\s","")+")":"")+"";
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
        boolean edit = false;
        if (source == quoteBox) {
            edit = quoteBox.getSelectedIndex() == quoteBox.getItemCount()-1?true:false;
            quoteBox.setEditable(edit);
        }
        else if (source == sepsBox) {
            edit = sepsBox.getSelectedIndex() == sepsBox.getItemCount()-1?true:false;
            sepsBox.setEditable(edit);
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
        if(file!=null && !file.isDirectory()) {
            String name = file.getName().replaceAll("\\..*", "");
            name = name.replaceAll("^[0-9]+|[^a-zA-Z|^0-9|^_]",".");
            dataName.setText(name);
        }
        else dataName.setText(null);
    }
}
