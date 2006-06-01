package org.rosuda.ibase;

import java.util.*;

/**
   Maintains a fixed list of markers, i.e. an array of 0..xxx indices that can be marked each.
   It uses both a fixed map and dynamic list of marked positions. Moreover it extends
   {@link Notifier} class to allow notification upon changes of state.
 
   <p>This marker class implements two independent ways of tagging individual cases: primary and secondary markings.
  
   <p><b>primary marks</b> are binary only (i.e. on/off) and are implemented in a fashion that allows efficient access to both list of marked cases and fast query for a mark of a case. Main use is to implement highlighting.
 
   <p><b>secondary marks</b> allow (almost) arbitrary integers to be associated with individual cases. This kind of mark is not specially efficient. Main use is to implement color brushing.
 
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
	
	/** number of secondary marked cases */
	int secMarked=0;
    
    SVarSet masterSet;
    
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

    /** This methods allows to resize the marker. To ensure consistency the marker is never downsized. */
    public void resize(int newsize) {
        if (newsize<msize) return; // we can't allow downsizing
        list.removeAllElements();
        mask=new int[newsize];
        list=new Vector();
        msize=newsize;
        masterSet=null;
        maxMark=1; // it is never below 1 since 0 and 1 are used for primary mask
		secMarked=0;
    }
    
    /** returns size of the marker array */
    public int size() { return msize; };

    /** returns the number of marked cases */
    public int marked() { return list.size(); };

    /** gets mark at index <code>pos</code>
	@param pos desired index
	@return mark at the index <code>pos</code>. primary mask always returns -1 regardless of secondary mark
    */
    public int get(int pos) { return ((pos<0)||(pos>=msize))?0:(((mask[pos]&1)==1)?-1:(mask[pos]>>1)); };

    /** gets secondary mask at insex <code>pos</code> */
    public int getSec(int pos) { return ((pos<0)||(pos>=msize))?0:(mask[pos]>>1); };

    /** checks whether index <code>pos</code> has any mark
	@param pos desired index
	@return <code>true</code> if there is a primary mark at the index <code>pos</code> 
    */
    public boolean at(int pos) { return ((pos<0)||(pos>=msize))?false:((mask[pos]&1)==1); };

    /** returns a list of all marked indices
	@return <code>Vector</code> of <code>Integer</code> objects */
    public Vector getList() { return list; };

    /** returns an array with all selected IDs */
    public int[] getSelectedIDs() {
        int i=0, j=list.size();
        int[] l=new int[j];
        while (i<j) { l[i]=((Integer)list.elementAt(i)).intValue(); i++; }
        return l;
    }

    public static final int MASK_PRIMARY   = 0;
    public static final int MASK_SECONDARY = 1;
    public static final int MASK_RAW       = 2;

    /** get mask of the mark entries (as a newly allocated copy) - that is an array of the same size as the data containing a mark for each case. The values depend on the mask type specified by the parameter: <ul><li>{@link MASK_PRIMARY}: 0 - no mark, -1 - primary mark set (selection). secondary mark is ignored.</li><li>{@link MASK_SECONDARY}: any value 0,1,2,... corresponding to the secondary mark. primary mark is ignored.</li><li>MASK_RAW: mask as stored internally. currently LSB defines the primary mark and the remaining bits are used for the secondary mark, however this layout may change in the future, therefore it's not wise to rely on this unless in special cases for efficiency reasons.</li></ul>
        @param maskType one of the constants {@link MASK_PRIMARY},{@link MASK_SECONDARY} or {@link MASK_RAW}, see above */
    public int[] getMaskCopy(int maskType) {
        int mc[] = new int[mask.length];
        System.arraycopy(mask, 0, mc, 0, mask.length);
        if (maskType==MASK_PRIMARY) {
            int i=0; while (i<mc.length) { if ((mc[i]&1)==1) mc[i]=-1; i++; }
        }
        if (maskType==MASK_SECONDARY) {
            int i=0; while (i<mc.length) { mc[i]>>=1; i++; }
        }
        return mc;
    }
    
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
		if (mark>0 && (mask[pos]>>1)==0) secMarked++;
		else if (mark==0 && (mask[pos]>>1)>0) secMarked--;
        mask[pos]=(mask[pos]&1)|mark;
    }

    /** sets secondary mark of all selected cases */
    public void setSelected(int mark) {
	if (mark>maxMark) maxMark=mark;
        mark<<=1;
        for(Enumeration e=list.elements(); e.hasMoreElements();) {
            Integer i=(Integer)e.nextElement();
            if (i!=null) {
                int id=i.intValue();
				if (mark>0 && (mask[id]>>1)==0) secMarked++;
				else if (mark==0 && (mask[id]>>1)>0) secMarked--;
                mask[id]=(mask[id]&1)|mark;
            }
        }
    }

	/** returns the highest secondary mark. The return value does not necessarily reflect the highest currently used mark - it should be used as upper bound only. */
    public int getMaxMark() { return maxMark; }
	
	/** returns the number of cases having a secondary mark (irrespective of any primary mark). */
	public int getSecCount() { return secMarked; }
    
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

	/** removes all secondary marks */
	public void resetSec() {
		if (secMarked>0) {
			int i=0;
			while (i<msize) mask[i++]&=1;
		}
		maxMark=1;
		secMarked=0;
	}
	
    /** returns master dataset associated with this marker. Please note that a marker doesn't have to be associated with any SVarSet */
    public SVarSet getMasterSet() {
        return masterSet;
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
    }

    public void setSecBySelection(int markSel, int markNonsel) {
        boolean[] ids = new boolean[mask.length];
        int[] selIds = getSelectedIDs();
        
        for(int i=0; i<selIds.length; i++) {
            setSec(selIds[i],markSel);
            ids[selIds[i]] = true;
        }
        
        for(int i=0; i<ids.length; i++) if(!ids[i]) setSec(i,markNonsel);
    }
}
