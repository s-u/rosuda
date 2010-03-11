package org.rosuda.deducer.widgets;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JOptionPane;

import org.rosuda.JGR.layout.AnchorConstraint;
import org.rosuda.deducer.Deducer;


public class PlotRDialog extends RDialog implements ActionListener{

	private VariableSelectorWidget variableSelector;
	private SingleVariableWidget yaxis;
	private SingleVariableWidget xaxis;
	private SliderWidget slider;


	
	public void initGUI(){
		super.initGUI();
		

		variableSelector = new VariableSelectorWidget();
		this.add(variableSelector, new AnchorConstraint(12, 428, 900, 12, 
				AnchorConstraint.ANCHOR_ABS, AnchorConstraint.ANCHOR_REL, 
				AnchorConstraint.ANCHOR_REL, AnchorConstraint.ANCHOR_ABS));
		variableSelector.setPreferredSize(new java.awt.Dimension(216, 379));
		variableSelector.setTitle("Data");
		
		yaxis = new SingleVariableWidget("y axis",variableSelector);
		this.add(yaxis, new AnchorConstraint(121, 978, 327, 460, 
				AnchorConstraint.ANCHOR_REL, AnchorConstraint.ANCHOR_REL, 
				AnchorConstraint.ANCHOR_REL, AnchorConstraint.ANCHOR_REL));
		yaxis.setPreferredSize(new java.awt.Dimension(276, 63));
		
		xaxis = new SingleVariableWidget("x axis",variableSelector);
		this.add(xaxis, new AnchorConstraint(337, 978, 540, 460, 
				AnchorConstraint.ANCHOR_REL, AnchorConstraint.ANCHOR_REL, 
				AnchorConstraint.ANCHOR_REL, AnchorConstraint.ANCHOR_REL));
		xaxis.setPreferredSize(new java.awt.Dimension(276, 63));
		
		slider = new SliderWidget("Alpha level",new String[]{"Transparent","Opaque"});
		this.add(slider, new AnchorConstraint(610, 978, 840, 460, 
				AnchorConstraint.ANCHOR_REL, AnchorConstraint.ANCHOR_REL, 
				AnchorConstraint.ANCHOR_REL, AnchorConstraint.ANCHOR_REL));
		slider.setPreferredSize(new java.awt.Dimension(187, 44));
		slider.setDefaultModel(new Integer(100));
		
		this.setTitle("Scatter Plot");
		
		setOkayCancel(true,true,this);
		addHelpButton("pmwiki.php");
		this.setSize(555, 445);
	}

	public void actionPerformed(ActionEvent e) {
		String cmd = e.getActionCommand();
		
		if(cmd=="Run"){
			String xvar = xaxis.getSelectedVariable();
			String yvar = yaxis.getSelectedVariable();
			String data = variableSelector.getSelectedData();
			String alpha = new Double(((double) slider.getValue())/100.0).toString();
			if(yvar==null || xvar==null || data==null){
				JOptionPane.showMessageDialog(this, "You must specify both an x and y variable");
				return;
			}
			
			String command = "qplot("+xvar+", "+yvar+", data="+data+",alpha=I("+alpha+"))";
			
			Deducer.execute(command);		//execute command as if it had been entered into the console
			
			completed();	//dialog completed
			this.setVisible(false);
		}else if(cmd=="Cancel")
			this.setVisible(false);
		else if(cmd=="Reset")
			reset();
	}
	
}
