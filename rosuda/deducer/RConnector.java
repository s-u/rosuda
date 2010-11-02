package org.rosuda.deducer;

import org.rosuda.REngine.REXP;
import org.rosuda.REngine.REngine;

public interface RConnector {

	public void execute(String cmd);
	public void execute(String cmd,boolean addToHist);
	public REXP eval(String cmd);
	public REXP idleEval(String cmd);
	public REngine getREngine();
}
