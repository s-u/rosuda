//
//  Global.java
//  Klimt
//
//  Created by Simon Urbanek on Wed Jul 30 2003.
//  Copyright (c) 2003 __MyCompanyName__. All rights reserved.
//
//  $Id$

package org.rosuda.util;

import java.util.*;

/** Global is a "static" class for general global-scope flags, such as DEBUG */

public class Global {
    /** global debug level */
    public static int DEBUG=0;
    /** global profiling level */
    public static int PROFILE=0;
    
    /** global warning flag */
    public static boolean printWarnings=false;
    /** global loader verbosity flag */
    public static boolean informLoader=false;
    /** use Aqua-style background
        parameter equivalent: --with-aqua */
    public static boolean useAquaBg=false;
    
    /** AppType contstant: stand-alone application */
    public static final int AT_standalone = 0x0000;
    /** AppType contstant: applet (set by applet wrapper) */
    public static final int AT_applet     = 0x0001;

    /** application type. so far 0=stand-alone, other types are set by wrappers. See AT_xxx */
    public static int AppType=AT_standalone;

    /** display run-time warning (if --warn flag is enabled)
        @param w warning to display
        @return unused so far (returns -1) */
    public static int runtimeWarning(String w) {
        if (Global.DEBUG>0 || Global.printWarnings)
            System.out.println("*RTW "+(new Date()).toString()+": "+w);
        return -1;
    }
    
    /** parse command line arguments and set global flags correspondingly
        @param argv arguments as supplied by the main() method
        @return remaining arguments which were not recognized
        */
    public static String[] parseArguments(String[] argv) {
        int argc=argv.length;
        int carg=0;
        Vector rem=new Vector();
        
        while (carg<argv.length) {
            boolean remove=false;

            if (argv[carg].compareTo("--debug")==0) {
                Global.DEBUG=1; remove=true;
            }
            if (argv[carg].compareTo("--warn")==0 ||
                argv[carg].compareTo("--warning")==0) {
                Global.printWarnings=true;
                remove=true;
            }
            if (argv[carg].compareTo("--profile")==0) {
                Global.PROFILE=1; remove=true;
            }
            if (argv[carg].compareTo("--nodebug")==0) {
                Global.DEBUG=0; remove=true;
            }
            if (argv[carg].compareTo("--with-loader")==0) {
                Global.informLoader=true;
                System.out.println("InfoForLoader:Initializing...");
                remove=true;
            }
            if (argv[carg].compareTo("--with-aqua")==0 || argv[carg].compareTo("--aqua")==0) {
                Global.useAquaBg=true;
                remove=true;
            }
            if (argv[carg].compareTo("--without-aqua")==0) {
                Global.useAquaBg=false;
                remove=true;
            }
            if (!remove) rem.addElement(argv[carg]);
            carg++;
        }

        String[] filtered=new String[rem.size()];
        int i=0;
        while (i<rem.size()) { filtered[i]=(String) rem.elementAt(i); i++; };
        return filtered;
    }
}
