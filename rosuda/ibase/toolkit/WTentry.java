import java.awt.*;
import java.util.*;

// what we ought to add to WT are among others:
// - associated variables
// - class (other that just using java tools)

/** a {@link WinTracker} entry
    @version $Id$ 
*/
class WTentry extends Object
{
    public static int lid=1;

    public Window w;
    public String name;
    int id=0;
    MenuItem mi=null;
    Menu menu=null;

    WTentry(Window win) {
	w=win; id=lid; lid++;
	mi=newMenuItem();
    }

    WTentry(Window win,String nam) {
        name=nam; 
	w=win; id=lid; lid++;
	mi=newMenuItem();
    };

    public MenuItem newMenuItem() {
	MenuItem mi=new MenuItem(((name==null)?"Window":name)+" ["+id+"]");
	mi.setActionCommand("WTMwindow"+id);
	return mi;
    };

    public String toString() {
	return "WTentry(id="+id+", name="+name+", win="+((w==null)?"<null>":w.toString())+")";
    };
};
