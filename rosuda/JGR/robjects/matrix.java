package org.rosuda.JGR.robjects;
//
//  matrix.java
//  JGR
//
//  Created by Markus Helbig on Wed Mar 24 2004.
//  Copyright (c) 2004 __MyCompanyName__. All rights reserved.
//

import java.util.*;

import org.rosuda.JGR.*;

public class matrix extends RObject{

    
    private int width,height;
    
    public matrix() {
        this(null,null);
    }
    
    public matrix(String name,RObject parent) {
        super(name,RObject.MATRIX,parent);
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
        return getName() +  "\t (matrix - dim: "+height+", "+ width+")";
    }
}
