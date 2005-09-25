/*
 * SVM.java
 *
 * Created on 24. Februar 2005, 15:19
 */

package org.rosuda.JClaR;

import java.awt.Component;
import java.util.Vector;
import javax.swing.ImageIcon;
import org.rosuda.JRclient.RSrvException;


/**
 *
 * @author tobias
 */
public final class SVM implements Classifier {
    
    //TODO: Better plots! Noone will know what the crosses and circles in those different colours mean.
    
    private RserveConnection rcon;
    private Data data;
    
    private int number;
    
    private boolean trained=false;
    private ClassificationWindow svmwindow;
    
    protected SVMClassificationPlot plot;
    
    //TODO: necesarry?
    private Component parent;
    
    private String Rname;
    private int variablePos; //which data column was selected (starts at 0)
    private String variableName;
    
    protected int numberOfSupportVectors;
    
    protected int type=TYPE_C_CLASS;
    protected int kernel=KERNEL_RADIAL;
    protected double gamma; //default to 1/(#variables excl. classes variable)
    protected double cost = 1; // >0
    protected double nu = 0.5; // >0, <2*m/(m+M), with m: minimal group size, M: maximal group size
    protected int degree = 3;
    protected double coef0 = 0;
    protected boolean scale = true;
    protected int cross = 0;
    protected double tolerance = 0.001;
    private boolean fitted = true;
    //#T#O#D#O# class.weights, cashesize, epsilon, shrinking, probability, subset, na.action
    
    private double accuracy=0;
    private int[] confusionMatrix;
    private Vector classNames;
    
    /** Creates a new instance of SVM */
    public SVM(final Data data, final int variablePos) {
        if(data!=null){
            number = Main.getNewClassifierNumber();
            rcon=RserveConnection.getRconnection();
            
            setData(data,variablePos);
            
            //TODO: delete next line?
            data.addSVM(this);
            
            Rname = "m" + this.hashCode();
            gamma = 1/(data.getNumberOfVariables()-1.0);
        }
    }
    
    public void setData(final Data newData, final int newVariablePos){
        this.data=newData;
        this.variablePos=newVariablePos;
        this.variableName=(String)newData.getVariables().elementAt(newVariablePos);
        trained=false;
        //TODO: don't set it to null. save the data in some way.
        if(plot!=null) {
            plot.setFixVariablesDialog(null);
        }
        
    }
    
    public void setType(final int t) {
        if (t<5 && t>-1)  {
            type=t;
        }
        
    }
    
    public boolean train() {
        try{
            String formulaOpt = "";
            if(type==TYPE_C_CLASS || type==TYPE_NU_CLASS || type==TYPE_EPS_REGR || type==TYPE_NU_REGR) {
                formulaOpt = variableName + "~.";
            }
            
            
            String dataOpt = "";
            if(type==TYPE_C_CLASS || type==TYPE_NU_CLASS) {
                //dataOpt = ",data=" + data.getRefactoredDataFrame(variablePos);
                dataOpt = ",data=" + data.getRname();
            }
            
            else if(type==TYPE_ONE_CLASS)  {
                dataOpt = ",data=" + data.getRname();
            }
            
            //else dataOpt = ",data=" + data.getRname() + "[,-" + (variablePos+1) + "]," + data.getRname() + "[," + (variablePos+1) + "]";
            else  {
                dataOpt = ",data=" + data.getRname();
            }
            
            
            final String gammaOpt = ",gamma=" + gamma;
            final String costOpt = ",cost=" + cost;
            final String degreeOpt = ",degree=" + degree;
            final String coef0Opt = ",coef0=" + coef0;
            final String nuOpt = ",nu=" + nu;
            final String typeOpt = ",type='" + typeToString(type) + "'";
            
            final String kernelOpt = ",kernel='" + kernelToString(kernel) + "'";
            
            //TODO: Scaling can be turned on/off for each Variable in R. At the moment it is turned on/off for all variables.
            String scaleOpt="";
            if (!scale)  {
                scaleOpt = ",scale=FALSE";
            }
            
            
            String fittedOpt="";
            if (!fitted)  {
                fittedOpt = ",fitted=FALSE";
            }
            
            
            final String crossOpt=",cross=" + cross;
            final String toleranceOpt=",tolerance="+tolerance;
            rcon.voidEval(Rname + " <- svm(" + formulaOpt + dataOpt +  costOpt + gammaOpt + nuOpt
                    + degreeOpt + coef0Opt + typeOpt + scaleOpt + crossOpt + kernelOpt + toleranceOpt + fittedOpt + ")");
            
            numberOfSupportVectors = rcon.eval(Rname + "$tot.nSV").asInt();
            
            if(!trained){
                try{
                    classNames =  rcon.eval(Rname + "$levels").asVector();
                } catch (RSrvException rse) {
                    ErrorDialog.show(parent, rse, "SVM.train()");
                }
                trained=true;
            }
            updateAccuracy();
            return true;
        } catch(RSrvException rse) {
            switch(rse.getRequestReturnCode()){
                case RserveConnection.RERROR_OTHER:
                    ErrorDialog.show(parent,"Training error.");
                    break;
                default:
                    ErrorDialog.show(parent, rse, "svm.train()");
                    break;
            }
            return false;
        }
    }
    
    public String getVariableName(){
        return variableName;
    }
    
    public static final int TYPE_C_CLASS = 0;
    public static final int TYPE_NU_CLASS = 1;
    public static final int TYPE_ONE_CLASS = 2;
    public static final int TYPE_EPS_REGR = 3;
    public static final int TYPE_NU_REGR = 4;
    
    public static final int KERNEL_LINEAR = 0;
    public static final int KERNEL_POLYNOMIAL = 1;
    public static final int KERNEL_RADIAL = 2;
    public static final int KERNEL_SIGMOID = 3;
    
    
    
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
    
    public int getType() {
        return type;
    }
    
    public static String typeToString(final int type) {
        // These strings are also used as arguments of type in "svm(..., type=<>, ...)"!
        switch(type){
            case TYPE_C_CLASS:
                return "C-classification";
            case TYPE_EPS_REGR:
                return "eps-regression";
            case TYPE_NU_CLASS:
                return "nu-classification";
            case TYPE_NU_REGR:
                return "nu-regression";
            default:
                return "one-classification";
        }
    }
    
    public static String kernelToString(final int kernel) {
        // These strings are also used as arguments of kernel in "svm(..., kernel=<>, ...)"!
        switch(kernel){
            case KERNEL_LINEAR:
                return "linear";
            case KERNEL_POLYNOMIAL:
                return "polynomial";
            case KERNEL_RADIAL:
                return "radial";
            default:
                return "sigmoid";
        }
    }
    
    public void setCost(final double c){
        if(c>=0)  {
            cost=c;
        }
        
    }
    
    public void setGamma(final double g){
        //XXX: has gamma to be nonnegative?
        if(g>=0) {
            gamma=g;
        }
        
    }
    
    public void setNu(final double n){
        if(n>=0 && n<=1)  {
            nu=n;
        }
        
    }
    
    public void setScaled(final boolean s){
        scale=s;
    }
    
    public void setKernel(final int k){
        if (k<4 && k>-1)  {
            kernel = k;
        }
        
    }
    
    public int getKernel(){
        return kernel;
    }
    
    public void setDegree(final int d){
        //XXX: has degree to be nonnegative?
        if(d>=0) {
            degree=d;
        }
        
    }
    
    public void setCoef0(final double c){
        coef0=c;
    }
    
    public void setTolerance(final double t){
        if(t>=0) {
            tolerance=t;
        }
        
    }
    
    public void setFitted(final boolean f){
        fitted=f;
    }
    
    public void remove(final boolean removeInData){
        try{
            rcon.voidEval("rm(" + Rname + ")");
            if (removeInData)  {
                data.removeSVM(this);
            }
            
        } catch(RSrvException rse) {
            switch(rse.getRequestReturnCode()){
                case RserveConnection.RERROR_OTHER:
                    ErrorDialog.show(parent,"Could not free memory used by SVM.");
                    break;
                default:
                    ErrorDialog.show(parent,"Rserve exception in SVM.remove(boolean): "+rse.getMessage());
                    break;
            }
        }
    }
    
    public Data getData(){
        return data;
    }
    
    public void setParent(final Component parent){
        this.parent = parent;
    }
    
    public int getNumber(){
        return number;
    }
    
    public Data predict(final Data newdata){
        try{
            final Data prediction = new Data();
            rcon.voidEval(prediction.getRname() + " <- predict(" + Rname + "," + newdata.getRname() + ")");
            prediction.setName("Pred. SVM #" + number + ", dataset " + data.getPath());
            prediction.update();
            return prediction;
        } catch(RSrvException rse) {
            ErrorDialog.show(parent,"Rserve exception in SVM.predict(Data): "+rse.getMessage());
            return null;
        }
    }
    
    public void setCross(final int cross){
        if(cross>=0 && cross <= data.getLength())  {
            this.cross = cross;
        }
        
    }
    
    public boolean getFitted(){
        return fitted;
    }
    
    public void calculateFitted() {
        try{
            rcon.voidEval(Rname + "$fitted <- predict(" + Rname + "," + data.getRname() + ")");
        } catch(RSrvException rse) {
            ErrorDialog.show(parent,"Rserve exception in SVM.calculateFitted(): "+rse.getMessage());
            return;
        }
        fitted=true;
    }
    
    public String getRname() {
        return Rname;
    }
    
    public int getVariablePos() {
        return variablePos;
    }
    
    public double getGamma() {
        return gamma;
    }
    
    public double getCost() {
        return cost;
    }
    
    public double getNu() {
        return nu;
    }
    
    public int getDegree() {
        return degree;
    }
    
    public double getCoef0() {
        return coef0;
    }
    
    public int getNumberOfSupportVectors() {
        return numberOfSupportVectors;
    }
    
    public boolean getTrained(){
        return trained;
    }
    
    public Plot getPlot() {
        return plot;
    }
    
    public void setPlot(final Plot plot) {
        this.plot = (SVMClassificationPlot)plot;
    }
    
    public void show() {
        if (svmwindow==null)  {
            svmwindow = new SVMWindow(this);
        }
        
        svmwindow.show();
    }
    
    public boolean getScale(){
        return scale;
    }
    
    public int getCross(){
        return cross;
    }
    
    public double getTolerance(){
        return tolerance;
    }
    
    
    private void updateAccuracy(){
        if(!getFitted())  {
            calculateFitted();
        }
        
        try{
            confusionMatrix = rcon.eval("table(fitted(" + Rname + "), " +
                    data.getRname() + "[," + (variablePos+1) + "])").asIntArray();
            
            final int numClasses = rcon.eval("length(" + Rname + "$levels)").asInt();
            accuracy=0;
            for (int i=0; i<numClasses; i++){
                accuracy += confusionMatrix[i*(numClasses+1)];
            }
            accuracy /= (double)data.getLength();
        } catch (RSrvException rse){
            ErrorDialog.show(parent, rse, "SVM.updateAccuracy()");
        }
    }
    
    public double getAccuracy(){
        return accuracy;
    }
    
    public int[] getConfusionMatrix(){
        return confusionMatrix;
    }
    
    public Vector getClassNames(){
        return classNames;
    }
    
    public int getMinGroupSize(){
        int ret=-1;
        try{
            ret = rcon.eval("min(summary(factor(" + data.getRname() + "[," + (variablePos+1) + "])))").asInt();
        } catch (RSrvException rse){
            ErrorDialog.show(parent, rse, "SVM.getMinGroupSize()");
        }
        return ret;
    }
    
    public int getMaxGroupSize(){
        int ret=-1;
        try{
            ret = rcon.eval("max(summary(factor(" + data.getRname() + "[," + (variablePos+1) + "])))").asInt();
        } catch (RSrvException rse){
            ErrorDialog.show(parent, rse, "SVM.getMaxGroupSize()");
        }
        return ret;
    }
    
    public SVMSnapshotIF createSnapshot(){
        final Snapshot snapshot = new Snapshot();
        snapshot.type = type;
        snapshot.kernel = kernel;
        snapshot.degree = degree;
        snapshot.cross = cross;
        snapshot.numberOfSupportVectors = numberOfSupportVectors;
        snapshot.variablePos = variablePos;
        snapshot.gamma = gamma;
        snapshot.cost = cost;
        snapshot.coef0 = coef0;
        snapshot.nu = nu;
        snapshot.tolerance = tolerance;
        snapshot.scale = scale;
        snapshot.fitted = fitted;
        snapshot.data = data;
        snapshot.plot = plot;
        snapshot.thumbnail = new ImageIcon(plot.getImage().getScaledInstance(100, 100,
                java.awt.Image.SCALE_SMOOTH));
        try{
            rcon.voidEval(snapshot.Rname + " <- " + Rname);
        } catch(RSrvException rse){
            ErrorDialog.show(parent, rse, "SVM.createSnapshot()");
        }
        
        return snapshot;
    }
    
    public void restoreSnapshot(final SVMSnapshotIF snapIF){
        final Snapshot snap = (Snapshot)snapIF;
        type = snap.type;
        kernel = snap.kernel;
        degree = snap.degree;
        cross = snap.cross;
        numberOfSupportVectors = snap.numberOfSupportVectors;
        variablePos = snap.variablePos;
        gamma = snap.gamma;
        cost = snap.cost;
        coef0 = snap.coef0;
        nu = snap.nu;
        tolerance = snap.tolerance;
        scale = snap.scale;
        fitted = snap.fitted;
        data = snap.data;
        plot = snap.plot;
        
        variableName = (String)data.getVariables().elementAt(variablePos);
        try{
            rcon.voidEval(Rname + " <- " + snap.Rname);
        } catch(RSrvException rse){
            ErrorDialog.show(parent, rse, "SVM.createSnapshot()");
        }
    }

    public boolean isReady() {
        return getTrained();
    }

    public String classify(Data dataset) {
        String resultsRname = "pred" + dataset.getRname() + getRname();
        try{
            rcon.voidEval(resultsRname +  " <- predict(" + getRname() + "," + dataset.getRname() + ")");
        } catch(RSrvException rse){
            ErrorDialog.show(parent, rse, "SVM.classify(Data)");
            return null;
        }
        return resultsRname;
    }
    
    private static final class Snapshot implements SVMSnapshotIF {
        
        public ImageIcon thumbnail;
        public int type, kernel, degree, cross, numberOfSupportVectors, variablePos;
        public double gamma, cost, coef0, nu, tolerance;
        public boolean scale,fitted;
        public Data data;
        public SVMClassificationPlot plot;
        public String Rname;
                
        public Snapshot(){
            Rname = "snap" + hashCode();
        }
        
        public String getToolTipText() {
            String ttt;
            
            ttt = "#Support vectors: " + numberOfSupportVectors + "<br>" +
                    "Type: " + typeToString(type) + "<br>" +
                    "Kernel: " + kernelToString(kernel);
            
            final String gammaInf = "<br>Gamma: " + gamma;
            final String degreeInf = "<br>Degree: " + degree;
            final String coef0Inf = "<br>Coef0: " + coef0;
            final String costInf = "<br>Cost: " + cost;
            final String nuInf = "<br>Nu: " + nu;
            
            switch(kernel){
                case KERNEL_LINEAR:
                    ttt += costInf;
                    break;
                case KERNEL_POLYNOMIAL:
                    ttt += gammaInf + degreeInf + coef0Inf + costInf;
                    break;
                case KERNEL_RADIAL:
                    ttt += gammaInf + costInf;
                    break;
                default: //SIGMOID
                    ttt += gammaInf + coef0Inf + costInf;
                    break;
            }
            switch(type){
                case TYPE_C_CLASS:
                    break;
                case TYPE_NU_CLASS:
                    ttt += nuInf;
                    break;
                default:
                    break;
                    //not implemented
            }
            return "<html>" + ttt + "</html>";
        }
        
        public ImageIcon getThumbnail() {
            return thumbnail;
        }
    }

}