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

    /** minimal value (if numeric variable) */
    double min;
    /** maximal value (if numeric variable) */
    double max;

    public static final String missingCat = "NA";

    /** construct new variable and add first element
	@param Name variable name
	@param iscat <code>true</code> if categorial variable
	@param first first element to be added - see {@link #add} for details. If <code>null</code> is passed then no element is added. */
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
    };
    /** construct new variable (equals to <code>SVar(Name,iscat,null)</code>)
 	@param Name variable name
	@param iscat <code>true</code> if categorial variable */
    public SVar(String Name, boolean iscat) { this(Name,iscat,null); };
    /** construct new variable (equals to <code>SVar(Name,false,null)</code>)
 	@param Name variable name */
    public SVar(String Name) { this(Name,false,null); };

    /** define the variable explicitely as categorial
	@param rebuild if set to <code>true</code> force rebuild even if the variable is already categorial. <b>(rebuild is NOT implemented yet!! ToDO!)</b> */
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
	};
    };
    /** define the variable explicitely as categorial (equals to calling {@link #categorize(boolean) categorize(false)}) */
    public void categorize() { categorize(false); };
    /** define the variable explicitely as non-categorial (drop category list) */ 
    public void dropCat() {
	cats=null; ccnts=null; cat=false;
    };

    /** adds a new case to the variable (NEVER use addElement! see package header) Also beware, categorial varaibles are classified by object not by value!
     *  @param o object to be added. First call to <code>add</code> (even implicit if an object was specified on the call to the constructor) does also decide whether the variable will be numeric or not. If the first object is a subclass of <code>Number</code> then the variable is defined as numeric. There is a significant difference in handling numeric and non-numeric variabels, see package header.
     *  @return always <code>true<code> to comply with JDK 1.2 add definition */
    public boolean add(Object o) {
	super.addElement(o);
	if (o==null) hasNull=true;
	if (size()<2) {
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
	if (isnum) {
	    try {
		double val=((Number)o).doubleValue();
		if (val>max) max=val;
		if (val<min) min=val;
	    } catch(Exception E) {
		
		// what do we do when cast doesn't work ?
	    };
	};
	return true;
    };

    //---- uncomment for JDK 1.2 or higher if necessary - won't work with 1.1 ------
    // public void addElement(Object o) { add(o); };

    public double getMin() { return min; };
    public double getMax() { return max; };

    public Object at(int i) { return elementAt(i); };
    public int atI(int i) { return (isnum)?((Number)elementAt(i)).intValue():0; };
    public double atF(int i) { return (isnum)?((Number)elementAt(i)).doubleValue():0; };
    public double atD(int i) { return (isnum)?((Number)elementAt(i)).doubleValue():0; };
    public String atS(int i) { return elementAt(i).toString(); };
    public boolean isMissingAt(int i) { return elementAt(i)==null; };
    
    /** returns the ID of the category of the object */
    public int getCatIndex(Object o) {
	if (!cat) return -1;
	return cats.indexOf(o);
    };

    /** returns ID of the category of i-th case in the variable */
    public int getCatIndex(int i) { return getCatIndex(elementAt(i)); };
    
    /** returns the category with index ID or <code>null</code> if variable is not categorial */
    public Object getCatAt(int i) {
	if (!cat) return null;
	return cats.elementAt(i);
    };

    /** returns size of the category with index ID or -1 if variable is not categorial */
    public int getSizeCatAt(int i) {
	if (!cat) return -1;
	return ((Integer)ccnts.elementAt(i)).intValue();
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
	for details see @link{getRanked(SVar, SMarker, int)} */
    public int[] getRanked() { return getRanked(this,null,0); };

    /** returns list of indexes ordered by rank, for non-cat, num vars only. missing
	values are omitted.
	@param v variable (should be obtained by at(..))
	@param m marker to use for filtering. if <code>null</code> all cases will be checked
	@param markspec mark for filtering, i.e. cases with that mark will be used only
	@returns list of indexes or <code>null</code> is any of the following
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
