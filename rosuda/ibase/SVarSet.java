import java.util.*;
import java.io.*;

/** implements set of variables (aka dataset, used as data source)
    which consists basically of a set of variables (of class {@link SVar}) and
    a marker (of class {@link SMarker}).
    @version $Id$
*/
public class SVarSet {
    /** vector of {@link SVar} objects - the variables */
    Vector vars;
    /** marker associated with this dataset */
    SMarker mark;
    /** dataset name */
    String name;

    public class TreeEntry {
        public SNode root;
        public String name;
        public TreeEntry(SNode t, String n) {
            root=t; name=n;
        }
    }
    
    /** list of associated trees */
    Vector trees=null;
    
    int globalMisclassVarID=-1;
    int classifierCounter=1;

    /** default constructor of empty dataset */
    SVarSet() { vars=new Vector(); name="<unknown>"; };
    
    /** sets the marker for this dataset
	@param m marker */
    public void setMarker(SMarker m) { mark=m; m.masterSet=this; };
    /** returns the marker of this dataset
	@return marker */
    public SMarker getMarker() { return mark; };
    /** set dataset name
	@param s name */
    public void setName(String s) {
	name=s;
    };
    /** get dataset name
	@return dataset name */
    public String getName() { return name; };

    public int length() {
        int len=0;
        int i=0, l=vars.size();
        while (i<l) {
            int vl=((SVar)vars.elementAt(i)).size();
            if (vl>len) len=vl;
            i++;
        }
        return len;
    }
    
    /** add a new varaible to the dataset 
	@param v variable
	@return index of the newly create variable or <0 if an error occured (-1 on null-problems, -2 if that variable-name already exists) */
    public int add(SVar v) {  // return position or -1 on error or -2 if name exists
	if (v==null) return -1;
	String nn=v.getName(); if (nn==null) return -1;
	
	if (!vars.isEmpty()) {
	    for (Enumeration e=vars.elements(); e.hasMoreElements();) {
		SVar n=(SVar)e.nextElement();
		if (n.getName().compareTo(nn)==0) return -2;
	    };
	};
	
	vars.addElement(v);
	return vars.indexOf(v);
    };

    /** returns the index of a variable specified by its name
	@param nam variable name
	@return index of the variable (or -1 if not found) */
    public int indexOf(String nam) {
	int i=0;
	while (i<vars.size()) {
	    SVar n=(SVar)vars.elementAt(i);
	    if (n.getName().compareTo(nam)==0) return i;
	    i++;
	};
	return -1;
    };

    /** returns variable object specified by name
 	@param nam variable name
	@return variable object or <code>null</code> if not found. */
    public SVar byName(String nam) {
	if (vars.isEmpty()) return null;
	for (Enumeration e=vars.elements(); e.hasMoreElements();) {
	    SVar n=(SVar)e.nextElement();
	    if (n.getName().compareTo(nam)==0) return n;
	};
	return null;
    };

    /** returns variable object at specified index
	@param i index
	@return variable object or <code>null</code> if index out of range */
    public SVar at(int i) {
	return ((i<0)||(i>=vars.size()))?null:(SVar)vars.elementAt(i);
    };

    /** returns data value of a variable specified by name and row index
	@param nam variable name
	@param row row index
	@return data object (or <code>null</code> if out of range) */
    public Object data(String nam, int row) {
	SVar v=byName(nam);
	return (v==null)?null:v.elementAt(row);
    };
    
    /** returns data value of a variable specified by variable index and row index
	@param col variable index
	@param row row index
	@return data object (or <code>null</code> if out of range) */
    public Object data(int col, int row) {
	SVar v=at(col);
	return (v==null)?null:v.elementAt(row);
    };
    
    /** returns enumeration of all variable objects in this dataset
	@return object enumeration (of type {@link SVar}) */
    public Enumeration elements() { return vars.elements(); };    
    
    /** returns number of variables in this dataset
	@return # of variables */
    public int count() { return vars.size(); };

    /** register a tree with the dataset */
    public void registerTree(SNode t, String n) {
        if (trees==null) trees=new Vector();
        trees.addElement(new TreeEntry(t,n));
    }

    public boolean Export(PrintStream p, boolean all) { return Export(p,all,null); }
    public boolean Export(PrintStream p) { return Export(p,true,null); }
    public boolean Export(PrintStream p, boolean all, int vars[]) {
        boolean exportAll=all || mark==null || mark.marked()==0;
        try {
            if (p!=null) {
                int j=0,tcnt=0,fvar=0;
                j=0;
                if (vars==null || vars.length<1) {
                    while(j<count()) {
                        p.print(((tcnt==0)?"":"\t")+at(j).getName());
                        if (tcnt==0) fvar=j;
                        tcnt++;
                        j++;
                    }
                } else {
                    while(j<vars.length) {
                        p.print(((tcnt==0)?"":"\t")+at(vars[j]).getName());
                        if (tcnt==0) fvar=vars[j];
                        tcnt++;
                        j++;
                    }
                }
                p.println("");
                int i=0;
                while (i<at(fvar).size()) {
                    if (exportAll || mark.at(i)) {
                        j=fvar;
                        j=0;
                        if (vars==null || vars.length<1) {
                            while(j<count()) {
                                Object oo=at(j).at(i);
                                p.print(((j==0)?"":"\t")+((oo==null)?"NA":oo.toString()));
                                j++;
                            }
                        } else {
                            while(j<vars.length) {
                                Object oo=at(vars[j]).at(i);
                                p.print(((j==0)?"":"\t")+((oo==null)?"NA":oo.toString()));
                                j++;
                            }
                        }
                        p.println("");
                    }
                    i++;
                };
            };
            return true;
        } catch (Exception eee) {
            if (Common.DEBUG>0) {
                System.out.println("* SVarSet.Export...: something went wrong during the export: "+eee.getMessage()); eee.printStackTrace();
            };
        };
        return false;
    }

    public boolean exportForest(PrintStream p) {
        try {
            if (p!=null) {
                p.println("Tree\tVar\ttree.dev\ttree.gain\ttree.size\tsample.dev\tsample.gain\tsample.size\tdepth");
                SVarSet.TreeEntry te;
                if (Common.DEBUG>0) System.out.println("Forest export; total "+trees.size()+" trees associated.");
                for (Enumeration e=trees.elements(); e.hasMoreElements();) {
                    te=(SVarSet.TreeEntry)e.nextElement();
                    if (Common.DEBUG>0) System.out.println("exporting tree \""+te.name+"\"...");
                    if (te.root!=null) {
                        Vector v=new Vector();
                        te.root.getAllNodes(v);
                        if (Common.DEBUG>0) System.out.println(" total "+v.size()+" nodes.");
                        for (Enumeration e2=v.elements(); e2.hasMoreElements();) {
                            SNode np=(SNode)e2.nextElement();
                            if (!np.isLeaf()) {
                                SNode n=(SNode)np.at(0);
                                if (n!=null) {
                                    p.println(te.name+"\t"+n.splitVar.getName()+"\t"+np.F1+"\t"+np.devGain+"\t"+n.Cases+"\t"+np.sampleDev+"\t"+np.sampleDevGain+"\t"+np.data.size()+"\t"+np.getLevel());
                                };
                            }
                        }
                    }
                }
                p.close();
                return true;
            };
        } catch (Exception eee) {};
        return false;
    }

    public SNode[] getTrees() {
        SNode[] ts=new SNode[trees.size()];
        int i=0;
        SVarSet.TreeEntry te;
        for (Enumeration e=trees.elements(); e.hasMoreElements();) {
            te=(SVarSet.TreeEntry)e.nextElement();
            ts[i++]=te.root;
        }
        return ts;
    }
    
    public SVarSet getForestVarSet() {
        SVarSet fs=new SVarSet(); fs.setName("Forest");
        SVar v_tree=new SVar("Tree",true); fs.add(v_tree);
        SVar v_node=new SVar("NodeID"); fs.add(v_node);
        SVar v_var=new SVar("Variable",true); fs.add(v_var);
        SVar v_vspl=new SVar("Split.num.value"); fs.add(v_vspl);
        SVar v_scases=new SVar("s.cases"); fs.add(v_scases);
        SVar v_tcases=new SVar("t.cases"); fs.add(v_tcases);
        SVar v_sd=new SVar("s.deviance"); fs.add(v_sd);
        SVar v_td=new SVar("t.deviance"); fs.add(v_td);
        SVar v_sdg=new SVar("s.dev.Gain"); fs.add(v_sdg);
        SVar v_tdg=new SVar("t.dev.Gain"); fs.add(v_tdg);
        SVar v_root=new SVar("Root"); v_root.setContentsType(SVar.CT_Tree); fs.add(v_root);

        SVarSet.TreeEntry te;
        if (Common.DEBUG>0) System.out.println("Forest export; total "+trees.size()+" trees associated.");
        for (Enumeration e=trees.elements(); e.hasMoreElements();) {
            te=(SVarSet.TreeEntry)e.nextElement();
            if (Common.DEBUG>0) System.out.println("including tree \""+te.name+"\"...");
            if (te.root!=null) {
                Vector v=new Vector();
                te.root.getAllNodes(v);
                if (Common.DEBUG>0) System.out.println(" total "+v.size()+" nodes.");
                for (Enumeration e2=v.elements(); e2.hasMoreElements();) {
                    SNode np=(SNode)e2.nextElement();
                    if (!np.isLeaf()) {
                        SNode n=(SNode)np.at(0);
                        if (n!=null) {
                            if (Common.DEBUG>0)
                                System.out.println(te.name+", var="+n.splitVar.getName()+", cond="+n.Cond+", svF="+n.splitValF+", F1="+np.F1+", dg="+np.devGain+", cases="+n.Cases+", sd="+np.sampleDev+", sdg="+np.sampleDevGain+", ds="+np.data.size()+", lev="+np.getLevel());
                            v_tree.add(te.name); v_var.add(n.splitVar.getName());
                            v_root.add(te.root);
                            v_node.add(new Integer(n.id)); v_scases.add(new Integer(np.data.size()));
                            v_tcases.add(new Integer(np.Cases));
                            v_sdg.add(new Double(np.sampleDevGain)); v_sd.add(new Double(np.sampleDev));
                            v_tdg.add(new Double(np.devGain)); v_td.add(new Double(np.F1));
                            v_vspl.add(new Double(n.splitValF));
                        };
                    }
                }
            }
        }
        SMarker m=new SMarker(v_var.size());
        fs.setMarker(m);
        return fs;
    }
    
    public static void Debug(SVarSet sv) {
	System.out.println("DEBUG for SVarSet ["+sv.toString()+"]");
	for (Enumeration e=sv.elements(); e.hasMoreElements();) {
	    SVar v2=(SVar)e.nextElement();
	    System.out.println("Variable: "+v2.getName()+" ("+(v2.isNum()?"numeric":"string")+
			       ","+(v2.isCat()?"categorized":"free")+") with "+
			       v2.size()+" cases");
	    if (v2.isCat()) {
		Object[] c=v2.getCategories();
		System.out.print("  Categories: ");
		int i=0;
		while (i<c.length) {
		    System.out.print("{"+c[i].toString()+"} ");
		    i++;
		};
		System.out.println();
	    };
	};       
    };
};
