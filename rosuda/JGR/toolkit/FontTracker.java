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
import javax.swing.text.*;

public class FontTracker {

    public static FontTracker current = null;

    Vector components;


    /** FontTracker, every added component will change its font
      * currently there is a bug in the apple jre so we need to make some hacks*/

    public FontTracker() {
        components = new Vector();
    }

    /** add a JComponent
      * @param comp component to add */
    public void add(JComponent comp) {
        comp.setFont(iPreferences.DefaultFont);
        components.add(comp);
    }

    public void setFontBigger() {
        Enumeration e = components.elements();
        iPreferences.FontSize +=2;
        iPreferences.refresh();
        while (e.hasMoreElements()) {
            JComponent comp = (JComponent) e.nextElement();
            comp.setFont(iPreferences.DefaultFont);
            try {
                SyntaxArea area = (SyntaxArea) comp;
                StyledDocument doc = (StyledDocument) area.getDocument();
                doc.setCharacterAttributes(0, area.getText().length(),
                                           iPreferences.SIZE, false);
            }
            catch (Exception ex) {}
            try {
                System.out.println(comp.getClass().getName());
                System.out.println("table rows");
                if (comp.getClass().getName().equals("javax.swing.JTable") || comp.getClass().getName().equals("org.rosuda.JGR.RObjectManager$MTable")) {
                    System.out.println("table");
                    ((JTable) comp).setRowHeight((int) (iPreferences.FontSize *1.2));
                }
            } catch (Exception ex) {}
        }
    }

    public void setFontSmaller() {
        Enumeration e = components.elements();
        iPreferences.FontSize -=2;
        iPreferences.refresh();
        while (e.hasMoreElements()) {
            JComponent comp = (JComponent) e.nextElement();
            comp.setFont(iPreferences.DefaultFont);
            comp.setFont(iPreferences.DefaultFont);
            try {
                SyntaxArea area = (SyntaxArea) comp;
                StyledDocument doc = (StyledDocument) area.getDocument();
                doc.setCharacterAttributes(0, area.getText().length(),
                                           iPreferences.SIZE, false);
            }
            catch (Exception ex) {}
            try {
                System.out.println(comp.getClass().getName());
                if (comp.getClass().getName().equals("javax.swing.JTable") || comp.getClass().getName().equals("org.rosuda.JGR.RObjectManager$MTable")) {
                    ((JTable) comp).setRowHeight((int) (iPreferences.FontSize *1.2));
                }
            } catch (Exception ex) {}
        }
    }

    public void applyFont() {
        Enumeration e = components.elements();
        while (e.hasMoreElements()) {
            JComponent comp = (JComponent) e.nextElement();
            comp.setFont(iPreferences.DefaultFont);
            comp.setFont(iPreferences.DefaultFont);
            try {
                SyntaxArea area = (SyntaxArea) comp;
                StyledDocument doc = (StyledDocument) area.getDocument();
                doc.setCharacterAttributes(0, area.getText().length(),
                                           iPreferences.SIZE, false);
            }
            catch (Exception ex) {}
            try {
                System.out.println(comp.getClass().getName());
                if (comp.getClass().getName().equals("javax.swing.JTable") || comp.getClass().getName().equals("org.rosuda.JGR.RObjectManager$MTable")) {
                    ((JTable) comp).setRowHeight((int) (iPreferences.FontSize *1.2));
                }
            } catch (Exception ex) {}
        }
    }

}