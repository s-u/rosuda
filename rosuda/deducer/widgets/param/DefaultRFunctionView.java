package org.rosuda.deducer.widgets.param;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.border.Border;

import org.rosuda.JGR.layout.AnchorConstraint;
import org.rosuda.JGR.layout.AnchorLayout;
import org.rosuda.deducer.widgets.VariableSelectorWidget;
import org.rosuda.deducer.widgets.param.Param;
import org.rosuda.deducer.widgets.param.ParamWidget;

public class DefaultRFunctionView extends RFunctionView{
	protected JScrollPane scroller;
	protected JPanel paramPanel;
	protected RFunction model;
	protected VariableSelectorWidget selector;
	
	protected Vector widgets = new Vector();
	
	public DefaultRFunctionView(){
		initGui();
	}
	
	public DefaultRFunctionView(RFunction el){
		initGui();
		setModel(el);
	}
	
	
	private void initGui(){
		this.setLayout(new BorderLayout());
		scroller = new JScrollPane();
		scroller.setHorizontalScrollBarPolicy(
				ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		Border border = BorderFactory.createEmptyBorder(0, 0, 0, 0);
		scroller.setBorder(border);
		this.add(scroller);
		{
			paramPanel = new JPanel();
			scroller.setViewportView(paramPanel);
		}
	}
	
	public void updateGui(){
		boolean showSelector = false;
		for(int i=0;i<model.getParams().size();i++){
			Param p = (Param) model.getParams().get(i);
			if(p.requiresVariableSelector()){
				showSelector = true;
				break;
			}
		}
		
		if(showSelector){
			if(selector == null){
				selector = new VariableSelectorWidget();
				selector.setPreferredSize(new Dimension(150,300));
			}
			this.removeAll();
			AnchorLayout thisLayout = new AnchorLayout();
			this.setLayout(thisLayout);
			this.add(scroller, new AnchorConstraint(60, 1000, 1000, 160, 
					AnchorConstraint.ANCHOR_ABS, AnchorConstraint.ANCHOR_REL,
					AnchorConstraint.ANCHOR_REL, AnchorConstraint.ANCHOR_ABS));
			this.add(selector, new AnchorConstraint(20, 90, 1000, 10, 
					AnchorConstraint.ANCHOR_ABS, AnchorConstraint.ANCHOR_NONE,
					AnchorConstraint.ANCHOR_REL, AnchorConstraint.ANCHOR_ABS));		
		}else{
			this.removeAll();
			this.setLayout(new BorderLayout());
			this.add(scroller);
		}
		
		paramPanel.removeAll();
		BoxLayout thisLayout = new BoxLayout(paramPanel, javax.swing.BoxLayout.Y_AXIS);
		paramPanel.setLayout(thisLayout);	
		for(int i=0;i<model.getParams().size();i++){
			Param p = (Param) model.getParams().get(i);
			ParamWidget a ;
			if(!p.requiresVariableSelector())
				a = p.getView();
			else
				a = p.getView(selector);
			a.setAlignmentX(CENTER_ALIGNMENT);
			a.setMaximumSize(new Dimension(365,a.getMaximumSize().height));	
			widgets.add(a);
			paramPanel.add(a);
			paramPanel.add(Box.createRigidArea(new Dimension(0,10)));
		}
		paramPanel.validate();
		paramPanel.repaint();
	}
	
	public Param getModel() {
		updateModel();
		return model;
	}

	public void setModel(RFunction el) {
		model = el;
		updateGui();
	}

	public void updateModel() {
		for(int i=0;i<widgets.size();i++){
			Object o = widgets.get(i);
			((ParamWidget)o).updateModel();
		}
	}

	public void setModel(Param p) {
		setModel((RFunction) p);
	}

}