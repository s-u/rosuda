import java.util.*;
import java.io.*;

/* PluginManager - basic class for plugin detection and management of configurations
   $Id$
*/

public class PluginManager {
    static PluginManager mainManager=null;

    public static PluginManager getManager() {
        if (mainManager==null) mainManager=new PluginManager();
        return mainManager;
    }
    
    String configFile="plugins.cfg";
    Vector par;
    Vector val;
    
    public PluginManager() {
        par=new Vector();
        val=new Vector();
        loadSettings();
        setParS("PluginManager","configFile",configFile);
    }

    public String getParS(String plugin, String Par) {
        String pn=""+plugin+"."+Par;
        int i=par.indexOf(pn);
        return (i<0)?null:(String)val.elementAt(i);
    }

    public boolean setParS(String plugin, String Par, String Val) {
        return internal_setParS(plugin+"."+Par,Val);
    };
    
    boolean internal_setParS(String pn, String Val) {
        int i=par.indexOf(pn);
        if (i<0) {
            par.addElement(pn);
            val.addElement(Val);
        } else {
            val.setElementAt(Val,i);
        }
        // as long as we don't have shutdown hook we save setting upon change
        saveSettings();
        return true;
    }

    public boolean saveSettings() {
        try {
            PrintStream p=new PrintStream(new FileOutputStream(configFile));
            p.println("<pluginSettings ver=100>");
            int i=0;
            while(i<par.size()) {
                p.println("<setting name="+par.elementAt(i)+">");
                p.println(val.elementAt(i));
                p.println("</setting>");
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

    public boolean loadSettings() {
        try {
            BufferedReader b=new BufferedReader(new FileReader(configFile));
            boolean isVal=false;
            String curPar=null;
            String curCont=null;
            while(b.ready()) {
                String s=b.readLine();
                int cf=s.indexOf("</setting>");
                int of=s.indexOf("<setting name=");
                // we process generated file only, so we assume each flag has its own line
                if (cf>=0) isVal=false;
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
        } catch (Exception e) { }
        return false;
    }
}
