package org.rosuda.ibase.toolkit;

import java.awt.*;
import java.util.*;

// what we ought to add to WT are among others:
// - associated variables
// - class (other that just using java tools)

/** a {@link WinTracker} entry
    @version $Id$
*/
public class WTentry extends Object
{
    public static int lid=1;

    public Window w;
    public String name;
    public int id=0;
    public MenuItem mi=null;
    public Menu menu=null;
    public int wclass=0;

    public WTentry(Window win) {
	w=win; id=lid; lid++;
	mi=newMenuItem();
    }

    public WTentry(Window win,String nam,int wndclass) {
        name=nam;
	w=win; id=lid; lid++;
        wclass=wndclass;
	mi=newMenuItem();
    };

    public MenuItem newMenuItem() {
	MenuItem mi=new MenuItem(((name==null)?"Window":name)+" ["+id+"]");
	mi.setActionCommand("WTMwindow"+id);
	return mi;
    };

    public String toString() {
	return "WTentry(id="+id+", class="+wclass+", name="+name+", win="+((w==null)?"<null>":w.toString())+")";
    };
};
