import java.awt.*;
import java.util.*;

/** keeps track of open frames and/or windows
    @version $Id$
*/
public class WinTracker
{
    public static WinTracker current=null;

    Vector wins;   

    public WinTracker()
    {
	wins=new Vector();
    };

    void add(WTentry we) { 
	wins.addElement(we); 
	if (Common.DEBUG>0)
	    System.out.println(">>new window: \""+we.name+"\"");
    };
    void rm(WTentry we) {
	wins.removeElement(we); 
	if (Common.DEBUG>0)
	    System.out.println(">>window removed: \""+we.name+"\"");
    };
    void rm(Window w) {
	WTentry we=null;
	for(Enumeration e=wins.elements();
	    e.hasMoreElements();
	    we=(WTentry)e.nextElement());
	if (we.w==w) { rm(we); return; };
    };
    Enumeration elements() { return wins.elements(); };

    void disposeAll() {
	WTentry we=null;
	
	if (Common.DEBUG>0)
	    System.out.println(">>dispose all requested");
	for(Enumeration e=wins.elements();e.hasMoreElements(); we=(WTentry)e.nextElement())
	    if (we!=null && we.w!=null)
		we.w.dispose();		
	wins.removeAllElements();
    };

    // TODO: implement things like attachVar() etc.
};
