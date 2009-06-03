package org.rosuda.deducer.menu.twosample;


import java.util.Vector;

import javax.swing.DefaultListModel;
import javax.swing.JOptionPane;

import org.rosuda.JGR.JGR;
import org.rosuda.JGR.RController;
import org.rosuda.deducer.menu.SubsetDialog;


public class TwoSampleModel{
	public String dataName=null;
	public DefaultListModel variables = new DefaultListModel();
	public DefaultListModel factorName = new DefaultListModel();
	public String subset = "";
	
	public boolean doT=true;
	public boolean doBoot=false;
	public boolean doKS=false;
	public boolean doMW=false;
	public boolean doBM=false;
	public boolean tEqVar=false;
	public String bootStat="t";
	
	public OptionsModel optMod = new OptionsModel();
	public SplitModel splitMod = new SplitModel();
	
	public class OptionsModel{
		public boolean descriptives = true;
		public String digits = "<auto>";
		public String alternative = "two.sided";
		public double confLevel=.95;
	}		
	
	public class SplitModel{
		public boolean isCut=false;
		public String cutPoint="";
		public Vector group1 = new Vector();
		public Vector group2 = new Vector();
		
		public String getFactorName(){
			if(factorName.size()>0)
				return (String) factorName.firstElement();
			else
				return null;
		}
		public String getDataName(){
			return dataName;
		}
		
	}
	
	public boolean run(){
		
		if(dataName==null)
			return false;
		if(variables.size()==0){
			JOptionPane.showInternalMessageDialog(null, "Please select some outcome variables.");
			return false;
		}
		if(factorName.size()==0){
			JOptionPane.showInternalMessageDialog(null, "Please select a factor.");
			return false;			
		}
		subset = subset.trim();
		String cmd="";
		String subn;
		String outcomes = RController.makeRVector(variables);
		String factor = (String) factorName.get(0);
		
		if(splitMod.isCut){
			try{
				Double.parseDouble(splitMod.cutPoint);
			}catch(Exception e){
				splitMod.cutPoint="\""+splitMod.cutPoint+"\"";
			}
		}
		
		boolean isSubset=false;
		if(!subset.equals("") ){
			if(!SubsetDialog.isValidSubsetExp(subset,dataName)){
				JOptionPane.showMessageDialog(null, "Sorry, the subset expression seems to be invalid.");
				return false;
			}
			subn = JGR.MAINRCONSOLE.getUniqueName(dataName+".sub");
			cmd=subn+"<-subset("+dataName+","+subset+")"+"\n";
			isSubset=true;
		}else
			subn=dataName;
		if(optMod.descriptives){
			cmd+="descriptive.table("+subn+"["+RController.makeRStringVector(variables)+"],"+
							"func.names =c(\"Mean\",\"St. Deviation\",\"Valid N\"))\n";
		}
		if(doT){
			cmd += "print(two.sample.test(variables="+outcomes+",\n\t\tfactor.var="+factor+",\n\t\tdata="+subn+
				",\n\t\ttest=t.test"+
				(tEqVar?",\n\t\tvar.equal=TRUE":"")+
				(optMod.confLevel==.95 ? "" : ",\n\t\tconf.level="+optMod.confLevel)+	
				",\n\t\talternative=\""+optMod.alternative+"\""+
				(splitMod.isCut ?",\n\t\tcut="+splitMod.cutPoint:"")+
				(!splitMod.isCut && splitMod.group1.size()>0 ? ",\n\t\tgroup1="+ RController.makeRStringVector(splitMod.group1): "")+
				(!splitMod.isCut && splitMod.group2.size()>0 ? ",\n\t\tgroup2="+ RController.makeRStringVector(splitMod.group2): "")+
				")"+
				(optMod.digits.trim().equals("<auto>") ? "\n)" : ",\n\tdigits="+optMod.digits+")")+"\n";
		}
		if(doMW){
			cmd += "print(two.sample.test(variables="+outcomes+",\n\t\tfactor.var="+factor+",\n\t\tdata="+subn+
				",\n\t\ttest=wilcox.test"+
				(optMod.confLevel==.95 ? "" : ",\n\t\tconf.level="+optMod.confLevel)+	
				",\n\t\talternative=\""+optMod.alternative+"\""+
				(",\n\t\t correct=FALSE")+
				(splitMod.isCut ?",\n\t\tcut="+splitMod.cutPoint:"")+
				(!splitMod.isCut && splitMod.group1.size()>0 ? ",\n\t\tgroup1="+ RController.makeRStringVector(splitMod.group1): "")+
				(!splitMod.isCut && splitMod.group2.size()>0 ? ",\n\t\tgroup2="+ RController.makeRStringVector(splitMod.group2): "")+
				")"+
				(optMod.digits.trim().equals("<auto>") ? "\n)" : ",\n\tdigits="+optMod.digits+")")+"\n";
		}
		if(doBoot){
			cmd += "print(two.sample.test(variables="+outcomes+",\n\t\tfactor.var="+factor+",\n\t\tdata="+subn+
				",\n\t\ttest=perm.t.test"+
				(optMod.confLevel==.95 ? "" : ",\n\t\tconf.level="+optMod.confLevel)+	
				",\n\t\talternative=\""+optMod.alternative+"\""+
				(",\n\t\tstatistic='"+bootStat+"'")+
				(splitMod.isCut ?",\n\t\tcut="+splitMod.cutPoint:"")+
				(!splitMod.isCut && splitMod.group1.size()>0 ? ",\n\t\tgroup1="+ RController.makeRStringVector(splitMod.group1): "")+
				(!splitMod.isCut && splitMod.group2.size()>0 ? ",\n\t\tgroup2="+ RController.makeRStringVector(splitMod.group2): "")+
				")"+
				(optMod.digits.trim().equals("<auto>") ? "\n)" : ",\n\tdigits="+optMod.digits+")")+"\n";
		}
		if(doKS){
			cmd += "print(two.sample.test(variables="+outcomes+",\n\t\tfactor.var="+factor+",\n\t\tdata="+subn+
				",\n\t\ttest=ks.test"+
				(optMod.confLevel==.95 ? "" : ",\n\t\tconf.level="+optMod.confLevel)+	
				",\n\t\talternative=\""+optMod.alternative+"\""+
				(splitMod.isCut ?",\n\t\tcut="+splitMod.cutPoint:"")+
				(!splitMod.isCut && splitMod.group1.size()>0 ? ",\n\t\tgroup1="+ RController.makeRStringVector(splitMod.group1): "")+
				(!splitMod.isCut && splitMod.group2.size()>0 ? ",\n\t\tgroup2="+ RController.makeRStringVector(splitMod.group2): "")+
				")"+
				(optMod.digits.trim().equals("<auto>") ? "\n)" : ",\n\tdigits="+optMod.digits+")")+"\n";
		}
		if(doBM){
			String packages = RController.getCurrentPackages();
			if(!packages.contains("lawstat")){
				JGR.R.eval("cat('Package lawstat not found. Attempting to download...\n')");
				JGR.MAINRCONSOLE.execute("installPackages('lawstat');library(lawstat)",true);	
			}
			else
				cmd+=("library(lawstat)")+"\n";
			cmd += "print(two.sample.test(variables="+outcomes+",\n\t\tfactor.var="+factor+",\n\t\tdata="+subn+
				",\n\t\ttest=brunner.munzel.test"+
				(optMod.confLevel==.95 ? "" : ",\n\t\tconf.level="+optMod.confLevel)+	
				",\n\t\talternative=\""+optMod.alternative+"\""+
				(splitMod.isCut ?",\n\t\tcut="+splitMod.cutPoint:"")+
				(!splitMod.isCut && splitMod.group1.size()>0 ? ",\n\t\tgroup1="+ RController.makeRStringVector(splitMod.group1): "")+
				(!splitMod.isCut && splitMod.group2.size()>0 ? ",\n\t\tgroup2="+ RController.makeRStringVector(splitMod.group2): "")+
				")"+
				(optMod.digits.trim().equals("<auto>") ? "\n)" : ",\n\tdigits="+optMod.digits+")")+"\n";
		}
		if(isSubset)
			cmd+="rm("+subn+")\n";
		JGR.MAINRCONSOLE.execute(cmd);
		return true;
	}
	
}

