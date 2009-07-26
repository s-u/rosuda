package org.rosuda.deducer.models;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

import org.rosuda.JGR.util.ErrorMsg;
import org.rosuda.deducer.Deducer;

public class LinearDialog extends GLMDialog {
	public static LinearModel lastLinearModel;
	
	public LinearDialog(JDialog d,GLMModel mod) {
		super(d,mod);
	}
	public LinearDialog(JFrame frame,GLMModel mod) {
		super(frame,mod);
	}
	public LinearDialog(GLMModel mod) {
		super(mod);
	}
	public LinearDialog(JFrame frame) {
		this(frame,lastLinearModel==null ? new LinearModel() : lastLinearModel);
	}
	protected void initGUI(){
		super.initGUI();
		type.setSelectedItem("gaussian()");
		type.setVisible(false);
		typeLabel.setVisible(false);
		this.setTitle("Linear Regression Model");
	}
	
	public void continueClicked(){
		if(!valid())
			return;
		updateModel();
		LinearBuilder builder = new LinearBuilder(model);
		builder.setLocationRelativeTo(this);
		builder.setVisible(true);
		this.dispose();
	}
	
	
	public static void setLastModel(GLMModel mod){
		if((mod instanceof LinearModel))
				lastLinearModel = (LinearModel) mod;
		else
			new ErrorMsg("Invalid cast in LogisticDialog.setLastModel");
	}
	
	public boolean valid(){
		boolean result = super.valid();
		if(!result)
			return false;
		String out = (String) outcome.getModel().getElementAt(0);
		String dat = variableSelector.getSelectedData();
		boolean isNumeric = Deducer.rniEval("is.numeric("+dat+"$"+out+")").asBool().isTRUE();
		if(!isNumeric){
			JOptionPane.showMessageDialog(this, "Outcome variable is not numeric. Please select a numeric variable");
		}
		return isNumeric;
	}
	
	
}
