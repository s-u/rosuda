package org.rosuda.ibase.toolkit;

import java.util.Vector;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.io.PrintStream;

import org.rosuda.ibase.*;
import org.rosuda.pograss.*;
import org.rosuda.util.*;

/** PGScanvas - extends {@link LayerCanvas} by adding generic functionality for
    exporting the content to PGS metafile or PostScript format. Any implementing
    class must use PoGraSS methods instead of Graphics.
    @version $Id$
*/
public class PGSCanvas extends LayerCanvas implements Commander, Dependent {
    /** frame that owns this canvas. can be null if none does. it is mainly used
	to identify current frame in calls to dialogs */
    protected Frame myFrame=null;
    /** description of this canvas. */
    protected String desc="untitled PGS canvas";

    static Notifier globalNotifier=null;
    
    /** plot manager for any additional objects */
    protected PlotManager pm;

    /** inProgress flag to avoid recursions in paint methods */
    protected boolean inProgress=false;

    /** X-axis of the plot coordinates. beware that it may be <code>null</code> */
    protected Axis ax;
    /** Y-axis of the plot coordinates. beware that it may be <code>null</code> */
    protected Axis ay;

    protected boolean cancel;
    protected Dialog intDlg;
    
    public PGSCanvas(int layers, Axis x, Axis y) {
        this(layers);
	ax=x; ay=y;
    }

    public PGSCanvas(int layers) {
	super(layers);
	pm=new PlotManager(this);
        if (globalNotifier==null) globalNotifier=new Notifier();
        globalNotifier.addDepend(this);
    }

    public PGSCanvas() { // if no layer # specified, use 1 resulting in old behavior when DBCanvas was used
        this(1);
    }

    protected void finalize() {
        globalNotifier.delDepend(this);
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

    /** returns the global notifier common to all PGScanvas descendants. It is mainly used by routines which change some user settings and need to notify all plots regardless of content. */
    public static Notifier getGlobalNotifier() {
        if (globalNotifier==null) globalNotifier=new Notifier();
        return globalNotifier;
    }
    
    /** paintBuffer simply calls {@link #paintPoGraSS} on the supplied {@link Graphics}.
        Any further classes should override {@link #paintPoGraSS} instead of
        {@link #paintLayer}  */
    public void paintLayer(Graphics g, int layer) {
        if (inProgress) return; /* avoid recursions */
        inProgress=true;
        paintLayerCounter=0;
	PoGraSSgraphics p=new PoGraSSgraphics(g,layer);
	paintPoGraSS(p);
	endPaint(p);
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

    public Axis getXAxis() { return ax; }
    public Axis getYAxis() { return ay; }
    
    /** this method provides an API to fetch data contents of the plot. The id is implementation-dependent, but first two variables x and y should be mapped to 0 and 1 correspondingly. Therefore every plot containting data must support getData(0). For invalid ids <code>null</code> is returned. */
    public SVar getData(int id) { return null; }

    protected int paintLayerCounter;

    protected void nextLayer(PoGraSS p) {
	if (pm!=null) pm.drawLayer(p,paintLayerCounter,layers);
	paintLayerCounter++;
	p.nextLayer();
    }

    protected void endPaint(PoGraSS p) {
	while (paintLayerCounter<layers)
	    nextLayer(p);
    }

    public void forcedFlush() {
        Rectangle r=getBounds();
        setUpdateRoot(0);
        //setSize(r.width-1,r.height-1);
        setSize(r.width,r.height);
    }
    
    /** default handing of commands "exportPGS" and "exportPS". Any descendant should
	call <code>super.run(o,cmd)</code> to retain this functionality */
    public Object run(Object o, String cmd) {
        if (cmd=="BREAK" && Common.breakDispatcher!=null) {
            Common.breakDispatcher.NotifyAll(new NotifyMsg(this,Common.NM_BREAK));
        }
        if (cmd=="prefs") {
            Platform.getPlatform().handlePrefs();
        }
        if (cmd=="exportPGS") {
            PoGraSSmeta p=new PoGraSSmeta();
            paintPoGraSS(p);
	    endPaint(p);
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
		endPaint(p);
		p=null;
		outs.close();
		outs=null;
	    };
	};
        if (cmd=="exportPDF") {
            new MsgDialog(myFrame,"Information","Please note that the PDF export is very experimental. If the result is unsatisfactory, please use the PostScript export instead.");
            PrintStream outs=Tools.getNewOutputStreamDlg(myFrame,"Export as PDF to ...","output.pdf");
            if (outs!=null) {
                PoGraSSPDF p=new PoGraSSPDF(outs);
                p.setTitle(desc);
                paintPoGraSS(p);
		endPaint(p);
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
            PoGraSS p=null;
            try {
                Class c=Class.forName("PoGraSSSVG");
                p=(PoGraSS)c.newInstance();
                svgExtensionPresent=true;
            } catch (Throwable ee) {
                System.out.println("Cannot load SVG classes: "+ee.getMessage());
                ee.printStackTrace();
            };
            if (svgExtensionPresent) {
                PrintStream outs=Tools.getNewOutputStreamDlg(myFrame,"Export as SVG to ...","output.svg");
                if (outs!=null) {
                    p.setOutPrintStream(outs);
                    p.setTitle(desc);
                    paintPoGraSS(p);
		    endPaint(p);
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

    public void Notifying(NotifyMsg msg, Object o, Vector path) {
        if (myFrame!=null)
            myFrame.setBackground(Common.backgroundColor);
        setBackground(Common.backgroundColor);
        setUpdateRoot(0);
        repaint();
    }    
}
