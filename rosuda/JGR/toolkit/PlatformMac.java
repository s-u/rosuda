package org.rosuda.JGR.toolkit;

//
//  PlatformMac.java
//  JGR
//
//  Created by Markus Helbig on Fri Mar 05 2004.
//  Copyright (c) 2004 __MyCompanyName__. All rights reserved.
//


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
        Preferences.isMac = true;
    }

    public void handleQuit() {
        JGR.MAINRCONSOLE.execute("q()\n");
    }


    public void handleAbout() {
        new AboutDialog();
    }


    public void handleOpenFile(File fileName) {
    }

    public void handlePrefs() {
        new PrefsDialog();
    }


}
