//
//  Platform.java
//  Klimt
//
//  Created by Simon Urbanek on Sun Nov 24 2002.
//  Copyright (c) 2002 __MyCompanyName__. All rights reserved.
//

package org.rosuda.util;

import java.io.*;
import java.awt.*;
import java.lang.reflect.*;

public class Platform {
    static Platform p;

    public static Platform getPlatform() { return p; }
    
    public Platform() {
        p=this;
        if (!Common.initializedStatic) Common.initStatic();
        Common.cur_arrow=Common.cur_query=Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR);
        Common.cur_zoom=Common.cur_tick=Common.cur_aim=Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR);
        Common.cur_move=Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR);
        Common.cur_hand=Cursor.getPredefinedCursor(Cursor.HAND_CURSOR);
    }

    /** every platform should provide at least this method for each resource.
    */
    public InputStream getResourceInput(String rname) {
        return null;
    }

    /** if possible this returns full path to the specified resource file. if the platform
    doesn't store bundles in files, the platform should return <code>null</code>
    */
    public String getResourceFile(String rname) {
        String jar=System.getProperty("java.class.path");
        if (jar!=null) {
            // if there are more path entries we assume that ours is the last one
            int i=jar.lastIndexOf(File.pathSeparatorChar);
            if (i>-1)
                jar=jar.substring(i+1);
            if (Common.DEBUG>0)
                System.out.println("JAR: "+jar);
            File f=new File(jar);
            if (!f.exists()) return null;
            if (f.getParent()!=null)
                f=new File(f.getParent());
            
            File tf=new File(f,rname);
            if (tf.exists()) return tf.getAbsolutePath(); // try1: in same dir as jar
            if (f.getParent()!=null) {
                tf=new File(f.getParent());
                tf=new File(tf,rname);
                if (tf.exists()) return tf.getAbsolutePath(); // try2: in .. rel. to jar
            };
            tf=new File(f,"resource"+File.separator+rname);
            if (tf.exists()) return tf.getAbsolutePath(); // try3: resource/
        }
        return null;
    }

    /** if getResourceFile returns <code>null</code> then this methods should create a temporary file for the resource and return the file name. It should return <code>null</code> only if it finds no means of creating such temporary file. The current implementation first check getResourceFile, if it fails    but getResourceInput is successfull, then for JDK 1.2 and above it creates a temp file using createTempFile method and copies the contents from the InputStream. If this fails (creation of tmep file - this will fail for JDK 1.1 and below) then it tries to create a file "javaRes.tmp" in the current directory. <b>Beware:</b> the returned filename is valid only until the next call to this method. In fact on JDK 1.2 every call creates an unique file (flagged as delete-on-exit), but on JDK 1.1 and in other cases when this procedure fails the same file is used (and is not deleted upon exit!)    
    */
    public String forceResourceFile(String rname) {
        String fn=getResourceFile(rname);
        if (fn!=null) return fn;
        InputStream ris=getResourceInput(rname);
        if (ris==null) return null;
        File f=null;
        String p1="pres";
        String p2=".trf";
        try {
            Class c=Class.forName("java.io.File");
            Class ca[]=new Class[2];
            ca[0]=p1.getClass(); ca[1]=p2.getClass();
            Method m=c.getMethod("createTempFile",ca);
            Object arg[]=new Object[2];
            arg[0]=p1; arg[1]=p2;
            f=(File)m.invoke(null,arg);
            if (f!=null) {
                m=c.getMethod("deleteOnExit",null);
                m.invoke(f,null);
            }
        } catch(Throwable e) {
            if (Common.DEBUG>0)
                System.out.println("forceResourceFile(\""+rname+"\"): "+e.getMessage());
        };
        if (f==null)
            f=new File("javaRes.tmp");
        try {
            FileOutputStream fos=new FileOutputStream(f);
            byte[] buf=new byte[32768];
            while (true) {
                int r=ris.read(buf);
                if (r>0) fos.write(buf);
                if (r<1) break;
            }
            fos.close();
            return f.getAbsolutePath();
        } catch (Throwable t2) {
            if (Common.DEBUG>0)
                System.out.println("forceResourceFile(\""+rname+"\"): [onCopy] "+t2.getMessage());
        }
        return null;
    }

    String getPlatformName() { return "Generic"; }
}
