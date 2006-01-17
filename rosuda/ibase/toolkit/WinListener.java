package org.rosuda.ibase.toolkit;

import java.awt.*;
import java.awt.event.*;

import org.rosuda.ibase.*;

/** Default window listener.
 *  Handles default tasks, like close-window event,
 *  un-registers window from WinTracker etc.
 *  @version $Id$ */
public class WinListener implements WindowListener {
    public WinListener() {}

    public void windowClosing(final WindowEvent e) {
        final Window w = e.getWindow();
        final Container cc = w.getParent();
        w.dispose();
        if (cc != null) {
            cc.remove(w);
        }
        WinTracker.current.rm(w);
        w.removeAll();
 
        if (e.getWindow() == Common.mainFrame) {
            if (WinTracker.current != null) {
                WinTracker.current.disposeAll();
            }
            System.exit(0);
        }
    }

    public void windowClosed(final WindowEvent e) {
        final Window w = e.getWindow();
        WinTracker.current.rm(w);
        if (e.getWindow() == Common.mainFrame) {
            if (WinTracker.current != null) {
                WinTracker.current.disposeAll();
            }
            System.exit(0);
        }
    }

    public void windowOpened(final WindowEvent e) {}

    public void windowIconified(final WindowEvent e) {}

    public void windowDeiconified(final WindowEvent e) {}

    public void windowActivated(final WindowEvent e) {}

    public void windowDeactivated(final WindowEvent e) {}
}