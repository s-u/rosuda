import java.awt.*;
import java.awt.event.*;
import java.util.*;

/** keeps track of open frames and/or windows
    @version $Id$
*/
public class WinTracker implements ActionListener, FocusListener
{
    public static WinTracker current=null;

    Vector wins;   
    Menu m;
    WTentry curFocus=null;

    public WinTracker()
    {
	wins=new Vector();
	m=new Menu("Window");
	MenuItem mi=new MenuItem("Close"); mi.setActionCommand("close");
	m.add(mi); m.addSeparator();
	m.addActionListener(this);
    };

    public void add(WTentry we) { 
	if (we==null) return;
	wins.addElement(we);
	if (we.mi!=null) m.add(we.mi);
	if (we.w!=null) we.w.addFocusListener(this);
	if (Common.DEBUG>0)
	    System.out.println(">>new window: \""+we.name+"\" ("+we.w.toString()+")");
    };
    public void rm(WTentry we) {
	if (we==null) return;
	wins.removeElement(we);
	if (we.mi!=null) m.remove(we.mi);
	if (Common.DEBUG>0)
	    System.out.println(">>window removed: \""+we.name+"\"");
    };
    public void rm(Window w) {
	if (Common.DEBUG>0)
	    System.out.println(">>request to remove window \""+w.toString()+"\"");	
	for(Enumeration e=wins.elements(); e.hasMoreElements();) {
	    WTentry we=(WTentry)e.nextElement();
	    if (Common.DEBUG>0)
		System.out.println("-- lookup: "+((we==null)?"<null>":we.toString()));
	    if (we!=null && we.w==w) { 
		if (Common.DEBUG>0) System.out.println("-- matches");
		rm(we); return; 
	    };
	};
    };

    public Enumeration elements() { return wins.elements(); };

    public WTentry getEntry(int id) {
	for(Enumeration e=wins.elements(); e.hasMoreElements();) {
	    WTentry we=(WTentry)e.nextElement();
	    if (we!=null && we.id==id) return we;
	};
	return null;
    };

    public Menu getWindowMenu() { return m; };

    public void disposeAll() {
	if (Common.DEBUG>0)
	    System.out.println(">>dispose all requested");
	for(Enumeration e=wins.elements();e.hasMoreElements();){
	    WTentry we=(WTentry)e.nextElement();
	    if (we!=null && we.w!=null)
		we.w.dispose();
	};
	wins.removeAllElements();
    };

    public void actionPerformed(ActionEvent ev) {
	if (ev==null) return;
	String cmd=ev.getActionCommand();
	Object o=ev.getSource();
	if (Common.DEBUG>0)
	    System.out.println(">> action: "+cmd+" by "+o.toString());
	for(Enumeration e=wins.elements();e.hasMoreElements();){
	    WTentry we=(WTentry)e.nextElement();
	    if (we!=null && cmd.compareTo("WTMwindow"+we.id)==0) {
		if (Common.DEBUG>0)
		    System.out.println(">>activate: \""+we.name+"\" ("+we.w.toString()+")");
		we.w.requestFocus();
		we.w.toFront();
		return;
	    };
	};
	if (cmd=="close") {
	    if (curFocus!=null)
		curFocus.w.dispose();
	};
    };

    public void focusGained(FocusEvent ev) {
	Window w=(Window)ev.getSource();
	for(Enumeration e=wins.elements();e.hasMoreElements();){
	    WTentry we=(WTentry)e.nextElement();
	    if (we!=null && we.w==w) {
		curFocus=we;
	    };
	};
    };

    public void focusLost(FocusEvent ev) {
	Window w=(Window)ev.getSource();
	for(Enumeration e=wins.elements();e.hasMoreElements();){
	    WTentry we=(WTentry)e.nextElement();
	    if (we!=null && we.w==w) {
		if (curFocus==we) curFocus=null;
	    };
	};
    };

    public MenuBar buildQuickMenuBar(Frame f, ActionListener al,
				     String[] menuDef, boolean help) {
	MenuBar mb=f.getMenuBar();
	if (mb==null) mb=new MenuBar();	
	Menu m=null;
	int i=0;
	while (menuDef[i]!="0") {
	    MenuItem mi;
	    if (menuDef[i]=="+") {
		i++;
		mb.add(m=new Menu(menuDef[i])); i++;
	    };
	    if (menuDef[i]=="-") { m.addSeparator(); i++; };
	    m.add(mi=new MenuItem(menuDef[i])).setActionCommand(menuDef[i+1]);
	    mi.addActionListener(al);
	    i+=2;
	};
	
	if (help) mb.setHelpMenu(m);
	mb.add(getWindowMenu()); // add window menu
	f.setMenuBar(mb);
	return mb;
    };	


    // TODO: implement things like attachVar() etc.
};
