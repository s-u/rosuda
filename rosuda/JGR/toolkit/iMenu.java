package org.rosuda.JGR.toolkit;

/**
 *  iMenu
 * 
 *  simple menu
 *  
 */


import java.awt.*;
import java.awt.event.*;
import javax.swing.*;


import org.rosuda.ibase.*;
import org.rosuda.ibase.toolkit.*;

/** class that simplified menu building from lists */
public class iMenu {
    public static boolean staticInitDone=false;
    public static boolean hasSVG=false;

    public static JMenuBar getMenu(JFrame f, ActionListener al, String[] menuDef) {
        if (!staticInitDone) {
            try { Class c=Class.forName("PoGraSSSVG"); hasSVG=true; }
            catch (Throwable ee) {};
            staticInitDone=true;
        };
        WinTracker wt=WinTracker.current;
        WTentry we=(wt==null)?null:wt.getEntry(f);
        JMenuBar mb=f.getJMenuBar();
        if (mb==null) mb=new JMenuBar();
        JMenu m=null;
        int i=0;
        boolean lastSep=false;
        while (menuDef[i]!="0") {
            JMenuItem mi;
            boolean isNext=false;
            if (menuDef[i]=="0") break;
            if (menuDef[i]=="~File.Basic.End") {
                i++; isNext=true;
                if (Common.isMac()) {
                    mi=new JMenuItem("Close window");
                    mi.setAccelerator(javax.swing.KeyStroke.getKeyStroke('W',Toolkit.getDefaultToolkit().getMenuShortcutKeyMask(), false));
                    //mi.setActionCommand("WTMclose"+we.id);
                    mi.setActionCommand("exit");
                    mi.addActionListener(al);
                    m.add(mi);
                }
                if (!Common.isMac()) {
                    m.addSeparator();
                    mi=new JMenuItem("Quit");
                    mi.setAccelerator(javax.swing.KeyStroke.getKeyStroke('Q',Toolkit.getDefaultToolkit().getMenuShortcutKeyMask(), false));
                    mi.setActionCommand("exit");
                    mi.addActionListener(al);
                    m.add(mi);
                }
            }
            if (menuDef[i]=="~File.Quit") {
                i++; isNext=true;
                if (!Common.isMac()) {
                    m.addSeparator();
                    mi=new JMenuItem("Quit");
                    mi.setAccelerator(javax.swing.KeyStroke.getKeyStroke('Q', Toolkit.getDefaultToolkit().getMenuShortcutKeyMask(), false));
                    mi.setActionCommand("exit");
                    mi.addActionListener(al);
                    m.add(mi);
                }
            }
            if (menuDef[i]=="~File.Graph") {
                i++; isNext=true;
                mi=new JMenuItem("Save as PGS ...");
                mi.setActionCommand("exportPGS");
                mi.addActionListener(al);
                m.add(mi);
                mi=new JMenuItem("Save as PostScript ...");
                mi.setAccelerator(javax.swing.KeyStroke.getKeyStroke('P', Toolkit.getDefaultToolkit().getMenuShortcutKeyMask(), false));
                mi.setActionCommand("exportPS");
                mi.addActionListener(al);
                m.add(mi);
                mi=new JMenuItem("Save as PDF ...");
                mi.setAccelerator(javax.swing.KeyStroke.getKeyStroke('P', Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()+1, false));
                mi.setActionCommand("exportPDF");
                mi.addActionListener(al);
                m.add(mi);
                mi=new JMenuItem("Save as SVG ...");
                mi.setActionCommand("exportSVG");
                mi.addActionListener(al);
                m.add(mi);
                if (!hasSVG) mi.setEnabled(false);
                if (!Common.isMac()) {
                    m.addSeparator();
                    mi=new JMenuItem("Preferences ...");
                    mi.setActionCommand("prefs");
                    mi.addActionListener(al);
                    m.add(mi);
                }
                m.addSeparator();
                mi=new JMenuItem("Save selected as ...");
                mi.setActionCommand("exportCases");
                mi.addActionListener(al);
                m.add(mi);
                m.addSeparator();
                if (Common.supportsBREAK) {
                    mi=new JMenuItem("Break");
                    mi.setAccelerator(javax.swing.KeyStroke.getKeyStroke('B', Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()+1, false));
                    mi.setActionCommand("BREAK");
                    mi.addActionListener(al);
                    m.add(mi);
                    m.addSeparator();
                }
                mi=new JMenuItem("Close window");
                mi.setAccelerator(javax.swing.KeyStroke.getKeyStroke('W', Toolkit.getDefaultToolkit().getMenuShortcutKeyMask(), false));
                mi.setActionCommand("WTMclose"+we.id);
                mi.addActionListener(wt);
                m.add(mi);
                if (!Common.isMac())
                    mi=new JMenuItem("Quit");
                    mi.setAccelerator(javax.swing.KeyStroke.getKeyStroke('Q', Toolkit.getDefaultToolkit().getMenuShortcutKeyMask(), false));
                    mi.setActionCommand("exit");
                    mi.addActionListener(al);
                    m.add(mi);
            };
            if (menuDef[i].indexOf("~Edit") >=0) {
                i++; isNext=true;
                mb.add(m=new JMenu("Edit"));
                mi=new JMenuItem("Undo");
                mi.setAccelerator(javax.swing.KeyStroke.getKeyStroke('Z', Toolkit.getDefaultToolkit().getMenuShortcutKeyMask(), false));
                mi.setActionCommand("undo");
                mi.addActionListener(al);
                m.add(mi);
                mi=new JMenuItem("Redo");
                mi.setAccelerator(javax.swing.KeyStroke.getKeyStroke('Z', Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()+1, false));
                mi.setActionCommand("redo");
                mi.addActionListener(al);
                m.add(mi);
                m.addSeparator();
                mi=new JMenuItem("Cut");
                mi.setAccelerator(javax.swing.KeyStroke.getKeyStroke('X', Toolkit.getDefaultToolkit().getMenuShortcutKeyMask(), false));
                mi.setActionCommand("cut");
                mi.addActionListener(al);
                m.add(mi);
                mi=new JMenuItem("Copy");
                mi.setAccelerator(javax.swing.KeyStroke.getKeyStroke('C', Toolkit.getDefaultToolkit().getMenuShortcutKeyMask(), false));
                mi.setActionCommand("copy");
                mi.addActionListener(al);
                m.add(mi);
                if (menuDef[i-1].equals("~EditCPS")) {
                    JMenu m2 = new JMenu("Copy Special");
                    JMenuItem mi21 = new JMenuItem("Copy Output");
                    mi21.setActionCommand("copyoutput");
                    mi21.addActionListener(al);
                    m2.add(mi21);
                    JMenuItem mi22 = new JMenuItem("Copy Commands");
                    mi22.setActionCommand("copycmds");
                    mi22.addActionListener(al);
                    m2.add(mi22);
                    JMenuItem mi23 = new JMenuItem("Copy Results");
                    mi23.setActionCommand("copyresult");
                    mi23.addActionListener(al);
                    m2.add(mi23);
                    m.add(m2);
                }
                mi=new JMenuItem("Paste");
                mi.setAccelerator(javax.swing.KeyStroke.getKeyStroke('V', Toolkit.getDefaultToolkit().getMenuShortcutKeyMask(), false));
                mi.setActionCommand("paste");
                mi.addActionListener(al);
                m.add(mi);
                mi=new JMenuItem("Delete");
                mi.setActionCommand("delete");
                mi.addActionListener(al);
                m.add(mi);
                mi=new JMenuItem("Select All");
                mi.setAccelerator(javax.swing.KeyStroke.getKeyStroke('A', Toolkit.getDefaultToolkit().getMenuShortcutKeyMask(), false));
                mi.setActionCommand("selAll");
                mi.addActionListener(al);
                m.add(mi);
                m.addSeparator();
                mi=new JMenuItem("Clear Console");
                mi.setAccelerator(javax.swing.KeyStroke.getKeyStroke('L', Toolkit.getDefaultToolkit().getMenuShortcutKeyMask(), false));
                mi.setActionCommand("clearconsole");
                mi.addActionListener(al);
                m.add(mi);
                if (!Common.isMac()) {
                    m.addSeparator();
                    mi=new JMenuItem("Preferences...");
                    mi.setActionCommand("prefs");
                    mi.addActionListener(al);
                    m.add(mi);
                }
            };
            if (menuDef[i]=="~Window") {
                i++; isNext=true;
                if (we!=null && we.getWindowMenu()!=null)
                    mb.add((JMenu) we.getWindowMenu()); // add window menu
            };
            if (menuDef[i]=="~Help") {
                i++; isNext=true;
                mb.add(m=new JMenu("Help"));
                //mb.setHelpMenu(m);
            };
            if (menuDef[i]=="~About") {
                i++; isNext=true;
                if (!Common.isMac()) {
                    m.addSeparator();
                    mi=new JMenuItem("About");
                    mi.setActionCommand("about");
                    mi.addActionListener(al);
                    m.add(mi);
                }
            };
            if (menuDef[i]=="~Preferences") {
                i++; isNext=true;
                if (!Common.isMac()) {
                    m.addSeparator();
                    mi=new JMenuItem("Preferences...");
                    mi.setActionCommand("prefs");
                    mi.addActionListener(al);
                    m.add(mi);
                }
            };
            if (menuDef[i]=="+") {
                i++; isNext=true;
                m=getMenu(f,menuDef[i]);
                if (m==null) mb.add(m=new JMenu(menuDef[i]));
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
                    if (menuDef[i].charAt(0)=='#') {
                        m.add(mi=new JMenu(menuDef[i].substring(1)));
                    } else {
                        if (menuDef[i].charAt(0)=='@' || menuDef[i].charAt(0)=='!') {
                            mi=new JMenuItem(menuDef[i].substring(2));
                            mi.setAccelerator(javax.swing.KeyStroke.getKeyStroke((int)menuDef[i].charAt(1), Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()+((menuDef[i].charAt(0)=='!')?1:0),false));
                            mi.setActionCommand(rac);
                            m.add(mi);
                        } else
                            m.add(mi=new JMenuItem(menuDef[i])).setActionCommand(rac);
                        mi.addActionListener(al);
                    }
                    if (menuDef[i+1]=="WTMclose") mi.addActionListener(wt);
                    lastSep=false;
                };
                i+=2;
            };
        };

        f.setJMenuBar(mb);
        return mb;
    };

    public static void addMenu(JFrame f, String name) {
        JMenuBar mb = f.getJMenuBar();
        mb.add(new JMenu(name),(mb.getMenuCount()-2));
    }

    public static void addMenuItem(JFrame f, String menu, String name, String command, ActionListener al) {
        JMenu m = getMenu(f,menu);
        JMenuItem mi = new JMenuItem(name);
        mi.addActionListener(al);
        mi.setActionCommand(command);
        m.add(mi);
    }

    public static void addMenuSeparator(JFrame f, String menu) {
        JMenu m = getMenu(f,menu);
        m.addSeparator();
    }
    

    public static JMenu getMenu(JFrame f, String nam) {
        JMenuBar mb=f.getJMenuBar();
        if (mb==null) return null;
        int mc=mb.getMenuCount();
        int i=0;
        while(i<mc) {
            JMenu m=mb.getMenu(i);
            if (m.getText().equals(nam)) return m;
            i++;
        };
        return null;
    };

    public static JMenuItem getItem(JFrame f,String nam) {
        JMenuBar mb=f.getJMenuBar();
        if (mb==null) return null;
        int mc=mb.getMenuCount();
        int i=0;
        while(i<mc) {
            JMenu m=mb.getMenu(i);
            int ic=m.getItemCount();
            int j=0;
            while(j<ic) {
                JMenuItem mi=m.getItem(j);
                if (mi!=null && mi.getActionCommand()==nam)
                    return mi;
                j++;
            };
            i++;
        };
        return null;
    };

    public static JMenuItem getItemByLabel(JFrame f,String nam) {
        JMenuBar mb=f.getJMenuBar();
        if (mb==null) return null;
        int mc=mb.getMenuCount();
        int i=0;
        while(i<mc) {
            JMenu m=mb.getMenu(i);
            int ic=m.getItemCount();
            int j=0;
            while(j<ic) {
                JMenuItem mi=m.getItem(j);
                if (mi!=null && mi.getText().compareTo(nam)==0)
                    return mi;
                j++;
            };
            i++;
        };
        return null;
    };
}
