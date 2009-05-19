//
//  PlatformMac.java
//
//  Created by Simon Urbanek on Wed Jul 30 2003.
//  Copyright (c) 2003 __MyCompanyName__. All rights reserved.
//

package org.rosuda.iplots;

import java.io.*;
import java.awt.*;
import org.rosuda.ibase.*;
import org.rosuda.ibase.toolkit.*;
import org.rosuda.ibase.toolkit.SplashScreen;
import org.rosuda.util.*;

public class PlatformMac extends org.rosuda.util.PlatformMac {
    public void handleAbout() {
        SplashScreen.runMainAsAbout("iPlots "+Common.Version);
    }

    public void handlePrefs() {
        PrefsColorFrame.showPrefsDialog();
    }
}
