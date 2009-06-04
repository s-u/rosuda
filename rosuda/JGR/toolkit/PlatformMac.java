package org.rosuda.JGR.toolkit;

/**
 *  PlatformMac
 * 
 * 	mac-specific handlers
 * 
 */


import java.io.*;
import java.awt.*;
import java.util.zip.*;
import javax.swing.*;


import org.rosuda.JGR.*;
import org.rosuda.JGR.toolkit.*;
import org.rosuda.util.*;

/** This is just an example of what individaul implementations may want to do in order to make the handlers work. Clearly we cannot implement {@link #handleOpenFile} - that's up to the individual application. */
public class PlatformMac extends org.rosuda.util.PlatformMac {
    public PlatformMac() {
        super();
        JGRPrefs.isMac = true;
    }

    public void handleQuit() {
        JGR.MAINRCONSOLE.exit();
    }


    public void handleAbout() {
        new AboutDialog();
    }


    public void handleOpenFile(File fileName) {
    	System.out.println(fileName);
    }

    public void handlePrefs() {
        PrefDialog inst = PrefDialog.showPreferences(null);
        inst.setLocationRelativeTo(null);
        inst.setVisible(true);
    }


}
