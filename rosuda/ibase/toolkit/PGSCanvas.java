import java.awt.Frame;
import java.awt.Graphics;
import java.io.PrintStream;

/** PGScanvas - extends {@link DBCanvas} by adding generic functionality for
    exporting the content to PGS metafile or PostScript format. Any implementing
    class must use PoGraSS methods instead of Graphics.
    @version $Id$
*/
public class PGSCanvas extends DBCanvas implements Commander {
    /** frame that owns this canvas. can be null if none does. it is mainly used
	to identify current frame in calls to dialogs */
    Frame myFrame=null;
    /** description of this canvas. */
    String desc="untitled PGS canvas";

    /** inProgress flag to avoid recursions in paint methods */
    boolean inProgress=false;

    /** paintBuffer simply calls {@link #paintPoGraSS} on the supplied {@link Graphics}.
        Any further classes should override {@link #paintPoGraSS} instead of
        {@link #paintBuffer}  */
    public void paintBuffer(Graphics g) {
        if (inProgress) return; /* avoid recursions */
        inProgress=true;
	PoGraSSgraphics p=new PoGraSSgraphics(g);
	paintPoGraSS(p);
        inProgress=false;
    };

    /** set the corresponding frame that contains this canvas. It is used mainly
	for dialog boxes to raise the correct frame before entering modal state.
	If no frame is set, default common frame is used by the dialogs. */
    public void setFrame(Frame owner) { myFrame=owner; };
    /** returns corresponding frame containing this canvas as set by {@link #setFrame} 
	@return associated frame */
    public Frame getFrame() { return myFrame; };
    /** set canvas title */
    public void setTitle(String t) { desc=t; };
    /** return canvas title
	@return canvas title */
    public String getTitle() { return desc; };

    /** abstract paint class to be implemented by any descendants */
    public void paintPoGraSS(PoGraSS g) {};

    /** default handing of commands "exportPGS" and "exportPS". Any descendant should
	call <code>super.run(o,cmd)</code> to retain this functionality */
    public Object run(Object o, String cmd) {
        if (cmd=="exportPGS") {
            PoGraSSmeta p=new PoGraSSmeta();
            paintPoGraSS(p);
	    PrintStream outs=Tools.getNewOutputStreamDlg(myFrame,"Export as PoGraSS to ...","output.pgs");
	    if (outs!=null) {
		outs.print(p.getMeta());
		outs.close(); outs=null;
                //(new MsgDialog(null,"PGS Export","Current plot has been exported to output.pgs.")).show();
            };
        };
	if (cmd=="exportPS") {
	    PrintStream outs=Tools.getNewOutputStreamDlg(myFrame,"Export as PostScript to ...","output.ps");
	    if (outs!=null) {
		PoGraSSPS p=new PoGraSSPS(outs);
		p.setTitle(desc);
		paintPoGraSS(p);
		p=null;
		outs.close();
		outs=null;
	    };
	}; 
	return null;
    };
};
