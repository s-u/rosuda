import java.util.*;
//import SMarker;

/**
 * <b>Statistical Variable</b>
 * <p><u>IMPORTANT NOTES:</u>
 * <pre>
 * NOTE: do NOT remove any elements from SVar !! Any Vector methods that modify contents
 *       except for "add" are NOT safe (for categorized vars) as they DON'T update categories!!
 * NOTE: NEVER call addElement of SVar - unfortunately addElement is defined as 'final' in
 *       JDK 1.1 so it cannot be overriden. When using 1.2 API or higher you may uncomment
 *       the override for security purpose, but in that case it won't compile on 1.1.
 *
 * The "is number" property is set accoding to the first added element - thus you should NOT mix
 * numeric and non-numeric objects. It is safe to insert numbers in non-numeric value, but NOT
 * vice-versa as for numeric variables the detection of min/max casts any object to Number.</pre>
 * @version $Id$
 */
public class SVar extends Vector
{
    /** variable name */
    String  name;
    /** type of variable, <code>true</code> if categorial variable */
    boolean cat;
    /** vector of categories if cat. var. */
    Vector  cats; 
    /** vector if counts per category */
    Vector  ccnts;
    /** type of variable, <code>true</code> if numeric (i.e. subclass of <code>Number</code>) */
    boolean isnum;
    /** hasNull is true if any case contains null = missing values */
    boolean hasNull;

    /** specifies whether add(..) will try to guess numerical variables based on the first value added or not */
    boolean guessNum=true;
    
    /** minimal value (if numeric variable) */
    double min;
    /** maximal value (if numeric variable) */
    double max;

    /** # of missing cases in the variable */
    int missingCount=0;
    
    public static final String missingCat = "NA";

    /** notifier for changes */
    public Notifier notify;
    
    /** construct new variable and add first element
	@param Name variable name
	@param iscat <code>true</code> if categorial variable
	@param first first element to be added - see {@link #add} for details. If <code>null</code> is passed then no element is added. The usage of this constructor is discouraged/deprecated because first entry cannot contain a missing value thus leading to a loss of generality. */
    public SVar(String Name, boolean iscat, Object first) 
    {
	name=Name;
	cat=iscat;
	isnum=false;
	hasNull=false;

	if (iscat) {
	    cats=new Vector(); ccnts=new Vector();
	};
	
	if (first!=null) add(first);
        notify=new Notifier();
    };
    /** construct new variable (equals to <code>SVar(Name,iscat,null)</code>)
 	@param Name variable name
	@param iscat <code>true</code> if categorial variable */
    public SVar(String Name, boolean iscat) { this(Name,iscat,null); };
    /** construct new variable (equals to <code>SVar(Name,false,null)</code>)
 	@param Name variable name */
    public SVar(String Name) { this(Name,false,null); };

    /** sets the {@link #guessNum} flag. It must me set before the first add(..) call because the guess is made
        based on the first added object (hence makes no sense if {@link #SVar(String,boolean,Object)} was used). */
    public void tryToGuessNum(boolean doit) {
        guessNum=doit;
    };
    
    /** define the variable explicitely as categorial
	@param rebuild if set to <code>true</code> force rebuild even if the variable is already categorial. */
    public void categorize(boolean rebuild) {
	if (cat && !rebuild) return;
	cats=new Vector(); ccnts=new Vector();
	cat=true;
	if (!isEmpty()) {
	    for(Enumeration e=elements();e.hasMoreElements();) {
		Object oo=e.nextElement();
		if (oo==null) oo=missingCat;
		int i=cats.indexOf(oo);
		if (i==-1) {
		    cats.addElement(oo);
		    ccnts.addElement(new Integer(1));
		} else {
		    ccnts.setElementAt(new Integer(((Integer)ccnts.elementAt(i)).intValue()+1),i);
		};
	    };
            if (isNum()) { // if numerical and categorical then sort categories for convenience
                sortCategories();
            };
	};
        notify.NotifyAll(new NotifyMsg(this,Common.NM_VarTypeChange));
    };

    public static final int SM_lexi = 0; /** sort method lexicograph. */
    public static final int SM_num  = 1; /** sort method numerical (all objects are cast to Number, non-castable are assigned -0.01,
                                             basically to appear just before zero) */

    /** sort caregotires, using default method which is numerical for num. variables, lexicogr. otherwise */
    public void sortCategories() {
        sortCategories(isNum()?SM_num:SM_lexi);
    }
    /** sort categories by specifeid method
        @param method sort method, see SM_xxx constants */
    public void sortCategories(int method) {
        if (!isCat()) return;
        Vector ocats=cats; Vector occnts=ccnts;
        cats=new Vector(); ccnts=new Vector();
        boolean found=true;
        int cs=ocats.size();
        while (found) {
            found=false; int i=0,p=-1;
            double min=-0.01; boolean gotmin=false;
            String mino=null;
            while (i<cs) {
                Object o=ocats.elementAt(i);
                if (o!=null) {
                    if (method==SM_num) {
                        double val=-0.01;
                        try {
                            val=((Number)o).doubleValue();
                        } catch(Exception e) {};
                        if (!gotmin) {
                            gotmin=true; min=val; p=i;
                        } else {
                            if (val<min) {
                                min=val; p=i;
                            }
                        }
                    } else {
                        if (!gotmin) {
                            gotmin=true; mino=o.toString(); p=i;
                        } else {
                            if (mino.compareTo(o)>0) {
                                mino=o.toString(); p=i;
                            }
                        }
                    }
                }
                i++;
            }
            if (found=gotmin) {
                cats.add(ocats.elementAt(p)); ccnts.add(occnts.elementAt(p));
                ocats.setElementAt(null,p);
            }            
        }
    };
    /** define the variable explicitely as categorial (equals to calling {@link #categorize(boolean) categorize(false)}) */
    public void categorize() { categorize(false); };
    /** define the variable explicitely as non-categorial (drop category list) */ 
    public void dropCat() {
	cats=null; ccnts=null; cat=false;
        notify.NotifyAll(new NotifyMsg(this,Common.NM_VarTypeChange));
    };

    /** adds a new case to the variable (NEVER use addElement! see package header) Also beware, categorial varaibles are classified by object not by value!
     *  @param o object to be added. First call to <code>add</code> (even implicit if an object was specified on the call to the constructor) does also decide whether the variable will be numeric or not. If the first object is a subclass of <code>Number</code> then the variable is defined as numeric. There is a significant difference in handling numeric and non-numeric variabels, see package header.
     *  @return <code>true<code> if element was successfully added, or <code>false</code> upon failure - currently when non-numerical value is inserted in a numerical variable. It is strongly recommended to check the result and act upon it, because failing to do so can result in non-consistent datasets - i.e. mismatched row IDs */
    public boolean add(Object o) {
        if (o==null) {
            missingCount++;
            hasNull=true;
        }
	if (o!=null && size()==missingCount && guessNum) { // o not missing and all just missing so far and guess
	    try {	      
		if (Class.forName("java.lang.Number").isAssignableFrom(o.getClass())==true)
		    isnum=true;	       
	    } catch (Exception E) {};
	    if (isnum)
		min=max=((Number)o).doubleValue();	    
	};
	if (cat) {	    
	    Object oo=o;
	    if (o==null) oo=missingCat;
	    int i=cats.indexOf(oo);
	    if (i==-1) {
		cats.addElement(oo);
		ccnts.addElement(new Integer(1));
	    } else {
		ccnts.setElementAt(new Integer(((Integer)ccnts.elementAt(i)).intValue()+1),i);
	    };
	};
	if (isnum && o!=null) {
	    try {
		double val=((Number)o).doubleValue();
		if (val>max) max=val;
		if (val<min) min=val;
	    } catch(Exception E) {
                // what do we do when the cast doesn't work ? we return false indicating so
                return false;
            };
	};
       	super.addElement(o); // we don't add the element unless we're through all checks etc.
        notify.NotifyAll(new NotifyMsg(this,Common.NM_VarContentChange));
	return true;
    };

    /** replaces an element at specified position - use with care!. this doesn't work for categorical variables.
        in that case you need to dropCat(), make modifications and categorize().
        also numerical variables only "grow" their min/max - i.e. if min/max was the removed
        element, then min/max is not adapted to shrink the range
        */
    public boolean replace(int i, Object o) {
        if (i<0 || i>=size() || isCat()) return false;
        Object oo=at(i);
        if (oo==o) return true;
        if (oo==null) {
            missingCount--;
            if (missingCount==0) hasNull=false;
        }
        if (o==null) {
            missingCount++; hasNull=true;
        }
        if (isnum && o!=null) {
            try {
                double val=((Number)o).doubleValue();
                if (val>max) max=val;
                if (val<min) min=val;
            } catch(Exception E) {
                // what do we do when the cast doesn't work ? we return false indicating so
                return false;
            };
        };
       	setElementAt(o,i); // we don't modify the element unless we're through all checks etc.
        notify.NotifyAll(new NotifyMsg(this,Common.NM_VarContentChange));
        return true;
    }
    
    //---- uncomment for JDK 1.2 or higher if necessary - won't work with 1.1 ------
    // since add can return false on failure addElement should NOT be activated even in 1.2 and above
    // public void addElement(Object o) { add(o); };

    public double getMin() { return min; };
    public double getMax() { return max; };

    public Object at(int i) { return elementAt(i); };
    public int atI(int i) { return (isnum)?(elementAt(i)==null)?0:((Number)elementAt(i)).intValue():0; };
    public double atF(int i) { return (isnum)?(elementAt(i)==null)?0:((Number)elementAt(i)).doubleValue():0; };
    public double atD(int i) { return (isnum)?(elementAt(i)==null)?0:((Number)elementAt(i)).doubleValue():0; };
    public String atS(int i) { return elementAt(i).toString(); };
    public boolean isMissingAt(int i) { return elementAt(i)==null; };

    /** returs the # of missing values in this variable */
    public int getMissingCount() {
        return missingCount;
    }
    
    /** returns the ID of the category of the object
        @param object
        @return category ID
     */
    public int getCatIndex(Object o) {
	if (!cat) return -1;
        Object oo=o; if(o==null) oo=missingCat;
	return cats.indexOf(oo);
    };

    /** returns ID of the category of i-th case in the variable or -1 if i oob */
    public int getCatIndex(int i) {
        try {
            return getCatIndex(elementAt(i));
        } catch (Exception e) {
            return -1;
        }
    };
    
    /** returns the category with index ID or <code>null</code> if variable is not categorial */
    public Object getCatAt(int i) {
	if (!cat) return null;
	return cats.elementAt(i);
    };

    /** returns size of the category with index ID or -1 if variable is not categorial or index oob */
    public int getSizeCatAt(int i) {
	if (!cat) return -1;
        try { // catch exception if cat ID is out of bounds
            return ((Integer)ccnts.elementAt(i)).intValue();
        } catch  (Exception e) {
            return -1;
        }
    };

    /** returns size of the category o. If category does not exist or variable is not categorial, -1 is returned. */
    public int getSizeCat(Object o) {
	if (!cat) return -1;
	int i=cats.indexOf(o);
	return (i==1)?-1:((Integer)ccnts.elementAt(i)).intValue();
    };

    /** returns name of the variable */
    public String getName() { return name; };
    /** returns <code>true</code> if the variable has numerical content (i.e. it can be casted to Number) */
    public boolean isNum() { return isnum; };
    /** returns <code>true</code> if it's a categorial variable */
    public boolean isCat() { return cat; };

    /** returns the number of categories for this variable or 0 if the variable is not categorial */
    public int getNumCats() {
	if (!cat) return 0;
	return cats.size();
    };

    /** returns true if there are missing values */
    public boolean hasMissing() { return hasNull; };
    
    /** returns new, fixed array of categories */
    public Object[] getCategories() { 
	if (!cat) return null;
	
	Object c[] = new Object[cats.size()];
	cats.copyInto(c);
	return c; 
    };

    /** returns list of indexes ordered by rank.
        for details see @link{#getRanked(SVar, SMarker, int)} */
    public int[] getRanked() { return getRanked(this,null,0); };

    /** returns list of indexes ordered by rank, for non-cat, num vars only. missing
	values are omitted.
	@param v variable (should be obtained by at(..))
	@param m marker to use for filtering. if <code>null</code> all cases will be checked
	@param markspec mark for filtering, i.e. cases with that mark will be used only
	@return list of indexes or <code>null</code> is any of the following
	cases: variable is not numerical or is categorical, no cases matching
	specification are present */
    public static int[] getRanked(SVar v, SMarker m, int markspec) {
	if (v==null || v.isCat() || !v.isNum() || v.size()==0) return null;
	int ct=0;
	int x=v.size();
	int i=0; // pass 1 : find relevant cases
	while(i<x) {
	    Object o=v.at(i);
	    if (o!=null) {
		if (markspec==-1 || m==null || m.getMark(i)==markspec) {
		    ct++;
		};
	    };
	    i++;
	};
	if (ct==0) return null;
	
	int r[] = new int[ct];
	ct=0;
	i=0; // pass 2: store relevant IDs
	while(i<x) {
	    Object o=v.at(i);
	    if (o!=null) {
		if (markspec==-1 || m==null || m.getMark(i)==markspec) {
		    r[ct]=i;
		    ct++;
		};
	    };
	    i++;
	};
	
	// pass 3: sort by value
	i=0;
	while (i<ct-1) {
	    double d=v.atD(r[i]);
	    int j=ct-1;
	    while (j>i) {
		double d2=v.atD(r[j]);
		if (d2<d) {
		    int xx=r[i]; r[i]=r[j]; r[j]=xx;
		    d=d2;
		};
		j--;
	    };
	    i++;
	};
	// return the resulting list
	return r;
    };
};
