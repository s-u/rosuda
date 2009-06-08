package org.rosuda.deducer.menu;

import javax.swing.DefaultListModel;
import javax.swing.JOptionPane;

import org.rosuda.JGR.JGR;
import org.rosuda.JGR.RController;

public class CorModel {
	
	public String dataName = "";
	public DefaultListModel variables = new DefaultListModel();
	public DefaultListModel with = new DefaultListModel();
	
	public String subset = "";
	public String method = "pearson";
	
	public OptModel options= new OptModel();
	public Plots plots= new Plots();
	
	public boolean run(){
		boolean withExists = true;
		if(dataName==null)
			return false;
		if(variables.size()==0){
			JOptionPane.showMessageDialog(null, "Please select one or more outcome variables.");
			return false;
		}
		if(with.size()==0){
			withExists=false;			
		}
		subset = subset.trim();
		String cmd="";
		String subn;
		String outcomes = RController.makeRVector(variables);
		String withVec;
		if(withExists)
			withVec = RController.makeRVector(with);
		else
			withVec ="";
		String name = JGR.MAINRCONSOLE.getUniqueName("corr.mat");
		if(dataName=="")
			return false;
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
		
		cmd+=name+"<-cor.matrix(variables="+outcomes+
				(withExists ? ",\n\twith.variables="+withVec :",")+
				",\n\t data="+subn+
				",\n\t test=cor.test"+
				",\n\t method='"+method+"'"+
				(options.confLevel==.95 ? "" : ",\n\tconf.level="+options.confLevel)+
				",\n\talternative=\""+options.alternative+"\""+")\n";
		if(options.showTable){
			cmd+="print("+name+(options.digits.equals("<auto>")?"":",digits="+options.digits)+
						(options.n?"":",N=FALSE")+(options.ci?"":",CI=FALSE")+(options.stat?"":",stat=FALSE")+
						(options.pValue?"":",p.value=FALSE")+")\n";
		}
		
		if(!plots.none){
			if(plots.scatterArray){
				cmd+="qscatter_array("+outcomes+
				",\n\t"+(withExists ? withVec : outcomes)+
				",\n\tdata="+subn+
				(!plots.common ? ",common.scales=FALSE":"")+")";
			}
			if(plots.saLines.equals("Linear")){
				cmd+=" + geom_smooth(method=\"lm\")\n";
			}else if(plots.saLines.equals("Smooth")){
				cmd+=" + geom_smooth()\n";
			}else
				cmd+="\n";
		}
		
		
		if(isSubset)
			cmd+="rm('"+subn+"','"+name+"')\n";
		else
			cmd+="rm('"+name+"')\n";
		
		JGR.MAINRCONSOLE.executeLater(cmd);
		return true;
	}
	
	public class OptModel{
		public boolean showTable=true;
		public boolean ci=true;
		public boolean n=true;
		public boolean stat=true;
		public boolean pValue=true;

		public String digits = "<auto>";
		public String alternative = "two.sided";
		public double confLevel=.95;
	}
	
	public class Plots{
		public boolean scatterArray =false;
		
		public boolean common =true;
		public String saLines= "Linear";
		
		public boolean matrix =false;
		public boolean ellipse =false;
		public boolean circles =false;
		public boolean none =true;
		
	}

	
}
