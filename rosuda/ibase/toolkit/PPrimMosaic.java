package org.rosuda.ibase.toolkit;

import java.awt.Color;
import org.rosuda.pograss.PoGraSS;


public class PPrimMosaic extends PPrimRectangle {
    
    public String info = null;
    public boolean empty = false;
    private double obs;
    private char dir;
    private boolean censored;
    
    //get Information about this mosaic
    public String toString() {
        return info;
    }
    
    //if empty paint in red
    public void paint(PoGraSS g, int orientation) {
        if (r==null) return;
        if (col!=null)
            g.setColor(col.getRed(),col.getGreen(),col.getBlue());
        else
            g.setColor("object");
        if (empty)
            g.setColor(Color.red);
        if (!empty)
            g.fillRect(r.x,r.y,r.width,r.height);
        if (drawBorder) {
            if (!empty) g.setColor("outline");
            g.drawRect(r.x,r.y,r.width,r.height);
        }
    }

    public double getObs() {
        return obs;
    }

    public void setObs(double obs) {
        this.obs = obs;
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
}
