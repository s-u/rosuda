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
import java.util.Vector;
import org.rosuda.pograss.*;
import org.rosuda.ibase.*;

public class PlotText extends PlotObject {
    int x[], y[], maxw[];
    double ax[], ay[];
    String txt[];
    Rectangle txtFields[]; // the place which each text element occupies, is initialised in draw method
    boolean show=true;
    
    Vector addX=new Vector(),
            addY=new Vector(),
            addAx=new Vector(),
            addAy=new Vector(),
            addMaxW=new Vector(),
            addTxt=new Vector();
    
    public PlotText(PlotManager p) { super(p);}
    
    /* Clears all arrays */
    public void clear(){
        x=y=null;
        ax=ay=null;
        txt=null;
    }
    
    /* Adds the specified values. finishAdd needs to be called after the last call of add! */
    public void add(int X, int Y, double aX, double aY, int maxW, String text){
        addX.add(new Integer(X));
        addY.add(new Integer(Y));
        addAx.add(new Double(aX));
        addAy.add(new Double(aY));
        addMaxW.add(new Integer(maxW));
        addTxt.add(text);
    }
    
    public void add(int X, int Y, double aX, double aY, String text){
        add(X,Y,aX,aY,-1,text);
    }
    
    public void add(int X, int Y, String text){
        add(X, Y, 0.5, 0, -1, text);
    }
    
    public void finishAdd(){
        if(!addTxt.isEmpty()){
            int[] dX=x;
            int[] dY=y;
            double[] dAx=ax;
            double[] dAy=ay;
            String[] dTxt=txt;
            
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
                x[oldLen+i] = ((Integer)addX.elementAt(i)).intValue();
            for(int i=0; i<newLen; i++)
                y[oldLen+i] = ((Integer)addY.elementAt(i)).intValue();
            for(int i=0; i<newLen; i++)
                ax[oldLen+i] = ((Double)addAx.elementAt(i)).doubleValue();
            for(int i=0; i<newLen; i++)
                ay[oldLen+i] = ((Double)addAy.elementAt(i)).doubleValue();
            for(int i=0; i<newLen; i++)
                maxw[oldLen+i] = ((Integer)addMaxW.elementAt(i)).intValue();
            for(int i=0; i<newLen; i++)
                txt[oldLen+i] = (String)addTxt.elementAt(i);
            
            addX.removeAllElements();
            addY.removeAllElements();
            addAx.removeAllElements();
            addAy.removeAllElements();
            addMaxW.removeAllElements();
            addTxt.removeAllElements();
        }
    }
    
    /** The actual draw method. */
    public void draw(PoGraSS g) {
        if (!show || txt==null || txt.length==0) return;
        if (cold!=null) cold.use(g);
        if(txtFields==null || txtFields.length!=txt.length) txtFields=new Rectangle[txt.length];
        for (int i=0; i<txt.length; i++) {
            String t;
            if(maxw[i]>-1 && g.getWidthEstimate(txt[i])>maxw[i]) t=Common.getTriGraph(txt[i]);
            else t=txt[i];
            g.setColor("outline");
            g.drawString(t,x[i],y[i],ax[i],ay[i]);
            int w = g.getWidthEstimate(t);
            int h = g.getHeightEstimate(t);
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
    
    public int getTextAt(int x, int y){
        if(txtFields!=null){
            for (int i=0; i<txtFields.length; i++){
                if(txtFields[i].contains(x,y)) return i;
            }
        }
        return -1;
    }
}
