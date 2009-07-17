package org.rosuda.deducer.models;

import javax.swing.DefaultListModel;

import org.rosuda.deducer.Deducer;

import org.rosuda.JGR.*;

public class LogisticDialogSplitModel {
	public DefaultListModel levels = new DefaultListModel();
	public DefaultListModel suc = new DefaultListModel();
	public String cutValue = "";
	public String cutDirection = ">";
	public String expr = "";
	public int which = 1; //1=cut,2=define,3=expression
	public boolean isNumeric = false;
	public String variable;
	
	public LogisticDialogSplitModel(String s){
		variable = s;
		
		String[] levs = Deducer.rniEval("levels(factor("+s+"))").asStringArray();
		if(levs.length<50){
			for(int i=0;i<levs.length;i++)
				levels.addElement(levs[i]);
		}
		
		isNumeric = Deducer.rniEval("is.numeric("+s+")").asBool().isTRUE();
	
		if(isNumeric){
			cutValue = Deducer.rniEval("median("+s+",na.rm=TRUE)").asDouble()+"";
			which=1;
		}else{
			suc.addElement(levels.lastElement());
			levels.removeElement(levels.lastElement());
			which=2;
		}
		
	}
	public String getLHS(){
		if(which==1){
			return variable.substring(variable.indexOf("$")+1)+cutDirection+cutValue;
		}if(which==2){
			boolean isChar = Deducer.rniEval("is.character("+variable+")").asBool().isTRUE();
			boolean isFactor = Deducer.rniEval("is.factor("+variable+")").asBool().isTRUE();
			if(isChar || isFactor){
				if(suc.size()==1)
					return variable.substring(variable.indexOf("$")+1)+ "=='"+suc.get(0)+"'";
				else
					return variable.substring(variable.indexOf("$")+1)+" %in% "+
						RController.makeRStringVector(suc);
			}else{
				if(suc.size()==1)
					return variable.substring(variable.indexOf("$")+1) +"=="+suc.get(0)+"";
				else
					return variable.substring(variable.indexOf("$")+1)+" %in% "+
						RController.makeRVector(suc);
			}
		}else if(which==3){
			return expr;
		}
		return"";
	}
}
