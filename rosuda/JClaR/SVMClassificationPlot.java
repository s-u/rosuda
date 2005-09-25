/*
 * SVMClassificationPlot.java
 *
 * Created on 23. Mai 2005, 14:53
 */

package org.rosuda.JClaR;

import java.awt.Frame;
import org.rosuda.JRclient.RSrvException;


/**
 *
 * @author tobias
 */
public final class SVMClassificationPlot extends ContourPlot {
    
    private SVM svm;
    private boolean showDataInPlot=true;
    private boolean markSupportVectors=true;
    
    private FixVariablesDialog fixVariablesDialog;
    
    private String formulaOpt="";
    private String dataSubsetOpt="";
    private boolean success=true;
    private Frame parent;
    
    /** Creates a new instance of SVMClassificationPlot */
    public SVMClassificationPlot(final SVM svm, final Frame parent) {
        this(svm,parent,null);
    }
    
    public SVMClassificationPlot(final SVM svm, final Frame parent, final FixVariablesDialog fvd) {
        super(svm);
        this.svm=svm;
        dataSubsetOpt="dataSubset" + svm.getRname() + " <- data" + svm.getRname() + "\n"; //TODO: ändern! ist uneffizient
        
        this.parent=parent;
        if(fvd != null){
            setFixVariablesDialog(fvd);
        }
        
        try{
            rcon.voidEval("svSymbol" + svm.getRname() + " <- \"x\""); // default: \"x\"
            rcon.voidEval("dataSymbol" + svm.getRname() + " <- \"o\""); // default: \"o\"
            //rcon.voidEval("fill" + svm.getRname() + " <- TRUE");
            rcon.voidEval("grid" + svm.getRname() + " <- " + getGrid());
            rcon.voidEval("palette" + svm.getRname() + " <- terrain.colors");
        } catch (RSrvException rse){
            ErrorDialog.show(parent,rse, "SVMClassificationPlot(SVM, Frame, FixVariablesDialog)");
        }
    }
    
    public void setShowDataInPlot(final boolean showDataInPlot) {
        this.showDataInPlot = showDataInPlot;
        //createPlotCall(false);
    }
    
    
    protected void createPlotCall(){
        createPlotCall(true);
    }
    
    /**
     * Sets some variables in R and sets the R command which is used to plot the
     * svm. The R code is based on function plot.svm from R package e1071.
     *
     * @param hardChange Whether background should be calculated.
     */
    protected void createPlotCall(final boolean hardChange){
        if( svm.getData().getNumberOfVariables()-1<2){ // less than two variables
            ErrorDialog.show(parent, "Too few variables to plot.");
            setPlotCall("0");
            success=false;
        } else if ( svm.getData().getNumberOfVariables()-1==2){ // two variables
            // nothing to do?
        } else{ // more than two variables
            final FixVariablesDialog fvd = getFixVariablesDialog();
            
            formulaOpt = fvd.getFormula();
            setSliceOpt("slice" + svm.getRname() + " <- list(" + fvd.getFixedVariables() + ")");
            dataSubsetOpt = "dataSubset" + svm.getRname() + " <- subset(data" + svm.getRname() + "," + fvd.getSubsetExpression() + ")\n";
        }
        
        try{
            // create formula
            rcon.voidEval("data" + svm.getRname() + " <- " + svm.getData().getRname());
            if (!"".equals(formulaOpt)) {
                rcon.voidEval("formula" + svm.getRname() + " <- " + formulaOpt);
            } else{
                rcon.voidEval("formula" + svm.getRname() + " <- formula(delete.response(terms(" + svm.getRname() + ")))");
                rcon.voidEval("formula" + svm.getRname() + "[2:3] <- formula" + svm.getRname() + "[[2]][2:3]");
            }
            
            createSlice();
            
            rcon.voidEval(dataSubsetOpt);
            
            //TODO: make this modular, put it into ContourPlot
            if(showDataInPlot){
                // calculate subsets of the data to mark support vectors and/or misclassified points
                rcon.voidEval("subsetIndex" + svm.getRname() + " <- as.numeric(attr(dataSubset" + svm.getRname() + ",\"row.names\"))");
                if(markSupportVectors){
                    rcon.voidEval("svIndex" + svm.getRname() + " <- intersect(" + svm.getRname() + "$index,subsetIndex" + svm.getRname() + ")");
                    rcon.voidEval("nosvIndex" + svm.getRname() + " <- setdiff(subsetIndex" + svm.getRname() + ",svIndex" + svm.getRname() + ")");
                }
                if(markMisclassifiedPoints){
                    rcon.voidEval("classifiedIndex" + svm.getRname() + " <- intersect((1:length(data" + svm.getRname() + "[,1]))[(fitted(" + svm.getRname() + ")==data" + svm.getRname() + "[," + (svm.getVariablePos()+1) + "])],subsetIndex" + svm.getRname() + ")");
                    rcon.voidEval("misclassifiedIndex" + svm.getRname() + " <- setdiff(subsetIndex" + svm.getRname() + ",classifiedIndex" + svm.getRname() + ")");
                    if(markSupportVectors){
                        rcon.voidEval("misclassifiedsvIndex" + svm.getRname() + " <- intersect(misclassifiedIndex" + svm.getRname() + ",svIndex" + svm.getRname() + ")");
                        rcon.voidEval("classifiedsvIndex" + svm.getRname() + " <- intersect(classifiedIndex" + svm.getRname() + ",svIndex" + svm.getRname() + ")");
                        rcon.voidEval("misclassifiednosvIndex" + svm.getRname() + " <- intersect(misclassifiedIndex" + svm.getRname() + ",nosvIndex" + svm.getRname() + ")");
                        rcon.voidEval("classifiednosvIndex" + svm.getRname() + " <- intersect(classifiedIndex" + svm.getRname() + ",nosvIndex" + svm.getRname() + ")");
                    }
                }
            }
            
            // if a hard change is requested recalculate the background colors
            if (hardChange){
                calculateBackground();
            }
            
        } catch (RSrvException rse){
            ErrorDialog.show(parent,rse, "SVMClassificationPlot.createPlotCall()");
        }
        String bgOpt1 = ",bg = palette" + svm.getRname() + "(length(" + svm.getRname() + "$levels))[colind[";
        String bgOpt2 = svm.getRname() + "]]";
        
        //TODO: make this modular
        // draw data points
        if(showDataInPlot){
            if(markSupportVectors){ // mark support vectors
                if(markMisclassifiedPoints){ // mark misclassified points
                    setDataOpt("points(formula" + svm.getRname() + ", data = data" + svm.getRname() + "[classifiednosvIndex" + svm.getRname() + ", ], pch=21" +
                            bgOpt1 + "classifiednosvIndex" + bgOpt2 + ")\n" +
                            "points(formula" + svm.getRname() + ", data = data" + svm.getRname() + "[classifiedsvIndex" + svm.getRname() + ", ], pch = 22" +
                            bgOpt1 + "classifiedsvIndex" + bgOpt2 + ")\n"+
                            "points(formula" + svm.getRname() + ", data = data" + svm.getRname() + "[misclassifiednosvIndex" + svm.getRname() + ", ], pch=21,col=\"red\"" +
                            bgOpt1 + "misclassifiednosvIndex" + bgOpt2 + ")\n" +
                            "points(formula" + svm.getRname() + ", data = data" + svm.getRname() + "[misclassifiedsvIndex" + svm.getRname() + ", ], pch = 22,col=\"red\"" +
                            bgOpt1 + "misclassifiedsvIndex" + bgOpt2 + ")\n");
                } else { // don't mark misclassified points
                    setDataOpt("points(formula" + svm.getRname() + ", data = data" + svm.getRname() + "[nosvIndex" + svm.getRname() + ", ], pch=21" +
                            bgOpt1 + "nosvIndex" + bgOpt2 + ")\n" +
                            "points(formula" + svm.getRname() + ", data = data" + svm.getRname() + "[svIndex" + svm.getRname() + ", ], pch = 22" +
                            bgOpt1 + "svIndex" + bgOpt2 + ")\n");
                }
            } else{ // don't mark support vectors
                if(markMisclassifiedPoints){ // mark misclassified points
                    setDataOpt("points(formula" + svm.getRname() + ", data = data" + svm.getRname() + "[classifiedIndex" + svm.getRname() + ",], pch=21" +
                            bgOpt1 + "classifiedIndex" + bgOpt2 + ")\n"+
                            "points(formula" + svm.getRname() + ", data = data" + svm.getRname() + "[misclassifiedIndex" + svm.getRname() + ",], pch=21,col=\"red\"" +
                            bgOpt1 + "misclassifiedIndex" + bgOpt2 + ")\n");
                } else { // don't mark misclassified points
                    setDataOpt("points(formula" + svm.getRname() + ", data = data" + svm.getRname() + "[subsetIndex" + svm.getRname() + ",], pch=21" +
                            bgOpt1 + "subsetIndex" + bgOpt2 + ")\n");
                }
            }
        } else setDataOpt("");
        
        setPlotCall();
        
        success=true;
    }
    
    public boolean getSuccess(){
        return success;
    }
    
    public void setZoom(final double zoom){
        super.setZoom(zoom);
        createPlotCall();
    }
    
    public final boolean getShowDataInPlot(){
        return showDataInPlot;
    }
    
    public final FixVariablesDialog getFixVariablesDialog() {
        return fixVariablesDialog;
    }
    
    public final void setFixVariablesDialog(final FixVariablesDialog fixVariablesDialog) {
        this.fixVariablesDialog = fixVariablesDialog;
    }
    
    
    
    public boolean isMarkMisclassifiedPoints() {
        return this.markMisclassifiedPoints;
    }
    
    public boolean isMarkSupportVectors() {
        return this.markSupportVectors;
    }
    
    public void setMarkSupportVectors(final boolean markSupportVectors) {
        this.markSupportVectors = markSupportVectors;
    }
}
