/**
 * Klimt.java
 * Klimt - Interactive Trees 
 *
 * Created: Tue May  1 16:25:54 2001
 *
 * @author <a href="mailto:su@b-q-c.com">Simon Urbanek</a>
 * @version 0.94a $Id$
 */

package org.rosuda.klimt;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
/*SWING*/
import javax.swing.*;
/*ENDSWING*/

import org.rosuda.ibase.*;
import org.rosuda.ibase.toolkit.*;
import org.rosuda.util.*;
import org.rosuda.klimt.plots.*;
import org.rosuda.plugins.*;

//---------------------------------------------------------------------------
// Klimt
//---------------------------------------------------------------------------

/** Main Klimt Trees class. For historical reasons the main class of the KLIMT application
    is not Klimt (which is still provided as a wrapper) but this InTr (which stands for Interactive Trees, the
    original project name until it was renamed to Klimt) */
public class Klimt
{
    public static String Version = "0.98-pre1";
    public static String Release = "DB14";
    
    /** file name of the most recently loaded tree. Because of more recent support of multiple trees the use of the variable is deprecated for external packages. */
    public static String lastTreeFileName;

    /** this vector stores data roots of all data loaded in Klimt. each entry is of the class {@link DataRoot}. */
    static Vector data;

    /** return vector of all data roots */
    public static Vector getData() {
        if (data==null) data=new Vector();
        return data;
    }

    /** return corresponding {@link DataRoot} object to the specified dataset
        @param vs dataset
        @return root object or <code>null</code> if none was found
        */
    public static DataRoot getRootForData(SVarSet vs) {
        Vector v=getData();
        int l=v.size();
        int i=0;
        while (i<l) {
            DataRoot dr=(DataRoot) v.elementAt(i++);
            if (dr.getDataSet()==vs)
                return dr;
        }
        return null;
    }

    /** return corresponding {@link DataRoot} object to the specified dataset
        @param vs dataset
        @return root object or <code>null</code> if none was found
        */
    public static DataRoot getRootForTreeRegistry(TreeRegistry tr) {
        Vector v=getData();
        int l=v.size();
        int i=0;
        while (i<l) {
            DataRoot dr=(DataRoot) v.elementAt(i++);
            if (dr.getTreeRegistry()==tr)
                return dr;
        }
        return null;
    }

    /** add a new data root
        @param dr data root to add */
    public static void addData(DataRoot dr) {
        getData().addElement(dr);
    }

    /** create a new data root for the data set and return the corresponding root. if a root containing this dataset is already present, if will be returned without creating a new root. therefore it is safe to use this method for existing data sets.
        @param vs data set to add
        @return data root corresponding to the data set (created if necessary) */
    public static DataRoot addData(SVarSet vs) {
        if (Global.DEBUG>0)
            System.out.println("Klimt.addData("+vs+")");
        DataRoot xdr=getRootForData(vs);
        if (xdr==null) {
            xdr=new DataRoot(vs);
            addData(xdr);
        }
        if (Global.DEBUG>0)
            System.out.println("Klimt.addData: fetched DataRoot "+xdr);        
        return xdr;
    }
    
    /** creates a new tree display
	@param t root node
	@param tf frame of the tree
	@param x,y,w,h initial geometry of the canvas
	@return the newly created canvas */
    public static TreeCanvas newTreeDisplay(SNode t, TFrame tf,int x, int y, int w, int h) {
	if (t==null) return null;
	t.adjustDevGain();

	TFrame f=tf;
	if (f==null)
	    f=new TFrame("KLIMT "+Common.Version,TFrame.clsMain);
	
	TreeCanvas tc=new TreeCanvas(t,f);
	f.add(tc);
	f.addWindowListener(Common.getDefaultWindowListener());
	tc.setBounds(x,y,w,h);
	f.setBounds(x,y,w,h);
	f.pack();
	f.show();
	tc.redesignNodes();
	//t.printTree(" ");	
	return tc;
    };

    /** creates a new tree display with default geometry (0,0,800,500)
	@param t root node
	@param tf frame of the tree 
	@return the newly created canvas */
    public static TreeCanvas newTreeDisplay(SNode t, TFrame tf) {
	return newTreeDisplay(t,tf,0,0,800,500);
    };

    /** creates a new variables display
	@param vs the underlying dataset
	@param x,y,w,h initial geomery. Note: VarFrame itself modifies the height if necessary
	@return the newly created variables canvas */
    public static VarFrame newVarDisplay(DataRoot dr, int x, int y, int w, int h) {
	VarFrame VarF=new VarFrame(dr,x,y,w,h);
	return VarF;
    };

    /** creates a new variables display with default geometry (0,0,140,200)
	@param vs the underlying dataset
	@return the newly created variables canvas */
    public static VarFrame newVarDisplay(DataRoot dr) {
	return newVarDisplay(dr,0,0,140,200);
    };

    /** creates a pruned copy of a tree
	@param t root of the source tree
	@return copy of the tree without pruned nodes */
    public static SNode makePrunedCopy(SNode t) {
        return makePrunedCopy(t,false,null,true,null,null);
    }
    
    public static SNode makePrunedCopy(SNode t, boolean deepCopy, SNode cutpoint, boolean imTheRoot, Vector cps, String newName) 
    {
	SNode n=new SNode();
        if (imTheRoot) {
            RootInfo myRI=n.getRootInfo();
            RootInfo root=t.getRootInfo();
            myRI.name=(newName==null)?"Pr_"+root.name:newName;
            myRI.prediction=root.prediction;
            myRI.response=root.response;
	    myRI.formula=root.formula;
        }
	n.Cases=t.Cases; n.Cond=t.Cond;
        if (deepCopy) {
            /*BEGINNEW*/
            n.data=new Vector(t.data);
            /*ELSEOLD*
            n.data=new Vector();
            int i=0; while(i<t.data.size()) { n.data.addElement(t.data.elementAt(i)); i++; };
            *ENDNEW*/
        } else
            n.data=t.data;
        n.F1=t.F1; n.Name=t.Name; n.sel=0; n.id=t.id;
        n.sampleDev=t.sampleDev; n.sampleDevGain=t.sampleDevGain;
        n.splitComp=t.splitComp; n.splitIndex=t.splitIndex; n.predValD=t.predValD;
        n.splitVal=t.splitVal; n.splitValF=t.splitValF; n.splitVar=t.splitVar;
	n.V=t.V; n.vset=t.vset;
        if (cutpoint!=null && t==cutpoint && cps!=null)
            cps.addElement(n);
        if (!t.isLeaf() && (
                     (cutpoint==null && !t.isPruned()) ||
                     (cutpoint!=null && t!=cutpoint)))
	    for (Enumeration e=t.children(); e.hasMoreElements();) {
                SNode nc=makePrunedCopy((SNode)e.nextElement(),deepCopy,cutpoint,false,cps,null);
		n.add(nc);		
	    };
        return n;
    };

    public static SNode openTreeFile(Frame f,String fn,DataRoot dr) {
        return openTreeFile(f,fn,dr,false,true);
    }

    public static String lastUsedDir=null;
    
    /** loads a dataset and a tree from a file.
	@param f frame to be used for FileDialog if necessary
        @param fn filename of the source. If <code>null</code> {@link FileDialog} is used to let the user select the file
	@param tvs {@link SVarSet} object to be used for storage of the dataset.
        @param readOnlyDataset if set to <code>true</code> then tvs is not modified except for classifier
	@return root node of the tree or <code>null</code> if no tree was present. This methods returns <code>null</code> even if the dataset was loaded correcly and no tree was present. Total failure to process the file can be determined only by using clean dataset and check for size of the dataset after the call. */	
    public static SNode openTreeFile(Frame f,String fn, DataRoot dr,boolean readOnlyDataset,boolean createFrames)
    {
        if (Global.DEBUG>0)
            System.out.println("Klimt.openTreeFile("+f+","+fn+","+dr+","+readOnlyDataset+","+createFrames+")");
        SVarSet tvs=dr.getDataSet();
        TreeRegistry tr=dr.getTreeRegistry();
        SNode t=null;	
	String fnam=fn;
	try {
	    lastTreeFileName=fnam;
	    if (fnam==null) {
/*SWING*/
                if (Common.useSwing && tvs!=null && tvs.count()>0) {
                    JFileChooser chooser=null;
                    if (lastUsedDir!=null)
                        chooser=new JFileChooser(new File(lastUsedDir));
                    else
                        chooser = new JFileChooser();
                    chooser.setDialogTitle((tvs==null||tvs.count()==0)?"Select dataset file":"Select tree file(s)");
                    chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
                    chooser.setMultiSelectionEnabled(true);
                    int returnVal = chooser.showOpenDialog(f);
                    if(returnVal == JFileChooser.APPROVE_OPTION) {
                        File fs[]=chooser.getSelectedFiles();
                        if (fs!=null && fs.length>0) {
                            int fi=0;
                            while (fi<fs.length) {
                                BufferedReader r=new BufferedReader(new InputStreamReader(new FileInputStream(fs[fi])));
                                long fsz=0;
                                try {
                                    fsz=fs[fi].length();
                                } catch(Exception e) {};
                                t=TreeLoader.Load(r,fs[fi].getName(),dr);
                                if (t!=null && tvs!=null) {
                                    TFrame ff=null;
                                    t.getRootInfo().name=fs[fi].getName();
                                    if (createFrames) {
                                        t.getRootInfo().frame=ff=new TFrame(fs[fi].getName(),TFrame.clsTree);
                                        TreeCanvas tc=Klimt.newTreeDisplay(t,ff);
                                        tc.repaint(); tc.redesignNodes();                                        
                                    }
                                    tr.registerTree(t,fs[fi].getName());
                                };
                                fi++;
                            }
                        }
                    }
                    String wars=Common.getWarnings();
                    if (wars!=null) {
                        HelpFrame hf=new HelpFrame();
                        hf.t.setText("Following warnings were produced during dataset import:\n\n"+wars);
                        hf.setTitle("Load warnings");
                        //hf.setModal(true);
                        hf.show();
                    };
                    return t;
                };
/*ENDSWING*/
                FileDialog fd=new FileDialog(f,"Select data file");
		fd.setModal(true);
		fd.show();
		fnam=fd.getDirectory()+fd.getFile();
                lastUsedDir=fd.getDirectory();
		if (fd.getFile()!=null)
		    tvs.setName(lastTreeFileName=fd.getFile());
		else
		    return null;
            } else tvs.setName(fnam);
            if (Global.informLoader)
                System.out.println("InfoForLoader:Loading data...");
            BufferedReader r=new BufferedReader(new InputStreamReader(new FileInputStream(fnam)));
            Common.flushWarnings();
            long fsz=0;
            String fnn=fnam;
            try {
                File fil=new File(fnam);
                fnn=fil.getName();
                fsz=fil.length();
            } catch(Exception e) {};
            t=TreeLoader.Load(r,fnn,dr);
            if (t!=null) t.getRootInfo().name=fnn;
	    if (Global.DEBUG>0) tvs.printSummary();
	    if (tvs.getMarker()==null && (tvs.at(0)!=null)&&(tvs.at(0).size()>0))
		tvs.setMarker(new SMarker(tvs.at(0).size()));
            String wars=Common.getWarnings();
            if (wars!=null) {
                HelpFrame hf=new HelpFrame();
                hf.t.setText("Following warnings were produced during dataset import:\n\n"+wars);
                hf.setTitle("Load warnings");
                //hf.setModal(true);
                hf.show();
            };            
	} catch (Exception E) {
	    E.printStackTrace();
	    t=null;
	};
        if (t!=null && tr!=null) tr.registerTree(t,fnam);
        if (SplashScreen.recentOpen!=null) SplashScreen.recentOpen.addEntry((new File(fnam)).getAbsolutePath());

        return t;
    };

    /**
     * Main InTrees method, entry for KLIMT as stand-alone application.
     *
     * @param <code>argv</code> Run-time parameters. The syntax is as follows:<pre>
     * [--debug] source [source2 [source3 [...]]]
     *
     * source must contain a dataset and may contain a tree
     * source2... must contain a tree only
     * the --debug parameter switches debugging output on</pre>
     */
    public static void main(String[] argv)
    {
        boolean silentTreeLoad=false;
        int firstNonOption=-1;
        Common.appName="Klimt";
        Common.Version=Klimt.Version;
        if (Klimt.Release.compareTo(Common.Release)>0)
            Common.Release=Klimt.Release;
        argv=Global.parseArguments(argv);

        Platform.initPlatform("org.rosuda.klimt.");

        try {
	    int argc=argv.length;
	    int carg=0;

            while (carg<argv.length) {
                if (argv[carg].compareTo("--version")==0) {
                    System.out.println("KLIMT v"+Common.Version+" (Release "+Common.Release+")");
                    System.out.println("(C)Copyright 2001-3 Simon Urbanek (http://www.klimt-project.com)");
                    System.out.println("OS: "+System.getProperty("os.name")+" (version "+System.getProperty("os.version")+")");
                    return;
                };
                if (argv[carg].compareTo("--no-add")==0)
                    Common.noIntVar=true;
/*                if (argv[carg].compareTo("--start-Rserv")==0)
                    Common.startRserv=true; */
                if (firstNonOption==-1 && argv[carg].length()>0 && argv[carg].charAt(0)!='-')
                    firstNonOption=carg;
                carg++;
	    };
            if (Global.DEBUG>0)
                System.out.println("KLIMT v"+Common.Version+" (Release 0x"+Common.Release+")  "+
                                   ((Global.PROFILE>0)?"PROF ":"")+
                                   (Global.informLoader?"LOADER ":"")+
                                   (Global.useAquaBg?"AQUA ":"")+
                                   (silentTreeLoad?"SILENT ":""));
            
            PluginManager pm=PluginManager.getManager();
            Common.initValuesFromConfigFile(pm);
            String uRs=GlobalConfig.getS("Klimt.startRserve");
            if (uRs!=null && uRs.length()>0 && (uRs.charAt(0)=='y' || uRs.charAt(0)=='1')) {
                Common.startRserv=true;
                uRs=GlobalConfig.getS("Rserve.launch");
                if (uRs==null) uRs="R CMD Rserve";
                Plugin srp=PluginManager.loadPlugin("PluginDtartRserve");
                if (srp==null) {
                    if (Global.DEBUG>0)
                        System.out.println("** Cannot find PluginStartRserve.");
                } else {
                    if (Global.DEBUG>0)
                        System.out.println("Start of Rserv requested");
                    srp.initPlugin();
                    srp.setParameter("startCmd",uRs);
                    if (!srp.execPlugin())
                        if (Global.DEBUG>0)
                            System.out.println("Start of Rserve failed.");
                }
            }

            Dimension sres=Toolkit.getDefaultToolkit().getScreenSize();
            Common.screenRes=sres;

	    SVarSet tvs=new SVarSet();
            DataRoot dr=Klimt.addData(tvs);
            String fname=(firstNonOption>-1)?argv[firstNonOption]:null;
            if (fname==null || fname.length()<1 || fname.charAt(0)=='-') fname=null;

            if (Global.informLoader) {
                if (fname==null) System.out.println("InfoForLoader:Select file to load");
                else System.out.println("InfoForLoader:Loading data...");
            }
            
            if (fname!=null) {
                TFrame f=new TFrame("KLIMT "+Common.Version,TFrame.clsMain);
                Common.mainFrame=f;
                SNode t=openTreeFile(f,fname,dr);
                if (t==null && tvs.count()<1) {
                    new MsgDialog(f,"Load Error","I'm sorry, but I was unable to load the file you selected"+((fname!=null)?" ("+fname+")":"")+".");
                    System.exit(1);
                }

                if (Global.informLoader)
                    System.out.println("InfoForLoader:Setting up windows...");

                if (Global.DEBUG>0) {
                    for(Enumeration e=tvs.elements();e.hasMoreElements();) {
                        SVar vv=(SVar)e.nextElement();
                        System.out.println("==> "+vv.getName()+", CAT="+vv.isCat()+", NUM="+vv.isNum());
                        if (vv.isCat()) {
                            System.out.println("    categories: "+vv.getNumCats());
                        };
                    };
                }; 

                f.setTitle("KLIMT "+Common.Version+", "+tvs.getName()+" - tree");

                if (t!=null)
                    newTreeDisplay(t,f,0,0,sres.width-160,(sres.height>600)?600:sres.height-20);
                VarFrame vf=newVarDisplay(dr,sres.width-150,0,140,(sres.height>600)?600:sres.height-30);
                Common.mainFrame=vf;
            }

            KlimtSplash ss=new KlimtSplash();
            
	    carg=firstNonOption+1;
	    while (carg<argv.length) {
                if (argv[carg].compareTo("--silent")==0)
                    silentTreeLoad=true;                
                if (argv[carg].length()<2 || argv[carg].substring(0,2).compareTo("--")!=0) {
                    SNode ttt=Klimt.openTreeFile(Common.mainFrame,argv[carg],dr);
                    if (ttt!=null && !silentTreeLoad) {
                        TFrame fff=new TFrame(Klimt.lastTreeFileName,TFrame.clsTree);
                        TreeCanvas tc=Klimt.newTreeDisplay(ttt,fff);
                        tc.repaint(); tc.redesignNodes();
                    };
                };
		carg++;
	    };
            if (Global.informLoader)
                System.out.println("InfoForLoader:Done.");		
	} catch (Exception E) {
	    System.out.println("Something went wrong.");
	    System.out.println("LM: "+E.getLocalizedMessage());
	    System.out.println("MSG: "+E.getMessage());
	    E.printStackTrace();
        }
    }

    /** manages internal misclassification variable - that is creates new one if none exists
        or updates existing one
        @@param t root of the tree (prediction and response must be set in that node)
        @@param smcv system misclass variable or null if none exists yet
        @@return either smcv or newly created miscalss variable */
    public static SVar manageMisclassVar(SNode t, SVar smcv) {
        SVar mcv=smcv;
        SVar c=t.getRootInfo().prediction;
        SVar r=t.getRootInfo().response;
        if (c==null || r==null) return null;

        if (mcv!=null)
            mcv.getNotifier().beginBatch();
        if (mcv==null) {
            int j=0; mcv=new SVarObj("Misclass"); mcv.setInternalType(SVar.IVT_Misclass);
            mcv.getNotifier().beginBatch();
            while (j<c.size()) {
                if (c.at(j)!=null && r.at(j)!=null && c.at(j).toString().compareTo(r.at(j).toString())!=0)
                    mcv.add(new Integer(1));
                else
                    mcv.add(new Integer(0));
                j++;
            };
        } else {
            boolean wasCat=mcv.isCat();
            mcv.dropCat();
            int j=0;
            while (j<c.size()) {
                if (c.at(j)!=null && r.at(j)!=null && c.at(j).toString().compareTo(r.at(j).toString())!=0) {
                    int ct=mcv.atI(j)+1;
                    mcv.replace(j,new Integer(ct));
                }
                j++;
            };
            if (wasCat) mcv.categorize(true);
        }
        if (mcv!=null) mcv.getNotifier().endBatch();
        return mcv;
    }

    static int help11=0;

    public static SVar getPredictionVar(SNode t, SVar cv) {
        help11++;
        SVar v=null;
        v=new SVarObj("R_"+t.getRootInfo().name+"_"+help11,false); v.setInternalType(cv.isCat()?SVar.IVT_RCC:SVar.IVT_Resid);
        t.getSource().add(v);
        SVar nv=new SVarObj("N_"+t.getRootInfo().name+"_"+help11,true); nv.setInternalType(SVar.IVT_LeafID);
        t.getSource().add(nv);
        return getPredictionVar(t,cv,v,nv);
    }
    
    /** construct prediction variable, RCC/residuals variabe and node-index variable
        @@param t root of the tree
        @@param cv response variable (usually same as t.response)
        @@param tccv empty variable object for RCC (right class confidence) or residuals variable.
        if <code>null</code> then such variable is not created.
        @@param nids empty variable for node-IDs (numerical, discrete)
        @@return prediction variable corresponding to predictions made by the tree */
    public static SVar getPredictionVar(SNode t, SVar cv, SVar rccv, SVar nids) {
        Vector ns=new Vector();
        int maxid=cv.size();
        int cl[]= new int[maxid];
        //int nid[]= new int[maxid];
        SNode nod[] = new SNode[maxid];
        double cf[]=new double[maxid];
        boolean isCat=cv.isCat();
        int i=0; while(i<maxid) { cl[i]=-1; i++; };
        Object cat[]=null;
        if (isCat) cat=cv.getCategories();
        SNode root=(SNode)t.getRoot();
        int lid=1;

        t.getAllNodes(ns); // important - nodes are in prefix order, i.e. classification in this order is
                           // correct as no node has higher-order node in its path to root
        for (Enumeration e=ns.elements(); e.hasMoreElements();) {
            SNode n=(SNode)e.nextElement();
            if (true || n.isLeaf()) { // to catch all cases, even those with missing vals we consider all nodes
                                      // this won't be necessary if all cases will be dropped to leaf level (mv resolve)
                if (Global.DEBUG>0)
                    System.out.println("Leaf: "+n.toString());
                int j=-1;
                if (isCat) {
                    i=0; while(i<cat.length) {
                        if (cat[i].toString().compareTo(n.Name)==0) {
                            if (Global.DEBUG>0)
                                System.out.println("-- found at "+i+" ("+cat[i]+")");
                            j=i; break;
                        };
                        i++;
                    };
                };
                if(n.isLeaf()) n.tmp=lid++;
                if (!isCat || j>-1) {
                    int[] right=null;
                    if (isCat) right=new int[cat.length];
                    for (Enumeration f=n.data.elements(); f.hasMoreElements();) {
                        int cid=((Integer)f.nextElement()).intValue();
                        if (cid>=0 && cid<maxid) {
                            if (isCat) cl[cid]=j; //nid[cid]=n.id;
                            nod[cid]=n;
                            if (isCat) {
                                int ci=cv.getCatIndex(cid);
                                if (ci>-1 && ci<right.length) right[ci]++;
                            }
                        };
                    };
                    if (isCat && rccv!=null) {
                        for (Enumeration f=n.data.elements(); f.hasMoreElements();) {
                            int cid=((Integer)f.nextElement()).intValue();
                            if (cid>=0 && cid<maxid) {
                                cl[cid]=j;
                                int ci=cv.getCatIndex(cid);
                                if (ci>-1 && ci<right.length)
                                    cf[cid]=((double)right[ci])/((double)n.data.size());
                            }
                        }
                    }
                }
            }
        }
        SVar clv=new SVarObj((isCat?"C_":"P_")+t.getRootInfo().name+"_"+help11,isCat); clv.setInternalType(SVar.IVT_Prediction);
        clv.getNotifier().beginBatch();
        i=0; while(i<maxid) {
            if (isCat)
                clv.add((cl[i]==-1)?null:cat[cl[i]]);
            else
                clv.add((nod[i]==null)?null:new Double(nod[i].predValD));
            if (isCat && rccv!=null) rccv.add(new Double(cf[i]));
            if (!isCat && rccv!=null) rccv.add((nod[i]==null||clv.at(i)==null)?null:new Double(cv.atD(i)-nod[i].predValD));
            // warning nod[i]=null should not happen, but appeared when response contained NAs
            if (nids!=null) nids.add((nod[i]!=null && nod[i].isLeaf())?(new Integer(nod[i].tmp)):null);
            i++;
        }
        if (nids!=null) nids.sortCategories();
        clv.getNotifier().endBatch();
        return clv;
    }
}

