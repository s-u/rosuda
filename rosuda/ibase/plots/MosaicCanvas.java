package org.rosuda.ibase.plots;
import java.awt.Color;
import java.awt.Frame;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.util.Enumeration;
import java.util.Vector;

import org.rosuda.ibase.*;
import org.rosuda.ibase.toolkit.*;


public class MosaicCanvas extends BaseCanvas {
    
    private SVar[] v;
    private int vs;
    
    private Frame frame;
    
    private int maxLevel;
    
    private int w,h,hgap,vgap; //width, height and gaps between mosaics
    
    private static final int DISPLAY_MODE_OBSERVED = 0;
    private static final int DISPLAY_MODE_EXPECTED = 1;
    private static final int DISPLAY_MODE_SAMEBINSIZE = 2;
    private static final int DISPLAY_MODE_MULTIPLEBARCHARTS = 3;
    private static final int DISPLAY_MODE_FLUCTUATION = 4;
    
    private int mode = DISPLAY_MODE_OBSERVED;
    
    private FrequencyTable ft;
    private int[] combination; // indicates position of recursion
    
    public MosaicCanvas(PlotComponent pc, Frame f, SVar[] vars, SMarker mark) {
        super(pc,f, mark);
        ft = new FrequencyTable(vars);
        this.frame=f;
        this.v = vars;
        
        maxLevel = v.length;
        this.vs = v.length;
        for(int i=0; i<vs; i++){
            v[i].categorize();
        }
        
        String myMenu[]={"+","File","~File.Graph","+","View","Observed","observed","Expected","expected","Same bin size","samebinsize","Multiple barcharts","multiplebarcharts","Fluctuation","fluctuation","~Edit","~Window","0"};
        EzMenu.getEzMenu(f,this,myMenu);
        mLeft=40; mRight=5; mTop=20; mBottom=5;
        
        
        
        Dirs = new char[vs];
        for (int i=0; i<vs; i++ ) {
            if( (i % 2) == 0 )
                Dirs[i] = 'x';
            else
                Dirs[i] = 'y';
        }
    }
    
    public void updateObjects() {
        
        create(mLeft,mTop, pc.getWidth()-mRight, pc.getHeight()-mBottom, "");
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
        
        if (cmd=="observed") { if(mode!=DISPLAY_MODE_OBSERVED) {mode=DISPLAY_MODE_OBSERVED; setUpdateRoot(0); updateObjects(); repaint();}}
        if (cmd=="expected") { if(mode!=DISPLAY_MODE_EXPECTED) {mode=DISPLAY_MODE_EXPECTED; setUpdateRoot(0); updateObjects(); repaint();}}
        if (cmd=="samebinsize") { if(mode!=DISPLAY_MODE_SAMEBINSIZE) {mode=DISPLAY_MODE_SAMEBINSIZE; setUpdateRoot(0); updateObjects(); repaint();}}
        if (cmd=="multiplebarcharts") { if(mode!=DISPLAY_MODE_MULTIPLEBARCHARTS) {mode=DISPLAY_MODE_MULTIPLEBARCHARTS; setUpdateRoot(0); updateObjects(); repaint();}}
        if (cmd=="fluctuation") { if(mode!=DISPLAY_MODE_FLUCTUATION) {mode=DISPLAY_MODE_FLUCTUATION; setUpdateRoot(0); updateObjects(); repaint();}}
        return null;
    }
    
    private Vector rects = new Vector();
    private int[] plevels;
    private int[] aGap;
    private int[] Gaps;
    private char[] Dirs;
    private double residSum;
    private int censor=0;
    private void create(int x1, int y1, int x2, int y2, String info) {
        
        System.out.println("Du bist nicht null: " + ft);
        double[] table = ft.getTable();
        double[] exp = ft.getExp();
        
        int[] levels = ft.getLevels();
        String[][] lnames = ft.getLnames();
        
        rects.removeAllElements();
        
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
        labels.clear();
        if( Dirs[0] == 'x' && Dirs[1] == 'y' || Dirs[0] == 'y' && Dirs[1] == 'x') {
            for(int j=0; j<Math.min(2, maxLevel); j++)
                for( int i=0; i<levels[j]; i++) {
                if( Dirs[j] == 'x' )
                    labels.add((int)((x1+(double)(x2-x1)/(double)levels[j]*(i+0.5))), 0, 0.5,1, lnames[j][i]);
                else
                    labels.add(0, (int)((y1+(double)(y2-y1)/(double)levels[j]*(i+0.5))),0,0.5,mLeft, lnames[j][i]);
                }
        }
        labels.finishAdd();
        
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
    
    private void createMosaic(int start, int levelid, double[] Mtable, int x1, int y1, int x2, int y2, String infop) {
        
        //int levels = ft.getLevels()[levelid];
        int k = vs;
        String name = v[levelid].getName();
        
        Object[] lnames = v[levelid].getCategories();
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
                if(levelid+1<combination.length) combination[levelid+1]=-1;
                
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
                
                int[] ref = ft.getMatchingCases(combination,maxLevel);
                boolean empty = (ref.length==0);
                
                if( stop || empty ) {	            // Now the rectangles are generated
                    tile = new PPrimMosaic();
                    tile.info=info;
                    tile.setType(mode);
                    tile.ref = ref;
                    tile.setExp(exps[j]);
                    tile.setScale(4 / residSum);
                    tile.setP(ft.getP());
                    
                    if( Dirs[levelid] == 'x' ){
                        if( empty && mode!=DISPLAY_MODE_MULTIPLEBARCHARTS && mode!=DISPLAY_MODE_FLUCTUATION && mode!=DISPLAY_MODE_SAMEBINSIZE ){
                            tile.r = new Rectangle(x1 + (int)(counts[j] / total * sizeX) + j * thisGap,
                                    y1,
                                    emptyBin,
                                    sizeY+emptyWidth);
                            tile.setObs(0);
                            tile.setDir('y');
                        } else{
                            tile.r = new Rectangle(x1 + (int)(counts[j] / total * sizeX) + j * thisGap,
                                    y1,
                                    Math.max(1, (int)((counts[j+1] - counts[j]) / total * sizeX)) + addGapX,
                                    y2-y1 + addGapY);
                            tile.setObs(obs[j]);
                            tile.setDir('y');
                        }
                    } else {
                        if( empty && mode!=DISPLAY_MODE_MULTIPLEBARCHARTS && mode!=DISPLAY_MODE_FLUCTUATION && mode!=DISPLAY_MODE_SAMEBINSIZE ){
                            tile.r = new Rectangle(x1,
                                    y1 + (int)(counts[j] / total * sizeY) + j * thisGap,
                                    sizeX+emptyWidth,
                                    emptyBin);
                            tile.setObs(0);
                            tile.setDir('x');
                        } else {
                            tile.r = new Rectangle(x1,
                                    y1 + (int)(counts[j] / total * sizeY) + j * thisGap,
                                    x2-x1 + addGapX,
                                    Math.max(1, (int)((counts[j+1] - counts[j]) / total * sizeY)) + addGapY);
                            tile.setObs(obs[j]);
                            tile.setDir('x');
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
        }
        if(repaint){
            updateObjects();
            setUpdateRoot(0);
            repaint();
        }
        
        super.keyReleased(e);
    }
    
    public SVar getData(int id) { return (id>=0 && id<v.length)?v[id]:null; }
}
