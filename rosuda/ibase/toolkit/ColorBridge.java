//
//  ColorBridge.java
//  Klimt
//
//  Created by Simon Urbanek on Sat Mar 15 2003.
//  Copyright (c) 2003 __MyCompanyName__. All rights reserved.
//

package org.rosuda.ibase.toolkit;

import java.awt.Color;
import org.rosuda.ibase.Common;

/** ColorBridge maps integers, doubles or strings to colors */
public class ColorBridge {
    Color basicCol[];
    String name[];

    protected static ColorBridge main;
    
    public ColorBridge() {
        basicCol=new Color[128]; name=new String[128];
        basicCol[0]=new Color(255,255,255); name[0]="white";
        basicCol[1]=new Color(0,0,0);       name[1]="black";
        basicCol[2]=new Color(255,0,0);     name[2]="red";
        basicCol[3]=new Color(0,205,0);     name[3]="green";
        basicCol[4]=new Color(0,0,255);     name[4]="blue";
        basicCol[5]=new Color(0,255,255);
        basicCol[6]=new Color(255,0,255);
        basicCol[7]=new Color(255,255,0);   name[7]="yellow";
        basicCol[8]=new Color(190,190,190);
        setHCLParameters(35.0, 85.0);
    }

    public void setHCLParameters(double chroma, double luminance) {
        int i=0;
        while (i<64) {
            basicCol[i+64]=Common.getHCLcolor(((double)i)*360.0/64.0,chroma,luminance);
            i++;
        }
    }
    
    public static ColorBridge getMain() {
        if (main==null) main=new ColorBridge();
        return main;
    }
    
    public Color getColor(int id) {
        return basicCol[(id==0)?0:(((id-1)&127)+1)];
    }

    public Color getColor(double d) {
        if (d<0) d=0;
        if (d>1) d=1;
        return new Color((int)(d*255.0+0.5),(int)(d*255.0+0.5),(int)(d*255.0+0.5));
    }
    
    public Color getColor(String s) {
        int i=0;
        while (i<9) { if (name[i]!=null && s.equals(name[i])) return basicCol[i]; i++; };
        return basicCol[1];
    }
}
