package org.rosuda.ibase.toolkit;

import java.awt.*;
import java.awt.event.*;
import org.rosuda.ibase.*;

/** Default window listener.
 *  Handles default tasks, like close-window event,
 *  un-registers window from WinTracker etc.
 *  @version $Id$ */
public class WinListener implements WindowListener
{
    public WinListener() {}
    
    public void windowClosing(WindowEvent e)
    {	
	Window w=e.getWindow();
	Container cc=w.getParent();
	w.dispose();
	if (cc!=null) cc.remove(w);
	WinTracker.current.rm(w);
	w.removeAll();
	w=null;
	
	if (e.getWindow()==Common.mainFrame) {
	    if (WinTracker.current!=null)
		WinTracker.current.disposeAll();
	    System.exit(0);
	}
    }
    
    public void windowClosed(WindowEvent e) {
	Window w=e.getWindow();
	WinTracker.current.rm(w);
	if (e.getWindow()==Common.mainFrame) {
	    if (WinTracker.current!=null)
		WinTracker.current.disposeAll();
	    System.exit(0);
	}
    }
    
    public void windowOpened(WindowEvent e) {}
    public void windowIconified(WindowEvent e) {}
    public void windowDeiconified(WindowEvent e) {}
    public void windowActivated(WindowEvent e) {}
    public void windowDeactivated(WindowEvent e) {}
}
