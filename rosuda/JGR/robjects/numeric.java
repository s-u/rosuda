package org.rosuda.JGR.robjects;
//
//  numeric.java
//  JGR
//
//  Created by Markus Helbig on Wed Mar 17 2004.
//  Copyright (c) 2004 __MyCompanyName__. All rights reserved.
//


public class numeric extends RObject {
    
    public numeric() {
        this(null,null);
    }
    
    public numeric(String name,RObject parent) {
        super(name,RObject.NUMERIC,parent);
    }
    
    public String getToolTip() {
        return null;
    }
    
    public String toString() {
            return getName() + "\t (numeric)";
    }

}
