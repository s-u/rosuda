//
//  InGlyphs.java
//  InGlyphs
//
//  Created by Daniela DiBenedetto on Tue Nov 04 2003.
//  Copyright (c) 2003 __MyCompanyName__. All rights reserved.
//

package org.rosuda.InGlyphs;

import java.util.*;
import java.awt.*;

import org.rosuda.ibase.*;
import org.rosuda.ibase.toolkit.*;
import org.rosuda.util.*;
import org.rosuda.plugins.*;

public class InGlyphs {
    public static void main(String[] argv) {
        argv=Global.parseArguments(argv);

        Platform.initPlatform("org.rosuda.InGlyphs.");
        PluginManager pm=PluginManager.getManager();
        Common.initValuesFromConfigFile(pm);
        Dimension sres=Toolkit.getDefaultToolkit().getScreenSize();
        Common.screenRes=sres;

        TFrame f=new TFrame("In Glyphs "+Common.Version,TFrame.clsMain);

        Common.mainFrame=f;

        SVarSet tvs=new SVarSet();
        String fname=argv.length>0?argv[0]:null;

        if (fname==null || fname.length()<1 || fname.charAt(0)=='-') {
            fname=null;
        }

        GlyphsCanvas.openDataFile(f,tvs,fname);

        if (tvs.count()<1) {
            new MsgDialog(f,"Load Error","I'm sorry, but I was unable to load the file you selected"+((fname!=null)?" ("+fname+")":"")+".");
            System.exit(1);
        }

        if (Global.DEBUG>0) {
            for(Enumeration e=tvs.elements();e.hasMoreElements();) {
                SVar vv=(SVar)e.nextElement();
                System.out.println("==> "+vv.getName()+", CAT="+vv.isCat()+", NUM="+vv.isNum());
                if (vv.isCat()) {
                    System.out.println("    categories: "+vv.getNumCats());
                }
            }
        }
        f.setTitle("In Glyphs "+Common.Version+", "+tvs.getName()+" - tree");

        GlyphsFrame gf = new GlyphsFrame(tvs,sres.width-150,0,140,(sres.height>600)?600:sres.height);
    }
}
