import java.util.Vector;
import java.util.Enumeration;

/**
   Statistical {@link Node} class. It's used for storing statistical information in tree nodes.
   @version $Id$
*/
public class SNode extends Node implements Cloneable
{
    /** list of indices of the data used in this node */
    Vector data;
    /** Data source of this node */
    SVarSet vset;

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
    /** values (percentage of each class) */
    public Vector V;

    /** pruning flag. if set to <code>true</code> then all <i>children</i> has been pruned. beware, that does mean that the node has to check prune flag of its parent to see if it's been pruned also. */
    boolean pruned;

    /** position of the node and its geometry */
    public int x,y,x2,y2;

    /** selector (0=not sel, 1=selected node, 2=leaf of sel. category, 3=non-leaf of selected category */
    public int sel=0;

    /** index of the split variable */
    public int splitIndex;
    /** object of the splitting variable */
    SVar splitVar;
    /** split compare type (-1/0/1) */
    public int splitComp;
    /** value of the split condition as string */
    String splitVal;
    /** value of the split condition as string */
    double splitValF;

    /** user definable temporary variable */
    int tmp;

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
    };

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
	
    /** returns basic string representation of this node (suitable for debugging only)
	@return string representation of the contained data */
    public String toString()
    {
	return "SNode[cond=\""+Cond+"\",cases="+Cases+",F1="+F1+",Name=\""+Name+"\"]";
    };
};
