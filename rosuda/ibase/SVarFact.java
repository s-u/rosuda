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
public class SVarFact extends SVar
{
    public int[] cont;
    int[] temp;
    public String[] cats;
    String[] tempcats;
    int[] ccnts;

    int[] ranks=null;

    boolean lastIsMissing=false;

    //static int maxID = 0;

    public boolean muteNotify=false;

    /** return <code>true</code> if missings have their own category (the last one) */
    public boolean isLastMissing() {
        return lastIsMissing;
    }

    public SVarFact(String name, int len) {
        super(name,true);
        isnum = false;
        cont = new int[len];
        for (int i = 0; i < cont.length; i++) cont[i] = getCatIndex(missingCat);
        cats = new String[0];
    }

    /** construct new variable and add first element
	@param Name variable name
	@param iscat <code>true</code> if categorial variable
	@param first first element to be added - see {@link #add} for details. If <code>null</code> is passed then no element is added. The usage of this constructor is discouraged/deprecated because first entry cannot contain a missing value thus leading to a loss of generality. */
    public SVarFact(String Name, int[] ids, String[] cnames)
    {
        super(Name, true);
        // the defaults are different for SVarObj - due to guessing we must start with string assumption
        isnum=false;
        contentsType=CT_String;
        cont=ids;
        cats=cnames;
        ccnts=new int[cnames.length+1];
        int i=0;
        while (i<ids.length) {
            if (ids[i]>=0 && ids[i]<cats.length) {
                ccnts[ids[i]]++;
                //maxID = maxID < ids[i]?ids[i]:maxID;
            }
            else {
                cont[i]=-1; missingCount++;
            }
            i++;
        }
    }

    public boolean replaceAll(int[] ids, String[] cnames) {
        cont=ids;
        cats=cnames;
        ccnts=new int[cnames.length+1];
	missingCount=0;
        int i=0;
        while (i<ids.length) {
            if (ids[i]>=0 && ids[i]<cats.length) {
                ccnts[ids[i]]++;
                //maxID = maxID < ids[i]?ids[i]:maxID;
            }
            else {
                cont[i]=-1; missingCount++;
            }
            i++;
        }
	return true;
    }

    public void createMissingsCat() {
        if (!cat || cont==null || cont.length<1 || missingCount==0 || isLastMissing()) return;
        int j=0;
        int cvtdMissings=0;
        while (j<cont.length) {
            if (cont[j]==-1) {
                cont[j]=cats.length;
                cvtdMissings++;
            }
            j++;
        }
        String[] newcat=new String[cats.length+1];
        System.arraycopy(cats,0,newcat,0,cats.length);
        newcat[cats.length]=missingCat;
        int[] newcnts=new int[cats.length+1];
        System.arraycopy(ccnts,0,newcnts,0,ccnts.length);
        newcnts[cats.length]=cvtdMissings;
        missingCount=cvtdMissings;
        cats=newcat;
        ccnts=newcnts;
        lastIsMissing=true;
    }

    public void setAllEmpty(int size) {
        cont=new int[size];
        for (int i = 0; i < size; i++)
            cont[i]=-1;
        missingCount=size;
    }

    public int size() { return cont.length; }

    /** define the variable explicitely as categorical
	@param rebuild if set to <code>true</code> force rebuild even if the variable is already categorial. */
    public void categorize(boolean rebuild) {
	if (cat && !rebuild) return;
        if (!muteNotify) NotifyAll(new NotifyMsg(this,Common.NM_VarTypeChange));
    }

    /** sort categories by specifeid method
        @param method sort method, see SM_xxx constants */
    public void sortCategories(int method) {
        if (!isCat() || cats.length<2) return;
    }

    /** define the variable explicitely as non-categorial (drop category list) */
    public void dropCat() {
	cat=false;
        if (!muteNotify) NotifyAll(new NotifyMsg(this,Common.NM_VarTypeChange));
    }

    public void setCategorical(boolean nc) {
        cat=true;
    }

    public boolean add(Object o) {
        return false;
    }

    public boolean remove(int index) {
            int length = size();
            temp = new int[--length];
            try {
                for (int i = 0, z = 0; z < cont.length && i < temp.length; i++, z++) {
                    if (i == index) z++;
                    temp[i] = cont[z];
                }
                cont = temp;
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
                    else { temp[i] = cont[z]; }
                }
                cont = temp;
                if (o==null) cont[index] = getCatIndex(missingCat);
                else {
                    int catI = getCatIndex(o.toString());
                    if (catI==-1) {
                            tempcats = new String[cats.length+1];
                            for (int z = 0; z < cats.length; z++) tempcats[z] = cats[z];
                            tempcats[tempcats.length-1] = o.toString();
                            cats = tempcats;
                            catI = getCatIndex(o.toString());
                    }
                    cont[index] = getCatIndex(o.toString());
                }
                return true;
            }
            catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }



    public boolean replace(int i, Object o) {
        int catI =  getCatIndex(o.toString());
        if (catI==-1) {
            tempcats = new String[cats.length+1];
            for (int z = 0; z < cats.length; z++) tempcats[z] = cats[z];
            try {
              tempcats[tempcats.length - 1] = o.toString();
            }
            catch (Exception e) {e.printStackTrace();}
            System.out.println(tempcats[tempcats.length-1]);
            cats = tempcats;
            System.out.println(o.toString());
            catI = getCatIndex(o.toString());
        }
        cont[i] = catI;
        return false;
    }

    public Object at(int i) {
    	return (i<0 || i>=cont.length || cont[i]<0 || cont[i]>cats.length)?null:cats[cont[i]];
    }

    public int atI(int i) {
        return (i<0 || i>=cont.length || cont[i]<0 || cont[i]>cats.length)?-1:cont[i];
    }

    public int getCatIndex(Object o) {
	if (cats==null || missingCat.equals(o)) return -1;
        int i=0;
        while (i<cats.length) {
            if (cats[i].equals(o))
                return i;
            i++;
        }
        return -1;
    }

    public int getCatIndex(int i) {
        return (i<0 || i>=cont.length || cont[i]<0 || cont[i]>=cats.length)?-1:cont[i];
    }

    /** returns the category with index ID or <code>null</code> if variable is not categorial */
    public Object getCatAt(int i) {
        return (i<0 || i>=cats.length)?missingCat:cats[i];
    }

    /** returns size of the category with index ID or -1 if variable is not categorial or index oob */
    public int getSizeCatAt(int i) {
	if (cats==null) return -1;
        if (i==-1) return missingCount;
        return (i<0 || i>=cats.length)?-1:ccnts[i];
    }

    /** returns size of the category o. If category does not exist or variable is not categorial, -1 is returned. */
    public int getSizeCat(Object o) {
        if (o==null || o.equals(missingCat)) return missingCount;
        int ci=getCatIndex(o);
	return (ci<0||ci>=cats.length)?-1:ccnts[ci];
    }

    /** returns the number of categories for this variable or 0 if the variable is not categorial */
    public int getNumCats() {
	if (cats==null) return 0;
	return cats.length;
    }

    /** returns new, ed array of categories */
    public Object[] getCategories() {
        return cats;
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

        if (m==null && cacheRanks && ranks!=null) return ranks; // we can cache only ranks w/o a marker

        int[] r=null;

        /* okay in fact we always get the full ranks and then pick those marked (if desired) */

        if (!cacheRanks || ranks==null) {
            int ct=size();
            if (ct==0) return null;
            r = new int[ct];
            int i=0;
            while(i<ct) { r[i]=i; i++; }

            sw.profile("getRanked: prepare");
            // pass 3: sort by value
            i=0;
            while (i<ct-1) {
                int d=cont[r[i]];
                int j=ct-1;
                while (j>i) {
                    int d2=cont[r[j]];
                    if (d2<d) {
                        int xx=r[i]; r[i]=r[j]; r[j]=xx;
                        d=d2;
                    };
                    j--;
                }
                i++;
            }
            sw.profile("getRanked: sort");
            if (cacheRanks)
                ranks=r;
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

    public boolean hasEqualContents(int i2[], String s2[]) {
	if (i2.length!=cont.length || s2.length!=cats.length) return false;
	int i = 0;
	while (i<i2.length) { if (i2[i]!=cont[i]) return false; i++; }
	i = 0;
	while (i<s2.length) { if ((s2[i]==null && cats[i]!=null) ||
				  (s2[i]!=null && cats[i]==null) ||
				  (s2[i]!=null && cats[i]!=null && !s2[i].equals(cats[i]))) return false; i++; }
	return true;
    }
    
    public String toString() {
        return "SVarFact(\""+name+"\","+(cat?"cat,":"cont,")+(isnum?"num,":"txt,")+"n="+size()+",miss="+missingCount+")";
    }
}
