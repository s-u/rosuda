package org.rosuda.JGR.robjects;
//
//  unknown.java
//  JGR
//
//  Created by Markus Helbig on Wed Mar 17 2004.
//  Copyright (c) 2004 __MyCompanyName__. All rights reserved.
//

import org.rosuda.JGR.*;

public class unknown extends RObject{

    
    public unknown() {
        this(null,null);
    }
    
    public unknown(String name,RObject parent) {
        super(name,RObject.UNKNOWN,parent);
    }
    
    public String getToolTip() {
        return null;
    }    
    
    public String toString() {
            return getName() +" unknown";
    }

}
