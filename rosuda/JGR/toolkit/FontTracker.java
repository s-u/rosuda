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
        //System.out.println(comp);
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
        while (e.hasMoreElements()) {
            Component comp = (Component) e.nextElement();
            comp.setFont(JGRPrefs.DefaultFont);
            
            try {
                //System.out.println(comp.getClass().getName());
                if (comp.getClass().getName().equals("javax.swing.JTable") ||
                    comp.getClass().getName().equals(
                    "org.rosuda.JGR.RObjectManager$1")) {
                    ( (JTable) comp).setRowHeight( (int) (JGRPrefs.FontSize *
                        1.5));
                }
            } catch (Exception ex) {}
        }
    }

}