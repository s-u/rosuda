package org.rosuda.deducer.widgets.param;

import java.util.HashMap;

import org.rosuda.deducer.plots.ParamFacet;
import org.rosuda.deducer.plots.ParamScaleLegend;
import org.rosuda.deducer.plots.ParamStatSummary;

public class ParamFactory {

	private static HashMap params;
	private static boolean initialized = false;
	
	public static void init(){
		if(!initialized){
			params = new HashMap();
			params.put("org.rosuda.deducer.widgets.param.ParamAny", new ParamAny());
			params.put("org.rosuda.deducer.widgets.param.ParamCharacter", new ParamCharacter());
			params.put("org.rosuda.deducer.widgets.param.ParamColor", new ParamColor());
			params.put("org.rosuda.deducer.widgets.param.ParamLogical", new ParamLogical());
			params.put("org.rosuda.deducer.widgets.param.ParamNumeric", new ParamNumeric());
			params.put("org.rosuda.deducer.widgets.param.ParamRFunction", new ParamRFunction());
			params.put("org.rosuda.deducer.widgets.param.ParamVector", new ParamVector());
			
			params.put("org.rosuda.deducer.plots.ParamFacet", new ParamFacet());
			params.put("org.rosuda.deducer.plots.ParamScaleLegend", new ParamScaleLegend());
			params.put("org.rosuda.deducer.plots.ParamSummary", new ParamStatSummary());
		}
	}
	
	public static Param getParam(String paramName){
		init();
		Object o = params.get(paramName);
		if(o==null){
			System.out.println("Error in ParamFactory: could not find Param:" + paramName);
			(new Exception()).printStackTrace();
			return null;
		}
		return (Param) ((Param)o).clone();
	}
	
	public static void setParam(String paramName,Param p){
		init();
		params.put(paramName, p.clone());
	}
	
}
