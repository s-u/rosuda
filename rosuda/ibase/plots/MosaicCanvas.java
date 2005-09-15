package org.rosuda.ibase.plots;

import java.awt.AWTEvent;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Event;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Scrollbar;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.util.StringTokenizer;
import java.util.Vector;
import javax.swing.JFrame;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollBar;
import javax.swing.KeyStroke;
import javax.swing.ToolTipManager;

import org.rosuda.ibase.*;
import org.rosuda.ibase.toolkit.*;
import org.rosuda.util.ImageSelection;

public class MosaicCanvas extends BaseCanvas {
    
    SVar[] v;
    int vs;
    
    int maxLevel;
    
    int w,h,hgap,vgap; //width, height and gaps between mosaics
    double mUse = 0.95; //how much space we use
    
    static final int DISPLAY_MODE_OBSERVED = 0;
    static final int DISPLAY_MODE_EXPECTED = 1;
    static final int DISPLAY_MODE_SAMEBINSIZE = 2;
    static final int DISPLAY_MODE_MULTIPLEBARCHARTS = 3;
    static final int DISPLAY_MODE_FLUCTUATION = 4;
    
    int mode = DISPLAY_MODE_OBSERVED;
    
    FrequencyTable ft;
    
    public MosaicCanvas(Frame f, SVar[] vars, SMarker mark) {
        super(f, mark);
        setTitle("Mosaic Plot");
        this.v = vars;
        maxLevel = v.length; // should not be set here as it can be changed. is this the same as vs: ??
        this.vs = v.length;
        for(int i=0; i<vs; i++){
            v[i].categorize();
        }
        
        String myMenu[]={"+","File","~File.Graph","+","View","Observed","observed","Fluctuation","fluctuation","~Edit","~Window","0"};
        EzMenu.getEzMenu(f,this,myMenu);
        mLeft=10; mRight=10; mTop=10; mBottom=10;
        
        ft = new FrequencyTable(vars);
    }
    
    public void updateObjects() {
        
        create(0,0, getWidth(), getHeight(), "");
        if(pp==null) pp = new PlotPrimitive[rects.size()];
        rects.toArray(pp);
        
        setUpdateRoot(0);
    }
    
    public String queryObject(int i) {
        if (pp!=null && pp[i]!=null) {
            int mark=(int)(((double) pp[i].cases())*pp[i].getMarkedProportion(m,-1)+0.5);
            return ((PPrimMosaic) pp[i]).toString()+"\n"+((mark>0)?(""+mark+" of "+pp[i].cases()+" selected"):(""+pp[i].cases()+" cases"));
        }
        return "N/A";
    }
    
    public Object run(Object o, String cmd) {
        super.run(o,cmd);
        if (m!=null) m.run(o,cmd);
        if (cmd=="observed") { if(mode!=DISPLAY_MODE_OBSERVED) {mode=DISPLAY_MODE_OBSERVED; setUpdateRoot(0); updateObjects(); repaint();}}
        if (cmd=="fluctuation") { if(mode!=DISPLAY_MODE_FLUCTUATION) {mode=DISPLAY_MODE_FLUCTUATION; setUpdateRoot(0); updateObjects(); repaint();}}
        if (cmd=="print") run(o,"exportPS");
        if (cmd=="exit") WinTracker.current.Exit();
        return null;
    }
    
    Vector rects = new Vector();
    Vector Labels = new Vector();
    int[] plevels;
    int[] aGap;
    int[] Gaps;
    boolean printing=false;
    int printFactor=1;
    char[] Dirs;
    double residSum;
    int censor=0;
    public void create(int x1, int y1, int x2, int y2, String info) {
        
        double[] table = ft.getTable();
        double[] exp = ft.getExp();
        
        System.out.println(table);
        for(int i=0; i< table.length; i++) System.out.println(table[i]);
        
        Dirs = new char[v.length];
        for (int i=0; i<v.length; i++ ) {
            if( (i % 2) == 0 )
                Dirs[i] = 'x';
            else
                Dirs[i] = 'y';
        }
        
        int[] levels = ft.getLevels();
        String[][] lnames = ft.getLnames();
        int k = v.length;
        
        rects.removeAllElements();
        Labels.removeAllElements();
        
        plevels = new int[k];               // reverse cumulative product of levels
        plevels[k-1] = 0;
        if( k>1 )
            plevels[k-2] = levels[k-1];	// calculate the number of cells covered by a
        // category in level k
        for (int i=k-3; i>=0; i--) {
            plevels[i] = plevels[i+1] * levels[i+1];
        }
        
        int thisGap;
        int subX=0, subY=0;
        int mulX=1, mulY=1;
        Gaps = new int[maxLevel+2];
        aGap = new int[maxLevel+2];
        
        for( int j=0; j<maxLevel; j++) {
            if( !printing )
                thisGap = (maxLevel - j) * 3;
            else
                thisGap = (maxLevel - j) * 3 * printFactor;
            if( Dirs[j] == 'x' ) {
                subX += thisGap * (levels[j]-1) * mulX;
                mulX *= levels[j];
            } else {
                subY += thisGap * (levels[j]-1) * mulY;
                mulY *= levels[j];
            }
            Gaps[j] = thisGap;
        }
        for( int j=0; j<maxLevel; j++) {
            char dir = Dirs[j];
            int sum = Gaps[j];
            int kk=j+1; // replaced k by kk as variable k already exists
            while(kk<maxLevel) {
                int prod = 1;
                if( Dirs[kk] == dir ) {
                    int l=j+1;
                    while(l<kk) {
                        if( Dirs[l] == dir ) {
                            prod *= levels[l];
                        }
                        l++;
                    }
                    prod *= (levels[kk] - 1);
                    sum += Gaps[kk] * prod;
                }
                kk++;
            }
            aGap[j] = sum;
        }
        residSum = 0;
        for( int i=0; i<table.length; i++ )
            residSum += Math.abs( table[i] - exp[i] ) / Math.sqrt(exp[i]);
        if( Math.abs(residSum) < 0.0000001 ) {
            residSum =  1;
            for( int i=0; i<table.length; i++ )
                exp[i] = table[i];
        }
        double[] startTable = {1};
        switch(mode){
            case DISPLAY_MODE_OBSERVED:
                startTable = table;
                break;
            case DISPLAY_MODE_EXPECTED:
                startTable = exp;
                break;
            case DISPLAY_MODE_SAMEBINSIZE:
            case DISPLAY_MODE_MULTIPLEBARCHARTS:
            case DISPLAY_MODE_FLUCTUATION:
                startTable = new double[table.length];
                for( int i=0; i< startTable.length; i++ )
                    startTable[i] = 1;
                break;
        }
        
        // start the recursion ////////////
        createMosaic(0, 0, startTable, x1, y1, Math.max(x2-subX,1), Math.max(y2-subY,1), info);
        
        /*
         * Lables not implemented
         *
        // Create labels for the first 2 dimensions
        int pF = 1;
        if( printing )
            pF = printFactor;
        if( Dirs[0] == 'x' && Dirs[1] == 'y' || Dirs[0] == 'y' && Dirs[1] == 'x') {
            for(int j=0; j<Math.min(2, maxLevel); j++)
                for( int i=0; i<levels[j]; i++) {
                if( Dirs[j] == 'x' )
                    label = new MyText(lnames[j][i], (int)((x1+(double)(x2-x1)/(double)levels[j]*(i+0.5))), border-5*pF, 0, (x2-x1)/levels[j]-2);
                else
                    label = new MyText(lnames[j][i], -(int)((y1+(double)(y2-y1)/(double)levels[j]*(i+0.5))), border-6*pF,  -Math.PI/2.0, (y2-y1)/levels[j]-2);
                Labels.addElement(label);
                }
        }
         *
         */
        
        if( mode==DISPLAY_MODE_MULTIPLEBARCHARTS || mode==DISPLAY_MODE_FLUCTUATION ) {
            double maxCount=0;
            for (int i=0; i<rects.size(); i++)
                maxCount = Math.max(maxCount, ((MyRect)(rects.elementAt(i))).obs);
            for (int i=0; i<rects.size(); i++) {
                MyRect r = (MyRect)(rects.elementAt(i));
                int newH = 0;
                if( mode==DISPLAY_MODE_MULTIPLEBARCHARTS ) {
                    r.dir = 'y';
                    r.h = (int)((double)r.height * (1.0+(double)censor/5.0) * r.obs/maxCount);
                } else {
                    r.w = (int)((double)r.width * ((1.0+(double)censor/5.0) * Math.sqrt(r.obs/maxCount)));
                    r.h = (int)((double)r.height * ((1.0+(double)censor/5.0) * Math.sqrt(r.obs/maxCount)));
                }
                if( (r.h >= r.height && r.w >= r.width) && censor > 0)
                    r.censored = true;
                r.y += r.height-r.h;
            }
        }
    }
    
    public void createMosaic(int start, int levelid, double[] Mtable, int x1, int y1, int x2, int y2, String infop) {
        
        int levels = ft.getLevels()[levelid];
        int k = v.length;
        String name = v[levelid].getName();
        String[] lnames = (String[])v[levelid].getCategories();
        
        double[] exp = ft.getExp();
        double[] table = ft.getTable();
        
        double[] counts = new double[levels+1];
        double[] oCounts = new double[levels+1];
        double[]   exps = new double[levels];
        double[]    obs = new double[levels];
        double total = 0;
        
        String info;
        PPrimMosaic tile;
        int index;
        Vector[] tileIds = new Vector[levels];
        for (int j=0; j < levels; j++) {
            tileIds[j] = new Vector(8,8);
        }
        
        // Calculate the absolute counts for each level first
        if ( levelid < k-1 ) {	        // if we did not reach the lowest level
            
            for (int j=0; j < levels; j++) {
                for (int i=0; i < plevels[levelid]; i++) {
                    index = start+ j*plevels[levelid] + i;
                    total += Mtable[index];
                    counts[j+1] += Mtable[index];
                    oCounts[j+1] += table[index];
                    exps[j] += exp[index];
                    obs[j] += table[index];
                    if( levelid == maxLevel-1 )
                        tileIds[j].addElement(new Integer(index));
                }
                counts[j+1] += counts[j];
                oCounts[j+1] += oCounts[j];
            }
        } else {
            for (int j=0; j < levels; j++) {
                total += Mtable[start + j];
                counts[j+1] += Mtable[start + j];
                counts[j+1] += counts[j];
                oCounts[j+1] += table[start + j];
                oCounts[j+1] += oCounts[j];
                exps[j] += exp[start + j];
                obs[j] += table[start + j];
                tileIds[j].addElement(new Integer(start + j));
            }
        }
        
//      int thisGap = 0;
//      if( !displayMode.equals("Fluctuation") )
        int thisGap = aGap[levelid];
        
        int emptyBin = 0;
        int emptyWidth = 0;
        if( levelid > 0 ) {
            if( levelid == maxLevel-1 )
                emptyBin = 0;
            else if( levelid == maxLevel-2 )
                emptyBin = 1;
            else
                emptyBin = aGap[levelid] - Gaps[levelid];
            emptyWidth = aGap[levelid-1] - Gaps[levelid-1];
        }
        
        int sizeX = x2-x1;
        int sizeY = y2-y1;
        
        if( total > 0 )
            for (int j=0; j < levels; j++) {                                     // for each level in this variable
            
            info = infop.toString() + name + ": " + lnames[j] + '\n';// Add the popup information
            
            boolean empty = false;
            boolean stop  = false;
            int addGapX = 0;
            int addGapY = 0;
            
            if( counts[j+1] - counts[j] == 0 )
                empty = true;
            if( (mode==DISPLAY_MODE_SAMEBINSIZE) && oCounts[j+1]-oCounts[j] == 0 || levelid == maxLevel-1 ) {
                stop = true;
                for( int i=levelid+1; i<maxLevel; i++ )
                    if( Dirs[i] == 'x' )
                        addGapX += aGap[i];
                    else
                        addGapY += aGap[i];
            }
            
            if( stop || empty ) {	            // Now the rectangles are generated
                tile = new PPrimMosaic();
                tile.empty = empty;
                if( Dirs[levelid] == 'x' ){
                    if( empty ){                                        // empty bin
                        tile.r = new Rectangle(x1 + (int)(counts[j] / total * sizeX) + j * thisGap,
                                y1,
                                emptyBin,
                                sizeY+emptyWidth);
                        //dir='y', mode=displayMode
                        //missing: 0,exps[j], 4 / residSum, tablep.p,info, tileIds[j]
                    } else{
                        tile.r = new Rectangle(x1 + (int)(counts[j] / total * sizeX) + j * thisGap,
                                y1,
                                Math.max(1, (int)((counts[j+1] - counts[j]) / total * sizeX)) + addGapX,
                                y2-y1 + addGapY);
                        //dir='y', mode=displayMode
                        //missing: obs[j],exps[j], 4 / residSum, tablep.p,info, tileIds[j]
                    }
                } else {
                    if( empty ){
                        tile.r = new Rectangle(x1,
                                y1 + (int)(counts[j] / total * sizeY) + j * thisGap,
                                sizeX+emptyWidth,
                                emptyBin);
                        //dir='x', mode=displayMode
                        //missing: 0,exps[j], 4 / residSum, tablep.p,info, tileIds[j]
                    } else {
                        tile.r = new Rectangle(x1,
                                y1 + (int)(counts[j] / total * sizeY) + j * thisGap,
                                x2-x1 + addGapX,
                                Math.max(1, (int)((counts[j+1] - counts[j]) / total * sizeY)) + addGapY);
                        //dir='x', mode=displayMode
                        //missing: obs[j],exps[j], 4 / residSum, tablep.p,info, tileIds[j]
                    }
                }
                rects.addElement(tile);
            } else {						// Still to go in the recursion
                if( Dirs[levelid] == 'x' ) {
                    createMosaic(start + j*plevels[levelid],
                            levelid + 1,
                            Mtable,
                            x1 + j * thisGap + (int)(counts[j] / total * sizeX),
                            y1,
                            x1 + j * thisGap + Math.max((int)(counts[j] / total * sizeX +1),
                            (int)(counts[j+1] / total * sizeX)),
                            y2,
                            info);
                } else {
                    createMosaic(start + j*plevels[levelid],
                            levelid + 1,
                            Mtable,
                            x1,
                            y1 + j * thisGap + (int)(counts[j] / total * sizeY),
                            x2,
                            y1 + j * thisGap + Math.max((int)(counts[j] / total * sizeY +1),
                            (int)(counts[j+1] / total * sizeY)),
                            info);
                }
            }
            }
    }
    
    private double pnorm( double q ) {
        
        double up=0.9999999;
        double lp=0.0000001;
        while( Math.abs( up - lp ) > 0.0001 )
            if( qnorm( (up+lp)/2 ) <= q )
                lp = (up+lp)/2;
            else
                up = (up+lp)/2;
        return up;
    }
    
    private double qnorm( double p ) {
        
        double a0 = 2.515517;
        double a1 = 0.802853;
        double a2 = 0.010328;
        
        double b1 = 1.432788;
        double b2 = 0.189269;
        double b3 = 0.001308;
        
        double  t = Math.pow(-2*Math.log(1-p), 0.5);
        
        return t - (a0 + a1*t + a2*t*t) / (1 + b1*t + b2*t*t + b3*t*t*t);
    }
    
    public double round( double x, int n ) {
        return (double)Math.round(x*Math.pow(10,n))/Math.pow(10,n);
    }
    
    private final class MyRect extends Rectangle implements ActionListener {
        public int x, y, w, h;
        private int plusX = 0;
        private int plusY = 1;
        private String info;
        private String mode;
        private Graphics g;
        private JMenuItem infoText;
        private JPopupMenu popup;
        private boolean full;
        public boolean censored = false;
        public char dir;
        public double obs = 1;
        private double hilite;
        private double exp;
        private double scale;
        private float p;
        private Color drawColor = Color.black;
        private JPanel panel;
        public Vector tileIds;
        
        public MyRect(boolean full, char dir, String mode,
                int x, int y, int w, int h,
                double obs, double exp, double scale, double p, String info, Vector tileIds) {
            super(x, y, w, h);
            this.full = full; this.dir = dir; this.exp = exp; this.scale = scale; this.mode = mode;
            this.x = x; this.y = y; this.w = w; this.h = h; this.obs = obs; this.p = (float)p; this.info = info;
            this.tileIds = tileIds; this.panel = panel;
        }
        
        public void moveTo(int x, int y) {
            if( x != -1 )
                this.x = x;
            if( y != -1 )
                this.y = y;
        }
        
        public Rectangle getRect() {
            return (new Rectangle(x, y, w, h));
        }
        
        public void actionPerformed(ActionEvent e) {
            // Dummy, since we just use it for information display
        }
        
        public void setHilite(double hilite) {
            this.hilite = hilite;
        }
        
        public void setColor(Color color) {
            this.drawColor = color;
        }
        
        public double getHilite() {
            return hilite;
        }
        
        public double getAbsHilite() {
            return hilite * obs;
        }
        
        public void draw(Graphics g) {
            
            //System.out.println(residual);
            if( obs > 0 ) {
                if( dir != 'f' ) {
                    if( info.indexOf("�") == -1 && !(info.substring(0, 2)).equals("NA") )
                        g.setColor(Color.lightGray);
                    else
                        g.setColor(Color.white);
                    g.fillRect(x, y+Math.max(0, h-height), Math.min(w, width), Math.min(h, height) + 1);
                } else {
                    g.setColor(drawColor);
                    g.fillRect(x, y, w, h);
                }
            }
            if( mode.equals("Expected") ) {
                int high = (int)(192+63*(0.15+pnorm((1-p-0.9)*10)));
                int low =  (int)(192*(0.85-pnorm((1-p-0.9)*10)));
                if( obs-exp > 0.00001 )
                    g.setColor(new Color(low, low, high));
                else if( obs-exp < -0.00001 )
                    g.setColor(new Color(high, low, low));
                else
                    g.setColor(Color.lightGray);
                if( dir == 'x' )
                    g.fillRect(x, y, (int)((double)w*Math.abs((obs-exp)/Math.sqrt(exp)*scale)), h);
                else if ( dir == 'y' )
                    g.fillRect(x, y+h-(int)((double)h*Math.abs((obs-exp)/Math.sqrt(exp)*scale)),
                            w, (int)((double)h*Math.abs((obs-exp)/Math.sqrt(exp)*scale)));
            }
            if( obs == 0 || censored )
                g.setColor(Color.red);
            else
                g.setColor(Color.black);
            
            if( hilite > 0 ) {
                Color c = g.getColor();
                g.setColor(hiliteColor);
//System.out.println("wh: "+(int)((double)w*hilite));
                if( Math.min(w, width) > 2 && Math.min(h, height) > 2 ) {  // Mit Rahmen
                    plusX = 1;
                    plusY = 0;
                }
                if( dir == 'x' )
                    g.fillRect(x+plusX, y+Math.max(0, h-height), (((int)((double)w*hilite) == 0) ? 1: (int)Math.min(width, ((double)w*hilite))), Math.min(h, height));
                else if ( dir == 'y' )
                    g.fillRect(x, y+Math.max(0, h-height)+Math.min(h, height)-(((int)((double)(h+plusY)*hilite) == 0) ? (1-plusY): (int)Math.min(height, ((double)h*hilite))),
                            Math.min(w, width), (((int)((double)(h+plusY)*hilite) == 0) ? 1: (int)Math.min(height+plusY, ((double)(h+plusY)*hilite))));
                else {
                    g.setColor(new Color(255, 0, 0, (int)(255*hilite)));
                    g.fillRect(x, y, w, h);
                }
                g.setColor( c );
            }
            if( dir !='f' && Math.min(w, width) > 2 && Math.min(h, height) > 2 || obs == 0 || censored )
                g.drawRect(x, y+Math.max(0, h-height), Math.min(w, width), Math.min(h, height));
        }
        
        public void pop(DragBox panel, int x, int y) {
            popup = new JPopupMenu();
            
            String pinfo = getLabel();
            
            StringTokenizer info = new StringTokenizer(pinfo, "\n");
            
            while( info.hasMoreTokens()) {
                infoText = new JMenuItem( info.nextToken() );
                popup.add(infoText);
                infoText.addActionListener(this);
                //      infoText.setEnabled(false);
            }
            
            popup.show(panel, x, y);
        }
        
        public String getLabel() {
            String pinfo = info.toString();
            if( obs > 0 )
                pinfo += "\n" + "Count: "+obs;
            else
                pinfo += "\n" + "Empty Bin ";
            if( hilite > 0 )
                pinfo += "\n" + "Hilited: "+round(hilite*obs,0)+" ("+round(100*hilite,2)+"%)";
            if( mode.equals("Expected") ) {
                pinfo += "\n" + "Expected: "+round(exp,2);
                pinfo += "\n" + "Residual: "+round(obs-exp,3);
                pinfo += "\n" + "Scaled Res.:"+round(Math.abs((obs-exp)/Math.sqrt(exp)*scale/4*100),1)+"%";
            }
            
            return pinfo;
        }
    }
    
    private Color hiliteColor = new Color(180, 96, 135);
    public abstract class DragBox extends JPanel
            implements MouseListener, MouseMotionListener, AdjustmentListener, ActionListener, Printable {
        
        protected Color	background = Color.black;
        protected Color	dragboxcolor = Color.red;
        protected Graphics dragboxgraphics = null;
        
        public JFrame frame;                               // The frame we are within.
        public JScrollBar sb;                             // We might need a scroll bar
        
        public boolean selectFlag;                        // True if selection occured in this DragBox
        
        public boolean dataFlag;                          // True if change occured in this DragBox
        
        public boolean selectAll = false;                          // True if the user pressed META-A
        
        public boolean deleteAll = false;                          // True if the user pressed META-BACKSPACE
        
        public boolean switchSel = false;                          // True if the user pressed META-M
        
        public boolean switchAlpha = false;                          // True if the user pressed META-L
        
        public boolean changePop = false;												// True if the Sel Seq Popup was triggered
        
        public boolean scaleChanged = false;              // To indicate paint the new scale (without using events)
        
        public boolean printing;                               // flag to avoid double buffering while printing ...
        
        public PrinterJob pj;
        
        public int printFactor = 1;														// Increase in resolution for printing
        
        public Dimension printerPage;                               // must be accessible in different paints ...
        
        //
        // The PC implementation may need two minor changes:
        //
        //    1) BUTTON1_DOWN = InputEvent.BUTTON1_MASK
        //    2) Exchange definitions for buttons 2 and 3??
        //
        // Modifiers, as seen during button press - button 1 is strange!
        //
        
        protected final static int	BUTTON1_DOWN = 16; //InputEvent.BUTTON1_MASK;
        protected final static int	BUTTON2_DOWN = 8;  //InputEvent.BUTTON2_MASK;
        protected final static int	BUTTON3_DOWN = 4;  //InputEvent.BUTTON3_MASK;
        
        //
        // Modifiers, as seen during button release - notice button 1.
        //
        
        protected final static int	BUTTON1_UP = BUTTON1_DOWN; //InputEvent.BUTTON1_MASK;
        protected final static int	BUTTON2_UP = BUTTON2_DOWN; //InputEvent.BUTTON2_MASK;
        protected final static int	BUTTON3_UP = BUTTON3_DOWN; //InputEvent.BUTTON3_MASK;
        
        //
        // Modifiers, as seen during button release - notice button 1.
        //
        
        protected final static int	SHIFT_DOWN = 1; //InputEvent.SHIFT_MASK;
        protected final static int	CTRL_DOWN  = 2; //InputEvent.CTRL_MASK;
        protected final static int	META_DOWN  = 4; //InputEvent.META_MASK;
        protected final static int	ALT_DOWN   = 8; //InputEvent.ALT_MASK;
        
        //
        // Mouse status.
        //
        
        protected final int	AVAILABLE = 0;
        protected final int	DRAGGING  = 1;
        protected final int	MOVING    = 2;
        protected final int	RESIZEN   = 3;
        protected final int	RESIZENE  = 4;
        protected final int	RESIZEE   = 5;
        protected final int	RESIZESE  = 6;
        protected final int	RESIZES   = 7;
        protected final int	RESIZESW  = 8;
        protected final int	RESIZEW   = 9;
        protected final int	RESIZENW  = 10;
        protected final int	CHANGE    = 11;
        protected final int	ZOOMING   = 12;
        protected int	mouse = AVAILABLE;
        
        //
        // System Type.
        //
        
        protected final int	MAC  = 32;
        protected final int	WIN  = 64;
        protected final int	LNX  = 128;
        protected final int	NN   = 0;
        
        protected int SYSTEM;
        
        protected int movingID = 0;
        
        //
        // Permanent arrays for drawing polygon.
        //
        
        protected int	xcorner[] = {0, 0, 0, 0};
        protected int	ycorner[] = {0, 0, 0, 0};
        
        protected int diffX;
        protected int diffY;
        
        int border = 0;
        int xShift = 0;
        int yShift = 0;
        
        /*
         * SELECTIONS not implemented
         *
         
         Vector Selections = new Vector(10,0);
         
        Selection activeS;
         **/
        
        protected int minX = 0;
        protected int minY = 0;
        protected int maxX = 10000;
        protected int maxY = 10000;
        protected int maxWidth = 10000;
        protected int maxHeight = 10000;
        
        ///////////////////////// World - Screen Coordinates ///////////////////////////////////
        
        protected double hllx, llx;
        protected double hlly, lly;
        
        protected double hurx, urx;
        protected double hury, ury;
        
        protected double aspectRatio = -1;
        
        protected Vector zooms = new Vector(10,0);
        
        ///////////////////////// Methods for Coordinates /////////////////////////////////////
        
        public void setCoordinates(double llx, double lly, double urx, double ury, double aspectRatio) {
            
            this.hllx = llx;
            this.hlly = lly;
            this.hurx = urx;
            this.hury = ury;
            this.aspectRatio = aspectRatio;
            reScale(llx, lly, urx, ury);
        }
        
        public void setAspect(double aspectRatio) {
            this.aspectRatio = aspectRatio;
            updateScale();
        }
        
        public double getAspect() {
            return aspectRatio;
        }
        
//  public void print() {
//  }
        
        public void reScale(double llx, double lly, double urx, double ury) {
            
            this.llx = llx;
            this.lly = lly;
            this.urx = urx;
            this.ury = ury;
            
            zooms.addElement( new double[]{llx, lly, urx, ury});
            updateScale();
        }
        
        public void updateScale() {
            double mllx = ((double[])(zooms.elementAt( zooms.size()-1 )))[0];
            double mlly = ((double[])(zooms.elementAt( zooms.size()-1 )))[1];
            double murx = ((double[])(zooms.elementAt( zooms.size()-1 )))[2];
            double mury = ((double[])(zooms.elementAt( zooms.size()-1 )))[3];
            
            Dimension size = this.getSize();
            size.width  -= 2 * border;
            size.height -= 2 * border;
            if( size.width > 0 && size.height > 0 )
                if( aspectRatio > 0 )
                    if( (double)size.width/(double)size.height < (murx-mllx)/(mury-mlly) ) {
                this.ury = ((double)size.height / (double)size.width) * ((murx-mllx) / (mury-mlly)) * (mury-mlly) + mlly;
                this.urx = murx;
                    } else {
                this.ury = mury;
                this.urx = ((double)size.width / (double)size.height) * ((mury-mlly) / (murx-mllx)) * (murx-mllx) + mllx;
                    }
            //System.out.println("Height: "+size.height+" Width: "+size.width+" llx: "+llx+" lly: "+lly+" urx: "+urx+" ury:"+ury+" #:"+zooms.size());
        }
        
        public void home() {
            llx = this.hllx;
            lly = this.hlly;
            urx = this.hurx;
            ury = this.hury;
        }
        
        public double userToWorldX(double x) {
            
            Dimension size = this.getSize();
            size.width  -= 2 * border;
            //    return (x - llx) / (urx - llx) * size.width;
            return border + xShift + (x - llx) / (urx - llx) * size.width;
        }
        
        public double userToWorldY(double y) {
            
            Dimension size = this.getSize();
            size.height -= 2 * border;
            //    return size.height - (y - lly) / (ury - lly) * size.height;
            return border + yShift + size.height - (y - lly) / (ury - lly) * size.height;
        }
        
        public double worldToUserX(int x) {
            
            Dimension size = this.getSize();
            size.width  -= 2 * border;
            //    return llx + (urx - llx) * x / size.width;
            return llx + (urx - llx) * (x-border-xShift) / size.width;
        }
        
        public double worldToUserY(int y) {
            
            Dimension size = this.getSize();
            size.height -= 2 * border;
            //    return lly + (ury - lly) * (size.height - y) / size.height;
            return lly + (ury - lly) * (size.height - (y-border-yShift)) / size.height;
        }
        
        public double getLlx() {
            return llx;
        }
        
        public double getLly() {
            return lly;
        }
        
        public double getUrx() {
            return urx;
        }
        
        public double getUry() {
            return ury;
        }
        ///////////////////////////////////////////////////////////////////////////
        
        public DragBox(JFrame frame) {
            
            this.frame = frame;
            
            ToolTipManager.sharedInstance().registerComponent(this);
            ToolTipManager.sharedInstance().setLightWeightPopupEnabled(false);
            ToolTipManager.sharedInstance().setInitialDelay(0);
            ToolTipManager.sharedInstance().setDismissDelay(Integer.MAX_VALUE);
            ToolTipManager.sharedInstance().setReshowDelay(0);
            this.setToolTipText("<HTML>hold CTRL to query objects<br>hold SHIRT+CTRL for extended query</HTML>");
            
            addMouseListener(this);
            addMouseMotionListener(this);
            
            evtq = Toolkit.getDefaultToolkit().getSystemEventQueue();
            enableEvents(0);
            
            sb = new JScrollBar(Scrollbar.VERTICAL, 0, 300, 0, 300);
            sb.addAdjustmentListener(this);
            sb.setVisible(false);
            
            this.enableEvents(AWTEvent.KEY_EVENT_MASK);
            this.requestFocus();
            
            frame.addKeyListener(new KeyAdapter() { public void keyPressed(KeyEvent e) {processKeyEvent(e);} });
            
            if( ((System.getProperty("os.name")).toLowerCase()).indexOf("mac") > -1 )
                SYSTEM = MAC;
            else if( ((System.getProperty("os.name")).toLowerCase()).indexOf("win") > -1 )
                SYSTEM = WIN;
            else if( ((System.getProperty("os.name")).toLowerCase()).indexOf("linux") > -1 )
                SYSTEM = LNX;
            else
                SYSTEM = NN;
        }
        
        public void setScrollX() {
            frame.getContentPane().add(sb,"East");
        }
        
        public void setDragBoxConstraints(int minX, int minY, int maxX, int maxY, int maxWidth, int maxHeight) {
            this.minX      = minX;
            this.minY      = minY;
            this.maxX      = maxX;
            this.maxY      = maxY;
            this.maxWidth  = maxWidth;
            this.maxHeight = maxHeight;
        }
        
        public Dimension getViewportSize() {
            
            Dimension size;
            
            size = frame.getSize();
            size.height -= (frame.getInsets().top+frame.getInsets().bottom);
            size.width  -= (frame.getInsets().right+frame.getInsets().left);
            
            return size;
        }
        
        public void setSize(int widthN, int heightN) {
            
            Dimension size;
            size = frame.getSize();
            size.height -= (frame.getInsets().top+frame.getInsets().bottom);
            size.width  -= (frame.getInsets().right+frame.getInsets().left);
            
            if( heightN >  size.height) {
                super.setSize( size.width, size.height );
                sb.setValues(sb.getValue(),size.height,0,heightN);
                sb.setUnitIncrement(22);
                sb.setBlockIncrement((frame.getSize()).height);
                if( !sb.isVisible() ) {
                    sb.setVisible(true);
                    frame.setVisible(true);
                }
            } else {
                super.setSize( size.width, size.height );
                sb.setValues(0,size.height,0,size.height);
                sb.setVisible(false);
            }
        }
        
        public void update(Graphics g) {
            paint(g);
        }
        
        public Color getHiliteColor() {
            return hiliteColor;
        }
        
        /*
         * SELECTIONS not implemented
         *
        public void addSelectionListener(SelectionListener l) {
            listener = l;
        }
         
         
         
        public void processEvent(AWTEvent evt) {
            if( evt instanceof SelectionEvent ) {
                if( listener != null )
                    listener.updateSelection();
            } else super.processEvent(evt);
        }
         *
         **/
        
        ///////////////////////////////////////////////////////////////////////////
        
        public void	mouseDragged(MouseEvent e) {
            
            if (mouse != CHANGE) {
                dragBox(e.getX(), e.getY(), e);
            }
        }
        
        ///////////////////////////////////////////////////////////////////////////
        
        public void mousePressed(MouseEvent e) {
            
            System.out.println("SYSTEM = "+SYSTEM);
            System.out.println("mouse = "+mouse);
            System.out.println("Mouse pres: ... "+e.getModifiers());
            
            if (mouse == AVAILABLE) {
                if (e.getModifiers() == BUTTON1_DOWN ||
                        e.getModifiers() == BUTTON1_DOWN + SHIFT_DOWN ||
                        e.getModifiers() == BUTTON1_DOWN + SHIFT_DOWN + ALT_DOWN )
                    dragBegin(e.getX(), e.getY(), e);
                if (e.getModifiers() ==  BUTTON1_DOWN + META_DOWN && SYSTEM == MAC ||
                        e.getModifiers() ==  BUTTON2_DOWN             && SYSTEM != MAC ) {
                    mouse = ZOOMING;
                    System.out.println("Start ZOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOMING");
                    dragBegin(e.getX(), e.getY(), e);
                }
                if ((e.isPopupTrigger() ) && !e.isShiftDown() && e.getModifiers() !=24 ) {  // || (e.getModifiers() ==  BUTTON3_DOWN && SYSTEM == WIN)
                    mouse = CHANGE;
                    System.out.println("Start CHANGGEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEE");
                    dragBegin(e.getX(), e.getY(), e);
                }
            }   // End if //
        }
        
        ///////////////////////////////////////////////////////////////////////////
        
        public void
                mouseReleased(MouseEvent e) {
            
            int ev;
            ev=e.getModifiers();
            
            if (e.isPopupTrigger() && SYSTEM == WIN && !e.isShiftDown() ) {
                mouse = CHANGE;
                System.out.println("Start CHANGGEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEE");
                dragBegin(e.getX(), e.getY(), e);
            }
            
            System.out.println("Mouse rel: ... "+ev+" "+BUTTON1_UP +" "+ SHIFT_DOWN +" "+ ALT_DOWN);
            if (mouse != AVAILABLE) {
                switch (ev) {
                    case 0:
                    case 1:
                    case 2:
                        //	            if((System.getProperty("os.name")).equals("Mac OS") && (mouse != CHANGE) ) {
                        dragEnd(e);
                        //		    }
                        mouse = AVAILABLE;
                        break;
                    case BUTTON1_UP:
                    case BUTTON1_UP + CTRL_DOWN:
                    case BUTTON1_UP + SHIFT_DOWN:
                    case              SHIFT_DOWN + ALT_DOWN:
                    case BUTTON1_UP + SHIFT_DOWN + ALT_DOWN:
                    case BUTTON1_UP + META_DOWN:
                    case BUTTON2_UP:
                    case BUTTON3_UP:
                        if( mouse != CHANGE ) {
                            System.out.println(" dragEnd! ");
                            dragEnd(e);
                        }
                        mouse = AVAILABLE;
                        break;
                }	// End switch //
            }   // End if //
        }
        
        ///////////////////////////////////////////////////////////////////////////
        
        public void mouseClicked(MouseEvent e) {}
        public void mouseEntered(MouseEvent e) {}
        public void mouseExited(MouseEvent e) {}
        public void mouseMoved(MouseEvent e) {}
        
        ///////////////////////////////////////////////////////////////////////////
        
        public void dragboxCallback(int x0, int y0, int x1, int y1, MouseEvent e) {
            
            SelectionEvent se;
            /*//// SELECTIONS not implemented
            Selection S;
             **/
            Rectangle sr;
            
            int lx = Math.min(x0,x1);
            int ly = Math.min(y0,y1);
            int lw = Math.abs(x1-x0);
            int lh = Math.abs(y1-y0);
            
            sr = new Rectangle(lx, ly, lw, lh);
            
            int modifiers = e.getModifiers();
//System.out.println("modifiers callback = "+e.getModifiers());
//System.out.println("mouse "+mouse);
            
            if( modifiers == BUTTON1_UP ||
                    modifiers == BUTTON1_UP + META_DOWN - 16 && SYSTEM == MAC ||
                    modifiers == BUTTON2_UP && SYSTEM != MAC ) {
                if( mouse != CHANGE && mouse != ZOOMING ) {
                    /*
                     * SELECTIONS not implemented
                     *
                    if( mouse == DRAGGING ) {
                        S = new Selection(sr, null, 0, Selection.MODE_STANDARD, this);
                        activeS = S;
                        Selections.addElement(S);
                    } else {
                        S = ((Selection)Selections.elementAt(movingID));
                        S.r = sr;
                    }
                     **/
                    selectFlag = true;
                    se = new SelectionEvent(this);
                    evtq.postEvent(se);
                } else if ( mouse == ZOOMING ) {
                    if( Math.abs(x0-x1) < 5 && Math.abs(y0-y1) < 5 ) {
                        if( zooms.size() > 1 ) {
                            zooms.removeElementAt( zooms.size()-1 );
                            reScale( ((double[])(zooms.elementAt( zooms.size()-1 )))[0],
                                    ((double[])(zooms.elementAt( zooms.size()-1 )))[1],
                                    ((double[])(zooms.elementAt( zooms.size()-1 )))[2],
                                    ((double[])(zooms.elementAt( zooms.size()-1 )))[3] );
                            zooms.removeElementAt( zooms.size()-1 );
                        }
                    } else
                        reScale( worldToUserX( Math.min(x0, x1) ),
                                worldToUserY( Math.max(y0, y1) ),
                                worldToUserX( Math.max(x0, x1) ),
                                worldToUserY( Math.min(y0, y1) ));
                    scaleChanged = true;
                    update(this.getGraphics());
                }
            }
            if( modifiers == BUTTON1_UP + SHIFT_DOWN) {
                /*
                 * SELECTIONS not implemented
                 *
                if( mouse == DRAGGING ) {
                    S = new Selection(sr, null, 0, Selection.MODE_XOR, this);
                    activeS = S;
                    Selections.addElement(S);
                } else {
                    S = ((Selection)Selections.elementAt(movingID));
                    S.r = sr;
                }
                 *
                 */
                selectFlag = true;
                se = new SelectionEvent(this);
                evtq.postEvent(se);
            }
            if( modifiers == BUTTON1_UP + SHIFT_DOWN + ALT_DOWN || modifiers == SHIFT_DOWN + ALT_DOWN && SYSTEM == MAC ) {
//      if( modifiers == SHIFT_DOWN + ALT_DOWN ) && SYSTEM == MAC ) {
                /*
                 * SELECTIONS not implemented
                 *
                if( mouse == DRAGGING ) {
                    S = new Selection(sr, null, 0, Selection.MODE_OR, this);
                    activeS = S;
                    Selections.addElement(S);
                } else {
                    S = ((Selection)Selections.elementAt(movingID));
                    S.r = sr;
                }
                 */
                selectFlag = true;
                se = new SelectionEvent(this);
                evtq.postEvent(se);
            }
        }
        
        ///////////////////////////////////////////////////////////////////////////
        
        /*
         * SELECTIONS not implemented
         *
        public abstract void maintainSelection(Selection s);
         */
        
        public abstract void updateSelection();
        
        ///////////////////////////////////////////////////////////////////////////
        
        public abstract void dataChanged(int id);
        
        public abstract void paint(Graphics2D g);
        
        public void paint(Graphics g) {
            paint((Graphics2D)g);
        }
        
        public int print(Graphics g, PageFormat pageFormat, int pageIndex) {
            
            if (pageIndex > 0) {
                return(NO_SUCH_PAGE);
            } else {
//         printFactor = 5;
                System.out.println(" P R I N T I N G at: "+pageFormat.getImageableWidth()+" by "+pageFormat.getImageableHeight()+"printFactor: "+printFactor);
                Graphics2D g2d = (Graphics2D)g;
                g2d.translate(pageFormat.getImageableX() + pageFormat.getImageableWidth()*0.05,
                        pageFormat.getImageableY() + pageFormat.getImageableHeight()*0.05);
                g2d.scale(1.0/printFactor, 1.0/printFactor);
                // Turn off double buffering
                Dimension save = this.getViewportSize();
//        int setWidth  = (int)(pageFormat.getImageableWidth()*0.9)*printFactor;
//        int setHeight = (int)(pageFormat.getImageableHeight()*0.9)*printFactor;
                int setWidth  = save.width;
                int setHeight = save.height;
                if( aspectRatio == -1 )
                    if(   (double)setWidth / (double)setHeight
                        < (double)save.width / (double)save.height )
                        setHeight = (int)((double)(setWidth *  ((double)save.height / (double)save.width)));
                    else
                        setWidth  = (int)((double)(setHeight * ((double)save.width / (double)save.height)));
                super.setSize(setWidth, setHeight);
                g2d.setFont(new Font("SansSerif",0,11*printFactor));
                printing = true;
                this.paint(g2d);
                printing = false;
                this.setSize(save);
                // Turn double buffering back on
                return(PAGE_EXISTS);
            }
        }
        
        ///////////////////////////////////////////////////////////////////////////
        
        public abstract void adjustmentValueChanged(AdjustmentEvent e);
        public abstract void scrollTo(int id);
        
        ///////////////////////////////////////////////////////////////////////////
        
        /*
         * SELECTIONS not implemented
        public void drawBoldDragBox(Graphics g, Selection S) {
         
            Rectangle r = S.r;
         
            g.setColor( new Color(255, 255, 255, 90));
         
            g.fillRect(r.x, r.y, r.width, r.height);
         
            g.setColor( new Color(255, 255, 255, 150));
         
            if( mouse != ZOOMING && mouse !=DRAGGING && mouse!= AVAILABLE)
                g.drawString(S.step+"", r.x+r.width/2-3, r.y+r.height/2+5);
         
            if( S == activeS )
                g.setColor( new Color(0, 0, 0, 150));
            else
                g.setColor( new Color(255, 255, 255, 90));
         
//      g.drawRect(r.x, r.y, r.width, r.height);
//      g.drawRect(r.x-1, r.y-1, r.width+2, r.height+2);
         
            g.fillRect(r.x-4, r.y-4, 4, 4);
            g.fillRect(r.x+r.width/2-2, r.y-4, 4, 4);
            g.fillRect(r.x+r.width, r.y-4, 4, 4);
         
            g.fillRect(r.x-4, r.y+r.height/2-2, 4, 4);
            g.fillRect(r.x+r.width, r.y+r.height/2-2, 4, 4);
         
            g.fillRect(r.x-4, r.y+r.height, 4, 4);
            g.fillRect(r.x+r.width/2-2, r.y+r.height, 4, 4);
            g.fillRect(r.x+r.width, r.y+r.height, 4, 4);
        }
         */
        
        public void
                setColor(Color c) {
            
            dragboxcolor = c;
        }
        
        ///////////////////////////////////////////////////////////////////////////
        
        protected void
                drawDragBox() {
            
            if (xcorner[0] != xcorner[2] || ycorner[0] != ycorner[2]) {
                int[] drawYCorner = new int[4];
                for( int i=0; i<4; i++ )
                    drawYCorner[i] = ycorner[i] - sb.getValue();
                dragboxgraphics.drawPolygon(xcorner, drawYCorner, 4);
            }
        }
        
        ///////////////////////////////////////////////////////////////////////////
        
        protected void dragBegin(int x, int y, MouseEvent e) {
            
            boolean inBox = false;
            Rectangle sr=null;
            
            int modifiers = e.getModifiers();
            
            if (dragboxgraphics == null) {
                dragboxgraphics = getGraphics();
                dragboxgraphics.setColor(dragboxcolor);
                dragboxgraphics.setXORMode(getBackground());
            }   // End if //
            
            System.out.println("Mouse Action before check: "+mouse);
            if( mouse != ZOOMING ) {
                mouse = DRAGGING;
                /*
                 * SELECTIONS not implemented
                 *
                for( int i=0; i<Selections.size(); i++ ) {
                    int locMouse = determineAction(((Selection)Selections.elementAt(i)).r, new Point(x, y+sb.getValue()));
                    if(( locMouse <=10) && (locMouse >= 2 ))
                        activeS = (Selection)Selections.elementAt(i);
                    if( locMouse != DRAGGING && mouse != ZOOMING ) {
                        movingID = i;
                        mouse = locMouse;
                        sr = ((Selection)Selections.elementAt(movingID)).r;
                        xcorner[0] = sr.x;
                        xcorner[1] = xcorner[0];
                        xcorner[2] = sr.x + sr.width;
                        xcorner[3] = xcorner[2];
                        ycorner[0] = sr.y;
                        ycorner[1] = sr.y + sr.height;
                        ycorner[2] = ycorner[1];
                        ycorner[3] = ycorner[0];
                    }
                }
                 */
            } else
                mouse = ZOOMING;
            
            System.out.println("Mouse Action to check: "+mouse);
            
            switch (mouse) {
                
                case DRAGGING:
                case ZOOMING:
                    if( (e.isPopupTrigger()  || (e.getModifiers() ==  BUTTON3_DOWN && SYSTEM == WIN)) && !e.isShiftDown() ) {
                        System.out.println(" pop up in nowhere !!");
                        mouse = AVAILABLE;
                    } else {
                        xcorner[0] = x;
                        ycorner[0] = y + sb.getValue();
                        xcorner[2] = xcorner[0];
                        ycorner[2] = ycorner[0];
                        System.out.println("Mouse Action: DRAGGING");
                    }
                    break;
                case MOVING:
                    if( modifiers == BUTTON1_DOWN ) {
                        diffX = sr.x - x;
                        diffY = sr.y - (y+sb.getValue());
                    } else if( e.isPopupTrigger() || (e.getModifiers() ==  BUTTON3_DOWN && SYSTEM == WIN) && !e.isShiftDown() ) { // modifiers == BUTTON1_DOWN + SHIFT_DOWN ) {
                        mouse = CHANGE;
//System.out.println("Get/Change Info of Brush No: "+movingID);
                        JPopupMenu changeSelection = new JPopupMenu();
                        /*
                         * SELECTIONS not implemented
                         *
                        Selection S = ((Selection)Selections.elementAt(movingID));
                        int selStep = S.step;
                        JMenuItem Step = new JMenuItem("Step: "+selStep);
                        changeSelection.add(Step);
                         
                        //	  PopupMenu mode = new PopupMenu(S.getModeString(S.mode));
                        JMenu mode = new JMenu(S.getModeString(S.mode));
                        JMenuItem modeM = new JMenuItem(S.getModeString(S.mode));
                         
                        if( selStep > 1) {
                            if( S.mode != Selection.MODE_STANDARD ) {
                                JMenuItem Replace = new JMenuItem(S.getModeString(Selection.MODE_STANDARD));
                                mode.add(Replace);
                                Replace.addActionListener(this);
                                Replace.setActionCommand(S.getModeString(Selection.MODE_STANDARD));
                            }
                            if( S.mode != Selection.MODE_AND ) {
                                JMenuItem And = new JMenuItem("And");
                                mode.add(And);
                                And.addActionListener(this);
                                And.setActionCommand(S.getModeString(Selection.MODE_AND));
                            }
                            if( S.mode != Selection.MODE_OR ) {
                                JMenuItem Or = new JMenuItem("Or");
                                mode.add(Or);
                                Or.addActionListener(this);
                                Or.setActionCommand(S.getModeString(Selection.MODE_OR));
                            }
                            if( S.mode != Selection.MODE_XOR ) {
                                JMenuItem XOr = new JMenuItem("Xor");
                                mode.add(XOr);
                                XOr.addActionListener(this);
                                XOr.setActionCommand(S.getModeString(Selection.MODE_XOR));
                            }
                            if( S.mode != Selection.MODE_NOT ) {
                                JMenuItem Not = new JMenuItem("Not");
                                mode.add(Not);
                                Not.addActionListener(this);
                                Not.setActionCommand(S.getModeString(Selection.MODE_NOT));
                            }
                            changeSelection.add(mode);
                        } else
                            changeSelection.add(modeM);
                         */
                        
                        JMenuItem Delete = new JMenuItem("Delete");
                        changeSelection.add(Delete);
                        Delete.setAccelerator(KeyStroke.getKeyStroke(Event.BACK_SPACE, 0));
                        
                        Delete.setActionCommand("Delete");
                        Delete.addActionListener(this);
                        
                        JMenuItem DeleteAll = new JMenuItem("Delete All");
                        changeSelection.add(DeleteAll);
                        DeleteAll.setAccelerator(KeyStroke.getKeyStroke(Event.BACK_SPACE, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
                        
                        DeleteAll.setActionCommand("DeleteAll");
                        DeleteAll.addActionListener(this);
                        
                        frame.getContentPane().add(changeSelection);
                        changeSelection.show(e.getComponent(), e.getX(), e.getY());
                        
                        changePop = true;
                        
                        mouse = AVAILABLE;                            // We don't get a BOTTON_RELEASED Event on a popup
                    }
                    break;
            }
        }
        
        ///////////////////////////////////////////////////////////////////////////
        
        protected void dragBox(int x, int y, MouseEvent e) {
            
            if( (mouse != MOVING) && (Math.abs(xcorner[1] - x) > maxWidth) )
                x = xcorner[2];
            
            if( mouse == DRAGGING || mouse == ZOOMING )
                drawDragBox();
            
            if( y <= 10 ) {
                scrollTo( sb.getValue() - 22 );
            }
            if( y >= ((frame.getSize()).height - (frame.getInsets().top+frame.getInsets().bottom) - 10) ) {
                scrollTo( sb.getValue() + 22 );
            }
            
            if( mouse != DRAGGING && mouse != ZOOMING ) {
/*        EventQueue eq = Toolkit.getDefaultToolkit().getSystemEventQueue();
 
        AWTEvent evt = null;
        AWTEvent pE = eq.peekEvent();
 
        while( (pE != null) && ((pE.getID() == MouseEvent.MOUSE_DRAGGED) ||
                                (pE.getID() == SelectionEvent.SELECTION_EVENT) ))
          try {
            evt = eq.getNextEvent();
            pE = eq.peekEvent();
//System.out.println("====> trashed Event!"+evt.toString());
          }
        catch( InterruptedException ex)
        {}  */
                
                switch( mouse ) {
                    case MOVING:
                        /*
                         * SELECTIONS not implemented
                         *
                        Rectangle sr = ((Selection)Selections.elementAt(movingID)).r;
                         */
                        Rectangle sr = new Rectangle(10,10);
                        xcorner[0] = x                + diffX;
                        xcorner[1] = xcorner[0];
                        xcorner[2] = x + sr.width     + diffX;
                        xcorner[3] = xcorner[2];
                        ycorner[0] = y                + diffY + sb.getValue();
                        ycorner[1] = y + sr.height    + diffY + sb.getValue();
                        ycorner[2] = ycorner[1];
                        ycorner[3] = ycorner[0];
                        break;
                    case RESIZENW:
                        xcorner[0] = x;
                        ycorner[0] = y + sb.getValue();
                        xcorner[1] = x;
                        ycorner[3] = y + sb.getValue();
                        break;
                    case RESIZEN:
                        ycorner[0] = y + sb.getValue();
                        ycorner[3] = y + sb.getValue();
                        break;
                    case RESIZENE:
                        xcorner[3] = x;
                        ycorner[0] = y + sb.getValue();
                        xcorner[2] = x;
                        ycorner[3] = y + sb.getValue();
                        break;
                    case RESIZEE:
                        xcorner[2] = x;
                        xcorner[3] = x;
                        break;
                    case RESIZESE:
                        xcorner[2] = x;
                        ycorner[2] = y + sb.getValue();
                        xcorner[3] = x;
                        ycorner[1] = y + sb.getValue();
                        break;
                    case RESIZES:
                        ycorner[1] = y + sb.getValue();
                        ycorner[2] = y + sb.getValue();
                        break;
                    case RESIZESW:
                        xcorner[0] = x;
                        ycorner[2] = y + sb.getValue();
                        xcorner[1] = x;
                        ycorner[1] = y + sb.getValue();
                        break;
                    case RESIZEW:
                        xcorner[0] = x;
                        xcorner[1] = x;
                        break;
                }
                dragboxCallback(xcorner[0], ycorner[0], xcorner[2], ycorner[2], e);
            } else {
                xcorner[1] = xcorner[0];
                ycorner[1] = y + sb.getValue();
                xcorner[2] = x;
                ycorner[2] = ycorner[1];
                xcorner[3] = xcorner[2];
                ycorner[3] = ycorner[0];
                
                drawDragBox();
            }
        }
        
        ///////////////////////////////////////////////////////////////////////////
        
        protected void
                dragEnd(MouseEvent e) {
            // MTh   drawDragBox();
            dragboxgraphics.dispose();
            dragboxgraphics = null;
            
            dragboxCallback(xcorner[0], ycorner[0], xcorner[2], ycorner[2], e);
        }
        
        ///////////////////////////////////////////////////////////////////////////
        
        public void processKeyEvent(KeyEvent e) {
            
            if ((e.getID() == KeyEvent.KEY_PRESSED) &&
                    (e.getKeyCode() == KeyEvent.VK_P)   &&
                    (e.getModifiers() == Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()) ) {
                
                pj = PrinterJob.getPrinterJob();
                PageFormat pageFormat = pj.defaultPage();
                Dimension size = this.getSize();
                if( size.width > size.height )
                    pageFormat.setOrientation(pageFormat.LANDSCAPE);
                else
                    pageFormat.setOrientation(pageFormat.PORTRAIT);
                
                pageFormat = pj.pageDialog(pageFormat);
                if( pageFormat != null ) {
                    //        pageFormat = pj.validatePage(pageFormat);
                    pj.setPrintable(this, pageFormat);
                    
                    if (pj.printDialog()) {
                        try { pj.print(); } catch (PrinterException ex) { System.out.println(ex); }
                    }
                }
            }
            if ((e.getID() == KeyEvent.KEY_PRESSED) &&
                    (e.getKeyCode() == KeyEvent.VK_A)   &&
                    (e.getModifiers() == Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()) ) {
                
                selectAll = true;
                SelectionEvent se = new SelectionEvent(this);
                evtq.postEvent(se);
            }
            if ((e.getID() == KeyEvent.KEY_PRESSED) &&
                    (e.getKeyCode() == Event.BACK_SPACE))  {
                /*
                 * SELECTIONS not implemented
                 *
                if( Selections.size() > 0 ) {
                    //  Selection S = (Selection)Selections.lastElement();
                    int activeIndex = Selections.indexOf(activeS);
                 
                    Selections.removeElement(activeS);
                    activeS.status = Selection.KILLED;
                    if (activeIndex > 0) activeS = (Selection) Selections.elementAt(activeIndex-1);
                 
                    SelectionEvent se = new SelectionEvent(this);
                    evtq.postEvent(se);
                }
                 */
            }
            if ((e.getID() == KeyEvent.KEY_PRESSED) &&
                    (e.getKeyCode() == Event.BACK_SPACE) &&
                    (e.getModifiers() == Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()))  {
                System.out.println("Delete All Selections");
                deleteAll = true;
                SelectionEvent se = new SelectionEvent(this);
                evtq.postEvent(se);
            }
            if ((e.getID() == KeyEvent.KEY_PRESSED) &&
                    (e.getKeyCode() == KeyEvent.VK_M) &&
                    (e.getModifiers() == Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()))  {
                System.out.println("Switch Selection Mode");
                switchSel = true;
                SelectionEvent se = new SelectionEvent(this);
                evtq.postEvent(se);
            }
            if ((e.getID() == KeyEvent.KEY_PRESSED) &&
                    (e.getKeyCode() == KeyEvent.VK_L) &&
                    (e.getModifiers() == Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()))  {
                System.out.println("Switch Alpha Mode");
                switchAlpha = true;
                SelectionEvent se = new SelectionEvent(this);
                evtq.postEvent(se);
            }
            if (e.getModifiers() == Toolkit.getDefaultToolkit().getMenuShortcutKeyMask() && e.getKeyCode() == KeyEvent.VK_W ) {
                frame.dispose();
            }
            if (e.getModifiers() == Toolkit.getDefaultToolkit().getMenuShortcutKeyMask() && e.getKeyCode() == KeyEvent.VK_C ) {
                ImageSelection.copyComponent(this,false,true);
            }
        }
        
        public void keyPressed(KeyEvent e) {      System.out.println("Key typed");
        }
        
        public void keyReleased(KeyEvent e) {      System.out.println("Key typed");
        }
        
        public int determineAction(Rectangle r, Point p) {
            
            int tolerance = 6;
            
            if( (new Rectangle(r.x, r.y, r.width, r.height)).contains(p) )
                return MOVING;
            
            if( ((new HotRect(r.x-3, r.y-3, 4, 4)).larger(tolerance)).contains(p) )
                return RESIZENW;
            
            if( ((new HotRect(r.x+r.width/2-2, r.y-3, 4, 4)).larger(tolerance)).contains(p) )
                return RESIZEN;
            
            if( ((new HotRect(r.x+r.width, r.y-3, 4, 4)).larger(tolerance)).contains(p) )
                return RESIZENE;
            
            if( ((new HotRect(r.x-3, r.y+r.height/2-2, 4, 4)).larger(tolerance)).contains(p) )
                return RESIZEW;
            
            if( ((new HotRect(r.x+r.width, r.y+r.height/2-2, 4, 4)).larger(tolerance)).contains(p) )
                return RESIZEE;
            
            if( ((new HotRect(r.x-3, r.y+r.height, 4, 4)).larger(tolerance)).contains(p) )
                return RESIZESW;
            
            if( ((new HotRect(r.x+r.width/2-2, r.y+r.height, 4, 4)).larger(tolerance)).contains(p) )
                return RESIZES;
            
            if( ((new HotRect(r.x+r.width, r.y+r.height, 4, 4)).larger(tolerance)).contains(p) )
                return RESIZESE;
            
            return DRAGGING;
        }
        
        public void actionPerformed(ActionEvent e) {
            String command = e.getActionCommand();
            
            /*
             * SELECTIONS not implemented
             *
            Selection S = ((Selection)Selections.elementAt(movingID));
            if( command.equals("Delete") ) {	// die Abfrage nach der aktivierten Selektion kann man sich sparen - es ist sowieso nur moeglich auf aktivierte Elemente zuzugreifen
                Selections.removeElement(S);
                S.status = Selection.KILLED;
             
                if( Selections.size() > 1 )
                    activeS = (Selection) Selections.elementAt(Selections.size()-1);	// solange nur aktivierte Element geloescht werden koennen, reicht es
                // anschliessend das vorletzte Element zu selektieren
            }
            if( command.equals("DeleteAll") ) {
                deleteAll = true;
            }
            if( command.equals(S.getModeString(Selection.MODE_STANDARD)) )
                S.mode = Selection.MODE_STANDARD;
            if( command.equals(S.getModeString(Selection.MODE_AND)) )
                S.mode = Selection.MODE_AND;
            if( command.equals(S.getModeString(Selection.MODE_OR)) )
                S.mode = Selection.MODE_OR;
            if( command.equals(S.getModeString(Selection.MODE_XOR)) )
                S.mode = Selection.MODE_XOR;
            if( command.equals(S.getModeString(Selection.MODE_NOT)) )
                S.mode = Selection.MODE_NOT;
             
            SelectionEvent se = new SelectionEvent(this);
            evtq.postEvent(se);
             */
        }
        
        /*
         * SELECTIONS not implemented
         *
        private SelectionListener listener;
         */
        private EventQueue evtq;
        
        class HotRect extends Rectangle {
            
            public HotRect(int x, int y, int w, int h) {
                super.x = x;
                super.y = y;
                super.width = w;
                super.height = h;
            }
            
            public Rectangle larger(int t) {
                return new Rectangle(this.x-t, this.y-t, this.width+2*t, this.height+2*t);
            }
        }
    }
    
    class SelectionEvent extends AWTEvent {
        public SelectionEvent(DragBox s) {
            super( s, SELECTION_EVENT );
        }
        public static final int SELECTION_EVENT = AWTEvent.RESERVED_ID_MAX + 1;
    }
    
    
    
    
}
