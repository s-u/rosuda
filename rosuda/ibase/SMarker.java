import java.util.*;

/*
class SMarkerOperation {
    public static const int MOP_SET    = 1;
    public static const int MOP_CLEAR  = 0;
    public static const int MOP_SWITCH = 2;
    
    int curOp;
    int curMark;
    IVector curIV;
    SMarker m;
    
    SMarkerOperation(SMarker M, int mark, int Op, ) {
	m=M; curMark=mark; curOp=Op; curIV=new IVector();
    };
    void public finalize() {
	
    };
};
*/  

/**
   Maintains a fixed list of markers, i.e. an array of 0..xxx indices that can be marked each.
   It uses both a fixed map and dynamic list of marked positions. Moreover it extends
   {@link Notifier} class to allow notification upon changes of state.
   @version $Id$
 */
public class SMarker extends Notifier implements Commander {
    /** Fixed array of markings. */
    int mask[];
    /** Size(length) of the fixed array of markings. */
    int msize;
    /** List of marked indices (each as <code>Integer</code> object). */
    Vector list;

    SVarSet masterSet;
    
    /* --- the "currentNode" code is rather experimental because it's not really clean - but still it allows us to use the marker message dispatching to include displaying of splits */    
    SNode currentNode;

    /** The only constructor, allocates a new marker
	@param reqsize desired size of the marker array (# of indices) */
    public SMarker(int reqsize) {
	mask=new int[reqsize];
	list=new Vector();
	msize=reqsize;
        masterSet=null;
	//curIV=null; curOp=0;
    };

    /** returns size of the marker array */
    public int size() { return msize; };

    /** returns the number of marked cases */
    public int marked() { return list.size(); };

    /** gets mark at index <code>pos</code>
	@param pos desired index
	@return mark at the index <code>pos</code> 
    */
    public int getMark(int pos) { return ((pos<0)||(pos>=msize))?0:mask[pos]; };

    /** checks whether index <code>pos</code> has any mark
	@param pos desired index
	@return <code>true</code> if there is any mark at the index <code>pos</code> 
    */
    public boolean at(int pos) { return ((pos<0)||(pos>=msize))?false:(mask[pos]!=0); };

    /** returs a list of all marked indices
	@return <code>Vector</code> of <code>Integer</code> objects */
    public Vector getList() { return list; };
    
    /** sets a mask at specified index
	@param pos desired index
	@param mark to be set (setting mark to 0 does delete any mark if present) */
    public void set(int pos, int mark) {
	if ((pos<0)||(pos>=msize)) return;
	if ((mask[pos]==0)&&(mark!=0)) list.addElement(new Integer(pos));
	if ((mask[pos]!=0)&&(mark==0)) list.removeElement(new Integer(pos));					   
	mask[pos]=mark;
    };

    /** modifies mark at specified index according ot the mask supplied (OR)
 	@param pos desired index
	@param mark marking-mask - will be binary ORed to the current mark resent, i.e. specified bits are set. */
    public void setMask(int pos, int mark) {
	if (mark==0) return;
	if ((pos<0)||(pos>=msize)) return;
	if (mask[pos]==0) list.addElement(new Integer(pos));
	mask[pos]|=mark;
    };
    
    /** modifies mark at specified index according ot the mask supplied (NAND)
 	@param pos desired index
	@param mark marking-mask - will be binary NANDed to the current mark resent, i.e. specified bits will be cleared. */
    public void delMask(int pos, int mark) {
	if (mark==0) return;
	if ((pos<0)||(pos>=msize)) return;
	if (mask[pos]==0) return;
	mask[pos]&=(-1)^mark;
	if (mask[pos]==0) list.removeElement(new Integer(pos));
    };
    
    /** returns <code>Enumeration</code> of the marked indices.
	@return <Enumeration> of marked indices (which are a list of <code>Integer</code> objects). Analogous to <code>emelents()</code> method of a <code>Vector</code> */
    public Enumeration elements() { return list.elements(); };

    /** clears entire selection (i.e. sets all marks to 0) */
    public void selectNone() {
	int i=0; while(i<msize) { mask[i]=0; i++; };
	list.removeAllElements();
    };

    /** marks all indices as marked with the specified mark
	@param mark mark to be used for the selection (passing 0 results in a call to {@link #selectNone} method instead) */
    public void selectAll(int mark) {
	if (mark==0) { selectNone(); return; };
	int i=0; 
	while(i<msize) {
	    if (mask[i]==0) list.addElement(new Integer(i)); 
	    mask[i]=mark;
	    i++; 
	};
    };	

    public Object run(Object o, String cmd) {
        if (cmd=="selAll") {
            selectAll(1); NotifyAll(new NotifyMsg(this,Common.NM_MarkerChange));
        };
        if (cmd=="selNone") {
            selectNone(); NotifyAll(new NotifyMsg(this,Common.NM_MarkerChange));
        }
        if (cmd=="selInv") {
            int i=0;
            while(i<msize) {
                if (mask[i]==0) {
                    list.addElement(new Integer(i));
                    mask[i]=1;
                } else {
                    list.removeElement(new Integer(i));
                    mask[i]=0;
                }
                i++;
            };
            NotifyAll(new NotifyMsg(this,Common.NM_MarkerChange));
        }
        return null;
    };
    
    /* --- the experimental currentNode code --- */
    public void setNode(SNode n) {
	currentNode=n; NotifyAll(new NotifyMsg(this,Common.NM_NodeChange));
    };
    public SNode getNode() { return currentNode; };

    //---
};
