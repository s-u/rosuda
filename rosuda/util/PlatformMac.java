//
//  PlatformMac.java
//  Klimt
//
//  Created by Simon Urbanek on Sun Nov 24 2002.
//  Copyright (c) 2002 __MyCompanyName__. All rights reserved.
//

import com.apple.mrj.*;
import java.io.*;
import java.awt.*;

/** This is an implementation of the {@link Platform} class specific to Apple Macintosh systems. The fact that this class is loaded on Apple computers only allows us to use all of the Apple-specific MRJ classes without the risk of encountering missing classes.<p>Currently following "goodies" are present:<ul><li>Support for resources placed in MacOS X bundles</li><li>loading of custom MacOS X cursors.</li></ul>and ToDo:<ul><li>Register Apple-specific handlers such as "Preferences" or "About"</li><li>Apple-specific clipboard handling</li></ul> */ 
public class PlatformMac extends Platform implements MRJAboutHandler, MRJPrefsHandler, MRJOpenDocumentHandler {
    public PlatformMac() {
        super();
        if (Common.DEBUG>0)
            System.out.println("Welcome to the KLIMT for Mac! We got some goodies for ya ;)");
        registerHandlers();
        String fn;
        try {
            Toolkit tk=Toolkit.getDefaultToolkit();
            fn=getResourceFile("crs-arrow-1chtxa.raw");
            if (fn!=null) Common.cur_arrow=tk.createCustomCursor(RawImage.loadGrayAlphaImage(16,16,fn),new Point(1,1),"Arrow");
//            fn=getResourceFile("arrow-test4.raw");
//            if (fn!=null) Common.cur_arrow=tk.createCustomCursor(RawImage.loadRGBaImage(16,16,fn),new Point(1,1),"Arrow");
            fn=getResourceFile("cursor-16-8b-help.gif");
            if (fn!=null) Common.cur_query=tk.createCustomCursor(tk.getImage(fn),new Point(1,1),"Query");
            fn=getResourceFile("cursor-16-8b-tick.gif");
            if (fn!=null) Common.cur_tick=tk.createCustomCursor(tk.getImage(fn),new Point(1,1),"Move tick");
            fn=getResourceFile("cursor-16-ra8-zoom.raw");
            if (fn!=null) Common.cur_zoom=tk.createCustomCursor(RawImage.loadPureAlphaImage(16,16,fn),new Point(5,5),"Zoom");
        } catch (Exception e) {
            if (Common.DEBUG>0)
                System.out.println("PlatformMac(): "+e.getMessage());
        }
    }

    public String getResourceFile(String rname) {
        File f=null;
        try {
            f=MRJFileUtils.getResource(rname);
        } catch(Exception e) {
            if (Common.DEBUG>0)
                System.out.println("PlatformMac.getResourceFile(\""+rname+"\"): "+e.getMessage());
        }
        if (f==null) { // fall back to default resource loading if this ain't a MacOS X bundle
            String s=super.getResourceFile(rname);
            if (s!=null) f=new File(s);
        }
        return (f==null)?null:f.getAbsolutePath();        
    }

    String getPlatformName() { return "Apple Macintosh"; }

    // MRJ specific handlers

    public void registerHandlers() {
        MRJApplicationUtils.registerAboutHandler(this);
        MRJApplicationUtils.registerOpenDocumentHandler(this);
        MRJApplicationUtils.registerPrefsHandler(this);
    }
    
    public void handleAbout() {
        SplashScreen.main.runAsAbout();
    }

    public void handleOpenFile(File fileName) {
        SVarSet tvs=new SVarSet();
        Frame df=new Frame();
        SNode t=InTr.openTreeFile(df,fileName.getAbsolutePath(),tvs);
        if (t==null && tvs.count()<1) {
            new MsgDialog(df,"Load Error","I'm sorry, but I was unable to load the file you selected.");
            df=null;
        } else {
            df=null;
            Dimension sres=Toolkit.getDefaultToolkit().getScreenSize();
            Common.screenRes=sres;
            if (t!=null) {
                TFrame f=new TFrame(tvs.getName()+" tree",TFrame.clsTree);
                InTr.newTreeDisplay(t,f,0,0,sres.width-160,(sres.height>600)?600:sres.height-20);
            }
            VarFrame vf=InTr.newVarDisplay(tvs,sres.width-150,0,140,(sres.height>600)?600:sres.height);
            if (SplashScreen.main!=null)
                SplashScreen.main.setVisible(false);
        }
    }

    public void handlePrefs() {
        PreferencesFrame.showPrefsDialog();
    }
}
