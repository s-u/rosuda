//
//  EzMenu.java
//  Klimt
//
//  Created by Simon Urbanek on Fri Oct 11 2002.
//  $Id$
//

package org.rosuda.ibase.toolkit;

import java.awt.*;
import java.awt.event.ActionListener;
import org.rosuda.ibase.*;

/** class that simplified menu building from lists */
public class EzMenu {
    static final String M_CLOSEWINDOW = "Close window";
    static final String AC_WTMCLOSE = "WTMclose";
    static final String M_QUIT = "Quit";
    static final String AC_EXIT = "exit";
    public static boolean staticInitDone=false;
    public static boolean hasSVG=false;
    
    public static MenuBar getEzMenu(final Frame f, final ActionListener al, final String[] menuDef) {
        if (!staticInitDone) {
            try { Class.forName("PoGraSSSVG"); hasSVG=true; } catch (Throwable ee) {};
            staticInitDone=true;
        };
        final WinTracker wt=WinTracker.current;
        final WTentry we=(wt==null)?null:wt.getEntry(f);
        MenuBar mb=f.getMenuBar();
        if (mb==null) mb=new MenuBar();
        Menu m=null;
        int i=0;
        boolean lastSep=false;
        while (!"0".equals(menuDef[i])) {
            MenuItem mi;
            boolean isNext=false;
            if ("0".equals(menuDef[i])) break;
            if ("~File.Basic.End".equals(menuDef[i])) {
                i++; isNext=true;
                m.add(mi=new MenuItem(M_CLOSEWINDOW,new MenuShortcut('W'))).setActionCommand(AC_WTMCLOSE+we.id); mi.addActionListener(wt);
                if (!Common.isMac())
                    m.add(mi=new MenuItem(M_QUIT,new MenuShortcut('Q'))).setActionCommand(AC_EXIT); mi.addActionListener(al);
            }
            if ("~File.Quit".equals(menuDef[i])) {
                i++; isNext=true;
                if (!Common.isMac()) {
                    m.addSeparator();
                    m.add(mi=new MenuItem(M_QUIT,new MenuShortcut('Q'))).setActionCommand(AC_EXIT); mi.addActionListener(al);
                }
            }
            if ("~File.Graph".equals(menuDef[i])) {
                i++; isNext=true;
                m.add(mi=new MenuItem("Save as PGS ...")).setActionCommand("exportPGS"); mi.addActionListener(al);
                m.add(mi=new MenuItem("Save as PostScript ...")).setActionCommand("exportPS"); mi.addActionListener(al);
                m.add(mi=new MenuItem("Save as PDF ...")).setActionCommand("exportPDF"); mi.addActionListener(al);
                m.add(mi=new MenuItem("Save as SVG ...")).setActionCommand("exportSVG"); mi.addActionListener(al);
                if (!hasSVG) mi.setEnabled(false);
                m.add(mi=new MenuItem("Save as PNG ...")).setActionCommand("exportBitmapDlg"); mi.addActionListener(al);
                if (!Common.isMac()) {
                    m.addSeparator();
                    m.add(mi=new MenuItem("Preferences ...")).setActionCommand("prefs"); mi.addActionListener(al);
                }
                m.addSeparator();
                m.add(mi=new MenuItem("Save selected as ...")).setActionCommand("exportCases"); mi.addActionListener(al);
                m.addSeparator();
                m.add(mi=new MenuItem("Print ...",new MenuShortcut('P'))).setActionCommand("javaPrint"); mi.addActionListener(al);
                m.add(mi=new MenuItem("Page Setup ...",new MenuShortcut('P',true))).setActionCommand("pageSetup"); mi.addActionListener(al);
                m.addSeparator();
                if (Common.supportsBREAK) {
                    m.add(mi=new MenuItem("Break",new MenuShortcut('B',true))).setActionCommand("BREAK"); mi.addActionListener(al);
                    m.addSeparator();
                }
                m.add(mi=new MenuItem(M_CLOSEWINDOW,new MenuShortcut('W'))).setActionCommand(AC_WTMCLOSE+we.id); mi.addActionListener(wt);
                if (!Common.isMac())
                    m.add(mi=new MenuItem(M_QUIT,new MenuShortcut('Q'))).setActionCommand(AC_EXIT); mi.addActionListener(al);
            };
            if ("~Edit".equals(menuDef[i])) {
                i++; isNext=true;
                mb.add(m=new Menu("Edit"));
                m.add(mi=new MenuItem("Select all",new MenuShortcut('A'))).setActionCommand("selAll"); mi.addActionListener(al);
                m.add(mi=new MenuItem("Clear selection",new MenuShortcut('D'))).setActionCommand("selNone"); mi.addActionListener(al);
                m.add(mi=new MenuItem("Invert selection",new MenuShortcut('I'))).setActionCommand("selInv"); mi.addActionListener(al);
                m.addSeparator();
                m.add(mi=new MenuItem("Set size",new MenuShortcut(','))).setActionCommand("sizeDlg"); mi.addActionListener(al);
            };
            if ("~Window".equals(menuDef[i])) {
                i++; isNext=true;
                if (we!=null && we.getWindowMenu()!=null)
                    mb.add((Menu)we.getWindowMenu()); // add window menu
            };
            if ("~Help".equals(menuDef[i])) {
                i++; isNext=true;
                mb.add(m=new Menu("Help"));
                mb.setHelpMenu(m);
            };
            if ("+".equals(menuDef[i])) {
                i++; isNext=true;
                m=getMenu(f,menuDef[i]);
                if (m==null) mb.add(m=new Menu(menuDef[i]));
                i++;
            };
            if (isNext) lastSep=false;
            if ("-".equals(menuDef[i])) {
                if (!lastSep) m.addSeparator();
                lastSep=true;
                i++; isNext=true;
            };
            if (!isNext) {
                String rac=menuDef[i+1];
                if (AC_WTMCLOSE.equals(rac)) rac=AC_WTMCLOSE+we.id;
                mi=getItem(f,rac);
                if (mi==null) {
                    if (menuDef[i].charAt(0)=='#') {
                        m.add(mi=new Menu(menuDef[i].substring(1)));
                    } else {
                        if (menuDef[i].charAt(0)=='@' || menuDef[i].charAt(0)=='!') {
                            m.add(mi=new MenuItem(menuDef[i].substring(2),new MenuShortcut(menuDef[i].charAt(1),(menuDef[i].charAt(0)=='!')))).setActionCommand(rac);
                        } else
                            m.add(mi=new MenuItem(menuDef[i])).setActionCommand(rac);
                        mi.addActionListener(al);
                    }
                    if (AC_WTMCLOSE.equals(menuDef[i+1])) mi.addActionListener(wt);
                    lastSep=false;
                };
                i+=2;
            };
        };
        
        f.setMenuBar(mb);
        return mb;
    };
    
    public static Menu getMenu(final Frame f, final String nam) {
        final MenuBar mb=f.getMenuBar();
        if (mb==null) return null;
        final int mc=mb.getMenuCount();
        int i=0;
        while(i<mc) {
            final Menu m=mb.getMenu(i);
            if (m.getLabel().equals(nam))
                return m;
            i++;
        };
        return null;
    };
    
    public static MenuItem getItem(final Frame f,final String nam) {
        final MenuBar mb=f.getMenuBar();
        if (mb==null) return null;
        final int mc=mb.getMenuCount();
        int i=0;
        while(i<mc) {
            final Menu m=mb.getMenu(i);
            final int ic=m.getItemCount();
            int j=0;
            while(j<ic) {
                final MenuItem mi=m.getItem(j);
                if (mi.getActionCommand().equals(nam))
                    return mi;
                j++;
            };
            i++;
        };
        return null;
    };
    
    public static MenuItem getItemByLabel(final Frame f,final String nam) {
        final MenuBar mb=f.getMenuBar();
        if (mb==null) return null;
        final int mc=mb.getMenuCount();
        int i=0;
        while(i<mc) {
            final Menu m=mb.getMenu(i);
            final int ic=m.getItemCount();
            int j=0;
            while(j<ic) {
                final MenuItem mi=m.getItem(j);
                if (mi.getLabel().compareTo(nam)==0)
                    return mi;
                j++;
            };
            i++;
        };
        return null;
    };
}
