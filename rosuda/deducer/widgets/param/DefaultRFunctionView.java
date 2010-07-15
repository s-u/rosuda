package org.rosuda.deducer.widgets.param;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.util.Vector;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;

import org.rosuda.deducer.widgets.param.Param;
import org.rosuda.deducer.widgets.param.ParamWidget;

public class DefaultRFunctionView extends RFunctionView{
	protected JScrollPane scroller;
	protected JPanel paramPanel;
	protected RFunction model;
	
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
		this.add(scroller);
		{
			paramPanel = new JPanel();
			scroller.setViewportView(paramPanel);
		}
	}
	
	public void updatePanel(){
		paramPanel.removeAll();
		BoxLayout thisLayout = new BoxLayout(paramPanel, javax.swing.BoxLayout.Y_AXIS);
		paramPanel.setLayout(thisLayout);	
		for(int i=0;i<model.getParams().size();i++){
			Param p = (Param) model.getParams().get(i);
			ParamWidget a = p.getView();
			a.setAlignmentX(CENTER_ALIGNMENT);
			a.setMaximumSize(new Dimension(365,a.getMaximumSize().height));	
			widgets.add(a);
			paramPanel.add(a);
			paramPanel.add(Box.createRigidArea(new Dimension(0,10)));
		}
		paramPanel.validate();
		paramPanel.repaint();
	}
	
	public RFunction getModel() {
		updateModel();
		return model;
	}

	public void setModel(RFunction el) {
		model = el;
		updatePanel();
	}

	public void updateModel() {
		for(int i=0;i<widgets.size();i++){
			Object o = widgets.get(i);
			((ParamWidget)o).updateModel();
		}
	}

}