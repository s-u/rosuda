//
//  PlatformMac.java
//  Klimt
//
//  Created by Simon Urbanek on Sun Nov 24 2002.
//  Copyright (c) 2002 __MyCompanyName__. All rights reserved.
//

package org.rosuda.util;

import com.apple.mrj.*;
import java.io.*;
import java.awt.*;

/** This is an implementation of the {@link Platform} class specific to Apple Macintosh systems. The fact that this class is loaded on Apple computers only allows us to use all of the Apple-specific MRJ classes without the risk of encountering missing classes. */ 
public class PlatformMac extends Platform implements MRJAboutHandler, MRJPrefsHandler, MRJOpenDocumentHandler {

    public PlatformMac() {
        super();
        registerHandlers();
    }

    public String getResourceFile(String rname) {
        File f=null;
        try {
            f=MRJFileUtils.getResource(rname);
        } catch(Exception e) {
            if (Global.DEBUG>0)
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
}
