package org.rosuda.deducer.plots;

import java.util.Vector;


public class Facet implements ElementModel{

	public String name;
	
	public ParamFacet param;
	
	public String data;

	public String facetType;
	
	
	public static Facet makeFacet(String n){
		Facet f = new Facet();
		f.name = "facet_" + n;
		f.facetType = n;
		
		ParamFacet p = new ParamFacet();
		p.facetType = n;
		f.param = p;
		return f;
	}
	
	
	public Object clone(){
		Facet f = new Facet();
		f.name = name;
		f.data = data;
		f.param = (ParamFacet) param.clone();
		f.facetType = facetType;
		return f;
	}

	
	public String checkValid() {
		return null;
	}

	public String getCall() {
		String[] p = param.getParamCalls();
		String call = "facet_" + facetType + "(";
		for(int i =0;i<p.length;i++)
			call+=p[i];
		call+=")";
		return call;
	}

	public String getType() {
		return "facet";
	}

	public ElementView getView() {
		FacetPanel fp = new FacetPanel();
		fp.setModel(this);
		return fp;
	}


	public Vector getParams() {
		Vector v = new Vector();
		v.add(param);
		return v;
	}
}
