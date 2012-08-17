package org.rosuda.JGR.browser;

import java.util.HashMap;

public class BrowserController {
	
	public static int MAX_CHILDREN = 10000;
	
	protected static boolean initialized = false;
	
	protected static HashMap factories = new HashMap();
	
	protected static BrowserNodeFactory defaultFactory = new DefaultBrowserNode();
	
	public static void initialize(){
		if(!initialized){
			//System.out.println("initializing");
			initialized=true;
			setFactory("numeric",new NumericNode());
			setFactory("integer",new NumericNode());
			setFactory("factor",new FactorNode());
			setFactory("character",new FactorNode());
			setFactory("logical",new FactorNode());
			setFactory("data.frame",new DataFrameNode());
			setFactory("matrix",new MatrixNode());
			setFactory("environment",new EnvironmentNode());
			setFactory("function",new FunctionNode());
			setFactory("lm",new LmNode());
			setFactory("glm",new LmNode());
		}
	}

	public static BrowserNode createNode(BrowserNode parent, String rName, String rClass){
		initialize();
		BrowserNodeFactory fact = (BrowserNodeFactory) factories.get(rClass);
		if(fact==null)
			return defaultFactory.generate(parent, rName, rClass);
		
		return fact.generate(parent, rName, rClass);
	}
	
	public static void setFactory(String className,BrowserNodeFactory factory){
		initialize();
		factories.put(className, factory);
	}
	
	public void setDefaultFactory(BrowserNodeFactory factory){
		initialize();
		defaultFactory = factory;
	}
	
}
