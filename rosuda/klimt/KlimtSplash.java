//
//  KlimtSplash.java
//  Klimt
//
//  Created by Simon Urbanek on Wed Jul 30 2003.
//  Copyright (c) 2003 __MyCompanyName__. All rights reserved.
//

package org.rosuda.klimt;

import org.rosuda.ibase.*;
import org.rosuda.ibase.toolkit.*;


public class KlimtSplash extends SplashScreen {
    public KlimtSplash() {
        super("Klimt v"+Common.Version+" (release "+Common.Release+")");
    }

    /* we need to re-define this since we're KlimtSplash */
    public static void runMainAsAbout() {
        if (main==null) main=new KlimtSplash();
        main.runAsAbout();
    }
    
    public Object run(Object o, String cmd) {
        super.run(o,cmd); // let SplashScreen handle the defaults
        
        if (cmd=="prefs") {
            PreferencesFrame.showPrefsDialog();
        }

        if (cmd=="openData") {
            SVarSet tvs=new SVarSet();
            /*
            SNode t=InTr.openTreeFile(this,null,tvs);
            if (t==null && tvs.count()<1) {
            } else {
                if (t!=null) {
                    TFrame f=new TFrame("Tree "+tvs.getName(),TFrame.clsTree);
                    InTr.newTreeDisplay(t,f,0,0,Common.screenRes.width-160,(Common.screenRes.height>600)?600:Common.screenRes.height-20);
                }
                VarFrame vf=InTr.newVarDisplay(tvs,Common.screenRes.width-150,0,140,(Common.screenRes.height>600)?600:Common.screenRes.height-30);
                setVisible(false);
            }
             */
        }
        return null;
    }
    
}
