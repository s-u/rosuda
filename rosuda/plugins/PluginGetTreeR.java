import java.io.*;
import java.awt.*;
import java.awt.event.*;
import java.util.StringTokenizer;
import java.util.Vector;

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
        name="Generate tree via R 1.1";
        author="Simon Urbanek <simon.urbanek@math.uni-augsburg.de>";
        desc="Grows classification or regression trees using R or Rserv";
        type=PT_GenTree;
        pm=PluginManager.getManager();
        Rbin=pm.getParS("AllPlugins","latestRbinary");
        Rcall=pm.getParS("PluginGetTreeR","Rcall");
    }

    /** set a plugin parameter
        @param par parameter name
        @param val parameter value */
    public void setParameter(String par, Object val) {
        if (par=="dataset") vs=(SVarSet)val;
        if (par=="selectedOnly") useAll=!((Boolean)val).booleanValue();
        if (par=="treeLibrary") lib=(String)val;
        if (par=="formula") { // parse the formula
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

        if (par=="treeOptions") treeOpt=(String)val;
        if (par=="registerTree") registerPar=((Boolean)val).booleanValue();
        if (par=="useRserv") useRserv=((Boolean)val).booleanValue();    
        if (par=="holdConnection") holdConnection=((Boolean)val).booleanValue();
        if (par=="RservHost") RservHost=(String)val;
    }

    /** get a plugin parameter
        @param par parameter name
        @return parameter value or <code>null</code> if not availiable */
    public Object getParameter(String par) {
        if (par=="root" || par=="tree") return root;
        if (par=="Rversion") return Rver;
        if (par=="Rbin") return Rbin;
        if (par=="lastdump") return lastDump;
        if (par=="treeLibrary") return lib;
        if (par=="selectedOnly") return new Boolean(!useAll);
        if (par=="formula") return formula;
        if (par=="treeOptions") return treeOpt;
        if (par=="registerTree") return new Boolean(registerPar);
        if (par=="useRserv") return new Boolean(useRserv);
        if (par=="holdConnection") return new Boolean(holdConnection);
        if (par=="RservHost") return RservHost;
        return null;
    }

    BufferedReader in;

    /** initialize plugin insteance
        @return <code>true</code> if the initialization was successful */
    public boolean initPlugin() {
        if (useRserv) {
            rc=new Rconnection(RservHost);
            if (rc.isOk() && rc.isConnected()) {
                REXP rx=rc.eval("paste(version$major,version$minor,sep='.')");
                if (rc.isOk()) {
                    lastRver=Rver=rx.asString();
                    initializedSuccessfully=true;
                    if (!holdConnection) rc=null;
                    return true;
                }
            }
            rc=null; useRserv=false;
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
        d=new Dialog(f,"Generate Tree Plug-in",true);
        d.setBackground(Color.white);
        d.setLayout(new BorderLayout());
        d.add(new SpacingPanel(),BorderLayout.WEST);
        d.add(new SpacingPanel(),BorderLayout.EAST);
        Panel bp=new Panel(); bp.setLayout(new FlowLayout());
        bp.add(b=new Button("OK"));bp.add(b2=new Button("Cancel"));
        d.add(bp,BorderLayout.SOUTH);
        Choice c=new Choice();
        int j=0,lniv=0,lct=0; boolean frv=false;
        while(j<vs.count()) {
            if (!vs.at(j).isInternal()) { lniv=j; lct++; } // consider non-internal variables only
            j++;
        };
        List l=new List((lct>10)?10:lct,true);
        j=0;
        while(j<vs.count()) {
            if (!vs.at(j).isInternal()) { // consider non-internal variables only
                c.add(vs.at(j).getName());
                if (vs.at(j)==resp) {
                    c.select(vs.at(j).getName());
                    frv=true;
                };
                l.add(vs.at(j).getName());
            }
            j++;
        }
        j=0; while(j<lct) {
            l.select(j); j++;
        }
        if (!frv)
            c.select(vs.at(lniv).getName());
        Panel p=new Panel();
        p.setLayout(new BorderLayout());
        d.add(p);
        Panel lp=new Panel();
        lp.setLayout(new FlowLayout());
        lp.add(new Label("     Using R "+Rver+" in "+Rbin+"     "));
        d.add(lp,BorderLayout.NORTH);
        p.add(l,BorderLayout.NORTH);
        
        Panel bPanel=new Panel();
        TextField t=new TextField((treeOpt==null)?"":treeOpt,30);
        GridBagLayout gbl=new GridBagLayout();
        bPanel.setLayout(gbl);
        GridBagConstraints gcw = new GridBagConstraints();
        GridBagConstraints gce = new GridBagConstraints();
        gcw.gridx=0; gcw.anchor=GridBagConstraints.EAST;
        gce.gridx=1; gce.anchor=GridBagConstraints.WEST;
        gbl.setConstraints(bPanel.add(new Label("Response: ")),gcw);
        gbl.setConstraints(bPanel.add(c),gce);
        gbl.setConstraints(bPanel.add(new Label("Library:" )),gcw);
        Choice chLibrary=new Choice();
        chLibrary.add("rpart");
        chLibrary.add("tree");
        if (lib!=null) chLibrary.select(lib);
        gbl.setConstraints(bPanel.add(chLibrary),gce);
        gbl.setConstraints(bPanel.add(new Label("Parameters: ")),gcw);
        gbl.setConstraints(bPanel.add(t),gce);
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
        String [] sel=l.getSelectedItems();
        while(j<sel.length) {
            int vi=vs.indexOf(sel[j]);
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
            int vi=vs.indexOf(sel[j]);
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
        if (Common.DEBUG>0)
            System.out.println("formula: "+formula);
        treeOpt=t.getText();
        if (Common.DEBUG>0)
            System.out.println("tree options: \""+treeOpt+"\"");
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
        if (vs==null || pred==null || resp==null) {
            err="Dataset, predictors or response is empty!";
            System.out.println("execPlugin: ERR: "+err);
            return false;
        }
        try {
            String fprefix="";
            if (useRserv) {
                if (rc==null) {
                    rc=new Rconnection(RservHost);
                    if (!rc.isOk() || !rc.isConnected()) {
                        err="Rserv server does not respond ("+rc.getLastError()+") and native R is not configured. Start Rserv or re-initialize plugin for native R access.";
                        rc=null;
                        return false;
                    }
                    REXP rx=rc.eval("getwd()");
                    if (rx!=null && rx.asString()!=null) {
                        fprefix=rx.asString()+File.separator;
                    } else {
                        err="Cannot get current working directory ("+rc.getLastError()+")";
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
            p.print("invisible(options(echo = FALSE))\nlibrary("+lib+")\nd<-read.table(\"PluginInit.rds\",TRUE,\"\\t\",comment.char=\"\")\n");
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
                BufferedReader br=new BufferedReader(new FileReader(fprefix+"PluginInit.out"));
                lastTreeID++;
                root=RTree.Load(br,"GrownTree_"+lastTreeID,vs,0,"[1] TREE","[1] END",true,registerPar);
                if (root!=null) {
                    if (fr.exists()) fr.delete();
                    if (fd.exists()) fd.delete();
                    fo.delete();
                    System.out.println("Tree loaded!\n"+root.toString());
		    root.formula=formula; // set formula manually since RTree may have loaded partial formula only
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
            BufferedReader br=new BufferedReader(new FileReader("iris.rds"));
            RTree.Load(br,"iris",vs);
        } catch(Exception e) {
            System.out.println("cannot load test dataset (iris.rds), "+e.getMessage()); e.printStackTrace();
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
