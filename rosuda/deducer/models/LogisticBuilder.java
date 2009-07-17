package org.rosuda.deducer.models;

import javax.swing.DefaultListModel;
import javax.swing.JOptionPane;

public class LogisticBuilder extends GLMBuilder {

	public LogisticBuilder(ModelModel mod) {
		super(mod);
		if(! (mod instanceof LogisticModel)){
			JOptionPane.showMessageDialog(this, "Internal Error: Invalid ModelModel");
			mod=new LogisticModel();
		}
		setModel(mod);
		this.setTitle("Logistic Regression Model Builder");
	}
	
	public void specify() {
		if(! (model instanceof LogisticModel)){
			JOptionPane.showMessageDialog(this, "Internal Error: Invalid ModelModel");
			setModel(new LogisticModel());
		}
		LogisticDialog dia = new LogisticDialog((LogisticModel)model);
		dia.setLocationRelativeTo(this);
		dia.setVisible(true);
		this.dispose();
	}
	
	public void editSelectedOutcome(){
		super.editSelectedOutcome();
		LogisticDialogSplitModel m = ((LogisticModel)model).split;
		m.which=3;
		m.expr=(String)((DefaultListModel)outcomes.getModel()).get(outcomes.getSelectedIndex());
	}
	
	public void done(){
		if(! (model instanceof LogisticModel)){
			JOptionPane.showMessageDialog(this, "Internal Error: Invalid ModelModel");
			setModel(new LogisticModel());
			return;
		}
		updateModel();
		LogisticExplorer exp = new LogisticExplorer((LogisticModel)model);
		exp.setLocationRelativeTo(this);
		exp.setVisible(true);
		this.dispose();
	}

}
