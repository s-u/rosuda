/*
 * DoubleTuneVariablePanel.java
 *
 * Created on 3. April 2005, 15:38
 */

package org.rosuda.JClaR;

import javax.swing.SpinnerNumberModel;

/**
 *
 * @author tobias
 */
public final class DoubleTuneVariablePanel extends TuneVariablePanel{
    
    protected void initFields(){
        snmFrom = new SpinnerNumberModel(0.0,-Double.MAX_VALUE,Double.MAX_VALUE,0.1);
        snmTo = new SpinnerNumberModel(0.0,-Double.MAX_VALUE,Double.MAX_VALUE,0.1);
        snmSteps = new SpinnerNumberModel(4,2,Integer.MAX_VALUE,1);
        snmFix = new SpinnerNumberModel(0.0,-Double.MAX_VALUE,Double.MAX_VALUE,0.1);
    }
    
    public double getTo() {
        return snmTo.getNumber().doubleValue();
    }
    
    public double getFrom() {
        return snmFrom.getNumber().doubleValue();
    }
    
    public int getSteps() {
        return snmSteps.getNumber().intValue();
    }
    
    public double getFix(){
        return snmFix.getNumber().doubleValue();
    }
    
    public void setFromValue(final double value){
        snmFrom.setValue(new Double(value));
    }
    
    public void setFromStep(final double stepSize){
        snmFrom.setStepSize(new Double(stepSize));
    }
    
    public void setToValue(final double value){
        snmTo.setValue(new Double(value));
    }
    
    public void setToStep(final double stepSize){
        snmTo.setStepSize(new Double(stepSize));
    }
    
    public void setByValue(final double value){
        snmSteps.setValue(new Double(value));
    }
    
    public void setByMin(final double minimum){
        snmSteps.setMinimum(new Double(minimum));
    }
    
    public void setByMax(final double maximum){
        snmSteps.setMaximum(new Double(maximum));
    }
    
    public void setByStep(final double stepSize){
        snmSteps.setStepSize(new Double(stepSize));
    }
    
    public void setValue(final double value){
        snmFix.setValue(new Double(value));
        snmFrom.setValue(new Double(value));
        snmTo.setValue(new Double(value));
    }
    
    public void setMin(final double minimum){
        snmFix.setMinimum(new Double(minimum));
        snmFrom.setMinimum(new Double(minimum));
        snmTo.setMinimum(new Double(minimum));
    }
    
    public void setMax(final double maximum){
        snmFix.setMaximum(new Double(maximum));
        snmFrom.setMaximum(new Double(maximum));
        snmTo.setMaximum(new Double(maximum));
    }
    
    public void setFixStep(final double stepSize){
        snmFix.setStepSize(new Double(stepSize));
    }
    
    public void setDefaultFromToRange(){
        final Double fromValue;
        final Double toValue;
        final Double value = (Double)snmFix.getValue();
        
        final Comparable min = snmFrom.getMinimum();
        if (min!=null) {
            fromValue = new Double(Math.max(
                    ((Double)min).doubleValue(),
                    (value).doubleValue()-1
                    ));
        }
        
        else {
            fromValue = new Double(value.doubleValue()-1);
        }
        
        
        final Comparable max = snmFrom.getMaximum();
        if (max!=null) {
            toValue = new Double(Math.min(
                    ((Double)max).doubleValue(),
                    (value).doubleValue()+1
                    ));
        }
        
        else {
            toValue = new Double(value.doubleValue()+1);
        }
        
        
        snmFrom.setValue(fromValue);
        snmTo.setValue(toValue);
    }
    
}
