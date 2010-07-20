package org.rosuda.deducer.models;

import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.Vector;

import javax.swing.JButton;

import org.rosuda.JGR.util.ErrorMsg;
import org.rosuda.REngine.REXPLogical;
import org.rosuda.deducer.Deducer;
import org.rosuda.deducer.WindowTracker;
import org.rosuda.deducer.toolkit.HelpButton;
import org.rosuda.deducer.toolkit.IconButton;

public class GLMExplorer extends ModelExplorer implements WindowListener{
	
	protected GLMModel model = new GLMModel();
	protected RModel pre;
	protected ModelPlotPanel diagnosticTab;
	protected ModelPlotPanel termTab;
	protected ModelPlotPanel addedTab;
	protected IconButton assumpHomo;
	protected IconButton assumpFunc;
	protected IconButton assumpN;
	
	GLMExplorer(GLMModel mod){
		super();
		this.setTitle("Generalized Linear Model Explorer");
		help.setUrl(HelpButton.baseUrl + "pmwiki.php?n=Main.GeneralizedLinearModel");		
		setModel(mod);
		initTabs();
		initAssumptions();
		this.addWindowListener(this);
	}
	
	public void initTabs(){
		try{
			String call="par(mfrow = c(2, 3),mar=c(5,4,2,2))\n"+
				"hist(resid("+pre.modelName+"),main=\"Residual\",xlab=\"Residuals\")\n"+
				"plot("+pre.modelName+",2,sub.caption=\"\")\n"+
				"plot("+pre.modelName+", c(1,4,3,5),sub.caption=\"\")";
			diagnosticTab = new ModelPlotPanel(call);
			tabs.addTab("Diagnostics", diagnosticTab);
			
			call="par(mar=c(5,4,2,2))\n"+
				"try(cr.plots("+pre.modelName+",one.page=T,ask=F,identify.points=F,col=1),silent=TRUE)";
			termTab = new ModelPlotPanel(call);
			if(((REXPLogical)Deducer.eval("length(grep(\":\",c(attr(terms("+pre.modelName+"),\"term.labels\"))))==0")).isTRUE()[0])
				tabs.addTab("Terms", termTab);
			
			call="par(mar=c(5,4,2,2))\n"+
			"try(av.plots("+pre.modelName+",one.page=T,ask=F,identify.points=F,col=1),silent=TRUE)";
			addedTab = new ModelPlotPanel(call);
			tabs.addTab("Added Variable", addedTab);
		}catch(Exception e){
			new ErrorMsg(e);
		}
	}
	
	protected void initAssumptions(){
		{
			assumpN =  new IconButton("/icons/N_assump.png","Large Sample",null,"Large Sample");
			topPanel.add(assumpN);
			assumpN.setBounds(12, 8, 27, 27);
		}
		{
			assumpFunc = new IconButton("/icons/func_assump.png","Correct Functional Form",
					null,"Correct Functional Form");
			topPanel.add(assumpFunc);
			assumpFunc.setBounds(44, 8, 27, 27);
		}
		{
			assumpHomo = new IconButton("/icons/outlier_assump.png","No Outliers",null,"No Outliers");
			topPanel.add(assumpHomo);
			assumpHomo.setBounds(76, 8, 27, 27);
		}
	}
	
	public void setModel(GLMModel mod){
		model = mod;
		pre =model.run(true,pre);
		modelFormula.setText(pre.formula);
		preview.setText(pre.preview);
		preview.setCaretPosition(0);
	}
	
	
	public void run(){
		model.run(false,pre);
		this.dispose();
		GLMDialog.setLastModel(model);
		Deducer.eval("rm('"+pre.data.split("\\$")[1]+"','"+pre.modelName.split("\\$")[1]+"',envir="+Deducer.guiEnv+")");
	}
	
	public void updateClicked(){
		GLMBuilder bld = new GLMBuilder(model);
		bld.setLocationRelativeTo(this);
		bld.setVisible(true);
		WindowTracker.addWindow(bld);
		this.dispose();

	}
	
	public void optionsClicked(){
		GLMExplorerOptions opt = new GLMExplorerOptions(this,model);
		opt.setLocationRelativeTo(this);
		opt.setVisible(true);
		setModel(model);
	}
	
	public void postHocClicked(){
		GLMExplorerPostHoc post = new GLMExplorerPostHoc(this,model,pre);
		post.setLocationRelativeTo(this);
		post.setVisible(true);
		setModel(model);
	}
	public void exportClicked(){
		GLMExplorerExport exp = new GLMExplorerExport(this,model);
		exp.setLocationRelativeTo(this);
		exp.setVisible(true);
	}
	
	public void meansClicked(){
		GLMExplorerMeans m = new GLMExplorerMeans(this,model,pre);
		m.setLocationRelativeTo(this);
		m.setVisible(true);
		setModel(model);		
	}
	
	public void plotsClicked(){
		GLMExplorerPlots p = new GLMExplorerPlots(this,model,pre);
		p.setLocationRelativeTo(this);
		p.setVisible(true);
		setModel(model);	
	}
	
	public void testsClicked(){
		
		String[] s = new String[]{};
		try{
			s =Deducer.eval("names(coef("+pre.modelName+
					"))").asStrings();
		}catch(Exception e){
			e.printStackTrace();
			new ErrorMsg(e);
		}
		Vector trms = new Vector();
		for(int i=0;i<s.length;i++)
			trms.add(s[i]);
		
		GLMExplorerTests p = new GLMExplorerTests(this,trms,model);
		p.setLocationRelativeTo(this);
		p.setVisible(true);
		setModel(model);			
	}

	public void windowActivated(WindowEvent arg0) {}

	public void windowClosed(WindowEvent arg0) {
		if(diagnosticTab!=null)
			diagnosticTab.executeDevOff();
		if(termTab!=null)
			termTab.executeDevOff();
		if(addedTab!=null)
			addedTab.executeDevOff();
	}

	public void windowClosing(WindowEvent arg0) {}

	public void windowDeactivated(WindowEvent arg0) {}

	public void windowDeiconified(WindowEvent arg0) {}

	public void windowIconified(WindowEvent arg0) {}

	public void windowOpened(WindowEvent arg0) {}
}
