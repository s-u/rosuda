//
//  PlatformWin.java
//  Klimt
//
//  Created by Simon Urbanek on Wed Dec 18 2002.
//  Copyright (c) 2002 __MyCompanyName__. All rights reserved.
//

package org.rosuda.util;

import java.awt.*;

/** Platform-dependent class specific for MS Windows operating systems.
    @version $Id$
*/
public class PlatformWin extends Platform {
    PlatformWin() {
        super();
        try {
            String fn;
            Toolkit tk=Toolkit.getDefaultToolkit();
            //fn=getResourceFile("crs-arrow-1chtxa.raw");
            //if (fn!=null) Common.cur_arrow=tk.createCustomCursor(RawImage.loadGrayAlphaImage(16,16,fn),new Point(1,1),"Arrow");
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
                System.out.println("PlatformWin(): "+e.getMessage());
        }
    }
}
