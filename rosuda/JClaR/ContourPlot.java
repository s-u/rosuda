/*
 * ContourPlot.java
 *
 * Created on 28. August 2005, 19:20
 *
 */

package org.rosuda.JClaR;
import org.rosuda.JRclient.RSrvException;

/**
 *
 * @author tobias
 */
public abstract class ContourPlot extends Plot {
    
    private int grid;
    private String sliceOpt="";
    private String dataOpt="";
    
    boolean markMisclassifiedPoints=false;
    private double horizontalShift=0;
    private double verticalShift=0;
    
    ContourPlot(Classifier cl){
        super();
        setClassifier(cl);
        setGrid(50);
    }
    
    int getGrid() {
        return grid;
    }
    
    void setGrid(int grid) {
        this.grid = grid;
        RserveConnection rcon = RserveConnection.getRconnection();
        try{
            rcon.voidEval("grid" + getClassifier().getRname() + " <- " + grid);
        } catch (RSrvException rse){
            ErrorDialog.show(parent,rse, "setGrid(int)");
        }
    }
    
    void setMarkMisclassifiedPoints(final boolean markMisclassifiedPoints) {
        this.markMisclassifiedPoints = markMisclassifiedPoints;
    }
    
    
    protected void setHorizontalShift(final double shift) {
        horizontalShift = shift;
    }
    
    protected void setVerticalShift(final double shift) {
        verticalShift = shift;
    }
    
    /**
     * Calculates predictions on grid points.
     *
     * Assumes that there is an R function 'predict' that takes the classifier
     * as first argument and the grid as second. Expects that R variables
     * 'formula...' and 'slice...' are set.
     *
     * The R code is based on function plot.svm from R package e1071.
     */
    void calculateBackground(){
        String clRname = getClassifier().getRname();
        try{
            rcon.voidEval("if (is.null(formula" + clRname + "))\n" +
                    "stop(\"missing formula.\")");
            rcon.voidEval("sub" + clRname + " <- model.frame(formula" + clRname + ", data" + clRname + ")");
            rcon.voidEval("zoom" + clRname + " <- " + zoom);
            rcon.voidEval("horShift" + clRname + " <- " + horizontalShift);
            rcon.voidEval("verShift" + clRname + " <- " + verticalShift);
            if(zoom<=1){
                rcon.voidEval("xr" + clRname + " <- seq((1/2-horShift" + clRname + "-1/(2*zoom" + clRname + "))*max(sub" + clRname + "[, 2])+(1/2+horShift" + clRname + "+1/(2*zoom" + clRname + "))*min(sub" + clRname + "[, 2]), (1/2-horShift" + clRname + "+1/(2*zoom" + clRname + "))*max(sub" + clRname + "[, 2])+(1/2+horShift" + clRname + "-1/(2*zoom" + clRname + "))*min(sub" + clRname + "[, 2]), length = grid" + clRname + ")");
                rcon.voidEval("yr" + clRname + " <- seq((1/2-verShift" + clRname + "-1/(2*zoom" + clRname + "))*max(sub" + clRname + "[, 1])+(1/2+verShift" + clRname + "+1/(2*zoom" + clRname + "))*min(sub" + clRname + "[, 1]), (1/2-verShift" + clRname + "+1/(2*zoom" + clRname + "))*max(sub" + clRname + "[, 1])+(1/2+verShift" + clRname + "-1/(2*zoom" + clRname + "))*min(sub" + clRname + "[, 1]), length = grid" + clRname + ")");
            } else{
                rcon.voidEval("xr" + clRname + " <- seq((1/2-horShift" + clRname + "-1/(2*zoom" + clRname + "))*max(sub" + clRname + "[, 2])+(1/2+horShift" + clRname + "+1/(2*zoom" + clRname + "))*min(sub" + clRname + "[, 2]), (1/2-horShift" + clRname + "+1/(2*zoom" + clRname + "))*max(sub" + clRname + "[, 2])+(1/2+horShift" + clRname + "-1/(2*zoom" + clRname + "))*min(sub" + clRname + "[, 2]), length = grid" + clRname + ")");
                rcon.voidEval("yr" + clRname + " <- seq((1/2-verShift" + clRname + "-1/(2*zoom" + clRname + "))*max(sub" + clRname + "[, 1])+(1/2+verShift" + clRname + "+1/(2*zoom" + clRname + "))*min(sub" + clRname + "[, 1]), (1/2-verShift" + clRname + "+1/(2*zoom" + clRname + "))*max(sub" + clRname + "[, 1])+(1/2+verShift" + clRname + "-1/(2*zoom" + clRname + "))*min(sub" + clRname + "[, 1]), length = grid" + clRname + ")");
            }
            rcon.voidEval("l" + clRname + " <- length(slice" + clRname + ")");
            rcon.voidEval("if (l" + clRname + " < ncol(data" + clRname + ") - 3) {\n" +
                    "slnames" + clRname + " <- names(slice" + clRname + ")\n" +
                    "slice" + clRname + " <- c(slice" + clRname + ", rep(list(0), ncol(data" + clRname + ") - 3 - \n" +
                    "l" + clRname + "))\n" +
                    "names" + clRname + " <- labels(delete.response(terms(" + clRname + ")))\n" +
                    "names(slice" + clRname + ") <- c(slnames" + clRname + ", names[!names" + clRname + " %in%\n" +
                    " c(colnames(sub" + clRname + "), slnames" + clRname + ")])\n" +
                    "}");
            rcon.voidEval("lis" + clRname + " <- c(list(yr" + clRname + "), list(xr" + clRname + "), slice" + clRname + ")");
            rcon.voidEval("names(lis" + clRname + ")[1:2] <- colnames(sub" + clRname + ")");
            rcon.voidEval("new" + clRname + " <- expand.grid(lis" + clRname + ")[, labels(terms(" + clRname + "))]");
            rcon.voidEval("preds" + clRname + " <- predict(" + clRname + ", new" + clRname + ")");
        } catch (RSrvException rse){
            ErrorDialog.show(parent,rse, "ContourPlot.calculateBackground()");
        }
    }
    
    void createSlice(){
        try{
            if (!"".equals(sliceOpt)) {
                rcon.voidEval(sliceOpt);
            } else {
                rcon.voidEval("slice" + getClassifier().getRname() + " <- list()");
            }
        } catch (RSrvException rse){
            ErrorDialog.show(parent,rse, "ContourPlot.createSlice()");
        }
    }
    
    private String getSliceOpt() {
        return sliceOpt;
    }
    
    void setSliceOpt(String sliceOpt) {
        this.sliceOpt = sliceOpt;
    }
    
    /**
     * The R code is based on function plot.svm from R package e1071.
     */
    void setPlotCall(){
        String clRname = getClassifier().getRname();
        setPlotCall("filled.contour(xr" + clRname + ", yr" + clRname + ", matrix(as.numeric(preds" + clRname + "),\n" +
                "nr = length(xr" + clRname + "), byrow = TRUE), plot.axes = {\n" +
                "axis(1)\n" +
                "axis(2)\n" +
                "colind <- as.numeric(model.response(model.frame(" + clRname + ",\n" +
                "data" + clRname + ")))\n" +
                getDataOpt() +
                "}, levels = 1:(length(" + clRname + "$levels)+1), \n" +
                " key.axes = axis(4, 1:length(" + clRname + "$levels) + 0.5,\n" +
                "labels = abbreviate(" + clRname + "$levels, minlength=7), las = 3),\n" +
                "plot.title = title(main = \"SVM classification plot\",\n" +
                " xlab = names(lis" + clRname + ")[2], ylab = names(lis" + clRname + ")[1]),color.palette=palette" + clRname + ")");
    }
    
    private String getDataOpt() {
        return dataOpt;
    }
    
    void setDataOpt(String dataOpt) {
        this.dataOpt = dataOpt;
    }
    
}
