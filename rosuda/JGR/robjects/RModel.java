package org.rosuda.JGR.robjects;

import java.text.*;
import java.util.*;

/**
 *  RModel - This is a simple java-representation of a model in R (currently only lm and glm are implemented) providing several information.
 * 
 *	@author Markus Helbig
 * 	
 *  RoSuDA 2003 - 2005 
 */

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

    /**
     * Get the type of the model, like lm, glm.
     * @return r-class of model
     */
    public String getTypeName() {
    	return type;
    }
    
    /**
     * Get the name of the model.
     * @return  name of model
     */
    public String getName() {
    	return name;
    }

    /**
     * Set the call statement from R concerning the model.
     * @param call call-statement
     */
    public void setCall(String call) {
        this.call = call;
    }

    /**
     * Get call-statement of the model.
     * @return call-statement
     */
    public String getCall() {
        return call;
    }
    
    /**
     * Get tooltip-text implementation, shows the call statement.
     * @return tooltip-text
     */
    public String getToolTip() {
    	return "<html><pre>"+call+"</pre></html>";
    }

    /**
     * Set r-square value of the model.
     * @param r r-square value
     */
    public void setRsquared(double r) {
        this.rsquared = new Double(dformat.format(r).replace(',','.'));
    }
    
    /**
     * Set deviance value of model.
     * @param d deviance value
     */
    public void setDeviance(double d) {
        this.deviance = new Double(dformat.format(d).replace(',','.'));
    }
    
    /**
     * Set degree of freedom value of model.
     * @param df degree of freedom value
     */
    public void setDf(int df) {
        this.df = new Integer(df);
    }
    
    /**
     * Set AIC value of model.
     * @param a AIC value
     */
    public void setAic(double a) {
        this.aic = new Double(dformat.format(a).replace(',','.'));
    }
    
    /**
     * Set F-statistic value of model.
     * @param f F-statistic value
     */
    public void setFstat(double f) {
        this.fstatistics = new Double(dformat.format(f).replace(',','.'));
    }

    /**
     * Set family name of model, needed with glm-models: binomial, poisson ....
     * @param f family name
     */
    public void setFamily(String f) {
        this.family = f;
    }

    /**
     * Set data name of model, which means the data the model is based on.
     * @param d data name
     */
    public void setData(String d) {
        this.data = d;
    }

    /**
     * Get data name of model.
     * @return data-name
     */
    public String getData() {
        return data;
    }

    /**
     * Get information about this model: name, data, type, family, degree-of-freedom, r-square, aic, deviance.
     * @return information
     */
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
