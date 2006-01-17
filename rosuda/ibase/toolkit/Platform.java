//
//  Platform.java
//
//  Created by Simon Urbanek on Wed Jul 30 2003.
//  Copyright (c) 2003 __MyCompanyName__. All rights reserved.
//

package org.rosuda.ibase.toolkit;
import org.rosuda.ibase.*;


/** This platform implementation adds a preferences handler for colors and a default about splash screen.
Use initPlatform("org.rosuda.ibase.toolkit.") in order to enable it.
*/
public class Platform extends org.rosuda.util.Platform {
    public void handleAbout() {
        SplashScreen.runMainAsAbout("About "+Common.appName);
    }
    
    public void handlePrefs() {
        PrefsColorFrame.showPrefsDialog();
    }
}
