import java.awt.*;
import java.awt.event.*;

/** Default window listener.
 *  Handles default tasks, like close-window event,
 *  un-registers window from WinTracker etc.
 *  @version $Id$ */
public class DefWinL implements WindowListener
{
    DefWinL() {};
    public void windowClosing(WindowEvent e)
    {	
	Window w=e.getWindow(); w.dispose();
	Container cc=w.getParent();
	if (cc!=null) cc.remove(w);
	WinTracker.current.rm(w);
	w.removeAll();
	w=null;
	
	if (e.getWindow()==Common.mainFrame) {
	    if (WinTracker.current!=null)
		WinTracker.current.disposeAll();
	    System.exit(0);
	};
    };
    public void windowClosed(WindowEvent e) {};
    public void windowOpened(WindowEvent e) {};
    public void windowIconified(WindowEvent e) {};
    public void windowDeiconified(WindowEvent e) {};
    public void windowActivated(WindowEvent e) {};
    public void windowDeactivated(WindowEvent e) {};
};
