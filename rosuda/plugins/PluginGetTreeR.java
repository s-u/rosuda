import java.io.*;
import java.awt.*;
import java.awt.event.*;

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
    String dsfile="treeGenData.rds";
    String Rver="?";
    String Rbin="R";

    String lastDump=null;
    
    PluginGetTreeR() {
        name="Generate tree via R";
        author="Simon Urbanek <simon.urbanek@math.uni-augsburg.de>";
        desc="Grows classification or regression trees using R";
        type=PT_GenTree;
    }
    public void setParameter(String par, Object val) {
        if (par=="dataset") vs=(SVarSet)val;
    }
    public Object getParameter(String par) {
        if (par=="root" || par=="tree") return root;
        if (par=="Rversion") return Rver;
        if (par=="Rbin") return Rbin;
        if (par=="lastdump") return lastDump;
        return null;
    }

    BufferedReader in;
    
    public boolean initPlugin() {
        try {
            File fr=new File("PluginInit.r"); if (fr.exists()) fr.delete();
            File fo=new File("PluginInit.out"); if (fo.exists()) fo.delete();
            PrintStream p=new PrintStream(new FileOutputStream("PluginInit.r"));
            p.println("print(\"PLUGIN_INIT\",quote=FALSE)");
            //p.println("invisible(options(echo = FALSE))\nlibrary(tree)\ndata(iris)\nprint(\"TREE\",quote=FALSE)\nt<-tree(Species~.,iris)\nprint(t)\nprint(formula(terms(t)))\nprint(\"END\",quote=FALSE)\n");
            p.close();

            Process pc=null;
            try {
                Rbin="R";
                pc=Runtime.getRuntime().exec(Rbin+" --slave --no-save --no-restore CMD BATCH PluginInit.r PluginInit.out");
            } catch (Exception e1) {
                try {
                    Rbin="/usr/bin/R";
                    pc=Runtime.getRuntime().exec(Rbin+" --slave --no-save --no-restore CMD BATCH PluginInit.r PluginInit.out");
                } catch (Exception e2) {
                    try {
                        Rbin="/usr/local/bin/R";
                        pc=Runtime.getRuntime().exec(Rbin+" --slave --no-save --no-restore CMD BATCH PluginInit.r PluginInit.out");
                    } catch (Exception e3) {
                        try {
                            Rbin="/sw/bin/R";
                            pc=Runtime.getRuntime().exec(Rbin+" --slave --no-save --no-restore CMD BATCH PluginInit.r PluginInit.out");
                        } catch (Exception e4) {
                            err="Cannot find R executable!"; return false;
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
                return true;
            }
        } catch(Exception e) {
            System.out.println("initPlugin: "+e.getMessage()); err="While trying to detect R: "+e.getMessage(); e.printStackTrace();
        }
        return false;
    }

    /*
    boolean killRead=false;
    
    public void run() {
        System.out.println("read thread started\n");
        try {
            while(!killRead) {
                String s=in.readLine();
                System.out.println("R: "+s);
            }
        } catch(Exception e) {
            System.out.println("run: "+e.getMessage()); e.printStackTrace();
        }
        System.out.println("read thread finished\n");
    }
    */
    
    public boolean checkParameters() {
        return vs!=null;
    }

    Dialog d;
    
    public boolean pluginDlg(Frame f) {
        Button b=null;
        d=new Dialog(f,"Generate Tree Plug-in",true);
        d.setLayout(new BorderLayout());
        d.add(b=new Button("OK"),BorderLayout.SOUTH);
        Choice c=new Choice();
        List l=new List((vs.count()>10)?10:vs.count(),true);
        int j=0;
        while(j<vs.count()) {
            c.add(vs.at(j).getName());
            l.add(vs.at(j).getName());
            j++;
        }
        j=0; while(j<vs.count()) {
            l.select(j); j++;
        }
        Panel p=new Panel();
        p.setLayout(new BorderLayout());
        d.add(p);
        d.add(new Label("Using R "+Rver+" in "+Rbin),BorderLayout.NORTH);
        p.add(c);
        p.add(l,BorderLayout.NORTH);
        TextField t=new TextField("");
        p.add(t,BorderLayout.SOUTH);
        d.pack();
        b.addActionListener(this);
        d.setVisible(true);
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

        StringBuffer sb=new StringBuffer(resp.getName());
        sb.append("~");
        j=0;
        while(j<vc-1) {
            if (j>0) sb.append("+");
            sb.append(vs.at(pred[j]).getName());
            j++;
        }
        formula=sb.toString();
        System.out.println("formula: "+formula);
        d.dispose();
        return true;
    }
    
    public void actionPerformed(ActionEvent e) {
        d.setVisible(false);
    };
    
    public boolean execPlugin() {
        lastDump=null;
        System.out.println("execPlugin");
        if (vs==null || pred==null || resp==null) {
            err="Dataset, predictors or response is empty!";
            System.out.println("execPlugin: ERR: "+err);
            return false;
        }
        try {
            File fd=new File("PluginInit.rds"); if (fd.exists()) fd.delete();
            File fr=new File("PluginInit.r"); if (fr.exists()) fr.delete();
            File fo=new File("PluginInit.out"); if (fo.exists()) fo.delete();
            PrintStream dp=new PrintStream(new FileOutputStream("PluginInit.rds"));
            vs.Export(dp,true,pred);
            dp.close();
            System.out.println("dataExported");

            if (treeOpt==null) treeOpt="";
            if (treeOpt.length()>0) treeOpt=","+treeOpt;
            PrintStream p=new PrintStream(new FileOutputStream("PluginInit.r"));
            p.println("invisible(options(echo = FALSE))\nlibrary(tree)\nd<-read.table(\"PluginInit.rds\",TRUE,\"\\t\",comment.char=\"\")\nprint(\"TREE\",quote=FALSE)\nt<-tree("+formula+",d"+treeOpt+")\nprint(t)\nprint(formula(terms(t)))\nprint(\"END\",quote=FALSE)\n");
            p.close();
            System.out.println("execPlugin: starting R");
            Process pc=Runtime.getRuntime().exec(Rbin+" --slave --no-save --no-restore CMD BATCH PluginInit.r PluginInit.out");
            System.out.println("execPlugin: waiting for R to finish");            
            pc.waitFor();
            System.out.println("execPlugin: R finished");

            if (fr.exists()) fr.delete();
            if (!fo.exists()) {
                err="Unable to use R! Make sure R is installed and in your PATH.";
                System.out.println("execPlugin: ERR: "+err);
                return false;
            } else {
                BufferedReader br=new BufferedReader(new FileReader("PluginInit.out"));
                root=RTree.Load(br,vs,0,"[1] TREE","[1] END");
                if (root!=null) {
                    fo.delete();
                    System.out.println("Tree loaded!\n"+root.toString());
                    return true;
                }
                err="Commands executed, but no tree was found.";
                br.close();
                br=new BufferedReader(new FileReader("PluginInit.out"));
                StringBuffer dump=new StringBuffer();
                while(br.ready()) {
                    dump.append(br.readLine()); dump.append("\n");
                } br.close();
                fo.delete();
                lastDump=dump.toString();
                System.out.println("execPlugin: ERR: "+err+"lastDump:\n"+lastDump);
                return false;
            }
        } catch(Exception e) {
            System.out.println("execPlugin: "+e.getMessage()); err="White trying to run R: "+e.getMessage(); e.printStackTrace();
        }
        System.out.println("execPlugin: unexpectedly reched return, ERR: "+err);
        return false;
    }

    public static void main(String[] args) {
        SVarSet vs=new SVarSet();
        try {
            BufferedReader br=new BufferedReader(new FileReader("iris.rds"));
            RTree.Load(br,vs);
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
