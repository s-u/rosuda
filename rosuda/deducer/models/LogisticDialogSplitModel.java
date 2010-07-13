package org.rosuda.deducer.models;

import javax.swing.DefaultListModel;

import org.rosuda.deducer.Deducer;

import org.rosuda.JGR.*;
import org.rosuda.JGR.util.ErrorMsg;
import org.rosuda.REngine.REXPLogical;

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
		try{
			String[] levs = Deducer.eval("levels(factor("+s+"))").asStrings();
			if(levs.length<50){
				for(int i=0;i<levs.length;i++)
					levels.addElement(levs[i]);
			}
			
			isNumeric = ((REXPLogical)Deducer.eval("is.numeric("+s+")")).isTRUE()[0];
		
			if(isNumeric){
				cutValue = Deducer.eval("median("+s+",na.rm=TRUE)").asDouble()+"";
				which=1;
			}else{
				suc.addElement(levels.lastElement());
				levels.removeElement(levels.lastElement());
				which=2;
			}
		}catch (Exception e) {
			e.printStackTrace();
			new ErrorMsg(e);			
		}
	}
	public String getLHS(){
		if(which==1){
			return variable.substring(variable.indexOf("$")+1)+cutDirection+cutValue;
		}if(which==2){
			boolean isChar = ((REXPLogical)Deducer.eval("is.character("+variable+")")).isTRUE()[0];
			boolean isFactor = ((REXPLogical)Deducer.eval("is.factor("+variable+")")).isTRUE()[0];
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
