package org.rosuda.JGR.robjects;
//
//  list.java
//  JJGR
//
//  Created by Markus Helbig on Wed Mar 24 2004.
//  Copyright (c) 2004 __MyCompanyName__. All rights reserved.
//

import java.util.*;

import org.rosuda.JGR.*;

public class list extends RObject{

    public Vector vars;
    
    public int length;
    
    public list() {
        this(null,null);
    }
    
    public list(String name,RObject parent) {
        super(name,RObject.LIST,parent);
        vars = new Vector();
    }
    
    public void add(RObject v) {
        vars.add(v);
    }
    
    public void setLength(int l) {
        this.length = l;
    }
    
    public String getToolTip() {
        return null;
    }
    
    public String toString() {
        return getName() +  "\t (list - length: "+length+")";
    }
}
