//
//  EzMenu.java
//  Klimt
//
//  Created by Simon Urbanek on Fri Oct 11 2002.
//  $Id$
//

import java.awt.*;
import java.awt.event.ActionListener;

/** class that simplified menu building from lists */
public class EzMenu {
    public static MenuBar getEzMenu(Frame f, ActionListener al, String[] menuDef) {
        WinTracker wt=WinTracker.current;
        WTentry we=wt.getEntry(f);
        MenuBar mb=f.getMenuBar();
        if (mb==null) mb=new MenuBar();
        Menu m=null;
        int i=0;
        boolean lastSep=false;
        while (menuDef[i]!="0") {
            MenuItem mi;
            boolean isNext=false;
            if (menuDef[i]=="0") break;
            if (menuDef[i]=="~File.Graph") {
                i++; isNext=true;
                m.add(mi=new MenuItem("Save as PGS ...")).setActionCommand("exportPGS"); mi.addActionListener(al);
                m.add(mi=new MenuItem("Save as PostScript ...",new MenuShortcut('P'))).setActionCommand("exportPS"); mi.addActionListener(al);
                m.add(mi=new MenuItem("Save as PDF ...",new MenuShortcut('P',true))).setActionCommand("exportPDF"); mi.addActionListener(al);
                m.add(mi=new MenuItem("Save as SVG ...")).setActionCommand("exportSVG"); mi.addActionListener(al);
                m.addSeparator();
                m.add(mi=new MenuItem("Save selected as ...")).setActionCommand("exportCases"); mi.addActionListener(al);
                m.addSeparator();
                m.add(mi=new MenuItem("Close window",new MenuShortcut('W'))).setActionCommand("WTMclose"+we.id); mi.addActionListener(wt);
                m.add(mi=new MenuItem("Quit",new MenuShortcut('Q'))).setActionCommand("exit"); mi.addActionListener(al);
            };
            if (menuDef[i]=="~Edit") {
                i++; isNext=true;
                mb.add(m=new Menu("Edit"));
                m.add(mi=new MenuItem("Select all",new MenuShortcut('A'))).setActionCommand("selAll"); mi.addActionListener(al);
                m.add(mi=new MenuItem("Clear selection",new MenuShortcut('D'))).setActionCommand("selNone"); mi.addActionListener(al);
                m.add(mi=new MenuItem("Invert selection",new MenuShortcut('I'))).setActionCommand("selInv"); mi.addActionListener(al);
            };
            if (menuDef[i]=="~Window") {
                i++; isNext=true;
                if (we!=null && we.menu!=null)
                    mb.add(we.menu); // add window menu
            };
            if (menuDef[i]=="~Help") {
                i++; isNext=true;
                mb.add(m=new Menu("Help"));
                mb.setHelpMenu(m);
            };
            if (menuDef[i]=="+") {
                i++; isNext=true;
                m=getMenu(f,menuDef[i]);
                if (m==null) mb.add(m=new Menu(menuDef[i]));
                i++;
            };
            if (isNext) lastSep=false;
            if (menuDef[i]=="-") {
                if (!lastSep) m.addSeparator();
                lastSep=true;
                i++; isNext=true;
            };
            if (!isNext) {
                String rac=menuDef[i+1];
                if (rac=="WTMclose") rac="WTMclose"+we.id;
                mi=getItem(f,rac);
                if (mi==null) {
                    if (menuDef[i].charAt(0)=='@' || menuDef[i].charAt(0)=='!') {
                    m.add(mi=new MenuItem(menuDef[i].substring(2),new MenuShortcut((int)menuDef[i].charAt(1),(menuDef[i].charAt(0)=='!')))).setActionCommand(rac);
                    } else
                        m.add(mi=new MenuItem(menuDef[i])).setActionCommand(rac);
                    mi.addActionListener(al);
                    if (menuDef[i+1]=="WTMclose") mi.addActionListener(wt);
                    lastSep=false;
                };
                i+=2;
            };
        };

        f.setMenuBar(mb);
        return mb;
    };

    public static Menu getMenu(Frame f, String nam) {
        MenuBar mb=f.getMenuBar();
        if (mb==null) return null;
        int mc=mb.getMenuCount();
        int i=0;
        while(i<mc) {
            Menu m=mb.getMenu(i);
            if (m.getLabel()==nam)
                return m;
            i++;
        };
        return null;
    };

    public static MenuItem getItem(Frame f,String nam) {
        MenuBar mb=f.getMenuBar();
        if (mb==null) return null;
        int mc=mb.getMenuCount();
        int i=0;
        while(i<mc) {
            Menu m=mb.getMenu(i);
            int ic=m.getItemCount();
            int j=0;
            while(j<ic) {
                MenuItem mi=m.getItem(j);
                if (mi.getActionCommand()==nam)
                    return mi;
                j++;
            };
            i++;
        };
        return null;
    };
}
