//
//  TreeRegistry.java
//  Klimt
//
//  Created by Simon Urbanek on Tue Jul 29 2003.
//  Copyright (c) 2003 __MyCompanyName__. All rights reserved.
//

package org.rosuda.klimt;

import java.util.Vector;
import java.util.Enumeration;
import java.io.PrintStream;

import org.rosuda.ibase.*;
import org.rosuda.util.*;

public class TreeRegistry {
    /** list of associated trees - each of the class {@link TreeEntry} */
    Vector trees=null;

    /** register a tree with the dataset */
    public void registerTree(SNode t, String n) {
        if (trees==null) trees=new Vector();
        if (!contains(t))
            trees.addElement(new TreeEntry(t,n));
        t.getRootInfo().home=this;
    }

    /** checks whether a specific root is registered in the registry */
    public boolean contains(SNode r) {
        if (trees==null) return false;
        for (Enumeration e = trees.elements() ; e.hasMoreElements() ;) {
            TreeEntry te=(TreeEntry) e.nextElement();
            if (te!=null && te.root==r) return true;
        }
        return false;
    }

    /** checks whether a tree with the specified name is present in the registry */
    public boolean contains(String name) {
        if (trees==null) return false;
        for (Enumeration e = trees.elements() ; e.hasMoreElements() ;) {
            TreeEntry te=(TreeEntry) e.nextElement();
            if (te!=null && te.name==name) return true;
        }
        return false;
    }
    
    /** this method equals to calling registerTree(t,t.getRootInfo().name) and is provided for backwards compatibility with older applications */
    public void addTree(SNode t) {
        registerTree(t,t.getRootInfo().name);
    }

    /** export forest data of teh forest formed by the trees in this registry */
    public boolean exportForest(PrintStream p) {
        try {
            if (p!=null) {
                p.println("Tree\tVar\ttree.dev\ttree.gain\ttree.size\tsample.dev\tsample.gain\tsample.size\tdepth");
                TreeEntry te;
                if (Global.DEBUG>0) System.out.println("Forest export; total "+trees.size()+" trees associated.");
                for (Enumeration e=trees.elements(); e.hasMoreElements();) {
                    te=(TreeEntry)e.nextElement();
                    if (Global.DEBUG>0) System.out.println("exporting tree \""+te.name+"\"...");
                    if (te.root!=null) {
                        Vector v=new Vector();
                        te.root.getAllNodes(v);
                        if (Global.DEBUG>0) System.out.println(" total "+v.size()+" nodes.");
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

    public Vector getTreeEntries() {
        return trees;
    }
    
    public SNode[] getRoots() {
        SNode[] ts=new SNode[trees.size()];
        int i=0;
        TreeEntry te;
        for (Enumeration e=trees.elements(); e.hasMoreElements();) {
            te=(TreeEntry)e.nextElement();
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

        TreeEntry te;
        if (Global.DEBUG>0) System.out.println("Forest export; total "+trees.size()+" trees associated.");
        for (Enumeration e=trees.elements(); e.hasMoreElements();) {
            te=(TreeEntry)e.nextElement();
            if (Global.DEBUG>0) System.out.println("including tree \""+te.name+"\"...");
            if (te.root!=null) {
                Vector v=new Vector();
                te.root.getAllNodes(v);
                if (Global.DEBUG>0) System.out.println(" total "+v.size()+" nodes.");
                for (Enumeration e2=v.elements(); e2.hasMoreElements();) {
                    SNode np=(SNode)e2.nextElement();
                    if (!np.isLeaf()) {
                        SNode n=(SNode)np.at(0);
                        if (n!=null) {
                            if (Global.DEBUG>0)
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
}
