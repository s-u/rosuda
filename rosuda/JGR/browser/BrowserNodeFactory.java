package org.rosuda.JGR.browser;

public interface BrowserNodeFactory {
	
	public BrowserNode generate(BrowserNode parent, String rName, String rClass);
	
}
