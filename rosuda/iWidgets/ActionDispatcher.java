//
//  ActionDispatcher.java
//  iWidgets
//
//  Created by Simon Urbanek on Wed Jul 21 2004.
//  Copyright (c) 2004 Simon Urbanek. All rights reserved.
//
//  $Id$

package org.rosuda.iWidgets;

import java.awt.*;
import java.awt.event.*;
import javax.swing.event.*;
import java.util.*;

public class ActionDispatcher implements ActionListener, ChangeListener, ListSelectionListener {
    static ActionDispatcher globalDispatcher = new ActionDispatcher();
    static int uaid = 1;

    Vector list;
    
    class ADEntry {
        public Object obj;
        int    uaid;
        ADEntry(Object o, int id) { obj=o; uaid=id; }
    }
    
    public ActionDispatcher() {
        list=new Vector();
    }

    public static ActionDispatcher getGlobalDispatcher() { return globalDispatcher; }
    
    synchronized public String add(Object o) {
        list.addElement(new ADEntry(o, uaid));
        uaid++;
        return "actionDispEl."+(uaid-1);
    }

    synchronized public String rm(Object o) {
        int i=0, ct=list.size();
        while (i<ct) {
            ADEntry e=(ADEntry)list.elementAt(i);
            if (e.obj==o) {
                list.removeElement(e);
                return "actionDispEl."+e.uaid;
            }
            i++;
        }
        return null;
    }

    synchronized public String getID(Object o) {
        int i=0, ct=list.size();
        while (i<ct) {
            ADEntry e=(ADEntry)list.elementAt(i);
            if (e.obj==o)
                return "actionDispEl."+e.uaid;
            i++;
        }
        return null;
    }
    
    synchronized public Object getObject(String s) {
        if (s.substring(0,13).equals("actionDispEl.")) {
            String ids=s.substring(13);
            int i=0;
            try {
                i=Integer.parseInt(ids);
            } catch (NumberFormatException nfe) {}
            if (i<1) return null;
            int j=0, ct=list.size();
            while (j<ct) {
                ADEntry e=(ADEntry)list.elementAt(j);
                if (e.uaid==i) return e.obj;
                j++;
            }
        }
        return null;
    }

    synchronized public void rmByName(String s) {
        if (s.substring(0,13).equals("actionDispEl.")) {
            String ids=s.substring(13);
            int i=0;
            try {
                i=Integer.parseInt(ids);
            } catch (NumberFormatException nfe) {}
            if (i<1) return;
            int j=0, ct=list.size();
            while (j<ct) {
                ADEntry e=(ADEntry)list.elementAt(j);
                if (e.uaid==i) {
                    list.removeElementAt(j);
                    return;
                }
                j++;
            }
        }
    }
    
    public void actionPerformed(ActionEvent e) {
        try {
            String sid=getID(e.getSource());
            if (sid==null)
                System.out.println("Can't find object for action "+e+" and source "+e.getSource()+".\n");
            else
                org.rosuda.JRI.Rengine.getMainEngine().eval(".dispatch.event.for(\""+sid+"\",type=\"onAction\",action=\""+e.getActionCommand()+"\")");
        } catch (Exception ex) {
            System.out.println("Couldn't dispatch event, error: "+ex);
        }
    }

    public void stateChanged(ChangeEvent e) {
        try {
            String sid=getID(e.getSource());
            if (sid==null)
                System.out.println("Can't find object for source "+e.getSource()+".\n");
            else
                org.rosuda.JRI.Rengine.getMainEngine().eval(".dispatch.event.for(\""+sid+"\",type=\"onChange\")");
        } catch (Exception ex) {
            System.out.println("Couldn't dispatch event, error: "+ex);
        }
    }

    public void valueChanged(ListSelectionEvent e) {
        try {
            String sid=getID(e.getSource());
            if (sid==null)
                System.out.println("Can't find object for source "+e.getSource()+".\n");
            else
                org.rosuda.JRI.Rengine.getMainEngine().eval(".dispatch.event.for(\""+sid+"\",type=\"onChange\")");
        } catch (Exception ex) {
            System.out.println("Couldn't dispatch event, error: "+ex);
        }
    }
}
