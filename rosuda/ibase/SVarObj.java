package org.rosuda.ibase;

import java.util.*;

import org.rosuda.util.*;

//import SMarker;

/**
 * <b>Statistical Variable</b>
 * <p><u>IMPORTANT NOTES:</u>
 * <pre>
 * NOTE: do NOT remove any elements from SVar !! Any Vector methods that modify contents
 *       except for "add" are NOT safe (for categorized vars) as they DON'T update categories!!
 *
 * The "is number" property is set accoding to the first added (non-null) element - thus you should NOT mix
 * numeric and non-numeric objects. It is safe to insert numbers in non-numeric value, but NOT
 * vice-versa as for numeric variables the detection of min/max casts any object to Number.</pre>
 * @version $Id$
 */
public class SVarObj extends SVar
{
    /** vector of the actual content */
    Vector  cont;
    /** vector of categories if cat. var. */
    Vector  cats;
    /** vector of counts per category */
    Vector  ccnts;

    int[] ranks=null;

    /** construct new variable and add first element
	@param Name variable name
	@param iscat <code>true</code> if categorial variable
	@param first first element to be added - see {@link #add} for details. If <code>null</code> is passed then no element is added. The usage of this constructor is discouraged/deprecated because first entry cannot contain a missing value thus leading to a loss of generality. */
    public SVarObj(String Name, boolean iscat)
    {
        super(Name, iscat);
        // the defaults are different for SVarObj - due to guessing we must start with string assumption
        isnum=false;
        contentsType=CT_String;
        cont=new Vector();
        if (iscat) {
	    cats=new Vector(); ccnts=new Vector();
	}
    }

    /* added 02.01.04 MH */
    public SVarObj(String Name, boolean isnum, boolean iscat)
    {
      super(Name, isnum, iscat);
      guessing = false;
      cont=new Vector();
      if (iscat) {
        cats=new Vector(); ccnts=new Vector();
      }
    }


    /** construct new variable (equals to <code>SVar(Name,false,null)</code>)
 	@param Name variable name */
    public SVarObj(String Name) { this(Name,false); };

    /* added 28.12.03 MH */
    public void setAllEmpty(int size) {
      for (int i = 0; i < size; i++) {
        missingCount++;
        cont.add(cat?missingCat:null);
      }
      if (cat) this.categorize(true);
    }


    /** sets the {@link #guessNum} flag. It must me set before the first add(..) call because the guess is made
        based on the first added object (hence makes no sense if {@link #SVar(String,boolean,Object)} was used). */
    public void tryToGuessNum(boolean doit) {
        guessing=doit;
    }

    public int size() { return cont.size(); }

    /** define the variable explicitely as categorical
	@param rebuild if set to <code>true</code> force rebuild even if the variable is already categorial. */
    public void categorize(boolean rebuild) {
	if (cat && !rebuild) return;
	cats=new Vector(); ccnts=new Vector();
	cat=true;
	if (!isEmpty()) {
	    for(Enumeration e=cont.elements();e.hasMoreElements();) {
		Object oo=e.nextElement();
		if (oo==null) oo=missingCat;
		int i=cats.indexOf(oo);
		if (i==-1) {
		    cats.addElement(oo);
		    ccnts.addElement(new Integer(1));
		} else {
		    ccnts.setElementAt(new Integer(((Integer)ccnts.elementAt(i)).intValue()+1),i);
		}
	    }
            if (isNum()) { // if numerical and categorical then sort categories for convenience
                sortCategories();
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
        };
    };

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
        if (cacheRanks && ranks!=null) ranks=null; // remove ranks - we don't update them so far...
        if (o==null)
            missingCount++;
	if (o!=null && size()==missingCount && guessing) { // o not missing and all just missing so far and guess
	    try {
                if (Class.forName("java.lang.Number").isAssignableFrom(o.getClass())==true) {
                    isnum=true;	contentsType=CT_Number;
                }
	    } catch (Exception E) {};
	    if (isnum)
                min=max=((Number)o).doubleValue();
	}
	if (cat) {
	    Object oo=o;
	    if (o==null) oo=missingCat;
	    int i=cats.indexOf(oo);
	    if (i==-1) {
		cats.addElement(oo);
		ccnts.addElement(new Integer(1));
	    } else {
		ccnts.setElementAt(new Integer(((Integer)ccnts.elementAt(i)).intValue()+1),i);
	    }
	}
	if (isnum && o!=null) {
	    try {
		double val=((Number)o).doubleValue();
		if (val>max) max=val;
		if (val<min) min=val;
	    } catch(Exception E) {
                // what do we do when the cast doesn't work ? we return false indicating so
                return false;
            }
	}
       	cont.addElement(o); // we don't add the element unless we're through all checks etc.
        NotifyAll(new NotifyMsg(this,Common.NM_VarContentChange));
	return true;
    }


    /* added 28.12.03 MH */
    /** insets an empty case to var*/
    public boolean insert(Object o, int index) {
        if (o != null) ; //replace (o,index);
        if (cacheRanks && ranks!=null) ranks=null; // remove ranks - we don't update them so far...
        missingCount++;
        cont.insertElementAt(null,index);
        NotifyAll(new NotifyMsg(this,Common.NM_VarContentChange));
        return true;
    }

    /* added 28.12.03 MH */
    /** remove a case, if it's an NA do missingcount--, what i do not is updating cats*/
    public boolean remove(Object o, int index) {
      if (o == null && missingCount > -1) missingCount--;
      cont.removeElementAt(index);
      this.categorize(true);
      return true;
    }

    /** replaces an element at specified position - use with care!.
        also numerical variables only "grow" their min/max - i.e. if min/max was the removed
        element, then min/max is not adapted to shrink the range
        */
    /* added 31.12.03 MH */
    public boolean replace(Object o, int i) {
      if (i < 0 || i >= size()) return false;
      Object oo = at(i);
      if (oo == o) return true;
      if (oo == null)
        missingCount--;
      if (o == null)
        missingCount++;
      if (isnum && o!=null) {
        try {
          double val=((Number)o).doubleValue();
          if (val>max) max=val;
          if (val<min) min=val;
          //Now we have to update the max and min if nescessary
        } catch(Exception E) {
          // what do we do when the cast doesn't work ? we return false indicating so
          E.printStackTrace();
          return false;
        }
      }
      cont.setElementAt(o, i); // we don't modify the element unless we're through all checks etc.
      NotifyAll(new NotifyMsg(this, Common.NM_VarContentChange));
      if (cat) this.categorize(true);
      return true;
    }

    public Object at(int i) { return cont.elementAt(i); };

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
            if (size()<1000) {
                int ct=size();
                if (ct==0) return null;
                r = new int[ct];
                int i=0;
                while(i<ct) { r[i]=i; i++; }

                sw.profile("getRanked: prepare");
                // pass 3: sort by value
                i=0;
                while (i<ct-1) {
                    double d=atD(r[i]);
                    int j=ct-1;
                    while (j>i) {
                        double d2=atD(r[j]);
                        if (d2<d) {
                            int xx=r[i]; r[i]=r[j]; r[j]=xx;
                            d=d2;
                        };
                        j--;
                    };
                    i++;
                };
                sw.profile("getRanked: sort");
                if (cacheRanks)
                    ranks=r;
            } else {
                ProgressDlg pd=new ProgressDlg("Variable "+getName());
                pd.begin("Calculating ranks ...");
                int ct=size();
                r = new int[ct];
                double[] da = new double[ct];
                sw.profile("getRanked: alloc double array for "+ct+" cases");
                int i=0; // pass 2: store relevant IDs
                while(i<ct) {
                    r[i]=i; da[i]=atD(i);
                    i++;
                }
                sw.profile("getRanked: pass 2: store relevant values");

                // pass 3: sort by value
                i=0;
                while (i<ct-1) {
                    double d=da[r[i]];
                    int j=ct-1;
                    if ((i&255)==0)
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
                pd.setProgress(99);
                sw.profile("getRanked: pass 3: sort");
                if (cacheRanks)
                    ranks=r;
                da=null;
                pd.end();
                pd=null;
            }
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
        return "SVarObj(\""+name+"\","+(cat?"cat,":"cont,")+(isnum?"num,":"txt,")+"n="+size()+",miss="+missingCount+")";
    }
}
