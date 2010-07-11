package org.rosuda.deducer.plots;

import java.util.Vector;


public interface ElementModel {

	public abstract String getCall();
	public abstract String checkValid();
	public abstract String getType();
	public abstract ElementView getView();
	public abstract Vector getParams();
	public abstract Object clone();
}
