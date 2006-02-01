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
    
    final String GRIDNAME,SLICENAME,FORMULANAME,NAMESNAME,PALETTENAME;
    String DATANAME,CLASSIFIERNAME,ORIGDATANAME;
    
    ContourPlot(Classifier cl){
        super();
        setClassifier(cl);
        if(cl!=null) CLASSIFIERNAME = cl.getRname();
        setGrid(50);
        
        if(cl==null) {
            GRIDNAME="grid"+hashCode();
            SLICENAME="slice"+hashCode();
            FORMULANAME="formula"+hashCode();
            NAMESNAME="names"+hashCode();
            PALETTENAME="palette"+hashCode();
        } else {
            GRIDNAME="grid"+cl.getRname();
            SLICENAME="slice"+cl.getRname();
            FORMULANAME="formula"+cl.getRname();
            NAMESNAME="names"+cl.getRname();
            PALETTENAME="palette"+cl.getRname();
        }
    }
    
    int getGrid() {
        return grid;
    }
    
    void setGrid(int grid) {
        this.grid = grid;
        RserveConnection rcon = RserveConnection.getRconnection();
        try{
            rcon.voidEval(GRIDNAME + " <- " + grid);
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
        String clRname = CLASSIFIERNAME;
        try{
            rcon.voidEval("if (is.null(" + FORMULANAME + "))\n" +
                    "stop(\"missing formula.\")");
            rcon.voidEval("sub" + clRname + " <- model.frame(" + FORMULANAME + ", " + ORIGDATANAME + ")");
            rcon.voidEval("zoom" + clRname + " <- " + zoom);
            rcon.voidEval("horShift" + clRname + " <- " + horizontalShift);
            rcon.voidEval("verShift" + clRname + " <- " + verticalShift);
            if(zoom<=1){
                rcon.voidEval("xr" + clRname + " <- seq((1/2-horShift" + clRname + "-1/(2*zoom" + clRname + "))*max(sub" + clRname + "[, 2])+(1/2+horShift" + clRname + "+1/(2*zoom" + clRname + "))*min(sub" + clRname + "[, 2]), (1/2-horShift" + clRname + "+1/(2*zoom" + clRname + "))*max(sub" + clRname + "[, 2])+(1/2+horShift" + clRname + "-1/(2*zoom" + clRname + "))*min(sub" + clRname + "[, 2]), length = " + GRIDNAME + ")");
                rcon.voidEval("yr" + clRname + " <- seq((1/2-verShift" + clRname + "-1/(2*zoom" + clRname + "))*max(sub" + clRname + "[, 1])+(1/2+verShift" + clRname + "+1/(2*zoom" + clRname + "))*min(sub" + clRname + "[, 1]), (1/2-verShift" + clRname + "+1/(2*zoom" + clRname + "))*max(sub" + clRname + "[, 1])+(1/2+verShift" + clRname + "-1/(2*zoom" + clRname + "))*min(sub" + clRname + "[, 1]), length = " + GRIDNAME + ")");
            } else{
                rcon.voidEval("xr" + clRname + " <- seq((1/2-horShift" + clRname + "-1/(2*zoom" + clRname + "))*max(sub" + clRname + "[, 2])+(1/2+horShift" + clRname + "+1/(2*zoom" + clRname + "))*min(sub" + clRname + "[, 2]), (1/2-horShift" + clRname + "+1/(2*zoom" + clRname + "))*max(sub" + clRname + "[, 2])+(1/2+horShift" + clRname + "-1/(2*zoom" + clRname + "))*min(sub" + clRname + "[, 2]), length = " + GRIDNAME + ")");
                rcon.voidEval("yr" + clRname + " <- seq((1/2-verShift" + clRname + "-1/(2*zoom" + clRname + "))*max(sub" + clRname + "[, 1])+(1/2+verShift" + clRname + "+1/(2*zoom" + clRname + "))*min(sub" + clRname + "[, 1]), (1/2-verShift" + clRname + "+1/(2*zoom" + clRname + "))*max(sub" + clRname + "[, 1])+(1/2+verShift" + clRname + "-1/(2*zoom" + clRname + "))*min(sub" + clRname + "[, 1]), length = " + GRIDNAME + ")");
            }
            rcon.voidEval("l" + clRname + " <- length(" + SLICENAME + ")");
            rcon.voidEval("if (l" + clRname + " < ncol(" + DATANAME + ") - 3) {\n" +
                    "slnames" + clRname + " <- names(" + SLICENAME + ")\n" +
                    SLICENAME + " <- c(" + SLICENAME + ", rep(list(0), ncol(" + DATANAME + ") - 3 - \n" +
                    "l" + clRname + "))\n" +
                    NAMESNAME + " <- labels(delete.response(terms(" + CLASSIFIERNAME + ")))\n" +
                    "names(" + SLICENAME + ") <- c(slnames" + clRname + ", names[!" + NAMESNAME + " %in%\n" +
                    " c(colnames(sub" + clRname + "), slnames" + clRname + ")])\n" +
                    "}");
            rcon.voidEval("lis" + clRname + " <- c(list(yr" + clRname + "), list(xr" + clRname + "), " + SLICENAME + ")");
            rcon.voidEval("names(lis" + clRname + ")[1:2] <- colnames(sub" + clRname + ")");
            rcon.voidEval("new" + clRname + " <- expand.grid(lis" + clRname + ")[, labels(terms(" + CLASSIFIERNAME + "))]");
            rcon.voidEval("preds" + clRname + " <- predict(" + CLASSIFIERNAME + ", new" + clRname + ")");
        } catch (RSrvException rse){
            ErrorDialog.show(parent,rse, "ContourPlot.calculateBackground()");
        }
    }
    
    void createSlice(){
        try{
            if (!"".equals(sliceOpt)) {
                rcon.voidEval(sliceOpt);
            } else {
                rcon.voidEval(SLICENAME + " <- list()");
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
        setPlotCall("filled.contour(xr" + CLASSIFIERNAME + ", yr" + CLASSIFIERNAME + ", matrix(as.numeric(preds" + CLASSIFIERNAME + "),\n" +
                "nr = length(xr" + CLASSIFIERNAME + "), byrow = TRUE), plot.axes = {\n" +
                "axis(1)\n" +
                "axis(2)\n" +
                "colind <- as.numeric(model.response(model.frame(" + CLASSIFIERNAME + ",\n" +
                ORIGDATANAME + ")))\n" +
                getDataOpt() +
                "}, levels = 1:(length(" + CLASSIFIERNAME + "$levels)+1), \n" +
                " key.axes = axis(4, 1:length(" + CLASSIFIERNAME + "$levels) + 0.5,\n" +
                "labels = abbreviate(" + CLASSIFIERNAME + "$levels, minlength=7), las = 3),\n" +
                "plot.title = title(main = \"SVM classification plot\",\n" +
                " xlab = names(lis" + CLASSIFIERNAME + ")[2], ylab = names(lis" + CLASSIFIERNAME + ")[1]),color.palette=" + PALETTENAME + ")");
    }
    
    private String getDataOpt() {
        return dataOpt;
    }
    
    void setDataOpt(String dataOpt) {
        this.dataOpt = dataOpt;
    }
    
}
