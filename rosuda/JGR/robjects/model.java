package org.rosuda.JGR.robjects;

//
//  model.java
//  JGR
//
//  Created by Markus Helbig on Mon Mar 22 2004.
//  Copyright (c) 2004 __MyCompanyName__. All rights reserved.
//

import java.text.*;
import org.rosuda.JGR.toolkit.*;

public class model extends RObject {

    private Double rsquared, deviance, aic, fstatistics;
    private Integer df;
    private String family;

    private String call;

    private String temp;

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
        this.rsquared = new Double(r);
    }
    public void setDeviance(double d) {
        this.deviance = new Double(d);
    }
    public void setDf(int df) {
        this.df = new Integer(df);
    }
    public void setAic(double a) {
        this.aic = new Double(a);
    }
    public void setFstat(double f) {
        this.fstatistics = new Double(f);
    }

    public void setFamily(String f) {
        this.family = f;
    }

    public Object[] getInfo() {
        DecimalFormat dformat = new DecimalFormat("#0.00");
        Object[] o = new Object[7];
        o[0] = getName();
        o[1] = this.getTypeName();
        o[2] = family;
        o[3] = df;
        o[4] = rsquared;
        o[5] = aic;
        o[6] = deviance;
        //o[6] = fstatistics;
        return o;
    }

    public String toString() {
            return getName() + "model";
    }



}
