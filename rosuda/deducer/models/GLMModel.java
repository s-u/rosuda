package org.rosuda.deducer.models;

import java.util.ArrayList;
import java.util.Vector;

import javax.swing.DefaultListModel;
import javax.swing.table.DefaultTableModel;

import org.rosuda.JGR.JGR;
import org.rosuda.JGR.RController;
import org.rosuda.JRI.REXP;
import org.rosuda.deducer.Deducer;
import org.rosuda.deducer.data.ExDefaultTableModel;

public class GLMModel extends ModelModel {
	public DefaultListModel weights = new DefaultListModel();
	public String family = "gaussian()";
	public GLMOptions options = new GLMOptions();
	public PostHoc posthoc = new PostHoc();
	public Export export = new Export();
	public Effects effects = new Effects();
	public Plots plots = new Plots();
	public Tests tests = new Tests();
	
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
		
		cmd=runOptions(cmd,modelName,preview,tmp);
		cmd=runPostHoc(cmd,modelName,preview,tmp);
		cmd=runEffects(cmd,modelName,preview,tmp,prevModel);
		cmd=runPlots(cmd,modelName,preview,tmp,prevModel);
		cmd=runTests(cmd,modelName,preview,tmp,prevModel);
		cmd=runExport(cmd,modelName,preview,tmp,dataName,false);
		
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
		String[] out = new String[]{};
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
		return cmd;
	}
	
	protected String runEffects(String cmd,String modelName,boolean preview,ArrayList tmp,
								RModel prevModel){
		String[] out = new String[]{};
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
			Vector effectCalls=new Vector();
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
		return cmd;
	}
	
	protected String runPlots(String cmd,String modelName,boolean preview,ArrayList tmp,
								RModel prevModel){
		String[] out = new String[]{};
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
			Vector plotCalls=new Vector();
			for(int i=0;i<terms.size();i++){
				plotCalls.add("dev.new()");
				plotCalls.add("plot(effect(term="+terms.get(i)+",mod="+modelName+
										(",default.levels="+plots.defaultLevels)+
										(plots.confInt?"":",se=FALSE")+
										")"+
								((plots.ylab!="" && !plots.ylab.equals("<auto>"))? ",ylab='"+
																		plots.ylab+"'" : "")+
								(plots.scaled ? "":",rescale.axis=FALSE")+
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
		
		return cmd;
	}
	
	protected String runTests(String cmd,String modelName,boolean preview,ArrayList tmp, 
								RModel prevModel){
		String[] out = new String[]{};
		if(tests.size()>0){
			String[] t = new String[1];
			if(prevModel!=null){
				t=Deducer.rniEval("names(coef("+prevModel.modelName+
									"))").asStringArray();
			}else if(preview){
				t=Deducer.rniEval("names(coef("+modelName+
									"))").asStringArray();
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
				call = "lht("+modelName +","+matrixName+","+RController.makeRVector(rhs)+")";
				testCalls.add(call);
			}
			if(testCalls.size()>0)
				testCalls.add("rm('"+matrixName+"')");
			if(preview){
				String testCall;
				for(int i=0;i<testCalls.size();i++){
					testCall=(String)testCalls.get(i);
					REXP r =Deducer.rniEval("capture.output("+testCall.replace("\n", "").replace("\t", "")+")");
					if(r!=null)
						out = r.asStringArray();
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
	
	protected String runExport(String cmd,String modelName,boolean preview,ArrayList tmp,
			String dataName,boolean isLm){
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
			if(export.pred && !isLm){
				anyExport=true;
				cmd+="\n"+temp+"<-predict("+modelName+",type='response')";
				cmd+="\n"+dataName+"[names("+temp+"),\""+Deducer.rniEval("rev(make.unique(c(names("+dataName+
						"),\"predicted\")))[1]").asString()+"\"]<-"+temp;
			}else if(export.pred){
				anyExport=true;
				cmd+="\n"+temp+"<-predict("+modelName+")";
				cmd+="\n"+dataName+"[names("+temp+"),\""+Deducer.rniEval("rev(make.unique(c(names("+dataName+
						"),\"predicted\")))[1]").asString()+"\"]<-"+temp;				
			}
			if(export.linearPred && !isLm){
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
		}
		return cmd;
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
	
	class Tests{
		public String direction = "two.sided";
		private ArrayList tableModelList = new ArrayList();
		private ArrayList testNames = new ArrayList();
		
		public void reset(){
			direction = "two.sided";
			tableModelList = new ArrayList();
			testNames = new ArrayList();
		}
		public int size(){
			return tableModelList.size();
		}
		public ExDefaultTableModel getModel(int i){
			return (ExDefaultTableModel) tableModelList.get(i);
		}
		
		public String getName(int i){
			return (String) testNames.get(i);
		}
		
		public ExDefaultTableModel getDuplicateTableModel(int i){
			ExDefaultTableModel cur = (ExDefaultTableModel) tableModelList.get(i);
			ExDefaultTableModel newModel = new ExDefaultTableModel();
			newModel.setColumnCount(cur.getColumnCount());
			newModel.setRowCount(cur.getRowCount());
			for(int j=0;j<cur.getRowCount();j++)
				for(int k=0;k<cur.getColumnCount();k++)
					newModel.setValueAt(cur.getValueAt(j, k), j, k);
			return (ExDefaultTableModel) newModel;
		}
		
		public void addTest(String name,ExDefaultTableModel mod){
			tableModelList.add(mod);
			testNames.add(name);
		}
		
	}
}
