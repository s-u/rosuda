import java.util.*;
import java.io.*;

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
    
    /** initialization of PluginManager. the config files are loaded in following order:
        1) global config file (/etc/plugins.cfg) on unix platforms
        2) plugins.cfg in current working directory
        3) $HOME/.plugins.cfg if the plugins.cfg in cwd doesn't exist
        if neither 2) nor 3) exists then 3) is created and used

        instances of PluginManager should NOT be created manually. Use {@link #getManager()} instead. */
    public PluginManager() {
        par=new Vector();
        val=new Vector();
        pst=new Vector();
        pst_level=null;
        if (File.separatorChar=='/')
            loadSettings("/etc/plugins.cfg");
        pst_level=new Integer(1);
        if (!loadSettings()) {
            String uh=System.getProperty("user.home");
            if (uh==null && System.getProperty("os.name").indexOf("indows")>0) uh="C:\\";
            configFile=uh+File.separator+".plugins.cfg";
            loadSettings();
        }
        setParS("PluginManager","userConfigFile",configFile);
    }

    /** get String-valued parameter
        @param plugin name of the plugin
        @param Par name of the parameter
        @return parameter value or <code>null</code> if such parameter doesn't exist */
    public String getParS(String plugin, String Par) {
        String pn=""+plugin+"."+Par;
        int i=par.indexOf(pn);
        return (i<0)?null:(String)val.elementAt(i);
    }

    /** each parameter name consists of the plugin name and the parameter name, separated by a dot.
        there is no particular order in which the parameters are returned (actually it is order of creation/loading) */
    public String[] getAllParameters() {
        /*BEGINNEW*/
        return (String[]) par.toArray();
        /*ELSEOLD*
        String[] s=new String[par.size()];
        int i=0; while(i<par.size()) { s[i]=(String)par.elementAt(i); i++; };
        return s;
        *ENDNEW*/
    };

    /** set String-valued parameter
        @param plugin name of the plugin (or "PluginManager")
        @param Par name of the parameter
        @param Val value to be set
        @return <code>true</code> if successful, <code>false</code> otherwise */
    public boolean setParS(String plugin, String Par, String Val) {
        boolean r=internal_setParS(plugin+"."+Par,Val);
        if (r)         // as long as we don't have shutdown hook we save setting upon change
            saveSettings();
        return r;
    };
    
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
        if (Common.DEBUG>0)
            System.out.println("Save to config file \""+configFile+"\" ...");
        try {
            PrintStream p=new PrintStream(new FileOutputStream(configFile));
            p.println("<pluginSettings ver=100>");
            int i=0;
            while(i<par.size()) {
                if (pst.elementAt(i)!=null) { // save only persistent settings
                    p.println("<setting name="+par.elementAt(i)+">");
                    p.println(val.elementAt(i));
                    p.println("</setting>");
                    if (Common.DEBUG>0)
                        System.out.println("saveSettings.save: "+par.elementAt(i)+" -> "+val.elementAt(i));
                };
                i++;
            }
            p.println("</pluginSettings>");
            p.close();
            return true;
        } catch(Exception e) {
            if (Common.DEBUG>0) {
                System.out.println("PluginManager.saveSettings ERR: "+e.getMessage());
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
        if (Common.DEBUG>0)
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
                if (Common.DEBUG>0)
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
            if (Common.DEBUG>0) {
                System.out.println("PluginManager.loadSettings(\""+fName+"\") ERR: "+e.getMessage());
                e.printStackTrace();
            }
        }
        return false;
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
            if (Common.DEBUG>0) {
                System.out.println("PluginManager.loadPlugin(\""+className+"\"): unable to load plugin, "+e.getMessage());
                //e.printStackTrace();
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
        return false;
    }
}
