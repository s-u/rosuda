package org.rosuda.klimt;

import java.util.*;
import java.awt.Rectangle;

import org.rosuda.ibase.*;
import org.rosuda.util.*;
import org.rosuda.ibase.toolkit.*;

/**
   Statistical {@link Node} class. It's used for storing statistical information in tree nodes.
   @version $Id$
*/
public class SNode extends Node implements Cloneable
{
    /** list of indices of the data used in this node */
    public int data[];
    /** Data source of this node */
    SVarSet vset;
    /** root information. this should be <code>null</code> for non-root nodes. use {@link #getRootInfo()} to obrain root information from any node */
    RootInfo rootInfo;
    
    /** splitting condition as string */
    public String Cond;
    /** # of cases */
    public int Cases;
    /** deviation (or Gini-index) of this node */
    public double F1;
    /** deviation gain by this split */
    public double devGain;
    /** name of tha class assigned to this node */
    public String Name;
    /** predicted value (for regr. trees) */
    public double predValD;
    /** values (percentage of each class) */
    public Vector V;
    /** pruning flag. if set to <code>true</code> then all <i>children</i> has been pruned. beware, that does mean that the node has to check prune flag of its parent to see if it's been pruned also. */
    boolean pruned;
    
    /** id of the node */
    public int id;
    
    /** deviance in this node based on the sample */
    public double sampleDev;
    /** deviance gain based on the sample */
    public double sampleDevGain;

    /** position of the node and its geometry  (replaces former x,y,x2,y2) */
    public int cx,cy,width,height;

    /** underflow warning - if <code>true</code> then the node is displayed bigger than it should be */
    public boolean underflowWarning=false;
    /** overflow warning - if <code>true</code> then the node is bigger than its display representation */
    public boolean overflowWarning=false;
     
    /** selector (0=not sel, 1=selected node, 2=leaf of sel. category, 3=non-leaf of selected category */
    public int sel=0;

    /** index of the split variable */
    public int splitIndex;
    /** object of the splitting variable */
    public SVar splitVar;
    /** split compare type (-1/0/1) */
    public int splitComp;
    /** value of the split condition as string */
    public String splitVal;
    /** value of the split condition as string */
    public double splitValF;

    /** label rectangle */
    public Rectangle labelR;
    
    /** user definable temporary variable */
    public int tmp;

    /** default constructor */
    public SNode() { this(null); };

    /** constructs a new root node as the parent of a subtree
	@param t subtree */
    public SNode(Node t) { super(t); V=new Vector(); pruned=false; };

    /** sets data source
	@param src data source */
    public void setSource(SVarSet src) {
	vset=src; 
	// --- from the STree version:
	//if ((ch!=null)&&(ch.size()>0))
	//    for (Enumeration e=ch.elements(); e.hasMoreElements();)
	//	((STree)e.nextElement()).setSource(src);
    }

    /** returns root information associated with this tree
        @return root information */
    public RootInfo getRootInfo() {
        SNode n=(SNode) getRoot();
        if (n.rootInfo==null) n.rootInfo=new RootInfo();
        return n.rootInfo;
    }

    public void setFormula(String form) {
        getRootInfo().formula=form;
    }

    public String getFormula() {
        return getRootInfo().formula;
    }
    
    /** returns data source
	@return data source */
    public SVarSet getSource() { return vset; };

    /** set prune flag for this node and all children
	@param ps prune state to be set */
    public void setPrune(boolean ps) {
	pruned=ps;
	if (!isLeaf())
	    for (Enumeration e=ch.elements(); e.hasMoreElements();)
		((SNode)e.nextElement()).setPrune(ps);	
    };
    
    /** gets pruned flag
	@return pruned flag */
    public boolean isPruned() { return pruned; };

    /** print the tree on System.out (mainly for debugging purposes)
	@param prefix text to be printed before each line of output */
    public void printTree(String prefix) {
	System.out.println(prefix + " " + toString() +" {level="+level+"; height="+height+"}");
	if ((ch!=null)&&(ch.size()>0))
	    for (Enumeration e=ch.elements(); e.hasMoreElements();)
		((SNode)e.nextElement()).printTree(prefix+"  ");
    };

    /** sets temporary custom variable of the entire subtree to v */
    public void setAllTmp(int v) {
	tmp=v;
	if ((ch!=null)&&(ch.size()>0))
	    for (Enumeration e=ch.elements(); e.hasMoreElements();)
		((SNode)e.nextElement()).setAllTmp(v);	
    };

    /** calculates deviance and deviance gain based on the sample stored in data. note that
        this deviance may be different from the F1/devGain if the tree was based on another sample */
    public void calculateSampleDeviances() {
        if (data==null || data.length==0) return;
        double d=0;
        double nt=data.length;
        int rnt=0;
        SVar rv=getRootInfo().response;
        if (rv==null) return;
        if (rv.isCat()) {
            int cs=rv.getNumCats();
            int cts[]=new int[cs];
            int i=0; while(i<cs) {
                cts[i]=0; i++;
            }
            int e=0;
            while (e<data.length) {
                i=data[e++];
                int cix=rv.getCatIndex(i);
                if (cix>=0 && cix<cs) {
                    cts[cix]++;
                    rnt++;
                }
            }
            i=0;
            while (i<cs) {
                if (cts[i]>0) d+=((double)cts[i])*Math.log(((double)cts[i])/((double)rnt));
                i++;
            }
            sampleDev=-d;
        } else {
            double pv=predValD;
            double ds=0.0;
            int vsz=rv.size();
            int e=0;
            while (e<data.length) {
                int i=data[e++];
                if (i<vsz && rv.at(i)!=null)
                    ds+=(rv.atD(i)-pv)*(rv.atD(i)-pv);
            }
            sampleDev=ds;
        }
        double chdev=0;
        if ((ch!=null)&&(ch.size()>0))
            for (Enumeration e=ch.elements(); e.hasMoreElements();) {
                SNode cn=(SNode)e.nextElement();
                cn.calculateSampleDeviances();
                chdev+=cn.sampleDev;
            };
        sampleDevGain=isLeaf()?0:sampleDev-chdev;
    }

    /** returns the ID of this node in a binary tree (root=1) or -1 if the tree is not a binary tree */
    public int getBinaryID() {
        if (par==null) return 1;
        SNode p=(SNode) par;
        if (p.ch==null) return -1;
        int pcs=p.ch.size();
        if (pcs>2 || pcs<1) return -1;
        // this is just for the paranoid: we make sure that the parent has a reference to us (it should)
        if ((pcs==1 && p.ch.elementAt(0)!=this) ||
            (pcs==2 && p.ch.elementAt(0)!=this && p.ch.elementAt(1)!=this)) return -1;
        int pid=p.getBinaryID();
        return (p.ch.elementAt(0)==this)?(2*pid):(2*pid+1);
    }
    
    /** pass data from node ct down to this node according to the conditions in this node */
    public void passDownData(SNode ct) {
        SVar V=splitVar;
        int cmp=splitComp;
        String cond=splitVal;
        double limit=splitValF;
        int dv[]=new int[ct.data.length]; // tmp array - can't be bigger than the parent
        int dvs=0;
        if (V!=null) {
            boolean lexi=!V.isNum();
            int ei=0;
            while (ei<ct.data.length) {
                int I=ct.data[ei++];
                if (I<V.size()) {
                    if (lexi) {
                        if (cond.indexOf(",")>-1) { // more cats specified
                            StringTokenizer st=new StringTokenizer(cond,",");
                            while (st.hasMoreTokens()) {
                                String cd=st.nextToken();
                                Object oo=V.at(I);
                                if (oo==null) oo=SVar.missingCat;
                                int cr=oo.toString().compareTo(cd);
                                if (cr>0) cr=1;
                                if (cr<0) cr=-1;
                                if (cr==cmp) dv[dvs++]=I;
                            }
                        } else {
                            int cr=V.at(I).toString().compareTo(cond);
                            if (cr>0) cr=1;
                            if (cr<0) cr=-1;
                            if (cr==cmp) dv[dvs++]=I;
                        }
                    } else {
                        if (!V.isMissingAt(I)) {
                            double F=V.atF(I);
                            if (((cmp==0)&&(F==limit))||
                                ((cmp==-1)&&(F<=limit))||
                                ((cmp==1)&&(F>limit))) dv[dvs++]=I;
                        }
                    }
                }
            }
            data=new int[dvs];
            System.arraycopy(dv,0,data,0,dvs);
            dv=null;
            if (!isLeaf()) {
                for (Enumeration e=children(); e.hasMoreElements();) {
                    SNode n=(SNode)e.nextElement();
                    n.passDownData(this);
                }
            }
        } else data=new int[0];
    }

    /** adjusts cached deviance gain for an entire subtree
        @param t root of the subtree */
    void adjustDevGain() {
        double myDev=F1;
        if (isLeaf()) {
            devGain=0;
        } else {
            for (Enumeration e=children(); e.hasMoreElements();) {
                SNode c=(SNode)e.nextElement();
                if (c!=null) {
                    myDev-=c.F1;
                    c.adjustDevGain();
                }
            }
            devGain=myDev;
        }
    }
    
    /** returns basic string representation of this node (suitable for debugging only)
	@return string representation of the contained data */
    public String toString()
    {
	return "SNode[cond=\""+Cond+"\",cases="+Cases+",F1="+F1+",Name=\""+Name+"\"]";
    };
};
