package org.rosuda.JGR.toolkit;

//
//  FontTracker.java
//  JGR
//
//  Created by Markus Helbig on Fri Mar 05 2004.
//  Copyright (c) 2004 __MyCompanyName__. All rights reserved.
//

import java.awt.*;
import java.util.*;
import javax.swing.*;

public class FontTracker {

    public static FontTracker current = null;

    Vector components;


    /** FontTracker, every added component will change its font
      * currently there is a bug in the apple jre so we need to make some hacks*/

    public FontTracker() {
        components = new Vector();
    }

    /** add a Component
      * @param comp component to add */
    public void add(Component comp) {
        comp.setFont(JGRPrefs.DefaultFont);
        components.add(comp);
    }

    public void add(JComponent comp) {
        add((Component) comp);
    }

    public void setFontBigger() {
        Enumeration e = components.elements();
        JGRPrefs.FontSize +=2;
        JGRPrefs.refresh();
        applyFont();
    }

    public void setFontSmaller() {
        Enumeration e = components.elements();
        JGRPrefs.FontSize -=2;
        JGRPrefs.refresh();
        applyFont();
    }

    public void applyFont() {
        Enumeration e = components.elements();
        Font f = JGRPrefs.DefaultFont;
    	while (e.hasMoreElements()) {
            Component comp = (Component) e.nextElement();
            try {
            	Class sc = comp.getClass().getSuperclass();
            	while (!sc.getName().startsWith("java"))
            		sc = sc.getSuperclass();
                if (sc.getName().equals("javax.swing.JTable")){
                	if (f.getSize() > JGRPrefs.MINFONTSIZE)
                		f = new Font(f.getName(),f.getStyle(),JGRPrefs.MINFONTSIZE);
                	((javax.swing.JTable) comp).setRowHeight((int)(f.getSize()*1.6));
                }
                else if (sc.getName().equals("javax.swing.JTextComponent") || sc.getName().equals("javax.swing.JTextPane")) {
                }
                else {
                	if (f.getSize() > 18) 
                		f = new Font(f.getName(),f.getStyle(),JGRPrefs.MINFONTSIZE);
                }
                comp.setFont(f);
            } catch (Exception ex) {
			}
        }
    }

}