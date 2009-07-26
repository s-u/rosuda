package org.rosuda.deducer.models;

import org.rosuda.deducer.Deducer;

public class LinearExplorer extends GLMExplorer{

	LinearExplorer(GLMModel mod) {
		super(mod);
		this.setTitle("Linear Regression Model Explorer");
	}

	public void run(){
		if(((LinearModel)model).hccm){
			model.plots.confInt=false;
			model.effects.confInt=false;
		}
		model.run(false,pre);
		this.dispose();
		LinearDialog.setLastModel(model);
		Deducer.rniEval("rm('"+pre.data.split("$")[1]+"','"+pre.modelName.split("$")[1]+"',envir="+Deducer.guiEnv+")");
	}
	
	public void optionsClicked(){
		LinearExplorerOptions opt = new LinearExplorerOptions(this,(LinearModel)model);
		opt.setLocationRelativeTo(this);
		opt.setVisible(true);
		setModel(model);
	}
	
	public void plotsClicked(){
		LinearExplorerPlots p = new LinearExplorerPlots(this,model,pre,((LinearModel)model).hccm);
		p.setLocationRelativeTo(this);
		p.setVisible(true);
		setModel(model);	
	}
	
	public void meansClicked(){
		GLMExplorerMeans m = new GLMExplorerMeans(this,model,pre);
		m.setLocationRelativeTo(this);
		if(((LinearModel)model).hccm){
			m.disableConfInt();
		}
		m.setVisible(true);
		setModel(model);		
	}
	
	public void exportClicked(){
		GLMExplorerExport exp = new GLMExplorerExport(this,model);
		exp.setLocationRelativeTo(this);
		exp.setSinglePredicted();
		exp.setVisible(true);
	}
	
}
