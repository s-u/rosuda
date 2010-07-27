package org.rosuda.deducer.widgets.param;

import java.util.HashMap;
import java.util.Vector;

public class ParamRFunction extends Param{
	
	protected Vector value;
	protected Vector defaultValue;			//default	
	
	
	public ParamRFunction(){
		name = "";
		title = "";
		value = null;
		defaultValue = null;
		view = VIEW_RFUNCTION;
	}
	
	public ParamRFunction(String nm){
		name = nm;
		title = nm;
		value = null;
		defaultValue = null;
		view = VIEW_RFUNCTION;
	}
	
	public ParamWidget getView(){
		if(getViewType() == Param.VIEW_RFUNCTION)
			return new ParamRFunctionWidget(this);
		return null;
	}
	
	public Object clone(){
		ParamRFunction p = new ParamRFunction();
		p.setName(this.getName());
		p.setTitle(this.getTitle());
		p.setViewType(this.getViewType());
		if(this.getOptions()!=null){
			String[] v = new String[this.getOptions().length];
			for(int i=0;i<this.getOptions().length;i++)
				v[i] = this.getOptions()[i];
			p.setOptions(v);
		}
		if(getValue()!=null){
			Vector v = (Vector) getValue();
			Vector vNew = new Vector();
			vNew.add(v.get(0));
			HashMap hm = (HashMap) v.get(1);
			HashMap newHm = new HashMap();
			for(int i=0;i<getOptions().length;i++)
				newHm.put(getOptions()[i], ((RFunction)hm.get(getOptions()[i])).clone());
			vNew.add(newHm);
			p.setValue(vNew);
		}
		if(getDefaultValue()!=null){
			Vector v = (Vector) getDefaultValue();
			Vector vNew = new Vector();
			vNew.add(v.get(0));
			HashMap hm = (HashMap) v.get(1);
			HashMap newHm = new HashMap();
			for(int i=0;i<getOptions().length;i++)
				newHm.put(getOptions()[i], ((RFunction)hm.get(getOptions()[i])).clone());
			vNew.add(newHm);
			p.setDefaultValue(vNew);				
		}
		
		return p;
	}
	
	public String[] getParamCalls(){
		String[] calls;
		if(getValue()!=null && !getValue().equals(getDefaultValue())){
			String val="";
			Vector v = (Vector) getValue();
			String fName = (String) v.get(0);
			RFunction rf = (RFunction) ((HashMap)v.get(1)).get(fName);
			if(rf != null)
				val = rf.getCall();
			else
				val = "";
			if(val.length()>0)
				calls = new String[]{getName() + " = "+val};
			else
				calls = new String[]{};
		}else
			calls = new String[]{};
		return calls;
	}
	
	public void setDefaultValue(Object defaultValue) {
		if(defaultValue instanceof Vector || defaultValue ==null)
			this.defaultValue = (Vector) defaultValue;
		else
			System.out.println("ParamRFunction: invalid setDefaultValue");
	}
	
	public Object getDefaultValue() {
		return defaultValue;
	}
	
	public void setValue(Object value) {
		if(value instanceof Vector || value==null)
			this.value = (Vector) value;
		else{
			System.out.println("ParamRFunction: invalid setValue");
			Exception e = new Exception();
			e.printStackTrace();
		}
	}
	
	public Object getValue() {
		return value;
	}
	
	public void addRFunction(String name,RFunction rf){
		if(value==null){
			value = new Vector();
			value.add("");
			value.add(new HashMap());
			setOptions(new String[]{});
		}
		HashMap hm = (HashMap) value.get(1);
		int l = getOptions().length;
		String[] opts = new String[l+1];
		for(int i=0;i<l;i++)
			opts[i] = getOptions()[i];
		opts[l] = name;
		setOptions(opts);
		hm.put(name, rf);
	}
}
