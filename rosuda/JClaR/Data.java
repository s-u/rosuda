/*
 * Data.java
 *
 * Created on 24. Februar 2005, 15:22
 */

package org.rosuda.JClaR;

import java.io.File;
import java.util.Vector;
import java.util.Enumeration;
import java.awt.Component;
import org.rosuda.JRclient.REXP;
import org.rosuda.JRclient.RSrvException;



/**
 *
 * @author tobias
 */
public final class Data implements Cloneable {
    
    // If new fields are added the clone method probably has to be changed!
    private File file;
    private Vector variables = new Vector();
    private RserveConnection rcon;
    private String Rname;
    private int length;
    private Vector svms = new Vector();
    private String name;
    private boolean restricted=false;
    private int format;
    
    private Component parent;
    
    public Data(final File f) {
        file=f;
        rcon=RserveConnection.getRconnection();
        Rname="d" + this.hashCode();
        this.format=format;
        
        
        
        // get variables and length
        update();
    }
    
    /** Creates a new instance of Data */
    public Data(){
        rcon=RserveConnection.getRconnection();
        Rname="d" + this.hashCode();
    }
    
    public void removeNAs(){
        // remove rows with NAs as they would be ignored by svm anyway
        try{
            rcon.voidEval(Rname + " <- " + Rname + "[!apply(is.na(" + Rname + "),1,any),]");
        } catch (RSrvException rse){
            ErrorDialog.show(parent,"Rserve exception in Data(File,int) while removing rows with NAs: "+rse.getMessage());
        }
    }
    
    
    
    public String getPath() {
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
    
    public String getName(){
        if(name!=null)  {
            return name;
        }
        
        else  {
            return getPath();
        }
        
    }
    
    public int getNumberOfVariables(){
        return variables.size();
    }
    
    public int getLength(){
        return length;
    }
    
    public Vector getVariables(){
        return variables;
    }
    
    public String getRname(){
        return Rname;
    }
    
    public void remove(){
        try{
            rcon.voidEval("rm(" + Rname + ")");
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
    
    public void addSVM(final Classifier svm){
        svms.add(svm);
    }
    
    public void removeSVM(final Classifier svm){
        svms.remove(svm);
    }
    
    public Vector getSVMs(){
        return svms;
    }
    
    public void saveAs(final File newFile){
        try{
            rcon.voidEval("write.table(" + Rname + ",file='" + newFile.getAbsolutePath() + "',quote=FALSE,sep=',')");
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
    
    public void setParent(final Component parent){
        this.parent = parent;
    }
    
    public void setName(final String name){
        this.name = name;
    }
    
    /**
     * Sets new file.
     *
     * Only used when data is saved. Doesn't execute any R commands to read this file.
     *
     * @param file The new file.
     */
    public void setFile(final File file){
        this.file = file;
    }
    
    public void refactor(final int variablePos){
        try{
            rcon.voidEval("if(!is.factor(" + Rname + "[," + (variablePos+1) +  "])) " +
                    Rname + " <- data.frame(" + 
                    ((variablePos>0)?(Rname+"[1:"+variablePos+"],"):"") + 
                    (String)variables.elementAt(variablePos)+"=factor("+Rname +"[,"+(variablePos+1)+"])"+
                    ((variablePos+1<variables.size())?(","+Rname+"["+(variablePos+2)+":"+variables.size()+"]"):"") +
                    ")");
        } catch(RSrvException rse){
            ErrorDialog.show(parent,rse,"Data.refactor()");
        }
    }
    
    public Object clone(){
        final Data newData = new Data();
        newData.setRestricted(restricted);
        newData.setFile(file);
        
        //TODO: name, successfullyReadData, parent
        
        try{
            rcon.voidEval(newData.getRname() + " <- " + Rname);
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
    public void update() throws NullPointerException {
        try{
            //get variables
            Vector vars = rcon.eval("names(" + Rname + ")").asVector();
            if (vars==null){
                vars = new Vector();
                vars.add(rcon.eval("names(" + Rname + ")"));
            }
            for (final Enumeration e = vars.elements() ; e.hasMoreElements() ;){
                String name = ((REXP)e.nextElement()).asString();
                // variable mustn't have the same name as a parameter of data.frame()
                if (name.equals("row.names") || name.equals("check.rows") || name.equals("check.names")){
                    name += ".v";
                }
                variables.add(name);
            }
            
            //get length
            length = rcon.eval("length(data.frame(" + Rname + ")[,1])").asInt();
        } catch (RSrvException rse) {
            ErrorDialog.show(parent,"Rserve exception in Data(File,Rconnection): "+rse.getMessage());
        }
    }
    
    /**
     * Returns a new Data object, that contains only the selected variables.
     */
    public Data getRestrictedData(final int[] hiddenVariables){
        final Data restrictedData = (Data)this.clone();
        restrictedData.restrict(hiddenVariables);
        return restrictedData;
    }
    
    public void restrict(final int[] hiddenVariables){
        String varsString="";
        for(int i=hiddenVariables.length-1; i>=0; i--){
            varsString += (hiddenVariables[i]+1) + ",";
            variables.removeElementAt(hiddenVariables[i]);
        }
        try{
            if(!"".equals(varsString)) {
                rcon.voidEval(Rname + " <- " + Rname + "[,-c(" + varsString + ")]");
            }
            
        } catch(RSrvException rse) {
            ErrorDialog.show(parent,"Rserve exception in Data.restrict(int[]): "+rse.getMessage());
        }
        setRestricted(true);
    }
    
    public static final int DATA_FORMAT_NONE = -1;
    public static final int DATA_FORMAT_CSV = 0;
    public static final int DATA_FORMAT_CSV2 = 1;
    public static final int DATA_FORMAT_TABLE = 2;
    public static final int DATA_FORMAT_DELIM = 3;
    public static final int DATA_FORMAT_DELIM2 = 4;
    
    public boolean isRestricted() {
        return restricted;
    }
    
    public void setRestricted(final boolean restricted) {
        this.restricted = restricted;
    }
    
    public Component getParent() {
        return parent;
    }
    
    public void unclass(final int columnNotToUnclass){
        try{
            String columns="";
            boolean comma=false;
            for (int i=0, size = variables.size(); i<size; i++){
                if(!comma)  {
                    comma=true;
                }
                
                else  {
                    columns += ",";
                }
                
                if (i!=columnNotToUnclass) {
                    columns += (String)variables.elementAt(i) + "=unclass(" + Rname + "[," + (i+1) + "])";
                }
                
                else {
                    columns += (String)variables.elementAt(i) + "=" + Rname + "[," + (i+1) + "]";
                }
                
            }
            rcon.voidEval(Rname + "<- data.frame(" + columns + ")");
        } catch (RSrvException rse){
            ErrorDialog.show(parent, rse, "Data.unclass(int)");
        }
    }
}