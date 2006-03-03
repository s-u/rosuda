package org.rosuda.JClaR;

import java.awt.Component;
import org.rosuda.JRclient.RSrvException;


public abstract class DefaultClassifier implements Classifier {
    
    protected transient RserveConnection rcon;
    
    protected transient Component parent;
    
    protected double calculateAccuracyRate(String prediction, String reality, int[] matrix) {
        double ac=0;
        try{
            matrix = rcon.eval("table(" + prediction + "," + reality + ")").asIntArray();
            
            final int numClassesR = rcon.eval("length(levels(" + reality + "))").asInt();
            final int numClassesP = rcon.eval("length(levels(" + prediction + "))").asInt();
            for (int i=0; i<numClassesR; i++){
                ac += matrix[i*(numClassesP+1)];
            }
            ac /= (double)rcon.eval("length(" + prediction + ")").asInt();
        } catch (RSrvException rse){
            ErrorDialog.show(parent, rse, "SVM.calculateAccuracyRate(String,String)");
        }
        return ac;
    }
    
    
    public String getVariableName(){
        return variableName;
    }
    
    protected String variableName;
    
    
    protected String Rname;
    
    
    protected transient Data data;
    
    
    public Data getData(){
        return data;
    }
    
    
    
    
    public String getName() {
        if(data!=null){
            if(data.isRestricted()) {
                return data.getPath() + "(restricted)";
            }
            
            return data.getPath();
        } else  {
            return "null";
        }
        
    }
    
    
    public String getRname() {
        return Rname;
    }
    
    
    protected final void setParent(final Component parent){
        this.parent = parent;
    }
    
    
    protected transient SVMClassificationPlot plot;
    
    
    protected boolean trained = false;
    
    protected int variablePos; //which data column was selected (starts at 0)
    
    
    public Data getClassifiedData() {
        return classificationData;
    }
    
    
    public Plot getPlot() {
        return plot;
    }
    
    
    protected final boolean getTrained(){
        return trained;
    }
    
    
    public int getVariablePos() {
        return variablePos;
    }
    
    
    public boolean hasClassifiedData() {
        return classificationData != null;
    }
    
    
    public boolean isReady() {
        return getTrained();
    }
    
    
    public void reclassify() {
        classify(classificationData);
    }
    
    
    public void setPlot(final Plot plot) {
        this.plot = (SVMClassificationPlot) plot;
    }
    
    public boolean hasAccuracyOfPrediction() {
        return (hasClassifiedData() && classificationData.getVariables().contains(getVariableName()));
    }
    
    public double getAccuracy(){
        return accuracy;
    }
    
    protected transient Data classificationData;
    
    
    double accuracy=0;
    
    public double getAccuracyOfPrediction() {
        if(hasAccuracyOfPrediction()) return accuracyOfPrediction;
        else return -1;
    }
    
    double accuracyOfPrediction=0;
    
}
