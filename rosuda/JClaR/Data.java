/*
 * Data.java
 *
 * Created on 24. Februar 2005, 15:22
 */

package org.rosuda.JClaR;

import java.io.File;
import java.io.Serializable;
import java.util.Arrays;
import java.util.ListIterator;
import java.awt.Component;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;
import org.rosuda.JRclient.RFileOutputStream;
import org.rosuda.JRclient.RSrvException;



/**
 *
 * @author tobias
 */
public final class Data implements Cloneable, Serializable {
    static final long serialVersionUID = 200708211502L;
    
    // If new fields are added the clone method probably has to be changed!
    private File file;
    private ArrayList<String> variables = new ArrayList<String>();
    private String Rname;
    private int length;
    private ArrayList<Classifier> svms = new ArrayList<Classifier>();
    private String name;
    private boolean restricted=false;
    private int format;
    
    private Component parent;
    
    private Data(final File f) {
	file=f;
	Rname="d" + this.hashCode();
	this.format=format;
	
	
	
	// get variables and length
	update();
    }
    
    /** Creates a new instance of Data */
    Data(){
	Rname="d" + this.hashCode();
    }
    
    /** Creates a java object for the riven R dataset */
    Data(final String rData){
	Rname = rData;
	update();
    }
    
    void removeNAs(){
	// remove rows with NAs as they would be ignored by svm anyway
	try{
	    RserveConnection.voidEval(Rname + " <- " + Rname + "[!apply(is.na(" + Rname + "),1,any),]");
	} catch (RSrvException rse){
	    ErrorDialog.show(parent,"Rserve exception in Data(File,int) while removing rows with NAs: "+rse.getMessage());
	}
    }
    
    
    
    String getPath() {
	if(file!=null) {
	    String path=file.getPath();
	    if (File.separatorChar == '\\')  {
		path = path.replace('\\', '/');
	    }
	    
	    return path;
	} else  {
	    return "";
	}
	
    }
    
    private String getName(){
	if(name!=null)  {
	    return name;
	}
	
	else  {
	    return getPath();
	}
	
    }
    
    int getNumberOfVariables(){
	return variables.size();
    }
    
    int getLength(){
	return length;
    }
    
    List<String> getVariables(){
	return variables;
    }
    
    String getRname(){
	return Rname;
    }
    
    private void remove(){
	try{
	    RserveConnection.voidEval("rm(" + Rname + ")");
	} catch(RSrvException rse) {
	    switch(rse.getRequestReturnCode()){
		case RserveConnection.RERROR_OTHER:
		    ErrorDialog.show(parent,"Could not free memory used by data.");
		    break;
		default:
		    ErrorDialog.show(parent,"Rserve exception in Data.remove(): "+rse.getMessage());
		    break;
	    }
	}
    }
    
    void addSVM(final Classifier svm){
	svms.add(svm);
    }
    
    void removeSVM(final Classifier svm){
	svms.remove(svm);
    }
    
    private List getSVMs(){
	return svms;
    }
    
    private void saveAs(final File newFile){
	try{
	    RserveConnection.writeTable(Rname,newFile, ",quote=FALSE,sep=',')");
	} catch(RSrvException rse) {
	    switch(rse.getRequestReturnCode()){
		case RserveConnection.RERROR_OTHER:
		    ErrorDialog.show(parent,"Could not write data.\n\nRserve exception: "+rse.getMessage());
		    break;
		default:
		    ErrorDialog.show(parent,"Rserve exception in Data.saveAs(File): "+rse.getMessage());
		    break;
	    }
	}
    }
    
    private void setParent(final Component parent){
	this.parent = parent;
    }
    
    void setName(final String name){
	this.name = name;
    }
    
    /**
     * Sets new file.
     *
     * Only used when data is saved. Doesn't execute any R commands to read this file.
     *
     * @param file The new file.
     */
    void setFile(final File file){
	this.file = file;
    }
    
    void refactor(final int variablePos){
	try{
	    RserveConnection.voidEval("if(!is.factor(" + Rname + "[," + (variablePos+1) +  "])) " +
		    Rname + " <- data.frame(" +
		    ((variablePos>0)?(Rname+"[1:"+variablePos+"],"):"") +
		    (String)variables.get(variablePos)+"=factor("+Rname +"[,"+(variablePos+1)+"])"+
		    ((variablePos+1<variables.size())?(","+Rname+"["+(variablePos+2)+":"+variables.size()+"]"):"") +
		    ")");
	} catch(RSrvException rse){
	    ErrorDialog.show(parent,rse,"Data.refactor()");
	}
    }
    
    protected Object clone(){
	final Data newData = new Data();
	newData.setRestricted(restricted);
	newData.setFile(file);
	
	//TODO: name, successfullyReadData, parent
	
	try{
	    RserveConnection.voidEval(newData.getRname() + " <- " + Rname);
	} catch(RSrvException rse) {
	    ErrorDialog.show(parent,"Rserve exception in Data.clone(): "+rse.getMessage());
	}
	
	newData.update();
	return newData;
    }
    
    /**
     * Fetches variable names and number of cases.
     * @throws java.lang.NullPointerException Might be thrown if data is malformed.
     */
    void update() throws NullPointerException {
	try{
	    //get variables
	    String[] varsSA = RserveConnection.evalSL("names(" + Rname + ")");
	    List<String> vars = Arrays.asList(varsSA);
	    
	    for (final ListIterator<String> e = vars.listIterator() ; e.hasNext() ;){
		String name = e.next();
		// variable mustn't have the same name as a parameter of data.frame()
		if ("row.names".equals(name) || "check.rows".equals(name) || "check.names".equals(name)){
		    name += ".v";
		}
		variables.add(name);
	    }
	    
	    //get length
	    length = RserveConnection.evalI("length(data.frame(" + Rname + ")[,1])");
	} catch (RSrvException rse) {
	    ErrorDialog.show(parent,"Rserve exception in Data(File,Rconnection): "+rse.getMessage());
	}
    }
    
    /**
     * Returns a new Data object, that contains only the selected variables.
     */
    Data getRestrictedData(final int[] hiddenVariables){
	final Data restrictedData = (Data)this.clone();
	restrictedData.restrict(hiddenVariables);
	return restrictedData;
    }
    
    private void restrict(final int[] hiddenVariables){
	StringBuffer varsString=new StringBuffer();
	for(int i=hiddenVariables.length-1; i>0; i--){
	    varsString.append(hiddenVariables[i]+1).append(",");
	    variables.remove(hiddenVariables[i]);
	}
	varsString.append(hiddenVariables[0]+1);
	variables.remove(hiddenVariables[0]);
	try{
	    final String varsS = varsString.toString();
	    if(!"".equals(varsS)) {
		RserveConnection.voidEval(Rname + " <- " + Rname + "[,-c(" + varsS + ")]");
	    }
	    
	} catch(RSrvException rse) {
	    ErrorDialog.show(parent,"Rserve exception in Data.restrict(int[]): "+rse.getMessage());
	}
	setRestricted(true);
    }
    
    private static final int DATA_FORMAT_NONE = -1;
    private static final int DATA_FORMAT_CSV = 0;
    private static final int DATA_FORMAT_CSV2 = 1;
    private static final int DATA_FORMAT_TABLE = 2;
    private static final int DATA_FORMAT_DELIM = 3;
    private static final int DATA_FORMAT_DELIM2 = 4;
    
    boolean isRestricted() {
	return restricted;
    }
    
    private void setRestricted(final boolean restricted) {
	this.restricted = restricted;
    }
    
    Component getParent() {
	return parent;
    }
    
    void unclass(final int columnNotToUnclass){
	try{
	    StringBuffer columns=new StringBuffer();
	    boolean comma=false;
	    for (int i=0, size = variables.size(); i<size; i++){
		if(!comma)  {
		    comma=true;
		}
		
		else  {
		    columns.append(",");
		}
		
		if (i!=columnNotToUnclass) {
		    columns.append((String)variables.get(i))
		    .append("=unclass(")
		    .append(Rname)
		    .append("[,")
		    .append(i+1)
		    .append("])");
		}
		
		else {
		    columns.append((String)variables.get(i))
		    .append("=")
		    .append(Rname)
		    .append("[,")
		    .append(i+1)
		    .append("]");
		}
		
	    }
	    RserveConnection.voidEval(Rname + "<- data.frame(" + columns + ")");
	} catch (RSrvException rse){
	    ErrorDialog.show(parent, rse, "Data.unclass(int)");
	}
    }
    
    String getVariable(final String name) {
	return Rname + "$" + name;
    }
    
    private void writeObject(final ObjectOutputStream s) throws IOException {
	s.defaultWriteObject();
	
	try{
	    final String fileName = "data";
	    RserveConnection.voidEval("save(" + Rname + ",file='" + fileName + "')");
	    final byte[] b = RserveConnection.readFile(fileName);
	    s.writeObject(b);
	} catch (RSrvException rse){
	    ErrorDialog.show(parent, rse, "writeObject(ObjectOutputStream)");
	}
    }
    
    private void readObject(final ObjectInputStream s) throws IOException, ClassNotFoundException  {
	s.defaultReadObject();
	
	final byte[] b = (byte[])s.readObject();
	final String fileName = "data";
	final RFileOutputStream rfos = RserveConnection.createFile(fileName);
	rfos.write(b);
	rfos.close();
	try{
	    RserveConnection.voidEval("load('" + fileName + "')");
	} catch (RSrvException rse){
	    ErrorDialog.show(parent, rse, "readObject(ObjectOutputStream)");
	}
    }
}
