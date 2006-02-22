package org.rosuda.JGR.toolkit;

//JGR - Java Gui for R, see http://www.rosuda.org/JGR/
//Copyright (C) 2003 - 2005 Markus Helbig
//--- for licensing information see LICENSE file in the original JGR distribution ---


import java.awt.*;
import java.awt.event.ActionListener;
import java.io.*;
import javax.swing.*;

import org.rosuda.ibase.*;

/**
 *  FileSelector - use AWT filedialog on a Mac because of look&feel, and SWING on other machines because it provides more features.
 *   
 *	@author Markus Helbig
 *  
 * 	RoSuDa 2003 - 2005 
 */

public class FileSelector extends JFrame {

    /** @deprecated use LOAD*/
    public final static int OPEN = 0;
    /** OPEN DIALOG */
    public final static int LOAD = 0;
    /** SAVE DIALOG */
    public final static int SAVE = 1;

    private FileDialog awtDialog = null;
    private JFileChooser swingChooser = null;

    private int type = 0;
    private Frame f;

    /** 
     * Create a FileDialog, on Mac we use the AWT on others i'm currently using SWING.
     * @param f parent Frame
     * @param title Title
     * @param type OPEN or SAVE
     * @param directory should we start in a specified directory 
     */
    public FileSelector(Frame f, String title, int type,  String directory) {
        this.type = type;
        this.f = f;
        if (Common.isMac()) {
            awtDialog = new FileDialog(f,title,type);
            if (directory != null) awtDialog.setDirectory(directory);
        }
        else {
            if (directory != null) swingChooser = new JFileChooser(directory);
            else swingChooser = new JFileChooser();
            swingChooser.setDialogTitle(title);
            swingChooser.setFileHidingEnabled(!JGRPrefs.showHiddenFiles);
        }
    }
    
    public void addActionListener(ActionListener al) {
    	if (!Common.isMac()) swingChooser.addActionListener(al);
    }
    
    /**
     * Show fileselector.
     */
    public void setVisible(boolean b) {
        if (Common.isMac()) {
            awtDialog.setVisible(true);
        }
        else {
            if(type==OPEN) swingChooser.showOpenDialog(f);
            else if (type==SAVE) swingChooser.showSaveDialog(f);
            else swingChooser.showDialog(f,"OK");
        }
    }

    /**
     * Get selected filename. 
     * @return filename 
     */
    public String getFile() {
        try {
            if (Common.isMac())
                return awtDialog.getFile();
            else
                return swingChooser.getSelectedFile().getName();
        }
        catch (Exception e) { return null;}
    }

    /**
     * Get selected directoryname.
     *  @return directory 
     */
    public String getDirectory() {
        try {
            if (Common.isMac())
                return awtDialog.getDirectory();
			return swingChooser.getCurrentDirectory().getAbsolutePath()+File.separator;
        }
        catch (Exception e) { return null;}
    }

    /**
     * Set current file.
     * @param file filename
     */
    public void setFile(String file) {
        try {
            if (Common.isMac())
                awtDialog.setFile(file);
            else
                swingChooser.setSelectedFile(new File(file));
        }
        catch (Exception e) { }
    }
    
    public Component getSelector() {
        if (Common.isMac())
            return awtDialog;
		return swingChooser;
    }
}