package org.rosuda.JClaR;

import java.awt.Component;
import org.rosuda.JRclient.RSrvException;


public abstract class DefaultClassifier implements Classifier {
    
    transient Component parent;
    
    int[] confusionMatrix;
    
    final int[] calculateConfusionMatrix(final String prediction, final String reality){
        int[] matrix = null;
        try{
            matrix = RserveConnection.evalIA("table(" + prediction + "," + reality + ")");
        } catch (RSrvException rse){
            ErrorDialog.show(parent, rse, "SVM.calculateConfusionMatrix(String,String)");
        }
        return matrix;
    }
    
    final double calculateAccuracyRate(final String prediction, final String reality,final int[] matrix) {
        double ac=0;
        try{
            final int numClassesR = RserveConnection.evalI("length(levels(" + reality + "))");
            final int numClassesP = RserveConnection.evalI("length(levels(" + prediction + "))");
            for (int i=0; i<numClassesR; i++){
                ac += matrix[i*(numClassesP+1)];
            }
            ac /= RserveConnection.evalI("length(" + prediction + ")");
        } catch (RSrvException rse){
            ErrorDialog.show(parent, rse, "SVM.calculateAccuracyRate(String,String,int[])");
        }
        return ac;
    }
    
    
    public final String getVariableName(){
        return variableName;
    }
    
    String variableName;
    
    
    String Rname;
    
    
    transient Data data;
    
    
    public final Data getData(){
        return data;
    }
    
    
    
    
    public final String getName() {
        if(data!=null){
            if(data.isRestricted()) {
                return data.getPath() + "(restricted)";
            }
            
            return data.getPath();
        } else  {
            return "null";
        }
        
    }
    
    
    public final String getRname() {
        return Rname;
    }
    
    
    private final void setParent(final Component parent){
        this.parent = parent;
    }
    
    
    transient SVMClassificationPlot plot;
    
    
    boolean trained = false;
    
    int variablePos; //which data column was selected (starts at 0)
    
    
    public final Data getClassifiedData() {
        return classificationData;
    }
    
    
    public final Plot getPlot() {
        return plot;
    }
    
    
    final boolean getTrained(){
        return trained;
    }
    
    
    public final int getVariablePos() {
        return variablePos;
    }
    
    
    public final boolean hasClassifiedData() {
        return classificationData != null;
    }
    
    
    public final boolean isReady() {
        return getTrained();
    }
    
    
    public final void reclassify() {
        classify(classificationData);
    }
    
    
    public final void setPlot(final Plot plot) {
        this.plot = (SVMClassificationPlot) plot;
    }
    
    public final boolean hasAccuracyOfPrediction() {
        return (hasClassifiedData() && classificationData.getVariables().contains(getVariableName()));
    }
    
    public final double getAccuracy(){
        return accuracy;
    }
    
    transient Data classificationData;
    
    
    double accuracy=0;
    
    public final double getAccuracyOfPrediction() {
        if(hasAccuracyOfPrediction()) return accuracyOfPrediction;
        else return -1;
    }
    
    double accuracyOfPrediction=0;
    
}
