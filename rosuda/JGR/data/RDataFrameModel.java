package org.rosuda.JGR.data;

import java.awt.BorderLayout;
import javax.swing.*;
import javax.swing.table.*;
import org.rosuda.JRI.*;
import org.rosuda.JGR.JGR;


class RDataFrameModel extends AbstractTableModel {
	String rDataName=null;
	
	public static final int numExtensionRows = 30;
	public static final int numExtensionColumns = 1; 
	
	public RDataFrameModel(){}
	
	public RDataFrameModel(String name){
		rDataName = name;
	}
	
	public String getDataName(){return rDataName;}
	
	public void setDataName(String name){rDataName = name;}
	
	public int getColumnCount( ){
		if(rDataName!=null)
			return JGR.R.eval("ncol("+rDataName+")").asInt()+numExtensionColumns;
		else
			return 0;
	}
	
	public int getRealColumnCount( ){
		if(rDataName!=null)
			return JGR.R.eval("ncol("+rDataName+")").asInt();
		else
			return 0;
	}

	public int getRealRowCount(){
		if(rDataName!=null)
			return JGR.R.eval("nrow("+rDataName+")").asInt();
		else
			return 0;
	}
	
	public int getRowCount(){
		if(rDataName!=null)
			return JGR.R.eval("nrow("+rDataName+")").asInt()+numExtensionRows;
		else
			return 0;
	}

	public Object getValueAt(int row, int col){
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
		return "?";
	}
	public void setValueAt(Object value,int row, int col){	
		//notify if outside dataframe range
		if(row>=getRealRowCount() || col>=getRealColumnCount()){
			this.fireTableStructureChanged();
			this.fireTableDataChanged();
		}	
		REXP currentValue = JGR.R.eval(rDataName+"["+(row+1)+","+(col+1)+"]");	
		int type =currentValue.getType();
		JGR.R.eval("print('"+currentValue.toString()+"')");
		String valueString = value.toString().trim();
		this.fireTableCellUpdated(row, col);
		if(value.toString().equals("NA")){
			JGR.R.eval(rDataName+"["+(row+1)+","+(col+1)+"]<-NA");
			return;
		}else if(type ==REXP.XT_STR){
			JGR.R.eval(rDataName+"["+(row+1)+","+(col+1)+"]<-'"+value.toString()+"'");
		}else if(type == REXP.XT_ARRAY_DOUBLE){
			try{
				Double.parseDouble(valueString);
			}catch(Exception e){
				JGR.R.eval(rDataName+"[,"+(col+1)+"]<-as.character("+rDataName+"[,"+(col+1)+"])");
				JGR.R.eval(rDataName+"["+(row+1)+","+(col+1)+"]<-'"+value.toString()+"'");
				return;
			}
			JGR.R.eval(rDataName+"["+(row+1)+","+(col+1)+"]<-"+valueString);
			return;
		}else if(type == REXP.XT_ARRAY_INT){
			try{
				Integer.parseInt(valueString);
			}catch(Exception e){
				JGR.R.eval(rDataName+"[,"+(col+1)+"]<-as.character("+rDataName+"[,"+(col+1)+"])");
				JGR.R.eval(rDataName+"["+(row+1)+","+(col+1)+"]<-'"+value.toString()+"'");
				return;
			}
			JGR.R.eval(rDataName+"["+(row+1)+","+(col+1)+"]<-"+valueString+"L");
			return;
		}else if(type == REXP.XT_FACTOR){
			boolean isNewLevel=JGR.R.eval("'"+value.toString()+"' %in% " +
					"levels(" +rDataName+"[,"+(col+1)+"])").asBool().isFALSE();

			if(isNewLevel){
				String addLevel = "levels(" +rDataName+"[,"+(col+1)+"])<-c("+
						"levels(" +rDataName+"[,"+(col+1)+"]),'"+value.toString()+"')";
				JGR.R.eval(addLevel);
			}
			JGR.R.eval(rDataName+"["+(row+1)+","+(col+1)+"]<-'"+value.toString()+"'");
			return;
		}else if(type == REXP.XT_NULL){
			try{
				Double.parseDouble(valueString);
			}catch(Exception e){
				JGR.R.eval(rDataName+"[,"+(col+1)+"]<-as.character("+rDataName+"[,"+(col+1)+"])");
				JGR.R.eval(rDataName+"["+(row+1)+","+(col+1)+"]<-'"+value.toString()+"'");
				//notify if outside dataframe range
				if(row>=getRealRowCount() || col>=getRealColumnCount()){
					this.fireTableStructureChanged();
					this.fireTableDataChanged();
				}	
				return;
			}
			JGR.R.eval(rDataName+"["+(row+1)+","+(col+1)+"]<-"+valueString);
			//notify if outside dataframe range
			if(row>=getRealRowCount() || (col+1)>=getRealColumnCount()){
				fireTableStructureChanged();
				fireTableDataChanged();
			}
		}else{
			//default case:
			//if it looks like a number assign it, otherwise assign it in quotes
			try{
				Double.parseDouble(valueString);
			}catch(Exception e){
				JGR.R.eval(rDataName+"[,"+(col+1)+"]<-as.character("+rDataName+"[,"+(col+1)+"])");
				JGR.R.eval(rDataName+"["+(row+1)+","+(col+1)+"]<-'"+value.toString()+"'");
				return;
			}
			JGR.R.eval(rDataName+"["+(row+1)+","+(col+1)+"]<-"+valueString);
			

		}
		
	}
	public boolean isCellEditable(int rowIndex, int columnIndex){
		return true;
	}
}

