package org.rosuda.ibase;

import java.util.*;

import org.rosuda.util.*;

//import SMarker;

/**
 * <b>Statistical Variable</b>
 * This abstract class defines an interface to variables. There are various implementations (fixed size/flexible, fixed type/objects etc.
 * @version $Id$
 */
public abstract class SVar extends Notifier
{
    public static final int CT_String = 0;
    public static final int CT_Number = 1;
    public static final int CT_Map    = 8;
    public static final int CT_Tree   = 9;

    /** derived is not internal (and hence isInternal will return false) */
    public static final int IVT_Derived    =-1;
    public static final int IVT_Normal     = 0;
    public static final int IVT_Prediction = 1;
    public static final int IVT_Misclass   = 2;
    public static final int IVT_LeafID     = 3;
    public static final int IVT_Index      = 4; // row index
    public static final int IVT_Resid      = 8; // residuals
    public static final int IVT_RCC        = 9;
    public static final int IVT_ResidStat1 = 10; // residual statistics 1 - currently the mean
    public static final int IVT_ResidStat2 = 11;

    /** sort method lexicograph. */
    public static final int SM_lexi = 0;
    /** sort method numerical (all objects are cast to Number, non-castable are assigned -0.01,
        basically to appear just before zero) */
    public static final int SM_num  = 1;

    /** this value is returned by {@link #atI(int)} if the value is missing (<code>null</code>) or if the variable is not numerical. This variable should not be changed except on startup. */
    public static int int_NA = 0;
    /** this value is returned by {@link #atD(int)} if the value is missing (<code>null</code>) or if the variable is not numerical. This variable should not be changed except on startup. */
    public static double double_NA = Double.NaN;
    /** string denoting class of the missings */
    public static final String missingCat = "NA";

    protected double min, max;

    /** variable name */
    protected String  name;
    /** type of variable, <code>true</code> if categorial variable */
    protected boolean cat;
    /** type of variable, <code>true</code> if numeric (i.e. subclass of <code>Number</code>) */
    protected boolean isnum;

    /** if set to <code>true</code> then the type of the variable is not yet known and can be guessed */
    protected boolean guessing=true;

    /** type of the contents */
    protected int contentsType;

    /** flag denoting selection in a SVarSet */
    public boolean selected;

    /** # of missing cases in the variable */
    protected int missingCount=0;

    /** if >0 then this is an internal variable */
    int internalType=0;

    /** if <code>false</code> then ranks are not cached */
    public boolean cacheRanks=true;

    /** this attribute can be used by external code to store temporary information (tags) - it may not used by SVar or its subclasses. This attribute should not be used in environments that use SVar across multiple threads. */ 
    public int tag=0;
    
    /** permutation of categories */
    protected SCatSequence seq=null;

    /** construct new variable. iscat=<code>true</code> defaults to non-numerical, CT_String, whereas iscat=<code>false</code> defaults to numerical, CT_Number
	@param Name variable name
	@param iscat <code>true</code> if categorial variable
        */
    public SVar(String Name, boolean iscat)
    {
        name=Name;
        cat=iscat;
        isnum=iscat?false:true;
        contentsType=iscat?CT_String:CT_Number;
        selected=false;
    }
    /** construct new variable. iscat=<code>true</code> defaults to non-numerical, CT_String, whereas iscat=<code>false</code> defaults to numerical, CT_Number
        @param Name variable name
        @param isnum <code>true</code> if numeric variable
        @param iscat <code>true</code> if categorial variable
        */

    public SVar(String Name, boolean isnum, boolean iscat)
    {
      this.name=Name;
      this.cat=iscat;
      this.isnum=isnum;
      this.contentsType=isnum?CT_Number:CT_String;
      this.selected=false;
    }

    /** we want to be able to make an empty SVar with the specified size and <code>null</code> content (added 28.12.03 MH).
        remove abstract and define a fallback using replace/add/remove in case the subclass doesn't provide its optimized version (SU 19.1.04) */
    public void setAllEmpty(int size) {
        int i=0;
        int inits=size();
        if (inits>size) {
            while (inits>size) {
                remove(--inits);
            }
        }
        while (i<inits) {
            replace(i++, null);
        }
        while (i<size) {
            add(null); i++;
        }
    }

    /** sets type of an internal variable. internal variables are variables that were not contained
        in the original dataset. derived variables are also internal if they were derived in klimt and not loaded
        with the dataset. */
    public void setInternalType(int it) {
        internalType=it;
    }

    /** returns the main category sequence for this variable. */
    public SCatSequence mainSeq() {
        if (seq==null)
            seq=new SCatSequence(this,this,true);
        return seq;
    }

    /** returns the internal type of the variable */
    public int getInternalType() { return internalType; }

    /** returns true if the variable is internal, i.e. generated on-the fly. note that derived variables are
        NOT internal. use (getInterrnalType()==SVar.IVT_Normal) to check for original, non-derived variables. */
    public boolean isInternal() { return (internalType>0); }

    /** returns <code>true</code> the selected flag is set */
    public boolean isSelected() { return selected; }

    /** sets the selected flag to the specified state */
    public void setSelected(boolean setit) { selected=setit; }

    /** define the variable explicitely as categorical
	@param rebuild if set to <code>true</code> force rebuild even if the variable is already categorial. */
    public abstract void categorize(boolean rebuild);

    /** define the variable explicitely as categorial (equals to calling {@link #categorize(boolean) categorize(false)}) */
    public void categorize() { categorize(false); }

    /** since the current implemantation is a Notifier itef, it returns "this" */
    public Notifier getNotifier() {
        return this;
    }

    /** retrieves contents type of the variable
        @return contents type (see CT_xxx constants)
        */
    public int getContentsType() { return contentsType; }

    /** If it is to be used then is should be used BEFORE first data entries are inserted.
        A call to this method implicitely disables any type guessing. The results of
        calling this method on non-empty SVar is undefined, but the current
        implementation allows such use for custom types.
        @return returns <code>true</code> upon success. This method is guaranteed to succeed if
        setting a custom type when no data were inserted yet. The method may succeed in other cases,
        but its return value should be checked in that case.
    */
    public boolean setContentsType(int ct) {
        if (ct!=CT_Number) isnum=false;
        guessing=false; // the type was set specifically, so no guessing from now on
        contentsType=ct;
        return true;
    }

    /** sort caregotires, using default method which is numerical for num. variables, lexicogr. otherwise */
    public void sortCategories() {
        sortCategories(isNum()?SM_num:SM_lexi);
    }

    /** sort categories by specifeid method
        @param method sort method, see SM_xxx constants */
    public abstract void sortCategories(int method);

    /** define the variable explicitely as non-categorial (drop category list) */
    public abstract void dropCat();

    /** define the type of the variable. note that setting nc to <code>false</code> does not necessarily drop categories information. this method should be used for quick toggling of the variable type */
    public void setCategorical(boolean nc) {
        if (!nc)
            cat=false;
        else
            categorize();
    }

    /** returns the size (number of cases) of the variable */
    public abstract int size();

    /** adds a new case to the variable. the exact behavior is implementation-dependent.
        @returns <code>false</code> if some error occured (overflow, wrong type, ...) */
    public abstract boolean add(Object o);
    public boolean add(double d) { return add(new Double(d)); }
    public boolean add(int d) { return add(new Integer(d)); }

    /* added 28.12.03 MH */
    /** inserts a new case to the variable at specified index. the exact behavior is implementation-dependent.
        @returns <code>false</code> if some error occured (overflow, wrong type, ...) */
    public abstract boolean insert(Object o, int index);
    public boolean insert(double d, int index) { return insert(new Double(d), index); }
    public boolean insert(int d, int index) { return insert(new Integer(d), index); }


    /** removes a case from the variable at specified index. the exact behavior is implementation-dependent.*/
    public abstract boolean remove(int index);

    /** replaces an element at specified position
        @returns <code>false</code> if some error occured (overflow, wrong type, ...) */
    public abstract boolean replace(int index, Object o);
    public boolean replace(int index, double d) { return replace(index, new Double(d)); }
    public boolean replace(int index, int i) { return replace(index, new Integer(i)); }

    public double getMin() { return min; }
    public double getMax() { return max; }

    public abstract Object at(int i);

    /** for compatibility with old code that used Vector class */
    public Object elementAt(int i) { return at(i); }

    public int atI(int i) { return (isnum)?(at(i)==null)?int_NA:((Number)at(i)).intValue():int_NA; }
    public double atF(int i) { return (isnum)?(at(i)==null)?0:((Number)at(i)).doubleValue():0; }
    public double atD(int i) { return (isnum)?(at(i)==null)?double_NA:((Number)at(i)).doubleValue():double_NA; }
    public String atS(int i) { return (at(i)==null)?null:at(i).toString(); }

    public boolean isMissingAt(int i) { return at(i)==null; }

    /** returs the # of missing values in this variable */
    public int getMissingCount() {
        return missingCount;
    }

    /** returns the ID of the category
        @param category (usually string)
        @return category ID
     */
    public abstract int getCatIndex(Object o);

    /** returns ID of the category of i-th case in the variable or -1 if i oob */
    public int getCatIndex(int i) {
        try {
            return getCatIndex(at(i));
        } catch (Exception e) {
            return -1;
        }
    }

    /** returns the category with index ID or <code>null</code> if variable is not categorial */
    public abstract Object getCatAt(int i);

    /** returns size of the category with index ID or -1 if variable is not categorial or index oob */
    public abstract int getSizeCatAt(int i);

    /** returns size of the category o. If category does not exist or variable is not categorial, -1 is returned. */
    public abstract int getSizeCat(Object o);

    /** returns name of the variable */
    public String getName() { return name; }
    /** returns <code>true</code> if the variable has numerical content (i.e. it can be casted to Number) */
    public boolean isNum() { return isnum; }
    /** returns <code>true</code> if it's a categorial variable */
    public boolean isCat() { return cat; }

    public boolean isEmpty() { return size()==0; }

    /** warning! use with care! Nameshould not be changed after hte variable was registered with
        SVarSet. The behavior for doing so is undefined. */
    public void setName(String nn) { name=nn; }

    /** returns the number of categories for this variable or 0 if the variable is not categorial */
    public abstract int getNumCats();

    /** returns true if there are missing values */
    public boolean hasMissing() { return missingCount>0; }

    /** returns new, fixed array of categories */
    public abstract Object[] getCategories();

    /** returns list of indexes ordered by rank.
        for details see @link{#getRanked(SVar, SMarker, int)} */
    public int[] getRanked() { return getRanked(null,0); }

    public abstract int[] getRanked(SMarker m, int markspec);

    public static int[] filterRanksByID(int r[], int ids[]) {
        if (r==null || ids==null || ids.length<1) return r;
        int x=r.length;
        int map[]=new int[x];
        int ct=ids.length;
        int i=0; // pass 1 : construct a map from ids
        while(i<ct) {
            if (ids[i]>=0 && ids[i]<x) map[ids[i]]=1;
            i++;
        }
        int[] mr=new int[ct];
        i=0;
        int mri=0; // pass 2: copy the relevant ranks
        while(i<x) {
            if (map[r[i]]==1)
                mr[mri++]=r[i];
            i++;
        }
        map=null;
        return mr;
    }

    public static int[] filterRanksByMap(int r[], int map[], int mapEntry) {
        if (r==null || map==null || map.length<1) return r;
        int x=r.length;
        int ct=0;
        int i=0; // pass 1 : count the selected cases
        while(i<x) {
            if (r[i]>=0 && r[i]<map.length && map[r[i]]==mapEntry)
                ct++;
            i++;
        }
        int[] mr=new int[ct];
        i=0;
        int mri=0; // pass 2: copy the relevant ranks
        while(i<x) {
            if (r[i]>=0 && r[i]<map.length && map[r[i]]==mapEntry)
                mr[mri++]=r[i];
            i++;
        }
        return mr;
    }

    public String toString() {
        return "SVar(\""+name+"\","+(cat?"cat,":"cont,")+(isnum?"num,":"txt,")+"n="+size()+",miss="+missingCount+")";
    }
}
