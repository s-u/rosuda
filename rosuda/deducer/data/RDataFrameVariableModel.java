


package org.rosuda.deducer.data;


import javax.swing.JOptionPane;

import org.rosuda.REngine.REXP;
import org.rosuda.REngine.REXPLogical;
import org.rosuda.REngine.REXPMismatchException;
import org.rosuda.deducer.Deducer;


/**
 * A table for viewing and editing variable information
 * 
 * @author ifellows
 *
 */
class RDataFrameVariableModel extends ExDefaultTableModel {
	
	
	String rDataName=null;
	
	private VariableNumberListModel rowNamesModel;
	
	private final int numExtraColumns = 1;
	
	
	public RDataFrameVariableModel(){}
	
	public RDataFrameVariableModel(String name){
		rDataName = name;
		
	}
	
	public int getColumnCount( ){
			return 5;
	}
	
	public int getRowCount(){
		try{
			if(((REXPLogical)Deducer.eval("!exists('"+rDataName+"')")).isTRUE()[0])
				return 0;
			if(rDataName!=null)
				return Deducer.eval("ncol("+rDataName+")").asInteger()+numExtraColumns;
			else
				return 0;
		}catch(Exception e){return 0;}
	}
	
	public Object getValueAt(int row, int col){
		try{
			if(row>=(getRowCount()-numExtraColumns)){
				return null;
			}else if(col==0){
				return Deducer.eval("colnames("+rDataName+")["+(row+1)+"]").asString();
			}else if(col==1){
				REXP var = Deducer.eval(rDataName+"[,"+(row+1)+"]");
				if(var==null)
					return "?";
				REXP cls = var.getAttribute("class");
				String[] classes = null;
				String theClass = null;
				if(cls!=null){
					classes = cls.asStrings();
					if(classes.length>0)
						theClass = classes[classes.length-1];	
				}
				if(theClass!=null && theClass.equals("Date")) return "Date";
				else if(theClass!=null && theClass.equals("POSIXct")) return "Time";
				else if (var.isNull()) return "NULL";
				else if (var.isFactor()) return "Factor";
				else if (var.isInteger()) return "Integer";
				else if (var.isString()) return "Character";
				else if (var.isLogical()) return "Logical";
				else if (var.isNumeric()) return "Double";
				else return "Other";

			}else if(col==2){
				REXP var = Deducer.eval(rDataName+"[,"+(row+1)+"]");
				if(var.isFactor()){
					String[] levels = Deducer.eval("levels("+rDataName+"[,"+(row+1)+"])").asStrings();
					String lev = "";
					for(int i=0;i<Math.min(levels.length,50);i++){
						lev=lev.concat("("+(i+1)+") ");
						lev=lev.concat(levels[i]);	
						lev=lev.concat("; ");
					}
					return lev;
				}else 
					return "";
			}else
				return "";
		}catch(Exception e){return "?";}
	}
	
	public void setValueAt(Object value,int row, int col){
		if(row>=(getRowCount()-numExtraColumns)){
			if(col==0){
				Deducer.eval(rDataName+"[,"+(row+1)+"]<-NA");	
				Deducer.eval("colnames("+rDataName+")["+(row+1)+"]<-'"+value.toString().trim()+"'");
				refresh();
				rowNamesModel.refresh();
			}else
				return;
		}else if(col==0){
			Deducer.eval("colnames("+rDataName+")["+(row+1)+"]<-'"+value.toString().trim()+"'");
		}else if(col==1){
			String type = value.toString().toLowerCase().trim();
			if(type.equals("integer")) 
				Deducer.eval(rDataName+"[,"+(row+1)+"]<-as.integer("+rDataName+"[,"+(row+1)+"])");
			else if(type.equals("factor")) 
				Deducer.eval(rDataName+"[,"+(row+1)+"]<-as.factor("+rDataName+"[,"+(row+1)+"])");
			else if(type.equals("double")) 
				Deducer.eval(rDataName+"[,"+(row+1)+"]<-as.double("+rDataName+"[,"+(row+1)+"])");
			else if(type.equals("logical")) 
				Deducer.eval(rDataName+"[,"+(row+1)+"]<-as.logical("+rDataName+"[,"+(row+1)+"])");
			else if(type.equals("character")) 
				Deducer.eval(rDataName+"[,"+(row+1)+"]<-as.character("+rDataName+"[,"+(row+1)+"])");
			else if(type.equals("date")) 
				Deducer.eval(rDataName+"[,"+(row+1)+"]<-as.Date("+rDataName+"[,"+(row+1)+"])");
			else if(type.equals("time")) 
				Deducer.eval(rDataName+"[,"+(row+1)+"]<-as.POSIXct("+rDataName+"[,"+(row+1)+"])");			
			else if(type.equals("other"))
				JOptionPane.showMessageDialog(null, "Variables can not be changed to 'Other'");
			return;					
		}
	}
	
	public String getColumnName(int col){
		if(col==0){
			return "Variable";
		}else if(col==1){
			return "Type";	
		}else if(col==2){
			return "Factor Levels";
		}
		return "";
	}
	
	public void refresh(){
		this.fireTableDataChanged();
	}
	
	
	public class VariableNumberListModel extends RowNamesListModel{
		
		VariableNumberListModel(){
			rowNamesModel = this;
		}
		
		public Object getElementAt(int index) {
			return new Integer(index+1);
		}
		
		public int getSize() { 
			try {			
				if(((REXPLogical)Deducer.eval("!exists('"+rDataName+"')")).isTRUE()[0])
					return 0;
				return Deducer.eval("ncol("+rDataName+")").asInteger()+numExtraColumns;
			} catch (REXPMismatchException e) {
				return 0;
			}
		}
	}


}
