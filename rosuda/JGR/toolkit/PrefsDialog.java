package org.rosuda.JGR.toolkit;

//
//  PrefsDialog.java
//  JGR
//
//  Created by Markus Helbig on Fri Mar 05 2004.
//  Copyright (c) 2004 __MyCompanyName__. All rights reserved.
//


import java.awt.*;
import java.awt.event.*;
import java.io.*;
import javax.swing.*;

import org.rosuda.ibase.*;
import org.rosuda.ibase.toolkit.*;

public class PrefsDialog extends JDialog implements ActionListener{

    private Dimension screenSize = Common.getScreenRes();

    private String[] Sizes = {"2","4","6","8","9","10","11","12","14","16","18","20","22","24"}; //,"30","36","48","72" };
    private String[] Fonts;

    private JTabbedPane tabpane = new JTabbedPane();
    private JPanel lookandfeel = new JPanel();
    private RProfileArea RProfile = new RProfileArea();

    private JComboBox font = new JComboBox();
    private JComboBox size = new JComboBox();

    private DefaultComboBoxModel mf;
    private DefaultComboBoxModel ms;

    private JTextField helptabs = new JTextField();

    private JButton cancel = new JButton("Cancel");
    private JButton apply = new JButton("Apply");
    private JButton save = new JButton("Save");

    /** new PrefsDialog */
    public PrefsDialog() {
        this(null);
    }

    /** new PrefsDialog
     *  @param f parent JFrame */
    public PrefsDialog(JFrame f) {
        super(f,"Preferences",true);

        cancel.setActionCommand("cancel");
        apply.setActionCommand("apply");
        save.setActionCommand("save");

        cancel.addActionListener(this);
        apply.addActionListener(this);
        save.addActionListener(this);

        cancel.setToolTipText("Cancel");
        apply.setToolTipText("Apply changes to current session");
        save.setToolTipText("Save changes for future sessions and quit");

        helptabs.setMinimumSize(new Dimension(40,20));
        helptabs.setPreferredSize(new Dimension(40,20));
        helptabs.setMaximumSize(new Dimension(40,20));
        helptabs.setText(Preferences.MAXHELPTABS+"");
        helptabs.setToolTipText("Maximum amount of opened tabs");

        this.getRootPane().setDefaultButton(save);

        Fonts = GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames();
        font.setModel(mf = new DefaultComboBoxModel(Fonts));
        size.setModel(ms = new DefaultComboBoxModel(Sizes));
        for (int i = 0; i < Fonts.length; i++) if (Fonts[i].equals(Preferences.FontName)) { mf.setSelectedItem(Fonts[i]); break;}
        for (int i = 0; i < Sizes.length; i++) if (Sizes[i].equals(Preferences.FontSize+"")) { ms.setSelectedItem(Sizes[i]); break;}

        font.setMinimumSize(new Dimension(200,25));
        font.setPreferredSize(new Dimension(200,25));
        font.setMaximumSize(new Dimension(200,25));
        size.setMinimumSize(new Dimension(50,25));
        size.setPreferredSize(new Dimension(50,25));
        size.setMaximumSize(new Dimension(50,25));
        size.setEditable(true);

        this.getContentPane().setLayout(new GridBagLayout());

        lookandfeel.setLayout(new GridBagLayout());
        lookandfeel.add(new JLabel("Font:"), new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0
            , GridBagConstraints.NORTHWEST, GridBagConstraints.NONE,
            new Insets(2, 5, 5, 5), 0, 0));
        lookandfeel.add(font,
                                  new GridBagConstraints(1, 2, 2, 1, 0.0, 0.0
            , GridBagConstraints.WEST, GridBagConstraints.NONE,
            new Insets(2, 5, 5, 5), 0, 0));
        lookandfeel.add(size,
                                  new GridBagConstraints(3, 2, 1, 1, 0.0, 0.0
            , GridBagConstraints.WEST, GridBagConstraints.NONE,
            new Insets(2, 5, 5, 5), 0, 0));


        lookandfeel.add(new JLabel("HelpTabs:"), new GridBagConstraints(0, 3, 1, 1, 0.0, 0.0
            , GridBagConstraints.WEST, GridBagConstraints.NONE,
            new Insets(2, 5, 5, 5), 0, 0));
        lookandfeel.add(new JLabel("Amount "),
                                  new GridBagConstraints(1, 4, 1, 1, 0.0, 0.0
            , GridBagConstraints.WEST, GridBagConstraints.NONE,
            new Insets(2, 5, 5, 5), 0, 0));
        lookandfeel.add(helptabs,
                                  new GridBagConstraints(2, 4, 1, 1, 0.0, 0.0
            , GridBagConstraints.WEST, GridBagConstraints.NONE,
            new Insets(2, 5, 5, 5), 0, 0));

        tabpane.addTab("Look & Feel",lookandfeel);
        tabpane.addTab("RProfile",new JScrollPane(RProfile));

        this.getContentPane().add(tabpane,new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0
            , GridBagConstraints.WEST, GridBagConstraints.BOTH,
            new Insets(2, 5, 5, 5), 0, 0));


        JPanel buttons = new JPanel();
        buttons.add(cancel);
        buttons.add(apply);
        buttons.add(save);

        this.getContentPane().add(buttons,new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0
            , GridBagConstraints.EAST, GridBagConstraints.NONE,
            new Insets(2, 5, 5, 5), 0, 0));

        //this.setResizable(false);
        this.setSize(new Dimension(400, 350));
        this.setLocation((screenSize.width-400)/2,(screenSize.height-350)/2);
        this.addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                setVisible(false);
            }
        });
        this.show();
    }

    public void applyChanges() {
        Preferences.FontName = font.getSelectedItem().toString();
        Preferences.FontSize = new Integer(size.getSelectedItem().toString()).intValue();
        Preferences.MAXHELPTABS = new Integer(helptabs.getText()).intValue();
        Preferences.apply();
    }


    public void actionPerformed(ActionEvent e) {
        String cmd = e.getActionCommand();
        if (cmd=="apply") applyChanges();
        else if (cmd=="cancel") dispose();
        else if (cmd=="save") {
            applyChanges();
            Preferences.writePrefs();
            RProfile.save();
            this.dispose();
        }
    }


    class RProfileArea extends JTextArea {

        File profile;

        public RProfileArea() {
            try {
                profile = new File(System.getProperty("user.home")+File.separator+".Rprofile");
                //if (!profile.exists()) profile == null
            }
            catch (Exception e) {
                this.setEnabled(false);
            }
            try {
                if (profile != null && profile.exists()) {
                    BufferedReader reader = new BufferedReader(new FileReader(profile));
                    String text = "";
                    while(reader.ready()) text += reader.readLine()+"\n";
                    this.setText(text);
                }
            }
            catch (Exception e) {
                this.setEnabled(false);
            }
        }

        public void save() {
            try {
                BufferedWriter writer = new BufferedWriter(new FileWriter(profile));
                writer.write(this.getText());
                writer.flush();
            }
            catch (Exception e) {
            }
        }
    }
}
