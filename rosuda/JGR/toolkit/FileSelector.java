package org.rosuda.JGR.toolkit;

/**
 *  FileSelector
 * 
 *  use awt filedialog on a mac, and swing on other machines
 *   
 *	@author Markus Helbig
 *  
 * 	RoSuDA 2003 - 2004 
 */

import java.awt.*;
import java.io.*;
import javax.swing.*;

import org.rosuda.ibase.*;


public class FileSelector extends JFrame {

    /** @deprecated use LOAD, OPEN DIALOG */
    public final static int OPEN = 0;
    /** OPEN DIALOG */
    public final static int LOAD = 0;
    /** SAVE DIALOG */
    public final static int SAVE = 1;

    private FileDialog awtDialog = null;
    private JFileChooser swingChooser = null;

    private int type = 0;
    private Frame f;

    /** create a FileDialog, on Mac we use the AWT on others we are currently using SWING
     * @param f parent Frame
     * @param title Title
     * @param type OPEN or SAVE
     * @param directory should we start in a specified directory */
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
        }
    }
    
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

    /** @return filename */
    public String getFile() {
        try {
            if (Common.isMac())
                return awtDialog.getFile();
            else
                return swingChooser.getSelectedFile().getName();
        }
        catch (Exception e) { return null;}
    }

    /** @return directory */
    public String getDirectory() {
        try {
            if (Common.isMac())
                return awtDialog.getDirectory();
            else
                return swingChooser.getCurrentDirectory().getAbsolutePath()+File.separator;
        }
        catch (Exception e) { return null;}
    }

    public void setFile(String file) {
        try {
            if (Common.isMac())
                awtDialog.setFile(file);
            else
                swingChooser.setSelectedFile(new File(file));
        }
        catch (Exception e) { }
    }
}