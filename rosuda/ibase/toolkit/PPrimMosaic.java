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
    
    private double p;
    private double exp;
    private double scale;
    
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
        if( type==TYPE_EXPECTED ) {
            int high = (int)(192+63*(0.15+pnorm((1-p-0.9)*10)));
            int low =  (int)(192*(0.85-pnorm((1-p-0.9)*10)));
            if( obs-exp > 0.00001 )
                g.setColor(new Color(low, low, high));
            else if( obs-exp < -0.00001 )
                g.setColor(new Color(high, low, low));
            else
                g.setColor(Color.lightGray);
            if( dir == 'x' )
                g.fillRect(r.x, r.y, (int)((double)r.width*Math.abs((obs-exp)/Math.sqrt(exp)*scale)), r.height);
            else if ( dir == 'y' )
                g.fillRect(r.x, r.y+r.height-(int)((double)r.height*Math.abs((obs-exp)/Math.sqrt(exp)*scale)),
                        r.width, (int)((double)r.height*Math.abs((obs-exp)/Math.sqrt(exp)*scale)));
        }
    }
    
    private double pnorm( double q ) {
        
        double up=0.9999999;
        double lp=0.0000001;
        while( Math.abs( up - lp ) > 0.0001 )
            if( qnorm( (up+lp)/2 ) <= q )
                lp = (up+lp)/2;
            else
                up = (up+lp)/2;
        return up;
    }
    
    private double qnorm( double p ) {
        
        double a0 = 2.515517;
        double a1 = 0.802853;
        double a2 = 0.010328;
        
        double b1 = 1.432788;
        double b2 = 0.189269;
        double b3 = 0.001308;
        
        double  t = Math.pow(-2*Math.log(1-p), 0.5);
        
        return t - (a0 + a1*t + a2*t*t) / (1 + b1*t + b2*t*t + b3*t*t*t);
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

    public void setP(double p) {
        this.p = p;
    }

    public void setExp(double exp) {
        this.exp = exp;
    }

    public void setScale(double scale) {
        this.scale = scale;
    }
}
