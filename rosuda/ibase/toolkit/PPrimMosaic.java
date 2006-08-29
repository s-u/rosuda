package org.rosuda.ibase.toolkit;

import java.awt.Color;
import org.rosuda.ibase.Common;
import org.rosuda.ibase.SMarker;
import org.rosuda.pograss.PoGraSS;


public class PPrimMosaic extends PPrimRectangle {
    public Color fillColorEmpty = null;
    public Color borderColorEmpty = Color.RED;
    public Color binbgColor = new Color(Color.lightGray.getRed(),Color.lightGray.getGreen(),Color.lightGray.getBlue(),76);
    public Color borderColorSelCensored = Color.RED;
    public Color borderColorSelUncensored = COL_OUTLINE;
    public Color borderColorCensored = Color.RED;
    public Color fillColorCensored = Color.RED;
    
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
    public void paint(final PoGraSS g, final int orientation, final SMarker m) {
        if (r==null) return;
        switch(type){
            case TYPE_OBSERVED:
            case TYPE_EXPECTED:
            case TYPE_SAMEBINSIZE:
                Color fillCol,borderCol;
                
                if (isEmpty()){
                    fillCol = fillColorEmpty;
                    borderCol = (drawBorder?borderColorEmpty:null);
                } else {
                    fillCol = fillColor;
                    borderCol = (drawBorder?borderColor:null);
                }
                
                if(!isBrushed(m) || isEmpty() || type==TYPE_EXPECTED){
                    drawRect(g,r,fillCol,borderCol);
                } else{
                    brushRect(g,m,orientation,r,borderCol);
                }
                break;
            case TYPE_FLUCTUATION:
            case TYPE_MULTIPLEBARCHARTS:
                drawRect(g,origX,origY,fullW,fullH,binbgColor,binbgColor);
                if(!isEmpty()){
                    if(isBrushed(m)){
                        brushRect(g,m,orientation,r,censored?borderColorCensored:borderColor);
                    } else{
                        drawRect(g,r,fillColor,censored?borderColorCensored:borderColor);
                    }
                }
        }
		/*
        if( type==TYPE_EXPECTED ) {
            final int high = (int)(192+63*(0.15+pnorm((1-p-0.9)*10)));
            final int low =  (int)(192*(0.85-pnorm((1-p-0.9)*10)));
            if( obs-exp > 0.00001 )
                g.setColor(new Color(low, low, high));
            else if( obs-exp < -0.00001 )
                g.setColor(new Color(high, low, low));
            else
                g.setColor(Color.lightGray);
            if( dir == 'x' )
                g.fillRect(r.x, r.y, (int)(r.width*Math.abs((obs-exp)/Math.sqrt(exp)*scale)), r.height);
            else if ( dir == 'y' )
                g.fillRect(r.x, r.y+r.height-(int)(r.height*Math.abs((obs-exp)/Math.sqrt(exp)*scale)),
                        r.width, (int)(r.height*Math.abs((obs-exp)/Math.sqrt(exp)*scale)));
        }*/
    }
    
    private double pnorm( final double q ) {
        
        double up=0.9999999;
        double lp=0.0000001;
        while( Math.abs( up - lp ) > 0.0001 )
            if( qnorm( (up+lp)/2 ) <= q )
                lp = (up+lp)/2;
            else
                up = (up+lp)/2;
        return up;
    }
    
    private double qnorm( final double p ) {
        
        final double a0 = 2.515517;
        final double a1 = 0.802853;
        final double a2 = 0.010328;
        
        final double b1 = 1.432788;
        final double b2 = 0.189269;
        final double b3 = 0.001308;
        
        final double  t = Math.pow(-2*Math.log(1-p), 0.5);
        
        return t - (a0 + a1*t + a2*t*t) / (1 + b1*t + b2*t*t + b3*t*t*t);
    }
    
    public void setObs(final double obs){
        this.obs = obs;
    }
    
    public double getObs() {
        return obs;
    }
    
    public char getDir() {
        return dir;
    }
    
    public void setDir(final char dir) {
        this.dir = dir;
    }
    
    public boolean isCensored() {
        return censored;
    }
    
    public void setCensored(final boolean censored) {
        this.censored = censored;
    }
    
    public void setType(final int type) {
        this.type = type;
    }
    
    public boolean isEmpty() {
        return ref.length==0;
    }
    
    public void changeDimension(final int newWidth, final int newHeight){
        fullW = r.width;
        fullH = r.height;
        origX = r.x;
        origY = r.y;
        r.y += fullH - newHeight;
        r.width = newWidth;
        r.height = newHeight;
    }
    
    public void setP(final double p) {
        this.p = p;
    }
    
    public void setExp(final double exp) {
        this.exp = exp;
    }
    
    public void setScale(final double scale) {
        this.scale = scale;
    }
    
    public boolean contains(final int x, final int y) {
        if(type == TYPE_MULTIPLEBARCHARTS || type == TYPE_FLUCTUATION)
            return (x >= origX && x <= origX+fullW && y >= origY && y <= origY+fullH);
        else if(type == TYPE_SAMEBINSIZE)
            return (x >= r.x && x <= r.x+r.width && y >= r.y && y <= r.y+r.height);
        else
            return super.contains(x, y);
    }
    
    public void paintSelected(final PoGraSS g, final int orientation, final SMarker m) {
        borderColorSel = (censored?borderColorSelCensored:borderColorSelUncensored);
        super.paintSelected(g, orientation, m);
    }
}
