package org.rosuda.JGR.toolkit;


//
//  iToolkit.java
//  JGR
//
//  Created by Markus Helbig on Fri Mar 05 2004.
//  Copyright (c) 2004 __MyCompanyName__. All rights reserved.
//


import java.awt.*;
import javax.swing.*;

import java.io.*;
import java.util.zip.*;

import org.rosuda.util.*;


public class iToolkit {

    public static void cursor(JComponent c) {
        Component gp = c.getRootPane().getGlassPane();
        if (!gp.isVisible()) {
            gp.setCursor(new Cursor(Cursor.WAIT_CURSOR));
            gp.setVisible(true);
        }
        else {
            gp.setVisible(false);
        }
    }

    public static byte[] getResource(String name) {
         try {
           ZipFile MJF=null;
           byte[] istream;
           String jar=System.getProperty("java.class.path");
           if (jar!=null) {
             // if there are more path entries we assume that ours is the last one
             int i=jar.lastIndexOf(File.pathSeparatorChar);
             if (i>-1)
               jar=jar.substring(i+1);
             if (Global.DEBUG>0)
               System.out.println("my own jar file: "+jar);
             MJF = new ZipFile(jar);
           }
           if (MJF!=null) {
             ZipEntry LE = MJF.getEntry(name);
             InputStream rstream = MJF.getInputStream(LE);
             istream = new byte[(int)LE.getSize()];
             for( int i=0; i<istream.length; i++ ) {
               istream[i] = (byte)rstream.read();
             }
             if (Global.DEBUG>0)
               System.out.println("Resource OK, "+istream.length+" bytes.");
             return istream;
           }
         }
         catch (Exception e) {
           e.printStackTrace();
         }
         return null;
       }

       public static int schnitt(int[] widths) {
           int sum = 0,l;
           for (int i = 0; i < (l= widths.length); i++) sum += widths[i];
           return 1 + (int) sum / l;
       }
}
