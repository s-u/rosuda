package org.rosuda.plugins;

import java.util.*;
import java.io.*;

import org.rosuda.ibase.*;
import org.rosuda.util.*;

/** PluginManager - basic class for plugin detection and management of configurations
    $Id$
*/

public class PluginManager {
    static PluginManager mainManager=null;

    /** returns current plugin manager. if none existed before, a new one is silently created.
        @return current plugin manager */
    public static PluginManager getManager() {
        if (mainManager==null) mainManager=new PluginManager();
        return mainManager;
    }

    public PluginManager() {}

    public String getParS(String plugin, String par) { return GlobalConfig.getS(plugin+"."+par); }
    public void setParS(String plugin, String par, String val) {
        GlobalConfig.getGlobalConfig().setParS(plugin+"."+par,val);
    }  

    /** Tries to load a plugin specified by the supplied class.
        @param className class name of the plugin
        @return loaded plugin or <code>null</code> if the plugin couldn't be loaded
        */
    public static Plugin loadPlugin(String className) {
        Plugin p=null;
        try {
            Class c=Class.forName(className);
            p=(Plugin)c.newInstance();
        } catch(Exception e) {
            if (Global.DEBUG>0) {
                System.out.println("PluginManager.loadPlugin(\""+className+"\"): unable to load plugin, "+e.getMessage());
                //e.printStackTrace();
            }
        }
        if (p==null) { // if the above failed, try to prepend org.rosuda.plugin. for compatibility
            className="org.rosuda.plugins."+className;
            try {
                Class c=Class.forName(className);
                p=(Plugin)c.newInstance();
            } catch(Exception e) {
                if (Global.DEBUG>0) {
                    System.out.println("PluginManager.loadPlugin(\""+className+"\"): unable to load plugin, "+e.getMessage());
                    //e.printStackTrace();
                }
            }
        }
        return p;
    }

    /** Verifies whether the class identified by the class name exists and is a child of {@link Plugin}.
        @param className class name of the plugin
        @return <code>true</code> if the specified class exists and is assignable to {@link Plugin}
    */
    public static boolean pluginExists(String className) {
        try {
            Class c=Class.forName(className);
            Plugin p=new Plugin();
            if (p.getClass().isAssignableFrom(c)) return true;
        } catch(Exception e) {
            System.out.println("PluginManager.pluginExists(\""+className+"\"): failed to find plugin's class, "+e.getMessage());
        }
        className="org.rosuda.plugins."+className;
        try {
            Class c=Class.forName(className);
            Plugin p=new Plugin();
            if (p.getClass().isAssignableFrom(c)) return true;
        } catch(Exception e) {
            System.out.println("PluginManager.pluginExists(\""+className+"\"): failed to find plugin's class, "+e.getMessage());
        }
        return false;
    }
}
