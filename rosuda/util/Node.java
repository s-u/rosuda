package org.rosuda.util;

import java.util.*;

/**
   Implements the tree structure consisting of cascaded (unmarked) nodes. Each instance of <code>Node</code> represents a node.
   @version $Id$
*/
public class Node
{
    /** List of all child nodes (all of object type {@link Node}) */
    Vector ch;
    /** Parent node */
    Node  par;
    
    /** Depth (level) of this node. Root has a depth of 0 (cached value) */
    int level;
    /** Height of the tree downwards (i.e. assuming this node is the root). A single node has the height of 0 */
    int height;

    /** constructs an empty, lonely node */
    public Node() {} ;

    /** constructs a new root node as the parent of a subtree
	@param t subtree */
    public Node(Node t) {
	if (t!=null) {
	    ch=new Vector();
	    ch.addElement(t);
	    t.par=this;
	    t.level=level+1; 
	    t.rebuildLevels();
	    rebuildHeight();
	};
    };
    
    /** checks whether this node is a leaf
	@return <code>true</code> if this node is a leaf (i.e. has no children) */
    public boolean isLeaf() { return ((ch==null)||(ch.isEmpty())); };
    /** checks whether this node is a root
	@return <code>true</code> if this node is a root (i.e. has no parent) */
    public boolean isRoot() { return (par==null); };
    /** returns a list of all children
	@return an array of all children (as {@link Node} objects or <code>null</code> if this node is a leaf */
    public Node[] getChildren() {
	if ((ch==null)||(ch.isEmpty())) return null;
	Node[] a=new Node[ch.size()];
	ch.copyInto(a);
	return a; 
    };
    /** returns # of children
	@return # of children (or 0 if this node is a leaf) */
    public int count() { return (ch==null)?0:ch.size(); };
    /** returns the subnode (child) at specified index
 	@param pos index of the subnode (child). First child is at index 0, last at {@link #count}-1. Returns <code>null</code> if range is invalid or node has no children.
	@return subnode as <code>Node</code> object */
    public Node at(int pos) {
	return ((ch==null)||(pos<0)||(pos>=ch.size()))?null:(Node)ch.elementAt(pos);
    };
    /** adds a subtree as a child
	@param n node (root of the subtree) to be added */
    public void add(Node n) {
	if (ch==null) ch=new Vector();
	ch.addElement(n); n.par=this;  n.level=level+1; n.rebuildLevels();
	if (n.height+1>height) height=n.height+1;
	if (par!=null) par.rebuildHeight();
    };

    /** prune this node from its parent. As a result this node becomes a root and the parent loses one child. The method does nothing if this node is already a root. */
    public void prune() {
	if (par!=null) {
	    par.remove(this);	    
	    par=null;
	    level=0; rebuildLevels();
	};
    };

    /** remove a node from current children.
	@param l child to be removed */
    public void remove(Node l) {
	if ((l==null)||(l.par!=this)) return;
	ch.removeElement(l);
	l.par=null; l.level=0; l.rebuildLevels();
	rebuildHeight();
    };
    
    /* print the tree on System.out (mainly for debugging purposes)
	@param prefix text to be printed before each line of output
    public void printTree(String prefix) {
	System.out.println(prefix + " " + ((ct==null)?"[node]":ct.toString())+" {level="+level+"; height="+height+"}");
	if ((ch!=null)&&(ch.size()>0))
	    for (Enumeration e=ch.elements(); e.hasMoreElements();)
		((Node)e.nextElement()).printTree(prefix+"  ");
    };
    */

    /** returns children enumeration
	@return {@link Enumeration} of children of <code>null</code> if leaf.*/
    public Enumeration children() { return (ch==null)?null:ch.elements(); };
    
    /** returns parent node
	@return paret node (or <code>null</code> if this node is a root) */
    public Node getParent() { return par; };
    
    /** fills provided vector with all nodes at specified level (works downwards only, so makes sense basically only if used with roots) *experimental*
	@param dest level of nodes to be added
	@param nodes Vector to be used for storage of the nodes (as Node objects). This method does no sanity checks, so ensure your vector is valid */
    public void getNodesAtLevel(int dest, Vector nodes)
    {
	if (level>dest) return;
	if (level==dest) {
	    nodes.addElement(this); return;
	};
	if ((ch!=null)&&(ch.size()>0))
	    for (Enumeration e=ch.elements(); e.hasMoreElements();)
		((Node)e.nextElement()).getNodesAtLevel(dest,nodes);
    };

    /** returns root if the tree
	@return root of the tree (equals to <code>this</code> if the node is already the root) */
    public Node getRoot() { return (par==null)?this:par.getRoot(); };

    /** returns the level of this node (i.e. the distance from the root)
	@return level of this node (0 for root) */
    public int getLevel() {
	//return (par==null)?0:1+par.getLevel(); 
	return level;
    };

    /** returns the height of the tree from this node downwards
	@return tree height (0 for a leaf) */
    public int getHeight() { return height; };

    /** fills specified vector with all nodes contained in the tree (from this node downward, including) in prefix order
     *  (i.e. this node is stored as first)
     *  @param dstv destination vector. Objects of type {@link Node} are stored. 
     *  It's not cleared implicitely. Expect a null-exception if you pass <code>null</code> instead of a valid vector. */
    public void getAllNodes(Vector dstv) {	
	dstv.addElement(this);
	if ((ch!=null)&&(ch.size()>0))
	    for (Enumeration e=ch.elements(); e.hasMoreElements();)
		((Node)e.nextElement()).getAllNodes(dstv);
    };

    /** returns # of nodes in the tree from this node downwards
	(incl. current one).
 	@param leavesOnly if set to <code>true</code>, only leaves are counted.
	@return # of nodes */
    public int getNumNodes(boolean leavesOnly) {
	int ic=0;
	if ((!leavesOnly)||(isLeaf())) ic=1;

	if ((ch!=null)&&(ch.size()>0))
	    for (Enumeration e=ch.elements(); e.hasMoreElements();)
		ic+=((Node)e.nextElement()).getNumNodes(leavesOnly);
	return ic;
    };
    
    /** rebuilds levels cache for this node and all subnodes (is called internally after updates. There should be no need to call this method externally) */
    public void rebuildLevels() {
	if ((ch!=null)&&(ch.size()>0))
	    for (Enumeration e=ch.elements(); e.hasMoreElements();) {
		Node cch=(Node)e.nextElement(); 
		cch.level=level+1;
		cch.rebuildLevels();
	    };
    };
    
    /** rebuilds tree height cache for this node and upwards (is called internally after updates. There should be no need to call this method externally) */
    public void rebuildHeight() {
	height=0;
	if ((ch!=null)&&(ch.size()>0))
	    for (Enumeration e=ch.elements(); e.hasMoreElements();) {
		Node cch=(Node)e.nextElement(); 		
		if (cch.height+1>height)
		    height=cch.height+1;
	    };
	if (par!=null) par.rebuildHeight();
    };    
};
