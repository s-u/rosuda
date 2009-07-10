package org.rosuda.deducer.models;

import java.util.ArrayList;
import java.util.Vector;

import javax.swing.DefaultListModel;

import org.rosuda.JGR.JGR;
import org.rosuda.JGR.RController;
import org.rosuda.deducer.Deducer;

public class GLMModel extends ModelModel {
	public DefaultListModel weights = new DefaultListModel();
	public String family = "gaussian()";
	public GLMOptions options = new GLMOptions();
	public PostHoc posthoc = new PostHoc();
	public Export export = new Export();
	public Effects effects = new Effects();
	public Plots plots = new Plots();
	
	public RModel run(boolean preview,RModel prevModel){
		RModel rModel = new RModel();
		String cmd = "";
		boolean envDefined = Deducer.rniEval("'"+Deducer.guiEnv+"' %in% .getOtherObjects()").asBool().isTRUE();
		if(!envDefined){
			Deducer.rniEval(Deducer.guiEnv+"<-new.env(parent=emptyenv())");
		}
		
		String modelName ;
		if(preview)
			if(prevModel==null){
				modelName = Deducer.guiEnv+"$"+JGR.MAINRCONSOLE.getUniqueName("model.glm",Deducer.guiEnv);
			}else
				modelName = prevModel.modelName;
		else{
			if(export.modelName.equals("<auto>") || export.modelName.equals(""))
				modelName = JGR.MAINRCONSOLE.getUniqueName("model.glm");
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
		cmd+=modelName+" <- glm(formula="+formula+",family="+this.family+",data="+dataName+
				(weights.getSize()==0 ? "" : ",weights="+weights.get(0))+
				((subset==null || subset.length()==0) ? "" : ",subset = "+subset)+
				",na.action=na.omit"+")";
		
		ArrayList tmp = new ArrayList();
		String[] out = new String[]{};	
		if(preview){
			Deducer.rniEval(dataName+"<-"+data);
			Deducer.rniEval(cmd);
			tmp.add("\n>"+cmd);
		}
			
		

		if(this.options.anova){
			String anovaCall = "Anova("+modelName+",type='"+options.type+"',test.statistic='"+options.test+"')";
			if(preview){
				out = Deducer.rniEval("capture.output("+anovaCall+")").asStringArray();
				tmp.add("\n>"+anovaCall+"\n");
				for(int i=0;i<out.length;i++)
					tmp.add(out[i]);
			}else{
				cmd+="\n"+anovaCall;
			}
		}
		if(this.options.summary){
			String summaryCall = "summary("+modelName+(options.paramCor ?",correlation=TRUE":"")+")";
			if(preview){
				out = Deducer.rniEval("capture.output("+summaryCall+")").asStringArray();
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
				out = Deducer.rniEval("capture.output("+vifCall+")").asStringArray();
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
				out = Deducer.rniEval("capture.output("+infCall+")").asStringArray();
				tmp.add("\n>"+infCall+"\n");
				for(int i=0;i<out.length;i++)
					tmp.add(out[i]);
			}else{
				cmd+="\n"+infCall;
			}
		}
		
		if(posthoc.posthoc.size()>0){
			String postCall = "";
			String cor = "univariate()";
			if(posthoc.correction!="No Correction"){
				cor = "adjusted(\""+posthoc.correction+"\")";
			}
			for(int i=0;i<posthoc.posthoc.getSize();i++){

				postCall = "summary(glht("+modelName+",linfct=mcp('"+posthoc.posthoc.get(i)+
							"'=\""+posthoc.type+"\")),test="+cor+")";
				if(preview){
					out = Deducer.rniEval("capture.output("+postCall+")").asStringArray();
					tmp.add("\n>"+postCall+"\n");
					for(int j=0;j<out.length;j++)
						tmp.add(out[j]);
				}else{
					cmd+="\n"+postCall;
				}
				if(posthoc.confInt){
					postCall = "confint(glht("+modelName+",linfct=mcp('"+posthoc.posthoc.get(i)+
					"'=\""+posthoc.type+"\")))";
					if(preview){
						out = Deducer.rniEval("capture.output("+postCall+")").asStringArray();
						tmp.add("\n>"+postCall+"\n");
						for(int j=0;j<out.length;j++)
							tmp.add(out[j]);
					}else{
						cmd+="\n"+postCall;
					}
				}
			}
		}
		
		if(effects.effects.size()>0){
			String[] t = new String[1];
			if(prevModel!=null){
				t=Deducer.rniEval("attr(terms("+prevModel.modelName+
									"),\"term.labels\")").asStringArray();
			}
			Vector ter = new Vector();
			for(int i=0;i<t.length;i++)
				ter.add(t[i]);
			Vector terms = new Vector();
			for(int i=0;i<effects.effects.size();i++){
				if(prevModel==null || ter.contains(effects.effects.get(i)))
					terms.add("\""+effects.effects.get(i)+"\"");
			}
			Vector effectCalls=new Vector();;
			for(int i=0;i<terms.size();i++){
				if(effects.confInt)
					effectCalls.add("summary(effect(term="+terms.get(i)+",mod="+modelName+"))");
				else
					effectCalls.add("\neffect(term="+terms.get(i)+",mod="+modelName+")");
			}
			if(preview){
				String effectCall;
				for(int i=0;i<effectCalls.size();i++){
					effectCall=(String)effectCalls.get(i);
					out = Deducer.rniEval("capture.output("+effectCall+")").asStringArray();
					tmp.add("\n>"+effectCall+"\n");
					for(int j=0;j<out.length;j++)
						tmp.add(out[j]);
				}
			}else{
				String effectCall;
				for(int i=0;i<effectCalls.size();i++){
					effectCall=(String)effectCalls.get(i);
					cmd+="\n"+effectCall;
				}
			}
		}
		
		if(plots.effects.size()>0){
			String[] t = new String[1];
			if(prevModel!=null){
				t=Deducer.rniEval("attr(terms("+prevModel.modelName+
									"),\"term.labels\")").asStringArray();
			}
			Vector ter = new Vector();
			for(int i=0;i<t.length;i++)
				ter.add(t[i]);
			Vector terms = new Vector();
			for(int i=0;i<plots.effects.size();i++){
				if(prevModel==null || ter.contains(plots.effects.get(i)))
					terms.add("\""+plots.effects.get(i)+"\"");
			}
			Vector plotCalls=new Vector();;
			for(int i=0;i<terms.size();i++){
				plotCalls.add("dev.new()");
				plotCalls.add("plot(effect(term="+terms.get(i)+",mod="+modelName+
										(",default.levels="+plots.defaultLevels)+")"+
								((plots.ylab!="" && !plots.ylab.equals("<auto>"))? ",ylab='"+
																		plots.ylab+"'" : "")+
								(plots.confInt?"":",confint=TRUE")+
								(plots.scaled ? "":",rescaled.axis=FALSE")+
								(plots.multi ? ",multiline=TRUE" : "")+
								(plots.rug ? "" : ",rug=FALSE")+
								")");
			}
			if(!preview){
				String plotCall;
				for(int i=0;i<plotCalls.size();i++){
					plotCall=(String)plotCalls.get(i);
					cmd+="\n"+plotCall;
				}
			}
		}
		
		if(!preview){
			String temp = JGR.MAINRCONSOLE.getUniqueName("tmp");
			boolean anyExport=false;
			if(export.cooks){
				anyExport=true;
				cmd+="\n"+temp+"<-cooks.distance("+modelName+")";
				cmd+="\n"+dataName+"[names("+temp+"),\""+Deducer.rniEval("rev(make.unique(c(names("+dataName+
						"),\"cooks\")))[1]").asString()+"\"]<-"+temp;
			}
			if(export.resid){
				anyExport=true;
				cmd+="\n"+temp+"<-residuals("+modelName+")";
				cmd+="\n"+dataName+"[names("+temp+"),\""+Deducer.rniEval("rev(make.unique(c(names("+dataName+
						"),\"Residuals\")))[1]").asString()+"\"]<-"+temp;
			}
			if(export.sdresid){
				anyExport=true;
				cmd+="\n"+temp+"<-rstandard("+modelName+")";
				cmd+="\n"+dataName+"[names("+temp+"),\""+Deducer.rniEval("rev(make.unique(c(names("+dataName+
						"),\"resid.standardized\")))[1]").asString()+"\"]<-"+temp;
			}
			if(export.stresid){
				anyExport=true;
				cmd+="\n"+temp+"<-rstudent("+modelName+")";
				cmd+="\n"+dataName+"[names("+temp+"),\""+Deducer.rniEval("rev(make.unique(c(names("+dataName+
						"),\"resid.studentized\")))[1]").asString()+"\"]<-"+temp;
			}
			if(export.pred){
				anyExport=true;
				cmd+="\n"+temp+"<-predict("+modelName+",type='response')";
				cmd+="\n"+dataName+"[names("+temp+"),\""+Deducer.rniEval("rev(make.unique(c(names("+dataName+
						"),\"predicted\")))[1]").asString()+"\"]<-"+temp;
			}
			if(export.linearPred){
				anyExport=true;
				cmd+="\n"+temp+"<-predict("+modelName+",type='link')";
				cmd+="\n"+dataName+"[names("+temp+"),\""+Deducer.rniEval("rev(make.unique(c(names("+dataName+
						"),\"linear.pred\")))[1]").asString()+"\"]<-"+temp;
			}
			if(export.dfbeta){
				anyExport=true;
				cmd+="\n"+JGR.MAINRCONSOLE.getUniqueName(modelName+".dfbeta")+"<-dfbeta("+modelName+")";
			}
			if(export.dffits){
				anyExport=true;
				cmd+="\n"+temp+"<-dffits("+modelName+")";
				cmd+="\n"+dataName+"[names("+temp+"),\""+Deducer.rniEval("rev(make.unique(c(names("+dataName+
						"),\"dffits\")))[1]").asString()+"\"]<-"+temp;
			}
			if(export.hats){
				anyExport=true;
				cmd+="\n"+temp+"<-hatvalues("+modelName+")";
				cmd+="\n"+dataName+"[names("+temp+"),\""+Deducer.rniEval("rev(make.unique(c(names("+dataName+
						"),\"hats\")))[1]").asString()+"\"]<-"+temp;
			}
			if(export.covratio){
				anyExport=true;
				cmd+="\n"+temp+"<-covratio("+modelName+")";
				cmd+="\n"+dataName+"[names("+temp+"),\""+Deducer.rniEval("rev(make.unique(c(names("+dataName+
						"),\"cov.ratio\")))[1]").asString()+"\"]<-"+temp;
			}
			if(anyExport)
				cmd+="\nrm('"+temp+"')";
			if(!export.keepModel)
				cmd+="\nrm('"+modelName+"')";
			JGR.MAINRCONSOLE.executeLater(cmd);
		}
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
	
	class GLMOptions{
		public boolean summary = true;
		public boolean paramCor = false;
		public boolean anova = true;
		public String type = "II";
		public String test = "Wald";
		public boolean vif = false;
		public boolean influence = false;
	}
	
	class PostHoc{
		public DefaultListModel posthoc = new DefaultListModel();
		public String type = "Tukey";
		public boolean confInt = false;
		public String correction = "No Correction";	
	}
	
	class Export{
		public boolean resid = false;
		public boolean sdresid = false;
		public boolean stresid = false;
		public boolean pred =false;
		public boolean linearPred = false;
		public boolean dfbeta = false;
		public boolean dffits=false;
		public boolean covratio=false;
		public boolean hats = false;
		public boolean cooks = false;
		public boolean keepModel = false;
		public String modelName = "<auto>";
	}
	
	class Effects{
		public DefaultListModel effects = new DefaultListModel();
		public boolean confInt = false;
	}
	
	class Plots{
		public DefaultListModel effects = new DefaultListModel();
		public boolean confInt = true;		
		public boolean scaled = false;
		public boolean multi = false;
		public boolean rug = true;
		public String ylab = "<auto>";
		public int defaultLevels = 20;
	}
}
