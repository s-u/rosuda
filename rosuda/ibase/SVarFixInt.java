//
//  SVarFixInt.java
//  Klimt
//
//  Created by Simon Urbanek on Fri Nov 14 2003.
//  Copyright (c) 2003 __MyCompanyName__. All rights reserved.
//

package org.rosuda.ibase;

import java.util.*;
import org.rosuda.util.*;

/** SVar implementation for fixed-length ints variable
*/

public class SVarFixInt extends SVar
{
    /** the actual content */
    int[] cont;

    /** insertion point for add */
    int insertPos=0;

    /** vector of categories if cat. var. */
    Vector  cats;
    /** vector if counts per category */
    Vector  ccnts;

    int[] ranks=null;

    static int[] temp;

    /** construct new variable
	@param Name variable name
	@param len length of the fixed variable
        */
    public SVarFixInt(String Name, int len)
    {
        super(Name, false);
        if (len<0) len=0;
        guessing=false;
        contentsType=CT_Number;
        isnum=true;
        cont=new int[len];
        for (int i = 0; i < cont.length; i++) cont[i] = SVar.int_NA;
        insertPos = len;
    }

    public SVarFixInt(String Name, int[] d) {
        this(Name, d, true);
    }

    public SVarFixInt(String Name, int[] d, boolean copyContents)
    {
        super(Name, false);
        boolean firstValid=true;
        min=max=0;
        if (copyContents) {
            cont=new int[d.length];
            int i=0;
            while (i<d.length) {
                cont[i]=d[i];
                if (cont[i]==int_NA) missingCount++;
                else {
                    if (firstValid) {
                        min=max=cont[i];
                        firstValid=false;
                    } else {
                        if (cont[i]>max) max=cont[i]; else
                            if (cont[i]<min) min=cont[i];
                    }
                }
                i++;
            }
        } else {
            cont=d;
            int i=0;
            while (i<d.length) {
                if (cont[i]==int_NA) missingCount++;
                else {
                    if (firstValid) {
                        min=max=cont[i];
                        firstValid=false;
                    } else {
                        if (cont[i]>max) max=cont[i]; else
                            if (cont[i]<min) min=cont[i];
                    }
                }
                i++;
            }
        }
        insertPos=d.length;
        guessing=false;
        contentsType=CT_Number;
        isnum=true;
    }

    public int size() { return cont.length; }

    /** define the variable explicitely as categorical
	@param rebuild if set to <code>true</code> force rebuild even if the variable is already categorial. */
    public void categorize(boolean rebuild) {
	if (cat && !rebuild) return;
	cats=new Vector(); ccnts=new Vector();
	cat=true;
	if (!isEmpty()) {
            int ci=0;
            while (ci<cont.length) {
                String oo=cont[ci]==int_NA?missingCat:Integer.toString(cont[ci]);
		int i=cats.indexOf(oo);
                if (i==-1) {
                    cats.addElement(oo);
                    ccnts.addElement(new Integer(1));
		} else {
		    ccnts.setElementAt(new Integer(((Integer)ccnts.elementAt(i)).intValue()+1),i);
		}
	    }
            if (isNum()) { // if numerical and categorical then sort categories for convenience
                sortCategories(SM_num);
            }
	}
        NotifyAll(new NotifyMsg(this,Common.NM_VarTypeChange));
    }

    /** sort categories by specifeid method
        @param method sort method, see SM_xxx constants */
    public void sortCategories(int method) {
        if (!isCat() || cats.size()<2) return;
        Stopwatch sw=null;
        if (Global.DEBUG>0) {
            sw=new Stopwatch();
            System.out.println("Sorting variable \""+name+"\"");
        }
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
                            if (mino.compareTo(o.toString())>0) {
                                mino=o.toString(); p=i;
                            }
                        }
                    }
                }
                i++;
            }
            if (found=gotmin) {
                cats.addElement(ocats.elementAt(p)); ccnts.addElement(occnts.elementAt(p));
                ocats.setElementAt(null,p);
            }
        }
        if (Global.DEBUG>0) {
            sw.profile("sorted");
        }
    }

    /** define the variable explicitely as non-categorial (drop category list) */
    public void dropCat() {
	cats=null; ccnts=null; cat=false;
        NotifyAll(new NotifyMsg(this,Common.NM_VarTypeChange));
    }

    public void setCategorical(boolean nc) {
        if (!nc) {
            cat=false;
        } else {
            if (cats==null) categorize(); else cat=true;
        }
    }

    /** adds a new case to the variable (NEVER use addElement! see package header) Also beware, categorial varaibles are classified by object not by value!
     *  @param o object to be added. First call to <code>add</code> (even implicit if an object was specified on the call to the constructor) does also decide whether the variable will be numeric or not. If the first object is a subclass of <code>Number</code> then the variable is defined as numeric. There is a significant difference in handling numeric and non-numeric variabels, see package header.
     *  @return <code>true<code> if element was successfully added, or <code>false</code> upon failure - currently when non-numerical value is inserted in a numerical variable. It is strongly recommended to check the result and act upon it, because failing to do so can result in non-consistent datasets - i.e. mismatched row IDs */
    public boolean add(Object o) {
        if (insertPos>=cont.length) return false;
        if (cacheRanks && ranks!=null) ranks=null; // remove ranks - we don't update them so far...
        int val=int_NA;
        if (o!=null) {
            try {
                val=Integer.parseInt(o.toString());
            } catch(NumberFormatException nfe) {
                return false;
            }
        }
        return add(val);
    }

    public boolean add(double d) { return add((int) d); }

    public boolean add(int d) {
        if (insertPos>=cont.length) return false;
	if (cat) {
            Object oo=d==int_NA?missingCat:Integer.toString(d);
	    int i=cats.indexOf(oo);
	    if (i==-1) {
		cats.addElement(oo);
		ccnts.addElement(new Integer(1));
	    } else {
		ccnts.setElementAt(new Integer(((Integer)ccnts.elementAt(i)).intValue()+1),i);
	    }
	}
	if (d!=int_NA) {
            if (d>max) max=d;
            if (d<min) min=d;
	} else
            missingCount++;
        cont[insertPos++]=d;
        NotifyAll(new NotifyMsg(this,Common.NM_VarContentChange));
	return true;
    }

    /** replaces an element at specified position - use with care!. this doesn't work for categorical variables.
        in that case you need to dropCat(), make modifications and categorize().
        also numerical variables only "grow" their min/max - i.e. if min/max was the removed
        element, then min/max is not adapted to shrink the range
        */
    public boolean replace(int i, Object o) {
        try {replace(i,Integer.parseInt(o.toString())); return true;} catch (Exception e) {}
        return false;
    }

    public boolean replace(int i, int d) {
        if (i<0 || i>=cont.length || isCat()) return false;
        cont[i]=d;
        return true;
    }

    public Object at(int i) { return (i<0||i>=insertPos||cont[i]==SVar.int_NA)?null:new Integer(cont[i]); };
    public double atD(int i) { return (i<0||i>=insertPos)?int_NA:cont[i]; }
    public int atI(int i) { return (i<0||i>=insertPos)?int_NA:((int)(cont[i]+0.5)); }
    public String asS(int i) { return (i<0||i>=insertPos)?null:Integer.toString(cont[i]); }

    /** returns the ID of the category of the object
        @param object
        @return category ID
     */
    public int getCatIndex(Object o) {
	if (cats==null) return -1;
        Object oo=o; if(o==null) oo=missingCat;
	return cats.indexOf(oo);
    }

    /** returns ID of the category of i-th case in the variable or -1 if i oob */
    public int getCatIndex(int i) {
        try {
            return getCatIndex(elementAt(i));
        } catch (Exception e) {
            return -1;
        }
    }

    /** returns the category with index ID or <code>null</code> if variable is not categorial */
    public Object getCatAt(int i) {
        if (cats==null) return null;
        try {
            return cats.elementAt(i);
        } catch (Exception e) {
            return null;
        }
    }

    /** returns size of the category with index ID or -1 if variable is not categorial or index oob */
    public int getSizeCatAt(int i) {
	if (cats==null) return -1;
        try { // catch exception if cat ID is out of bounds
            return ((Integer)ccnts.elementAt(i)).intValue();
        } catch  (Exception e) {
            return -1;
        }
    }

    /** returns size of the category o. If category does not exist or variable is not categorial, -1 is returned. */
    public int getSizeCat(Object o) {
	if (cats==null) return -1;
	int i=cats.indexOf(o);
	return (i==1)?-1:((Integer)ccnts.elementAt(i)).intValue();
    }

    /** returns the number of categories for this variable or 0 if the variable is not categorial */
    public int getNumCats() {
	if (cats==null) return 0;
	return cats.size();
    }

    /** returns new, fixed array of categories */
    public Object[] getCategories() {
	if (cats==null) return null;

	Object c[] = new Object[cats.size()];
	cats.copyInto(c);
	return c;
    }

    /** we don't support replace [[FIXME: replace needs to re-alloc the vector or something like that ... ]] */
    public boolean remove(int index) {
            int length = size();
            temp = new int[--length];
            try {
                for (int i = 0, z = 0; z < cont.length && i < temp.length; i++, z++) {
                    if (i == index) z++;
                    temp[i] = cont[z];
                }
                cont = temp;
                insertPos = cont.length;
                return true;
            }
            catch (Exception e) {
                return false;
            }
        }


        public boolean insert(Object o, int index) {
            int length = size();
            temp = new int[++length];
            try {
                for (int i = 0, z = 0; z < cont.length && i < temp.length; i++, z++) {
                    if (i == index) z--;
                    else temp[i] = cont[z];
                }
                cont = temp;
                cont[index] = o==null?int_NA:Integer.parseInt(o.toString());
                insertPos = cont.length;
                return true;
            }
            catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }


    /** returns list of indexes ordered by rank, for non-cat, num vars only. missing
        values are omitted.
        @param v variable (should be obtained by at(..))
        @param m marker to use for filtering. if <code>null</code> all cases will be checked
        @param markspec mark for filtering, i.e. cases with that mark will be used only
        @return list of indexes or <code>null</code> is any of the following
cases: variable is not numerical or is categorical, no cases matching
        specification are present */
    public int[] getRanked(SMarker m, int markspec) {
        Stopwatch sw=new Stopwatch();
        if (isCat() || !isNum() || size()==0) return null;

        if (m==null && cacheRanks && ranks!=null) return ranks; // we can cache only ranks w/o a marker

        int[] r=null;

        /* okay in fact we always get the full ranks and then pick those marked (if desired) */

        if (!cacheRanks || ranks==null) {
            // due to the massive amount of lookups necessary during the sorting, we allocate a separate double buffer with a copy of the data and work on that one instead of the atD access, if the number of cases is large enough.

            ProgressDlg pd=null;
            if (size()>1000) {
                pd=new ProgressDlg("Variable "+getName());
                pd.begin("Calculating ranks ...");
            }
            int ct=size();
            r = new int[ct];
            int[] da = cont;

            sw.profile("getRanked: pass 1: store relevant values");

            // pass 3: sort by value
            int i=0;
            while (i<ct-1) {
                double d=da[r[i]];
                int j=ct-1;
                if (pd!=null && (i&255)==0)
                    pd.setProgress((int)(((double)i)*99.0/((double)ct)));
                while (j>i) {
                    double d2=da[r[j]];
                    if (d2<d) {
                        int xx=r[i]; r[i]=r[j]; r[j]=xx;
                        d=d2;
                    }
                    j--;
                }
                i++;
            }
            if (pd!=null)
                pd.setProgress(99);
            sw.profile("getRanked: pass 2: sort");
            if (cacheRanks)
                ranks=r;
            da=null;
            if (pd!=null)
                pd.end();
            pd=null;
        } else {
            r=ranks;
        }

        // we got the full list - now we need to thin it out if a marker was specified
        if (m!=null && r!=null) {
            int x=r.length;
            int ct=0;
            int i=0; // pass 1 : find the # of relevant cases
            while(i<x) {
                if (m.get(i)==markspec)
                    ct++;
                i++;
            }
            if (ct==0) return null;
            int[] mr=new int[ct];
            i=0;
            int mri=0;
            while(i<x) {
                if (m.get(r[i])==markspec)
                    mr[mri++]=r[i];
                i++;
            }
            r=null;
            r=mr;
        }

        // return the resulting list
        return r;
    }

    public String toString() {
        return "SVarFixInt(\""+name+"\","+(cat?"cat,":"cont,")+(isnum?"num,":"txt,")+"n="+size()+"/"+cont.length+",miss="+missingCount+")";
    }
}

