package org.rosuda.plugins;

import java.io.*;
import java.awt.*;
import java.awt.event.*;
import java.util.StringTokenizer;
import java.util.Vector;

import org.rosuda.ibase.*;
import org.rosuda.ibase.toolkit.*;
import org.rosuda.klimt.*;
import org.rosuda.JRclient.*;
import org.rosuda.util.*;

import javax.swing.*;

/* generate tree plugin */

public class PluginGetTreeR extends Plugin implements ActionListener {
    /* tree root */
    SNode root;
    /* dataset */
    SVarSet vs;
    /* predictor variables */
    int pred[];
    /* response variable */
    SVar resp;

    String treeOpt=null;
    String formula;
    String Rver="?";
    String Rbin=null;
    String Rcall=null;
    /** library to use for tree generation */
    String lib="rpart";
    /** host to use for Rserv connections */
    String RservHost="127.0.0.1";
    
    boolean useAll=true;
    boolean registerPar=true;
    boolean useRserv=true;
    boolean holdConnection=false;
    boolean useBootstrap=false;
    int     numBootstrap=10;
    
    String filePrefix="";
    
    Rconnection rc=null;
    
    PluginManager pm=null;
    
    /* values for cahced initialization - just one plugin needs to detect R */
    public static boolean initializedSuccessfully=false;
    public static String lastRbin=null;
    public static String lastRver=null;
    public static String lastRcall=null;

    /* last ID suffix */
    public static int lastTreeID=0;

    String lastDump=null;

    /** create a new instance of the plugin */
    public PluginGetTreeR() {
        name="Generate tree via R 1.2";
        author="Simon Urbanek <simon.urbanek@math.uni-augsburg.de>";
        desc="Grows classification or regression trees using R or Rserv";
        type=PT_GenTree;
        pm=PluginManager.getManager();
        Rbin=pm.getParS("AllPlugins","latestRbinary");
        Rcall=pm.getParS("PluginGetTreeR","Rcall");
        if (System.getProperty("os.name").equals("Mac OS X"))
            filePrefix="/tmp/"; // just a hack, but well...
    }

    /** set a plugin parameter
        @param par parameter name
        @param val parameter value */
    public void setParameter(String par, Object val) {
        if (par.equals("dataset")) vs=(SVarSet)val;
        if (par.equals("selectedOnly")) useAll=!((Boolean)val).booleanValue();
        if (par.equals("useBootstrap")) useBootstrap=!((Boolean)val).booleanValue();
        if (par.equals("treeLibrary")) lib=(String)val;
        if (par.equals("formula")) { // parse the formula
	    String f=(String)val;
	    //System.out.println("Formula: "+f);
	    if (f==null || vs==null || f.indexOf("~")<1) return;	    
	    String pnam=f.substring(0,f.indexOf("~")).trim();
	    String covs=f.substring(f.indexOf("~")+1);
	    StringTokenizer st=new StringTokenizer(covs,"+");
	    Vector v=new Vector();
	    while(st.hasMoreTokens()) {
		String cov=st.nextToken().trim();
		int id=vs.indexOf(cov);
		//System.out.println("covariate: ("+id+") \""+cov+"\"");
		if (id>=0)
		    v.add(new Integer(id));
	    }
	    int id=vs.indexOf(pnam);
	    //System.out.println("response: ("+id+") \""+pnam+"\"");
	    if (id>=0) {
		resp=vs.at(id);
		v.add(new Integer(id));
	    };
	    pred=new int[v.size()];
	    int i=0;
	    while(i<v.size()) { pred[i]=((Integer)v.elementAt(i)).intValue(); i++; };
	    formula=f;
	}

        if (par.equals("treeOptions")) treeOpt=(String)val;
        if (par.equals("registerTree")) registerPar=((Boolean)val).booleanValue();
        if (par.equals("useRserv")) useRserv=((Boolean)val).booleanValue();    
        if (par.equals("holdConnection")) holdConnection=((Boolean)val).booleanValue();
        if (par.equals("RservHost")) RservHost=(String)val;
    }

    /** get a plugin parameter
        @param par parameter name
        @return parameter value or <code>null</code> if not availiable */
    public Object getParameter(String par) {
        if (par.equals("root") || par.equals("tree")) return root;
        if (par.equals("Rversion")) return Rver;
        if (par.equals("Rbin")) return Rbin;
        if (par.equals("lastdump")) return lastDump;
        if (par.equals("treeLibrary")) return lib;
        if (par.equals("selectedOnly")) return new Boolean(!useAll);
        if (par.equals("formula")) return formula;
        if (par.equals("treeOptions")) return treeOpt;
        if (par.equals("registerTree")) return new Boolean(registerPar);
        if (par.equals("useRserv")) return new Boolean(useRserv);
        if (par.equals("holdConnection")) return new Boolean(holdConnection);
        if (par.equals("RservHost")) return RservHost;
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
            File fr=new File(filePrefix+"PluginInit.r"); if (fr.exists()) fr.delete();
            File fo=new File(filePrefix+"PluginInit.out"); if (fo.exists()) fo.delete();
            PrintStream p=new PrintStream(new FileOutputStream(filePrefix+"PluginInit.r"));
            p.println("print(\"PLUGIN_INIT\",quote=FALSE)");
            //p.println("invisible(options(echo = FALSE))\nlibrary(tree)\ndata(iris)\nprint(\"TREE\",quote=FALSE)\nt<-tree(Species~.,iris)\nprint(t)\nprint(formula(terms(t)))\nprint(\"END\",quote=FALSE)\n");
            p.close();

            Process pc=null;
            String postBin=" --slave --no-save --no-restore CMD BATCH "+filePrefix+"PluginInit.r "+filePrefix+"PluginInit.out";
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
                            Rbin="/Library/Frameworks/R.framework/Resources/bin/R"; Rcall=Rbin+postBin;
                            pc=Runtime.getRuntime().exec(Rcall);
                            pm.setParS("AllPlugins","latestRbinary",Rbin);
                        } catch (Exception e3) {
                            try {
                                Rbin="/usr/local/bin/R"; Rcall=Rbin+postBin;
                                pc=Runtime.getRuntime().exec(Rcall);
                                pm.setParS("AllPlugins","latestRbinary",Rbin);
                            } catch (Exception e4) {
                                try {
                                    Rbin="/sw/bin/R"; Rcall=Rbin+postBin;
                                    pc=Runtime.getRuntime().exec(Rcall);
                                    pm.setParS("AllPlugins","latestRbinary",Rbin);
                                } catch (Exception e5) {
                                    err="Cannot find R executable!"; return false;
                                }
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
                BufferedReader br=new BufferedReader(new FileReader(filePrefix+"PluginInit.out"));
                while(br.ready()) {
                    String s=br.readLine();
                    if (s.length()>7 && s.substring(0,7).equals("Version"))
                        Rver=s;
                }
                br.close();
                fo.delete();
                System.out.println("Found R "+Rver);
                int iISBN=Rver.indexOf(", ISBN");
                if (iISBN>-1) Rver=Rver.substring(0,iISBN);
                iISBN=Rver.indexOf("Under");
                if (iISBN>-1) Rver=Rver.substring(0,iISBN);
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
    JCheckBox cbBoot;
    JLabel lBoot,lBoot2;
    JTextField tfBoot;

    /** pop up dialog asking for variables to be used, response, library and parameters
        @param f parent frame
        @return <code>false</code> if the selection was invalid or user pressed cancel.
                Check the boolean "cancel" variable to see if the cause was error or cancel */
    public boolean pluginDlg(Frame f) {
        Button b=null, b2=null; cancel=false;
        JDialog jd=new JDialog(f,"Generate Tree Plug-in",true);
        d=jd;
        //d.setBackground(Color.white);
        Container rp=jd.getContentPane();
        rp.setLayout(new BorderLayout());
        rp.add(new JSpacingPanel(),BorderLayout.WEST);
        rp.add(new JSpacingPanel(),BorderLayout.EAST);
        JPanel bp=new JPanel(new FlowLayout());
        bp.add(b=new Button("OK"));
        bp.add(b2=new Button("Cancel"));
        rp.add(bp,BorderLayout.SOUTH);
        Choice c=new Choice();
        int j=0,lniv=0,lct=0; boolean frv=false;
        while(j<vs.count()) {
            if (!vs.at(j).isInternal()) { lniv=j; lct++; } // consider non-internal variables only
            j++;
        };
        Vector pv=new Vector();
        Vector vsel=new Vector();
        j=0;
	int ji=0;
        int totNSel=0;
        while(j<vs.count()) {
            if (!vs.at(j).isInternal()) { // consider non-internal variables only
                c.add(vs.at(j).getName());
                if (vs.at(j)==resp) {
                    c.select(vs.at(j).getName());
                    frv=true;
                }
                pv.add(vs.at(j).getName());
                boolean selMe=false;
		if (pred!=null) {
		    int jj=0; while(jj<pred.length) {
			if (pred[jj]==j) {
			    selMe=true; break;
                        }
			jj++;
		    }
		} else selMe=true;
                if (selMe) totNSel++;
                vsel.add(new Boolean(selMe));
		ji++;
            }
            j++;
        }
        JList l=new JList(pv);
        int[] si=new int[totNSel];
        j=0; int six=0;
        while(j<lct) {
            if (((Boolean)vsel.elementAt(j)).booleanValue())
                si[six++]=j;
            j++;
        }
        l.setSelectedIndices(si);
        si=null; vsel=null; pv=null;
        if (!frv)
            c.select(vs.at(lniv).getName());
        JPanel p=new JPanel(new BorderLayout());
        rp.add(p);
        JPanel lp=new JPanel(new FlowLayout());
        lp.add(new JLabel("     Using R "+Rver+" in "+Rbin+"     "));
        rp.add(lp,BorderLayout.NORTH);
        p.add(l,BorderLayout.NORTH);
        
        JPanel bPanel=new JPanel();
        JTextField t=new JTextField((treeOpt==null)?"":treeOpt,30);
        GridBagLayout gbl=new GridBagLayout();
        bPanel.setLayout(gbl);
        GridBagConstraints gcw = new GridBagConstraints();
        GridBagConstraints gce = new GridBagConstraints();
        gcw.gridx=0; gcw.anchor=GridBagConstraints.EAST;
        gce.gridx=1; gce.anchor=GridBagConstraints.WEST;
        gbl.setConstraints(bPanel.add(new JLabel("Response: ")),gcw);
        gbl.setConstraints(bPanel.add(c),gce);
        gbl.setConstraints(bPanel.add(new JLabel("Library: " )),gcw);
        Choice chLibrary=new Choice();
        chLibrary.add("rpart");
        chLibrary.add("tree");
        if (lib!=null) chLibrary.select(lib);
        gbl.setConstraints(bPanel.add(chLibrary),gce);
        gbl.setConstraints(bPanel.add(new JLabel("Parameters: ")),gcw);
        gbl.setConstraints(bPanel.add(t),gce);
        JPanel bsp=new JPanel(new FlowLayout());
        bsp.add(cbBoot=new JCheckBox("bootstrap", useBootstrap));
        cbBoot.setActionCommand("bootstrap");
        cbBoot.addActionListener(this);
        bsp.add(lBoot=new JLabel(", generate "));
        bsp.add(tfBoot=new JTextField(""+numBootstrap,5));
        bsp.add(lBoot2=new JLabel(" trees"));
        lBoot.setEnabled(useBootstrap);
        lBoot2.setEnabled(useBootstrap);
        tfBoot.setEnabled(useBootstrap);
        gbl.setConstraints(bPanel.add(bsp),gce);
        p.add(bPanel,BorderLayout.SOUTH);
        d.pack();
        b.addActionListener(this);b2.addActionListener(this);
        d.setVisible(true);
        if (cancel) {
            d.dispose();
            err="Cancelled by user.";
            return false;
        }
        System.out.println("back for good ... ");
        int vc=0;
        int ri=vs.indexOf(c.getSelectedItem());
        resp=vs.byName(c.getSelectedItem());
        j=0;
        boolean inclResp=false;
        int[] sel=l.getSelectedIndices();
        while(j<sel.length) {
            int vi=sel[j];
            if (vi>=0) vc++;
            if (vi==ri) inclResp=true;
            j++;
        }
        if (!inclResp) vc++;
        if (vc<2) {
            err="You must select at least one predictor and one response varaible (distinct).";
            return false;
        }
        j=0;
        pred=new int[vc]; int k=0;
        while(j<sel.length) {
            int vi=sel[j];
            if (vi>=0 && vi!=ri) {
                pred[k]=vi; k++;
            }
            j++;
        }
        pred[vc-1]=ri;

        lib=chLibrary.getSelectedItem();
        
        StringBuffer sb=new StringBuffer(resp.getName());
        sb.append("~");
        j=0;
        while(j<vc-1) {
            if (j>0) sb.append("+");
            sb.append(vs.at(pred[j]).getName());
            j++;
        }
        formula=sb.toString();
        if (Global.DEBUG>0)
            System.out.println("formula: "+formula);
        treeOpt=t.getText();
        if (Global.DEBUG>0)
            System.out.println("tree options: \""+treeOpt+"\"");
        useBootstrap=cbBoot.isSelected();
        try {
            numBootstrap=Integer.parseInt(tfBoot.getText());
        } catch (Exception e) {};
        if (Global.DEBUG>0)
            System.out.println("useBootstrap: "+useBootstrap+", numBootstrap: "+numBootstrap);
        //cbBoot=null; lBoot=null; lBoot2=null; tfBoot=null;
        d.dispose();
        return true;
    }

    /** activated if a button was pressed. It determines whether "cancer" was pressed or OK" */
    public void actionPerformed(ActionEvent e) {
        String ac=e.getActionCommand();
        if (ac.equals("OK") || ac.equals("Cancel")) {
            cancel=!ac.equals("OK");
            d.setVisible(false);
        }
        if (ac.equals("bootstrap")) {
            boolean es=cbBoot.isSelected();
            lBoot.setEnabled(es);
            lBoot2.setEnabled(es);
            tfBoot.setEnabled(es);
        }
    }

   /** executes plugin - generates a tree. If {@link #useRserv} is <code>true</code> then an Rserv connection
       is established (if not existing already due to {@link #holdConnection}). If Rserv id not availiable,
       fall back to native R
       @return <code>true</code> if everything went fine, <code>false</code> otherwise. */
    public boolean execPlugin() {
        lastDump=null;
        System.out.println("execPlugin");
        if (vs==null || pred==null || resp==null) {
            err="Dataset, predictors or response is empty!";
            System.out.println("execPlugin: ERR: "+err);
            return false;
        }
        try {
            String fprefix=filePrefix;
            if (useRserv) {
                if (rc==null) {
                    try {
                        rc=new Rconnection(RservHost);
                    } catch (RSrvException rse1) {
                        err="Rserv server does not respond ("+rse1.getMessage()+") and native R is not configured. Start Rserv or re-initialize plugin for native R access.";
                        rc=null;
                        return false;
                    }
                    REXP rx=null;
                    String xe="?";
                    try {
                        rx=rc.eval("getwd()");
                    } catch (RSrvException rse2) {
                        xe=rse2.getMessage();
                    };
                    if (rx!=null && rx.asString()!=null) {
                        fprefix=rx.asString()+File.separator;
                    } else {
                        err="Cannot get current working directory ("+xe+")";
                        rc=null;
                    }
                }
            }
            
            File fd=new File(fprefix+"PluginInit.rds"); if (fd.exists()) fd.delete();
            File fr=new File(fprefix+"PluginInit.r"); if (fr.exists()) fr.delete();
            File fo=new File(fprefix+"PluginInit.out"); if (fo.exists()) fo.delete();
            PrintStream dp=new PrintStream(new FileOutputStream(fprefix+"PluginInit.rds"));
            vs.Export(dp,useAll,pred);
            dp.close();
            System.out.println("dataExported");

            if (treeOpt==null) treeOpt="";
            if (treeOpt.length()>0) treeOpt=","+treeOpt;
            PrintStream p=new PrintStream(new FileOutputStream(fprefix+"PluginInit.r"));
            p.print("invisible(options(echo = FALSE))\nlibrary("+lib+")\nd<-read.table(\""+fprefix+"PluginInit.rds\",TRUE,\"\\t\",comment.char=\"\",quote=\"\")\n");
            { int k=0; while (k<pred.length) { if (vs.at(pred[k]).isCat()) p.print("d$"+vs.at(pred[k]).getName()+"<-factor(d$"+vs.at(pred[k]).getName()+")\n"); k++; }  };
            p.print("print(\"TREE\",quote=FALSE)\nt<-"+lib+"("+formula+",d"+treeOpt+")\nprint(t)\nprint(formula(terms(t)))\nprint(\"END\",quote=FALSE)\n");
            p.close();
            if (!useRserv) {
                System.out.println("execPlugin: starting R");
                Process pc=Runtime.getRuntime().exec(Rcall);
                System.out.println("execPlugin: waiting for R to finish");
                pc.waitFor();
                System.out.println("execPlugin: R finished");
            } else {
                System.out.println("sink.begin");
                rc.voidEval("sink(\""+fprefix+"PluginInit.out\")");
                System.out.println("source");
                rc.voidEval("source(\""+fprefix+"PluginInit.r\")");
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
                BufferedReader br=new BufferedReader(new FileReader(fprefix+"PluginInit.out"));
                lastTreeID++;
                DataRoot dr=Klimt.getRootForData(vs);
                root=TreeLoader.LoadTree(br,dr,"GrownTree_"+lastTreeID,registerPar
                                         /*,vs,0,"[1] TREE","[1] END",true,registerPar */
                                         );
                if (root!=null) {
                    if (fr.exists()) fr.delete();
                    if (fd.exists()) fd.delete();
                    fo.delete();
                    System.out.println("Tree loaded!\n"+root.toString());
		    root.setFormula(formula); // set formula manually since RTree may have loaded partial formula only
                    return true;
                }
                err="Commands executed, but no tree was found.";
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

    /** just a tiny test code */
    public static void main(String[] args) {
        SVarSet vs=new SVarSet();
        try {
            BufferedReader br=new BufferedReader(new FileReader("iris.tsv"));
            Loader.LoadTSV(br,vs,true);
        } catch(Exception e) {
            System.out.println("cannot load test dataset (iris.tsv), "+e.getMessage()); e.printStackTrace();
        }
        PluginGetTreeR gt=new PluginGetTreeR();
        gt.initPlugin();
        gt.setParameter("dataset",vs);
        gt.checkParameters();
        Frame f=new Frame("parent");
        gt.pluginDlg(f);
        gt.execPlugin();
        gt.donePlugin();
        f.dispose();
    }
}
