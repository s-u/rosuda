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
    private SVMClassificationPlot(final SVM svm, final Frame parent) {
        this(svm,parent,null,false);
    }
    
    SVMClassificationPlot(final SVM svm, final Frame parent, final FixVariablesDialog fvd) {
        this(svm,parent,fvd,false);
    }
    
    SVMClassificationPlot(final SVM svm, final Frame parent, final FixVariablesDialog fvd, final boolean useClassifiedData) {
        super(svm);
        if(!useClassifiedData) ORIGDATANAME = DATANAME = svm.getData().getRname();
        else {
            final Data cd = svm.getClassifiedData();
            final int i = cd.getVariables().indexOf(svm.getVariableName());
            if (i<0) ORIGDATANAME = DATANAME = svm.getClassifiedDataFrame();
            else ORIGDATANAME = DATANAME = cd.getRname();
        }
        this.svm=svm;
        dataSubsetOpt="dataSubset" + svm.getRname() + " <- " + DATANAME + "\n"; //TODO: ändern! ist uneffizient
        
        this.parent=parent;
        if(fvd != null){
            setFixVariablesDialog(fvd);
        }
        
        try{
            RserveConnection.voidEval("svSymbol" + svm.getRname() + " <- \"x\""); // default: \"x\"
            RserveConnection.voidEval("dataSymbol" + svm.getRname() + " <- \"o\""); // default: \"o\"
            //RserveConnection.voidEval("fill" + svm.getRname() + " <- TRUE");
            RserveConnection.voidEval(GRIDNAME + " <- " + getGrid());
            RserveConnection.voidEval(PALETTENAME + " <- terrain.colors");
        } catch (RSrvException rse){
            ErrorDialog.show(parent,rse, "SVMClassificationPlot(SVM, Frame, FixVariablesDialog)");
        }
    }
    
    void setShowDataInPlot(final boolean showDataInPlot) {
        this.showDataInPlot = showDataInPlot;
        //createPlotCall(false);
    }
    
    
    void createPlotCall(){
        createPlotCall(true);
    }
    
    /**
     * Sets some variables in R and sets the R command which is used to plot the
     * svm. The R code is based on function plot.svm from R package e1071.
     *
     * @param hardChange Whether background should be calculated.
     */
    void createPlotCall(final boolean hardChange){
        if( svm.getData().getNumberOfVariables()-1<2){ // less than two variables
            ErrorDialog.show(parent, "Too few variables to plot.");
            setPlotCall("0");
            success=false;
        } else if ( svm.getData().getNumberOfVariables()-1==2){ // two variables
            // nothing to do?
        } else{ // more than two variables
            final FixVariablesDialog fvd = getFixVariablesDialog();
            
            formulaOpt = fvd.getFormula();
            setSliceOpt(SLICENAME + " <- list(" + fvd.getFixedVariables() + ")");
            dataSubsetOpt = "dataSubset" + svm.getRname() + " <- subset(" + DATANAME + "," + fvd.getSubsetExpression() + ")\n";
        }
        
        try{
            // create formula
            //RserveConnection.voidEval("" + DATANAME + " <- " + svm.getData().getRname());
            if (!"".equals(formulaOpt)) {
                RserveConnection.voidEval(FORMULANAME + " <- " + formulaOpt);
            } else{
                RserveConnection.voidEval(FORMULANAME + " <- formula(delete.response(terms(" + svm.getRname() + ")))");
                RserveConnection.voidEval(FORMULANAME + "[2:3] <- " + FORMULANAME + "[[2]][2:3]");
            }
            
            createSlice();
            
            RserveConnection.voidEval(dataSubsetOpt);
            
            //TODO: make this modular, put it into ContourPlot
            if(showDataInPlot){
                // calculate subsets of the data to mark support vectors and/or misclassified points
                RserveConnection.voidEval("subsetIndex" + svm.getRname() + " <- as.numeric(attr(dataSubset" + svm.getRname() + ",\"row.names\"))");
                if(markSupportVectors){
                    RserveConnection.voidEval("svIndex" + svm.getRname() + " <- intersect(" + svm.getRname() + "$index,subsetIndex" + svm.getRname() + ")");
                    RserveConnection.voidEval("nosvIndex" + svm.getRname() + " <- setdiff(subsetIndex" + svm.getRname() + ",svIndex" + svm.getRname() + ")");
                }
                if(markMisclassifiedPoints){
                    RserveConnection.voidEval("classifiedIndex" + svm.getRname() + " <- intersect((1:length(" + DATANAME + "[,1]))[(fitted(" + svm.getRname() + ")==" + DATANAME + "[," + (svm.getVariablePos()+1) + "])],subsetIndex" + svm.getRname() + ")");
                    RserveConnection.voidEval("misclassifiedIndex" + svm.getRname() + " <- setdiff(subsetIndex" + svm.getRname() + ",classifiedIndex" + svm.getRname() + ")");
                    if(markSupportVectors){
                        RserveConnection.voidEval("misclassifiedsvIndex" + svm.getRname() + " <- intersect(misclassifiedIndex" + svm.getRname() + ",svIndex" + svm.getRname() + ")");
                        RserveConnection.voidEval("classifiedsvIndex" + svm.getRname() + " <- intersect(classifiedIndex" + svm.getRname() + ",svIndex" + svm.getRname() + ")");
                        RserveConnection.voidEval("misclassifiednosvIndex" + svm.getRname() + " <- intersect(misclassifiedIndex" + svm.getRname() + ",nosvIndex" + svm.getRname() + ")");
                        RserveConnection.voidEval("classifiednosvIndex" + svm.getRname() + " <- intersect(classifiedIndex" + svm.getRname() + ",nosvIndex" + svm.getRname() + ")");
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
        final String bgOpt1 = ",bg = " + PALETTENAME + "(length(" + svm.getRname() + "$levels))[colind[";
        final String bgOpt2 = svm.getRname() + "]]";
        
        //TODO: make this modular
        // draw data points
        if(showDataInPlot){
            if(markSupportVectors){ // mark support vectors
                if(markMisclassifiedPoints){ // mark misclassified points
                    setDataOpt("points(" + FORMULANAME + ", data = " + DATANAME + "[classifiednosvIndex" + svm.getRname() + ", ], pch=21" +
                            bgOpt1 + "classifiednosvIndex" + bgOpt2 + ")\n" +
                            "points(" + FORMULANAME + ", data = " + DATANAME + "[classifiedsvIndex" + svm.getRname() + ", ], pch = 22" +
                            bgOpt1 + "classifiedsvIndex" + bgOpt2 + ")\n"+
                            "points(" + FORMULANAME + ", data = " + DATANAME + "[misclassifiednosvIndex" + svm.getRname() + ", ], pch=21,col=\"red\"" +
                            bgOpt1 + "misclassifiednosvIndex" + bgOpt2 + ")\n" +
                            "points(" + FORMULANAME + ", data = " + DATANAME + "[misclassifiedsvIndex" + svm.getRname() + ", ], pch = 22,col=\"red\"" +
                            bgOpt1 + "misclassifiedsvIndex" + bgOpt2 + ")\n");
                } else { // don't mark misclassified points
                    setDataOpt("points(" + FORMULANAME + ", data = " + DATANAME + "[nosvIndex" + svm.getRname() + ", ], pch=21" +
                            bgOpt1 + "nosvIndex" + bgOpt2 + ")\n" +
                            "points(" + FORMULANAME + ", data = " + DATANAME + "[svIndex" + svm.getRname() + ", ], pch = 22" +
                            bgOpt1 + "svIndex" + bgOpt2 + ")\n");
                }
            } else{ // don't mark support vectors
                if(markMisclassifiedPoints){ // mark misclassified points
                    setDataOpt("points(" + FORMULANAME + ", data = " + DATANAME + "[classifiedIndex" + svm.getRname() + ",], pch=21" +
                            bgOpt1 + "classifiedIndex" + bgOpt2 + ")\n"+
                            "points(" + FORMULANAME + ", data = " + DATANAME + "[misclassifiedIndex" + svm.getRname() + ",], pch=21,col=\"red\"" +
                            bgOpt1 + "misclassifiedIndex" + bgOpt2 + ")\n");
                } else { // don't mark misclassified points
                    setDataOpt("points(" + FORMULANAME + ", data = " + DATANAME + "[subsetIndex" + svm.getRname() + ",], pch=21" +
                            bgOpt1 + "subsetIndex" + bgOpt2 + ")\n");
                }
            }
        } else setDataOpt("");
        
        setPlotCall();
        
        success=true;
    }
    
    private boolean getSuccess(){
        return success;
    }
    
    void setZoom(final double zoom){
        super.setZoom(zoom);
        createPlotCall();
    }
    
    boolean getShowDataInPlot(){
        return showDataInPlot;
    }
    
    private FixVariablesDialog getFixVariablesDialog() {
        return fixVariablesDialog;
    }
    
    void setFixVariablesDialog(final FixVariablesDialog fixVariablesDialog) {
        this.fixVariablesDialog = fixVariablesDialog;
    }
    
    
    
    boolean isMarkMisclassifiedPoints() {
        return this.markMisclassifiedPoints;
    }
    
    boolean isMarkSupportVectors() {
        return this.markSupportVectors;
    }
    
    void setMarkSupportVectors(final boolean markSupportVectors) {
        this.markSupportVectors = markSupportVectors;
    }
}
