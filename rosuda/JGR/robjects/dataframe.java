package org.rosuda.JGR.robjects;
//
//  dataframe.java
//  JGR
//
//  Created by Markus Helbig on Wed Mar 17 2004.
//  Copyright (c) 2004 __MyCompanyName__. All rights reserved.
//

import java.util.*;

import org.rosuda.JGR.*;

public class dataframe extends RObject{

    public Vector vars;
    
    private int width,height;
    
    public dataframe() {
        this(null);
    }
    
    public dataframe(String name) {
        super(name,RObject.DATAFRAME,null);
        vars = new Vector();
    }
    
    public void add(RObject v) {
        vars.add(v);
    }
    
    public void setDim(int h, int w) {
        this.height = h;
        this.width = w;
    }
    
    public String getDim() {
        return "( "+((height==-1)?"":(height+" / "))+((width==-1)?"":(width+""))+" )";
    }
    
    public String getToolTip() {
        return null;
    }
    
    public String toString() {
        return getName() +  "\t - dim: "+height+", "+ width+"";
    }
}
