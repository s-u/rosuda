package org.rosuda.ibase.plots;

import java.awt.AWTEvent;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Event;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.FontMetrics;
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
import java.util.Enumeration;
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
    
    Frame frame;
    
    int maxLevel;
    
    int w,h,hgap,vgap; //width, height and gaps between mosaics
    
    static final int DISPLAY_MODE_OBSERVED = 0;
    static final int DISPLAY_MODE_EXPECTED = 1;
    static final int DISPLAY_MODE_SAMEBINSIZE = 2;
    static final int DISPLAY_MODE_MULTIPLEBARCHARTS = 3;
    static final int DISPLAY_MODE_FLUCTUATION = 4;
    
    int mode = DISPLAY_MODE_OBSERVED;
    
    FrequencyTable ft;
    int[] combination; // indicates position of recursion
    
    public MosaicCanvas(Frame f, SVar[] vars, SMarker mark) {
        super(f, mark);
        
        this.frame=f;
        this.v = vars;
        maxLevel = v.length; // should not be set here as it can be changed. is this the same as vs: ??
        this.vs = v.length;
        for(int i=0; i<vs; i++){
            v[i].categorize();
        }
        
        String myMenu[]={"+","File","~File.Graph","+","View","Observed","observed","Expected","expected","Same bin size","samebinsize","Multiple barcharts","multiplebarcharts","Fluctuation","fluctuation","~Edit","~Window","0"};
        EzMenu.getEzMenu(f,this,myMenu);
        mLeft=40; mRight=5; mTop=20; mBottom=5;
        
        ft = new FrequencyTable(vars);
        
        Dirs = new char[vs];
        for (int i=0; i<vs; i++ ) {
            if( (i % 2) == 0 )
                Dirs[i] = 'x';
            else
                Dirs[i] = 'y';
        }
    }
    
    public void updateObjects() {
        
        create(mLeft,mTop, getWidth()-mRight, getHeight()-mBottom, "");
        if(pp==null || pp.length!=rects.size()) pp = new PlotPrimitive[rects.size()];
        rects.toArray(pp);
        
        String titletext = "Mosaic Plot (";
        
        for (int i=0; i<vs ; i++) {
            if( i < vs-1 && i != maxLevel-1 )
                titletext += v[i].getName() +", ";
            else
                titletext += v[i].getName();
            if( i+1 == maxLevel && maxLevel < vs )
                titletext += ")[";
        }
        if ( maxLevel < vs )
            titletext += "]";
        else
            titletext += ")";
        
        frame.setTitle(titletext);
        
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
        if (cmd=="expected") { if(mode!=DISPLAY_MODE_EXPECTED) {mode=DISPLAY_MODE_EXPECTED; setUpdateRoot(0); updateObjects(); repaint();}}
        if (cmd=="samebinsize") { if(mode!=DISPLAY_MODE_SAMEBINSIZE) {mode=DISPLAY_MODE_SAMEBINSIZE; setUpdateRoot(0); updateObjects(); repaint();}}
        if (cmd=="multiplebarcharts") { if(mode!=DISPLAY_MODE_MULTIPLEBARCHARTS) {mode=DISPLAY_MODE_MULTIPLEBARCHARTS; setUpdateRoot(0); updateObjects(); repaint();}}
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
    char[] Dirs;
    double residSum;
    int censor=0;
    public void create(int x1, int y1, int x2, int y2, String info) {
        
        double[] table = ft.getTable();
        double[] exp = ft.getExp();
        
        int[] levels = ft.getLevels();
        String[][] lnames = ft.getLnames();
        
        rects.removeAllElements();
        Labels.removeAllElements();
        
        plevels = new int[vs];               // reverse cumulative product of levels
        plevels[vs-1] = 0;
        if( vs>1 )
            plevels[vs-2] = levels[vs-1];	// calculate the number of cells covered by a
        // category in level vs
        for (int i=vs-3; i>=0; i--) {
            plevels[i] = plevels[i+1] * levels[i+1];
        }
        
        int thisGap;
        int subX=0, subY=0;
        int mulX=1, mulY=1;
        Gaps = new int[maxLevel+2];
        aGap = new int[maxLevel+2];
        
        for( int j=0; j<maxLevel; j++) {
            thisGap = (maxLevel - j) * 3;
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
        
        combination = new int[vs];
        
        // start the recursion ////////////
        createMosaic(0, 0, startTable, x1, y1, Math.max(x2-subX,1), Math.max(y2-subY,1), info);
        
        // Create labels for the first 2 dimensions
        MyText label;
        if( Dirs[0] == 'x' && Dirs[1] == 'y' || Dirs[0] == 'y' && Dirs[1] == 'x') {
            for(int j=0; j<Math.min(2, maxLevel); j++)
                for( int i=0; i<levels[j]; i++) {
                if( Dirs[j] == 'x' )
                    label = new MyText(lnames[j][i], (int)((x1+(double)(x2-x1)/(double)levels[j]*(i+0.5))), 0, 0);
                else
                    label = new MyText(lnames[j][i], 0, (int)((y1+(double)(y2-y1)/(double)levels[j]*(i+0.5))), 1);
                Labels.addElement(label);
                }
        }
        
        if( mode==DISPLAY_MODE_MULTIPLEBARCHARTS || mode==DISPLAY_MODE_FLUCTUATION ) {
            double maxCount=0;
            for (int i=0; i<rects.size(); i++)
                maxCount = Math.max(maxCount, ((PPrimMosaic)(rects.elementAt(i))).getObs());
            for (int i=0; i<rects.size(); i++) {
                PPrimMosaic r = (PPrimMosaic)(rects.elementAt(i));
                int newH, newW;
                int newY=r.r.y;
                if( mode==DISPLAY_MODE_MULTIPLEBARCHARTS ) {
                    r.setDir('y');
                    newW = r.r.width;
                    newH = (int)((double)r.r.height * (1.0+(double)censor/5.0) * r.getObs()/maxCount);
                } else {
                    newW = (int)((double)r.r.width * ((1.0+(double)censor/5.0) * Math.sqrt(r.getObs()/maxCount)));
                    newH = (int)((double)r.r.height * ((1.0+(double)censor/5.0) * Math.sqrt(r.getObs()/maxCount)));
                }
                if( (newH >= r.r.height && newW >= r.r.width) && censor > 0){
                    r.setCensored(true);
                    newH=r.r.height;
                    newW=r.r.width;
                }
                r.changeDimension(newW,newH);
            }
        }
    }
    
    public void createMosaic(int start, int levelid, double[] Mtable, int x1, int y1, int x2, int y2, String infop) {
        
        //int levels = ft.getLevels()[levelid];
        int k = vs;
        String name = v[levelid].getName();
        String[] lnames = (String[])v[levelid].getCategories();
        int levels = lnames.length;
        
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
        
        if( total > 0 ){
            for (int j=0; j < levels; j++) {
                combination[levelid]=j;
                
                info = infop.toString() + name + ": " + lnames[j] + '\n';// Add the popup information
                
                boolean stop  = false;
                int addGapX = 0;
                int addGapY = 0;

                if( (mode==DISPLAY_MODE_SAMEBINSIZE) && oCounts[j+1]-oCounts[j] == 0 || levelid == maxLevel-1 ) {
                    stop = true;
                    for( int i=levelid+1; i<maxLevel; i++ )
                        if( Dirs[i] == 'x' )
                            addGapX += aGap[i];
                        else
                            addGapY += aGap[i];
                }
                
                int[] ref = ft.getMatchingCases(combination);
                boolean empty = (ref.length==0);
                
                if( stop || empty ) {	            // Now the rectangles are generated
                    tile = new PPrimMosaic();
                    tile.info=info;
                    tile.setType(mode);
                    tile.ref = ref;
                    
                    if( Dirs[levelid] == 'x' ){
                        if( empty && mode!=DISPLAY_MODE_MULTIPLEBARCHARTS && mode!=DISPLAY_MODE_FLUCTUATION ){
                            tile.r = new Rectangle(x1 + (int)(counts[j] / total * sizeX) + j * thisGap,
                                    y1,
                                    emptyBin,
                                    sizeY+emptyWidth);
                            tile.setObs(0);
                            //dir='y', mode=displayMode
                            //missing: exps[j], 4 / residSum, tablep.p
                        } else{
                            tile.r = new Rectangle(x1 + (int)(counts[j] / total * sizeX) + j * thisGap,
                                    y1,
                                    Math.max(1, (int)((counts[j+1] - counts[j]) / total * sizeX)) + addGapX,
                                    y2-y1 + addGapY);
                            tile.setObs(obs[j]);
                            //dir='y', mode=displayMode
                            //missing: exps[j], 4 / residSum, tablep.p
                        }
                    } else {
                        if( empty && mode!=DISPLAY_MODE_MULTIPLEBARCHARTS && mode!=DISPLAY_MODE_FLUCTUATION ){
                            tile.r = new Rectangle(x1,
                                    y1 + (int)(counts[j] / total * sizeY) + j * thisGap,
                                    sizeX+emptyWidth,
                                    emptyBin);
                            tile.setObs(0);
                            //dir='x', mode=displayMode
                            //missing: exps[j], 4 / residSum, tablep.p
                        } else {
                            tile.r = new Rectangle(x1,
                                    y1 + (int)(counts[j] / total * sizeY) + j * thisGap,
                                    x2-x1 + addGapX,
                                    Math.max(1, (int)((counts[j+1] - counts[j]) / total * sizeY)) + addGapY);
                            tile.setObs(obs[j]);
                            //dir='x', mode=displayMode
                            //missing: exps[j], 4 / residSum, tablep.p
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
    }
    
    private Color hiliteColor = new Color(180, 96, 135);
    
    public void paintBack(org.rosuda.pograss.PoGraSS g) {
        for(Enumeration en=Labels.elements(); en.hasMoreElements();){
            MyText label = (MyText)en.nextElement();
            double ax=0;
            double ay=0;
            String s = label.s;
            switch(label.rot){
                case 0:
                    ax=0.5;
                    ay=1;
                    break;
                case 1:
                    ax=0;
                    ay=0.5;
                    if (g.getWidthEstimate(label.s)>mLeft)
                        s=Common.getTriGraph(s);
                    break;
            }
            g.drawString(s, label.x, label.y, ax, ay);
        }
    }
    
    protected void processKeyEvent(KeyEvent e) {
        super.processKeyEvent(e);
        
        
    }
    
    public void keyReleased(KeyEvent e) {
        int code = e.getKeyCode();
        boolean repaint=false;
        switch(code){
            case KeyEvent.VK_DOWN:
                if( e.isShiftDown() )
                    if( censor > 0 ){
                    censor--;
                    repaint=true;
                    } else
                        return;
                else
                    if( maxLevel < vs ) {
                    maxLevel += 1;
                    repaint=true;
                    }
                break;
            case KeyEvent.VK_UP:
                if( e.isShiftDown() ){
                    censor++;
                    repaint=true;
                } else
                    if( maxLevel > 1 ) {
                    maxLevel -= 1;
                    repaint=true;
                    }
                break;
            case KeyEvent.VK_LEFT:
                if( maxLevel != vs ) {
                    int[] rotation = new int[vs];
                    for (int i=0; i<maxLevel-1; i++)
                        rotation[i] = i;
                    for (int i=maxLevel-1; i<vs ; i++)
                        rotation[i] = i+1;
                    rotation[vs-1] = maxLevel-1;
                    ft.permute(rotation);
                    v = ft.getVars();
                    repaint=true;
                }
                break;
            case KeyEvent.VK_RIGHT:
                if( maxLevel != vs ) {
                    int[] rotation = new int[vs];
                    for (int i=0; i<maxLevel-1; i++)
                        rotation[i] = i;
                    for (int i=maxLevel; i<vs ; i++)
                        rotation[i] = i-1;
                    rotation[maxLevel-1] = vs-1;
                    ft.permute(rotation);
                    v = ft.getVars();
                    repaint=true;
                }
                break;
            case KeyEvent.VK_R:
                if(e.getModifiers() == Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()){
                    for( int i=maxLevel-1; i<vs; i++ )
                        if( Dirs[i] == 'x')
                            Dirs[i] = 'y';
                        else
                            Dirs[i] = 'x';
                    repaint=true;
                }
                break;
            case KeyEvent.VK_ADD:
                if(e.getModifiers() != Toolkit.getDefaultToolkit().getMenuShortcutKeyMask())
                    break;
            case KeyEvent.VK_SUBTRACT:
                //frame.setCursor(Frame.WAIT_CURSOR);
                int[] interact = new int[maxLevel];
                for( int i=0; i<maxLevel; i++ )
                    interact[i] = i;
                if( e.getKeyCode() == KeyEvent.VK_ADD )
                    if( !ft.addInteraction( interact, true ) )
                        Toolkit.getDefaultToolkit().beep();
                if( e.getKeyCode() == KeyEvent.VK_SUBTRACT )
                    if( !ft.deleteInteraction( interact ) )
                        Toolkit.getDefaultToolkit().beep();
                //frame.setCursor(Frame.DEFAULT_CURSOR);
                repaint=true;
        }
        if(repaint){
            updateObjects();
            setUpdateRoot(0);
            repaint();
        }
        
        super.keyReleased(e);
    }
    
    /**
     *  From org.rosuda.Mondrian.MyText, reduced and modified.
     *  Preliminary! org.rosuda.ibase.toolkit.PlotText should be used instead.
     */
    class MyText {
        String s;
        public int x;
        public int y;
        public int rot; // 0: no rotation; 1: 90 degrees left
        
        public MyText(String s, int x, int y, int rot) {
            this.s = s;
            this.x = x;
            this.y = y;
            this.rot = rot;
        }
    }
}
