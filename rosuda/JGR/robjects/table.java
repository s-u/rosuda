package org.rosuda.JGR.robjects;
//
//  table.java
//  JGR
//
//  Created by Markus Helbig on Thu Mar 18 2004.
//  Copyright (c) 2004 __MyCompanyName__. All rights reserved.
//

import java.util.*;

public class table extends RObject{

    public Vector vars = new Vector();

    public table() {
        this(null);
    }
    
    public table(String name) {
        super(name,RObject.TABLE,null);
    }
    
    public void add(RObject v) {
        vars.add(v);
    }
    
    public String getToolTip() {
        return null;
    }
    
    public String toString() {
            return getName() + "\t - dim: "+vars.size();
    }

}
