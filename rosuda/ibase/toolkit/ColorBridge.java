//
//  ColorBridge.java
//  Klimt
//
//  Created by Simon Urbanek on Sat Mar 15 2003.
//  Copyright (c) 2003 __MyCompanyName__. All rights reserved.
//

package org.rosuda.ibase.toolkit;

import java.awt.Color;

public class ColorBridge {
    Color basicCol[];
    String name[];

    public static ColorBridge main;
    
    public ColorBridge() {
        basicCol=new Color[9]; name=new String[9];
        basicCol[0]=new Color(255,255,255); name[0]="white";
        basicCol[1]=new Color(0,0,0);       name[1]="black";
        basicCol[2]=new Color(255,0,0);     name[2]="red";
        basicCol[3]=new Color(0,205,0);     name[3]="green";
        basicCol[4]=new Color(0,0,255);     name[4]="blue";
        basicCol[5]=new Color(0,255,255);
        basicCol[6]=new Color(255,0,255);
        basicCol[7]=new Color(255,255,0);
        basicCol[8]=new Color(190,190,190);
    }

    public Color getColor(int id) {
        return basicCol[(id==0)?0:(((id-1)&7)+1)];
    }

    public Color getColor(String s) {
        int i=0;
        while (i<9) { if (name[i]!=null && s.compareTo(name[i])==0) return basicCol[i]; i++; };
        return basicCol[1];
    }
}
