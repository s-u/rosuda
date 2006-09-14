//
//  PlatformMac.java
//  Klimt
//
//  Created by Simon Urbanek on Wed Jul 30 2003.
//  Copyright (c) 2003 __MyCompanyName__. All rights reserved.
//

package org.rosuda.iplots;

import org.rosuda.ibase.*;
import org.rosuda.ibase.toolkit.*;
import org.rosuda.util.*;

public class Platform extends org.rosuda.util.Platform {
    public void handleAbout() {
        SplashScreen.runMainAsAbout("iPlots "+Common.Version);
    }

    public void handlePrefs() {
        PrefsColorFrame.showPrefsDialog();
    }
}
