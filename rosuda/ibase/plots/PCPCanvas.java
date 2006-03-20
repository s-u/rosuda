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
    
    /** create a new PCP
     * @param f associated frame (or <code>null</code> if none)
     * @param yvs list of variables
     * @param mark associated marker */
    public PCPCanvas(final PlotComponent ppc, final Frame f, final SVar[] yvs, final SMarker mark) {
        super(ppc,f, yvs,mark);
        type=TYPE_PCP;
        updateMargins();
        dontPaint=false;
    }
    
    protected void addLabelsAndTicks(PoGraSS g) {
        /* draw ticks and labels for X axis */
        {
            final int numCats=xv.getNumCats();
            final int[] valuePoss = new int[numCats];
            final String[] labs = new String[numCats];
            final Object[] categories = xv.getCategories();
            for(int i=0; i<numCats; i++){
                valuePoss[ax.getCatSeqIndex(i)] = getAxCatPos(i);
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
                final int t=getAxCatPos(xx);
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
                mTop=smallMTop;
                mLeft = bigMLeft;
                mRight = smallMRight;
        }
        super.rotate(amount);
    }
    
    protected void initFlagsAndFields() {
        super.initFlagsAndFields();
        
        useRegularPositioning=true;
        bigMLeft=bigMRight=50;
    }
};