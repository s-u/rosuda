package org.rosuda.deducer.models;

import java.util.ArrayList;
import java.util.Vector;


import org.rosuda.JGR.JGR;
import org.rosuda.JGR.RController;
import org.rosuda.JGR.util.ErrorMsg;
import org.rosuda.REngine.REXP;
import org.rosuda.REngine.REXPLogical;
import org.rosuda.REngine.REXPMismatchException;
import org.rosuda.deducer.Deducer;
import org.rosuda.deducer.data.ExDefaultTableModel;

public class LinearModel extends GLMModel {
	
	public boolean hccm = false;
	
	LinearModel(){
		super();
		plots.confInt=true;
	}
	
	
	public RModel run(boolean preview,RModel prevModel){
		RModel rModel = new RModel();
		String cmd = "";
		boolean envDefined = ((REXPLogical)Deducer.eval("'"+Deducer.guiEnv+"' %in% .getOtherObjects()")).isTRUE()[0];
		if(!envDefined){
			Deducer.eval(Deducer.guiEnv+"<-new.env(parent=emptyenv())");
		}
		
		String modelName ;
		if(preview)
			if(prevModel==null){
				modelName = Deducer.guiEnv+"$"+JGR.MAINRCONSOLE.getUniqueName("model.lm",Deducer.guiEnv);
			}else
				modelName = prevModel.modelName;
		else{
			if(export.modelName.equals("<auto>") || export.modelName.equals(""))
				modelName = JGR.MAINRCONSOLE.getUniqueName("model.lm");
			else
				modelName = RController.makeValidVariableName(export.modelName);
		}
		
		String dataName;
		if(preview){
			if(prevModel==null){
				dataName = Deducer.guiEnv+"$"+JGR.MAINRCONSOLE.getUniqueName(data,Deducer.guiEnv);
			}else
				dataName = prevModel.data;
		}else
			dataName = data;
		
		String formula=Deducer.makeFormula(outcomes, terms);
		cmd+=modelName+" <- lm(formula="+formula+",data="+dataName+
				(weights.getSize()==0 ? "" : ",weights="+weights.get(0))+
				((subset==null || subset.length()==0) ? "" : ",subset = "+subset)+
				",na.action=na.omit"+")";
		
		ArrayList tmp = new ArrayList();
		String[] out = new String[]{};	
		if(preview){
			Deducer.eval(dataName+"<-"+data);
			Deducer.eval(cmd);
			tmp.add("\n>"+cmd);
		}
		
		cmd=runOptions(cmd,modelName,preview,tmp);
		cmd=runPostHoc(cmd,modelName,preview,tmp);
		cmd=runEffects(cmd,modelName,preview,tmp,prevModel);
		cmd=runPlots(cmd,modelName,preview,tmp,prevModel);
		cmd=runTests(cmd,modelName,preview,tmp,prevModel);
		cmd=runExport(cmd,modelName,preview,tmp,dataName,true);
		
		if(!preview)
			JGR.MAINRCONSOLE.executeLater(cmd);
		String prev = "";
		for(int i =0;i<tmp.size();i++)
			prev+=tmp.get(i)+"\n";
		rModel.call=cmd;
		rModel.data=dataName;
		rModel.formula=formula;
		rModel.modelName=modelName;
		rModel.preview=prev;
		return rModel;
	}
	
	protected String runOptions(String cmd,String modelName,boolean preview,ArrayList tmp){
		try{
			String[] out = new String[]{};
			if(this.options.anova){
				String anovaCall = "Anova("+modelName+",type='"+options.type+"'"+
					(hccm? ",white.adjust='hc3')" : ")");
				if(preview){
					out = Deducer.eval("capture.output(try("+anovaCall+"))").asStrings();
					tmp.add("\n>"+anovaCall+"\n");
					for(int i=0;i<out.length;i++)
						tmp.add(out[i]);
				}else{
					cmd+="\n"+anovaCall;
				}
			}
			if(this.options.summary){
				String summaryCall = "summary("+modelName+(options.paramCor ?",correlation=TRUE":"")+
											(hccm? ",white.adjust='hc3'":"")+")";
				if(preview){
					out = Deducer.eval("capture.output(print(try("+summaryCall+")))").asStrings();
					tmp.add("\n>"+summaryCall+"\n");
					for(int i=0;i<out.length;i++)
						tmp.add(out[i]);
				}else{
					cmd+="\n"+summaryCall;
				}
			}
			
			if(this.options.vif){
				String vifCall = "vif("+modelName+")";
				if(preview){
					out = Deducer.eval("capture.output("+vifCall+")").asStrings();
					tmp.add("\n>"+vifCall+"\n");
					for(int i=0;i<out.length;i++)
						tmp.add(out[i]);
				}else{
					cmd+="\n"+vifCall;
				}
			}
			
			if(this.options.influence){
				String infCall = "summary(influence.measures("+modelName+"))";
				if(preview){
					out = Deducer.eval("capture.output("+infCall+")").asStrings();
					tmp.add("\n>"+infCall+"\n");
					for(int i=0;i<out.length;i++)
						tmp.add(out[i]);
				}else{
					cmd+="\n"+infCall;
				}
			}
		}catch (Exception e) {
			e.printStackTrace();
			new ErrorMsg(e);			
		}

		return cmd;
	}
	

	protected String runPostHoc(String cmd,String modelName,boolean preview,ArrayList tmp){
		String[] out = new String[]{};
		if(posthoc.posthoc.size()>0){
			String postCall = "";
			String cor = "univariate()";
			if(posthoc.correction!="No Correction"){
				cor = "adjusted(\""+posthoc.correction+"\")";
			}
			for(int i=0;i<posthoc.posthoc.getSize();i++){

				postCall = "summary(glht("+modelName+",linfct=mcp('"+posthoc.posthoc.get(i)+
							"'=\""+posthoc.type+"\")"+
							(hccm ? ",vcov=function(model) hccm(model)":"")+
							"),test="+cor+")";
				if(preview){
					try {
						out = Deducer.eval("capture.output("+postCall+")").asStrings();
					} catch (Exception e) {
						out = new String[]{""};
						posthoc = new PostHoc();
						return cmd;
					}
					tmp.add("\n>"+postCall+"\n");
					for(int j=0;j<out.length;j++)
						tmp.add(out[j]);
				}else{
					cmd+="\n"+postCall;
				}
				if(posthoc.confInt){
					postCall = "confint(glht("+modelName+",linfct=mcp('"+posthoc.posthoc.get(i)+
					"'=\""+posthoc.type+"\")"+
					(hccm ? ",vcov=function(model) hccm(model)":"")+					
					"))";
					if(preview){
						try {
							out = Deducer.eval("capture.output("+postCall+")").asStrings();
						} catch (Exception e) {
							out = new String[]{""};
						}
						tmp.add("\n>"+postCall+"\n");
						for(int j=0;j<out.length;j++)
							tmp.add(out[j]);
					}else{
						cmd+="\n"+postCall;
					}
				}
			}
		}
		return cmd;
	}
	

	protected String runTests(String cmd,String modelName,boolean preview,ArrayList tmp, 
								RModel prevModel){
		String[] out = new String[]{};
		if(tests.size()>0){
			String[] t = new String[1];
			try{
				if(prevModel!=null){
					t=Deducer.eval("names(coef("+prevModel.modelName+
										"))").asStrings();
				}else if(preview){
					t=Deducer.eval("names(coef("+modelName+
										"))").asStrings();
				}
			}catch (Exception e) {
				e.printStackTrace();
				new ErrorMsg(e);			
			}
			Vector testCalls = new Vector();
			String matrixName;
			if(preview)
				matrixName =  Deducer.guiEnv+"$"+JGR.MAINRCONSOLE.getUniqueName(
													"lh.mat",Deducer.guiEnv);
			else
				matrixName = JGR.MAINRCONSOLE.getUniqueName("lh.mat");
			String call = "";
			for(int i=0;i<tests.size();i++){
				ExDefaultTableModel tmod = tests.getModel(i);
				if((prevModel!=null && tmod.getColumnCount()!=t.length+1) ||
						(prevModel==null && preview && tmod.getColumnCount()!=t.length+1))
					continue;
				
				Vector row = new Vector();
				Vector rhs = new Vector();
				call = matrixName +"<-rbind(";
				for(int j=0;j<tmod.getRowCount();j++){
					row.clear();
					for(int k=0;k<tmod.getColumnCount()-1;k++)
						row.add(tmod.getValueAt(j, k));
					call+= RController.makeRVector(row);
					if(j<tmod.getRowCount()-1)
						call+=",\n\t";
					else
						call+=")";
					rhs.add(tmod.getValueAt(j, tmod.getColumnCount()-1));
				}
				testCalls.add(call);
				call = "lht("+modelName +","+matrixName+","+RController.makeRVector(rhs)+
				(hccm ? ",white.adjust='hc3'":"")+")";
				testCalls.add(call);
			}
			if(testCalls.size()>0)
				testCalls.add("rm('"+matrixName+"')");
			if(preview){
				String testCall;
				for(int i=0;i<testCalls.size();i++){
					testCall=(String)testCalls.get(i);
					REXP r =Deducer.eval("capture.output("+testCall.replaceAll("\n", "").replaceAll("\t", "")+")");
					if(r!=null)
						try {
							out = r.asStrings();
						} catch (REXPMismatchException e) {
							e.printStackTrace();
						}
					else
						out =new String[] {"Error"};
					tmp.add("\n>"+testCall+"\n");
					
					for(int j=0;j<out.length;j++)
						tmp.add(out[j]);
				}
			}else{
				String testCall;
				for(int i=0;i<testCalls.size();i++){
					testCall=(String)testCalls.get(i);
					cmd+="\n"+testCall;
				}
			}
		}
		return cmd;
	}
	

	
}
