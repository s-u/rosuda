/**
 * InTr.java
 * Klimt - Interactive Trees 
 *
 * Created: Tue May  1 16:25:54 2001
 *
 * @author <a href="mailto:su@b-q-c.com">Simon Urbanek</a>
 * @version 0.94a $Id$
 */

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;

//---------------------------------------------------------------------------
// InTr
//---------------------------------------------------------------------------

/** Main Interactive Trees class. For historical reasons the main class of the KLIMT application
    is not Klimt (which is still provided as a wrapper) but this InTr (which stands for Interactive Trees, the
    original project name until it was renamed to Klimt) */
public class InTr
{
    /** file name of the most recently loaded tree. Because of more recent support of multiple trees the use of the variable is deprecated for external packages. */
    public static String lastTreeFileName;
   
    /** creates a new tree display
	@param t root node
	@param tf frame of the tree
	@param x,y,w,h initial geometry of the canvas
	@return the newly created canvas */
    public static TreeCanvas newTreeDisplay(SNode t, TFrame tf,int x, int y, int w, int h) {
	if (t==null) return null;
	adjustDevGain(t);

	TFrame f=tf;
	if (f==null)
	    f=new TFrame("KLIMT "+Common.Version);
	
	TreeCanvas tc=new TreeCanvas(t,f);
	f.add(tc);
	if (Common.defaultWindowListener==null)
	    Common.defaultWindowListener=new DefWinL();
	f.addWindowListener(Common.defaultWindowListener);
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
    public static VarFrame newVarDisplay(SVarSet vs,int x, int y, int w, int h) {
	VarFrame VarF=new VarFrame(vs,x,y,w,h);
	return VarF;
    };

    /** creates a new variables display with default geometry (0,0,140,200)
	@param vs the underlying dataset
	@return the newly created variables canvas */
    public static VarFrame newVarDisplay(SVarSet vs) {
	return newVarDisplay(vs,0,0,140,200);
    };

    /** creates a pruned copy of a tree
	@param t root of the source tree
	@return copy of the tree without pruned nodes */
    public static SNode makePrunedCopy(SNode t) 
    {
	SNode n=new SNode();
	n.Cases=t.Cases; n.Cond=t.Cond; n.data=t.data;
	n.F1=t.F1; n.Name=t.Name; n.sel=0;
	n.splitComp=t.splitComp; n.splitIndex=t.splitIndex;
	n.splitVal=t.splitVal; n.splitValF=t.splitValF;
	n.V=t.V; n.vset=t.vset;
	if (!t.isLeaf() && !t.isPruned())
	    for (Enumeration e=t.children(); e.hasMoreElements();) {
		SNode nc=makePrunedCopy((SNode)e.nextElement());
		n.add(nc);		
	    };
	return n;
    };

    /** loads a dataset and a tree from a file.
	@param f frame to be used for FileDialog if necessary
        @param fn filename of the source. If <code>null</code> {@link FileDialog} is used to let the user select the file
	@param tvs {@link SVarSet} object to be used for storage of the dataset.
	@return root node of the tree or <code>null</code> if no tree was present. This methods returns <code>null</code> even if the dataset was loaded correcly and no tree was present. Total failure to process the file can be determined only by using clean dataset and check for size of the dataset after the call. */	
    public static SNode openTreeFile(Frame f,String fn,SVarSet tvs)
    {
	SNode t=null;	
	String fnam=fn;
	try {
	    lastTreeFileName=fnam;
	    if (fnam==null) {
		FileDialog fd=new FileDialog(f,"Select data and tree file");
		fd.setModal(true);
		fd.show();
		fnam=fd.getDirectory()+fd.getFile();
		if (fd.getFile()!=null)
		    tvs.setName(lastTreeFileName=fd.getFile());
		else
		    return null;
	    } else tvs.setName(fnam);
	    
	    BufferedReader r=new BufferedReader(new InputStreamReader(new FileInputStream(fnam)));
            Common.flushWarnings();
            long fsz=0;
            try {
                File fil=new File(fnam);
                fsz=fil.length();
            } catch(Exception e) {};
            t=RTree.Load(r,tvs,fsz);
	    if (Common.DEBUG>0) SVarSet.Debug(tvs);
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
	try {
	    int argc=argv.length;
	    int carg=0;
	    
	    if ((argc>0)&&(argv[0].compareTo("--debug")==0)) {
		Common.DEBUG=1; carg++;
		System.out.println("KLIMT v"+Common.Version+" (Release 0x"+Common.Release+")");
	    };
	    
 	    TFrame f=new TFrame("KLIMT "+Common.Version);
	    Common.mainFrame=f;
	    
	    SVarSet tvs=new SVarSet();
            String fname=(argc>carg)?argv[carg]:null;
            if (fname==null || fname.length()<1 || fname.charAt(0)=='-') fname=null;
	    SNode t=openTreeFile(f,fname,tvs);
	    if (t==null && tvs.count()<1) {
                new MsgDialog(f,"Load Error","I'm sorry, but I was unable to load the file you selected"+((fname!=null)?" ("+fname+")":"")+".");
		System.exit(1);
	    };

	    if (Common.DEBUG>0) {
		for(Enumeration e=tvs.elements();e.hasMoreElements();) {
		    SVar vv=(SVar)e.nextElement();
		    System.out.println("==> "+vv.getName()+", CAT="+vv.isCat()+", NUM="+vv.isNum());
		    if (vv.isCat()) {
			System.out.println("    categories: "+vv.getNumCats());
		    };
		};
	    }; 
	    
	    f.setTitle("KLIMT "+Common.Version+", "+tvs.getName()+" - tree");

	    Dimension sres=Toolkit.getDefaultToolkit().getScreenSize();
            Common.screenRes=sres;
	    if (t!=null)
		newTreeDisplay(t,f,0,0,sres.width-160,(sres.height>600)?600:sres.height-20);
	    VarFrame vf=newVarDisplay(tvs,sres.width-150,0,140,(sres.height>600)?600:sres.height);
	    Common.mainFrame=vf;		
	    
	    carg++;
	    while (carg<argv.length) {
		SNode ttt=InTr.openTreeFile(Common.mainFrame,argv[carg],tvs);
		if (ttt!=null) {
		    TFrame fff=new TFrame(InTr.lastTreeFileName);
		    TreeCanvas tc=InTr.newTreeDisplay(ttt,fff);
		    tc.repaint(); tc.redesignNodes();		
		};
		carg++;
	    };  
		
	} catch (Exception E) {
	    System.out.println("Something went wrong.");
	    System.out.println("LM: "+E.getLocalizedMessage());
	    System.out.println("MSG: "+E.getMessage());
	    E.printStackTrace();
	};
    };

    /** adjusts cached deviance gain for an entire subtree
	@param t root of the subtree */
    static void adjustDevGain(SNode t) {
	if (t==null) return;
	double myDev=t.F1;
	if (t.isLeaf()) {
	    t.devGain=0;
	} else {
	    for (Enumeration e=t.children(); e.hasMoreElements();) {
		SNode c=(SNode)e.nextElement();
		if (c!=null) {
		    myDev-=c.F1;
		    adjustDevGain(c);
		};
	    };
	    t.devGain=myDev;
	};
    };
};
