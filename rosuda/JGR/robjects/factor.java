package org.rosuda.JGR.robjects;
//
//  factor.java
//  JGR
//
//  Created by Markus Helbig on Wed Mar 17 2004.
//  Copyright (c) 2004 __MyCompanyName__. All rights reserved.
//

import org.rosuda.JGR.*;

public class factor extends RObject {

    private int levels;

    public factor() {
        this(null,-1);
    }

    public factor(String name) {
        this(name,-1);
    }

    public factor(int levels) {
        this(null,levels);
    }

    public factor(String name, int levels, RObject parent) {
        super(name,RObject.FACTOR,parent);
        this.levels = levels;
    }

    public factor(String name, int levels) {
        this(name,levels,null);
    }

    public void setLevels(int levels) {
        this.levels = levels;
    }

    public int getLevels() {
        return levels;
    }

    public String getToolTip() {
        return RTalk.getFactorLevels(this); //(parent==null?"":(parent.getName()+"$"))+getName());
    }

    public String toString() {
        return getName() + "\t (factor \t levels: " +levels+")";
    }
}

