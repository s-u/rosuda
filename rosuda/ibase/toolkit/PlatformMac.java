//
//  PlatformMac.java
//
//  Created by Simon Urbanek on Wed Jul 30 2003.
//  Copyright (c) 2003 __MyCompanyName__. All rights reserved.
//

package org.rosuda.ibase.toolkit;

import java.io.*;
import java.awt.*;
import org.rosuda.ibase.*;
import org.rosuda.ibase.toolkit.*;
import org.rosuda.util.*;

/** This is just an example of what individaul implementations may want to do in order to make the handlers work. Clearly we cannot implement {@link #handleOpenFile} - that's up to the individual application. */
public class PlatformMac extends org.rosuda.util.PlatformMac {
    public PlatformMac() {
        super();
    }

    public void handleAbout() {
        SplashScreen.runMainAsAbout(Common.appName);
    }

    public void handleOpenFile(File fileName) {
    }

    public void handlePrefs() {
        PrefsColorFrame.showPrefsDialog();
    }
}
