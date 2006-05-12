package org.rosuda.ibase.toolkit;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Vector;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.*;
import java.awt.image.*;
import java.awt.print.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.io.PrintStream;
import java.io.File;

import org.rosuda.ibase.*;
import org.rosuda.pograss.*;
import org.rosuda.util.*;

/** PGScanvas - extends {@link LayerCanvas} by adding generic functionality for
 * exporting the content to PGS metafile or PostScript format. Any implementing
 * class must use PoGraSS methods instead of Graphics.
 * @version $Id$
 */
public class PGSCanvas extends PlotComponent implements Commander, Dependent, Printable {
    static final String LBL_OK = "OK";
    static final String str2 = "Cancel";
    
    /** frame that owns this canvas. can be null if none does. it is mainly used
     * to identify current frame in calls to dialogs */
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
    
    public PageFormat pageFormat;
    
    /** creates a new PoGraSS-capable driver consisting of layers to draw on, using ps plot component as its target, specified number of layers and the axes x and y. If pc is set to <code>null</code> then a plot component of the default type (see Common.defaultPlotComponentType) is created automatically. Axes can be <code>null</code> if not used. */
    public PGSCanvas(final int gd, final int layers, final Axis x, final Axis y) {
        this(gd, layers);
        ax=x; ay=y;
    }
    
    /** equals to using PGSCanvas(gd, layers, null, null) */
    public PGSCanvas(final int gd, final int layers) {
        super(gd, layers);
        pm=new PlotManager(this);
        if (globalNotifier==null) globalNotifier=new Notifier();
        globalNotifier.addDepend(this);
    }
    
    /** equals to using PGSCanvas(-1, layers) */
    public PGSCanvas(final int layers) {
        this(-1, layers);
    }
    
    /** equals to using PGSCanvas(-1, 1) */
    public PGSCanvas() {
        this(-1, 1);
    }
    
    protected void finalize() {
        globalNotifier.delDepend(this);
    }
    
    public class IDlgCL implements ActionListener {
        PGSCanvas c;
        public IDlgCL(final PGSCanvas cc) { c=cc; };
        
        /** activated if a button was pressed. It determines whether "cancel" was pressed or OK" */
        public void actionPerformed(final ActionEvent e) {
            c.cancel=!"OK".equals(e.getActionCommand());
            c.intDlg.setVisible(false);
        }
    }
    
    /** returns the global notifier common to all PGSCanvas descendants. It is mainly used by routines which change some user settings and need to notify all plots regardless of content. */
    public static Notifier getGlobalNotifier() {
        if (globalNotifier==null) globalNotifier=new Notifier();
        return globalNotifier;
    }
    
    /** paintBuffer simply calls {@link #paintPoGraSS} on the supplied {@link Graphics}.
     * Any further classes should override {@link #paintPoGraSS} instead of
     * {@link #paintLayer}  */
    public void paintLayer(final Graphics g, final int layer) {
        if (inProgress) return; /* avoid recursions */
        inProgress=true;
        if (Global.forceAntiAliasing) {
            final Graphics2D g2=(Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        }
        final PoGraSSgraphics p=new PoGraSSgraphics(g,layer);
        beginPaint(p);
        paintPoGraSS(p);
        endPaint(p);
        inProgress=false;
    }
    
    public int print(final Graphics g, final PageFormat pf, final int pi) {
        if (pi >= 1) {
            return Printable.NO_SUCH_PAGE;
        }
        final Graphics2D g2=(Graphics2D) g;
        // move the origin such that it's inside the printable area
        g2.translate(pf.getImageableX(), pf.getImageableY());
        // TODO: support for something like "fit to page" ... (just use scale(...))
        // we must use paintLayer, becasue paint does all the buffering and rasterizing
        paintLayer(g, -1);
        return Printable.PAGE_EXISTS;
    }
    
    /** set the corresponding frame that contains this canvas. It is used mainly
     * for dialog boxes to raise the correct frame before entering modal state.
     * If no frame is set, default common frame is used by the dialogs. */
    public void setFrame(final Frame owner) {
        myFrame=owner;
    };
    /** returns corresponding frame containing this canvas as set by {@link #setFrame}
     * @return associated frame */
    public Frame getFrame() { return myFrame; };
    /** set canvas title */
    public void setTitle(final String t) { desc=t; };
    /** return canvas title
     * @return canvas title */
    public String getTitle() { return desc; };
    
    /** abstract paint class to be implemented by any descendants. */
    public void paintPoGraSS(final PoGraSS g) {};
    
    /** get the PlotManager associated with this plot */
    public PlotManager getPlotManager() { return pm; }
    
    public Axis getXAxis() { return ax; }
    public Axis getYAxis() { return ay; }
    
    /** this method provides an API to fetch data contents of the plot. The id is implementation-dependent, but first two variables x and y should be mapped to 0 and 1 correspondingly. Therefore every plot containting data must support getData(0). For invalid ids <code>null</code> is returned. */
    public SVar getData(final int id) { return null; }
    
    protected int paintLayerCounter;
    
    protected void nextLayer(final PoGraSS p) {
        if (pm!=null) pm.drawLayer(p,paintLayerCounter,layers);
        paintLayerCounter++;
        p.nextLayer();
    }
    
    /** before using {@link #paintPoGraSS} this method should be called to ensure that a consistent state while painting. Currently the main goal of this function is to reset the paintLayerCounter. */
    protected void beginPaint(final PoGraSS p) {
        paintLayerCounter=0;
    }
    
    /** this methods finalizes painting tasks. If {@link #paintPoGraSS} is called directly this method should not be ommitted. */
    protected void endPaint(final PoGraSS p) {
        while (paintLayerCounter<layers)
            nextLayer(p);
    }
    
    public void forcedFlush() {
        final Rectangle r=getBounds();
        setUpdateRoot(0);
        //setSize(r.width-1,r.height-1);
        setSize(r.width,r.height);
    }
    
    /** default handing of commands "exportPGS" and "exportPS". Any descendant should
     * call <code>super.run(o,cmd)</code> to retain this functionality */
    public Object run(final Object o, final String cmd) {
        if ("BREAK".equals(cmd) && Common.breakDispatcher!=null) {
            Common.breakDispatcher.NotifyAll(new NotifyMsg(this,Common.NM_BREAK));
        }
        if ("prefs".equals(cmd)) {
            Platform.getPlatform().handlePrefs();
        }
        if ("exportPGS".equals(cmd)) {
            final PoGraSSmeta p=new PoGraSSmeta();
            paintPoGraSS(p);
            endPaint(p);
            final PrintStream outs=Tools.getNewOutputStreamDlg(myFrame,"Export as PoGraSS to ...","output.pgs");
            if (outs!=null) {
                outs.print(p.getMeta());
                outs.close();
                //(new MsgDialog(null,"PGS Export","Current plot has been exported to output.pgs.")).show();
            };
        };
        if ("exportPS".equals(cmd)) {
            final PrintStream outs=Tools.getNewOutputStreamDlg(myFrame,"Export as PostScript to ...","output.ps");
            if (outs!=null) {
                final PoGraSSPS p=new PoGraSSPS(outs);
                p.setTitle(desc);
                beginPaint(p);
                paintPoGraSS(p);
                endPaint(p);
                outs.close();
            };
        };
        if ("exportPDF".equals(cmd)) {
            new MsgDialog(myFrame,"Information","Please note that the PDF export is very experimental. If the result is unsatisfactory, please use the PostScript export instead.");
            final PrintStream outs=Tools.getNewOutputStreamDlg(myFrame,"Export as PDF to ...","output.pdf");
            if (outs!=null) {
                final PoGraSSPDF p=new PoGraSSPDF(outs);
                p.setTitle(desc);
                beginPaint(p);
                paintPoGraSS(p);
                endPaint(p);
                outs.close();
            };
        };
        if ("sizeDlg".equals(cmd)) {
            final Dialog d=intDlg=new Dialog(myFrame,"Set plot size",true);
            
            d.setBackground(Color.white);
            d.setLayout(new BorderLayout());
            d.add(new SpacingPanel(),BorderLayout.WEST);
            d.add(new SpacingPanel(),BorderLayout.EAST);
            final Panel bp=new Panel(); bp.setLayout(new FlowLayout());
            final Button b;
            bp.add(b=new Button(LBL_OK));final Button b2;
            bp.add(b2=new Button(str2));
            d.add(bp,BorderLayout.SOUTH);
            d.add(new Label(" "),BorderLayout.NORTH);
            final Panel cp=new Panel(); cp.setLayout(new FlowLayout());
            d.add(cp);
            cp.add(new Label("width: "));
            final TextField tw=new TextField(""+getSize().width,6);
            final TextField th=new TextField(""+getSize().height,6);
            cp.add(tw);
            cp.add(new Label(", height: "));
            cp.add(th);
            d.pack();
            final IDlgCL ic = new IDlgCL(this);
            b.addActionListener(ic);b2.addActionListener(ic);
            d.setVisible(true);
            if (!cancel) {
                int w=Tools.parseInt(tw.getText());
                final int h=Tools.parseInt(th.getText());
                if(w<10) w=getSize().width;
                if(h<10) w=getSize().height;
                setSize(w,h);
                if (myFrame!=null) myFrame.pack();
            };
            d.dispose();
        };
        if ("javaPrint".equals(cmd)) {
            final PrinterJob printJob = PrinterJob.getPrinterJob();
            if (pageFormat==null) pageFormat=printJob.defaultPage();
            printJob.setPrintable(this, pageFormat);
            if (printJob.printDialog()) {
                try {
                    printJob.print();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }
        if ("pageSetup".equals(cmd)) {
            final PrinterJob printJob = PrinterJob.getPrinterJob();
            if (pageFormat==null) pageFormat=printJob.defaultPage();
            pageFormat=printJob.pageDialog(pageFormat);
        }
        if ("exportBitmapDlg".equals(cmd)) {
            final Dialog d=intDlg=new Dialog(myFrame,"Export as bitmap with size",true);
            
            d.setBackground(Color.white);
            d.setLayout(new BorderLayout());
            d.add(new SpacingPanel(),BorderLayout.WEST);
            d.add(new SpacingPanel(),BorderLayout.EAST);
            final Panel bp=new Panel(); bp.setLayout(new FlowLayout());
            final Button b;
            bp.add(b=new Button(LBL_OK));final Button b2;
            bp.add(b2=new Button(str2));
            d.add(bp,BorderLayout.SOUTH);
            d.add(new Label(" "),BorderLayout.NORTH);
            final Panel cp=new Panel(); cp.setLayout(new FlowLayout());
            d.add(cp);
            cp.add(new Label("width: "));
            final int ow=getSize().width;
            final int oh=getSize().height;
            final TextField tw=new TextField(""+ow,6);
            final TextField th=new TextField(""+oh,6);
            
            cp.add(tw);
            cp.add(new Label(", height: "));
            cp.add(th);
            cp.add(new Label(", font size: "));
            final TextField tfs = new TextField("10",6);
            cp.add(tfs);
            d.pack();
            final IDlgCL ic = new IDlgCL(this);
            b.addActionListener(ic);b2.addActionListener(ic);
            d.setVisible(true);
            if (!cancel) {
                int w=Tools.parseInt(tw.getText());
                final int h=Tools.parseInt(th.getText());
                final double fs=Tools.parseDouble(tfs.getText());
                if(w<10) w=getSize().width;
                if(h<10) w=getSize().height;
                
                setSize(w,h);
                final BufferedImage img=new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
                final Graphics2D g=img.createGraphics();
                final PoGraSSgraphics p=new PoGraSSgraphics(g);
                p.setTitle(desc);
                beginPaint(p);
                p.setFontSize(fs);
                paintPoGraSS(p);
                endPaint(p);
                setSize(ow,oh);
                
                final FileDialog fd=new FileDialog(myFrame,desc,FileDialog.SAVE);
                fd.setModal(true);
                fd.show();
                String fnam="";
                
                if (fd.getDirectory()!=null) fnam+=fd.getDirectory();
                if (fd.getFile()!=null) fnam+=fd.getFile();
                
                if (!PngEncoder.savePNG(new File(fnam),new PngEncoder(img, true, PngEncoder.FILTER_NONE,7)))
                    System.err.println("PGSCanvas.run.exportBitmapDlg: Unable to write PNG file \""+fnam+"\"");
            }
            d.dispose();
        }
        if ("exportSVG".equals(cmd)) {
            boolean svgExtensionPresent=false;
            PoGraSS p=null;
            try {
                final Class c=Class.forName("PoGraSSSVG");
                p=(PoGraSS)c.newInstance();
                svgExtensionPresent=true;
            } catch (Throwable ee) {
                System.out.println("Cannot load SVG classes: "+ee.getMessage());
                ee.printStackTrace();
            };
            if (svgExtensionPresent) {
                final PrintStream outs=Tools.getNewOutputStreamDlg(myFrame,"Export as SVG to ...","output.svg");
                if (outs!=null) {
                    p.setOutPrintStream(outs);
                    p.setTitle(desc);
                    beginPaint(p);
                    paintPoGraSS(p);
                    endPaint(p);
                    outs.close();
                };
            } else {
                (new MsgDialog(myFrame,"PGS Export","Cannot find SVG-extensions. Please make sure that PoGraSSSVG is properly installed.")).show();
            };
        };
        return null;
    };
    
    public void Notifying(final NotifyMsg msg, final Object o, final Vector path) {
        if (myFrame!=null)
            myFrame.setBackground(Common.backgroundColor);
        setBackground(Common.backgroundColor);
        setUpdateRoot(0);
        repaint();
    }
    
    public void setOption(final String variable, final boolean value){
        Field field = null;
        try {
            field = this.getClass().getField(variable);
        } catch (NoSuchFieldException ex) {} // proceed and try to invoke setter method later
        catch (SecurityException ex) {
            ex.printStackTrace();
        }
        
        if(field!=null){
            try {
                field.setBoolean(this,value);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        } else{
            try {
                // try to use setter method
                invokeSetterMethod(variable,Boolean.TYPE,Boolean.valueOf(value));
            } catch (NoSuchMethodException ex) {
                System.err.println("Variable " + variable + " does not exist or cannot be set.");
            }
        }
    }
    
    public void setOption(final String variable, final int value){
        if(!setOptionInt(variable,value))
            System.err.println("Variable " + variable + " does not exist or cannot be set.");
    }
    
    public void setOption(final String variable, final double value){
        Field field = null;
        try {
            field = this.getClass().getField(variable);
        } catch (NoSuchFieldException ex) {} // proceed and try to invoke setter method later
        catch (SecurityException ex) {
            ex.printStackTrace();
        }
        
        if(field!=null){
            try {
                field.setDouble(this,value);
            } catch (IllegalArgumentException ex) {
                if(value-(int)value == 0){
                    if(setOptionInt(variable,(int)value)) return;
                }
            } catch (Exception ex){
                ex.printStackTrace();
            }
        } else{
            try {
                // try to use setter method
                invokeSetterMethod(variable,Double.TYPE,new Double(value));
            } catch (NoSuchMethodException ex) {
                if(value-(int)value == 0)
                    try {
                        invokeSetterMethod(variable,Integer.TYPE,new Integer((int)value));
                        return;
                    } catch (NoSuchMethodException exx) {
                        System.err.println("Variable " + variable + " does not exist or cannot be set.");
                    }
                System.err.println("Variable " + variable + " does not exist or cannot be set.");
            }
        }
    }
    
    public void setOption(final String variable, final String value){
        Field field = null;
        try {
            field = this.getClass().getField(variable);
        } catch (NoSuchFieldException ex) {} // proceed and try to invoke setter method later
        catch (SecurityException ex) {
            ex.printStackTrace();
        }
        
        if(field!=null){
            Object val = value;
            if(Color.class.isAssignableFrom(field.getType())){ // if variable is of type Color
                val = getColorForString(value);
            }
            if(val!=null) try {
                field.set(this,val);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        } else{
            try {
                // try to use setter method
                invokeSetterMethod(variable,value.getClass(),value);
            } catch (NoSuchMethodException ex) {
                System.err.println("Variable " + variable + " does not exist or cannot be set.");
            }
        }
    }
    
    private void invokeSetterMethod(String variable,Class paramType,Object newValue) throws NoSuchMethodException {
        Method setter = null;
        Object parameter = newValue;
        final String methodName = "set" + variable.substring(0,1).toUpperCase() + variable.substring(1);
        try {
            setter = this.getClass().getMethod(methodName, new Class[] {paramType});
        } catch (NoSuchMethodException ex){
            if(String.class.isAssignableFrom(paramType)){ // so maybe this is actually of type Color
                setter = this.getClass().getMethod(methodName, new Class[] {Color.class});
                parameter = getColorForString((String)newValue);
                if(parameter==null) return;
            } else throw ex;
        } catch (SecurityException ex) {
            ex.printStackTrace();
        } catch (IllegalArgumentException ex) {
            ex.printStackTrace();
        }
        if(setter == null) throw new NoSuchMethodException("There is no setter method for " + variable);
        else try {
            setter.invoke(this,new Object[] {parameter});
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    
    private boolean setOptionInt(String variable, int value) {
        Field field = null;
        try {
            field = this.getClass().getField(variable);
        } catch (NoSuchFieldException ex) {} // proceed and try to invoke setter method later
        catch (SecurityException ex) {
            ex.printStackTrace();
            return false;
        }
        
        if(field!=null){
            try {
                field.setInt(this,value);
            } catch (Exception ex) {
                ex.printStackTrace();
                return false;
            }
        } else{
            try {
                // try to use setter method
                invokeSetterMethod(variable,Integer.TYPE,new Integer(value));
            } catch (NoSuchMethodException ex) {
                System.err.println("Variable " + variable + " does not exist or cannot be set.");
                return false;
            }
        }
        return true;
    }
    
    private Color getColorForString(String value) {
        Field col;
        Color ret=null;
        try {
            col = Color.class.getField(value);
            ret = (Color)col.get(null);
        } catch (NoSuchFieldException ex) {
            System.err.println("There is no predefined color named " + value + ".");
        } catch (IllegalAccessException ex) {
            ex.printStackTrace();
        }
        return ret;
    }
    
}
