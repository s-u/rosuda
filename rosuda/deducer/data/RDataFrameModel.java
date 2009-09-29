package org.rosuda.deducer.data;


import java.awt.Component;
import java.awt.Font;

import javax.swing.*;

import org.rosuda.JGR.JGR;
import org.rosuda.JGR.util.ErrorMsg;
import org.rosuda.REngine.REXP;
import org.rosuda.REngine.REXPFactor;
import org.rosuda.REngine.REXPList;
import org.rosuda.REngine.REXPLogical;
import org.rosuda.REngine.REXPMismatchException;
import org.rosuda.REngine.RFactor;
import org.rosuda.REngine.RList;
import org.rosuda.deducer.Deducer;


/**
 * Data Frame model
 * 
 * @author ifellows
 *
 */
class RDataFrameModel extends ExDefaultTableModel {
	
	private static String guiEnv = Deducer.guiEnv;
	
	private String rDataName=null;
	
	private String tempDataName=null;
	
	RowNamesModel rowNamesModel = new RowNamesModel();
	
	public static final int numExtensionRows = 50;
	public static final int numExtensionColumns = 10; 
	
	public RDataFrameModel(){}
	
	public RDataFrameModel(String name){
		setDataName(name);	
	}
	
	public String getDataName(){return rDataName;}
	
	public void setDataName(String name){
		
		boolean envDefined = ((REXPLogical)Deducer.eval("'"+guiEnv+"' %in% .getOtherObjects()")).isTRUE()[0];
		
		if(!envDefined){
			Deducer.rniEval(guiEnv+"<-new.env(parent=emptyenv())");
		}
		if(tempDataName!=null)
			Deducer.eval("rm("+tempDataName+",envir="+guiEnv+")");
		rDataName = name;
		tempDataName = JGR.MAINRCONSOLE.getUniqueName(rDataName,guiEnv);
		Deducer.eval(guiEnv+"$"+tempDataName+"<-"+rDataName);
		this.fireTableStructureChanged();
		this.fireTableDataChanged();
	}
	
	public int getColumnCount( ){
		if(rDataName!=null){
			boolean nExists = ((REXPLogical)Deducer.eval("!exists('"+rDataName+"')")).isTRUE()[0];
			if(nExists)
				return 0;
			try {
				return Deducer.eval("ncol("+rDataName+")").asInteger()+numExtensionColumns;
			} catch (REXPMismatchException e) {
				return 0;
			}
		}else
			return 0;
	}
	
	public int getRealColumnCount( ){
		if(rDataName!=null){
			if(((REXPLogical)Deducer.eval("!exists('"+rDataName+"')")).isTRUE()[0])
				return 0;
			try {
				return Deducer.eval("ncol("+rDataName+")").asInteger();
			} catch (REXPMismatchException e) {
				return 0;
			}
		}else
			return 0;
	}

	public int getRealRowCount(){
		if(rDataName!=null){
			if(((REXPLogical)Deducer.eval("!exists('"+rDataName+"')")).isTRUE()[0])
				return 0;
			try {
				return Deducer.eval("nrow("+rDataName+")").asInteger();
			} catch (REXPMismatchException e) {
				return 0;
			}
		}else
			return 0;
	}
	
	public int getRowCount(){
		if(rDataName!=null){
			if(((REXPLogical)Deducer.eval("!exists('"+rDataName+"')")).isTRUE()[0])
				return 0;
			try {
				return Deducer.eval("nrow("+rDataName+")").asInteger()+numExtensionRows;
			} catch (REXPMismatchException e) {
				return 0;
			}
		}else
			return 0;
	}
	
	public void removeColumn(int colNumber){
		if(colNumber<getRealColumnCount()){
			Deducer.eval(rDataName+"<-"+rDataName+"[,-"+(colNumber+1)+"]");
			refresh();
		}
	}

	public void removeRow(int row){
		if((row+1)<=getRealRowCount()){
			Deducer.eval(rDataName + "<- "+rDataName + "[-"+(row+1)+",]");
			refresh();
		}
	}
	
	public void insertNewColumn(int col){
		if(col>getRealColumnCount()+1)
			return;
		if(col<1)
			Deducer.eval(rDataName+"<-data.frame(V=as.integer(NA),"+
					rDataName+"[,"+(col+1)+":"+getRealColumnCount()+",drop=FALSE])");
		else if(col>=getRealColumnCount())
			Deducer.eval(rDataName+"<-data.frame("+rDataName+",V=as.integer(NA))");
		else
			Deducer.eval(rDataName+"<-data.frame("+rDataName+"[,1:"+col+",drop=FALSE],V=as.integer(NA),"+
				rDataName+"[,"+(col+1)+":"+getRealColumnCount()+",drop=FALSE])");
		refresh();
	}
	
	public void insertNewRow(int row){
		int rowCount =getRealRowCount();
		setValueAt("NA",Math.max(rowCount,row),0);
		Deducer.eval("attr("+rDataName+",'row.names')["+(Math.max(rowCount,row)+1)+"]<-'New'");
		if(row<1)
			Deducer.eval(rDataName+"<-rbind("+rDataName+
					"["+(rowCount+1)+",],"+rDataName+"["+(row+1)+":"+rowCount+",,drop=FALSE])");
		else if(row<rowCount)
			Deducer.eval(rDataName+"<-rbind("+rDataName+"[1:"+row+",,drop=FALSE],"+rDataName+
					"["+(rowCount+1)+",],"+rDataName+"["+(row+1)+":"+rowCount+",,drop=FALSE])");
		Deducer.eval("rownames("+rDataName+")<-make.unique(rownames("+rDataName+"))");
		
	}
	
	
	public Object getValueAt(int row, int col){
		Object value = "?";
		try {
			//if(Deducer.rniEval("!exists('"+rDataName+"')").asBool().isTRUE())
			//	value= "?";
			//else 
			if(row>=getRealRowCount() || col>=getRealColumnCount()){
				value= "";
			}else{
				REXP val = Deducer.eval(rDataName+"["+(row+1)+","+(col+1)+"]");
				if(val.isNA()[0])
					value = new NAHolder();
				else if(val.isFactor()){
					value = val.asFactor().asStrings()[0];
				}else if(val.isInteger())
					value = new Integer(val.asInteger());
				else if( val.isLogical())
					value = new Boolean(((REXPLogical)val).isTRUE()[0]);				
				else if(val.isNumeric())
					value = (new Double(val.asDouble()));
				else if(val.isString()){
					if(((REXPLogical)Deducer.eval("is.na("+rDataName+"["+(row+1)+","+(col+1)+"])")).isTRUE()[0])
						value = new NAHolder();
					else
						value= val.asString();
				}
			}
		} catch (REXPMismatchException e) {
			value = "?";
		}
		return value;
	}
	/*public Object getValueAt(int row, int col){
		Object value = "?";
		try {
			//if(Deducer.rniEval("!exists('"+rDataName+"')").asBool().isTRUE())
			//	value= "?";
			//else 
			if(row>=getRealRowCount() || col>=getRealColumnCount()){
				value= "";
			}else{
				REXP var = data.at(col);
				if(var.isNA()[row])
					value = new NAHolder();
				else if(var.isFactor()){
					value = var.asFactor().asStrings()[row];
				}else if(var.isInteger())
					value = new Integer(var.asIntegers()[row]);
				else if( var.isLogical())
					value = new Boolean(((REXPLogical)var).isTRUE()[row]);				
				else if(var.isNumeric())
					value = (new Double(var.asDoubles()[row]));
				else if(var.isString()){
					if(((REXPLogical)Deducer.eval("is.na("+rDataName+"["+(row+1)+","+(col+1)+"])")).isTRUE()[0])
						value = new NAHolder();
					else
						value= var.asStrings()[row];
				}
			}
		} catch (REXPMismatchException e) {
			value = "?";
		}
		return value;
	}*/
	
	
	public void setValueAt(Object value,int row, int col){	
		if(!this.isCellEditable(row, col))
			return;
		REXP currentValue = Deducer.eval(rDataName+"["+(row+1)+","+(col+1)+"]");	
		int numRealRows =getRealRowCount();
		int numRealCols =getRealColumnCount();	
		String valueString = null;
		boolean isDouble = false;
		boolean isInteger =false;
		if(value!=null){
			valueString = value.toString().trim();	
			isDouble=true;
			try{
				Double.parseDouble(valueString);
			}catch(Exception e){
				isDouble=false;
			}
			isInteger=true;
			try{
				Integer.parseInt(valueString);
			}catch(Exception ex){
				isInteger=false;
			}
		}


		if(value==null){
			if((row+1)<=numRealRows && (col+1)<=numRealCols)
				Deducer.eval(rDataName+"["+(row+1)+","+(col+1)+"]<-NA");
		}else if(currentValue.isNull()){
			if(!isDouble){
				if(value.toString().equals("NA"))
					Deducer.eval(rDataName+"["+(row+1)+","+(col+1)+"]<-NA");
				else if(value.toString().toLowerCase().equals("true"))
					Deducer.eval(rDataName+"["+(row+1)+","+(col+1)+"]<-TRUE");
				else if(value.toString().toLowerCase().equals("false"))
					Deducer.eval(rDataName+"["+(row+1)+","+(col+1)+"]<-FALSE");
				else
					Deducer.eval(rDataName+"["+(row+1)+","+(col+1)+"]<-'"+value.toString()+"'");
			}else{
				Deducer.eval(rDataName+"["+(row+1)+","+(col+1)+"]<-"+valueString);

			}
		}else if( value.toString().equals("NA")){
			Deducer.eval(rDataName+"["+(row+1)+","+(col+1)+"]<-NA");
		}else if(currentValue.isString()){
			Deducer.eval(rDataName+"["+(row+1)+","+(col+1)+"]<-'"+value.toString()+"'");
		}else if(currentValue.isInteger()){
			if(!isInteger){
				if(!isDouble){
					Deducer.eval(rDataName+"[,"+(col+1)+"]<-as.character("+rDataName+"[,"+(col+1)+"])");
					Deducer.eval(rDataName+"["+(row+1)+","+(col+1)+"]<-'"+value.toString()+"'");
				}else{
					Deducer.eval(rDataName+"["+(row+1)+","+(col+1)+"]<-"+valueString);
					this.fireTableDataChanged();
				}
			}else{
				Deducer.eval(rDataName+"["+(row+1)+","+(col+1)+"]<-"+valueString+"L");
			}
		}else if(currentValue.isFactor()){
			boolean isNewLevel=((REXPLogical)Deducer.eval("'"+value.toString()+"' %in% " +
					"levels(" +rDataName+"[,"+(col+1)+"])")).isFALSE()[0];

			if(isNewLevel){
				String addLevel = "levels(" +rDataName+"[,"+(col+1)+"])<-c("+
						"levels(" +rDataName+"[,"+(col+1)+"]),'"+value.toString()+"')";
				Deducer.eval(addLevel);
			}
			Deducer.eval(rDataName+"["+(row+1)+","+(col+1)+"]<-'"+value.toString()+"'");
		}else if( currentValue.isLogical()){
			if(valueString.equals("1") || valueString.toLowerCase().equals("true")
					|| valueString.toLowerCase().equals("t"))
				Deducer.eval(rDataName+"["+(row+1)+","+(col+1)+"]<-TRUE");
			else if(valueString.equals("0") || valueString.toLowerCase().equals("false")
					|| valueString.toLowerCase().equals("f"))
				Deducer.eval(rDataName+"["+(row+1)+","+(col+1)+"]<-FALSE");
			else
				Deducer.eval(rDataName+"["+(row+1)+","+(col+1)+"]<-'"+valueString+"'");
		}else if(currentValue.isNumeric()){
			if(!isDouble){
				Deducer.eval(rDataName+"[,"+(col+1)+"]<-as.character("+rDataName+"[,"+(col+1)+"])");
				Deducer.eval(rDataName+"["+(row+1)+","+(col+1)+"]<-'"+value.toString()+"'");
			}else{
				Deducer.eval(rDataName+"["+(row+1)+","+(col+1)+"]<-"+valueString);
			}
		}
		this.fireTableCellUpdated(row, col);
		if((row+1)>numRealRows){
			Deducer.eval("rownames("+rDataName+")<-make.unique(rownames("+rDataName+"))");
			this.fireTableRowsInserted(numRealRows,row);			
			this.fireTableRowsUpdated(numRealRows,row);
		}
		if((col+1)>numRealCols){
			this.fireTableDataChanged();
			try {
				this.addColumn(Deducer.eval("colnames("+rDataName+")["+(col+1)+"]").asString());
			} catch (REXPMismatchException e) {}
		}
		Deducer.eval(guiEnv+"$"+tempDataName+"<-"+rDataName);
	}
	/**
	 * Notifies components about changes in the model
	 */
	public boolean refresh(){
		boolean changed = false;
		REXP exist = Deducer.idleEval("exists('"+rDataName+"')");
		if(exist!=null && ((REXPLogical)exist).isTRUE()[0]){
			REXP ident =Deducer.idleEval("identical("+rDataName+","+guiEnv+"$"+tempDataName+")"); 
			if(ident!=null && ((REXPLogical)ident).isFALSE()[0]){
				REXP strChange = Deducer.eval("all(dim("+rDataName+")==dim("+guiEnv+"$"+tempDataName+")) && " +
								"identical(colnames("+rDataName+"),colnames("+guiEnv+"$"+tempDataName+"))");
				if(strChange!=null && ((REXPLogical)strChange).isTRUE()[0])
					this.fireTableStructureChanged();
				if(strChange!=null)
					this.fireTableDataChanged();			
				Deducer.eval(guiEnv+"$"+tempDataName+"<-"+rDataName);
				changed=true;
			}
		}
		return changed;
	}
	
	public String getColumnName(int col){
		if(col<getRealColumnCount()){
			REXP colName = Deducer.eval("colnames("+rDataName+")["+(col+1)+"]");
			if(colName.isString())
				try {
					return colName.asString();
				} catch (REXPMismatchException e) {
					return "?";
				}
			else
				return "?";
		}else{
			return "";
		}
	}
	
	public boolean isCellEditable(int rowIndex, int columnIndex){
		if(columnIndex<=getRealColumnCount())
			return true;
		else
			return false;
	}
	/**
	 * 		Deletes the cached data frame from the gui environment
	 */
	public void removeCachedData(){
		boolean tempStillExists = ((REXPLogical)Deducer.eval("exists('"+tempDataName+"',where="+guiEnv+",inherits=FALSE)")).isTRUE()[0];
		if(tempStillExists)
			Deducer.eval("rm("+tempDataName+",envir="+guiEnv+")");		
	}
	
	protected void finalize() throws Throwable {
		removeCachedData();
		super.finalize();
	}
	
	class RowNamesModel extends RowNamesListModel{
		
		public int getSize() { 
			return getRowCount(); 
		}
		
		public Object getElementAt(int index) {
			if(index<getRealRowCount()){
				REXP rRowName =Deducer.eval("rownames("+rDataName+")["+(index+1)+"]");
				if(rRowName.isString())
					try {
						return rRowName.asString();
					} catch (REXPMismatchException e) {
						return "?";
					}
				else
					return "?";
			}else
				return new Integer(index+1).toString();
		}
		
		public void initHeaders(int n){}
		
		public int getMaxNumChar(){
			String[] rowNames={""};
			try {
				rowNames = Deducer.eval("rownames("+rDataName+")").asStrings();
			} catch (REXPMismatchException e) {}
			int max = 0;
			for(int i=0;i<rowNames.length;i++){
				max = Math.max(max,rowNames[i].length());
			}
			return max;
		}
		
	}
	
	public RowNamesListModel getRowNamesModel() { return rowNamesModel;}
	public void setRowNamesModel(RowNamesModel model){rowNamesModel = model;}
	
	public class NAHolder{
		public String toString(){
			return "NA";
		}
	}
	
	/**
	 * 		Implements nice printing of NAs, as well as left alignment
	 * 		for strings and right alignment for numbers.
	 * 
	 * @author ifellows
	 *
	 */
	public class RCellRenderer extends ExCellRenderer
	{
		public Component getTableCellRendererComponent(JTable table,
						Object value, boolean selected, boolean focused,
						int row, int column){
			super.getTableCellRendererComponent(table, value,
					selected, focused, row, column);

			if(row<getRealRowCount() || column<getRealColumnCount()){
				if(value instanceof NAHolder){
					setHorizontalAlignment(RIGHT);
					setVerticalAlignment(BOTTOM);
					Font f = new Font("Dialog", Font.PLAIN, 6);
					setFont(f);					
				}else if(value instanceof String){
					setHorizontalAlignment(LEFT);	
					setVerticalAlignment(CENTER);
				}else{
					setHorizontalAlignment(RIGHT);
					setVerticalAlignment(CENTER);					
				}
			}
			
			return this;
		}
	}
	


	
	
}

