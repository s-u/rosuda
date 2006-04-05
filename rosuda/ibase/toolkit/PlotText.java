//
//  PlotText.java
//  Klimt
//
//  Created by Simon Urbanek on Fri May 09 2003.
//  Copyright (c) 2003 __MyCompanyName__. All rights reserved.
//
//  $Id$

/** PlotText implements an array of text labels, specified by x,y,ax,ay and the text itself.
 * @version $Id$
 */

package org.rosuda.ibase.toolkit;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;
import org.rosuda.pograss.*;
import org.rosuda.ibase.*;

public class PlotText extends PlotObject {
    int x[], y[], maxw[];
    double ax[], ay[];
    String txt[];
    Rectangle txtFields[]; // the place which each text element occupies, is initialised in draw method
    boolean show=true;
    
    List addX=new ArrayList(),
            addY=new ArrayList(),
            addAx=new ArrayList(),
            addAy=new ArrayList(),
            addMaxW=new ArrayList(),
            addTxt=new ArrayList();
    
    public PlotText(final PlotManager p) { super(p);}
    
    /* Clears all arrays */
    public void clear(){
        x=y=null;
        ax=ay=null;
        txt=null;
    }
    
    /**
     * Adds the specified values. {@link #finishAdd()} needs to be called after the last
     * call of add!
     * The text specified by <CODE>text</CODE> will be displayed at the point
     * (<CODE>X</CODE>,<CODE>Y</CODE>) aligned according to <CODE>aX</CODE> and
     * <CODE>aY</CODE>. If aX/aY is set to 0 the text's left/bottom border will be
     * placed at X/Y, if set to 1 the right/top border will be placed at X/Y.
     * If maxW is nonnegative and the text's width exceeds maxW it will be
     * abbreviated.
     *
     * @param X horizontal coordinate of anchor point
     * @param Y vertical coordinate of anchor point
     * @param aX horizontal alignment
     * @param aY vertical alignment
     * @param maxW maximal width of displayed text
     * @param text string to be displayed
     */
    public void add(final int X, final int Y, final double aX, final double aY, final int maxW, final String text){
        addX.add(new Integer(X));
        addY.add(new Integer(Y));
        addAx.add(new Double(aX));
        addAy.add(new Double(aY));
        addMaxW.add(new Integer(maxW));
        addTxt.add(text);
    }
    
    public void add(final int X, final int Y, final double aX, final double aY, final String text){
        add(X,Y,aX,aY,-1,text);
    }
    
    public void add(final int X, final int Y, final String text){
        add(X, Y, 0.5, 0, -1, text);
    }
    
    /**
     * Puts the vectors' contents into arrays. Needs to be called after all
     * text elements have been added.
     */
    public void finishAdd(){
        if(!addTxt.isEmpty()){
            final int[] dX=x;
            final int[] dY=y;
            final double[] dAx=ax;
            final double[] dAy=ay;
            final String[] dTxt=txt;
            
            final int oldLen=(dTxt==null)?0:dTxt.length;
            final int newLen=addTxt.size();
            
            x=new int[oldLen+newLen];
            y=new int[oldLen+newLen];
            ax=new double[oldLen+newLen];
            ay=new double[oldLen+newLen];
            maxw=new int[oldLen+newLen];
            txt=new String[oldLen+newLen];
            
            if(dTxt!=null){
                System.arraycopy(dX, 0, x, 0, dX.length);
                System.arraycopy(dY, 0, y, 0, dY.length);
                System.arraycopy(dAx, 0, ax, 0, dAx.length);
                System.arraycopy(dAy, 0, ay, 0, dAy.length);
                System.arraycopy(dTxt, 0, txt, 0, dTxt.length);
            }
            
            for(int i=0; i<newLen; i++)
                x[oldLen+i] = ((Integer)addX.get(i)).intValue();
            for(int i=0; i<newLen; i++)
                y[oldLen+i] = ((Integer)addY.get(i)).intValue();
            for(int i=0; i<newLen; i++)
                ax[oldLen+i] = ((Double)addAx.get(i)).doubleValue();
            for(int i=0; i<newLen; i++)
                ay[oldLen+i] = ((Double)addAy.get(i)).doubleValue();
            for(int i=0; i<newLen; i++)
                maxw[oldLen+i] = ((Integer)addMaxW.get(i)).intValue();
            for(int i=0; i<newLen; i++)
                txt[oldLen+i] = (String)addTxt.get(i);
            
            addX.clear();
            addY.clear();
            addAx.clear();
            addAy.clear();
            addMaxW.clear();
            addTxt.clear();
        }
    }
    
    /** The actual draw method. */
    public void draw(final PoGraSS g) {
        if (!show || txt==null || txt.length==0) return;
        if (cold!=null) cold.use(g);
        if(txtFields==null || txtFields.length!=txt.length) txtFields=new Rectangle[txt.length];
        for (int i=0; i<txt.length; i++) {
            String t;
            if(maxw[i]>=0 && g.getWidthEstimate(txt[i])>maxw[i]) t=Common.getTriGraph(txt[i]);
            else t=txt[i];
            g.setColor("outline");
            g.drawString(t,x[i],y[i],ax[i],ay[i]);
            final int w = g.getWidthEstimate(t);
            final int h = g.getHeightEstimate(t);
            txtFields[i] = new Rectangle(x[i]-(int)(ax[i]*w+0.5),y[i]+(int)((-1+ay[i])*h+0.5), w,h);
            //g.drawRect(txtFields[i].x,txtFields[i].y, txtFields[i].x+txtFields[i].width,  txtFields[i].y+txtFields[i].height);
        }
    }
    
    public String toString() {
        if (txt==null) return "PlotText(<no text>)";
        if (x==null || y==null) return "PlotText(<coordinates incomplete>)";
        int l=txt.length; if (x.length>l) l=x.length; if (y.length>l) l=y.length;
        return "PlotText(labels="+l+",coord="+coordX+"/"+coordY+",visible="+isVisible()+")";
    }
    
    public int getTextAt(final int x, final int y){
        if(txtFields!=null){
            for (int i=0; i<txtFields.length; i++){
                if(txtFields[i].contains(x,y)) return i;
            }
        }
        return -1;
    }
}
