//
//  WTentryAWT.java
//  Klimt
//
//  Created by Simon Urbanek on Fri May 07 2004.
//  Copyright (c) 2004 __MyCompanyName__. All rights reserved.
//

package org.rosuda.ibase.toolkit;

import java.awt.*;
import java.util.*;

public class WTentryAWT extends WTentry {
    /** the "Window" menu */
    Menu winMenu;
    
    public WTentryAWT(WinTracker wt, Window win, String nam, int wndclass) {
        super(wt, win, nam, wndclass);
    }
    
    public Object getWindowMenu() {
        chkWinMenu();
        return winMenu;
    }

    protected void chkWinMenu() {
        if (winMenu==null)
            winMenu=new Menu(windowMenuName);
    }
    
    public void addMenuSeparator() {
        chkWinMenu();
        winMenu.addSeparator();
    }

    public void addMenuItem(String name, String action) {
        MenuItem mi;
        chkWinMenu();
        if (name.charAt(0)=='@' || name.charAt(0)=='!')
            mi=new MenuItem(name.substring(2),
                            new MenuShortcut((int)name.charAt(1),(name.charAt(0)=='!')));
        else
            mi=new MenuItem(name);
        mi.setActionCommand(action);
        mi.addActionListener(wt);
        winMenu.add(mi);
    }
    
    public void rmMenuItemByAction(String action) {
        chkWinMenu();
        MenuItem mi=(MenuItem) getMenuItemByAction(action);
        if (mi!=null)
            winMenu.remove(mi);
    }

    public Object getMenuItemByAction(String action) {
        chkWinMenu();
        int i=0;
        int ms=winMenu.getItemCount();
        while (i<ms) {
            MenuItem mi=winMenu.getItem(i);
            if (mi.getActionCommand().equals(action))
                return mi;
            i++;
        }
        return null;
    }
    
    public void setNameByAction(String action, String name) {
        chkWinMenu();
        MenuItem mi=(MenuItem) getMenuItemByAction(action);
        if (mi!=null) {
            if (name.charAt(0)=='@' || name.charAt(0)=='!') {
                mi.setLabel(name.substring(2));
                mi.setShortcut(new MenuShortcut((int)name.charAt(1),(name.charAt(0)=='!')));
            } else {
                mi.setLabel(name);
                mi.deleteShortcut();
            }
        }
    }
}
