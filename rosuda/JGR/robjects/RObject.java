package org.rosuda.JGR.robjects;

//
//  RObject.java
//  JGR
//
//  Created by Markus Helbig on Wed Mar 17 2004.
//  Copyright (c) 2004 __MyCompanyName__. All rights reserved.
//

import org.rosuda.JGR.*;

public abstract class RObject {

    public static int UNKNOWN = 0;
    public static int NUMERIC = 1;
    public static int FACTOR  = 2;
    public static int DATAFRAME = 3;
    public static int TABLE = 4;
    public static int TABLEVAR = 5;
    public static int LM = 6;
    public static int GLM = 7;
    public static int ANOVA = 8;
    public static int MATRIX = 9;
    public static int LIST = 10;
    public static int OTHER = 50;


    private String name;
    private String ClassName;
    private int type = 0;
    public RObject parent;


    public RObject(String name, String c, RObject parent) {
        this(name,OTHER,parent);
        ClassName = c;
    }


    public RObject(String name, int type, RObject parent) {
        this.name = name;
        this.type = type;
        this.parent = parent;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setClassName(String c) {
        this.ClassName = c;
    }

    public String getClassName() {
        return ClassName;
    }

    public int getType() {
        return type;
    }

    public void setParent(RObject p) {
        this.parent = p;
    }

    public RObject getParent() {
        return parent;
    }

    public abstract String getToolTip();

    public String getSummary() {
        return RController.getSummary(this);
    }

    public String toString() {
        return name +"\t ("+ClassName+")";
    }
}
