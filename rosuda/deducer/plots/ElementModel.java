package org.rosuda.deducer.plots;

import javax.swing.JPanel;

public interface ElementModel {

	public abstract String getCall();
	public abstract String checkValid();
	public abstract String getType();
	public abstract ElementView getView();
	public abstract Object clone();
}
