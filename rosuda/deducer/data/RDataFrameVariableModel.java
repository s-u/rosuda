


package org.rosuda.deducer.data;

import javax.swing.*;
import javax.swing.table.*;

import org.rosuda.JRI.*;
import org.rosuda.JGR.JGR;
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
		if(rDataName!=null)
			return 5;
		else
			return 0;
	}
	
	public int getRowCount(){
		if(Deducer.rniEval("!exists('"+rDataName+"')").asBool().isTRUE())
			return 0;
		if(rDataName!=null)
			return Deducer.rniEval("ncol("+rDataName+")").asInt()+numExtraColumns;
		else
			return 0;
	}
	
	public Object getValueAt(int row, int col){
		if(row>=(getRowCount()-numExtraColumns)){
			return null;
		}else if(col==0){
			return Deducer.rniEval("colnames("+rDataName+")["+(row+1)+"]").asString();
		}else if(col==1){
			int xt = Deducer.rniEval(rDataName+"[,"+(row+1)+"]").getType();
			if (xt==REXP.XT_NULL) return "NULL";
			if (xt==REXP.XT_ARRAY_INT) return "Integer";
			if (xt==REXP.XT_ARRAY_STR) return "String";
			if (xt==REXP.XT_ARRAY_DOUBLE) return "Double";
			if (xt==REXP.XT_ARRAY_BOOL) return "Logical";
			if (xt==REXP.XT_ARRAY_BOOL_INT) return "Logical";
			if (xt==REXP.XT_FACTOR) return "Factor";
			return "?";
		}else if(col==2){
			int type = Deducer.rniEval(rDataName+"[,"+(row+1)+"]").getType();
			//Deducer.rniEval("print('"+type+"')");
			if(type == REXP.XT_FACTOR){
				String[] levels = Deducer.rniEval("levels("+rDataName+"[,"+(row+1)+"])").asStringArray();
				//Deducer.rniEval("print('"+levels.length+"')");
				String lev = "";
				for(int i=0;i<levels.length;i++){
					lev=lev.concat("("+(i+1)+") ");
					lev=lev.concat(levels[i]);	
					lev=lev.concat("; ");
				}
				return lev;
			}else 
				return null;
		}else
			return null;
	}
	
	public void setValueAt(Object value,int row, int col){
		if(row>=(getRowCount()-numExtraColumns)){
			if(col==0){
				Deducer.rniEval(rDataName+"[,"+(row+1)+"]<-NA");	
				Deducer.rniEval("colnames("+rDataName+")["+(row+1)+"]<-'"+value.toString().trim()+"'");
				refresh();
				rowNamesModel.refresh();
			}else
				return;
		}else if(col==0){
			Deducer.rniEval("colnames("+rDataName+")["+(row+1)+"]<-'"+value.toString().trim()+"'");
		}else if(col==1){
			String type = value.toString().toLowerCase().trim();
			if(type.equals("integer")) Deducer.rniEval(rDataName+"[,"+(row+1)+
										"]<-as.integer("+rDataName+"[,"+(row+1)+"])");
			if(type.equals("factor")) Deducer.rniEval(rDataName+"[,"+(row+1)+
					"]<-as.factor("+rDataName+"[,"+(row+1)+"])");
			if(type.equals("double")) Deducer.rniEval(rDataName+"[,"+(row+1)+
					"]<-as.double("+rDataName+"[,"+(row+1)+"])");
			if(type.equals("logical")) Deducer.rniEval(rDataName+"[,"+(row+1)+
					"]<-as.logical("+rDataName+"[,"+(row+1)+"])");
			if(type.equals("string")) Deducer.rniEval(rDataName+"[,"+(row+1)+
					"]<-as.character("+rDataName+"[,"+(row+1)+"])");
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
		//this.fireTableStructureChanged();
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
			if(Deducer.rniEval("!exists('"+rDataName+"')").asBool().isTRUE())
				return 0;
			return Deducer.rniEval("ncol("+rDataName+")").asInt()+numExtraColumns;
		}
	}


}
