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
    
    /** creates a (weighted) barchart
     * @param f associated frame (or <code>null</code> if common default frame is to be used)
     * @param var associated variable
     * @param mark associated marker
     * @param wvar weight variable for weighted barcharts or null for normal ones; is ignored at the moment */
    public BarCanvas(PlotComponent pc, Frame f, SVar var, SMarker mark, SVar wvar) {
        super(pc,f,mark);
        
        allowDragMove=true;
        
        mBottom = 20;
        mTop = 10;
        mLeft = mRight = 10;
        
        v=var; weight=wvar;
        setTitle("Barchart ("+v.getName()+")");
        ax=new Axis(v,Axis.O_X,Axis.T_EqCat);
        ax.addDepend(this);
        ay=new Axis(v,Axis.O_Y,Axis.T_EqSize);
        ay.addDepend(this);
        cats=v.getNumCats();
        bars=cats;
        if (v.hasMissing()) bars++;
        
        pp = new PPrimRectangle[bars];
        updateObjects();
        MenuBar mb=null;
        String myMenu[]={"+","File","~File.Graph","~Edit","+","View","@RRotate","rotate","Spineplot","spine","~Window","0"};
        EzMenu.getEzMenu(f,this,myMenu);
        MIspine=EzMenu.getItem(f,"spine");
    };
    
    public BarCanvas(PlotComponent pc, Frame f, SVar var, SMarker mark) { this(pc,f,var,mark,null); };
    
    public SVar getData(int id) { return (id==0)?v:((id==1)?weight:null); }
    
    /** rebuilds bars */
    public void updateObjects() {
        countMax=0; c_max=0;
        Object[] cts=v.getCategories();
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
        };
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
                int[] cl=(int[])cols[i];
                int k=m.get(j);
                if (k==-1) cl[0]++;
                else if (k!=0 && k<cl.length) { cl[k]++; hasSec=true; }
            }
            count[i]++;
            if (count[i]>countMax) countMax=count[i];
            j++;
        };
        ay.setValueRange(countMax);
        
        Rectangle r=pc.getBounds();
        int w=r.width, h=r.height;
        
        int i=0;
        int lh=ay.getCasePos(0);
        while(i<bars) {
            pp[i]=new PPrimRectangle();
            
            int cl=ax.getCatLow(i);
            int cu=ax.getCatUp(i);
            int cd=cu-cl;
            cu-=cd/10;
            cl+=cd/10;
            
            int ch=0;
            ch=ay.getCasePos(count[i]);
            if (isSpine) ch=lh+ay.gLen;
            
            if(orientation==0) ((PPrimRectangle)pp[i]).r = new Rectangle(cl,ch,cu-cl,lh-ch);
            else ((PPrimRectangle)pp[i]).r = new Rectangle(lh,cl,ch-lh,cu-cl);
            
            i++;
        };
        
        int[] copy_of_count = new int[count.length];
        System.arraycopy(count, 0, copy_of_count, 0, count.length);
        
        for (i=0; i<v.size(); i++){
            int b=v.getCatIndex(i);
            if (b>=0){
                if (((PPrimRectangle)pp[b]).ref == null){
                    ((PPrimRectangle)pp[b]).ref = new int[count[b]];
                }
                copy_of_count[b]--;
                ((PPrimRectangle)pp[b]).ref[copy_of_count[b]]=i;
            }
        }
    };
    
    private void sortBars(boolean bySelected) {
        int ix[]=null;
        int[] marked = new int[bars];
        for (int i=0; i<bars; i++){
            marked[i] = getMarked(i);
        }
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
    public void paintBack(PoGraSS g) {
        if (bars==0) return;
        
        Rectangle r=pc.getBounds();
        int w=r.width, h=r.height;
        
        if (orientation==0){
            int basey=h-mBottom;
            g.drawLine(mLeft,basey,w-mLeft-mRight,basey);
        } else {
            g.drawLine(mLeft,mTop,mLeft,h-mTop-mBottom);
        }
        
        if(showLabels){
            labels.clear();
            String[] text = new String[bars];
            double[] X = new double[bars];
            double[] Y = new double[bars];
            double[] aX = new double[bars];
            double[] aY = new double[bars];
            double overlap=0; // used to handle overlapping labels
            boolean prevEmpty=true;
            for(int i=0; i<bars; i++){
                String label=null;
                Rectangle rec = ((PPrimRectangle)pp[i]).r;
                if (orientation==0){
                    if (rec.width<g.getWidthEstimate(cat_nam[i])){ // if there is not enoug space for full category name
                        if(overlap<=0){ // if there is no label overlapping this label's space
                            String abbrCatName = Common.getTriGraph(cat_nam[i]);
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
                    if (mLeft<cat_nam[i].length()*8)
                        label=Common.getTriGraph(cat_nam[i]);
                    else
                        label=cat_nam[i];
                    if(label!=null){
                        labels.add(0,(2*rec.y+rec.height)/2,0,0.5,label);
                    }
                }
            }
            labels.finishAdd();
        }
    };
    
    public void paintPost(PoGraSS g){
        if(baseDrag && moveDrag) {
            int h=pc.getBounds().height;
            int basey=h-mBottom;
            int pos = (orientation==0)?baseDragX2:baseDragY2;
            int dragNew = ax.getCatByPos(pos);
            int myX1=ax.getCatLow(dragNew);
            int myX2=ax.getCatUp(dragNew);
            if(orientation==0){
                g.setColor(192,192,192);
                g.fillRect(baseDragX2-dragW/2,basey-dragH,dragW,dragH);
                g.setColor("outline");
                g.drawRect(baseDragX2-dragW/2,basey-dragH,dragW,dragH);
            } else{
                g.setColor(192,192,192);
                g.fillRect(mLeft,baseDragY2-dragH/2,dragW,dragH);
                g.setColor("outline");
                g.drawRect(mLeft,baseDragY2-dragH/2,dragW,dragH);
            }
            g.setColor("drag");
            int difference;
            if(Math.abs(difference=pos-ax.getCatCenter(dragNew)) > (myX2-myX1)/4){
                int x,w;
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
        };
    }
    
    
    public void mousePressed(MouseEvent ev) {
        super.mousePressed(ev);
        int x=ev.getX(), y=ev.getY();
        Common.printEvent(ev);
        
        if (Common.isMoveTrigger(ev)) {
            int i=0, bars=cats, setTo=0;
            while (i<bars) {
                if (pp[i]!=null && pp[i].contains(x,y)) {
                    dragW=((PPrimRectangle)pp[i]).r.width; dragH=((PPrimRectangle)pp[i]).r.height;
                    if (!inQuery) pc.setCursor(Common.cur_hand);
                    break;
                };
                i++;
            };
        }// no longer testing for Common.isSelectTrigger. is this ok?
    };
    
    public void mouseReleased(MouseEvent e) {
        if (baseDrag && moveDrag) {
            int pos = (orientation==0)?e.getX():e.getY();
            int dragNew = ax.getCatByPos(pos);
            int difference;
            int myX1=ax.getCatLow(dragNew);
            int myX2=ax.getCatUp(dragNew);
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
    
    public void keyTyped(KeyEvent e) {
        super.keyTyped(e);
        if (e.getKeyChar()=='o') sortBars(false);
        if (e.getKeyChar()=='O') sortBars(true);
        if (e.getKeyChar()=='s') run(this,"spine");
        if (e.getKeyChar()=='l') run(this,"labels");
        
        if (e.getKeyChar()>='0' && e.getKeyChar()<='9') {
            m.setSelected(e.getKeyChar()-'0');
            m.NotifyAll(new NotifyMsg(this,Common.NM_SecMarkerChange));
            setUpdateRoot(0);
            repaint();
        }
    }
    
    public Object run(Object o, String cmd) {
        super.run(o,cmd);
        
        if (cmd=="labels") {
            showLabels=!showLabels;
            setUpdateRoot(0);
            repaint();
        };
        if (cmd=="spine") {
            if (isSpine) {
                ax.setType(Axis.T_EqCat);
                MIspine.setLabel("Spineplot");
                isSpine=false;
            } else {
                ax.setType(Axis.T_PropCat);
                MIspine.setLabel("Barchart");
                isSpine=true;
            };
            updateObjects();
            setUpdateRoot(0);
            repaint();
        };
        return null;
    };
    
    public String queryObject(int i) {
        String qs="Name: "+cat_nam[i]+"\n";
        boolean actionExtQuery = false; //ExtQueries not implemented
        if (actionExtQuery) {
            qs+="consists of "+count[i]+" cases ("+
                    Tools.getDisplayableValue(100.0*((double)count[i])/((double)v.size()),2)+
                    "% of total)\nSelected "+getMarked(i)+" cases ("+
                    Tools.getDisplayableValue(100.0*pp[i].getMarkedProportion(m, -1)  ,2)+
                    "% of this cat., " +
                    Tools.getDisplayableValue(100.0*((double)getMarked(i))/((double)v.size()),2)+"% of total)";
        } else {
            qs+="Selected "+getMarked(i)+" of "+count[i];
        };
        
        return qs;
    }
    
    /*
     * Returns the number of selected cases in bar.
     */
    private int getMarked(int bar){
        return (int)(((double) pp[bar].cases())*pp[bar].getMarkedProportion(m,-1)+0.5);
    }
    
    public void rotate(int amount) {
        int puffer;
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
    
}
