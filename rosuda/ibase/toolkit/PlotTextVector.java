//
//  PlotTextVector.java
//
//  Created by Simon Urbanek on Tue June 20 2006.
//  Copyright (c) 2006 Simon Urbanek. All rights reserved.
//
//  $Id$

/** This is an andvanced version of {@link PlotText} that allows a gradual construction of text arrays one by one.
 * @version $Id$
 */

package org.rosuda.ibase.toolkit;
import java.awt.Color;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;
import org.rosuda.pograss.*;
import org.rosuda.ibase.*;

public class PlotTextVector extends PlotText {
    List addX=new ArrayList(),
	addY=new ArrayList(),
	addAx=new ArrayList(),
	addAy=new ArrayList(),
	addMaxW=new ArrayList(),
	addMaxH=new ArrayList(),
	addRot=new ArrayList(),
	addTxt=new ArrayList();

	/** create a new PlotTextVector and add it to the specified {@link PlotManager} */
    public PlotTextVector(final PlotManager p) { super(p); }
	/** create a new PlotTextVector, add it to the specified {@link PlotManager} and set coordinate system types */ 
	public PlotTextVector(final PlotManager p, int coordX, int coordY) { super(p); this.coordX=coordX; this.coordY=coordY; }
    
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
     * @param rotation angle to rotate text
     * @param text string to be displayed
     */
    public void add(final double X, final double Y, final double aX, final double aY, final int maxW, final int maxH, final String text, final double rotation){
        addX.add(new Double(X));
        addY.add(new Double(Y));
        addAx.add(new Double(aX));
        addAy.add(new Double(aY));
        addMaxW.add(new Integer(maxW));
        addMaxH.add(new Integer(maxH));
        addRot.add(new Double(rotation));
        addTxt.add(text);
    }
    
    public void add(final double X, final double Y, final double aX, final double aY, final int maxW, final String text, final double rotation){
        add(X,Y,aX,aY,maxW,-1,text,rotation);
    }
    
    public void add(final double X, final double Y, final double aX, final double aY, final int maxW, final String text){
        add(X,Y,aX,aY,maxW,-1,text,0);
    }
    
    public void add(final double X, final double Y, final double aX, final double aY, final String text){
        add(X,Y,aX,aY,-1,-1,text,0);
    }
    
    public void add(final double X, final double Y, final double aX, final double aY, final String text, final double rotation){
        add(X,Y,aX,aY,-1,-1,text,rotation);
    }
    
    public void add(final double X, final double Y, final String text){
        add(X, Y, 0.5, 0, -1, text);
    }
    
    /**
     * Puts the vectors' contents into arrays. Needs to be called after all
     * text elements have been added.
     */
    public void finishAdd(){
        if(!addTxt.isEmpty()){
            final double[] dX=x;
            final double[] dY=y;
            final double[] dAx=ax;
            final double[] dAy=ay;
            final int[] dMaxw=maxw;
            final int[] dMaxh=maxh;
            final double[] dRot=rot;
            final String[] dTxt=txt;
            
            final int oldLen=(dTxt==null)?0:dTxt.length;
            final int newLen=addTxt.size();
            
            x=new double[oldLen+newLen];
            y=new double[oldLen+newLen];
            ax=new double[oldLen+newLen];
            ay=new double[oldLen+newLen];
            maxw=new int[oldLen+newLen];
            maxh=new int[oldLen+newLen];
            rot=new double[oldLen+newLen];
            txt=new String[oldLen+newLen];
            
            if(dTxt!=null){
                System.arraycopy(dX, 0, x, 0, dX.length);
                System.arraycopy(dY, 0, y, 0, dY.length);
                System.arraycopy(dAx, 0, ax, 0, dAx.length);
                System.arraycopy(dAy, 0, ay, 0, dAy.length);
                System.arraycopy(dMaxw, 0, maxw, 0, dMaxw.length);
                System.arraycopy(dMaxh, 0, maxh, 0, dMaxh.length);
                System.arraycopy(dRot, 0, rot, 0, dRot.length);
                System.arraycopy(dTxt, 0, txt, 0, dTxt.length);
            }
            
            for(int i=0; i<newLen; i++){
                x[oldLen+i] = ((Double)addX.get(i)).doubleValue();
                y[oldLen+i] = ((Double)addY.get(i)).doubleValue();
                ax[oldLen+i] = ((Double)addAx.get(i)).doubleValue();
                ay[oldLen+i] = ((Double)addAy.get(i)).doubleValue();
                maxw[oldLen+i] = ((Integer)addMaxW.get(i)).intValue();
                maxh[oldLen+i] = ((Integer)addMaxH.get(i)).intValue();
                rot[oldLen+i] = ((Double)addRot.get(i)).intValue();
                txt[oldLen+i] = (String)addTxt.get(i);
            }
            
            addX.clear();
            addY.clear();
            addAx.clear();
            addAy.clear();
            addMaxW.clear();
            addMaxH.clear();
            addRot.clear();
            addTxt.clear();
			recalc();
        }
    }
}
