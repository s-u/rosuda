package org.rosuda.plugins;

import java.io.*;
import java.awt.*;
import java.awt.event.*;
import java.util.StringTokenizer;
import java.util.Vector;

import org.rosuda.ibase.*;
import org.rosuda.ibase.toolkit.*;
import org.rosuda.JRclient.*;
import org.rosuda.util.*;

/* generate tree plugin */

public class PluginDeriveVar extends Plugin implements ActionListener {
    /* dataset */
    SVarSet vs;
    String Rcommand=null;
    String varName=null;
    
    String Rver="?";
    String Rbin=null;
    String Rcall=null;
    /** host to use for Rserv connections */
    String RservHost="127.0.0.1";
    
    boolean useAll=true;
    boolean useRserv=true;
    boolean holdConnection=false;

    Rconnection rc=null;
    
    PluginManager pm=null;
    
    /* values for cahced initialization - just one plugin needs to detect R */
    public static boolean initializedSuccessfully=false;
    public static String lastRbin=null;
    public static String lastRver=null;
    public static String lastRcall=null;

    /* last ID suffix */
    public static int lastID=0;

    String lastDump=null;

    /** create a new instance of the plugin */
    public PluginDeriveVar() {
        name="Generate derived variable via R 1.0";
        author="Simon Urbanek <simon.urbanek@math.uni-augsburg.de>";
        desc="Generates new derived variable using R or Rserv";
        type=PT_GenTree;
        pm=PluginManager.getManager();
        Rbin=pm.getParS("AllPlugins","latestRbinary");
        Rcall=pm.getParS("PluginR","Rcall");
    }

    /** set a plugin parameter
        @param par parameter name
        @param val parameter value */
    public void setParameter(String par, Object val) {
        if (par=="dataset") vs=(SVarSet)val;
        if (par=="selectedOnly") useAll=!((Boolean)val).booleanValue();
        if (par=="useRserv") useRserv=((Boolean)val).booleanValue();    
        if (par=="holdConnection") holdConnection=((Boolean)val).booleanValue();
        if (par=="RservHost") RservHost=(String)val;
        if (par=="Rcommand") Rcommand=(String)val;
        if (par=="varName") varName=(String)val;
    }

    /** get a plugin parameter
        @param par parameter name
        @return parameter value or <code>null</code> if not availiable */
    public Object getParameter(String par) {
        if (par=="Rversion") return Rver;
        if (par=="Rbin") return Rbin;
        if (par=="lastdump") return lastDump;
        if (par=="selectedOnly") return new Boolean(!useAll);
        if (par=="useRserv") return new Boolean(useRserv);
        if (par=="holdConnection") return new Boolean(holdConnection);
        if (par=="RservHost") return RservHost;
        if (par=="Rcommand") return Rcommand;
        if (par=="varName") return varName;
        return null;
    }

    BufferedReader in;

    /** initialize plugin insteance
        @return <code>true</code> if the initialization was successful */
    public boolean initPlugin() {
        if (useRserv) {
            try {
                rc=new Rconnection(RservHost);
                REXP rx=rc.eval("paste(version$major,version$minor,sep='.')");
                lastRver=Rver=rx.asString();
                initializedSuccessfully=true;
                if (!holdConnection) rc=null;
                return true;
            } catch (RSrvException rse) {
                rc=null; useRserv=false;
            }
        }

        if (initializedSuccessfully) { /* cached initialization if another instance found R already */
            Rbin=lastRbin; Rver=lastRver; Rcall=lastRcall;
            return true;
        }
        
        try {
            File fr=new File("PluginInit.r"); if (fr.exists()) fr.delete();
            File fo=new File("PluginInit.out"); if (fo.exists()) fo.delete();
            PrintStream p=new PrintStream(new FileOutputStream("PluginInit.r"));
            p.println("print(\"PLUGIN_INIT\",quote=FALSE)");
            //p.println("invisible(options(echo = FALSE))\nlibrary(tree)\ndata(iris)\nprint(\"TREE\",quote=FALSE)\nt<-tree(Species~.,iris)\nprint(t)\nprint(formula(terms(t)))\nprint(\"END\",quote=FALSE)\n");
            p.close();

            Process pc=null;
            String postBin=" --slave --no-save --no-restore CMD BATCH PluginInit.r PluginInit.out";
            if (Rbin!=null || Rcall!=null) {
                if (Rcall==null) Rcall=Rbin+postBin; 
                try {
                    pc=Runtime.getRuntime().exec(Rcall);
                } catch (Exception e0) {};
            };
            if (pc==null) {
                try {
                    Rbin="R"; Rcall=Rbin+postBin;
                    pc=Runtime.getRuntime().exec(Rcall);
                } catch (Exception e1) {
                    try {
                        Rbin="/usr/bin/R"; Rcall=Rbin+postBin;
                        pc=Runtime.getRuntime().exec(Rcall);
                        pm.setParS("AllPlugins","latestRbinary",Rbin);
                    } catch (Exception e2) {
                        try {
                            Rbin="/usr/local/bin/R"; Rcall=Rbin+postBin;
                            pc=Runtime.getRuntime().exec(Rcall);
                            pm.setParS("AllPlugins","latestRbinary",Rbin);
                        } catch (Exception e3) {
                            try {
                                Rbin="/sw/bin/R"; Rcall=Rbin+postBin;
                                pc=Runtime.getRuntime().exec(Rcall);
                                pm.setParS("AllPlugins","latestRbinary",Rbin);
                            } catch (Exception e4) {
                                err="Cannot find R executable!"; return false;
                            }
                        }
                    }
                }
            }
            
            if (pc!=null) pc.waitFor();
            if (fr.exists()) fr.delete();
            if (!fo.exists()) {
                err="Unable to use R! Make sure R is installed and in your PATH.";
                return false;
            } else {
                BufferedReader br=new BufferedReader(new FileReader("PluginInit.out"));
                while(br.ready()) {
                    String s=br.readLine();
                    if (s.length()>7 && s.substring(0,7).equals("Version"))
                        Rver=s;
                }
                br.close();
                fo.delete();
                System.out.println("Found R "+Rver);
                initializedSuccessfully=true;
                lastRbin=Rbin;
                lastRver=Rver; lastRcall=Rcall;
                return true;
            }
        } catch(Exception e) {
            System.out.println("initPlugin: "+e.getMessage()); err="While trying to detect R: "+e.getMessage(); e.printStackTrace();
        }
        return false;
    }

    /** check consistency of the parameters; for this plugin all we need is
        the dataset. */
    public boolean checkParameters() {
        return vs!=null;
    }

    Dialog d;

    /** pop up dialog asking for variables to be used, response, library and parameters
        @param f parent frame
        @return <code>false</code> if the selection was invalid or user pressed cancel.
                Check the boolean "cancel" variable to see if the cause was error or cancel */
    public boolean pluginDlg(Frame f) {
        Button b=null, b2=null; cancel=false;
        d=new Dialog(f,"New derived variable plug-in",true);
        d.setBackground(Color.white);
        d.setLayout(new BorderLayout());
        d.add(new SpacingPanel(),BorderLayout.WEST);
        d.add(new SpacingPanel(),BorderLayout.EAST);
        Panel bp=new Panel(); bp.setLayout(new FlowLayout());
        bp.add(b=new Button("OK"));bp.add(b2=new Button("Cancel"));
        d.add(bp,BorderLayout.SOUTH);
        TextArea tac=new TextArea(20,5);
        TextField tfv=new TextField((varName==null)?"derived."+lastID:varName);
        Panel p=new Panel();
        p.setLayout(new BorderLayout());
        d.add(p);
        p.add(tac);
        p.add(tfv,BorderLayout.NORTH);
        Panel lp=new Panel();
        lp.setLayout(new FlowLayout());
        lp.add(new Label("     Using R "+Rver+" in "+Rbin+"     "));
        d.add(lp,BorderLayout.NORTH);
        //p.add(l,BorderLayout.NORTH);
        d.pack();
        b.addActionListener(this);b2.addActionListener(this);
        d.setVisible(true);
        if (cancel) {
            d.dispose();
            err="Cancelled by user.";
            return false;
        }
        System.out.println("back for good ... ");
        Rcommand=tac.getText();
        varName=tfv.getText();
        if (Global.DEBUG>0)
            System.out.println("Rcommand: \""+Rcommand+"\", varName: \""+varName+"\"");
        d.dispose();
        return true;
    }

    /** activated if a button was pressed. It determines whether "cancer" was pressed or OK" */
    public void actionPerformed(ActionEvent e) {
        cancel=!e.getActionCommand().equals("OK");
        d.setVisible(false);
    };

   /** executes plugin - generates a tree. If {@link #useRserv} is <code>true</code> then an Rserv connection
       is established (if not existing already due to {@link #holdConnection}). If Rserv id not availiable,
       fall back to native R
       @return <code>true</code> if everything went fine, <code>false</code> otherwise. */
    public boolean execPlugin() {
        lastDump=null;
        System.out.println("execPlugin");
        if (vs==null || Rcommand==null) {
            err="Dataset or R-command is empty!";
            System.out.println("execPlugin: ERR: "+err);
            return false;
        }
        try {
            String fprefix="";
            if (useRserv) {
                if (rc==null) {
                    try {
                        rc=new Rconnection(RservHost);
                    } catch (RSrvException rse1) {
                        err="Rserv server does not respond ("+rc.getLastError()+") and native R is not configured. Start Rserv or re-initialize plugin for native R access.";
                        rc=null;
                        return false;
                    }
                    REXP rx=null;
                    try {
                        rx=rc.eval("getwd()");
                    } catch (RSrvException rse2) {};
                    if (rx!=null && rx.asString()!=null) {
                        fprefix=rx.asString()+File.separator;
                    } else {
                        err="Cannot get current working directory ("+rc.getLastError()+")";
                        rc=null;
                    }
                }
            }

            File fd=new File(fprefix+"PluginInit.ods"); if (fd.exists()) fd.delete();
            fd=new File(fprefix+"PluginInit.rds"); if (fd.exists()) fd.delete();
            File fr=new File(fprefix+"PluginInit.r"); if (fr.exists()) fr.delete();
            File fo=new File(fprefix+"PluginInit.out"); if (fo.exists()) fo.delete();
            PrintStream dp=new PrintStream(new FileOutputStream(fprefix+"PluginInit.rds"));
            vs.Export(dp,true);
            dp.close();
            System.out.println("dataExported");

            PrintStream p=new PrintStream(new FileOutputStream(fprefix+"PluginInit.r"));
            p.print("invisible(options(echo = FALSE))\nd<-read.table(\"PluginInit.rds\",TRUE,\"\\t\",comment.char=\"\")\nattach(d)\nwrite.table(("+Rcommand+"),\"PluginInit.ods\",quote=FALSE,row.names=FALSE,col.names=TRUE,sep=\"\\t\")\n");
            p.close();
            if (!useRserv) {
                System.out.println("execPlugin: starting R");
                Process pc=Runtime.getRuntime().exec(Rcall);
                System.out.println("execPlugin: waiting for R to finish");
                pc.waitFor();
                System.out.println("execPlugin: R finished");
            } else {
                System.out.println("sink.begin");
                rc.voidEval("sink(\"PluginInit.out\")");
                System.out.println("source");
                rc.voidEval("source(\"PluginInit.r\")");
                System.out.println("sink.end");
                rc.voidEval("sink()");
                if (!holdConnection) rc=null;
                System.out.println("Rserv.done");
            }
            if (!fo.exists()) {
                if (fr.exists()) fr.delete();
                if (fd.exists()) fd.delete();
                err="Unable to use R! Make sure R is installed and in your PATH.";
                System.out.println("execPlugin: ERR: "+err);
                return false;
            } else {
                BufferedReader br=new BufferedReader(new FileReader(fprefix+"PluginInit.ods"));
                lastID++;
                SVarSet newvs=new SVarSet();
                int vls=Loader.LoadTSV(br,newvs,true);
                if (vls>0 && newvs.count()>0 && (vs.count()<1 || newvs.at(0).size()==vs.at(0).size())) {
                    if (fr.exists()) fr.delete();
                    if (fd.exists()) fd.delete();
                    fo.delete();
                    System.out.println("Variable loaded.\n");
                    newvs.at(0).setName(varName);
                    vs.add(newvs.at(0));
                    if (newvs.count()>1) {
                        newvs.at(1).setName(varName+".2");
                        vs.add(newvs.at(1));
                    }
                    newvs=null;
                    return true;
                }
                err="Commands executed, but no valid data was found. ";
                if (newvs.count()>0) err+="The newly created variable does not have the same size as the original dataset. ("+newvs.at(0).size()+" vs "+vs.at(0).size()+")";
                br.close();
                br=new BufferedReader(new FileReader(fprefix+"PluginInit.out"));
                StringBuffer dump=new StringBuffer();
                while(br.ready()) {
                    dump.append(br.readLine()); dump.append("\n");
                } br.close();
                fo.delete();
                lastDump=dump.toString();
                System.out.println("execPlugin: ERR: "+err+"\nlastDump:\n"+lastDump);
                return false;
            }
        } catch(Exception e) {
            System.out.println("execPlugin: "+e.getMessage()); err="White trying to run R: "+e.getMessage(); e.printStackTrace();
        }
        System.out.println("execPlugin: unexpectedly reached return, ERR: "+err);
        return false;
    }
}
