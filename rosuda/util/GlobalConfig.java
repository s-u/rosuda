//
//  GlobalConfig.java
//  Klimt
//
//  Created by Simon Urbanek on Wed Jul 30 2003.
//  Copyright (c) 2003 __MyCompanyName__. All rights reserved.
//
package org.rosuda.util;

import java.util.*;
import java.io.*;

public class GlobalConfig {
    /** current config file */
    String configFile="plugins.cfg";
    /** vector of parameter names (of the form "plugin.parameter") */
    Vector par;
    /** vector of values */
    Vector val;
    /** vector of persistence states. see pst_level */
    Vector pst;

    /** persistence level; if set to <code>null</code> then any parameters
        modified from here on are not saved in the user config file.
        this should be set to <code>null</code> when loading global options */
    Integer pst_level=null;

    static GlobalConfig current;

    public static GlobalConfig getGlobalConfig() {
        if (current==null)
            current=new GlobalConfig();
        return current;
    }
    
    public GlobalConfig() {
        par=new Vector();
        val=new Vector();
        pst=new Vector();
        pst_level=null;
        if (File.separatorChar=='/')
            loadSettings("/etc/plugins.cfg");
        pst_level=new Integer(1);
        String uh=System.getProperty("user.home");
        if (uh==null && System.getProperty("os.name").indexOf("indows")>0) uh="C:\\";
        configFile=uh+File.separator+".plugins.cfg";
        loadSettings();
        setParS("GlobalConfig.userConfigFile",configFile);
    }

    /** get String-valued parameter
        @param Par name of the parameter
        @return parameter value or <code>null</code> if such parameter doesn't exist */
    public String getParS(String Par) {
        int i=par.indexOf(Par);
        return (i<0)?null:(String)val.elementAt(i);
    }

    public static String getS(String Par) {
        return getGlobalConfig().getParS(Par);
    }
    
    /** each parameter name consists of the plugin name and the parameter name, separated by a dot.
        there is no particular order in which the parameters are returned (actually it is order of creation/loading) */
    public Object[] getAllParameters() {
        /*BEGINNEW*/
        return (Object[]) par.toArray();
        /*ELSEOLD*
        Object[] s=new Object[par.size()];
        int i=0; while(i<par.size()) { s[i]=par.elementAt(i); i++; };
        return s;
        *ENDNEW*/
    }

    /** set String-valued parameter
        @param Par name of the parameter
        @param Val value to be set
        @return <code>true</code> if successful, <code>false</code> otherwise */
    public boolean setParS(String Par, String Val) {
        boolean r=internal_setParS(Par,Val);
        if (r)         // as long as we don't have shutdown hook we save setting upon change
            saveSettings();
        return r;
    }

    boolean internal_setParS(String pn, String Val) {
        int i=par.indexOf(pn);
        if (i<0) {
            par.addElement(pn);
            val.addElement(Val);
            pst.addElement(pst_level);
        } else {
            val.setElementAt(Val,i); pst.setElementAt(pst_level,i);
        }
        return true;
    }

    /** save settings to the current config file */
    public boolean saveSettings() {
        if (Global.DEBUG>0)
            System.out.println("Save to config file \""+configFile+"\" ...");
        try {
            PrintStream p=new PrintStream(new FileOutputStream(configFile));
            p.println("<globalSettings ver=100>");
            int i=0;
            while(i<par.size()) {
                if (pst.elementAt(i)!=null) { // save only persistent settings
                    p.println("<setting name="+par.elementAt(i)+">");
                    p.println(val.elementAt(i));
                    p.println("</setting>");
                    if (Global.DEBUG>0)
                        System.out.println("saveSettings.save: "+par.elementAt(i)+" -> "+val.elementAt(i));
                };
                i++;
            }
            p.println("</globalSettings>");
            p.close();
            return true;
        } catch(Exception e) {
            if (Global.DEBUG>0) {
                System.out.println("GlobalConfig.saveSettings ERR: "+e.getMessage());
                e.printStackTrace();
            }
        }
        return false;
    }

    /** load settings from the current config file
        @return <code>true</code> on success, <code>false</code> otherwise */
    public boolean loadSettings() { return loadSettings(configFile); };

    /** load settings from the specified file. note that the specified file
        is NOT automatically used as current config file.
        @return <code>true</code> on success, <code>false</code> otherwise */
    public boolean loadSettings(String fName) {
        if (fName==null) fName=configFile;
        if (Global.DEBUG>0)
            System.out.println("Processing config file \""+fName+"\" ...");
        try {
            BufferedReader b=new BufferedReader(new FileReader(fName));
            boolean isVal=false;
            String curPar=null;
            String curCont=null;
            while(b.ready()) {
                String s=b.readLine();
                int cf=s.indexOf("</setting>");
                int of=s.indexOf("<setting name=");
                if (Global.DEBUG>0)
                    System.out.println("LoadSetting: cf="+cf+", of="+of+", isVal="+isVal+", curPar="+curPar+", ln="+s);
                // we process generated file only, so we assume each flag has its own line
                if (isVal) {
                    if (cf>=0) {
                        if (curCont!=null) internal_setParS(curPar,curCont);
                        curCont=null;
                        isVal=false;
                    } else {
                        if (curCont==null) curCont=s; else curCont+="\n"+s;
                    };
                }
                if (of>=0) {
                    s=s.substring(of+14);
                    int cc=s.indexOf(">");
                    if (cc>=0) s=s.substring(0,cc);
                    curPar=s;
                    isVal=true;
                }
            }
            b.close();
            return true;
        } catch (Exception e) {
            if (Global.DEBUG>0) {
                System.out.println("GlobalConfig.loadSettings(\""+fName+"\") ERR: "+e.getMessage());
                e.printStackTrace();
            }
        }
        return false;
    }    
}
