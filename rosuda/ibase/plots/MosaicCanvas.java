package org.rosuda.ibase.plots;
import java.awt.Frame;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import org.rosuda.ibase.*;
import org.rosuda.ibase.toolkit.*;
import org.rosuda.pograss.PoGraSS;
import org.rosuda.util.Tools;


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
    
    private int[] x1x2 = new int[2];
    private int[] y1y2 = new int[2];
    
    private int subX;
    
    
    
    private final int standardMLeft=20;
    
    public MosaicCanvas(final int gd, final Frame f, final SVar[] vars, final SMarker mark) {
        super(gd,f, mark);
        this.frame=f;
        this.v = vars;
        changingHilitingNeedsUpdateRoot=0;
        
        maxLevel = v.length;
        this.vs = v.length;
        for(int i=0; i<vs; i++){
            v[i].addDepend(this);
            v[i].categorize();
        }
        
        createMenu(f,true,false,false,true,new String[]{
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
            "Reorder variables left (left)",M_PERMUTELEFT,
            "Reorder variables right (right)",M_PERMUTERIGHT,
        });
        
        setDefaultMargins(new int[] {standardMLeft,5,20,5});
        
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
        
        create(mLeft,mTop, getWidth()-mRight, getHeight()-mBottom, "");
        if(pp==null || pp.length!=rects.size()) pp = new PlotPrimitive[rects.size()];
        rects.toArray(pp);
        for(int i=0; i<pp.length; i++) setColors((PPrimBase)pp[i]);
        
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
        String qs="";
        final boolean actionExtQuery=isExtQuery;
        if(actionExtQuery) {
            if(pp!=null && pp[i]!= null) {
                final int mark=(int)((pp[i].cases())*pp[i].getMarkedProportion(m,-1)+0.5);
                qs+=((PPrimMosaic) pp[i]).toString()+
                        "\ncount: "+pp[i].cases();
                if(v!=null && v[0]!=null) qs+=" ("+Tools.getDisplayableValue(100.0*(pp[i].cases())/((double)v[0].size()),2)+"% of total)";
                if(mark>0) {
                    qs+="\nselected: "+mark+" ("+Tools.getDisplayableValue(100.0*pp[i].getMarkedProportion(m, -1)  ,2)+"% of this cat., "+
                            ((v!=null&&v[0]!=null)?Tools.getDisplayableValue(100.0*mark/((double)v[0].size()),2)+"% of total, ":"")+
                            Tools.getDisplayableValue(100.0*mark/((double)m.marked()),2)+"% of total selection)";
                }
            } else qs="N/A";
        } else {
            if (pp!=null && pp[i]!=null) {
                final int mark=(int)((pp[i].cases())*pp[i].getMarkedProportion(m,-1)+0.5);
                qs+=((PPrimMosaic) pp[i]).toString()+
                        "\ncount: "+pp[i].cases()+((mark>0)?"\nselected: "+mark:"");
            } else qs="N/A";
        }
        return qs;
    }
    
    public String queryPlotSpace() {
        if(v==null) return null;
        else {
            String qps="Mosaicplot (";
            for(int i=0;i<v.length-1;i++) qps+=v[i].getName()+", ";
            qps+=v[v.length-1].getName()+")";
            return qps+((m.marked()>0)?"\n"+m.marked()+" selected case(s)":"");
        }
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
        if (M_SETCB1.equals(cmd)) {
            if (pp!=null && pp.length>0) {
                int i=0;
                while (i<pp.length) {
                    final int cs[] = ((PPrimBase)pp[i]).getCaseIDs();
                    int j=0;
                    if (cs!=null)
                        while (j<cs.length)
                            m.setSec(cs[j++],i+16);
                    i++;
                }
                m.NotifyAll(new NotifyMsg(this,Common.NM_SecMarkerChange));
            }
        }
        if (M_SETCB64.equals(cmd)) {
            if (pp!=null && pp.length>0) {
                int i=0;
                while (i<pp.length) {
                    //System.out.println("set64: "+i+" (of "+pp.length+") mapped to "+ax.getCatAtSeqIndex(i)+", pp="+pp[i]);
                    final int cs[] = ((PPrimBase)pp[i]).getCaseIDs();
                    int j=0;
                    if (cs!=null)
                        while (j<cs.length)
                            m.setSec(cs[j++],64+(64*i/pp.length));
                    i++;
                }
                m.NotifyAll(new NotifyMsg(this,Common.NM_SecMarkerChange));
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
    private void create(final int x1, final int y1, final int x2, final int y2, final String info) {
        
        final double[] table = ft.getTable();
        final double[] exp = ft.getExp();
        
        final int[] levels = ft.getLevels();
        
        
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
        subX = 0;
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
        
        if( Dirs[0] == 'x' && Dirs[1] == 'y' || Dirs[0] == 'y' && Dirs[1] == 'x') {
            x1x2[0] = x1;
            x1x2[1] = x2;
            y1y2[0] = y1;
            y1y2[1] = y2;
        }
        
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
                final boolean empty = (ref.length==0)&& mode!=DISPLAY_MODE_MULTIPLEBARCHARTS && mode!=DISPLAY_MODE_FLUCTUATION && mode!=DISPLAY_MODE_EXPECTED && mode!=DISPLAY_MODE_SAMEBINSIZE;
                
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
                            tile.setBounds(x1 + (int)(counts[j] / total * sizeX) + j * thisGap,
                                    y1,
                                    emptyBin,
                                    sizeY+emptyWidth);
                            tile.setObs(0);
                            tile.setDir('y');
                        } else{
                            tile.setBounds(x1 + (int)(counts[j] / total * sizeX) + j * thisGap,
                                    y1,
                                    Math.max(1, (int)((counts[j+1] - counts[j]) / total * sizeX)) + addGapX,
                                    y2-y1 + addGapY);
                            tile.setObs(obs[j]);
                            tile.setDir('y');
                        }
                    } else {
                        if( empty  ){
                            tile.setBounds(x1,
                                    y1 + (int)(counts[j] / total * sizeY) + j * thisGap,
                                    sizeX+emptyWidth,
                                    emptyBin);
                            tile.setObs(0);
                            tile.setDir('x');
                        } else {
                            tile.setBounds(x1,
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
    
    public void paintBack(final PoGraSS g) {
        
        final int[] levels = ft.getLevels();
        
        final String[][] lnames = ft.getLnames();
        final int x1=x1x2[0];
        final int x2=x1x2[1];
        final int y1=y1y2[0];
        final int y2=y1y2[1];
        
        startAddingLabels();
        int maxLabelLength = 0;
        if( Dirs[0] == 'x' && Dirs[1] == 'y' || Dirs[0] == 'y' && Dirs[1] == 'x') {
            for(int j=0; j<Math.min(2, maxLevel); j++){
                
                
                
                
                if( Dirs[j] == 'x' ){
                    final int[] positions = new int[levels[j]];
                    for(int i=0; i<levels[j]; i++) positions[i] = (int)(x1+(double)(x2-x1)/(double)levels[j]*(i+0.5));
                    addXLabels(g,ax,lnames[j],(Math.max(x2-subX,1))/levels[j],positions,false,true,true);
                } else{
                    final int[] positions = new int[levels[j]];
                    final int[] maxH = new int[levels[j]];
                    for(int i=0; i<levels[j]; i++) {
                        positions[i] = (int)((y1+(double)(y2-y1)/(double)levels[j]*(i+0.5)));
                        if(rotateYLabels){
                            final int sup,sub;
                            if(i==0) sup=mTop;
                            else sup = (int)(y1+(double)(y2-y1)/(double)levels[j]*(i-1+0.5));
                            
                            if(i==levels[j]-1) sub=getBounds().height-mBottom;
                            else sub = (int)(y1+(double)(y2-y1)/(double)levels[j]*(i+1+0.5));
                            
                            maxH[i]=2*Math.min(sub-positions[i],positions[i]-sup);
                        } else maxH[i]=(mLeft-5);
                    }
                    
                    addYLabels(g,ay,lnames[j],maxH,positions,false,true);
                }
                
            }
            
            
        }
        endAddingLabels();
    }

}
