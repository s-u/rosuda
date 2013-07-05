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

/** Generic platform class. It provides interface to platform-dependent functionality. */ 
public class Platform {
    static Platform p=null;

    public static boolean isMac=false;
    public static boolean isWin=false;
    
    public static Dimension screenRes;
    
    public static Platform getPlatform() { return p; }

    /** the constructor should never be called directly. Platform classes are created by the {@link #initPlatform} methods. */
    public Platform() {
    }

    /** initialize platforms with the default implementation (in org.rosuda.util)
        @return newly initailized platform object */
    public static Platform initPlatform() { return initPlatform("org.rosuda.util."); }

    /** initialize platforms using the specified class prefix. If the desired class is not found using the class prefix, fall-back to org.rosuda.util classes is attempted.
        @param classPrefix prefix (including the trailing dot) for the platform classes
        @return newly initailized platform object */
    public static Platform initPlatform(String classPrefix) {
        if (p!=null) return p; // prevent loops
        if (Platform.screenRes==null) Platform.screenRes=Toolkit.getDefaultToolkit().getScreenSize();
        if (System.getProperty("os.name").indexOf("Mac OS")>-1 || 
        		System.getProperty("java.vendor").indexOf("Apple")>-1) {
            isMac=true;
            try {
                Class c=Class.forName(classPrefix+"PlatformMac");
                p=(Platform) c.newInstance();
                return p;
            } catch (Exception e) {
                if (Global.DEBUG>0) System.out.println("Platform.init[Mac platform] failed to create platform-dependent class "+classPrefix+"PlatformMac: "+e.getMessage());
            }
            try {
                Class c=Class.forName(classPrefix+"Platform");
                p=(Platform) c.newInstance();
                return p;
            } catch (Exception e) {
                if (Global.DEBUG>0) System.out.println("Platform.init[Mac platform] failed to create platform-dependent class "+classPrefix+"Platform: "+e.getMessage());
            }
            try {
                Class c=Class.forName("org.rosuda.util.PlatformMac");
                p=(Platform) c.newInstance();
                return p;
            } catch (Exception e) {
                if (Global.DEBUG>0) System.out.println("Platform.init[Mac platform] failed to create platform-dependent class org.rosuda.util.PlatformMac: "+e.getMessage());
            }
        } else {
            if (System.getProperty("os.name").indexOf("Windows")>-1) {
                isWin=true;
                try {
                    Class c=Class.forName(classPrefix+"PlatformWin");
                    p=(Platform) c.newInstance();
                    return p;
                } catch (Exception e) {
                    if (Global.DEBUG>0) System.out.println("Platform.init[Windows platform] failed to create platform-dependent class "+classPrefix+"PlatformWin: "+e.getMessage());
                }
                try {
                    Class c=Class.forName(classPrefix+"Platform");
                    p=(Platform) c.newInstance();
                    return p;
                } catch (Exception e) {
                    if (Global.DEBUG>0) System.out.println("Platform.init[Windows platform] failed to create platform-dependent class "+classPrefix+"Platform: "+e.getMessage());
                }
                try {
                    Class c=Class.forName("org.rosuda.util.PlatformWin");
                    p=(Platform) c.newInstance();
                    return p;
                } catch (Exception e) {
                    if (Global.DEBUG>0) System.out.println("Platform.init[Windows platform] failed to create platform-dependent class org.rosuda.util.PlatformWin: "+e.getMessage());
                }
            }
        }
        if (classPrefix!="") {
            try {
                Class c=Class.forName(classPrefix+"Platform");
                p=(Platform) c.newInstance();
                return p;
            } catch (Exception e) {
                if (Global.DEBUG>0) System.out.println("Platform.init[generic platform] failed to create platform-dependent class "+classPrefix+"Platform: "+e.getMessage());
            }
            if (Global.DEBUG>0) System.out.println("Platform.init: fallback to org.rosuda.util.Platform");
        }
        p=new Platform();
        return p;
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
            if (Global.DEBUG>0)
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
                m=c.getMethod("deleteOnExit",(Class[])null);
                m.invoke(f,(Object[])null);
            }
        } catch(Throwable e) {
            if (Global.DEBUG>0)
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
            if (Global.DEBUG>0)
                System.out.println("forceResourceFile(\""+rname+"\"): [onCopy] "+t2.getMessage());
        }
        return null;
    }

    String getPlatformName() { return "Generic"; }

    // Applications should override those
    public void handleAbout() {}
    public void handleOpenFile(File fileName) {}
    public void handlePrefs() {}  
    public void handleQuit() {}  
}
