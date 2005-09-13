/*
 * Tune.java
 *
 * Created on 14. März 2005, 09:08
 */

package org.rosuda.JClaR;

import org.rosuda.JRclient.REXP;
import org.rosuda.JRclient.RList;
import org.rosuda.JRclient.RSrvException;




/**
 *
 * @author tobias
 */
public final class Tune {
    
    private Data data;
    private String Rname;
    private RserveConnection rcon;
    
    private double bestCost;
    private double bestGamma;
    private int bestDegree;
    private double bestNu;
    private double bestCoef0;
    
    /** Creates a new instance of Tune */
    public Tune(final Data data) {
        rcon=RserveConnection.getRconnection();
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
    
    public void setVariable(final String variable){
        this.variable = variable;
    }
    
    public void setFromGamma(final double fromGamma) {
        this.fromGamma = fromGamma;
    }
    
    public void setToGamma(final double toGamma) {
        this.toGamma = toGamma;
    }
    
    public void setFromCost(final double fromCost) {
        this.fromCost = fromCost;
    }
    
    public void setToCost(final double toCost) {
        this.toCost = toCost;
    }
    
    public void tune() {
        try{
            final String gammaRange;
            final String costRange;
            final String degreeRange;
            final String nuRange;
            final String coef0Range;
            
            final String fixGammaOpt;
            final String fixCostOpt;
            final String fixDegreeOpt;
            final String fixNuOpt;
            final String fixCoef0Opt;
            
            final String kernelOpt;
            final String typeOpt;
            
            final double zeroFactor = 0.3;
            
            if(tuneGamma) {
                if(fromGamma==0) {
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
            if(tuneCost) {
                if(fromCost==0) {
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
            if(tuneDegree) {
                degreeRange="degree=seq(" + fromDegree + "," + toDegree + ",length=" + byDegree + ")";
                fixDegreeOpt="";
            } else {
                degreeRange="";
                fixDegreeOpt=",degree=" + fixDegree;
            }
            if(tuneNu) {
                if(fromNu==0) {
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
            if(tuneCoef0) {
                if(fromCoef0<0){
                    if(toCoef0<0) {
                        coef0Range="coef0=-2^seq(log2(" + (-fromCoef0) + ")," +
                                "log2(" + (-toCoef0) + "),length=" + byCoef0 + ")";
                    }
                    
                    else if(toCoef0==0) {
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
                    
                } else if(fromCoef0==0) {
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
            
            kernelOpt = ",kernel='" + SVM.kernelToString(kernel) + "'";
            typeOpt = ",type='" + SVM.typeToString(type) + "'";
            
            //#T#O#D#O#: use other tune.control?
            //XXX: what effect has setting the cross variable here?
            rcon.voidEval(Rname + " <- tune(svm," + variable + "~.,data=" + data.getRname() + ","
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
            
            rcon.voidEval("print(" + Rname + "$best.parameters)");
            
            if(tuneGamma)  {
                bestGamma = rcon.eval(Rname + "$best.parameters").asList().at("gamma").asDouble();
            }
            
            else  {
                bestGamma = fixGamma;
            }
            
            if(tuneCost)  {
                bestCost = rcon.eval(Rname + "$best.parameters").asList().at("cost").asDouble();
            }
            
            else  {
                bestCost = fixCost;
            }
            
            if(tuneDegree)  {
                bestDegree = (int)Math.round(rcon.eval(Rname + "$best.parameters").asList().at("degree").asDouble());
            }
            
            else  {
                bestDegree = fixDegree;
            }
            
            if(tuneNu)  {
                bestNu = rcon.eval(Rname + "$best.parameters").asList().at("nu").asDouble();
            }
            
            else  {
                bestNu = fixNu;
            }
            
            if(tuneCoef0)  {
                bestCoef0 = rcon.eval(Rname + "$best.parameters").asList().at("coef0").asDouble();
            }
            
            else  {
                bestCoef0 = fixCoef0;
            }
            
        } catch(RSrvException rse) {
            error("Rserve exception in Tune.tune(): "+rse.getMessage());
        }
    }
    
    public String toString(){
        String ret="";
        if(tuneGamma)  {
            ret += "Gamma: " + bestGamma + "\n";
        }
        
        if(tuneCost)  {
            ret += "Cost: " + bestCost + "\n";
        }
        
        if(tuneDegree)  {
            ret += "Degree: " + bestDegree + "\n";
        }
        
        if(tuneNu)  {
            ret += "Nu: " + bestNu + "\n";
        }
        
        if(tuneCoef0)  {
            ret += "Coef0: " + bestCoef0 + "\n";
        }
        
        return ret;
    }
    
    public double getBestCost() {
        return bestCost;
    }
    
    public double getBestGamma() {
        return bestGamma;
    }
    
    public void setByGamma(final int byGamma) {
        this.byGamma = byGamma;
    }
    
    public void setByCost(final int byCost) {
        this.byCost = byCost;
    }
    
    public void setFromCoef0(final double fromCoef0) {
        this.fromCoef0 = fromCoef0;
    }
    
    public void setToCoef0(final double toCoef0) {
        this.toCoef0 = toCoef0;
    }
    
    public void setByCoef0(final int byCoef0) {
        this.byCoef0 = byCoef0;
    }
    
    public void setFromNu(final double fromNu) {
        this.fromNu = fromNu;
    }
    
    public void setToNu(final double toNu) {
        this.toNu = toNu;
    }
    
    public void setByNu(final int byNu) {
        this.byNu = byNu;
    }
    
    public void setFromDegree(final int fromDegree) {
        this.fromDegree = fromDegree;
    }
    
    public void setToDegree(final int toDegree) {
        this.toDegree = toDegree;
    }
    
    public void setByDegree(final int byDegree) {
        this.byDegree = byDegree;
    }
    
    public void setTuneGamma(final boolean tuneGamma) {
        this.tuneGamma = tuneGamma;
    }
    
    public void setTuneCost(final boolean tuneCost) {
        this.tuneCost = tuneCost;
    }
    
    public void setTuneCoef0(final boolean tuneCoef0) {
        this.tuneCoef0 = tuneCoef0;
    }
    
    public void setTuneNu(final boolean tuneNu) {
        this.tuneNu = tuneNu;
    }
    
    public void setTuneDegree(final boolean tuneDegree) {
        this.tuneDegree = tuneDegree;
    }
    
    public int getBestDegree() {
        return bestDegree;
    }
    
    public double getBestNu() {
        return bestNu;
    }
    
    public double getBestCoef0() {
        return bestCoef0;
    }
    
    public boolean isTuneGamma() {
        return tuneGamma;
    }
    
    public boolean isTuneCost() {
        return tuneCost;
    }
    
    public boolean isTuneCoef0() {
        return tuneCoef0;
    }
    
    public boolean isTuneNu() {
        return tuneNu;
    }
    
    public boolean isTuneDegree() {
        return tuneDegree;
    }
    
    public void error(final String message){
        if(data!=null) {
            ErrorDialog.show(data.getParent(),message);
        }
        
        else  {
            ErrorDialog.show(null,message);
        }
        
    }
    
    public void setFixGamma(final double fixGamma) {
        this.fixGamma = fixGamma;
    }
    
    public void setFixCost(final double fixCost) {
        this.fixCost = fixCost;
    }
    
    public void setFixCoef0(final double fixCoef0) {
        this.fixCoef0 = fixCoef0;
    }
    
    public void setFixNu(final double fixNu) {
        this.fixNu = fixNu;
    }
    
    public void setFixDegree(final int fixDegree) {
        this.fixDegree = fixDegree;
    }
    
    public Plot plot() {
        return new TunePlot(this);
    }
    
    public String getRname() {
        return Rname;
    }
    
    public int getNumberOfTunedParameters(){
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
    
    public Plot plot(final java.awt.Frame parent) {
        return plot();
    }
    
    public void setKernel(int kernel) {
        this.kernel = kernel;
    }
    
    public void setType(int type) {
        this.type = type;
    }
    
}
