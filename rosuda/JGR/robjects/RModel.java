package org.rosuda.JGR.robjects;

/**
 *  RModel
 * 
 * 	java-representation of a model in R
 * 
 *	@author Markus Helbig
 *  
 * 	RoSuDA 2003 - 2004 
 */

import java.text.*;
import java.util.*;

public class RModel {

    private Double rsquared, deviance, aic, fstatistics;
    
    private Integer df;
    
    private String family;

    private String call;

    private String data;

    private Vector info = new Vector();

    private DecimalFormat dformat = new DecimalFormat("#0.00");
    
    private String type = "model";
    
    private String name;

   
    public RModel(String name, String type) {
        this.name = name;
        if (type != null) this.type = type;
    }

    public String getTypeName() {
    	return type;
    }
    
    public String getName() {
    	return name;
    }

    public void setCall(String call) {
        this.call = call;
    }

    public String getCall() {
        return call;
    }
    
    public String getToolTip() {
    	return "<html><pre>"+call+"</pre></html>";
    }

    public void setRsquared(double r) {
        this.rsquared = new Double(dformat.format(r).replace(',','.'));
    }
    
    public void setDeviance(double d) {
        this.deviance = new Double(dformat.format(d).replace(',','.'));
    }
    
    public void setDf(int df) {
        this.df = new Integer(df);
    }
    
    public void setAic(double a) {
        this.aic = new Double(dformat.format(a).replace(',','.'));
    }
    
    public void setFstat(double f) {
        this.fstatistics = new Double(dformat.format(f).replace(',','.'));
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
            info.add(df);
            info.add(rsquared);
            info.add(aic);
            info.add(deviance);
        }
        return info;
    }

    public String toString() {
            return getName() + "model";
    }
}
