import java.util.*;

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

    /** highest used mark - for friend functions that need to allocate arrays for mask maps */
    int maxMark;
    
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
        maxMark=1; // it is never below 1 since 0 and 1 are used for primary mask
	//curIV=null; curOp=0;
    };

    /** returns size of the marker array */
    public int size() { return msize; };

    /** returns the number of marked cases */
    public int marked() { return list.size(); };

    /** gets mark at index <code>pos</code>
	@param pos desired index
	@return mark at the index <code>pos</code>. primary mask always returns 1 regardless of secondary mark
    */
    public int get(int pos) { return ((pos<0)||(pos>=msize))?0:(((mask[pos]&1)==1)?-1:(mask[pos]>>1)); };

    /** gets secondary mask at insex <code>pos</code> */
    public int getSec(int pos) { return ((pos<0)||(pos>=msize))?0:(mask[pos]>>1); };

    /** checks whether index <code>pos</code> has any mark
	@param pos desired index
	@return <code>true</code> if there is a primary mark at the index <code>pos</code> 
    */
    public boolean at(int pos) { return ((pos<0)||(pos>=msize))?false:((mask[pos]&1)==1); };

    /** returs a list of all marked indices
	@return <code>Vector</code> of <code>Integer</code> objects */
    public Vector getList() { return list; };
    
    /** sets the primary mark at specified index
	@param pos desired index
	@param pMark whether the primary mark is set
        */
    public void set(int pos, boolean pMark) {
	if ((pos<0)||(pos>=msize)) return; // out of range
        if (pMark==((mask[pos]&1)==1)) return; // no change
        if (((mask[pos]&1)==0)&&(pMark)) list.addElement(new Integer(pos));
        if (((mask[pos]&1)==1)&&(!pMark)) list.removeElement(new Integer(pos));
	mask[pos]^=1;
    }

    /** sets secondary mark */
    public void setSec(int pos, int mark) {
        if (mark>maxMark) maxMark=mark;
        mark<<=1;
        mask[pos]=(mask[pos]&1)|mark;
    }

    /** sets secondary mark of all selected cases */
    public void setSelected(int mark) {
        mark<<=1;
        for(Enumeration e=list.elements(); e.hasMoreElements();) {
            Integer i=(Integer)e.nextElement();
            if (i!=null) {
                int id=i.intValue();
                mask[id]=(mask[id]&1)|mark;
            }
        }
    }

    public int getMaxMark() { return maxMark; }
    
    /** returns <code>Enumeration</code> of the marked indices.
	@return <Enumeration> of marked indices (which are a list of <code>Integer</code> objects). Analogous to <code>emelents()</code> method of a <code>Vector</code> */
    public Enumeration elements() { return list.elements(); };

    /** clears entire selection (i.e. sets all primary marks to 0) */
    public void selectNone() {
	int i=0; while(i<msize) { mask[i]&=(-1)^1; i++; };
	list.removeAllElements();
    };

    /** marks all indices as marked with the specified mark
	@param mark mark to be used for the selection (passing 0 results in a call to {@link #selectNone} method instead) */
    public void selectAll() {
        list.removeAllElements();
	int i=0; 
	while(i<msize) {
	    list.addElement(new Integer(i)); 
	    mask[i]|=1;
	    i++; 
	};
    };	

    public void selectInverse() {
        int i=0;
        while(i<msize) {
            if ((mask[i]&1)==0) {
                list.addElement(new Integer(i));
                mask[i]|=1;
            } else {
                list.removeElement(new Integer(i));
                mask[i]&=(-1)^1;
            }
            i++;
        };
    }

    public Object run(Object o, String cmd) {
        if (cmd=="selAll") {
            selectAll(); NotifyAll(new NotifyMsg(this,Common.NM_MarkerChange));
        };
        if (cmd=="selNone") {
            selectNone(); NotifyAll(new NotifyMsg(this,Common.NM_MarkerChange));
        }
        if (cmd=="selInv") {
            selectInverse(); NotifyAll(new NotifyMsg(this,Common.NM_MarkerChange));
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
