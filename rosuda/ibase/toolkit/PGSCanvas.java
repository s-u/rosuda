import java.awt.Frame;
import java.awt.Graphics;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.io.PrintStream;

/** PGScanvas - extends {@link LayerCanvas} by adding generic functionality for
    exporting the content to PGS metafile or PostScript format. Any implementing
    class must use PoGraSS methods instead of Graphics.
    @version $Id$
*/
public class PGSCanvas extends LayerCanvas implements Commander {
    /** frame that owns this canvas. can be null if none does. it is mainly used
	to identify current frame in calls to dialogs */
    Frame myFrame=null;
    /** description of this canvas. */
    String desc="untitled PGS canvas";

    /** plot manager for any additional objects */
    PlotManager pm;

    /** inProgress flag to avoid recursions in paint methods */
    boolean inProgress=false;

    boolean cancel;
    Dialog intDlg;
    
    public PGSCanvas(int layers) {
        super(layers);
    }

    public PGSCanvas() { // if no layer # specified, use 1 resulting in old behavior when DBCanvas was used
        super(1);
    }

    class IDlgCL implements ActionListener {
        PGSCanvas c;
        IDlgCL(PGSCanvas cc) { c=cc; };

        /** activated if a button was pressed. It determines whether "cancel" was pressed or OK" */
        public void actionPerformed(ActionEvent e) {
            c.cancel=!e.getActionCommand().equals("OK");
            c.intDlg.setVisible(false);
        };
    }

    /** paintBuffer simply calls {@link #paintPoGraSS} on the supplied {@link Graphics}.
        Any further classes should override {@link #paintPoGraSS} instead of
        {@link #paintLayer}  */
    public void paintLayer(Graphics g, int layer) {
        if (inProgress) return; /* avoid recursions */
        inProgress=true;
	PoGraSSgraphics p=new PoGraSSgraphics(g,layer);
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

    /** get the PlotManager associated with this plot */
    public PlotManager getPlotManager() { return pm; }

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
        if (cmd=="exportPDF") {
            PrintStream outs=Tools.getNewOutputStreamDlg(myFrame,"Export as PDF to ...","output.pdf");
            if (outs!=null) {
                PoGraSSPDF p=new PoGraSSPDF(outs);
                p.setTitle(desc);
                paintPoGraSS(p);
                p=null;
                outs.close();
                outs=null;
            };
        };
        if (cmd=="sizeDlg") {
            Dialog d=intDlg=new Dialog(myFrame,"Set plot size",true);
            IDlgCL ic=new IDlgCL(this);
            d.setBackground(Color.white);
            d.setLayout(new BorderLayout());
            d.add(new SpacingPanel(),BorderLayout.WEST);
            d.add(new SpacingPanel(),BorderLayout.EAST);
            Panel bp=new Panel(); bp.setLayout(new FlowLayout());
            Button b,b2;
            bp.add(b=new Button("OK"));bp.add(b2=new Button("Cancel"));
            d.add(bp,BorderLayout.SOUTH);
            d.add(new Label(" "),BorderLayout.NORTH);
            Panel cp=new Panel(); cp.setLayout(new FlowLayout());
            d.add(cp);
            cp.add(new Label("width: "));
            TextField tw=new TextField(""+getSize().width,6);
            TextField th=new TextField(""+getSize().height,6);
            cp.add(tw);
            cp.add(new Label(", height: "));
            cp.add(th);
            d.pack();
            b.addActionListener(ic);b2.addActionListener(ic);
            d.setVisible(true);
            if (!cancel) {
                int w=Tools.parseInt(tw.getText());
                int h=Tools.parseInt(th.getText());
                if(w<10) w=getSize().width;
                if(h<10) w=getSize().height;
                setSize(w,h);
                if (myFrame!=null) myFrame.pack();
            };
            d.dispose();
        };
        if (cmd=="exportSVG") {
            boolean svgExtensionPresent=false;
            try {
                Class c=Class.forName("PoGraSSSVG");
                svgExtensionPresent=true;
            } catch (Throwable ee) {
            };
            if (svgExtensionPresent) {
                PrintStream outs=Tools.getNewOutputStreamDlg(myFrame,"Export as SVG to ...","output.svg");
                if (outs!=null) {
                    PoGraSSSVG p=new PoGraSSSVG(outs);
                    p.setTitle(desc);
                    paintPoGraSS(p);
                    p=null;
                    outs.close();
                    outs=null;
                };
            } else {
                (new MsgDialog(myFrame,"PGS Export","Cannot find SVG-extensions. Please make sure that PoGraSSSVG is properly installed.")).show();
            };
        };
        return null;
    };
}
