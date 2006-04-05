package org.rosuda.ibase.plots;
import java.awt.Frame;
import java.awt.Rectangle;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import org.rosuda.ibase.*;
import org.rosuda.ibase.toolkit.*;


public class MosaicCanvas extends BaseCanvas {
    static final String M_PLUS = "+";
    static final String M_OBSERVED = "observed";
    static final String M_EXPECTED = "expected";
    static final String M_SAMEBINSIZE = "samebinsize";
    static final String M_MULTIPLEBARCHARTS = "multiplebarcharts";
    static final String M_FLUCTUATION = "fluctuation";
    static final String M_MAXLEVELUP = "maxLevelUp";
    static final String M_MAXLEVELDOWN = "maxLevelDown";
    static final String M_CENSORUP = "censorUp";
    static final String M_CENSORDOWN = "censorDown";
    static final String M_PERMUTELEFT = "permuteLeft";
    static final String M_PERMUTERIGHT = "permuteRight";
    static final String M_SET1 = "set1";
    static final String M_SET64 = "set64";
    static final String M_RESET = "reset";
    
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
    
    private final int standardMLeft=35;
    
    public MosaicCanvas(final PlotComponent ppc, final Frame f, final SVar[] vars, final SMarker mark) {
        super(ppc,f, mark);
        this.frame=f;
        this.v = vars;
        
        maxLevel = v.length;
        this.vs = v.length;
        for(int i=0; i<vs; i++){
            v[i].addDepend(this);
            v[i].categorize();
        }
        
        createMenu(f,true,false,false,new String[]{
            "Observed",M_OBSERVED,
            "Expected",M_EXPECTED,
            "Same bin size",M_SAMEBINSIZE,
            "Multiple barcharts",M_MULTIPLEBARCHARTS,
            "Fluctuation",M_FLUCTUATION,
            "-",
            "Less variables (up)",M_MAXLEVELUP,
            "More variables (down)",M_MAXLEVELDOWN,
            "Increase censor (shift+up)",M_CENSORUP,
            "Decrease censor (shift+down)",M_CENSORDOWN,
            "Rotate variables left (left)",M_PERMUTELEFT,
            "Rotate variables right (right)",M_PERMUTERIGHT,
            "-",
            "Set Colors (CB)",M_SET1,
            "Set Colors (rainbow)",M_SET64,
            "Clear Colors",M_RESET
        });
        
        mLeft=standardMLeft; mRight=5; mTop=20; mBottom=5;
        
        ft=new FrequencyTable(v);
        Dirs = new char[vs];
        for (int i=0; i<vs; i++ ) {
            if( (i % 2) == 0 )
                Dirs[i] = 'x';
            else
                Dirs[i] = 'y';
        }
        dontPaint=false;
    }
    
    public void updateObjects() {
        // updateObjects is called when variables are categorized, i.e. before the frequency table is initialized, so:
        if(dontPaint) return;
        
        final int maxLabelLength=create(mLeft,mTop, pc.getWidth()-mRight, pc.getHeight()-mBottom, "");
        final int omLeft=mLeft;
        if(maxLabelLength*8>standardMLeft){
            mLeft=8*maxLabelLength+2;
        } else mLeft=standardMLeft;
        if(mLeft!=omLeft) create(mLeft,mTop, pc.getWidth()-mRight, pc.getHeight()-mBottom, "");
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
    
    public String queryObject(final int i) {
        if (pp!=null && pp[i]!=null) {
            final int mark=(int)((pp[i].cases())*pp[i].getMarkedProportion(m,-1)+0.5);
            return ((PPrimMosaic) pp[i]).toString()+"\n"+((mark>0)?(""+mark+" of "+pp[i].cases()+" selected"):(""+pp[i].cases()+" cases"));
        }
        return "N/A";
    }
    
    public Object run(final Object o, final String cmd) {
        super.run(o,cmd);
        
        if (M_OBSERVED.equals(cmd)) { if(mode!=DISPLAY_MODE_OBSERVED) {mode=DISPLAY_MODE_OBSERVED; setUpdateRoot(0); updateObjects(); repaint();}}
        if (M_EXPECTED.equals(cmd)) { if(mode!=DISPLAY_MODE_EXPECTED) {mode=DISPLAY_MODE_EXPECTED; setUpdateRoot(0); updateObjects(); repaint();}}
        if (M_SAMEBINSIZE.equals(cmd)) { if(mode!=DISPLAY_MODE_SAMEBINSIZE) {mode=DISPLAY_MODE_SAMEBINSIZE; setUpdateRoot(0); updateObjects(); repaint();}}
        if (M_MULTIPLEBARCHARTS.equals(cmd)) { if(mode!=DISPLAY_MODE_MULTIPLEBARCHARTS) {mode=DISPLAY_MODE_MULTIPLEBARCHARTS; setUpdateRoot(0); updateObjects(); repaint();}}
        if (M_FLUCTUATION.equals(cmd)) { if(mode!=DISPLAY_MODE_FLUCTUATION) {mode=DISPLAY_MODE_FLUCTUATION; setUpdateRoot(0); updateObjects(); repaint();}}
        
        if (M_MAXLEVELUP.equals(cmd)) {
            if( maxLevel > 1 ) {
                maxLevel -= 1;
                updateObjects(); setUpdateRoot(0); repaint();
            }
        }
        if (M_MAXLEVELDOWN.equals(cmd)) {
            if( maxLevel < vs ) {
                maxLevel += 1;
                updateObjects(); setUpdateRoot(0); repaint();
            }
        }
        if(M_CENSORUP.equals(cmd)){
            censor++;
            updateObjects(); setUpdateRoot(0); repaint();
        }
        if(M_CENSORDOWN.equals(cmd)){
            if( censor > 0 ){
                censor--;
                updateObjects(); setUpdateRoot(0); repaint();
            }
        }
        if(M_PERMUTELEFT.equals(cmd)){
            if( maxLevel != vs ) {
                final int[] rotation = new int[vs];
                for (int i=0; i<maxLevel-1; i++)
                    rotation[i] = i;
                for (int i=maxLevel-1; i<vs ; i++)
                    rotation[i] = i+1;
                rotation[vs-1] = maxLevel-1;
                ft.permute(rotation);
                v = ft.getVars();
                updateObjects(); setUpdateRoot(0); repaint();
            }
        }
        if(M_PERMUTERIGHT.equals(cmd)){
            if( maxLevel != vs ) {
                final int[] rotation = new int[vs];
                for (int i=0; i<maxLevel-1; i++)
                    rotation[i] = i;
                for (int i=maxLevel; i<vs ; i++)
                    rotation[i] = i-1;
                rotation[maxLevel-1] = vs-1;
                ft.permute(rotation);
                v = ft.getVars();
                updateObjects(); setUpdateRoot(0); repaint();
            }
        }
        return null;
    }
    
    private List rects = new ArrayList();
    private int[] plevels;
    private int[] aGap;
    private int[] Gaps;
    private char[] Dirs;
    private double residSum;
    private int censor=0;
    private int create(final int x1, final int y1, final int x2, final int y2, final String info) {
        
        final double[] table = ft.getTable();
        final double[] exp = ft.getExp();
        
        final int[] levels = ft.getLevels();
        final String[][] lnames = ft.getLnames();
        
        rects.clear();
        
        plevels = new int[vs];               // reverse cumulative product of levels
        plevels[vs-1] = 0;
        if( vs>1 )
            plevels[vs-2] = levels[vs-1];	// calculate the number of cells covered by a
        // category in level vs
        for (int i=vs-3; i>=0; i--) {
            plevels[i] = plevels[i+1] * levels[i+1];
        }
        
        Gaps = new int[maxLevel+2];
        aGap = new int[maxLevel+2];
        
        int thisGap;
        int subY = 0;
        int subX = 0;
        int mulY = 1;
        int mulX = 1;
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
            final char dir = Dirs[j];
            int sum = Gaps[j];
            int kk=j+1; // replaced k by kk as variable k already exists
            while(kk<maxLevel) {
                
                if( Dirs[kk] == dir ) {
                    int prod = 1;
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
        int maxLabelLength = 0;
        if( Dirs[0] == 'x' && Dirs[1] == 'y' || Dirs[0] == 'y' && Dirs[1] == 'x') {
            for(int j=0; j<Math.min(2, maxLevel); j++){
                for( int i=0; i<levels[j]; i++) {
                    if( Dirs[j] == 'x' ){
                        labels.add((int)((x1+(double)(x2-x1)/(double)levels[j]*(i+0.5))), mTop-5, 0.5,0, lnames[j][i]);
                    } else{
                        labels.add(mLeft-5, (int)((y1+(double)(y2-y1)/(double)levels[j]*(i+0.5))),1,0.5,mLeft-5, lnames[j][i]);
                        if(lnames[j][i].length()>maxLabelLength) maxLabelLength=lnames[j][i].length();
                    }
                }
            }
        }
        labels.finishAdd();
        
        if( mode==DISPLAY_MODE_MULTIPLEBARCHARTS || mode==DISPLAY_MODE_FLUCTUATION ) {
            double maxCount=0;
            for (int i=0, rsize = rects.size(); i<rsize; i++)
                maxCount = Math.max(maxCount, ((PPrimMosaic)(rects.get(i))).getObs());
            for (int i=0, rsize = rects.size(); i<rsize; i++) {
                final PPrimMosaic r = (PPrimMosaic)(rects.get(i));
                int newH, newW;
                
                if( mode==DISPLAY_MODE_MULTIPLEBARCHARTS ) {
                    r.setDir('y');
                    newW = r.r.width;
                    newH = (int)(r.r.height * (1.0+(double)censor/5.0) * r.getObs()/maxCount);
                } else {
                    newW = (int)(r.r.width * ((1.0+(double)censor/5.0) * Math.sqrt(r.getObs()/maxCount)));
                    newH = (int)(r.r.height * ((1.0+(double)censor/5.0) * Math.sqrt(r.getObs()/maxCount)));
                }
                if( (newH >= r.r.height && newW >= r.r.width) && censor > 0){
                    r.setCensored(true);
                    newH=r.r.height;
                    newW=r.r.width;
                }
                r.changeDimension(newW,newH);
            }
        }
        return maxLabelLength;
    }
    
    private void createMosaic(final int start, final int levelid, final double[] Mtable, final int x1, final int y1, final int x2, final int y2, final String infop) {
        
        //int levels = ft.getLevels()[levelid];
        final int k = vs;
        final String name = v[levelid].getName();
        
        final Object[] lnames = v[levelid].getCategories();
        final int levels = lnames.length;
        
        final double[] exp = ft.getExp();
        final double[] table = ft.getTable();
        
        final double[] counts = new double[levels+1];
        final double[] oCounts = new double[levels+1];
        final double[]   exps = new double[levels];
        final double[]    obs = new double[levels];
        
        // Calculate the absolute counts for each level first
        double total = 0;
        if ( levelid < k-1 ) {
            int index;	        // if we did not reach the lowest level
            
            for (int j=0; j < levels; j++) {
                for (int i=0; i < plevels[levelid]; i++) {
                    index = start+ j*plevels[levelid] + i;
                    total += Mtable[index];
                    counts[j+1] += Mtable[index];
                    oCounts[j+1] += table[index];
                    exps[j] += exp[index];
                    obs[j] += table[index];
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
            }
        }
        
//      int thisGap = 0;
//      if( !displayMode.equals("Fluctuation") )
        final int thisGap = aGap[levelid];
        
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
        
        final int sizeX = x2-x1;
        final int sizeY = y2-y1;
        
        if( total > 0 ){
            String info;
            PPrimMosaic tile;
            for (int j=0; j < levels; j++) {
                combination[levelid]=j;
                if(levelid+1<combination.length) combination[levelid+1]=-1;
                
                info = infop + name + ": " + lnames[j] + '\n';// Add the popup information
                
                boolean stop  = false;
                int addGapX = 0;
                int addGapY = 0;
                
                if( (mode==DISPLAY_MODE_SAMEBINSIZE) && Math.abs(oCounts[j+1]-oCounts[j]) < 0.0001 || levelid == maxLevel-1 ) {
                    stop = true;
                    for( int i=levelid+1; i<maxLevel; i++ )
                        if( Dirs[i] == 'x' )
                            addGapX += aGap[i];
                        else
                            addGapY += aGap[i];
                }
                
                final int[] ref = ft.getMatchingCases(combination,maxLevel);
                final boolean empty = (ref.length==0)&& mode!=DISPLAY_MODE_MULTIPLEBARCHARTS && mode!=DISPLAY_MODE_FLUCTUATION && mode!=DISPLAY_MODE_SAMEBINSIZE;
                
                if( stop || empty ) {	            // Now the rectangles are generated
                    tile = new PPrimMosaic();
                    tile.info=info;
                    tile.setType(mode);
                    tile.ref = ref;
                    tile.setExp(exps[j]);
                    tile.setScale(4 / residSum);
                    tile.setP(ft.getP());
                    
                    if( Dirs[levelid] == 'x' ){
                        if( empty ){
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
                        if( empty  ){
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
                    rects.add(tile);
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
    
    public void keyReleased(final KeyEvent e) {
        final int code = e.getKeyCode();
        
        switch(code){
            case KeyEvent.VK_DOWN:
                if( e.isShiftDown() )
                    run(this,M_CENSORDOWN);
                else
                    run(this,M_MAXLEVELDOWN);
                break;
            case KeyEvent.VK_UP:
                if( e.isShiftDown() ){
                    run(this,M_CENSORUP);
                } else
                    run(this,M_MAXLEVELUP);
                break;
            case KeyEvent.VK_LEFT:
                run(this,M_PERMUTELEFT);
                break;
            case KeyEvent.VK_RIGHT:
                run(this,M_PERMUTERIGHT);
                break;
        }
        final boolean repaint = false;
        if(repaint){
            updateObjects();
            setUpdateRoot(0);
            repaint();
        }
        
        super.keyReleased(e);
    }
    
    public SVar getData(final int id) { return (id>=0 && id<v.length)?v[id]:null; }
    
    public void Notifying(final NotifyMsg msg, final Object o, final Vector path) {
        if(!ignoreNotifications && msg.getMessageID()==Common.NM_VarSeqChange){
            ft=new FrequencyTable(v);
        }
        super.Notifying(msg, o, path);
    }
    
    public void rotate(final int amount) {
        if((amount&1) == 1){
            for( int i=maxLevel-1; i<vs; i++ )
                if( Dirs[i] == 'x')
                    Dirs[i] = 'y';
                else
                    Dirs[i] = 'x';
            updateObjects();
            setUpdateRoot(0);
            repaint();
        }
    }
    
    
}
