package org.rosuda.JGR;

//JGR - Java Gui for R, see http://www.rosuda.org/JGR/
//Copyright (C) 2003 - 2005 Markus Helbig
//--- for licensing information see LICENSE file in the original JGR distribution ---


import java.awt.*;
import java.awt.event.*;
import java.io.*;
import javax.swing.*;

import org.rosuda.JGR.toolkit.*;

/**
 *  JGRPackageInstaller - implementation of a simple package installer widget.
 *
 *	@author Markus Helbig
 *
 * 	RoSuDa 2003 - 2005
 */

public class JGRPackageInstaller extends iFrame implements ActionListener {

    private String[] packages = null;
    private JList pkgList;

    private JButton install = new JButton("Install");
    private JButton close = new JButton("Close");

    private String type = "binaries";
	private String current = RController.getCurrentPackages();

    /**
     * Create a package-installer window.
     * @param pkgs array of packages
     * @param type binary- or source-packages
     */
    public JGRPackageInstaller(String[] pkgs, String type) {
        super("Package Installer",iFrame.clsPackageUtil);
		this.type = type;
        packages = pkgs;

        String[] Menu = {
            /*"+", "File", "~File.Basic.End",*/
            "~Window","0"};
        iMenu.getMenu(this, this, Menu);

        close.setActionCommand("close");
        close.addActionListener(this);
        install.setActionCommand("install");
        install.addActionListener(this);


        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttons.add(install);
        buttons.add(close);


        this.getContentPane().setLayout(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.WEST;
		gbc.weightx = 1.0;
		gbc.weighty = 1.0;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.insets = new Insets(2,2,2,2);
        gbc.gridx = 0;
        gbc.gridy = 0;        
        this.getContentPane().add(new JScrollPane(pkgList = new JList(packages)),gbc);
        gbc.gridy = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
        this.getContentPane().add(buttons, gbc);
		
		pkgList.setCellRenderer(new PkgCellRenderer());
        
        this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        this.getRootPane().setDefaultButton(close);
        this.setMinimumSize(new Dimension(150,250));
        this.setLocation(200,10);
        this.setSize(200,400);
        this.setResizable(false);
    }

    private void installPkg() {
        String destDir = null;
        for (int i = 0; i < JGR.RLIBS.length; i++) {
            if (checkLibPaths(JGR.RLIBS[i])) {
                destDir = JGR.RLIBS[i];
                break;
            }
        }
        if (destDir == null) {
            JFileChooser chooser = new JFileChooser();
            chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            chooser.setDialogTitle("Choose Installation Directory");
            int r = chooser.showOpenDialog(this);
            if (r == JFileChooser.CANCEL_OPTION) return;
            if (chooser.getSelectedFile()!=null)
                destDir = chooser.getSelectedFile().toString();
            if (!checkLibPaths(destDir)) destDir = null;
            if (destDir != null) {
                String[] libs = new String[(JGR.RLIBS.length+1)];
                libs[0] = destDir;
                System.arraycopy(JGR.RLIBS,0,libs,1,JGR.RLIBS.length);
                JGR.RLIBS = libs;
                JGRPrefs.writePrefs(true);
                JGR.R.eval(".libPaths(\""+destDir+"\")");
            }
        }
        if (destDir == null) {
            JOptionPane.showMessageDialog(this,"JGR was unable to write to the library directory.\nPlease change your library path or get sufficient rights.","Permisson denied",JOptionPane.OK_OPTION);
            return;
        }
        Object[] instPkgs = pkgList.getSelectedValues();
        String cmd = "c(";
        if (instPkgs.length > 0) {
            for (int i = 0; i < instPkgs.length-1; i++) cmd += "\""+instPkgs[i]+"\",";
            cmd += "\""+instPkgs[instPkgs.length-1]+"\")";
            if (type.equals("binaries") && JGRPrefs.isMac)
                JGR.MAINRCONSOLE.execute("install.binaries("+cmd+",\""+destDir+"\")",true);
				//JGR.MAINRCONSOLE.execute("install.packages("+cmd+",\""+destDir+"\",contriburl=contrib.url(getOption(\"CRAN\"),type=\"mac.binary\"))");
            else
                JGR.MAINRCONSOLE.execute("install.packages("+cmd+",\""+destDir+"\");.refreshHelpFiles()",true);
        }
    }

    private boolean checkLibPaths(String path) {
        try {
            String file = path+"/JGR.test";
            if (System.getProperty("os.name").startsWith("Windows")) file = file.replace('/','\\');
            File f = new File(file);
            f.createNewFile();
            f.delete();
        }
        catch (Exception e) {
            return false;
        }
        return true;
    }

    /**
     * actionPerformed: handle action event: buttons.
     */
    public void actionPerformed(ActionEvent e) {
        String cmd = e.getActionCommand();
        if (cmd=="close" || cmd=="exit") dispose();
        else if (cmd=="install") installPkg();
    }

	 class PkgCellRenderer extends JLabel implements ListCellRenderer {
	     
		 public PkgCellRenderer() {
			 setOpaque(true);
		 }
		 
		 public Component getListCellRendererComponent(JList list,Object value,int index,boolean isSelected,boolean cellHasFocus) {
	         setText(value.toString());
			 if (current.indexOf(value.toString()) > -1) {
		         setBackground(isSelected ? Color.blue : Color.lightgray);
		         setForeground(isSelected ? Color.green : Color.black);
			 }
			 else {
				 setBackground(isSelected ? Color.blue : Color.white);
				 setForeground(isSelected ? Color.white : Color.black);
			 }
	         return this;
	     }
	 }
}
