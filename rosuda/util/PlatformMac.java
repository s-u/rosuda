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
public class PlatformMac extends Platform {
    public PlatformMac() {
        super();
        if (Common.DEBUG>0)
            System.out.println("Welcome to the KLIMT for Mac! We got some goodies for ya ;)");
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
}
