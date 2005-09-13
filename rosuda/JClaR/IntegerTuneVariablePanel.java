/*
 * IntegerTuneVariablePanel.java
 *
 * Created on 3. April 2005, 15:35
 */

package org.rosuda.JClaR;

import javax.swing.SpinnerNumberModel;

/**
 *
 * @author tobias
 */
public final class IntegerTuneVariablePanel extends TuneVariablePanel {
    
    protected void initFields(){
        snmFrom = new SpinnerNumberModel();
        snmTo = new SpinnerNumberModel();
        snmSteps = new SpinnerNumberModel(4,2,Integer.MAX_VALUE,1);
        snmFix = new SpinnerNumberModel();
    }
    
    public int getTo() {
        return snmTo.getNumber().intValue();
    }
    
    public int getFrom() {
        return snmFrom.getNumber().intValue();
    }
    
    public int getSteps() {
        return snmSteps.getNumber().intValue();
    }
    
    public int getFix(){
        return snmFix.getNumber().intValue();
    }
    
    public void setFromValue(final int value){
        snmFrom.setValue(new Integer(value));
    }
    
    public void setFromStep(final int stepSize){
        snmFrom.setStepSize(new Integer(stepSize));
    }
    
    public void setToValue(final int value){
        snmTo.setValue(new Integer(value));
    }
    
    public void setToStep(final int stepSize){
        snmTo.setStepSize(new Integer(stepSize));
    }
    
    public void setByValue(final int value){
        snmSteps.setValue(new Integer(value));
    }
    
    public void setByMin(final int minimum){
        snmSteps.setMinimum(new Integer(minimum));
    }
    
    public void setByMax(final int maximum){
        snmSteps.setMaximum(new Integer(maximum));
    }
    
    public void setByStep(final int stepSize){
        snmSteps.setStepSize(new Integer(stepSize));
    }
    
    public void setValue(final int value){
        snmFix.setValue(new Integer(value));
    }
    
    public void setMin(final int minimum){
        snmFix.setMinimum(new Integer(minimum));
        snmFrom.setMinimum(new Integer(minimum));
        snmTo.setMinimum(new Integer(minimum));
    }
    
    public void setMax(final int maximum){
        snmFix.setMaximum(new Integer(maximum));
        snmFrom.setMaximum(new Integer(maximum));
        snmTo.setMaximum(new Integer(maximum));
    }
    
    public void setFixStep(final int stepSize){
        snmFix.setStepSize(new Integer(stepSize));
    }
    
    public void setDefaultFromToRange(){
        final Integer fromValue;
        final Integer toValue;
        final Integer value = (Integer)snmFix.getValue();
        
        final Comparable min = snmFrom.getMinimum();
        if (min!=null) {
            fromValue = new Integer(Math.max(
                    ((Integer)min).intValue(),
                    (value).intValue()-2
                    ));
        }
        
        else {
            fromValue = new Integer(value.intValue()-2);
        }
        
        
        final Comparable max = snmFrom.getMaximum();
        if (max!=null) {
            toValue = new Integer(Math.min(
                    ((Integer)max).intValue(),
                    (value).intValue()+2
                    ));
        }
        
        else {
            toValue = new Integer(value.intValue()+2);
        }
        
        
        snmFrom.setValue(fromValue);
        snmTo.setValue(toValue);
    }
}
