import java.awt.Frame;

/* framework for pligins for KLIMT */

public class Plugin {
    public static final int PT_GenTree = 0x8010;

    String author="<unknown>";
    String name  ="<unnamed>";
    String desc  ="";
    int    type  =0;
    String err   =null;

    public boolean cancel=false; /* can be checked after pluginDlg whether used cancelled manually */
    
    String getName() { return name; }
    String getAuthor() { return author; }
    String getDescription() { return desc; }
    int    getType() { return type; }
    String getLastError() { return err; }

    public void setParameter(String par, Object val) {}
    public Object getParameter(String par) { return null; }

    public boolean initPlugin() { return true; }
    public boolean donePlugin() { return true; }

    public boolean checkParameters() { return true; }
    public boolean pluginDlg(Frame f) { return true; }
    public boolean execPlugin() { return true; }
}
