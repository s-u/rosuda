package org.rosuda.ibase;

import java.util.*;
import java.io.*;
import org.rosuda.util.*;

/** implements set of variables (aka dataset, used as data source)
    which consists basically of a set of variables (of class {@link SVar}) and
    a marker (of class {@link SMarker}).
    @version $Id$
*/
public class SVarSet {
    /** vector of {@link SVar} objects - the variables */
    protected Vector vars;
    /** marker associated with this dataset */
    protected SMarker mark;
    /** dataset name */
    protected String name;

    public int globalMisclassVarID=-1;
    public int classifierCounter=1;

    /** default constructor of empty dataset */
    public SVarSet() { vars=new Vector(); name="<unknown>"; };

    /** sets the marker for this dataset
	@param m marker */
    public void setMarker(SMarker m) { mark=m; m.masterSet=this; };
    /** returns the marker of this dataset
	@return marker */
    public SMarker getMarker() { return mark; };
    /** set dataset name
	@param s name */
    public void setName(String s) {
	name=s;
    };
    /** get dataset name
	@return dataset name */
    public String getName() { return name; };

    public int length() {
        int len=0;
        int i=0, l=vars.size();
        while (i<l) {
            int vl=((SVar)vars.elementAt(i)).size();
            if (vl>len) len=vl;
            i++;
        }
        return len;
    }

    /** add a new variable to the dataset
	@param v variable
	@return index of the newly create variable or <0 if an error occured (-1 on null-problems, -2 if that variable-name already exists) */
    public int add(SVar v) {  // return position or -1 on error or -2 if name exists
	if (v==null) return -1;
	String nn=v.getName(); if (nn==null) return -1;

	if (!vars.isEmpty()) {
	    for (Enumeration e=vars.elements(); e.hasMoreElements();) {
		SVar n=(SVar)e.nextElement();
		if (n.getName().compareTo(nn)==0) return -2;
	    };
	};

	vars.addElement(v);
	return vars.indexOf(v);
    };

    /** add a new empty variable to the dataset
            @param index index
            @param size size of SVar
            @param isnum numbers
            @param cat categorical
    /* added 28.12.04 MH */

    public void insert(int index,int size,boolean isnum, boolean cat) {  // return position or -1 on error or -2 if name exists
      SVar v = new SVarObj("Var"+index,isnum,cat);
      v.setAllEmpty(size);
      vars.insertElementAt(v,index);
    };

    /** removes a variable of the dataset at specified index
        @param index index
        @return boolean false if remove fails els true

    /* added 28.12.03 MH */
    public void remove(int index) {  // return position or -1 on error or -2 if name exists
      vars.removeElementAt(index);
    };

    /* added 28.12.03 MH */
    /** add an empty case to the set
     *         @param index index*/
    public boolean insertCaseAt(int index) {
      Enumeration e = vars.elements();
      while(e.hasMoreElements()) {
        if (!((SVar) e.nextElement()).insert(null,index)) return false;
      }
      return true;
    }
    
    /* added 28.12.03 MH */
    /** delete a case of the set
     *         @param index index*/
    public boolean removeCaseAt(int index) {
      Enumeration e = vars.elements();
      while(e.hasMoreElements()) {
        SVar v = ((SVar) e.nextElement());
        if (!v.remove(index))
          return false;
      }
      return true;
    }

    /** returns the index of a variable specified by its name
	@param nam variable name
	@return index of the variable (or -1 if not found) */
    public int indexOf(String nam) {
	int i=0;
	while (i<vars.size()) {
	    SVar n=(SVar)vars.elementAt(i);
	    if (n.getName().compareTo(nam)==0) return i;
	    i++;
	};
	return -1;
    };

    /** returns variable object specified by name
 	@param nam variable name
	@return variable object or <code>null</code> if not found. */
    public SVar byName(String nam) {
	if (vars.isEmpty()) return null;
	for (Enumeration e=vars.elements(); e.hasMoreElements();) {
	    SVar n=(SVar)e.nextElement();
	    if (n.getName().compareTo(nam)==0) return n;
	};
	return null;
    };

    /** returns variable object at specified index
	@param i index
	@return variable object or <code>null</code> if index out of range */
    public SVar at(int i) {
        return ((i<0)||(i>=vars.size()))?null:(SVar)vars.elementAt(i);
    }

    /** repaces a variable at the specified index. Use with extreme care! Should be used only in the loading stage before the variable is ever used, in order to provide optimized version of the same variable once the type is known. */
    public void replace(int i, SVar v) {
        if (i>=0 && i<vars.size()) vars.setElementAt(v, i);
    }

    /** returns data value of a variable specified by name and row index
	@param nam variable name
	@param row row index
	@return data object (or <code>null</code> if out of range) */
    public Object data(String nam, int row) {
	SVar v=byName(nam);
	return (v==null)?null:v.elementAt(row);
    };

    /** returns data value of a variable specified by variable index and row index
	@param col variable index
	@param row row index
	@return data object (or <code>null</code> if out of range) */
    public Object data(int col, int row) {
	SVar v=at(col);
	return (v==null)?null:v.elementAt(row);
    };

    /** returns coordiantes where searched Object was found
        @param o o Object to search for
        @param c_off c_off search from this case(x) on
        @param r_off r_off search from this case(y) on
        @return coord coord */
    public int[] whereis(Object o, int c_off, int r_off) {
      int[] index = {-1,-1};
      for (int i = r_off; i < at(0).size(); i++) {
        for (int z = c_off!=0?c_off:0; z < vars.size(); z++) {
          if (at(z).at(i) != null && at(z).at(i).toString().toLowerCase().indexOf(o.toString().toLowerCase()) != -1) {
            index[0] = z;
            index[1] = i;
            return index;
          }
        }
        c_off = 0;
      }
      return index;
    }

    /** returns enumeration of all variable objects in this dataset
	@return object enumeration (of type {@link SVar}) */
    public Enumeration elements() { return vars.elements(); };

    /** returns number of variables in this dataset
	@return # of variables */
    public int count() { return vars.size(); };

    public boolean Export(PrintStream p, boolean all) { return Export(p,all,null); }
    public boolean Export(PrintStream p) { return Export(p,true,null); }
    public boolean Export(PrintStream p, boolean all, int vars[]) {
        boolean exportAll=all || mark==null || mark.marked()==0;
        try {
            if (p!=null) {
                int j=0,tcnt=0,fvar=0;
                j=0;
                if (vars==null || vars.length<1) {
                    while(j<count()) {
                        p.print(((tcnt==0)?"":"\t")+at(j).getName());
                        if (tcnt==0) fvar=j;
                        tcnt++;
                        j++;
                    }
                } else {
                    while(j<vars.length) {
                        p.print(((tcnt==0)?"":"\t")+at(vars[j]).getName());
                        if (tcnt==0) fvar=vars[j];
                        tcnt++;
                        j++;
                    }
                }
                p.println("");
                int i=0;
                while (i<at(fvar).size()) {
                    if (exportAll || mark.at(i)) {
                        j=fvar;
                        j=0;
                        if (vars==null || vars.length<1) {
                            while(j<count()) {
                                Object oo=at(j).at(i);
                                p.print(((j==0)?"":"\t")+((oo==null)?"NA":oo.toString()));
                                j++;
                            }
                        } else {
                            while(j<vars.length) {
                                Object oo=at(vars[j]).at(i);
                                p.print(((j==0)?"":"\t")+((oo==null)?"NA":oo.toString()));
                                j++;
                            }
                        }
                        p.println("");
                    }
                    i++;
                };
            };
            return true;
        } catch (Exception eee) {
            if (Global.DEBUG>0) {
                System.out.println("* SVarSet.Export...: something went wrong during the export: "+eee.getMessage()); eee.printStackTrace();
            };
        };
        return false;
    }

    public void printSummary() {
        System.out.println("DEBUG for SVarSet ["+toString()+"]");
        for (Enumeration e=elements(); e.hasMoreElements();) {
            SVar v2=(SVar)e.nextElement();
            if (v2==null)
                System.out.println("Variable: null!");
            else {
                System.out.println("Variable: "+v2.getName()+" ("+(v2.isNum()?"numeric":"string")+
                                   ","+(v2.isCat()?"categorized":"free")+") with "+
                                   v2.size()+" cases");
                if (v2.isCat()) {
                    Object[] c=v2.getCategories();
                    System.out.print("  Categories: ");
                    int i=0;
                    while (i<c.length) {
                        System.out.print("{"+c[i].toString()+"} ");
                        i++;
                    };
                    System.out.println();
                }
            }
        }
    }
}
