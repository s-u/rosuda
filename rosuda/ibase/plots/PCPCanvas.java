package org.rosuda.ibase.plots;

import java.awt.*;
import java.awt.event.*;

import org.rosuda.ibase.*;
import org.rosuda.ibase.toolkit.*;
import org.rosuda.pograss.*;
import org.rosuda.util.Tools;

/** implementation of line plot
 * @version $Id$
 */
public class PCPCanvas extends ParallelAxesCanvas {
    
    private int mouseX,mouseY;
    
    /** create a new PCP
     * @param f associated frame (or <code>null</code> if none)
     * @param yvs list of variables
     * @param mark associated marker */
    public PCPCanvas(final PlotComponent ppc, final Frame f, final SVar[] yvs, final SMarker mark) {
        super(ppc,f, yvs,mark);
        
        bigMLeft=bigMRight=50;
        updateMargins();
        
        dontPaint=false;
    }
    
    public void paintBack(final PoGraSS g){
        if (drawAxes) {
            g.setColor(C_WHITE);
            g.setLineWidth(1.5f);
            int xx=0;
            while (xx<xv.getNumCats()) {
                final int t=ax.getRegularCatPos(xx++, leftGap, rightGap);
                if(orientation==0)
                    g.drawLine(t,mTop,t,pc.getSize().height-mTop-mBottom);
                else
                    g.drawLine(mLeft,t,pc.getSize().width-mRight,t);
            }
            g.setLineWidth(1.0f);
        }
        super.paintBack(g);
    };
    
    public void updateObjects() {
        
        if (pp==null || pp.length!=v[0].size()) {
            pp=new PlotPrimitive[v[0].size()];
        }
        
        TW = pc.getSize().width;
        TH = pc.getSize().height;
        
        
        final int[][] xs = new int[v[0].size()][v.length];
        final int[][] ys = new int[v[0].size()][v.length];
        //boolean[] na = new boolean[v[0].size()];
        final int[][] na = new int[v[0].size()][];
        final int[] naIndices = new int[v.length+1];
        for (int i=0;i<v[0].size();i++){
            int numNAs=0;
            for (int j=0;j<v.length;j++){
                if ((drawHidden || !m.at(i)) && (v[j].at(i)!=null)) {
                    xs[i][ax.getCatSeqIndex(j)] = ax.getRegularCatPos(j, leftGap, rightGap);
                    ys[i][ax.getCatSeqIndex(j)] = ((commonScale||j==0)?ay:opAy[j-1]).getValuePos(v[j].atD(i));
                } else{
                    xs[i][ax.getCatSeqIndex(j)] = ax.getRegularCatPos(j, leftGap, rightGap);
                    ys[i][ax.getCatSeqIndex(j)] = ((commonScale||j==0)?ay:opAy[j-1]).getValuePos(v[j].atD(i));
                    naIndices[numNAs++] = j;
                }
            }
            if(numNAs>0){
                na[i] = new int[numNAs];
                System.arraycopy(naIndices, 0, na[i], 0, numNAs);
            }
        }
        
        for(int j=0; j<xs.length; j++){
            pp[j] = new PPrimPolygon();
            if(orientation==0) ((PPrimPolygon)pp[j]).pg = new Polygon(xs[j], ys[j], xs[j].length);
            else               ((PPrimPolygon)pp[j]).pg = new Polygon(ys[j], xs[j], xs[j].length);
            ((PPrimPolygon)pp[j]).closed=false;
            ((PPrimPolygon)pp[j]).fill=false;
            ((PPrimPolygon)pp[j]).selectByCorners=!drawLines;
            ((PPrimPolygon)pp[j]).drawCorners = drawPoints;
            ((PPrimPolygon)pp[j]).ref = new int[] {j};
            ((PPrimPolygon)pp[j]).setNodeSize(nodeSize);
            ((PPrimPolygon)pp[j]).drawBorder=drawLines;
            ((PPrimPolygon)pp[j]).showInvisibleLines=drawNAlines;
            final boolean[] nas = new boolean[xs[j].length];
            final boolean[] gap = new boolean[xs[j].length];
            
            if(na[j]!=null){
                final boolean[] nod = new boolean[xs[j].length];
                for(int i=0; i<na[j].length; i++) {
                    nas[na[j][i]]=true;
                    if(na[j][i]>0) nas[na[j][i]-1]=true;
                    nod[na[j][i]]=true;
                }
                ((PPrimPolygon)pp[j]).noDotsAt = nod;
                for(int i=0; i<na[j].length-1; i++){
                    if(na[j][i+1]-na[j][i]==2) gap[na[j][i]+1]=true;
                }
                if(na[j][0]==1) gap[0]=true;
                if(na[j][na[j].length-1]==gap.length-2) gap[gap.length-1]=true;
            }
            ((PPrimPolygon)pp[j]).invisibleLines=nas;
            ((PPrimPolygon)pp[j]).gapDots=gap;
        }
    }
    
    
    
    public void mouseReleased(final MouseEvent e) {
        if (baseDrag && moveDrag) {
            final int pos = (orientation==0)?e.getX():e.getY();
            final int dragNew = ax.getCatByPos(pos);
            final int dragAxis = ax.getCatByPos(baseDragX1);
            final int difference;
            final int myX1=ax.getCatLow(dragNew);
            final int myX2=ax.getCatUp(dragNew);
            if(Math.abs(difference=pos-ax.getRegularCatPos(dragNew, leftGap, rightGap)) > (myX2-myX1)/4){
                int newPos=ax.getCatSeqIndex(dragNew);
                if(difference>0) newPos += 1;
                if(dragAxis<newPos) newPos -=1;
                ax.moveCat(dragAxis, newPos);
            } else{
                if(orientation==0) ax.swapCats(dragNew, ax.getCatByPos(baseDragX1));
                else ax.swapCats(dragNew, dragAxis);
            }
            
            baseDrag=false;
            updateObjects();
            setUpdateRoot(0);
            repaint();
        } else super.mouseReleased(e);
    }
    
    public void paintPost(final PoGraSS g) {
        if(baseDrag && moveDrag){
            final int basey=pc.getBounds().height-mBottom;
            final int pos = (orientation==0)?baseDragX2:baseDragY2;
            final int dragNew = ax.getCatByPos(pos);
            final int myX1=ax.getCatLow(dragNew);
            final int myX2=ax.getCatUp(dragNew);
            final int difference;
            if(Math.abs(difference=pos-ax.getRegularCatPos(dragNew, leftGap, rightGap)) > (myX2-myX1)/4){
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
        super.paintPost(g);
    }
    
    
    
    public String queryObject(PlotPrimitive p) {
        
        String retValue="";
        
        if(isExtQuery){
            for(int i=0; i<v.length; i++){
                retValue += v[i].getName() + ": ";
                if(v[i].isCat()){
                    retValue += v[i].getCatAt((int)((commonScale||i==0)?ay:opAy[i-1]).getValueForPos(((PPrimPolygon)p).pg.ypoints[i])) + "\n";
                } else{
                    retValue += Tools.getDisplayableValue(
                            ((commonScale||i==0)?ay:opAy[i-1]).getValueForPos(((PPrimPolygon)p).pg.ypoints[i])) + "\n";
                }
                
            }
        } else{
            int c = ax.getCatByPos((orientation==0)?mouseX:mouseY);
            int i = ax.getCatSeqIndex(c);
            retValue += v[c].getName() + ": ";
            if(v[c].isCat()){
                retValue += v[c].getCatAt((int)((commonScale||i==0)?ay:opAy[i-1]).getValueForPos(((PPrimPolygon)p).pg.ypoints[i])) + "\n";
            } else{
                retValue += Tools.getDisplayableValue(
                        ((commonScale||c==0)?ay:opAy[c-1]).getValueForPos(((PPrimPolygon)p).pg.ypoints[i]));
            }
        }
        
        return retValue;
    }
    
    protected String getShortClassName() {
        return "PCP";
    }
    
    public boolean adjustMargin(final PoGraSS g) {
        if(orientation==0 && commonScale){
            /* determine maximal label length */
            int maxWidth=0;
            final double f=ay.getSensibleTickDistance(50,18);
            double fi=ay.getSensibleTickStart(f);
            while (fi<ay.vBegin+ay.vLen) {
                final String s=v[0].isCat()?Common.getTriGraph(v[0].getCatAt((int)fi).toString()):ay.getDisplayableValue(fi);
                int wi = g.getWidthEstimate(s);
                if(wi>maxWidth) maxWidth=wi;
                fi+=f;
            }
            return adjustMargin(maxWidth);
        }
        if(orientation==1){
            int maxWidth=0;
            final Object[] categories = xv.getCategories();
            for(int i=0; i<xv.getNumCats(); i++){
                final String s = xv.isCat()?((useX3)?Common.getTriGraph(categories[i].toString()):
                    categories[i].toString()):categories[i].toString();
                int wi = g.getWidthEstimate(s);
                if(wi>maxWidth) maxWidth=wi;
            }
            return adjustMargin(maxWidth);
        }
        return false;
    }
    
    protected boolean getValid() {
        return (TW>=50&&TH>=50);
    }
    
    protected void addLabelsAndTicks(PoGraSS g) {
        /* draw ticks and labels for X axis */
        {
            final double f=(orientation==0)?(ax.getSensibleTickDistance(50,26)):(ax.getSensibleTickDistance(30,18));
            double fi=ax.getSensibleTickStart(f);
            
            final int numCats=xv.getNumCats();
            final int[] valuePoss = new int[numCats];
            final String[] labs = new String[numCats];
            final Object[] categories = xv.getCategories();
            for(int i=0; i<numCats; i++){
                valuePoss[ax.getCatSeqIndex(i)] = ax.getRegularCatPos(i,leftGap,rightGap);
                labs[ax.getCatSeqIndex(i)] = xv.isCat()?((useX3)?Common.getTriGraph(categories[i].toString()):
                    categories[i].toString()):categories[i].toString();
            }
            
            for(int i=0; i<valuePoss.length; i++) {
                if (isShowLabels() && labs[i]!=null){
                    
                    if(orientation==0){
                        final boolean bottom = (i&1)==0;
                        int maxWidth=-1;
                        if(i==0){
                            if(valuePoss.length>1) maxWidth=valuePoss[1]-valuePoss[0];
                        } else if (i==valuePoss.length-1){
                            if(i>0) maxWidth=valuePoss[i]-valuePoss[i-1];
                        } else{
                            if(i+1<valuePoss.length && i-1>=0) maxWidth=valuePoss[i+1]-valuePoss[i-1];
                        }
                        
                        labels.add(valuePoss[i],
                                bottom?(H-mBottom+2):(mTop-5),
                                (i==0)?0:
                                    ((i==valuePoss.length-1)?1:
                                        0.5),
                                bottom?1:0,
                                maxWidth,
                                labs[i]);
                    } else
                        labels.add(mLeft-4,
                                valuePoss[i],
                                1,
                                0.5,
                                mLeft-4,
                                labs[i]);
                }
            }
            final int b = (orientation==0)?(pc.getSize().height-mBottom):(pc.getSize().width-mRight);
            
            int xx=0;
            while (xx<xv.getNumCats()) {
                final int t=ax.getRegularCatPos(xx, leftGap, rightGap);
                if(orientation==0){
                    if((ax.getCatSeqIndex(xx)&1)==0) g.drawLine(t,b,t,b+2);
                    else g.drawLine(t,mTop,t,mTop-2);
                } else{
                    g.drawLine(mLeft,t,mLeft-2,t);
                }
                xx++;
            }
        }
        
        /* draw ticks and labels for Y axis */
        if (commonScale) {
            final double f=ay.getSensibleTickDistance(50,18);
            double fi=ay.getSensibleTickStart(f);
            while (fi<ay.vBegin+ay.vLen) {
                final int t=ay.getValuePos(fi);
                if(orientation==0){
                    g.drawLine(mLeft-2,t,mLeft,t);
                    if(isShowLabels())
                        labels.add(mLeft-2,(t+5),1,0, v[0].isCat()?Common.getTriGraph(v[0].getCatAt((int)fi).toString()):ay.getDisplayableValue(fi));
                }else{
                    g.drawLine(t,pc.getHeight()-mBottom,t,pc.getHeight()-mBottom+2);
                    if(isShowLabels())
                        labels.add(t,pc.getHeight()-mBottom+2,0.5,1, v[0].isCat()?Common.getTriGraph(v[0].getCatAt((int)fi).toString()):ay.getDisplayableValue(fi));
                }
                fi+=f;
            }
            if(orientation==0)
                g.drawLine(mLeft, mTop, mLeft, pc.getSize().height-mBottom);
            else
                g.drawLine(mLeft, pc.getHeight()-mBottom, pc.getWidth()-mRight,pc.getHeight()-mBottom);
        }
    }
    
    public void mouseMoved(final MouseEvent ev) {
        super.mouseMoved(ev);
        if (Common.isQueryTrigger(ev)) {
            mouseX=ev.getX();
            mouseY=ev.getY();
        }
    }
    
    public void rotate(final int amount) {
        switch((orientation+amount)&1){
            case 0:
                mBottom=bigMBottom;
                mTop=bigMTop;
                mLeft=smallMLeft;
                mRight=smallMRight;
                break;
            case 1:
                mBottom=commonScale?bigMBottom:smallMBottom;
                mTop=smallMBottom;
                mLeft = bigMLeft;
                mRight = smallMRight;
        }
        super.rotate(amount);
    }
    
    protected void updateMargins() {
        switch(orientation){
            case 0:
                mBottom=defaultMBottom=bigMBottom;
                mTop=defaultMTop=bigMTop;
                mLeft=defaultMLeft=smallMLeft;
                mRight=defaultMRight=smallMRight;
                break;
            case 1:
                mBottom=defaultMBottom=commonScale?bigMBottom:smallMBottom;
                mTop=defaultMTop=smallMTop;
                mLeft=defaultMLeft=bigMLeft;
                mRight=defaultMRight=smallMRight;
        }
    }
};