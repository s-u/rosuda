//
//  PlatformMac.java
//
//  Created by Simon Urbanek on Wed Jul 30 2003.
//  Copyright (c) 2003 __MyCompanyName__. All rights reserved.
//

package org.rosuda.InGlyphs;

import java.io.*;
import java.awt.*;
import org.rosuda.ibase.*;
import org.rosuda.ibase.toolkit.*;
import org.rosuda.util.*;

public class PlatformMac extends org.rosuda.util.PlatformMac {
    public PlatformMac() {
        super();
    }

    public void handleAbout() {
        SplashScreen.runMainAsAbout("InGlyphs");
    }

    public void handleOpenFile(File fileName) {
        SVarSet tvs=new SVarSet();
        Frame df=new Frame();
        GlyphsCanvas.openDataFile(df,tvs,fileName.getAbsolutePath());
        if (tvs.count()<1) {
            new MsgDialog(df,"Load Error","I'm sorry, but I was unable to load the file you selected.");
            df=null;
        } else {
            df=null;
            Dimension sres=Platform.screenRes;
            if (SplashScreen.main!=null)
                SplashScreen.main.setVisible(false);
            GlyphsFrame gf = new GlyphsFrame(tvs,sres.width-150,0,140,(sres.height>600)?600:sres.height);
        }
    }

    public void handlePrefs() {
        PrefsColorFrame.showPrefsDialog();
    }
}
