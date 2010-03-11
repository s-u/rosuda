package org.rosuda.deducer.widgets;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JOptionPane;

import org.rosuda.JGR.layout.AnchorConstraint;
import org.rosuda.REngine.REXP;
import org.rosuda.REngine.REXPMismatchException;
import org.rosuda.REngine.REngineException;
import org.rosuda.deducer.Deducer;

public class SimpleRDialog extends RDialog implements ActionListener{
	
	private String rCheckFunc;
	private String rRunFunc;
	private REXP env;

	public SimpleRDialog(){
		super();
		setOkayCancel(true,true,this);
	}
	
	public SimpleRDialog(String rCheckFunction, String rRunFunction, REXP environment){
		super();
		rCheckFunc = rCheckFunction;
		rRunFunc = rRunFunction;
		env = environment;
		setOkayCancel(true,true,this);
		this.setLocationRelativeTo(null);
	}

	public void setCheckFunction(String func){
		rCheckFunc = func;
	}
	
	public String getCheckFunction(){
		return rCheckFunc;
	}
	
	public void setRunFunction(String func){
		rRunFunc = func;
	}
	
	public REXP getEnv(){
		return env;
	}
	
	public void setEnv(REXP envir){
		env = envir;
	}

	public void actionPerformed(ActionEvent e) {
		String cmd = e.getActionCommand();
		if(cmd=="Run"){
			String state = getWidgetStatesAsString();
			REXP rCheck = null;
			rCheck = Deducer.eval(rCheckFunc + "(" + state + ")");

			
			String check = "";
			try {
				if(rCheck!=null)
					check = rCheck.asString();
			} catch (REXPMismatchException e1) {
				JOptionPane.showMessageDialog(this, "Dialog error. Check function must return a string. Return" +
													"'' if there the check passes.");
			}
			if(check.length()<1){
				this.setVisible(false);
				try {
					Deducer.engine.parseAndEval(rRunFunc + "(" + state + ")", env, false);
				} catch (REngineException e1) {
					e1.printStackTrace();
				} catch (REXPMismatchException e1) {
					e1.printStackTrace();
				}
				completed();
			}else{
				JOptionPane.showMessageDialog(this, check);
				return;
			}
		}else if(cmd=="Cancel")
			this.setVisible(false);
		else if(cmd=="Reset")
			reset();
	}
	
	
	public void add(Component comp,int top,int right, int bottom, int left, String topType,
					String rightType, String bottomType, String leftType){
		int topTyp = topType.equals("REL") ? AnchorConstraint.ANCHOR_REL : (topType.equals("ABS") ? AnchorConstraint.ANCHOR_ABS : AnchorConstraint.ANCHOR_NONE );
		int rightTyp = rightType.equals("REL") ? AnchorConstraint.ANCHOR_REL : (rightType.equals("ABS") ? AnchorConstraint.ANCHOR_ABS : AnchorConstraint.ANCHOR_NONE );
		int bottomTyp = bottomType.equals("REL") ? AnchorConstraint.ANCHOR_REL : (bottomType.equals("ABS") ? AnchorConstraint.ANCHOR_ABS : AnchorConstraint.ANCHOR_NONE );
		int leftTyp = leftType.equals("REL") ? AnchorConstraint.ANCHOR_REL : (leftType.equals("ABS") ? AnchorConstraint.ANCHOR_ABS : AnchorConstraint.ANCHOR_NONE );
		
		AnchorConstraint constr = new AnchorConstraint(top, right, bottom, left, 
				topTyp, rightTyp, bottomTyp, leftTyp);
		this.add(comp,constr);
	}
	
	
}
