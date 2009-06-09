package org.rosuda.deducer.data;


import java.awt.Component;
import java.awt.Font;

import javax.swing.*;

import org.rosuda.JRI.*;
import org.rosuda.JGR.JGR;


/**
 * Data Frame model
 * 
 * @author ifellows
 *
 */
class RDataFrameModel extends ExDefaultTableModel {
	
	private static String guiEnv = "gui.working.env";
	
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
		boolean envDefined = JGR.R.eval("'"+guiEnv+"' %in% .getOtherObjects()").asBool().isTRUE();
		if(!envDefined){
			JGR.R.eval(guiEnv+"<-new.env(parent=emptyenv())");
		}
		if(tempDataName!=null)
			JGR.R.eval("rm("+tempDataName+",envir="+guiEnv+")");
		rDataName = name;
		tempDataName = JGR.MAINRCONSOLE.getUniqueName(rDataName,guiEnv);
		JGR.R.eval(guiEnv+"$"+tempDataName+"<-"+rDataName);
		this.fireTableStructureChanged();
		this.fireTableDataChanged();
	}
	
	public int getColumnCount( ){
		if(rDataName!=null){
			if(JGR.R.eval("!exists('"+rDataName+"')").asBool().isTRUE())
				return 0;
			return JGR.R.eval("ncol("+rDataName+")").asInt()+numExtensionColumns;
		}else
			return 0;
	}
	
	public int getRealColumnCount( ){
		if(rDataName!=null){
			if(JGR.R.eval("!exists('"+rDataName+"')").asBool().isTRUE())
				return 0;
			return JGR.R.eval("ncol("+rDataName+")").asInt();
		}else
			return 0;
	}

	public int getRealRowCount(){
		if(rDataName!=null){
			if(JGR.R.eval("!exists('"+rDataName+"')").asBool().isTRUE())
				return 0;
			return JGR.R.eval("nrow("+rDataName+")").asInt();
		}else
			return 0;
	}
	
	public int getRowCount(){
		if(rDataName!=null){
			if(JGR.R.eval("!exists('"+rDataName+"')").asBool().isTRUE())
				return 0;
			return JGR.R.eval("nrow("+rDataName+")").asInt()+numExtensionRows;
		}else
			return 0;
	}
	
	public void removeColumn(int colNumber){
		if(colNumber<getRealColumnCount()){
			JGR.R.eval(rDataName+"<-"+rDataName+"[,-"+(colNumber+1)+"]");
			refresh();
		}
	}

	public void removeRow(int row){
		if((row+1)<=getRealRowCount()){
			JGR.R.eval(rDataName + "<- "+rDataName + "[-"+(row+1)+",]");
			refresh();
		}
	}
	
	public void insertNewColumn(int col){
		JGR.R.eval(rDataName+"<-data.frame("+rDataName+"[,1:"+col+"],V=as.integer(NA),"+
				rDataName+"[,"+(col+1)+":"+getRealColumnCount()+"])");
		refresh();
	}
	
	public void insertNewRow(int row){
		int rowCount =getRealRowCount();
		setValueAt("NA",rowCount,0);
		JGR.R.eval("attr("+rDataName+",'row.names')["+(rowCount+1)+"]<-'New'");
		JGR.R.eval(rDataName+"<-rbind("+rDataName+"[1:"+row+",],"+rDataName+
				"["+(rowCount+1)+",],"+rDataName+"["+(row+1)+":"+rowCount+",])");
		JGR.R.eval("rownames("+rDataName+")<-make.unique(rownames("+rDataName+"))");
		
	}
	
	
	public Object getValueAt(int row, int col){
		if(JGR.R.eval("!exists('"+rDataName+"')").asBool().isTRUE())
			return "?";
		if(row>=getRealRowCount() || col>=getRealColumnCount()){
			return "";
		}
		REXP value = JGR.R.eval(rDataName+"["+(row+1)+","+(col+1)+"]");
		int type =value.getType();
		if(JGR.R.eval("is.na("+rDataName+"["+(row+1)+","+(col+1)+"])").asBool().isTRUE())
			return "NA";
		if(type == REXP.XT_ARRAY_DOUBLE)
			return (new Double(value.asDouble()));
		else if(type ==REXP.XT_STR)
			return value.asString();
		else if(type == REXP.XT_ARRAY_INT){
			return new Integer(value.asInt());
		}else if(type == REXP.XT_FACTOR)
			return value.asFactor().at(0);
		else if( type == REXP.XT_ARRAY_BOOL || type == REXP.XT_ARRAY_BOOL_INT || type == REXP.XT_BOOL){
			return value.asBool();
		}
		return "?";
	}
	public void setValueAt(Object value,int row, int col){	
		if(!this.isCellEditable(row, col))
			return;
		REXP currentValue = JGR.R.eval(rDataName+"["+(row+1)+","+(col+1)+"]");	
		int type =currentValue.getType();
		int numRealRows =getRealRowCount();
		int numRealCols =getRealColumnCount();
		//JGR.R.eval("print('"+type+"')");
		this.fireTableCellUpdated(row, col);	
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
				JGR.R.eval(rDataName+"["+(row+1)+","+(col+1)+"]<-NA");
		}else if(type == REXP.XT_NULL){
			if(!isDouble){
				if(value.toString().equals("NA"))
					JGR.R.eval(rDataName+"["+(row+1)+","+(col+1)+"]<-NA");
				else
					JGR.R.eval(rDataName+"["+(row+1)+","+(col+1)+"]<-'"+value.toString()+"'");
			}else{
				JGR.R.eval(rDataName+"["+(row+1)+","+(col+1)+"]<-"+valueString);

			}
		}else if( value.toString().equals("NA")){
			JGR.R.eval(rDataName+"["+(row+1)+","+(col+1)+"]<-NA");
		}else if(type ==REXP.XT_STR){
			JGR.R.eval(rDataName+"["+(row+1)+","+(col+1)+"]<-'"+value.toString()+"'");
		}else if(type == REXP.XT_ARRAY_DOUBLE){
			if(!isDouble){
				JGR.R.eval(rDataName+"[,"+(col+1)+"]<-as.character("+rDataName+"[,"+(col+1)+"])");
				JGR.R.eval(rDataName+"["+(row+1)+","+(col+1)+"]<-'"+value.toString()+"'");
			}else{
				JGR.R.eval(rDataName+"["+(row+1)+","+(col+1)+"]<-"+valueString);
			}
		}else if(type == REXP.XT_ARRAY_INT){
			if(!isInteger){
				if(!isDouble){
					JGR.R.eval(rDataName+"[,"+(col+1)+"]<-as.character("+rDataName+"[,"+(col+1)+"])");
					JGR.R.eval(rDataName+"["+(row+1)+","+(col+1)+"]<-'"+value.toString()+"'");
				}else{
					JGR.R.eval(rDataName+"["+(row+1)+","+(col+1)+"]<-"+valueString);
					this.fireTableDataChanged();
				}
			}else{
				JGR.R.eval(rDataName+"["+(row+1)+","+(col+1)+"]<-"+valueString+"L");
			}
		}else if(type == REXP.XT_FACTOR){
			boolean isNewLevel=JGR.R.eval("'"+value.toString()+"' %in% " +
					"levels(" +rDataName+"[,"+(col+1)+"])").asBool().isFALSE();

			if(isNewLevel){
				String addLevel = "levels(" +rDataName+"[,"+(col+1)+"])<-c("+
						"levels(" +rDataName+"[,"+(col+1)+"]),'"+value.toString()+"')";
				JGR.R.eval(addLevel);
			}
			JGR.R.eval(rDataName+"["+(row+1)+","+(col+1)+"]<-'"+value.toString()+"'");
		}else if( type == REXP.XT_ARRAY_BOOL || type == REXP.XT_ARRAY_BOOL_INT 
				|| type == REXP.XT_BOOL){
			if(valueString.equals("1") || valueString.toLowerCase().equals("true")
					|| valueString.toLowerCase().equals("t"))
				JGR.R.eval(rDataName+"["+(row+1)+","+(col+1)+"]<-TRUE");
			else if(valueString.equals("0") || valueString.toLowerCase().equals("false")
					|| valueString.toLowerCase().equals("f"))
				JGR.R.eval(rDataName+"["+(row+1)+","+(col+1)+"]<-FALSE");
			else
				JGR.R.eval(rDataName+"["+(row+1)+","+(col+1)+"]<-'"+valueString+"'");
		}
		
		if((row+1)>numRealRows){
			JGR.R.eval("rownames("+rDataName+")<-make.unique(rownames("+rDataName+"))");
			this.fireTableRowsInserted(numRealRows,row);			
			this.fireTableRowsUpdated(numRealRows,row);
		}
		if((col+1)>numRealCols){
			this.fireTableDataChanged();
			this.addColumn(JGR.R.eval("colnames("+rDataName+")["+(col+1)+"]").asString());
		}
		JGR.R.eval(guiEnv+"$"+tempDataName+"<-"+rDataName);
	}
	/**
	 * Notifies components about changes in the model
	 */
	public boolean refresh(){
		boolean changed = false;
		REXP exist = JGR.R.idleEval("exists('"+rDataName+"')");
		if(exist!=null && exist.asBool().isTRUE()){
			REXP ident =JGR.R.idleEval("identical("+rDataName+","+guiEnv+"$"+tempDataName+")"); 
			if(ident!=null && ident.asBool().isFALSE()){
				REXP strChange = JGR.R.idleEval("all(dim("+rDataName+")==dim("+guiEnv+"$"+tempDataName+")) && " +
								"identical(colnames("+rDataName+"),colnames("+guiEnv+"$"+tempDataName+"))");
				if(strChange!=null && strChange.asBool().isFALSE())
					this.fireTableStructureChanged();
				if(strChange!=null)
					this.fireTableDataChanged();			
				JGR.R.idleEval(guiEnv+"$"+tempDataName+"<-"+rDataName);
				changed=true;
			}
		}
		return changed;
	}
	
	public String getColumnName(int col){
		if(col<getRealColumnCount()){
			REXP colName = JGR.R.eval("colnames("+rDataName+")["+(col+1)+"]");
			if(colName.getType() ==REXP.XT_STR)
				return colName.asString();
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
		boolean tempStillExists = JGR.R.eval("exists('"+tempDataName+"',where="+guiEnv+",inherits=FALSE)").asBool().isTRUE();
		if(tempStillExists)
			JGR.R.eval("rm("+tempDataName+",envir="+guiEnv+")");		
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
				REXP rRowName =JGR.R.eval("rownames("+rDataName+")["+(index+1)+"]");
				if(rRowName.getType() ==REXP.XT_STR)
					return rRowName.asString();
				else
					return "?";
			}else
				return new Integer(index+1).toString();
		}
		
		public void initHeaders(int n){}
		
		public int getMaxNumChar(){
			String[] rowNames = JGR.R.eval("rownames("+rDataName+")").asStringArray();
			int max = 0;
			for(int i=0;i<rowNames.length;i++){
				max = Math.max(max,rowNames[i].length());
			}
			return max;
		}
		
	}
	
	public RowNamesListModel getRowNamesModel() { return rowNamesModel;}
	public void setRowNamesModel(RowNamesModel model){rowNamesModel = model;}
	
	
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
				boolean isNA = value.toString().equals("NA");
				if(isNA){
					if(JGR.R.eval("is.na("+rDataName+"["+(row+1)+","+(column+1)+"])").asBool().isTRUE()){
						setHorizontalAlignment(RIGHT);
						setVerticalAlignment(BOTTOM);
						Font f = new Font("Dialog", Font.PLAIN, 6);
						setFont(f);
					}else{
						setHorizontalAlignment(LEFT);
						setVerticalAlignment(CENTER);
					}
				}else{
					REXP rValue = JGR.R.eval(rDataName+"["+(row+1)+","+(column+1)+"]");
					int type = rValue.getType();
					if( type==REXP.XT_STR || type == REXP.XT_FACTOR )
						setHorizontalAlignment(LEFT);
					else 
						setHorizontalAlignment(RIGHT);
					setVerticalAlignment(CENTER);
				}
			}
			
			return this;
		}
	}
	
	
	
	
}

