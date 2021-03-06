/*
 * SVM.java
 *
 * Created on 24. Februar 2005, 15:19
 */

package org.rosuda.JClaR;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import java.util.ListIterator;
import java.util.Vector;
import javax.swing.ImageIcon;
import org.rosuda.JRclient.REXP;
import org.rosuda.JRclient.RFileInputStream;
import org.rosuda.JRclient.RFileOutputStream;
import org.rosuda.JRclient.RSrvException;

/**
 *
 * @author tobias
 */
public final class SVM extends DefaultClassifier {
    static final long serialVersionUID = 200708211433L;
    
    //TODO: Better plots! Noone will know what the crosses and circles in those different colours mean.
    private transient Data prediction;
    
    private int number;
    private transient ClassificationWindow svmwindow;
    
    private int numberOfSupportVectors;
    
    private int type=TYPE_C_CLASS;
    private int kernel=KERNEL_RADIAL;
    private double gamma; //default to 1/(#variables excl. classes variable)
    private double cost = 1; // >0
    private double nu = 0.5; // >0, <2*m/(m+M), with m: minimal group size, M: maximal group size
    private int degree = 3;
    private double coef0 = 0;
    private boolean scale = true;
    private int cross = 0;
    private double tolerance = 0.001;
    private boolean fitted = true;    //#T#O#D#O# class.weights, cashesize, epsilon, shrinking, probability, subset, na.action
    private List<String> classNames;
    
    private String CLASSIFICATIONRESULTNAME;
    
    
    
    /** Creates a new instance of SVM */
    SVM(final Data data, final int variablePos) {
	if(data!=null){
	    number = Main.getNewClassifierNumber();
	    
	    setData(data,variablePos);
	    
	    //TODO: delete next line?
	    data.addSVM(this);
	    
	    Rname = "m" + this.hashCode();
	    gamma = 1/(data.getNumberOfVariables()-1.0);
	}
    }
    
    /** Creates a dummy SVM to plot classified data */
    private SVM(final Data data, final String classification, final SVM svm) {
	Rname = "m" + this.hashCode();
	try{
	    RserveConnection.voidEval("d" + Rname + " <- data.frame(" + data.getRname() + "," + classification + ")");
	    RserveConnection.voidEval(Rname + " <- " + svm.getRname()); //TODO: this creates a copy of the original SVM. not necessary!!!
	} catch (RSrvException rse){
	    //TODO CATCH
	}
	final Data nData = new Data("d" + Rname);
	setData(nData, nData.getNumberOfVariables()-1);
    }
    
    public void setData(final Data newData, final int newVariablePos){
	this.data=newData;
	this.variablePos=newVariablePos;
	this.variableName=(String)newData.getVariables().get(newVariablePos);
	trained=false;
	//TODO: don't set it to null. save the data in some way.
	if(plot!=null) {
	    plot.setFixVariablesDialog(null);
	}
	
    }
    
    void setType(final int t) {
	if (t<5 && t>-1)  {
	    type=t;
	}
	
    }
    
    boolean train() {
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
	    RserveConnection.voidEval(Rname + " <- svm(" + formulaOpt + dataOpt +  costOpt + gammaOpt + nuOpt
		    + degreeOpt + coef0Opt + typeOpt + scaleOpt + crossOpt + kernelOpt + toleranceOpt + fittedOpt + ")");
	    
	    numberOfSupportVectors = RserveConnection.evalI(Rname + "$tot.nSV");
	    
	    if(!trained){
		try{
		    final String[] classNamesSA = RserveConnection.evalSL(Rname + "$levels");
		    classNames = Arrays.asList(classNamesSA);
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
    
    static final int TYPE_C_CLASS = 0;
    static final int TYPE_NU_CLASS = 1;
    static final int TYPE_ONE_CLASS = 2;
    static final int TYPE_EPS_REGR = 3;
    private static final int TYPE_NU_REGR = 4;
    
    static final int KERNEL_LINEAR = 0;
    static final int KERNEL_POLYNOMIAL = 1;
    static final int KERNEL_RADIAL = 2;
    static final int KERNEL_SIGMOID = 3;
    
    int getType() {
	return type;
    }
    
    static String typeToString(final int type) {
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
    
    static String kernelToString(final int kernel) {
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
    
    void setCost(final double c){
	if(c>=0)  {
	    cost=c;
	}
	
    }
    
    void setGamma(final double g){
	//XXX: has gamma to be nonnegative?
	if(g>=0) {
	    gamma=g;
	}
	
    }
    
    void setNu(final double n){
	if(n>=0 && n<=1)  {
	    nu=n;
	}
	
    }
    
    private void setScaled(final boolean s){
	scale=s;
    }
    
    void setKernel(final int k){
	if (k<4 && k>-1)  {
	    kernel = k;
	}
	
    }
    
    int getKernel(){
	return kernel;
    }
    
    void setDegree(final int d){
	//XXX: has degree to be nonnegative?
	if(d>=0) {
	    degree=d;
	}
	
    }
    
    void setCoef0(final double c){
	coef0=c;
    }
    
    private void setTolerance(final double t){
	if(t>=0) {
	    tolerance=t;
	}
	
    }
    
    private void setFitted(final boolean f){
	fitted=f;
    }
    
    public void remove(final boolean removeInData){
	try{
	    RserveConnection.voidEval("rm(" + Rname + ")");
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
    
    public int getNumber(){
	return number;
    }
    
    void setCross(final int cross){
	if(cross>=0 && data!=null && cross <= data.getLength())  {
	    this.cross = cross;
	}
	
    }
    
    private boolean getFitted(){
	return fitted;
    }
    
    private void calculateFitted() {
	try{
	    RserveConnection.voidEval(Rname + "$fitted <- predict(" + Rname + "," + data.getRname() + ")");
	} catch(RSrvException rse) {
	    ErrorDialog.show(parent,"Rserve exception in SVM.calculateFitted(): "+rse.getMessage());
	    return;
	}
	fitted=true;
    }
    
    double getGamma() {
	return gamma;
    }
    
    double getCost() {
	return cost;
    }
    
    double getNu() {
	return nu;
    }
    
    int getDegree() {
	return degree;
    }
    
    double getCoef0() {
	return coef0;
    }
    
    int getNumberOfSupportVectors() {
	return numberOfSupportVectors;
    }
    
    public void show() {
	if (svmwindow==null)  {
	    svmwindow = new SVMWindow(this);
	}
	
	svmwindow.setVisible(true);
    }
    
    private boolean getScale(){
	return scale;
    }
    
    int getCross(){
	return cross;
    }
    
    private double getTolerance(){
	return tolerance;
    }
    
    
    private void updateAccuracy(){
	if(!getFitted())  {
	    calculateFitted();
	}
	final String prediction = "fitted(" + Rname + ")";
	final String         reality   = data.getRname() + "[," + (variablePos+1) + "]";
	confusionMatrix = calculateConfusionMatrix(prediction,reality);
	accuracy = calculateAccuracyRate(prediction,reality,confusionMatrix);
    }
    
    private void updateAccuracyOfPrediction(){
	if(hasAccuracyOfPrediction()){
	    final String pred = prediction.getRname();
	    final String         reality   = classificationData.getVariable(getVariableName());
	    accuracyOfPrediction =  calculateAccuracyRate(pred,reality,calculateConfusionMatrix(pred,reality));
	}
    }
    
    public int[] getConfusionMatrix(){
	return confusionMatrix;
    }
    
    public List getClassNames(){
	return classNames;
    }
    
    int getMinGroupSize(){
	int ret=-1;
	if(data!=null){
	    try{
		ret = RserveConnection.evalI("min(summary(factor(" + data.getRname() + "[," + (variablePos+1) + "])))");
	    } catch (RSrvException rse){
		ErrorDialog.show(parent, rse, "SVM.getMinGroupSize()");
	    }
	}
	return ret;
    }
    
    int getMaxGroupSize(){
	int ret=-1;
	if(data!=null){
	    try{
		ret = RserveConnection.evalI("max(summary(factor(" + data.getRname() + "[," + (variablePos+1) + "])))");
	    } catch (RSrvException rse){
		ErrorDialog.show(parent, rse, "SVM.getMaxGroupSize()");
	    }
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
	    RserveConnection.voidEval(snapshot.Rname + " <- " + Rname);
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
	
	variableName = (String)data.getVariables().get(variablePos);
	try{
	    RserveConnection.voidEval(Rname + " <- " + snap.Rname);
	} catch(RSrvException rse){
	    ErrorDialog.show(parent, rse, "SVM.createSnapshot()");
	}
    }
    
    public void classify(final Data dataset) {
	if(dataset==null) return;
	classificationData = dataset;
	
	if(prediction==null){
	    prediction = new Data();
	    CLASSIFICATIONRESULTNAME = prediction.getRname();
	}
	try{
	    RserveConnection.voidEval(prediction.getRname() + " <- predict(" + Rname + "," + classificationData.getRname() + ")");
	    prediction.setName("Pred. SVM #" + number + ", dataset " + data.getPath());
	    prediction.update();
	} catch(RSrvException rse){
	    ErrorDialog.show(parent, rse, "SVM.classify(Data)");
	}
	classificationData=dataset;
	updateAccuracyOfPrediction();
    }
    
    public String getClassifiedDataFrame() {
	if(hasClassifiedData())
	    return "data.frame(" + getVariableName() + "=" + prediction.getRname() + "," + classificationData.getRname() + ")";
	else
	    return null;
    }
    
    private void writeObject(final ObjectOutputStream s) throws IOException {
	s.defaultWriteObject();
	
	try{
	    final String modelFileName = "model";
	    RserveConnection.voidEval("save(" + Rname + ",file='" + modelFileName + "')");
	    final byte[] b = RserveConnection.readFile(modelFileName);
	    s.writeObject(b);
	} catch (RSrvException rse){
	    ErrorDialog.show(parent, rse, "writeObject(ObjectOutputStream)");
	}
	// private transient SVMClassificationPlot plot;?
    }
    
    private void readObject(final ObjectInputStream s) throws IOException, ClassNotFoundException  {
	s.defaultReadObject();
	
	final byte[] b = (byte[])s.readObject();
	final String modelFileName = "model";
	final RFileOutputStream rfos = RserveConnection.createFile(modelFileName);
	rfos.write(b);
	rfos.close();
	try{
	    RserveConnection.voidEval("load('" + modelFileName + "')");
	} catch (RSrvException rse){
	    ErrorDialog.show(parent, rse, "readObject(ObjectOutputStream)");
	}
	//TODO: parent is not restored
	//plot?
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
	    StringBuffer ttt = new StringBuffer();
	    
	    ttt.append("#Support vectors: ")
	    .append(numberOfSupportVectors)
	    .append("<br>")
	    .append("Type: ")
	    .append(typeToString(type))
	    .append("<br>")
	    .append("Kernel: ")
	    .append(kernelToString(kernel));
	    
	    final String gammaInf = "<br>Gamma: " + gamma;
	    final String degreeInf = "<br>Degree: " + degree;
	    final String coef0Inf = "<br>Coef0: " + coef0;
	    final String costInf = "<br>Cost: " + cost;
	    final String nuInf = "<br>Nu: " + nu;
	    
	    switch(kernel){
		case KERNEL_LINEAR:
		    ttt.append(costInf);
		    break;
		case KERNEL_POLYNOMIAL:
		    ttt.append(gammaInf)
		    .append(degreeInf)
		    .append(coef0Inf)
		    .append(costInf);
		    break;
		case KERNEL_RADIAL:
		    ttt.append(gammaInf)
		    .append(costInf);
		    break;
		default: //SIGMOID
		    ttt.append(gammaInf)
		    .append(coef0Inf)
		    .append(costInf);
		    break;
	    }
	    switch(type){
		case TYPE_C_CLASS:
		    break;
		case TYPE_NU_CLASS:
		    ttt.append(nuInf);
		    break;
		default:
		    break;
		    //not implemented
	    }
	    return "<html>" + ttt.toString() + "</html>";
	}
	
	public ImageIcon getThumbnail() {
	    return thumbnail;
	}
    }
    
}