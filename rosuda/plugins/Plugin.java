package org.rosuda.plugins;

import java.awt.Frame;

/* framework for pligins for KLIMT */

public class Plugin {
    public static final int PT_GenTree = 0x8010;

    protected String author="<unknown>";
    protected String name  ="<unnamed>";
    protected String desc  ="";
    protected int    type  =0;
    protected String err   =null;

    public boolean cancel=false; /* can be checked after pluginDlg whether used cancelled manually */

    public String getName() { return name; }
    public String getAuthor() { return author; }
    public String getDescription() { return desc; }
    public int    getType() { return type; }
    public String getLastError() { return err; }

    public void setParameter(String par, Object val) {}
    public Object getParameter(String par) { return null; }

    public boolean initPlugin() { return true; }
    public boolean donePlugin() { return true; }

    public boolean checkParameters() { return true; }
    public boolean pluginDlg(Frame f) { return true; }
    public boolean execPlugin() { return true; }
}
