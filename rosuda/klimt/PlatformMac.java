//
//  PlatformMac.java
//  Klimt
//
//  Created by Simon Urbanek on Wed Jul 30 2003.
//  Copyright (c) 2003 __MyCompanyName__. All rights reserved.
//

package org.rosuda.klimt;

import java.io.*;
import java.awt.*;
import org.rosuda.ibase.*;
import org.rosuda.ibase.toolkit.*;
import org.rosuda.util.*;

public class PlatformMac extends org.rosuda.util.PlatformMac {
    public PlatformMac() {
        super();
        /*
        String fn;
        try {
            Toolkit tk=Toolkit.getDefaultToolkit();
            fn=getResourceFile("crs-arrow-1chtxa.raw");
            if (fn!=null) Common.cur_arrow=tk.createCustomCursor(RawImage.loadGrayAlphaImage(16,16,fn),new Point(1,1),"Arrow");
            fn=getResourceFile("cursor-16-8b-help.gif");
            if (fn!=null) Common.cur_query=tk.createCustomCursor(tk.getImage(fn),new Point(1,1),"Query");
            fn=getResourceFile("cursor-16-8b-tick.gif");
            if (fn!=null) Common.cur_tick=tk.createCustomCursor(tk.getImage(fn),new Point(1,1),"Move tick");
            fn=getResourceFile("cursor-16-ra8-zoom.raw");
            if (fn!=null) Common.cur_zoom=tk.createCustomCursor(RawImage.loadPureAlphaImage(16,16,fn),new Point(5,5),"Zoom");
        } catch (Exception e) {
            if (Global.DEBUG>0)
                System.out.println("PlatformMac(): "+e.getMessage());
        }
         */
    }
    
    public void handleAbout() {
        KlimtSplash.runMainAsAbout();
    }

    public void handleOpenFile(File fileName) {
        SVarSet tvs=new SVarSet();
        DataRoot dr=Klimt.addData(tvs);
        Frame df=new Frame();
        SNode t=Klimt.openTreeFile(df,fileName.getAbsolutePath(),dr);
        if (t==null && tvs.count()<1) {
            new MsgDialog(df,"Load Error","I'm sorry, but I was unable to load the file you selected.");
            df=null;
        } else {
            df=null;
            Dimension sres=org.rosuda.util.Platform.screenRes;
            /*
            if (t!=null) {
                TFrame f=new TFrame(tvs.getName()+" tree",TFrame.clsTree);
                InTr.newTreeDisplay(t,f,0,0,sres.width-160,(sres.height>600)?600:sres.height-20);
            }
            VarFrame vf=InTr.newVarDisplay(tvs,sres.width-150,0,140,(sres.height>600)?600:sres.height);
             */
            if (SplashScreen.main!=null)
                SplashScreen.main.setVisible(false);
        }
    }

    public void handlePrefs() {
        PrefsColorFrame.showPrefsDialog();
    }    
}
