//
//  WTentrySwing.java
//  Klimt
//
//  Created by Simon Urbanek on Fri May 07 2004.
//  Copyright (c) 2004 __MyCompanyName__. All rights reserved.
//

package org.rosuda.ibase.toolkit;

import java.awt.*;
import java.util.*;
import javax.swing.*;

public class WTentrySwing extends WTentry {
    /** the "Window" menu */
    JMenu winMenu;

    public WTentrySwing(WinTracker wt, Window win, String nam, int wndclass) {
        super(wt, win, nam, wndclass);
    }

    protected void chkWinMenu() {
        if (winMenu==null)
            winMenu=new JMenu(windowMenuName);
    }

    public Object getWindowMenu() {
        chkWinMenu();
        return winMenu;
    }

    public void addMenuSeparator() {
        chkWinMenu();
        winMenu.addSeparator();
    }

    public void addMenuItem(String name, String action) {
        chkWinMenu();
        JMenuItem mi;
        if (name.charAt(0)=='@' || name.charAt(0)=='!') {
            mi=new JMenuItem(name.substring(2));
            mi.setAccelerator(javax.swing.KeyStroke.getKeyStroke(name.charAt(1), Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()+((name.charAt(0)=='!')?1:0),false));
        } else
            mi=new JMenuItem(name);
        mi.setActionCommand(action);
        mi.addActionListener(wt);
        winMenu.add(mi);
    }

    public void rmMenuItemByAction(String action) {
        chkWinMenu();
        JMenuItem mi=(JMenuItem) getMenuItemByAction(action);
        if (mi!=null)
            winMenu.remove(mi);
    }

    public Object getMenuItemByAction(String action) {
        chkWinMenu();
        int i=0;
        int ms=winMenu.getItemCount();
        while (i<ms) {
            JMenuItem mi=winMenu.getItem(i);
            if (mi != null && mi.getActionCommand().equals(action))
                return mi;
            i++;
        }
        return null;
    }

    public void setNameByAction(String action, String name) {
        chkWinMenu();
        JMenuItem mi=(JMenuItem) getMenuItemByAction(action);
        if (mi!=null) {
            if (name.charAt(0)=='@' || name.charAt(0)=='!') {
                mi.setText(name.substring(2));
                mi.setAccelerator(javax.swing.KeyStroke.getKeyStroke(name.charAt(1), Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()+((name.charAt(0)=='!')?1:0),false));
            } else {
                mi.setText(name);
                // I know of no way to delete shortcuts in Swing ... really stupid ..
            }
        }
    }
}
