package org.rosuda.JGR.robjects;
//
//  noname.java
//  JGR
//
//  Created by Markus Helbig on Wed Mar 17 2004.
//  Copyright (c) 2004 __MyCompanyName__. All rights reserved.
//

import org.rosuda.JGR.*;

public class tableVar extends RObject{
   
    private int levels = -1;
    
    public tableVar() {
        this(null,null);
    }
    
    public tableVar(String name,RObject parent) {
        super(name,RObject.TABLEVAR,parent);
    }
    
    public void setLevels(int l) {
        this.levels = l;
    }
    
    public String getToolTip() {
        return null;
    }
    
    public String toString() {
            return getName() + "\t levels : "+levels;
    }

}
