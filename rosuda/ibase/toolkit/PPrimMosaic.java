package org.rosuda.ibase.toolkit;

import java.awt.Color;
import org.rosuda.pograss.PoGraSS;


public class PPrimMosaic extends PPrimRectangle {
    
    public String info = null;
    private char dir;
    private boolean censored;
    private double obs;
    
    public static final int TYPE_OBSERVED = 0;
    public static final int TYPE_EXPECTED = 1;
    public static final int TYPE_SAMEBINSIZE = 2;
    public static final int TYPE_MULTIPLEBARCHARTS = 3;
    public static final int TYPE_FLUCTUATION = 4;
    
    private int type=TYPE_OBSERVED;
    private int origX,origY,fullW,fullH;
    
    //get Information about this mosaic
    public String toString() {
        return info;
    }
    
    //if empty paint in red
    public void paint(PoGraSS g, int orientation) {
        if (r==null) return;
        switch(type){
            case TYPE_OBSERVED:
            case TYPE_EXPECTED:
                if (col!=null)
                    g.setColor(col.getRed(),col.getGreen(),col.getBlue());
                else
                    g.setColor("object");
                if (isEmpty())
                    g.setColor(Color.red);
                if (!isEmpty())
                    g.fillRect(r.x,r.y,r.width,r.height);
                if (!isEmpty()) g.setColor("outline");
                if (drawBorder)
                    g.drawRect(r.x,r.y,r.width,r.height);
                break;
            case TYPE_SAMEBINSIZE:
            case TYPE_FLUCTUATION:
            case TYPE_MULTIPLEBARCHARTS:
                g.setColor(Color.lightGray);
                g.fillRect(origX,origY, fullW,fullH);
                g.drawRect(origX,origY, fullW,fullH);
                if(!isEmpty()){
                    g.setColor("object");
                    g.fillRect(r.x,r.y,r.width,r.height);
                    if(censored)
                        g.setColor(Color.red);
                    else
                        g.setColor("outline");
                    g.drawRect(r.x,r.y,r.width,r.height);
                }
        }
    }
    
    public void setObs(double obs){
        this.obs = obs;
    }
    
    public double getObs() {
        return obs;
    }
    
    public char getDir() {
        return dir;
    }
    
    public void setDir(char dir) {
        this.dir = dir;
    }
    
    public boolean isCensored() {
        return censored;
    }
    
    public void setCensored(boolean censored) {
        this.censored = censored;
    }
    
    public void setType(int type) {
        this.type = type;
    }
    
    public boolean isEmpty() {
        return ref.length==0;
    }
    
    public void changeDimension(int newWidth, int newHeight){
        fullW = r.width;
        fullH = r.height;
        origX = r.x;
        origY = r.y;
        r.y += fullH - newHeight;
        r.width = newWidth;
        r.height = newHeight;
    }
}
