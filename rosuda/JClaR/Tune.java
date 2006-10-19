/*
 * Tune.java
 *
 * Created on 14. März 2005, 09:08
 */

package org.rosuda.JClaR;

import org.rosuda.JRclient.RSrvException;




/**
 *
 * @author tobias
 */
public final class Tune {
    
    private Data data;
    private String Rname;
    
    private double bestCost;
    private double bestGamma;
    private int bestDegree;
    private double bestNu;
    private double bestCoef0;
    
    /** Creates a new instance of Tune */
    Tune(final Data data) {
        this.data=data;
        Rname = "t" + this.hashCode();
    }
    
    private double fromGamma;
    private double toGamma;
    private int byGamma;
    private double fromCost;
    private double toCost;
    private int byCost;
    private double fromCoef0;
    private double toCoef0;
    private int byCoef0;
    private double fromNu;
    private double toNu;
    private int byNu;
    private int fromDegree;
    private int toDegree;
    private int byDegree;
    
    private double fixGamma;
    private double fixCost;
    private double fixCoef0;
    private double fixNu;
    private int fixDegree;
    
    private boolean tuneGamma;
    private boolean tuneCost;
    private boolean tuneCoef0;
    private boolean tuneNu;
    private boolean tuneDegree;
    
    private int kernel=SVM.KERNEL_RADIAL;
    private int type=SVM.TYPE_C_CLASS;
    
    private String variable;
    
    void setVariable(final String variable){
        this.variable = variable;
    }
    
    void setFromGamma(final double fromGamma) {
        this.fromGamma = fromGamma;
    }
    
    void setToGamma(final double toGamma) {
        this.toGamma = toGamma;
    }
    
    void setFromCost(final double fromCost) {
        this.fromCost = fromCost;
    }
    
    void setToCost(final double toCost) {
        this.toCost = toCost;
    }
    
    void tune() {
        try{
            final String gammaRange;
            
            
            
            
            
            final String fixGammaOpt;
            
            
            
            
            
            
            
            
            final double zeroFactor = 0.3;
            
            if(tuneGamma) {
                if(Math.abs(fromGamma - 0) < 0.0001) {
                    gammaRange="gamma=c(0,2^seq(log2(" + (zeroFactor*toGamma/byGamma) + ")," +
                            "log2(" + toGamma + "),length=" + (byGamma-1) + "))";
                }
                
                else {
                    gammaRange="gamma=2^seq(log2(" + fromGamma + "),log2(" + toGamma + "),length=" + byGamma + ")";
                }
                
                fixGammaOpt="";
            } else {
                gammaRange="";
                fixGammaOpt=",gamma=" + fixGamma;
            }
            final String costRange;
            final String fixCostOpt;
            if(tuneCost) {
                if(Math.abs(fromCost - 0) < 0.0001) {
                    costRange="cost=c(0,2^seq(log2(" + (zeroFactor*toCost/byCost) + ")," +
                            "log2(" + toCost + "),length=" + (byCost-1) + "))";
                }
                
                else {
                    costRange="cost=2^seq(log2(" + fromCost + "),log2(" + toCost + "),length=" + byCost + ")";
                }
                
                fixCostOpt="";
            } else {
                costRange="";
                fixCostOpt=",cost=" + fixCost;
            }
            final String degreeRange;
            final String fixDegreeOpt;
            if(tuneDegree) {
                degreeRange="degree=seq(" + fromDegree + "," + toDegree + ",length=" + byDegree + ")";
                fixDegreeOpt="";
            } else {
                degreeRange="";
                fixDegreeOpt=",degree=" + fixDegree;
            }
            final String nuRange;
            final String fixNuOpt;
            if(tuneNu) {
                if(Math.abs(fromNu - 0) < 0.0001) {
                    nuRange="nu=c(0,2^seq(log2(" + (zeroFactor*toNu/byNu) + ")," +
                            "log2(" + toNu + "),length=" + byNu + "))";
                }
                
                else {
                    nuRange="nu=2^seq(log2(" + fromNu + "),log2(" + toNu + "),length=" + byNu + ")";
                }
                
                fixNuOpt="";
            } else {
                nuRange="";
                fixNuOpt=",nu=" + fixNu;
            }
            final String fixCoef0Opt;
            final String coef0Range;
            if(tuneCoef0) {
                if(fromCoef0<0){
                    if(toCoef0<0) {
                        coef0Range="coef0=-2^seq(log2(" + (-fromCoef0) + ")," +
                                "log2(" + (-toCoef0) + "),length=" + byCoef0 + ")";
                    }
                    
                    else if(Math.abs(toCoef0 - 0) < 0.0001) {
                        coef0Range="coef0=c(-2^seq(log2(" + (-fromCoef0) + ")," +
                                "log2(" + (-zeroFactor*fromCoef0/byCoef0) + "),length=" + (byCoef0-1) + "),0)";
                    }
                    
                    else  {// toCoef0>0
                        coef0Range="coef0=c(-2^seq(log2(" + (-fromCoef0) + ")," +
                                "log2(" + (-zeroFactor*fromCoef0/byCoef0) + ")," +
                                "length=round(" + (-fromCoef0*(byCoef0-1)/(toCoef0-fromCoef0)) + ")),0," +
                                "-2^seq(log2(" + (zeroFactor*toCoef0/byCoef0) + ")," +
                                "log2(" + toCoef0 + ")," +
                                "length=round(" + (toCoef0*(byCoef0-1)/(toCoef0-fromCoef0)) + ")))";
                    }
                    
                } else if(Math.abs(fromCoef0 - 0) < 0.0001) {
                    coef0Range="coef0=c(0,2^seq(log2(" + (zeroFactor*toCoef0/byCoef0) + ")," +
                            "log2(" + toCoef0 + "),length=" + byCoef0 + "))";
                }
                
                else  {// fromCoef0>0
                    coef0Range="coef0=seq(" + fromCoef0 + "," + toCoef0 + "," + byCoef0 + ")";
                }
                
                fixCoef0Opt="";
            } else {
                coef0Range="";
                fixCoef0Opt = ",coef0=" + fixCoef0;
            }
            
            final String kernelOpt;
            kernelOpt = ",kernel='" + SVM.kernelToString(kernel) + "'";
            final String typeOpt;
            typeOpt = ",type='" + SVM.typeToString(type) + "'";
            
            //#T#O#D#O#: use other tune.control?
            //XXX: what effect has setting the cross variable here?
            RserveConnection.voidEval(Rname + " <- tune(svm," + variable + "~.,data=" + data.getRname() + ","
                    + "ranges=list("
                    + gammaRange + ","
                    + costRange + ","
                    + degreeRange + ","
                    + nuRange + ","
                    + coef0Range
                    + ")"
                    + ",tunecontrol = tune.control(sampling = 'fix')"
                    + fixGammaOpt
                    + fixCostOpt
                    + fixDegreeOpt
                    + fixNuOpt
                    + fixCoef0Opt
                    + kernelOpt
                    + typeOpt
                    + ")");
            
            RserveConnection.voidEval("print(" + Rname + "$best.parameters)");
            
            if(tuneGamma)  {
                bestGamma = RserveConnection.eval(Rname + "$best.parameters").asList().at("gamma").asDouble();
            }
            
            else  {
                bestGamma = fixGamma;
            }
            
            if(tuneCost)  {
                bestCost = RserveConnection.eval(Rname + "$best.parameters").asList().at("cost").asDouble();
            }
            
            else  {
                bestCost = fixCost;
            }
            
            if(tuneDegree)  {
                bestDegree = (int)Math.round(RserveConnection.eval(Rname + "$best.parameters").asList().at("degree").asDouble());
            }
            
            else  {
                bestDegree = fixDegree;
            }
            
            if(tuneNu)  {
                bestNu = RserveConnection.eval(Rname + "$best.parameters").asList().at("nu").asDouble();
            }
            
            else  {
                bestNu = fixNu;
            }
            
            if(tuneCoef0)  {
                bestCoef0 = RserveConnection.eval(Rname + "$best.parameters").asList().at("coef0").asDouble();
            }
            
            else  {
                bestCoef0 = fixCoef0;
            }
            
        } catch(RSrvException rse) {
            error("Rserve exception in Tune.tune(): "+rse.getMessage());
        }
    }
    
    public String toString(){
        StringBuffer ret = new StringBuffer();
        if(tuneGamma)  {
            ret.append("Gamma: ")
            .append(bestGamma)
            .append("\n");
        }
        
        if(tuneCost)  {
            ret.append("Cost: ")
            .append(bestCost)
            .append("\n");
        }
        
        if(tuneDegree)  {
            ret.append("Degree: ")
            .append(bestDegree)
            .append("\n");
        }
        
        if(tuneNu)  {
            ret.append("Nu: ")
            .append(bestNu)
            .append("\n");
        }
        
        if(tuneCoef0)  {
            ret.append("Coef0: ")
            .append(bestCoef0)
            .append("\n");
        }
        
        return ret.toString();
    }
    
    double getBestCost() {
        return bestCost;
    }
    
    double getBestGamma() {
        return bestGamma;
    }
    
    void setByGamma(final int byGamma) {
        this.byGamma = byGamma;
    }
    
    void setByCost(final int byCost) {
        this.byCost = byCost;
    }
    
    void setFromCoef0(final double fromCoef0) {
        this.fromCoef0 = fromCoef0;
    }
    
    void setToCoef0(final double toCoef0) {
        this.toCoef0 = toCoef0;
    }
    
    void setByCoef0(final int byCoef0) {
        this.byCoef0 = byCoef0;
    }
    
    void setFromNu(final double fromNu) {
        this.fromNu = fromNu;
    }
    
    void setToNu(final double toNu) {
        this.toNu = toNu;
    }
    
    void setByNu(final int byNu) {
        this.byNu = byNu;
    }
    
    void setFromDegree(final int fromDegree) {
        this.fromDegree = fromDegree;
    }
    
    void setToDegree(final int toDegree) {
        this.toDegree = toDegree;
    }
    
    void setByDegree(final int byDegree) {
        this.byDegree = byDegree;
    }
    
    void setTuneGamma(final boolean tuneGamma) {
        this.tuneGamma = tuneGamma;
    }
    
    void setTuneCost(final boolean tuneCost) {
        this.tuneCost = tuneCost;
    }
    
    void setTuneCoef0(final boolean tuneCoef0) {
        this.tuneCoef0 = tuneCoef0;
    }
    
    void setTuneNu(final boolean tuneNu) {
        this.tuneNu = tuneNu;
    }
    
    void setTuneDegree(final boolean tuneDegree) {
        this.tuneDegree = tuneDegree;
    }
    
    int getBestDegree() {
        return bestDegree;
    }
    
    double getBestNu() {
        return bestNu;
    }
    
    double getBestCoef0() {
        return bestCoef0;
    }
    
    private boolean isTuneGamma() {
        return tuneGamma;
    }
    
    private boolean isTuneCost() {
        return tuneCost;
    }
    
    private boolean isTuneCoef0() {
        return tuneCoef0;
    }
    
    private boolean isTuneNu() {
        return tuneNu;
    }
    
    private boolean isTuneDegree() {
        return tuneDegree;
    }
    
    private void error(final String message){
        if(data!=null) {
            ErrorDialog.show(data.getParent(),message);
        }
        
        else  {
            ErrorDialog.show(null,message);
        }
        
    }
    
    void setFixGamma(final double fixGamma) {
        this.fixGamma = fixGamma;
    }
    
    void setFixCost(final double fixCost) {
        this.fixCost = fixCost;
    }
    
    void setFixCoef0(final double fixCoef0) {
        this.fixCoef0 = fixCoef0;
    }
    
    void setFixNu(final double fixNu) {
        this.fixNu = fixNu;
    }
    
    void setFixDegree(final int fixDegree) {
        this.fixDegree = fixDegree;
    }
    
    Plot plot() {
        return new TunePlot(this);
    }
    
    String getRname() {
        return Rname;
    }
    
    int getNumberOfTunedParameters(){
        int n=0;
        if (tuneGamma)  {
            n++;
        }
        
        if (tuneCost)  {
            n++;
        }
        
        if (tuneNu)  {
            n++;
        }
        
        if (tuneDegree)  {
            n++;
        }
        
        if (tuneCoef0)  {
            n++;
        }
        
        return n;
    }
    
    private Plot plot(final java.awt.Frame parent) {
        return plot();
    }
    
    void setKernel(final int kernel) {
        this.kernel = kernel;
    }
    
    void setType(final int type) {
        this.type = type;
    }
    
}
