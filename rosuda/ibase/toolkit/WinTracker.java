package org.rosuda.ibase.toolkit;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;

import org.rosuda.ibase.*;
import org.rosuda.util.*;

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
	we.menu=new JMenu("Window");
	JMenuItem mi=new JMenuItem("Close window");
        mi.setAccelerator(javax.swing.KeyStroke.getKeyStroke('W', Toolkit.getDefaultToolkit().getMenuShortcutKeyMask(),false));
        mi.setActionCommand("WTMclose"+we.id);
        mi.addActionListener(this);
	we.menu.add(mi);
        mi=new JMenuItem("Close same type");
        mi.setAccelerator(javax.swing.KeyStroke.getKeyStroke('W',Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()+1,false));
        mi.setActionCommand("WTMcloseClass"+we.wclass);
        mi.addActionListener(this);
        we.menu.add(mi);
        mi=new JMenuItem("Close All");
        mi.setActionCommand("WTMcloseAll");
        mi.addActionListener(this);
        we.menu.add(mi);
        we.menu.addSeparator();
	for(Enumeration e=wins.elements(); e.hasMoreElements();) {
	    WTentry we2=(WTentry)e.nextElement();
	    if (we2!=null && we2!=we && we2.menu!=null) {
              we.menu.add(we2.newMenuItem(this));
            }
	};
	we.menu.addActionListener(this);
    };

    public void add(WTentry we) {
	if (we==null) return;
	wins.addElement(we);
	if (we.menu==null) {
          newWindowMenu(we);
        }
	if (we.mi!=null) {
	    for(Enumeration e=wins.elements(); e.hasMoreElements();) {
		WTentry we2=(WTentry)e.nextElement();
		if (Global.DEBUG>0)
		    System.out.println("-- updating menu; we2="+we2.toString());
		if (we2!=null && we2.menu!=null) {
		    we2.menu.add(we.newMenuItem(this));
		    if (Global.DEBUG>0) System.out.println("-- menu updated");
		};
	    };
	};
	if (we.w!=null) we.w.addFocusListener(this);
	if (Global.DEBUG>0)
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
			JMenuItem mi=we2.menu.getItem(i);
                        if (mi==null);
			else if (mi.getActionCommand().compareTo(ac)==0) {
			    if (Global.DEBUG>0)
				System.out.println("-- found by action command, removing");
			    we2.menu.remove(mi);
			    break;
			};
			i++;
		    };
		};
	    };
	};
	if (Global.DEBUG>0)
	    System.out.println(">>window removed: \""+we.name+"\"");
        if (wins.size()==0) {
            /*if (SplashScreen.main!=null && Global.AppType!=Common.AT_Framework)
                SplashScreen.main.setVisible(true);
            else*/ if(Global.AppType==Global.AT_standalone) {
                System.out.println("FATAL: Stand-alone mode, last window closed, but no splash screen present. Assuming exit request.");
                System.exit(0);
            }
        }
    }

    public void rm(Window w) {
	if (Global.DEBUG>0)
	    System.out.println(">>request to remove window \""+w.toString()+"\"");
	for(Enumeration e=wins.elements(); e.hasMoreElements();) {
	    WTentry we=(WTentry)e.nextElement();
	    if (Global.DEBUG>0)
		System.out.println("-- lookup: "+((we==null)?"<null>":we.toString()));
	    if (we!=null && we.w==w) {
		if (Global.DEBUG>0) System.out.println("-- matches");
		rm(we); return;
	    };
	};
    };

    public JMenu getWindowMenu(Window w) {
        WTentry we=getEntry(w);
        System.out.println(we.toString());
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
	if (Global.DEBUG>0)
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
	if (Global.DEBUG>0)
	    System.out.println(">> action: "+cmd+" by "+o.toString());
	for(Enumeration e=wins.elements();e.hasMoreElements();){
	    WTentry we=(WTentry)e.nextElement();
            if (we!=null && (cmd.compareTo("WTMclose"+we.id)==0 ||
                      (cmd=="WTMcloseAll" && we.wclass>TFrame.clsVars) ||
                      cmd.equals("WTMcloseClass"+we.wclass)
                      )) {
		if (Global.DEBUG>0)
		    System.out.println(">>close:  ("+we.id+")");
		if (we.w!=null) we.w.dispose();
	    }
	    if (we!=null && cmd.compareTo("WTMwindow"+we.id)==0) {
		if (Global.DEBUG>0)
		    System.out.println(">>activate: \""+we.name+"\" ("+we.w.toString()+")");
                if (we.w!=null) we.w.requestFocus();
                if (we.w!=null) we.w.toFront();
	    }
	}
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

    public void Exit() {
	disposeAll();
	System.exit(0);
    };

    // TODO: implement things like attachVar() etc.
};
