package org.rosuda.JGR.robjects;

//
//  model.java
//  JGR
//
//  Created by Markus Helbig on Mon Mar 22 2004.
//  Copyright (c) 2004 __MyCompanyName__. All rights reserved.
//

import java.text.*;
import java.util.*;
import org.rosuda.JGR.toolkit.*;

public class model extends RObject {

    //private Double rsquared, deviance, aic, fstatistics;
    private double rsquared, deviance, aic, fstatistics;
    private int df;
    //private Integer df;
    private String family;

    private String call;

    private String temp;

    private String data;

    private Vector info = new Vector();

    public model() {
        this(null,-1);
    }

    public model(String name, int type) {
        super(name,type,null);
    }

    public String getTypeName() {
        if (super.getType()==RObject.LM) return "lm";
        else if (super.getType()==RObject.GLM) return "glm";
        else if (super.getType()==RObject.ANOVA) return "aov";
        return "model";
    }

    public void setCall(String call) {
        this.call = call;
    }

    public String getCall() {
        return call;
    }

    public String getToolTip() {
        return call; //"<html><font size="+Preferences.FontSize/2+">"+call+"</font></html>";
    }

    public void setRsquared(double r) {
        this.rsquared = r;
    }
    public void setDeviance(double d) {
        this.deviance = d;
    }
    public void setDf(int df) {
        this.df = df;
    }
    public void setAic(double a) {
        this.aic = a;
    }
    public void setFstat(double f) {
        this.fstatistics = f;
    }

    public void setFamily(String f) {
        this.family = f;
    }

    public void setData(String d) {
        this.data = d;
    }

    public String getData() {
        return data;
    }

    public Vector getInfo() {
        if (info.size() == 0) {
            info.add(getName());
            info.add(getData());
            info.add(getTypeName());
            info.add(family);
            info.add(new Integer(df));
            info.add(new Double(rsquared));
            info.add(new Double(aic));
            info.add(new Double(deviance));
        }
        return info;
        /*Object[] o = new Object[8];
        o[0] = getName();
        o[1] = getData();
        o[2] = this.getTypeName();
        o[3] = family;
        o[4] = df;
        o[5] = rsquared;
        o[6] = aic;
        o[7] = deviance;*/
        //o[6] = fstatistics;
        //return o;
    }

    public String toString() {
            return getName() + "model";
    }



}
