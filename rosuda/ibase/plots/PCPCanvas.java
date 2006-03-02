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
    
    private int mouseX;
    
    /** create a new PCP
     * @param f associated frame (or <code>null</code> if none)
     * @param yvs list of variables
     * @param mark associated marker */
    public PCPCanvas(final PlotComponent ppc, final Frame f, final SVar[] yvs, final SMarker mark) {
        super(ppc,f, yvs,mark);
        dontPaint=false;
    }
    
    public void paintBack(final PoGraSS g){
        if (drawAxes) {
            g.setColor(C_WHITE);
            g.setLineWidth(1.5f);
            int xx=0;
            while (xx<xv.getNumCats()) {
                final int t=ax.getRegularCatPos(xx++, leftGap, rightGap);
                g.drawLine(t,mTop,t,pc.getSize().height-mTop-mBottom);
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
            ((PPrimPolygon)pp[j]).pg = new Polygon(xs[j], ys[j], xs[j].length);
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
            ((PPrimPolygon)pp[j]).setNoInterior();
        }
    }
    
    
    
    public void mouseReleased(final MouseEvent e) {
        if (baseDrag && moveDrag) {
            final int pos = (orientation==0)?e.getX():e.getY();
            final int dragNew = ax.getCatByPos(pos);
            final int dragAxis = ax.getCatByPos(baseDragY1);
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
        
        /* This will be the extended query
         for(int i=0; i<v.length; i++){
            retValue += v[i].getName() + ": ";
            if(v[i].isCat()){
                retValue += v[i].getCatAt((int)((commonScale||i==0)?ay:opAy[i-1]).getValueForPos(((PPrimPolygon)p).pg.ypoints[i])) + "\n";
            } else{
                retValue += Tools.getDisplayableValue(
                        ((commonScale||i==0)?ay:opAy[i-1]).getValueForPos(((PPrimPolygon)p).pg.ypoints[i])) + "\n";
            }
         
        }
         **/
        
        int i = ax.getCatByPos(mouseX);
        retValue += v[i].getName() + ": ";
        if(v[i].isCat()){
            retValue += v[i].getCatAt((int)((commonScale||i==0)?ay:opAy[i-1]).getValueForPos(((PPrimPolygon)p).pg.ypoints[i])) + "\n";
        } else{
            retValue += Tools.getDisplayableValue(
                    ((commonScale||i==0)?ay:opAy[i-1]).getValueForPos(((PPrimPolygon)p).pg.ypoints[i])) + "\n";
        }
        
        return retValue;
    }
    
    protected String getShortClassName() {
        return "PCP";
    }
    
    public boolean adjustMargin() {
        if(commonScale){
            /* determine maximal label length */
            int maxLabelLength=0;
            final double f=ay.getSensibleTickDistance(50,18);
            double fi=ay.getSensibleTickStart(f);
            while (fi<ay.vBegin+ay.vLen) {
                final String s=v[0].isCat()?Common.getTriGraph(v[0].getCatAt((int)fi).toString()):ay.getDisplayableValue(fi);
                if(s.length()>maxLabelLength) maxLabelLength=s.length();
                fi+=f;
            }
            return adjustMargin(maxLabelLength);
        }
        return false;
    }
    
    protected boolean getValid() {
        return (TW>=50&&TH>=50);
    }
    
    protected void addLabelsAndTicks(PoGraSS g) {
        /* draw ticks and labels for X axis */
        {
            final double f=ax.getSensibleTickDistance(50,26);
            double fi=ax.getSensibleTickStart(f);
            
            final int[] valuePoss = new int[(int)((ax.vBegin+ax.vLen-fi)/f)+5];
            final String[] labs = new String[(int)((ax.vBegin+ax.vLen-fi)/f)+5];
            int i=0;
            while (fi<ax.vBegin+ax.vLen) {
                valuePoss[i] = ax.getValuePos(fi);
                labs[i] = xv.isCat()?((useX3)?Common.getTriGraph(xv.getCatAt((int)fi).toString()):
                    xv.getCatAt((int)fi).toString()):ax.getDisplayableValue(fi);
                fi+=f;
                i++;
            }
            
            for(i=0; i<valuePoss.length; i++) {
                if (isShowLabels() && labs[i]!=null){
                    labels.add(valuePoss[i]-5,
                            ((i&1)==0)?(H-mBottom+2):(mTop-5),
                            0.5,
                            ((i&1)==0)?1:0,
                            (i==0)?(2*(valuePoss[1]-valuePoss[0])):((i==valuePoss.length-1)?(2+(valuePoss[i]-valuePoss[i-1])):(valuePoss[i+1]-valuePoss[i-1])),
                            labs[i]);
                }
            }
            final int b = pc.getSize().height-mBottom;
            g.drawLine(mLeft, b, pc.getSize().width-mRight, b);
            //g.drawLine(mLeft, mTop, pc.getSize().width-mRight, mTop);
            
            int xx=0;
            while (xx<xv.getNumCats()) {
                final int t=ax.getRegularCatPos(xx, leftGap, rightGap);
                if((xx&1)==0) g.drawLine(t,b,t,b+2);
                else g.drawLine(t,mTop,t,mTop-2);
                xx++;
            }
        }
        
        /* draw ticks and labels for Y axis */
        if (commonScale) {
            final double f=ay.getSensibleTickDistance(50,18);
            double fi=ay.getSensibleTickStart(f);
            while (fi<ay.vBegin+ay.vLen) {
                final int t=ay.getValuePos(fi);
                g.drawLine(mLeft-2,t,mLeft,t);
                if(isShowLabels())
                    labels.add(mLeft-3,(t+5),1,0, v[0].isCat()?Common.getTriGraph(v[0].getCatAt((int)fi).toString()):ay.getDisplayableValue(fi));
                fi+=f;
            }
            g.drawLine(mLeft, mTop, mLeft, pc.getSize().height-mBottom);
        }
    }
    
    public void mouseMoved(final MouseEvent ev) {
        super.mouseMoved(ev);
        if (Common.isQueryTrigger(ev)) mouseX=ev.getX();
    }
};