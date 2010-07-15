package org.rosuda.deducer.widgets.param;

import java.util.Vector;

import org.rosuda.deducer.Deducer;

public class RFunction {

	private String name;
	private Vector params = new Vector();
	
	public Vector getParams(){return params;}
	public void setParams(Vector p){params = p;}
	public String getName(){return name;}
	public void setName(String n){name = n;}
	
	
	public Object clone(){
		RFunction s = new RFunction();
		for(int i=0;i<params.size();i++)
			s.params.add(((Param)params.get(i)).clone());
		s.name = name;
		return s;
	}

	
	public String checkValid() {
		return null;
	}

	public String getCall() {
		Vector paramCalls = new Vector();
		for(int i=0;i<params.size();i++){
			Param prm = (Param) params.get(i);

			String[] p = prm.getParamCalls();
			for(int j=0;j<p.length;j++)
				paramCalls.add(p[j]);				
		}
		String call = Deducer.makeRCollection(paramCalls, name, false);
		return call;
	}


	public RFunctionView getView() {
		return new DefaultRFunctionView(this);
	}
	
	public void add(Param p){
		params.add(p);
	}
	public Param get(int i){
		return (Param) params.get(i);
	}
	public void remove(Param p){
		params.remove(p);
	}
	
}
