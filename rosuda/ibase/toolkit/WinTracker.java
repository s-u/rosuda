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
    WTentry curFocus=null;

    public WinTracker()
    {
	wins=new Vector();
    };

    void newWindowMenu(WTentry we) {
	we.menu=new Menu("Window");
	MenuItem mi=new MenuItem("Close"); mi.setActionCommand("WTMclose"+we.id);
	we.menu.add(mi); we.menu.addSeparator();
	for(Enumeration e=wins.elements(); e.hasMoreElements();) {
	    WTentry we2=(WTentry)e.nextElement();
	    if (we2!=null && we2!=we && we2.menu!=null)
		we.menu.add(we2.newMenuItem());
	};	
	we.menu.addActionListener(this);
    };

    public void add(WTentry we) { 
	if (we==null) return;
	wins.addElement(we);
	if (we.menu==null) newWindowMenu(we);
	if (we.mi!=null) {
	    for(Enumeration e=wins.elements(); e.hasMoreElements();) {
		WTentry we2=(WTentry)e.nextElement();
		if (Common.DEBUG>0)
		    System.out.println("-- updating menu; we2="+we2.toString());
		if (we2!=null && we2.menu!=null) {
		    we2.menu.add(we.newMenuItem());
		    System.out.println("-- menu updated");
		};
	    };
	};
	if (we.w!=null) we.w.addFocusListener(this);
	if (Common.DEBUG>0)
	    System.out.println(">>new window: \""+we.name+"\" ("+we.w.toString()+")");
    };

    public void rm(WTentry we) {
	if (we==null) return;
	wins.removeElement(we);
	if (we.mi!=null) {
	    for(Enumeration e=wins.elements(); e.hasMoreElements();) {
		WTentry we2=(WTentry)e.nextElement();
		if (we2!=null && we2.menu!=null) {
		    int i=0;
		    String ac=we.mi.getActionCommand();
		    while(i<we2.menu.getItemCount()) {
			MenuItem mi=we2.menu.getItem(i);
			if (mi.getActionCommand().compareTo(ac)==0) {
			    if (Common.DEBUG>0)
				System.out.println("-- found by action command, removing");
			    we2.menu.remove(mi);
			    break;
			};
			i++;
		    };
		};
	    };
	};
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

    public Menu getWindowMenu(Window w) {
	WTentry we=getEntry(w);
	return (we==null)?null:we.menu;
    };

    public Enumeration elements() { return wins.elements(); };

    public WTentry getEntry(int id) {
	for(Enumeration e=wins.elements(); e.hasMoreElements();) {
	    WTentry we=(WTentry)e.nextElement();
	    if (we!=null && we.id==id) return we;
	};
	return null;
    };

    public WTentry getEntry(Window w) {
	for(Enumeration e=wins.elements(); e.hasMoreElements();) {
	    WTentry we=(WTentry)e.nextElement();
	    if (we!=null && we.w==w) return we;
	};
	return null;
    };

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
	    if (we!=null && cmd.compareTo("WTMclose"+we.id)==0) {
		if (Common.DEBUG>0)
		    System.out.println(">>close: \""+we.name+"\" ("+we.w.toString()+")");
		we.w.dispose();
	    };
	    if (we!=null && cmd.compareTo("WTMwindow"+we.id)==0) {
		if (Common.DEBUG>0)
		    System.out.println(">>activate: \""+we.name+"\" ("+we.w.toString()+")");
		we.w.requestFocus();
		we.w.toFront();
		return;
	    };
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
	WTentry we=getEntry(f);
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
	    String rac=menuDef[i+1];
	    if (rac=="WTMclose") rac="WTMclose"+we.id;
	    m.add(mi=new MenuItem(menuDef[i])).setActionCommand(rac);
	    mi.addActionListener(al);
	    if (menuDef[i+1]=="WTMclose") mi.addActionListener(this);
	    i+=2;
	};
	
	if (help) mb.setHelpMenu(m);
	if (we!=null && we.menu!=null)
	    mb.add(we.menu); // add window menu
	f.setMenuBar(mb);
	return mb;
    };	
    
    public void Exit() {
	disposeAll();
	System.exit(0);	
    };

    // TODO: implement things like attachVar() etc.
};
