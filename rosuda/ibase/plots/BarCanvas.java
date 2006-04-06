package org.rosuda.ibase.plots;

import java.awt.*;
import java.awt.event.*;

import org.rosuda.ibase.*;
import org.rosuda.ibase.toolkit.*;
import org.rosuda.pograss.*;
import org.rosuda.util.*;

/** BarCanvas - implementation of the barcharts (new version - based on BaseCanvas).
 * @version $Id$
 */
public class BarCanvas extends BaseCanvas {
    static final String M_PLUS = "+";
    static final String M_SET1 = "set1";
    static final String M_SET64 = "set64";
    static final String M_RESET = "reset";
    static final String M_SPINE = "spine";
    static final String M_SORTBYCOUNT = "sortByCount";
    static final String M_SORTBYMARKED = "sortByMarked";
    static final String M_LABELS = "labels";
    static final String M_OUTLINE = "outline";
    
    /** corresponding variable */
    private SVar v;
    /** weight variable for weighted barcharts */
    private SVar weight;
    
    private int[] cat_seq;
    private String[] cat_nam;
    private int cats;
    
    private int countMax=0;
    private int count[];
    
    // for weighted barcharts
    private double c_max;
    private double cumulated[];
    private double c_marked[];
    
    private Object[] cols; // colors 0=regular sel, 1... sec marks
    private boolean hasSec; // has sec marks
    
    private int bars=20;
    private boolean isSpine=false;
    private int dragBar, dragW, dragH;
    
    private MenuItem MIspine=null;
    private MenuItem MIlabels=null;
    
    private PlotPrimitive lastQueriedPrimitive=null;
    private int lastQueriedIndex;
    
    /** creates a (weighted) barchart
     * @param f associated frame (or <code>null</code> if common default frame is to be used)
     * @param var associated variable
     * @param mark associated marker
     * @param wvar weight variable for weighted barcharts or null for normal ones; is ignored at the moment */
    public BarCanvas(final PlotComponent ppc, final Frame f, final SVar var, final SMarker mark, final SVar wvar) {
        super(ppc,f,mark);
        
        allowDragMove=true;
        
        mBottom = 20;
        mTop = 10;
        mLeft = mRight = 10;
        
        v=var; weight=wvar;
        setTitle("Barchart ("+v.getName()+")");
        v.addDepend(this);
        ax=new Axis(v,Axis.O_X,Axis.T_EqCat);
        ax.addDepend(this);
        ay=new Axis(v,Axis.O_Y,Axis.T_EqSize);
        ay.addDepend(this);
        cats=v.getNumCats();
        bars=cats;
        if (v.hasMissing()) bars++;
        
        pp = new PPrimRectangle[bars];
        updateObjects();
        
        createMenu(f,true,false,false,new String[]{
            "@SSpineplot",M_SPINE,
            "@OSort by count",M_SORTBYCOUNT,
            "!OSort by marked",M_SORTBYMARKED,
            "@LHide Labels",M_LABELS,
            "Set Colors (CB)",M_SET1,
            "Set Colors (rainbow)",M_SET64,
            "Clear Colors",M_RESET
        });
        MIspine=EzMenu.getItem(f,M_SPINE);
        MIlabels=EzMenu.getItem(f,M_LABELS);
        dontPaint=false;
    };
    
    public BarCanvas(final PlotComponent pc, final Frame f, final SVar var, final SMarker mark) { this(pc,f,var,mark,null); };
    
    public SVar getData(final int id) { return (id==0)?v:((id==1)?weight:null); }
    
    /** rebuilds bars */
    public void updateObjects() {
        countMax=0; c_max=0;
        final Object[] cts=v.getCategories();
        cat_nam=new String[cts.length+1];
        if (bars!=v.getNumCats()) {
            cats=v.getNumCats();
            bars=cats;
            if (v.hasMissing()) bars++;
            
            pp=new PPrimRectangle[bars];
        }
        cols=new Object[cats+1]; hasSec=false;
        int j=0;
        while (j<cats) {
            cat_nam[j]=cts[j].toString();
            if (m!=null)
                cols[j]=new int[m.getMaxMark()+1];
            j++;
        }
        cat_nam[j]="n/a"; // if you see this category, then somehting's wrong as getCatIndex returns -1
        if (m!=null)
            cols[j]=new int[m.getMaxMark()+1];
        count=new int[bars];
        //marked=new int[bars];
        j=0;
        while (j<v.size()) {
            int i=v.getCatIndex(j);
            if (i==-1) i=cats;
            if (m!=null) {
                final int[] cl=(int[])cols[i];
                final int k=m.get(j);
                if (k==-1) cl[0]++;
                else if (k!=0 && k<cl.length) { cl[k]++; hasSec=true; }
            }
            count[i]++;
            if (count[i]>countMax) countMax=count[i];
            j++;
        }
        ay.setValueRange(countMax);
        
        int i=0;
        final int lh=ay.getCasePos(0);
        while(i<bars) {
            pp[i]=new PPrimRectangle();
            
            int cl=ax.getCatLow(i);
            int cu=ax.getCatUp(i);
            final int cd=cu-cl;
            cu-=cd/10;
            cl+=cd/10;
            
            int ch;
            ch=ay.getCasePos(count[i]);
            if (isSpine) ch=lh+ay.gLen;
            
            if(orientation==0) ((PPrimRectangle)pp[i]).r = new Rectangle(cl,ch,cu-cl,lh-ch);
            else ((PPrimRectangle)pp[i]).r = new Rectangle(lh,cl,ch-lh,cu-cl);
            
            i++;
        }
        
        final int[] copy_of_count = new int[count.length];
        System.arraycopy(count, 0, copy_of_count, 0, count.length);
        
        for (i=0; i<v.size(); i++){
            final int b=v.getCatIndex(i);
            if (b>=0){
                if (((PPrimRectangle)pp[b]).ref == null){
                    ((PPrimRectangle)pp[b]).ref = new int[count[b]];
                }
                copy_of_count[b]--;
                ((PPrimRectangle)pp[b]).ref[copy_of_count[b]]=i;
            }
        }
    };
    
    private void sortBars(final boolean bySelected) {
        
        final int[] marked = new int[bars];
        for (int i=0; i<bars; i++){
            marked[i] = getMarked(i);
        }
        final int[] ix;
        ix=Tools.sortIntegersIndex(bySelected?marked:count);
        int i=0;
        while (i<bars-1) {
            ax.moveCat(ix[i],i);
            i++;
        }
        updateObjects();
        setUpdateRoot(0);
        repaint();
    }
    
    // this should be split into paintInit/Back/Objects/Selected/Post for better performance
    public void paintBack(final PoGraSS g) {
        if (bars==0) return;
        
        final Rectangle r=pc.getBounds();
        final int w=r.width;
        final int h=r.height;
        
        if (orientation==0){
            final int basey=h-mBottom;
            g.drawLine(mLeft,basey,w-mLeft-mRight,basey);
        } else {
            g.drawLine(mLeft,mTop,mLeft,h-mTop-mBottom);
        }
        
        if(isShowLabels()){
            labels.clear();
            
            double overlap=0; // used to handle overlapping labels
            boolean prevEmpty=true;
            for(int i=0; i<bars; i++){
                String label=null;
                final Rectangle rec = ((PPrimRectangle)pp[i]).r;
                if (orientation==0){
                    if (rec.width<g.getWidthEstimate(cat_nam[i])){ // if there is not enoug space for full category name
                        if(overlap<=0){ // if there is no label overlapping this label's space
                            final String abbrCatName = Common.getTriGraph(cat_nam[i]);
                            if(rec.width<g.getWidthEstimate(abbrCatName)+10){ // if there is not enough space for TriGraph
                                overlap=g.getWidthEstimate(abbrCatName)-rec.width+10;
                                if(prevEmpty) label=abbrCatName;
                            } else{
                                label=abbrCatName;
                                prevEmpty=false;
                            }
                        } else{
                            overlap-=rec.width;
                            prevEmpty=true;
                        }
                    } else{
                        label=cat_nam[i];
                        prevEmpty=false;
                        if(overlap>0){ // if there is a label overlapping this label's space
                            overlap-=rec.width;
                        }
                    }
                    if(label!=null){
                        labels.add((2*rec.x+rec.width)/2,h-mBottom/2,0.5,0.3,label);
                    }
                } else {
                    label=cat_nam[i];
                    if(label!=null){
                        labels.add(2,(2*rec.y+rec.height)/2,0,0.5,mLeft,label);
                    }
                }
            }
            labels.finishAdd();
        }
    };
    
    public void paintPost(final PoGraSS g){
        if(baseDrag && moveDrag) {
            final int h=pc.getBounds().height;
            final int basey=h-mBottom;
            final int pos = (orientation==0)?baseDragX2:baseDragY2;
            final int dragNew = ax.getCatByPos(pos);
            final int myX1=ax.getCatLow(dragNew);
            final int myX2=ax.getCatUp(dragNew);
            if(orientation==0){
                g.setColor(192,192,192);
                g.fillRect(baseDragX2-dragW/2,basey-dragH,dragW,dragH);
                g.setColor(M_OUTLINE);
                g.drawRect(baseDragX2-dragW/2,basey-dragH,dragW,dragH);
            } else{
                g.setColor(192,192,192);
                g.fillRect(mLeft,baseDragY2-dragH/2,dragW,dragH);
                g.setColor(M_OUTLINE);
                g.drawRect(mLeft,baseDragY2-dragH/2,dragW,dragH);
            }
            g.setColor("drag");
            final int difference;
            if(Math.abs(difference=pos-ax.getCatCenter(dragNew)) > (myX2-myX1)/4){
                final int x;
                final int w;
                if(difference>0){
                    x=ax.getCatCenter(dragNew);
                    w=2*(myX2-x);
                } else{
                    w=2*(ax.getCatCenter(dragNew)-myX1);
                    x=ax.getCatCenter(dragNew)-w;
                    
                }
                if(orientation==0) g.fillRect(x,basey,w,4);
                else g.fillRect(mLeft,x,4,w);
            } else{
                if(orientation==0) g.fillRect(myX1,basey,myX2-myX1,4);
                else g.fillRect(mLeft,myX1,4,myX2-myX1);
            }
        }
    }
    
    
    public void mousePressed(final MouseEvent ev) {
        super.mousePressed(ev);
        final int x=ev.getX();
        final int y=ev.getY();
        Common.printEvent(ev);
        
        if (Common.isMoveTrigger(ev)) {
            
            
            bars=cats;
            int i = 0;
            while (i<bars) {
                if (pp[i]!=null && pp[i].contains(x,y)) {
                    dragW=((PPrimRectangle)pp[i]).r.width; dragH=((PPrimRectangle)pp[i]).r.height;
                    if (!inQuery) pc.setCursor(Common.cur_hand);
                    break;
                }
                i++;
            }
        }// no longer testing for Common.isSelectTrigger. is this ok?
    };
    
    public void mouseReleased(final MouseEvent e) {
        if (baseDrag && moveDrag) {
            final int pos = (orientation==0)?e.getX():e.getY();
            final int dragNew = ax.getCatByPos(pos);
            final int difference;
            final int myX1=ax.getCatLow(dragNew);
            final int myX2=ax.getCatUp(dragNew);
            if(Math.abs(difference=pos-ax.getCatCenter(dragNew)) > (myX2-myX1)/4){
                int newPos=ax.getCatSeqIndex(dragNew);
                if(difference>0) newPos += 1;
                for(dragBar=0; dragBar<bars; dragBar++){
                    if(pp[dragBar]!=null && pp[dragBar].contains(baseDragX1,baseDragY1)) break;
                }
                if(dragBar<newPos) newPos -=1;
                ax.moveCat(dragBar, newPos);
            } else{
                if(orientation==0) ax.swapCats(dragNew, ax.getCatByPos(baseDragX1));
                else ax.swapCats(dragNew, ax.getCatByPos(baseDragY1));
            }
            
            baseDrag=false;
            updateObjects();
            setUpdateRoot(0);
            repaint();
        } else super.mouseReleased(e);
    }
    
    public Object run(final Object o, final String cmd) {
        super.run(o,cmd);
        
        if(M_SORTBYCOUNT.equals(cmd)) {
            sortBars(false);
        }
        if(M_SORTBYMARKED.equals(cmd)) {
            sortBars(true);
        }
        if (M_LABELS.equals(cmd)) {
            if(isShowLabels()){
                MIlabels.setLabel("Show Labels");
                setShowLabels(false);
            } else{
                MIlabels.setLabel("Hide Labels");
                setShowLabels(true);
            }
            setUpdateRoot(0);
            repaint();
        }
        if (M_SPINE.equals(cmd)) {
            if (isSpine) {
                ax.setType(Axis.T_EqCat);
                MIspine.setLabel("Spineplot");
                isSpine=false;
            } else {
                ax.setType(Axis.T_PropCat);
                MIspine.setLabel("Barchart");
                isSpine=true;
            }
            updateObjects();
            setUpdateRoot(0);
            repaint();
        }
        if (M_SET1.equals(cmd)) {
            if (pp!=null && pp.length>0) {
                int i=0;
                while (i<pp.length) {
                    final int cs[] = ((PPrimBase)pp[ax.getCatAtSeqIndex(i)]).getCaseIDs();
                    int j=0;
                    if (cs!=null)
                        while (j<cs.length)
                            m.setSec(cs[j++],i+16);
                    i++;
                }
                m.NotifyAll(new NotifyMsg(this,Common.NM_MarkerChange));
            }
        }
        if (M_SET64.equals(cmd)) {
            if (pp!=null && pp.length>0) {
                int i=0;
                while (i<pp.length) {
                    //System.out.println("set64: "+i+" (of "+pp.length+") mapped to "+ax.getCatAtSeqIndex(i)+", pp="+pp[i]);
                    final int cs[] = ((PPrimBase)pp[ax.getCatAtSeqIndex(i)]).getCaseIDs();
                    int j=0;
                    if (cs!=null)
                        while (j<cs.length)
                            m.setSec(cs[j++],64+(64*i/pp.length));
                    i++;
                }
                m.NotifyAll(new NotifyMsg(this,Common.NM_MarkerChange));
            }
        }
        if (M_RESET.equals(cmd)) {
            if (m.getSecCount()>0) {
                m.resetSec();
                m.NotifyAll(new NotifyMsg(this,Common.NM_MarkerChange));
            }
        }
        return null;
    };
    
    public String queryObject(final int i) {
        String qs="Name: "+cat_nam[i]+"\n";
        final boolean actionExtQuery = isExtQuery;
        if (actionExtQuery) {
            qs+="consists of "+count[i]+" cases ("+
                    Tools.getDisplayableValue(100.0*(count[i])/((double)v.size()),2)+
                    "% of total)\nSelected "+getMarked(i)+" cases ("+
                    Tools.getDisplayableValue(100.0*pp[i].getMarkedProportion(m, -1)  ,2)+
                    "% of this cat., " +
                    Tools.getDisplayableValue(100.0*(getMarked(i))/((double)v.size()),2)+"% of total)";
        } else {
            qs+="Selected "+getMarked(i)+" of "+count[i];
        }
        
        return qs;
    }
    
    /*
     * Returns the number of selected cases in bar.
     */
    private int getMarked(final int bar){
        return (int)((pp[bar].cases())*pp[bar].getMarkedProportion(m,-1)+0.5);
    }
    
    public void rotate(final int amount) {
        
        if (orientation==0){ // so orientation 1 afterwards
            mBottom = 10;
            mTop = 10;
            mLeft = 40;
            mRight = 10;
        } else{
            mBottom = 20;
            mTop = 10;
            mLeft = 10;
            mRight = 10;
        }
        super.rotate(amount);
    }
    
    public boolean adjustMargin(final PoGraSS g) {
        if (orientation==0) return false;
        
        int maxWidth=0;
        for(int i=0; i<cat_nam.length; i++){
            int wi=g.getWidthEstimate(cat_nam[i]);
            maxWidth = Math.max(wi,maxWidth);
        }
        maxWidth+=4;
        if(maxWidth!=mLeft && maxWidth<=pc.getBounds().width/2){
            mLeft = maxWidth;
            return true;
        }
        return false;
    }

    public String queryObject(final PlotPrimitive p) {
        if(lastQueriedPrimitive!=null && lastQueriedPrimitive==p) return queryObject(lastQueriedIndex);
        for(int i=0; i<pp.length; i++){
            if(pp[i]==p) return queryObject(i);
        }
        return super.queryObject(p);
    }
}
