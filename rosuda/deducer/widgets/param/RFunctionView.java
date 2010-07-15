package org.rosuda.deducer.widgets.param;

import javax.swing.JPanel;


public abstract class RFunctionView extends JPanel{
	
	public abstract void setModel(RFunction el);
	public abstract RFunction getModel();
	public abstract void updateModel();

}
