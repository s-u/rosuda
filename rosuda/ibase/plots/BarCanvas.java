package org.rosuda.ibase.plots;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.io.*;

import org.rosuda.ibase.*;
import org.rosuda.ibase.toolkit.*;
import org.rosuda.pograss.*;
import org.rosuda.util.*;

/** BarCanvas - implementation of the barcharts (new version - based on BaseCanvas).
 * @version $Id$
 */
public class BarCanvas extends BaseCanvas {
    /** corresponding variable */
    SVar v;
    /** weight variable for weighted barcharts */
    SVar weight;
    
    int[] cat_seq;
    String[] cat_nam;
    int cats;
    
    int countMax=0;
    int count[];
    
    // for weighted barcharts
    double c_max;
    double cumulated[];
    double c_marked[];
    
    Object[] cols; // colors 0=regular sel, 1... sec marks
    boolean hasSec; // has sec marks
    
    int bars=20;
    boolean isSpine=false;
    int dragBar, dragW, dragH;
    boolean selDrag=false;
    
    MenuItem MIspine=null;
    
    /** creates a (weighted) barchart
     * @param f associated frame (or <code>null</code> if common default frame is to be used)
     * @param var associated variable
     * @param mark associated marker
     * @param wvar weight variable for weighted barcharts or null for normal ones; is ignored at the moment */
    public BarCanvas(Frame f, SVar var, SMarker mark, SVar wvar) {
        super(f,mark);
        
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
        String myMenu[]={"+","File","~File.Graph","~Edit","-","Set color by category","autoColor","Clear all colors","clearColor","+","View","Spineplot","spine","~Window","0"};
        EzMenu.getEzMenu(f,this,myMenu);
        MIspine=EzMenu.getItem(f,"spine");
    };
    
    public BarCanvas(Frame f, SVar var, SMarker mark) { this(f,var,mark,null); };
    
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
        
        Rectangle r=getBounds();
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
    
    public Dimension getMinimumSize() { return new Dimension(mLeft*2+30,mTop+mBottom+30); };
    
    public void sortBars(boolean bySelected) {
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
        
        Rectangle r=getBounds();
        int w=r.width, h=r.height;
        
        if (orientation==0){
            int basey=h-mBottom;
            g.drawLine(mLeft,basey,w-mLeft-mRight,basey);
        } else {
            g.drawLine(mLeft,mTop,mLeft,h-mTop-mBottom);
        }
        
        if(showLabels){
            for(int i=0; i<bars; i++){
                Rectangle rec = ((PPrimRectangle)pp[i]).r;
                if (orientation==0){
                    if (rec.width<cat_nam[i].length()*8)
                        g.drawString(Common.getTriGraph(cat_nam[i]),(2*rec.x+rec.width)/2,h-mBottom/2,0.5,0.3);
                    else
                        g.drawString(cat_nam[i],(2*rec.x+rec.width)/2,h-mBottom/2,0.5,0.3);
                } else {
                    if (mLeft<cat_nam[i].length()*8)
                        g.drawString(Common.getTriGraph(cat_nam[i]),0,(2*rec.y+rec.height)/2,0,0.5);
                    else
                        g.drawString(cat_nam[i],0,(2*rec.y+rec.height)/2,0,0.5);
                }
            }
        }
    };
    
    public void paintPost(PoGraSS g){
        if(baseDrag && moveDrag) {
            int h=getBounds().height;
            int basey=h-mBottom;
            int dragNew = ax.getCatByPos((orientation==0)?baseDragX2:baseDragY2);
            int myX1=ax.getCatLow(dragNew);
            int myX2=ax.getCatUp(dragNew);
            g.setColor(192,192,192);
            g.fillRect(baseDragX2-dragW/2,basey-dragH,dragW,dragH);
            g.setColor("outline");
            g.drawRect(baseDragX2-dragW/2,basey-dragH,dragW,dragH);
            g.setColor("drag");
            g.fillRect(myX1,basey,myX2-myX1,4);
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
                    if (!inQuery) setCursor(Common.cur_hand);
                    break;
                };
                i++;
            };
        }// no longer testing for Common.isSelectTrigger. is this ok?
    };
    
    public void mouseReleased(MouseEvent e) {
        if (baseDrag && moveDrag) {
            int dragNew;
            for(dragBar=0; dragBar<bars; dragBar++){
                if(pp[dragBar]!=null && pp[dragBar].contains(baseDragX1,baseDragY1)) break;
            }
            dragNew = ax.getCatByPos((orientation==0)?e.getX():e.getY());
            
            ax.moveCat(dragBar, ax.getCatSeqIndex(dragNew));
            
            baseDrag=false;
            updateObjects();
            setUpdateRoot(0);
            repaint();
        } else super.mouseReleased(e);
    }
    
    double hclCh=55.0, hclLum=75.0;
    
    public void keyTyped(KeyEvent e) {
        super.keyTyped(e);
        if (e.getKeyChar()=='o') sortBars(false);
        if (e.getKeyChar()=='O') sortBars(true);
        if (e.getKeyChar()=='R') run(this,"rotate");
        if (e.getKeyChar()=='s') run(this,"spine");
        if (e.getKeyChar()=='l') run(this,"labels");
        
        if (e.getKeyChar()>='0' && e.getKeyChar()<='9') {
            m.setSelected(e.getKeyChar()-'0');
            m.NotifyAll(new NotifyMsg(this,Common.NM_SecMarkerChange));
            setUpdateRoot(0);
            repaint();
        }
        
        if (e.getKeyChar()==',') { hclCh+=5.0; ColorBridge.getMain().setHCLParameters(hclCh, hclLum); setUpdateRoot(0); repaint(); };
        if (e.getKeyChar()=='.') { hclCh-=5.0; ColorBridge.getMain().setHCLParameters(hclCh, hclLum); setUpdateRoot(0); repaint(); };
        if (e.getKeyChar()=='<') { hclLum+=1.0; ColorBridge.getMain().setHCLParameters(hclCh, hclLum); setUpdateRoot(0); repaint(); };
        if (e.getKeyChar()=='>') { hclLum-=1.0; ColorBridge.getMain().setHCLParameters(hclCh, hclLum); setUpdateRoot(0); repaint(); };
    }
    
    public Object run(Object o, String cmd) {
        super.run(o,cmd);
        if (m!=null) m.run(o,cmd);
        if (cmd=="labels") {
            showLabels=!showLabels;
            setUpdateRoot(0);
            repaint();
        };
        if (cmd=="autoColor") {
            if (!v.isCat()) return null;
            int i=0;
            int cs=v.getNumCats();
            if (cs==0) return null;
            while (i<v.size()) {
                int c=v.getCatIndex(i);
                if (c>=0)
                    m.setSec(i,64+(c*64/cs));
                i++;
            }
            m.NotifyAll(new NotifyMsg(this,Common.NM_SecMarkerChange));
            setUpdateRoot(0);
            repaint();
        }
        if (cmd=="clearColor") {
            int i=0;
            while (i<v.size()) {
                m.setSec(i,0);
                i++;
            }
            m.NotifyAll(new NotifyMsg(this,Common.NM_SecMarkerChange));
            setUpdateRoot(0);
            repaint();
        }
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
        if (cmd=="exportCases") {
            try {
                PrintStream p=Tools.getNewOutputStreamDlg(myFrame,"Export selected cases to ...","selected.txt");
                if (p!=null) {
                    p.println(v.getName());
                    int i=0, sz=v.size();
                    while (i<sz) {
                        Object oo=v.at(i);
                        if (m.at(i)) {
                            if (oo!=null)
                                p.println(oo.toString());
                            else
                                p.println("NA");
                        }
                        i++;
                    }
                    p.close();
                }
            } catch (Exception eee) {}
        }
        if (cmd=="exit") WinTracker.current.Exit();
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
        if (orientation==0){
            puffer = mTop;
            mTop = mLeft;
            mLeft = mBottom;
            mBottom = mRight;
            mRight = puffer;
        } else{
            puffer = mTop;
            mTop = mRight;
            mRight = mBottom;
            mBottom = mLeft;
            mLeft = puffer;
        }
        super.rotate(amount);
    }
    
}
