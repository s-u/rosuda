package org.rosuda.JGR;

/**
 *  JGRPackageInstaller
 *
 *  install packages if user has enough permissions  
 * 
 *	@author Markus Helbig
 *  
 * 	RoSuDA 2003 - 2004 
 */

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import javax.swing.*;

import org.rosuda.JGR.toolkit.*;

public class JGRPackageInstaller extends iFrame implements ActionListener {

    private String[] packages = null;
    private JList pkgList;

    private JButton install = new JButton("Install");
    private JButton close = new JButton("Close");

    public JGRPackageInstaller(String[] pkgs) {
        super("Package Installer",iFrame.clsPackageUtil);

        packages = pkgs;

        String[] Menu = {
            /*"+", "File", "~File.Basic.End",*/
            "~Window","0"};
        iMenu.getMenu(this, this, Menu);

        close.setActionCommand("close");
        close.addActionListener(this);
        install.setActionCommand("install");
        install.addActionListener(this);


        JPanel buttons = new JPanel();
        buttons.add(install);
        buttons.add(close);
        
        this.getContentPane().setLayout(new GridBagLayout());
        this.getContentPane().add(new JScrollPane(pkgList = new JList(packages)),
            new GridBagConstraints(0, 0, 2, 1, 1.0, 1.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(5, 5, 1, 5), 0, 0));
        this.getContentPane().add(buttons, new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0, GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(2, 5, 5, 5), 0, 0));

        this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        this.getRootPane().setDefaultButton(close);
        this.setMinimumSize(new Dimension(150,250));
        this.setLocation(200,10);
        this.setSize(200,400);
        


    }

    public void installPkg() {
        try {
            String file = JGR.RLIBS[0]+"/JGR.test";
            if (System.getProperty("os.name").startsWith("Windows")) file = file.replace('/','\\');
            File f = new File(file);
            f.createNewFile();
            f.delete();
        }
        catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this,"JGR was unable to write to the library directory.\nPlease change your library path or get sufficient rights.","Permisson denied",JOptionPane.OK_OPTION);
            return;
        }
        Object[] instPkgs = pkgList.getSelectedValues();
        String cmd = "c(";
        if (instPkgs.length > 0) {
            for (int i = 0; i < instPkgs.length-1; i++) cmd += "\""+instPkgs[i]+"\",";
            cmd += "\""+instPkgs[instPkgs.length-1]+"\")";
            JGR.MAINRCONSOLE.execute("install.packages("+cmd+")");
        }
    }

    public void actionPerformed(ActionEvent e) {
        String cmd = e.getActionCommand();
        System.out.println(cmd);
        if (cmd=="close" || cmd=="exit") dispose();
        else if (cmd=="install") installPkg();
    }

}
