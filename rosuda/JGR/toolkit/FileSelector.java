package org.rosuda.JGR.toolkit;

//
//  FileSelector.java
//  JGR
//
//  Created by Markus Helbig on Fri Mar 05 2004.
//  Copyright (c) 2004 __MyCompanyName__. All rights reserved.
//

import java.awt.*;
import java.io.*;
import javax.swing.*;

import org.rosuda.ibase.*;


public class FileSelector extends JFrame {

    /** OPEN DIALOG */
    public final static int OPEN = 0;
    /** SAVE DIALOG */
    public final static int SAVE = 1;

    private FileDialog awtDialog = null;
    private JFileChooser swingChooser = null;


    /** create a FileDialog, on Mac we use the AWT on others we are currently using SWING
     * @param f parent Frame
     * @param title Title
     * @param type OPEN or SAVE
     * @param directory should we start in a specified directory */
    public FileSelector(Frame f, String title, int type,  String directory) {
        if (Common.isMac()) {
            awtDialog = new FileDialog(f,title,type);
            if (directory != null) awtDialog.setDirectory(directory);
            awtDialog.show();
        }
        else {
            if (directory != null) swingChooser = new JFileChooser(directory);
            else swingChooser = new JFileChooser();
            swingChooser.setDialogTitle(title);
            if(type==OPEN) swingChooser.showOpenDialog(f);
            else if (type==SAVE) swingChooser.showSaveDialog(f);
            else swingChooser.showDialog(f,"OK");
        }
    }

    /** @returns filename */
    public String getFile() {
        try {
            if (Common.isMac())
                return awtDialog.getFile();
            else
                return swingChooser.getSelectedFile().getName();
        }
        catch (Exception e) { return null;}
    }

    /** @returns directory */
    public String getDirectory() {
        try {
            if (Common.isMac())
                return awtDialog.getDirectory();
            else
                return swingChooser.getCurrentDirectory().getAbsolutePath()+File.separator;
        }
        catch (Exception e) { return null;}
    }
}